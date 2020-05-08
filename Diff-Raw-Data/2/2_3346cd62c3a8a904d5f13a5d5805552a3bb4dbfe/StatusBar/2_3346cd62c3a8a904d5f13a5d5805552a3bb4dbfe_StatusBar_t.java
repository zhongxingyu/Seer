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
 
 package com.sourcery.magiccontrol.fragments;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.SystemProperties;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.text.Spannable;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Toast;
 
 import com.sourcery.magiccontrol.R;
 import com.sourcery.magiccontrol.util.Helpers;
 import com.sourcery.magiccontrol.SettingsPreferenceFragment;
 import com.sourcery.magiccontrol.util.CMDProcessor;
 import com.sourcery.magiccontrol.util.Utils;
 
 import net.margaritov.preference.colorpicker.ColorPickerPreference;
 import net.margaritov.preference.colorpicker.ColorPickerView;
 
 public class StatusBar extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
 
     public static final String TAG = "StatusBar";
    
     private static final String PREF_STATUSBAR_BACKGROUND_COLOR = "statusbar_background_color";
     private static final String PREF_STATUSBAR_BRIGHTNESS = "statusbar_brightness_slider";
     private static final String PREF_STATUSBAR_BACKGROUND_STYLE = "statusbar_background_style";
    
    
  	
       
     ColorPickerPreference mStatusbarBgColor;
     CheckBoxPreference mStatusbarSliderPreference;
     ListPreference mStatusbarBgStyle;
     
     private Activity mActivity;
 
     private int seekbarProgress;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mActivity = getActivity();
 
         addPreferencesFromResource(R.xml.prefs_statusbar_add);
 
         PreferenceScreen prefs = getPreferenceScreen();
 
        
        
         mStatusbarBgColor = (ColorPickerPreference) findPreference(PREF_STATUSBAR_BACKGROUND_COLOR);
         mStatusbarBgColor.setOnPreferenceChangeListener(this);
 
         mStatusbarSliderPreference = (CheckBoxPreference) findPreference(PREF_STATUSBAR_BRIGHTNESS);
         mStatusbarSliderPreference.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                  Settings.System.STATUSBAR_BRIGHTNESS_SLIDER, true));
       
         mStatusbarBgStyle = (ListPreference) findPreference(PREF_STATUSBAR_BACKGROUND_STYLE);
         mStatusbarBgStyle.setOnPreferenceChangeListener(this);
                
         }
         
      
  	
     private void updateVisibility() {
          int visible = Settings.System.getInt(getActivity().getContentResolver(),
                      Settings.System.STATUSBAR_BACKGROUND_STYLE, 2);
          if (visible == 2) {
              mStatusbarBgColor.setEnabled(false);
          } else {
              mStatusbarBgColor.setEnabled(true);
          } 
    }
  
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
             Preference preference) {
 
        if (preference == mStatusbarSliderPreference) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BRIGHTNESS_SLIDER,
                    isCheckBoxPreferenceChecked(preference));
             return true;   
     }
          return super.onPreferenceTreeClick(preferenceScreen, preference);
 
      }
 
           
          @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
          
          if (preference == mStatusbarBgStyle) {
              int value = Integer.valueOf((String) newValue);
              int index = mStatusbarBgStyle.findIndexOfValue((String) newValue);
              Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                      Settings.System.STATUSBAR_BACKGROUND_STYLE, value);
              preference.setSummary(mStatusbarBgStyle.getEntries()[index]);
              updateVisibility();
              return true;
           } else if (preference == mStatusbarBgColor) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
   
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                      Settings.System.STATUSBAR_BACKGROUND_COLOR, intHex);
             Log.e("SOURCERY", intHex + "");
          
          return true;
            }
           return false;
           }
 }
