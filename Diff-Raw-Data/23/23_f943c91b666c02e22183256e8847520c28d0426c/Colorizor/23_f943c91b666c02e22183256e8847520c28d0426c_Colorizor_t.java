 package net.daboross.bukkitdev.uberchat;
 
 import org.bukkit.ChatColor;
 
 /**
  *
  * @author Dabo Ross <Dabo.Ross at daboross.net>
  */
 public class Colorizor {
 
     private static int num = 0;
 
     public static String colorize(String orig) {
        char[] list = ChatColor.stripColor(orig).toCharArray();
         if (list.length == 0) {
             return "";
         }
         for (int i = 0; i < list.length; i++) {
         }
         String colorizedString = "";
         for (int i = 0; i < list.length; i++,num++) {
             if (list[i] != ' ') {
                 if (num > 8) {
                     num = 0;
                 }
                 colorizedString += getColor(num);
             }
             colorizedString += list[i];
         }
         return colorizedString;
     }
     private static String[] colorList;
 
     private static String getColor(int i) {
         if (colorList == null) {
             colorList = new String[9];
             colorList[0] = ChatColor.AQUA.toString();
             colorList[1] = ChatColor.BLUE.toString();
             colorList[2] = ChatColor.DARK_BLUE.toString();
             colorList[3] = ChatColor.DARK_PURPLE.toString();
             colorList[4] = ChatColor.DARK_RED.toString();
             colorList[5] = ChatColor.RED.toString();
             colorList[6] = ChatColor.GOLD.toString();
             colorList[7] = ChatColor.GREEN.toString();
             colorList[8] = ChatColor.DARK_GREEN.toString();
         }
         int v = i;
         while (v > 8) {
             v -= 8;
         }
         return colorList[v];
     }
 }
