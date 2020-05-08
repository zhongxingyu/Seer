 package edu.fsu.cs.contextprovider;
 
 import edu.fsu.cs.contextprovider.data.ContextConstants;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.text.method.DigitsKeyListener;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 	private static final String TAG = "edu.fsu.cs.PrefsActivity";
 	private static final boolean DEBUG = true;
 	
 	private static final int MENU_ABOUT_ID = Menu.FIRST;
 	private static final int ABOUT_DIALOG = 0;
 	private static final int DIALOG_ABOUT = 0;		
 
 	private SharedPreferences prefs;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
		getPreferenceManager().setSharedPreferencesName(ContextConstants.CONTEXT_PREFS);
 		addPreferencesFromResource(R.xml.prefs);
		prefs = getSharedPreferences(ContextConstants.CONTEXT_PREFS, MODE_PRIVATE);
 //      PreferenceManager.setDefaultValues(this, ContextConstants.CONTEXT_PREFS, MODE_WORLD_READABLE, R.xml.prefs, false);
 //      PreferenceManager.setDefaultValues(PrefsActivity.this, R.xml.prefs, false);
//		prefs = getPreferenceScreen().getSharedPreferences();
 //		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefs.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		prefs.unregisterOnSharedPreferenceChangeListener(this);
 //		Intent intent = new Intent(ContextConstants.CONTEXT_RESTART_INTENT);
 //		sendBroadcast(intent);
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(0, MENU_ABOUT_ID, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_ABOUT_ID:
 			showDialog(ABOUT_DIALOG);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch (id) {
 		case DIALOG_ABOUT:
 			dialog = getAboutBox();
 			break;
 
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	private AlertDialog getAboutBox() {
 		String title = getString(R.string.app_name) + " build " + getVersion(this);
 
 		return new AlertDialog.Builder(PrefsActivity.this).setTitle(title).setView(View.inflate(this, R.layout.about, null)).setIcon(R.drawable.context64).setPositiveButton("OK", null).create();
 
 	}
 
 	public static String getVersion(Context context) {
 		String version = "1.0";
 		try {
 			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
 			version = pi.versionName;
 		} catch (PackageManager.NameNotFoundException e) {
 			Log.e(TAG, "Package name not found", e);
 		}
 		return version;
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 //	    Preference pref = findPreference(key);
 //	    if (pref instanceof ListPreference) {
 //	        ListPreference listPref = (ListPreference) pref;
 //	        pref.setSummary(listPref.getEntry());
 //	    }
         if (key.equals(ContextConstants.PREFS_ACCURACY_POPUP_ENABLED) || key.equals(ContextConstants.PREFS_ACCURACY_POPUP_FREQ)) {
     		if (DEBUG) {
     			Toast.makeText(this, "ACCURACY_POPUP changed", Toast.LENGTH_SHORT).show();
     		}
         	
         	boolean accuracyPopupEnabled = prefs.getBoolean(ContextConstants.PREFS_ACCURACY_POPUP_ENABLED, true);
 //        	int accuracyPopupPeriod = prefs.getInt(ContextConstants.PREFS_ACCURACY_POPUP_FREQ, 45);
         	String accuracyPopupPeriod = prefs.getString(ContextConstants.PREFS_ACCURACY_POPUP_FREQ, "45");
         	int period = Integer.parseInt(accuracyPopupPeriod);
         	
         	if (DEBUG) {
         		Log.d(TAG, "accuracyPopupPeriod: " + accuracyPopupPeriod + "  period: " + period);
         	}
         	
         	
         	AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 			Intent intent = new Intent(getBaseContext(), edu.fsu.cs.contextprovider.wakeup.WakeupAlarmReceiver.class);
 			PendingIntent pi = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
         	
         	if (accuracyPopupEnabled) {
         		manager.cancel(pi);
     			manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, period * 1000, pi);
         	} else {
         		manager.cancel(pi);
         	}
         }	    	    	    
 	}
 	
 //	private void enabled(final boolean enable) {
 //	String dialogTitle;
 //	if (enable) {
 //		dialogTitle = getString(R.string.enable_dialog_msg);
 //	} else {
 //		dialogTitle = getString(R.string.disable_dialog_msg);
 //	}
 //	new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(dialogTitle).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 //		public void onClick(DialogInterface dialog, int whichButton) {
 //			// enable monitor here
 //		}
 //	}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 //		public void onClick(DialogInterface dialog, int whichButton) {
 //			// Do nothing
 //		}
 //	}).show();
 //}
 
 }
