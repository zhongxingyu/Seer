 package com.attask.jenkins;
 
 import hudson.Extension;
 import hudson.matrix.*;
 import hudson.model.*;
 import hudson.model.queue.QueueTaskFuture;
 import jenkins.model.Jenkins;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.tools.ant.filters.StringInputStream;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Logger;
 
 /**
  * User: Joel Johnson
  * Date: 1/15/13
  * Time: 4:31 PM
  */
 public class PreMatrixJob extends DefaultMatrixExecutionStrategyImpl {
 	private static final Logger log = Logger.getLogger("TriggerJobPreMatrix");
 
 	private String preJobName;
 	private String preJobParameters;
 	private String prePropertiesFileToInject;
 
 	private String postJobName;
 	private String postJobParameters;
 	private String postPropertiesFileToInject;
 
 	@DataBoundConstructor
 	public PreMatrixJob(String preJobName, String preJobParameters, String prePropertiesFileToInject,
 						String postJobName, String postJobParameters, String postPropertiesFileToInject) {
 		super(false, false, "", null, new NoopMatrixConfigurationSorter());
 
 		this.preJobName = preJobName;
 		this.preJobParameters = preJobParameters;
 		this.prePropertiesFileToInject = prePropertiesFileToInject;
 
 		this.postJobName = postJobName;
 		this.postJobParameters = postJobParameters;
 		this.postPropertiesFileToInject = postPropertiesFileToInject;
 	}
 
 	@Override
 	public Result run(MatrixBuild.MatrixBuildExecution execution) throws InterruptedException, IOException {
 		BuildListener listener = execution.getListener();
 		Run build = execution.getBuild();
 
 		preRun(build, listener);
 
 		Result result;
 		try {
 			result = super.run(execution);
 		} finally {
 			postRun(build, listener);
 		}
 
 		return result;
 	}
 
 	private static void runJob(Run build, BuildListener listener, String jobName, String jobParameters, String propertiesFileToInject) throws InterruptedException, IOException {
 		log.info(build.getFullDisplayName() + " running " + jobName + " before matrix jobs.");
 		@SuppressWarnings("unchecked")
 		AbstractProject<?, ? extends AbstractBuild> jobToRun = Jenkins.getInstance().getItemByFullName(jobName, AbstractProject.class);
 
 		listener.getLogger().println("Scheduling " + jobName);
 		QueueTaskFuture<? extends AbstractBuild> future = jobToRun.scheduleBuild2(0, new Cause.UpstreamCause(build), parseParameters(jobParameters));
		if (future == null) {
			String errorMessage = build.getFullDisplayName() + " was unable to schedule " + jobName + ". This could be for a number of reasons. Make sure the build is able to do concurrent builds and isn't disabled.";
			log.warning(errorMessage);
			throw new NullPointerException(errorMessage);
		}

 		try {
 			AbstractBuild abstractBuild = future.waitForStart();
			if(abstractBuild == null) {
				log.severe("The build's waitForStart future returned null. This is most likely a bug in Jenkins core.");
				throw new NullPointerException("The build's waitForStart future returned null. This is most likely a bug in Jenkins core.");
			}
 
 			listener.getLogger().print("Running ");
			listener.hyperlink("../../../" + abstractBuild.getUrl(), abstractBuild.getFullDisplayName());
 			listener.getLogger().println(".");
		} catch (ExecutionException e) {
			log.severe(e.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(e));
			throw new RuntimeException(e);
		}
 
		try {
			AbstractBuild abstractBuild = future.get();
			if(abstractBuild == null) {
				log.severe("The build's future returned null. This is most likely a bug in Jenkins core.");
				throw new NullPointerException("The build's future returned null. This is most likely a bug in Jenkins core.");
			}
 
 			listener.getLogger().print("Finished running ");
			listener.hyperlink("../../../" + abstractBuild.getUrl(), abstractBuild.getFullDisplayName());
 			listener.getLogger().println(".");
 
 			if(propertiesFileToInject != null && !propertiesFileToInject.isEmpty()) {
 				listener.getLogger().println("Injecting " + propertiesFileToInject);
 				File artifactsDir = abstractBuild.getArtifactsDir();
 				File artifact = new File(artifactsDir, propertiesFileToInject);
 
 				FileInputStream inputStream = new FileInputStream(artifact);
 				Properties properties;
 				try {
 					properties = new Properties();
 					properties.load(inputStream);
 				} finally {
 					inputStream.close();
 				}
 
 				build.addAction(new MapInjectorAction(properties));
 			}
 		} catch (ExecutionException e) {
 			log.severe(e.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(e));
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static ParametersAction parseParameters(String propertiesStr) {
 		StringInputStream inStream = new StringInputStream(propertiesStr);
 		return parseParameters(inStream);
 	}
 
 	private static ParametersAction parseParameters(InputStream inStream) {
 		List<ParameterValue> values = new LinkedList<ParameterValue>();
 
 		Properties properties = new Properties();
 		try {
 
 			properties.load(inStream);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		for (Object keyObj : properties.keySet()) {
 			String key = String.valueOf(keyObj);
 			String value = properties.getProperty(key);
 			values.add(new StringParameterValue(key, value));
 		}
 
 		return new ParametersAction(values);
 	}
 
 	private void preRun(Run build, BuildListener listener) throws InterruptedException, IOException {
 		runJob(build, listener, getPreJobName(), getPreJobParameters(), getPrePropertiesFileToInject());
 	}
 
 	private void postRun(Run build, BuildListener listener) throws InterruptedException, IOException {
 		runJob(build, listener, getPostJobName(), getPostJobParameters(), getPostPropertiesFileToInject());
 	}
 
 	public String getPreJobName() {
 		return preJobName;
 	}
 
 	public String getPreJobParameters() {
 		return preJobParameters;
 	}
 
 	public String getPrePropertiesFileToInject() {
 		return prePropertiesFileToInject;
 	}
 
 	public String getPostJobName() {
 		return postJobName;
 	}
 
 	public String getPostJobParameters() {
 		return postJobParameters;
 	}
 
 	public String getPostPropertiesFileToInject() {
 		return postPropertiesFileToInject;
 	}
 
 	@Extension
 	public static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
 		@Override
 		public String getDisplayName() {
 			return "Trigger Job Before/After Matrix Builds";
 		}
 	}
 }
