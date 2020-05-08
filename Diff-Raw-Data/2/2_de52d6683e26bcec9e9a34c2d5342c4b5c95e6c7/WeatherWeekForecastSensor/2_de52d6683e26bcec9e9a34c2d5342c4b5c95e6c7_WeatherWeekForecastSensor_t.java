 package com.ai.myplugin.sensor;
 
 
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import com.ai.bayes.scenario.TestResult;
 
 @PluginImplementation
 
 public class WeatherWeekForecastSensor extends WeatherAbstractSensor{
     @Override
     protected String getTag() {
         return WEEK_FORECAST;
     }
 
     @Override
     protected String getSensorName() {
        return "WeatherWeekForecastSensor";
     }
 
     public static void main(String[] args){
         WeatherWeekForecastSensor weatherSensor = new WeatherWeekForecastSensor();
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
     }
 }
