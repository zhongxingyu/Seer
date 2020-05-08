 package edu.msu.nscl.olog.api;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created by eschuhmacher on 2/3/14.
  */
 public class OlogClientIT {
 
     private static OlogClient client;
     private static BufferedWriter out;
 
     @BeforeClass
     public static void prepareTest() throws Exception {
         client = OlogClientImpl.OlogClientBuilder.serviceURL().withHTTPAuthentication(true).username("olog").password("olog")
                 .create();
        String filePath = OlogClientIT.class.getResource("OlogClientTest.class").getPath();
         File outputFile = new File(filePath.substring(0,filePath.indexOf("target")) + "src/test/java/edu/msu/nscl/olog/api/OlogClientTestResult.txt");
         out = new BufferedWriter(new FileWriter(outputFile));
     }
 
     @AfterClass
     public static void close() throws IOException {
         out.close();
     }
 
 
     @Test
     public void findAllProperties() throws IOException, OlogException {
         long startTime = System.nanoTime();
         Collection<Property> properties = client.listProperties();
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find all properties  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
     @Test
     public void getLogByAttribute() throws Exception {
         Map<String,String> map = new HashMap<String, String>();
         map.put("johnprop.images_collected", "0");
         map.put("limit", "20");
         long startTime = System.nanoTime();
         Collection<Log> logs = client.findLogs(map);
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find a log by attribute is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
     @Test
     public void insertLogTest2() throws IOException {
         Log log = client.getLog(107854l);
         LogBuilder builder = LogBuilder.log(log);
         builder =  builder.id(null);
         long startTime = System.nanoTime();
         Log newLog = client.set(builder);
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to insert a log  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
 
     @Test
     public void insertLogTest() throws IOException {
         Log log = client.getLog(22121l);
         LogBuilder builder = LogBuilder.log(log);
         builder =  builder.id(null);
         long startTime = System.nanoTime();
         Log newLog = client.set(builder);
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to insert a log  is: " + totalTime + "(s)");
         out.newLine();
     }
 
 
 
     @Test
     public void insertLogTest3() throws IOException {
         Log log = client.getLog(5l);
         LogBuilder builder = LogBuilder.log(log);
         builder =  builder.id(null);
         long startTime = System.nanoTime();
         Log newLog = client.set(builder);
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to insert a log  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
     @Test
     public void findLogByDateTest() throws IOException {
         Map<String,String> map = new HashMap<String, String>();
         map.put("start", String.valueOf(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH).getTime() / 1000));
         map.put("limit", "10");
         long startTime = System.nanoTime();
         Collection<Log> logs = client.findLogs(map);
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find a log by date  is: " + totalTime + "(s)");
         out.newLine();
     }
 
     @Test
     public void findLogsByProperty() throws IOException {
         long startTime = System.nanoTime();
         Collection<Log> logs = client.findLogsByProperty("sweep", "crystal_name", "OCT_AP11_3");
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find a log by property  is: " + totalTime + "(s)");
         out.newLine();
     }
 
     @Test
     public void findLogByLogbook() throws IOException {
         long startTime = System.nanoTime();
         Collection<Log> logs = client.findLogsByLogbook("testLog");
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find a log by date  is: " + totalTime + "(s)");
         out.newLine();
     }
 
     @Test
     public void findLogById() throws OlogException, IOException {
         long startTime = System.nanoTime();
         Log log = client.getLog(27455l);
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find a log by id is: " + totalTime + "(s)");
         out.newLine();
     }
 
 
     @Test
     public void removeLog() throws OlogException, IOException {
         long startTime = System.nanoTime();
         client.delete(27455l);
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to delete a log  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
 
     @Test
     public void findPropertyByName() throws IOException, OlogException {
         long startTime = System.nanoTime();
         Property property = client.getProperty("johnprop");
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find a property by name  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
 
     @Test
     public void createProperty() throws IOException, OlogException {
         long startTime = System.nanoTime();
         Property property = client.set(PropertyBuilder.property("testProp"));
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to create a property  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
 
     @Test
     public void removeProperty() throws IOException, OlogException {
         long startTime = System.nanoTime();
         client.deleteProperty("testProp");
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to remove a property  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
     @Test
     public void findAllLogbooks() throws IOException, OlogException {
         long startTime = System.nanoTime();
         Collection<Logbook> logbooks = client.listLogbooks();
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find all logbooks  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
     @Test
     public void createLogbook() throws IOException, OlogException {
         long startTime = System.nanoTime();
         Logbook logbook = client.set(LogbookBuilder.logbook("testLog").owner("testOwner"));
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to create a logbook  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
     @Test
     public void removeLogbook() throws IOException, OlogException {
         long startTime = System.nanoTime();
         client.deleteLogbook("testLog");
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to remove a logbook  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
     @Test
     public void findAllTags() throws IOException, OlogException {
         long startTime = System.nanoTime();
         Collection<Tag> tags = client.listTags();
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to find all tags  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
 
     @Test
     public void createTag() throws IOException, OlogException {
         long startTime = System.nanoTime();
         Tag Tag = client.set(TagBuilder.tag("testTag2"));
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to create a tag  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 
 
     @Test
     public void removeTag() throws IOException, OlogException {
         long startTime = System.nanoTime();
         client.deleteTag("testTag2");
         long endTime = System.nanoTime();
         double totalTime =(endTime - startTime) / 1000000000.0;
         out.write(" Time consume to remove a tag  is: " + totalTime + "(s)");
         out.newLine();
 
     }
 }
