 /*
  * Copyright 2013 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package oly.netpowerctrl.main;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcEvent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 
 import oly.netpowerctrl.R;
 import oly.netpowerctrl.datastructure.DeviceCollection;
 import oly.netpowerctrl.datastructure.SceneCollection;
 import oly.netpowerctrl.listadapter.DeviceListAdapter;
 import oly.netpowerctrl.listadapter.DrawerAdapter;
 import oly.netpowerctrl.listadapter.OutletSwitchListAdapter;
 import oly.netpowerctrl.listadapter.ScenesListAdapter;
 import oly.netpowerctrl.plugins.PluginController;
 import oly.netpowerctrl.preferences.PreferencesFragment;
 import oly.netpowerctrl.preferences.SharedPrefs;
 import oly.netpowerctrl.utils.JSONHelper;
 import oly.netpowerctrl.utils.NFC;
 
 public class NetpowerctrlActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {
     public static NetpowerctrlActivity instance = null;
 
     // Drawer
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private View mDrawerView;
     private ActionBarDrawerToggle mDrawerToggle;
     private DrawerAdapter mDrawerAdapter;
     private CharSequence mDrawerTitle;
     private CharSequence mTitle;
     private boolean drawerControllableByMenuKey = false;
 
     // Core
     private DeviceListAdapter adpConfiguredDevices;
     private OutletSwitchListAdapter adpOutlets;
     private ScenesListAdapter adpScenes;
 
     BroadcastReceiver wifiChangedListener = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             //WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
             Handler handler = new Handler();
             handler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     if (NetpowerctrlApplication.instance != null)
                         NetpowerctrlApplication.instance.detectNewDevicesAndReachability(true);
                 }
             }, 1500);
         }
     };
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (isFinishing())
             NetpowerctrlApplication.instance.pluginController.destroy();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         // Set theme, call super onCreate and set content view
         if (SharedPrefs.isDarkTheme(this)) {
             setTheme(R.style.Theme_CustomDarkTheme);
         } else {
             setTheme(R.style.Theme_CustomLightTheme);
         }
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         instance = this;
 
         // Create view adapters
         adpConfiguredDevices = new DeviceListAdapter(this, false);
         adpOutlets = new OutletSwitchListAdapter(this);
         adpScenes = new ScenesListAdapter(this);
 
         // References for the drawer
         mTitle = mDrawerTitle = getTitle();
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
         mDrawerView = findViewById(R.id.left_drawer);
 
         // Hack to always show the overflow of the actionbar instead of
         // relying on the menu button that is only present on some devices
         // and may cause confusion.
         try {
             Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
             if (menuKeyField != null) {
                 menuKeyField.setAccessible(true);
                 menuKeyField.setBoolean(ViewConfiguration.get(this), false);
             }
         } catch (Exception ex) {
             // Ignore
         }
 
         try {
             //noinspection ConstantConditions
             ((TextView) findViewById(R.id.version)).setText(getResources().getText(R.string.Version) + " " +
                     getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
         } catch (PackageManager.NameNotFoundException ignored) {
         }
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         // set up the drawer's list view with items and click listener
 
         mDrawerAdapter = new DrawerAdapter(this);
         mDrawerAdapter.add(getResources().getStringArray(R.array.drawer_titles_outlets),
                 getResources().getStringArray(R.array.drawer_descriptions_outlets),
                 new String[]{"", OutletsFragment.class.getName(), ScenesFragment.class.getName()});
 
         mDrawerAdapter.add(getResources().getStringArray(R.array.drawer_titles_devices),
                 getResources().getStringArray(R.array.drawer_descriptions_devices),
                 new String[]{"", NewDevicesListFragment.class.getName(), ConfiguredDevicesListFragment.class.getName()});
 
         mDrawerAdapter.usePositionForPlugins();
         mDrawerAdapter.add(getResources().getStringArray(R.array.drawer_titles_app),
                 getResources().getStringArray(R.array.drawer_descriptions_app),
                 new String[]{"", PreferencesFragment.class.getName(), HelpFragment.class.getName(), FeedbackDialog.class.getName()});
 
         mDrawerAdapter.addCacheFragment(OutletsFragment.class.getName());
 
         mDrawerList.setAdapter(mDrawerAdapter);
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
         // Plugins
         if (NetpowerctrlApplication.instance.pluginController == null)
             NetpowerctrlApplication.instance.pluginController = new PluginController(this, mDrawerAdapter);
        else
            NetpowerctrlApplication.instance.pluginController.recreate();
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         //noinspection ConstantConditions
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // ActionBarDrawerToggle ties together the the proper interactions
         // between the sliding drawer and the action bar app icon
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
         ) {
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             public void onDrawerOpened(View drawerView) {
                 getActionBar().setTitle(mDrawerTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         // Restore the last visited screen
         int pos;
         if (savedInstanceState == null) {
             pos = mDrawerAdapter.indexOf(SharedPrefs.getFirstTab(this));
         } else {
             pos = mDrawerAdapter.indexOf(savedInstanceState.getString("lastFragment"));
         }
         if (pos == -1)
             pos = mDrawerAdapter.indexOf(HelpFragment.class.getName());
         selectItem(pos);
 
         // NFC
         NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         if (mNfcAdapter != null) {
             // Register callback
             mNfcAdapter.setNdefPushMessageCallback(this, this);
         }
     }
 
     /* Called whenever we call invalidateOptionsMenu() */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // If the nav drawer is open, hide action items related to the content view
         boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerView);
         drawerControllableByMenuKey = menu.size() <= 2;
         if (drawerOpen)
             for (int i = 0; i < menu.size(); i++) {
                 menu.getItem(i).setVisible(false);
             }
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     protected void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle icicle) {
         super.onSaveInstanceState(icicle);
         // Save current tab
         final int currentPosition = mDrawerList.getCheckedItemPosition();
         if (mDrawerAdapter.getItemViewType(currentPosition) != 0) {
             DrawerAdapter.DrawerItem item = (DrawerAdapter.DrawerItem) mDrawerAdapter.getItem(currentPosition);
             if (!item.mClazz.equals(""))
                 icicle.putString("lastFragment", item.mClazz);
         }
     }
 
     @Override
     public boolean onKeyUp(int keyCode, @SuppressWarnings("NullableProblems") KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_MENU && drawerControllableByMenuKey) {
             if (mDrawerLayout.isDrawerOpen(mDrawerView))
                 mDrawerLayout.closeDrawer(mDrawerView);
             else
                 mDrawerLayout.openDrawer(mDrawerView);
         }
         return super.onKeyUp(keyCode, event);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         // Save current tab
         final int currentPosition = mDrawerList.getCheckedItemPosition();
         if (mDrawerAdapter.getItemViewType(currentPosition) != 0) {
             DrawerAdapter.DrawerItem item = (DrawerAdapter.DrawerItem) mDrawerAdapter.getItem(currentPosition);
             if (!item.mClazz.equals(""))
                 SharedPrefs.setFirstTab(this, item.mClazz);
         }
 
         // Stop listener
         NetpowerctrlApplication.instance.stopListener();
         unregisterReceiver(wifiChangedListener);
     }
 
     @Override
     public NdefMessage createNdefMessage(NfcEvent event) {
         String text;
         try {
             JSONHelper h = new JSONHelper();
             NFC.NFC_Transfer.fromData(
                     SceneCollection.fromScenes(adpScenes.getScenes()),
                     DeviceCollection.fromDevices(NetpowerctrlApplication.instance.configuredDevices)).toJSON(h.createWriter());
             text = h.getString();
         } catch (IOException e) {
             Log.w("createNdefMessage", e.getMessage());
             return null;
         }
 
         if (Build.VERSION.SDK_INT < 14) {
             NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                     "application/oly.netpowerctrl".getBytes(),
                     new byte[0], text.getBytes());
             return new NdefMessage(new NdefRecord[]{
                     mimeRecord,
                     NdefRecord.createApplicationRecord("oly.netpowerctrl")
             });
         } else {
             return new NdefMessage(
                     NdefRecord.createMime("application/oly.netpowerctrl", text.getBytes()),
                     NdefRecord.createApplicationRecord("oly.netpowerctrl")
             );
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         // Check to see that the Activity started due to an Android Beam
         Intent intent = getIntent();
         String intentAction = intent.getAction();
 
         if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentAction)) {
             Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
             // only one message sent during the beam
             assert rawMessages != null;
             NdefMessage msg = (NdefMessage) rawMessages[0];
             NFC.parseNFC(this, new String(msg.getRecords()[0].getPayload()));
         }
 
         // Listen for wifi changes
         IntentFilter filter = new IntentFilter();
         filter.addAction("android.net.wifi.STATE_CHANGE");
         filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
         registerReceiver(wifiChangedListener, filter);
 
         // Start listener and request new device states after around 800ms
         // Its better to request device state after the gui has established itself
         // even on slower devices before starting the listener service.
         Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 NetpowerctrlApplication.instance.startListener(true);
             }
         }, 800);
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         // onResume gets called after this to handle the intent
         setIntent(intent);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
     }
 
 
     /* The click listener for ListView in the navigation drawer */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             selectItem(position);
         }
     }
 
     private void selectItem(int position) {
         if (mDrawerAdapter.getItemViewType(position) == 0)
             return;
         DrawerAdapter.DrawerItem item = (DrawerAdapter.DrawerItem) mDrawerAdapter.getItem(position);
         if (item.mClazz.equals(""))
             return;
 
         Fragment fragment = mDrawerAdapter.getCachedFragment(item.mClazz);
         if (fragment == null)
             fragment = Fragment.instantiate(this, item.mClazz);
 
         if (item.mExtra != -1) {
             Bundle b = new Bundle();
             b.putInt("extra", item.mExtra);
             fragment.setArguments(b);
         }
         FragmentManager fragmentManager = getFragmentManager();
         if (item.mDialog) {
             FragmentTransaction ft = getFragmentManager().beginTransaction();
             Fragment prev = getFragmentManager().findFragmentByTag("dialog");
             if (prev != null) {
                 ft.remove(prev);
             }
             ft.addToBackStack(null);
             ((DialogFragment) fragment).show(ft, "dialog");
         } else {
             fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
             // update selected item and title, then close the drawer
             mDrawerList.setItemChecked(position, true);
             setTitle(item.mTitle);
         }
 
         Handler mHandler = new Handler();
         mHandler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 mDrawerLayout.closeDrawer(mDrawerView);
             }
         }, 150);
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
         //noinspection ConstantConditions
         getActionBar().setTitle(mTitle);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggle
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     public DeviceListAdapter getConfiguredDevicesAdapter() {
         return adpConfiguredDevices;
     }
 
     public OutletSwitchListAdapter getOutletsAdapter() {
         return adpOutlets;
     }
 
     public ScenesListAdapter getScenesAdapter() {
         return adpScenes;
     }
 }
