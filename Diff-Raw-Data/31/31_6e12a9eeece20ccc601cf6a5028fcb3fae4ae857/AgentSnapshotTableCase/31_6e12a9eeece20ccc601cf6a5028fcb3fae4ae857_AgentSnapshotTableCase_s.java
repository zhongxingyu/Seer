 package edu.wheaton.simulator.test.statistics;
 
 /**
  * Tests for the AgentSnapshotTable class
  * 
  * @author akonwi & Grant Hensel
  */
 import static org.junit.Assert.assertNotNull;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.AgentID;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.statistics.AgentSnapshot;
 import edu.wheaton.simulator.statistics.AgentSnapshotTable;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 
 @RunWith(JUnit4.class)
 public class AgentSnapshotTableCase {
 
 	AgentSnapshotTable table;
 	ArrayList<AgentSnapshot> snaps;
 	HashSet<AgentID> ids;
 	Prototype prototype;
 	String[] names = new String[] {"bear", "tom", "john", "piglet", "reese"};
 
 	/**
 	 * Make entities and then snapshots to put into the table
 	 */
 	@Before
 	public void setUp() {
 		Grid grid = new Grid(10, 10);
 		snaps = new ArrayList<AgentSnapshot>();
 		ids = new HashSet<AgentID>();
 		prototype = new Prototype("Tester");
 		for(int i = 0; i < 5; i++) {
 			Agent agent = prototype.createAgent(grid);
 			try {
 				agent.addField("name", names[i]);
 				agent.addField("weight", "50");
 			} catch (ElementAlreadyContainedException e) {
 				e.printStackTrace();
 			}
 			ids.add(agent.getID());
 			for(int s = 1; s < 3; s++) {
				snaps.add(new AgentSnapshot(agent.getID(), SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), s, prototype.getName(), null, 0, 0));
 			}
 		}
 		table = null;
 		fillTable();
 	}
 
 	/**
 	 * Helper method to fill the table for tests to use
 	 */
 	public void fillTable() {
 		table = new AgentSnapshotTable();
 		for(AgentSnapshot snap: snaps) {
 			table.putEntity(snap);
 		}
 	}
 
 	@After
 	public void tearDown() {
 		table = null;
 		snaps = null;
 		ids = null;
 	}
 
 	/**
 	 * Test whether the table actually saves records
 	 */
 	@Test
 	public void tableStoresStuff() {
 		assertNotNull("failure-table should not be null", table);
 		for(AgentSnapshot snap: snaps) {
 			table.putEntity(snap);
 		}
 		org.junit.Assert.assertEquals("failure-table.size() should equal 10. it equals " + table.getSize(), table.getSize(), 10);
 	}
 
 	/**
 	 * Test whether the table stores and retrieves records correctly
 	 */
 	@Test
 	public void tableStoresCorrectly() {
 		for(AgentID id : ids) {
 			int s = 1;
 			while(s < 3) {
 				AgentSnapshot snap = table.get(id, s);
 				org.junit.Assert.assertNotNull("failure-Snap should not be null(" + snap+")", snap);
 				s++;
 			}
 		}
 		
 		Grid grid = new Grid(10, 10);
 		Prototype gType = new Prototype("GRID");
 		Integer step = 1;
 		AgentSnapshot gSnap = SnapshotFactory.makeGlobalVarSnapshot(grid, gType, step);
 		table.putEntity(gSnap);
 		org.junit.Assert.assertNotNull("failure-Snap should not be null(" + table.get(Grid.getID(), step) +")", table.get(Grid.getID(), step));
 
 	}
 
 	/*
 	 * Test that table columns are existent and correct
 	 */
 	@Test
 	public void tableChecksForStep() {
 		org.junit.Assert.assertTrue(table.containsStep(1));
 		org.junit.Assert.assertTrue(table.containsStep(2));
 	}
 
 	/*
 	 * Test retrieval of snapshots
 	 */
 	@Test
 	public void getSnapshotsOfEntity() {
 		for(AgentID id : ids) {
 			ImmutableMap<Integer, AgentSnapshot> result = table.getSnapshotsOfEntity(id);
 			System.out.println(result);
 			AgentSnapshot e = result.get(1);
 			org.junit.Assert.assertNotNull("Snapshot(" + e + ") should not be null", e); 
 		}
 	}
 }
