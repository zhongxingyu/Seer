 /*
  * Copyright (C) 2012 The Carbon Project
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
 
 package com.android.settings.carbon;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.SystemProperties;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.text.Spannable;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.android.settings.R;
 import com.android.settings.Utils;
 import com.android.settings.util.Helpers;
 import com.android.settings.util.CMDProcessor;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.widgets.AlphaSeekBar;
 
 public class InterfaceSettings extends SettingsPreferenceFragment
 			implements Preference.OnPreferenceChangeListener {
 
     public static final String TAG = "InterfaceSettings";
     private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
     private static final String KEY_RECENTS_RAM_BAR = "recents_ram_bar";
     private static final String KEY_DUAL_PANE = "dual_pane";
     private static final String PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED = "wakeup_when_plugged_unplugged";
     private static final String PREF_NOTIFICATION_SHOW_WIFI_SSID = "notification_show_wifi_ssid";
     private static final String PREF_POWER_CRT_SCREEN_ON = "system_power_crt_screen_on";
     private static final String PREF_POWER_CRT_SCREEN_OFF = "system_power_crt_screen_off";
     private static final String PREF_USER_MODE_UI = "user_mode_ui";
     private static final String PREF_HIDE_EXTRAS = "hide_extras";
     private static final String KEY_LOW_BATTERY_WARNING_POLICY = "pref_low_battery_warning_policy";
 
     Preference mCustomLabel;
     Preference mRamBar;
     Preference mLcdDensity;
     CheckBoxPreference mDualPane;
     CheckBoxPreference mWakeUpWhenPluggedOrUnplugged;
     CheckBoxPreference mShowWifiName;
     CheckBoxPreference mCrtOff;
     CheckBoxPreference mCrtOn;
     CheckBoxPreference mHideExtras;
     ListPreference mUserModeUI;
     Context mContext;
     ListPreference mLowBatteryWarning;
 
     Random randomGenerator = new Random();
 
     String mCustomLabelText = null;
 
     int newDensityValue;
     DensityChanger densityFragment;
     Configuration mCurConfig = new Configuration();
 
     private boolean isCrtOffChecked = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mContext = getActivity();
 
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.interface_settings);
         PreferenceScreen prefSet = getPreferenceScreen();
         ContentResolver cr = mContext.getContentResolver();
 
        // respect device default configuration
         // true fades while false animates
         boolean electronBeamFadesConfig = mContext.getResources().getBoolean(
                 com.android.internal.R.bool.config_animateScreenLights);
 
         // use this to enable/disable crt on feature
         // crt only works if crt off is enabled
         // total system failure if only crt on is enabled
         isCrtOffChecked = Settings.System.getInt(cr,
                 Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                 electronBeamFadesConfig ? 0 : 1) == 1;
 
         mCrtOff = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_OFF);
         mCrtOff.setChecked(isCrtOffChecked);
         mCrtOff.setOnPreferenceChangeListener(this);
 
         mCrtOn = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_ON);
         mCrtOn.setChecked(Settings.System.getInt(cr,
                 Settings.System.SYSTEM_POWER_ENABLE_CRT_ON, 0) == 1);
         mCrtOn.setEnabled(isCrtOffChecked);
         mCrtOn.setOnPreferenceChangeListener(this);
 
         mLcdDensity = findPreference("lcd_density_setup");
         String currentProperty = SystemProperties.get("ro.sf.lcd_density");
         try {
             newDensityValue = Integer.parseInt(currentProperty);
         } catch (Exception e) {
             getPreferenceScreen().removePreference(mLcdDensity);
         }
 
         mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + currentProperty);
 
         mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
         updateCustomLabelTextSummary();
 
         mRamBar = findPreference(KEY_RECENTS_RAM_BAR);
         updateRamBar();
 
         mHideExtras = (CheckBoxPreference) findPreference(PREF_HIDE_EXTRAS);
         mHideExtras.setChecked(Settings.System.getBoolean(cr,
                         Settings.System.HIDE_EXTRAS_SYSTEM_BAR, false));
 
         mUserModeUI = (ListPreference) findPreference(PREF_USER_MODE_UI);
         int uiMode = Settings.System.getInt(cr,
                 Settings.System.CURRENT_UI_MODE, 0);
         mUserModeUI.setValue(Integer.toString(Settings.System.getInt(cr,
                 Settings.System.USER_UI_MODE, uiMode)));
         mUserModeUI.setOnPreferenceChangeListener(this);
 
         mDualPane = (CheckBoxPreference) findPreference(KEY_DUAL_PANE);
         boolean preferDualPane = getResources().getBoolean(
                 com.android.internal.R.bool.preferences_prefer_dual_pane);
         boolean dualPaneMode = Settings.System.getInt(cr,
                 Settings.System.DUAL_PANE_PREFS, (preferDualPane ? 1 : 0)) == 1;
         mDualPane.setChecked(dualPaneMode);
 
         mLowBatteryWarning = (ListPreference) findPreference(KEY_LOW_BATTERY_WARNING_POLICY);
         int lowBatteryWarning = Settings.System.getInt(getActivity().getContentResolver(),
                                     Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY, 0);
         mLowBatteryWarning.setValue(String.valueOf(lowBatteryWarning));
         mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntry());
         mLowBatteryWarning.setOnPreferenceChangeListener(this);
 
         mWakeUpWhenPluggedOrUnplugged = (CheckBoxPreference) findPreference(PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED);
         mWakeUpWhenPluggedOrUnplugged.setChecked(Settings.System.getBoolean(cr,
                         Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED, true));
 
         // hide option if device is already set to never wake up
         if(!mContext.getResources().getBoolean(
                 com.android.internal.R.bool.config_unplugTurnsOnScreen)) {
            ((PreferenceGroup) findPreference("advanced_options")).removePreference(mWakeUpWhenPluggedOrUnplugged);
         }
 
         mShowWifiName = (CheckBoxPreference) findPreference(PREF_NOTIFICATION_SHOW_WIFI_SSID);
         mShowWifiName.setChecked(Settings.System.getInt(cr,
                 Settings.System.NOTIFICATION_SHOW_WIFI_SSID, 0) == 1);
 
         PackageManager pm = getPackageManager();
         boolean isMobileData = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
 
         if (!Utils.isPhone(getActivity()) || !isMobileData) {
             // Nothing for tablets, large screen devices and non Wifi devices remove options
             getPreferenceScreen().removePreference(mShowWifiName);
         }
 
         setHasOptionsMenu(true);
     }
 
     private void updateCustomLabelTextSummary() {
         mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                 Settings.System.CUSTOM_CARRIER_LABEL);
         if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
             mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
         } else {
             mCustomLabel.setSummary(mCustomLabelText);
         }
     }
 
     private void updateRamBar() {
         int ramBarMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.RECENTS_RAM_BAR_MODE, 0);
         if (ramBarMode != 0)
             mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_enabled));
         else
             mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_disabled));
     }
 
     private void openTransparencyDialog() {
         getFragmentManager().beginTransaction().add(new AdvancedTransparencyDialog(), null)
                 .commit();
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         if (preference == mCustomLabel) {
             AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
             alert.setTitle(R.string.custom_carrier_label_title);
             alert.setMessage(R.string.custom_carrier_label_explain);
             final EditText input = new EditText(getActivity());
             input.setText(mCustomLabelText != null ? mCustomLabelText : "");
             alert.setView(input);
 
             alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String value = ((Spannable) input.getText()).toString();
                     Settings.System.putString(getActivity().getContentResolver(),
                             Settings.System.CUSTOM_CARRIER_LABEL, value);
                     updateCustomLabelTextSummary();
                     Intent i = new Intent();
                     i.setAction("com.android.settings.LABEL_CHANGED");
                     getActivity().getApplicationContext().sendBroadcast(i);
                 }
             });
             alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 }
             });
             alert.show();
         } else if (preference == mLcdDensity) {
             ((PreferenceActivity) getActivity())
             .startPreferenceFragment(new DensityChanger(), true);
             return true;
         } else if (preference == mDualPane) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.DUAL_PANE_PREFS,
                     ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
             return true;
         } else if (preference == mHideExtras) {
             Settings.System.putBoolean(mContext.getContentResolver(),
                     Settings.System.HIDE_EXTRAS_SYSTEM_BAR,
                     ((CheckBoxPreference) preference).isChecked());
             return true;
         } else if (preference.getKey().equals("transparency_dialog")) {
             // getFragmentManager().beginTransaction().add(new
             // TransparencyDialog(), null).commit();
             openTransparencyDialog();
             return true;
         } else if (preference == mWakeUpWhenPluggedOrUnplugged) {
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED,
                     ((CheckBoxPreference) preference).isChecked());
             return true;
         } else if (preference == mShowWifiName) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.NOTIFICATION_SHOW_WIFI_SSID,
                     mShowWifiName.isChecked() ? 1 : 0);
             return true;
         }
 
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         updateRamBar();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         updateRamBar();
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         final String key = preference.getKey();
          if (mCrtOff.equals(preference)) {
             isCrtOffChecked = ((Boolean) newValue).booleanValue();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                     (isCrtOffChecked ? 1 : 0));
             // if crt off gets turned off, crt on gets turned off and disabled
             if (!isCrtOffChecked) {
                 Settings.System.putInt(getActivity().getContentResolver(),
                         Settings.System.SYSTEM_POWER_ENABLE_CRT_ON, 0);
                 mCrtOn.setChecked(false);
             }
             mCrtOn.setEnabled(isCrtOffChecked);
             return true;
         } else if (mCrtOn.equals(preference)) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.SYSTEM_POWER_ENABLE_CRT_ON,
                     ((Boolean) newValue).booleanValue() ? 1 : 0);
             return true;
         } else if (preference == mUserModeUI) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.USER_UI_MODE, Integer.parseInt((String) newValue));
             Helpers.restartSystemUI();
             return true;
         } else if (preference == mLowBatteryWarning) {
             int lowBatteryWarning = Integer.valueOf((String) newValue);
             int index = mLowBatteryWarning.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY,
                     lowBatteryWarning);
             mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntries()[index]);
             return true;
         }
         return false;
     }
 
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.user_interface, menu);
     }
 
     public void copy(File src, File dst) throws IOException {
         InputStream in = new FileInputStream(src);
         FileOutputStream out = new FileOutputStream(dst);
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }
 
     public static class AdvancedTransparencyDialog extends DialogFragment {
 
         private static final int KEYGUARD_ALPHA = 112;
 
         private static final int STATUSBAR_ALPHA = 0;
         private static final int STATUSBAR_KG_ALPHA = 1;
         private static final int NAVBAR_ALPHA = 2;
         private static final int NAVBAR_KG_ALPHA = 3;
 
         boolean linkTransparencies = true;
         CheckBox mLinkCheckBox, mMatchStatusbarKeyguard, mMatchNavbarKeyguard;
         ViewGroup mNavigationBarGroup;
 
         TextView mSbLabel;
 
         AlphaSeekBar mSeekBars[] = new AlphaSeekBar[4];
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setShowsDialog(true);
             setRetainInstance(true);
             linkTransparencies = getSavedLinkedState();
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             View layout = View.inflate(getActivity(), R.layout.dialog_transparency, null);
             mLinkCheckBox = (CheckBox) layout.findViewById(R.id.transparency_linked);
             mLinkCheckBox.setChecked(linkTransparencies);
 
             mNavigationBarGroup = (ViewGroup) layout.findViewById(R.id.navbar_layout);
             mSbLabel = (TextView) layout.findViewById(R.id.statusbar_label);
             mSeekBars[STATUSBAR_ALPHA] = (AlphaSeekBar) layout.findViewById(R.id.statusbar_alpha);
             mSeekBars[STATUSBAR_KG_ALPHA] = (AlphaSeekBar) layout
                     .findViewById(R.id.statusbar_keyguard_alpha);
             mSeekBars[NAVBAR_ALPHA] = (AlphaSeekBar) layout.findViewById(R.id.navbar_alpha);
             mSeekBars[NAVBAR_KG_ALPHA] = (AlphaSeekBar) layout
                     .findViewById(R.id.navbar_keyguard_alpha);
 
             mMatchStatusbarKeyguard = (CheckBox) layout.findViewById(R.id.statusbar_match_keyguard);
             mMatchNavbarKeyguard = (CheckBox) layout.findViewById(R.id.navbar_match_keyguard);
 
             try {
                 // restore any saved settings
                 int alphas[] = new int[2];
                 final String sbConfig = Settings.System.getString(getActivity()
                         .getContentResolver(),
                         Settings.System.STATUS_BAR_ALPHA_CONFIG);
                 if (sbConfig != null) {
                     String split[] = sbConfig.split(";");
                     alphas[0] = Integer.parseInt(split[0]);
                     alphas[1] = Integer.parseInt(split[1]);
 
                     mSeekBars[STATUSBAR_ALPHA].setCurrentAlpha(alphas[0]);
                     mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
 
                     mMatchStatusbarKeyguard.setChecked(alphas[1] == KEYGUARD_ALPHA);
 
                     if (linkTransparencies) {
                         mSeekBars[NAVBAR_ALPHA].setCurrentAlpha(alphas[0]);
                         mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                     } else {
                         final String navConfig = Settings.System.getString(getActivity()
                                 .getContentResolver(),
                                 Settings.System.NAVIGATION_BAR_ALPHA_CONFIG);
                         if (navConfig != null) {
                             split = navConfig.split(";");
                             alphas[0] = Integer.parseInt(split[0]);
                             alphas[1] = Integer.parseInt(split[1]);
                             mSeekBars[NAVBAR_ALPHA].setCurrentAlpha(alphas[0]);
                             mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
 
                             mMatchNavbarKeyguard.setChecked(alphas[1] == KEYGUARD_ALPHA);
                         }
                     }
                 }
             } catch (Exception e) {
                 resetSettings();
             }
 
             updateToggleState();
             mMatchStatusbarKeyguard.setOnCheckedChangeListener(mUpdateStatesListener);
             mMatchNavbarKeyguard.setOnCheckedChangeListener(mUpdateStatesListener);
             mLinkCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     linkTransparencies = isChecked;
                     saveSavedLinkedState(isChecked);
                     updateToggleState();
                 }
             });
 
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             builder.setView(layout);
             builder.setTitle(getString(R.string.transparency_dialog_title));
             builder.setNegativeButton(R.string.cancel, null);
             builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
 
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     if (linkTransparencies) {
                         String config = mSeekBars[STATUSBAR_ALPHA].getCurrentAlpha() + ";" +
                                 mSeekBars[STATUSBAR_KG_ALPHA].getCurrentAlpha();
                         Settings.System.putString(getActivity().getContentResolver(),
                                 Settings.System.STATUS_BAR_ALPHA_CONFIG, config);
                         Settings.System.putString(getActivity().getContentResolver(),
                                 Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, config);
                     } else {
                         String sbConfig = mSeekBars[STATUSBAR_ALPHA].getCurrentAlpha() + ";" +
                                 mSeekBars[STATUSBAR_KG_ALPHA].getCurrentAlpha();
                         Settings.System.putString(getActivity().getContentResolver(),
                                 Settings.System.STATUS_BAR_ALPHA_CONFIG, sbConfig);
 
                         String nbConfig = mSeekBars[NAVBAR_ALPHA].getCurrentAlpha() + ";" +
                                 mSeekBars[NAVBAR_KG_ALPHA].getCurrentAlpha();
                         Settings.System.putString(getActivity().getContentResolver(),
                                 Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, nbConfig);
                     }
                 }
             });
 
             return builder.create();
         }
 
         private void resetSettings() {
             Settings.System.putString(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_ALPHA_CONFIG, null);
             Settings.System.putString(getActivity().getContentResolver(),
                     Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, null);
         }
 
         private void updateToggleState() {
             if (linkTransparencies) {
                 mSbLabel.setText(R.string.transparency_dialog_transparency_sb_and_nv);
                 mNavigationBarGroup.setVisibility(View.GONE);
             } else {
                 mSbLabel.setText(R.string.transparency_dialog_statusbar);
                 mNavigationBarGroup.setVisibility(View.VISIBLE);
             }
 
             mSeekBars[STATUSBAR_KG_ALPHA]
                     .setEnabled(!mMatchStatusbarKeyguard.isChecked());
             mSeekBars[NAVBAR_KG_ALPHA]
                     .setEnabled(!mMatchNavbarKeyguard.isChecked());
 
             // disable keyguard alpha if needed
             if (!mSeekBars[STATUSBAR_KG_ALPHA].isEnabled()) {
                 mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(KEYGUARD_ALPHA);
             }
             if (!mSeekBars[NAVBAR_KG_ALPHA].isEnabled()) {
                 mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(KEYGUARD_ALPHA);
             }
         }
 
         @Override
         public void onDestroyView() {
             if (getDialog() != null && getRetainInstance())
                 getDialog().setDismissMessage(null);
             super.onDestroyView();
         }
 
         private CompoundButton.OnCheckedChangeListener mUpdateStatesListener = new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 updateToggleState();
             }
         };
 
         private boolean getSavedLinkedState() {
             return getActivity().getSharedPreferences("transparency", Context.MODE_PRIVATE)
                     .getBoolean("link", true);
         }
 
         private void saveSavedLinkedState(boolean v) {
             getActivity().getSharedPreferences("transparency", Context.MODE_PRIVATE).edit()
                     .putBoolean("link", v).commit();
         }
     }
 }
