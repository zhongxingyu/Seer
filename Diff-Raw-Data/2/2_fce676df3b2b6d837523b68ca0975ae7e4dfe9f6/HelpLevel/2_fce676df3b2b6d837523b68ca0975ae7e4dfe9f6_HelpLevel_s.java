 package com.connor.helpdesk;
 
 import org.bukkit.entity.Player;
 
 public enum HelpLevel {
     MOD(1), ADMIN(2), OP(3);
 
     int numLevel;
     
     HelpLevel(int numLevel) {
         this.numLevel = numLevel;
     }
     
     public int toInt() {
         return numLevel;
     }
     
     public static HelpLevel getPlayerHelpLevel(Player player) {
         if (player.hasPermission("helpdesk.mod")) {
             return MOD;
         } else if (player.hasPermission("helpdesk.admin")) {
             return ADMIN;
        } else if (player.hasPermission("helpdesk.admin")) {
             return OP;
         }
         return null;
     }
     
     public static int getPlayerHelpLevelInt(Player player) {
         HelpLevel level = getPlayerHelpLevel(player);
         if (level != null) {
             return level.toInt();
         } else {
             return 0;
         }
     }
 }
