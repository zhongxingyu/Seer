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
 
 package com.android.settings.cyanogenmod;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ColorPickerPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 import com.android.settings.util.CMDProcessor;
 import com.android.settings.util.Helpers;
 
 
 public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
 
     private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
    private static final String STATUS_BAR_CATEGORY_COLOR = "status_bar_color";
     private static final String STATUS_BAR_CATEGORY_CLOCK = "status_bar_clock";
     private static final String STATUS_BAR_COLOR_DEF = "statusbar_color_default";
     private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
     private static final String STATUS_BAR_BATTERY = "status_bar_battery";
     private static final String STATUS_BAR_MAX_NOTIF = "status_bar_max_notifications";
     private static final String STATUS_BAR_CLOCK = "status_bar_show_clock";
     private static final String STATUS_BAR_CENTER_CLOCK = "status_bar_center_clock";
     private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
     private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
     private static final String COMBINED_BAR_AUTO_HIDE = "combined_bar_auto_hide";
     private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
     private static final String STATUS_BAR_DONOTDISTURB = "status_bar_donotdisturb";
     private static final String STATUS_BAR_COLOR = "status_bar_color";
     private static final String STATUS_BAR_WEEKDAY = "status_bar_weekday";
     private static final String STATUS_BAR_WEEKDAY_FORMAT = "status_bar_weekday_format";
     private static final String STATUS_BAR_DAYMONTH = "status_bar_daymonth";
     private static final String PREF_COLOR_PICKER = "clock_color";
 
 
     private ListPreference mStatusBarAmPm;
     private ListPreference mStatusBarBattery;
     private ListPreference mStatusBarMaxNotif;
     private ColorPickerPreference mColorPicker;
     private ListPreference mStatusBarCmSignal;
     private ColorPickerPreference mStatusBarColor;
     private ListPreference mStatusBarWeekday;
     private ListPreference mStatusBarWeekdayFormat;
     private ListPreference mStatusBarDaymonth;
    private ListPreference mStatusBarCmSignal;
     private CheckBoxPreference mStatusBarClock;
     private CheckBoxPreference mStatusBarCenterClock;
     private CheckBoxPreference mStatusBarBrightnessControl;
     private CheckBoxPreference mCombinedBarAutoHide;
     private CheckBoxPreference mStatusBarNotifCount;
     private CheckBoxPreference mStatusBarDoNotDisturb;
     private Preference mResetColor;
     private PreferenceCategory mPrefCategoryGeneral;
    private PreferenceCategory mPrefCategoryColor;
     private PreferenceCategory mPrefCategoryClock;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.status_bar);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mStatusBarClock = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CLOCK);
         mStatusBarCenterClock = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CENTER_CLOCK);
         mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
         mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
         mStatusBarBattery = (ListPreference) prefSet.findPreference(STATUS_BAR_BATTERY);
         mStatusBarWeekday = (ListPreference) prefSet.findPreference(STATUS_BAR_WEEKDAY);
         mStatusBarWeekdayFormat = (ListPreference) prefSet.findPreference(STATUS_BAR_WEEKDAY_FORMAT);
         if (Utils.isTablet()) {
             mStatusBarWeekdayFormat.setEntries(new String[]{"Short","Medium (default)"});
             mStatusBarWeekdayFormat.setEntryValues(new String[]{"0", "1"});
         }
         mStatusBarDaymonth = (ListPreference) prefSet.findPreference(STATUS_BAR_DAYMONTH);
         mStatusBarMaxNotif = (ListPreference) prefSet.findPreference(STATUS_BAR_MAX_NOTIF);
         mCombinedBarAutoHide = (CheckBoxPreference) prefSet.findPreference(COMBINED_BAR_AUTO_HIDE);
         mStatusBarDoNotDisturb = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_DONOTDISTURB);
         mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);
         mColorPicker = (ColorPickerPreference) findPreference(PREF_COLOR_PICKER);
         mColorPicker.setOnPreferenceChangeListener(this);
 
         mStatusBarColor = (ColorPickerPreference) prefSet.findPreference(STATUS_BAR_COLOR);
         mStatusBarColor.setOnPreferenceChangeListener(this);
         mStatusBarColor.setAlphaSliderEnabled(true);
 
         mResetColor = (Preference) findPreference(STATUS_BAR_COLOR_DEF);
         mResetColor.setOnPreferenceClickListener(this);
 
         mStatusBarClock.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_CLOCK, 1) == 1));
         mStatusBarCenterClock.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_CENTER_CLOCK, 0) == 1));
         mStatusBarCenterClock.setEnabled(mStatusBarClock.isChecked());
         mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
         mStatusBarDoNotDisturb.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_DONOTDISTURB, 0) == 1));
 
         try {
             if (Settings.System.getInt(getActivity().getContentResolver(),
                     Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                 mStatusBarBrightnessControl.setEnabled(false);
                 mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
             }
         } catch (SettingNotFoundException e) {
         }
 
         try {
             if (Settings.System.getInt(getActivity().getContentResolver(),
                     Settings.System.TIME_12_24) == 24) {
                 mStatusBarAmPm.setEnabled(false);
                 mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
             }
         } catch (SettingNotFoundException e) {
         }
 
         int statusBarAmPm = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_AM_PM, 2);
         mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
         mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
         mStatusBarAmPm.setOnPreferenceChangeListener(this);
 
         int statusBarWeekday = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_WEEKDAY, 2);
         mStatusBarWeekday.setValue(String.valueOf(statusBarWeekday));
         mStatusBarWeekday.setSummary(mStatusBarWeekday.getEntry());
         mStatusBarWeekday.setOnPreferenceChangeListener(this);
 
         int statusBarWeekdayFormat = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_WEEKDAY_FORMAT, 1);
         mStatusBarWeekdayFormat.setValue(String.valueOf(statusBarWeekdayFormat));
         mStatusBarWeekdayFormat.setSummary(mStatusBarWeekdayFormat.getEntry());
         mStatusBarWeekdayFormat.setOnPreferenceChangeListener(this);
 
         int statusBarDaymonth = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_DAYMONTH, 2);
         mStatusBarDaymonth.setValue(String.valueOf(statusBarDaymonth));
         mStatusBarDaymonth.setSummary(mStatusBarDaymonth.getEntry());
         mStatusBarDaymonth.setOnPreferenceChangeListener(this);
 
         int statusBarBattery = Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_BATTERY, 0);
         mStatusBarBattery.setValue(String.valueOf(statusBarBattery));
         mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
         mStatusBarBattery.setOnPreferenceChangeListener(this);
 
         int maxNotIcons = Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.MAX_NOTIFICATION_ICONS, 2);
         mStatusBarMaxNotif.setValue(String.valueOf(maxNotIcons));
         mStatusBarMaxNotif.setOnPreferenceChangeListener(this);
 
         int signalStyle = Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
         mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
         mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
         mStatusBarCmSignal.setOnPreferenceChangeListener(this);
 
         mCombinedBarAutoHide.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.COMBINED_BAR_AUTO_HIDE, 0) == 1));
 
         mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
         mStatusBarNotifCount.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1));
 
         mPrefCategoryGeneral = (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_GENERAL);
        mPrefCategoryColor = (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_COLOR);
         mPrefCategoryClock = (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_CLOCK);
 
         if (Utils.isTablet()) {
             mPrefCategoryClock.removePreference(mStatusBarCenterClock);
            mPrefCategoryColor.removePreference(mStatusBarColor);
             mPrefCategoryGeneral.removePreference(mStatusBarBrightnessControl);
             mPrefCategoryGeneral.removePreference(mStatusBarCmSignal);
         } else {
             mPrefCategoryGeneral.removePreference(mStatusBarMaxNotif);
             mPrefCategoryGeneral.removePreference(mCombinedBarAutoHide);
         }
     }
 
     @Override
     public boolean onPreferenceClick(Preference pref) {
         if (pref == mResetColor) {
             int color = 0xFF000000;
             Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BACKGROUND_COLOR, color);
            mStatusbarBgColor.onColorChanged(color);
 
             color = 0xFF33B5E5;
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_COLOR, color);
             mColorPicker.onColorChanged(color);
 
         }
         return false;
     }
 
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mStatusBarAmPm) {
             int statusBarAmPm = Integer.valueOf((String) newValue);
             int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
             mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
             return true;
         } else if (preference == mStatusBarWeekday) {
             int statusBarWeekday = Integer.valueOf((String) newValue);
             int index = mStatusBarWeekday.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_WEEKDAY, statusBarWeekday);
             mStatusBarWeekday.setSummary(mStatusBarWeekday.getEntries()[index]);
             return true;
         } else if (preference == mStatusBarWeekdayFormat) {
             int statusBarWeekdayFormat = Integer.valueOf((String) newValue);
             int index = mStatusBarWeekdayFormat.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_WEEKDAY_FORMAT, statusBarWeekdayFormat);
             mStatusBarWeekdayFormat.setSummary(mStatusBarWeekdayFormat.getEntries()[index]);
             return true;
         }  else if (preference == mStatusBarDaymonth) {
             int statusBarDaymonth = Integer.valueOf((String) newValue);
             int index = mStatusBarDaymonth.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_DAYMONTH, statusBarDaymonth);
             mStatusBarDaymonth.setSummary(mStatusBarDaymonth.getEntries()[index]);
             return true;
         } else if (preference == mStatusBarBattery) {
             int statusBarBattery = Integer.valueOf((String) newValue);
             int index = mStatusBarBattery.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_BATTERY, statusBarBattery);
             mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
             return true;
         } else if (preference == mStatusBarMaxNotif) {
             int maxNotIcons = Integer.valueOf((String) newValue);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.MAX_NOTIFICATION_ICONS, maxNotIcons);
             return true;
         } else if (preference == mStatusBarColor) {
             String hex = ColorPickerPreference.convertToARGB(
                     Integer.valueOf(String.valueOf(newValue)));
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_COLOR, intHex);
             return true;
          } else if (preference == mStatusBarCmSignal) {
             int signalStyle = Integer.valueOf((String) newValue);
             int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
             mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
             return true;
         } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                     .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_COLOR, intHex);
             return true;
         }
         return false;
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
 
         if (preference == mStatusBarClock) {
             value = mStatusBarClock.isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_CLOCK, value ? 1 : 0);
             mStatusBarCenterClock.setEnabled(value);
             return true;
         } else if (preference == mStatusBarCenterClock) {
             value = mStatusBarCenterClock.isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_CENTER_CLOCK, value ? 1 : 0);
             return true;
         } else if (preference == mStatusBarDoNotDisturb) {
             value = mStatusBarDoNotDisturb.isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_DONOTDISTURB, value ? 1 : 0);
             return true;
         } else if (preference == mStatusBarBrightnessControl) {
             value = mStatusBarBrightnessControl.isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
             return true;
         } else if (preference == mCombinedBarAutoHide) {
             value = mCombinedBarAutoHide.isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.COMBINED_BAR_AUTO_HIDE, value ? 1 : 0);
             return true;
         } else if (preference == mStatusBarNotifCount) {
             value = mStatusBarNotifCount.isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
             return true;
         }
         return false;
     }
 }
