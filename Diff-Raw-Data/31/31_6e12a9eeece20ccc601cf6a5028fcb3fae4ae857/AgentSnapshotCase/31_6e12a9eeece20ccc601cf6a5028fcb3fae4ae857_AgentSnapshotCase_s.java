 package edu.wheaton.simulator.test.statistics;
 
 /**
  * A JUnit test case for testing AgentSnapshot.java.
  * 
  * @author Nico Lasta & akonwi
  * Wheaton College, CSCI 335
  * Spring 2013
  * 25 Mar 2013
  */
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.statistics.AgentSnapshot;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 
 @RunWith(JUnit4.class)
 public class AgentSnapshotCase {
 
 	Agent agent;
 	Grid grid;
 	Prototype prototype;
 	Integer step;
 
 	/**
 	 * Initialize variables.
 	 */
 	@Before
 	public void setUp() {
 		grid = new Grid(10, 10);
 		prototype = new Prototype("tester");
 		agent = prototype.createAgent(grid);
 		try {
 			agent.addField("Pig", "Tom");
 			agent.addField("Monkey", "Olly");
 			agent.addField("Cat", "Joomba");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		step = new Integer(23);
 	}
 
 	/**
 	 * Give variables null pointers.
 	 */
 	@After
 	public void tearDown() {
 		agent = null;
 		grid = null;
 		prototype = null;
 		step = null;
 	}
 
 	/**
 	 * Tests to make sure an AgentSnapshot object was successfully created.
 	 */
 	@Test
 	public void agentSnapshotTest() {
 		AgentSnapshot agentSnap = new AgentSnapshot(agent.getID(),
 				SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), step,
				prototype.getName(), null, 0, 0);
 		Assert.assertNotNull("AgentSnapshot not created.", agentSnap);
 	}
 	
 	/**
 	 * Tests the serialize() method 
 	 */
 	@Test
 	public void serializeTest(){
 		AgentSnapshot agentSnap = new AgentSnapshot(agent.getID(),
 				SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), step,
				prototype.getName(), null, 0, 0);
 		
 		String expected = "AgentSnapshot\ntester\n0\n0\nFieldSnapshot Cat Joomba\nFieldSnapshot " +
 				"Pig Tom\nFieldSnapshot Monkey Olly"; 
 		System.out.println(agentSnap.serialize()); 
 		
 		Assert.assertEquals(expected, agentSnap.serialize()); 	
 	}
 	
 	/**
 	 * Tests making a snapshot for global variables
 	 */
 	@Test
 	public void globalVarTest() {
 		Prototype gType = new Prototype("GRID");
 		try {
 			grid.addField("name", "akon");
 			grid.addField("owner", "chris");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		AgentSnapshot gSnap = SnapshotFactory.makeGlobalVarSnapshot(grid, gType, step);
 		
 		String expected = "AgentSnapshot\nGRID\n0\n0\nFieldSnapshot name akon" +
 				"\nFieldSnapshot owner chris"; 
 		System.out.println(gSnap.serialize());
 		
 		org.junit.Assert.assertEquals(expected, gSnap.serialize());
 	}
 }
