 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.*;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Created by User: veselin
  * On Date: 26/12/13
  */
 @PluginImplementation
 public class GentParking implements BNSensorPlugin{
     protected static final Log log = LogFactory.getLog(GentParking.class);
     static final String DISTANCE = "distance";
     static final String RUNTIME_LATITUDE = "runtime_latitude";
     static final String RUNTIME_LONGITUDE = "runtime_longitude";
 
     Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
 
     String [] states = {"Found", "Not Found"};
    private static final String NAME = "GentParking";
 
     @Override
     public String[] getRequiredProperties() {
         return new String[]{DISTANCE};
     }
 
     @Override
     public String[] getRuntimeProperties() {
         return new String[]{RUNTIME_LATITUDE, RUNTIME_LONGITUDE};
     }
 
     @Override
     public void setProperty(String string, Object obj) {
         if(Arrays.asList(getRequiredProperties()).contains(string)) {
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
         return "Check for parking space is within a distance from a given location";
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
 
         JSONObject jsonObject = new JSONObject();
         jsonObject.put(RUNTIME_LATITUDE, runtime_latitude);
         jsonObject.put(RUNTIME_LONGITUDE, runtime_longitude);
 
         String pathURL = "http://datatank.gent.be/Mobiliteitsbedrijf/Parkings11.json";
         ArrayList<MyParkingData> parkingDatas = new ArrayList<MyParkingData>();
         try{
             String stringToParse = Rest.httpGet(pathURL);
             log.debug(stringToParse);
             JSONObject parkingObj = (JSONObject) new JSONParser().parse(stringToParse);
             JSONArray parkings = ((JSONArray)((JSONObject) (parkingObj.get("Parkings11"))).get("parkings"));
             for(Object parking : parkings){
                 parkingDatas.add(new MyParkingData(parking, runtime_latitude, runtime_longitude));
             }
             Collections.sort(parkingDatas);
         } catch (Exception e) {
             log.error(e.getLocalizedMessage());
             return new EmptyTestResult();
         }
         log.info("Best spot is "+parkingDatas.get(0));
         JSONArray jsonArray = new JSONArray();
         for(MyParkingData parkingData : parkingDatas){
             jsonArray.add(parkingData.getAsJSON());
 
         }
         jsonObject.put("parkings", jsonArray);
         jsonObject.put("best", jsonArray.get(0));
 
 
         //log.info("Computed parking: " + Arrays.asList(parkingDatas).toString());
         log.info("raw data is "+jsonObject.toJSONString());
 
 
         final String state;
         if(parkingDatas.get(0).distance  < Utils.getDouble(getProperty(DISTANCE)))
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
         GentParking locationSensor = new GentParking();
         locationSensor.setProperty(DISTANCE, 10);
         TestSessionContext testSessionContext = new TestSessionContext(1);
         testSessionContext.setAttribute(RUNTIME_LONGITUDE, 3.68);
         testSessionContext.setAttribute(RUNTIME_LATITUDE, 50.99);
         TestResult testResult = locationSensor.execute(testSessionContext);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
     }
 
     private class MyParkingData implements Comparable{
         String address;
         Double latitude;
         Double longitude;
         Double capacity;
         Double free;
         Integer distance;
         Double formulaCalc;
         String mapURL;
         public MyParkingData(Object parking, Double runtime_latitude, Double runtime_longitude) {
             JSONObject obj = (JSONObject) parking;
             address = (String) obj.get("address");
             latitude = Utils.getDouble(obj.get("latitude"));
             longitude = Utils.getDouble(obj.get("longitude"));
             capacity = Utils.getDouble(obj.get("totalCapacity"));
             free = Utils.getDouble(obj.get("availableCapacity"));
             mapURL = "https://maps.google.com/maps?q="  +latitude + "," + longitude ;
 
 
             distance = FormulaParser.calculateDistance(runtime_latitude, runtime_longitude,
                     latitude, longitude);
 
             if(free < 10)
                 formulaCalc = 1d/distance * free/capacity;
             else
                 formulaCalc = 1d/distance;
             GentParking.this.log.info(this.toString());
         }
 
         @Override
         public int compareTo(Object o) {
             MyParkingData other = (MyParkingData) o;
             return other.formulaCalc.compareTo(formulaCalc);
         }
 
         @Override
         public String toString() {
             return "MyParkingData{" +
                     "address='" + address + '\'' +
                     ", latitude=" + latitude +
                     ", longitude=" + longitude +
                     ", capacity=" + capacity +
                     ", free=" + free +
                     ", distance=" + distance +
                     ", formulaCalc=" + formulaCalc +
                     ", mapURL='" + mapURL + '\'' +
                     '}';
         }
 
         public JSONObject getAsJSON(){
             JSONObject jsonObject = new JSONObject();
             jsonObject.put("address", address.replace("<br>"," "));
             jsonObject.put("url", mapURL);
             jsonObject.put("latitude", latitude);
             jsonObject.put("longitude", latitude);
             jsonObject.put("capacity", capacity.intValue());
             jsonObject.put("free", free.intValue());
             jsonObject.put("distance", distance.intValue());
             return jsonObject;
         }
     }
 }
