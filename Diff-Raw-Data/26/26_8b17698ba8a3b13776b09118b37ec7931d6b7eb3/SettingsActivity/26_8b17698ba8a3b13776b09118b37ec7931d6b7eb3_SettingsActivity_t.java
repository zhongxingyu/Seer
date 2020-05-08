 package com.bustiblelemons.tasque.main;
 
 import java.util.List;
 import java.util.Map;
 
 import com.bustiblelemons.tasque.R;
 
 import yuku.ambilwarna.widget.AmbilWarnaPreference;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 
 public class SettingsActivity extends PreferenceActivity {
 	private static final boolean ALWAYS_SIMPLE_PREFS = false;
 	private static final int SCREENLAYOUT_SIZE_XLARGE = 0x00000004;
 	private static Context context;
 	private static String regex;
 	private static String currentlyPrefix;
	
 	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 		context = getApplicationContext();
 		currentlyPrefix = context.getString(R.string.pref_currently_prefix);
 		regex = currentlyPrefix + ".*";
	}

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
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_auto_cap_characters_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_hide_keyboard_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_time_out_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_export_database_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_completion_date_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_due_date_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_list_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_input_key)));
 		PreferenceCategory fakeHeader = new PreferenceCategory(this);
         fakeHeader.setTitle(R.string.pref_time_category_title);
         getPreferenceScreen().addPreference(fakeHeader);
 		addPreferencesFromResource(R.xml.pref_date_time);
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_show_date_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_format_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_format_show_hour_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_format_show_hour_ampm_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_use_color_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_color_today_key)));
 		bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_color_overdue_key)));
 
 		addPreferencesFromResource(R.xml.pref_about);
 	}
 
 	@Override
 	public boolean onIsMultiPane() {
 		return isXLargeTablet(this) && !isSimplePreferences(this);
 	}
 
 	private static boolean isXLargeTablet(Context context) {
 		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= SCREENLAYOUT_SIZE_XLARGE;
 	}
 
 	private static boolean isSimplePreferences(Context context) {
 		return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
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
 			if (value != null) {
 				String stringValue = "";
 				String newSummary = "";
 				String currentSummary = "";
 				String newValue = "";
 				if (value instanceof Boolean) {
 					stringValue = (Boolean) value == true ? context.getString(R.string.pref_enabled) : context
 							.getString(R.string.pref_disabled);
 				} else if (value instanceof String) {
 					stringValue = value.toString();
 				}
 				if (preference instanceof ListPreference) {
 					CharSequence[] entries = ((ListPreference) preference).getEntries();
 					newValue = (String) entries[((ListPreference) preference).findIndexOfValue((String) value)];
 				} else if (preference instanceof AmbilWarnaPreference) {
 					return true;
 				} else {
 					newValue = stringValue;
 				}
 				if (preference.getSummary() != null) {
 					currentSummary = preference.getSummary().toString() + currentlyPrefix;
 					newSummary = currentSummary.replaceFirst(regex, currentlyPrefix + newValue.toString());
 				} else {
 					newSummary += currentlyPrefix;
 					newSummary = newSummary.replaceFirst(regex, currentlyPrefix + newValue.toString());
 				}
 				preference.setSummary(newSummary);
 			}
 			return true;
 		}
 	};
 
 	private static void bindPreferenceSummaryToValue(Preference preference) {
 		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 		Map<String, ?> allPrefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getAll();
 		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, allPrefs.get(preference.getKey()));
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public static class GeneralPreferenceFragment extends PreferenceFragment {
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			addPreferencesFromResource(R.xml.pref_general);
 			context = getActivity().getApplicationContext();
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_export_database_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_auto_cap_characters_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_hide_keyboard_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_time_out_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_completion_date_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_due_date_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_list_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_font_size_input_key)));
 		}
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public static class DateTimePreferenceFragment extends PreferenceFragment {
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
 			addPreferencesFromResource(R.xml.pref_date_time);
 			context = getActivity().getApplicationContext();
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_show_date_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_format_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_format_show_hour_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_date_format_show_hour_ampm_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_color_today_key)));
 			bindPreferenceSummaryToValue(findPreference(context.getString(R.string.pref_color_overdue_key)));
 		}
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public static class AboutPreferenceFragment extends PreferenceFragment {
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
 			addPreferencesFromResource(R.xml.pref_about);
 		}
 	}
 }
