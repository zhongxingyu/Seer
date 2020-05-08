 /*
  * Copyright (C) 2011 The SalvageMod Project
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
 
 package com.android.launcher2;
 
 import android.app.Activity;
 import android.widget.TextView;
 import android.os.Bundle;
 import java.lang.Boolean;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceScreen;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceGroup;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.view.View.OnClickListener;
 import android.preference.PreferenceManager;
 import android.preference.CheckBoxPreference;
 import android.content.Context;
 import android.view.View;
 import android.util.Log;
 
 import com.android.launcher.R;
 
 public class LauncherPreferenceActivity extends PreferenceActivity{
 
     public static final String LAUNCHER_HIDE_LABELS = "pref_key_launcher_hide_labels";
     public static final String LAUNCHER_SHOW_SHORTCUTS_LABEL = "pref_key_launcher_show_shortcuts_label";
     public static final String LAUNCHER_FULLSCREEN = "pref_key_launcher_fullscreen";
     public static final String LAUNCHER_WALLPAPER_SCROLLING = "pref_key_launcher_wallpaper_scrolling";
    public static final String LAUNCHER_ORIENTATION = "pref_key_launcher_orientation";
 
    //private static final String APPS2D_PREF = "pref_key_2d_setting";
 
    //private CheckBoxPreference m2dSettingPref;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.preferences);
 
     }
 
 }
