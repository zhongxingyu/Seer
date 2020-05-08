 package uk.co.blackpepper.penguin.android;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import uk.co.blackpepper.penguin.R;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class SettingsActivity extends PreferenceActivity 
 {
 	public static final String PENGUIN_SERVER_URL_PREF_KEY = "penguin_server_url";
 	public static final String AUTHOR_NAME_PREF_KEY = "display_name";
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.pref_general);
 		
 		setupAuthorNamePreference();
 		setupServerUrlPreference(this);
 
 	}
 
 	private void setupAuthorNamePreference() 
 	{
 		EditTextPreference autorUrlPref =(EditTextPreference)getPreferenceScreen().findPreference(AUTHOR_NAME_PREF_KEY);
		
		autorUrlPref.setSummary(PreferenceManager.getDefaultSharedPreferences(this).getString(AUTHOR_NAME_PREF_KEY, "Please Configure Author Name."));
 		
 		autorUrlPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				String stringValue = newValue.toString();
 				preference.setSummary(stringValue);
                 return true;			
             }
 		});
 	}
 
 	private void setupServerUrlPreference(final Context context) 
 	{
 		EditTextPreference serverNamePref =	(EditTextPreference)getPreferenceScreen().findPreference(PENGUIN_SERVER_URL_PREF_KEY);
		
		serverNamePref.setSummary(PreferenceManager.getDefaultSharedPreferences(this).getString(PENGUIN_SERVER_URL_PREF_KEY, "Please Configure Server Url."));
 		
 		serverNamePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				String newServerUrl = newValue.toString();
 				Boolean valid = true;
                 if (!validServerUrl(newServerUrl)) {
                     final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                     builder.setTitle("Invalid Server Url");
                     builder.setMessage("Please enter a valid Url.");
                     builder.setPositiveButton(android.R.string.ok, null);
                     builder.show();
                     valid = false;
                 } else {
     				preference.setSummary(newServerUrl);
                 }
 				
                 return valid;			}
 		});
 	}
 	
 	private boolean validServerUrl(String serverUrl) 
 	{
 		try
 		{
 			new URL(serverUrl);
 			return true;
 		}
 		catch (MalformedURLException e)
 		{
 			return false;
 		}
 	}
 }
