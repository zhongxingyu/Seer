 package com.cyanogenmod.settings.device;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceManager;
 
 public class ButtonBrightnessControl implements OnPreferenceChangeListener {
 
     private static final String FILE = "/sys/class/leds/button-backlight/button_brightness";
 
     public static boolean isSupported() {
         return Utils.fileWritable(FILE);
     }
 
    public static String getValue(Context context) {
        String value = Utils.getFileValue(FILE, "150");
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
         return sharedPrefs.getString(DeviceSettings.KEY_BUTTON_BRIGHTNESS, value);
    }
 	
     /**
      * Restore Buttonlight Brightness setting from SharedPreferences. (Write to kernel.)
      * @param context       The context to read the SharedPreferences from
      */
     public static void restore(Context context) {
         if (!isSupported()) {
             return;
         }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean backlightDisabled = sharedPrefs.getBoolean(DeviceSettings.KEY_BACKLIGHTDISABLE, false);
 
        if (!backlightDisabled) {
	     String value = getValue(context);
             Utils.writeValue(FILE, value);
             return;
        }
        Utils.writeValue(FILE, "0");
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         Utils.writeValue(FILE, (String) newValue);
         return true;
     }
 
 }
