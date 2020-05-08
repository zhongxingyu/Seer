 // 
 //  AccelerometerMagnitudeAlgorithm.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-02-03.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waverecipesample;
 
 import edu.berkeley.androidwave.waverecipe.waverecipealgorithm.WaveRecipeAlgorithm;
 import edu.berkeley.androidwave.waverecipe.waverecipealgorithm.WaveRecipeAlgorithmListener;
 import edu.berkeley.androidwave.waveservice.sensorengine.WaveSensorData;
 
public class AccelerometerMagnitudeAlgorithm implements WaveRecipeAlgorithm {
     
     public boolean setWaveRecipeAlgorithmListener(WaveRecipeAlgorithmListener listener) {
         // null implementation
         return false;
     }
     
     public void ingestSensorData(WaveSensorData sensorData) {
         // null implementation
     }
 }
