 package com.BASeCamp.GPEnderHoppers;
 
 import java.util.logging.Logger;
 
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 import nl.rutgerkok.betterenderchest.BetterEnderChest;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class GPEnderHopper extends JavaPlugin {
 
     public static GriefPrevention gp = null;
     public static BetterEnderChest becPlugin = null;
     public static Logger log;
     public static GPEnderHopper self;
     public Configuration config;
     static HopperHandler hh = null;
     HopperCommand hc = null;
 
     @Override
     public void onDisable() {
         ClaimData.closeAll();
         becPlugin = null;
         gp = null;
         self = null;
         hh = null;
     }
 
     @Override
     public void onEnable() {
         self = this;
         log = getLogger();
         log.info("GPEnderHopper Loading...");
         try {
             gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
         } catch (Exception e) {
             log.severe("Found a GriefPrevention plugin but it is not of the right class!");
         }
         if (gp == null) {
             log.severe("GPEnderHopper: Could not find GriefPrevention. Disabling!");
             getPluginLoader().disablePlugin(this);
             return;
         }
         try {
             becPlugin = (BetterEnderChest) Bukkit.getPluginManager().getPlugin("BetterEnderChest");
         } catch (Exception e) {
            log.severe("Found a GriefPrevention plugin but it is not of the right class!");
         }
         saveDefaultConfig();
         config = getConfig();
         ClaimData.setFolder(this);
         // register for Hopper Events.
         hh = new HopperHandler(this);
         Bukkit.getPluginManager().registerEvents(hh, this);
 
         hc = new HopperCommand();
         getCommand("claimecpull").setExecutor(hc);
         getCommand("claimecpush").setExecutor(hc);
 
     }
 
 }
