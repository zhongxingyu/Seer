 package com.joelj.jenkins.claimblame;
 
 import com.attask.jenkins.testreport.*;
 import hudson.Extension;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.model.User;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 import java.util.Collection;
 
 /**
  * User: Joel Johnson
  * Date: 2/7/13
  * Time: 11:17 AM
  */
 public class ClaimAtTaskTestPublisher extends TestDataPublisher {
 	private transient User culprit;
 	private transient Status status;
 	private transient String testName;
 	private transient String projectId;
 
 	@DataBoundConstructor
 	public ClaimAtTaskTestPublisher() {
 
 	}
 
 	public User getCulprit() {
 		return culprit;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public Collection<User> getUsers() {
 		return User.getAll();
 	}
 
 	public String getTestName() {
 		return testName;
 	}
 
 	public User getCurrentUser() {
 		return User.current();
 	}
 
 	public String getProjectId() {
 		return projectId;
 	}
 
 	@Override
 	public String getDisplayName() {
 		return "ClaimBlame";
 	}
 
 	@Override
 	public boolean before(AbstractBuild<?, ?> abstractBuild, Collection<TestResult> testResults) throws IOException, InterruptedException {
 		if(testResults != null) {
 			String key = abstractBuild.getProject().getName();
 			Blamer blamer = null;
 			for (TestResult testResult : testResults) {
 				if(testResult.getStatus() == TestStatus.FINISHED) {
 					if(blamer == null) {
 						String uniquifier = testResult.getUniquifier();
 						if(uniquifier != null) {
 							key = key + "." + uniquifier;
 						}
 						blamer = BlamerFactory.getBlamerForJob(key);
 						blamer.startTransaction();
 					}
 					Status oldStatus = blamer.getStatus(testResult.getName());
 					if(oldStatus != Status.Unassigned && oldStatus != Status.Fixed) {
 						blamer.setStatus(testResult.getName(), Status.Fixed);
                        blamer.setCulprit(testResult.getName(), null);
 					}

 				}
 			}
 			if(blamer != null) {
 				blamer.endTransaction();
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean each(AbstractBuild<?, ?> abstractBuild, TestResult testResult) throws IOException, InterruptedException {
 		String key = abstractBuild.getProject().getName();
 		String uniquifier = testResult.getUniquifier();
 		if(uniquifier != null) {
 			key = key + "." + uniquifier;
 		}
 
 		Blamer blamer = BlamerFactory.getBlamerForJob(key);
 
 		testName = testResult.getName();
 		culprit = blamer.getCulprit(testName);
 		status = blamer.getStatus(testName);
 		projectId = key;
 
 		return true;
 	}
 
 	@Override
 	public boolean after(AbstractBuild<?, ?> abstractBuild, Collection<TestResult> testResults) throws IOException, InterruptedException {
 		String key = abstractBuild.getProject().getName();
 		if(testResults != null && testResults.size() > 0) {
 			String uniquifier = testResults.iterator().next().getUniquifier();
 			if(uniquifier != null) {
 				key = key + "." + uniquifier;
 			}
 		}
 		projectId = key;
 		return true;
 	}
 
 	@Extension
 	public static class DescriptorImpl extends Descriptor<TestDataPublisher> {
 		@Override
 		public String getDisplayName() {
 			return "Claim/Blame";
 		}
 	}
 }
