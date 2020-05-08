 package nl.vincentkriek.lastfm;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 
 import android.app.WallpaperManager;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.util.Log;
 import android.widget.Toast;
 
 public class LastFMTest extends PreferenceActivity {
     public static final String TAG = "nl.vincentkriek.lastfm.LastFMTest";
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.settings);
 		
 		Preference setNow = findPreference("set_now");
 		setNow.setOnPreferenceClickListener(setNowListener);
 		
 		CheckBoxPreference enableTimer = (CheckBoxPreference)findPreference("refresh");
 		enableTimer.setOnPreferenceChangeListener(enableTimerListener);
     }
     
     private OnPreferenceClickListener setNowListener = new OnPreferenceClickListener() {
 
 		public boolean onPreferenceClick(Preference preference) {
 			setWallpaperTask.run();
 			return true;
 		}
 
 	};
 	
 	private OnPreferenceChangeListener enableTimerListener = new OnPreferenceChangeListener() {
 		
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object newValue) {
 			Boolean isChecked = (Boolean)newValue;
 			((CheckBoxPreference)preference).setChecked(isChecked);
 			
 			handler.removeCallbacks(setWallpaperTask);
 			if(isChecked) {
 				handler.postDelayed(setWallpaperTask, 100);
 			}
 			
 			return false;
 		}
 	};
     
     private Handler handler = new Handler();
     
     private Runnable setWallpaperTask = new Runnable() {
     	   public void run() {
     	    	EditTextPreference user = (EditTextPreference)findPreference("username");
     	    	Bitmap background;
     	    	try {
     				background = LastFM.getAlbumArtByUser(user.getText().toString());
     				WallpaperManager.getInstance(getApplicationContext()).setBitmap(background);
     			} catch (JSONException e) {
     				Log.e(TAG, e.getMessage());
     			} catch (IOException e) {
     				Log.e(TAG, e.getMessage());
     			}
     			
    			EditTextPreference intervalPreference = (EditTextPreference)findPreference("interval");
    			String interval_string = intervalPreference.getText();
    			int interval = Integer.parseInt(interval_string);
     			
     			handler.postDelayed(setWallpaperTask, (interval * 60 * 60) * 1000);
     	   }
     	};
   
 }
