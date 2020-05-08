 package com.suma.lockAdmin;
 
 import android.app.Activity;
 import android.app.admin.DevicePolicyManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 
 public class SettingActivity extends PreferenceActivity {
 	static final int RESULT_ENABLE = 1;
 
 	private static final String TAG = "LockAdmin";
 
     private DevicePolicyManager mDevicePolicyManager;
     private ComponentName mDeviceAdmin;
 	private boolean mAdminActivated = false;
 
 	private CheckBoxPreference mDelayEnableCheckbox;
 	private ListPreference mDelayList;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Prepare to work with the DevicePolicyManager
         mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
         mDeviceAdmin = new ComponentName(this, AdminReceiver.class);
 
         // Activate Device Administrator
         Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
         intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
         startActivityForResult(intent, RESULT_ENABLE);
 
 		// Initialize activity
 		addPreferencesFromResource(R.xml.preference);
 
 		// Enable checkbox
 		mDelayEnableCheckbox = (CheckBoxPreference)findPreference("delay_enable");
 		mDelayEnableCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object o) {
 				return setDelayEnabled(((Boolean)o).booleanValue());
 			}
 		});
 
 		// Delay listbox
 		mDelayList = (ListPreference)findPreference("delay_time");
 		mDelayList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object o) {
 				setTimeoutDelayFromList();
 
 				String entry = (String) mDelayList.getEntries()[mDelayList.findIndexOfValue((String)o)];
 				String summary = getString(R.string.pref_delay_time_summary, entry);
 				mDelayList.setSummary(summary);
 				return true;
 			}
 		});
 
 		updateDelaySummary();
     }
 
 	private void updateDelaySummary()
 	{
 		String summary = getString(R.string.pref_delay_time_summary, (String)mDelayList.getEntry());
 		mDelayList.setSummary(summary);
 	}
 
 	boolean setDelayEnabled(boolean enable)
 	{
 		if (enable) {
 			return setTimeoutDelayFromList();
 		} else if (mAdminActivated) {
 			Log.d(TAG, "Lock delay disabled");
 			mDevicePolicyManager.setMaximumTimeToLock(mDeviceAdmin, 10);	// 0 means no restriction(infinite)
 			return true;
 		}
 
 		return false;
 	}
 
 	private boolean setTimeoutDelayFromList()
 	{
 		if (mAdminActivated) {
 			Log.d(TAG, "Lock delay enabled");
 			int value = Integer.valueOf(mDelayList.getValue());
 			mDevicePolicyManager.setMaximumTimeToLock(mDeviceAdmin, value);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == RESULT_ENABLE) {
 			if (resultCode == Activity.RESULT_OK && mDevicePolicyManager.isAdminActive(mDeviceAdmin)) {
 				mAdminActivated = true;
				Log.d(TAG, "Successed: Administration activation");
 			} else {
				Log.d(TAG, "FAILED: Administration activation");
 				mDelayEnableCheckbox.setChecked(false);
 			}
 			return;
 		}
 
         super.onActivityResult(requestCode, resultCode, data);
     }
 
 }
