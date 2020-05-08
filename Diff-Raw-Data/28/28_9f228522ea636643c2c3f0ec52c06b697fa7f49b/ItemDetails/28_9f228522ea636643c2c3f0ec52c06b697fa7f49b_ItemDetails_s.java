 package com.brousalis;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class ItemDetails extends Activity {
 	
 	private Bundle _extras;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.item_details);
 		_extras = this.getIntent().getExtras();
 		_extras.get("title");
 		_extras.get("summary");
 	}
 }
