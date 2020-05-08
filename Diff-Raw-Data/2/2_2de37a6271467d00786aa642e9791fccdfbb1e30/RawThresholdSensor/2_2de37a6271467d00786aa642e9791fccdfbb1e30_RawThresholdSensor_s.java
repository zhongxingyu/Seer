 /**
  * Created with IntelliJ IDEA.
  * User: pizuricv
  */
 
 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.Utils;
 import com.ai.util.resource.NodeSessionParams;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 @PluginImplementation
 public class RawThresholdSensor implements BNSensorPlugin {
     private ArrayList<Long> threshold = new ArrayList<Long>();
     private ArrayList<String> states = new ArrayList<String>();
     //in case that states are defined via property
     private ArrayList<String> definedStates  = new ArrayList<String>();
     private static final String NAME = "RawThresholdSensor";
     private String rawData;
     private String node;
 
     @Override
     public String[] getRequiredProperties() {
        return new String [] {"threshold", "rawData", "node"} ;
     }
 
 
     //comma separated list of thresholds
     @Override
     public void setProperty(String s, Object o) {
         if("threshold".endsWith(s)){
             if(o instanceof String)  {
                 String input = (String) o;
                 StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                 int i = 0;
                 states.add("level_"+ i++);
                 while(stringTokenizer.hasMoreElements()){
                     threshold.add(Long.parseLong(stringTokenizer.nextToken().trim()));
                     states.add("level_"+ i++);
                 }
             } else {
                 threshold.add((Long) o);
                 states.add("level_0");
                 states.add("level_1");
             }
             Collections.reverse(threshold);
         } else if ("rawData".equals(s)){
             rawData = o.toString();
         } else if ("node".equals(s)){
             node = o.toString();
         } else if("states".endsWith(s)){
             if(o instanceof String)  {
                 String input = (String) o;
                 StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                 while(stringTokenizer.hasMoreElements())
                     definedStates.add(stringTokenizer.nextToken().trim());
             }
         }
     }
 
     @Override
     public Object getProperty(String s) {
         if("threshold".endsWith(s)){
             return threshold;
         } else if("rawData".endsWith(s)){
             return rawData;
         } else if("node".endsWith(s)){
             return node;
         }
         else{
             throw new RuntimeException("Property " + s + " not recognised by " + getName());
         }
     }
 
     @Override
     public String getDescription() {
         return "Parse raw data from the scenario context";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         Map<String, Object> mapTestResult = (Map<String, Object>) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA);
         if(mapTestResult == null)
             return new EmptyResult();
         JSONObject jsonObject;
         try {
             jsonObject = (JSONObject) new JSONParser().parse(mapTestResult.get(node).toString());
         } catch (ParseException e) {
             e.printStackTrace();
             return new EmptyResult();
         }
 
         final Object value = jsonObject.get(rawData);
         Double dataD = Utils.getDouble(value);
 
         final String level = mapResult(dataD);
         return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return true;
             }
 
             @Override
             public String getName() {
                 return "Raw Test Result";
             }
 
             @Override
             public String getObserverState() {
                 return level;
             }
 
             @Override
             public String getRawData() {
                 return "{" +
                         "\"rawData\" : " + rawData + " ,"+
                         "\"value\" : " + value +
                         "}";
             }
 
         };
     }
 
     @Override
     public String getName() {
         return "Raw Test Result";
     }
 
     @Override
     public String[] getSupportedStates() {
         return states.toArray(new String[states.size()]);
     }
 
     private String mapResult(Double result) {
         if(definedStates.size() == 0){
             int i = states.size() - 1;
             for(Long l : threshold){
                 if(result  > l){
                     return "level_" + i;
                 }
                 i --;
             }
             return "level_0";
         } else {
             int i = definedStates.size() - 1;
             for(Long l : threshold){
                 if(result  > l){
                     return definedStates.get(i);
                 }
                 i --;
             }
             return definedStates.get(0);
         }
     }
 
     public static void main(String []args){
         WeatherSensor weatherSensor = new WeatherSensor();
         weatherSensor.setProperty("city", "London");
         TestResult testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
         //this is injected by scenario
         RawThresholdSensor rawThresholdSensor = new RawThresholdSensor();
         rawThresholdSensor.setProperty("rawData", "temperature");
         rawThresholdSensor.setProperty("threshold", "5,10,15,25,35");
         rawThresholdSensor.setProperty("node", "node1");
         TestSessionContext testSessionContext = new TestSessionContext(1);
         Map<String, Object> mapTestResult = new HashMap<String, Object>();
         mapTestResult.put("node1", testResult.getRawData());
         testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
         testResult = rawThresholdSensor.execute(testSessionContext);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
 
 
         rawThresholdSensor = new RawThresholdSensor();
         rawThresholdSensor.setProperty("rawData", "temperature");
         rawThresholdSensor.setProperty("threshold", "5,15,25");
         rawThresholdSensor.setProperty("states", "low,medium,high, heat");
         rawThresholdSensor.setProperty("node", "node1");
         testResult = rawThresholdSensor.execute(testSessionContext);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
     }
 
     private class EmptyResult implements TestResult {
         @Override
         public boolean isSuccess() {
             return false;
         }
 
         @Override
         public String getName() {
             return "";
         }
 
         @Override
         public String getObserverState() {
             return null;
         }
 
         @Override
         public String getRawData() {
             return null;
         }
     }
 }
