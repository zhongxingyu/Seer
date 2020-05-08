 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.Rest;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.json.simple.JSONObject;
 
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * Created by User: veselin
  * On Date: 21/10/13
  */
 
 /*
 http://luchtkwaliteit.vmm.be/lijst.php
 </tr>
             <tr class="trEVEN">
     <td headers="Details">
         <a href="details.php?station=44R701" title="Gent-Baudelostraat">
             <img src="image/information.png" />
         </a>
     </td>
     <td headers="Gemeente">
         <a href="details.php?station=44R701" title="Gent-Baudelostraat">
             Gent
         </a>
     </td>
     <td headers="Locatie">
         <a href="details.php?station=44R701" title="Gent-Baudelostraat">
             Baudelostraat
         </a>
     </td>
     <td headers="Provincie">
         <a href="details.php?station=44R701" title="Gent-Baudelostraat">
             Oost-Vlaanderen
         </a>
     </td>
     <td headers="Index" style="text-align:left;">
         <a href="details.php?station=44R701" title="Gent-Baudelostraat">
             <span>&nbsp;<span class="index3">&nbsp;&nbsp;&nbsp;</span>
                 &nbsp;3</span>
                                 </a>
     </td>
 </tr>
  */
 
 @PluginImplementation
 public class AirQualitySensor implements BNSensorPlugin{
     public static final String LOCATION = "location";
     private String location = null;
 
     @Override
     public String[] getRequiredProperties() {
         return new String[]{LOCATION};
     }
 
     @Override
     public void setProperty(String s, Object o) {
         if(LOCATION.equals(s))
             location = o.toString();
         else
             throw new RuntimeException("Property "+ s + " not in the required settings");
     }
 
     @Override
     public Object getProperty(String s) {
         if(LOCATION.equals(s))
             return location;
         else
             throw new RuntimeException("Property "+ s + " not in the required settings");
     }
 
     @Override
     public String getDescription() {
         return "Air Quality";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         for(String property : getRequiredProperties()){
             if(getProperty(property) == null)
                 throw new RuntimeException("Required property "+property + " not defined");
         }
 
         boolean testSuccess = true;
         int value = -1;
         String stringToParse = "";
 
         String pathURL = "http://luchtkwaliteit.vmm.be/lijst.php";
         try{
             stringToParse = Rest.httpGet(pathURL);
             //System.out.println(stringToParse);
         } catch (Exception e) {
             testSuccess = false;
         }
         if(testSuccess){
             testSuccess = false;
             int len = stringToParse.indexOf(location);
             try{
                 if(len > 0){
                     stringToParse = stringToParse.substring(len);
                     len = stringToParse.indexOf("Index");
                     if(len > 0) {
                         stringToParse = stringToParse.substring(len);
                         len = stringToParse.indexOf("\t&nbsp;");
                         if(len > 0){
                             stringToParse = stringToParse.substring(len);
                             stringToParse = stringToParse.substring(stringToParse.indexOf("&nbsp;"),
                                     stringToParse.indexOf("</s")).replaceAll("&nbsp;","").trim();
                             value = Integer.parseInt(stringToParse);
                             testSuccess = true;
                         }
                     }
                 }
             }catch (Exception e){
                 System.err.println(e.getLocalizedMessage());
                 testSuccess = false;
             }
         }
         if(testSuccess)  {
             final int finalValue = value;
             return new TestResult() {
                 @Override
                 public boolean isSuccess() {
                     return true;
                 }
 
                 @Override
                 public String getName() {
                     return "Water level result";
                 }
 
                 @Override
                 public String getObserverState() {
                     return mapValue(finalValue);
                 }
 
                 @Override
                 public List<Map<String, Number>> getObserverStates() {
                     return null;
                 }
 
                 @Override
                 public String getRawData() {
                     JSONObject jsonObject = new JSONObject();
                     jsonObject.put("value", finalValue);
                     return jsonObject.toJSONString();
                 }
             };
 
 
         }
         else return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return false;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public String getName() {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public String getObserverState() {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public List<Map<String, Number>> getObserverStates() {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public String getRawData() {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         };
     }
 
     private String mapValue(int finalValue) {
         if(finalValue < 3)
             return "EXCELLENT";
         if(finalValue < 5)
             return "GOOD";
         if(finalValue < 7)
             return "NORMAL";
         if(finalValue < 9)
             return "POOR";
         return "BAD";
     }
 
     @Override
     public String getName() {
         return "AirQualitySensor";
     }
 
     @Override
     public String[] getSupportedStates() {
        return new String[] {"EXCELLENT","GOOD", "NORMAL", "POOR", "BAD"};
     }
 
     public static void main(String []args){
         AirQualitySensor airQualitySensor = new AirQualitySensor();
         airQualitySensor.setProperty(LOCATION, "Gent");
         TestResult testResult = airQualitySensor.execute(null);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
 
         airQualitySensor.setProperty(LOCATION, "Antwerp");
         testResult = airQualitySensor.execute(null);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
     }
 }
 
 
