 package me.wizzledonker.plugins.oblicomcore;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 import me.wizzledonker.plugins.oblicomcore.exceptions.OblicomConfigException;
 
 /**
  *
  * @author nagib.kanaan
  */
 public class OblicomConfig {
     
     private static OblicomConfig instance = null;
     
     /**
      * The plugin configuration file
      */
     private final YamlConfiguration configuration;
 
     /**
      * The Plugin configuration file
      */
     private final File configurationFile;    
     
     /**
      * The plugin this metrics submits for
      */
     private final Plugin plugin;
     
     private OblicomConfig(Plugin plugin) throws OblicomConfigException {
         
         this.plugin = plugin;
         
         // load the config
         configurationFile = getConfigFile();
         configuration = YamlConfiguration.loadConfiguration(configurationFile);
         
         if (!configurationFile.exists()) {            
             _setDefaults();
             
             configuration.options().copyHeader(true);
             configuration.options().copyDefaults(true);
             
             try {
                 configuration.save(configurationFile);
             } catch(IOException error) {
                 throw new OblicomConfigException("Error to save the config file", error);
             }
         }
     }
     
     public static YamlConfiguration getConfiguration() {
        return OblicomConfig.instance.configuration;
     }
 
     public static YamlConfiguration getConfiguration(Plugin plugin) throws OblicomConfigException {
        if (OblicomConfig.instance == null) {
            OblicomConfig.instance = new OblicomConfig(plugin);
         }
         
        return OblicomConfig.instance.configuration;
     }
     
     /**
      * Gets the File object of the config file that should be used to store data.
      *
      * @return the File object for the config file
      */    
     private File getConfigFile() {
         File pluginsFolder = plugin.getDataFolder().getParentFile();
 
         return new File(new File(pluginsFolder, "OblicomCore"), "config.yml");
     }
     
     /**
      * Set default values.
      */    
     private void _setDefaults() {
 
         configuration.options().header("OblicomCore Config.");
         
         // Police Config : Jail
         configuration.addDefault("police.jail.time", 5);
         configuration.addDefault("police.jail.bail_amount", 250.0);
         configuration.addDefault("police.jail.message", "You have been Jailed!");
         configuration.addDefault("police.jail.releasemessage", "You have been Released!");
         configuration.addDefault("police.jail.score", 50);
         
         List<String> commands = Arrays.asList("login", "register", "list");
         configuration.addDefault("police.jail.allowed-commands", commands);
         
         // Police Config : Wanted
         configuration.addDefault("police.wanted.time", 10);
         configuration.addDefault("police.tracker.time", 30);
 
         // Criminal Config : Pickpocket
         configuration.addDefault("criminal.pickpocket.chance", 25);
         configuration.addDefault("criminal.pickpocket.damage", 2);
         configuration.addDefault("criminal.pickpocket.time", 2);
         configuration.addDefault("criminal.pickpocket.max", 20);
         configuration.addDefault("criminal.pickpocket.experience", 2);
         configuration.addDefault("criminal.pickpocket.message", "%thief% attempted to steal from %victim%!");
         configuration.addDefault("criminal.pickpocket.score", 25);
 
         // Criminal Config : Lockpick
         configuration.addDefault("criminal.lockpick.item", 265);
         configuration.addDefault("criminal.lockpick.chance", 25);
         configuration.addDefault("criminal.lockpick.damage", 3);
         configuration.addDefault("criminal.lockpick.experience", 4);
         configuration.addDefault("criminal.lockpick.time", 5);
         configuration.addDefault("criminal.lockpick.score", 25);
 
         //Add the award defaults
         configuration.addDefault("award.monster", 15);
         configuration.addDefault("award.player", 50);
         configuration.addDefault("award.passive", 10);
     }   
 }
