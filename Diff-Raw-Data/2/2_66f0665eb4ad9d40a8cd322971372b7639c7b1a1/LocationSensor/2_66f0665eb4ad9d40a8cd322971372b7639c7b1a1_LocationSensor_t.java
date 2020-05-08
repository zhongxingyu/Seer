 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.*;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Created by User: veselin
  * On Date: 26/12/13
  */
 @PluginImplementation
 public class LocationSensor implements BNSensorPlugin{
     protected static final Log log = LogFactory.getLog(LocationSensor.class);
     static final String LOCATION = "location";
     static final String LATITUDE = "latitude";
     static final String LONGITUDE = "longitude";
     static final String DISTANCE = "distance";
     static final String RUNTIME_LATITUDE = "runtime_latitude";
     static final String RUNTIME_LONGITUDE = "runtime_longitude";
     Double configuredLatitude = Double.MAX_VALUE;
     Double configuredLongitude = Double.MAX_VALUE;
 
     Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
 
     String [] states = {"Within", "Out"};
     private static final String NAME = "LocationSensor";
 
     @Override
     public String[] getRequiredProperties() {
         return new String[]{LOCATION, LATITUDE, LONGITUDE, DISTANCE};
     }
 
     @Override
     public String[] getRuntimeProperties() {
         return new String[]{RUNTIME_LATITUDE, RUNTIME_LONGITUDE};
     }
 
     @Override
     public void setProperty(String string, Object obj) {
         if(Arrays.asList(getRequiredProperties()).contains(string)) {
            if(LOCATION.equals(string))
                string = URLEncoder.encode(string);
             propertiesMap.put(string, obj);
         } else {
             throw new RuntimeException("Property "+ string + " not in the required settings");
         }
     }
 
     @Override
     public Object getProperty(String string) {
         return propertiesMap.get(string);
     }
 
     @Override
     public String getDescription() {
         return "Checks whether a collected data is within a distance from a given location";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
         if(getProperty(DISTANCE) == null)
             throw new RuntimeException("distance not set");
 
         Object rt1 = testSessionContext.getAttribute(RUNTIME_LATITUDE);
         Object rt2 = testSessionContext.getAttribute(RUNTIME_LONGITUDE);
         if(rt1 == null || rt2 == null){
             log.warn("no runtime longitude or latitude given");
             return new EmptyTestResult();
         }
         Double runtime_latitude = Utils.getDouble(rt1);
         Double runtime_longitude = Utils.getDouble(rt2);
         log.info("Current location: "+ runtime_latitude + ","+runtime_longitude);
 
         Map<String, String> map = new ConcurrentHashMap<String, String>();
         map.put("X-Mashape-Authorization", Mashape.getKey());
         JSONObject jsonObject = null;
         if(configuredLatitude.equals(Double.MAX_VALUE) || configuredLongitude.equals(Double.MAX_VALUE)){
             String str;
             try {
                 if(getProperty(LOCATION) != null){
                     str = Rest.httpGet(LocationRawSensor.server + getProperty(LOCATION), map);
                 } else if (getProperty(LONGITUDE)!= null && getProperty(LATITUDE)!= null){
                     String configuredLatitudeStr = LATITUDE + "="+ URLEncoder.encode(getProperty(LATITUDE).toString());
                     String longitudeCoordinateStr = LONGITUDE + "="+ URLEncoder.encode(getProperty(LONGITUDE).toString());
                     str = Rest.httpGet(LatitudeLongitudeRawSensor.server + longitudeCoordinateStr + "&"+
                             configuredLatitudeStr, map);
                 } else
                     throw new RuntimeException("location not properly set");
                 jsonObject = (JSONObject) new JSONParser().parse(str);
                 jsonObject.put(RUNTIME_LATITUDE, runtime_latitude);
                 jsonObject.put(RUNTIME_LONGITUDE, runtime_longitude);
                 configuredLongitude = Utils.getDouble(jsonObject.get("longitude"));
                 configuredLatitude = Utils.getDouble(jsonObject.get("latitude"));
                 log.info("Configured location: "+ getProperty(LATITUDE) + ","+getProperty(LONGITUDE));
             }
             catch (Exception e) {
                 e.printStackTrace();
                 log.error(e.getLocalizedMessage());
                 return new EmptyTestResult();
             }
         }
         double distance = FormulaParser.calculateDistance(runtime_latitude, runtime_longitude,
                 configuredLatitude, configuredLongitude);
         log.info("Computed distance: "+ distance);
         if(jsonObject != null)
             jsonObject.put("distance", distance);
 
         final String state;
         if(distance  < Utils.getDouble(getProperty(DISTANCE)))
             state = states[0];
         else
             state = states[1];
 
         final JSONObject finalJsonObject = jsonObject;
         return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return true;
             }
 
             @Override
             public String getName() {
                 return "Location result";
             }
 
             @Override
             public String getObserverState() {
                 return state;
             }
 
             @Override
             public List<Map<String, Number>> getObserverStates() {
                 return null;
             }
 
             @Override
             public String getRawData() {
                 return finalJsonObject.toJSONString();
             }
         };
 
     }
 
     @Override
     public void shutdown(TestSessionContext testSessionContext) {
 
     }
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public String[] getSupportedStates() {
         return states;
     }
 
     public static void main(String []args) throws ParseException {
         LocationSensor locationSensor = new LocationSensor();
         locationSensor.setProperty(LONGITUDE, 19.851858);
         locationSensor.setProperty(LATITUDE, 45.262231);
         locationSensor.setProperty(DISTANCE, 100);
         TestSessionContext testSessionContext = new TestSessionContext(1);
         testSessionContext.setAttribute(RUNTIME_LONGITUDE, 19.851858);
         testSessionContext.setAttribute(RUNTIME_LATITUDE, 45.262231);
         TestResult testResult = locationSensor.execute(testSessionContext);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
     }
 }
