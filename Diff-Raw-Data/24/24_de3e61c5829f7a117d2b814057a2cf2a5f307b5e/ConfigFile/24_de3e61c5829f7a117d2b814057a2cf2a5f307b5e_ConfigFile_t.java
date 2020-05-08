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
     * <p>
     * This method loads the config of this plugin</p>
      */
    public void loadConfig() {
        File cfg = new File("plugins" + File.separator + "MakeYourOwnCommands" + File.separator + "config.yml");
         main.getDataFolder().mkdirs();
         YamlConfiguration config = YamlConfiguration.loadConfiguration(cfg);
         if (cfg.exists()) {
             if (config.getInt("Version") != 1) {
                 cfg.renameTo(new File("plugins" + File.separator + "MakeYourOwnCommands" + File.separator + "config_old.yml"));
                 generate();
             }
             main.getConfig().options().copyDefaults(true);
         } else {
            generate();
             main.getConfig().options().copyDefaults(true);
         }
     }
 
     public FileConfiguration getConfig() {
         return main.getConfig();
     }

    private void generate() {
        main.saveResource("config.yml", false);
    }
 }
