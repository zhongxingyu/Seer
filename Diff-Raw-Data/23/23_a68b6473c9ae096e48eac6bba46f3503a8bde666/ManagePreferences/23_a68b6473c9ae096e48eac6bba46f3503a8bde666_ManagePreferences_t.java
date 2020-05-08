 package net.anei.cadpage;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.preference.PreferenceManager;
 
 public class ManagePreferences {
   
   // Preference version.  This needs to be incremented every time a new
   // configuration setting is added to force it to initialize properly
   // when the new release is first run.
   // (OK, if you know what you are doing, and the only new settings added
   // are boolean settings that default to false, you can get away with not
   // changing this)
  private static final int PREFERENCE_VERSION = 8;
   
   private static ManagePreferences prefs;
 
   /**
    * Initialize the ManagePreferences class
    * @param context
    */
   public static void setupPreferences(Context context) {
     
     // Initialize the preference object
     prefs = new ManagePreferences();
     prefs.setContext(context);
 
     // Before we do anything else, see what the old preference version number was
     int oldVersion = prefs.getInt(R.string.pref_version_key, 0);
     
     // If the old version doesn't match the current version, we need to reload
     // the preference defaults and update the preference version
     if (oldVersion != PREFERENCE_VERSION) {
       PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
       prefs.putInt(R.string.pref_version_key, PREFERENCE_VERSION);
     }
     
     // If old version was < 1, we need to reset the popup button configuration settings
     if (oldVersion < 1) {
       prefs.putString(R.string.pref_button1_key, context.getString(R.string.pref_button1_default));
       prefs.putString(R.string.pref_button2_key, context.getString(R.string.pref_button2_default));
       prefs.putString(R.string.pref_button3_key, context.getString(R.string.pref_button3_default));
     }
     
     // If location code is the old numeric code, convert it
     String location = location();
     if (location.length() <= 2) {
       int code = Integer.parseInt(location) - 1;
       String[] legacy = context.getResources().getStringArray(R.array.pref_location_legacy);
       if (code >= legacy.length) {
         location = "General";
       } else {
         location = legacy[code];
       }
       setLocation(location);
     }
     
     // Ditto if is a newer parser code that has been renamed,
     else {
       String newLocation = convertOldLocationCode(context, location);
       if (! location.equals(newLocation)) {
         setLocation(newLocation);
       }
     }
   }
   
   /**
    * Convert any old obsolete location codes to new equivalent
    * @param current context
    * @param location location to be checked
    * @return the new location that should be used
    */
   public static String convertOldLocationCode(Context context, String location) {
     
     // Scan through all of the location codes looking to see if any of thm
     // match an old code
     String[] oldCodes = context.getResources().getStringArray(R.array.old_location_values);
     String[] locs = location.split(",");
     boolean bad = false;
     for (String loc : locs) {
       for (String code : oldCodes) {
         if (loc.equals(code)) {
           bad = true;
           break;
         }
       }
       if (bad) break;
     }
     
     // If not, the usual case, return the original location string
     if (!bad) return location;
     
     String[] newCodes = context.getResources().getStringArray(R.array.new_location_values);
     ArrayList<String> locList = new ArrayList<String>();
     for (String loc : locs) {
       for (int ndx = 0; ndx < oldCodes.length; ndx++) {
         if (loc.equals(oldCodes[ndx])) {
           loc = newCodes[ndx];
           break;
         }
       }
       if (!locList.contains(loc)) locList.add(loc);
     }
     
     Collections.sort(locList);
     StringBuilder sb = new StringBuilder();
     for (String loc : locList) {
       if (sb.length() > 0) sb.append(',');
       sb.append(loc);
     }
     return sb.toString();
   }
   
   /**
    * Backdoor allows test classes to substitute their own ManagerPreference object
    * for the standard one
    * @param prefs custom ManagerPreference object
    */
   protected static void setTestPreference(ManagePreferences prefs) {
     ManagePreferences.prefs = prefs;
   }
   
   public static boolean initialized() {
     return prefs.getBoolean(R.string.pref_initialized_key);
   }
   
   public static String location() {
     return prefs.getString(R.string.pref_location_key, "General");
   }
   
   public static void setLocation(String newLocation) {
     prefs.putString(R.string.pref_location_key, newLocation);
   }
   
   public static boolean overrideFilter() {
     return prefs.getBoolean(R.string.pref_override_filter_key);
   }
   
   public static String filter() {
     return prefs.getString(R.string.pref_filter_key);
   }
   
   public static boolean genAlert() {
     return prefs.getBoolean(R.string.pref_gen_alert_key);
   }
   
   public static boolean showSource() {
     return prefs.getBoolean(R.string.pref_show_source_key);
   }
   
   public static boolean overrideDefaults() {
     return prefs.getBoolean(R.string.pref_override_default_key);
   }
   
   public static String defaultCity() {
     return prefs.getString(R.string.pref_defcity_key);
   }
   
   public static String defaultState() {
     return prefs.getString(R.string.pref_defstate_key);
   }
   
   public static int logLimit() {
     return Integer.parseInt(prefs.getString(R.string.pref_loglimit_key));
   }
   
   public static int partMsgTimeout() {
     return Integer.parseInt(prefs.getString(R.string.pref_msgtimeout_key));
   }
   
   public static boolean suppressDupMsg() {
     return prefs.getBoolean(R.string.pref_suppress_dup_msg_key);
   }
   
   public static boolean notifyEnabled() {
     return prefs.getBoolean(R.string.pref_notif_enabled_key);
   }
   
   public static boolean notifyOverride() {
     return prefs.getBoolean(R.string.pref_notif_override_key);
   }
   
   public static String notifySound() {
     return prefs.getString(R.string.pref_notif_sound_key);
   }
   
   public static boolean vibrate() {
     return prefs.getBoolean(R.string.pref_vibrate_key);
   }
   
   public static String vibratePattern() {
     return prefs.getString(R.string.pref_vibrate_pattern_key);
   }
   
   public static String vibratePatternCustom() {
     if ( prefs.getString(R.string.pref_vibrate_pattern_custom_key) == null){
       return prefs.getString(R.string.pref_vibrate_pattern_key);
     }
     return prefs.getString(R.string.pref_vibrate_pattern_custom_key);
   }
   
   public static boolean flashLED() {
     return prefs.getBoolean(R.string.pref_flashled_key);
   }
   
   public static String flashLEDColor() {
     return prefs.getString(R.string.pref_flashled_color_key);
   }
   
  public static String textSize() {
    if (prefs.getString(R.string.pref_textsize_key)==null){
      return prefs.getString(R.string.pref_textsize_default);
    }
   return  prefs.getString(R.string.pref_textsize_key);
  }
  
   public static String flashLEDColorCustom() {
     if (prefs.getString(R.string.pref_flashled_color_custom_key)==null){
       return prefs.getString(R.string.pref_flashled_color_key);
     }
     return prefs.getString(R.string.pref_flashled_color_custom_key);
   }
   
   public static String flashLEDPattern() {
     return prefs.getString(R.string.pref_flashled_pattern_key);
   }
   
   public static String flashLEDPatternCustom() {
     if (prefs.getString(R.string.pref_flashled_pattern_custom_key)==null){
       return prefs.getString(R.string.pref_flashled_pattern_key);
     }
     return prefs.getString(R.string.pref_flashled_pattern_custom_key);
   }
   
   public static boolean notifyRepeat() {
     return prefs.getBoolean(R.string.pref_notif_repeat_key);
   }
   
   public static int repeatInterval() {
     return Integer.parseInt(prefs.getString(R.string.pref_notif_repeat_interval_key));
   }
   
   public static int repeatTimes() {
     return Integer.parseInt(prefs.getString(R.string.pref_notif_repeat_times_key));
   }
   
   public static boolean popupEnabled() {
     return prefs.getBoolean(R.string.pref_popup_enabled_key);
   }
   
   public static boolean onlyShowOnKeyguard() {
     return prefs.getBoolean(R.string.pref_onlyShowOnKeyguard_key);
   }
   
   public static boolean noShowInCall() {
     return prefs.getBoolean(R.string.pref_noShowInCall_key);
   }
 
   public static boolean smspassthru() {
     return prefs.getBoolean(R.string.pref_passthrusms_key);
   }
   
   public static boolean screenOn() {
     return prefs.getBoolean(R.string.pref_screen_on_key);
   }
   
   public static int timeout() {
     return Integer.parseInt(prefs.getString(R.string.pref_timeout_key));
   }
   
   public static boolean dimScreen() {
     return prefs.getBoolean(R.string.pref_dimscreen_key);
   }
   
   public static boolean privacyMode() {
     return prefs.getBoolean(R.string.pref_privacy_key);
   }
   
   public static boolean autoRotate() {
     return prefs.getBoolean(R.string.pref_autorotate_key);
   }
 
   public static int historyCount() {
     return Integer.parseInt(prefs.getString(R.string.pref_history_limit_key));
   }
   
   public static boolean showButtons() {
     return prefs.getBoolean(R.string.pref_show_buttons_key);
   }
   
   public static int popupButton(int button) {
     
     switch (button) {
     case 1:
       return Integer.parseInt(prefs.getString(R.string.pref_button1_key));
       
     case 2:
       return Integer.parseInt(prefs.getString(R.string.pref_button2_key));
       
     case 3:
       return Integer.parseInt(prefs.getString(R.string.pref_button3_key));
       
     default:    // Anything else is disabled
       return 0;
     }
   }
 
   public static String getCallback() {
     return prefs.getString(R.string.pref_callback_key);
   }
 
 
   /**
    * Append configuration information to constructed message
    * @param context active context
    * @param sb Stringbuilder object where message is constructed
    */
   public static void addConfigInfo(Context context, StringBuilder sb) {
     sb.append(String.format("\n\n----------\nSysinfo - %s\nModel: %s\n\n",
         Build.FINGERPRINT, Build.MODEL));
     
     sb.append("Preference Configuration:\n");
 
     // Array of preference keys to include in email
     final int[] pref_keys = {
         R.string.pref_initialized_key,
         R.string.pref_enabled_key,
         R.string.pref_location_key,
         R.string.pref_override_filter_key,
         R.string.pref_filter_key,
         R.string.pref_gen_alert_key,
         R.string.pref_show_source_key,
         R.string.pref_callback_key,
         R.string.pref_override_default_key,
         R.string.pref_defcity_key,
         R.string.pref_defstate_key,
         R.string.pref_loglimit_key,
         
         R.string.pref_notif_enabled_key,
         R.string.pref_notif_override_key,
         R.string.pref_notif_sound_key,
         
         R.string.pref_vibrate_key,
         R.string.pref_vibrate_pattern_key,
         R.string.pref_vibrate_pattern_custom_key,
         
         R.string.pref_flashled_key,
         R.string.pref_flashled_color_key,
         R.string.pref_flashled_color_custom_key,
         R.string.pref_flashled_pattern_key,
         R.string.pref_flashled_pattern_custom_key,
 
         R.string.pref_notif_repeat_key,
         R.string.pref_notif_repeat_times_key,
         R.string.pref_notif_repeat_interval_key,
 
         R.string.pref_popup_enabled_key,
         R.string.pref_onlyShowOnKeyguard_key,
         R.string.pref_noShowInCall_key,
         R.string.pref_passthrusms_key,
         
         R.string.pref_screen_on_key,
         R.string.pref_timeout_key,
         R.string.pref_dimscreen_key,
         R.string.pref_privacy_key,
         
         R.string.pref_autorotate_key,
         
         R.string.pref_show_buttons_key,
         R.string.pref_button1_key,
         R.string.pref_button2_key,
         R.string.pref_button3_key
     };
 
     Map<String, ?> map = prefs.mPrefs.getAll();
     
     for (int key : pref_keys) {
       String keyName = context.getString(key);
       sb.append(String.format("%s: %s\n", keyName, map.get(keyName)));
     }
 
     // Add locale info
     sb.append(String.format("locale: %s\n",
         context.getResources().getConfiguration().locale.getDisplayName()));
   }
 
   
   
   
   private Context context;
   private SharedPreferences mPrefs;
   
   
   protected ManagePreferences() {}
   
   /**
    * private constructor
    * @param _context context
    */
    void setContext(Context _context) {
     context = _context;
     mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
   }
   
   protected boolean getBoolean(int resPrefId) {
     return mPrefs.getBoolean(context.getString(resPrefId), false);
   }
   
   protected String getString(int resPrefId) {
     String result = mPrefs.getString(context.getString(resPrefId), null);
     if (result == null) throw new RuntimeException("No configured preference value found");
     return result;
   }
   
   protected int getInt(int resPrefId, int defValue) {
     return mPrefs.getInt(context.getString(resPrefId), defValue);
   }
   
   protected int getInt(int resPrefId) {
     int result = mPrefs.getInt(context.getString(resPrefId), Integer.MAX_VALUE);
     if (result == Integer.MAX_VALUE) throw new RuntimeException("No configured preference value found");
     return result;
   }
 
   public void putString(int resPrefId, String newVal) {
     SharedPreferences.Editor settings = mPrefs.edit();
     settings.putString(context.getString(resPrefId), newVal);
     settings.commit();
   }
 
   public void putInt(int resPrefId, int newVal) {
     SharedPreferences.Editor settings = mPrefs.edit();
     settings.putInt(context.getString(resPrefId), newVal);
     settings.commit();
   }
   
   // All of the following methods are deprecated, but we only officially
   // flag the constructor
 
   /**
    * @deprecated - Preferred alternatives is to set up and use the static methods
    * to retrieve preference values 
    */
   public ManagePreferences(Context _context) {
     setContext(_context);
   }
 
   public boolean getBoolean(int resPrefId, int resDefaultId, int dbColumnNum) {
     return getBoolean(resPrefId, resDefaultId);
   }
 
   public boolean getBoolean(int resPrefId, boolean prefDefault, int dbColumnNum) {
     return getBoolean(resPrefId, prefDefault);
   }
 
   public boolean getBoolean(int resPrefId, int resDefaultId) {
     return mPrefs.getBoolean(context.getString(resPrefId),
         Boolean.parseBoolean(context.getString(resDefaultId)));
   }
 
   public boolean getBoolean(int resPrefId, boolean prefDefault) {
     return mPrefs.getBoolean(context.getString(resPrefId), prefDefault);
   }
 
   public String getString(int resPrefId, int resDefaultId, int dbColumnNum) {
     return getString(resPrefId, resDefaultId);
   }
 
   public String getString(int resPrefId, String defaultVal, int dbColumnNum) {
     return mPrefs.getString(context.getString(resPrefId), defaultVal);
   }
 
   public String getString(int resPrefId, int resDefaultId) {
     return mPrefs.getString(context.getString(resPrefId), context.getString(resDefaultId));
   }
 
   public String getString(int resPrefId, String defaultVal) {
     return mPrefs.getString(context.getString(resPrefId), defaultVal);
   }
   
   public void close() {}
 }
