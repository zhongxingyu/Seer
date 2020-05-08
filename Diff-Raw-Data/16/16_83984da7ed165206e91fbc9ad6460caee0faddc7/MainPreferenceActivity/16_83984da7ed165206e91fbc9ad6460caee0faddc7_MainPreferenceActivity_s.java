 package com.androidhuman.putmedown.activity;
 
 import android.annotation.TargetApi;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.nfc.NfcAdapter;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.view.Menu;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Switch;
 import android.widget.Toast;
 
 import com.androidhuman.putmedown.R;
 import com.androidhuman.putmedown.service.IProtectionService;
 import com.androidhuman.putmedown.service.ProtectionService;
 import com.androidhuman.putmedown.util.Util;
 
 public class MainPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
 	
 	private IProtectionService mService;
 	private ServiceConnection conn = new ServiceConnection(){
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			mService = IProtectionService.Stub.asInterface(service);
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			mService = null;
 		}
 		
 	};
 
 	/** Called when the activity is first created. */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    startService(new Intent(this, ProtectionService.class));
 	    
 	    this.addPreferencesFromResource(R.xml.general);
 	    
 	    SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
 	    
 	    Preference detailSettingPref = getPreferenceScreen().getPreference(Build.VERSION.SDK_INT>=14 ? 1 : 2);
 	    detailSettingPref.setSummary(
 	    		defaultPref.getString("unlock_method", "pin").equals("pin") ? 
 	    				R.string.set_pin_to_unlock : R.string.set_nfc_tag_to_unlock);
 	    
 	    Preference unlockMethodPref = getPreferenceScreen().getPreference(Build.VERSION.SDK_INT>=14 ? 0 : 1);
 	    unlockMethodPref.setSummary(
 	    		defaultPref.getString("unlock_method", "pin").equals("pin") ? 
 	    				R.string.pin : R.string.nfc);
 	    
 	    // Register listener for preference change
 
 	    
 	}
 	
 	private BroadcastReceiver lockReceiver = new BroadcastReceiver(){
 
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			finish();
 		}
 		
 	};
 	
 	private boolean isReceiverRegistered = false;
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if(isReceiverRegistered){
 			unregisterReceiver(lockReceiver);
 			isReceiverRegistered =false;
 		}
 		if(mService!=null)
 			unbindService(conn);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ProtectionService.ACTION_ALARM_FIRED);
 		registerReceiver(lockReceiver, filter);
 		isReceiverRegistered = true;
 		bindService(new Intent(MainPreferenceActivity.this, ProtectionService.class), conn, Context.BIND_AUTO_CREATE);
 		getPreferenceScreen().getSharedPreferences()
     	.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		
 		getMenuInflater().inflate(R.menu.general, menu);
 		
 		// Get an instance of switch on the actionview
 		if(Build.VERSION.SDK_INT >= 14){
 			Switch enableSwitch = (Switch)menu.getItem(0).getActionView();
 			enableSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("enabled", false));
 			enableSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 	
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
 					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
 					if(isChecked){
 						editor.putBoolean("enabled", true);
 					}else{
 						editor.putBoolean("enabled", false);
 					}
 					editor.commit();
 					
 				}
 				
 			});
 			return true;
 		}
 		// On Honeycomb or lower, switch is not supported. so we provide
 		// enable/disable switch on the preferences list, rather than on action bar.
 		return false;
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if(key.equals("enabled")){
 			boolean enabled = sharedPreferences.getBoolean("enabled", false);
 			Toast.makeText(getApplicationContext(), enabled ? "Protection mode enabled" : "Protection mode disabled", Toast.LENGTH_SHORT).show();
 			if(enabled){
 				bindService(new Intent(MainPreferenceActivity.this, ProtectionService.class), conn, Context.BIND_AUTO_CREATE);
 				if(mService!=null){
 					try{
 						mService.enableService();
 					}catch(RemoteException e){
 						e.printStackTrace();
 					}
 				}
 			}else{
 				if(mService!=null){
 					try {
 						mService.disableService();
 						
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 				//stopService(new Intent(MainPreferenceActivity.this, ProtectionService.class));
 				finish();
 			}
 		}else if(key.equals("unlock_method")){
 			String value = sharedPreferences.getString("unlock_method", "pin");
 			
 			Preference unlockMethodPref = getPreferenceScreen().getPreference(Build.VERSION.SDK_INT>=14 ? 0 : 1);
 			Preference detailSettingPref = getPreferenceScreen().getPreference(Build.VERSION.SDK_INT>=14 ? 1 : 2);
 		
 			if(value.equals("nfc")){
 				// If NFC hardware is not available, notify this to user and
 				// Force unlock method set to PIN
 				NfcAdapter adapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
 				
 				if(adapter==null){
 					Toast.makeText(getApplicationContext(), R.string.nfc_is_not_supported, Toast.LENGTH_SHORT).show();
 					sharedPreferences.edit().putString("unlock_method", "pin").commit();
 					
 					unlockMethodPref.setSummary(R.string.pin);
 					detailSettingPref.setSummary(R.string.set_pin_to_unlock);
 				}else{
 					// If device's NFC feature is turned off
 					if(!adapter.isEnabled()){
 						Toast.makeText(getApplicationContext(), R.string.nfc_is_not_enabled, Toast.LENGTH_SHORT).show();
 						sharedPreferences.edit().putString("unlock_method", "pin").commit();
 						unlockMethodPref.setSummary(R.string.pin);
 						detailSettingPref.setSummary(R.string.set_pin_to_unlock);
 						
 						// On IceCreamSandwich or higher, show NFC settings activity
 						// directly
 						if(Build.VERSION.SDK_INT >= 14){
 							startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
 						}else{
 							startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
 						}
 						
 					}
 					// NFC is available
					sharedPreferences.edit().putString("unlock_method", "nfc");
 					unlockMethodPref.setSummary(R.string.nfc);
 					detailSettingPref.setSummary(R.string.set_nfc_tag_to_unlock);
 				}
 			}else{
 				unlockMethodPref.setSummary(R.string.pin);
 				detailSettingPref.setSummary(R.string.set_pin_to_unlock);
 			}
 		}
 	}
 
 }
