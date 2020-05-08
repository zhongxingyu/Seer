 package com.attask.jenkins;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrapperDescriptor;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 
 /**
  * User: Joel Johnson
  * Date: 11/15/12
  * Time: 6:59 PM
  */
 public class RetryBuildWrapper extends BuildWrapper {
 	private final String worseThan;
 
 	@DataBoundConstructor
 	public RetryBuildWrapper(int numberRetries, String worseThan) {
 		//I like storing raw values. I know, I'm weird.
 		//Converting worseThan to an Enum then back again converts it to FAILURE if it's invalid.
 		this.worseThan = worseThan == null || worseThan.isEmpty() ? Result.FAILURE.toString() : Result.fromString(worseThan).toString();
 	}
 
 	@Override
 	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
 		return new Environment() {
 			@Override
 			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				Result result = build.getResult();
				if(result != null && result.isWorseOrEqualTo(Result.fromString(worseThan))) {
 					RetriedCause cause = (RetriedCause) build.getCause(RetriedCause.class);
 					if(cause == null) {
 						build.addAction(new RetriedAction(build));
 						ParametersAction action = build.getAction(ParametersAction.class);
 						build.getProject().scheduleBuild(0, new RetriedCause(build), action);
 					}
 				}
 				return true;
 			}
 		};
 	}
 
 	public String getWorseThan() {
 		return worseThan;
 	}
 
 	@Extension
 	public static class DescriptorImpl extends BuildWrapperDescriptor {
 		@Override
 		public boolean isApplicable(AbstractProject<?, ?> item) {
 			return true;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Auto-retry build if it fails";
 		}
 	}
 }
