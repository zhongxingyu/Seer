 
 package com.android.settings.fnv;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.text.Spannable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 import com.android.settings.util.Helpers;
 import com.android.settings.util.ShortcutPickerHelper;
 
 import net.margaritov.preference.colorpicker.ColorPickerPreference;
 
public class StatusBar extends SettingsPreferenceFragment  implements
                ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {
 
     private static final String PREF_COLOR_PICKER = "clock_color";
     private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";
     private static final String PREF_CLOCK_WEEKDAY = "clock_weekday";
     private static final String PREF_ENABLE = "clock_style";
     private static final String PREF_BATT_ICON = "battery_icon_list";
     private static final String PREF_BATT_BAR = "battery_bar_list";
     private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
     private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
     private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
     private static final String PREF_BATT_ANIMATE = "battery_bar_animate";
     private static final String PREF_STATUSBAR_BACKGROUND_STYLE = "statusbar_background_style";
     private static final String PREF_STATUSBAR_BACKGROUND_COLOR = "statusbar_background_color";
     private static final String PREF_CLOCK_SHORTCLICK = "clock_shortclick";
     private static final String PREF_CLOCK_LONGCLICK = "clock_longclick";
     private static final String PREF_CLOCK_DOUBLECLICK = "clock_doubleclick";
 
     private int shortClick = 0;
     private int longClick = 1;
     private int doubleClick = 2;
 
     private ColorPickerPreference mStatusbarBgColor;
     private ColorPickerPreference mColorPicker;
     private ColorPickerPreference mBatteryBarColor;
     private ColorPickerPreference mDbmColorPicker;
     private ColorPickerPreference mWifiColorPicker;
     private ListPreference mStatusbarBgStyle;
     private ListPreference mBatteryIcon;
     private ListPreference mBatteryBar;
     private ListPreference mBatteryBarStyle;
     private ListPreference mBatteryBarThickness;
     private ListPreference mClockStyle;
     private ListPreference mClockAmPmstyle;
     private ListPreference mClockWeekday;
     private ListPreference mClockShortClick;
     private ListPreference mClockLongClick;
     private ListPreference mClockDoubleClick;
     private ListPreference mDbmStyletyle;
     private ListPreference mWifiStyle;
     private CheckBoxPreference mBatteryBarChargingAnimation;
     private CheckBoxPreference mHideSignal;
 
     private int seekbarProgress;
 
     private ShortcutPickerHelper mPicker;
     private Preference mPreference;
     private String mString;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.status_bar_settings);
 
         mPicker = new ShortcutPickerHelper(this, this);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mColorPicker = (ColorPickerPreference) prefSet.findPreference(PREF_COLOR_PICKER);
         mColorPicker.setOnPreferenceChangeListener(this);
 
         mDbmStyletyle = (ListPreference) findPreference("signal_style");
         mDbmStyletyle.setOnPreferenceChangeListener(this);
         mDbmStyletyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_SIGNAL_TEXT,
                 0)));
 
         mDbmColorPicker = (ColorPickerPreference) findPreference("signal_color");
         mDbmColorPicker.setOnPreferenceChangeListener(this);
         mWifiStyle = (ListPreference) findPreference("wifi_signal_style");
         mWifiStyle.setOnPreferenceChangeListener(this);
         mWifiStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT,
                 0)));
 
         mWifiColorPicker = (ColorPickerPreference) findPreference("wifi_signal_color");
         mWifiColorPicker.setOnPreferenceChangeListener(this);
 
         mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
         mHideSignal.setChecked(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
                 0) != 0);
 
         mClockStyle = (ListPreference) prefSet.findPreference(PREF_ENABLE);
         mClockStyle.setOnPreferenceChangeListener(this);
         mClockStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_CLOCK_STYLE,
                 1)));
 
         mClockAmPmstyle = (ListPreference) prefSet.findPreference(PREF_AM_PM_STYLE);
         mClockAmPmstyle.setOnPreferenceChangeListener(this);
         mClockAmPmstyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                 2)));
 
         mClockWeekday = (ListPreference) prefSet.findPreference(PREF_CLOCK_WEEKDAY);
         mClockWeekday.setOnPreferenceChangeListener(this);
         mClockWeekday.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_CLOCK_WEEKDAY,
                 0)));
 
         mBatteryIcon = (ListPreference) prefSet.findPreference(PREF_BATT_ICON);
         mBatteryIcon.setOnPreferenceChangeListener(this);
         mBatteryIcon.setValue((Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_BATTERY_ICON,
                 0))
                 + "");
 
         mBatteryBar = (ListPreference) prefSet.findPreference(PREF_BATT_BAR);
         mBatteryBar.setOnPreferenceChangeListener(this);
         mBatteryBar.setValue((Settings.System
                 .getInt(getActivity().getContentResolver(),
                         Settings.System.STATUSBAR_BATTERY_BAR, 0))
                 + "");
 
         mBatteryBarStyle = (ListPreference) prefSet.findPreference(PREF_BATT_BAR_STYLE);
         mBatteryBarStyle.setOnPreferenceChangeListener(this);
         mBatteryBarStyle.setValue((Settings.System.getInt(getActivity()
                 .getContentResolver(),
                 Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0))
                 + "");
 
         mBatteryBarColor = (ColorPickerPreference) prefSet.findPreference(PREF_BATT_BAR_COLOR);
         mBatteryBarColor.setOnPreferenceChangeListener(this);
 
         mBatteryBarChargingAnimation = (CheckBoxPreference) prefSet.findPreference(PREF_BATT_ANIMATE);
         mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(
                 getActivity().getContentResolver(),
                 Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);
 
         mBatteryBarThickness = (ListPreference) prefSet.findPreference(PREF_BATT_BAR_WIDTH);
         mBatteryBarThickness.setOnPreferenceChangeListener(this);
         mBatteryBarThickness.setValue((Settings.System.getInt(getActivity()
                 .getContentResolver(),
                 Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1))
                 + "");
 
         mStatusbarBgColor = (ColorPickerPreference) prefSet.findPreference(PREF_STATUSBAR_BACKGROUND_COLOR);
         mStatusbarBgColor.setOnPreferenceChangeListener(this);
 
         mStatusbarBgStyle = (ListPreference) prefSet.findPreference(PREF_STATUSBAR_BACKGROUND_STYLE);
         mStatusbarBgStyle.setOnPreferenceChangeListener(this);
 
         mClockShortClick = (ListPreference) findPreference(PREF_CLOCK_SHORTCLICK);
         mClockShortClick.setOnPreferenceChangeListener(this);
         mClockShortClick.setSummary(getProperSummary(mClockShortClick));
 
         mClockLongClick = (ListPreference) findPreference(PREF_CLOCK_LONGCLICK);
         mClockLongClick.setOnPreferenceChangeListener(this);
         mClockLongClick.setSummary(getProperSummary(mClockLongClick));
 
         mClockDoubleClick = (ListPreference) findPreference(PREF_CLOCK_DOUBLECLICK);
         mClockDoubleClick.setOnPreferenceChangeListener(this);
         mClockDoubleClick.setSummary(getProperSummary(mClockDoubleClick));
 
         updateVisibility();
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
         if (preference == mBatteryBarChargingAnimation) {
 
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE,
                     ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
             return true;
         } else if (preference == mHideSignal) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
                     ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
 
             return true;
         }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         boolean result = false;
 
         if (preference == mDbmStyletyle) {
 
             int val = Integer.parseInt((String) newValue);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_SIGNAL_TEXT, val);
             return true;
         } else if (preference == mDbmColorPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
 
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);
 
             return true;
         } else if (preference == mWifiStyle) {
 
             int val = Integer.parseInt((String) newValue);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, val);
             return true;
         } else if (preference == mWifiColorPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
 
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR, intHex);
 
             return true;
         } else if (preference == mClockAmPmstyle) {
             int val = Integer.parseInt((String) newValue);
             int index = mClockAmPmstyle.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, val);
             mClockAmPmstyle.setSummary(mClockAmPmstyle.getEntries()[index]);
             return true;
         } else if (preference == mBatteryIcon) {
 
             int val = Integer.parseInt((String) newValue);
             return Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_ICON, val);
         } else if (preference == mClockStyle) {
             int val = Integer.parseInt((String) newValue);
             int index = mClockStyle.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_STYLE, val);
             mClockStyle.setSummary(mClockStyle.getEntries()[index]);
             return true;
         } else if (preference == mClockWeekday) {
             int val = Integer.parseInt((String) newValue);
             int index = mClockWeekday.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_WEEKDAY, val);
             mClockWeekday.setSummary(mClockStyle.getEntries()[index]);
             return true;
         } else if (preference == mColorPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
 
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_COLOR, intHex);
             Log.e("FNV", intHex + "");
         } else if (preference == mBatteryBarColor) {
             String hex = ColorPickerPreference.convertToARGB(Integer
                     .valueOf(String.valueOf(newValue)));
             preference.setSummary(hex);
 
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
             return true;
         } else if (preference == mBatteryBar) {
 
             int val = Integer.parseInt((String) newValue);
             return Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_BAR, val);
         } else if (preference == mBatteryBarStyle) {
 
             int val = Integer.parseInt((String) newValue);
             return Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);
         } else if (preference == mBatteryBarThickness) {
 
             int val = Integer.parseInt((String) newValue);
             return Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
 
         } else if (preference == mStatusbarBgStyle) {
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
             Log.e("BAKED", intHex + "");
         } else if (preference == mClockShortClick) {
             mPreference = preference;
             mString = Settings.System.NOTIFICATION_CLOCK[shortClick];
             if (newValue.equals("**app**")) {
                 mPicker.pickShortcut();
             } else {
                 result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK[shortClick], (String) newValue);
                 mClockShortClick.setSummary(getProperSummary(mClockShortClick));
             }
         } else if (preference == mClockLongClick) {
             mPreference = preference;
             mString = Settings.System.NOTIFICATION_CLOCK[longClick];
             if (newValue.equals("**app**")) {
                 mPicker.pickShortcut();
             } else {
                 result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK[longClick], (String) newValue);
                 mClockLongClick.setSummary(getProperSummary(mClockLongClick));
             }
         } else if (preference == mClockDoubleClick) {
             mPreference = preference;
             mString = Settings.System.NOTIFICATION_CLOCK[doubleClick];
             if (newValue.equals("**app**")) {
                 mPicker.pickShortcut();
             } else {
                 result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK[doubleClick], (String) newValue);
                 mClockDoubleClick.setSummary(getProperSummary(mClockDoubleClick));
             }
         }
         return result;
     }
     public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
           mPreference.setSummary(friendlyName);
           Settings.System.putString(getContentResolver(), mString, (String) uri);
     }
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == Activity.RESULT_OK) {
             if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                     || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                     || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                 mPicker.onActivityResult(requestCode, resultCode, data);
             }
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     private String getProperSummary(Preference preference) {
         if (preference == mClockDoubleClick) {
             mString = Settings.System.NOTIFICATION_CLOCK[doubleClick];
         } else if (preference == mClockLongClick) {
             mString = Settings.System.NOTIFICATION_CLOCK[longClick];
         } else if (preference == mClockShortClick) {
             mString = Settings.System.NOTIFICATION_CLOCK[shortClick];
         }
 
         String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
         String empty = "";
 
         if (uri == null)
             return empty;
 
         if (uri.startsWith("**")) {
             if (uri.equals("**alarm**"))
                 return getResources().getString(R.string.alarm);
             else if (uri.equals("**event**"))
                 return getResources().getString(R.string.event);
             else if (uri.equals("**voiceassist**"))
                 return getResources().getString(R.string.voiceassist);
             else if (uri.equals("**clockoptions**"))
                 return getResources().getString(R.string.clock_options);
             else if (uri.equals("**today**"))
                 return getResources().getString(R.string.today);
             else if (uri.equals("**null**"))
                 return getResources().getString(R.string.nothing);
         } else {
             return mPicker.getFriendlyNameForUri(uri);
         }
         return null;
     }
 }
