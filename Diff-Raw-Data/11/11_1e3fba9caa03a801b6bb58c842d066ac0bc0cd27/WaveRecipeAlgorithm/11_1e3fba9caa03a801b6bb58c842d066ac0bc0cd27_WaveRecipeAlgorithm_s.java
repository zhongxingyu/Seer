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
 * Class describing a WaveRecipeAlgorithm, which provides the
  * computational component of a WaveRecipe.  Recipe creators must implement
  * this interface, and provide that implementation within their recipe.
  */
public abstract class WaveRecipeAlgorithm {
     
    public abstract boolean setWaveRecipeAlgorithmListener(WaveRecipeAlgorithmListener listener);
     
    public abstract void ingestSensorData(WaveSensorData sensorData);
 }
