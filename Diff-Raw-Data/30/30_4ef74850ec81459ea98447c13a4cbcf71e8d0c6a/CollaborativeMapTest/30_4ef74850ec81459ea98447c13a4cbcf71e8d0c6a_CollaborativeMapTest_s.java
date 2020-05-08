 package gx.realtime;
 
 import static org.junit.Assert.*;
 
 import java.util.AbstractMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CollaborativeMapTest {
 
 	private static TestObject simpleObject;
 	private static TestObject testObject;
 	private static CollaborativeMap<TestObject> map;
 	
 	@Before
 	public void setUp(){
		map = new CollaborativeMap<TestObject>(null);
 		simpleObject = new TestObject(123);
 		testObject = new TestObject(456, simpleObject);
 	}
 	
 	@Test
 	public void testCollaborativeMap(){
		CollaborativeMap<TestObject> map = new CollaborativeMap<TestObject>(null);
 		assertEquals(0, map.size());
 		assertTrue(map.isEmpty());
 	}
 	
 	@Test
 	public void testItems(){
 		assertTrue(map.items().isEmpty());
 		
 		map.set("123", simpleObject);
 		map.set("456", testObject);
 		
 		assertEquals(2, map.size());
 		assertFalse(map.isEmpty());
 		
 		Set<Entry<String, TestObject>> items = map.items();
 		assertEquals(2, items.size());
 
 		//check if they are actually clones
 		Iterator<Entry<String, TestObject>> iterator = items.iterator();
 		Entry<String, TestObject> obj1 = iterator.next();
 		Entry<String, TestObject> obj2 = iterator.next();
 		
 		if(obj1.getKey().equals("123")){
 			assertEquals(simpleObject, obj1.getValue());
 			assertEquals(testObject, obj2.getValue());
 			assertFalse(simpleObject == obj1.getValue());
 			assertFalse(testObject == obj2.getValue());
 		} else {
 			assertEquals(testObject, obj1.getValue());
 			assertEquals(simpleObject, obj2.getValue());
 			assertFalse(testObject == obj1.getValue());
 			assertFalse(simpleObject == obj2.getValue());
 		}
 	}
 	
 	@Test
 	public void testValues(){
 		assertEquals(0, map.values().size());
 		
 		map.set("123", simpleObject);
 		map.set("456", testObject);
 		
 		List<TestObject> values = map.values();
         System.out.println("List: " + values);
 		assertEquals(2, values.size());
 
 		TestObject obj1 = values.get(0);
 		TestObject obj2 = values.get(1);
 		if(obj1.getId() == 123){
             //obj1 =  simpleObject, obj2 = testObject
 			assertEquals(simpleObject, obj1);
 			assertEquals(testObject, obj2);
 			assertFalse(simpleObject == obj1);
 			assertFalse(testObject == obj2);
 		} else if(obj1.getId() == 456) {
             //obj1 = testObject, obj2 = simpleObject
 			assertEquals(testObject, obj1);
 			assertEquals(simpleObject, obj2);
 			assertFalse(testObject == obj1);
 			assertFalse(simpleObject == obj2);
 		} else {
             fail("Unknown element found in list");
         }
 	}
 }
