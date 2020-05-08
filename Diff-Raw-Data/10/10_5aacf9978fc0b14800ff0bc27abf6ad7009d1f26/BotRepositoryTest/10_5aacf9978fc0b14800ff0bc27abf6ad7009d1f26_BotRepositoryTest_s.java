 package bots;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
 import org.junit.Test;
 
 import bots.demobots.SimpleBot;
 
 import com.biotools.meerkat.Player;
 
 public class BotRepositoryTest {
 	@Test
 	public void testGetBot() {
 		BotRepository botRepository = new BotRepository();
 		Set<String> botNames = botRepository.getBotNames();
		assertTrue(botNames.contains("SimpleBot/SimpleBot"));
 
		BotMetaData botMetaData = botRepository.getBotMetaData("SimpleBot/SimpleBot");
		assertEquals("SimpleBot/SimpleBot", botMetaData.getBotName());
 		assertEquals("bots.demobots.SimpleBot", botMetaData.getBotClassName());
 		assertNotNull(botMetaData.getBotPreferences());
 	}
 
 	@Test
 	public void testCreateBot() {
 		BotRepository botRepository = new BotRepository();
		Player bot = botRepository.createBot("SimpleBot/SimpleBot");
 		assertNotNull(bot);
 		assertTrue(bot instanceof SimpleBot);
 	}
 }
