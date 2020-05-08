 import java.util.Arrays;
 
 
 /**
  * CookMe for CraftBukkit/Bukkit
  * Handles the commands!
  * 
  * Refer to the forum thread:
  * http://bit.ly/cookmebukkit
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/cookmebukkitdev
  *
  * @author xGhOsTkiLLeRx
  * @thanks nisovin for his awesome code snippet!
  * 
  */
 
public class CookMeCommands {
     private CookMe plugin;
 
     public CookMeCommands(CookMe instance) {
 	plugin = instance;
     }
 
     // Commands; always check for permissions!
     public boolean onCommand (Player player, String[] args) {
 	// reload
 	if (player.canUseCommand("/cookme") || !plugin.permissions) {
 	    if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
 		CookMeReload(player);
 		return true;
 	    }
 	    // help
 	    if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
 		CookMeHelp(player);	
 		return true;
 	    } else {
 		return false;
 	    }
 	}
 	// Set cooldown, duration or percentage of an effect
 	if (args.length > 1 && args[0].equalsIgnoreCase("set")) {
 	    if (args[1].equalsIgnoreCase("cooldown")) {
 		if (args.length > 2) {
 		    int cooldown = 0;
 		    try {
 			cooldown = Integer.valueOf(args[2]);
 		    }
 		    // Cooldown not a number?
 		    catch (NumberFormatException e) {
 			String message = plugin.localization.getString("no_number");
 			plugin.message(player, message, null, null);
 			return true;
 		    }
 		    plugin.config.setInt("configuration.cooldown", cooldown);
 		    plugin.config.save();
 		    String message = plugin.localization.getString("changed_cooldown");
 		    plugin.message(player, message, Integer.toString(cooldown), null);
 		    plugin.cooldownManager.setCooldown(cooldown);
 		    return true;
 		} else {
 		    return false;
 		}
 	    } else {
 		return false;
 	    }
 	}
 	// Duration
 	if (args[1].equalsIgnoreCase("duration") && args.length > 2) {
 	    // Max or Min
 	    if (args[2].equalsIgnoreCase("max") || args[2].equalsIgnoreCase("min")) {
 		if (args.length > 3) {
 		    int duration = 0;
 		    try {
 			duration = Integer.valueOf(args[3]);
 		    }
 		    // Duration not a number?
 		    catch (NumberFormatException e) {
 			String message = plugin.localization.getString("no_number");
 			plugin.message(player, message, null, null);
 			return true;
 		    }
 		    plugin.config.setInt("configuration.duration." + args[2].toLowerCase(), duration);
 		    plugin.config.save();
 		    String message = plugin.localization.getString("changed_duration_" + args[2].toLowerCase());
 		    plugin.message(player, message, Integer.toString(duration), null);
 		    return true;
 		} else {
 		    return false;
 		}
 	    } else {
 		return false;
 	    }
 	}
 	// Effect
 	if (Arrays.asList(plugin.effects).contains(args[1].toLowerCase())) {
 	    String effect = args[1].toLowerCase();
 	    if (args.length > 2) {
 		double percentage = 0.0;
 		try {
 		    percentage = Double.valueOf(args[2]);
 		}
 		// Percentage not a number?
 		catch (NumberFormatException e) {
 		    String message = plugin.localization.getString("no_number");
 		    plugin.message(player, message, null, null);
 		    return true;
 		}
 		plugin.config.setDouble("effects." + effect.toLowerCase(), percentage);
 		plugin.config.save();
 		String message = plugin.localization.getString("changed_effect");
 		plugin.message(player, message, effect, Double.toString(percentage));
 		return true;
 	    } else {
 		return false;
 	    }
 	}
 	// enable
 	if (args.length > 0 && args[0].equalsIgnoreCase("enable")) {
 	    // permissions
 	    if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 		CookMeEnablePermissions(player);
 		return true;
 	    }
 	    // messages
 	    if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 		CookMeEnableMessages(player);
 		return true;
 	    } else {
 		return false;
 	    }
 	}
 	// disable
 	if (args.length > 0 && args[0].equalsIgnoreCase("disable")) {
 	    // permissions
 	    if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 		CookMeDisablePermissions(player);
 		return true;
 	    }
 	    // messages
 	    if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 		CookMeDisableMessages(player);
 		return true;
 	    } else {
 		return false;
 	    }
 	}
 	else {
 	    String message = plugin.localization.getString("permission_denied");
 	    plugin.message(player, message, null, null);
 	    return true;
 	}
     }
 
     // See the help with /cookme help
     private void CookMeHelp(Player player) {
 	for (int i = 1; i <= 11; i++) {
 	    String message = plugin.localization.getString("help_" + Integer.toString(i));
 	    plugin.message(player, message, null, null);
 	}
     }
 
     // Reloads the config with /cookme reload
     private void CookMeReload(Player player) {
 	plugin.loadConfigsAgain();		
 	String message = plugin.localization.getString("reload");
 	plugin.message(player, message, null, null);
     }
 
     // Enables permissions with /cookme enable permissions
     private void CookMeEnablePermissions(Player player) {
 	plugin.config.setBoolean("configuration.permissions", true);
 	plugin.config.save();
 	for (int i = 1; i <= 2; i++) {
 	    String message = plugin.localization.getString("enable_permissions_" + Integer.toString(i));
 	    plugin.message(player, message, null, null);
 	}
     }
 
     // Disables permissions with /cookme disable permissions
     private void CookMeDisablePermissions(Player player) {
 	plugin.config.setBoolean("configuration.permissions", false);
 	plugin.config.save();
 	for (int i = 1; i <= 2; i++) {
 	    String message = plugin.localization.getString("disable_permissions_" + Integer.toString(i));
 	    plugin.message(player, message, null, null);
 	}
     }
 
     // Enables messages with /cookme enable messages
     private void CookMeEnableMessages(Player player) {
 	plugin.config.setBoolean("configuration.messages", true);
 	plugin.config.save();
 	String message = plugin.localization.getString("enable_messages");
 	plugin.message(player, message, null, null);
     }
 
     // Disables messages with /cookme disable messages
     private void CookMeDisableMessages(Player player) {
 	plugin.config.setBoolean("configuration.messages", false);
 	plugin.config.save();
 	String message = plugin.localization.getString("disable_messages");
 	plugin.message(player, message, null, null);
     }
 }
