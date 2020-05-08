 package com.rubixconsulting.walletcracker;
 
 import java.util.List;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class PreferencesMain extends PreferenceActivity implements OnSharedPreferenceChangeListener {
   protected final String TAG = this.getClass().getSimpleName();
 
   public static class DataPreferences extends PreferenceFragment {
     @Override public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       addPreferencesFromResource(R.xml.data_preferences);
     }
   }
 
   @Override public void onBuildHeaders(List<Header> target) {
     super.onBuildHeaders(target);
     loadHeadersFromResource(R.xml.preference_headers, target);
   }
 
   @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
     Log.i(TAG, "onSharedPreferenceChanged: "+key);
     if (key.equals("demo_mode")) {
       ObscurableString.setObscure(sharedPreferences.getBoolean(key, false));
     }
   }
 
   @Override protected void onResume() {
     super.onResume();
     PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
     Log.i(TAG, "onResume");
   }
 
   @Override protected void onPause() {
     super.onPause();
     PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
     Log.i(TAG, "onPause" + (isFinishing() ? "Finishing" : ""));
   }
 }
