 package edu.ncsu.csc216.solitaire.model;
 
 import java.util.NoSuchElementException;
 
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
 		try {
 			assertEquals(dll1.get(9), 4);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		try {
 			assertEquals(dll1.get(-1), 38);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 	}
 
 	/**
 	 * Tests the detachAt method
 	 */
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
 
 	/**
 	 * Tests the exchange method
 	 */
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
 
 		try {
 			dll = new DeckLinkedList();
 			dll.add(intArr);
 			dll.exchange(-1,6);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 	}
 
 	/**
 	 * Tests the get method
 	 */
 	public void testGet() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		
 		DeckLinkedList dll = new DeckLinkedList();
 		
 		dll.add(intArr);
 
 		assertEquals(dll.get(0), 4);
 		assertEquals(dll.get(1), 6);
 		assertEquals(dll.get(2), 8);
 		assertEquals(dll.get(3), 10);
 		assertEquals(dll.get(4), 12);
 		assertEquals(dll.get(5), 14);
 		assertEquals(dll.get(6), 16);
 		
 		try {
 			dll.get(7);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 		try {
 			dll.get(8);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 		try {
 			dll.get(30);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 		try {
 			dll.get(-1);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 		try {
 			dll.get(-2);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 		try {
 			dll.get(-50);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 	}
 
 	/**
 	 * Tests the indexOf method
 	 */
 	public void testIndexOf() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		
 		DeckLinkedList dll = new DeckLinkedList();
 		
 		dll.add(intArr);
 		
 		assertEquals(dll.indexOf(4), 0);
 		assertEquals(dll.indexOf(6), 1);
 		assertEquals(dll.indexOf(8), 2);
 		assertEquals(dll.indexOf(10), 3);
 		assertEquals(dll.indexOf(12), 4);
 		assertEquals(dll.indexOf(14), 5);
 		assertEquals(dll.indexOf(16), 6);
 		
 		try {
 			dll.indexOf(-50);
 			fail("Should have thrown exception");
 		} catch (NoSuchElementException nsee) {
 			
 		}
 		
 		try {
 			dll.indexOf(5);
 			fail("Should have thrown exception");
 		} catch (NoSuchElementException nsee) {
 			
 		}
 		
 		try {
 			dll.indexOf(17);
 			fail("Should have thrown exception");
 		} catch (NoSuchElementException nsee) {
 			
 		}
 	}
 
 	/**
 	 * Tests the remove method
 	 */
 	public void testRemove() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		
 		DeckLinkedList dll = new DeckLinkedList();
 		
 		try {
 			dll.remove(0);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 		dll.add(intArr);
 		
 		assertEquals(dll.remove(6), 16);
 		assertEquals(dll.remove(0), 4);
 		assertEquals(dll.remove(0), 6);
 		assertEquals(dll.remove(0), 8);
 		assertEquals(dll.remove(0), 10);
 		assertEquals(dll.remove(0), 12);
 		assertEquals(dll.remove(0), 14);
 		
 		try {
 			dll.remove(0);
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 	}
 
 	/**
 	 * Tests the set method
 	 */
 	public void testSet() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		DeckLinkedList dll = new DeckLinkedList();
 		dll.add(intArr);
 		
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 		dll.set(0, 1);
 		assertEquals(dll.get(0),1);
 	}
 
 	/**
 	 * Tests the removeLast method
 	 */
 	public void testRemoveLast() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		DeckLinkedList dll = new DeckLinkedList();
 		
 		try {
 			dll.removeLast();
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 		
 		dll.add(intArr);
 		
 		assertEquals(dll.removeLast(), 16);
 		assertEquals(dll.removeLast(), 14);
 		assertEquals(dll.removeLast(), 12);
 		assertEquals(dll.removeLast(), 10);
 		assertEquals(dll.removeLast(), 8);
 		assertEquals(dll.removeLast(), 6);
 		assertEquals(dll.removeLast(), 4);
 		
 		try {
 			dll.removeLast();
 			fail("Should have thrown exception");
 		} catch (IndexOutOfBoundsException iobe) {
 			
 		}
 	}
 	
 	/**
 	 * Tests the size getter
 	 */
 	public void testSize() {
 		int[] intArr = {4,6,8,10,12,14,16};
 		DeckLinkedList dll = new DeckLinkedList();
 		
 		assertEquals(dll.size(), 0);
 		dll.add(4);
 		assertEquals(dll.size(), 1);
 		dll.add(intArr);
 		assertEquals(dll.size(), 8);
 	}
 }
