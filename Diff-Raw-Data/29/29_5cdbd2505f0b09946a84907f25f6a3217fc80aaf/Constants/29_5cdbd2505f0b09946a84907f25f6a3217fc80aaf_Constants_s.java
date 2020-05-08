 package org.thereflectproject.trackjd;
 
 public interface Constants {
   /**
    * Application-wide preferences.
    */
   public static String PREFS_FILE_NAME = "prefs";
   
   /**
    * Key for the preference that stores the server name. Note: includes the
    * port but not the path or the 'http://' prefix.
    */
   public static String PREF_SERVER_NAME = "server_name";
 
   /**
    * Boolean.
    */
   public static String PREF_DIM_SCREEN = "dim_screen";
 
   /**
    * Integer in microseconds.
    */
   public static String PREF_ACCELEROMETER_INTERVAL = "accelerometer_interval";
 
   /**
    * Integer in microseconds.
    */
   public static String PREF_ORIENTATION_INTERVAL = "orientation_interval";
 
   /**
    * Integer in milliseconds.
    */
   public static String PREF_BLUETOOTH_INTERVAL = "bluetooth_interval";
 }
