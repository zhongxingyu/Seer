 /*
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
 
 package com.carbon.settings.fragments;
 
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 
 import com.carbon.settings.R;
 import com.carbon.settings.SettingsPreferenceFragment;
 
 public class LockscreenInterface extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
     private static final String TAG = "LockscreenInterface";
 
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
     private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
 
     private PreferenceScreen mLockscreenButtons;
     private ListPreference mBatteryStatus;
     private CheckBoxPreference mMusicControls;
 
     public boolean hasButtons() {
         return !getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.lockscreen_interface_settings);
 
         // Battery status
        mBatteryStatus = (ListPreference) findPreference(KEY_ALWAYS_BATTERY_PREF);
         if (mBatteryStatus != null) {
             int batteryStatus = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0);
             mBatteryStatus.setValueIndex(batteryStatus);
             mBatteryStatus.setSummary(mBatteryStatus.getEntries()[batteryStatus]);
             mBatteryStatus.setOnPreferenceChangeListener(this);
         }
 
         mMusicControls = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_MUSIC_CONTROLS);
         mMusicControls.setOnPreferenceChangeListener(this);
 
         mLockscreenButtons = (PreferenceScreen) findPreference(KEY_LOCKSCREEN_BUTTONS);
         if (!hasButtons()) {
             getPreferenceScreen().removePreference(mLockscreenButtons);
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         ContentResolver cr = getActivity().getContentResolver();
         if (mMusicControls != null) {
             mMusicControls.setChecked(Settings.System.getInt(cr,
                     Settings.System.LOCKSCREEN_MUSIC_CONTROLS, 1) == 1);
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object objValue) {
         if (preference == mBatteryStatus) {
             int value = Integer.valueOf((String) objValue);
             int index = mBatteryStatus.findIndexOfValue((String) objValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, value);
             mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
             return true;
         } else if (preference == mMusicControls) {
             boolean value = (Boolean) objValue;
             Settings.System.putInt(cr, Settings.System.LOCKSCREEN_MUSIC_CONTROLS, value ? 1 : 0);
             return true;
         }
         return false;
     }
 }
