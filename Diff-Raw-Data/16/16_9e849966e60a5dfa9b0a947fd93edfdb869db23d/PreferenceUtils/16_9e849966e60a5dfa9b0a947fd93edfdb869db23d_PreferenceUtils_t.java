 package dolphin.android.preference;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.preference.PreferenceManager;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Created by 97011424 on 2013/8/14.
  */
 public class PreferenceUtils {
 
     private Context mContext;
     private SharedPreferences mSharedPreferences;
 
     public PreferenceUtils(Context context) {
         mContext = context;
         mSharedPreferences = getDefaultSharedPreferences(mContext);
     }
 
     public SharedPreferences getSharedPreferences() {
         return mSharedPreferences;
     }
 
     public SharedPreferences.Editor edit() {
         return mSharedPreferences.edit();
     }
 
     public void commit() {
 
     }
 
     public Set<String> getStringSet(String key) {
         return getStringSet(key, null);
     }
 
     public Set<String> getStringSet(String key, Set<String> defaultValue) {
         //Android setSringSet() / getStringSet() with compatibility
         //https://gist.github.com/shreeshga/5398506
         Set<String> set = null;

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //[57]++ java.lang.String cannot be cast to java.util.Set
            try {
                set = mSharedPreferences.getStringSet(key, null);
            } catch (Exception e) {//try to get as String
                String s = mSharedPreferences.getString(key, null);
                if (s != null)
                    set = new HashSet<String>(Arrays.asList(s.split(",")));
                else
                    set = new HashSet<String>();
                //clear the String format, and convert to StringSet
                mSharedPreferences.edit().remove(key).commit();
                putStringSet(key, set);//update with new format
            }
         } else {
             String s = mSharedPreferences.getString(key, null);
             if (s != null)
                 set = new HashSet<String>(Arrays.asList(s.split(",")));
         }
         return set;
     }
 
     public void putStringSet(String key, Set<String> set) {
         SharedPreferences.Editor editor = mSharedPreferences.edit();
         //Android setSringSet() / getStringSet() with compatibility
         //https://gist.github.com/shreeshga/5398506
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             editor.putStringSet(key, set);
         } else {
             editor.putString(key, join(set, ","));
         }
         editor.commit();
     }
 
     private static String join(Set<String> set, String delim) {
         StringBuilder sb = new StringBuilder();
         String loopDelim = "";
 
         for (String s : set) {
             sb.append(loopDelim);
             sb.append(s);
 
             loopDelim = delim;
         }
 
         return sb.toString();
     }
 
     public String getString(String key) {
         return getString(key, null);
     }
 
     public String getString(String key, String defaultValue) {
         return mSharedPreferences.getString(key, defaultValue);
     }
 
     public void putString(String key, String value) {
         SharedPreferences.Editor editor = mSharedPreferences.edit();
         editor.putString(key, value);
         editor.commit();
     }
 
     public void remove(String key) {
         SharedPreferences.Editor editor = mSharedPreferences.edit();
         editor.remove(key);
         editor.commit();
     }
 
     public static SharedPreferences getDefaultSharedPreferences(Context context) {
         return PreferenceManager.getDefaultSharedPreferences(context);
     }
 
     public int getInt(String key) {
         return getInt(key, 0);
     }
 
     public int getInt(String key, int defaultValue) {
         return mSharedPreferences.getInt(key, defaultValue);
     }
 
     public void putInt(String key, int value) {
         SharedPreferences.Editor editor = mSharedPreferences.edit();
         editor.putInt(key, value);
         editor.commit();
     }
 
     public boolean getBoolean(String key) {
         return getBoolean(key, false);
     }
 
     public boolean getBoolean(String key, boolean defaultValue) {
         return mSharedPreferences.getBoolean(key, defaultValue);
     }
 
     public void putBoolean(String key, boolean value) {
         SharedPreferences.Editor editor = mSharedPreferences.edit();
         editor.putBoolean(key, value);
         editor.commit();
     }
 }
