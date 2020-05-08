 package com.attask.jenkins;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.Builder;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.*;
 
 /**
  * User: joeljohnson
  * Date: 3/6/12
  * Time: 9:07 PM
  */
 @ExportedBean
 public class TriggerJobBuildStep extends Builder {
 	private final String jobName;
 	private final String envVarName;
 	private final String parameters;
 	private final int waitLimitMinutes;
 	private final String runOnCondition;
 
 
 	@DataBoundConstructor
 	public TriggerJobBuildStep(String jobName, String envVarName, String parameters, int waitLimitMinutes, String runOnCondition) {
 		this.jobName = jobName;
 		this.envVarName = envVarName;
 		this.parameters = parameters;
 		this.waitLimitMinutes = waitLimitMinutes <= 0 ? 15 : waitLimitMinutes;
 		this.runOnCondition = runOnCondition;
 	}
 
 	@Exported
 	public String getJobName() {
 		return jobName;
 	}
 
 	@Exported
 	public String getEnvVarName() {
 		return envVarName;
 	}
 
 	@Exported
 	public String getParameters() {
 		return parameters;
 	}
 
 	@Exported
 	public int getWaitLimitMinutes() {
 		return waitLimitMinutes;
 	}
 
 	@Exported
 	public String getRunOnCondition() {
 		return runOnCondition;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
 		String runOnConditionExpanded = build.getEnvironment(listener).expand(this.runOnCondition);
         if (!shouldRun(runOnConditionExpanded)) {
             listener.getLogger().println("Not triggering job '" + jobName + "' since 'Only run if this value is true' is '" + runOnConditionExpanded + "'");
             return true;
         }
 
         EnvVars envVars = build.getEnvironment(listener);
 		final String variableName = envVars.expand(this.envVarName);
 		String jobName = envVars.expand(this.jobName);
 		TopLevelItem topLevelItem = Hudson.getInstance().getItem(jobName);
 		if(topLevelItem == null || !(topLevelItem instanceof AbstractProject)) {
 			listener.getLogger().println(jobName + " is not a Project {" + topLevelItem + "}");
 			return false;
 		}
 
 		final AbstractProject job = (AbstractProject)topLevelItem;
 		final int nextBuildNumber = triggerBuild(build, listener, job, build.getEnvironment(listener));
 		if(nextBuildNumber < 0) {
 			listener.error("Couldn't start the build. Error code: " + nextBuildNumber);
 			return false;
 		}
 
 		if(variableName != null && !variableName.isEmpty()) {
 			listener.getLogger().println("setting environment variable '" + variableName + "' to '" + nextBuildNumber + "'");
 			build.addAction(new EnvAction(variableName, String.valueOf(nextBuildNumber)));
 		}
 		return true;
 	}
 
     public static boolean shouldRun(String runOnConditionExpanded) {
         if(runOnConditionExpanded != null && !runOnConditionExpanded.isEmpty() && !isAffirmativeWord(runOnConditionExpanded)) {
             return false;
         }
         return true;
     }
 
     /**
 	 * Checks if the given word is either "true" or "yes".
 	 * The check is case-insensitive, and any white space is trimmed from the start and end of the given string.
 	 * @return False if the given word is null or is not "true" or "yes".
 	 */
 	private static boolean isAffirmativeWord(String word) {
         if (word == null) {
             return false;
         }
         boolean inverse = false;
         if (word.startsWith("!")) {
             inverse = true;
             word = word.substring(1);
         }
         boolean result = Boolean.parseBoolean(word);
         /*
           result = true && inverse = true: return false
           result = true && inverse = false: return true
           result = false && inverse = true: return true
           result = false && inverse = false: return false
          */
         return result ^ inverse;
     }
 
 	private int triggerBuild(Run upstreamRun, BuildListener listener, final AbstractProject jobToStart, EnvVars vars) throws IOException {
 		int nextBuildNumber = jobToStart.getNextBuildNumber();
 		boolean addedToQueue = jobToStart.scheduleBuild(0, new Cause.UpstreamCause(upstreamRun), getParameterActions(jobToStart, vars.expand(parameters), listener));
 		if(!addedToQueue) {
 			listener.error("Didn't start job! Apparently the same job is queued.");
 			return -1;
 		}
 
 		listener.getLogger().print("Queued job ");
 		listener.hyperlink(WaitForBuildStep.getRootUrl() + jobToStart.getUrl(), jobToStart.getFullDisplayName());
 		listener.getLogger().println();
 
 		int waitTimeMillis = 1000;
 		for(int attempt = 0; attempt < waitLimitMinutes * 6; attempt++) {
 			for(int jobNumber = nextBuildNumber; jobNumber < jobToStart.getNextBuildNumber(); jobNumber++) {
 				Run run = jobToStart.getBuildByNumber(jobNumber);
				if(run == null) {
					continue;
				}
 				@SuppressWarnings("unchecked")
 				Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause)run.getCause(Cause.UpstreamCause.class);
 				if(upstreamCause == null || !upstreamCause.pointsTo(upstreamRun)) {
 					continue;
 				}
 
 				listener.getLogger().print("Started job ");
 				listener.hyperlink(WaitForBuildStep.getRootUrl() + jobToStart.getUrl(), jobToStart.getFullDisplayName());
 				listener.getLogger().print(" ");
 				listener.hyperlink(WaitForBuildStep.getRootUrl() + run.getUrl(), run.getDisplayName());
 				listener.getLogger().println();
 
 				return run.getNumber();
 			}
 
 			try {
 				//wait for one second to start with, then double the wait time every time.
 				listener.getLogger().println("Job hasn't started. Waiting for "+ (waitTimeMillis / 1000) +" seconds.");
 				Thread.sleep(waitTimeMillis);
 				waitTimeMillis *= 2;
 				if(waitTimeMillis > 30000) {
 					waitTimeMillis = 30000; //Don't allow a single wait last more than 30 seconds.
 				}
 			} catch (InterruptedException e) {
 				listener.fatalError(e.getMessage());
				break;
 			}
 		}
 
 		listener.error("Couldn't find started job!");
 		return -2;
 	}
 
 	private Action getParameterActions(AbstractProject project, String parameters, BuildListener listener) {
 		PrintStream logger = listener.getLogger();
 		List<ParameterValue> result = new ArrayList<ParameterValue>();
 		Map<String, String> propertiesMap = getPropertiesMap(parameters);
 
 		@SuppressWarnings("unchecked")
 		ParametersDefinitionProperty projectProperties = (ParametersDefinitionProperty)project.getProperty(ParametersDefinitionProperty.class);
 		if(projectProperties != null) {
 			List<ParameterDefinition> parameterDefinitions = projectProperties.getParameterDefinitions();
 			if(parameterDefinitions != null) {
 				for (ParameterDefinition parameterDefinition : parameterDefinitions) {
 					String propertyName = parameterDefinition.getName();
 					if(propertiesMap.containsKey(propertyName)) {
 						String value = propertiesMap.get(propertyName);
 						logger.println("using variable: '" + propertyName + "' -> '" + value + "'");
 						result.add(new StringParameterValue(propertyName, value));
 					} else {
 						ParameterValue defaultParameterValue = parameterDefinition.getDefaultParameterValue();
 						logger.println("using default for: '" + defaultParameterValue.getName() + "' -> '" + defaultParameterValue.toString() + "'");
 						result.add(defaultParameterValue);
 					}
 				}
 			}
 		}
 
 		return new ParametersAction(result);
 	}
 
 	private Map<String, String> getPropertiesMap(String parameters) {
 		String[] split = parameters.split("\n");
 		Map<String, String> properties = new HashMap<String, String>();
 		for(int i = 0; i < split.length; i++) {
 			String lineWithoutComments = split[i].split("#", 2)[0];
 			String[] property = lineWithoutComments.split("=", 2);
 			if(property.length == 2) {
 				properties.put(property[0].trim(), property[1].trim());
 			}
 		}
 		return properties;
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends com.attask.jenkins.BuildStepDescriptor {
 		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
 			return true;
 		}
 
 		public String getDisplayName() {
 			return "Trigger a build";
 		}
 	}
 }
