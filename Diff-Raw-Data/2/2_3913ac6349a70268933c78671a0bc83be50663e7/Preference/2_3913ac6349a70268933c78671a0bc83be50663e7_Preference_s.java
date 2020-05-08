 package org.javaopen.prefix.phone;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class Preference extends PreferenceActivity {
 	private static final String TAG = Preference.class.getName();
 	
 	EditTextPreference prefixEdit = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.addPreferencesFromResource(R.xml.preference);
 		
 		prefixEdit = (EditTextPreference)findPreference(getString(R.string.prefix_key));
 		
 		initDefaults(prefixEdit);
 
 		prefixEdit.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(
 					android.preference.Preference preference, Object newValue) {
 				preference.setSummary(newValue.toString());
 				return true;
 			}});
 	}
 	
 	void initDefaults(EditTextPreference prefixEdit) {
 		SharedPreferences sp =
 				PreferenceManager.getDefaultSharedPreferences(this);
 		String defValue = getString(R.string.prefix_default);
 		String key = getString(R.string.prefix_key);
 		String value = sp.getString(key, null);
 		Log.d(TAG, "onCreate: defValue="+defValue+", key="+key+", value="+value);
 		if (value == null || value.length() <= 0) {
 			Editor editor = sp.edit();
 			editor.putString(key, defValue);
 			editor.commit();
 		}
 		
		value = sp.getString(key, null);
 		prefixEdit.setText(value);
 		prefixEdit.setSummary(value);
 	}
 }
