 package se.chalmers.kangaroo.model.creature;
 import static org.junit.Assert.*;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import se.chalmers.kangaroo.model.creatures.TurtleCreature;
 import se.chalmers.kangaroo.model.creatures.TurtleCreature;
 import se.chalmers.kangaroo.model.utils.Direction;
 import se.chalmers.kangaroo.model.utils.Position;
 
 
 public class TurtleCreatureTest {
 
	@Test
	public void test() {
		fail("Not yet implemented");
	}
 	
 	@Test
 	public void testUpdateCreature() {
 		TurtleCreature t = new TurtleCreature(new Position(1,1));
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		c.changeState();
 		t.move();
 		c.move();
 		assertTrue(t.getPosition().getX() == 0 && c.getPosition().getX() == 1);
 		
 	}
 	
 	@Test
 	public void testChangeState() {
 		TurtleCreature t = new TurtleCreature(new Position(1,1));
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		t.changeState();
 		c.changeState();
 		c.changeState();
 		assertTrue(!t.isKillable() && c.isKillable());
 	}
 	
 	@Test
 	public void testIsKillable() {
 		TurtleCreature t = new TurtleCreature(new Position(1,1));
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		c.changeState();
 		assertTrue(t.isKillable() && !c.isKillable());
 	}
 	
 	@Test
 	public void testMove() {
 		TurtleCreature t = new TurtleCreature(new Position(1,1));
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		c.changeDirection();
 		t.move();
 		c.move();
 		assertTrue(t.getPosition().equals(new Position(0,1)) && c.getPosition().equals(new Position(2,1)));
 	}
 	
 	@Test
 	public void testGetId() {
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		assertTrue(c.getId() == 112);
 	}
 	
 	@Test
 	public void testGetPosition() {
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		TurtleCreature d = new TurtleCreature(new Position(42,1337));
 		assertTrue(c.getPosition().getX() == 1 && c.getPosition().getY() == 1 && d.getPosition().getX() == 42 && d.getPosition().getY() == 1337);
 	}
 	
 	@Test
 	public void testChangeDirection() {
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		TurtleCreature d = new TurtleCreature(new Position(1,1));
 		d.changeDirection();
 		assertTrue(c.getDirection() == Direction.DIRECTION_WEST && d.getDirection() == Direction.DIRECTION_EAST);
 	}
 	
 	@Test
 	public void testGetDirection() {
 		TurtleCreature c = new TurtleCreature(new Position(1,1));
 		TurtleCreature d = new TurtleCreature(new Position(1,1));
 		d.changeDirection();
 		assertTrue(c.getDirection() == Direction.DIRECTION_WEST && d.getDirection() == Direction.DIRECTION_EAST);
 	}
 
 
 }
