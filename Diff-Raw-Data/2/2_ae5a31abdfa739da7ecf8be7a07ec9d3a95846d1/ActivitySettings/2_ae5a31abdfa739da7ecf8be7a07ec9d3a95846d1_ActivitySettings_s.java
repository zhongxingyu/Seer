 //
 // Copyright (C) 2013, Maxim Osipov
 //
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without modification,
 // are permitted provided that the following conditions are met:
 //
 //  - Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 //  - Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 //  - Neither the name of the University of Oxford nor the names of its
 //    contributors may be used to endorse or promote products derived from this
 //    software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 // IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 // LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 
 package com.ibme.android.actopsy;
 
 import com.ibme.android.actopsy.R;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class ActivitySettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 
 	private static final String TAG = "ActopsySettings";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Preference pref;
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.layout.preferences);
 
 		pref = findPreference("editLocalStorage");
 		pref.setOnPreferenceChangeListener(numberCheckListener);
 		String local = ((EditTextPreference)pref).getText();
 		pref.setSummary(local + " (10 days recommended, one day takes 10-30MB of external storage)");
 
 		pref = findPreference("editUserBday");
 		pref.setSummary(((EditTextPreference)pref).getText());
 
 		pref = findPreference("editUserLocation");
 		pref.setSummary(((EditTextPreference)pref).getText());
 
 		pref = findPreference("listUserGender");
 		pref.setSummary(((ListPreference)pref).getEntry());
 
 		pref = findPreference("listUserStatus");
 		pref.setSummary(((ListPreference)pref).getEntry());
 
 		pref = findPreference("listMobileUsage");
 		pref.setSummary(((ListPreference)pref).getEntry());
 	}
 
 	Preference.OnPreferenceChangeListener numberCheckListener = new Preference.OnPreferenceChangeListener() {
 	    @Override
 	    public boolean onPreferenceChange(Preference preference, Object newValue) {
 	        //Check that the string is an integer.
 	        return numberCheck(newValue);
 	    }
 	};
 
 	private boolean numberCheck(Object newValue) {
 	    if( !newValue.toString().equals("")  &&  newValue.toString().matches("\\d*") ) {
 	    	int i = Integer.valueOf(newValue.toString());
 	    	if (i > 0) {
 	    		return true;
 	    	} else {
 		        Toast.makeText(this, "Value should be > 0", Toast.LENGTH_SHORT).show();
 		        return false;
 	    	}
 	    }
 	    else {
 	        Toast.makeText(this, newValue + " is not a number", Toast.LENGTH_SHORT).show();
 	        return false;
 	    }
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		Preference pref = findPreference(key);
 		if (key.equals("editUserBday") || key.equals("editUserLocation")) {
 			pref.setSummary(((EditTextPreference)pref).getText());
 
 		} else if (key.equals("editLocalStorage")) {
 			String local = ((EditTextPreference)pref).getText();
			pref.setSummary(local + " (10 days recommended, one day takes 10-30MB of external storage)");
 
 		} else if (key.equals("listUserGender") || key.equals("listUserStatus") || key.equals("listMobileUsage")) {
 			pref.setSummary(((ListPreference)pref).getEntry());
 
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_actopsy, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int itemId = item.getItemId();
 		if (itemId == R.id.menu_profile) {
 			Intent mainActivity = new Intent(getBaseContext(), ActivityProfile.class);
 			startActivity(mainActivity);
 			return true;
 		} else if (itemId == R.id.menu_settings) {
 			return super.onOptionsItemSelected(item);
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
