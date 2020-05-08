 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Test;
 
 
 /**
  * @author Jared Moore
  * @version Jan 17, 2013
  */
 public class LinkedListTest {
 
 	/** Method to create a LinkedList filled with data
 	 * @return A LinkedList with data in it
 	 */
 	public LinkedList<Integer> addElements() {
 		
 		LinkedList<Integer> list = new LinkedList<Integer>();
 		ArrayList<Integer> ints = new ArrayList<Integer>();
 		for (int i = 1; i < 8; i++)
 			ints.add(i);
 		list.addAll(ints);
 		return list;
 	}
 	/**
 	 * Test method for {@link LinkedList#contains(java.lang.Object)}.
 	 */
 	@Test
 	public void testContains() {
 		LinkedList<Integer> list = addElements();
 		assertEquals(true, list.contains(1));
 		assertEquals(true, list.contains(5));
 		assertEquals(true, list.contains(7));
 		assertEquals(false, list.contains(12));
 	}
 
 	/**
 	 * Test method for {@link LinkedList#get(int)}.
 	 */
 	@Test
 	public void testGet() {
 		LinkedList<Integer> list = addElements();
 		assertEquals(new Integer(4), list.get(3));
 		assertEquals(new Integer(1), list.get(0));
 		assertEquals(new Integer(7), list.get(list.size() - 1));
 	}
 
 	/**
 	 * Test method for {@link LinkedList#isEmpty()}.
 	 */
 	@Test
 	public void testIsEmpty() {
 		LinkedList<Integer> list = addElements();
 		assertEquals(false, list.isEmpty());
 		list.clear();
 		assertEquals(true, list.isEmpty());
 	}
 
 	/**
 	 * Test method for {@link LinkedList#remove(java.lang.Object)}.
 	 */
 	@Test
 	public void testRemoveObject() {
 		LinkedList<Integer> list = addElements();
 		assertEquals(new Integer(4), list.remove(new Integer(4)));
 		
 		assertEquals(null, list.remove(new Integer(12)));
 	}
 
 	/**
 	 * Test method for {@link LinkedList#set(int, java.lang.Object)}.
 	 */
 	@Test
 	public void testSet() {
 		LinkedList<Integer> list = addElements();
 		assertEquals(list.get(4), list.set(4, new Integer(35)));
 	}
 	
 	/**
 	 * @author Adam Yost
 	 */
 	@Test
 	//@Worth(5) // these annotations are used by us to determine how much a method is worth
 	public void add() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		assertEquals("[ A ]", l1.toString());
 	}
 	
 	@Test
 	//@Worth(5)
 	public void addAll() {
 		List<String> l1 = new LinkedList<>(); 
 		Collection<String> c = new ArrayList<>();
 		c.add("A");
 		c.add("B");
 		c.add("C");
 		c.add("D");
 		l1.addAll(c);
 		//System.out.println(l1);
 		assertEquals("[ A B C D ]", l1.toString());
 	}
 
 	@Test
 	//@Worth(3)
 	public void clear() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		//System.out.println(l1);
 		l1.clear();
 		assertEquals("[ ]", l1.toString());
 	}
 
 	@Test
 	//@Worth(2)
 	public void contains() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		l1.add("B");
 		l1.add("C");
 		assertTrue(l1.contains("B"));
 	}
 	
 	@Test
 	//@Worth(3)
 	public void get() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		l1.add("B");
 		assertEquals("B", l1.get(1));
 	}
 	
 	@Test
 	//@Worth(3)
 	public void indexOf() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		l1.add("B");
 		assertEquals(1,l1.indexOf("B"));
 	}
 	
 	@Test
 	//@Worth(3)
 	public void isEmpty() {
 	List<String> l1 = new LinkedList<>();
 		assertTrue(l1.isEmpty());
 		l1.add("A");
 		l1.add("B");
 		assertFalse(l1.isEmpty());
 	}
 	
 	@Test
 	//@Worth(3)
 	public void removeindex() {
 	List<String> l1 = new LinkedList<>();
 		assertTrue(l1.isEmpty());
 		l1.add("A");
 		l1.add("B");
 		l1.add("C");
 		String e = l1.remove(1);
 		assertEquals("B",e);
 		assertEquals("[ A C ]", l1.toString());
 	}
 	
 	@Test
 	//@Worth(3)
 	public void remove2() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		l1.add("B");
 		l1.add("C");
 		String e = l1.remove("B");
 		assertEquals("B", e);
 		assertEquals("[ A C ]", l1.toString());
 	}
 
 	@Test
 	//@Worth(3)
 	public void set() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		l1.add("B");
 		l1.add("C");
 		l1.set(1, "E");
 		assertEquals("[ A E C ]", l1.toString());
 	}
 
 	@Test
 	//@Worth(3)
 	public void size() {
 		List<String> l1 = new LinkedList<>();
 		l1.add("A");
 		l1.add("B");
 		l1.add("C");
 		assertTrue(l1.size() == 3);
 	}
 
 	@Test
 	//@Worth(3)
 	public void setSize() {
 		LinkedList<String> l1 = new LinkedList<>();
 		l1.add("A");
 		l1.add("B");
 		l1.add("C");
 		l1.setSize(1);
 		assertTrue(l1.size()==1);
 	}
 }
