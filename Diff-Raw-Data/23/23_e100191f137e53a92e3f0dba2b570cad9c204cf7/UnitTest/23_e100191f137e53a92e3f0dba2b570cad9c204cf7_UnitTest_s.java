 package projectrts.core;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
import projectrts.model.core.Player;
import projectrts.model.core.Position;
import projectrts.model.core.entities.Unit;

 public class UnitTest {
 	/*
 	@Test
 	public void testUnit() {
 		Player player = new Player();
 		Position pos = new Position(15, 13);
 		Unit unit = new Unit(pos, player);
 		
 		assertTrue(pos.equals(unit.getPosition()));
 		assertTrue(player.equals(unit.getOwner()));
 	}
 */
 
 	/* Already tested in constructor
 	@Test
 	public void testGetPosition() {
 		fail("Not yet implemented");
 	}
 	*/
 
 	
 	/*
 	@Test
 	public void testGetSize() {
 		Player player = new Player();
 		Position pos = new Position(15, 13);
 		Unit unit = new Unit(pos, player);
 		
 		assertTrue(unit.getSize() == 1); // Does basically nothing atm
 	}
 
 	@Test
 	public void testGetOwner() {
 		Player player = new Player();
 		Position pos = new Position(15, 13);
 		Unit unit = new Unit(pos, player);
 		
 		assertTrue(player.equals(unit.getOwner()));
 	}
 
 	@Test
 	public void testMove() {
 		Player player = new Player();
 		Position pos = new Position(15, 13);
 		Unit unit = new Unit(pos, player);
 		Position target = new Position(200, 153);
 		
 		int counter = 0;
 		unit.moveTo(target);
 		while (!unit.getPosition().equals(target))
 		{
 			unit.update(1);
 			counter++;
 			
 			assertTrue(counter < 1000);
 		}
 	}
 	*/
 }
