 package test;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import map.*;
 
 public class TestSimpleHashMap {
 	Map<Integer, Integer> m;
 	Map<Integer, Integer> m16;
 	Map<String, Integer> s;
 	
 
 	@Before
 	public void setUp() throws Exception {
//		m = new SimpleHashMap<Integer, Integer>(10);
//		m16 = new SimpleHashMap<Integer, Integer>();
//		s = new SimpleHashMap<String, Integer>();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		m = null;
 		m16 = null;
 	}
 	
 	@Test
 	public final void testEmpty() {
 		assertTrue("isEmpty false for empty set", m.isEmpty());
 		m.put(1, 1);
 		assertFalse("isEmpty true for non empty set", m.isEmpty());
 	}
 	
 	@Test
 	public final void testPutInEmptyMap() {
 		assertNull("wrong put(1, 1):", m.put(1, 1));
 		assertEquals("wrong size():", 1, m.size());
 		assertEquals("key not found in map: 1", new Integer(1), m.get(1));
 	}
 	
 	@Test
 	public final void testPutSameHashCode() {
 		m16.put(1, 1);
 		m16.put(17, 17);
 		assertEquals("wrong size():", 2, m16.size());
 		assertEquals("key not found in map: 1", new Integer(1), m16.get(1));
 		assertEquals("key not found in map: 17", new Integer(17), m16.get(17));
 	}
 	
 	@Test
 	public final void testDuplicates() {
 		m16.put(1, 1);
 		m16.put(17, 17);
 		assertEquals("wrong put(1, 2):", new Integer(1), m16.put(1, 2));
 		assertEquals("wrong put(17, 18):", new Integer(17), m16.put(17, 18));
 		assertEquals("wrong size():", 2, m16.size());
 		assertEquals("key not found in map: 2", new Integer(2), m16.get(1));
 		assertEquals("key not found in map: 18", new Integer(18), m16.get(17));
 	}
 	
 	@Test
 	public final void testNegative() {
 		m.put(1, 1);
 		m.put(-17, -17);
 		assertEquals("wrong size():", 2, m.size());
 		assertEquals("key not found in map: -17", new Integer(-17), m.get(-17));
 	}
 	
 	@Test
 	public final void testSize() {
 		assertEquals("wrong size():", m.size(), 0);
 		m.put(0, 0);
 		assertEquals("wrong size():", m.size(), 1);
 		for (int i = 1; i < 100; i++) {
 			m.put(i,i);
 		}
 		assertEquals("wrong size():", m.size(), 100);
 	}
 	
 	@Test
 	public final void testGetInEmptyMap() {
 		assertNull("wrong get():", m.get(1));
 	}
 	
 	@Test
 	public final void testGet() {
 		m16.put(1, 1);
 		m16.put(17, 17);
 		m16.put(33, 33);
 		m16.put(49, 49);
 		assertEquals("wrong size():", 4, m16.size());
 		assertEquals("key not found in map: 1", new Integer(1), m16.get(1));    // last or first
 		assertEquals("key not found in map: 17", new Integer(17), m16.get(17)); // middle
 		assertEquals("key not found in map: 33", new Integer(33), m16.get(33)); // middle
 		assertEquals("key not found in map: 49", new Integer(49), m16.get(49)); // first or last
 	}
 	
 	@Test
 	public final void testRemoveInEmptyMap() {
 		assertNull("wrong remove():", m.remove(1));
 	}
 	
 	@Test
 	public final void testRemoveOneElement() {
 		m.put(1, 1);
 		assertNull("wrong remove():", m.remove(2));
 		assertEquals("wrong result from remove: 1", new Integer(1), m.remove(1));
 		assertNull("wrong get():", m.get(1));
 		assertNull("wrong put():", m.put(1, 1));
 	}
 	
 	@Test
 	public final void testRemove() {
 		m16.put(1, 1);
 		m16.put(17, 17);
 		m16.put(33, 33);
 		m16.put(49, 49);
 		assertEquals("wrong size():", 4, m16.size());
 		assertEquals("wrong result from remove: 1", new Integer(49), m16.remove(49)); // first or last
 		assertEquals("wrong result from remove: 1", new Integer(17), m16.remove(17)); // middle?
 		assertEquals("wrong result from remove: 1", new Integer(33), m16.remove(33)); // middle?
 		assertEquals("wrong result from remove: 1", new Integer(1), m16.remove(1));   // last or first
 		assertEquals("wrong size():", 0, m16.size());
 		assertNull("wrong get():", m.get(1));
 		assertNull("wrong get():", m.get(17));
 		assertNull("wrong get():", m.get(133));
 		assertNull("wrong get():", m.get(49));
 	}
 	
 	@Test
 	public final void testRemoveNonExistingElements() {
 		m16.put(1, 1);
 		m16.put(17, 17);
 		m16.put(33, 33);
 		assertEquals("wrong size():", 3, m16.size());
 		assertNull("wrong result from remove: 49", m16.remove(49)); // non-existing element in non-empty list
 		assertEquals("wrong size():", 3, m16.size());
 		assertNull("wrong result from remove: 2", m16.remove(2)); // non-existing element in empty list
 		assertEquals("wrong size():", 3, m16.size());
 		assertEquals("key not found in map: 1", new Integer(1), m16.get(1));
 		assertEquals("key not found in map: 17", new Integer(17), m16.get(17));
 		assertEquals("key not found in map: 33", new Integer(33), m16.get(33));
 	}
 	
 	@Test
 	public final void testManyPutAndGet() {
 		// Random, spara tal i vektor, prova get och remove (i egen metod)
 		java.util.Random random = new java.util.Random(123456);
 		ArrayList<Integer> randNbrs = new ArrayList<Integer>();
 		for (int i = 0; i < 100; i++) {
 			int r = random.nextInt(10000);			
 			m16.put(r, r);
 			randNbrs.add(r);
 		}
 		for (int i : randNbrs) {			
 			assertEquals("key not found in map:" + i, new Integer(i), m16.get(i));
 		}
 	}
 	
 	@Test
 	public final void testStringKeys() {
 		s.put("abc", 0);
 		s.put("def", 1);
 		s.put("ghi", 2);
 		s.put("jkl", 3);
 		s.put("abc", 4);
 		assertEquals("wrong size():", 4, s.size());
 		// Uses new String to make sure that target keys are different objects than those in the map.
 		assertEquals("key not found in map: 1", new Integer(4), s.get(new String("abc")));
 		assertEquals("key not found in map: 17", new Integer(1), s.get(new String("def")));
 		assertEquals("key not found in map: 33", new Integer(2), s.get(new String("ghi")));
 		assertEquals("key not found in map: 49", new Integer(3), s.get(new String("jkl")));
 
 	}
 }
 
