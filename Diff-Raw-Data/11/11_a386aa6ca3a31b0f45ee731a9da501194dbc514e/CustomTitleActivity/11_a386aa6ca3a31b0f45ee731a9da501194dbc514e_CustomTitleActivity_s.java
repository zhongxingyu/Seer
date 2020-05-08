 package com.playphone.sdk.example;
 
 import com.playphone.multinet.MNDirectUIHelper;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 
 public class CustomTitleActivity extends Activity{
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
 		
 		Button btnHome = (Button) findViewById(R.id.btnHome);
 		btnHome.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Log.d("playphone","Home button has been pressed");
 				//Need to start the Home activity but clear all the rest of the stack
 				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 				startActivity(intent);
 			}
 		});

 	}
 
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		MNDirectUIHelper.setHostActivity(null);
 	}
  
 	@Override
 	protected void onResume() {
 		Log.d("playphone","CustomTitle onResume() called");
 		MNDirectUIHelper.setHostActivity(this);
 		super.onResume();
 	}
 
 	
 	
 }
