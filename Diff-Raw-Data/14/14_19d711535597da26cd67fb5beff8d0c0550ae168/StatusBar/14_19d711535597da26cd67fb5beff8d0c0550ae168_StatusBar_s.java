 /*
  * Copyright (C) 2012 The CyanogenMod Project
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
 
 package com.android.settings.carbon;
 
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 
 public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
 
     private static final String TAG = "StatusBar";
 
     private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
     private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
     private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
     private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
     private static final String KEY_STATUS_BAR_ICON_OPACITY = "status_bar_icon_opacity";
     private static final String KEY_MMS_BREATH = "mms_breath";
     private static final String KEY_MISSED_CALL_BREATH = "missed_call_breath";
 
     private ListPreference mStatusBarCmSignal;
     private CheckBoxPreference mStatusBarBrightnessControl;
     private CheckBoxPreference mStatusBarNotifCount;
     private PreferenceScreen mClockStyle;
     private PreferenceCategory mPrefCategoryGeneral;
     private ListPreference mStatusBarIconOpacity;
     private CheckBoxPreference mMMSBreath;
     private CheckBoxPreference mMissedCallBreath;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.status_bar);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
         mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);
 
         mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
 
         try {
             if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                 mStatusBarBrightnessControl.setEnabled(false);
                 mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
             }
         } catch (SettingNotFoundException e) {
         }
 
         int signalStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
         mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
         mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
         mStatusBarCmSignal.setOnPreferenceChangeListener(this);
 
         mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
         mStatusBarNotifCount.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1));
 
         int iconOpacity = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_NOTIF_ICON_OPACITY, 140);
         mStatusBarIconOpacity = (ListPreference) findPreference(KEY_STATUS_BAR_ICON_OPACITY);
         mStatusBarIconOpacity.setValue(String.valueOf(iconOpacity));
         mStatusBarIconOpacity.setOnPreferenceChangeListener(this);
 
         mMMSBreath = (CheckBoxPreference) findPreference(KEY_MMS_BREATH);
         mMMSBreath.setChecked(Settings.System.getInt(resolver,
                 Settings.System.MMS_BREATH, 0) == 1);
 
         mMissedCallBreath = (CheckBoxPreference) findPreference(KEY_MISSED_CALL_BREATH);
         mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                 Settings.System.MISSED_CALL_BREATH, 0) == 1);
 
         mPrefCategoryGeneral = (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_GENERAL);
 
         if (Utils.isWifiOnly(getActivity())) {
             mPrefCategoryGeneral.removePreference(mStatusBarCmSignal);
         }
 
         if (Utils.isTablet(getActivity())) {
             mPrefCategoryGeneral.removePreference(mStatusBarBrightnessControl);
         }
 
         mClockStyle = (PreferenceScreen) prefSet.findPreference("clock_style_pref");
         if (mClockStyle != null) {
             updateClockStyleDescription();
         }
 
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         boolean result = false;
         if (preference == mStatusBarCmSignal) {
             int signalStyle = Integer.valueOf((String) newValue);
             int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
             mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
             return true;
         } else if (preference == mStatusBarIconOpacity) {
             int iconOpacity = Integer.valueOf((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_NOTIF_ICON_OPACITY, iconOpacity);
             return true;
         }
         return false;
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
 
         if (preference == mStatusBarBrightnessControl) {
             value = mStatusBarBrightnessControl.isChecked();
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
             return true;
         } else if (preference == mStatusBarNotifCount) {
             value = mStatusBarNotifCount.isChecked();
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
             return true;
         } else if (preference == mMMSBreath) {
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.MMS_BREATH, 
                     mMMSBreath.isChecked() ? 1 : 0);
             return true;
         } else if (preference == mMissedCallBreath) {
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.MISSED_CALL_BREATH, 
                     mMissedCallBreath.isChecked() ? 1 : 0);
             return true;
         }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     private void updateClockStyleDescription() {
         if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
             mClockStyle.setSummary(getString(R.string.clock_enabled));
         } else {
             mClockStyle.setSummary(getString(R.string.clock_disabled));
          }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         updateClockStyleDescription();
     }
 }
