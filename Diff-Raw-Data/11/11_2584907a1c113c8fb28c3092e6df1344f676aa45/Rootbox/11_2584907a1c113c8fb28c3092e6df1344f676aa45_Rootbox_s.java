 /*
  * Copyright (C) 2012 RootBox Project
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
 
 package com.android.settings;
 
 import android.app.Activity;
 import android.app.ActivityManagerNative;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.os.SystemProperties;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 import android.view.IWindowManager;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 
 public class Rootbox extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
     private static final String TAG = "Rootbox";
 
     private static final String KEY_LOCK_CLOCK = "lock_clock";
     private static final String KEY_HARDWARE_KEYS = "hardware_keys";
     private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
     private static final String KEY_EXPANDED_DESKTOP = "power_menu_expanded_desktop";
     private static final String KEY_HEADSET_CONNECT_PLAYER = "headset_connect_player";
     private static final String KEY_VOLUME_ADJUST_SOUNDS = "volume_adjust_sounds";
     private static final String KEY_SEE_TRHOUGH = "see_through";
     private static final String KEY_MMS_BREATH = "mms_breath";
     private static final String KEY_MISSED_CALL_BREATH = "missed_call_breath";
     private static final String KEY_NOTIFICATION_BEHAVIOUR = "notifications_behaviour";
     private static final String KEY_STATUS_BAR_ICON_OPACITY = "status_bar_icon_opacity";
     private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";
     private static final String PREF_KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
     private static final String PREF_FULLSCREEN_KEYBOARD = "fullscreen_keyboard";
     private static final String KEYBOARD_ROTATION_TOGGLE = "keyboard_rotation_toggle";
     private static final String KEYBOARD_ROTATION_TIMEOUT = "keyboard_rotation_timeout";
     private static final String PREF_LOW_BATTERY_WARNING_POLICY = "pref_low_battery_warning_policy";
     private static final String PREF_NOTIFICATION_SHOW_WIFI_SSID = "notification_show_wifi_ssid";
     private static final String VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
     private static final String RB_HARDWARE_KEYS = "rb_hardware_keys";
     private static final String RB_GENERAL_UI = "rb_general_ui";
 
     private static final int KEYBOARD_ROTATION_TIMEOUT_DEFAULT = 5000; // 5s
     
     private PreferenceScreen mLockscreenButtons;
     private PreferenceScreen mHardwareKeys;
     private CheckBoxPreference mExpandedDesktopPref;
     private CheckBoxPreference mHeadsetConnectPlayer;
     private CheckBoxPreference mVolumeAdjustSounds;
     private CheckBoxPreference mKillAppLongpressBack;
     private CheckBoxPreference mCrtOff;
     private CheckBoxPreference mCrtOn;
     private CheckBoxPreference mFullscreenKeyboard;
     private CheckBoxPreference mKeyboardRotationToggle;
     private CheckBoxPreference mSeeThrough;
     private CheckBoxPreference mMMSBreath;
     private CheckBoxPreference mMissedCallBreath;
     private CheckBoxPreference mShowWifiName;
     private CheckBoxPreference mSwapVolumeButtons;
     private ListPreference mVolumeKeyCursorControl;
     private ListPreference mKeyboardRotationTimeout;
     private ListPreference mLowBatteryWarning;
     private ListPreference mNotificationsBeh;
     private ListPreference mStatusBarIconOpacity;
     private final Configuration mCurConfig = new Configuration();
     private ContentResolver mCr;
     private Context mContext;
     private PreferenceScreen mPrefSet;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         ContentResolver resolver = getContentResolver();
         mContext = getActivity();
         mPrefSet = getPreferenceScreen();
         mCr = getContentResolver();
 
         addPreferencesFromResource(R.xml.rootbox_settings);
         PreferenceScreen prefs = getPreferenceScreen();
 
         mSeeThrough = (CheckBoxPreference) findPreference(KEY_SEE_TRHOUGH);
         mSeeThrough.setChecked(Settings.System.getInt(resolver,
                 Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1);
 
         mMMSBreath = (CheckBoxPreference) findPreference(KEY_MMS_BREATH);
         mMMSBreath.setChecked(Settings.System.getInt(resolver,
                 Settings.System.MMS_BREATH, 0) == 1);
 
         mMissedCallBreath = (CheckBoxPreference) findPreference(KEY_MISSED_CALL_BREATH);
         mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                 Settings.System.MISSED_CALL_BREATH, 0) == 1);
 
         mShowWifiName = (CheckBoxPreference) findPreference(PREF_NOTIFICATION_SHOW_WIFI_SSID);
         mShowWifiName.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.NOTIFICATION_SHOW_WIFI_SSID, 0) == 1);
 
         int CurrentBeh = Settings.Secure.getInt(mCr, Settings.Secure.NOTIFICATIONS_BEHAVIOUR, 0);
         mNotificationsBeh = (ListPreference) findPreference(KEY_NOTIFICATION_BEHAVIOUR);
         mNotificationsBeh.setValue(String.valueOf(CurrentBeh));
                 mNotificationsBeh.setSummary(mNotificationsBeh.getEntry());
         mNotificationsBeh.setOnPreferenceChangeListener(this);
 
         int iconOpacity = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_NOTIF_ICON_OPACITY, 140);
         mStatusBarIconOpacity = (ListPreference) findPreference(KEY_STATUS_BAR_ICON_OPACITY);
         mStatusBarIconOpacity.setValue(String.valueOf(iconOpacity));
         mStatusBarIconOpacity.setOnPreferenceChangeListener(this);
 
         mLockscreenButtons = (PreferenceScreen) findPreference(KEY_LOCKSCREEN_BUTTONS);
         mHardwareKeys = (PreferenceScreen) findPreference(KEY_HARDWARE_KEYS);
 
         mFullscreenKeyboard = (CheckBoxPreference) findPreference(PREF_FULLSCREEN_KEYBOARD);
         mFullscreenKeyboard.setChecked(Settings.System.getInt(resolver,
                 Settings.System.FULLSCREEN_KEYBOARD, 0) == 1);
 
         mKeyboardRotationToggle = (CheckBoxPreference) findPreference(KEYBOARD_ROTATION_TOGGLE);
         mKeyboardRotationToggle.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.KEYBOARD_ROTATION_TIMEOUT, 0) > 0);
 
         mKeyboardRotationTimeout = (ListPreference) findPreference(KEYBOARD_ROTATION_TIMEOUT);
         mKeyboardRotationTimeout.setOnPreferenceChangeListener(this);
         updateRotationTimeout(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.KEYBOARD_ROTATION_TIMEOUT, KEYBOARD_ROTATION_TIMEOUT_DEFAULT));
 
         mLowBatteryWarning = (ListPreference) findPreference(PREF_LOW_BATTERY_WARNING_POLICY);
         int lowBatteryWarning = Settings.System.getInt(getActivity().getContentResolver(),
                                     Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY, 3);
         mLowBatteryWarning.setValue(String.valueOf(lowBatteryWarning));
         mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntry());
         mLowBatteryWarning.setOnPreferenceChangeListener(this);
 
         mVolumeKeyCursorControl = (ListPreference) findPreference(VOLUME_KEY_CURSOR_CONTROL);
         if(mVolumeKeyCursorControl != null) {
             mVolumeKeyCursorControl.setOnPreferenceChangeListener(this);
             mVolumeKeyCursorControl.setValue(Integer.toString(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0)));
             mVolumeKeyCursorControl.setSummary(mVolumeKeyCursorControl.getEntry());
         }
 
         mHeadsetConnectPlayer = (CheckBoxPreference) findPreference(KEY_HEADSET_CONNECT_PLAYER);
         mHeadsetConnectPlayer.setChecked(Settings.System.getInt(resolver,
                 Settings.System.HEADSET_CONNECT_PLAYER, 0) != 0);
 
         mVolumeAdjustSounds = (CheckBoxPreference) findPreference(KEY_VOLUME_ADJUST_SOUNDS);
         mVolumeAdjustSounds.setPersistent(false);
         mVolumeAdjustSounds.setChecked(Settings.System.getInt(resolver,
                 Settings.System.VOLUME_ADJUST_SOUNDS_ENABLED, 1) != 0);
 
         mSwapVolumeButtons = (CheckBoxPreference) findPreference(KEY_SWAP_VOLUME_BUTTONS);
         mSwapVolumeButtons.setChecked(Settings.System.getInt(resolver,
                 Settings.System.SWAP_VOLUME_KEYS, 0) == 1);
 
         mKillAppLongpressBack = (CheckBoxPreference) findPreference(PREF_KILL_APP_LONGPRESS_BACK);
                 updateKillAppLongpressBackOptions();
 
         boolean hasNavBarByDefault = getResources().getBoolean(
                 com.android.internal.R.bool.config_showNavigationBar);
 
         mExpandedDesktopPref = (CheckBoxPreference) findPreference(KEY_EXPANDED_DESKTOP);
         boolean showExpandedDesktopPref =
             getResources().getBoolean(R.bool.config_show_expandedDesktop);
         if (!showExpandedDesktopPref) {
             if (mExpandedDesktopPref != null) {
                 getPreferenceScreen().removePreference(mExpandedDesktopPref);
             }
         } else {
             mExpandedDesktopPref.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 0) == 1));
         }
 
         // Do not display lock clock preference if its not installed
         removePreferenceIfPackageNotInstalled(findPreference(KEY_LOCK_CLOCK));
 
         // Only show the hardware keys config on a device that does not have a navbar
        IWindowManager windowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            if (windowManager.hasNavigationBar()) {
                   getPreferenceScreen().removePreference(findPreference(RB_HARDWARE_KEYS));
                   PreferenceCategory generalCategory = (PreferenceCategory) findPreference(RB_GENERAL_UI);
                   generalCategory.removePreference(mKillAppLongpressBack);
            }
        } catch (RemoteException e) {
            // Do nothing
         }
     }
 
     public void updateRotationTimeout(int timeout) {
         if (timeout == 0)
             timeout = KEYBOARD_ROTATION_TIMEOUT_DEFAULT;
         mKeyboardRotationTimeout.setValue(Integer.toString(timeout));
         mKeyboardRotationTimeout.setSummary(getString(R.string.keyboard_rotation_timeout_summary, mKeyboardRotationTimeout.getEntry()));
     }
     
     @Override
     public void onResume() {
         super.onResume();
     }
 
     private void writeKillAppLongpressBackOptions() {
         Settings.System.putInt(getActivity().getContentResolver(),
                 Settings.System.KILL_APP_LONGPRESS_BACK, mKillAppLongpressBack.isChecked() ? 1 : 0);
     }
 
     private void updateKillAppLongpressBackOptions() {
         mKillAppLongpressBack.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.KILL_APP_LONGPRESS_BACK, 0) != 0);
     }
 
     @Override
     public void onPause() {
         super.onPause();
     }
 
     public void mKeyboardRotationDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setMessage(R.string.keyboard_rotation_dialog);
         builder.setCancelable(false);
         builder.setPositiveButton(getResources().getString(com.android.internal.R.string.ok), null);
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
          boolean value;
 
          if (preference == mExpandedDesktopPref) {
             value = mExpandedDesktopPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED,
                     value ? 1 : 0);
          } else if (preference == mHeadsetConnectPlayer) {
             Settings.System.putInt(getContentResolver(), Settings.System.HEADSET_CONNECT_PLAYER,
                     mHeadsetConnectPlayer.isChecked() ? 1 : 0);
          } else if (preference == mVolumeAdjustSounds) {
             Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ADJUST_SOUNDS_ENABLED,
                     mVolumeAdjustSounds.isChecked() ? 1 : 0);
          } else if (preference == mSwapVolumeButtons) {
             Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SWAP_VOLUME_KEYS,
                     mSwapVolumeButtons.isChecked() ? 1 : 0);
          } else if (preference == mKillAppLongpressBack) {
             writeKillAppLongpressBackOptions();
          } else if (preference == mFullscreenKeyboard) {
             Settings.System.putInt(getActivity().getContentResolver(), Settings.System.FULLSCREEN_KEYBOARD,
                     mFullscreenKeyboard.isChecked() ? 1 : 0);
          } else if (preference == mMMSBreath) {
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.MMS_BREATH, 
                     mMMSBreath.isChecked() ? 1 : 0);
          } else if (preference == mMissedCallBreath) {
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.MISSED_CALL_BREATH, 
                     mMissedCallBreath.isChecked() ? 1 : 0);
          } else if (preference == mSeeThrough) {
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH, 
                     mSeeThrough.isChecked() ? 1 : 0);
          } else if (preference == mShowWifiName) {
             Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NOTIFICATION_SHOW_WIFI_SSID,
                     mShowWifiName.isChecked() ? 1 : 0);
          } else if (preference == mKeyboardRotationToggle) {
             boolean isAutoRotate = (Settings.System.getInt(getContentResolver(),
                         Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
             if (isAutoRotate && mKeyboardRotationToggle.isChecked())
                 mKeyboardRotationDialog();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.KEYBOARD_ROTATION_TIMEOUT,
                     mKeyboardRotationToggle.isChecked() ? KEYBOARD_ROTATION_TIMEOUT_DEFAULT : 0);
             updateRotationTimeout(KEYBOARD_ROTATION_TIMEOUT_DEFAULT);
          }  else {
               // If not handled, let preferences handle it.
               return super.onPreferenceTreeClick(preferenceScreen, preference);
          }
          return true;    
      }
 
     public boolean onPreferenceChange(Preference preference, Object Value) {
         final String key = preference.getKey();
          if (preference == mLowBatteryWarning) {
             int lowBatteryWarning = Integer.valueOf((String) Value);
             int index = mLowBatteryWarning.findIndexOfValue((String) Value);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY, lowBatteryWarning);
             mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntries()[index]);
             return true;
         } else if (preference == mVolumeKeyCursorControl) {
             String volumeKeyCursorControl = (String) Value;
             int val = Integer.parseInt(volumeKeyCursorControl);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.VOLUME_KEY_CURSOR_CONTROL, val);
             int index = mVolumeKeyCursorControl.findIndexOfValue(volumeKeyCursorControl);
             mVolumeKeyCursorControl.setSummary(mVolumeKeyCursorControl.getEntries()[index]);
             return true;
         } else if (preference == mNotificationsBeh) {
             String val = (String) Value;
                      Settings.Secure.putInt(mCr, Settings.Secure.NOTIFICATIONS_BEHAVIOUR,
             Integer.valueOf(val));
             int index = mNotificationsBeh.findIndexOfValue(val);
             mNotificationsBeh.setSummary(mNotificationsBeh.getEntries()[index]);
             return true;
         } else if (preference == mStatusBarIconOpacity) {
             int iconOpacity = Integer.valueOf((String) Value);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_NOTIF_ICON_OPACITY, iconOpacity);
             return true;
         } else if (preference == mKeyboardRotationTimeout) {
             int timeout = Integer.parseInt((String) Value);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.KEYBOARD_ROTATION_TIMEOUT, timeout);
             updateRotationTimeout(timeout);
             return true;
         }
         return false;
     }
 
     private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
         String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
         Pattern pattern = Pattern.compile("component=([^/]+)/");
         Matcher matcher = pattern.matcher(intentUri);
 
         String packageName = matcher.find() ? matcher.group(1) : null;
         if (packageName != null) {
             try {
                 getPackageManager().getPackageInfo(packageName, 0);
             } catch (NameNotFoundException e) {
                 Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                 getPreferenceScreen().removePreference(preference);
                 return true;
             }
         }
         return false;
     }
 
 }
