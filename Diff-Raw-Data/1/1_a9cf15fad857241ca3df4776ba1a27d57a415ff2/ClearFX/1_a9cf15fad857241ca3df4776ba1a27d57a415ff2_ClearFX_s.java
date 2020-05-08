 package com.kierdavis.clearfx;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 
 public class ClearFX extends JavaPlugin {
     public void onEnable() {
         getCommand("clearfx").setExecutor(new ClearFXCommandExecutor(this));
         
         // Start Metrics
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         }
         catch (IOException e) {
             getLogger().severe("Failed to submit stats to Metrics: " + e.toString());
         }
     }
 }
