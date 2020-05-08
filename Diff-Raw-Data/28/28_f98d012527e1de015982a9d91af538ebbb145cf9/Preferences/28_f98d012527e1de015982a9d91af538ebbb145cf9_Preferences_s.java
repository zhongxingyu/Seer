 /**
  * 
  */
 package com.aboveware.abovetracker;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.widget.ListView;
 
 public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 
 	private final static int SECONDS = 1000;
 	private final static int MINUTES = 60000;
 	private final static String DEFAULT_MIN_DISTANCE = "1000";
 	private final static String DEFAULT_MIN_TIME = "60";
 	private final static String DEFAULT_INTERVAL = "10";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		Customize();
 		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 	}
 
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		Customize();
 	}
 
 	private void Customize() {
 		Preference preference = findPreference(getString(R.string.min_time));
 		preference.setSummary(Html.fromHtml(getString(R.string.min_time_summary, getMinTime(this) / SECONDS)));
 		preference = findPreference(getString(R.string.min_distance));
 		preference.setSummary(Html.fromHtml(getString(R.string.min_distance_summary, getMinDistance(this))));
 		preference = findPreference(getString(R.string.sms_send_interval));
 		preference.setSummary(Html.fromHtml(getString(R.string.sms_send_interval_summary, getSmsInterval(this) / MINUTES)));
 		preference = findPreference(getString(R.string.sms_receive_keyword));
 		preference.setSummary(Html.fromHtml(getString(R.string.sms_receive_keyword_summary, getTriggerKeyword(this))));
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		Customize();
 	}
 
 	static public float getMinDistance(Context context) {
 		try {
 			SharedPreferences sharedPref = PreferenceManager
 			    .getDefaultSharedPreferences(context);
 			return Float.parseFloat(sharedPref.getString(context.getString(R.string.min_distance), DEFAULT_MIN_DISTANCE));
 		} catch (Exception e) {
 			return 1000;
 		}
 	}
 
 	static public String getTriggerKeyword(Context context) {
 		SharedPreferences sharedPref = PreferenceManager
 		    .getDefaultSharedPreferences(context);
 		return sharedPref.getString(context.getString(R.string.sms_receive_keyword), context.getString(R.string.sms_trigger_keyword_default));
 	}
 
 	static public long getMinTime(Context context) {
 		try {
 			SharedPreferences sharedPref = PreferenceManager
 			    .getDefaultSharedPreferences(context);
 			return Long.parseLong(sharedPref.getString(context.getString(R.string.min_time), DEFAULT_MIN_TIME)) * SECONDS;
 		} catch (Exception e) {
 			return 60 *1000;
 		}
 	}
 
 	static public long getSmsInterval(Context context) {
 		try {
 			SharedPreferences sharedPref = PreferenceManager
 			    .getDefaultSharedPreferences(context);
 			return Long.parseLong(sharedPref.getString(context.getString(R.string.sms_send_interval), DEFAULT_INTERVAL)) * MINUTES;
 		} catch (Exception e) {
 			return 10 * 60 * 1000;
 		}
 	}
 
	static public boolean getSmsEnabled(Context context) {
 		SharedPreferences sharedPref = PreferenceManager
 		    .getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(context.getString(R.string.sms_preferences_enable), false);
 	}
 
 	public static String getSmsRecipient(Context context) {
 		SharedPreferences sharedPref = PreferenceManager
 		    .getDefaultSharedPreferences(context);
 		return sharedPref.getString(context.getString(R.string.sms_recipient),
 		    context.getString(R.string.not_assigned));
 	}
 }
