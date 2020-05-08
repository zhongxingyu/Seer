 package hammer;
 
 import java.util.ArrayList;
 
 /**
  * A collection of HAMMER tests to be run in one go.
  * @author rynor_000
  *
  */
 public class HammerSuite {
 	private String mName;
 	private ArrayList<HammerTest> mTests;
 
 	public HammerSuite(String name) {
 		mName = name;
 		mTests = new ArrayList<HammerTest>();
 	}
 
 	/**
 	 * Adds a test to the test suite.
 	 * @param test
 	 */
 	public void addTest(HammerTest test) {
 		mTests.add(test);
 	}
 	
 	/**
 	 * Runs all the tests in the suite in the order they were added.
 	 */
 	public void run() {
 		ArrayList<HammerTest> failedTests = new ArrayList<HammerTest>();
 		
 		// Silence test output.
 		// In future this should only silence output to stdout.
 		// The log file should still be written.
		HammerLog.LogLevel prevLevel = HammerLog.getLogLevel();
 		HammerLog.setLogLevel(HammerLog.LogLevel.NONE);
 		
 		System.out.println("  === Running HAMMER suite: " + mName + " ===");
 		
 		for (HammerTest test : mTests) {
 			if (test.run()) {
 				System.out.print('.');
 			}
 			else {
 				System.out.print('F');
 				failedTests.add(test);
 			}
 		}
 		
 		int numTests = mTests.size();
 		int failures = failedTests.size();
 		
 		System.out.println("\nTest suite complete: out of " + 
 					 	   String.valueOf(numTests) + " tests, " + 
 					 	   String.valueOf(failures) + " failed.\n");
 		
 		if (failures > 0) {
			System.out.println("Failed tests: ");
 			for (HammerTest test : failedTests) {
 				System.out.println("    " + test.getName());
 			}
			System.out.println("");
 		}
		
		// Reset the log level to what it was before.
		HammerLog.setLogLevel(prevLevel);
 	}
 }
