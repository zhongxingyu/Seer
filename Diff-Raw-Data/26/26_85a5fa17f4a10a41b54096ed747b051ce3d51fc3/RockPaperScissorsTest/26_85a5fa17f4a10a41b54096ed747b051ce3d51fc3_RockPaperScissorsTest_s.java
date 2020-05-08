 package edu.wheaton.simulator.test.simulation;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
 import edu.wheaton.simulator.simulation.Grid;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 
 public class RockPaperScissorsTest {
 	Grid testGrid;
 	String [] agentType = {"rock", "paper", "scissors"};
 	
 	@Before
 	public void setUp(){
 		this.testGrid = new Grid(100, 100);
 		
 		Expression xMoveRight = new Expression("move('this', this.x + 1, this.y)");		
 		Expression yMoveUp = new Expression("move('this', this.x, this.y + 1)");		
 		Expression xMoveLeft = new Expression("move('this', this.x - 1, this.y})");		
 		Expression yMoveDown = new Expression("move('this', this.x, this.y - 1)");
 		
 		// behavior: turn clockwise
 		Expression turnClockwise = new Expression("setField('this', 'direction', (this.direction +1)%4)");
 		
 		// behavior: turn to match the direction of the agent in front of me and set my type id to his. I know this does not change the name too.
 		Expression changeIDAndTurnAround = new Expression("setField('this', 'direciton', (this.direction +2)%4) && setField('this', 'typeID', (this.typeID -1)%3)");
 		
 		// behavior: change the type of the agent (name) to the type id
 		Expression changeTypeToRock = new Expression("setField('this', 'type', 'rock')");
 		Expression changeTypeToPaper = new Expression("setField('this', 'type', 'paper')");
 		Expression changeTypeToScissors = new Expression("setField('this', 'type', 'scissors')");
 		
 		// condition: if nobody ahead in this direction
 		Expression dir0 = new Expression("(this.direction == 0) && isSlotOpen(this.x,this.y+1)");		
 		Expression dir1 = new Expression("(this.direction == 1) && isSlotOpen(this.x+1,this.y)");
 		Expression dir2 = new Expression("(this.direction == 2) && isSlotOpen(this.x,this.y-1)");
 		Expression dir3 = new Expression("(this.direction == 3) && isSlotOpen(this.x-1,this.y)");
 		
 		// condition: if there is an obstruction ahead (should be checked after checking for conflicts)
 		Expression obstruction0 = new Expression("(this.direction == 0) && !(isSlotOpen(this.x,this.y+1))");		
 		Expression obstruction1 = new Expression("(this.direction == 1) && !(isSlotOpen(this.x+1,this.y))");
 		Expression obstruction2 = new Expression("(this.direction == 2) && !(isSlotOpen(this.x,this.y-1))");
 		Expression obstruction3 = new Expression("(this.direction == 3) && !(isSlotOpen(this.x-1,this.y))");
 		
 		// I am weaker than opponent in front of me condition
 		Expression loseConflict0 = new Expression("(this.direction == 0) && isValidCoord(this.x,this.y+1) && !isSlotOpen(this.x,this.y+1)" +
				" && getFieldOfAgentAt(this.x,this.y+1, typeID) == (this.typeID + 1)%3" +		// agent in front is of type one less than me	
 				" && getFieldOfAgentAt(this.x,this.y+1, 'direction') == (this.direction +2)%4");		// agent in front of me is facing opposite direction from me
 		Expression loseConflict1 = new Expression("(this.direction == 1) && isValidCoord(this.x+1,this.y) && !isSlotOpen(this.x+1,this.y)" +
				" && getFieldOfAgentAt(this.x + 1,this.y, typeID) == (this.typeID + 1)%3" +		// agent in front is of type one less than me	
 				" && getFieldOfAgentAt(this.x + 1,this.y, 'direction') == (this.direction +2)%4");		// agent in front of me is facing opposite direction from me
 		Expression loseConflict2 = new Expression("(this.direction == 2) && isValidCoord(this.x,this.y-1) && !isSlotOpen(this.x,this.y-1)" +
				" && getFieldOfAgentAt(this.x,this.y - 1, typeID) == (this.typeID + 1)%3" +		// agent in front is of type one less than me	
 				" && getFieldOfAgentAt(this.x,this.y - 1, 'direction') == (this.direction +2)%4");		// agent in front of me is facing opposite direction from me
 		Expression loseConflict3 = new Expression("(this.direction == 3) && isValidCoord(this.x-1,this.y) && !isSlotOpen(this.x-1,this.y)" +
				" && getFieldOfAgentAt(this.x - 1,this.y, typeID) == (this.typeID + 1)%3" +		// agent in front is of type one less than me	
 				" && getFieldOfAgentAt(this.x - 1,this.y, 'direction') == (this.direction +2)%4");		// agent in front of me is facing opposite direction from me
 		
 		// condition: if my id says that my typeName does not match my typeId
 		Expression checkId0 = new Expression("this.typeId%3 == 0 && !(this.type == 'rock')");		// my id is rock but m y name is not rock
 		Expression checkId1 = new Expression("this.typeId%3 == 1 && !(this.type == 'paper')");		// paper
 		Expression checkId2 = new Expression("this.typeId%3 == 2 && !(this.type == 'scissors')");		// scissors
 				
 		for(int j = 0; j < agentType.length; j ++){
 			Prototype testPrototype = new Prototype(testGrid, "testPrototype");
 			try {
 				testPrototype.addField("type", agentType[j]);
 				testPrototype.addField("typeID", j);
 				testPrototype.addField("direction", j);
 				testPrototype.addField("initialDIrection", j);
 			} catch (ElementAlreadyContainedException e) {
 				e.printStackTrace();
 			}
 			// Normal moving behavior
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 2, dir0, xMoveRight));
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 2, dir1, yMoveUp));
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 2, dir2, xMoveLeft));
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 2, dir3, yMoveDown));
 			
 			// conflict behavior: lose
 			testPrototype.addTrigger(new Trigger("loseConflict", 1, loseConflict0, changeIDAndTurnAround));
 			testPrototype.addTrigger(new Trigger("loseConflict", 1, loseConflict1, changeIDAndTurnAround));
 			testPrototype.addTrigger(new Trigger("loseConflict", 1, loseConflict2, changeIDAndTurnAround));
 			testPrototype.addTrigger(new Trigger("loseConflict", 1, loseConflict3, changeIDAndTurnAround));
 			// conflict behavior: win (if you win a conflict, then you should not do anything, only end turn)
 
 			// Our ID was changed due to conflict but our name still needs to be updated.
 			testPrototype.addTrigger(new Trigger("updateMyName", 1, checkId0, changeTypeToRock));
 			testPrototype.addTrigger(new Trigger("updateMyName", 1, checkId1, changeTypeToPaper));
 			testPrototype.addTrigger(new Trigger("updateMyName", 1, checkId2, changeTypeToScissors));
 			
 			// There is an obstacle ahead of us so we need to turn
 			
 			for(int i = 0; i < 10; i ++){
 				testGrid.spawnAgent(testPrototype.clonePrototype());
 			}
 		}
 	}
 	@Test
 	public void test() throws SimulationPauseException {
 		//Run through multiple "steps"
 		int numSteps = 100;
 		for(int i = 0; i < numSteps; i++){
 			testGrid.updateEntities();
 		}
 		
 	}
 }
