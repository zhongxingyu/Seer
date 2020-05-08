 package com.attask.jenkins.testreport;
 
 import hudson.Launcher;
 import hudson.matrix.MatrixAggregator;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixRun;
 import hudson.model.BuildListener;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: Joel Johnson
  * Date: 1/21/13
  * Time: 7:26 PM
  */
 public class TestResultMatrixAggregator extends MatrixAggregator {
 	private final List<TestDataPublisher> testDataPublishers;
 
 	protected TestResultMatrixAggregator(MatrixBuild build, Launcher launcher, BuildListener listener, List<TestDataPublisher> testDataPublishers) {
 		super(build, launcher, listener);
 		this.testDataPublishers = testDataPublishers;
 	}
 
 	@Override
 	public boolean endBuild() throws InterruptedException, IOException {
 		List<MatrixRun> runs = build.getRuns();
 		if(runs != null && runs.size() > 0) {
 			Set<TestResult> testResults = new HashSet<TestResult>();
 			String uniquifier = null;
 			String url = "testReport";
 			for (MatrixRun run : runs) {
				if(run.getNumber() == build.getNumber()) {
					List<TestResultAction> actions = run.getActions(TestResultAction.class);
					if(actions != null) {
						for (TestResultAction action : actions) {
							testResults.addAll(action.getTestResults().values());
							uniquifier = action.getUniquifier();
						}
 					}
 				}
 			}
 			if(uniquifier != null) {
 				build.addAction(new TestResultAction(build, testResults, uniquifier, url, testDataPublishers));
 			}
 		}
 
 		return true;
 	}
 
 	private List<TestResult> mergeResults(List<MatrixRun> runs) {
 		List<TestResult> testResults = new ArrayList<TestResult>();
 
 		for (MatrixRun run : runs) {
 			List<TestResultAction> actions = run.getActions(TestResultAction.class);
 			if(actions != null) {
 				for (TestResultAction action : actions) {
 					testResults.addAll(action.getTestResults().values());
 				}
 			}
 		}
 		return testResults;
 	}
 }
