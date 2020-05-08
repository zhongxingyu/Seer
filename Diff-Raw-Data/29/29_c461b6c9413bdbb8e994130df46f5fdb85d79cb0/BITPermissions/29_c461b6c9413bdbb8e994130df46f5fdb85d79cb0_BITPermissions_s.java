 package dk.gabriel333.Library;
 
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 //import bPermissions
 
 //import PermissionsEx classes
 
 public class BITPermissions {
 
 	public static String PERMISSION_NODE;
 	public final static Boolean QUIET = false;
 	public final static Boolean NOT_QUIET = true;
         
         public static Permission permission = null;
 
 
 	// Initialize all permissionsplugins
         protected static void setupPermissions(Plugin plugin)
         {
             if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                 RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
                 if (permissionProvider != null) {
                     permission = permissionProvider.getProvider();
                 }
            }
         }
 
 	// Test if the player has permissions to do the action
 	public static boolean hasPerm(CommandSender sender, String label,
 			Boolean not_quiet) {
 		if (BITConfig.DEBUG_PERMISSIONS) {
 			sender.sendMessage("Testing permission: "
 					+ (PERMISSION_NODE + label).toLowerCase());
 		}
 
 		SpoutPlayer sPlayer = (SpoutPlayer) sender;
 		Boolean hasPermission;
 
                 if(permission != null) {
                    hasPermission = permission.has(sender, PERMISSION_NODE + label);
                    
 		} else {
 			// fallback builtin Permission system
 			hasPermission = sPlayer.hasPermission((PERMISSION_NODE + label)
 					.toLowerCase());
 		}
 
 		// return permission
 		if (BITConfig.DEBUG_PERMISSIONS)
 			sPlayer.sendMessage(ChatColor.RED + "The result is:"
 					+ hasPermission);
 		if (hasPermission) {
 			if (BITConfig.DEBUG_PERMISSIONS)
 				sPlayer.sendMessage(ChatColor.GREEN
 						+ "G333Permissions: You have permission to: "
 						+ (PERMISSION_NODE + label).toLowerCase());
 			return true;
 		} else if (not_quiet) {
 			if (BITConfig.DEBUG_PERMISSIONS) {
 				sPlayer.sendMessage(ChatColor.RED
 						+ "You to dont have permission to do this." + " ("
 						+ (BITPlugin.PLUGIN_NAME + "." + label).toLowerCase()
 						+ ")");
 			} else {
 				sPlayer.sendMessage(ChatColor.RED
 						+ "You to dont have permission to do this.");
 			}
 		}
 
 		return false;
 	}
 }
