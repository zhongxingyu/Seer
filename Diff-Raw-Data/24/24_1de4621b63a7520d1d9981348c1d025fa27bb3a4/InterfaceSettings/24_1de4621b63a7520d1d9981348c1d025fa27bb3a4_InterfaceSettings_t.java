 
 package com.android.settings.eos;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.teameos.jellybean.settings.EOSConstants;
 
 import com.android.internal.telephony.Phone;
 import com.android.internal.telephony.RILConstants;
 import com.android.settings.R;
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.IWindowManager;
 
 import com.android.settings.SettingsPreferenceFragment;
 
 public class InterfaceSettings extends SettingsPreferenceFragment implements
         OnPreferenceChangeListener {
 
     private Context mContext;
     private ContentResolver mContentResolver;
 
     private CheckBoxPreference mUseTabletUI;
     private CheckBoxPreference mSystemUISettings;
     private CheckBoxPreference mRecentsKillallButtonPreference;
     private CheckBoxPreference mRecentsMemDisplayPreference;
     private CheckBoxPreference mLowProfileNavBar;
     private CheckBoxPreference mFatFingers;
     private CheckBoxPreference mHideIndicator;
     private ColorPreference mIndicatorColor;
     private Preference mIndicatorDefaultColor;
     private ListPreference mRotationLockTogglePreference;
     private ListPreference mSystemUISettingsLocation;
     private EosMultiSelectListPreference mStandardSettingsView;
     private EosMultiSelectListPreference mEosQuickSettingsView;
     private boolean mEosSettingsEnabled = false;
     private boolean mIsTabletMode = false;
     private boolean mHasNavBar = false;
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         mContext = getActivity();
         mContentResolver = mContext.getContentResolver();
         IWindowManager mWindowManager = IWindowManager.Stub.asInterface(
                 ServiceManager.getService(Context.WINDOW_SERVICE));
         try {
             mHasNavBar = mWindowManager.hasNavigationBar();
         } catch (RemoteException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         addPreferencesFromResource(R.xml.eos_interface_settings);
 
         mUseTabletUI = (CheckBoxPreference) findPreference("eos_interface_statusbar_use_tablet_ui");
         mSystemUISettings = (CheckBoxPreference) findPreference("eos_interface_settings_eos_enabled");
         mEosQuickSettingsView = (EosMultiSelectListPreference) findPreference("eos_interface_eos_quick_enabled");
         mSystemUISettingsLocation = (ListPreference) findPreference("eos_interface_settings_eos_settings_location");
         mRotationLockTogglePreference = (ListPreference) findPreference("eos_interface_rotationlock_toggle");
         mRecentsKillallButtonPreference = (CheckBoxPreference) findPreference("eos_interface_recents_killall_button");
         mRecentsMemDisplayPreference = (CheckBoxPreference) findPreference("eos_interface_recents_mem_display");
         mLowProfileNavBar = (CheckBoxPreference) findPreference("eos_interface_navbar_low_profile");
         mFatFingers = (CheckBoxPreference) findPreference("eos_interface_systembar_fat_fingers");
         mHideIndicator = (CheckBoxPreference)findPreference("eos_interface_settings_indicator_visibility");
         mIndicatorColor = (ColorPreference) findPreference("eos_interface_settings_indicator_color");
         mIndicatorDefaultColor = (Preference) findPreference("eos_interface_settings_indicator_color_default");
 
         if (findPreference("eos_interface_normal_settings") != null) {
             mStandardSettingsView = (EosMultiSelectListPreference) findPreference("eos_interface_normal_settings");
             mStandardSettingsView.setOnPreferenceChangeListener(this);
             mStandardSettingsView.setEntries(getResources().getStringArray(
                     R.array.eos_interface_settings_standard_entries));
             mStandardSettingsView.setEntryValues(getResources().getStringArray(
                     R.array.eos_interface_settings_standard_values));
             populateStandardSettingsList();
         }
 
         if(!mHasNavBar) {
             PreferenceScreen ps = this.getPreferenceScreen();
             PreferenceCategory pc = (PreferenceCategory) ps.findPreference("eos_interface_navbar");
             if (pc != null) ps.removePreference(pc);
         }
 
         // will be null on tablets and grouper in tablet mode
         if(mLowProfileNavBar != null && mHasNavBar) {
             mLowProfileNavBar.setChecked(Settings.System.getInt(getContentResolver(),
                     EOSConstants.SYSTEMUI_BAR_SIZE_MODE, 0) == 1);
             mLowProfileNavBar.setOnPreferenceChangeListener(this);
         }
 
         if (mContext.getResources().getBoolean(R.bool.eos_tablet) || mIsTabletMode) {
 
             if (mSystemUISettings != null) {
                 mSystemUISettings.setChecked(Settings.System.getInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_ENABLED, 0) == 1);
                 mSystemUISettings.setOnPreferenceChangeListener(this);
                 mEosSettingsEnabled = mSystemUISettings.isChecked();
             }
         } else {
             boolean eosSettingsEnabled = Settings.System.getInt(getContentResolver(),
                     EOSConstants.SYSTEMUI_SETTINGS_ENABLED,
                     EOSConstants.SYSTEMUI_SETTINGS_ENABLED_DEF) == 1;
             boolean eosSettingsTop = Settings.System.getInt(getContentResolver(),
                     EOSConstants.SYSTEMUI_SETTINGS_PHONE_TOP,
                     EOSConstants.SYSTEMUI_SETTINGS_PHONE_TOP_DEF) == 1;
             if (mSystemUISettingsLocation != null) {
                 if (!eosSettingsEnabled) {
                     mSystemUISettingsLocation.setValue("disabled");
                     mSystemUISettingsLocation.notifyDependencyChange(true);
                     mEosSettingsEnabled = false;
                 } else if ((eosSettingsEnabled && eosSettingsTop)) {
                     mSystemUISettingsLocation.setValue("top");
                     mSystemUISettingsLocation.notifyDependencyChange(false);
                     mEosSettingsEnabled = true;
                 }
                 mSystemUISettingsLocation.setOnPreferenceChangeListener(this);
             }
         }
 
         if (mEosQuickSettingsView != null) {
             mEosQuickSettingsView.setOnPreferenceChangeListener(this);
             mEosQuickSettingsView.setEntries(getResources().getStringArray(
                     R.array.eos_quick_enabled_names));
             mEosQuickSettingsView.setEntryValues(getResources().getStringArray(
                     R.array.eos_quick_enabled_preferences));
             mEosQuickSettingsView.setReturnFullList(true);
             populateEosSettingsList();
         }
 
         if (mHideIndicator != null) {
             mHideIndicator.setChecked(Settings.System.getInt(getContentResolver(),
                     EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_HIDDEN,
                     EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_HIDDEN_DEF) == 1);
             mHideIndicator.setOnPreferenceChangeListener(this);
         }
 
         if (mIndicatorColor != null) {
             mIndicatorColor.setProviderTarget(EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_COLOR);
         }
 
         if (mIndicatorDefaultColor != null) {
             mIndicatorDefaultColor
                     .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 
                         @Override
                         public boolean onPreferenceClick(Preference preference) {
                             // TODO Auto-generated method stub
                             Settings.System.putInt(mContext.getContentResolver(),
                                     EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_COLOR,
                                     EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_COLOR_DEF);
                             return true;
                         }
                     });
         }
 
         if (mRotationLockTogglePreference != null) {
             mRotationLockTogglePreference.setOnPreferenceChangeListener(this);
             String currentValue = Settings.System.getString(mContentResolver, EOSConstants.SYSTEMUI_INTERFACE_ROTATIONLOCK_TOGGLE);
             if (!("show".equals(currentValue) || "hide".equals(currentValue))) {
                 currentValue = "default";
             }
             mRotationLockTogglePreference.setValue(currentValue);
         }
 
 	    if (mRecentsKillallButtonPreference != null) {
             mRecentsKillallButtonPreference.setOnPreferenceChangeListener(this);
         }
 	    if (mRecentsMemDisplayPreference != null) {
             mRecentsMemDisplayPreference.setOnPreferenceChangeListener(this);
         }
     }
 
 
     class UiModeDialog {
         BroadcastReceiver mBarStateReceiver;
         ProgressDialog dialog;
         boolean isTabletMode;
         int message;
         String title;
 
         public UiModeDialog(Context context, boolean tabletMode) {
             IntentFilter filter = new IntentFilter();
             filter.addAction(EOSConstants.INTENT_SYSTEMUI_KILL_SERVICE);
             filter.addAction(EOSConstants.INTENT_SYSTEMUI_BAR_RESTORED);
             mBarStateReceiver = new BroadcastReceiver() {
 
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     // TODO Auto-generated method stub\
                     if (EOSConstants.INTENT_SYSTEMUI_BAR_RESTORED.equals(intent.getAction())) {
                         mContext.unregisterReceiver(mBarStateReceiver);
                         dialog.dismiss();
                         // send this off right before we die
                         mContext.sendBroadcast(new Intent()
                             .setAction(EOSConstants.INTENT_SETTINGS_RESTART_INTERFACE_SETTINGS));
                         finishFragment();
                     } else if (EOSConstants.INTENT_SYSTEMUI_KILL_SERVICE.equals(intent.getAction())) {
                         new Handler().postDelayed(new Runnable() {
 
                             @Override
                             public void run() {
                                 // TODO Auto-generated method stub
                                 Intent i = new Intent();
                                 i.setComponent(ComponentName
                                         .unflattenFromString("com.android.systemui/.SystemUIService"));
                                 mContext.startService(i);
                             }
                         }, 500);
                     }
                 };
             };
             mContext.registerReceiver(mBarStateReceiver, filter);
             message = tabletMode ?
                     R.string.eos_interface_hybridui_change_tablet_message :
                     R.string.eos_interface_hybridui_change_hybrid_message;
 
             title = getString(R.string.eos_interface_hybridui_change_title);
             dialog = ProgressDialog.show(mContext, title, getString(message), true);
             mContext.sendBroadcast(new Intent()
                     .setAction(EOSConstants.INTENT_SYSTEMUI_REMOVE_BAR));
         }
     }
 
     private void populateStandardSettingsList() {
         HashSet<String> selectedvalues = new HashSet<String>();
         int defaultValues[] = getResources().getIntArray(
                 R.array.eos_interface_settings_standard_defaults);
         String preferences[] = getResources().getStringArray(
                 R.array.eos_interface_settings_standard_values);
 
         for (int i = 0; i < preferences.length; i++) {
             if (Settings.System.getInt(getContentResolver(), preferences[i], defaultValues[i]) == 1)
                 selectedvalues.add(preferences[i]);
 
         }
         mStandardSettingsView.setValues(selectedvalues);
     }
 
     private void populateEosSettingsList() {
         LinkedHashSet<String> selectedValues = new LinkedHashSet<String>();
         String enabledControls = Settings.System.getString(getContentResolver(),
                 EOSConstants.SYSTEMUI_SETTINGS_ENABLED_CONTROLS);
         if (enabledControls != null) {
             String[] controls = enabledControls.split("\\|");
             selectedValues.addAll(Arrays.asList(controls));
         } else {
             selectedValues.addAll(Arrays.asList(EOSConstants.SYSTEMUI_SETTINGS_DEFAULTS));
         }
         mEosQuickSettingsView.setValues(selectedValues);
 
         ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         if (!manager.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
             mEosQuickSettingsView
                     .removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_MOBILEDATA);
             mEosQuickSettingsView
                     .removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_WIFITETHER);
             mEosQuickSettingsView.removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_USBTETHER);
         }
         TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
         if(!(tm.getCurrentPhoneType() == TelephonyManager.PHONE_TYPE_CDMA)
                 || !(tm.getNetworkType() == RILConstants.NETWORK_MODE_LTE_CDMA_EVDO)) {
             mEosQuickSettingsView.removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_LTE);
         }
         if(!hasTorch()) {
             mEosQuickSettingsView.removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_TORCH);
         }
         mEosQuickSettingsView.setEnabled(mEosSettingsEnabled);
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference.equals(mUseTabletUI)) {
             Boolean value = (Boolean) newValue;
             Settings.System.putInt(mContentResolver, EOSConstants.SYSTEMUI_USE_TABLET_UI,
                     value.booleanValue() ? 1 : 0);
             new UiModeDialog(mContext,value.booleanValue());
             return true;
         } else if (preference.equals(mStandardSettingsView)) {
             Map<String, Boolean> changes = (Map<String, Boolean>) newValue;
             for (Entry<String, Boolean> entry : changes.entrySet()) {
                 Settings.System.putInt(getContentResolver(), entry.getKey(), (entry.getValue() ? 1 : 0));
             }
            return true;
         } else if (preference.equals(mEosQuickSettingsView)) {
             Map<String, Boolean> values = (Map<String, Boolean>) newValue;
             StringBuilder newPreferenceValue = new StringBuilder();
             for (Entry entry : values.entrySet()) {
                 newPreferenceValue.append(entry.getKey());
                 newPreferenceValue.append("|");
             }
             Settings.System.putString(getContentResolver(),
                     EOSConstants.SYSTEMUI_SETTINGS_ENABLED_CONTROLS,
                     newPreferenceValue.toString());
             return true;
         } else if (preference.equals(mRotationLockTogglePreference)) {
             final String newToggleMode = (String) newValue;
             Settings.System.putString(getContentResolver(),
                     EOSConstants.SYSTEMUI_INTERFACE_ROTATIONLOCK_TOGGLE,
                     newToggleMode);
           return true;
         } else if (preference.equals(mRecentsKillallButtonPreference)) {
             Settings.System.putInt(mContext.getContentResolver(),
                     EOSConstants.SYSTEMUI_RECENTS_KILLALL_BUTTON,
                     ((Boolean) newValue).booleanValue() ? 1 : 0);
           return true;
         } else if (preference.equals(mRecentsMemDisplayPreference)) {
             Settings.System.putInt(mContext.getContentResolver(),
                     EOSConstants.SYSTEMUI_RECENTS_MEM_DISPLAY,
                     ((Boolean) newValue).booleanValue() ? 1 : 0);
           return true;
         } else if (preference.equals(mSystemUISettings)) {
             Settings.System.putInt(getContentResolver(),
                     EOSConstants.SYSTEMUI_SETTINGS_ENABLED,
                     ((Boolean) newValue).booleanValue() ? 1 : 0);
             mEosQuickSettingsView.setEnabled(((Boolean) newValue).booleanValue());
             return true;
         } else if (preference.equals(mSystemUISettingsLocation)) {
             String value = (String) newValue;
             if (value.equals("disabled")) {
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_ENABLED, 0);
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_PHONE_TOP, 0);
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_PHONE_BOTTOM, 0);
                 preference.notifyDependencyChange(true);
                 mEosQuickSettingsView.setEnabled(false);
             } else if (value.equals("top")) {
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_ENABLED, 1);
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_PHONE_TOP, 1);
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_PHONE_BOTTOM, 0);
                 preference.notifyDependencyChange(false);
                 mEosQuickSettingsView.setEnabled(true);
             } else if (value.equals("bottom")) {
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_ENABLED, 1);
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_PHONE_TOP, 0);
                 Settings.System.putInt(getContentResolver(),
                         EOSConstants.SYSTEMUI_SETTINGS_PHONE_BOTTOM, 1);
                 preference.notifyDependencyChange(false);
                 mEosQuickSettingsView.setEnabled(true);
             }
             return true;
         } else if (preference.equals(mLowProfileNavBar)) {
             Settings.System.putInt(mContext.getContentResolver(),
                     EOSConstants.SYSTEMUI_BAR_SIZE_MODE,
                     ((Boolean) newValue).booleanValue() ? 1 : 0);
           return true;
         } else if (preference.equals(mFatFingers)) {
             Settings.System.putInt(mContext.getContentResolver(),
                     EOSConstants.SYSTEMUI_TABLET_BIG_CLEAR_BUTTON,
                     ((Boolean) newValue).booleanValue() ? 1 : 0);
             return true;
         } else if (preference.equals(mHideIndicator)) {
             Settings.System.putInt(mContext.getContentResolver(),
                     EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_HIDDEN,
                     ((Boolean) newValue).booleanValue() ? 1 : 0);
             return true;
         }
         return false;
     }
 
     private boolean hasTorch() {
         Camera mCamera = null;
         Parameters parameters;
         try {
             mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            parameters = mCamera.getParameters();
             List<String> flashModes = parameters.getSupportedFlashModes();
             if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) return true;
         } catch (RuntimeException e) {
            Log.i("EosInterfaceSettings", "Unable to acquire camera or failed to check if device is Torch capable");
         }
         finally {
            if(mCamera != null){
                mCamera.release();
                mCamera = null;
            }
         }
         return false;
     }
 }
