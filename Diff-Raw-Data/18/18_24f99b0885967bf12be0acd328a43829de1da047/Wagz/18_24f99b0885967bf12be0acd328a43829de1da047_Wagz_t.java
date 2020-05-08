 /*
  *  Wagz - Android App
  *  Copyright (C) 2010 Konreu (Conroy Whitney)
  *  Based on the Pedometer Android App by Levente Bagi (http://code.google.com/p/pedometer/)
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.konreu.android.wagz.activities;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 import com.github.droidfu.activities.BetterDefaultActivity;
 import com.konreu.android.wagz.AppState;
 import com.konreu.android.wagz.Dog;
 import com.konreu.android.wagz.PedometerSettings;
 import com.konreu.android.wagz.R;
 import com.konreu.android.wagz.StepService;
 
 public class Wagz extends BetterDefaultActivity {
 	private static String TAG = "Wagz";	
 		
     private StepService mService;
         
     private SeekBar mHappinessBar;
     private TextView mDogNameView;    
     private ImageView mLoyaltyRating;
     
     private Dog mDog;
     
     static final int DIALOG_ABOUT = 1;
 
     private boolean isRunning() {
     	Log.v(TAG + ".isRunning", "StepService.isRunning = " + StepService.isRunning());
 		return StepService.isRunning();
     }
             
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
                 
 //        String sTAG = TAG + "onCreate";
                 
         setContentView(R.layout.main);
           
         /* Initialize our Views */
         
         mHappinessBar = (SeekBar) findViewById(R.id.happiness_bar);
         mHappinessBar.setEnabled(false);
         mHappinessBar.setFocusable(false);
     	
     	mLoyaltyRating = (ImageView) findViewById(R.id.loyalty_rating);
     	
     	mDogNameView = (TextView) findViewById(R.id.dog_name);
     }
         
     @Override
     protected void onResume() {
         super.onResume();
         
         mDog = Dog.getInstance(this);        
         
         if (this.isRunning()) {
         	bindStepService();
         }
         
     	if (this.isLaunching()) {
     		// See if we should be updating happiness and loyalty
     		if (mDog.lostHappinessOnStartup()) {
     			// No big deal
     		}
     		if (mDog.lostLoyaltyOnStartup()) {
     			// Let them know that they waited too long
     			new AlertDialog.Builder(this)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setTitle(R.string.alert_lost_loyalty_title)
                 .setMessage(R.string.alert_lost_loyalty_explanation)
                 .setNegativeButton(R.string.ok, null)
                 .show();
     		}
     	}
         
         // Do this the very last thing ...
         updateUI();
     }
         
     private void updateUI() {   	
     	int iHappiness = mDog.getHappiness();
     	mHappinessBar.setProgress(iHappiness);
     	
     	int iLoyalty = mDog.getLoyalty();
     	
     	if (iLoyalty == 1) { mLoyaltyRating.setImageResource(R.drawable.heart_1of7); }
     	else if (iLoyalty == 2) { mLoyaltyRating.setImageResource(R.drawable.heart_2of7); }
     	else if (iLoyalty == 3) { mLoyaltyRating.setImageResource(R.drawable.heart_3of7); }
     	else if (iLoyalty == 4) { mLoyaltyRating.setImageResource(R.drawable.heart_4of7); }
     	else if (iLoyalty == 5) { mLoyaltyRating.setImageResource(R.drawable.heart_5of7); }
     	else if (iLoyalty == 6) { mLoyaltyRating.setImageResource(R.drawable.heart_6of7); }
     	else if (iLoyalty == 7) { mLoyaltyRating.setImageResource(R.drawable.heart_7of7); }
     	else { mLoyaltyRating.setImageResource(R.drawable.heart_3of7); }  // default
     	
     	mDogNameView.setText(PedometerSettings.getInstance(this).getDogName());
     	mDogNameView.setGravity(Gravity.CENTER_HORIZONTAL);
     }
     
     /***
      * Bind to StepService and show "Stop" button
      */
     protected void startWalk() {
     	startStepService();
         bindStepService();
     }
     
     /***
      * Unbind from StepService and show "Start" button
      */
     protected void stopWalk() {
         unbindStepService();
         stopStepService();
     }
         
     @Override
     protected void onPause() {
         if (this.isRunning()) {
             unbindStepService();
         }
         super.onPause();
     }
     
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         resetValues(true);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
     }
 
     protected void onDestroy() {
         super.onDestroy();
     }
     
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
         	mService = ((StepService.StepBinder) service).getService();
         	mService.registerCallback(mCallback);
         	mService.reloadSettings();
         }
 
         public void onServiceDisconnected(ComponentName className) {
             mService = null;
         }
     };
     
     private void startStepService() {
         startService(new Intent(Wagz.this, StepService.class));
     }
     
     private void bindStepService() {
         bindService(new Intent(Wagz.this, StepService.class), 
         		mConnection, Context.BIND_AUTO_CREATE);
     }
 
     private void unbindStepService() {
         unbindService(mConnection);
     }
     
     private void stopStepService() {
         if (mService != null) {
             stopService(new Intent(Wagz.this,
                   StepService.class));
         }
     }
     
     private void resetValues(boolean updateDisplay) {
         if (this.isRunning()) {
             mService.resetValues();
         } else {
         	AppState.getInstance(this).clear();
         }
         
         Dog.resetInstance(this);
        mDog = Dog.getInstance(this);
     }
     
     private static final int MENU_PAUSE = 0;
     private static final int MENU_RESUME = 1;
     private static final int MENU_SETTINGS = 2;
     private static final int MENU_DETAILS = 3;
     
     private static final int MENU_ABOUT = 7;
     private static final int MENU_RESET = 8;
     private static final int MENU_QUIT = 9;
     
     /* Creates the menu items */
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         if (this.isRunning()) {
             menu.add(0, MENU_PAUSE, 0, R.string.pause)
             .setIcon(android.R.drawable.ic_media_pause)
             .setShortcut('1', 'w');
         } else {
             menu.add(0, MENU_RESUME, 0, R.string.resume)
             .setIcon(android.R.drawable.ic_media_play)
             .setShortcut('1', 'w');
         }
 	    menu.add(0, MENU_SETTINGS, 0, R.string.settings)
 	        .setIcon(android.R.drawable.ic_menu_preferences)
 	        .setShortcut('2', 's')
 	        .setIntent(new Intent(this, Settingz.class));        
         menu.add(0, MENU_DETAILS, 0, R.string.menu_details)
 	        .setIcon(android.R.drawable.ic_menu_info_details)
 	        .setShortcut('3', 'd')
         	.setIntent(new Intent(this, Detailz.class));
         
         // Bottom row
         menu.add(0, MENU_ABOUT, 0, R.string.menu_about)
 	    	.setIcon(android.R.drawable.ic_menu_help)
 	    	.setShortcut('7', 'a');        
         menu.add(0, MENU_RESET, 0, R.string.reset)
 	        .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
 	        .setShortcut('8', 'r');                
 	    menu.add(0, MENU_QUIT, 0, R.string.quit)
 	        .setIcon(android.R.drawable.ic_lock_power_off)
 	        .setShortcut('9', 'q');
         
         return true;
     }
 
     /* Handles item selections */
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_PAUSE:
                 stopWalk();
                 return true;
             case MENU_RESUME:
                 startWalk();
                 return true;
             case MENU_RESET:
             	// Alert the user that they are about to lose their values
                 new AlertDialog.Builder(this)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setTitle(R.string.really_reset_values)
                 .setMessage(R.string.warning_reset_values)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                     	resetValues(true);
                     }
                 })
                 .setNegativeButton(R.string.cancel, null)
                 .show();
 
                 return true;                 
             case MENU_QUIT:
                 stopStepService();
                 finish();
                 return true;
             case MENU_ABOUT:
             	showDialog(DIALOG_ABOUT);
             	return true;
         }
         return false;
     }  
     
     // TODO: unite all into 1 type of message
     private StepService.ICallback mCallback = new StepService.ICallback() {
         public void distanceChanged(float value) {
         	// do nothing
         }
         public void elapsedTimeChanged(long value) {
         	Log.v(TAG, "timeChanged: " + Long.toString(value));
             mHandler.sendMessage(mHandler.obtainMessage(ELAPSED_TIME_MSG, value));
         }        
     };
     
     private static final int ELAPSED_TIME_MSG = 1;
     
     private Handler mHandler = new Handler() {
         @Override public void handleMessage(Message msg) {
             switch (msg.what) {
                 case ELAPSED_TIME_MSG:
                 	mDog.updateHappiness((Long)msg.obj);
                 	updateUI();
                 default:
                     super.handleMessage(msg);
             }
         }
     };  
        
     protected Dialog onCreateDialog(int id) {
     	Dialog dialog;
     	switch(id) {
     		case DIALOG_ABOUT:
     			dialog = new Dialog(this);
     	    	dialog.setContentView(R.layout.about);
     	    	dialog.setTitle("About " + getString(R.string.app_name));
     	    	
     	    	// Set the application version
     	    	// HOLY SHIT THIS IS ALOT OF WORK FOR ONE NUMBER
     	    	PackageManager pm = getPackageManager();
     	    	PackageInfo pi = null;
     	        try {
     	        	pi = pm.getPackageInfo("com.konreu.android.wagz", 0);
     	        } catch (NameNotFoundException nnfe) {
     	        	pi = null;
     	        	Log.e(TAG, "error getting package info: " + nnfe.getMessage());
     	        }
     	        TextView text = (TextView) dialog.findViewById(R.id.app_version);
     	        if (pi != null) {
     	        	text.setText(pi.versionName);
     	        } else {
     	        	text.setVisibility(View.GONE);
     	        }
     	    	
     	    	break;
 	        default:
 	            dialog = null;
         }
         return dialog;
     }    
 }
