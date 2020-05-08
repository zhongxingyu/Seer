 package com.kolinkrewinkel.BitLimitTweaks;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.Plugin;
 import java.util.*;
 
 public class BitLimitTweaks extends JavaPlugin {
 
     @Override
     public void onEnable() {
        new BitLimitTNTListener(this);
 
         this.getCommand("tweaks").setExecutor(new TweaksCommandExecutor(this));
     }
 
     @Override
     public void onDisable() {        
         // save the configuration file, if there are no values, write the defaults.
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
     }
 }
 
