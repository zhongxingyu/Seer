 package riz.silvano.intervaltree;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import riz.silvano.intervaltree.IntervalTree.TreeStatusListener;
 
 /**
  * Tests
  * 
  * @author mele
  */
 @RunWith(JUnit4.class)
 public class IntervalTreeTest {
 
 	@Before
 	public void setUp() {
 
 	}
 
 	@Test
 	public void testIntervalTree() throws InterruptedException {
 
 		List<Interval> data = generate(-1);
 
 		StatusListener listener = new StatusListener();
 		IntervalTree tree = new IntervalTree(data, listener);
 
 		while (!listener.isLoaded()) {
 			Thread.sleep(100);
 		}
 
 		Assert.assertNotNull(tree);
 		Assert.assertEquals(data.size(), tree.getNodeCount());
 
 		System.out.println(tree);
 
 	}
 
 	@Test
 	public void testRecursionLimit() throws InterruptedException {
 
 		Memory before = new Memory();
 
 		// Used -Xmx2048m
 		// 1000000 give a java.lang.OutOfMemoryError: Java heap space in the construction
 		// 100000 give a java.lang.OutOfMemoryError: Java heap space in the processing
 		// 50000 it's seems very slow
 		// 20000 it's seems slow
 		List<Interval> data = generate(100000);
 
 		StatusListener listener = new StatusListener();
 		IntervalTree tree = new IntervalTree(data, listener);
 
 		while (!listener.isLoaded()) {
 			Thread.sleep(100);
 		}
 
 		Assert.assertNotNull(tree);
 		Assert.assertEquals(data.size(), tree.getNodeCount());
 
 		Memory after = new Memory();
 
 		System.out.println("Mem before\n " + before);
 		System.out.println("Mem after\n " + after);
 
 	}
 
 	@Test
 	public void testQueryTime() throws InterruptedException {
 
 		Memory before = new Memory();
 
 		List<Interval> data = generate(100000);
 
 		StatusListener listener = new StatusListener();
 		IntervalTree tree = new IntervalTree(data, listener);
 
 		while (!listener.isLoaded()) {
 			Thread.sleep(100);
 		}
 
 		Assert.assertNotNull(tree);
 		Assert.assertEquals(data.size(), tree.getNodeCount());
 
 		Memory after = new Memory();
 
 		System.out.println("Mem before\n " + before);
 		System.out.println("Mem after\n " + after);
 
 		// Try a query
		long start = System.currentTimeMillis();
		List<Interval> resultset = tree.query(500);
		long end = System.currentTimeMillis();
		System.out.println("Query executed in " + (end - start) + " ms ");
		printQueryResult(resultset, 500);
 
 	}
 
 	@Test
 	public void testQuery() throws InterruptedException {
 		List<Interval> data = generate(-1);
 
 		StatusListener listener = new StatusListener();
 		IntervalTree tree = new IntervalTree(data, listener);
 
 		while (!listener.isLoaded()) {
 			Thread.sleep(100);
 		}
 
 		System.out.println(tree);
 
 		List<Interval> resultset = tree.query(5);
 		printQueryResult(resultset, 5);
 
 		resultset = tree.query(15);
 		printQueryResult(resultset, 15);
 
 		resultset = tree.query(25);
 		printQueryResult(resultset, 25);
 
 		resultset = tree.query(35);
 		printQueryResult(resultset, 35);
 
 		resultset = tree.query(45);
 		printQueryResult(resultset, 45);
 
 		resultset = tree.query(53);
 		printQueryResult(resultset, 53);
 	}
 
 	// ---------------------------------
 	// Utility methods
 	// ---------------------------------
 
 	private void printQueryResult(List<Interval> intervals, long query) {
 		System.out.println("Query: " + query);
 
 		if (intervals == null) {
 			System.out.println("Null resultset");
 		} else if (intervals.size() == 0) {
 			System.out.println("Empty resultset");
 		} else {
 			for (Interval interval : intervals) {
 				System.out.print(String.format("[%d..%d]", interval.getMin(), interval.getMax()));
 			}
 			System.out.println();
 		}
 	}
 
 	private List<Interval> generate(int size) {
 		List<Interval> data = new ArrayList<Interval>();
 
 		if (size < 1) {
 			data.add(new Interval(0, 9, "0..9"));
 			data.add(new Interval(10, 19, "10..19"));
 			data.add(new Interval(20, 29, "20..29"));
 			data.add(new Interval(30, 39, "30..39"));
 			data.add(new Interval(40, 49, "40..49"));
 			data.add(new Interval(50, 59, "50..59"));
 			data.add(new Interval(45, 55, "45..55"));
 		} else {
 
 			int minBoundary = 0;
			int numOfIntervals = 1000000;
 			long minWidth = 50;
 			long maxWidth = 1000;
 
 			long min;
 			long width;
 			String info;
 			for (int i = 0; i < size; i++) {
				min = minBoundary + (int) (Math.random() * ((numOfIntervals - minWidth - minBoundary) + 1));
 				width = minWidth + (int) (Math.random() * ((maxWidth - minWidth) + 1));
 				info = String.format("%d..%d", min, min + width);
 				data.add(new Interval(min, min + width, info));
 			}
 		}
 
 		return data;
 
 	}
 
 }
 
 class StatusListener implements TreeStatusListener {
 	private AtomicBoolean loaded = new AtomicBoolean(false);
 
 	public boolean isLoaded() {
 		return loaded.get();
 	}
 
 	public void loaded() {
 		loaded.set(true);
 	}
 
 }
 
 class Memory {
 	private static final int MB = 1024 * 1024;
 
 	private long usedMemory;
 	private long freeMemory;
 	private long totalMemory;
 	private long maxMemory;
 
 	Memory() {
 		Runtime runtime = Runtime.getRuntime();
 		usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / MB;
 		freeMemory = runtime.freeMemory() / MB;
 		totalMemory = runtime.totalMemory() / MB;
 		maxMemory = runtime.maxMemory() / MB;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder("\n##### Heap utilization statistics [MB] #####");
 
 		sb.append(String.format("\nUsed Memory: %d", usedMemory));
 		sb.append(String.format("\nFree Memory: %d", freeMemory));
 		sb.append(String.format("\nTotal Memory: %d", totalMemory));
 		sb.append(String.format("\nMax Memory: %d", maxMemory));
 
 		sb.append("\n#############################################\n");
 
 		return sb.toString();
 	}
 
 }
