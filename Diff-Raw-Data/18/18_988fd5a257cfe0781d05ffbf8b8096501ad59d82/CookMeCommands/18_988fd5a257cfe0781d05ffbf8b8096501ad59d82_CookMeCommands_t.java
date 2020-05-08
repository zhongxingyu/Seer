 package de.dustplanet.cookme;
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * CookMe for CraftBukkit/Bukkit Handles the commands!
  * 
  * Refer to the forum thread: http://bit.ly/cookmebukkit Refer to the
  * dev.bukkit.org page: http://bit.ly/cookmebukkitdev
  * 
  * @author xGhOsTkiLLeRx thanks nisovin for his awesome code snippet!
  * 
  */
 
 public class CookMeCommands implements CommandExecutor {
     private CookMe plugin;
 
     public CookMeCommands(CookMe instance) {
 	plugin = instance;
     }
 
     // Commands; always check for permissions!
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 	// reload
 	if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
 	    if (sender.hasPermission("cookme.reload") || !plugin.permissions) {
 		cookMeReload(sender);
 	    } else {
 		String message = plugin.localization.getString("permission_denied");
 		plugin.message(sender, null, message, null, null);
 	    }
 	    return true;
 	} else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
 	    // help
 	    if (sender.hasPermission("cookme.help") || !plugin.permissions) {
 		cookMeHelp(sender);
 	    } else {
 		String message = plugin.localization.getString("permission_denied");
 		plugin.message(sender, null, message, null, null);
 	    }
 	    return true;
 	} else if (args.length > 0 && args[0].equalsIgnoreCase("debug") && plugin.debug) {
 	    // Debug
 	    if (sender instanceof Player) {
 		Player player = (Player) sender;
 		player.setFoodLevel(10);
 		player.sendMessage(ChatColor.GREEN + "Food level reduced!");
 	    }
 	    else {
 		sender.sendMessage(ChatColor.RED + "Please use debug mode ingame!");
 	    }
	    return true;
 	} else if (args.length > 1 && args[0].equalsIgnoreCase("set")) {
 	    // Set cooldown, duration or percentage of an effect
 	    if (args[1].equalsIgnoreCase("cooldown")) {
 		if (sender.hasPermission("cookme.cooldown") || !plugin.permissions) {
 		    if (args.length > 2) {
 			int cooldown = 0;
 			try {
 			    cooldown = Integer.valueOf(args[2]);
 			} catch (NumberFormatException e) {
 			    // Cooldown not a number?
 			    String message = plugin.localization.getString("no_number");
 			    plugin.message(sender, null, message, null, null);
 			    return true;
 			}
 			plugin.config.set("configuration.cooldown", cooldown);
 			plugin.saveConfig();
 			String message = plugin.localization.getString("changed_cooldown");
 			plugin.message(sender, null, message, Integer.toString(cooldown), null);
 			plugin.cooldownManager.setCooldown(cooldown);
 		    }
 		} else {
 		    String message = plugin.localization.getString("permission_denied");
 		    plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	    } else if (args[1].equalsIgnoreCase("duration") && args.length > 2) {
 		// Duration
 		if (sender.hasPermission("cookme.duration") || !plugin.permissions) {
 		    // Max or Min
 		    if (args[2].equalsIgnoreCase("max") || args[2].equalsIgnoreCase("min")) {
 			if (args.length > 3) {
 			    int duration = 0;
 			    try {
 				duration = Integer.valueOf(args[3]);
 			    } catch (NumberFormatException e) {
 				// Duration not a number?
 				String message = plugin.localization.getString("no_number");
 				plugin.message(sender, null, message, null, null);
 				return true;
 			    }
 			    plugin.config.set("configuration.duration." + args[2].toLowerCase(), duration);
 			    plugin.saveConfig();
 			    String message = plugin.localization.getString("changed_duration_" + args[2].toLowerCase());
 			    plugin.message(sender, null, message, Integer.toString(duration), null);
 			}
 		    } else {
 			return false;
 		    }
 		} else {
 		    String message = plugin.localization.getString("permission_denied");
 		    plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	    } else if (Arrays.asList(plugin.effects).contains(args[1].toLowerCase())) {
 		// Effects
 		String effect = args[1].toLowerCase();
 		if (args.length > 2) {
 		    if (sender.hasPermission("cookme.set." + effect) || !plugin.permissions) {
 			double percentage = 0.0;
 			try {
 			    percentage = Double.valueOf(args[2]);
 			} catch (NumberFormatException e) {
 			    // Percentage not a number?
 			    String message = plugin.localization.getString("no_number");
 			    plugin.message(sender, null, message, null, null);
 			    return true;
 			}
 			plugin.config.set("effects." + effect.toLowerCase(), percentage);
 			plugin.saveConfig();
 			String message = plugin.localization.getString("changed_effect");
 			plugin.message(sender, null, message, effect, Double.toString(percentage));
 		    } else {
 			String message = plugin.localization.getString("permission_denied");
 			plugin.message(sender, null, message, null, null);
 		    }
 		    return true;
 		} else {
 		    return false;
 		}
 	    }
 	} else if (args.length > 0 && args[0].equalsIgnoreCase("enable")) {
 	    // Enable
 	    // permissions
 	    if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 		if (sender.hasPermission("cookme.enable.permissions") || !plugin.permissions) {
 		    cookMeEnablePermissions(sender);
 		} else {
 		    String message = plugin.localization.getString("permission_denied");
 		    plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	    } else if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 		// Messages
 		if (sender.hasPermission("cookme.enable.messages") || !plugin.permissions) {
 		    cookMeEnableMessages(sender);
 		} else {
 		    String message = plugin.localization.getString("permission_denied");
 		    plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	    }
 	} else if (args.length > 0 && args[0].equalsIgnoreCase("disable")) {
 	    // Disable
 	    // permissions
 	    if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 		if (sender.hasPermission("cookme.disable.permissions") || !plugin.permissions) {
 		    cookMeDisablePermissions(sender);
 		} else {
 		    String message = plugin.localization.getString("permission_denied");
 		    plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	    } else if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 		// Messages
 		if (sender.hasPermission("cookme.disable.messages") || !plugin.permissions) {
 		    cookMeDisableMessages(sender);
 		} else {
 		    String message = plugin.localization.getString("permission_denied");
 		    plugin.message(sender, null, message, null, null);
 		}
 		return true;
 	    }
 	}
 	return false;
     }
 
     // See the help with /cookme help
     private void cookMeHelp(CommandSender sender) {
 	for (int i = 1; i <= 11; i++) {
 	    String message = plugin.localization.getString("help_" + Integer.toString(i));
 	    plugin.message(sender, null, message, null, null);
 	}
     }
 
     // Reloads the config with /cookme reload
     private void cookMeReload(CommandSender sender) {
 	plugin.loadConfigsAgain();
 	String message = plugin.localization.getString("reload");
 	plugin.message(sender, null, message, null, null);
     }
 
     // Enables permissions with /cookme enable permissions
     private void cookMeEnablePermissions(CommandSender sender) {
 	plugin.config.set("configuration.permissions", true);
 	plugin.saveConfig();
 	for (int i = 1; i <= 2; i++) {
 	    String message = plugin.localization.getString("enable_permissions_" + Integer.toString(i));
 	    plugin.message(sender, null, message, null, null);
 	}
     }
 
     // Disables permissions with /cookme disable permissions
     private void cookMeDisablePermissions(CommandSender sender) {
 	plugin.config.set("configuration.permissions", false);
 	plugin.saveConfig();
 	for (int i = 1; i <= 2; i++) {
 	    String message = plugin.localization.getString("disable_permissions_" + Integer.toString(i));
 	    plugin.message(sender, null, message, null, null);
 	}
     }
 
     // Enables messages with /cookme enable messages
     private void cookMeEnableMessages(CommandSender sender) {
 	plugin.config.set("configuration.messages", true);
 	plugin.saveConfig();
 	String message = plugin.localization.getString("enable_messages");
 	plugin.message(sender, null, message, null, null);
     }
 
     // Disables messages with /cookme disable messages
     private void cookMeDisableMessages(CommandSender sender) {
 	plugin.config.set("configuration.messages", false);
 	plugin.saveConfig();
 	String message = plugin.localization.getString("disable_messages");
 	plugin.message(sender, null, message, null, null);
     }
 }
