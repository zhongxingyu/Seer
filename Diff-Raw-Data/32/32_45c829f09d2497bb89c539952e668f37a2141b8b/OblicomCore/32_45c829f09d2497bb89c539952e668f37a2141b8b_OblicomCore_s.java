 package me.wizzledonker.plugins.oblicomcore;
 
 import me.wizzledonker.plugins.oblicomcore.world.OblicomWorld;
 import me.wizzledonker.plugins.oblicomcore.command.ArrestCommand;
 import me.wizzledonker.plugins.oblicomcore.command.PayToReleaseCommand;
 import me.wizzledonker.plugins.oblicomcore.command.ReleaseCommand;
 import me.wizzledonker.plugins.oblicomcore.command.WantedAddCommand;
 import me.wizzledonker.plugins.oblicomcore.command.WantedCommand;
 import me.wizzledonker.plugins.oblicomcore.command.WantedRemoveCommand;
 import me.wizzledonker.plugins.oblicomcore.command.SetjailCommand;
 
 import me.wizzledonker.plugins.oblicomcore.listeners.CitizenListener;
 import me.wizzledonker.plugins.oblicomcore.listeners.CriminalListener;
 import me.wizzledonker.plugins.oblicomcore.listeners.EntityListener;
 import me.wizzledonker.plugins.oblicomcore.listeners.PlayerListener;
 import me.wizzledonker.plugins.oblicomcore.listeners.PoliceListener;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import me.wizzledonker.plugins.oblicomcore.exceptions.OblicomConfigException;
 import me.wizzledonker.plugins.oblicomcore.exceptions.OblicomWorldException;
 
 /*
  * This is the main plugin for Oblicom, the minecraft server
  * it allows easy integration of the factions, and a gaol feature.
  * 
  * (C) WizzleDonker, Sn0wmatt and nagib.kanaan 2012
  */
 public class OblicomCore extends JavaPlugin {
 
     public PluginManager pluginManager;
     public static YamlConfiguration configuration;
     public static OblicomWorld world;
 
     @Override
     public void onDisable() {
         log("ObicomCore disabled.");
     }
 
     @Override
     public void onEnable() {
         this.pluginManager = getServer().getPluginManager();
 
         try {
             configuration = OblicomConfig.getConfiguration(this);
         } catch (OblicomConfigException error) {
             log("Error to load Oblicom config: " + error.getMessage());
             pluginManager.disablePlugin(this);
             return;
         }
 
         try {
             world = OblicomWorld.getWorld(this);
         } catch (OblicomWorldException error) {
             log("Error to load Oblicom world: " + error.getMessage());
             pluginManager.disablePlugin(this);
             return;
         }
 
         // Register Events
         _registerEvents();
 
         // Register Commands
         _registerCommands();
 
         log("OblicomCore has loaded.");
     }
 
     public static void log(String text) {
        System.out.println("[OlibomCoret] " + text);
     }
 
     private void _registerCommands() {
         getCommand("oblisetjail").setExecutor(new SetjailCommand(this));
         getCommand("oblijail").setExecutor(new ArrestCommand(this));
         getCommand("obliunjail").setExecutor(new ReleaseCommand(this));
         getCommand("bail").setExecutor(new PayToReleaseCommand(this));
         getCommand("wanted").setExecutor(new WantedCommand(this));
         getCommand("unwanted").setExecutor(new WantedRemoveCommand(this));
         getCommand("addwanted").setExecutor(new WantedAddCommand(this));
     }
 
     private void _registerEvents() {
         pluginManager.registerEvents(new PlayerListener(this), this);
         pluginManager.registerEvents(new EntityListener(this), this);
         pluginManager.registerEvents(new CitizenListener(this), this);
         pluginManager.registerEvents(new PoliceListener(this), this);
         pluginManager.registerEvents(new CriminalListener(this), this);
     }
 }
