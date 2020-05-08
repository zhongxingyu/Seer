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
 
 import android.app.Activity;
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
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 import com.konreu.android.wagz.R;
 import com.konreu.android.wagz.StepService;
 
 public class Wagz extends Activity {
 	private static String TAG = "Wagz";	
         
     private StepService mService;
     
     static final int DIALOG_ABOUT = 1;
 
     private boolean isRunning() {
     	Log.v(TAG + ".isRunning", "StepService.isRunning = " + StepService.isRunning());
 		return StepService.isRunning();
     }
         
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
                 
         setContentView(R.layout.main);
         
         SeekBar sb = (SeekBar) findViewById(R.id.happiness_bar);
         sb.setEnabled(false);
        sb.setFocusable(false);
         
     	Button btnStartWalk = (Button) findViewById(R.id.btn_start_walk);
     	btnStartWalk.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startWalk();
             }
         });
     	
     	Button btnStopWalk = (Button) findViewById(R.id.btn_stop_walk);
     	btnStopWalk.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 stopWalk();
             }
         });
     	
     	setButtonStartWalk();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         
         if (this.isRunning()) {
         	bindStepService();
         	
         	// If already running, want to show "Stop" button
         	setButtonStopWalk();
         } else {
         	// If not yet running, show "Start" button
         	setButtonStartWalk();
         }
     }
     
     /***
      * Show Start button, hide Stop button
      */
     protected void setButtonStartWalk() {
     	((Button) findViewById(R.id.btn_start_walk)).setVisibility(View.VISIBLE);
     	((Button) findViewById(R.id.btn_stop_walk)).setVisibility(View.GONE);
     }
     
     /***
      * Hide Start button, show Stop button
      */
     protected void setButtonStopWalk() {
     	((Button) findViewById(R.id.btn_start_walk)).setVisibility(View.GONE);
     	((Button) findViewById(R.id.btn_stop_walk)).setVisibility(View.VISIBLE);
     }
     
     /***
      * Bind to StepService and show "Stop" button
      */
     protected void startWalk() {
     	startStepService();
         bindStepService();
         setButtonStopWalk();
     }
     
     /***
      * Unbind from StepService and show "Start" button
      */
     protected void stopWalk() {
         unbindStepService();
         stopStepService();
         setButtonStartWalk();
     }
         
     @Override
     protected void onPause() {
         if (this.isRunning()) {
             unbindStepService();
         }
         super.onPause();
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
             SharedPreferences state = getSharedPreferences(StepService.STATE_KEY, 0);
             SharedPreferences.Editor stateEditor = state.edit();
             if (updateDisplay) {
                 stateEditor.putFloat(StepService.STATE_DISTANCE, 0);
                 stateEditor.putLong(StepService.STATE_ELAPSED_TIME, 0);
                 stateEditor.commit();
             }
         }
     }
 
     private static final int MENU_SETTINGS = 8;
     private static final int MENU_QUIT = 9;
     private static final int MENU_PAUSE = 1;
     private static final int MENU_RESUME = 2;
     private static final int MENU_DETAILS = 3;
     private static final int MENU_ABOUT = 4;
     
     /* Creates the menu items */
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         if (this.isRunning()) {
             menu.add(0, MENU_PAUSE, 0, R.string.pause)
             .setIcon(android.R.drawable.ic_media_pause)
             .setShortcut('1', 'p');
         } else {
             menu.add(0, MENU_RESUME, 0, R.string.resume)
             .setIcon(android.R.drawable.ic_media_play)
             .setShortcut('1', 'p');
         }
         menu.add(0, MENU_DETAILS, 0, R.string.menu_details)
 	        .setIcon(android.R.drawable.ic_menu_info_details)
 	        .setShortcut('2', 'd')
         	.setIntent(new Intent(this, Detailz.class));
         menu.add(0, MENU_SETTINGS, 0, R.string.settings)
 	        .setIcon(android.R.drawable.ic_menu_preferences)
 	        .setShortcut('7', 's')
 	        .setIntent(new Intent(this, Settingz.class));
         menu.add(0, MENU_ABOUT, 0, R.string.menu_about)
         	.setIcon(android.R.drawable.ic_menu_help)
         	.setShortcut('8', 'a');
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
             case MENU_QUIT:
             	// Alert the user that they are about to lose their values
                 new AlertDialog.Builder(this)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setTitle(R.string.really_reset_values)
                 .setMessage(R.string.warning_reset_values)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         resetValues(false);
                         stopStepService();
                         finish();
                     }
                 })
                 .setNegativeButton(R.string.cancel, null)
                 .show();
                 return true;
             case MENU_ABOUT:
             	showDialog(DIALOG_ABOUT);
             	return true;
         }
         return false;
     }  
     
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
