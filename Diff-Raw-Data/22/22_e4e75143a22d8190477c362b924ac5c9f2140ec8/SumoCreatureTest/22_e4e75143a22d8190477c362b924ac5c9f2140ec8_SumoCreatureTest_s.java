 package se.chalmers.kangaroo.model.creature;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import se.chalmers.kangaroo.model.creatures.SumoCreature;
 import se.chalmers.kangaroo.model.utils.Direction;
 import se.chalmers.kangaroo.model.utils.Position;
 
 
 public class SumoCreatureTest {
 
 	@Test
	public void test() {
		fail("Not yet implemented");
	}
	@Test
 	public void testIsKillable() {
 		SumoCreature s = new SumoCreature(new Position(42,1337));
 		assertTrue(!s.isKillable());
 	}
 	
 	@Test
 	public void testIsStomping() {
 		SumoCreature s = new SumoCreature(new Position(1,1));
 		assertTrue(!s.isStomping());
 	}
 	
 	@Test
 	public void testIsJumping() {
 		SumoCreature s = new SumoCreature(new Position(1,1));
 		assertTrue(!s.isJumping());
 		
 	}
 
 	@Test
 	public void testMove() {
 		SumoCreature s = new SumoCreature(new Position(1,1));
 		s.move();
 		assertTrue(s.getPosition().getX() == 1 && s.getPosition().getY() == 1);
 	}
 	
 	@Test
 	public void testGetId() {
 		SumoCreature s = new SumoCreature(new Position(1,1));
 		assertTrue(s.getId() == 115);
 	}
 	
 	@Test
 	public void testGetPosition() {
 		SumoCreature s = new SumoCreature(new Position(1,1));
 		SumoCreature sc = new SumoCreature(new Position(42,1337));
 		assertTrue(s.getPosition().getX() == 1 && s.getPosition().getY() == 1 && sc.getPosition().getX() == 42 && sc.getPosition().getY() == 1337);
 	}
 	
 	@Test
 	public void testChangeDirection() {
 		SumoCreature s = new SumoCreature(new Position(1,1));
 		SumoCreature sc = new SumoCreature(new Position(1,1));
 		sc.changeDirection();
 		assertTrue(s.getDirection() == Direction.DIRECTION_EAST && sc.getDirection() == Direction.DIRECTION_WEST);
 	}
 	
 	@Test
 	public void testGetDirection() {
 		SumoCreature s = new SumoCreature(new Position(1,1));
 		SumoCreature sc = new SumoCreature(new Position(1,1));
 		sc.changeDirection();
 		assertTrue(s.getDirection() == Direction.DIRECTION_EAST && sc.getDirection() == Direction.DIRECTION_WEST);
 	}
 
 }
