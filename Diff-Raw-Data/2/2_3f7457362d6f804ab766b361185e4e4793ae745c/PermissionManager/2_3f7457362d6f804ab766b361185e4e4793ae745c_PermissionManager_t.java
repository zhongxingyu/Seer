 package me.ryall.scavenger.settings;
 
 import me.ryall.scavenger.Scavenger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.nijiko.permissions.PermissionHandler;
 
 public class PermissionManager
 {
    public static String PERMISSIONS_PREFIX = "scavenger.";
 
     public PermissionHandler permissions;
 
     public void load()
     {
         if (permissions == null)
         {
             Plugin plugin = Scavenger.get().getServer().getPluginManager().getPlugin("Permissions");
 
             if (plugin != null)
             {
                 Scavenger.get().logInfo("Attached to Permissions");
                 permissions = ((Permissions) plugin).getHandler();
             }
         }
     }
 
     protected boolean hasGlobalPermission(Player _player)
     {
         return (permissions == null && _player.isOp()) || hasPermission(_player, PERMISSIONS_PREFIX + "*") || hasPermission(_player, "*");
     }
 
     public boolean hasScavengePermission(Player _player)
     {
         return hasGlobalPermission(_player) || hasPermission(_player, PERMISSIONS_PREFIX + "scavenge");
     }
 
     private boolean hasPermission(Player _player, String _permission)
     {
         if (permissions != null)
             return permissions.has(_player, _permission);
 
         return false;
     }
 }
