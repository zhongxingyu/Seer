 package com.atlassian.sal.refimpl.pluginsettings;
 
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 import com.atlassian.sal.api.ApplicationProperties;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * This implementation can be backed by a file on the file system.  If a file in the current working directory called
  * "pluginsettings.xml" exists (can be overridden with system property sal.pluginsettings.store) exists, it loads and
  * persists all plugin settings to and from this file.  If no file exists, plugin settings are purely in memory.
  */
 public class RefimplPluginSettingsFactory implements PluginSettingsFactory
 {
     private static final String CHARLIE_KEYS = "charlie.keys";
 
     private static final Logger log = Logger.getLogger(RefimplPluginSettingsFactory.class);
     private final Properties properties;
     private final File file;
 
     public RefimplPluginSettingsFactory(ApplicationProperties applicationProperties)
     {
         // Maintain backwards compatibility, check the old locations
         File file = new File(System.getProperty("sal.pluginsettings.store", "pluginsettings.xml"));
         properties = new Properties();
         if (!file.exists() || !file.canRead())
         {
             // Use refapp home directory
             File dataDir = new File(applicationProperties.getHomeDirectory(), "data");
             try
             {
                 if (!dataDir.exists())
                 {
                     dataDir.mkdirs();
                 }
                 file = new File(dataDir, "pluginsettings.xml");
                 file.createNewFile();
             }
             catch (IOException ioe)
             {
                 log.error("Error creating plugin settings properties, using memory store", ioe);
                 file = null;
             }
         }
         if (file != null && file.length() > 0)
         {
             InputStream is = null;
             try
             {
                 is = new FileInputStream(file);
                 properties.loadFromXML(is);
             }
             catch (Exception e)
             {
                 log.error("Error loading plugin settings properties, using memory store", e);
                 file = null;
             }
             finally
             {
                 if (is != null)
                 {
                     try
                     {
                         is.close();
                     }
                     catch (IOException ioe)
                     {
                         log.error("Error closing file", ioe);
                     }
                 }
             }
         }
         if (file != null)
         {
             // File is a new file
             log.info("Using " + file.getAbsolutePath() + " as plugin settings store");
         }
         this.file = file;
     }
 
     public PluginSettings createSettingsForKey(String key)
     {
        if (key != null)
         {
            List<String> charlies = (List<String>) new RefimplPluginSettings(new SettingsMap(null)).get(CHARLIE_KEYS);
            if (charlies == null || !charlies.contains(key))
            {
                throw new IllegalArgumentException("No Charlie with key " + key + " exists.");
            }
         }
         return new RefimplPluginSettings(new SettingsMap(key));
     }
 
     public PluginSettings createGlobalSettings()
     {
         return createSettingsForKey(null);
     }
 
     @SuppressWarnings("AccessToStaticFieldLockedOnInstance")
     private synchronized void store()
     {
         if (file == null || !file.canWrite())
         {
             // Read only settings
             return;
         }
         OutputStream os = null;
         try
         {
             os = new FileOutputStream(file);
             properties.storeToXML(os, "SAL Reference Implementation plugin settings");
         }
         catch (IOException ioe)
         {
             log.error("Error storing properties", ioe);
         }
         finally
         {
             if (os != null)
             {
                 try
                 {
                     os.close();
                 }
                 catch (IOException ioe)
                 {
                     log.error("Error closing output stream", ioe);
                 }
             }
         }
     }
 
     private class SettingsMap extends AbstractMap<String, String>
     {
         private final String settingsKey;
 
         private SettingsMap(String settingsKey)
         {
             if (settingsKey == null)
             {
                 this.settingsKey = "global.";
             }
             else
             {
                 this.settingsKey = "keys." + settingsKey + ".";
             }
         }
 
         public Set<Entry<String, String>> entrySet()
         {
             // Not used
             return Collections.emptySet();
         }
 
         public String get(Object key)
         {
             return properties.getProperty(settingsKey + key);
         }
 
         public String put(String key, String value)
         {
             String result = (String) properties.setProperty(settingsKey + key, value);
             store();
             return result;
         }
 
         public String remove(Object key)
         {
             String result = (String) properties.remove(settingsKey + key);
             store();
             return result;
         }
     }
 }
