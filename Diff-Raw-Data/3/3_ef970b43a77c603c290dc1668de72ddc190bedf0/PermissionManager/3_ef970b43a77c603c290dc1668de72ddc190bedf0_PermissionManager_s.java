 package me.ryall.wrath.settings;
 
 import me.ryall.wrath.Wrath;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.nijiko.permissions.PermissionHandler;
 
 public class PermissionManager
 {
     public static String PERMISSIONS_PREFIX = "wrath.";
     
     public PermissionHandler permissions;
     
     public void load()
     {
         if (permissions == null) 
         {
             Plugin plugin = Wrath.get().getServer().getPluginManager().getPlugin("Permissions");
             
             if (plugin != null)
             {
                 Wrath.get().logInfo("Attached to Permissions");
                 permissions = ((Permissions)plugin).getHandler();
             }
         }
     }
     
     protected boolean hasGlobalPermission(Player _player)
     {
        return (permissions == null && _player.isOp()) || 
             hasPermission(_player, PERMISSIONS_PREFIX + "*") || 
             hasPermission(_player, "*");
     }
     
     public boolean hasDrownPermission(Player _player)
     {
         return hasGlobalPermission(_player) || 
             hasPermission(_player, PERMISSIONS_PREFIX + "drown");
     }
     
     public boolean hasExplodePermission(Player _player)
     {
         return hasGlobalPermission(_player) || 
             hasPermission(_player, PERMISSIONS_PREFIX + "explode");
     }
     
     public boolean hasIgnitePermission(Player _player)
     {
         return hasGlobalPermission(_player) || 
             hasPermission(_player, PERMISSIONS_PREFIX + "ignite");
     }
     
     public boolean hasImpalePermission(Player _player)
     {
         return hasGlobalPermission(_player) || 
             hasPermission(_player, PERMISSIONS_PREFIX + "impale");
     }
     
     public boolean hasRavagePermission(Player _player)
     {
         return hasGlobalPermission(_player) || 
             hasPermission(_player, PERMISSIONS_PREFIX + "ravage");
     }
     
     public boolean hasStrikePermission(Player _player)
     {
         return hasGlobalPermission(_player) || 
             hasPermission(_player, PERMISSIONS_PREFIX + "strike");
     }
     
     private boolean hasPermission(Player _player, String _permission)
     {
         if (permissions != null)
             return permissions.has(_player, _permission);
         
         return false;
     }
 }
