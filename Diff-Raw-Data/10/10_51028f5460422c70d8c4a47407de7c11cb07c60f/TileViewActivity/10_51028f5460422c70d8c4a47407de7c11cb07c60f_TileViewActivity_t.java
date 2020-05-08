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
 
 package com.cyanogenmod.cmparts.activities;
 
 import com.android.internal.telephony.Phone;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.ContentResolver;
 import android.net.wimax.WimaxHelper;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.MultiSelectListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.text.InputFilter;
 import android.text.InputFilter.LengthFilter;
 import android.text.TextUtils;
 import android.widget.EditText;
 import android.util.Log;
 
 import com.cyanogenmod.cmparts.R;
 import com.cyanogenmod.cmparts.utils.TileViewUtil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class TileViewActivity extends PreferenceActivity implements OnPreferenceChangeListener {
 
     private static final String TAG = "TileViewActivity";
 
     private static final String TILES_CATEGORY = "pref_tiles";
     private static final String SELECT_TILE_KEY_PREFIX = "pref_tile_";
 
     private static final String EXP_BRIGHTNESS_MODE = "pref_brightness_mode";
     private static final String EXP_NETWORK_MODE = "pref_network_mode";
     private static final String EXP_SCREENTIMEOUT_MODE = "pref_screentimeout_mode";
     private static final String EXP_RING_MODE = "pref_ring_mode";
     private static final String EXP_FLASH_MODE = "pref_flash_mode";
     private static final String EXP_MOBILEDATANETWORK_MODE = "pref_mobiledatanetwork_mode";
     private static final String PREF_USER_WIDGETS = "pref_user_widgets";
 
     private HashMap<CheckBoxPreference, String> mCheckBoxPrefs = new HashMap<CheckBoxPreference, String>();
 
     MultiSelectListPreference mBrightnessMode;
     ListPreference mNetworkMode;
     ListPreference mScreenTimeoutMode;
     MultiSelectListPreference mRingMode;
     ListPreference mFlashMode;
     ListPreference mMobileDataNetworkMode;
     EditTextPreference mUserNumbers;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitle(R.string.title_tileview);
         addPreferencesFromResource(R.xml.tileview_widget);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mUserNumbers = (EditTextPreference) prefSet.findPreference(PREF_USER_WIDGETS);
         if (mUserNumbers != null) {
             EditText numberEditText = mUserNumbers.getEditText();
 
             if (numberEditText != null) {
                 InputFilter lengthFilter = new InputFilter.LengthFilter(25);
                 numberEditText.setFilters(new InputFilter[]{lengthFilter});
                 numberEditText.setSingleLine(true);
             }
         }
 
         String userNumber = Settings.System.getString(getContentResolver(),
                 Settings.System.USER_MY_NUMBERS);
 
        if (userNumber == null || userNumber.equals("") || TextUtils.isEmpty(userNumber)) {
             userNumber = "000000000";
             Settings.System.putString(getContentResolver(), Settings.System.USER_MY_NUMBERS,
 		    userNumber);
 	}
         mUserNumbers.setText(userNumber);
         mUserNumbers.setOnPreferenceChangeListener(this);
 
         mBrightnessMode = (MultiSelectListPreference) prefSet.findPreference(EXP_BRIGHTNESS_MODE);
         mBrightnessMode.setValue(Settings.System.getString(getContentResolver(), Settings.System.EXPANDED_BRIGHTNESS_MODE));
         mBrightnessMode.setOnPreferenceChangeListener(this);
         mNetworkMode = (ListPreference) prefSet.findPreference(EXP_NETWORK_MODE);
         mNetworkMode.setOnPreferenceChangeListener(this);
         mScreenTimeoutMode = (ListPreference) prefSet.findPreference(EXP_SCREENTIMEOUT_MODE);
         mScreenTimeoutMode.setOnPreferenceChangeListener(this);
         mRingMode = (MultiSelectListPreference) prefSet.findPreference(EXP_RING_MODE);
         mRingMode.setValue(Settings.System.getString(getContentResolver(), Settings.System.EXPANDED_RING_MODE));
         mRingMode.setOnPreferenceChangeListener(this);
         mFlashMode = (ListPreference) prefSet.findPreference(EXP_FLASH_MODE);
         mFlashMode.setOnPreferenceChangeListener(this);
 	mMobileDataNetworkMode = (ListPreference) prefSet.findPreference(EXP_MOBILEDATANETWORK_MODE);
         mMobileDataNetworkMode.setOnPreferenceChangeListener(this);
 
         PreferenceCategory prefTiles = (PreferenceCategory) prefSet.findPreference(TILES_CATEGORY);
 
         // empty our preference category and set it to order as added
         prefTiles.removeAll();
         prefTiles.setOrderingAsAdded(false);
 
         // emtpy our checkbox map
         mCheckBoxPrefs.clear();
 
         // get our list of tiles
         ArrayList<String> tileList = TileViewUtil.getTileListFromString(TileViewUtil.getCurrentTiles(this));
 
         // Don't show WiMAX option if not supported
         boolean isWimaxEnabled = WimaxHelper.isWimaxSupported(this);
         if (!isWimaxEnabled) {
             TileViewUtil.TILES.remove(TileViewUtil.TILE_WIMAX);
         }
 
         // fill that checkbox map!
         for(TileViewUtil.TileInfo tile : TileViewUtil.TILES.values()) {
             // create a checkbox
             CheckBoxPreference cb = new CheckBoxPreference(this);
 
             // set a dynamic key based on tile id
             cb.setKey(SELECT_TILE_KEY_PREFIX + tile.getId());
 
             // set vanity info
             cb.setTitle(tile.getTitleResId());
 
             // set our checked state
             cb.setChecked(tileList.contains(tile.getId()));
 
             // add to our prefs set
             mCheckBoxPrefs.put(cb, tile.getId());
 
             // specific checks for availability on some platforms
             if (TileViewUtil.TILE_TORCH.equals(tile.getId()) &&
                     !getResources().getBoolean(R.bool.has_led_flash)) { // disable flashlight if it's not supported
                 cb.setEnabled(false);
                 mFlashMode.setEnabled(false);
             } else if (TileViewUtil.TILE_NETWORKMODE.equals(tile.getId())) {
                 // some phones run on networks not supported by this tile, so disable it
                 boolean knownState = false;
 
                 try {
                     int networkState = Settings.Secure.getInt(getContentResolver(),
                             Settings.Secure.PREFERRED_NETWORK_MODE);
 
                     switch (networkState) {
                         // list of supported network modes
                         case Phone.NT_MODE_WCDMA_PREF:
                         case Phone.NT_MODE_WCDMA_ONLY:
                         case Phone.NT_MODE_GSM_UMTS:
                         case Phone.NT_MODE_GSM_ONLY:
                             knownState = true;
                             break;
                     }
                 } catch(Settings.SettingNotFoundException e) {
                     Log.e(TAG, "Unable to retrieve PREFERRED_NETWORK_MODE", e);
                 }
 
                 if (!knownState) {
                     cb.setEnabled(false);
                     mNetworkMode.setEnabled(false);
                 }
             } else if (TileViewUtil.TILE_WIMAX.equals(tile.getId())) {
                 if (!isWimaxEnabled) {
                     cb.setEnabled(false);
                 }
             }
 
             // add to the category
             prefTiles.addPreference(cb);
         }
         ContentResolver cr = getContentResolver();
         String value;
 
         value = Settings.System.getString(cr, Settings.System.EXPANDED_BRIGHTNESS_MODE);
         if (value != null) {
             mBrightnessMode.setValue(value);
         }
 
         value = Settings.System.getString(cr, Settings.System.EXPANDED_RING_MODE);
         if (value != null) {
             mRingMode.setValue(value);
         }
 
         mNetworkMode.setValueIndex(Settings.System.getInt(
                 cr, Settings.System.EXPANDED_NETWORK_MODE, 0));
         mScreenTimeoutMode.setValueIndex(Settings.System.getInt(
                 cr, Settings.System.EXPANDED_SCREENTIMEOUT_MODE, 0));
         mFlashMode.setValueIndex(Settings.System.getInt(
                 cr, Settings.System.EXPANDED_FLASH_MODE, 0));
         mMobileDataNetworkMode.setValueIndex(Settings.System.getInt(
                 cr, Settings.System.EXPANDED_MOBILEDATANETWORK_MODE, 0));
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         // we only modify the tile list if it was one of our checks that was clicked
         boolean tileWasModified = false;
         ArrayList<String> tileList = new ArrayList<String>();
         for(Map.Entry<CheckBoxPreference, String> entry : mCheckBoxPrefs.entrySet()) {
             if(entry.getKey().isChecked()) {
                 tileList.add(entry.getValue());
             }
 
             if(preference == entry.getKey()) {
                 tileWasModified = true;
             }
         }
 
         if(tileWasModified) {
             // now we do some wizardry and reset the tile list
             TileViewUtil.saveCurrentTiles(this, TileViewUtil.mergeInNewTileString(
                     TileViewUtil.getCurrentTiles(this), TileViewUtil.getTileStringFromList(tileList)));
             return true;
         }
 
         return false;
     }
 
     private void usersWidgets() {
         AlertDialog alert = new AlertDialog.Builder(this).create();
         alert.setTitle("CyanMobile Notice");
         alert.setMessage(getResources().getString(R.string.userwidgets_message));
         alert.setButton(DialogInterface.BUTTON_POSITIVE,
                 getResources().getString(com.android.internal.R.string.ok),
                 new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 return;
             }
         });
         alert.show();
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if(preference == mBrightnessMode) {
             Settings.System.putString(getContentResolver(), Settings.System.EXPANDED_BRIGHTNESS_MODE, (String) newValue);
         } else if(preference == mNetworkMode) {
             int value = Integer.valueOf((String)newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_NETWORK_MODE, value);
         } else if(preference == mScreenTimeoutMode) {
             int value = Integer.valueOf((String)newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_SCREENTIMEOUT_MODE, value);
         } else if(preference == mRingMode) {
             Settings.System.putString(getContentResolver(), Settings.System.EXPANDED_RING_MODE, (String) newValue);
         } else if(preference == mFlashMode) {
             int value = Integer.valueOf((String)newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_FLASH_MODE, value);
 	} else if(preference == mMobileDataNetworkMode) {
             int value = Integer.valueOf((String)newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_MOBILEDATANETWORK_MODE, value);
         } else if (preference == mUserNumbers) {
             String userNumbers = String.valueOf(newValue);
            if (userNumbers == null || userNumbers.equals("")|| TextUtils.isEmpty(userNumbers)) {
               userNumbers = "000000000";
               Settings.System.putString(getContentResolver(), Settings.System.USER_MY_NUMBERS, userNumbers);
	    } else {
               Settings.System.putString(getContentResolver(), Settings.System.USER_MY_NUMBERS, userNumbers);
            }
             usersWidgets();
         }
         return true;
     }
 }
