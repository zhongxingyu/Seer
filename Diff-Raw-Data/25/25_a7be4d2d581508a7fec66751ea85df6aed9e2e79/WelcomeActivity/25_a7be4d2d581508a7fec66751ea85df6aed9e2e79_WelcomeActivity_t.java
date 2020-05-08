 package com.clarityforandroid.controllers;
 
 import java.util.Calendar;
 
 import com.clarityforandroid.R;
 import com.clarityforandroid.helpers.ClarityDialogFactory;
 import com.clarityforandroid.models.ProviderModel;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.animation.AnimationUtils;
 import android.widget.EditText;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Typeface;
 
 /**
  * The controller that controls logging users in or registering them 
  * with the system.
  * 
  * @author Jonathan Ballands
  * @version 1.0
  */
 public class WelcomeActivity extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	
 		// Set up views.
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.setContentView(R.layout.activity_welcome);
 		findViewById(R.id.imageViewClarityLogo).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein));
 		EditText passwordField = (EditText)(findViewById(R.id.passwordField));
 		passwordField.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
 		
 		// Listeners. Use lambda expressions.
 		findViewById(R.id.loginButton).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				new AsyncLoginLoader().execute();
 			}
 		});
 	}
 	
 	/**
 	 * The asynchronous loader that communicates with the Clarity server
 	 * and attempts to sign the user in.
 	 * 
 	 * @author Jonathan Ballands
 	 * @version 1.0
 	 */
 	private class AsyncLoginLoader extends AsyncTask<Void, Void, Void> {
 		
 		ProgressDialog loadingDialog;
 		
 		@Override
 		protected void onPreExecute() {
 			loadingDialog = ClarityDialogFactory.displayNewProgressDialog(WelcomeActivity.this, "Communicating with the Clarity server.");
 		}
 		
 		@Override
 		protected Void doInBackground(Void... voids) {
 			// TODO: Connect to server.
 			
 			// Just go to sleep to simulate a connection.
 			try {
 				Thread.sleep(10000);
 			} 
 			catch (InterruptedException e) {
 				System.out.println("The asynctask thread was woken up before scheduled : " + e.getCause() + ": " + e.getMessage());
 				System.exit(1);
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void params) {
 			loadingDialog.dismiss();
 			// TODO: Do something to figure out if login was successful (or not).
 			
 			// Bundle provider data.
			ProviderModel newModel = new ProviderModel("Akshay", "Sharma", "johndoe", "Austin, Texas", Calendar.getInstance(), null);
 			
 			// Start home activity.
 			Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
 			intent.putExtra("provider_model", newModel);
 			startActivity(intent);
 		}
 		
 	}
 	
 }
