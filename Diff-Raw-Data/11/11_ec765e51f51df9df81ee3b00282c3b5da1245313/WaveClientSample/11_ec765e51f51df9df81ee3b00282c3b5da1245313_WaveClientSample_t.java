 // 
 //  WaveClientSample.java
 //  WaveClientSample
 //  
 //  Created by Philip Kuryloski on 2011-03-22.
 //  Copyright 2011 University of California, Berkeley. All rights reserved.
 // 
 
 package edu.berkeley.waveclientsample;
 
 import edu.berkeley.androidwave.waveclient.IWaveServicePublic;
 import edu.berkeley.androidwave.waveclient.IWaveRecipeOutputDataListener;
 import edu.berkeley.androidwave.waveclient.WaveRecipeOutputDataImpl;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WaveClientSample extends Activity {
     
     private static final String TAG = WaveClientSample.class.getSimpleName();
     
     private static final String ACTION_WAVE_SERVICE = "edu.berkeley.androidwave.intent.action.WAVE_SERVICE";
     private static final String ACTION_DID_AUTHORIZE = "edu.berkeley.androidwave.intent.action.DID_AUTHORIZE";
     private static final String ACTION_DID_DENY = "edu.berkeley.androidwave.intent.action.DID_DENY";
     private static final int REQUEST_CODE_AUTH = 1;
     private final String RECIPE_ID = "edu.berkeley.waverecipe.AccelerometerMagnitude";
     private final String API_KEY = "snathtosoeaseseoadrc,h.bmte";
     
     private IWaveServicePublic mWaveService;
     private boolean mBound;
     
     Button authRequestButton;
     TextView messageTextView;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setTitle(R.string.main_activity_name);
         setContentView(R.layout.main);
         
         authRequestButton = (Button) findViewById(R.id.auth_request_button);
         messageTextView = (TextView) findViewById(R.id.message_textview);
         
         // disable the button until we have connected to the service
         authRequestButton.setEnabled(false);
         authRequestButton.setOnClickListener(authRequestListener);
 
         // connect to the service
         Intent i = new Intent(ACTION_WAVE_SERVICE);
         if (bindService(i, mConnection, Context.BIND_AUTO_CREATE)) {
             mBound = true;
             Toast.makeText(WaveClientSample.this, "Connected to WaveService", Toast.LENGTH_SHORT).show();
         } else {
             Log.d(getClass().getSimpleName(), "Could not bind with "+i);
             // TODO: replace this Toast with a dialog that allows quitting
             Toast.makeText(WaveClientSample.this, "Could not connect to the WaveService!", Toast.LENGTH_SHORT).show();
             messageTextView.setText("ERROR:\n\nFailed to bind to the WaveService.\n\nIs AndroidWave installed on this device?\n\nPlease address this issue and restart this Application.");
         }
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         
         try {
            mWaveService.unregisterRecipeOutputListener(API_KEY, RECIPE_ID);
         } catch (RemoteException e) {
             Log.d("WaveClientSample", "lost connection to the service");
         }
 
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
     }
     
     private void setButtonForWaveUi() {
         authRequestButton.setText("Deauthorize in Wave UI");
         authRequestButton.setOnClickListener(waveUiRequestListener);
         authRequestButton.setEnabled(true);
     }
     
     private void beginStreamingRecipeData() {
         try {
            boolean didRegister = mWaveService.registerRecipeOutputListener(API_KEY, RECIPE_ID, outputListener);
            if (!didRegister) {
                Toast.makeText(WaveClientSample.this, "Error requesting recipe data stream.", Toast.LENGTH_SHORT).show();
            }
         } catch (RemoteException e) {
             Log.d("WaveClientSample", "lost connection to the service");
         }
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE_AUTH) {
             if (resultCode == RESULT_OK) {
                 if (data.getAction().equals(ACTION_DID_AUTHORIZE)) {
                     Toast.makeText(WaveClientSample.this, "Authorization Successful!", Toast.LENGTH_SHORT).show();
                     
                     setButtonForWaveUi();
                     beginStreamingRecipeData();
                 } else {
                     Toast.makeText(WaveClientSample.this, "Authorization Denied!", Toast.LENGTH_SHORT).show();
                 }
             } else {
                 Toast.makeText(WaveClientSample.this, "Authorization process canceled.", Toast.LENGTH_SHORT).show();
             }
         }
     }
     
     private OnClickListener authRequestListener = new OnClickListener() {
         public void onClick(View v) {
             try {
                 // get an auth intent from the service
                 Intent i = mWaveService.getAuthorizationIntent(RECIPE_ID, API_KEY);
             
                 // then run it looking for a result
                 try {
                     startActivityForResult(i, REQUEST_CODE_AUTH);
                 } catch (ActivityNotFoundException anfe) {
                     anfe.printStackTrace();
                     Toast.makeText(WaveClientSample.this, "Error launching authorization UI", Toast.LENGTH_SHORT).show();
                 }
             } catch (RemoteException e) {
                 Log.d("WaveClientSample", "lost connection to the service");
             }
         }
     };
     
     private OnClickListener waveUiRequestListener = new OnClickListener() {
         public void onClick(View v) {
             // TODO: make this call up the specific authorization via ACTION_EDIT + appropriate extras
             // set up an intent to switch to the Wave UI
             Intent i = new Intent(Intent.ACTION_MAIN);
             i.setPackage("edu.berkeley.androidwave");
             try {
                 startActivity(i);
             } catch (ActivityNotFoundException anfe) {
                 anfe.printStackTrace();
                 Toast.makeText(WaveClientSample.this, "Error launching Wave UI", Toast.LENGTH_SHORT).show();
             }
         }
     };
     
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             mWaveService = IWaveServicePublic.Stub.asInterface(service);
             
             // check if we are authorized for the recipe and update the UI
             //  - if we are already authorized, let the user switch to the WaveUI
             //    to deauthorize
             //  - if we are not authorized, let the user request it
             try {
                 // enable the button now that the service is connected
                 authRequestButton.setEnabled(true);
                 
                 if (mWaveService.isAuthorized(API_KEY, RECIPE_ID)) {
                     Toast.makeText(WaveClientSample.this, "Already authorized for Recipe\n"+RECIPE_ID, Toast.LENGTH_SHORT).show();
                     
                     // reconfigure the UI after auth
                     setButtonForWaveUi();
                     // we should request that data be streamed and start displaying it in the log
                     beginStreamingRecipeData();
                 }
             } catch (RemoteException re) {
                 Log.d(TAG, "lost connection to the service", re);
             }
         }
         
         public void onServiceDisconnected(ComponentName className) {
             mWaveService = null;
         }
     };
     
     private IWaveRecipeOutputDataListener outputListener = new IWaveRecipeOutputDataListener.Stub() {
         public void receiveWaveRecipeOutputData(WaveRecipeOutputDataImpl wrOutput) {
             // update the log text
            Toast.makeText(WaveClientSample.this, "Got "+wrOutput, Toast.LENGTH_LONG).show();
         }
     };
     
     public boolean isBound() {
         return (mBound && (mWaveService != null));
     }
 }
