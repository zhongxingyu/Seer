 package org.hackystat.dailyprojectdata.resource.filemetric.jaxb;
 
 import static org.junit.Assert.assertEquals;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.hackystat.dailyprojectdata.client.DailyProjectDataClient;
 import org.hackystat.dailyprojectdata.test.DailyProjectDataTestHelper;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Properties;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.junit.Test;
 
 public class TestFileMetricRestApi extends DailyProjectDataTestHelper {
 
   /** The user for this test case. */
   private String user = "TestDevTime@hackystat.org";
 
   /**
    * Test that GET {host}/filemetric/{user}/default/{starttime} works properly.
    * First, it creates a test user and sends some sample FileMetric data to the
    * SensorBase. Then, it invokes the GET request and checks to see that it
    * obtains the right answer. Finally, it deletes the data and the user.
    *
    * @throws Exception If problems occur.
    */
   @Test
   public void getDefaultFileMetric() throws Exception {
     // First, create a batch of DevEvent sensor data.
     SensorDatas batchData = new SensorDatas();
     batchData.getSensorData().add(
         makeFileMetric("2007-04-30T02:00:00", "2007-04-30T02:00:00.001", user,
             "/home/hackystat-sensorbase-uh/src/org/hackystat/projects/Property.java", "111"));
     batchData.getSensorData().add(
         makeFileMetric("2007-04-30T02:00:00", "2007-04-30T02:00:00.002", user,
             "/home/hackystat-sensorbase-uh/src/org/hackystat/projects/Foo.java", "123"));
     batchData.getSensorData().add(
         makeFileMetric("2007-04-30T02:00:00", "2007-04-30T02:00:00.003", user,
             "/home/hackystat-sensorbase-uh/src/org/hackystat/projects/Bar.java", "456"));
     batchData.getSensorData().add(
         makeFileMetric("2007-04-30T02:12:00", "2007-04-30T02:12:00.001", user,
             "/home/hackystat-sensorbase-uh/src/org/hackystat/projects/Foo.java", "120"));
     batchData.getSensorData().add(
         makeFileMetric("2007-04-30T02:12:00", "2007-04-30T02:12:00.002", user,
             "/home/hackystat-sensorbase-uh/src/org/hackystat/projects/Bar.java", "450"));
 
     // Connect to the sensorbase and register the DailyProjectDataDevEvent user.
     SensorBaseClient.registerUser(getSensorBaseHostName(), user);
     SensorBaseClient client = new SensorBaseClient(getSensorBaseHostName(), user, user);
     client.authenticate();
     // Send the sensor data to the SensorBase.
     client.putSensorDataBatch(batchData);
 
     // Now connect to the DPD server.
     DailyProjectDataClient dpdClient = new DailyProjectDataClient(getDailyProjectDataHostName(),
         user, user);
     dpdClient.authenticate();
     FileMetricDailyProjectData fileMetric = dpdClient.getFileMetric(user, "Default",
         Tstamp.makeTimestamp("2007-04-30"));
     assertEquals("Checking default fileMetric", 570, fileMetric.getTotalSizeMetricValue().intValue());
     assertEquals("Checking MemberData size", 2, fileMetric.getFileData().size());
     fileMetric = dpdClient.getFileMetric(user, "Default", Tstamp.makeTimestamp("2007-05-01"));
     // the value should be 0
     assertEquals("Checking fileMetric day after data", 0, fileMetric.getTotalSizeMetricValue()
         .intValue());
     fileMetric = dpdClient.getFileMetric(user, "Default", Tstamp.makeTimestamp("2005-04-12"));
    assertEquals("Checking fileMetric before any data.", 0 , fileMetric.getTotalSizeMetricValue()
        .intValue());
   }
 
   /**
    * Creates a sample SensorData DevEvent instance given a timestamp and a user.
    *
    * @param tstampString The timestamp as a string
    * @param user The user.
    * @return The new SensorData DevEvent instance.
    * @throws Exception If problems occur.
    */
   private SensorData makeFileMetric(String runTstampString, String tstampString, String user,
       String file, String size) throws Exception {
     XMLGregorianCalendar tstamp = Tstamp.makeTimestamp(tstampString);
     XMLGregorianCalendar runStamp = Tstamp.makeTimestamp(runTstampString);
     String sdt = "FileMetric";
     SensorData data = new SensorData();
     String tool = "SCLC";
     data.setTool(tool);
     data.setOwner(user);
     data.setSensorDataType(sdt);
     data.setTimestamp(tstamp);
     data.setResource(file);
     data.setRuntime(runStamp);
     Property property = new Property();
     property.setKey("TotalLines");
     property.setValue(size);
     Properties properties = new Properties();
     properties.getProperty().add(property);
     data.setProperties(properties);
     return data;
   }
 
 }
