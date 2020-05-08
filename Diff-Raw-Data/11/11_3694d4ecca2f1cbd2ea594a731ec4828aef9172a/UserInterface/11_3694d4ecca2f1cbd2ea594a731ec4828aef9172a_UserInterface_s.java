 
 package com.sourcery.magiccontrol.fragments;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.SystemProperties;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.text.Spannable;
 import android.widget.EditText;
 
 import com.sourcery.magiccontrol.SettingsPreferenceFragment;
 import com.sourcery.magiccontrol.R;
 import com.sourcery.magiccontrol.util.CMDProcessor;
 import com.sourcery.magiccontrol.util.Helpers;
 
 public class UserInterface extends SettingsPreferenceFragment {
 
     public static final String TAG = "UserInterface";
 
     private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
     private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
     private static final String PREF_180 = "rotate_180";
     private static final String PREF_IME_SWITCHER = "ime_switcher";
     private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
 
     CheckBoxPreference mEnableVolumeOptions;
     CheckBoxPreference mDisableBootAnimation;
     CheckBoxPreference mStatusBarNotifCount;
     CheckBoxPreference mAllow180Rotation;
     CheckBoxPreference mShowImeSwitcher;
     Preference mCustomLabel;
 
      Random randomGenerator = new Random();
 
      String mCustomLabelText = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.prefs_ui);
 
         mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
         mEnableVolumeOptions.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.ENABLE_VOLUME_OPTIONS, 0) == 1);
 
         mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
         mStatusBarNotifCount.setChecked(Settings.System.getInt(mContext
                 .getContentResolver(), Settings.System.STATUS_BAR_NOTIF_COUNT,
                  0) == 1);
 
         mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
         mShowImeSwitcher.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_IME_SWITCHER, 1) == 1);
 
         mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
         mAllow180Rotation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));
 
 
         mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
         mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
                 if (mDisableBootAnimation.isChecked()) {
                     Resources res = mContext.getResources();
                     String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                     int randomInt = randomGenerator.nextInt(insults.length);
                     mDisableBootAnimation.setSummary(insults[randomInt]);
                  }
          mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
          updateCustomLabelTextSummary();
  	    
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
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
             Preference preference) {
          if (preference == mEnableVolumeOptions) {
 
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.ENABLE_VOLUME_OPTIONS, checked ? 1 : 0);
             return true;
 
           } else if (preference == mStatusBarNotifCount) {
  	     Settings.System.putInt(mContext.getContentResolver(),
                      Settings.System.STATUS_BAR_NOTIF_COUNT,
  	             ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
             return true;
 
           } else if (preference == mShowImeSwitcher) {
 
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_IME_SWITCHER, checked ? 1 : 0);
             return true;
 
           } else if (preference == mAllow180Rotation) {
 
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.ACCELEROMETER_ROTATION_ANGLES, checked ? (1 | 2 | 4 | 8)
                             : (1 | 2 | 8));
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
                    i.setAction("com.aokp.romcontrol.LABEL_CHANGED");
                     mContext.sendBroadcast(i);
                 }
             });
             alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     // Canceled.
                 }
             });
 
             alert.show();
             }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 }
