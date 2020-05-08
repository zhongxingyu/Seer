 package com.lebelw.Tickets;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /**
  * @description Handles all plugin permissions
  * @author Tagette
  */
 public class TPermissions {
 
     private enum PermissionHandler {
 
         PERMISSIONS, GROUP_MANAGER, NONE
     }
     private static PermissionHandler handler;
     public static Plugin PermissionPlugin;
     private static Tickets plugin;
 
     public static void initialize(Tickets instance) {
         TPermissions.plugin = instance;
         Plugin Permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
        Plugin GroupManager = plugin.getServer().getPluginManager().getPlugin("GroupManager");
         handler = PermissionHandler.NONE;
 
         if (Permissions != null) {
             PermissionPlugin = Permissions;
             handler = PermissionHandler.PERMISSIONS;
             String version = PermissionPlugin.getDescription().getVersion();
                 TLogger.info("Permissions version " + version + " loaded.");
        } else if (GroupManager != null) {
            PermissionPlugin = GroupManager;
            handler = PermissionHandler.GROUP_MANAGER;
            String version = PermissionPlugin.getDescription().getVersion();
                TLogger.info("GroupManager version " + version + " loaded.");
         } else{
         	TLogger.info("No permissions system found!");
         }
     }
 
     public static void onEnable(Plugin plugin) {
         if (PermissionPlugin == null) {
             String pluginName = plugin.getDescription().getName();
             handler = PermissionHandler.NONE;
 
             if (pluginName.equals("Permissions")) {
                 PermissionPlugin = plugin;
                 handler = PermissionHandler.PERMISSIONS;
                 String version = plugin.getDescription().getVersion();
                     TLogger.info("Permissions version " + version + " loaded.");
             } else if (pluginName.equals("GroupManager")) {
                 PermissionPlugin = plugin;
                 handler = PermissionHandler.GROUP_MANAGER;
                 String version = plugin.getDescription().getVersion();
                     TLogger.info("GroupManager version " + version + " loaded.");
             }
         }
     }
 
     public static boolean permission(Player player, String permission, boolean defaultPerm) {
         switch (handler) {
             case PERMISSIONS:
                 return ((Permissions) PermissionPlugin).getHandler().has(player, permission);
            case GROUP_MANAGER:
                return ((GroupManager) PermissionPlugin).getWorldsHolder().getWorldPermissions(player).has(player, permission);
             case NONE:
                 return defaultPerm;
             default:
                 return defaultPerm;
         }
     }
 
     public static boolean isAdmin(Player player) {
         return permission(player, "template.admin", player.isOp());
     }
 }
