 package org.hackystat.projectbrowser.test;
 
 import java.util.Properties;
 import org.hackystat.projectbrowser.ProjectBrowserProperties;
 import org.junit.BeforeClass;
 
 /**
  * Supports JUnit testing of the ProjectBrowser by creating test instances of the sensorbase,
  * dailyprojectdata, and telemetry services. 
  * @author Philip Johnson
  *
  */
 public class ProjectBrowserTestHelper {
   /** The Sensorbase server used in these tests. */
   @SuppressWarnings("unused")
   private static org.hackystat.sensorbase.server.Server sensorbaseServer;
   /** The DailyProjectData server used in these tests. */
   @SuppressWarnings("unused")
   private static org.hackystat.dailyprojectdata.server.Server dpdServer;  
   /** The Telemetry server used in these tests. */
   private static org.hackystat.telemetry.service.server.Server telemetryServer;  
   
 
   /**
    * Constructor.
    */
   public ProjectBrowserTestHelper () {
     // Does nothing.
   }
   
   /** 
    * Starts the servers going for these tests. 
    * @throws Exception If problems occur setting up the server. 
    */
   @BeforeClass 
   public static void setupServer() throws Exception {
     // Create testing versions of the Sensorbase, DPD, and Telemetry servers.
    ProjectBrowserTestHelper.sensorbaseServer = 
      org.hackystat.sensorbase.server.Server.newTestInstance();
    ProjectBrowserTestHelper.dpdServer = 
      org.hackystat.dailyprojectdata.server.Server.newTestInstance(); 
     ProjectBrowserTestHelper.telemetryServer = 
       org.hackystat.telemetry.service.server.Server.newTestInstance();
   }
 
   /**
    * Returns the hostname associated with this Telemetry test server. 
    * @return The host name, including the context root. 
    */
   protected String getTelemetryHostName() {
     return ProjectBrowserTestHelper.telemetryServer.getHostName();
   }
   
   /**
    * Returns the sensorbase hostname that this Telemetry server communicates with.
    * @return The host name, including the context root. 
    */
   protected String getSensorBaseHostName() {
     return ProjectBrowserTestHelper.sensorbaseServer.getHostName();
   }
   
   /**
    * Returns the sensorbase hostname that this Telemetry server communicates with.
    * @return The host name, including the context root. 
    */
   protected String getDailyProjectDataHostName() {
     return ProjectBrowserTestHelper.dpdServer.getHostName();
   }
   
   /**
    * Returns a Properties instance with the SensorBase, DPD, and Telemetry services 
    * ProjectBrowserProperties set to test values. 
    * @return A Properties instances with the service host names set to test values. 
    */
   protected Properties getTestProperties() {
     Properties testProperties = new Properties();
     testProperties.put(ProjectBrowserProperties.SENSORBASE_HOST_KEY, getSensorBaseHostName());
     testProperties.put(ProjectBrowserProperties.DAILYPROJECTDATA_HOST_KEY, 
         getDailyProjectDataHostName());
     testProperties.put(ProjectBrowserProperties.TELEMETRY_HOST_KEY, getTelemetryHostName());
     return testProperties;
     
     
 
   }
 }
