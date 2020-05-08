 package com.mutation.testrunner.junit3;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import junit.framework.Test;
 import junit.framework.TestResult;
 
 import com.mutation.runner.Mutant;
 import com.mutation.runner.events.IEventListener;
 import com.mutation.runner.events.TestNotExecuted;
 import com.mutation.runner.events.TestsExecuted;
 import com.mutation.testrunner.ITestRunner;
 
 /**
  * Executes mutation tests using junit tests
  * 
  * @author mike
  * 
  */
 public class JUnitRunner implements ITestRunner {
 
 	private URL[] testClassesLocations;
 
 	private boolean stopOnFirstFailedTest = true;
 
 	/**
 	 * 
 	 * @return sum of failures and errors
 	 */
 	private int runTest(String testCase, Mutant mutant) {
 
 		MutantJUnitRunner runner = new MutantJUnitRunner(getTestClassesLocations(), mutant);
 		Test suite = runner.getTest(testCase);
 		TestResult result = runner.doRun(suite, false);
 		int errors = result.errorCount();
 		int failures = result.failureCount();
 		return errors + failures;
 	}
 
 	public URL[] getTestClassesLocations() {
 		return testClassesLocations;
 	}
 
 	public void setTestClassesLocations(URL[] testClassesLocation) {
 		this.testClassesLocations = testClassesLocation;
 	}
 
 	public void setTestClassesLocationsAsFiles(List<File> testClassesLocPaths) throws MalformedURLException {
 		URL[] urls = new URL[testClassesLocPaths.size()];
 		for (int i = 0; i < testClassesLocPaths.size(); i++) {
 			urls[i] = testClassesLocPaths.get(i).toURL();
 		}
 
 		this.testClassesLocations = urls;
 	}
 
 	public void execute(Mutant mutant, Set<String> origTestNames, IEventListener eventListener) {
 		boolean survived = true;
 		
 		Set<String> executedTestsNames = new HashSet<String>(origTestNames);
 		Set<String> killerTestNames = new TreeSet<String>();
 		for (String testCase : executedTestsNames) {
 			try {
 				int failures = runTest(testCase, mutant);
 				if (failures > 0) {
 					survived = false;
 					//TODO IMHO it would be better to have the surived flag separated
 					mutant.setSurvived(false);
 					killerTestNames.add(testCase);
 					if (stopOnFirstFailedTest) {
 						break;
 					}
 				}
 				
 			} catch (Exception e) {
				
				//TODO: BUG ! concurrent update on collection !
 				executedTestsNames.remove(testCase);
 				eventListener.notifyEvent(new TestNotExecuted(mutant, testCase, e));
 			}
 		}
 		eventListener.notifyEvent(new TestsExecuted(mutant, executedTestsNames, survived, killerTestNames));
 	}
 
 	public void setStopOnFirstFailedTest(boolean stopOnFirstFailedTest) {
 		this.stopOnFirstFailedTest = stopOnFirstFailedTest;
 	}
 
 }
