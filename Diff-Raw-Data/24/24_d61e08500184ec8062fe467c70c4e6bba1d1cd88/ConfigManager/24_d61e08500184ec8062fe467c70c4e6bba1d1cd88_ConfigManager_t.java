 package me.mentioum.plugins.lpmcnavigator;
 
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.config.Configuration;
 
 public class ConfigManager {
     
     private static Configuration config;
     
     public static void Startup(Plugin plugin)
     {
         config = plugin.getConfiguration();
         config.load();
         PluginEnabled();
        config.save();
     }
     
 //    Adding an easy way for Admins to disable the plugin without having to move files around on the server.
     public static Boolean PluginEnabled()
     {
         return config.getBoolean("Plugin Enabled", true);
     }
     
     
     
 }
