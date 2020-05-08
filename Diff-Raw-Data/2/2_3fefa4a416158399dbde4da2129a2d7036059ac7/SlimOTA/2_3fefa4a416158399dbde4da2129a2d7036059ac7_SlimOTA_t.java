 /*=========================================================================
  *
  *  PROJECT:  SlimRoms
  *            Team Slimroms (http://www.slimroms.net)
  *
  *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
  *            All rights reserved
  *
  *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
  *
  *  AUTHORS:     fronti90, mnazim, tchaari, kufikugel
  *  DESCRIPTION: SlimOTA keeps our rom up to date
  *
  *=========================================================================
  */
 
 package com.slim.ota;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 
 import android.app.AlarmManager;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.slim.ota.updater.UpdateChecker;
 import com.slim.ota.updater.UpdateListener;
 import com.slim.ota.settings.Settings;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 
 public class SlimOTA extends Activity implements OnSharedPreferenceChangeListener{
 
     private static final int ID_DEVICE_NAME = R.id.deviceName;
     private static final int ID_DEVICE_CODE_NAME = R.id.deviceCodename;
     private static final int ID_CURRENT_VERSION = R.id.curVer;
     private static final int ID_CURRENT_FILE = R.id.curFile;
     private static final int ID_UPDATE_FILE = R.id.upToDate;
     private static final int ID_STATUS_IMAGE = R.id.updateIcon;
 
     private static final String LAST_INTERVAL = "lastInterval";
 
     private TextView mDeviceOut;
     private TextView mCodenameOut;
     private TextView mCurVerOut;
     private TextView mCurFileOut;
     private TextView mUpdateFile;
     private ImageView mStatusIcon;
 
     private String mStrDevice;
     private String mStrCodename;
     private String mStrCurVer;
     private String mStrCurFile;
     private String mStrUpToDate;
 
     SharedPreferences prefs;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.slim_ota);
 
         mDeviceOut = (TextView) findViewById(ID_DEVICE_NAME);
         mCodenameOut = (TextView) findViewById(ID_DEVICE_CODE_NAME);
         mCurVerOut = (TextView) findViewById(ID_CURRENT_VERSION);
         mCurFileOut = (TextView) findViewById(ID_CURRENT_FILE);
         mUpdateFile = (TextView) findViewById(ID_UPDATE_FILE);
         mStatusIcon = (ImageView) findViewById(ID_STATUS_IMAGE);
 
         prefs = getSharedPreferences("UpdateChecker", 0);
         prefs.registerOnSharedPreferenceChangeListener(this);
 
         if (UpdateChecker.connectivityAvailable(SlimOTA.this)) {
            doTheUpdateCheck();
         } else {
            Toast.makeText(SlimOTA.this, R.string.toast_no_data_text, Toast.LENGTH_LONG).show();
         }
 
         setDeviceInfoContainer();
         addShortCutFragment();
 
         setInitialUpdateInterval();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.slim_ota_base_menu, menu);
         return true;
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         if (key.equalsIgnoreCase("Filename")) {
             setDeviceInfoContainer();
             addShortCutFragment();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if (prefs != null) prefs.unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         prefs = getSharedPreferences("UpdateChecker", 0);
         prefs.registerOnSharedPreferenceChangeListener(this);
     }
 
     private void doTheUpdateCheck(){
         UpdateChecker otaChecker = new UpdateChecker();
         otaChecker.execute(SlimOTA.this);
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_settings:
                 Intent intent = new Intent(this, Settings.class);
                 startActivity(intent);
                 return true;
             case R.id.menu_update:
                 doTheUpdateCheck();
                setDeviceInfoContainer();
                addShortCutFragment();
                 return true;
              default:
                 return super.onContextItemSelected(item);
         }
     }
 
     private void setDeviceInfoContainer() {
         try {
             FileInputStream fstream = new FileInputStream("/system/build.prop");
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 String[] line = strLine.split("=");
                 if (line[0].equals("ro.product.device")) {
                     mStrCodename = line[1];
                 } else if (line[0].equals("slim.ota.version")) {
                     mStrCurVer = line[1];
                 } else if (line[0].equals("ro.product.model")) {
                     mStrDevice = line[1];
                 } else if (line[0].equals("ro.modversion")) {
                     mStrCurFile = line[1];
                 }
             }
             in.close();
         } catch (Exception e) {
             Toast.makeText(getBaseContext(), getString(R.string.system_prop_error),
                     Toast.LENGTH_LONG).show();
             e.printStackTrace();
         }
 
         mDeviceOut.setText(getString(R.string.device_name_title) + " " + mStrDevice);
         mCodenameOut.setText(getString(R.string.codename_title) + " " + mStrCodename);
         mCurVerOut.setText(getString(R.string.version_title) + " " + mStrCurVer);
         mCurFileOut.setText(getString(R.string.file_name_title) + " " + mStrCurFile);
 
         SharedPreferences prefs = getSharedPreferences("UpdateChecker", 0);
         String updateFile = prefs.getString("Filename", "");
 
         mUpdateFile.setTextColor(Color.RED);
 
         if (updateFile.equals(mStrCurFile)) {
             mUpdateFile.setTextColor(Color.GREEN);
             mStrUpToDate = getString(R.string.up_to_date_title);
             mStatusIcon.setImageResource(R.drawable.ic_uptodate);
         } else if (!UpdateChecker.connectivityAvailable(SlimOTA.this)) {
             mStrUpToDate = getString(R.string.no_data_title);
             mStatusIcon.setImageResource(R.drawable.ic_no_data);
         } else if (updateFile.equals("")) {
             mStrUpToDate = getString(R.string.error_reading_title);
             mStatusIcon.setImageResource(R.drawable.ic_no_data);
         } else {
             mStatusIcon.setImageResource(R.drawable.ic_need_update);
             mStrUpToDate = updateFile;
         }
 
         mUpdateFile.setText(" " + mStrUpToDate);
     }
 
     private void addShortCutFragment() {
         FragmentManager fragmentManager = getFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         ShortCutFragment shortCut = new ShortCutFragment();
         fragmentTransaction.replace(R.id.shortCutsFragment, shortCut);
         fragmentTransaction.commit();
     }
 
     private void setInitialUpdateInterval() {
         SharedPreferences prefs = getSharedPreferences(LAST_INTERVAL, 0);
         long value = prefs.getLong(LAST_INTERVAL,0);
         //set interval to 12h if user starts first time SlimOTA and it was not installed by system before
         //yes ask lazy tarak....he has this case ;)
         if (value == 0) {
             UpdateListener.interval = AlarmManager.INTERVAL_HALF_DAY;
             prefs.edit().putLong(LAST_INTERVAL, UpdateListener.interval).apply();
             WakefulIntentService.scheduleAlarms(new UpdateListener(), this, false);
         }
     }
 
 }
