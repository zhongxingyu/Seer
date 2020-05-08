 package com.brianreber.cokerewards;
 
 import android.app.Activity;
import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 /**
  * Login activity for the app
  * 
  * @author breber
  */
 public class RegisterActivity extends Activity {
 
 	/**
 	 * Google Analytics tracker
 	 */
 	private GoogleAnalyticsTracker tracker;
 
 	/**
 	 * Handler for posting runnables between threads
 	 */
 	private Handler mHandler = new Handler();
 	
 	/**
 	 * A loading dialog box
 	 */
	private Dialog dlg;
 
 	/**
 	 * A Runnable that will close this activity if the user is
 	 * logged in properly
 	 */
 	private Runnable updateUIRunnable = new Runnable() {
 		@Override
 		public void run() {
 			if (CokeRewardsActivity.isLoggedIn(RegisterActivity.this)) {
 				RegisterActivity.this.finish();
 			} else {
 				SharedPreferences prefs = getSharedPreferences(CokeRewardsActivity.COKE_REWARDS, Context.MODE_WORLD_WRITEABLE);
 				Editor edit = prefs.edit();
 
 				edit.remove(CokeRewardsActivity.EMAIL_ADDRESS);
 				edit.remove(CokeRewardsActivity.PASSWORD);
 				edit.commit();
 			}
 			
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 		}
 	};
 
 	/**
 	 * A Runnable that will show an error Toast
 	 */
 	private Runnable errorRunnable = new Runnable() {
 		@Override
 		public void run() {
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 			
 			Toast.makeText(RegisterActivity.this, "Error logging in. Please try again.", Toast.LENGTH_SHORT).show();
 		}
 	};
 
 
 	/**
 	 * Set up the basic UI elements
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.login);
 
 		tracker = GoogleAnalyticsTracker.getInstance();
 
 		dlg = new ProgressDialog(this);
		dlg.setTitle(R.string.loggingin);
 		dlg.setCancelable(false);
 		
 		Button login = (Button) findViewById(R.id.performLogin);
 		login.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dlg.show();
 				getNumberOfPoints();
 			}
 		});
 	}
 
 	/**
 	 * Get the number of points from the MyCokeRewards server
 	 */
 	private void getNumberOfPoints() {
 		EditText txt = (EditText) findViewById(R.id.username);
 		final String emailAddress = txt.getText().toString();
 
 		txt = (EditText) findViewById(R.id.password);
 		final String password = txt.getText().toString();
 
 		if ("".equals(emailAddress) || "".equals(password)) {
 			Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		SharedPreferences prefs = getSharedPreferences(CokeRewardsActivity.COKE_REWARDS, Context.MODE_WORLD_WRITEABLE);
 		Editor edit = prefs.edit();
 
 		edit.putString(CokeRewardsActivity.EMAIL_ADDRESS, emailAddress);
 		edit.putString(CokeRewardsActivity.PASSWORD, password);
 		edit.commit();
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					tracker.trackEvent("Logon", "Logon", "Logging on", 0);
 					try {
 						CokeRewardsActivity.getData(RegisterActivity.this,
 								CokeRewardsRequest.createLoginRequestBody(RegisterActivity.this),
 								updateUIRunnable, true);
 					} catch (Exception e) {
 						tracker.trackEvent("Exception", "ExceptionSecureLogon", "Exception when trying to log on: " + e.getMessage(), 0);
 						
 						CokeRewardsActivity.getData(RegisterActivity.this,
 								CokeRewardsRequest.createLoginRequestBody(RegisterActivity.this),
 								updateUIRunnable, false);
 					} 
 				} catch (Exception e) {
 					tracker.trackEvent("Exception", "ExceptionLogon", "Exception when trying to log on: " + e.getMessage(), 0);
 					e.printStackTrace();
 
 					mHandler.post(errorRunnable);
 				}
 			}
 		}).start();
 	}
 }
