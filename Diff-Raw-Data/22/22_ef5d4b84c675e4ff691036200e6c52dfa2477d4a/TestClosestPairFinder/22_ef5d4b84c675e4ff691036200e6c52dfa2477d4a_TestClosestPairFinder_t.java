 package testclosestpair;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 import java.util.Scanner;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import closestpair.ClosestPairFinder;
 
 public class TestClosestPairFinder {
	private double errorSum = 0;
	
 	/** Method to run an actual test case.
 	 * 
 	 * @param filepath
 	 *            Name of test data to be used, e.g.
 	 *            "stable-marriage-friends".
 	 * @param expected TODO
 	 * @return TODO */
 	private long runTestCase(String filepath, double expected) {
 		System.out.print("Running test: " + filepath);
 
 		/* Divert stdout to buffer */
 		PrintStream oldOut = System.out;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		PrintStream ps = new PrintStream(baos);
 		System.setOut(ps);
 
 		String[] args = new String[1];
 		args[0] = filepath;
 
 		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
 		long startTime = threadBean.getCurrentThreadCpuTime();
 		ClosestPairFinder.main(args);
 		long stopTime = threadBean.getCurrentThreadCpuTime();
 
 		/* Restore stdout */
 		System.setOut(oldOut);
 
		addToErrorSum(expected - Double.valueOf(baos.toString()));
 		
 		long timeTaken = (stopTime - startTime) / 1000000;
 		System.out.println(". It took " + timeTaken + " ms to complete.");
 		
 		return timeTaken;
 	}
 	
	private void addToErrorSum(double error) {
		errorSum += Math.abs(error);
	}

 	@Test
 	public void testTSPFiles() {
 		Scanner scanner = null;
 		try {
 			scanner = new Scanner(new File("./data/closest-pair.out"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		while (scanner.hasNextLine()) {
 			String[] testData = scanner.nextLine().split("\\s+");
 			String filepath = testData[0].substring(1, testData[0].length() - 1);
 			runTestCase(filepath, Double.valueOf(testData[2]));
 		}
		
		System.out.println("The sum of error is: " + errorSum);
 	}
 	
 	
 	@Test public void testBurma14() {
 		runTestCase("./data/burma14.tsp", 0.23);
 	}
 }
