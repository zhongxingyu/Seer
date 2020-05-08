 package uk.co.drnaylor.mcmmopartyadmin.permissions;
 
 import org.bukkit.entity.Player;
 
 public abstract class PermissionHandler {
     
     /**
      * Checks if a player has permission to spy on Party Chat.
      * 
      * @param player Player to check
      * @return true if so, false otherwise
      */
     public static boolean canSpy(Player player) {
         return (player.hasPermission("mcmmopartyadmin.spy") || player.isOp());
     }
     
     /**
     * Checks if a player has Admin powers over this plugin
      * @param player Player to check
      * @return true if so, false otherwise
      */
     public static boolean isAdmin(Player player) {
         return (player.hasPermission("mcmmopartyadmin.admin") || player.isOp());
     }
     
 }
