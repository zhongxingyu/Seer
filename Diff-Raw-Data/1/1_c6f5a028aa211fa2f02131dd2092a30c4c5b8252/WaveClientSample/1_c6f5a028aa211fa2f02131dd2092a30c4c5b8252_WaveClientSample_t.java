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
 
 public class WaveClientSample extends Activity
 {
     private static final String ACTION_WAVE_SERVICE = "edu.berkeley.androidwave.intent.action.WAVE_SERVICE";
     private static final String ACTION_DID_AUTHORIZE = "edu.berkeley.androidwave.intent.action.DID_AUTHORIZE";
     private static final String ACTION_DID_DENY = "edu.berkeley.androidwave.intent.action.DID_DENY";
     private static final int REQUEST_CODE_AUTH = 1;
     private final String RECIPE_ID = "edu.berkeley.waverecipe.AccelerometerMagnitude";
     
     private IWaveServicePublic mWaveService;
     private boolean mBound;
     
     Button authRequestButton;
     TextView messageTextView;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         authRequestButton = (Button) findViewById(R.id.auth_request_button);
         messageTextView = (TextView) findViewById(R.id.message_textview);
         
         authRequestButton.setEnabled(false);
 
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
             mWaveService.registerRecipeOutputListener(outputListener, true);
         } catch (RemoteException e) {
             Log.d("WaveClientSample", "lost connection to the service");
         }
 
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
     }
     
     private void afterBind() {
         // check if we are authorized for the recipe and update the UI
         //  - if we are already authorized, let the user switch to the WaveUI
         //    to deauthorize
         //  - if we are not authorized, let the user request it
 
         try {
             if (mWaveService.isAuthorized(RECIPE_ID)) {
                 Toast.makeText(WaveClientSample.this, "Already authorized for Recipe "+RECIPE_ID, Toast.LENGTH_SHORT).show();
             
                 // we should configure the button to take us to the Wave UI
                 authRequestButton.setOnClickListener(waveUiRequestListener);
                 authRequestButton.setEnabled(true);
             
                 // we should request that data be streamed and start displaying it in the log
                 beginStreamingRecipeData();
             } else {
                 if (mWaveService.recipeExists(RECIPE_ID, false)) {
                     authRequestButton.setOnClickListener(authRequestListener);
                     authRequestButton.setEnabled(true);
                 } else {
                     // TODO: replace this Toast with a dialog that allows quitting
                     Toast.makeText(WaveClientSample.this, "WaveService can't find Recipe\n"+RECIPE_ID, Toast.LENGTH_SHORT).show();
                     messageTextView.setText("ERROR:\n\nThe WaveService cannot locate Recipe "+RECIPE_ID+"\n\nIs that ID correct, and is the recipe server reachable?\n\nPlease address this issue and restart this Application.");
                 }
             }
         } catch (RemoteException e) {
             Log.d("WaveClientSample", "lost connection to the service");
         }
     }
     
     private void beginStreamingRecipeData() {
         try {
             mWaveService.registerRecipeOutputListener(outputListener, true);
         } catch (RemoteException e) {
             Log.d("WaveClientSample", "lost connection to the service");
         }
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE_AUTH) {
             if (resultCode == RESULT_OK) {
                 if (data.getAction().equals(ACTION_DID_AUTHORIZE)) {
                     Toast.makeText(WaveClientSample.this, "Authorization Successful!", Toast.LENGTH_SHORT).show();
                 
                     // reassign the auth button
                    authRequestButton.setText("Deauthorize in Wave UI");
                     authRequestButton.setOnClickListener(waveUiRequestListener);
                     authRequestButton.setEnabled(true);
                 
                     beginStreamingRecipeData();
                 } else {
                     Toast.makeText(WaveClientSample.this, "Authorization Denied!", Toast.LENGTH_SHORT).show();
                 }
             } else {
                 Toast.makeText(WaveClientSample.this, "Authorization process failed unexpectedly.", Toast.LENGTH_SHORT).show();
             }
         }
     }
     
     private OnClickListener authRequestListener = new OnClickListener() {
         public void onClick(View v) {
             try {
                 // get an auth intent from the service
                 Intent i = mWaveService.getAuthorizationIntent(RECIPE_ID);
             
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
             // set up an intent to switch to the Wave UI
             Intent i = new Intent(Intent.ACTION_MAIN);
             i.setClassName("edu.berkeley.androidwave", "edu.berkeley.androidwave.waveui.AndroidWaveActivity");
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
             afterBind();
         }
         
         public void onServiceDisconnected(ComponentName className) {
             mWaveService = null;
         }
     };
     
     private IWaveRecipeOutputDataListener outputListener = new IWaveRecipeOutputDataListener.Stub() {
         public void receiveWaveRecipeOutputData(WaveRecipeOutputDataImpl wrOutput) {
             // update the log text
             Toast.makeText(WaveClientSample.this, "NOT IMPLEMENTED YET!", Toast.LENGTH_LONG).show();
         }
     };
     
     public boolean isBound() {
         return (mBound && (mWaveService != null));
     }
 }
