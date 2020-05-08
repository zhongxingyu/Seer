 package com.ngc0202.warningplugin;
 
 import java.io.File;
 import java.util.Map;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author ngc0202
  */
 public class WarningPlugin extends JavaPlugin {
 
     final WarningPlugin plugin = this;
     PropertiesFile warns;
     PropertiesFile bans;
 
     @Override
     public void onEnable() {
         getDataFolder().mkdirs();
         File warnFile = new File(getDataFolder(), "warns.properties");
         File banFile = new File(getDataFolder(), "bans.properties");
         warns = new PropertiesFile(warnFile.getAbsolutePath());
         warns.load();
         bans = new PropertiesFile(banFile.getAbsolutePath());
         bans.load();
         this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
             public void run() {
                 plugin.banCheckAll();
             }
         }, 1L, 6000L);
         System.out.println(getDescription().getFullName() + " von ngc0202 ist aktiviert.");
     }
 
     @Override
     public void onDisable() {
         System.out.println(getDescription().getName() + " ist deaktiviert.");
         warns.save();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         warns.load();
        if (commandLabel.equalsIgnoreCase("warnen")) {
            if (args.length == 0) {
                return false;
            }
             Player ply;
             if (args[0].equalsIgnoreCase("mindern")) {
                 if (!((args.length == 2) || (args.length == 3))) {
                     return false;
                 }
                 ply = sender.getServer().getPlayer(args[1]);
                 if (ply == null) {
                     sender.sendMessage(ChatColor.RED + "Konnte nicht finden " + args[1] + ".");
                     return false;
                 }
                 int remCnt = Integer.parseInt(args[2]);
                 if (!warns.keyExists(ply.getName())) {
                    sender.sendMessage(ChatColor.BLUE + ply.getName() + " hat schon keine Warnungen.");
                     return true;
                 }
                 int newCnt = warns.getInt(ply.getName()) - remCnt;
                 if (newCnt < 0) {
                     newCnt = 0;
                 }
                 warns.setInt(ply.getName(), newCnt);
                 warns.save();
                sender.sendMessage(ChatColor.BLUE + ply.getName() + " hat jetzt " + newCnt + " Warnungen.");
                 return true;
             } else {
                 if (args.length == 0) {
                     return false;
                 }
                 ply = sender.getServer().getPlayer(args[0]);
                 if (ply == null) {
                     sender.sendMessage(ChatColor.RED + "Konnte nicht finden " + args[0] + ".");
                     return false;
                 }
                 if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Bitte gib einen Grund an.");
                     return false;
                 }
                 int newCnt = warns.getInt(ply.getName()) + 1;
                 warns.setInt(ply.getName(), newCnt);
                 warns.save();
                 String reason = this.buildReason(args);
                ply.sendMessage(ChatColor.RED + "Sie wurden verwarnt, für: \"" + reason + "\"");
                 if (newCnt == 4) {
                     bans.setLong(ply.getName(), System.currentTimeMillis() + 3600000L);
                     ply.setBanned(true);
                    ply.kickPlayer("Verbannte für zu viel Warnungen. (1 Stunden)");
                 } else if (newCnt >= 10) {
                     bans.removeKey(ply.getName());
                     ply.setBanned(true);
                    ply.kickPlayer("Verbannte für zu viel Warnungen.");
                 }
                 return true;
             }
         }
         return false;
     }
 
     private void banCheckAll() {
         try {
             bans.load();
             Map<String, String> map = bans.returnMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 String key = entry.getKey();
                 String val = entry.getValue();
                 if (Long.parseLong(val) <= System.currentTimeMillis()) {
                     OfflinePlayer ply = getServer().getOfflinePlayer(key);
                     ply.setBanned(false);
                     bans.removeKey(key);
                 }
             }
         } catch (Exception ex) {
             System.out.println(ChatColor.RED + "Unfähig, die WarningPlugin-Bannliste zu laden.");
         }
     }
 
     private String buildReason(String[] args) {
         String reason = "";
         int len = args.length;
         for (int i = 1; i < len; i++) {
            reason += args[i];
            if (i < (len - 1)) {
                reason += " ";
             }
         }
         return reason;
     }
 }
