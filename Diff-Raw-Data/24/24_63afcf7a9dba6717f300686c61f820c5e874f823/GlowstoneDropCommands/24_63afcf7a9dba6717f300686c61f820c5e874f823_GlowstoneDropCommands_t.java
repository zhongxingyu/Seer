 package de.xghostkillerx.glowstonedrop;
 
 import java.util.Arrays;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * GlowstoneDrop for CraftBukkit/Bukkit Handles the commands!
  * Refer to the forum thread: 
  * http://bit.ly/oW6iR1
  * Refer to the dev.bukkit.org page: 
  * http://bit.ly/rcN2QB
  * 
  * @author  xGhOsTkiLLeRx
  * @thanks  to XxFuNxX for the original GlowstoneDrop plugin!
  */
 
 public class GlowstoneDropCommands implements CommandExecutor {
 	private GlowstoneDrop plugin;
 
 	public GlowstoneDropCommands(GlowstoneDrop plugin) {
 		this.plugin = plugin;
 	}
 	private String[] worlds = {"normal", "nether", "end"};
 	private String[] values = {"block", "dust"};
 	private String message;
 	private int i;
 
 	// Commands; always check for permissions!
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		// reload
 		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
 			if (plugin.config.getBoolean("configuration.permissions") == true) {
 				if (sender.hasPermission("glowstonedrop.reload")) {
 					GlowstoneDropReload(sender);
 					return true;
 				}
 				else {
 					message = plugin.localization.getString("permission_denied");
 					plugin.message(sender, null, message, null, null);
 					return true;
 				}
 			}
 			if (plugin.config.getBoolean("configuration.permissions") == false) {
 				GlowstoneDropReload(sender);
 				return true;
 			}
 		}
 		// help
 		if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
 			if (plugin.config.getBoolean("configuration.permissions") == true) {
 				if (sender.hasPermission("glowstonedrop.help")) {
 					GlowstoneDropHelp(sender);
 					return true;
 				}
 				else {
 					message = plugin.localization.getString("permission_denied");
 					plugin.message(sender, null, message, null, null);
 					return true;
 				}
 			}
 			if (plugin.config.getBoolean("configuration.permissions") == false) {
 				GlowstoneDropHelp(sender);
 				return true;
 			}
 		}
 		// set
 		if (args.length > 2 && args[0].equalsIgnoreCase("set")) {
 			String world = args[1], value = args[2];
 			if (Arrays.asList(worlds).contains(args[1])) {
 				if (Arrays.asList(values).contains(args[2])) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.set.normal")) {
 							GlowstoneDropSet(sender, world, value);
 						}
 						else {
 							message = plugin.localization.getString("permission_denied");
 							plugin.message(sender, null, message, null, null);
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropSet(sender, world, value);
 						return true;
 					}
 				}
 			}
 			// all
 			if (args.length > 2 && args[1].equalsIgnoreCase("all")) {
 				// block
 				if (Arrays.asList(values).contains(args[2])) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.set.all")) {
 							GlowstoneDropSetAll(sender, value);
 							return true;
 						}
 						else {
 							message = plugin.localization.getString("permission_denied");
 							plugin.message(sender, null, message, null, null);
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropSetAll(sender, value);
 						return true;
 					}
 				}
 			}
 		}
 		// enable
 		if (args.length > 0 && args[0].equalsIgnoreCase("enable"))   {
 			if ((args[1].equalsIgnoreCase("messages")) || args[1].equalsIgnoreCase("permissions")) {
 				// permissions
 				String value = args[1];
 				if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.enable." + args[0])) {
 							GlowstoneDropEnable(sender, value);
 							return true;
 						}
 						else {
 							message = plugin.localization.getString("permission_denied");
 							plugin.message(sender, null, message, null, null);
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropEnable(sender, value);
 						return true;
 					}
 				}
 			}
 		}
 		// disable
 		if (args.length > 0 && args[0].equalsIgnoreCase("disable"))   {
 			if ((args[1].equalsIgnoreCase("messages")) || args[1].equalsIgnoreCase("permissions")) {
 				// permissions
 				String value = args[1];
 				if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.disable." + args[0])) {
 							GlowstoneDropDisable(sender, value);
 							return true;
 						}
 						else {
 							message = plugin.localization.getString("permission_denied");
 							plugin.message(sender, null, message, null, null);
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropDisable(sender, value);
 						return true;
 					}
 				}
 			}
 		}
		return false;
 	}
 
 	// Set a value
 	private void GlowstoneDropSet(CommandSender sender, String world, String value) {
 		plugin.config.set("worlds." + world.toLowerCase(), value.toLowerCase());
 		plugin.saveConfig();
 		message = plugin.localization.getString("set");
 		plugin.message(sender, null, message, value, world);
 
 	}
 
 	// See the help with /glowstonedrop help or /glowdrop help
 	private boolean GlowstoneDropHelp(CommandSender sender) {
 		for (i=1; i <= 12; i++) {
 			message = plugin.localization.getString("help_" + Integer.toString(i));
 			plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	}
 
 	// Reloads the config with /glowstonedrop reload or /glowdrop reload
 	private boolean GlowstoneDropReload(CommandSender sender) {
 		plugin.loadConfigsAgain();
 		message = plugin.localization.getString("reload");
 		plugin.message(sender, null, message, null, null);
 		return true;
 	}
 
 	// Enables permissions with /glowstonedrop enable <value> or /glowdrop enable <value>
 	private boolean GlowstoneDropEnable(CommandSender sender, String value) {
 		plugin.config.set("configuration." + value, true);
 		plugin.saveConfig();
 		if (value.equalsIgnoreCase("permissions")) {
 			for (i=1; i <= 2; i++) {
 				message = plugin.localization.getString("enable_permissions_" + Integer.toString(i));
 				plugin.message(sender, null, message, null, null);
 			}
 		}
 		else {
 			message = plugin.localization.getString("enable_messages");
 			plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	}
 
 	// Disables messages with /glowstonedrop disable <value> or /glowdrop disable <value>
 	private boolean GlowstoneDropDisable(CommandSender sender, String value) {
 		plugin.config.set("configuration." + value, false);
 		plugin.saveConfig();
 		if (value.equalsIgnoreCase("permissions")) {
 			for (i=1; i <= 2; i++) {
 				message = plugin.localization.getString("disable_permissions_" + Integer.toString(i));
 				plugin.message(sender, null, message, null, null);
 			}
 		}
 		else {
 			message = plugin.localization.getString("disable_messages");
 			plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	}
 
 
 	// Sets the all drops to dust with /glowstonedrop set all dust or /glowdrop set all dust
 	private boolean GlowstoneDropSetAll(CommandSender sender, String value) {
 		plugin.config.set("worlds.normal", value.toLowerCase());
 		plugin.config.set("worlds.nether", value.toLowerCase());
 		plugin.config.set("worlds.end", value.toLowerCase());
 		plugin.saveConfig();
 		message = plugin.localization.getString("set_all");
 		plugin.message(sender, null, message, value, null);
 		return true;
 	}
 }
