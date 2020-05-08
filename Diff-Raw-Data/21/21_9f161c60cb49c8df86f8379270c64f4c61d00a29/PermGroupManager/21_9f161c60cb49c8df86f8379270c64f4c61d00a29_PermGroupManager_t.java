 package se.myller.GuestUnlock.Permission;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import se.myller.GuestUnlock.Main;
 
 public class PermGroupManager {
 
 	private Main plugin;
 	public PermGroupManager(Main instance) {
 		plugin = instance;
 	}
 	/*
 	 * 
 	 * 
 	 * GroupManager
 	 * 
 	 */
 	public void getGM() {
 		Plugin GM = plugin.pm.getPlugin("GroupManager");
 		
 		if (GM != null) {
 			if (GM.getClass().getName().equals("org.anjocaido.groupmanager.GroupManager")) {
 				plugin.log.info("[GuestUnlock] =====   Found GroupManager!");
 				plugin.groupManager = (GroupManager)GM;
 			} else {
 				plugin.log.severe("[GuestUnlock] -----------------------------------------");
 				plugin.log.severe("[GuestUnlock] =====   You wanted GroupManager support!");
 				plugin.log.severe("[GuestUnlock] =====   I cant find it!");
 				plugin.log.severe("[GuestUnlock] =====   Could not find GroupManager.");
 				plugin.log.severe("[GuestUnlock] -----------------------------------------");
 				return;
 				}
 		} else {
 			plugin.log.severe("[GuestUnlock] -----------------------------------------");
 			plugin.log.severe("[GuestUnlock] =====   You wanted GroupManager support!");
 			plugin.log.severe("[GuestUnlock] =====   I cant find it!");
 			plugin.log.severe("[GuestUnlock] =====   Could not find GroupManager.");
 			plugin.log.severe("[GuestUnlock] -----------------------------------------");
 			return;
 		}
 	}
 	public boolean setGroupGM(final Player base) {
 		final OverloadedWorldHolder handler = plugin.groupManager.getWorldsHolder().getWorldData(base);
 		if (handler == null) {
 			return false;
 		}
 		if (handler.getUser(base.getName()).getGroupName().equals("Permissions.GroupManager.Group.Default")) {
 			handler.getUser(base.getName()).setGroup(handler.getGroup(plugin.config.getString("Permissions.GroupManager.Group.Build")));
 			plugin.log.info("[GuestUnlock] Set " + base.getName() + ":s group to " + plugin.config.getString("Permissions.GroupManager.Group.Build"));
 			base.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.GREEN + "Your group is now " + plugin.config.getString("Permissions.GroupManager.Group.Build"));
 			handler.reloadGroups();
 			handler.reloadUsers();
			if (plugin.config.getBoolean("Permissions.SendMessageOnGroupChange") == true ) {
				Player[] players = plugin.getServer().getOnlinePlayers();
				for (Player player : players) {
					if (player.hasPermission("GuestUnlock.moderator")) {
						player.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.BOLD + "The player: " + ChatColor.YELLOW + player.getName() + ChatColor.BOLD + " was moved to the build-group.");
					}
				}
			}
 			return true;
 		} else {
 			base.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.RED + "Haha, tricky one, you doesnt belong to the default group!");
 		}
 		return false;
 		
 	}
 	
 }
