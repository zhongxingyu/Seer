 package edu.wheaton.simulator.test.statistics;
 
 /**
  * A JUnit test case for testing StatisticsManager.java.
  * 
  * @author Grant Hensel
  * Wheaton College, CSCI 335
  * Spring 2013
  * 25 Mar 2013
  */
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.AgentID;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.statistics.AgentSnapshot;
 import edu.wheaton.simulator.statistics.PrototypeSnapshot;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 
 public class StatisticsManagerTest {
 
 	StatisticsManager sm;
 	Grid g; 
 	String categoryName;
 	Grid grid;
 	Prototype prototype;
 	HashMap<String, String> fields;
 	int population;
 	ImmutableSet<AgentID> children;
 	Integer step;
 	PrototypeSnapshot protoSnap;
 	PrototypeSnapshot protoSnap2;
 
 	@Before
 	public void setUp() {
 		sm = new StatisticsManager();
 		g = new Grid(10,  10); 		
 
 		//Add a test PrototypeSnapshot
 		categoryName = "testing";
 		grid = new Grid(10, 10);
 		prototype = new Prototype(grid, "tester");
 		fields = new HashMap<String, String>();
 		population = 50;
 		children = prototype.childIDs();
 		step = new Integer(1);
 
 		protoSnap = new PrototypeSnapshot(categoryName, SnapshotFactory.makeFieldSnapshots(fields), population, children, step); 
 
 		categoryName = "testing2";
 		prototype = new Prototype(grid, "tester2");
 		population = 40;
 		step = new Integer(2);
 
 		//Add another test PrototypeSnapshot
 		protoSnap2 = new PrototypeSnapshot(categoryName,
 				SnapshotFactory.makeFieldSnapshots(fields), population,
 				children, step);
 	}
 
 	@After
 	public void tearDown() {
 		//Nothing to do here
 	}
 
 	@Test
 	public void testStatisticsManager() { 
 		Assert.assertNotNull("Constructor failed", sm);
 	}
 
 	@Test
 	public void testGetGridObserver() {
 		Assert.assertNotNull("Failed to get GridObserver", sm.getGridObserver()); 
 	}
 
 	@Test
 	public void testAddPrototypeSnapshot() {
 		Prototype p = new Prototype(g, "TestPrototype"); 
 		Assert.assertNotNull(p); 
 		
 		PrototypeSnapshot protoSnap = new PrototypeSnapshot("categoryname",
 				SnapshotFactory.makeFieldSnapshots(new HashMap<String, String>()), 100, p.childIDs(), new Integer(2)); 
 		sm.addPrototypeSnapshot(protoSnap);
 	}
 	
 	@Test
 	public void testGetPopVsTime() {
 		sm.addPrototypeSnapshot(protoSnap); 
 
 		//Create data for a test simulation with a random number of steps
 		//and random population in each step
 		Random R = new Random(); 
 		int numSteps = R.nextInt(10) + 2;
 		int[] expected = new int[numSteps];
 
 		//Populate the test "expected" array and add the data to the StatsManager
 		for(int i = 0; i < numSteps; i++){
 			expected[i] = R.nextInt(100);
 
 			//Add the appropriate AgentSnapshots to the StatsManager
 			for(int pop = 0; pop < expected[i]; pop++){
 				Agent agent = prototype.createAgent();
 				sm.addGridEntity(new AgentSnapshot(agent.getID(), 
 						SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), 
 						i, protoSnap.categoryName));
 			}
 		}
 		
 		int[] result = sm.getPopVsTime(protoSnap.categoryName);
 		System.out.println("\ngetPopVsTime()"); 
 		System.out.println("Expected: " + display(expected));
 		System.out.println("Result: " + display(result));
 		Assert.assertArrayEquals(expected, result); 
 	}
 
 	/**
 	 * Display the contents of an int array
 	 * @param array The array to display
 	 * @return The display string
 	 */
	private static String display(int[] array) {
 		String ret = "["; 
 		for(int x : array){
 			ret += x + ", ";  
 		}
 
 		if(array.length > 0)
 			ret = ret.substring(0, ret.length()-2);
 
 		return ret + "]";
 	}
 
 	@Test
 	public void testGetAvgFieldValue() {
 		ArrayList<AgentSnapshot> snaps = new ArrayList<AgentSnapshot>();
 		HashSet<AgentID> ids = new HashSet<AgentID>();
 		String[] names = new String[] {"bear", "tom", "john", "piglet", "reese"};
 
 		/* create snapshots */
 		for(int i = 0; i < 5; i++) {
 			Agent agent = prototype.createAgent();
 			try {
 				agent.addField("name", names[i]);
 				agent.addField("weight", "10");
 			} catch (ElementAlreadyContainedException e) {
 				e.printStackTrace();
 			}
 			ids.add(agent.getID());
 			for(int s = 1; s < 3; s++) {
 				snaps.add(new AgentSnapshot(agent.getID(), SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), s, protoSnap.categoryName));
 			}
 		}
 
 		/* fill table w/ snapshots */
 		for(AgentSnapshot snap: snaps) {
 			sm.addGridEntity(snap);
 		}
 
 		/* test method */
 		double[] avg = sm.getAvgFieldValue(protoSnap.categoryName, "weight");
 		for(double i : avg) {
 			int a = (int) i;
 			org.junit.Assert.assertEquals(10, a);
 		}
 	}
 
 	@Test
 	public void testGetAvgLifespan() {
 		ArrayList<AgentSnapshot> snaps = new ArrayList<AgentSnapshot>();
 
 		//Generate lifespans for each agent that will be inserted. 
 		Random R = new Random(); 
 		ArrayList<Integer> lifespans = new ArrayList<Integer>(); 
 		int numAgents = R.nextInt(41);
 		for(int i = 0; i < numAgents; i++){
 			lifespans.add(R.nextInt(100)); 
 		} 
 
 		/* create snapshots */
 		for(int i = 0; i < lifespans.size(); i++) {
 			Agent agent = prototype.createAgent();
 			AgentID ID = agent.getID(); 
 
 			//For each agent, add a snapshot of it at each Step it was alive
 			for(int step = 0; step <= lifespans.get(i); step++) {
 				snaps.add(new AgentSnapshot(ID, SnapshotFactory.makeFieldSnapshots(agent.getCustomFieldMap()), step, protoSnap.categoryName));
 			}
 		}
 
 		/* fill table w/ snapshots */
 		for(AgentSnapshot snap: snaps) {
 			sm.addGridEntity(snap);
 		}		
 
 		double actual = sm.getAvgLifespan(protoSnap.categoryName);
 		double expected = average(lifespans); 
 		System.out.println("\nGetAvgLifesppan()"); 
 		System.out.println("Expected: " + expected);
 		System.out.println("Actual:   " + actual);
 		Assert.assertEquals((int)expected, (int)actual); 
 	}
 
 	/**
 	 * Calculate the average of the values in an int array
 	 * @param array The array of integer values
 	 * @return The average of the values in the given array
 	 */
	private static double average(ArrayList<Integer> list){
 		double avg = 0.0; 
 		for(int i : list)
 			avg += i; 
 		return avg / list.size(); 
 	}
 
 }
