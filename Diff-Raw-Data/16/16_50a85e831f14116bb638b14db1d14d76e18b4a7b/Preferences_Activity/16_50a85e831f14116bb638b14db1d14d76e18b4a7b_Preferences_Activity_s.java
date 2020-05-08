 package com.appsmarttech.ut90;
 
 import java.util.prefs.Preferences;
 
 import com.actionbarsherlock.app.SherlockPreferenceActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceScreen;
 
 public class Preferences_Activity extends SherlockPreferenceActivity{
 	    @Override
 	    public boolean onCreateOptionsMenu(Menu menu) {
 	        //Used to put dark icons on light action bar
 	        
 
 	        menu.add("Share UltiTrack 90")
 	            .setIcon(R.drawable.ic_share)
 	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 	        	
 
 	        menu.add("Email Support")
 	            .setIcon(R.drawable.ic_email)
 	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
 	        menu.add("WebPage")
 	            .setIcon(R.drawable.ic_website)
 	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
 	        return super.onCreateOptionsMenu(menu);
 	    }
 
 	    @Override
 	    protected void onCreate(Bundle savedInstanceState) {
 	        
 	        super.onCreate(savedInstanceState);
 	        //change the animation
 	        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
 	        //add preference file
 	        addPreferencesFromResource(R.xml.utpro_preferences);
 	        //declare the preference category hidden, and hide it from the user
 	        Preference pHiddenPreferences = findPreference("kHide");
 	        PreferenceScreen preferenceScreen = getPreferenceScreen();
 	        preferenceScreen.removePreference(pHiddenPreferences); 
 	        //set onclick listener for the rate UT90 button
 	        getPreferenceManager()
 	        .findPreference("kRateUs")
 	        .setOnPreferenceClickListener(
 	           new OnPreferenceClickListener() {
 	         @Override
 	         public boolean onPreferenceClick(Preference preference) {
 	             Intent intent = new Intent(Intent.ACTION_VIEW);
 	             intent.setData(Uri.parse("market://details?id=com.appsmarttech.ut90"));
 	             startActivity(intent);
 	             return true;
 	         }
 	     });
 	        //set onclick listener for the view other apps button
 	        getPreferenceManager()
 	        .findPreference("kOurApps")
 	        .setOnPreferenceClickListener(
 	           new OnPreferenceClickListener() {
 	         @Override
 	         public boolean onPreferenceClick(Preference preference) {
 	             Intent intent = new Intent(Intent.ACTION_VIEW);
 	             intent.setData(Uri.parse("market://search?q=pub:AppSmart+Tech+LLC"));
 	             startActivity(intent);
 	             return true;
 	         }
 	     });
 	    }
 	
 	//setting the actions for the actionbar icons
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getTitle() == "Share UltiTrack 90")
 		{
 			shareApp();
 		}
 		if(item.getTitle() == "Email Support")
 		{
 			sendEmail();
 		}
 		if(item.getTitle() == "WebPage")
 		{
 			launchWebPage();
 		}
 
 
 		return true;
 	}
 	
     public void sendEmail(){
    	Intent intEmail = new Intent(android.content.Intent.ACTION_SEND);
    	intEmail.setType("message/rfc822");
    	intEmail.putExtra(android.content.Intent.EXTRA_EMAIL, "support@appsmarttech.com");
    	intEmail.putExtra(android.content.Intent.EXTRA_SUBJECT, "UltiTrack 90 - Support");
    	startActivity(intEmail);
     }
     
     public void shareApp(){
     	Intent intShare = new Intent(android.content.Intent.ACTION_SEND);
     	intShare.setType("text/plain");
     	intShare.putExtra(android.content.Intent.EXTRA_TEXT, "Check out UltiTrack 90, it's the shit!  http://www.ultitrack.net");
     	startActivity(Intent.createChooser(intShare, "Share UltiTrack 90 :"));
     }
     
     public void launchWebPage(){
     	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ultitrack.net"));
     	startActivity(browserIntent);
     }
 }
