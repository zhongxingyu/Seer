 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 /**
  * @author Jared Moore
  * @version Feb 27, 2013
  */
 public class BinaryHeapTest {
 
 	@Test
 	public void testSimpleAdd() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		assertEquals(
 				"[1, 2, 3, null, null, null, null, null, null, null, null]",
 				Arrays.toString(heap.getData())); // it is easy enough to write
 													// a getter for data
 	}
 
 	@Test
 	public void testSimpleBubbleUp() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>();
 		heap.add(2);
 		heap.add(3);
 		heap.add(1);
 		assertEquals(
 				"[1, 3, 2, null, null, null, null, null, null, null, null]",
 				Arrays.toString(heap.getData()));
 	}
 
 	@Test
 	public void testSimpleRemove() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		assertEquals(1, (int) heap.remove());
 		assertEquals(
 				"[2, null, 3, null, null, null, null, null, null, null, null]",
 				Arrays.toString(heap.getData()));
 	}
 
 	@Test
 	public void testSimplePeek() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		assertEquals(1, (int) heap.peek());
 	}
 
 	@Test
 	public void funWithNulls() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		heap.add(null);
 		assertEquals(4, heap.size());
 		heap.remove();
 		heap.remove();
 		heap.remove();
 		heap.remove();
 		assertTrue(heap.isEmpty());
 	}
 
 	@Test
 	public void testLargerAdd() {
 		BinaryHeap<Integer> heap = makeBiggerHeap();
		Integer array[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
		System.out.println(Arrays.toString(heap.getData()));
 		assertArrayEquals(array, heap.getData());
 	}
 
 	/**
 	 * Makes a bigger heap (added large to small)
 	 * 
 	 * @return the heap
 	 */
 	private BinaryHeap<Integer> makeBiggerHeap() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>();
 		for (int i = 11; i > 0; i--)
 			heap.add(i);
 		return heap;
 	}
 
 	/**
 	 * Makes a simple heap
 	 * 
 	 * @return the heap
 	 */
 	private BinaryHeap<Integer> makeSimpleHeap() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>();
 		heap.add(1);
 		heap.add(2);
 		heap.add(3);
 		return heap;
 	}
 
 }
