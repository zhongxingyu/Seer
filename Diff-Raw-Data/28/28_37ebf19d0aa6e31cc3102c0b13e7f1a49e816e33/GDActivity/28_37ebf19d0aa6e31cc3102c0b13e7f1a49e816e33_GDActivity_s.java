 package com.appspot.yapkke.stanford.gindroid;
 
 import android.app.*;
 import android.content.*;
 import android.widget.*;
 import android.preference.*;
 
 import android.os.Bundle;
 
 /** GINDroid's Activity
  * 
  * @author ykk
  * @date Nov 2011
  */
 public class GDActivity
     extends Activity
 {
     /** WebGin instance
      */
     protected WebGin wg;
     /** Listing
      */
     WebGin.Listing listing;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
 	//Create new WebGin
 	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 	wg = new WebGin();
 	wg.auth(settings.getString("username", ""),
 		settings.getString("password", ""));	
     }
 
     public void refresh_listing()
     {
 	listing = wg.briefListing();
	Toast.makeText(this, "Fetch GIN data", Toast.LENGTH_SHORT).show();
     }
 }
