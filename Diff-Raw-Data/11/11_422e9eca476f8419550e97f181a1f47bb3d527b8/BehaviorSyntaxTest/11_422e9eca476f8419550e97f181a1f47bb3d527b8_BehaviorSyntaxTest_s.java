 package edu.wheaton.simulator.test.behavior;
 
import static org.junit.Assert.*;

 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 
 /**
  * Simple tests to verify the syntax of important hard coded behaviors
  * 
  * @author David Emmanuel
  *
  */
 
 public class BehaviorSyntaxTest {
 
 	Grid testGrid;
 	Prototype proto;
 	/**
 	 * Makes a cat at position 5, 5 with the field of health set to 100
 	 * 
 	 * @throws ElementAlreadyContainedException
 	 */
 	@Before
 	public void setUp() throws ElementAlreadyContainedException {
 		testGrid = new Grid(20, 20);
 		proto = new Prototype(testGrid, "cat");
 		proto.addField("health", 100);
 		testGrid.spawnAgent(proto.createAgent(), 5, 5);
 	}
 	
 	@Test
 	public void testCloneAgentAtPositionBehavior() throws SimulationPauseException {
 		Expression cloneAgentAtPosition = new Expression("cloneAgentAtPosition('this', 5, 5, 6 , 6)");
 		Expression alwaysTrue = new Expression("1 < 2");
 		proto.addTrigger(new Trigger("cloneAgentAtPosition", 1, alwaysTrue, cloneAgentAtPosition));
 		testGrid.updateEntities();
 		Assert.assertFalse(testGrid.emptySlot(6, 6));
 	}
 	
 	@Test
 	public void testCloneBehavior() throws SimulationPauseException {
 		Expression clone = new Expression("clone('this', 6 , 6)");
 		Expression alwaysTrue = new Expression("1 < 2");
 		proto.addTrigger(new Trigger("clone", 1, alwaysTrue, clone));
 		testGrid.updateEntities();
 		Assert.assertFalse(testGrid.emptySlot(6, 6));
 	}
 
 	@Test
 	public void testDieBehavior() throws SimulationPauseException {
 		// there is an agent at 5, 5
 		Assert.assertFalse(testGrid.emptySlot(5, 5));	
 
 		Expression die = new Expression("die('this')");
 		Expression alwaysTrue = new Expression("1 < 2");
 		proto.addTrigger(new Trigger("clone", 1, alwaysTrue, die));
 		testGrid.updateEntities();
 		// agent is removed from the grid
 		Assert.assertTrue(testGrid.emptySlot(5, 5));	
 		}
 
 	@Test
 	public void testMoveBehavior() throws SimulationPauseException {
 		// one agent was spawned at 5, 5
 		Assert.assertFalse(testGrid.emptySlot(5, 5));
 		// 6, 6 is open for an agent to move there
 		Assert.assertTrue(testGrid.emptySlot(6, 6));
 		Expression move = new Expression("move('this', 6 , 6)");
 		Expression alwaysTrue = new Expression("1 < 2");
 		proto.addTrigger(new Trigger("move", 1, alwaysTrue, move));
 		testGrid.updateEntities();
 		Assert.assertTrue(testGrid.emptySlot(5, 5));
 		Assert.assertFalse(testGrid.emptySlot(6, 6));	
 		}
 	
 	@Test
 	public void testSetFieldBehavior() throws SimulationPauseException {
 		// one agent was spawned at 5, 5
 		Assert.assertFalse(testGrid.emptySlot(5, 5));
 		// health starts at 100
 		Assert.assertEquals(testGrid.getAgent(5, 5).getFieldValue("health") + "", "100");
 		Expression setField = new Expression("setField('this', 'health' , 50)");
 		Expression alwaysTrue = new Expression("1 < 2");
 		proto.addTrigger(new Trigger("setAgent", 1, alwaysTrue, setField));
 		testGrid.updateEntities();
 		Assert.assertEquals(testGrid.getAgent(5, 5).getFieldValue("health"), "50.0");
 	}
 	
 	@Test
 	public void testSetFieldOfAgentBehavior() throws SimulationPauseException {
 		// one agent was spawned at 5, 5
 		Assert.assertFalse(testGrid.emptySlot(5, 5));
 		// spawn another agent at 4, 4 and make sure that it spawned
 		Assert.assertTrue(testGrid.emptySlot(4, 4));
 		testGrid.spawnAgent(proto.createAgent(), 4, 4);
 		Assert.assertFalse(testGrid.emptySlot(4, 4));
 		// health starts at 100
 		Assert.assertEquals(testGrid.getAgent(4, 4).getFieldValue("health") + "", "100");
 		Expression setFieldOfAgent = new Expression("setFieldOfAgent('this', 4, 4, 'health', 50)");
 		Expression alwaysTrue = new Expression("1 < 2");
 		proto.addTrigger(new Trigger("setFieldOfAgent", 1, alwaysTrue, setFieldOfAgent));
 		testGrid.updateEntities();
 		Assert.assertEquals(testGrid.getAgent(4, 4).getFieldValue("health"), "50.0");	
 	}
 }
