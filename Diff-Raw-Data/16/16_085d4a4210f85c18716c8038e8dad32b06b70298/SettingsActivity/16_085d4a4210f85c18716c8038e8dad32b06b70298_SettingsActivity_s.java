 package uk.co.blackpepper.penguin;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
 
 import java.util.List;
 
 public class SettingsActivity extends PreferenceActivity {
 
 	private static final boolean ALWAYS_SIMPLE_PREFS = false;
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 
 		setupSimplePreferencesScreen();
 	}
 
 	private void setupSimplePreferencesScreen() {
 		if (!isSimplePreferences(this)) {
 			return;
 		}
 
 		addPreferencesFromResource(R.xml.pref_general);
 
 		bindPreferenceSummaryToValue(findPreference("example_text"));
 		bindPreferenceSummaryToValue(findPreference("penguin_server_url"));
 	}
 
 	@Override
 	public boolean onIsMultiPane() {
 		return isXLargeTablet(this) && !isSimplePreferences(this);
 	}
 
 	private static boolean isXLargeTablet(Context context) {
 		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
 	}
 
 	private static boolean isSimplePreferences(Context context) {
 		return ALWAYS_SIMPLE_PREFS
 				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
 				|| !isXLargeTablet(context);
 	}
 
 	@Override
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public void onBuildHeaders(List<Header> target) {
 		if (!isSimplePreferences(this)) {
 			loadHeadersFromResource(R.xml.pref_headers, target);
 		}
 	}
 
 	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object value) {
 			String stringValue = value.toString();
 
 			preference.setSummary(stringValue);
 			
 			return true;
 		}
 	};
 
 	private static void bindPreferenceSummaryToValue(Preference preference) {
 		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 
 		sBindPreferenceSummaryToValueListener.onPreferenceChange(
 				preference,
 				PreferenceManager.getDefaultSharedPreferences(
 						preference.getContext()).getString(preference.getKey(),
 						""));
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public static class GeneralPreferenceFragment extends PreferenceFragment {
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			addPreferencesFromResource(R.xml.pref_general);
 
 			bindPreferenceSummaryToValue(findPreference("example_text"));
 			bindPreferenceSummaryToValue(findPreference("penguin_server_url"));
 		}
 	}
 }
