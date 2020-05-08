 package com.github.groupENIGMA.journalEgocentrique;
 
import android.app.Activity;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 
 public class Preferences extends PreferenceActivity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Add all the options
         addPreferencesFromResource(R.xml.preferences);
     }
 
 
 }
