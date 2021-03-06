 /**
  * 
  */
 package com.syncapse.jenkinsci.plugins.awscloudformationwrapper;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrapperDescriptor;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
  * @author erickdovale
  * 
  */
 public class CloudFormationBuildWrapper extends BuildWrapper {
 
     private static final Logger logger = Logger.getLogger(CloudFormationBuildWrapper.class.getName());
 
     protected List<StackBean> stacks;
 
 	private final transient List<CloudFormation> cloudFormations = new ArrayList<CloudFormation>();
 
     @DataBoundConstructor
 	public CloudFormationBuildWrapper(List<StackBean> stacks) {
 		this.stacks = stacks;
 	}
     
     @Override
     public void makeBuildVariables(AbstractBuild build,
     		Map<String, String> variables) {
     	
     	for (CloudFormation cf : cloudFormations){
     		variables.putAll(cf.getOutputs());
     	}
     	
     }
 
 	@Override
 	public Environment setUp(AbstractBuild build, Launcher launcher,
 			BuildListener listener) throws IOException, InterruptedException {
 
         EnvVars env = build.getEnvironment(listener);
         env.overrideAll(build.getBuildVariables());
 
 		for (StackBean stackBean : stacks) {
 
 			final CloudFormation cloudFormation = newCloudFormation(stackBean,
 					build, env, listener.getLogger());
 
 			try {
 				if (cloudFormation.create()) {
 					cloudFormations.add(cloudFormation);
 					env.putAll(cloudFormation.getOutputs());
 				} else {
 					build.setResult(Result.FAILURE);
 					break;
 				}
 			} catch (TimeoutException e) {
 				listener.getLogger().append("ERROR creating stack with name " + stackBean.getStackName() + ". Operation timedout. Try increasing the timeout period in your stack configuration.");
 				build.setResult(Result.FAILURE);
 				break;
 			}
 
 		}
 
 		return new Environment() {
 			@Override
 			public boolean tearDown(AbstractBuild build, BuildListener listener)
 					throws IOException, InterruptedException {
 
 				boolean result = true;
 
 				List<CloudFormation> reverseOrder = new ArrayList<CloudFormation>(cloudFormations);
 				Collections.reverse(reverseOrder);
 
 				for (CloudFormation cf : reverseOrder) {
                     // automatically delete the stack?
                     if (cf.getAutoDeleteStack()) {
                         // delete the stack
                         result = result && cf.delete();
                     }
 				}
 
 				return result;
 
 			}
 
 		};
 	}
 
 	protected CloudFormation newCloudFormation(StackBean stackBean,
 			AbstractBuild<?, ?> build, EnvVars env, PrintStream logger) throws IOException {
 		
 		return new CloudFormation(logger, stackBean.getStackName(), build
 				.getWorkspace().child(stackBean.getCloudFormationRecipe())
 				.readToString(), stackBean.getParsedParameters(env),
				stackBean.getTimeout(), stackBean.getParsedAwsAccessKey(env),
				stackBean.getParsedAwsSecretKey(env), stackBean.getAutoDeleteStack());
 		
 	}
 
 	@Extension
 	public static class DescriptorImpl extends BuildWrapperDescriptor {
 
 		@Override
 		public String getDisplayName() {
 			return "Create AWS Cloud Formation stack";
 		}
 
 		@Override
 		public boolean isApplicable(AbstractProject<?, ?> item) {
 			return true;
 		}
 
 	}
 
 	public List<StackBean> getStacks() {
 		return stacks;
 	}
 
 }
