 package nl.codecentric.jenkins.appd;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 import hudson.util.FormValidation;
 import hudson.util.ListBoxModel;
 import nl.codecentric.jenkins.appd.rest.RestConnection;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import static nl.codecentric.jenkins.appd.util.LocalMessages.PUBLISHER_DISPLAYNAME;
 
 /**
  * Main class for this Jenkins Plugin.<br />
  * Hooks into the build flow as post-build step, then collecting data and generating the report.<br /><br />
  * <p/>
  * Configuration is set from the Jenkins Build Configuration menu. When a build is triggered, the
  * {@link AppDynamicsResultsPublisher#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)}
  * method is called. This will then trigger the {@link AppDynamicsDataCollector} and parse any results and produces
  * {@link AppDynamicsReport}'s.<br />
  * A {@link AppDynamicsBuildAction} is used to store data per-build, so it can be compared later.
  */
 public class AppDynamicsResultsPublisher extends Recorder {
 
   private static final String DEFAULT_USERNAME = "username@customer1";
   private static final int DEFAULT_THRESHOLD_UNSTABLE = 80;
   private static final int DEFAULT_THRESHOLD_FAILED = 65;
 
   public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
     @Override
     public String getDisplayName() {
       return PUBLISHER_DISPLAYNAME.toString();
     }
 
     @Override
     public String getHelpFile() {
       return "/plugin/appdynamics-dashboard/help.html";
     }
 
     @Override
     public boolean isApplicable(Class<? extends AbstractProject> jobType) {
       return true;
     }
 
     public String getDefaultUsername() {
       return DEFAULT_USERNAME;
     }
 
     public int getDefaultUnstableThreshold() {
       return DEFAULT_THRESHOLD_UNSTABLE;
     }
 
     public int getDefaultFailedThreshold() {
       return DEFAULT_THRESHOLD_FAILED;
     }
 
     public ListBoxModel doFillThresholdMetricItems() {
       ListBoxModel model = new ListBoxModel();
 
       for (String value : AppDynamicsDataCollector.getAvailableMetricPaths()) {
         model.add(value);
       }
 
       return model;
     }
 
     public FormValidation doCheckAppdynamicsRestUri(@QueryParameter final String appdynamicsRestUri) {
       FormValidation validationResult;
 
       if (RestConnection.validateRestUri(appdynamicsRestUri)) {
         validationResult = FormValidation.ok();
       } else {
         validationResult = FormValidation.error("AppDynamics REST uri is not valid, cannot be empty and has to " +
             "start with 'http://' or 'https://'");
       }
 
       return validationResult;
     }
 
     public FormValidation doCheckUsername(@QueryParameter final String username) {
       FormValidation validationResult;
 
       if (RestConnection.validateUsername(username)) {
         validationResult = FormValidation.ok();
       } else {
         validationResult = FormValidation.error("Username for REST interface cannot be empty");
       }
 
       return validationResult;
     }
 
     public FormValidation doCheckPassword(@QueryParameter final String password) {
       FormValidation validationResult;
 
       if (RestConnection.validatePassword(password)) {
         validationResult = FormValidation.ok();
       } else {
         validationResult = FormValidation.error("Password for REST interface cannot be empty");
       }
 
       return validationResult;
     }
 
     public FormValidation doCheckApplicationName(@QueryParameter final String applicationName) {
       FormValidation validationResult;
 
       if (RestConnection.validateApplicationName(applicationName)) {
         validationResult = FormValidation.ok();
       } else {
         validationResult = FormValidation.error("AppDynamics application name cannot be empty");
       }
 
       return validationResult;
     }
 
     public FormValidation doTestAppDynamicsConnection(@QueryParameter("appdynamicsRestUri") final String appdynamicsRestUri,
                                                       @QueryParameter("username") final String username,
                                                       @QueryParameter("password") final String password,
                                                       @QueryParameter("applicationName") final String applicationName) {
       FormValidation validationResult;
       RestConnection connection = new RestConnection(appdynamicsRestUri, username, password, applicationName);
 
       if (connection.validateConnection()) {
         validationResult = FormValidation.ok("Connection successful");
       } else {
         validationResult = FormValidation.warning("Connection with AppDynamics RESTful interface could not be established");
       }
 
       return validationResult;
     }
   }
 
   @Extension
   public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
   private RestConnection connection;
   /**
    * Below fields are configured via the <code>config.jelly</code> page.
    */
   private String appdynamicsRestUri = "";
   private String username = "";
   private String password = "";
   private String applicationName = "";
   private String thresholdMetric;
   private Boolean lowerIsBetter = true;
   private Integer performanceFailedThreshold;
   private Integer performanceUnstableThreshold;
 
   @DataBoundConstructor
   public AppDynamicsResultsPublisher(final String appdynamicsRestUri, final String username,
                                      final String password, final String applicationName,
                                      final String thresholdMetric, final Boolean lowerIsBetter,
                                      final Integer performanceFailedThreshold,
                                      final Integer performanceUnstableThreshold) {
     setAppdynamicsRestUri(appdynamicsRestUri);
     setUsername(username);
     setPassword(password);
     setApplicationName(applicationName);
     setThresholdMetric(thresholdMetric);
     setLowerIsBetter(lowerIsBetter);
     setPerformanceFailedThreshold(performanceFailedThreshold);
     setPerformanceUnstableThreshold(performanceUnstableThreshold);
   }
 
   @Override
   public BuildStepDescriptor<Publisher> getDescriptor() {
     return DESCRIPTOR;
   }
 
   @Override
   public Action getProjectAction(AbstractProject<?, ?> project) {
     return new AppDynamicsProjectAction(project);
   }
 
   public BuildStepMonitor getRequiredMonitorService() {
     // No synchronization necessary between builds
     return BuildStepMonitor.NONE;
   }
 
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
       throws InterruptedException, IOException {
     PrintStream logger = listener.getLogger();
 
     RestConnection connection = new RestConnection(appdynamicsRestUri, username, password, applicationName);
     logger.println("Verify connection to AppDynamics REST interface ...");
     if (!connection.validateConnection()) {
       logger.println("Connection to AppDynamics REST interface unsuccessful, cannot proceed with this build step");
       if (build.getResult().isBetterOrEqualTo(Result.UNSTABLE))
         build.setResult(Result.FAILURE);
       return true;
     }
 
     logger.println("Connection successful, continue to fetch measurements from AppDynamics Controller ...");
 
     AppDynamicsDataCollector dataCollector = new AppDynamicsDataCollector(connection, build);
     AppDynamicsReport report = dataCollector.createReportFromMeasurements();
 
     AppDynamicsBuildAction buildAction = new AppDynamicsBuildAction(build, report);
     build.addAction(buildAction);
 
     logger.println("Ready building AppDynamics report");
     logger.println("Verifying for improving or degrading performance, main metric: " + thresholdMetric +
         " where lower is better = " + lowerIsBetter);
 
     try {
       // Verify if the necessary metric is successfully fetched.
       report.getMetricByKey(this.thresholdMetric);
     } catch (Exception e) {
       logger.println("Unable to fetch (threshold) metric to determine if build is degrading. Aborting");
       if (build.getResult().isBetterOrEqualTo(Result.UNSTABLE))
         build.setResult(Result.FAILURE);
       return true;
     }
 
     if (performanceUnstableThreshold >= 0 && performanceUnstableThreshold <= 100) {
       logger.println("Performance degradation greater or equal than "
           + performanceUnstableThreshold + "% sets the build as "
           + Result.UNSTABLE.toString().toLowerCase());
     } else {
       logger.println("Performance: No threshold configured for making the test "
           + Result.UNSTABLE.toString().toLowerCase());
     }
     if (performanceFailedThreshold >= 0 && performanceFailedThreshold <= 100) {
       logger.println("Performance degradation greater or equal than "
           + performanceFailedThreshold + "% sets the build as "
           + Result.FAILURE.toString().toLowerCase());
     } else {
       logger.println("Performance: No threshold configured for making the test "
           + Result.FAILURE.toString().toLowerCase());
     }
 
 
     // mark the build as unstable or failure depending on the outcome.
     List<AppDynamicsReport> previousReportList = getListOfPreviousReports(build, report.getTimestamp());
     logger.println("Number of old reports located for average: " + previousReportList.size());
     double averageOverTime = calculateAverageBasedOnPreviousReports(previousReportList);
     logger.println("Calculated average from previous reports: " + averageOverTime);
 
     double currentReportAverage = report.getAverageForMetric(thresholdMetric);
     logger.println("Current report average: " + currentReportAverage);
     double performanceAsPercentageOfAverage;
     if (lowerIsBetter) {
       performanceAsPercentageOfAverage = (averageOverTime / currentReportAverage) * 100;
     } else {
       performanceAsPercentageOfAverage = (currentReportAverage / averageOverTime) * 100;
     }
     logger.println("Current average as percentage of total average: " + performanceAsPercentageOfAverage + "%");
 
     Result result;
     if (performanceFailedThreshold >= 0
         && performanceAsPercentageOfAverage - performanceFailedThreshold < 0) {
       build.setResult(Result.FAILURE);
     } else if (performanceUnstableThreshold >= 0
         && performanceAsPercentageOfAverage - performanceUnstableThreshold < 0) {
       result = Result.UNSTABLE;
       if (result.isWorseThan(build.getResult())) {
         build.setResult(result);
       }
     }
 
     logger.println("Metric: " + thresholdMetric
         + " reported performance compared to average of " + performanceAsPercentageOfAverage
         + "% . Build status is: " + build.getResult());
 
     return true;
   }
 
   private double calculateAverageBasedOnPreviousReports(final List<AppDynamicsReport> reports) {
     double calculatedSum = 0;
     int numberOfMeasurements = 0;
     for (AppDynamicsReport report : reports) {
       double value = report.getAverageForMetric(thresholdMetric);
       if (value >= 0) {
         calculatedSum += value;
         numberOfMeasurements++;
       }
     }
 
     double result = -1;
     if (numberOfMeasurements > 0) {
       result = calculatedSum / numberOfMeasurements;
     }
 
     return result;
   }
 
   private List<AppDynamicsReport> getListOfPreviousReports(final AbstractBuild<?, ?> build,
                                                            final long currentTimestamp) {
     final List<AppDynamicsReport> previousReports = new ArrayList<AppDynamicsReport>();
 
     final List<? extends AbstractBuild<?, ?>> builds = build.getProject().getBuilds();
     for (AbstractBuild<?, ?> currentBuild : builds) {
       final AppDynamicsBuildAction performanceBuildAction = currentBuild.getAction(AppDynamicsBuildAction.class);
       if (performanceBuildAction == null) {
         continue;
       }
       final AppDynamicsReport report = performanceBuildAction.getBuildActionResultsDisplay().getAppDynamicsReport();
      if (report != null && (report.getTimestamp() != currentTimestamp || builds.size() == 1)) {
         previousReports.add(report);
       }
     }
 
     return previousReports;
   }
 
   public String getAppdynamicsRestUri() {
     return appdynamicsRestUri;
   }
 
   public void setAppdynamicsRestUri(final String appdynamicsRestUri) {
     this.appdynamicsRestUri = appdynamicsRestUri;
   }
 
   public String getUsername() {
     return username;
   }
 
   public void setUsername(final String username) {
     this.username = username;
   }
 
   public String getPassword() {
     return password;
   }
 
   public void setPassword(final String password) {
     this.password = password;
   }
 
   public String getApplicationName() {
     return applicationName;
   }
 
   public void setApplicationName(final String applicationName) {
     this.applicationName = applicationName;
   }
 
   public String getThresholdMetric() {
     return thresholdMetric;
   }
 
   public void setThresholdMetric(String thresholdMetric) {
     this.thresholdMetric = thresholdMetric;
   }
 
   public Boolean getLowerIsBetter() {
     return lowerIsBetter;
   }
 
   public void setLowerIsBetter(Boolean lowerIsBetter) {
     this.lowerIsBetter = lowerIsBetter;
   }
 
   public Integer getPerformanceFailedThreshold() {
     return performanceFailedThreshold;
   }
 
   public void setPerformanceFailedThreshold(final Integer performanceFailedThreshold) {
     this.performanceFailedThreshold = Math.max(0, Math.min(performanceFailedThreshold, 100));
   }
 
   public Integer getPerformanceUnstableThreshold() {
     return performanceUnstableThreshold;
   }
 
   public void setPerformanceUnstableThreshold(final Integer performanceUnstableThreshold) {
     this.performanceUnstableThreshold = Math.max(0, Math.min(performanceUnstableThreshold, 100));
   }
 }
