 //Copyright (C) 2011-2013 Chris Price (xCP23x)
 //This software uses the GNU GPL v2 license
 //See http://github.com/xCP23x/RestockIt/blob/master/README and http://github.com/xCP23x/RestockIt/blob/master/LICENSE for details
 
 package org.cp23.restockit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 class PlayerUtils extends RestockIt {
     //Colours for use in player messages:
     private static final ChatColor bCol = ChatColor.DARK_AQUA; //Bracket colour
     private static final ChatColor rCol = ChatColor.GRAY; // RestockIt colour
     private static final ChatColor eCol = ChatColor.DARK_RED; //Error colour
     private static final ChatColor dCol = ChatColor.RED;//Description colour
     private static final ChatColor sCol = ChatColor.GOLD; //String colour
     
     public static void sendPlayerMessage(Player player, int number) {
         RestockIt.debug(player.getName() + " sent error message " + number);
         //Give the error code here, so we only have to do it once
         player.sendMessage(bCol + "[" + rCol + "RestockIt" + bCol + "]" + eCol + " Error " + number + ":");
         switch(number){
             case 1:
                 player.sendMessage(dCol + "This is already a RestockIt container");
                 break;
             case 6:
                 player.sendMessage(dCol + "No chest was found below or above the sign");
                 break;
             default:
                 player.sendMessage(dCol + "Unspecified Error");
                 break;
         }
     }
     
     public static void sendPlayerMessage(Player player, int number, String string) {
         RestockIt.debug(player.getName() + " sent error message " + number + " with data: " + string);
         //Give the error code here, so we only have to do it once
         player.sendMessage(bCol + "[" + rCol + "RestockIt" + bCol + "]" + eCol + " Error " + number + ":");
         switch(number){
             case 2:
                 player.sendMessage(dCol + "You do not have permission to create a RestockIt " + sCol + string);
                 break;
             case 3:
                 player.sendMessage(sCol + string + dCol + " is not a valid item ID");
                 break;
             case 4:
                 player.sendMessage(sCol + string + dCol + " is not a valid item name");
                 break;
             case 5:
                 player.sendMessage(sCol + string + dCol + " is not a valid damage value");
                 break;
             case 7:
                 player.sendMessage(dCol + "You do not have permission to use " + sCol + string);
                 break;
             case 8:
                 player.sendMessage(dCol + "You do not have permission to open a RestockIt " + sCol + string);
                 break;
             case 9:
                 player.sendMessage(dCol + "You do not have permission to destroy a RestockIt " + sCol + string);
                 break;
             default:
                 player.sendMessage(dCol + "Unspecified Error");
                 break;
         }
     }
     
     public static boolean hasPermissions(RIperm riperm){
         String perm = riperm.getPerm();
         String depperm = riperm.getDeprecatedPerm();
         debug("Permission: " + perm);
         debug("Deprecated Permission: " + depperm);
         Player player = riperm.getPlayer();
         
         PermissionManager pm = Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx") ? PermissionsEx.getPermissionManager() : null;
         //If using SuperPerms:
         if(pm == null) {
             RestockIt.debug(("Using SuperPerms"));
             if(RestockIt.plugin.getConfig().getBoolean("opsOverrideBlacklist") && player.isOp() && "restockit.blacklist.bypass".equals(perm)) return true;
             if(player.hasPermission(perm))return true;
            if(depperm!=null && player.hasPermission(depperm)) {
                 warnDepPermissions(riperm);
                 return true;
             }
             
         //If using PermissionsEx:
         } else{
             RestockIt.debug("Using PermissionsEx");
             if(pm.has(player, perm, player.getWorld().getName())) return true;
            if(depperm!=null && pm.has(player, depperm, player.getWorld().getName())) {
                 warnDepPermissions(riperm);
                 return true;
             }
         }
         
         return false;
     }
     
     private static void warnDepPermissions(RIperm riperm){
         RestockIt.log.warning("[RestockIt] Using deprecated permission: " + riperm.getDeprecatedPerm());
         RestockIt.log.info("[RestockIt] Use " + riperm.getPerm() + " instead");
         RestockIt.log.info("[RestockIt] See http://dev.bukkit.org/server-mods/restockit/ for more info");
     }
 }
