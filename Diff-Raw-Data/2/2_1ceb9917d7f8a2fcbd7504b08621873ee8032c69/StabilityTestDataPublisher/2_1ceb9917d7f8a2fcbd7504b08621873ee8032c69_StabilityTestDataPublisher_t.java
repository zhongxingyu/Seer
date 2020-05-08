 package de.esailors.jenkins.teststability;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.tasks.junit.PackageResult;
 import hudson.tasks.junit.TestDataPublisher;
 import hudson.tasks.junit.TestResult;
 import hudson.tasks.junit.TestResultAction.Data;
 import hudson.tasks.junit.ClassResult;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import de.esailors.jenkins.teststability.StabilityTestData.Result;
 
 public class StabilityTestDataPublisher extends TestDataPublisher {
 	
 	@DataBoundConstructor
 	public StabilityTestDataPublisher() {
 	}
 	
 	@Override
 	public Data getTestData(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener, TestResult testResult) throws IOException,
 			InterruptedException {
 		
 		Map<String,CircularStabilityHistory> stabilityHistoryPerTest = new HashMap<String,CircularStabilityHistory>();
 		
 		for (hudson.tasks.test.TestResult result: getClassAndCaseResults(testResult)) {
 			
 			CircularStabilityHistory history = getPreviousHistory(result);
 			
 			if (history != null) {
 				if (result.isPassed()) {
 					history.add(build.getNumber(), true);
 					
 					if (history.isAllPassed()) {
 						history = null;
 					}
 					
 				} else if (result.getFailCount() > 0) {
 					history.add(build.getNumber(), false);
 				}
 				// else test is skipped and we leave history unchanged
 				
 				if (history != null) {
 					stabilityHistoryPerTest.put(result.getId(), history);
 				} else {
 					stabilityHistoryPerTest.remove(result.getId());
 				}
 			} else if (isFirstTestFailure(result, history)) {
 				int maxHistoryLength = getDescriptor().getMaxHistoryLength();
 				CircularStabilityHistory ringBuffer = new CircularStabilityHistory(maxHistoryLength);
 				
 				// add previous results (if there are any):
 				buildUpInitialHistory(ringBuffer, result, maxHistoryLength - 1);
 				
 				ringBuffer.add(build.getNumber(), false);
 				stabilityHistoryPerTest.put(result.getId(), ringBuffer);
 			}
 		}
 		
 		return new StabilityTestData(stabilityHistoryPerTest);
 	}
 
 	private CircularStabilityHistory getPreviousHistory(hudson.tasks.test.TestResult result) {
 		hudson.tasks.test.TestResult previous = getPreviousResult(result);
 
 		if (previous != null) {
 			StabilityTestAction previousAction = previous.getTestAction(StabilityTestAction.class);
 			if (previousAction != null) {
 				CircularStabilityHistory prevHistory = previousAction.getRingBuffer();
 				
 				if (prevHistory == null) {
 					return null;
 				}
 				
 				// copy to new to not modify the old data
 				CircularStabilityHistory newHistory = new CircularStabilityHistory(getDescriptor().getMaxHistoryLength());
 				newHistory.addAll(prevHistory.getData());
 				return newHistory;
 			}
 		}
 		return null;
 	}
 
 	private boolean isFirstTestFailure(hudson.tasks.test.TestResult result,
 			CircularStabilityHistory previousRingBuffer) {
 		return previousRingBuffer == null && result.getFailCount() > 0;
 	}
 	
 	private void buildUpInitialHistory(CircularStabilityHistory ringBuffer, hudson.tasks.test.TestResult result, int number) {
 		List<Result> testResultsFromNewestToOldest = new ArrayList<Result>(number);
 		hudson.tasks.test.TestResult previousResult = getPreviousResult(result);
 		while (previousResult != null) {
 			testResultsFromNewestToOldest.add(
 					new Result(previousResult.getOwner().getNumber(), previousResult.isPassed()));
 			previousResult = previousResult.getPreviousResult();
 		}
 
 		for (int i = testResultsFromNewestToOldest.size() - 1; i >= 0; i--) {
 			ringBuffer.add(testResultsFromNewestToOldest.get(i));
 		}
 	}
 
 	
 	private hudson.tasks.test.TestResult getPreviousResult(hudson.tasks.test.TestResult result) {
 		try {
 			return result.getPreviousResult();
 		} catch (RuntimeException e) {
 			// there's a bug (only on freestyle builds!) that getPreviousResult may throw a NPE (only for ClassResults!)
			// Note: doesn't seem to occur anymore in Jenkins 1.520
			// Don't know about the versions between 1.480 and 1.520
 			return null;
 		}
 	}
 	
 	private Collection<hudson.tasks.test.TestResult> getClassAndCaseResults(TestResult testResult) {
 		List<hudson.tasks.test.TestResult> results = new ArrayList<hudson.tasks.test.TestResult>();
 		
 		Collection<PackageResult> packageResults = testResult.getChildren();
 		for (PackageResult pkgResult : packageResults) {
 			Collection<ClassResult> classResults = pkgResult.getChildren();
 			for (ClassResult cr : classResults) {
 				results.add(cr);
 				results.addAll(cr.getChildren());
 			}
 		}
 
 		return results;
 	}
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 	
 
 	@Extension
 	public static class DescriptorImpl extends Descriptor<TestDataPublisher> {
 		
 		private int maxHistoryLength = 30;
 
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject json)
 				throws FormException {
 			this.maxHistoryLength = json.getInt("maxHistoryLength");
 			
 			save();
             return super.configure(req,json);
 		}
 		
 		public int getMaxHistoryLength() {
 			return this.maxHistoryLength;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Test stability history";
 		}
 	}
 }
