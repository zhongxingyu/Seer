 package com.alvarosantisteban.pathos.preferences;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.alvarosantisteban.pathos.DateActivity;
 import com.alvarosantisteban.pathos.R;
 import com.alvarosantisteban.pathos.utils.StringUtils;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.MultiSelectListPreference;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * Creates the settings for versions newer than Honeycomb. 
  * Allows the user to select the kind of organization that he/she wants between type, topic or origin of the events.
  * It also allows the user to choose the set of tags that wants to see. * 
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
 	
 	/**
 	 * Used for logging purposes
 	 */
 	private static final String TAG = "SettingsFragment";
 	
 	// -----------
 	// PREFERENCES
 	// -----------
 	/**
 	 * Preference for the multilist with the selection of sites (origin)
 	 */
 	public static final String KEY_PREF_MULTILIST_SITES = "multilist_sites";
 	/**
 	 * Preference for the multilist with the selection of tags for topic
 	 */
 	public static final String KEY_PREF_MULTILIST_TOPIC = "multilist_thema";
 	/**
 	 * Preference for the multilist with the selection of tags for type
 	 */
 	public static final String KEY_PREF_MULTILIST_TYPE = "multilist_type";
 	/**
 	 * Preference for the multilist with the last selection of sites, used to compare it with the actual and know which changed
 	 */
 	public static final String KEY_PREF_MULTILIST_LAST_SELECTION = "lastSelection";
 	/**
 	 * Preference for the list with possible organizations
 	 */
 	public static final String KEY_PREF_LIST_ORGANIZATIONS = "possible_organizations_list";
 	 
 	// -----------
 	// CONSTANTS
 	// -----------
 	private String TYPE_ORGANIZATION;
 	private String TOPIC_ORGANIZATION; 
 
 	ListPreference organizationList; 
 	MultiSelectListPreference topicMultiList;
 	MultiSelectListPreference typeMultiList;
 	MultiSelectListPreference originMultiList;
 	
 	Set<String> setOfSites;
 	
 	 /**
 	  * Loads the preferences from the XML file
 	  */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "ON CREATE THE SETTINGS FRAGMENT");
         TYPE_ORGANIZATION = getResources().getString(R.string.organization_by_type);
         TOPIC_ORGANIZATION = getResources().getString(R.string.organization_by_topic);
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.preferences);
         
         organizationList = (ListPreference) findPreference(KEY_PREF_LIST_ORGANIZATIONS); 
         topicMultiList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_TOPIC);
         typeMultiList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_TYPE);
         originMultiList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_SITES);
         
         // Enable the active multilist and disable the other two 
         enableActiveOrganization(organizationList.getValue());
         
         // Get the set of active websites
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         	setOfSites = sharedPref.getStringSet(KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(getResources().getStringArray(R.array.default_sites_array))));
 		} else {
 			String s = sharedPref.getString(KEY_PREF_MULTILIST_SITES, getResources().getString(R.string.sites_pseudoarray_values));
 			if(s != null){
 				setOfSites = new HashSet<String>(Arrays.asList(s.split(",")));
 			}
 		}
         // Set the intent for the Manifesto preference (triggered when clicked)
         findPreference("manifesto").setIntent(new Intent(getActivity(), ManifestoActivity.class));
         // Set the intent for the About preference (triggered when clicked)
         findPreference("about").setIntent(new Intent(getActivity(), AboutActivity.class));
        // Set the intent for the Known Issues preference (triggered when clicked)
        findPreference("known_issues").setIntent(new Intent(getActivity(), KnownIssuesActivity.class));
         // Set the intent for the Legal Notices preference (triggered when clicked)
         findPreference("legal").setIntent(new Intent(getActivity(), LegalNoticesActivity.class));  
     }
 
 	/**
 	 * Listens for a change in the preferences
 	 */
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		Log.d(TAG, "GUEEEEE ON SHARED PREFERENCE CHANGED");
 		if (key.equals(KEY_PREF_MULTILIST_SITES)) {
         	Log.d(TAG,"key=multilist sites changed");
             // Save the old values
             Editor editor = sharedPreferences.edit();
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				editor.putStringSet(KEY_PREF_MULTILIST_LAST_SELECTION, setOfSites);
 			} else {
 				editor.putString(KEY_PREF_MULTILIST_LAST_SELECTION, StringUtils.join(setOfSites, ","));
 			}
             editor.commit();
             
             // Set the new values
             setOfSites = originMultiList.getValues();
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				editor.putStringSet(KEY_PREF_MULTILIST_SITES, setOfSites);
 			} else {
 				editor.putString(KEY_PREF_MULTILIST_SITES, StringUtils.join(setOfSites, ","));
 			}
             editor.commit();
             
             Intent i = new Intent(getActivity(), DateActivity.class);
 			startActivity(i);	
         }else if (key.equals(KEY_PREF_MULTILIST_TYPE)){
         	Log.d(TAG,"key=multilist type changed");
         	Editor editor = sharedPreferences.edit();
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				editor.putStringSet(KEY_PREF_MULTILIST_TYPE, typeMultiList.getValues());
 			} else {
 				editor.putString(KEY_PREF_MULTILIST_TYPE, StringUtils.join(typeMultiList.getValues(), ","));
 			}
             editor.commit();
             
         	// Go to Date Activity
             Intent i = new Intent(getActivity(), DateActivity.class);
 			startActivity(i);
         }else if (key.equals(KEY_PREF_MULTILIST_TOPIC)){
         	Log.d(TAG,"key=multilist topic changed");
         	Editor editor = sharedPreferences.edit();
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				editor.putStringSet(KEY_PREF_MULTILIST_TOPIC, topicMultiList.getValues());
 			} else {
 				editor.putString(KEY_PREF_MULTILIST_TOPIC, StringUtils.join(topicMultiList.getValues(), ","));
 			}
             editor.commit();
         	
             // Go to Date Activity
             Intent i = new Intent(getActivity(), DateActivity.class);
 			startActivity(i);
         }else if (key.equals(KEY_PREF_LIST_ORGANIZATIONS)){
         	Log.d(TAG,"key=possible_organizations_list changed");
         	String kindOfOrganization = sharedPreferences.getString(key, TYPE_ORGANIZATION);
         	enableActiveOrganization(kindOfOrganization);
         	if(kindOfOrganization.equals(TYPE_ORGANIZATION) || kindOfOrganization.equals(TOPIC_ORGANIZATION)){
         		Log.d(TAG,"Selected By Type or By Topic. Restore default websites selection"); 
         		restoreDefaultWebsitesSelection(sharedPreferences);
         	}
         }
 	}
 
 	private void restoreDefaultWebsitesSelection(SharedPreferences sharedPreferences) {		
 		// Get the default sites selection
 		Set<String> defaultSitesSelection = new HashSet<String>(Arrays.asList(getResources().getStringArray(R.array.default_sites_array)));
 		// Set the values on the shown list
 		originMultiList.setValues(defaultSitesSelection);
 		// Save the new values
 		Editor editor = sharedPreferences.edit();
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			editor.putStringSet(KEY_PREF_MULTILIST_SITES, defaultSitesSelection);
 		} else {
 			editor.putString(KEY_PREF_MULTILIST_SITES, StringUtils.join(defaultSitesSelection, ","));
 		}
 		editor.commit();
 	}
   	
 	/**
 	 * Enables the active organization and disables the others
 	 * 
 	 * @param activeOrganization the key that identifies the active kind of organization
 	 */
 	private void enableActiveOrganization(String activeOrganization) {
 		if (activeOrganization.equals(TYPE_ORGANIZATION)){
 	    	topicMultiList.setEnabled(false);
 	    	typeMultiList.setEnabled(true);
 	    	originMultiList.setEnabled(false);
 	    }else if(activeOrganization.equals(TOPIC_ORGANIZATION)){
 	    	topicMultiList.setEnabled(true);
 	    	typeMultiList.setEnabled(false);
 	    	originMultiList.setEnabled(false);
 	    }else{
 	    	topicMultiList.setEnabled(false);
 	    	typeMultiList.setEnabled(false);
 	    	originMultiList.setEnabled(true);
 	    }
 	}
 	
 	@Override
 	public void onResume() {
 	    super.onResume();
 	    Log.d(TAG,"ON Resume THE SETTINGS FRAGMENT");
 	    // Set up a listener whenever a key changes
 	    getPreferenceScreen().getSharedPreferences()
 	            .registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onPause() {
 	    super.onPause();
 	    Log.d(TAG,"ON Pause THE SETTINGS FRAGMENT");
 	    // Unregister the listener whenever a key changes
 	    getPreferenceScreen().getSharedPreferences()
 	            .unregisterOnSharedPreferenceChangeListener(this);
 	}
 	
 	/**
 	 * Prints out all preferences
 	 * 
 	 * @param sharedPref the shared preferences
 	 */
 	@SuppressWarnings("unused")
 	private void printAllPreferences(SharedPreferences sharedPref) {
 		Map<String,?> keys = sharedPref.getAll();
 
 		Log.d(TAG,"------START--------");
         for(Map.Entry<String,?> entry : keys.entrySet()){
         	Log.d(TAG,"map values: " +entry.getKey() + ": " + entry.getValue().toString());            
          }
         Log.d(TAG,"-------END-------");
 	}
 }
