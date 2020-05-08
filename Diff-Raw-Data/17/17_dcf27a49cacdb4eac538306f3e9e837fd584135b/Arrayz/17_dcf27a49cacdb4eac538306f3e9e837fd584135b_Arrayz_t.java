 package com.example.helloandroid;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 public class Arrayz extends Activity {
 
	private static final String DTAG = Arrayz.class.getSimpleName();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.arrayz);
 		String[] aColors = getResources().getStringArray(R.array.aColors);
 
 		for (int i = 0; i < aColors.length; i++) {
			Log.v(DTAG, "color#: " + i + " is " + aColors[i]);
 			String color = aColors[i];
 			char[] charArray = color.toCharArray();
 			for (int j = 0; j < charArray.length; j++) {
				Log.v(DTAG, "letter#: " + j + " -> " + charArray[j]);
 			}
 		}
 
 		for (String num : aColors) {
			Log.v(DTAG, num);
 		}
 
 		int numColor = aColors.length;
		Log.v(DTAG, "numColor: " + numColor);
 
 		while (numColor > -1) {
			Log.v(DTAG, "color#: " + numColor + " is " + aColors[numColor]);
 			numColor--;
 		}
 
 	}
 
 }
