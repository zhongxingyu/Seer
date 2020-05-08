 package com.taxisoft.taxiorder;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class OrderFromActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_order_from);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_order_from, menu);
 		return true;
 	}
 
 }
