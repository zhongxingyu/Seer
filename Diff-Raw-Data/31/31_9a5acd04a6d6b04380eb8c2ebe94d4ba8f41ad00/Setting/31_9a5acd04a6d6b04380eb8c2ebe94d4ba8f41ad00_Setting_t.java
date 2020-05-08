 package jp.co.c_lis.glassradiosdisabler;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class Setting {
 
     private SharedPreferences mPref = null;
 
     private static final String PREF_FILE_NAME = "pref.dat";
    private static final String PREF_KEY_PATTERN = "key_pattern";

    public boolean hasPattern() {
        return mPref.getString(PREF_KEY_PATTERN, null) != null;
    }
 
     public String getPattern() {
        return mPref.getString(PREF_KEY_PATTERN, null);
    }

    public void setPattern(String pattern) {
        mPref.edit().putString(PREF_KEY_PATTERN, pattern).commit();
     }
 
    public void clearPattern() {
        setPattern(null);
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
