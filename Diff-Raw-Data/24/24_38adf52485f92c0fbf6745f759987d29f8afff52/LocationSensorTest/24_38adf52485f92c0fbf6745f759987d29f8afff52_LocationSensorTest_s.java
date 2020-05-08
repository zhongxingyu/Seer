 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.scenario.TestResult;
 import com.ai.util.resource.TestSessionContext;
 import junit.framework.TestCase;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /**
  * Created by User: veselin
  * On Date: 26/12/13
  */
 public class LocationSensorTest extends TestCase{
     public void testSensorExecute() throws Exception {
         LocationSensor locationSensor = new LocationSensor();
         locationSensor.setProperty(LocationSensor.LONGITUDE, 19.851858);
         locationSensor.setProperty(LocationSensor.LATITUDE, 45.262231);
         locationSensor.setProperty(LocationSensor.DISTANCE, 100);
         TestSessionContext testSessionContext = new TestSessionContext(1);
         testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, 19.851858);
         testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, 45.262231);
         TestResult testResult = locationSensor.execute(testSessionContext);
         JSONObject res = (JSONObject) new JSONParser().parse(testResult.getRawData());
         assertEquals("Within", testResult.getObserverState());
         assertEquals(0.0, (Double) res.get("distance"), 0.1);
     }
 
     public void testDistanceCalculation() throws ParseException {
         LocationRawSensor locationRawSensor1 = new LocationRawSensor();
         locationRawSensor1.setProperty("location", "Krekelstraat 60, 9052 Gent, Belgium");
         TestResult testResult1 = locationRawSensor1.execute(null);
         LocationRawSensor locationRawSensor2 = new LocationRawSensor();
         locationRawSensor2.setProperty("location", "Novi Sad");
         TestResult testResult2 = locationRawSensor2.execute(null);
 
         JSONObject res1 = (JSONObject) new JSONParser().parse(testResult1.getRawData());
         System.out.println(res1.toString());
         JSONObject res2 = (JSONObject) new JSONParser().parse(testResult2.getRawData());
 
 
         LocationSensor locationSensor = new LocationSensor();
         locationSensor.setProperty(LocationSensor.LONGITUDE, res1.get("longitude"));
         locationSensor.setProperty(LocationSensor.LATITUDE, res1.get("latitude"));
         TestSessionContext testSessionContext = new TestSessionContext(1);
         testSessionContext.setAttribute(LocationSensor.RUNTIME_LONGITUDE, res2.get("longitude"));
         testSessionContext.setAttribute(LocationSensor.RUNTIME_LATITUDE, res2.get("latitude"));
 
 
         locationSensor.setProperty(LocationSensor.DISTANCE, 1000);
         TestResult testResult = locationSensor.execute(testSessionContext);
         JSONObject res = (JSONObject) new JSONParser().parse(testResult.getRawData());
         assertEquals("Out", testResult.getObserverState());
         assertEquals(1355, (Double) res.get("distance"), 200);
 
         locationSensor.setProperty(LocationSensor.DISTANCE, 1400);
         testResult = locationSensor.execute(testSessionContext);
         assertEquals("Within", testResult.getObserverState());
 
         locationSensor.setProperty(LocationSensor.LOCATION, "Krekelstraat 60, 9052 Gent, Belgium");
        locationSensor.setProperty(LocationSensor.LONGITUDE, 1);
 
         testResult = locationSensor.execute(testSessionContext);
         assertEquals("Within", testResult.getObserverState());
 
     }
 }
