 
 /**
  * User: pizuricv
  */
 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.Rest;
 import com.ai.myplugin.util.Utils;
 import com.ai.util.resource.TestSessionContext;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import java.net.URLEncoder;
 
 public abstract class WeatherAbstractSensor implements BNSensorPlugin {
     String city;
     static final String TEMP = "temperature";
     static final String HUMIDITY = "humidity";
     static final String WEATHER = "weather";
     static final String CITY = "city";
     static final String server = "http://api.openweathermap.org/";
 
     protected abstract String getTag();
     protected abstract String getSensorName();
 
     protected static String [] weatherStates = {"Clouds", "Clear", "Rain",
             "Storm", "Snow", "Fog", "Mist" , "Drizzle",
             "Smoke", "Dust", "Tropical Storm", "Hot", "Cold" ,
             "Windy", "Hail"};
     String [] humidityStates = {"Low", "Normal", "High"};
     String [] tempStates = {"Freezing", "Cold", "Mild", "Warm", "Heat"};
     private static final String NAME = "Weather";
 
     @Override
     public String[] getRequiredProperties() {
        return new String[] {"City", "Option"};
     }
 
     @Override
     public void setProperty(String string, Object obj) {
         if(string.equalsIgnoreCase(CITY)) {
             city = URLEncoder.encode((String) obj);
         } else {
             throw new RuntimeException("Property "+ string + " not in the required settings");
         }
     }
 
     @Override
     public Object getProperty(String string) {
         return city;
     }
 
     @Override
     public String getDescription() {
         return "Weather information";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         if(city == null){
             throw new RuntimeException("City not defined");
         }
 
         boolean testSuccess = true;
         String stringToParse = "";
 
         String pathURL = server+ "data/2.5/find?q="+ city+ "&mode=json&units=metric&cnt=0";
         try{
             stringToParse = Rest.httpGet(pathURL);
             System.out.println(stringToParse);
         } catch (Exception e) {
             testSuccess = false;
         }
 
         JSONParser parser=new JSONParser();
         JSONObject obj  = null;
         try {
             obj = (JSONObject) parser.parse(stringToParse);
         } catch (ParseException e) {
             testSuccess = false;
         }
 
         int temp = -1;
         int humidity = -1;
         int weatherID = -1;
         int pressure = -1;
         double windSpeed = -1;
         int cloudCoverage = -1;
 
         if(testSuccess ){
             JSONArray jsonArray = (JSONArray)obj.get("list");
             for (Object o : jsonArray.toArray()) {
                 JSONObject jo = (JSONObject) o;
                 if(jo.get("main") != null){
                     temp = Utils.getDouble( ((JSONObject)jo.get("main")).get("temp")).intValue();
                     humidity = Utils.getDouble(((JSONObject)jo.get("main")).get("humidity")).intValue();
                     pressure = Utils.getDouble(((JSONObject)jo.get("main")).get("pressure")).intValue();
                 }
                 if(jo.get("weather") != null){
                     JSONArray jsonArray1 = (JSONArray)jo.get("weather");
                     for (Object o2 : jsonArray1.toArray()) {
                         JSONObject jo2 = (JSONObject) o2;
                         if(jo2.get("id") != null){
                             weatherID =  Utils.getDouble(jo2.get("id")).intValue();
                         }
                     }
                 }
                 if(jo.get("wind") != null){
                      windSpeed =  Utils.getDouble(((JSONObject) jo.get("wind")).get("speed"));
                 }
                 if(jo.get("clouds") != null){
                     cloudCoverage =  Utils.getDouble(((JSONObject)jo.get("clouds")).get("all")).intValue();
                 }
             }
         } else{
             temp = -1;
             humidity = -1;
             weatherID = -1;
             pressure = -1;
             windSpeed = -1;
             cloudCoverage = -1;
         }
 
         final boolean finalTestSuccess = testSuccess;
         final int finalHumidity = humidity;
         final int finalTemp = temp;
         final int finalWeatherID = weatherID;
         final int finalPressure = pressure;
         final double finalWindSpeed = windSpeed;
         final int finalCloudCoverage = cloudCoverage;
         return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return finalTestSuccess;
             }
 
             @Override
             public String getName() {
                 return "Weather result";
             }
 
             @Override
             public String getObserverState() {
                 if(getTag().equals(TEMP)){
                     return mapTemperature();
                 } else if(getTag().equals(WEATHER)){
                     return mapWeather();
                 } else {
                     return mapHumidity();
                 }
             }
 
             @Override
             public String getRawData(){
                 return "{" +
                         "\"temperature\" : " + finalTemp + "," +
                         "\"weather\" : " + "\""+mapWeather() + "\""+ "," +
                         "\"humidity\" : " + finalHumidity + "," +
                         "\"pressure\" : " + finalPressure + "," +
                         "\"cloudCoverage\" : " + finalCloudCoverage + "," +
                         "\"windSpeed\" : " + finalWindSpeed +
                         "}";
             }
 
             private String mapWeather() {
                 //String [] weatherStates = {"Clouds", "Clear", "Rain", "Storm", "Snow", "Fog"};
                 //http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
                if(finalWeatherID < 300){
                     return "Storm";
                 } else if(finalWeatherID < 400){
                     return "Drizzle";
                 } else if(finalWeatherID < 600){
                     return "Rain";
                 } else if(finalWeatherID < 700){
                     return "Snow";
                 } else if(finalWeatherID == 701){
                     return "Mist";
                 } else if(finalWeatherID == 711){
                     return "Smoke";
                 } else if(finalWeatherID == 721){
                     return "Haze";
                 } else if(finalWeatherID == 731){
                     return "Dust";
                 } else if(finalWeatherID == 741){
                     return "Fog";
                 } else if(finalWeatherID == 800){
                     return "Clear";
                 } else if(finalWeatherID < 900){
                     return "Clouds";
                 } else if(finalWeatherID == 900){
                     return "Tornado";
                 } else if(finalWeatherID == 901){
                     return "Tropical Storm";
                 } else if(finalWeatherID == 902){
                     return "Cold";
                 } else if(finalWeatherID == 903){
                     return "Hot";
                 } else if(finalWeatherID == 904){
                     return "Windy";
                 }  else if(finalWeatherID == 9035){
                     return "Hail";
                 }
                 return "Extreme";
 
             }
 
             private String mapHumidity() {
                 //    String [] humidityStates = {"Low", "Normal", "High"};
                 System.out.println("Map humidity "+ finalHumidity);
                 if(finalHumidity < 70) {
                     return "Low";
                 } else if(finalHumidity < 90) {
                     return "Normal";
                 }
                 return "High";
             }
 
             private String mapTemperature() {
                 System.out.println("Map temperature "+ finalTemp);
                 if(finalTemp < 0) {
                     return "Freezing";
                 }  else if(finalTemp < 8) {
                     return "Cold";
                 } else if(finalTemp < 15) {
                     return "Mild";
                 }  else if(finalTemp < 25) {
                     return "Warm";
                 }
                 return "Heat";
             };
         };
     }
 
     @Override
     public String getName() {
         return getSensorName();
     }
 
     @Override
     public String[] getSupportedStates() {
         if(TEMP.equals(getTag())){
             return tempStates;
         } else if(WEATHER.equals(getTag())){
             return weatherStates;
         } else if(HUMIDITY.equals(getTag())){
             return humidityStates;
         } else {
             return new String[]{};
         }
     }
 
     public static void main(String[] args){
         WeatherAbstractSensor weatherSensor = new WeatherAbstractSensor() {
             @Override
             protected String getTag() {
                 return WEATHER;
             }
 
             @Override
             protected String getSensorName() {
                 return "";
             }
         };
         weatherSensor.setProperty("city", "Gent");
         TestResult testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
 
         weatherSensor.setProperty("city", "London");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Sidney");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Bangalore");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Chennai");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Moscow");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Belgrade");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Split");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
         System.out.println(testResult.getRawData());
     }
 }
