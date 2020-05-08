 package com.github.barcodescanner.activities;
 
 import com.github.barcodescanner.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.view.Window;
 
 public class IntroductionActivity extends Activity {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "IntroductionActivity";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_introduction);
 	}
 
	public void enterAsScanMode(View view) {
 		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("isOwner", false);
 		startActivity(intent);
 	}
 
	public void enterAsAdminMode(View view) {
 		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("isOwner", true);
 		startActivity(intent);
 	}
 
 }
