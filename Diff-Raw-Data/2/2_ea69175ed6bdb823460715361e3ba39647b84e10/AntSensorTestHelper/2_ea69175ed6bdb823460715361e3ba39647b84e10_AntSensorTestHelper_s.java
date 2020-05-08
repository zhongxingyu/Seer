 package org.hackystat.sensor.ant.test;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
 import org.hackystat.sensorbase.server.Server;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 /**
  * Provides a helper method for Ant sensor test case development. 
  * @author Philip Johnson
  *
  */
 public class AntSensorTestHelper {
   
   /** The test user. */
   protected static final String user = "TestAntSensors@hackystat.org";
   /** The test host. */
   protected static String host;
   /** The test sensorbase server. */
   protected static Server server;
 
   /**
    * Starts the server going for these tests, and makes sure our test user is registered. 
    * @throws Exception If problems occur setting up the server. 
    */
   @BeforeClass
   public static void setupServer() throws Exception {
    server = Server.newInstance();
     host = server.getHostName();
     SensorBaseClient.registerUser(host, user);
   }
   
   /**
    * Gets rid of the sent sensor data and the user. 
    * @throws Exception If problems occur setting up the server.
    */
   @AfterClass 
   public static void teardownServer() throws Exception {
     // Now delete all data sent by this user.
     SensorBaseClient client = new SensorBaseClient(host, user, user);
     // First, delete all sensor data sent by this user. 
     SensorDataIndex index = client.getSensorDataIndex(user);
     for (SensorDataRef ref : index.getSensorDataRef()) {
       client.deleteSensorData(user, ref.getTimestamp());
     }
     // Now delete the user too.
     client.deleteUser(user);
   }
   
   
   /**
    * Returns a list of XML files in the passed testFileDir directory. 
    * @param testFileDir The directory in which the test files are found.
    * @return The list of XML files. 
    * @throws Exception If the test directory cannot be found.
    */
   public List<File> getXmlFiles(String testFileDir) throws Exception {
     File directory = new File(testFileDir);
     
     // Make sure this is a directory, otherwise fail.
     if (!directory.isDirectory()) {
       throw new Exception ("The testFilePath passed was not a directory: " + testFileDir);
     }
     
     File[] files = directory.listFiles();
 
     // create a file filter that only accepts xml files
     FileFilter filter = new FileFilter() {
       public boolean accept(File pathname) {
         if (pathname.getName().endsWith(".xml")) {
           return true;
         }
         return false;
       }
     };
     
     List<File> fileList = new ArrayList<File>();
     // Generate the list of XML files. 
     for (int j = 0; j < files.length; j++) {
       if (filter.accept(files[j])) {
         fileList.add(files[j]);
       }
     }
     return fileList;
   } 
 
 }
