 package com.dgsd.android.ShiftTracker;
 
 import android.app.backup.BackupManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.text.TextUtils;
 import android.text.format.DateFormat;
 import android.text.format.DateUtils;
 import com.actionbarsherlock.app.SherlockPreferenceActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.dgsd.android.ShiftTracker.Util.DiagnosticUtils;
 import com.dgsd.android.ShiftTracker.Util.IntentUtils;
 import com.dgsd.android.ShiftTracker.Util.Prefs;
 import com.dgsd.android.ShiftTracker.View.ListPreference;
 
 import java.text.NumberFormat;
 
 /**
  *
  * @author Daniel Grech
  */
 public class SettingsActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
 
     private Preference mWeekStartDayPref;
     private Preference mStartTimePref;
     private Preference mEndTimePref;
     private Preference mBreakDurationPref;
     private Preference mPayratePref;
 
     private Prefs mPrefs;
 
     private BackupManager mBackupManager;
 
     /**
      * Give each application a chance to set some custom preferences of their own
      */
     private static OnCreateSettingsListener mOnCreateSettingsListener;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mBackupManager = new BackupManager(this);
 
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setTitle(R.string.settings);
 
         PreferenceManager prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName(Const.SHARED_PREFS_NAME);
         prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
 
         addPreferencesFromResource(R.xml.settings);
 
         mPrefs = Prefs.getInstance(this);
 
         //Set the version string
         Preference p = prefMgr.findPreference(getString(R.string.settings_key_version));
         p.setSummary(DiagnosticUtils.getApplicationVersionString(this));
 
         mWeekStartDayPref = prefMgr.findPreference(getString(R.string.settings_key_start_day));
         mStartTimePref = prefMgr.findPreference(getString(R.string.settings_key_default_start_time));
         mEndTimePref = prefMgr.findPreference(getString(R.string.settings_key_default_end_time));
         mBreakDurationPref = prefMgr.findPreference(getString(R.string.settings_key_default_break_duration));
         mPayratePref = prefMgr.findPreference(getString(R.string.settings_key_default_pay_rate));
 
         prefMgr.findPreference(getString(R.string.settings_key_rate)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 Uri uri = Uri.parse("market://details?id=" + DiagnosticUtils.getApplicationPackage(SettingsActivity.this)) ;
                 startActivity(new Intent(Intent.ACTION_VIEW, uri));
                 return true;
             }
         });
 
         if(mOnCreateSettingsListener != null)
             mOnCreateSettingsListener.onSettingsCreated(this, prefMgr, getPreferenceScreen());
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         mWeekStartDayPref.setSummary(getListPreferenceEntry(mWeekStartDayPref));
 
         String breakDuration = mPrefs.get(getString(R.string.settings_key_default_break_duration), null);
         if(!TextUtils.isEmpty(breakDuration))
             mBreakDurationPref.setSummary(breakDuration + " mins");
 
         String payRate = mPrefs.get(getString(R.string.settings_key_default_pay_rate), null);
         if(!TextUtils.isEmpty(payRate))
             mPayratePref.setSummary(NumberFormat.getCurrencyInstance().format(Float.valueOf(payRate)) + "/hour");
 
         long startTime = mPrefs.get(getString(R.string.settings_key_default_start_time), -1L);
         if(startTime != -1)
             setTimePreference(mStartTimePref, startTime);
 
         long endTime = mPrefs.get(getString(R.string.settings_key_default_end_time), -1L);
         if(startTime != -1)
             setTimePreference(mEndTimePref, endTime);
 
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
     }
 
     private void setTimePreference(Preference p, long millis) {
         int flags = DateUtils.FORMAT_SHOW_TIME;
         if(DateFormat.is24HourFormat(this))
             flags |= DateUtils.FORMAT_24HOUR;
         else
             flags |= DateUtils.FORMAT_12HOUR;
 
         p.setSummary(DateUtils.formatDateRange(this, millis, millis, flags));
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.settings, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public void onBackPressed() {
         StApp.doDefaultNavigateUp(this);
     }
 
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
         if(item.getItemId() == android.R.id.home) {
             StApp.doDefaultNavigateUp(this);
         } else if(item.getItemId() == R.id.contact) {
             startActivity(IntentUtils.newEmailIntent(getString(R.string.support_email),
                     DiagnosticUtils.getApplicationName(this) + " support", null, "Contact support"));
         }
 
         return true;
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
         if(TextUtils.equals(key, getString(R.string.settings_key_start_day))) {
             mWeekStartDayPref.setSummary(getListPreferenceEntry(mWeekStartDayPref));
         } else if(TextUtils.equals(key, getString(R.string.settings_key_default_break_duration))) {
             String val = mPrefs.get(key, null);
             if(TextUtils.isEmpty(val)) {
                 mBreakDurationPref.setSummary(getString(R.string.settings_summary_default_break_duration));
             } else {
                 mBreakDurationPref.setSummary(val + " mins");
             }
         } else if(TextUtils.equals(key, getString(R.string.settings_key_default_pay_rate))) {
             String val = mPrefs.get(key, null);
             if(TextUtils.isEmpty(val)) {
                 mPayratePref.setSummary(getString(R.string.settings_summary_default_pay_rate));
             } else {
                 mPayratePref.setSummary(NumberFormat.getCurrencyInstance().format(Float.valueOf(val)) + "/hour");
             }
         } else if(TextUtils.equals(key, getString(R.string.settings_key_default_start_time))) {
             setTimePreference(mStartTimePref, mPrefs.get(key, 0L));
         } else if(TextUtils.equals(key, getString(R.string.settings_key_default_end_time))) {
             setTimePreference(mEndTimePref, mPrefs.get(key, 0L));
         }
 
         mBackupManager.dataChanged();
     }
 
     private CharSequence getListPreferenceEntry(Preference p) {
         if(p instanceof ListPreference)
             return ((ListPreference) p).getEntry();
         else if(p instanceof android.preference.ListPreference)
             return ((android.preference.ListPreference) p).getEntry();
         else
             return null;
     }
 
     public static void setOnCreateSettingsListener(OnCreateSettingsListener listener) {
         mOnCreateSettingsListener = listener;
     }
 
     public static interface OnCreateSettingsListener {
         public void onSettingsCreated(PreferenceActivity activity, PreferenceManager prefsManager, PreferenceScreen screen);
     }
 }
