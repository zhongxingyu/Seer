 /*
  * Copyright 2010-2012  Vasily Romanikhin  bac1ca89@gmail.com
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * 3. The name of the author may not be used to endorse or promote
  *    products derived from this software without specific prior written
  *    permission.
  *
  * The advertising clause requiring mention in adverts must never be included.
  */
 
 /*! ---------------------------------------------------------------
  * PROJ: OSLL/geo2tag
  * ---------------------------------------------------------------- */
 
 package org.geo2tag.tracker;
 
 import org.geo2tag.tracker.exception.ExceptionHandler;
 import org.geo2tag.tracker.preferences.Settings;
 import org.geo2tag.tracker.preferences.SettingsActivity;
 import org.geo2tag.tracker.preferences.Settings.ITrackerAppSettings;
 import org.geo2tag.tracker.services.RequestService;
 import org.geo2tag.tracker.utils.TrackerUtil;
 
 import org.geo2tag.tracker.R; 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TrackerActivity extends Activity {
 	public static String LOG = "Tracker";
 	
 	private TextView mLogView;
 	private TextView mStatusView;
     private BroadcastReceiver mTrackerReceiver = new TrackerReceiver();
     private BroadcastReceiver mLocationReceiver = new LocationReceiver();
     
     private Button mBtnService; 
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
 		registerReceiver(mTrackerReceiver, new IntentFilter(TrackerReceiver.ACTION_MESS));
 		registerReceiver(mLocationReceiver, new IntentFilter(LocationReceiver.ACTION_LOCATION));
 		
 		mLogView = (TextView) findViewById(R.id.TextField);
 		mStatusView = (TextView) findViewById(R.id.status_text_view);
 		
 		initialization();
 		
 		Settings settings = new Settings(this);
 		if (settings.isSettingsEmpty()){
 			settings.setDefaultSettrings();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(mTrackerReceiver);
 		unregisterReceiver(mLocationReceiver);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mLogView.setText(TrackerUtil.getLogText());
 		refreshStatusTextView();
 
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		TrackerUtil.setLogText(mLogView.getText());
 	}
 	
 	private void refreshStatusTextView(){
 
 		SharedPreferences settings = new Settings(this).getPreferences();
 		String channelName = settings.getString(Settings.ITrackerNetSettings.CHANNEL, "");
 		String statusText = "";
 		
 		statusText = "Channel: "+channelName;
 
 		
 		mStatusView.setText(statusText);
 	}
 	
 	private void refreshStatusTextView(String location){
 		refreshStatusTextView();
 		mStatusView.setText(mStatusView.getText().toString()+", "+location);
 		
 		Log.v(LOG, mStatusView.getText().toString());
 	}
 	
 	
 	private void initialization(){
 		Log.v(LOG, "TrackerActivity - initialization");
 		
 
 		refreshStatusTextView();
 		
 		mBtnService = (Button) findViewById(R.id.start_stop_button);
 		
 		
 		boolean trackerState = TrackerUtil.isServiceRunning(this, RequestService.class);
 		TrackerUtil.notify(this, trackerState);
 		
		if (trackerState) mBtnService.setText(getResources().getString(R.string.btnStop));
		
 		mBtnService.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	if (TrackerUtil.isServiceRunning(TrackerActivity.this, RequestService.class)){
             		Log.v(LOG, "Tracker is running, stopping");
             		stopTracker();
             	}else{
             		Log.v(LOG, "Tracker is stopped, running");
             		startTracker();
             	}
             }
         });
 
 		
 		final Button settingsBtn = (Button) findViewById(R.id.settings_button);
 		settingsBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 			 //   if (TrackerUtil.isServiceRunning(TrackerActivity.this, RequestService.class)) {
 				//	showToast(R.string.msg_settigns_not_available);
 				//} else {
 					startActivity(new Intent(TrackerActivity.this, SettingsActivity.class));
 				//}
 			}
 		});
 
 		final Button creenBtn = (Button) findViewById(R.id.screeen_down_button);
 		creenBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TrackerUtil.hideApplication(TrackerActivity.this); 
 			}
 		});
 	
 		
 	}
 	
 	
 	public boolean onKeyDown(int keycode, KeyEvent event) {
 	    if (keycode == KeyEvent.KEYCODE_BACK) {
 	    	stopTracker();
 			TrackerUtil.disnotify(TrackerActivity.this);
 	        moveTaskToBack(true);
 	    }
 	    return super.onKeyDown(keycode, event);
 	}
 	
 	private void startTracker(){
 		if (TrackerUtil.isServiceRunning(this, RequestService.class)){
 			showToast(R.string.msg_tracker_already_running);
 		} else if (TrackerUtil.isOnline(this)){
 			showToast(R.string.msg_tracker_start);
     		mBtnService.setText(getResources().getString(R.string.btnStop));
     		
     		TrackerUtil.disnotify(this);
     		TrackerUtil.notify(this, true);
     		
 			clearLogView();
 
 			startService(new Intent(this, RequestService.class));
 			
 			if (Settings.getPreferences(this).getBoolean(ITrackerAppSettings.IS_HIDE_APP, true)){
 				TrackerUtil.hideApplication(TrackerActivity.this);
 			}
 		} else if (!TrackerUtil.isOnline(this)){
 			showToast(R.string.msg_fail_connection);
 		}
 	}
 	
 	private void stopTracker(){
 		if (TrackerUtil.isServiceRunning(this, RequestService.class)){
 			showToast(R.string.msg_tracker_stop);
 			mBtnService.setText(getResources().getString(R.string.btnStart));
     		TrackerUtil.disnotify(this);
     		TrackerUtil.notify(this, false);
 		//	TrackerUtil.disnotify(this);
 			stopService(new Intent(this, RequestService.class));
 		}
 	}
 
 	private void showToast(final String mess){
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(TrackerActivity.this, mess, Toast.LENGTH_SHORT).show();				
 			}
 		});
 	}
 	
 	private void showToast(final int resId){
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(TrackerActivity.this, resId, Toast.LENGTH_SHORT).show();              
             }
         });
     }
 
 
 	private static int lineCount = 0;
 	private static final int maxLines = 16;
 	public void appendToLogView(final String mess){
 		if (lineCount > maxLines){
 			clearLogView();
 			lineCount = 0;
 		}
 		appendToLogViewInternal(mess);
 		lineCount++;
 	}
 	
 	private void appendToLogViewInternal(final String mess){
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				mLogView.append("\n" + mess);
 			}
 		});
 	}
 
 	private void clearLogView(){
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				mLogView.setText("");
 			}
 		});
 	}	
 	
 	public class TrackerReceiver extends BroadcastReceiver {
 		public static final String 	ACTION_MESS   = "action.mess";
 		public static final String TYPE_MESS      = "type.mess";
 		public static final String TYPE_OPEATION  = "type.operation";
 		public static final int ID_SHOW_TOAST     = 0;
 		public static final int ID_APPEND_TO_LOG  = 1;
         public static final int ID_LOG_AND_TOAST  = 2;
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			final String mess = intent.getStringExtra(TYPE_MESS);
 			final int type = intent.getIntExtra(TYPE_OPEATION, -1);
             switch (type) {
                 case ID_SHOW_TOAST:
                     showToast(mess);
                     break;
                 case ID_APPEND_TO_LOG:
                     appendToLogView(mess);
                     break;
                 case ID_LOG_AND_TOAST:
                     showToast(mess);
                     appendToLogView(mess);
                     break;
             }
 		}
 	}
 	
 	public class LocationReceiver extends BroadcastReceiver {
 		public static final String ACTION_LOCATION	 = "action.location";
 		public static final String TYPE_LOCATION = "type.location";
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 				final String location = intent.getStringExtra(TYPE_LOCATION);
 				refreshStatusTextView (location);
 					
 		}		
 				
 	}
 }
