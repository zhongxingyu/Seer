 package com.droidconcepts.settings.activities;
 
 import com.droidconcepts.settings.R;
 
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Parcel;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 
 
public class UserInterface extends PreferenceActivity {
 	
     /* Preference Screens */
     private static final String BATTERY_CLOCK_SCREEN = "battery_clock_settings";
     private static final String DATE_PROVIDER_SCREEN = "date_provider_settings";
     private static final String NOTIFICATION_SCREEN = "notification_settings";
     private static final String EXTRAS_SCREEN = "extra_tweaks";
     private static final String ROTATION_90_PREF = "pref_rotation_90";
     private static final String ROTATION_180_PREF = "pref_rotation_180";
     private static final String ROTATION_270_PREF = "pref_rotation_270";
     private static final String UI_EXP_WIDGET = "expanded_widget";
     private static final String UI_EXP_WIDGET_COLOR = "expanded_color_mask";
     private static final String UI_EXP_WIDGET_PICKER = "widget_picker";
     private static final String UI_EXP_WIDGET_HIDE_ONCHANGE = "expanded_hide_onchange";
     private static final String LOCKSCREEN_TIMEOUT_DELAY_PREF = "pref_lockscreen_timeout_delay";
     private static final String LOCKSCREEN_SCREENOFF_DELAY_PREF = "pref_lockscreen_screenoff_delay";
 	
     private PreferenceScreen mBatteryClockScreen;
     private PreferenceScreen mDateProviderScreen;
     private PreferenceScreen mNotificationScreen;
     private PreferenceScreen mExtrasScreen;
     private CheckBoxPreference mRotation90Pref;
     private CheckBoxPreference mRotation180Pref;
     private CheckBoxPreference mRotation270Pref;
     private CheckBoxPreference mPowerWidget;
     private Preference mPowerWidgetColor;
     private PreferenceScreen mPowerPicker;
     private CheckBoxPreference mPowerWidgetHideOnChange;
     private ListPreference mScreenLockTimeoutDelayPref;
     private ListPreference mScreenLockScreenOffDelayPref;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(R.string.ui_title);
         addPreferencesFromResource(R.xml.ui_settings);
 
         PreferenceScreen prefSet = getPreferenceScreen();
         
         /* Preference Screens */
         mBatteryClockScreen = (PreferenceScreen) prefSet.findPreference(BATTERY_CLOCK_SCREEN);
         mDateProviderScreen = (PreferenceScreen) prefSet.findPreference(DATE_PROVIDER_SCREEN);
         mNotificationScreen = (PreferenceScreen) prefSet.findPreference(NOTIFICATION_SCREEN);
         mExtrasScreen = (PreferenceScreen) prefSet.findPreference(EXTRAS_SCREEN);
         
         /* Rotation */
         mRotation90Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_90_PREF);
         mRotation180Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_180_PREF);
         mRotation270Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_270_PREF);
         int mode = Settings.System.getInt(getContentResolver(),
                         Settings.System.ACCELEROMETER_ROTATION_MODE, 5);
         mRotation90Pref.setChecked((mode & 1) != 0);
         mRotation180Pref.setChecked((mode & 2) != 0);
         mRotation270Pref.setChecked((mode & 4) != 0);
 
         /* Screen Lock */
         mScreenLockTimeoutDelayPref = (ListPreference) prefSet.findPreference(LOCKSCREEN_TIMEOUT_DELAY_PREF);
         int timeoutDelay = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_LOCK_TIMEOUT_DELAY, 5000);
         mScreenLockTimeoutDelayPref.setValue(String.valueOf(timeoutDelay));
         mScreenLockTimeoutDelayPref.setOnPreferenceChangeListener(this);
 
         mScreenLockScreenOffDelayPref = (ListPreference) prefSet.findPreference(LOCKSCREEN_SCREENOFF_DELAY_PREF);
         int screenOffDelay = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_LOCK_SCREENOFF_DELAY, 0);
         mScreenLockScreenOffDelayPref.setValue(String.valueOf(screenOffDelay)); 
         mScreenLockScreenOffDelayPref.setOnPreferenceChangeListener(this);
 
         /* Expanded View Power Widget */
         mPowerWidget = (CheckBoxPreference) prefSet.findPreference(UI_EXP_WIDGET);
         mPowerWidgetColor = prefSet.findPreference(UI_EXP_WIDGET_COLOR);
         mPowerPicker = (PreferenceScreen)prefSet.findPreference(UI_EXP_WIDGET_PICKER);
         mPowerWidgetHideOnChange = (CheckBoxPreference)
                 prefSet.findPreference(UI_EXP_WIDGET_HIDE_ONCHANGE);
         mPowerWidgetHideOnChange.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1));
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         
         if (preference == mBatteryClockScreen) {
         	startActivity(mBatteryClockScreen.getIntent());
         }
         if (preference == mDateProviderScreen) {
         	startActivity(mDateProviderScreen.getIntent());
         }
         if (preference == mNotificationScreen) {
         	startActivity(mNotificationScreen.getIntent());
         }
         if (preference == mExtrasScreen) {
         	startActivity(mExtrasScreen.getIntent());
         }
         if(preference == mPowerPicker) {
             startActivity(mPowerPicker.getIntent());
         }
         if (preference == mRotation90Pref ||
             preference == mRotation180Pref ||
             preference == mRotation270Pref) {
             int mode = 0;
             if (mRotation90Pref.isChecked()) mode |= 1;
             if (mRotation180Pref.isChecked()) mode |= 2;
             if (mRotation270Pref.isChecked()) mode |= 4;
             Settings.System.putInt(getContentResolver(),
                      Settings.System.ACCELEROMETER_ROTATION_MODE, mode);
         }
         if(preference == mPowerWidget) {
             value = mPowerWidget.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.EXPANDED_VIEW_WIDGET, value ? 1 : 0);
         }
 
         if (preference == mPowerWidgetColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this,
                 mWidgetColorListener,
                 readWidgetColor());
             cp.show();
         }
 
         if(preference == mPowerWidgetHideOnChange) {
             value = mPowerWidgetHideOnChange.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.EXPANDED_HIDE_ONCHANGE, value ? 1 : 0);
         }
 
         return true;
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mScreenLockTimeoutDelayPref) {
             int timeoutDelay = Integer.valueOf((String)newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_LOCK_TIMEOUT_DELAY, timeoutDelay);
             return true;
         } else if (preference == mScreenLockScreenOffDelayPref) {
             int screenOffDelay = Integer.valueOf((String)newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_LOCK_SCREENOFF_DELAY, screenOffDelay);
             return true;
 
         }
         return false;
     }
 
     private int readWidgetColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_COLOR);
         }
         catch (SettingNotFoundException e) {
             return -16777216;
         }
     }
     ColorPickerDialog.OnColorChangedListener mWidgetColorListener =
         new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 }
