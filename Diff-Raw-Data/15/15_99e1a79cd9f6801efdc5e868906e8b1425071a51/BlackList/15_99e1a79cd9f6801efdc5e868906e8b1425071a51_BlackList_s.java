 package me.DDoS.Quicksign.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import me.DDoS.Quicksign.QuickSign;
 import me.DDoS.Quicksign.permission.Permission;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author DDoS
  */
 public class BlackList {
 
     private final Set<String> blackList = new HashSet<String>();
     private final QuickSign plugin;
 
     public BlackList(QuickSign plugin) {
 
         this.plugin = plugin;
 
         YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/QuickSign/black_list.yml"));
 
         if (config.getKeys(true).isEmpty()) {
 
            config.set("BlackList", Arrays.asList("[buy]", "[sell]"));
 
             try {
 
                 config.save("plugins/QuickSign/black_list.yml");
 
             } catch (IOException ex) {
 
                 QuickSign.log.info("[QuickSign] Couldn't save black list: " + ex.getMessage());
                 return;
 
             }
         }
 
         List<String> bl = config.getStringList("BlackList");
 
         for (String ble : bl) {
 
             if (!blackList.contains(ble)) {
 
                 blackList.add(ble.toLowerCase());
 
             }
         }
 
         QuickSign.log.info("[QuickSign] Black list loaded");
 
     }
 
     public boolean allows(String line, Player player) {
 
         if (!plugin.hasPermissions(player,
                 Permission.IGNORE_BLACK_LIST)) {
 
             if (blackList.contains(line.toLowerCase())) {
                 
                 return false;
                 
             }
         }
 
         if (!plugin.hasPermissions(player,
                 Permission.ALLOW_ICS)) {
 
             if (checkForIC(line)) {
 
                 return false;
 
             }
         }
 
         return true;
 
     }
 
     public boolean allows(String[] lines, Player player) {
 
         if (!plugin.hasPermissions(player,
                 Permission.IGNORE_BLACK_LIST)) {
 
             for (String line : lines) {
 
                 if (blackList.contains(line.toLowerCase())) {
                     
                     return false;
                     
                 }
             }
         }
 
         if (!plugin.hasPermissions(player,
                 Permission.ALLOW_ICS)) {
 
             for (String line : lines) {
 
                 if (checkForIC(line)) {
 
                     return false;
 
                 }
             }
         }
 
         return true;
 
     }
     
     public boolean allows(Sign sign, Player player) {
         
         return allows(sign.getLines(), player);
         
     }
 
     private boolean checkForIC(String txt) {
         //[MCXXXX]
         if (txt.length() >= 8) {
 
             if (txt.substring(0, 3).equalsIgnoreCase("[MC")
                     && QSUtil.isParsableToInt(txt.substring(3, 7))
                     && txt.charAt(7) == ']') {
 
                 return true;
 
             }
         }
 
         return false;
 
     }
 }
