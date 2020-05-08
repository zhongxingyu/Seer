 package projectrts.core;
 
import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
import projectrts.model.player.Player;
import projectrts.model.utils.Position;
 
 public class PlayerTest {
 
 	
 	// This test requires the player to instantiate a unit in the constructor
 	@Test
 	public void testSelect() {
 		Position onUnit = new Position(10, 10);
 		Position closeToUnit = new Position(9.5f, 9.5f);
 		Position farFromUnit = new Position(5, 5);
 		Player player = new Player();
		/*
		 * TODO  FIX!!!!
 		assertTrue(player.getSelectedEntities().size() == 0);
 		
 		player.select(onUnit);
 		assertTrue(player.getSelectedEntities().size() != 0);
 		
 		player = new Player();
 		player.select(closeToUnit);
 		assertTrue(player.getSelectedEntities().size() != 0);
 		
 		player = new Player();
 		player.select(farFromUnit);
 		assertTrue(player.getSelectedEntities().size() == 0);
		*/
 	}
 
 	//TODO Jakob: Fix with EntityManager
 	/*
 	@Test
 	public void testMoveSelected() {
 		
 		Position onUnit = new Position(10, 10);
 		Position target = new Position(25, 35);
 		
 		Player player = new Player();
 		player.select(onUnit);
 		player.moveSelectedTo(target);
 		
 		
 		
 		int counter = 0;
 		while (!player.getUnits().get(0).getPosition().equals(target))
 		{
 			player.update(1);
 			counter++;
 			
 			assertTrue(counter < 1000);
 		}
 	}
 	*/
 }
