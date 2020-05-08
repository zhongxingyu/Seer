 package com.example.proauth;
 
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 
 public class SetPreferencesActivity extends PreferenceActivity {
 
 	CheckBoxPreference monitor;
 	CheckBoxPreference app_timeout;
 	CheckBoxPreference system_timeout;
 	Editor e;
 	public static String TAG = "SetPreferencesActivity";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
         doBindService();
 
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		e = sp.edit();
         
         PreferenceManager preferenceManager = getPreferenceManager();  
         monitor = 
                  (CheckBoxPreference) preferenceManager.findPreference("monitor_on"); 
 
         Log.d(TAG, "The monitor button: " + monitor.toString());
         monitor.setOnPreferenceClickListener(new OnPreferenceClickListener(){
         	
 			@Override
 			public boolean onPreferenceClick(Preference arg0) {
 				// TODO Auto-generated method stub
 				if(monitor.isChecked()){
 					Log.i("PREFERENCES", "Starting monitoring service");
 					Intent intent = new Intent();
 			    	intent.setClass(SetPreferencesActivity.this, MonitorService.class);
 			    	startService(intent);
 			    	system_timeout.setEnabled(true);
 				}
 				else{
 					Log.i("PREFERENCES", "Stopping monitoring service");
 					Intent intent = new Intent();
 			    	intent.setClass(SetPreferencesActivity.this, MonitorService.class);
 			    	stopService(intent);
 //			    	system_timeout.setEnabled(false);
 //			    	app_timeout.setEnabled(false);
 //			    	system_timeout.setChecked(false);
 //			    	app_timeout.setChecked(false);
 				}
 				return true;
 			}
         	
         });
         
 
         app_timeout = (CheckBoxPreference) preferenceManager.findPreference("trigger_0"); 
         system_timeout = (CheckBoxPreference) preferenceManager.findPreference("trigger_1"); 
         
         
         app_timeout.setEnabled(app_timeout.isChecked());
         if(!app_timeout.isEnabled()){
         	system_timeout.setEnabled(true);
         }
         
         if(!monitor.isChecked()){
         	app_timeout.setChecked(false);
         	system_timeout.setChecked(false);
         }
         
         
         app_timeout.setOnPreferenceClickListener(new OnPreferenceClickListener(){
 			@Override
 			public boolean onPreferenceClick(Preference arg0) {
 				if(app_timeout.isChecked()){
 					e.putBoolean("trigger_1", false);
 					e.commit();
 					system_timeout.setChecked(false);
 					system_timeout.setEnabled(false);
 				} else {
 					system_timeout.setEnabled(true);
 					
 				}
 				return true;
 			}
         	
         });
         
         system_timeout.setOnPreferenceClickListener(new OnPreferenceClickListener(){
 			@Override
 			public boolean onPreferenceClick(Preference arg0) {
 				if(system_timeout.isChecked()){
 					e.putBoolean("trigger_0", false);
 					e.commit();
 					app_timeout.setChecked(false);
 					app_timeout.setEnabled(false);
 					
 					if(mIsBound){
 						monitorService.registerHandlerScreenListeners();
 					}
 					else{
 						Log.d("JOAO", "mIsBound is false");
 					}
 					
 				} else{
 					app_timeout.setEnabled(true);
 					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 					Editor e = sp.edit();
 					e.putString(MainActivity.PHONE_SECURITY_STATE, SecurityLevel.PUBLIC.toString());
 					e.commit();
 				}
 				return true;
 			}
         	
         });
         
 
         
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		Intent intent = new Intent("com.example.proauth.FINISH_ACTIVITY");
     	sendBroadcast(intent);
 		finish();
 	}
 	
 	/*
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 	*/
 	
 	private MonitorService monitorService;
 	private boolean mIsBound;
 	
 	private ServiceConnection mServiceConnection = new ServiceConnection(){
 		@Override
 	    public void onServiceConnected(ComponentName className, IBinder service) {
 			Log.d("JOAO", "BOUND to the service");
 	        monitorService = ((MonitorService.LocalBinder)service).getService();
 	    }
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			 Log.d("JOAO", "Disconnected to the service");
 			 monitorService = null;
 			
 		}
 	    
 	};
 	
 	void doBindService() {
 		Log.d("JOAO", "do bind service was called");
 	    boolean a = getApplicationContext().bindService(new Intent(getApplicationContext(), 
 	            MonitorService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
 	    Log.d("JOAO", "bind to service: " + a);
 	    mIsBound = true;
 	}
 	
 	void doUnbindService() {
 	    if (mIsBound) {
 	        // Detach our existing connection.
 	    	getApplicationContext().unbindService(mServiceConnection);
 	        mIsBound = false;
 	    }
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		doUnbindService();
 	}
 }
