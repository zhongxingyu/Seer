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
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcEvent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.IOException;
 
 import oly.netpowerctrl.R;
 import oly.netpowerctrl.anelservice.DeviceQuery;
 import oly.netpowerctrl.datastructure.DeviceCollection;
 import oly.netpowerctrl.listadapter.AdapterController;
 import oly.netpowerctrl.listadapter.DrawerAdapter;
 import oly.netpowerctrl.preferences.PreferencesFragment;
 import oly.netpowerctrl.preferences.SharedPrefs;
 import oly.netpowerctrl.utils.NFC;
 
 public class NetpowerctrlActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {
     public static NetpowerctrlActivity _this = null;
 
     // Drawer
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private View mDrawerView;
     private ActionBarDrawerToggle mDrawerToggle;
     private DrawerAdapter mDrawerAdapter;
     private CharSequence mDrawerTitle;
     private CharSequence mTitle;
 
     // Core
     public AdapterController adapterUpdateManger;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         _this = this;
         setContentView(R.layout.activity_main);
 
         mTitle = mDrawerTitle = getTitle();
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
         mDrawerView = findViewById(R.id.left_drawer);
 
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
 
         mDrawerAdapter.add(getResources().getStringArray(R.array.drawer_titles_app),
                 getResources().getStringArray(R.array.drawer_descriptions_app),
                 new String[]{"", PreferencesFragment.class.getName(), HelpFragment.class.getName(), FeedbackDialog.class.getName()});
 
 
         mDrawerList.setAdapter(mDrawerAdapter);
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
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
         if (savedInstanceState == null) {
             int pos = SharedPrefs.getFirstTab(this);
             if (pos == -1)
                 pos = mDrawerAdapter.indexOf(HelpFragment.class.getName());
             selectItem(pos);
         } else {
             selectItem(savedInstanceState.getInt("navigation"));
         }
 
         // NFC
         NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         if (mNfcAdapter != null) {
             // Register callback
             mNfcAdapter.setNdefPushMessageCallback(this, this);
         }
 
         // Core
         adapterUpdateManger = new AdapterController(this);
     }
 
     /* Called whenever we call invalidateOptionsMenu() */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // If the nav drawer is open, hide action items related to the content view
         boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerView);
         if (drawerOpen)
             for (int i = 0; i < menu.size(); i++) {
                 menu.getItem(i).setVisible(false);
             }
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     protected void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle icicle) {
         super.onSaveInstanceState(icicle);
         icicle.putInt("navigation", mDrawerList.getCheckedItemPosition());
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         SharedPrefs.setFirstTab(this, mDrawerList.getCheckedItemPosition());
         // Stop listener
         NetpowerctrlApplication.instance.stopListener();
     }
 
     @Override
     public NdefMessage createNdefMessage(NfcEvent event) {
         String text = null;
         try {
             text = DeviceCollection.fromDevices(NetpowerctrlApplication.instance.configuredDevices).toJSON();
         } catch (IOException ignored) {
            Log.w("DeviceCollection.fromDevices", ignored.toString());
             return null;
         }
 
         return new NdefMessage(
                NdefRecord.createMime("application/oly.netpowerctrl", text.getBytes()),
                NdefRecord.createApplicationRecord("oly.netpowerctrl")
         );
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
            NFC.showSelectionDialog(this, new String(msg.getRecords()[0].getPayload()));
         }
 
         // Start listener and request new device states
         NetpowerctrlApplication.instance.startListener();
         DeviceQuery.sendBroadcastQuery(this);
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
 
         Fragment fragment = Fragment.instantiate(this, item.mClazz);
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
 
         mDrawerLayout.closeDrawer(mDrawerView);
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
         //noinspection ConstantConditions
         getActionBar().setTitle(mTitle);
     }
 
     /**
      * When using the ActionBarDrawerToggle, you must call it during
      * onPostCreate() and onConfigurationChanged()...
      */
 
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
 }
