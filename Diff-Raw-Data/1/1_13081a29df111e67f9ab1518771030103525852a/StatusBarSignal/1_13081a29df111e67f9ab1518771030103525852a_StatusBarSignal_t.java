 package com.revolt.control.fragments;
 
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import com.revolt.control.ReVoltPreferenceFragment;
 import com.revolt.control.R;
 import com.revolt.control.util.Helpers;
 import net.margaritov.preference.colorpicker.ColorPickerPreference;
 
 public class StatusBarSignal extends ReVoltPreferenceFragment implements
         OnPreferenceChangeListener {
 
     ListPreference mDbmStyletyle;
     ListPreference mWifiStyle;
     ColorPickerPreference mColorPicker;
     ColorPickerPreference mWifiColorPicker;
     CheckBoxPreference mHideSignal;
     CheckBoxPreference mAltSignal;
     CheckBoxPreference mStatusBarTraffic;
     CheckBoxPreference mSMSBreath;
     CheckBoxPreference mMissedCallBreath;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitle(R.string.title_statusbar_signal);
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.prefs_statusbar_signal);
 
         PreferenceScreen prefs = getPreferenceScreen();
 
         mDbmStyletyle = (ListPreference) findPreference("signal_style");
         mDbmStyletyle.setOnPreferenceChangeListener(this);
         mDbmStyletyle.setValue(Integer.toString(Settings.System.getInt(mContentRes,
                 Settings.System.STATUSBAR_SIGNAL_TEXT, 0)));
 
         mColorPicker = (ColorPickerPreference) findPreference("signal_color");
         mColorPicker.setOnPreferenceChangeListener(this);
 
         mWifiStyle = (ListPreference) findPreference("wifi_signal_style");
         mWifiStyle.setOnPreferenceChangeListener(this);
         mWifiStyle.setValue(Integer.toString(Settings.System.getInt(mContentRes,
                 Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, 0)));
 
         mWifiColorPicker = (ColorPickerPreference) findPreference("wifi_signal_color");
         mWifiColorPicker.setOnPreferenceChangeListener(this);
 
         mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
         mHideSignal.setChecked(Settings.System.getBoolean(mContentRes,
                 Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, false));
 
         mAltSignal = (CheckBoxPreference) findPreference("alt_signal");
         mAltSignal.setChecked(Settings.System.getBoolean(mContentRes,
                 Settings.System.STATUSBAR_SIGNAL_CLUSTER_ALT, false));
 
         mStatusBarTraffic = (CheckBoxPreference) findPreference("status_bar_traffic");
         mStatusBarTraffic.setChecked(Settings.System.getBoolean(mContentRes,
                 Settings.System.STATUS_BAR_TRAFFIC, false));
 
         mSMSBreath = (CheckBoxPreference) findPreference("sms_breath");
         mSMSBreath.setChecked(Settings.System.getInt(mContentRes,
                 Settings.System.SMS_BREATH, 1) == 1);
 
         mMissedCallBreath = (CheckBoxPreference) findPreference("missed_call_breath");
         mMissedCallBreath.setChecked(Settings.System.getBoolean(mContentRes,
                 Settings.System.MISSED_CALL_BREATH, false));
 
         if (Integer.parseInt(mDbmStyletyle.getValue()) == 0) {
             mColorPicker.setEnabled(false);
             mColorPicker.setSummary(R.string.enable_signal_text);
         }
 
         if (Integer.parseInt(mWifiStyle.getValue()) == 0) {
             mWifiColorPicker.setEnabled(false);
             mWifiColorPicker.setSummary(R.string.enable_wifi_text);
         }
 
         if (!hasPhoneAbility(mContext)) {
             prefs.removePreference(mDbmStyletyle);
             prefs.removePreference(mColorPicker);
             prefs.removePreference(mHideSignal);
             prefs.removePreference(mAltSignal);
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                          Preference preference) {
         if (preference == mHideSignal) {
             Settings.System.putBoolean(mContentRes,
                     Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, mHideSignal.isChecked());
 
             return true;
         } else if (preference == mAltSignal) {
             Settings.System.putBoolean(mContentRes,
                     Settings.System.STATUSBAR_SIGNAL_CLUSTER_ALT, mAltSignal.isChecked());
             return true;
          } else if (preference == mStatusBarTraffic) {
              Settings.System.putBoolean(mContentRes,
                     Settings.System.STATUS_BAR_TRAFFIC, mStatusBarTraffic.isChecked());
             return true;
          } else if (preference == mSMSBreath) {
              Settings.System.putInt(mContentRes,
                     Settings.System.SMS_BREATH, mSMSBreath.isChecked() ? 1 : 0);
            return true;
          } else if (preference == mMissedCallBreath) {
              Settings.System.putBoolean(mContentRes,
                     Settings.System.MISSED_CALL_BREATH, mMissedCallBreath.isChecked());
            return true;
         }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mDbmStyletyle) {
 
             int val = Integer.parseInt((String) newValue);
             Settings.System.putInt(mContentRes,
                     Settings.System.STATUSBAR_SIGNAL_TEXT, val);
             mColorPicker.setEnabled(val == 0 ? false : true);
             if (val == 0) {
                 mColorPicker.setSummary(R.string.enable_signal_text);
             } else {
                 mColorPicker.setSummary(null);
             }
             Helpers.restartSystemUI();
             return true;
         } else if (preference == mColorPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
 
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(mContentRes,
                     Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);
             return true;
         } else if (preference == mWifiStyle) {
 
             int val = Integer.parseInt((String) newValue);
             Settings.System.putInt(mContentRes,
                     Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, val);
             mWifiColorPicker.setEnabled(val == 0 ? false : true);
             if (val == 0) {
                 mWifiColorPicker.setSummary(R.string.enable_wifi_text);
             } else {
                 mWifiColorPicker.setSummary(null);
             }
             Helpers.restartSystemUI();
             return true;
         } else if (preference == mWifiColorPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
 
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(mContentRes,
                     Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR, intHex);
             return true;
         }
         return false;
     }
 }
