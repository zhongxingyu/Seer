 package com.example.getconnected.activities;
 
 import com.example.getconnected.R;
 
 import android.os.Bundle;
 
 public class CreateRideActivity extends BaseActivity{
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_createride);
		initLayout(R.string.title_activity_createride, true, true, true, true);
 	}
 }
