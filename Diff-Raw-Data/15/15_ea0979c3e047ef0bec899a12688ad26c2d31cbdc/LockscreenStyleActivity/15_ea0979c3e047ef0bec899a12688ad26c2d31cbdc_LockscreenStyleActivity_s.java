 /*
 ** Copyright (C) 2011 The Liquid Settings Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License"); 
 ** you may not use this file except in compliance with the License. 
 ** You may obtain a copy of the License at 
 **
 **     http://www.apache.org/licenses/LICENSE-2.0 
 **
 ** Unless required by applicable law or agreed to in writing, software 
 ** distributed under the License is distributed on an "AS IS" BASIS, 
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 ** See the License for the specific language governing permissions and 
 ** limitations under the License.
 */
 
 package com.liquid.settings.activities;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.ActivityNotFoundException;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;    
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceScreen;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.view.Window;
 import android.widget.Toast;
 
 import com.liquid.settings.R;
 import com.liquid.settings.utilities.ShortcutPickHelper;
 
 public class LockscreenStyleActivity extends PreferenceActivity implements
                     OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {
 
     private static final int LOCKSCREEN_BACKGROUND = 1024;
     private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";
     private static final String IN_CALL_STYLE_PREF = "pref_in_call_style";
     private static final String LOCKSCREEN_CUSTOM_APP_TOGGLE = "pref_lockscreen_custom_app_toggle";
     private static final String LOCKSCREEN_CUSTOM_APP_ACTIVITY = "pref_lockscreen_custom_app_activity";
     private static final String LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE = "pref_lockscreen_rotary_unlock_down_toggle";
     private static final String LOCKSCREEN_RING_UNLOCK_MIDDLE_TOGGLE = "pref_lockscreen_ring_unlock_middle_toggle";
     private static final String LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE = "pref_lockscreen_rotary_hide_arrows_toggle";  
     private static final String LOCKSCREEN_HIDE_CARRIER_PREF = "pref_lockscreen_hide_carrier";
     private static final String LOCKSCREEN_CUSTOM_ICON_STYLE = "pref_lockscreen_custom_icon_style";
     private static final String LOCKSCREEN_CUSTOM_BACKGROUND = "pref_lockscreen_background";
 
     private CheckBoxPreference mCustomAppTogglePref;
     private CheckBoxPreference mRotaryUnlockDownToggle;
     private CheckBoxPreference mRingUnlockMiddleToggle;
     private CheckBoxPreference mRotaryHideArrowsToggle;
     private CheckBoxPreference mHideCarrierPref;
     private CheckBoxPreference mCustomIconStyle;
     
     private ListPreference mLockscreenStylePref;
     private ListPreference mInCallStylePref;
     private Preference mCustomAppActivityPref;
     private ListPreference mCustomBackground;
 
     private int mWhichApp = -1;
     private File wallpaperImage;
     private File wallpaperTemporary;
     private int mMaxRingCustomApps = Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES.length;
 
     enum LockscreenStyle{
         Slider,
         Rotary,
         Sense,
         Lense,
         Ring,
         Circle,
         Honey,
         Halo;
 
         static public LockscreenStyle getStyleById(int id){
             switch (id){
                 case 1:
                     return Slider;
                 case 2:
                     return Rotary;
                 case 3:
                     return Sense;
                 case 4:
                     return Lense;
                 case 5:
                     return Ring;
                 case 6:
                     return Circle;
                 case 7:
                     return Honey;
                 case 8:
                     return Halo;
                 default:
                     return Ring;
             }
         }
 
         static public LockscreenStyle getStyleById(String id){
             return getStyleById(Integer.valueOf(id));
         }
 
         static public int getIdByStyle(LockscreenStyle lockscreenstyle){
             switch (lockscreenstyle){
                 case Slider:
                     return 1;
                 case Rotary:
                     return 2;
                 case Sense:
                     return 3;
                 case Lense:
                     return 4;
                 case Ring:
                     return 5;
                 case Circle:
                     return 6;
                 case Honey:
                     return 7;
                 case Halo:
                     return 8;
                 default:
                     return 5;
             }
         }
     }
 
     enum InCallStyle {
         Slider,
         Rotary,
         Sense,
         Ring;
 
         static public InCallStyle getStyleById(int id){
             switch (id){
                 case 1:
                     return Slider;
                 case 2:
                     return Rotary;
                 case 3:
                     return Sense;
                 case 4:
                     return Ring;
                 default:
                     return Ring;
             }
         }
 
         static public InCallStyle getStyleById(String id){
             return getStyleById(Integer.valueOf(id));
         }
 
         static public int getIdByStyle(InCallStyle inCallStyle){
             switch (inCallStyle){
                 case Slider:
                     return 1;
                 case Rotary:
                     return 2;
                 case Sense:
                     return 3;
                 case Ring:
                     return 4;
                 default:
                     return 4;
             }
         }
     }
 
     private InCallStyle mInCallStyle;
     private ShortcutPickHelper mPicker;
     private LockscreenStyle mLockscreenStyle;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(R.string.lockscreen_title);
         addPreferencesFromResource(R.xml.lockscreen_style_settings);
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mLockscreenStylePref = (ListPreference) prefSet.findPreference(LOCKSCREEN_STYLE_PREF);
         mLockscreenStyle = LockscreenStyle.getStyleById(
                 Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_STYLE_PREF, 5));
         mLockscreenStylePref.setValue(String.valueOf(LockscreenStyle.getIdByStyle(mLockscreenStyle)));
         mLockscreenStylePref.setOnPreferenceChangeListener(this);
 
         mInCallStylePref = (ListPreference) prefSet.findPreference(IN_CALL_STYLE_PREF);
         mInCallStyle = InCallStyle.getStyleById(
                 Settings.System.getInt(getContentResolver(),
                 Settings.System.IN_CALL_STYLE_PREF, 4));
         mInCallStylePref.setValue(String.valueOf(InCallStyle.getIdByStyle(mInCallStyle)));
         mInCallStylePref.setOnPreferenceChangeListener(this);
         
         mRotaryUnlockDownToggle = (CheckBoxPreference) prefSet
                 .findPreference(LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE);
         mRotaryUnlockDownToggle.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, 0) == 1);
 
         mRingUnlockMiddleToggle = (CheckBoxPreference) prefSet
                 .findPreference(LOCKSCREEN_RING_UNLOCK_MIDDLE_TOGGLE);
         mRingUnlockMiddleToggle.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_RING_UNLOCK_MIDDLE, 0) == 1);
 
         mRotaryHideArrowsToggle = (CheckBoxPreference) prefSet
                 .findPreference(LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE);
         mRotaryHideArrowsToggle.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, 0) == 1);
 
         mCustomAppTogglePref = (CheckBoxPreference) prefSet
                 .findPreference(LOCKSCREEN_CUSTOM_APP_TOGGLE);
         mCustomAppTogglePref.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, 0) == 1);
        
 	    mHideCarrierPref = (CheckBoxPreference) prefSet
 		        .findPreference(LOCKSCREEN_HIDE_CARRIER_PREF);
         mHideCarrierPref.setChecked(Settings.System.getInt(getContentResolver(),
         	    Settings.System.LOCKSCREEN_HIDE_CARRIER, 0) == 1);
         
         mCustomIconStyle = (CheckBoxPreference) prefSet
                 .findPreference(LOCKSCREEN_CUSTOM_ICON_STYLE);
         mCustomIconStyle.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE, 1) == 2);
 
         updateStylePrefs(mLockscreenStyle, mInCallStyle);
 
         mCustomAppActivityPref = prefSet
                 .findPreference(LOCKSCREEN_CUSTOM_APP_ACTIVITY);
 
         mCustomBackground = (ListPreference) prefSet
         .findPreference(LOCKSCREEN_CUSTOM_BACKGROUND);
         mCustomBackground.setOnPreferenceChangeListener(this);
         wallpaperImage = new File(getApplicationContext().getFilesDir()+"/lockwallpaper");
         wallpaperTemporary = new File(getApplicationContext().getFilesDir()+"/lockwallpaper.tmp");
         updateCustomBackgroundSummary();
         mPicker = new ShortcutPickHelper(this, this);
     }
 
     private void updateCustomBackgroundSummary() {
         int resId;
         String value = Settings.System.getString(getContentResolver(),
                 Settings.System.LOCKSCREEN_BACKGROUND);
 
         if (value == null) {
             resId = R.string.pref_lockscreen_custom_background_summary_default;
         } else if (value.isEmpty()) {
             resId = R.string.pref_lockscreen_custom_background_summary_image;
         } else {
             resId = R.string.pref_lockscreen_custom_background_summary_color;
         }
 
         mCustomBackground.setSummary(getResources().getString(resId));
     }
     
    @Override
    public void onResume() {
        super.onResume();

         if (mLockscreenStyle == LockscreenStyle.Ring) {
             mCustomAppActivityPref.setSummary(getCustomRingAppSummary());
         } else {
             String value = Settings.System.getString(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY);
             mCustomAppActivityPref.setSummary(mPicker.getFriendlyNameForUri(value));
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == LOCKSCREEN_BACKGROUND) {
             if (resultCode == RESULT_OK) {
                 if (wallpaperTemporary.exists()) {
                     wallpaperTemporary.renameTo(wallpaperImage);
                 }
 
                 wallpaperImage.setReadOnly();
                 Toast.makeText(this, getResources().getString(R.string.
                         pref_lockscreen_custom_background_result_successful), Toast.LENGTH_LONG).show();
                 Settings.System.putString(getContentResolver(),
                         Settings.System.LOCKSCREEN_BACKGROUND,"");
                 mCustomBackground.setValueIndex(1);
                 updateCustomBackgroundSummary();
             } else {
                 if (wallpaperTemporary.exists()) {
                     wallpaperTemporary.delete();
                 }
 
                 Toast.makeText(this, getResources().getString(R.string.
                         pref_lockscreen_custom_background_result_not_successful), Toast.LENGTH_LONG).show();
             }
         }
 
         mPicker.onActivityResult(requestCode, resultCode, data);
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         if (preference == mCustomAppTogglePref) {
             value = mCustomAppTogglePref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, value ? 1 : 0);
             updateStylePrefs(mLockscreenStyle, mInCallStyle);
             return true;
         } else if (preference == mRotaryUnlockDownToggle) {
             value = mRotaryUnlockDownToggle.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, value ? 1 : 0);
             return true;
         } else if (preference == mRingUnlockMiddleToggle) {
             value = mRingUnlockMiddleToggle.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_RING_UNLOCK_MIDDLE, value ? 1 : 0);
             return true;
         } else if (preference == mRotaryHideArrowsToggle) {
             value = mRotaryHideArrowsToggle.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, value ? 1 : 0);        
             return true;
         } else if (preference == mHideCarrierPref) {
             value = mHideCarrierPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_HIDE_CARRIER, value ? 1 : 0);
             return true;
         } else if (preference == mCustomIconStyle) {
             value = mCustomIconStyle.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE, value ? 2 : 1);
             return true;
         } else if (preference == mCustomAppActivityPref) {
             if (mLockscreenStyle == LockscreenStyle.Ring) {
                 final String[] items = getCustomRingAppItems();
 
                 if (items.length == 0) {
                     mWhichApp = 0;
                     mPicker.pickShortcut();
                 } else {
                     AlertDialog.Builder builder = new AlertDialog.Builder(this);
                     builder.setTitle(R.string.pref_lockscreen_ring_custom_apps_dialog_title_set);
                     builder.setItems(items, new Dialog.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                             mWhichApp = which;
                             mPicker.pickShortcut();
                         }
                     });
                     if (items.length < mMaxRingCustomApps) {
                         builder.setPositiveButton(R.string.pref_lockscreen_ring_custom_apps_dialog_add,
                                 new Dialog.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                                 mWhichApp = items.length;
                                 mPicker.pickShortcut();
                             }
                         });
                     }
                     builder.setNeutralButton(R.string.pref_lockscreen_ring_custom_apps_dialog_remove,
                             new Dialog.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
 
                             AlertDialog.Builder builder = new AlertDialog.Builder(LockscreenStyleActivity.this);
                             builder.setTitle(R.string.pref_lockscreen_ring_custom_apps_dialog_title_unset);
                             builder.setItems(items, new Dialog.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss();
                                     Settings.System.putString(getContentResolver(),
                                             Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[which], null);
                                     //shift the rest of items down
                                     for (int q = which + 1; q < mMaxRingCustomApps; q++) {
                                         Settings.System.putString(getContentResolver(),
                                                 Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q - 1],
                                                 Settings.System.getString(getContentResolver(),
                                                 Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q]));
                                         Settings.System.putString(getContentResolver(),
                                                 Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q], null);
                                     }
                                     mCustomAppActivityPref.setSummary(getCustomRingAppSummary());
                                 }
                             });
                             builder.setNegativeButton(R.string.pref_lockscreen_ring_custom_apps_dialog_cancel,
                                     new Dialog.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss();
                                 }
                             });
                             builder.setCancelable(true);
                             builder.create().show();
                         }
                     });
                     builder.setNegativeButton(R.string.pref_lockscreen_ring_custom_apps_dialog_cancel,
                             new Dialog.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     });
                     builder.setCancelable(true);
                     builder.create().show();
                 }
             } else {
                 mPicker.pickShortcut();
             }
         }
 
         return false;
     }
 
     ColorPickerDialog.OnColorChangedListener mPackageColorListener =
             new ColorPickerDialog.OnColorChangedListener() {
         public void colorChanged(int color) {
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_BACKGROUND,color);
             mCustomBackground.setValueIndex(0);
             updateCustomBackgroundSummary();
         }
         @Override
         public void colorUpdate(int color) {
         }
     };
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         String val = newValue.toString();
         if (preference == mLockscreenStylePref) {
             mLockscreenStyle = LockscreenStyle.getStyleById((String) newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_STYLE_PREF,
                     LockscreenStyle.getIdByStyle(mLockscreenStyle));
             updateStylePrefs(mLockscreenStyle, mInCallStyle);
             return true;
         }
         if (preference == mInCallStylePref) {
             mInCallStyle = InCallStyle.getStyleById((String) newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.IN_CALL_STYLE_PREF,
                     InCallStyle.getIdByStyle(mInCallStyle));
             updateStylePrefs(mLockscreenStyle, mInCallStyle);
             return true;
         }
         if (preference == mCustomBackground) {
             int indexOf = mCustomBackground.findIndexOfValue(val);
 
             switch (indexOf) {
                 case 0:
                     ColorPickerDialog cp = new ColorPickerDialog(this,mPackageColorListener,
                             Settings.System.getInt(getContentResolver(),
                                     Settings.System.LOCKSCREEN_BACKGROUND, 0));
                     cp.show();
                     return false;
                 case 1:
                     Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                     intent.setType("image/*");
                     intent.putExtra("crop", "true");
                     intent.putExtra("scale", true);
                     intent.putExtra("scaleUpIfNeeded", false);
                     intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
 
                     int width = getWindowManager().getDefaultDisplay().getWidth();
                     int height = getWindowManager().getDefaultDisplay().getHeight();
                     Rect rect = new Rect();
                     Window window = getWindow();
                     window.getDecorView().getWindowVisibleDisplayFrame(rect);
 
                     int statusBarHeight = rect.top;
                     int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                     int titleBarHeight = contentViewTop - statusBarHeight;
                     boolean isPortrait = getResources().getConfiguration().orientation ==
                         Configuration.ORIENTATION_PORTRAIT;
                     intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
                     intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
 
                     try {
                         wallpaperTemporary.createNewFile();
                         wallpaperTemporary.setWritable(true, false);
                         intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(wallpaperTemporary));
                         intent.putExtra("return-data", false);
                         startActivityForResult(intent,LOCKSCREEN_BACKGROUND);
                     } catch (IOException e) {
                     } catch (ActivityNotFoundException e) {
                     }
                     return false;
                 case 2:
                     Settings.System.putString(getContentResolver(),
                             Settings.System.LOCKSCREEN_BACKGROUND,null);
                     updateCustomBackgroundSummary();
                     break;
             }
 
             return true;
         }
 
         return false;
     }
 
     @Override
     public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
         if (mWhichApp == -1) {
             if (Settings.System.putString(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, uri)) {
                 mCustomAppActivityPref.setSummary(friendlyName);
             }
         } else {
             Settings.System.putString(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[mWhichApp], uri);
             mCustomAppActivityPref.setSummary(getCustomRingAppSummary());
             mWhichApp = -1;
         }
     }
 
     private void updateStylePrefs(LockscreenStyle lockscreenStyle, InCallStyle inCallStyle) {
         if (lockscreenStyle == LockscreenStyle.Slider ||
         lockscreenStyle == LockscreenStyle.Lense || lockscreenStyle == LockscreenStyle.Circle ||
         lockscreenStyle == LockscreenStyle.Honey || lockscreenStyle == LockscreenStyle.Halo) {
             if (inCallStyle == InCallStyle.Slider || 
                 inCallStyle == InCallStyle.Ring) {
                 mRotaryHideArrowsToggle.setChecked(false);
                 mRotaryHideArrowsToggle.setEnabled(false);
             } else {
                 mRotaryHideArrowsToggle.setEnabled(true);
             }
 
             mRotaryUnlockDownToggle.setChecked(false);
             mRotaryUnlockDownToggle.setEnabled(false);
             mRingUnlockMiddleToggle.setChecked(false);
             mRingUnlockMiddleToggle.setEnabled(false);
         } else if (lockscreenStyle == LockscreenStyle.Rotary || 
                    lockscreenStyle == LockscreenStyle.Sense) {
             mRotaryHideArrowsToggle.setEnabled(true);
 
             if (mCustomAppTogglePref.isChecked() == true) {
 			    mRotaryUnlockDownToggle.setEnabled(true);
 			} else {
 				mRotaryUnlockDownToggle.setChecked(false);
 			    mRotaryUnlockDownToggle.setEnabled(false);
 			}
 
             mRingUnlockMiddleToggle.setChecked(false);
             mRingUnlockMiddleToggle.setEnabled(false);
         } else if (lockscreenStyle == LockscreenStyle.Ring) {
             mRotaryUnlockDownToggle.setChecked(false);
             mRotaryUnlockDownToggle.setEnabled(false);
 
             if (inCallStyle == InCallStyle.Rotary ||
                 inCallStyle == InCallStyle.Sense) {
                 mRotaryHideArrowsToggle.setEnabled(true);
             } else {
                 mRotaryHideArrowsToggle.setChecked(false);
                 mRotaryHideArrowsToggle.setEnabled(false);
             }
 
             if (mCustomAppTogglePref.isChecked() == true) {
                 mRingUnlockMiddleToggle.setEnabled(true);
             } else {
                 mRingUnlockMiddleToggle.setChecked(false);
                 mRingUnlockMiddleToggle.setEnabled(false);
             }
         }
 
         if (lockscreenStyle == LockscreenStyle.Lense || lockscreenStyle == LockscreenStyle.Circle ||
         lockscreenStyle == LockscreenStyle.Honey || lockscreenStyle == LockscreenStyle.Halo) {
             mCustomIconStyle.setChecked(false);
             mCustomAppTogglePref.setChecked(false);
             mCustomAppTogglePref.setEnabled(false);
         } else {
             mCustomAppTogglePref.setEnabled(true);
         }
 
         if (lockscreenStyle == LockscreenStyle.Ring) {
             mCustomIconStyle.setChecked(false);
             mCustomIconStyle.setEnabled(false);
         } else if (mCustomAppTogglePref.isChecked() == true){
             mCustomIconStyle.setEnabled(true);
         }
 
         boolean value = mRotaryUnlockDownToggle.isChecked();
         Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN,
                 value ? 1 : 0);
 
         value = mRotaryHideArrowsToggle.isChecked();
         Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS,
                 value ? 1 : 0);
 
         value = mCustomAppTogglePref.isChecked();
         Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE,
                 value ? 1 : 0);
 
         value = mHideCarrierPref.isChecked();
         Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_HIDE_CARRIER,
                 value ? 1 : 0);
 
         value = mCustomIconStyle.isChecked();
         Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE,
                 value ? 2 : 1);
     }
 
     private String getCustomRingAppSummary() {
         String summary = "";
         String[] items = getCustomRingAppItems();
         for (int q = 0; q < items.length; q++) {
             if (q != 0) {
                 summary += ", ";
             }
             summary += items[q];
         }
 
         return summary;
     }
 
     private String[] getCustomRingAppItems() {
         ArrayList<String> items = new ArrayList<String>();
         for (int q = 0; q < mMaxRingCustomApps; q++) {
             String uri = Settings.System.getString(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q]);
             if (uri != null) {
                 items.add(mPicker.getFriendlyNameForUri(uri));
             }
         }
 
         return items.toArray(new String[0]);
     }
 }
