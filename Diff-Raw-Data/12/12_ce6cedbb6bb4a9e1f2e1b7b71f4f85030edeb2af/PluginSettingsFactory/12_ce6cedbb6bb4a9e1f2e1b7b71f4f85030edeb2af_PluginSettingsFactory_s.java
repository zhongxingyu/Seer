 package com.atlassian.sal.api.pluginsettings;
 
 /**
  * Factory for mutable, non-threadsafe PluginSettings objects.
  *
  * @since 2.0
  */
 public interface PluginSettingsFactory
 {
     /**
     * Gets all settings for a key, usually a space, project, or repository key
      *
     * @param key The key, can be null to retrieve global settings
      * @throws IllegalArgumentException if no "concept" for the key can be found
      * @return The settings
      */
     PluginSettings createSettingsForKey(String key);
 
     /**
     * Gets all global settings
      *
      * @return Global settings
      */
     PluginSettings createGlobalSettings();
 }
