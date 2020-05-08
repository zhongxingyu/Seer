 /**
  * ConwaysGameOfLifeTest.java
  * 
  * See wiki for an overview
  */
 
 package edu.wheaton.simulator.test.simulation;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.ExpressionEvaluator;
 import edu.wheaton.simulator.simulation.Grid;
 
 public class ConwaysGameOfLifeTest {
 
 	private Grid testGrid;
 
 	@Before
 	public void setUp() {
 
 		// NOTE: SEE THE WIKI FOR AN OVERVIEW OF THIS
 
 		this.testGrid = new Grid(20, 30);
 
 		Prototype being = new Prototype(testGrid, "Being");
 
 		// Add fields
 		try {
 			being.addField("alive", 0); // 0 for false, 1 for true
 			being.addField("age", 0);
 			being.addField("neighbors", 0);
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		
 		Prototype border = new Prototype(testGrid, "Border");
 		
 		try {
 			border.addField("alive", 0);
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		border.addTrigger(new Trigger("place", 1, new ExpressionEvaluator("1==1"), new ExpressionEvaluator("setField('this','alive',0)")));
 
 		// Set up conditionals
 		ExpressionEvaluator isAlive = new ExpressionEvaluator("#{this.alive} == 1");
 		ExpressionEvaluator neigh1 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}-1, #{this.y}-1, 'alive') == 0");
 		ExpressionEvaluator neigh2 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}, #{this.y}-1, 'alive') == 0");
 		ExpressionEvaluator neigh3 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}+1, #{this.y}-1, 'alive') == 0");
 		ExpressionEvaluator neigh4 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}-1, #{this.y}, 'alive') == 0");
 		ExpressionEvaluator neigh5 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}+1, #{this.y}, 'alive') == 0");
 		ExpressionEvaluator neigh6 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}-1, #{this.y}+1, 'alive') == 0");
 		ExpressionEvaluator neigh7 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}, #{this.y}+1, 'alive') == 0");
 		ExpressionEvaluator neigh8 = new ExpressionEvaluator("getFieldOfAgentAt(#{this.x}+1, #{this.y}+1, 'alive') == 0");
 		ExpressionEvaluator dieCond = new ExpressionEvaluator("(#{this.alive} == 1)&& (#{this.neighbors} < 2 || #{this.neighbors} > 3)");
 		ExpressionEvaluator reviveCond = new ExpressionEvaluator("(#{this.alive} == 0) && (#{this.neighbors} == 0)");
 
 		// Set up behaviors
 		ExpressionEvaluator incrementAge = new ExpressionEvaluator("setField('this', 'age', #{this.age}+1)");
 		ExpressionEvaluator decrementNeighbors = new ExpressionEvaluator("setField('this', 'neighbors', #{this.neighbors}-1)");
 		ExpressionEvaluator die = new ExpressionEvaluator("setField('this', 'alive', 0) && setField('this', 'age', 0) && " +
 				"setField('this', 'colorRed', 255) && setField('this', 'colorGreen', 255) && setField('this', 'colorBlue', 255)");
 		ExpressionEvaluator revive = new ExpressionEvaluator("setField('this', 'alive', 1) && " +
 				"setField('this', 'colorRed', 0) && setField('this', 'colorGreen', 0) && setField('this', 'colorBlue', 0)");
 		ExpressionEvaluator resetNeighbors = new ExpressionEvaluator("setField('this', 'neighbors', 8)");
 
 		// Add triggers
 		being.addTrigger(new Trigger("updateAge", 1, isAlive, incrementAge));
 		being.addTrigger(new Trigger("resetNeighbors", 2, new ExpressionEvaluator("1 == 1"), resetNeighbors));
 		being.addTrigger(new Trigger("checkNeigh1", 3, neigh1, decrementNeighbors));
 		being.addTrigger(new Trigger("checkNeigh2", 4, neigh2, decrementNeighbors));
 		being.addTrigger(new Trigger("checkNeigh3", 5, neigh3, decrementNeighbors));
 		being.addTrigger(new Trigger("checkNeigh4", 6, neigh4, decrementNeighbors));
 		being.addTrigger(new Trigger("checkNeigh5", 7, neigh5, decrementNeighbors));
 		being.addTrigger(new Trigger("checkNeigh6", 8, neigh6, decrementNeighbors));
 		being.addTrigger(new Trigger("checkNeigh7", 9, neigh7, decrementNeighbors));
 		being.addTrigger(new Trigger("checkNeigh8", 10, neigh8, decrementNeighbors));
 		being.addTrigger(new Trigger("die", 4, dieCond, die));
 		being.addTrigger(new Trigger("revive", 4, reviveCond, revive));
 
 		// Make a another prototype that is initially alive
 		Prototype aliveBeing = being;
 		aliveBeing.removeField("alive");
 		try {
 			aliveBeing.addField("alive", "1");
 		} catch (ElementAlreadyContainedException e) {
 			System.err.println("Unable to add \"alive\" field");
 			e.printStackTrace();
 		}
 
 		// Place dead beings in Grid, accept some that are alive
 		for (int x = 1; x < testGrid.getWidth()-1; x++) 
 			for(int y = 1; y < testGrid.getHeight()-1; y++) {
 				if (((x - y) == 0) || x == 4 || x == 5) {
 					testGrid.addAgent(aliveBeing.clonePrototype(), x, y);
 				}
 				testGrid.addAgent(being.clonePrototype(), x, y);
 			}
 		for (int x = 0; x < testGrid.getWidth(); x++) {
 			testGrid.addAgent(border.clonePrototype(), x, 0);
 			testGrid.addAgent(border.clonePrototype(), x, testGrid.getHeight());
 		}
 		for (int y = 0; y < testGrid.getHeight(); y++) {
 			testGrid.addAgent(border.clonePrototype(), 0, y);
 			testGrid.addAgent(border.clonePrototype(), testGrid.getWidth(), y);
 		}
 	}
 
 	@Test
 	public void test() {
 		// Run through multiple "steps"
 		int numSteps = 100;
 		for (int i = 0; i < numSteps; i++) {
 			try {
 				testGrid.updateEntities();
 			} catch(Exception e) {
 				System.out.println(e.getMessage());
 				e.printStackTrace();
 				Assert.fail("Bad Evaluation");
 			}
 		}
 	}
 }
