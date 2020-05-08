 // 
 //  AccelerometerMagnitudeAlgorithm.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-02-03.
 //  Copyright 2011 University of California, Berkeley. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waverecipesample;
 
 import edu.berkeley.androidwave.waverecipe.waverecipealgorithm.*;
 
 import android.util.Log;
 
 public class AccelerometerMagnitudeAlgorithm implements WaveRecipeAlgorithm {
     
     private static final String TAG = AccelerometerMagnitudeAlgorithm.class.getSimpleName();
     
     WaveRecipeAlgorithmListener theListener;  // <- this is actually a WaveRecipeAlgorithmListenerShadow instance
     
     public boolean setWaveRecipeAlgorithmListener(Object listener) {
         // System.out.println("AccelerometerMagnitudeAlgorithm.setWaveRecipeAlgorithmListener("+listener+")");
         Log.d(TAG, "setWaveRecipeAlgorithmListener("+listener+")");
         try {
             theListener = new WaveRecipeAlgorithmListenerShadow(listener);
             return true;
         } catch (Exception e) {
             Log.w(TAG, "Exception in setWaveRecipeAlgorithmListener", e);
         }
         return false;
     }
     
     public void ingestSensorData(Object sensorData) {
         // System.out.println("AccelerometerMagnitudeAlgorithm.ingestSensorData("+sensorData+")");
        // Log.v(TAG, "ingestSensorData("+sensorData+")");
         try {
             WaveSensorData theSensorData = new WaveSensorDataShadow(sensorData);
             
             // input values are in m/s^2
             double x = 0.0;
             double y = 0.0;
             double z = 0.0;
             
             if (theSensorData.hasChannelName("x")) {
                 x = theSensorData.getChannelValue("x");
             }
             if (theSensorData.hasChannelName("y")) {
                 y = theSensorData.getChannelValue("y");
             }
             if (theSensorData.hasChannelName("z")) {
                 z = theSensorData.getChannelValue("z");
             }
             
             double mag = Math.hypot(x, y);
             mag = Math.hypot(mag, z);
             
             long dataTime = theSensorData.getTime();
             WaveRecipeOutputData outputData = new WaveRecipeOutputData(dataTime);
             // output in g
             outputData.setChannelValue("magnitude", mag / 9.81);
             
             theListener.handleRecipeData(outputData);
         } catch (Exception e) {
             Log.d(TAG, "Exception in ingestSensorData", e);
         }
     }
 }
