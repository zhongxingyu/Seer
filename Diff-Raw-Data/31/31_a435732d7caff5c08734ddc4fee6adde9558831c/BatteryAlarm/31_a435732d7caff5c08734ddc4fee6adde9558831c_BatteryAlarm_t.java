 package uk.me.dewi.android.batteryalarm;
 
 import uk.me.dewi.android.preference.EditTimePreference;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class BatteryAlarm extends PreferenceActivity implements OnSharedPreferenceChangeListener{
 
     public static final String PREF_ENABLED = "prefEnabled";
     public static final String PREF_NOTIFICATION_SOUND = "prefNotificationSound";
     public static final String PREF_THRESHOLD = "prefThreshold";
     public static final String PREF_LAUNCH_ON_STARTUP = "prefLaunchOnStartup";
     public static final String PREF_DELAY_MINUTES = "prefDelayMinutes";
     public static final String PREF_DISABLE_AT_NIGHT = "prefDisableAtNight";
     public static final String PREF_MIN_TIME = "prefMinTime";
     public static final String PREF_MAX_TIME = "prefMaxTime";
     
     public static final int SOUND_TYPE_DEFAULT = 0;
     public static final int SOUND_TYPE_SYSTEM = 1;
     public static final int SOUND_TYPE_CUSTOM = 2;
     
     public static final int DEFAULT_DELAY_MINUTES = 20;
     public static final int DEFAULT_THRESHOLD = 15;
     public static final boolean DEFAULT_LAUNCH_ON_STARTUP = true;
     public static final int DEFAULT_NOTIFICATION_SOUND_TYPE = SOUND_TYPE_DEFAULT;
     
     public static final boolean DEFAULT_DISABLE_AT_NIGHT = false;
     public static final String DEFAULT_MIN_TIME = "9:00";
     public static final String DEFAULT_MAX_TIME = "23:00";
     
     public static SharedPreferences mSettings;
     public static BatteryAlarmLauncher mLauncher;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     
         addPreferencesFromResource(R.layout.main);  
         
         mSettings = PreferenceManager.getDefaultSharedPreferences(this);
         mSettings.registerOnSharedPreferenceChangeListener(this);
         mLauncher = new BatteryAlarmLauncher();
         
         setDefaultsIfEmpty();
         
         if(mSettings.getBoolean(PREF_ENABLED, true)){
             if(!mLauncher.isRunning()){
                 start();
             }
         }
         
         updateDelay(getPreferenceScreen().findPreference(PREF_DELAY_MINUTES));
         updateLauchOnStartup(getPreferenceScreen().findPreference(PREF_LAUNCH_ON_STARTUP));
         updateThreshold(getPreferenceScreen().findPreference(PREF_THRESHOLD));
         updateNotificationSound(getPreferenceScreen().findPreference(PREF_NOTIFICATION_SOUND));
        
        Preference disableAtNightPref = getPreferenceScreen().findPreference(PREF_DISABLE_AT_NIGHT);
        Preference minTimePref = getPreferenceScreen().findPreference(PREF_MIN_TIME);
        Preference maxTimePref = getPreferenceScreen().findPreference(PREF_MAX_TIME);
        updateDisableAtNight(disableAtNightPref, minTimePref, maxTimePref);
     }
 
     @Override
     protected void onDestroy() {
         mSettings.unregisterOnSharedPreferenceChangeListener(this);
         super.onDestroy();
     }
     
     @Override
     protected void onStop(){
         super.onStop();
         System.exit(0);
     }
     
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         Preference preference = getPreferenceScreen().findPreference(key);
         
        Preference disableAtNightPref = getPreferenceScreen().findPreference(PREF_DISABLE_AT_NIGHT);
        Preference minTimePref = getPreferenceScreen().findPreference(PREF_MIN_TIME);
        Preference maxTimePref = getPreferenceScreen().findPreference(PREF_MAX_TIME);
        
         if(PREF_ENABLED.equals(preference.getKey())){
             if(mSettings.getBoolean(preference.getKey(), false)){
                 start();
             }
             else {
                 stop();
             }
             
         }
         else if(PREF_DELAY_MINUTES.equals(preference.getKey())){
             updateDelay(preference);
             restart();
         }
         else if(PREF_LAUNCH_ON_STARTUP.equals(preference.getKey())){
             updateLauchOnStartup(preference);
         }
         else if(PREF_THRESHOLD.equals(preference.getKey())){
             updateThreshold(preference);
             restart();
         }
         else if(PREF_NOTIFICATION_SOUND.equals(preference.getKey())){
             updateNotificationSound(preference);
             restart();
         }
         else if(PREF_DISABLE_AT_NIGHT.equals(preference.getKey())){
            updateDisableAtNight(preference, minTimePref, maxTimePref);
             restart();
         }
         else if(PREF_MIN_TIME.equals(preference.getKey())){
            updateDisableAtNight(disableAtNightPref, preference, maxTimePref);
             restart();
         }
         else if(PREF_MAX_TIME.equals(preference.getKey())){
            updateDisableAtNight(disableAtNightPref, minTimePref, preference);
             restart();
         }
     }
 
     public void stop() {
         mLauncher.stop(this);
     }
 
     public void start() {
         mLauncher.launch(this);
     }
 
     public void restart(){
         if(mLauncher.isRunning()){
             stop();
             start();
         }
     }
 
     public void updateThreshold(Preference preference) {
         preference.setSummary(getString(R.string.threshold_summary)
                                     + ' '
                                     + getThreshold()
                                     + '%');
     }
 
 
     public void updateDelay(Preference preference) {
         preference.setSummary(getString(R.string.delay_summary)
                                     + ' '
                                     + getDelayMinutes()
                                     + ' '
                                     + getString(R.string.minutes));
     }
 
 
     public void updateLauchOnStartup(Preference preference) {
         if(mSettings.getBoolean(preference.getKey(), true)){
             preference.setSummary(getString(R.string.will_launch_on_startup));
         }
         else {
             preference.setSummary(getString(R.string.will_not_launch_on_startup));
         }
     }
     
     public void updateNotificationSound(Preference preference) {
         String url = mSettings.getString(PREF_NOTIFICATION_SOUND, "");
         if(url.length() > 0){
             preference.setSummary("Ringtone "+url.substring(url.lastIndexOf('/')+1));
         }
         else{
             preference.setSummary("");
         }
         
     }
     
    private void updateDisableAtNight(Preference disableAtNightPref, Preference minTimePref, Preference maxTimePref) {

         String minTime = convert24hrTo12(mSettings.getString(PREF_MIN_TIME, DEFAULT_MIN_TIME));
         String maxTime = convert24hrTo12(mSettings.getString(PREF_MAX_TIME, DEFAULT_MAX_TIME));
         
         if(mSettings.getBoolean(PREF_DISABLE_AT_NIGHT, DEFAULT_DISABLE_AT_NIGHT)){
             minTimePref.setEnabled(true);
             maxTimePref.setEnabled(true);
             minTimePref.setSummary(minTime);
             maxTimePref.setSummary(maxTime);
             disableAtNightPref.setSummary("Alert between " + minTime+ " and " +maxTime);
         }
         else{
             minTimePref.setEnabled(false);
             maxTimePref.setEnabled(false);
             maxTimePref.setSummary("");
             minTimePref.setSummary("");
             disableAtNightPref.setSummary("Alert at any time");
         }
         
     }
     
     public int getDelayMinutes(){
         return Integer.parseInt(mSettings.getString(PREF_DELAY_MINUTES, Integer.valueOf(DEFAULT_DELAY_MINUTES).toString()));
     }
     
     public int getThreshold(){
         return Integer.parseInt(mSettings.getString(PREF_THRESHOLD, Integer.valueOf(DEFAULT_THRESHOLD).toString()));
     }
     
     public void setDefaultsIfEmpty(){
         Editor editor = mSettings.edit();
         if(!mSettings.contains(PREF_ENABLED)){
             editor.putBoolean(PREF_ENABLED, false);
         }
         if(!mSettings.contains(PREF_DELAY_MINUTES)){
             editor.putString(PREF_DELAY_MINUTES, Integer.valueOf(DEFAULT_DELAY_MINUTES).toString());
         }
         if(!mSettings.contains(PREF_THRESHOLD)){
             editor.putString(PREF_THRESHOLD, Integer.valueOf(DEFAULT_THRESHOLD).toString());
         }
         if(!mSettings.contains(PREF_LAUNCH_ON_STARTUP)){
             editor.putBoolean(PREF_LAUNCH_ON_STARTUP, false);
         }
         if(!mSettings.contains(PREF_DISABLE_AT_NIGHT)){
             editor.putBoolean(PREF_DISABLE_AT_NIGHT, false);
         }
         if(!mSettings.contains(PREF_MIN_TIME)){
             editor.putString(PREF_MIN_TIME, DEFAULT_MIN_TIME);
         }
         if(!mSettings.contains(PREF_MAX_TIME)){
             editor.putString(PREF_MAX_TIME, DEFAULT_MAX_TIME);
         }
         editor.commit();
     }
 
     public static int parseHours(String time){
         return Integer.parseInt(time.split(":")[0]);
     }
     
     public static int parseMinutes(String time){
         return Integer.parseInt(time.split(":")[1]);
     }
     
     public static String convert24hrTo12(String time){
         int hours = parseHours(time);
        int minutes = parseMinutes(time);
         boolean isPM = hours > 12;
         if(isPM) hours -= 12;
         return "" + hours + ":" + EditTimePreference.MINUTE_FMT.format(minutes) + (isPM ? "pm" : "am");
     }
     
 }
