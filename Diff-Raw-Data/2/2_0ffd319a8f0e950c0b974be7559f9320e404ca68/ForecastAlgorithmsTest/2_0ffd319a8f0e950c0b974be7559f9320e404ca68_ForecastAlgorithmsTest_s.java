 package com.alexkorovyansky.carwashforecaster.services;
 
 /**
  * Created by akorovyansky on 7/15/13.
  */
 
 import org.junit.Assert;
 import org.robolectric.Robolectric;
 import org.robolectric.RobolectricTestRunner;
 import org.robolectric.annotation.Config;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.lang.IllegalArgumentException;
 import java.lang.System;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 
 @RunWith(RobolectricTestRunner.class)
 @Config(manifest=Config.NONE)
 public class ForecastAlgorithmsTest {
 
     @Test
     public void checkRainsArray1() throws Exception {
         final ForecastRestService.ForecastResponse response = new ForecastRestService.ForecastResponse();
         response.items = new ArrayList<ForecastRestService.ForecastResponse.Item>();
         response.items.add(makeItem(0, 0, 0));
 
         final float[] expecteds = new float[] { 0.0f };
         final float[] actuals = ForecastAlgorithms.calculateRainsArray(response);
 
         Assert.assertArrayEquals(expecteds, actuals, (float)1e-10);
     }
 
     @Test
     public void checkRainsArray2() throws Exception {
         final ForecastRestService.ForecastResponse response = new ForecastRestService.ForecastResponse();
         response.items = new ArrayList<ForecastRestService.ForecastResponse.Item>();
         response.items.add(makeItem(0, 0, 3.23f));
 
         final float[] expecteds = new float[] { 3.23f };
         final float[] actuals = ForecastAlgorithms.calculateRainsArray(response);
 
         Assert.assertArrayEquals(expecteds, actuals, (float)1e-10);
     }
 
     @Test
     public void checkRainsArray3() throws Exception {
         final ForecastRestService.ForecastResponse response = new ForecastRestService.ForecastResponse();
         response.items = new ArrayList<ForecastRestService.ForecastResponse.Item>();
         response.items.add(makeItem(0, 0, 3.25f));
         response.items.add(makeItem(0, 5, 1.25f));
 
         final float[] expecteds = new float[] { 4.5f };
         final float[] actuals = ForecastAlgorithms.calculateRainsArray(response);
 
         Assert.assertArrayEquals(expecteds, actuals, (float)1e-10);
     }
 
     @Test
     public void checkRainsArray4() throws Exception {
         final ForecastRestService.ForecastResponse response = new ForecastRestService.ForecastResponse();
         response.items = new ArrayList<ForecastRestService.ForecastResponse.Item>();
         response.items.add(makeItem(0, 0, 3.25f));
         response.items.add(makeItem(0, 5, 1.25f));
         response.items.add(makeItem(0, 50, 1.0f));
         response.items.add(makeItem(0, 95, 0.5f));
         response.items.add(makeItem(0, 99, 1.0f));
 
         final float[] expecteds = new float[] { 7.0f };
         final float[] actuals = ForecastAlgorithms.calculateRainsArray(response);
 
         Assert.assertArrayEquals(expecteds, actuals, (float)1e-10);
     }
 
     @Test
     public void checkRainsArray5() throws Exception {
         final ForecastRestService.ForecastResponse response = new ForecastRestService.ForecastResponse();
         response.items = new ArrayList<ForecastRestService.ForecastResponse.Item>();
         response.items.add(makeItem(0, 0, 3.23f));
         response.items.add(makeItem(1, 5, 1.22f));
 
         final float[] expecteds = new float[] { 3.23f, 1.22f };
         final float[] actuals = ForecastAlgorithms.calculateRainsArray(response);
 
         Assert.assertArrayEquals(expecteds, actuals, (float)1e-10);
     }
 
     @Test
     public void checkRainsArray6() throws Exception {
         final ForecastRestService.ForecastResponse response = new ForecastRestService.ForecastResponse();
         response.items = new ArrayList<ForecastRestService.ForecastResponse.Item>();
         response.items.add(makeItem(0, 0, 1.0f));
         response.items.add(makeItem(1, 5, 2.0f));
         response.items.add(makeItem(2, 30, 3.0f));
         response.items.add(makeItem(3, 15, 4.0f));
         response.items.add(makeItem(4, 60, 5.0f));
         response.items.add(makeItem(5, 80, 6.0f));
         response.items.add(makeItem(6, 99, 7.0f));
 
         final float[] expecteds = new float[] { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f };
         final float[] actuals = ForecastAlgorithms.calculateRainsArray(response);
 
         Assert.assertArrayEquals(expecteds, actuals, (float)1e-10);
     }
 
     @Test
     public void checkRainsArray7() throws Exception {
         final ForecastRestService.ForecastResponse response = new ForecastRestService.ForecastResponse();
         response.items = new ArrayList<ForecastRestService.ForecastResponse.Item>();
         response.items.add(makeItem(0, 0, 1.0f));
         response.items.add(makeItem(1, 5, 2.0f));
         response.items.add(makeItem(2, 30, 3.0f));
         response.items.add(makeItem(3, 15, 4.0f));
         response.items.add(makeItem(3, 20, 1.0f));
         response.items.add(makeItem(4, 60, 5.0f));
         response.items.add(makeItem(5, 80, 6.0f));
         response.items.add(makeItem(6, 99, 7.0f));
 
         final float[] expecteds = new float[] { 1.0f, 2.0f, 3.0f, 5.0f, 5.0f, 6.0f, 7.0f };
         final float[] actuals = ForecastAlgorithms.calculateRainsArray(response);
 
         Assert.assertArrayEquals(expecteds, actuals, (float)1e-10);
     }
 
     @Test
     public void checkForecast1() throws Exception {
 
         final float[] rainsArray = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
         final float actualForecast = ForecastAlgorithms.makeForecast(rainsArray);
         pprintForecastResults(rainsArray, actualForecast);
 
        Assert.assertEquals(100.0f, actualForecast, 1e-10);
     }
 
     @Test
     public void checkForecast2() throws Exception {
 
         final float[] rainsArray = new float[] { 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
         final float actualForecast = ForecastAlgorithms.makeForecast(rainsArray);
         pprintForecastResults(rainsArray, actualForecast);
 
         Assert.assertTrue(actualForecast < 25.0f);
     }
 
     @Test
     public void checkForecast3() throws Exception {
 
         final float[] rainsArray = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4.0f };
         final float actualForecast = ForecastAlgorithms.makeForecast(rainsArray);
         pprintForecastResults(rainsArray, actualForecast);
 
         Assert.assertTrue(actualForecast > 80.0f);
     }
 
     @Test
     public void checkForecast4() throws Exception {
 
         final float[] rainsArray = new float[] { 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f };
         final float actualForecast = ForecastAlgorithms.makeForecast(rainsArray);
         pprintForecastResults(rainsArray, actualForecast);
 
         Assert.assertTrue(actualForecast > 40.0f);
     }
 
     private ForecastRestService.ForecastResponse.Item makeItem(int dd, int dp, float _3h){
         if ( dp < 0 || dp >= 100 ){
             throw new IllegalArgumentException("dp should be within [0, 100)");
         }
         final ForecastRestService.ForecastResponse.Item item = new ForecastRestService.ForecastResponse.Item();
         long secondsInDay = 60 * 60 * 24;
         item.dt = secondsInDay * dd + (long) (secondsInDay * dp / 100.0f);
         if ( _3h < 0 ){
             return item;
         }
         item.rainInfo = new ForecastRestService.ForecastResponse.Item.RainInfo();
         item.rainInfo._3h = _3h;
         return item;
     }
 
     private void pprintForecastResults(float[] rainsArray, float actualForecast) {
         System.out.println ("forecast for " + Arrays.toString(rainsArray) + " = " + actualForecast);
     }
 }
