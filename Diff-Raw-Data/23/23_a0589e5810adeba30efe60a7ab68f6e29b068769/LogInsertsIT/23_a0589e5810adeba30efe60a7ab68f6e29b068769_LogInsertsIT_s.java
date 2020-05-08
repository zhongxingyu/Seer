 package edu.msu.nscl.olog.api;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Created by eschuhmacher on 3/3/14.
  */
 public class LogInsertsIT {
 
     private static OlogClient client;
     private static BufferedWriter out;
 
     @BeforeClass
     public static void prepareTest() throws Exception {
         client = OlogClientImpl.OlogClientBuilder.serviceURL().withHTTPAuthentication(true).username("olog").password("olog")
                 .create();
        String filePath = LogInsertsIT.class.getResource("LogInsertsTest.class").getPath();
         File outputFile = new File(filePath.substring(0,filePath.indexOf("target")) + "src/test/java/edu/msu/nscl/olog/api/LogInsertTestResult.txt");
         out = new BufferedWriter(new FileWriter(outputFile));
     }
 
     @AfterClass
     public static void close() throws IOException {
         out.close();
     }
 
     @Test
     public void InsertLogs() throws IOException {
         Log log = client.getLog(2000147l);
         LogBuilder newLog = LogBuilder.log(log);
         newLog = newLog.id(null);
         long startTimeTotal = System.nanoTime();
         for (int i=0 ; i<1000; i++) {
             newLog = newLog.description(log.getDescription() + i);
             long startTime = System.nanoTime();
             client.set(newLog);
             long endTime = System.nanoTime();
             double totalTime =(endTime - startTime) / 1000000000.0;
             out.write(" Time consume to insert a log  is: " + totalTime + "(s)");
             out.newLine();
         }
         long endTimeTotal = System.nanoTime();
         double totalTimeTotal =(endTimeTotal - startTimeTotal) / 1000000000.0;
         out.write(" Time consume to insert 1000 log  is: " + totalTimeTotal + "(s)");
         out.newLine();
     }
 }
