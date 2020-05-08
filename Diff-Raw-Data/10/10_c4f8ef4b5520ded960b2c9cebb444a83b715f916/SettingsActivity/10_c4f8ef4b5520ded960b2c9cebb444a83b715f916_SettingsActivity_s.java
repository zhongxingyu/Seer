 package com.jessescott.sonicity;
 
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
 import processing.core.*;
 
 public class SettingsActivity extends PApplet {
 	
 	// GLOBALS
 	//private static final String TAG = "SoniCity";
 	
 	/*
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// UI
 		setContentView(R.layout.settings_layout);
 		Log.v(TAG, " - Starting The Settings Screen - ");
 	}
 	*/
 	
 	/* LIFECYCLE */
 	public void setup() {
 		background(255, 0, 0);
 		
 	}
 	
 	public void draw() {
 		
 		
 	}
 	
 	/* LIFECYCLE */
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		println(" - Exiting From The Settings Screen - ");
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 	}
 
 }
