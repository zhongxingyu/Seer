 package com.akrivos.eos.config;
 
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 /**
  * An Enum singleton used for application's settings.
  */
 public enum Settings {
     INSTANCE;
 
     public static final SettingKey<String> SERVER_ADDRESS =
             new SettingKey<String>("server.address", "0.0.0.0");
 
     public static final SettingKey<Integer> SERVER_PORT =
             new SettingKey<Integer>("server.port", 8080);
 
     public static final SettingKey<String> SERVER_ROOT =
             new SettingKey<String>("server.root", "~/www");
 
     public static final SettingKey<String> SERVER_INDEX_NAMES =
             new SettingKey<String>("server.index.names", "index.html index.htm");
 
     private static final Logger logger = Logger.getLogger(Settings.class);
     private final Map<String, String> map;
 
     /**
      * Initialises the singleton instance of the Settings enum.
      */
     private Settings() {
         map = new HashMap<String, String>();
     }
 
     /**
      * Returns the value for a setting as a String. If the key is found, it
      * returns the value, otherwise, it returns the setting's default value.
      *
      * @param settingKey the setting's key.
      * @return if found, it returns the value as String, otherwise,
      *         it returns the setting's default value.
      */
     public String getValueFor(SettingKey<String> settingKey) {
         String value = map.get(settingKey.getKey());
         if (value != null) {
             // special case for SERVER_ROOT with ~
             if (settingKey == SERVER_ROOT && value.startsWith("~")) {
                 return value.replaceFirst("~", System.getProperty("user.home"));
             }
            return value;
         }
 
         if (logger.isTraceEnabled()) {
             logger.trace("There is no setting for " + settingKey.getKey()
                     + ". Returning default value: "
                     + settingKey.getDefaultValue());
         }
        if (settingKey == SERVER_ROOT && settingKey.getDefaultValue().startsWith("~")) {
            return settingKey.getDefaultValue().replaceFirst("~", System.getProperty("user.home"));
        }
         return settingKey.getDefaultValue();
     }
 
     /**
      * Returns the value for a setting as an int. If the key is found, it
      * returns the value, otherwise, it returns the setting's default value.
      *
      * @param settingKey the setting's key.
      * @return if found, it returns the value as int, otherwise,
      *         it returns the setting's default value.
      */
     public int getValueAsIntegerFor(SettingKey<Integer> settingKey) {
         try {
             String value = map.get(settingKey.getKey());
             if (value != null) {
                 return Integer.parseInt(value);
             }
 
             if (logger.isTraceEnabled()) {
                 logger.trace("There is no setting for " + settingKey.getKey()
                         + ". Returning default value: "
                         + settingKey.getDefaultValue());
             }
             return settingKey.getDefaultValue();
         } catch (NumberFormatException ex) {
             if (logger.isDebugEnabled()) {
                 logger.debug("Invalid setting for " + settingKey.getKey()
                         + ". Returning default value: "
                         + settingKey.getDefaultValue());
             }
             return settingKey.getDefaultValue();
         }
     }
 
     /**
      * Loads the configuration from a properties file.
      *
      * @param propertiesFile the properties file path.
      */
     public void loadSettings(String propertiesFile) {
         Properties props = new Properties();
         try {
             props.load(new FileInputStream(propertiesFile));
 
             // load all the settings from the file and put them in the map
             for (Object key : props.keySet()) {
                 final String keyStr = String.valueOf(key);
                 map.put(keyStr, props.getProperty(keyStr));
             }
         } catch (IOException ex) {
             logger.error("Could not load configuration from properties file: "
                     + propertiesFile + ". Using default server configuration");
         }
     }
 
     /**
      * Checks if every setting in server configuration is valid.
      *
      * @return true if all settings are valid, false otherwise.
      */
     public boolean areValid() {
         try {
             if (logger.isTraceEnabled()) {
                 logger.trace("Validating server configuration...");
             }
 
             String address = Settings.INSTANCE.getValueFor(Settings.SERVER_ADDRESS);
             Pattern ipPattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                     + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                     + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                     + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
             if (!ipPattern.matcher(address).matches()) {
                 logger.error("Error in server configuration: The server address "
                         + "is not a valid ip address (" + address + ")");
                 return false;
             }
 
             int port = Settings.INSTANCE.getValueAsIntegerFor(Settings.SERVER_PORT);
             if (port < 1 || port > 65535) {
                 logger.error("Error in server configuration: The server port "
                         + "is out of range 1-65535 (" + port + ")");
                 return false;
             }
 
             String root = Settings.INSTANCE.getValueFor(Settings.SERVER_ROOT);
             File rootDirectory = new File(root);
             if (!rootDirectory.getCanonicalFile().isDirectory()) {
                 logger.error("Error in server configuration: The server root "
                         + "address is not a valid directory (" + root + ")");
                 return false;
             }
 
             if (logger.isTraceEnabled()) {
                 logger.trace("Server configuration validated successfully");
             }
             return true;
         } catch (Exception ex) {
             logger.error("Error while validating configuration: "
                     + ex.getMessage());
             return false;
         }
     }
 }
