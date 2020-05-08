 package info.eigenein.openwifi.helpers;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import java.util.UUID;
 
 /**
  * Wraps preference manager for convenience.
  */
 public class Settings {
     private static final String LOG_TAG = Settings.class.getCanonicalName();
 
     public static final String SCAN_PERIOD_KEY = "scan_period";
 
     public static final String IS_NETWORK_PROVIDER_ENABLED_KEY = "is_network_provider_enabled";
 
     public static final String SHARE_DATABASE_KEY = "share_database";
 
     public static final String STATISTICS_KEY = "show_statistics";
 
     public static final String MAX_SCAN_RESULTS_FOR_BSSID_KEY = "max_scan_results_for_bssid";
 
     public static final String CLIENT_ID_KEY = "client_id";
 
     public static final String LAST_SYNC_ID_KEY = "last_sync_id";
 
     public static final String SYNC_NOW_KEY = "sync_now";
 
     private final SharedPreferences preferences;
 
     public static Settings with(Context context) {
         return new Settings(context);
     }
 
     private Settings(Context context) {
         this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
     }
 
     public boolean isNetworkProviderEnabled() {
         return preferences.getBoolean(IS_NETWORK_PROVIDER_ENABLED_KEY, false);
     }
 
     public int maxScanResultsForBssidCount() {
        return Integer.parseInt(preferences.getString(MAX_SCAN_RESULTS_FOR_BSSID_KEY, "4"));
     }
 
     public long scanPeriod() {
         String periodString = preferences.getString(SCAN_PERIOD_KEY, "60");
         return 1000L * Long.parseLong(periodString);
     }
 
     public String clientId() {
         String clientId = preferences.getString(CLIENT_ID_KEY, null);
         if (clientId == null) {
             clientId = UUID.randomUUID().toString();
             Log.i(LOG_TAG, "clientId: " + clientId);
             preferences.edit().putString(CLIENT_ID_KEY, clientId).commit();
         }
         return clientId;
     }
 
     /**
      * Gets the ID of the last synchronized scan result.
      */
     public String lastSyncId() {
         String syncId = preferences.getString(LAST_SYNC_ID_KEY, null);
         if (syncId != null) {
             return syncId;
         } else {
             // Return the minimal object ID.
             return "000000000000000000000000";
         }
     }
 
     public SettingsEditor edit() {
         return new SettingsEditor(preferences.edit());
     }
 
     public class SettingsEditor {
         private final SharedPreferences.Editor editor;
 
         private SettingsEditor(SharedPreferences.Editor editor) {
             this.editor = editor;
         }
 
         public void commit() {
             editor.commit();
         }
 
         /**
          * Sets the ID of the last synchronized scan result.
          */
         public SettingsEditor lastSyncId(String syncId) {
             editor.putString(LAST_SYNC_ID_KEY, syncId);
             return this;
         }
     }
 }
