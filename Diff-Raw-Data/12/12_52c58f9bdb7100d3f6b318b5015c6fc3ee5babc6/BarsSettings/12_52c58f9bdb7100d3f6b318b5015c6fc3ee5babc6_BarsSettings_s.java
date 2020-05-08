 /*
  *  Copyright (C) 2013 The OmniROM Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package org.omnirom.omnigears.interfacesettings;
 
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.R;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 import android.view.WindowManagerGlobal;
 
 public class BarsSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
     private static final String TAG = "BarsSettings";
 
     private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
     private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
     private static final String STATUS_BAR_TRAFFIC = "status_bar_traffic";
     private static final String STATUS_BAR_NETWORK_ACTIVITY = "status_bar_network_activity";
 
     private static final String CATEGORY_NAVBAR = "category_navigation_bar";
 
     private CheckBoxPreference mStatusBarBrightnessControl;
     private CheckBoxPreference mStatusBarNotifCount;
     private CheckBoxPreference mStatusBarTraffic;
     private CheckBoxPreference mStatusBarNetworkActivity;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.bars_settings);
 
         PreferenceScreen prefSet = getPreferenceScreen();
         ContentResolver resolver = getActivity().getContentResolver();
 
         mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
         mStatusBarBrightnessControl.setChecked((Settings.System.getInt(resolver,Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
         mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);
 
         try {
             if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                 mStatusBarBrightnessControl.setEnabled(false);
                 mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
             }
         } catch (SettingNotFoundException e) {
         }
 
         mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
         mStatusBarNotifCount.setChecked(Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1);
         mStatusBarNotifCount.setOnPreferenceChangeListener(this);
 
         mStatusBarTraffic = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC);
         mStatusBarTraffic.setChecked(Settings.System.getInt(resolver,
             Settings.System.STATUS_BAR_TRAFFIC, 0) == 1);
         mStatusBarTraffic.setOnPreferenceChangeListener(this);
 
         mStatusBarNetworkActivity = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_ACTIVITY);
         mStatusBarNetworkActivity.setChecked(Settings.System.getInt(resolver,
             Settings.System.STATUS_BAR_NETWORK_ACTIVITY, 0) == 1);
         mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);
         mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);
 
        try {
            boolean hasNavBar = WindowManagerGlobal.getWindowManagerService().hasNavigationBar();
            // Hide navigation bar category on devices without navigation bar
            if (!hasNavBar) {
                prefSet.removePreference(findPreference(CATEGORY_NAVBAR));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting navigation bar status");
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         // If we didn't handle it, let preferences handle it.
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     public boolean onPreferenceChange(Preference preference, Object objValue) {
         ContentResolver resolver = getActivity().getContentResolver();
         if (preference == mStatusBarBrightnessControl) {
             boolean value = (Boolean) objValue;
             Settings.System.putInt(resolver,Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
         } else if (preference == mStatusBarNotifCount) {
             boolean value = (Boolean) objValue;
             Settings.System.putInt(resolver, Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
         } else if (preference == mStatusBarTraffic) {
             boolean value = (Boolean) objValue;
             Settings.System.putInt(resolver,
                 Settings.System.STATUS_BAR_TRAFFIC, value ? 1 : 0);
         } else if (preference == mStatusBarNetworkActivity) {
             boolean value = (Boolean) objValue;
             Settings.System.putInt(resolver,
                 Settings.System.STATUS_BAR_NETWORK_ACTIVITY, value ? 1 : 0);
         } else {
             return false;
         }
         return true;
     }
 }
