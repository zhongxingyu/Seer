 package uk.co.oliwali.MultiHome;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Permission {
 	
 	private MultiHome plugin;
 	private PermissionPlugin handler = PermissionPlugin.OP;
 	private Plugin permissionPlugin;
 	
 	public Permission(MultiHome instance) {
 		plugin = instance;
 		Plugin groupManager = plugin.getServer().getPluginManager().getPlugin("GroupManager");
         Plugin permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
         
         if (groupManager != null) {
         	permissionPlugin = groupManager;
         	handler = PermissionPlugin.GROUP_MANAGER;
         	plugin.sendMessage("info", "Using GroupManager for user permissions");
         }
         else if (permissions != null) {
        	permissionPlugin = groupManager;
         	handler = PermissionPlugin.PERMISSIONS;
         	plugin.sendMessage("info", "Using Permissions for user permissions");
         }
         else {
         	plugin.sendMessage("info", "No permission handler detected, only ops can use home commands");
         }
 	}
 	
 	private boolean hasPermission(Player player, String node) {
 		switch (handler) {
 			case GROUP_MANAGER:
 				return ((GroupManager) permissionPlugin).getWorldsHolder().getWorldPermissions(player).has(player, node);
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
 		GROUP_MANAGER,
 		OP
 	}
 
 }
