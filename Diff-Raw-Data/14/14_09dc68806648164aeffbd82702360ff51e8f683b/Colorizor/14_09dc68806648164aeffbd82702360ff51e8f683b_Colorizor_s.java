 package net.daboross.bungeedev.uberchat;
 
 import net.md_5.bungee.api.ChatColor;
 
 
 /**
  *
 * @author Dabo Ross <Dabo.Ross at daboross.net>
  */
 public class Colorizor {
 
     private static int currentColorNum = 0;
 
     public static String getColorString(String input) {
         char[] list = ChatColor.stripColor(input).toCharArray();
         if (list.length == 0) {
             return "";
         }
         StringBuilder resultBuilder = new StringBuilder();
         for (int i = 0; i < list.length; i++) {
             if (list[i] != ' ') {
                 resultBuilder.append(getNextColor());
             }
             resultBuilder.append(list[i]);
         }
         return resultBuilder.toString();
     }
     private static final String[] colorList = {ChatColor.AQUA.toString(), ChatColor.BLUE.toString(), ChatColor.DARK_BLUE.toString(), ChatColor.DARK_PURPLE.toString(), ChatColor.DARK_RED.toString(), ChatColor.RED.toString(), ChatColor.GOLD.toString(), ChatColor.GREEN.toString(), ChatColor.DARK_GREEN.toString()};
 
     private static String getNextColor() {
         currentColorNum = (currentColorNum + 1) % 8;
         return colorList[currentColorNum];
     }
 }
