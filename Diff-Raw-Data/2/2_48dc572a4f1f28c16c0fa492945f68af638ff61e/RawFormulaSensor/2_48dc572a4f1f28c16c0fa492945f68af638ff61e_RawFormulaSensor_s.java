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
 
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptEngine;
 import javax.script.ScriptException;
 
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 
 @PluginImplementation
 public class RawFormulaSensor implements BNSensorPlugin {
 
     private final String THRESHOLD = "threshold";
     private final String FORMULA = "formula";
     ScriptEngineManager mgr = new ScriptEngineManager();
     ScriptEngine engine = mgr.getEngineByName("JavaScript");
 
     Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
 
     private static final String NAME = "RawFormulaSensor";
 
     @Override
     public String[] getRequiredProperties() {
         return new String [] {THRESHOLD, FORMULA} ;
     }
 
     public void setProperty(String string, Object obj) {
         if(Arrays.asList(getRequiredProperties()).contains(string)) {
             propertiesMap.put(string, obj);
         } else {
             throw new RuntimeException("Property "+ string + " not in the required settings");
         }
     }
 
     public Object getProperty(String string) {
         return propertiesMap.get(string);
     }
 
     private Double executeFormula(String formula) throws ScriptException {
         return (Double) engine.eval(formula);
     }
 
     //formula in format node1->param1 OPER node=>param3 OPER node=>param3 ...
     private String parse(Map<String, Object>  attribute) throws ParseException {
         String returnString = ((String) getProperty(FORMULA)).replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ");
         String [] split = returnString.split("\\s+");
         Map<String, String> nodeMaps = new ConcurrentHashMap();
         for(String s1 : split)   {
             String [] s2 = s1.split("->");
             if(s2.length == 2)  {
                 nodeMaps.put(s2[0], s2[1]);
             }
         }
         Map <String, Double> values = new ConcurrentHashMap<String, Double>();
         for(Map.Entry<String, String> entry: nodeMaps.entrySet()){
             JSONObject jsonObject = (JSONObject) (attribute.get(entry.getKey()));
             if(jsonObject == null)
                 return null;
             Object value = ((JSONObject) new JSONParser().parse((String) jsonObject.get("rawData"))).get(entry.getValue());
             values.put(entry.getKey(), Utils.getDouble(value));
         }
         if(values.size() != nodeMaps.size())
             throw new RuntimeException("Error in parsing the formula "+ getProperty(FORMULA));
 
         for(Map.Entry<String, String> entry: nodeMaps.entrySet()){
             returnString = returnString.replaceAll(entry.getKey() + "->" + entry.getValue(), values.get(entry.getKey()).toString());
         }
         return returnString;
     };
 
 
     @Override
     public String getDescription() {
         return "Parse raw data from the scenario context";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         final double res;
         try {
             String parseFormula = parse((Map<String, Object>) testSessionContext.getAttribute(NodeSessionParams.RAW_DATA)) ;
             res = executeFormula(parseFormula);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return true;
             }
 
             @Override
             public String getName() {
                 return "Raw Data Sensor Result";
             }
 
             @Override
             public String getObserverState() {
                 if(res == Utils.getDouble(getProperty(THRESHOLD)))
                     return "EQUAL";
                 if(res > Utils.getDouble(getProperty(THRESHOLD)))
                     return "ABOVE";
                 else
                     return "BELOW";
             }
 
             @Override
             public List<Map<String, Number>> getObserverStates() {
                 return null;
             }
 
             @Override
             public String getRawData() {
                 JSONObject jsonObject = new JSONObject();
                 jsonObject.put("value", res);
                 return  jsonObject.toJSONString();
             }
 
         };
     }
 
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public String[] getSupportedStates() {
        return new String[] {"BELOW", "ABOVE"};
     }
 
 
     public static void main(String []args){
         RawFormulaSensor rawFormulaSensor = new RawFormulaSensor();
         rawFormulaSensor.setProperty("formula", "node1->value1 + node2->value2");
 
         rawFormulaSensor.setProperty("threshold", "4");
         TestSessionContext testSessionContext = new TestSessionContext(1);
         Map<String, Object> mapTestResult = new HashMap<String, Object>();
         JSONObject jsonObject = new JSONObject();
         JSONObject jsonRaw = new JSONObject();
         jsonRaw.put("value1", 1);
         jsonRaw.put("value2", 3);
         jsonObject.put("rawData", jsonRaw.toJSONString());
         mapTestResult.put("node1", jsonObject);
         mapTestResult.put("node2", jsonObject);
         testSessionContext.setAttribute(NodeSessionParams.RAW_DATA, mapTestResult);
         TestResult testResult = rawFormulaSensor.execute(testSessionContext);
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
         public List<Map<String, Number>> getObserverStates() {
             return null;
         }
 
         @Override
         public String getRawData() {
             return null;
         }
     }
 }
