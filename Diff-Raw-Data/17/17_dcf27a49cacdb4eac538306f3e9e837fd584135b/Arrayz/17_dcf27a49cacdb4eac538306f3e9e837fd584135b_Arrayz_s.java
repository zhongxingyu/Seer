 package com.example.helloandroid;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 public class Arrayz extends Activity {
 
	private static final String DEBUG_TAG = "DEBUG_TAG -> arrayz";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.arrayz);
 		String[] aColors = getResources().getStringArray(R.array.aColors);
 
 		for (int i = 0; i < aColors.length; i++) {
			Log.v(DEBUG_TAG, "color#: " + i + " is " + aColors[i]);
 			String color = aColors[i];
 			char[] charArray = color.toCharArray();
 			for (int j = 0; j < charArray.length; j++) {
				Log.v(DEBUG_TAG, "letter#: " + j + " -> " + charArray[j]);
 			}
 		}
 
 		for (String num : aColors) {
			Log.v(DEBUG_TAG, num);
 		}
 
 		int numColor = aColors.length;
		Log.v(DEBUG_TAG, "numColor: " + numColor);
 
 		while (numColor > -1) {
			Log.v(DEBUG_TAG, "color#: " + numColor + " is " + aColors[numColor]);
 			numColor--;
 		}
 
 	}
 
 }
