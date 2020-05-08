 package com.attask.jenkins;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 import org.apache.tools.ant.DirectoryScanner;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 import java.io.*;
 import java.util.List;
 
 /**
  * User: joeljohnson
  * Date: 3/6/12
  * Time: 4:52 PM
  */
 public class WaitForBuildStep extends Builder {
 	public final String jobName;
 	public final String buildNumber;
 	public final int retries;
 	public final int delay;
 	public final String filesToCopy;
 	public final boolean copyBuildResult;
 	public final boolean failOnFailure;
 	public final int numberLogLinesToCopyOnFailure;
 	public final String statusVariableName;
     public final String runOnCondition;
 
     @DataBoundConstructor
     public WaitForBuildStep(String jobName, String buildNumber, int retries, int delay, String filesToCopy, boolean copyBuildResult, boolean failOnFailure, int numberLogLinesToCopyOnFailure, String statusVariableName, String runOnCondition) throws FormValidation {
         this.jobName = jobName;
 		this.buildNumber = buildNumber;
         this.runOnCondition = runOnCondition;
         this.retries = retries < 0 ? 0 : retries;
 		this.delay = delay < 5000 ? 5000 : delay;
 		this.filesToCopy = filesToCopy;
 		this.copyBuildResult = copyBuildResult;
 		this.failOnFailure = failOnFailure;
 		this.numberLogLinesToCopyOnFailure = numberLogLinesToCopyOnFailure;
 		this.statusVariableName = statusVariableName;
     }
 
 	@Override
 	public boolean perform(final AbstractBuild build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
 		final PrintStream logger = listener.getLogger();
 
         String runOnConditionExpanded = build.getEnvironment(listener).expand(this.runOnCondition);
         if (!TriggerJobBuildStep.shouldRun(runOnConditionExpanded)) {
             listener.getLogger().println("Not waiting for job '" + jobName + "' since 'Only run if this value is true' is '" + runOnConditionExpanded + "'");
             return true;
         }
 
 		EnvVars envVars = build.getEnvironment(listener);
 		Hudson jenkins = Hudson.getInstance();
 
 		String jobName = envVars.expand(this.jobName);
 		TopLevelItem topLevelItem = jenkins.getItem(jobName);
 
 		if(topLevelItem == null || !(topLevelItem instanceof Job)) {
 			logger.println(jobName + " is not a Job {" + topLevelItem + "}");
 			return false;
 		}
 
 		int buildNumber = Integer.parseInt(envVars.expand(this.buildNumber));
 
 		Job job = (Job) topLevelItem;
 		final Run buildToWaitFor = job.getBuildByNumber(buildNumber);
 		Waiter wait = new Waiter(retries, delay);
 		boolean waitResult = wait.retryUntil(new Waiter.Predicate() {
 			public boolean call() {
 				try {
 					logger.print("Checking status of build ");
 					listener.hyperlink(getRootUrl() + buildToWaitFor.getUrl(), buildToWaitFor.getFullDisplayName());
 					if (buildToWaitFor.isBuilding()) {
 						logger.print(" (building)");
 						logger.println();
 						return false;
 					} else {
 						logger.print(" (complete)");
 						logger.println();
 						return true;
 					}
 				} catch (IOException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		});
 		if(!waitResult) {
 			listener.hyperlink(getRootUrl() + buildToWaitFor.getUrl(), buildToWaitFor.getFullDisplayName());
 			logger.println(" didn't finish");
 		} else {
 			Result downstreamResult = buildToWaitFor.getResult();
 			if(copyBuildResult) {
 				build.setResult(downstreamResult);
 				listener.error("Downstream build ended with the status: " + downstreamResult);
 				if(downstreamResult.isWorseOrEqualTo(Result.FAILURE) && failOnFailure) {
 					listener.error("Downstream build failed. Will not continue this build.");
 					waitResult = false; //don't continue with other jobs
 				}
 			}
 			logger.println("Copying artifacts from downstream build.");
 			copyArtifacts(filesToCopy, buildToWaitFor, listener, build);
 
 			String statusActionValue;
 			if(downstreamResult.isWorseOrEqualTo(Result.FAILURE)) {
 				statusActionValue = "false";
 				if (numberLogLinesToCopyOnFailure > 0) {
 
 					@SuppressWarnings("unchecked")
 					List<String> log = buildToWaitFor.getLog(numberLogLinesToCopyOnFailure+1); //Add one more because it adds the "truncated X lines" as the first line
 
 					int numberPrinting = log.size() >= numberLogLinesToCopyOnFailure ? numberLogLinesToCopyOnFailure : log.size();
 					logger.println(buildToWaitFor.getFullDisplayName() + " failed. Here's the last " + numberPrinting + " console lines:");
 					for (String s : log) {
 						logger.println("["+buildToWaitFor.getFullDisplayName()+"]"+s);
 					}
 				}
 			} else {
 				statusActionValue = "true";
 			}
 			if(statusVariableName != null && !statusVariableName.isEmpty()) {
 				build.addAction(new EnvAction(statusVariableName, statusActionValue));
 			}
 		}
 		return waitResult;
 	}
 
 	public static String getRootUrl() {
 		String rootUrl = Hudson.getInstance().getRootUrl();
 		return rootUrl == null ? "/" : rootUrl;
 	}
 
 	private void copyArtifacts(String filesToCopy, Run waitedForBuild, BuildListener listener, AbstractBuild<?, ?> currentBuild) {
 		if (filesToCopy == null || filesToCopy.isEmpty() || waitedForBuild == null) {
 			return;
 		}
 		DirectoryScanner scanner = new DirectoryScanner();
 		scanner.setIncludes(new String[]{filesToCopy});
 		scanner.setBasedir(waitedForBuild.getArtifactsDir());
 		scanner.scan();
 		String[] includedFiles = scanner.getIncludedFiles();
 
 		for (String includedFile : includedFiles) {
 			try {
 				FilePath artifactDirectory = currentBuild.getWorkspace();
 				if (!artifactDirectory.child(jobName).exists()) {
 					listener.getLogger().println("Directory: " + jobName + " does not exist. Creating directory: " + jobName);
 					artifactDirectory.child(jobName).mkdirs();
 					listener.getLogger().println("Created directory: " + jobName);
 				}
 
 				File sourceFile = new File(waitedForBuild.getArtifactsDir(), includedFile);
 				FilePath copiedArtifact = artifactDirectory.child(jobName).child(sourceFile.getName());
 				try {
 					copiedArtifact.copyFrom(sourceFile.toURL());
 					listener.getLogger().println("Copied artifact: '" + includedFile + "' to '" + artifactDirectory.absolutize().toString() + "'");
 				} catch (FileNotFoundException e) {
 					listener.error(e.getMessage());
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace(listener.getLogger());
				break;
 			} catch (IOException e) {
 				e.printStackTrace(listener.getLogger());
 			}
 		}
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends com.attask.jenkins.BuildStepDescriptor {
 		public FormValidation doCheckBuildNumber(@QueryParameter(value = "buildNumber", required = true) String value,
 												 @QueryParameter(value = "jobName", required = true) String jobName
 		) {
 			try {
 				if(value == null || value.isEmpty()) {
 					return FormValidation.error("Build Number is a required field");
 				}
 				if(value.contains("$")) {
 //					return FormValidation.warning("It appears you are using a variable. Unable to validate the Build Number.");
 					return FormValidation.ok();
 				}
 				if(jobName.contains("$")) {
 //					return FormValidation.warning("It appears you are using a variable for Job Name. Unable to validate the Build Number.");
 					return FormValidation.ok();
 				}
 
 				int buildNumber = Integer.parseInt(value);
 				if(buildNumber <= 0) {
 					return FormValidation.error("Build Number must be a valid positive/non-zero number or an environment variable.");
 				}
 				TopLevelItem topLevelItem = Hudson.getInstance().getItem(jobName);
 				if(!(topLevelItem instanceof Job)) {
 					return FormValidation.warning("Cannot validate Build Number without a valid Job Name.");
 				}
 
 				Job job = (Job)topLevelItem;
 				if(job.getBuildByNumber(buildNumber) == null) {
 					return FormValidation.error("Build with given number does not exist for the given Job name.");
 				}
 			} catch(NumberFormatException e) {
 				return FormValidation.error("Build Number must be a valid positive/non-zero number or an environment variable.");
 			}
 
 			return FormValidation.ok();
 		}
 
 		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
 			return true;
 		}
 
 		public String getDisplayName() {
 			return "Wait for build to finish";
 		}
 	}
 }
