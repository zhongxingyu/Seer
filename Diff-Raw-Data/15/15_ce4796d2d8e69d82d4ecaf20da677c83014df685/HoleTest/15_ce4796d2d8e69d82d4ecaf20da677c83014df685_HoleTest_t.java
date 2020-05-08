 package se.chalmers.dryleafsoftware.androidrally.model.gameBoard;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 public class HoleTest {
 
 	@Test
 	public void testInstantAction() {
 		Hole hole = new Hole();
 		Robot robot = new Robot(5, 5);
 		robot.newSpawnPoint();
 		
 		robot.move(2, GameBoard.NORTH);
 		robot.move(3, GameBoard.WEST);
 		
 		assertTrue(robot.getLife() == 3);
 		
		robot.setX(robot.getSpawnPointX());
		robot.setY(robot.getSpawnPointY());
		
 		hole.instantAction(robot);
 		assertTrue(robot.getLife() == 2);
		assertTrue(robot.getX() == 5);
		assertTrue(robot.getY() == 5);
 	}
 
 }
