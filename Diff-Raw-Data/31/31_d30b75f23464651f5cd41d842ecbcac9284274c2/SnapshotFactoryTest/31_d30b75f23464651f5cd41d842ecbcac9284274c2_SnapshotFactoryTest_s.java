 package edu.wheaton.simulator.test.statistics;
 
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableMap;
 
import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.EntityID;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.PrototypeID;
 import edu.wheaton.simulator.statistics.AgentSnapshot;
import edu.wheaton.simulator.statistics.EntitySnapshot;
 import edu.wheaton.simulator.statistics.FieldSnapshot;
 import edu.wheaton.simulator.statistics.SnapshotFactory;
 
 public class SnapshotFactoryTest {
 
 	Grid g; 
 	
 	@AfterClass
 	public static void tearDownAfterClass() {
 		//Nothing to do here
 	}
 
 	@Before
 	public void setUp() {
 		g = new Grid(10, 10);
 	}
 
 	@After
 	public void tearDown() {
 		//Nothing to do here
 	}
 
 	@Test
 	public void testMakeAgentSnapshot() {
 		Prototype p = new Prototype(g, "bear"); 
 		Agent a = new Agent(g, p);
 		PrototypeID pID = p.getPrototypeID();
 		EntityID aID = a.getEntityID(); 
 		Integer step = 15; 
 		
 		AgentSnapshot ass = SnapshotFactory.makeAgentSnapshot(a, step);
 		
 		Assert.assertNotNull(ass.entityID);
 		Assert.assertNotNull(ass.fields);
 		Assert.assertNotNull(ass.prototype);
 		Assert.assertNotNull(ass.step);
 		Assert.assertEquals(ass.entityID, aID);
 		Assert.assertEquals(ass.prototype, pID);
 		Assert.assertEquals(ass.step, step);
 	}
 
 	@Test (expected = UnsupportedOperationException.class)
 	public void testMakeFieldSnapshot() {
 		FieldSnapshot fss1 = SnapshotFactory.makeFieldSnapshot("foo", "'bar'");
 		FieldSnapshot fss2 = SnapshotFactory.makeFieldSnapshot("int", "1");
 		FieldSnapshot fss3 = SnapshotFactory.makeFieldSnapshot("double", "2.0");
 				
 		Assert.assertNotNull(fss1.value);
 		Assert.assertNotNull(fss2.value);
 		Assert.assertNotNull(fss3.value);
 		Assert.assertEquals(fss1.name, "foo");
 		Assert.assertEquals(fss2.name, "int");
 		Assert.assertEquals(fss3.name, "double");
 		Assert.assertNotNull(fss1.name);
 		Assert.assertNotNull(fss2.name);
 		Assert.assertNotNull(fss3.name);
 		Assert.assertFalse(fss1.isNumber);
 		Assert.assertTrue(fss2.isNumber);
 		Assert.assertTrue(fss3.isNumber);
 		Assert.assertEquals(fss1.value, "'bar'");
 		Assert.assertEquals(fss2.value, "1");
 		Assert.assertEquals(fss3.value, "2.0");
 		Assert.assertEquals(fss2.getNumericalValue(), 1.0);
 		Assert.assertEquals(fss3.getNumericalValue(), 2.0);
 		//Raise exception.
 		fss1.getNumericalValue();
 	}
 
 	@Test (expected = UnsupportedOperationException.class)
 	public void testMakeFieldSnapshots() {
 		String name1 = "a";
 		String name2 = "b";
 		String name3 = "c"; 
 		String value1 = "'1'";
 		String value2 = "2"; 
 		String value3 = "3.0"; 
 		boolean value1IsNumber = false;  
 		boolean value2IsNumber = true; 
 		boolean value3IsNumber = true; 
 		double value2Num = 2.0; 
 		double value3Num = 3.0; 
 		
 		HashMap<String, String> map = new HashMap<String, String>(); 
 		map.put(name1, value1);
 		map.put(name2, value2);
 		map.put(name3, value3);
 		ImmutableMap<String, FieldSnapshot> fieldMap = SnapshotFactory.makeFieldSnapshots(map);
 		
 		Assert.assertNotNull(fieldMap.get(name1));
 		Assert.assertNotNull(fieldMap.get(name2));
 		Assert.assertNotNull(fieldMap.get(name3));
 
 		Assert.assertEquals(fieldMap.get(name1).name, name1);
 		Assert.assertEquals(fieldMap.get(name2).name, name2);
 		Assert.assertEquals(fieldMap.get(name3).name, name3);
 		
 		Assert.assertEquals(fieldMap.get(name1).value, value1);
 		Assert.assertEquals(fieldMap.get(name2).value, value2);
 		Assert.assertEquals(fieldMap.get(name3).value, value3);
 
 		Assert.assertEquals(fieldMap.get(name1).isNumber, value1IsNumber);
 		Assert.assertEquals(fieldMap.get(name2).isNumber, value2IsNumber);
 		Assert.assertEquals(fieldMap.get(name3).isNumber, value3IsNumber);
 
 		Assert.assertEquals(fieldMap.get(name2).getNumericalValue(), value2Num);
 		Assert.assertEquals(fieldMap.get(name3).getNumericalValue(), value3Num);
 		
 		fieldMap.get(name1).getNumericalValue();
 	}
 
 	@Test
 	public void testMakePrototypeSnapshot() {
 		fail("Not yet implemented");
 	}
 
 }
