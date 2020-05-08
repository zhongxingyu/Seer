 package se.myller.GuestUnlock.Permission;
 
 import java.util.logging.Level;
 
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
 	 */
 	public void getGM() {
 		plugin.log("DEBUG: Getting GroupManager", true, Level.INFO);
 		Plugin GM = plugin.pluginManager.getPlugin("GroupManager");
 
 		if (GM != null) {
 			if (GM.getClass().getName()
 					.equals("org.anjocaido.groupmanager.GroupManager")) {
 				plugin.log(
 						"=====   Found GroupManager, I will try to enable a hook!",
 						false, Level.INFO);
 				plugin.groupMan = (GroupManager) GM;
 			} else {
 				plugin.log("-----------------------------------------", true,
 						Level.INFO);
 				plugin.log(
 						"=====   You wanted GroupManager support, could not find it.",
 						false, Level.INFO);
 				plugin.log("=====   I cant find it!", true, Level.INFO);
 				plugin.log("=====   Could not find GroupManager.", true,
 						Level.INFO);
 				plugin.log("-----------------------------------------", true,
 						Level.INFO);
 				return;
 			}
 		} else {
 			plugin.log("-----------------------------------------", true,
 					Level.INFO);
 			plugin.log(
 					"=====   You wanted GroupManager support, could not find it.",
 					false, Level.INFO);
 			plugin.log("=====   I cant find it!", true, Level.INFO);
 			plugin.log("=====   Could not find GroupManager.", true, Level.INFO);
 			plugin.log("-----------------------------------------", true,
 					Level.INFO);
 			return;
 		}
 	}
 
 	public boolean setGroupGM(final Player base) {
 		plugin.log("DEBUG: Changing player " + base.getName() + "s group", true, Level.INFO);
 		final OverloadedWorldHolder handler = plugin.groupMan.getWorldsHolder()
 				.getWorldData(base);
 		if (handler == null) {
 			return false;
 		}
		if (handler.getUser(base.getName()).getGroupName()
				.equals("Permissions.GroupManager.Group.Default")) {
			handler.getUser(base.getName()).setGroup(handler.getGroup(plugin.config
									.getString("Permissions.GroupManager.Group.Build")));
			plugin.log("Set "
							+ base.getName()
							+ ":s group to "
							+ plugin.config.getString("Permissions.GroupManager.Group.Build"),
					true, Level.INFO);
 			base.sendMessage(ChatColor.AQUA
 					+ "[GuestUnlock] "
 					+ ChatColor.GREEN
 					+ "Your group is now "
 					+ plugin.config.getString("Permissions.GroupManager.Group.Build"));
 			handler.reloadGroups();
 			handler.reloadUsers();
 			if (plugin.config.getBoolean("Permissions.SendMessageOnGroupChange")) {
 				plugin.log("DEBUG: Sending message to mods, cause: player group change", true, Level.INFO);
 				Player[] players = plugin.getServer().getOnlinePlayers();
 				for (Player player : players) {
 					if (player.hasPermission("GuestUnlock.moderator")) {
 						player.sendMessage(ChatColor.AQUA + "[GuestUnlock] "
 								+ ChatColor.GREEN + "The player: "
 								+ ChatColor.YELLOW + player.getName()
 								+ ChatColor.GREEN
 								+ " was moved to the build-group.");
 					}
 				}
 			}
 			return true;
 		} else {
 			base.sendMessage(ChatColor.AQUA
 					+ "[GuestUnlock] "
 					+ ChatColor.RED
 					+ "Haha, tricky one, you doesnt belong to the default group!");
 			plugin.log("DEBUG: Someone tried to send the password but was not in the default group.", true, Level.INFO);
 		}
 		return false;
 
 	}
 
 }
