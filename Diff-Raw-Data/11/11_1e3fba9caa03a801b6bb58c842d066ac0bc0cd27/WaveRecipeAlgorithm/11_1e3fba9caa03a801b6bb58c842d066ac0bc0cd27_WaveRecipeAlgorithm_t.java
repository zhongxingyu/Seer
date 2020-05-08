 // 
 //  WaveRecipeAlgorithm.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-02-02.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waverecipe.waverecipealgorithm;
 
 import edu.berkeley.androidwave.waveservice.sensorengine.WaveSensorData;
 
 /**
  * WaveRecipeAlgorithm
  *
 * Interface describing a WaveRecipeAlgorithm, which provides the
  * computational component of a WaveRecipe.  Recipe creators must implement
  * this interface, and provide that implementation within their recipe.
  */
public interface WaveRecipeAlgorithm {
     
    public boolean setWaveRecipeAlgorithmListener(WaveRecipeAlgorithmListener listener);
     
    public void ingestSensorData(WaveSensorData sensorData);
 }
