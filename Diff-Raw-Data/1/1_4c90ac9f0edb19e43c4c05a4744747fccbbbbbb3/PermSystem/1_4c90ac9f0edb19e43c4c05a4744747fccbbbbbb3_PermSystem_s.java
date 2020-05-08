 package se.myllers.guestunlock;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 import de.bananaco.bpermissions.api.ApiLayer;
 import de.bananaco.bpermissions.api.util.CalculableType;
 
 public class PermSystem {
 
 	/**
 	 * Called to get the plugin
 	 * <p />
 	 * PermissionsEx
 	 */
 	public static final void getPEX() {
 		Main.DEBUG("Getting PEX");
 		final Plugin pex = Main.pm.getPlugin("PermissionsEx");
 		if (pex != null) {
 			if (pex.getClass().getName()
 					.equals("ru.tehkode.permissions.bukkit.PermissionsEx")) {
 				Main.INFO("Sucessfully hooked in to PermissionsEx");
 			} else {
 				Main.SEVERE("Failed to find PermissionsEx, class-name not matching");
 			}
 		} else {
 			Main.SEVERE("Failed to find PermissionsEx, 'pex == null'");
 		}
 	}
 
 	/**
 	 * Called to get the plugin
 	 * <p />
 	 * GroupManager
 	 */
 	public static final void getGM() {
 		Main.DEBUG("Getting GM");
 		final Plugin gm = Main.pm.getPlugin("GroupManager");
 		if (gm != null) {
 			if (gm.getClass().getName()
 					.equals("org.anjocaido.groupmanager.GroupManager")) {
 				Main.INFO("Sucessfully hooked in to GroupManager");
 			} else {
 				Main.SEVERE("Failed to find GroupManager, class-name not matching");
 			}
 		} else {
 			Main.SEVERE("Failed to find GroupManager, 'gm == null'");
 		}
 	}
 
 	/**
 	 * Called to get the plugin
 	 * <p />
 	 * bPermissions
 	 */
 	public static final void getBP() {
 		Main.DEBUG("Getting BP");
 		final Plugin bp = Main.pm.getPlugin("bPermissions");
 		if (bp != null) {
 			if (bp.getClass().getName()
 					.equals("de.bananaco.bpermissions.imp.Permissions")) {
 				Main.INFO("Sucessfully hooked in to bPermissions");
 			} else {
 				Main.SEVERE("Failed to find bPermissions, class-name not matching");
 			}
 		} else {
 			Main.SEVERE("Failed to find bPermissions, 'bp == null'");
 		}
 	}
 
 	/**
 	 * Moves p to the group specifyed in the config.yml
 	 * <p />
 	 * Method for PermissionsEx
 	 * 
 	 * @param p - The Player to move
 	 */
 	public static final void setGroupPEX(final Player p) {
 		Main.DEBUG("Setting players group, PEX");
 		final PermissionUser user = PermissionsEx.getUser(p);
 		final String[] groups = { Main.config
 				.getString("PermissionSystem.PermissionsEx.BuildGroup") };
 		user.setGroups(groups);
 		onGroupChange(p);
 		p.sendMessage(ChatColor.GREEN
 				+ "You have been moved to the build group!");
 		Main.INFO("Set " + p.getName()
 				+ "s group to the one specifyed in the config.yml");
 		Main.DEBUG("Setting players group DONE, PEX");
 	}
 
 	/**
 	 * Moves p to the group specifyed in the config.yml
 	 * <p />
 	 * Method for GroupManager
 	 * 
 	 * @param p - The Player to move
 	 */
 	public static final void setGroupGM(final Player p) {
 		Main.DEBUG("Setting players group, GM");
 		final Plugin gm = Main.pm.getPlugin("GroupManager");
 		final OverloadedWorldHolder handler = ((GroupManager) gm)
 				.getWorldsHolder().getWorldData(p);
 		if (handler == null) {
 			Main.WARNING("Failed to move " + p.getName()
 					+ " to the build group!, GroupManager");
 			return;
 		}
 		final org.anjocaido.groupmanager.data.Group group = handler
 				.getGroup(Main.config
 						.getString("PermissionSystem.GroupManager.BuildGroup"));
 		// handler.getUser(p.getName()).setGroup(group);
 		handler.getUser(p.getName()).setGroup(group, true);
		handler.reload();
 		
 		onGroupChange(p);
 		p.sendMessage(ChatColor.GREEN
 				+ "You have been moved to the build group!");
 		Main.INFO("Set " + p.getName()
 				+ "s group to the one specifyed in the config.yml");
 		Main.DEBUG("Setting players group DONE, GM");
 	}
 	
 	/**
 	 * Moves p to the group specifyed in the config.yml
 	 * <p />
 	 * Method for bPermissions
 	 * 
 	 * @param p - The Player to move
 	 */
 	public static final void setGroupBP(final Player p) {
 		Main.DEBUG("Setting players group, BP");
 		
 		ApiLayer.setGroup(p.getWorld().getName(), CalculableType.USER,
 				p.getName(),
 				Main.config.getString("PermissionSystem.bPermissions.BuildGroup"));
 		ApiLayer.update();
 		onGroupChange(p);
 		p.sendMessage(ChatColor.GREEN
 				+ "You have been moved to the build group!");
 		Main.INFO("Set " + p.getName()
 				+ "s group to the one specifyed in the config.yml");
 		Main.DEBUG("Setting players group DONE, BP");
 	}
 
 	/**
 	 * Sends messages to moderators that the player p has been
 	 * moved to another group
 	 * 
 	 * @param p - The player that is being moved
 	 */
 	private static final void onGroupChange(final Player p) {
 		for (final Player x : Bukkit.getServer().getOnlinePlayers()) {
 			if (Permission.isModerator(x)) {
 				x.sendMessage(ChatColor.YELLOW + x.getName()
 						+ " was moved to the build-group!");
 			}
 		}
 	}
 }
