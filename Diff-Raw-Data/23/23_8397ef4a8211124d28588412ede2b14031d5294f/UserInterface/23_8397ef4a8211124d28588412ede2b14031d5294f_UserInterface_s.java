 
 package com.sourcery.magiccontrol.fragments;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 import android.text.Spannable;
 import android.view.LayoutInflater;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.sourcery.magiccontrol.SettingsPreferenceFragment;
 import com.sourcery.magiccontrol.R;
 import com.sourcery.magiccontrol.util.CMDProcessor;
 import com.sourcery.magiccontrol.util.Helpers;
 
 public class UserInterface extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
 
     public static final String TAG = "UserInterface";
 
     private static final String PREF_180 = "rotate_180";
     private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
     private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
     private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
     private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
     private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";
     private static final String PREF_RAM_USAGE_BAR = "ram_usage_bar";
     private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
     private static final String PREF_IME_SWITCHER = "ime_switcher";
     private static final String PREF_ALARM_ENABLE = "alarm";
     private static final String PREF_SHOW_OVERFLOW = "show_overflow";
     private static final String PREF_FORCE_DUAL_PANEL = "force_dualpanel";
     private static final String PREF_USER_MODE_UI = "user_mode_ui";
    //private static final String PREF_HIDE_EXTRAS = "hide_extras";
 
     CheckBoxPreference mAllow180Rotation;
     CheckBoxPreference mStatusBarNotifCount;
     Preference mCustomLabel;
     CheckBoxPreference mUseAltResolver;
     CheckBoxPreference mVibrateOnExpand;
     CheckBoxPreference mRecentKillAll;
     CheckBoxPreference mRamBar;
     SharedPreferences prefs;
     CheckBoxPreference mEnableVolumeOptions;
     CheckBoxPreference mDisableBootAnimation;
     CheckBoxPreference mShowImeSwitcher;
     CheckBoxPreference mAlarm;
     CheckBoxPreference mShowActionOverflow;
     CheckBoxPreference mDualpane;
     ListPreference mUserModeUI;
     CheckBoxPreference mHideExtras;
 
     Random randomGenerator = new Random();
 
     String mCustomLabelText = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitle(R.string.title_ui);
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.prefs_ui);
 
         PreferenceScreen prefs = getPreferenceScreen();
         ContentResolver cr = mContext.getContentResolver();
 
 
         mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
         mEnableVolumeOptions.setChecked(Settings.System.getBoolean(getActivity()
                 .getContentResolver(),
                 Settings.System.ENABLE_VOLUME_OPTIONS, false));
 
         mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
         mAllow180Rotation.setChecked(Settings.System.getInt(mContext
                  .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));
 
         mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
         mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContext
                 .getContentResolver(), Settings.System.STATUSBAR_NOTIF_COUNT,
                 false));
 
         mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
         updateCustomLabelTextSummary();
 
         mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
         mUseAltResolver.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                         Settings.System.ACTIVITY_RESOLVER_USE_ALT, false));
 
         mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
         mShowImeSwitcher.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                          Settings.System.SHOW_STATUSBAR_IME_SWITCHER, true));
 
         mAlarm = (CheckBoxPreference) findPreference(PREF_ALARM_ENABLE);
         mAlarm.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                          Settings.System.STATUSBAR_SHOW_ALARM, 1) == 1);
          
         mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
         mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                 Settings.System.VIBRATE_NOTIF_EXPAND, true));
 
         
         mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
         mRecentKillAll.setChecked(Settings.System.getBoolean(getActivity  ().getContentResolver(),
                 Settings.System.RECENT_KILL_ALL_BUTTON, false));
 
         mRamBar = (CheckBoxPreference) findPreference(PREF_RAM_USAGE_BAR);
         mRamBar.setChecked(Settings.System.getBoolean(getActivity  ().getContentResolver(),
                 Settings.System.RAM_USAGE_BAR, false));
 
         mShowActionOverflow = (CheckBoxPreference) findPreference(PREF_SHOW_OVERFLOW);
         mShowActionOverflow.setChecked((Settings.System.getInt(getActivity().
                         getApplicationContext().getContentResolver(),
                         Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0) == 1));
 
      //  mHideExtras = (CheckBoxPreference) findPreference(PREF_HIDE_EXTRAS);
      //  mHideExtras.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
       //                 Settings.System.HIDE_EXTRAS_SYSTEM_BAR, false));
 
 
         mDualpane = (CheckBoxPreference) findPreference(PREF_FORCE_DUAL_PANEL);
         mDualpane.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                         Settings.System.FORCE_DUAL_PANEL, getResources().getBoolean(
                         com.android.internal.R.bool.preferences_prefer_dual_pane)));
     
         mUserModeUI = (ListPreference) findPreference(PREF_USER_MODE_UI);
         int uiMode = Settings.System.getInt(cr,
			                Settings.System.CURRENT_UI_MODE, 0);
         mUserModeUI.setValue(Integer.toString(Settings.System.getInt(cr,
                 Settings.System.USER_UI_MODE, uiMode)));
         mUserModeUI.setOnPreferenceChangeListener(this);
 
          mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
          mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
                  if (mDisableBootAnimation.isChecked()) {
                      Resources res = mContext.getResources();
                      String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                      int randomInt = randomGenerator.nextInt(insults.length);
                      mDisableBootAnimation.setSummary(insults[randomInt]);
                  }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
             final Preference preference) {
        if (preference == mEnableVolumeOptions) {
 
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.ENABLE_VOLUME_OPTIONS, checked);
            return true;
        } else if (preference == mAllow180Rotation) {
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.ACCELEROMETER_ROTATION_ANGLES, checked ? (1 | 2 | 4 | 8) : (1 | 2 | 8 ));
             return true;
         } else if (preference == mStatusBarNotifCount) {
             Settings.System.putBoolean(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_NOTIF_COUNT,
                     ((CheckBoxPreference) preference).isChecked());
             return true;
      //  } else if (preference == mHideExtras) {
       //     Settings.System.putBoolean(mContext.getContentResolver(),
       //             Settings.System.HIDE_EXTRAS_SYSTEM_BAR,
        //            ((CheckBoxPreference) preference).isChecked());
        //    return true;
         } else if (preference == mShowImeSwitcher) {
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.SHOW_STATUSBAR_IME_SWITCHER,
                     isCheckBoxPreferenceChecked(preference));
             return true;
         } else if (preference == mAlarm) {
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                      Settings.System.STATUSBAR_SHOW_ALARM, checked ? 1 : 0);
         } else if (preference == mCustomLabel) {
             AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
 
             alert.setTitle(R.string.custom_carrier_label_title);
             alert.setMessage(R.string.custom_carrier_label_explain);
 
             // Set an EditText view to get user input
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
                     i.setAction("com.sourcery.magiccontrol.LABEL_CHANGED");
                     mContext.sendBroadcast(i);
                 }
             });
             alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     // Canceled.
                 }
             });
 
             alert.show();
         } else if (preference == mUseAltResolver) {
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.ACTIVITY_RESOLVER_USE_ALT,
                     isCheckBoxPreferenceChecked(preference));
             return true;
         } else if (preference == mVibrateOnExpand) {
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.VIBRATE_NOTIF_EXPAND,
                     ((CheckBoxPreference) preference).isChecked());
             Helpers.restartSystemUI();
             return true;
         } else if (preference == mRecentKillAll) {
             boolean checked = ((CheckBoxPreference)preference).isChecked();
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.RECENT_KILL_ALL_BUTTON, checked ? true : false);
             return true;
         } else if (preference == mDualpane) {
              Settings.System.putBoolean(mContext.getContentResolver(),
                     Settings.System.FORCE_DUAL_PANEL,
                     ((CheckBoxPreference) preference).isChecked());
             Helpers.restartSystemUI();
             return true;
         } else if (preference == mRamBar) {
             boolean checked = ((CheckBoxPreference)preference).isChecked();
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.RAM_USAGE_BAR, checked ? true : false);
             return true;
          } else if (preference == mShowActionOverflow) {
             boolean enabled = mShowActionOverflow.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                     enabled ? 1 : 0);
             // Show toast appropriately
             if (enabled) {
                 Toast.makeText(getActivity(), R.string.show_overflow_toast_enable,
                         Toast.LENGTH_LONG).show();
             } else {
                 Toast.makeText(getActivity(), R.string.show_overflow_toast_disable,
                         Toast.LENGTH_LONG).show();
             }
             return true;
         } else if (preference == mDisableBootAnimation) {
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             if (checked) {
                 Helpers.getMount("rw");
                 new CMDProcessor().su
                         .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.sourcery");
                 Helpers.getMount("ro");
                 Resources res = mContext.getResources();
                 String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                 int randomInt = randomGenerator.nextInt(insults.length);
                 preference.setSummary(insults[randomInt]);
             } else {
                 Helpers.getMount("rw");
                 new CMDProcessor().su
                         .runWaitFor("mv /system/media/bootanimation.sourcery /system/media/bootanimation.zip");
                 Helpers.getMount("ro");
                 preference.setSummary("");
             }
             return true;
             }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
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
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
          if (preference == mUserModeUI) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.USER_UI_MODE, Integer.parseInt((String) newValue));
             Helpers.restartSystemUI();
             return true;
         }
         return false;
     }
 }
