 package org.hackystat.dailyprojectdata.test;
 
 import org.hackystat.dailyprojectdata.server.Server;
 import org.junit.BeforeClass;
 
 public class DailyProjectDataTestHelper {
 
   /** The DailyProjectData server used in these tests. */
   private static Server server;
 
  /** The SensorBase URI. */
  private static String sensorBaseHost; 

   /**
    * Constructor.
    */
   public DailyProjectDataTestHelper () {
   }
   
   /**
    * Starts the server going for these tests. 
    * @throws Exception If problems occur setting up the server. 
    */
   @BeforeClass public static void setupServer() throws Exception {
     DailyProjectDataTestHelper.server = Server.newInstance();
    DailyProjectDataTestHelper.sensorBaseHost = server.get
    
    
   }
 
   /**
    * Returns the hostname associated with this test server. 
    * @return The host name, including the context root. 
    */
   protected String getHostName() {
     return DailyProjectDataTestHelper.server.getHostName();
   }
 }
 
