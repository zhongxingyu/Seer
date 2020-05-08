 package com.github.barcodescanner.activities;
 
 import com.github.barcodescanner.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Window;
 
 public class HelpActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_help);
 	}
 
 }
