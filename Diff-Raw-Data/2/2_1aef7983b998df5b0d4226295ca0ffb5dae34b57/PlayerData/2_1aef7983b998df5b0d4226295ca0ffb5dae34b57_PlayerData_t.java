 package com.scizzr.bukkit.plugins.pksystem.config;
 
 import java.io.File;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.scizzr.bukkit.plugins.pksystem.Main;
 
 public class PlayerData {
     static YamlConfiguration config = new YamlConfiguration();
     
     static boolean changed = false;
     
     public static boolean load() {
         if(!Main.filePlayerData.exists()) {
             try {
                 Main.filePlayerData.createNewFile();
                 Main.log.info(Main.prefixConsole + "Blank playerData.yml created");
                 return true;
             } catch (Exception ex) {
                 Main.log.info(Main.prefixConsole + "Failed to make playerData.yml");
                 Main.suicide(ex);
                 return false;
             }
         } else {
             try {
                 config.load(Main.filePlayerData);
                 return true;
             } catch (Exception ex) {
                 Main.suicide(ex);
                 return false;
             }
         }
     }
     
     public static void setOpt(Player p, String o, String v) {
        config.set(p.getName() + ".options." + o, v);
         
         try {
             config.save(Main.filePlayerData);
         } catch (Exception ex) {
             Main.suicide(ex);
         }
     }
     
     public static String getOpt(Player p, String o) {
         try {
             config.load(Main.filePlayerData);
         } catch (Exception ex) {
             Main.suicide(ex);
         }
         
         String val = config.getString(p.getName() + "." + o);
         
         return val != null ? val : null;
     }
     
     public static void checkAll(Player p) {
         File file = Main.filePlayerData;
         try {
             config.load(Main.filePlayerData);
             
             editOption(config, p, "eff-pot-self", "options.eff-pot-self");
             editOption(config, p, "eff-pot-other", "options.eff-pot-other");
             
             checkOption(config, p, "options.eff-pot-self", "true");
             checkOption(config, p, "options.eff-pot-other", "true");
             
             config.save(Main.filePlayerData);
             
             if (changed) {
                 config.options().header("PKSystem Configuration - Main");
                 try {
                     config.save(file);
                 } catch (Exception ex) {
                     Main.log.info(Main.prefixConsole + "Failed to save configMain.yml");
                     Main.suicide(ex);
                 }
             }
         } catch (Exception ex) {
             Main.suicide(ex);
         }
     }
     
     static void checkOption(YamlConfiguration config, Player p, String opt, String def) {
         if (!config.isSet(p.getName() + "." + opt)) {
             config.set(p.getName() + "." + opt, def);
             changed = true;
             try {
                 config.save(Main.filePlayerData);
             } catch (Exception ex) {
                 Main.suicide(ex);
             }
         }
     }
     
     static void editOption(YamlConfiguration config, Player p, String nodeOld, String nodeNew) {
         if (config.isSet(p.getName() + "." + nodeOld)) {
             if (nodeNew != null) {
                 config.set(p.getName() + "." + nodeNew, config.get(p.getName() + "." + nodeOld));
                 config.set(p.getName() + "." + nodeOld, null);
             }
             config.set(nodeOld, null);
             try {
                 config.save(Main.filePlayerData);
             } catch (Exception ex) {
                 Main.suicide(ex);
             }
         }
     }
 }
