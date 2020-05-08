 package se751.team13.quicksort;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import se751.team13.quicksort.parallel.InternetDump;
 import se751.team13.quicksort.parallel.InternetDump.Manager;
 import se751.team13.realquicksort.RecursiveTaskSorter;
 
 public class SorterTest {
 
 	private List<Integer> unsorted;
 	private Sorter sorter;
 
 	@Before
 	public void setUp() throws Exception {
 		unsorted = Arrays.asList(4, 5, 1, 7, 2, 9, 6, 1, 3);
 
 	}
 
 	@Test
 	public void testInsertionSort() {
 		sorter = new InsertionSorter();
 		List<Integer> list;
 		try {
 			list = sorter.sort(unsorted);
 			if (!Util.inOrder(list)) {
 				fail();
 			} else {
 				assertTrue(true);
 			}
 		} catch (InterruptedException | BrokenBarrierException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			fail("Exception");
 		}
 	}
 
 //	@Test
 //	public void testInplaceQuickSort() {
 //		sorter = new InplaceQuickSorter();
 //		List<Integer> list;
 //		try {
 //			list = sorter.sort(unsorted);
 //			if (!Util.inOrder(list)) {
 //				fail();
 //			} else {
 //				assertTrue(true);
 //			}
 //		} catch (InterruptedException | BrokenBarrierException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //			fail("Exception");
 //		}
 //	}
 
 	@Test
 	public void testInternetDump() {
 
 		int RANGE = 1000;
 
 		Random rand = new Random();
 		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 1000000; i++) {
 			list.add(rand.nextInt(RANGE));
 		}
 		long start, end;
 
		Sorter<Integer> sorter = new RecursiveTaskSorter<>();
 		start = System.currentTimeMillis();
 		try {
 			sorter.sort(list);
 		} catch (InterruptedException | BrokenBarrierException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		end = System.currentTimeMillis();
 		System.out.println("time(ms):" + (end - start));
 
 		if (Util.inOrder(list)) {
 			assertTrue(true);
 		} else {
 			fail();
 		}
 	}
 
 }
