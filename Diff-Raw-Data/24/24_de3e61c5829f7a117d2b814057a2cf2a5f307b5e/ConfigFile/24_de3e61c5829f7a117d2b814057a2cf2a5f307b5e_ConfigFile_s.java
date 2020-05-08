 package de.JeterLP.MakeYourOwnCommands.utils;
 
 import de.JeterLP.MakeYourOwnCommands.Main;
 import java.io.File;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author JeterLP
  */
 public class ConfigFile {
 
     private Main main;
 
     public ConfigFile(Main main) {
         this.main = main;
     }
 
     /**
     * <p>This method loads the config of this plugin</p>
      */
    public boolean loadConfig() {
        File cfg = new File("plugins/MakeYourOwnCommands/config.yml");
         main.getDataFolder().mkdirs();
         YamlConfiguration config = YamlConfiguration.loadConfiguration(cfg);
         if (cfg.exists()) {
             if (config.getInt("Version") != 1) {
                return false;
             }
             main.getConfig().options().copyDefaults(true);
            return true;
         } else {
            main.saveDefaultConfig();
             main.getConfig().options().copyDefaults(true);
            return true;
         }
     }
 
     public FileConfiguration getConfig() {
         return main.getConfig();
     }
 }
