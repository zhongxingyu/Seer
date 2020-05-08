 package com.level42.mixit.activities;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
import roboguice.inject.InjectResource;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
 
 import com.level42.mixit.R;
 
 /**
  * Ecran d'adresse.
  */
 @ContentView(R.layout.activity_adresse)
 public class AdresseActivity extends RoboActivity {
 
    @InjectResource(R.string.mixit_address)
    private String adresse;
    
    /**
     * Ouvre la navigation
     * @param v
     */
    public void actionGoToAddress(View v) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + adresse));
        startActivity(intent);
    }
    
    /**
     * Ouvre la google map
     * @param v
     */
    public void actionShowAddress(View v) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?q=" + adresse));
        startActivity(intent);
    }
 }
