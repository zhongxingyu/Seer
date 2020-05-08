 package edu.usf.eng.pie.avatars4change.wallpaper;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 import edu.usf.eng.pie.avatars4change.R;
 import edu.usf.eng.pie.avatars4change.dataInterface.activityMonitor;
 import edu.usf.eng.pie.avatars4change.dataInterface.userData;
 
 public class avatarWallpaperSettings extends PreferenceActivity 
     implements SharedPreferences.OnSharedPreferenceChangeListener {
 	private static final String TAG = "avatarWallpaperSettings";
 	
 //    public static boolean debugMode = true;	//TODO: this is not yet a setting, but should be
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);		
         getPreferenceManager().setSharedPreferencesName(getString(R.string.shared_prefs_name));
         addPreferencesFromResource(R.xml.avatar_settings);
         getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);        
         
         // watch out for admin/debug area access
         findPreference(getString(R.string.key_adminscreen)).setOnPreferenceClickListener(
         		new OnPreferenceClickListener(){
         			@Override
         			public boolean onPreferenceClick(Preference preference) {
         	    		Toast.makeText(getApplicationContext(), "study administrators only please.", Toast.LENGTH_LONG).show();
         	    		Log.d(TAG,"admin settings area accessed");
         	    		//TODO: access to this area should be logged or restricted
         				return true; // true return means that the click was handled
         			}
         		});
 
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
         super.onDestroy();
         getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
     }
     
     // preference listener is triggered when a preference changes and responds accordingly
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
     	Log.d(TAG,key+" preference changed");
     	avatarWallpaperSettings.handleKey(getApplicationContext(),key,sharedPreferences); 
     }
     
     // load in all preferences
     public static void loadPrefs(Context ctx, SharedPreferences sharedPrefs){
 		Log.d(TAG, "loading preferences...");
 		// a list preferences which need to be loaded in manually
 		String PREFERENCE_KEYS[] = {
 				ctx.getString(R.string.key_configmacro),
 				ctx.getString(R.string.key_activeonevens),
 				ctx.getString(R.string.key_activitymonitor),
 				ctx.getString(R.string.key_realismlevel),
 				ctx.getString(R.string.key_scale),
 				ctx.getString(R.string.key_uid),
 				ctx.getString(R.string.key_wifionly)
 				};
 		for ( int i = 0; i<PREFERENCE_KEYS.length; i++ ){
 				// load preference
 				avatarWallpaperSettings.handleKey(ctx, PREFERENCE_KEYS[i],sharedPrefs);
 		}
 	}
 
     //  responds to the preference key given accordingly
     private static void handleKey(Context ctx, String key, SharedPreferences mPrefs){
     	//check for settings macro change
     		
     	//TODO: replace killMe with activity & intent or remove altogether...
     	if(key.equals(ctx.getString(R.string.key_killme))){	//1
     		android.os.Process.killProcess(android.os.Process.myPid());
     		
     	}else if(key.equals(ctx.getString(R.string.key_realismlevel))){	//2
 			avatarWallpaper.theAvatar.setRealismLevel((int) mPrefs.getLong(key, avatarWallpaper.theAvatar.getRealismLevel()));
 			Log.d(TAG, "RealismLevel:"+avatarWallpaper.theAvatar.getRealismLevel());
 
     	}else if(key.equals(ctx.getString(R.string.key_currentactivity))){	//3
 			avatarWallpaper.theAvatar.setActivityName(mPrefs.getString(key, "running"));
 			avatarWallpaper.theAvatar.lastActivityChange = SystemClock.elapsedRealtime();
 			Log.d(TAG, "CurrentActivity:"+avatarWallpaper.theAvatar.getActivityName());
     		
 			//TODO: replace with log-clearing activity
     	}else if (key.equals(ctx.getString(R.string.key_resetlogs))){	//5
 			avatarWallpaper.keepLogs = !mPrefs.getBoolean(key, avatarWallpaper.keepLogs);
 			Log.d(TAG, "keepLogs?:"+avatarWallpaper.keepLogs);
 			
     	}else if (key.equals(ctx.getString(R.string.key_activeonevens))){	//6
 			sceneBehaviors.setActiveOnEvens(mPrefs.getBoolean(key, sceneBehaviors.getActiveOnEvens()));			
 			Log.d(TAG,"activeOnEvens:"+sceneBehaviors.getActiveOnEvens());
 			
 			//TODO: remove & use
 			//    SharedPreferences userDetails = context.getSharedPreferences("userdetails", MODE_PRIVATE);
 			//    String Uname = userDetails.getString("username", "");
     	}else if (key.equals(ctx.getString(R.string.key_uid))){	//7
 			userData.USERID = mPrefs.getString(key,userData.USERID);
 			Log.d(TAG,"UID:"+userData.USERID);
 		
     	}else if (key.equals(ctx.getString(R.string.key_configmacro))){	//8
     		//TODO: load settings values for selected macro
 			avatarWallpaper.theAvatar.setBehaviorSelectorMethod(mPrefs.getString(key, avatarWallpaper.theAvatar.behaviorSelectorMethod));
 			Log.d(TAG, "behaviorSelector:"+avatarWallpaper.theAvatar.behaviorSelectorMethod);
 			
 			//TODO: remove (like above)
     	}else if (key.equals(ctx.getString(R.string.key_scale))){		//10
 			avatarWallpaper.theAvatar.scaler = Float.parseFloat(mPrefs.getString(key, "1.0f"));
 			Log.d(TAG, "scale:"+Float.toString(avatarWallpaper.theAvatar.scaler));
 			
     	}else if (key.equals(ctx.getString(R.string.key_activitymonitor))){
     		activityMonitor.setActivityMonitor(ctx,mPrefs.getString(key, avatarWallpaper.theAvatar.behaviorSelectorMethod));
     		userData.resetPAmeasures();	
 			Log.d(TAG, "activityMonitor:"+activityMonitor.getActivityMonitor());
     	}else{	//unknown preference key
     		Log.d(TAG,"preference "+key+" has no onChanged() call.");
     		return;
     	} 
     	Log.d(TAG, key + " preference onChanged() handled..."); //only prints if last 'else' case not triggered
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
