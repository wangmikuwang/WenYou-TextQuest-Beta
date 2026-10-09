package io.wenyou.textquest

import io.wenyou.textquest.data.model.AppBundle
import io.wenyou.textquest.data.model.AppJson
import io.wenyou.textquest.data.model.StoryMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The genre pack widens the built-in stories beyond romance and stays all-ages and self-contained. */
class GenrePresetTest {
    @Test fun genrePackIsAllAgesAndSelfContained() {
        val file = listOf(File("src/beta/assets/presets"), File("app/src/beta/assets/presets")).first(File::isDirectory)
            .resolve("wenyou-genres-presets.json")
        val pack = AppJson.decodeFromString(AppBundle.serializer(), file.readText())
        assertEquals(7, pack.stories.size)
        val ids = pack.characters.map { it.id }.toSet()
        assertTrue(pack.characters.all { !it.adult && it.initial.metrics.isNotEmpty() })
        assertTrue(pack.stories.all { !it.adult && it.characterIds.isNotEmpty() && ids.containsAll(it.characterIds) })
        // Script stories must be playable to an ending.
        for (story in pack.stories.filter { it.mode == StoryMode.SCRIPT }) {
            val reached = mutableSetOf<String>()
            val todo = ArrayDeque(listOf(story.startNodeId))
            while (todo.isNotEmpty()) {
                val id = todo.removeFirst()
                if (!reached.add(id)) continue
                story.nodes[id]!!.choices.forEach { todo += it.next }
            }
            assertEquals(story.nodes.keys, reached)
            assertTrue(reached.any { story.nodes[it]!!.kind == io.wenyou.textquest.data.model.NodeKind.ENDING })
        }
    }
}
