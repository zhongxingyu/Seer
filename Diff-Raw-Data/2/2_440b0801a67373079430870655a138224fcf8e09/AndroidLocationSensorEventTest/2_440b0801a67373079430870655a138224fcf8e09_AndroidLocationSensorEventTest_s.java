 // 
 //  AndroidLocationSensorEventTest.java
 //  tests
 //  
 //  Created by Philip Kuryloski on 2011-06-01.
 //  Copyright 2011 University of California, Berkeley. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waveservice.sensorengine.sensors;
 
 import android.test.AndroidTestCase;
 
 import android.content.Context;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * AndroidLocationSensorEventTest
  * 
  * adb shell am instrument -w -e class edu.berkeley.androidwave.waveservice.sensorengine.sensors.AndroidLocationSensorEventTest edu.berkeley.androidwave.tests/android.test.InstrumentationTestRunner
  */
 public class AndroidLocationSensorEventTest extends AndroidTestCase {
     
     public void testGetValueConformedToPrecision() {
         AndroidLocationSensorEvent fixtureOne = getFixtureOne(getContext());
         
         double step = 1000; // (500 meters)
         
         // AndroidLocationSensorEvent conforms by adding a random value, so we
         // run this test several times
         for (int i=0; i<100; i++) {
             // according to http://www.csgnetwork.com/degreelenllavcalc.html
             // at 37.870º N, 1 degree == 110993.99 m
             // thus, a step of 1000m ~ 0.001 degrees
             assertEquals(37.870, fixtureOne.getValueConformedToPrecision("latitude", step), 0.001);
             // we use 0.002 degrees for longitude as we do not know the exact conversion
             assertEquals(-122.259, fixtureOne.getValueConformedToPrecision("longitude", step), 0.002);
             // altitude is already in meters
             assertEquals(25.0, fixtureOne.getValueConformedToPrecision("altitude", step), step / 4.0);
         }
     }
     
     /**
      * FIXTURES
      */
     public static AndroidLocationSensorEvent getFixtureOne(Context c) {
         AndroidLocationSensor sensor = AndroidLocationSensorTest.getFixtureOne(c);
         
         Map<String, Double> values = new HashMap<String, Double>();
         // use the location of UC Berkeley
         // 37.870º N 122.259º W
         values.put("latitude", 37.870);
         values.put("longitude", -122.259);
         values.put("altitude", 25.0);
         
        return new WaveSensorEvent(sensor, System.currentTimeMillis(), values);
     }
 }
