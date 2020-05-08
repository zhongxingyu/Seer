 package edu.ncsu.csc216.solitaire.model;
 
 import junit.framework.TestCase;
 
 public class DeckLinkedListTest extends TestCase {
 
 	/**
 	 * A list to run tests on
 	 */
 	private DeckLinkedList testList;
 	
 	/**
 	 * sets up the tests for DeckLinkedList
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		testList = new DeckLinkedList();
 	}
 
 	/**
 	 * Tests the add method
 	 */
 	public void testAdd() {
 		testList.add(5);
 		assertEquals(testList.get(0),5);
 		testList.add(10);
 		assertEquals(testList.get(1),10);
 		testList.add(15);
 		assertEquals(testList.get(2),15);
 	}
 
 	/**
 	 * Tests the addFirst method
 	 */
 	public void testAddFirst() {
 		testList.addFirst(25);
 		assertEquals(testList.get(0), 25);
 		testList.addFirst(2);
 		assertEquals(testList.get(0), 2);
 		testList.addFirst(5);
 		assertEquals(testList.get(0), 5);
 	}
 
 	/**
 	 * Tests the clear method
 	 */
 	public void testClear() {
 		int[] intArr = {4,2,3,5,7};
 		testList.add(intArr);
 		assertEquals(testList.get(3), 5);
 		testList.clear();
 		testList.add(14);
 		assertEquals(testList.get(0), 14);
 	}
 
 	/**
 	 * Tests the concat method
 	 */
 	public void testConcat() {
 		int[] intArr1 = {4,2,3,5,7};
 		int[] intArr2 = {9,15,24,38};
 		
 		DeckLinkedList dll1 = new DeckLinkedList();
 		DeckLinkedList dll2 = new DeckLinkedList();
 		
 		dll1.add(intArr1);
 		dll2.add(intArr2);
 		
 		dll1.concat(dll2);
 		
 		assertEquals(dll1.get(0), 4);
 		assertEquals(dll1.get(4), 7);
 		assertEquals(dll1.get(5), 9);
 		assertEquals(dll1.get(8), 38);
 		assertEquals(dll1.get(9), 4);
 		assertEquals(dll1.get(-1), 38);
 	}
 
 	public void testDetachAt() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		DeckLinkedList dll;
 		
 		dll = new DeckLinkedList();
 		dll.add(intArr);
 		DeckLinkedList dll2 = dll.detachAt(3);
 		assertEquals(dll2.get(0), 10);
 		
 		dll = new DeckLinkedList();
 		dll.add(intArr);
 		dll2 = dll.detachAt(1);
 		assertEquals(dll2.get(0), 6);
 
 		dll = new DeckLinkedList();
 		dll.add(intArr);
 		dll2 = dll.detachAt(0);
 		assertEquals(dll2.get(0), 4);
 	}
 
 	public void testExchange() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		DeckLinkedList dll;
 		
 		dll = new DeckLinkedList();
 		dll.add(intArr);
 		dll.exchange(0,1);
 		assertEquals(dll.get(0), 6);
 
 		dll = new DeckLinkedList();
 		dll.add(intArr);
 		dll.exchange(0,0);
 		assertEquals(dll.get(0), 4);
 		
 		dll = new DeckLinkedList();
 		dll.add(intArr);
 		dll.exchange(2,6);
 		assertEquals(dll.get(2), 16);
 		
 		dll = new DeckLinkedList();
 		dll.add(intArr);
 		dll.exchange(-1,6);
 		assertEquals(dll.get(-1), 16);
 	}
 
 	public void testGet() {
 		int[] intArr = {4,6,8,10,12,14,16};
		DeckLinkedList dll;
 		
		dll = new DeckLinkedList();
		dll.add(intArr);
 		
 		assertEquals(dll.get(0), 4);
 		assertEquals(dll.get(1), 6);
 		assertEquals(dll.get(2), 8);
 		assertEquals(dll.get(3), 10);
 		assertEquals(dll.get(4), 12);
 		assertEquals(dll.get(5), 14);
 		assertEquals(dll.get(6), 16);
 		assertEquals(dll.get(7), 4);
 	}
 
 	public void testIndexOf() {
 		fail("Not yet implemented");
 	}
 
 	public void testRemove() {
 		fail("Not yet implemented");
 	}
 
 	public void testSet() {
 		fail("Not yet implemented");
 	}
 
 	public void testAddLast() {
 		fail("Not yet implemented");
 	}
 
 	public void testRemoveLast() {
 		fail("Not yet implemented");
 	}
 
 }
