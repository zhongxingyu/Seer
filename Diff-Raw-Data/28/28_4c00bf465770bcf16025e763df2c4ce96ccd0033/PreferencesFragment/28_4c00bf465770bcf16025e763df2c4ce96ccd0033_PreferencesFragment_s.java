 package org.routy.fragment;
 
 import org.routy.R;
 import org.routy.model.PreferencesModel;
 import org.routy.model.RouteOptimizePreference;
 
 import android.app.FragmentManager;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 import android.preference.SwitchPreference;
 import android.util.Log;
 
 public class PreferencesFragment extends PreferenceFragment {
 	private static final String TAG = "PreferenceFragment";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 	    super.onCreate(savedInstanceState);
 	    final FragmentManager fragmentManager = getFragmentManager();;
 	    addPreferencesFromResource(R.xml.preferences);
 	    
 	    Preference myPref = (Preference) findPreference("pref_about");
 	    myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {
				OneButtonDialog dialog = new OneButtonDialog(getResources().getString(R.string.about_title), getResources().getString(R.string.about_text)) {
 					@Override
					public void onButtonClicked(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				};
 				dialog.show(fragmentManager, TAG);
 				return true;
 			}
 	    });
 	    
 	    Preference reset_instructions = (Preference) findPreference("pref_reset_instruction_text");
 	    reset_instructions.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {
 				SharedPreferences defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
 				Editor e = defaultSharedPrefs.edit();
 				e.putBoolean("routy_noob", true);
//				e.putBoolean("entry_noob", true);
 				e.putBoolean("results_noob", true);
 				e.commit();
 				PreferencesModel.getSingleton().setRoutyNoob(true);
//				PreferencesModel.getSingleton().setEntryNoob(true);
 				PreferencesModel.getSingleton().setResultsNoob(true);
 				return true;
 			}
 	    });
 	    
 	    SwitchPreference routeModeSwitch = (SwitchPreference) findPreference("route_mode");
 	    routeModeSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				if (preference.getKey().equals("route_mode")) {
 					boolean mode = (Boolean) newValue;		// False = distance; True = time
 					Log.v(TAG, "route mode changed to " + mode);
 					
 					if (!mode) {
 						PreferencesModel.getSingleton().setRouteOptimizeMode(RouteOptimizePreference.PREFER_DISTANCE);
 					} else {
 						PreferencesModel.getSingleton().setRouteOptimizeMode(RouteOptimizePreference.PREFER_DURATION);
 					}
 				}
 				return true;
 			}
 		});
 	}
 	
 	public void onToggleClicked(boolean on) {
 		if (on) {
 			PreferencesModel.getSingleton().setRouteOptimizeMode(RouteOptimizePreference.PREFER_DURATION);
 		} 
 		else {
 			PreferencesModel.getSingleton().setRouteOptimizeMode(RouteOptimizePreference.PREFER_DISTANCE);
 		}
 	}
 }
