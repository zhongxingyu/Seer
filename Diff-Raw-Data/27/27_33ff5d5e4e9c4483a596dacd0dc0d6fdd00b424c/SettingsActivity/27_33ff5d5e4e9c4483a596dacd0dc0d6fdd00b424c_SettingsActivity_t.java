 package com.raouldc.uoacompsci;
 
import android.app.ActionBar;
import android.content.Intent;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
import android.view.MenuItem;
 
 public class SettingsActivity extends PreferenceActivity{
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
         
         Preference button = (Preference)findPreference("deleteCache");
         button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                         @Override
                         public boolean onPreferenceClick(Preference arg0) { 
                             //code for what you want it to do   
                             return true;
                         }
                     });
     }
    
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;

    }
 }
