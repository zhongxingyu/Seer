 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author Jared Moore
  * @version Feb 20, 2013
  */
 public class HashTableTest {
 
 	@Test
 	public void testSimplePut() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 
 		assertEquals(
 				"[null, one, two, three, four, null, null, null, null, null, null]",
 				table.toString());
 	}
 
 	@Test
 	public void testSingleOverlap() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one again");
 
 		assertEquals(
 				"[null, one, two, three, four, one again, null, null, null, null, null]",
 				table.toString());
 	}
 
 	@Test
 	public void testSimpleResize() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one again");
 		table.put(6, "six");
 		table.put(7, "seven");
 		table.put(8, "eight");
 
 		assertEquals(
 				"[null, one, two, three, four, null, six, seven, eight, null, null, null, one again, null, null, null, null, null, null, null, null, null, null]",
 				table.toString());
 	}
 
 	@Test
 	public void testWrapAround() {
 
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one again");
 		table.put(10, "ten");
 		table.put(21, "ten again");
 
 		assertEquals(
 				"[ten again, one, two, three, four, one again, null, null, null, null, ten]",
 				table.toString());
 	}
 
 	@Test
 	public void testReplace() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		assertEquals("one", table.put(1, "one again"));
 
 		assertEquals(
 				"[null, one again, two, three, four, null, null, null, null, null, null]",
 				table.toString());
 	}
 
 	@Test
 	public void testGet() {
 
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one again");
 		table.put(10, "ten");
 		table.put(21, "ten again");
 
 		assertEquals("ten", table.get(10));
 		assertEquals("ten again", table.get(21)); // same hash value as previous
 	}
 
 	@Test
 	public void testContains() {
 
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one again");
 		table.put(10, "ten");
 		table.put(21, "ten again");
 		table.put(7, null);
 
 		assertEquals(true, table.containsKey(10));
 		assertEquals(true, table.containsKey(21));
 		assertEquals(true, table.containsKey(7));
 	}
 
 	@Test
 	public void testRemove() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one again");
 
 		table.remove(1);
 		assertEquals(
 				"[null, hidden, two, three, four, one again, null, null, null, null, null]",
 				table.toString()); // my toString prints "hidden" if the entry
 									// has been removed
 		
 		table.put(5, null);
 		table.remove(5);
 		assertEquals(
 				"[null, hidden, two, three, four, one again, hidden, null, null, null, null]",
 				table.toString()); 
 	}
 	
 	@Test
 	public void testValues() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one");
 		
		assertEquals("[one, two, three, four]", table.values().toString());
 	}
 	
 	@Test
 	public void testKeys() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one"); // You cannot really get duplicates, so that cannot be tested
 		
 		assertEquals("[1, 2, 3, 4, 12]", table.keySet().toString());
 	}
 	
 	@Test
 	public void testEntries() {
 		HashTable<Integer, String> table = new HashTable<Integer, String>();
 		table.put(1, "one");
 		table.put(2, "two");
 		table.put(3, "three");
 		table.put(4, "four");
 		table.put(12, "one");
 		
 		assertEquals(5, table.entrySet().size()); // cannot test order as order is not preserved
 	}
 }
