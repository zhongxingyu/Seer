 package com.joelj.jenkins.claimblame;
 
 import com.google.common.collect.ImmutableList;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.scm.ChangeLogSet;
 import hudson.tasks.junit.*;
 import hudson.tasks.junit.TestObject;
 import hudson.tasks.junit.TestResult;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: joeljohnson
  * Date: 4/11/12
  * Time: 7:32 PM
  */
 public class ClaimTestPublisher extends TestDataPublisher {
 	@DataBoundConstructor
 	public ClaimTestPublisher() {
 
 	}
 
 	@Override
 	public TestResultAction.Data getTestData(final AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, TestResult testResult) throws IOException, InterruptedException {
 		resolveTests(build, testResult.getSuites());
 		return new TestResultAction.Data() {
 			@Override
 			@SuppressWarnings("deprecation")
 			public List<? extends TestAction> getTestAction(TestObject testObject) {
 				Blamer blamer = BlamerFactory.getBlamerForJob(build.getProject());
 				ImmutableList.Builder<TestAction> builder = ImmutableList.builder();
 				if(testObject instanceof CaseResult) {
 					String rootUrl = Hudson.getInstance().getRootUrl();
 					if (rootUrl == null) {
 						rootUrl = "/";
 					}
 					BlameAction blameAction = new BlameAction(build.getExternalizableId(), ((CaseResult) testObject).getFullName(), blamer, rootUrl + build.getUrl() + "testReport" + testObject.getUrl());
 					builder.add(blameAction);
 				}
 				return builder.build();
 			}
 		};
 	}
 
 	private void resolveTests(AbstractBuild<?, ?> build, Collection<SuiteResult> suites) {
 		Blamer blamer = BlamerFactory.getBlamerForJob(build.getProject());
 
 		User culprit = getSingleCulprit(build);
 
 		for (SuiteResult suite : suites) {
 			for (CaseResult caseResult : suite.getCases()) {
 				if(caseResult.isPassed()) {
 					blamer.setStatus(caseResult.getFullName(), Status.Fixed);
 				} else if(caseResult.getAge() == 1) {
 					//if culprit is null, it will remove any assignment
 					blamer.setCulprit(caseResult.getFullName(), culprit);
 				}
 			}
 		}
 	}
 
 	private User getSingleCulprit(AbstractBuild<?, ?> build) {
 		User culprit = null;
 		Set<String> committers = findCommitters(build);
 		if(committers.size()==1){
 			User user = User.get(committers.iterator().next());
 			culprit= user;
 		}
 		return culprit;
 	}
 
 	public static Set<String> findCommitters(AbstractBuild build) {
 		AbstractBuild theBuild=build;
 		Set<String> result=new HashSet<String>();
 		while(theBuild!=null){
 			for (Object changeObj : build.getChangeSet()) {
 				ChangeLogSet.Entry change = (ChangeLogSet.Entry)changeObj;
 				User culprit = change.getAuthor();
 				if(User.getAll().contains(culprit)){
 					result.add(culprit.getId());
 				}
 			}
 			theBuild= (AbstractBuild) getUpstreamProject(theBuild);
 		}
 		return Collections.unmodifiableSet(result);
 	}
 
 	private static Run getUpstreamProject(Run build){
 		Cause.UpstreamCause cause = (Cause.UpstreamCause) build.getCause(Cause.UpstreamCause.class);
 		if(cause != null) {
 			String upstreamProject = cause.getUpstreamProject();
 			int upstreamBuildNumber = cause.getUpstreamBuild();
			Project project = (Project) Project.findNearest(upstreamProject);
 			return project.getBuildByNumber(upstreamBuildNumber);
 		}
 		return null;
 	}
 
 	@Extension
 	public static class DescriptorImpl extends Descriptor<TestDataPublisher> {
 		@Override
 		public String getDisplayName() {
 			return "Enable claiming/blaming test results";
 		}
 	}
 }
