 package org.hackystat.dailyprojectdata.resource.issue;
 
 import static org.junit.Assert.assertEquals;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.hackystat.dailyprojectdata.client.DailyProjectDataClient;
 import org.hackystat.dailyprojectdata.resource.issue.jaxb.IssueDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.issue.jaxb.IssueData;
 import org.hackystat.dailyprojectdata.test.DailyProjectDataTestHelper;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.junit.Test;
 
 /**
  * Test Issue REST API.
  * @author Shaoxuan
  *
  */
 public class TestIssueRestApi extends DailyProjectDataTestHelper {
 
   private static final String NEW = "New";
   private static final String STARTED = "Started";
   private static final String ACCEPTED = "Accepted";
   private static final String FIXED = "Fixed";
   private static final String DEFECT = "Defect";
   private static final String TASK = "Task";
   private static final String ENHANCEMENT = "Enhancement";
   private static final String MEDIUM = "Medium";
   private static final String HIGH = "High";
   private static final String CRITICAL = "Critical";
 
   private static final String dataOwner = "testDataOwner@hackystat.org";
   private static final String testUser1 = "tester1@hackystat.org";
   private static final String testUser2 = "tester2@hackystat.org";
   
   private static final String[] testData1T1 = {"21", DEFECT, ACCEPTED, MEDIUM, "", testUser1, 
       "2008-09-07T11:00:00"};
   private static final String[] testData1T2 = {"21", ENHANCEMENT, FIXED, MEDIUM, "8.4", testUser1, 
       "2008-09-07T11:00:00"};
   
   private static final String[] testData2T1 = {"23", ENHANCEMENT, ACCEPTED, MEDIUM, "", testUser1, 
       "2009-07-20T00:24:06"};
   private static final String[] testData2T2 = {"23", ENHANCEMENT, ACCEPTED, HIGH, "", testUser2, 
       "2009-07-20T00:24:06"};
   
   private static final String[] testData3T1 = {"14", ENHANCEMENT, NEW, HIGH, "", testUser2, 
       "2009-06-20T14:35:32"};
   
   private static final String[] testData4T1 = {"25", DEFECT, STARTED, CRITICAL, "8.4", testUser1, 
       "2009-07-21T20:00:00"};
   
   private static final String[] testData5T1 = {"18", TASK, FIXED, HIGH, "", testUser2, 
       "2009-07-21T18:00:00"};
   
   /**
    * Constructor, prepare the test data.
    * @throws Exception if error when making XMLGregorianCalendar timestamps.
    */
   @Test public void testIssueRestApi() throws Exception {
     XMLGregorianCalendar testTime1 = Tstamp.makeTimestamp("2009-07-20T00:00:00");
     XMLGregorianCalendar testTime2 = Tstamp.makeTimestamp("2009-07-22T00:00:00");
     // First, create a batch of Issue sensor data.
     final SensorDatas batchData = new SensorDatas();
     batchData.getSensorData().add(TestIssueDataParser.makeIssueSensorData(dataOwner,
         new String[][]{testData1T1, testData1T2}, 
         new XMLGregorianCalendar[]{testTime1, testTime2}));
     batchData.getSensorData().add(TestIssueDataParser.makeIssueSensorData(dataOwner,
         new String[][]{testData2T1, testData2T2}, 
         new XMLGregorianCalendar[]{testTime1, testTime2}));
     batchData.getSensorData().add(TestIssueDataParser.makeIssueSensorData(dataOwner,
         new String[][]{testData3T1}, new XMLGregorianCalendar[]{testTime1}));
     batchData.getSensorData().add(TestIssueDataParser.makeIssueSensorData(dataOwner,
         new String[][]{testData4T1}, new XMLGregorianCalendar[]{testTime2}));
     batchData.getSensorData().add(TestIssueDataParser.makeIssueSensorData(dataOwner,
         new String[][]{testData5T1}, new XMLGregorianCalendar[]{testTime2}));
 
     // Connect to the sensorbase and register the DailyProjectDataDevEvent user. 
     SensorBaseClient.registerUser(getSensorBaseHostName(), dataOwner);
     final SensorBaseClient client = 
       new SensorBaseClient(getSensorBaseHostName(), dataOwner, dataOwner);
     client.authenticate();
     client.deleteSensorData(dataOwner);
     // Send the sensor data to the SensorBase. 
     client.putSensorDataBatch(batchData);
     
     // Now connect to the DPD server. 
     final DailyProjectDataClient dpdClient = 
       new DailyProjectDataClient(getDailyProjectDataHostName(), dataOwner, dataOwner);
     dpdClient.authenticate(); 
    final IssueDailyProjectData issueT1 = dpdClient.getIssues(dataOwner, "Default", testTime1);
     
     System.out.println("issue dpd for time 1");
     for (IssueData data : issueT1.getIssueData()) {
       System.out.println("#" + data.getId() + "," + data.getType() + "," + data.getStatus());
     }
       
     assertEquals("Checking total issues for time 1.", 3, issueT1.getIssueData().size());
     assertEquals("Checking open issues for time 1.", 3, issueT1.getOpenIssues());
     
    final IssueDailyProjectData issueT2 = dpdClient.getIssues(dataOwner, "Default", testTime2);
     assertEquals("Checking total issues for time 2.", 5, issueT2.getIssueData().size());
     assertEquals("Checking open issues for time 2.", 3, issueT2.getOpenIssues());
     //assertEquals("Checking MemberData size", 1, devTime.getMemberData().size());
     
     final IssueDailyProjectData issueT3 = 
      dpdClient.getIssues(dataOwner, "Default", testTime2, "open");
     assertEquals("Checking open issues for time 2 with open status query.", 
         3, issueT3.getIssueData().size());
     assertEquals("Checking open issues count for time 2 with open status query.", 
         3, issueT3.getOpenIssues());
     
     final IssueDailyProjectData issueT4 = 
      dpdClient.getIssues(dataOwner, "Default", testTime2, "closed");
     assertEquals("Checking closed issues for time 2 with closed status query.", 
         2, issueT4.getIssueData().size());
     assertEquals("Checking open issues for time 2 with closed status query.", 
         0, issueT4.getOpenIssues());
   }
   
 }
