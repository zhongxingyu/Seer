 package nl.codecentric.jenkins.appd;
 
 import hudson.Extension;
 import hudson.ExtensionPoint;
 import hudson.model.*;
 import nl.codecentric.jenkins.appd.rest.types.MetricData;
 import nl.codecentric.jenkins.appd.rest.RestConnection;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.util.logging.Logger;
 
 /**
  * The {@link AppDynamicsDataCollector} will eventually fetch the performance statistics from the
  * AppDynamics REST interface and parse them into a {@link AppDynamicsReport}.<br />
  * <br />
  * Perhaps create separate Collectors again when this is more logical to create separate graphs. For
  * now this single collector should get all data.
  */
 public class AppDynamicsDataCollector {
   private static final Logger LOG = Logger.getLogger(AppDynamicsDataCollector.class.getName());
   private static final String[] METRIC_PATHS = {
      "Overall Application Performance|Calls per Minute",
       "Overall Application Performance|Average Response Time (ms)",
       "Overall Application Performance|Normal Average Response Time (ms)",
       "Overall Application Performance|Number of Slow Calls",
       "Overall Application Performance|Number of Very Slow Calls",
       "Overall Application Performance|Errors per Minute",
       "Overall Application Performance|Exceptions per Minute",
       "Overall Application Performance|Infrastructure Errors per Minute"};
 
   private final RestConnection restConnection;
   private final AbstractBuild<?, ?> build;
   private final int minimumDurationInMinutes;
 
   public AppDynamicsDataCollector(final RestConnection connection, final AbstractBuild<?, ?> build,
                                   final int minimumDurationInMinutes) {
     this.restConnection = connection;
     this.build = build;
     this.minimumDurationInMinutes = minimumDurationInMinutes;
   }
 
   public static String[] getAvailableMetricPaths() {
     return METRIC_PATHS;
   }
 
   /** Parses the specified reports into {@link AppDynamicsReport}s. */
   public AppDynamicsReport createReportFromMeasurements() {
     long buildStartTime = build.getRootBuild().getTimeInMillis();
     int durationInMinutes = calculateDurationToFetch(buildStartTime);
 
     LOG.fine(String.format("Current time: %d - Build time: %d - Duration: %d", System.currentTimeMillis(),
         buildStartTime, durationInMinutes));
 
     AppDynamicsReport adReport = new AppDynamicsReport(buildStartTime, durationInMinutes);
     for (String metricPath : METRIC_PATHS) {
       MetricData metric = restConnection.fetchMetricData(metricPath, durationInMinutes);
       if (adReport != null) {
         adReport.addMetrics(metric);
       }
     }
 
     return adReport;
   }
 
 
   private int calculateDurationToFetch(final Long buildStartTime) {
     long duration = System.currentTimeMillis() - buildStartTime;
 
     int durationInMinutes = (int) (duration / (1000*60));
     if (durationInMinutes < minimumDurationInMinutes) {
       durationInMinutes = minimumDurationInMinutes;
     }
 
     return durationInMinutes;
   }
 
 }
