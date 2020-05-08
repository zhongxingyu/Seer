 /*
  * This file is part of verfluchter-android.
  *
  * verfluchter-android is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * verfluchter-android is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pl.xsolve.verfluchter.tools;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.util.Log;
 import com.google.inject.Inject;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Konrad Ktoso Malawski
  */
 public class AutoSettingsImpl extends Activity implements AutoSettings {
 
     // Logger tag
     private static final String TAG = AutoSettingsImpl.class.getSimpleName();
 
 //    My instance, "the one to rule them all"
 //    private static AutoSettings myInstance;
 
     protected SharedPreferences preferences;
 
     protected PasswdUtil passwdUtil;
 
     protected Map<String, Object> settings = new HashMap<String, Object>();
 
     protected static final String INT_SUFFIX = "_I";
     protected static final String FLOAT_SUFFIX = "_F";
     protected static final String STRING_SUFFIX = "_S";
     protected static final String BOOLEAN_SUFFIX = "_B";
     protected static final String LONG_SUFFIX = "_L";
 
     @Inject
    public AutoSettingsImpl(SharedPreferences preferences, PasswdUtil passwdUtil) {
         this.preferences = preferences;
         this.passwdUtil = passwdUtil;
 
         //default values and automatic setting+loading setup
         settings.put(SERVER_DOMAIN_S, Constants.DEFAULT.SERVER_DOMAIN);
         settings.put(MY_AUTH_USER_S, null);
         settings.put(MY_AUTH_PASS_S, null);
         settings.put(BASIC_AUTH_USER_S, Constants.DEFAULT.BASIC_AUTH_USER);
         settings.put(BASIC_AUTH_PASS_S, Constants.DEFAULT.BASIC_AUTH_PASS);
         settings.put(USE_REMINDER_SERVICE_B, Constants.DEFAULT.USE_REMINDER_SERVICE);
         settings.put(USE_REFRESHER_SERVICE_B, Constants.DEFAULT.USE_REFRESHER_SERVICE);
         settings.put(WORKING_HOURS_START_HOUR_I, Constants.DEFAULT.WORKING_HOURS_START_HOUR);
         settings.put(WORKING_HOURS_START_MIN_I, Constants.DEFAULT.WORKING_HOURS_START_MIN);
         settings.put(WORKING_HOURS_END_HOUR_I, Constants.DEFAULT.WORKING_HOURS_END_HOUR);
         settings.put(WORKING_HOURS_END_MIN_I, Constants.DEFAULT.WORKING_HOURS_END_MIN);
         settings.put(USE_SOUND_B, Constants.DEFAULT.USE_SOUND);
 
         restoreSettings();
     }
     
     /**
      * Restore preferences and load them into our variables
      */
     @Override
     public void restoreSettings() {
         for (String key : settings.keySet()) {
             Object value = restoreFromPreferences(preferences, key);
 //            if (key.contains("PASS") && value != null) {
 //                value = passwdUtil.decrypt((String) value);
 //            }
             if (value != null) {
                 settings.put(key, value);
             }
         }
     }
 
     /**
      * Persist our cache into persistent SharedPreferences
      */
     @Override
     public void persistSettings() {
         Log.d(TAG, "Persisting autoSettings: " + settings);
 
         // We need an Editor object to make preference changes.
         SharedPreferences.Editor editor = preferences.edit();
         for (String key : settings.keySet()) {
             Object value = getSetting(key, Object.class);
 //            if (key.contains("PASS") && value != null) {
 //                value = passwdUtil.encrypt((String) value);
 //            }
             persistIntoPreferencesEditor(editor, key, value);
         }
 
         editor.commit(); // Commit the edits!
     }
 
     private Object restoreFromPreferences(SharedPreferences preferences, String key) {
         if (key.endsWith(STRING_SUFFIX)) {
             return preferences.getString(key, null);
         } else if (key.endsWith(BOOLEAN_SUFFIX)) {
             return preferences.getBoolean(key, false);
         } else if (key.endsWith(INT_SUFFIX)) {
             return preferences.getInt(key, 0);
         } else if (key.endsWith(FLOAT_SUFFIX)) {
             return preferences.getFloat(key, 0);
         } else if (key.endsWith(LONG_SUFFIX)) {
             return preferences.getLong(key, 0);
         }
         return null;
     }
 
     private void persistIntoPreferencesEditor(SharedPreferences.Editor editor, String key, Object value) {
         if (key.endsWith(STRING_SUFFIX) || value == null) {
             editor.putString(key, (String) value);
         } else if (key.endsWith(BOOLEAN_SUFFIX)) {
             editor.putBoolean(key, (Boolean) value);
         } else if (key.endsWith(INT_SUFFIX)) {
             editor.putInt(key, (Integer) value);
         } else if (key.endsWith(FLOAT_SUFFIX)) {
             editor.putFloat(key, (Float) value);
         } else if (key.endsWith(LONG_SUFFIX)) {
             editor.putLong(key, (Long) value);
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T getSetting(String key, Class<T> clazz) {
         return (T) settings.get(key);
     }
 
     @Override
     public <T> void setSetting(String key, T value) {
         settings.put(key, value);
     }
 
     @Override
     public String print() {
         StringBuilder sb = new StringBuilder("-- AutoSettings (" + this + ") --\n");
         sb.append(settings).append("\n");
         return sb.append("------------------").toString();
     }
 }
