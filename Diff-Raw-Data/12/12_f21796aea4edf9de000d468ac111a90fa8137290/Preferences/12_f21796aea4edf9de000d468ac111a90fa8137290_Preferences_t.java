 package ru.narkdevils.shifr;
 
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 
 public class Preferences extends PreferenceActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preference);
 		
 		Preference myPref = (Preference) findPreference("like_button");
 		myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 		      public boolean onPreferenceClick(Preference arg0) {
 		    	  try {
		    		  Intent intent = new Intent(Intent.ACTION_VIEW);
 		    		  intent.setData(Uri.parse("market://details?id=ru.narkdevils.shifr"));
		    		  startActivity(intent);
 		    	  } catch (ActivityNotFoundException e) {
		    		  Intent intent = new Intent(Intent.ACTION_VIEW);
 		    		  intent.setData(Uri.parse("http://market.android.com/details?id=ru.narkdevils.shifr"));
		    		  startActivity(intent);
 		    	  }
		    	  
 		          return true;
 		      }
 		});
 	}
 
 }
