 package com.yiling.lu.algorithm.datastructure;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.yiling.lu.algorithm.util.AssortedUtils;
 
 public class HeapTest {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testInsert() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>(Integer.class, 0);
 		int len = heap.getArray().length;
 		
 		System.out.println("initial array length: " + len);
 		
 		heap.insert(100);
 
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testInsert2() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>(Integer.class, 1);
 		int len = heap.getArray().length;
 		
 		System.out.println("initial array length: " + len);
 		
 		heap.insert(100);
 		heap.insert(-1);
 
 	}
 	@Test
 	public void testInsert3() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>(Integer.class, 10);
 		int len = heap.getArray().length;
 		
 		System.out.println("initial array length: " + len);
 		
 		heap.insert(100);
 		heap.insert(600);
 		heap.insert(-1);
 		heap.insert(10);
 		heap.insert(-39);
 		
 		heap.insert(55);
 		heap.insert(-45);
 		heap.insert(-144);
 		heap.insert(105);
 		heap.insert(-36);
 		
 		int max = heap.findMax();
 		
 		assertEquals(600, max);
 		
 		while(heap.getHeapLength()>0){
 			heap.extractMax();
 		}
 		
 		AssortedUtils.verifyOrder(heap.getArray(), true);
 
 	}
 	
 	
 	@Test
 	public void testBuildHeapSimple() {
 		Integer[] input = {100,-2,0,500};
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>(input);
 
 		
 		heap.buildHeap();
 		int actualMax = heap.findMax();
 		
 		assertEquals(500, actualMax);
 		
 	}
 	
 	@Test
 	public void testBuildHeap() {
 		Integer[] input = {100,-2,0,50,300,-3,-6,50,30,66,90};
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>(input);
 
 		
 		heap.buildHeap();
 		int actualMax = heap.findMax();
 		
 		assertEquals(300, actualMax);
 		
 	}
 
 
 	@Test
	public void testFindMax() {
		fail("Not yet implemented");
	}

	@Test
 	public void testDeleteMax() {
 		Integer[] input = {100,-2,0,50,300,-3,-6,50,30,66,90};
 
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>(input);
 
 		
 		heap.buildHeap();
 		int actualMax = heap.extractMax();
 		
 		assertEquals(300, actualMax);
 		assertEquals(input.length-1, heap.getHeapLength());
 		
 		while(heap.getHeapLength()>0){
 			heap.extractMax();
 		}
 		
 		AssortedUtils.verifyOrder(heap.getArray(), true);
 		assertEquals(input.length, heap.getArray().length);
 	}
 	
 	@Test
 	public void testDeleteMax2() {
 		Integer[] input = {1, 3, 2};
 
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>(input);
 
 		
 		heap.buildHeap();
 		int actualMax = heap.extractMax();
 		
 		assertEquals(3, actualMax);
 		assertEquals(input.length-1, heap.getHeapLength());
 		
 		while(heap.getHeapLength()>0){
 			heap.extractMax();
 		}
 		
 		AssortedUtils.verifyOrder(heap.getArray(), true);
 		assertEquals(input.length, heap.getArray().length);
 	}	
 
 }
