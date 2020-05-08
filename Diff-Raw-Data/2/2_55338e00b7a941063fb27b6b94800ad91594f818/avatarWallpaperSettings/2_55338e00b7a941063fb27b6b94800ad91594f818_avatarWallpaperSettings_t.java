 package edu.usf.eng.pie.avatars4change.wallpaper;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import edu.usf.eng.pie.avatars4change.R;
 
 public class avatarWallpaperSettings extends PreferenceActivity 
     implements SharedPreferences.OnSharedPreferenceChangeListener {
 	private static final String TAG = "avatarWallpaperSettings";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getPreferenceManager().setSharedPreferencesName(avatarWallpaper.SHARED_PREFS_NAME);
         addPreferencesFromResource(R.xml.avatar_settings);
         getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
     	//       groupID  ,itemID,order, title
     	menu.add(Menu.NONE,0     ,0    , "Support");
     	menu.add(Menu.NONE,1     ,1    , "initialSetup");
     	return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     protected void onDestroy() {
         getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
         super.onDestroy();
     }
 
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
     	Log.d(TAG, key + " preference changed");
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         	case 0:
         		displayContactInfo();
         		return true;
             case 1:
                 startActivity(new Intent(getApplicationContext(), edu.usf.eng.pie.avatars4change.wallpaper.AvatarWallpaperSetup.class));
                 return true;
         }
         return false;
     }
     
     private void displayContactInfo(){
     	AlertDialog.Builder dlg = new AlertDialog.Builder(this);
    	dlg.setMessage("For support please contact " + getString(R.string.contactemail) + "\n"+
     			       "\n" +
     			       "To report issues or for more info please visit our repo " +
     			       "on github.com/7yl4r/AvatarWallpaper");
     	dlg.setTitle("AvatarWallpaper Support");
     	dlg.setPositiveButton("OK", null);
     	dlg.setCancelable(true);
     	dlg.create().show();
     }
 }
