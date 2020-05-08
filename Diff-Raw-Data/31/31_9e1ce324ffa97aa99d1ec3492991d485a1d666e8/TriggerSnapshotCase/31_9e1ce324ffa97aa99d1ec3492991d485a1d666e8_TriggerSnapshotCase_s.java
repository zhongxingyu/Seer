 package edu.wheaton.simulator.test.statistics;
 
 /**
  * A JUnit test case for testing BehaviorSnapshot.java.
  * 
  * @author Nico Lasta
  * Wheaton College, CSCI 335
  * Spring 2013
  * 11 Apr 2013
  */
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import edu.wheaton.simulator.behavior.AbstractBehavior;
 import edu.wheaton.simulator.behavior.MoveBehavior;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.entity.Trigger.Builder;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 import edu.wheaton.simulator.statistics.TriggerSnapshot;
 
 @RunWith(JUnit4.class)
 public class TriggerSnapshotCase {
 
 	Grid grid;
 	Prototype prototype;
 	Agent actor, recipient;
 	AbstractBehavior behavior;
 	Integer step;
 
 	@Before
 	public void setUp() {
 		grid = new Grid(1, 1);
 		prototype = new Prototype(grid, "BehaviorSnapshotTest");
 		actor = prototype.createAgent();
 		recipient = prototype.createAgent();
 		behavior = new MoveBehavior();
 		step = 23; // arbitrary
 
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void makeSnapshotTest() {
 		Builder builder = new Trigger.Builder(prototype);
 		builder.addBehavioral("behavior");
 		builder.addConditional("conditional");
 		builder.addName("trigger");
 		builder.addPriority(1);
 		
 		Trigger trigger = builder.build();
 		
 		Agent agent = prototype.createAgent();
 		agent.addTrigger(trigger);
 		
		TriggerSnapshot tSnap = SnapshotFactory.makeTriggerSnapshot(agent.getID(), trigger.getName(), trigger.getPriority(), trigger.getConditions().toString(), trigger.getBehavior().toString(), 1);
 		org.junit.Assert.assertNotNull("Trigger Snapshot(" + tSnap + ") shouldn't be null", tSnap);
 	}
 
 }
