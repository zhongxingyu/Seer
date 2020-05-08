 package org.jenkinsci.testinprogress.server.filters;
 
 import org.jenkinsci.testinprogress.server.messages.ITestRunListener;
 
 /**
  * Wrapper around an {@link ITestRunListener} that filter stacktraces for failed
  * tests
  * 
  * @author Cedric Chabanois (cchabanois at gmail.com)
  * 
  */
 public class StackTraceFilterTestRunnerWrapper implements ITestRunListener {
 	private final ITestRunListener testRunListener;
 	private final StackTraceFilter stackTraceFilter;
 
 	public StackTraceFilterTestRunnerWrapper(ITestRunListener testRunListener,
 			StackTraceFilter stackTraceFilter) {
 		this.testRunListener = testRunListener;
 		this.stackTraceFilter = stackTraceFilter;
 	}
 
 	public void testRunStarted(long timestamp, int testCount, String runId) {
 		testRunListener.testRunStarted(timestamp, testCount, runId);
 	}
 
 	public void testRunEnded(long timestamp, long elapsedTime) {
 		testRunListener.testRunEnded(timestamp, elapsedTime);
 	}
 
 	public void testStarted(long timestamp, String testId, String testName, boolean ignored) {
 		testRunListener.testStarted(timestamp, testId, testName, ignored);
 	}
 
 	public void testEnded(long timestamp, String testId, String testName, boolean ignored) {
 		testRunListener.testEnded(timestamp, testId, testName, ignored);
 	}
 
 	public void testRunTerminated() {
 		testRunListener.testRunTerminated();
 	}
 
 	public void testTreeEntry(long timestamp, String testId, String testName,
			String parentId, String parentName, boolean isSuite, int testCount) {
		testRunListener.testTreeEntry(timestamp, testId, testName, parentId, parentName, isSuite, testCount);
 	}
 
 	public void testFailed(long timestamp, int status, String testId,
 			String testName, String trace, String expected, String actual, boolean assumptionFailed) {
 		trace = stackTraceFilter.filter(trace);
 		testRunListener.testFailed(timestamp, status, testId, testName, trace,
 				expected, actual, assumptionFailed);
 	}
 
 }
