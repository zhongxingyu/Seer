 package de.hswt.hrm.common;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Properties;
 
 /**
  * Singleton that holds the current configuration.
  */
public class Config {
     private static Config instance;
     private Properties props = new Properties();
 
    private Config() {

    }
 
     /**
      * @return The current configuration instance.
      */
     public static Config getInstance() {
         if (instance == null) {
             instance = new Config();
         }
 
         return instance;
     }
 
     /**
      * Replace the current configuration with the content of the given properties file.
      * 
      * @param path
      *            Path to the properties file.
      * @throws IOException
      */
     public void load(Path path) throws IOException {
         checkArgument(Files.exists(path), "Configuration file is not present.");
         props = loadFromFile(path);
     }
 
     /**
      * You mostly should use keys from {@link Keys} for the key.
      * 
      * @see {@link Properties#setProperty(String, String)}
      */
     public void setProperty(String key, String value) {
         props.setProperty(key, value);
     }
 
     /**
      * You mostly should use keys from {@link Keys} for the key.
      * 
      * @see {@link Properties#getProperty(String)}
      */
     public String getProperty(String key) {
         return props.getProperty(key);
     }
 
     /**
      * You mostly should use keys from {@link Keys} for the key.
      * 
      * @see {@link Properties#getProperty(String, String)}
      */
     public String getProperty(String key, String defaultValue) {
         return props.getProperty(key, defaultValue);
     }
 
     private Properties loadFromFile(Path path) throws IOException {
         Properties props = new Properties();
         props.load(Files.newInputStream(path));
         return props;
     }
 
     /**
      * Holds the possible keys from the configuration.
      */
     public static class Keys {
         public static final String DB_HOST = "db_host";
         public static final String DB_USER = "db_user";
         public static final String DB_PASSWORD = "db_password";
         public static final String DB_NAME = "db_name";
     }
 }
