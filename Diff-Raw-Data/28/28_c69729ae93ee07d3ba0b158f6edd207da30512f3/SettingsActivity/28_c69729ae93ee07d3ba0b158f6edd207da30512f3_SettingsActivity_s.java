 package com.example.swp_ucd_2013_eule;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class SettingsActivity extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setTitle(R.id.action_settings);
 
         // Display the fragment as the main content.
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new SettingsFragment())
                 .commit();
     }
 }
