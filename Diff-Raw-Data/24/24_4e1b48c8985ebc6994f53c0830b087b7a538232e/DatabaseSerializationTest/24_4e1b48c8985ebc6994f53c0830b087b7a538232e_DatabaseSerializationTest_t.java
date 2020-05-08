 package testcases;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 import elfville.server.Database;
 import elfville.server.model.*;
 
 public class DatabaseSerializationTest {
 
 	@Test
 	public void testDBrestore() {
 		Database.DB = new Database();
 		
 		Elf e = new Elf();
 		e.setDescription("Likes pork");
 		e.setElfName("Larry");
 		e.save();
 		
 		Clan c = new Clan("Pork Lovers", "Clan of Larry");
 		c.setLeader(e);
 		c.save();
 		
 		try {
 			Database.DB.writeToDisk("/tmp/elfville-test.db");
 		} catch (IOException e1) {
 			fail("Could not save database: " + e1.getMessage());
 		}
 		
 		try {
 			Database.DB = Database.load("/tmp/elfville-test.db");
 			assertEquals("Elfs persist", e.getElfName(), Database.DB.elfDB.findByID(e.getModelID()).getElfName());
 			assertEquals("Clans persist", c.getName(), Database.DB.clanDB.findByModelID(c.getModelID()).getName());
 		} catch (Exception e1) {
 			fail("Could not restore database: " + e1.getMessage());
 		}
 	}
 
 }
