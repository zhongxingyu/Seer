 package jp.co.c_lis.glassradiosdisabler;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class Setting {
 
     private SharedPreferences mPref = null;
 
     private static final String PREF_FILE_NAME = "pref.dat";
    private static final String PREF_KEY_PASSPHRESE = "key_passphrese";
 
     public String getPattern() {
        return mPref.getString(PREF_KEY_PASSPHRESE, null);
     }
 
    public void setPassphrese(String phrese) {
        mPref.edit().putString(PREF_KEY_PASSPHRESE, phrese).commit();
     }
 
     private static Setting sInstance = null;
 
     public synchronized static Setting getInstance(Context context) {
         if (sInstance == null) {
             sInstance = new Setting(context);
         }
         return sInstance;
     }
 
     private Setting(Context context) {
         mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
     }
 }
