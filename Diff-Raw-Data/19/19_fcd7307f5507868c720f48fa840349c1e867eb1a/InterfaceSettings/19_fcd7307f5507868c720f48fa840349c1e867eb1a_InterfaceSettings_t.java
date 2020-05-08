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
 
 package com.carbon.settings.fragments;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.FragmentTransaction;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.graphics.Rect;
 import android.net.Uri;
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
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.text.Spannable;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.IWindowManager;
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
 import android.widget.Toast;
 
 import com.carbon.settings.R;
 import com.carbon.settings.Utils;
 import com.carbon.settings.util.Helpers;
 import com.carbon.settings.SettingsPreferenceFragment;
 import com.carbon.settings.widgets.AlphaSeekBar;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class InterfaceSettings extends SettingsPreferenceFragment
 			implements Preference.OnPreferenceChangeListener {
 
     public static final String TAG = "InterfaceSettings";
     private static final String KEY_HARDWARE_KEYS = "hardware_keys";
     private static final String KEY_LOCK_CLOCK = "lock_clock";
     private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
     private static final String KEY_SHOW_OVERFLOW = "show_overflow";
     private static final String KEY_RECENTS_RAM_BAR = "recents_ram_bar";
     private static final String KEY_WAKE_WHEN_PLUGGED_OR_UNPLUGGED = "wake_when_plugged_or_unplugged";
 
     private CheckBoxPreference mShowActionOverflow;
     private CheckBoxPreference mUseAltResolver;
     private Preference mHardwareKeys;
     private Preference mRamBar;
     private CheckBoxPreference mWakeWhenPluggedOrUnplugged;
 
     Context mContext;
 
     Configuration mCurConfig = new Configuration();
 
     private static ContentResolver mContentResolver;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mContext = getActivity();
 
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.interface_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver cr = mContext.getContentResolver();
        mContentResolver = getContentResolver();
        Resources res = getResources();
 
         mHardwareKeys = (Preference) findPreference(KEY_HARDWARE_KEYS);
 
         IWindowManager windowManager = IWindowManager.Stub.asInterface(
                 ServiceManager.getService(Context.WINDOW_SERVICE));
         try {
             if (windowManager.hasNavigationBar()) {
                 prefSet.removePreference(findPreference(KEY_HARDWARE_KEYS));
             } else {
                 // should not need to remove anything
             }
         } catch (RemoteException e) {
             // Do nothing
         }
 
         mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setChecked(Settings.System.getBoolean(cr,
                 Settings.System.ACTIVITY_RESOLVER_USE_ALT, false));
 
         mShowActionOverflow = (CheckBoxPreference) findPreference(KEY_SHOW_OVERFLOW);
        mShowActionOverflow.setChecked(Settings.System.getInt(cr,
                 Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0) == 1);
 
         mRamBar = findPreference(KEY_RECENTS_RAM_BAR);
         updateRamBar();
 
         // Default value for wake-on-plug behavior from config.xml
         boolean wakeUpWhenPluggedOrUnpluggedConfig = res.getBoolean(
                 com.android.internal.R.bool.config_unplugTurnsOnScreen);
 
         mWakeWhenPluggedOrUnplugged =
                 (CheckBoxPreference) findPreference(KEY_WAKE_WHEN_PLUGGED_OR_UNPLUGGED);
        mWakeWhenPluggedOrUnplugged.setChecked(Settings.Global.getInt(cr,
                 Settings.Global.WAKE_WHEN_PLUGGED_OR_UNPLUGGED,
                 (wakeUpWhenPluggedOrUnpluggedConfig ? 1 : 0)) == 1);
 
         // Dont display the lock clock preference if its not installed
         removePreferenceIfPackageNotInstalled(findPreference(KEY_LOCK_CLOCK));
 
         setHasOptionsMenu(true);
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
          return false;
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
         if (preference.getKey().equals("transparency_dialog")) {
             // getFragmentManager().beginTransaction().add(new
             // TransparencyDialog(), null).commit();
             openTransparencyDialog();
             return true;
         } else if (preference == mUseAltResolver) {
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.ACTIVITY_RESOLVER_USE_ALT,
                     ((CheckBoxPreference) preference).isChecked());
             return true;
         } else if (preference == mHardwareKeys) {
             FragmentTransaction ft = getFragmentManager().beginTransaction();
             HardwareKeys fragment = new HardwareKeys();
             ft.addToBackStack("hardware_keys_binding");
             ft.replace(this.getId(), fragment);
             ft.commit();
             return true;
         } else if (preference == mShowActionOverflow) {
             boolean enabled = mShowActionOverflow.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                     enabled ? 1 : 0);
             // Show appropriate
             if (enabled) {
                 Toast.makeText(getActivity(), R.string.hardware_keys_show_overflow_toast_enable,
                         Toast.LENGTH_LONG).show();
             } else {
                 Toast.makeText(getActivity(), R.string.hardware_keys_show_overflow_toast_disable,
                         Toast.LENGTH_LONG).show();
             }
             return true;
        } else if (preference == mWakeWhenPluggedOrUnplugged) {
             Settings.Global.putInt(getContentResolver(),
                     Settings.Global.WAKE_WHEN_PLUGGED_OR_UNPLUGGED,
                     mWakeWhenPluggedOrUnplugged.isChecked() ? 1 : 0);
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
         private static final int LOCKSCREEN_ALPHA = 4;
         boolean linkTransparencies = true;
 
         CheckBox mLinkCheckBox, mMatchStatusbarKeyguard, mMatchNavbarKeyguard;
         ViewGroup mNavigationBarGroup;
         TextView mSbLabel;
         AlphaSeekBar mSeekBars[] = new AlphaSeekBar[5];
 
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
             mSeekBars[LOCKSCREEN_ALPHA] = (AlphaSeekBar) layout.findViewById(R.id.lockscreen_alpha);
 
             try {
                 // restore any saved settings
                 int alphas[] = new int[2];
                 ContentResolver resolver = getActivity().getContentResolver();
                 int lockscreen_alpha = Settings.System.getInt(resolver,
                         Settings.System.LOCKSCREEN_ALPHA_CONFIG, KEYGUARD_ALPHA);
                 mSeekBars[LOCKSCREEN_ALPHA].setCurrentAlpha(lockscreen_alpha);
                 String sbConfig = Settings.System.getString(resolver,
                         Settings.System.STATUS_BAR_ALPHA_CONFIG);
                 if (sbConfig != null) {
                     String split[] = sbConfig.split(";");
                     alphas[0] = Integer.parseInt(split[0]);
                     alphas[1] = Integer.parseInt(split[1]);
                     mSeekBars[STATUSBAR_ALPHA].setCurrentAlpha(alphas[0]);
                     mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                     mMatchStatusbarKeyguard.setChecked(alphas[1] == lockscreen_alpha);
                     if (linkTransparencies) {
                         mSeekBars[NAVBAR_ALPHA].setCurrentAlpha(alphas[0]);
                         mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                     } else {
                         String navConfig = Settings.System.getString(resolver,
                                 Settings.System.NAVIGATION_BAR_ALPHA_CONFIG);
                         if (navConfig != null) {
                             split = navConfig.split(";");
                             alphas[0] = Integer.parseInt(split[0]);
                             alphas[1] = Integer.parseInt(split[1]);
                             mSeekBars[NAVBAR_ALPHA].setCurrentAlpha(alphas[0]);
                             mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                             mMatchNavbarKeyguard.setChecked(alphas[1] == lockscreen_alpha);
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
                     Settings.System.putInt(mContentResolver,
                             Settings.System.LOCKSCREEN_ALPHA_CONFIG,
                             mSeekBars[LOCKSCREEN_ALPHA].getCurrentAlpha());
                     // update keyguard alpha
                     if (!mSeekBars[STATUSBAR_KG_ALPHA].isEnabled()) {
                         mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(
                                 mSeekBars[LOCKSCREEN_ALPHA].getCurrentAlpha());
                     }
                     if (!mSeekBars[NAVBAR_KG_ALPHA].isEnabled()) {
                         mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(
                                 mSeekBars[LOCKSCREEN_ALPHA].getCurrentAlpha());
                     }
                     if (linkTransparencies) {
                         String config = mSeekBars[STATUSBAR_ALPHA].getCurrentAlpha() + ";" +
                                 mSeekBars[STATUSBAR_KG_ALPHA].getCurrentAlpha();
                         Settings.System.putString(mContentResolver,
                                 Settings.System.STATUS_BAR_ALPHA_CONFIG, config);
                         Settings.System.putString(mContentResolver,
                                 Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, config);
                     } else {
                         String sbConfig = mSeekBars[STATUSBAR_ALPHA].getCurrentAlpha() + ";" +
                                 mSeekBars[STATUSBAR_KG_ALPHA].getCurrentAlpha();
                         Settings.System.putString(mContentResolver,
                                 Settings.System.STATUS_BAR_ALPHA_CONFIG, sbConfig);
 
                         String nbConfig = mSeekBars[NAVBAR_ALPHA].getCurrentAlpha() + ";" +
                                 mSeekBars[NAVBAR_KG_ALPHA].getCurrentAlpha();
                         Settings.System.putString(mContentResolver,
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
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_ALPHA_CONFIG, KEYGUARD_ALPHA);
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
 
             // update keyguard alpha
             int lockscreen_alpha = Settings.System.getInt(getActivity().getContentResolver(),
                         Settings.System.LOCKSCREEN_ALPHA_CONFIG, KEYGUARD_ALPHA);
             if (!mSeekBars[STATUSBAR_KG_ALPHA].isEnabled()) {
                 mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(lockscreen_alpha);
             }
             if (!mSeekBars[NAVBAR_KG_ALPHA].isEnabled()) {
                 mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(lockscreen_alpha);
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
