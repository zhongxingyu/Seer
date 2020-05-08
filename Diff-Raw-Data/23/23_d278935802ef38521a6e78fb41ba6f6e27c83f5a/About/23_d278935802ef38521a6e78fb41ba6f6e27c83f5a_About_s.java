 package com.sportsfire.exposure;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class About extends Activity{
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		setContentView(R.layout.about);
 }
 }
