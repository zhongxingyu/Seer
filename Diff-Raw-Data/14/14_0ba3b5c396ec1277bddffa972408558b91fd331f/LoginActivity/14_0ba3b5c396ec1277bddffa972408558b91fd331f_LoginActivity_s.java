 package com.example.laundrylist;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class LoginActivity extends Activity {
 	private LoginController controller;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_login);
 		controller = new LoginController(this);
 	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

 }
