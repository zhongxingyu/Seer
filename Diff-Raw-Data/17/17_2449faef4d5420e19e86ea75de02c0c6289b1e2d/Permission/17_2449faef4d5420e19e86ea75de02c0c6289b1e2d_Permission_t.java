 package uk.co.oliwali.MultiHome;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Permission {
 	
 	private MultiHome plugin;
 	private PermissionPlugin handler = PermissionPlugin.OP;
 	private Plugin permissionPlugin;
 	
 	public Permission(MultiHome instance) {
 		plugin = instance;
         Plugin permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
        if (permissions != null) {
         	permissionPlugin = permissions;
         	handler = PermissionPlugin.PERMISSIONS;
         	plugin.sendMessage("info", "Using Permissions for user permissions");
         }
         else {
         	plugin.sendMessage("info", "No permission handler detected, only ops can use home commands");
         }
 	}
 	
 	private boolean hasPermission(Player player, String node) {
 		switch (handler) {
 			case PERMISSIONS:
 				return ((Permissions) permissionPlugin).getHandler().has(player, node);
 			case OP:
 				return player.isOp();
 		}
 		return false;
 	}
 	
 	public boolean home(Player player) {
 		return hasPermission(player, "multihome.home");
 	}
 	
 	private enum PermissionPlugin {
 		PERMISSIONS,
 		OP
 	}
 
 }
