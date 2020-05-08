 package net.lankylord.simplehomes;
 
 import net.lankylord.simplehomes.commands.*;
 import net.lankylord.simplehomes.managers.ConfigManager;
 import net.lankylord.simplehomes.managers.HomeFileManager;
 import net.lankylord.simplehomes.managers.HomeManager;
 import net.lankylord.simplehomes.util.Updater;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 public class SimpleHomes extends JavaPlugin {
 
     private HomeFileManager homeFileManager;
     private HomeManager homeManager;
     private ConfigManager configManager;
 
     @Override
     public void onEnable() {
         this.getConfig().options().copyDefaults(true);
        loadCommands();
         saveDefaultConfig();
         saveConfig();
         this.homeFileManager = new HomeFileManager(this);
         this.homeManager = new HomeManager(homeFileManager);
         this.configManager = new ConfigManager(this);
         homeFileManager.saveHomes();
         loadMetrics();
         if (getConfig().getBoolean("AutoUpdater.Enabled", true)) {
             loadUpdater();
         }
         getLogger().info("[SimpleHomes] SimpleHomes Enabled!");
     }
 
     @Override
     public void onDisable() {
         homeFileManager.saveHomes();
         getLogger().log(Level.INFO, "SimpleHomes Disabled!");
     }
 
     private void loadMetrics() {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             getLogger().log(Level.WARNING, "Failed to submit stats.");
         }
     }
 
     private void loadUpdater() {
         Updater updater = new Updater(this, "simplehomes", this.getFile(), Updater.UpdateType.DEFAULT, true);
         getLogger().log(Level.INFO, "[SimpleHomes] AutoUpdater Enabled.");
     }
 
     private void loadCommands() {
         this.getCommand("delhome").setExecutor(new DeleteHomeCommand(this));
         this.getCommand("home").setExecutor(new HomeCommand(this));
         this.getCommand("homelist").setExecutor(new HomeListCommand(this));
         this.getCommand("otherhome").setExecutor(new OtherHomeCommand(this));
         this.getCommand("sethome").setExecutor(new SetHomeCommand(this));
     }
 
     public HomeFileManager getHomeFileManager() {
         return homeFileManager;
     }
 
     public HomeManager getHomeManager() {
         return homeManager;
     }
 }
