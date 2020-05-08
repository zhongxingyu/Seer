 /*
  * Copyright (C) 2013 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.example.android.activityrecognition;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.Spanned;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.example.android.activityrecognition.ActivityUtils.REQUEST_TYPE;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Sample application that demonstrates the use of
  * ActivityRecognitionClient}. It registers for activity detection updates
  * at a rate of 20 seconds, logs them to a file, and displays the detected
  * activities with their associated confidence levels.
  * <p>
  * An IntentService receives activity detection updates in the background
  * so that detection can continue even if the Activity is not visible.
  */
 public class MainActivity extends Activity {
 	
 	public static MainActivity mainContext = null;
 
     private static final int MAX_LOG_SIZE = 5000;
 
     // Instantiates a log file utility object, used to log status updates
     private LogFile mLogFile;
 
     // Store the current request type (ADD or REMOVE)
     private REQUEST_TYPE mRequestType;
 
     private ImageView image_status = null;
 
     /*
      *  Intent filter for incoming broadcasts from the
      *  IntentService.
      */
     IntentFilter mBroadcastFilter;
 
     // Instance of a local broadcast manager
     private LocalBroadcastManager mBroadcastManager;
 
     // The activity recognition update request object
     private DetectionRequester mDetectionRequester;
 
     // The activity recognition update removal object
     private DetectionRemover mDetectionRemover;
 
     /*
      * Set main UI layout, get a handle to the ListView for logs, and create the broadcast
      * receiver.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Set the main layout
         setContentView(R.layout.activity_main);
 
         image_status = (ImageView) findViewById(R.id.status_image);
 
         // Set the broadcast receiver intent filer
         mBroadcastManager = LocalBroadcastManager.getInstance(this);
 
         // Create a new Intent filter for the broadcast receiver
         mBroadcastFilter = new IntentFilter(ActivityUtils.ACTION_REFRESH_STATUS_LIST);
         mBroadcastFilter.addCategory(ActivityUtils.CATEGORY_LOCATION_SERVICES);
 
         // Get detection requester and remover objects
         mDetectionRequester = new DetectionRequester(this);
         mDetectionRemover = new DetectionRemover(this);
 
         // Create a new LogFile object
         mLogFile = LogFile.getInstance(this);
         
         LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(RECEIVE_JSON);
         bManager.registerReceiver(bReceiver, intentFilter);
 
     }
 
     /*
      * Handle results returned to this Activity by other Activities started with
      * startActivityForResult(). In particular, the method onConnectionFailed() in
      * DetectionRemover and DetectionRequester may call startResolutionForResult() to
      * start an Activity that handles Google Play services problems. The result of this
      * call returns here, to onActivityResult.
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 
         // Choose what to do based on the request code
         switch (requestCode) {
 
             // If the request code matches the code sent in onConnectionFailed
             case ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
 
                 switch (resultCode) {
                     // If Google Play services resolved the problem
                     case Activity.RESULT_OK:
 
                         // If the request was to start activity recognition updates
                         if (ActivityUtils.REQUEST_TYPE.ADD == mRequestType) {
 
                             // Restart the process of requesting activity recognition updates
                             mDetectionRequester.requestUpdates();
 
                         // If the request was to remove activity recognition updates
                         } else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType ){
 
                                 /*
                                  * Restart the removal of all activity recognition updates for the 
                                  * PendingIntent.
                                  */
                                 mDetectionRemover.removeUpdates(
                                     mDetectionRequester.getRequestPendingIntent());
 
                         }
                     break;
 
                     // If any other result was returned by Google Play services
                     default:
 
                         // Report that Google Play services was unable to resolve the problem.
                         Log.d(ActivityUtils.APPTAG, getString(R.string.no_resolution));
                 }
 
             // If any other request code was received
             default:
                // Report that this Activity received an unknown requestCode
                Log.d(ActivityUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));
 
                break;
         }
     }
 
     /*
      * Create the menu
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
 
     }
 
     /**
      * Verify that Google Play services is available before making a request.
      *
      * @return true if Google Play services is available, otherwise false
      */
     private boolean servicesConnected() {
 
         // Check that Google Play services is available
         int resultCode =
                 GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 
         // If Google Play services is available
         if (ConnectionResult.SUCCESS == resultCode) {
 
             // In debug mode, log the status
             Log.d(ActivityUtils.APPTAG, getString(R.string.play_services_available));
 
             // Continue
             return true;
 
         // Google Play services was not available for some reason
         } else {
 
             // Display an error dialog
             GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
             return false;
         }
     }
     /**
      * Respond to "Start" button by requesting activity recognition
      * updates.
      * @param view The view that triggered this method.
      */
     public void onStartUpdates(View view) {
 
         // Check for Google Play services
         if (!servicesConnected()) {
 
             return;
         }
 
         /*
          * Set the request type. If a connection error occurs, and Google Play services can
          * handle it, then onActivityResult will use the request type to retry the request
          */
         mRequestType = ActivityUtils.REQUEST_TYPE.ADD;
 
         // Pass the update request to the requester object
         mDetectionRequester.requestUpdates();
     }
 
     /**
      * Respond to "Stop" button by canceling updates.
      * @param view The view that triggered this method.
      */
     public void onStopUpdates(View view) {
 
         // Check for Google Play services
         if (!servicesConnected()) {
 
             return;
         }
 
         /*
          * Set the request type. If a connection error occurs, and Google Play services can
          * handle it, then onActivityResult will use the request type to retry the request
          */
         mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;
 
         // Pass the remove request to the remover object
         mDetectionRemover.removeUpdates(mDetectionRequester.getRequestPendingIntent());
 
         /*
          * Cancel the PendingIntent. Even if the removal request fails, canceling the PendingIntent
          * will stop the updates.
          */
         mDetectionRequester.getRequestPendingIntent().cancel();
     }
 
    
     
     
     
     
     public static final String RECEIVE_JSON = "scooby doo, where are you";
     BroadcastReceiver bReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
 
             /*
              * When an Intent is received from the update listener IntentService, update
              * the displayed log.
              */
             //updateActivityHistory();
         	if(intent.getAction().equals(RECEIVE_JSON)) {
         		String data = intent.getExtras().getString("json");
         		if ((data.equalsIgnoreCase("still")) ||  (data.equalsIgnoreCase("unknow")) || (data.equalsIgnoreCase("tilting"))){
 				//Toast.makeText(mainContext, data, Toast.LENGTH_SHORT).show();
         			image_status.setImageResource(R.drawable.stop);
        		} else if (data.equalsIgnoreCase("on foot")){
         			image_status.setImageResource(R.drawable.foot);
         		} else {
         			image_status.setImageResource(R.drawable.road);
         		}
         	}
         	
         	
         }
     };
 }
