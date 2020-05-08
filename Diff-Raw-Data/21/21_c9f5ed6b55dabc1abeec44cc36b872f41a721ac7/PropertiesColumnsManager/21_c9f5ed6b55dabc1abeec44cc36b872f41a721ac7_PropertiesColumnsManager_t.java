 package com.mymed.properties;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import com.mymed.utils.MLogger;
 
 /**
  * Properties manager for the columns and supercolumns names in the database
  * 
  * @author Milo Casagrande
  * 
  */
 public class PropertiesColumnsManager implements IProperties {
 
   private static final PropertiesColumnsManager INSTANCE = new PropertiesColumnsManager();
   private static Properties properties;
 
   /**
    * Initialize the instance, loading the values from the columns name
    * properties file
    */
   private PropertiesColumnsManager() {
     try {
       properties = new Properties();
       final FileInputStream in = new FileInputStream("columns.properties");
       properties.load(in);
     } catch (final FileNotFoundException ex) {
      // We should never, ever, ever get here!
       MLogger.getLogger().debug("Impossible to find 'column.properties' properties file in the PATH", ex);
     } catch (final IOException ex) {
      // Nor here!
       MLogger.getLogger().debug("Error loading the properties from the 'column.properties' file", ex);
     }
   }
 
   /**
    * The class is implemented as a singleton
    * 
    * @return the properties manager instance for the columns name
    */
   public static IProperties getInstance() {
     return INSTANCE;
   }
 
   @Override
   public String get(final String key) {
     return properties.getProperty(key);
   }
 }
