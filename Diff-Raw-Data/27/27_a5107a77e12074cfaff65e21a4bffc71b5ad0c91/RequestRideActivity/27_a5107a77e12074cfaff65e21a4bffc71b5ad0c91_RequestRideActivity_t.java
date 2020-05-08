 package com.example.getconnected.activities;
 
 import com.example.getconnected.R;
 
 import android.os.Bundle;
 
 public class RequestRideActivity extends BaseActivity {
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_requestride);
		initLayout(R.string.title_activity_requestride, true, true, true, true);
 	}
 }
