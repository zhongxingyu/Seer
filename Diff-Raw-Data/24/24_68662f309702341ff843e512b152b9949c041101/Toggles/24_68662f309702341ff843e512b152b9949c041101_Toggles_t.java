 /*
  * Copyright (C) 2011 The CyanogenMod Project
  * Copyright (C) 2011 The AOKP Project
  * This code has been modified.  Portions copyright (C) 2012, ParanoidAndroid Project.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.settings.cyanogenmod;
 
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.wimax.WimaxHelper;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.android.settings.Utils;
 
 import com.android.internal.telephony.Phone;
 import com.android.settings.cyanogenmod.TouchInterceptor;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.R;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Toggles extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
 
     private static final String TAG = "TogglesLayout";
 
     private static final String PREF_SHOW_TOGGLES = "enable_toggles";
     private static final String PREF_ENABLED_TOGGLES = "enabled_toggles";
     private static final String PREF_SHOW_BRIGHTNESS = "show_brightness_slider";
     private static final String PREF_TOGGLES_STYLE = "toggle_style";
     private static final String PREF_TOGGLES_LAYOUT = "toggles_layout";
     private static final String PREF_TOGGLES_DISABLE_SCROLLING = "disable_scrollbar";
     private static final String PREF_ALT_BUTTON_LAYOUT = "toggles_layout_preference";
 
     private static final int LAYOUT_SWITCH = 0;
     private static final int LAYOUT_TOGGLE = 1;
     private static final int LAYOUT_BUTTON = 2;
     private static final int LAYOUT_MULTIROW = 3;
 
     private static final int ROTATE = 0;
     private static final int BLUETOOTH = 1;
     private static final int NETWORK = 2;
     private static final int DATA = 3;
     private static final int GPS = 4;
     private static final int WIFI = 5;
     private static final int WIFI_AP = 7;
     private static final int USB_TETHER = 8;
     private static final int LTE = 10;
     private static final int TORCH = 12;
     private static final int NFC = 14;
     private static final int DONOTDISTURB = 15;
 
     // Arrays containing the entire set of toggles
     private static ArrayList<String> allEntries;
     private static ArrayList<String> allValues;
 
     // Filtered entries, removed unexistent hardware
     private static String[] mValues;
     private static String[] mEntries;
 
     private static PackageManager pm;
 
     private static Context mContext;
 
     CheckBoxPreference mShowToggles;
     Preference mEnabledToggles;
     Preference mLayout;
     CheckBoxPreference mDisableScrolling;
     CheckBoxPreference mShowBrightness;
     ListPreference mTogglesLayout;
     ListPreference mToggleStyle;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.notification_drawer_toggles);
 
         mContext = getActivity();
 
         mShowToggles = (CheckBoxPreference) findPreference(PREF_SHOW_TOGGLES);
         mShowToggles.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_TOGGLES_ENABLE, 0) == 1);
 
         mDisableScrolling = (CheckBoxPreference) findPreference(PREF_TOGGLES_DISABLE_SCROLLING);
         mDisableScrolling.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_TOGGLES_DISABLE_SCROLL, 0) == 1);
 
         mShowBrightness = (CheckBoxPreference) findPreference(PREF_SHOW_BRIGHTNESS);
         mShowBrightness.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_TOGGLES_SHOW_BRIGHTNESS, 0) == 1);
 
         mToggleStyle = (ListPreference) findPreference(PREF_TOGGLES_STYLE);
         mToggleStyle.setOnPreferenceChangeListener(this);
         mToggleStyle.setValue(Integer.toString(Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_TOGGLES_STYLE, LAYOUT_TOGGLE)));
 
         int val = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS, 1);
 
         mTogglesLayout = (ListPreference) findPreference(PREF_ALT_BUTTON_LAYOUT);
         mTogglesLayout.setOnPreferenceChangeListener(this);
         mTogglesLayout.setValue(Integer.toString(val));
 
         mEnabledToggles = findPreference(PREF_ENABLED_TOGGLES);
 
         mLayout = findPreference(PREF_TOGGLES_LAYOUT);
 
        adjustPreferences(val);

         pm = mContext.getPackageManager();
 
         getAvailableToggleList(getResources()
             .getStringArray(R.array.available_toggles_entries), getResources()
             .getStringArray(R.array.available_toggles_values));
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         if(preference == mShowToggles) {
             boolean value = mShowToggles.isChecked();
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_TOGGLES_ENABLE, value ? 1 : 0);
             return true;
         } else if(preference == mDisableScrolling) {
             boolean value = mDisableScrolling.isChecked();
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_TOGGLES_DISABLE_SCROLL, value ? 1 : 0);
             return true;
         } else if (preference == mShowBrightness) {
             boolean value = mShowBrightness.isChecked();
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_TOGGLES_SHOW_BRIGHTNESS, value ? 1 : 0);
             return true;
         } else if (preference == mEnabledToggles) {
             AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 
             ArrayList<String> enabledToggles = getTogglesStringArray(mContext);
 
             boolean checkedToggles[] = new boolean[mValues.length];
 
             for (int i = 0; i < checkedToggles.length; i++) {
                 if (enabledToggles.contains(mValues[i])) {
                     checkedToggles[i] = true;
                 }
             }
 
             builder.setTitle(R.string.toggles_display_dialog);
             builder.setCancelable(true);
             builder.setPositiveButton(R.string.toggles_display_close,
                     new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     });
 
             builder.setMultiChoiceItems(mEntries, checkedToggles, new OnMultiChoiceClickListener() {
 
                 @Override
                 public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                     String toggleKey = (mValues[which]);
 
                     if (isChecked) {
                         addToggle(mContext, toggleKey);
                     } else {
                         removeToggle(mContext, toggleKey);
                     }
                 }
             });
 
             AlertDialog d = builder.create();
 
             d.show();
 
             return true;
         } else if (preference == mLayout) {
             FragmentTransaction ft = getFragmentManager().beginTransaction();
             TogglesLayout fragment = new TogglesLayout();
             ft.addToBackStack(PREF_TOGGLES_LAYOUT);
             ft.replace(this.getId(), fragment);
             ft.commit();
             return true;
         }
         return false;
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         boolean result = false;
         if (preference == mToggleStyle) {
             int val = Integer.parseInt((String) newValue);
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_TOGGLES_STYLE, val);
             return true;
         } else if (preference == mTogglesLayout) {
             int val = Integer.parseInt((String) newValue);
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS, val);
            adjustPreferences(val);
             return true;
         }
         return false;
     }
 
    public void adjustPreferences(int val) {
        mToggleStyle.setEnabled(!(val == LAYOUT_BUTTON ||
                val == LAYOUT_MULTIROW));

        mDisableScrolling.setEnabled(!(val == LAYOUT_SWITCH ||
                val == LAYOUT_MULTIROW));
    }

     public static void addToggle(Context context, String key) {
         ArrayList<String> enabledToggles = getTogglesStringArray(context);
         enabledToggles.add(key);
         setTogglesFromStringArray(context, enabledToggles);
     }
 
     public static void removeToggle(Context context, String key) {
         ArrayList<String> enabledToggles = getTogglesStringArray(context);
         enabledToggles.remove(key);
         setTogglesFromStringArray(context, enabledToggles);
     }
 
     public static class TogglesLayout extends ListFragment {
 
         private ListView mButtonList;
         private ButtonAdapter mButtonAdapter;
         private Context mTogglesContext;
 
         /** Called when the activity is first created. */
         @Override
         public void onCreate(Bundle icicle) {
             super.onCreate(icicle);
 
             mTogglesContext = getActivity().getBaseContext();
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             // Inflate the layout for this fragment
             View v = inflater.inflate(R.layout.order_power_widget_buttons_activity, container,
                     false);
 
             return v;
         }
 
         public void onActivityCreated(Bundle savedInstanceState) {
             super.onActivityCreated(savedInstanceState);
             mButtonList = this.getListView();
             ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
             mButtonAdapter = new ButtonAdapter(mTogglesContext);
             setListAdapter(mButtonAdapter);
         };
 
         @Override
         public void onDestroy() {
             ((TouchInterceptor) mButtonList).setDropListener(null);
             setListAdapter(null);
             super.onDestroy();
         }
 
         @Override
         public void onResume() {
             super.onResume();
             // reload our buttons and invalidate the views for redraw
             mButtonAdapter.reloadButtons();
             mButtonList.invalidateViews();
         }
 
         private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
             public void drop(int from, int to) {
                 // get the current button list
                 ArrayList<String> toggles = getTogglesStringArray(mContext);
 
                 // move the button
                 if (from < toggles.size()) {
                     String toggle = toggles.remove(from);
 
                     if (to <= toggles.size()) {
                         toggles.add(to, toggle);
 
                         // save our buttons
                         setTogglesFromStringArray(mTogglesContext, toggles);
 
                         // tell our adapter/listview to reload
                         mButtonAdapter.reloadButtons();
                         mButtonList.invalidateViews();
                     }
                 }
             }
         };
 
         private class ButtonAdapter extends BaseAdapter {
             private Context mButtonContext;
             private Resources mSystemUIResources = null;
             private LayoutInflater mInflater;
             private ArrayList<Toggle> mToggles;
 
             public ButtonAdapter(Context c) {
                 mButtonContext = c;
                 mInflater = LayoutInflater.from(mButtonContext);
 
                 if (pm != null) {
                     try {
                         mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                     } catch (Exception e) {
                         mSystemUIResources = null;
                         Log.e(TAG, "Could not load SystemUI resources", e);
                     }
                 }
 
                 reloadButtons();
             }
 
             public void reloadButtons() {
                 mToggles = new ArrayList<Toggle>();
                 ArrayList<String> toggleArray = getTogglesStringArray(mButtonContext);
 
                 for(String toggle : toggleArray) {
                     if(!toggle.equals("")) {
                         mToggles.add(new Toggle(toggle));
                     }
                 }
             }
 
             public int getCount() {
                 return mToggles.size();
             }
 
             public Object getItem(int position) {
                 return mToggles.get(position);
             }
 
             public long getItemId(int position) {
                 return position;
             }
 
             public View getView(int position, View convertView, ViewGroup parent) {
                 final View v;
                 if (convertView == null) {
                     v = mInflater.inflate(R.layout.order_power_widget_button_list_item, null);
                 } else {
                     v = convertView;
                 }
 
                 Toggle toggle = mToggles.get(position);
                 final TextView name = (TextView) v.findViewById(R.id.name);
 
                 for(int i = 0; i < mValues.length; i++) {
                     if(toggle.getId().equals(mValues[i])) {
                         name.setText(mEntries[i]);
                         break;
                     }
                 }
 
                 return v;
             }
         }
 
     }
 
     public static class Toggle {
         private String mId;
 
         public Toggle(String id) {
             mId = id;
         }
 
         public String getId() {
             return mId;
         }
     }
 
     public static void getAvailableToggleList(String[] entries, String[] values) {
         allEntries = new ArrayList<String>(Arrays.asList(entries));
         allValues = new ArrayList<String>(Arrays.asList(values));
 
         // Check if device has gyroscope
         if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
             removeEntry(values[ROTATE]);
         }
 
         // Check if device has bluetooth
         if (!pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
             removeEntry(values[BLUETOOTH]);
         }
 
         // Check if device has network capabilities
         boolean hasMobileData = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
         if (!hasMobileData) {
             removeEntry(values[NETWORK]);
             removeEntry(values[DATA]);
         }
 
         // Check if device has GPS
         if(!pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
             removeEntry(values[GPS]);
         }
 
         // Check if device has Wi-Fi
         if(!pm.hasSystemFeature(PackageManager.FEATURE_WIFI)) {
             removeEntry(values[WIFI]);
         }
 
         // Check if device has tethering capabilities
         ConnectivityManager cm =
                 (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
 
         String[] mUsbRegexs = cm.getTetherableUsbRegexs();
         String[] mWifiRegexs = cm.getTetherableWifiRegexs();
 
         final boolean usbAvailable = mUsbRegexs.length != 0;
         final boolean wifiAvailable = mWifiRegexs.length != 0;
 
         if (!wifiAvailable) {
             removeEntry(values[WIFI_AP]);
         }
 
         if (!usbAvailable) {
             removeEntry(values[USB_TETHER]);
         }
 
         // Check if device has LTE
         if(Phone.LTE_ON_CDMA_TRUE != TelephonyManager.getDefault().getLteOnCdmaMode() ||
                 TelephonyManager.getDefault().getLteOnGsmMode() == 0) {
             removeEntry(values[LTE]);
         }
 
         // Check if torch app is installed
         try{
             ApplicationInfo info = pm.getApplicationInfo("net.cactii.flash2", 0);
         } catch(PackageManager.NameNotFoundException e){
             removeEntry(values[TORCH]);
         }
 
         // Check if device has NFC
         if(!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
             removeEntry(values[NFC]);
         }
 
         mEntries = allEntries.toArray(new String[allEntries.size()]);
         mValues = allValues.toArray(new String[allValues.size()]);
     }
 
     public static void removeEntry(String entry) {
         for(int i = 0; i < allValues.size(); i++) {
             if(allValues.get(i).equals(entry)) {
                 allValues.remove(i);
                 allEntries.remove(i);
             }
         }
     }
 
     public static void setTogglesFromStringArray(Context c, ArrayList<String> newGoodies) {
         String newToggles = "";
 
         for (int i = 0; i < newGoodies.size(); i++) {
             newToggles += newGoodies.get(i);
 
             if(i + 1 < newGoodies.size()) {
                 newToggles += "|";
             }
         }
 
         Settings.System.putString(c.getContentResolver(), Settings.System.STATUSBAR_TOGGLES,
                 newToggles);
     }
 
     public static ArrayList<String> getTogglesStringArray(Context c) {
         String cluster = Settings.System.getString(c.getContentResolver(),
                 Settings.System.STATUSBAR_TOGGLES);
 
         if (cluster == null) {
             Log.e(TAG, "cluster was null");
             cluster = "|";
         }
 
         String[] togglesStringArray = cluster.split("\\|");
 
         return new ArrayList<String>(Arrays.asList(togglesStringArray));
     }
 }
