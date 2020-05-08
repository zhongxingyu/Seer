 package com.relurori.engine.graphics;
 
import android.util.Log;

 public class Scale {
 
	private static final String TAG = Scale.class.getSimpleName();
	
 	private float xMultiplier;
 	private float yMultiplier;
 	
 	public Scale(float width, float height) {
 		xMultiplier = width / 1280;
 		yMultiplier = height / 720;
 		
		Log.d(TAG,"multpliers=" + xMultiplier + "," + yMultiplier);
 	}
 	
 	public float getScaledY(float y) {
 		return yMultiplier * y;
 	}
 	
 
 	public float getScaledX(float x) {
 		return xMultiplier * x;
 	}
 }
