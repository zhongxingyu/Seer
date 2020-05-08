 package com.raouldc.uoacompsci;
 
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 
 public class SettingsActivity extends PreferenceActivity{
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
         
         Preference button = (Preference)findPreference("deleteCache");
         button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                         @Override
                         public boolean onPreferenceClick(Preference arg0) { 
                             //code for what you want it to do   
                             return true;
                         }
                     });
     }
 }
