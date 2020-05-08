 package com.example.getconnected.activities;
 
 import com.example.getconnected.R;
 
 import android.os.Bundle;
 
 public class MarketplaceActivity extends BaseActivity{
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_marketplace);
		initLayout(R.string.title_activity_marketplace, true, true, true, false);
 	}
 }
