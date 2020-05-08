 package org.ohmage.mwoc;
 
 import org.apache.cordova.DroidGap;
 
 import android.os.Bundle;
 
 public class MWoCActivity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if(getIntent().getBooleanExtra("SHOW_PENDING_SURVEYS_LIST", false)){
        	super.loadUrl("file:///android_asset/www/pending-surveys.html");	
         }else{
         	super.loadUrl("file:///android_asset/www/index.html");
         }
         
     }
 }		
