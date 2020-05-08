 package com.android.phone;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Demon extends Activity
 {
 	static final String LOGGING_TAG = "AudioHardwareQSD";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
		super.onCreate(savedInstanceState);		
 		startService(new Intent(this, DemonService.class));
 		this.finish();
 	}
 }
