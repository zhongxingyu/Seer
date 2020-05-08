 package tests;
 
 import static org.junit.Assert.*;
 
 import models.AntWorld;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import util.Position;
 
 import ai.AntBrain;
 import cells.Cell;
 import cells.ClearCell;
 import exceptions.SyntacticallyInvalidInputException;
 import fileio.AntBrainLoader;
 import fileio.SimpleWorldLoader;
 
 public class BrainCommandTests {
 
 	@Test
 	public void TurnRight() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnRight.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 1);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 2);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 3);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 4);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 5);
 	}
 	
 	@Test
 	public void TurnLeft() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnLeft.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 5);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 4);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 3);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 2);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 1);
 	}
 	
 	@Test	//fails because does not run the second command but the first twice !
 	public void TurnLeftNRight() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnLeftRight.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();	//turn Left
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 5);
 		world.update();	//turn right
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 	}
 	
 	@Test
 	public void Move() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/move.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getPosition().getX() == 5);
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getPosition().getY() == 4);
 		world.update();
 		//System.out.println(world);
 		assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().getPosition().getX() == 6);
 		assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().getPosition().getY() == 4);
 	}
 	
 	@Test	//checks resting decreases on each turn until 0
 	public void MoveNRest() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/move.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getPosition().getX() == 5);
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getPosition().getY() == 4);
 		world.update();		//move
 		assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().getPosition().getX() == 6);
 		assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().getPosition().getY() == 4);
		for (int i = 14; i > 0; i--){
 			assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().getResting() == i);
 			world.update();	//rest
 		}
 		world.update();		// on this round move command is executed again
 		assertTrue(((ClearCell)world.getWorld()[7][4]).getAnt().getPosition().getX() == 7);
 		assertTrue(((ClearCell)world.getWorld()[7][4]).getAnt().getPosition().getY() == 4);
 	}
 	
 	@Test
 	public void MoveSucessState() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/moveStateSuccess.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
		assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().getState() == 0);
 	}
 	
 	@Test		//bug found does not switch to failed state !
 	public void MoveFailedState() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/moveStateFail.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and rock.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();				//move failed because rock in the way
 		//System.out.println(world);
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 6);
 	}
 	
 	@Test
 	public void TurnLeft1move() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnLeft1move.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();	//turn Left
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 5);
 		world.update();	//move
 		assertTrue(world.getWorld()[5][3].isOccupied());	//successfully moved
 		assertTrue(((ClearCell)world.getWorld()[5][3]).getAnt().getDirection() == 5);	//direction has not changed
 	}
 	
 	@Test
 	public void TurnLeft2move() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnLeft2move.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();	//turn Left
 		world.update();	//turn Left
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 4);
 		world.update();	//move
 		assertTrue(world.getWorld()[4][3].isOccupied());	//successfully moved
 		assertTrue(((ClearCell)world.getWorld()[4][3]).getAnt().getDirection() == 4);	//direction has not changed
 	}
 	
 	@Test
 	public void TurnLeft3move() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnLeft3move.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();	//turn Left
 		world.update();	//turn Left
 		world.update();	//turn Left
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 3);
 		world.update();	//move
 		assertTrue(world.getWorld()[4][4].isOccupied());	//successfully moved
 		assertTrue(((ClearCell)world.getWorld()[4][4]).getAnt().getDirection() == 3);	//direction has not changed
 	}
 	
 	@Test
 	public void TurnLeft4move() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnLeft4move.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();	//turn Left
 		world.update();	//turn Left
 		world.update();	//turn Left
 		world.update();	//turn Left
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 2);
 		//System.out.println(world);
 		world.update();	//move
 		//System.out.println(world);
 		assertTrue(world.getWorld()[4][5].isOccupied());	//successfully moved
 		assertTrue(((ClearCell)world.getWorld()[4][5]).getAnt().getDirection() == 2);	//direction has not changed
 	}
 	
 	@Test
 	public void TurnLeft5move() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/turnLeft5move.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();	//turn Left
 		world.update();	//turn Left
 		world.update();	//turn Left
 		world.update();	//turn Left
 		world.update();	//turn Left
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 1);	//full 360
 		world.update();	//move
 		assertTrue(world.getWorld()[5][5].isOccupied());	//successfully moved
 		assertTrue(((ClearCell)world.getWorld()[5][5]).getAnt().getDirection() == 1);	//direction has not changed
 	}
 	
 	@Test
 	public void MoveForwardPickup() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/movePickup.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and food.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getDirection() == 0);
 		world.update();	//move
 		assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().getDirection() == 0);
 		world.update();	//pickup
 		//System.out.println(world.printWorld());
 		assertTrue(((ClearCell)world.getWorld()[6][4]).numFood() == 0);	//no food on the cell
 		assertTrue(((ClearCell)world.getWorld()[6][4]).getAnt().hasFood());	//ant has food
 	}
 	
 	@Test
 	public void senseHomeSuccess() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseHomeHere.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);	//brain goes to state 1 if success
 	}
 	
 	@Test
 	public void senseFoodFail() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFoodHere.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/singleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);	//brain goes to state 1 if fail
 	}
 	
 	@Test	//sense food command fails !
 	public void senseFoodAhead() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFoodAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and food.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
		System.out.println(world);
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);
 	}
 	
 	@Test	//sense food command fails !
 	public void senseFoodRightAhead() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFoodRightAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and food right ahead.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		//System.out.println(world.printWorld());
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);
 	}
 	
 	@Test	//sense food command fails !
 	public void senseFoodLeftAhead() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFoodLeftAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and food left ahead.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		//System.out.println(world.printWorld());
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);
 	}
 	
 	@Test
 	public void senseRockAhead() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseRockAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and Rock.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		//System.out.println(world.printWorld());
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);
 	}
 	
 	@Test
 	public void senseFoeAhead() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFoeAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/turnLeft.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and foe.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		//System.out.println(world.printWorld());
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);
 	}
 	
 	@Test
 	public void senseFoeAheadFail() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFoeAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/SingleAnt.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 3);
 	}
 	
 	@Test
 	public void senseFoeHomeAhead() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFoeHomeAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/turnLeft.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and foe.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 3);
 	}
 	
 	@Test
 	public void senseFriendAhead() throws Exception {
 		AntBrain redBrain = AntBrainLoader.load("singleCommandBrain/senseFriendAhead.brain");
 		AntBrain blackBrain = AntBrainLoader.load("singleCommandBrain/no.brain");
 		Cell[][] cells = SimpleWorldLoader.load("working worlds/ant and friend.world");
 		AntWorld world = new AntWorld(cells, redBrain, blackBrain);
 		
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 0);
 		world.update();
 		//System.out.println(world.printWorld());
 		assertTrue(((ClearCell)world.getWorld()[5][4]).getAnt().getState() == 1);
 	}
 	
 	
 }
