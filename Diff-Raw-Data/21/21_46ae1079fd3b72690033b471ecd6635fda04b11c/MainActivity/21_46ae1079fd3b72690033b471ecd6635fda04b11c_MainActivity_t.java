 package com.example.therunningapp;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void settings (View view) {
 		Intent intent = new Intent(this, Settings.class);
 		startActivity(intent);
 
 	}
 
 	public void history (View view) {
 		Intent intent = new Intent(this, History.class);
 		startActivity(intent);
 
 	}
 	
 	
 	public void workoutStart (View view) {
 		if(servicesConnected()) {
 		Intent intent = new Intent(this, WorkoutStart.class);
 		startActivity(intent);
 		}
 		else {
 			String T_Errortext = "Google Play services are not available";
			TextView T_textView = (TextView) findViewById(R.id.date);
 			T_textView.setText(T_Errortext);
 		}
 
 	}
 	
 	//The following code is retrieved from developer.android.com,
 	//to check if google play services are available.
 	// Global constants
     /*
      * Define a request code to send to Google Play services
      * This code is returned in Activity.onActivityResult
      */
     private final static int
             CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 
     // Define a DialogFragment that displays the error dialog
     public static class ErrorDialogFragment extends DialogFragment {
         // Global field to contain the error dialog
         private Dialog mDialog;
         // Default constructor. Sets the dialog field to null
         public ErrorDialogFragment() {
             super();
             mDialog = null;
         }
         // Set the dialog to display
         public void setDialog(Dialog dialog) {
             mDialog = dialog;
         }
         // Return a Dialog to the DialogFragment.
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return mDialog;
         }
     }
     
     /*
      * Handle results returned to the FragmentActivity
      * by Google Play services
      */
     @Override
     protected void onActivityResult(
             int requestCode, int resultCode, Intent data) {
         // Decide what to do based on the original request code
         switch (requestCode) {
             
             case CONNECTION_FAILURE_RESOLUTION_REQUEST :
             /*
              * If the result code is Activity.RESULT_OK, try
              * to connect again
              */
                 switch (resultCode) {
                     case Activity.RESULT_OK :
                     /*
                      * Try the request again
                      */
                     
                     break;
             }
         }
     }
    
     private boolean servicesConnected() {
         // Check that Google Play services is available
         int resultCode =
                 GooglePlayServicesUtil.
                         isGooglePlayServicesAvailable(this);
         // If Google Play services is available
         if (ConnectionResult.SUCCESS == resultCode) {
             // In debug mode, log the status
             Log.d("Location Updates",
                     "Google Play services is available.");
             // Continue
             return true;
         // Google Play services was not available for some reason
         } else {
             // Get the error dialog from Google Play services
             Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                     resultCode,
                     this,
                     CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
             // If Google Play services can provide an error dialog
             if (errorDialog != null) {
                 // Create a new DialogFragment for the error dialog
                 ErrorDialogFragment errorFragment =
                         new ErrorDialogFragment();
                 // Set the dialog in the DialogFragment
                 errorFragment.setDialog(errorDialog);
                 // Show the error dialog in the DialogFragment
                 errorFragment.show(getSupportFragmentManager(),
                         "Location Updates");
             }
             return false;
         }
 		
     }
 	
 }
