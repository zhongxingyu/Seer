 package org.gestern;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 import static org.gestern.Configuration.CONF;
 
 public class MyPlugin extends JavaPlugin {
 
    /** The plugin instance. */
     public static MyPlugin P;
 
     private Logger log;
 
     public Economy eco;
 
     @Override
     public void onEnable() {
 
         P = this;
         log = getLogger();
 
         // load and init configuration
         saveDefaultConfig(); // saves default configuration if no config.yml exists yet
         FileConfiguration savedConfig = getConfig();
         CONF.readConfig(savedConfig);
 
         CommandExecutor myCommands = new Commands();
 
         getCommand("myplugin").setExecutor(myCommands);
 
         getServer().getPluginManager().registerEvents(new MyListener(), this);
 
         setupEconomy();
         setupMetrics();
 
     }
 
     @Override
     public void onDisable() {
 
     }
 
     private boolean setupMetrics() {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
             return true;
         } catch (IOException e) {
             log.info("Failed to submit PluginMetrics stats");
             return false;
         }
     }
 
     private boolean setupEconomy()
     {
         try {
             @SuppressWarnings("unchecked")
             Class<Economy> c = (Class<Economy>) Class.forName("net.milkbowl.vault.economy.Economy");
             eco = getServer().getServicesManager().load(c);
 
         } catch (ClassNotFoundException e) {
             // loading vault failed
         }
 
         if (eco != null)
             log.info("Vault hooked.");
         else {
             log.info("Unable to load Vault.");
         }
 
         return (eco != null);
     }
 
 
 }
