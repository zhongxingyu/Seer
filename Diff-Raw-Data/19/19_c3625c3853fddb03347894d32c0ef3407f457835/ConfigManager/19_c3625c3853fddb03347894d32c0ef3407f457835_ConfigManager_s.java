 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package redcastlemedia.multitallented.bukkit.partialcreative;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author danielmiller
  */
 public class ConfigManager {
   public static HashSet<String> getPermList(PartialCreative plugin) {
     HashSet<String> tempSet = new HashSet<String>();
     
     try {
       File folder = plugin.getDataFolder();
       if (!folder.exists()) {
        folder.createNewFile();
       }
       File configFile = new File(plugin.getDataFolder(), "config.yml");
       if (!configFile.exists()) {
         configFile.createNewFile();
       }
       
       FileConfiguration config = new YamlConfiguration();
       config.load(configFile);
       
       if (config.contains("perms")) {
         for (String s : config.getStringList("perms")) {
           tempSet.add(s);
         }
       } else {
         ArrayList<String> tempList = new ArrayList<String>();
         tempList.add("essentials.kit.starter");
         config.set("perms", tempList);
         config.save(configFile);
       }
       
       
     } catch (Exception e) {
       System.out.println("[PartialCreative] failed to load config.yml");
     }
     
     return tempSet;
   }
 }
