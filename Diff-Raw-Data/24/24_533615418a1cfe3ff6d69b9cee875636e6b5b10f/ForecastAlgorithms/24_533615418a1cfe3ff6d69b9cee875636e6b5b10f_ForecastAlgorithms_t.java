 package com.alexkorovyansky.carwashfor.services;
 
 import android.graphics.Color;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Alex Korovyansky (korovyansk@gmail.com)
  */
 public class ForecastAlgorithms {
 
     public static float makeForecast(float[] rainsArray){
         if ( rainsArray.length == 0 ) {
             throw new IllegalArgumentException("rainsArray length should be >= 0");
         }
         float dayP = 100.0f / rainsArray.length;
 
         float result = 0.0f;
         float k = 1.0f;
 
         for (int i = 0; i < rainsArray.length; ++i){
             final float ki = (float) (1 / (Math.pow(rainsArray[i], 3) + 1 ));
             k *= ki;
             result += dayP * k;
         }
 
         if ( result < 1 ){
             result = 1;
         }
 
         if ( result > 99 ){
             result = 99;
         }
         return result;
     }
 
     public static float[] calculateRainsArray(ForecastRestService.ForecastResponse response){
         long secondsInDay = 60 * 60 * 24;
 
         final Map<Long, Float> resultMap = new HashMap<Long, Float>();
 
        for (ForecastRestService.ForecastResponse.Item item: response.items) {
             final long date = (item.dt / secondsInDay) * secondsInDay;
             final float _3h = item.rainInfo == null ? 0.0f : item.rainInfo._3h;
 
             if ( resultMap.containsKey(date) ){
                 resultMap.put(date, resultMap.get(date) + _3h);
             } else{
                 resultMap.put(date, _3h);
             }
         }
 
         final List<Long> dates = new ArrayList<Long>(resultMap.keySet());
         Collections.sort(dates);
 
         final float[] result = new float[dates.size()];
 
         for ( int i = 0; i < result.length; ++i){
             result[i] = resultMap.get(dates.get(i));
         }
 
         return result;
     }
 
     /**
      * 0% - RED
      * 50% - YELLOW
      * 100% - GREEN
      * @param forecast
      * @return
      */
     public static int calculateForecastColor(int forecast) {
         if (forecast <= 50) {
             return calculateGradient(Color.RED, Color.YELLOW, 0, 50, forecast);
         } else {
             return calculateGradient(Color.YELLOW, Color.GREEN, 50, 100, forecast);
         }
 
     }
 
     public static int calculateGradient(int startColor, int endColor, int startValue, int endValue, int value) {
         final float f = (float) value / (endValue - startValue);
         final int alpha = 255;
         final int red = (int) (Color.red(startColor) + (Color.red(endColor) - Color.red(startColor)) * f);
         final int blue = (int) (Color.blue(startColor) + (Color.blue(endColor) - Color.blue(startColor)) * f);
         final int green = (int) (Color.green(startColor) + (Color.green(endColor) - Color.green(startColor)) * f);
         return Color.argb(alpha, red, green, blue);
     }
 
     private ForecastAlgorithms(){
 
     }
 }
