 package com.example.projectv1;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuItem;
 
 
 public class Preferences extends PreferenceActivity {
     
     @Override
     public void onCreate(Bundle savedInstanceState) {       
         super.onCreate(savedInstanceState);       
         
         // must call this method to check password before  displaying activity
         checkPassword();
         
         addPreferencesFromResource(R.xml.preferences);       
         
         //Go back a screen
         Preference button = (Preference)getPreferenceManager().findPreference("backButtons");      
         
         button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                         @Override
                         public boolean onPreferenceClick(Preference arg0) { 
                         	
                         	Log.v("back","button");
                 			Intent openMenu = new Intent("com.project.MENU");
                 			startActivity(openMenu);
 
                             finish();
          	                            return true;
                         }
                     });
 
         //Force refresh iCal
         Preference buttoniCal = (Preference)getPreferenceManager().findPreference("refresh");      
         
         buttoniCal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                         @Override
                         public boolean onPreferenceClick(Preference arg0) {
                         	//Code goes here to force iCal refresh
                 			//Intent openMenu = new Intent("com.project.MENU");
                 			//startActivity(openMenu);
 
                             //finish();
          	                            return true;
                         }
                     });
     
     }
      
     @Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		checkPassword();
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, 0, 0, "Show current settings");
         return super.onCreateOptionsMenu(menu);
     }
  
     @Override
 public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case 0:
 //                startActivity(new Intent(this, ShowSettingsActivity.class));
                 return true;
         }
         return false;
     }
     
     public void checkPassword()		// compare submitted password correct one. Redirect back to password prompt if they don't match
     { 	
 		Bundle submitted = getIntent().getExtras();
 		long savedPass=0, submittedPass=-1;
 		
 		if (submitted !=null){			
 			submittedPass = submitted.getLong("submitted_pass");								
 		}
 		
 		SharedPreferences prefs = this.getSharedPreferences("com.example.projectv1", Context.MODE_PRIVATE);
 		
 	//	prefs.edit().putLong("savedPass", "default".hashCode()).commit();						
 		savedPass = prefs.getLong("savedPass", "default".hashCode());
 		
 		if (savedPass != submittedPass){			
			finish();
			//Intent promptPass = new Intent("android.intent.action.PASS");
			//startActivity(promptPass);				
 		} 
     } 
 }
