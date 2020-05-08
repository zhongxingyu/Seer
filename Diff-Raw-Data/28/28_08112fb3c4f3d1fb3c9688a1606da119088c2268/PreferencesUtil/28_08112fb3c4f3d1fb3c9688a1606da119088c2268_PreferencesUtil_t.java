 package fr.ybo.ybotv.android.util;
 
 
 import android.content.SharedPreferences;
 import android.os.Build;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class PreferencesUtil {
 
 
     public static Set<String> getStringSets(SharedPreferences prefs, String key) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                return prefs.getStringSet(key, new HashSet<String>());
            } catch (ClassCastException ignore) {
                return getStringsSetsForOldVersion(prefs, key);
             }
        } else {
            return getStringsSetsForOldVersion(prefs, key);
        }
    }

    private static Set<String> getStringsSetsForOldVersion(SharedPreferences prefs, String key) {
        GsonBuilder build = new GsonBuilder();
        Gson gson = build.create();
        String serializedValue = prefs.getString(key, "[]");
        if (serializedValue == null) {
            serializedValue = "[]";
         }
        return gson.fromJson(serializedValue, new TypeToken<Set<String>>() {
        }.getType());
     }
 
     public static void putStringSets(SharedPreferences prefs, String key, Set<String> values) {
         SharedPreferences.Editor edit = prefs.edit();
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             edit.putStringSet(key, values);
         } else {
             GsonBuilder build = new GsonBuilder();
             Gson gson = build.create();
             String serializedValue = gson.toJson(values);
             edit.putString(key, serializedValue);
         }
         edit.commit();
     }
 
 
 }
