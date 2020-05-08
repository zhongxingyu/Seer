 /*
  * Copyright (C) 2011 The CyanogenMod Project
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
 
 package com.savagedzem.szparts.activities;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.content.Intent.ShortcutIconResource;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 
 import com.savagedzen.szparts.R;
 
 public class DeviceActivity extends PreferenceActivity {
 
     private static final String TRACKBALL_WAKE_PREF = "pref_trackball_wake";
 
     private CheckBoxPreference mTrackballWakePref;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
        setTitle(R.string.input_settings_title_subhead);
         addPreferencesFromResource(R.xml.device_settings);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         /* Trackball Wake */
         mTrackballWakePref = (CheckBoxPreference) prefSet.findPreference(TRACKBALL_WAKE_PREF);
         mTrackballWakePref.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.TRACKBALL_WAKE_SCREEN, 1) == 1);
 
         if (!getResources().getBoolean(R.bool.has_trackball)) {
             buttonCategory.removePreference(mTrackballWakePref);
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         if (preference == mTrackballWakePref) {
             value = mTrackballWakePref.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.TRACKBALL_WAKE_SCREEN,
                     value ? 1 : 0);
             return true;
        } else if (preference == mVolumeWakePref) {
            value = mVolumeWakePref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
         }
 
         return false;
     }
    @Override
 }
