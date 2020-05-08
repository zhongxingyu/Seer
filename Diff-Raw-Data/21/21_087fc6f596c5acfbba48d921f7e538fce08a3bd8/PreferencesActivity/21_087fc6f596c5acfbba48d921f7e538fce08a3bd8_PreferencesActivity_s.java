 /*
 	This file is part of LogAcceleration.
 
     LogAcceleration is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     LogAcceleration is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with LogAcceleration.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tntexplosivesltd.acceleration;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.widget.Toast;
 
 /**
  * @brief Preferences activity that gets launched when "Settings" is pressed
  */
 public class PreferencesActivity extends PreferenceActivity {
 	/**
 	 * @brief Sets what the PreferencesActivity looks like.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 	    super.onCreate(savedInstanceState);
 	    addPreferencesFromResource(R.xml.preferences);
 	    Preference reset_pref = (Preference)findPreference("reset_pref");
 	    Preference web_pref = (Preference)findPreference("web_pref");
 	    Preference bug_pref = (Preference)findPreference("bug_pref");
 	    reset_pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
 	    {
 	    	public boolean onPreferenceClick(Preference preference)
 	    	{
 	    		ColourManager.reset();
 	    		Panel.refresh_colours();
 	    		ColourManager.was_reset = true;
 	    		Toast.makeText(getBaseContext(), "Colours Reset", Toast.LENGTH_SHORT).show();
 	    		PreferencesActivity.this.finish();
 	    		return true;
 	    	}
 	    });
 	    
 	    web_pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
 	    {
 	    	public boolean onPreferenceClick(Preference preference)
 	    	{
 	    		Intent website_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://entropy.net.nz"));
 	    		startActivity(website_intent);
 	    		return true;
 	    	}
 	    });
 	    
 	    bug_pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
 	    {
 	    	public boolean onPreferenceClick(Preference preference)
 	    	{
	    		Intent website_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://entropy.net.nz/Software"));
 	    		startActivity(website_intent);
 	    		return true;
 	    	}
 	    });
 	}
 }
