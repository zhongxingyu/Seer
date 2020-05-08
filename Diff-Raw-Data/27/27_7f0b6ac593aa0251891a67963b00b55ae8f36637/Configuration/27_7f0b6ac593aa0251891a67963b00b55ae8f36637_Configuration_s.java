 package me.limebyte.battlenight.core.util.config;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Configuration {
     private final String fileName;
     private final String directory;
 
     private File file;
     private YamlConfiguration fileConfig;
 
     public Configuration(String fileName) {
         this.fileName = fileName;
         this.directory = BattleNight.instance.getDataFolder().getAbsolutePath();
     }
 
    public Configuration(String fileName, String directory) {
         this.fileName = fileName;
         this.directory = BattleNight.instance.getDataFolder().getAbsolutePath() + File.separator + directory;
     }
 
     public void reload() {
         if (file == null) {
             file = new File(directory, fileName);
         }
         fileConfig = YamlConfiguration.loadConfiguration(file);
 
         // Look for non-existent defaults
         InputStream defConfigStream = BattleNight.instance.getResource(fileName);
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             fileConfig.setDefaults(defConfig);
             fileConfig.options().indent(4);
            fileConfig.options().copyDefaults(true);
         }
     }
 
     public FileConfiguration get() {
         if (fileConfig == null) {
             this.reload();
         }
         return fileConfig;
     }
 
     public void save() {
         if (fileConfig == null || file == null) {
             return;
         } else {
             try {
                 get().save(file);
             } catch (IOException ex) {
                 BattleNight.log.severe("Could not save config to " + file + ": " + ex.getMessage());
             }
         }
     }
 }
