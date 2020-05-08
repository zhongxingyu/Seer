 package cc.thedudeguy.xpinthejar.util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import cc.thedudeguy.xpinthejar.XPInTheJar;
 
 public class Debug {
 
     public static final String tag = "XP-in-the-Jar";
 
     public static void debug(Object... debugTexts) {
         debug(null, debugTexts);
     }
 
     public static void debug(Player player, String debugText) {
         if (XPInTheJar.instance.getConfig().getBoolean("debug")) {
             if(player != null) {
                 player.sendMessage(ChatColor.DARK_GRAY + "[" + tag + "] " + ChatColor.GRAY + debugText);
             }
             StringBuilder message = new StringBuilder(ChatColor.GOLD + "[" + tag + "] ");
             if(player != null) {
                 message.append(player.getName()).append(": ");
             }
             Bukkit.getServer().getConsoleSender().sendMessage(message.append(debugText).toString());
         }
     }
 
     public static void debug(Player player, Object... debugTexts) {
         StringBuilder allText = new StringBuilder();
         for (Object debugText : debugTexts) {
             allText.append(debugText.toString());
         }
        debug(player, allText.toString());
     }
 
 }
