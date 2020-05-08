 package se.myller.GuestUnlock.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 import se.myller.GuestUnlock.Main;
 
 public class PermPermissionsEx {
 
 	private Main plugin;
 	public PermPermissionsEx(Main instance) {
 		plugin = instance;
 	}
 	
 	/*
 	 * 
 	 * PermissionsEx
 	 * 
 	 */
 	public void getPex() {
 		Plugin pex = plugin.pm.getPlugin("PermissionsEx");
 		if (pex != null) {
 			if (pex.getClass().getName().equals("ru.tehkode.permissions")) {
 				plugin.log.info("[GuestUnlock] =====   Found PermissionEx!");
 			} else {
 				plugin.log.severe("[GuestUnlock] -----------------------------------------");
 				plugin.log.severe("[GuestUnlock] =====   You wanted PermissionsEx support!");
 				plugin.log.severe("[GuestUnlock] =====   I cant find it!");
 				plugin.log.severe("[GuestUnlock] =====   Could not find PermissionsEx.");
 				plugin.log.severe("[GuestUnlock] -----------------------------------------");
 				return;
 				}
 		} else {
 			plugin.log.severe("[GuestUnlock] -----------------------------------------");
 			plugin.log.severe("[GuestUnlock] =====   You wanted PermissionsEx support!");
 			plugin.log.severe("[GuestUnlock] =====   I cant find it!");
 			plugin.log.severe("[GuestUnlock] =====   Could not find PermissionsEx.");
 			plugin.log.severe("[GuestUnlock] -----------------------------------------");
 			return;
 		}
 	}
 	public void setGroupPEX(Player player) {
 		PermissionUser user  = PermissionsEx.getUser(player);
 		String name = user.getName();
 		PermissionGroup[] userGroups = user.getGroups();
 		for (PermissionGroup pg: userGroups) {
 			if (pg.toString().equals((plugin.config.getString("Permissions.PermissionsEx.Group.Default")))) {
 				user.setGroups(new String[] { plugin.config.getString("Permissions.PermissionsEx.Group") });
 				plugin.log.info("[GuestUnlock] Set " + name + ":s group to " + plugin.config.getString("Permissions.PermissionsEx.Group"));
 				player.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.GREEN + "Your group is now " + plugin.config.getString("Permissions.PermissionsEx.Group"));
 				return;
 			} 
 		}
 			player.sendMessage(ChatColor.AQUA + "[GuestUnlock]" + ChatColor.RED + "Haha, tricky one, you doesnt belong to the default group!");
 	}
 	
 }
