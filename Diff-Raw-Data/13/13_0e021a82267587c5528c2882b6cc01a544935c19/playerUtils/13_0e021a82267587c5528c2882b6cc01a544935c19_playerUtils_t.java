 //@author Chris Price (xCP23x)
 
 package org.xcp23x.restockit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class playerUtils {
     //Colours for use in player messages:
     private static ChatColor bCol = ChatColor.DARK_AQUA; //Bracket colour
     private static ChatColor rCol = ChatColor.GRAY; // RestockIt colour
     private static ChatColor eCol = ChatColor.DARK_RED; //Error colour
     private static ChatColor dCol = ChatColor.RED;//Description colour
     private static ChatColor sCol = ChatColor.GOLD; //String colour
     
     public static void sendPlayerMessage(Player player, int number) {
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
             default:
                 player.sendMessage(dCol + "Unspecified Error");
                 break;
         }
     }
     
     public static boolean hasBlacklistPermissions(Player player){
         if(hasPermissions(player, "restockit.blacklist.bypass")) return true; else return false;
     }
     
     public static boolean hasContainerPermissions(Player player, Block container, String line) {
         //Check if it's an incinerator
         String containerName = signUtils.isIncinerator(line) ? "incinerator" : container.getType().name().toLowerCase();
         String perm = "restockit." + containerName;
         if(hasPermissions(player, perm)) return true; else return false;
     }
     
     private static boolean hasPermissions(Player player, String perm){
         //If PermissionsEx is enabled, set it as the PermissionManager
         PermissionManager pm = Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx") ? PermissionsEx.getPermissionManager() : null;
         if(pm == null) { //If using SuperPerms:
             if(RestockIt.plugin.getConfig().getBoolean("opsOverrideBlacklist") && player.isOp() && "restockit.blacklist.bypass".equals(perm)) return true;
             if(player.hasPermission(perm)) return true;
             
         //If using PermissionsEx:
         } else if(pm.has(player, perm, player.getWorld().getName())) return true;
         return false;
     }
 }
