 package se751.team13.quicksort;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.BrokenBarrierException;
 
 import org.junit.Test;
 
 public class QuickSorterTest<T extends Comparable<? super T>> {
 	private final int SHORT = 100;
 	private final int LARGE = 10000;
 	private final int LOW_MAX = 1000;
 	private final int HIGH_MAX = 100000;
 			
 	Sorter pqs = new ParallelQuicksort();	
 	
 	private static List<Integer> generateRandomNumbers(int amount, int max) {
 		Random rand = new Random(System.currentTimeMillis());
 		List<Integer> nums = new ArrayList<Integer>();
 
 		for (int i = 0; i < amount; i++) {
 			nums.add(rand.nextInt(max));
 		}
 		return nums;
 	}
 	
 	private static List<Integer> generateInOrderNumbers(int amount) {
 		List<Integer> nums = new ArrayList<Integer>();
 
 		for (int i = 0; i < amount; i++) {
 			nums.add(i);
 		}
 		return nums;
 	}
 	
 	@Test
 	public void testSmallRandList(){
		List<Integer> unsorted = generateRandomNumbers(10, 100);
 		List<Integer> sorted = null;
 		try {
 			
 			sorted = pqs.sort(unsorted);
 		} catch (InterruptedException | BrokenBarrierException e) {
 			e.printStackTrace();
 		}
 		Integer previous = null;
 		for (Integer current : sorted) {
 			if (previous != null) {
 				assertTrue(previous.compareTo(current) < 0);
 			}
 		}	
 		assertTrue(sorted.size() == unsorted.size());		
 	}
 	
 	@Test
 	public void testSmallOrderList(){
		List<Integer> unsorted = generateInOrderNumbers(10);
 		List<Integer> sorted = null;
 		try {
 			sorted = pqs.sort(unsorted);
 		} catch (InterruptedException | BrokenBarrierException e) {
 			e.printStackTrace();
 		}
 		Integer previous = null;
 		for (Integer current : sorted) {
 			if (previous != null) {
 				assertTrue(previous.compareTo(current) < 0);
 			}
 		}	
 		assertTrue(sorted.size() == unsorted.size());
 	}
 	
 	@Test
 	public void testRandOrderShortInteger() {
 		List<Integer> unsorted = generateRandomNumbers(SHORT,
 				LOW_MAX);
 		List<Integer> sorted = null;
 		try {
 			sorted = pqs.sort(unsorted);
 		} catch (InterruptedException | BrokenBarrierException e) {
 			e.printStackTrace();
 		}
 		Integer previous = null;
 		for (Integer current : sorted) {
 			if (previous != null) {
 				assertTrue(previous.compareTo(current) < 0);
 			}
 		}
 		assertTrue(sorted.size() == unsorted.size());
 	}
 	
 	@Test
 	public void testInOrderShortInteger() {
 		List<Integer> unsorted = generateInOrderNumbers(SHORT);
 		List<Integer> sorted = null;
 		try {
 			sorted = pqs.sort(unsorted);
 		} catch (InterruptedException | BrokenBarrierException e) {
 			e.printStackTrace();
 		}
 		Integer previous = null;
 		for (Integer current : sorted) {
 			if (previous != null) {
 				assertTrue(previous.compareTo(current) < 0);
 			}
 		}	
 		assertTrue(sorted.size() == unsorted.size());
 	}
 	
 	@Test
 	public void testRandOrderLargeInteger() {
 		List<Integer> unsorted = generateRandomNumbers(LARGE,
 				HIGH_MAX);
 		List<Integer> sorted = null;
 		try {
 			sorted = pqs.sort(unsorted);
 		} catch (InterruptedException | BrokenBarrierException e) {
 			e.printStackTrace();
 		}
 		Integer previous = null;
 		for (Integer current : sorted) {
 			if (previous != null) {
 				assertTrue(previous.compareTo(current) < 0);
 			}
 		}	
 		assertTrue(sorted.size() == unsorted.size());
 	}
 		
 	@Test
 	public void testInOrderLargeInteger() {
 		List<Integer> unsorted = generateInOrderNumbers(LARGE);
 		List<Integer> sorted = null;
 		try {
 			sorted = pqs.sort(unsorted);
 		} catch (InterruptedException | BrokenBarrierException e) {
 			e.printStackTrace();
 		}
 		Integer previous = null;
 		for (Integer current : sorted) {
 			if (previous != null) {
 				assertTrue(previous.compareTo(current) < 0);
 			}
 		}	
 		assertTrue(sorted.size() == unsorted.size());
 	}
 }
