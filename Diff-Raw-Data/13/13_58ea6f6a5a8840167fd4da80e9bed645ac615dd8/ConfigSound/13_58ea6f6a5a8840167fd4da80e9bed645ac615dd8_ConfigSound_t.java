 package com.taamc.PlayerDinger;
 
 import org.bukkit.plugin.Plugin;
  
  
 public class ConfigSound extends Config {
  
     public ConfigSound(Plugin plugin) {
         this.setFile(plugin);
     }
  
     String sound = "LEVEL_UP";
    double pitch = .5;
    double volume = 2;
  
 }
