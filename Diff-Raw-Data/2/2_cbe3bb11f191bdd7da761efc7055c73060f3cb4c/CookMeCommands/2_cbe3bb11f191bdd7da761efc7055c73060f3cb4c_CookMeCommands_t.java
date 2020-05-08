 import java.util.Arrays;
 
 /**
  * CookMe for Canary (legacy)
  * Handles the commands!
  * 
  * Refer to the forum thread:
  * http://forums.canarymod.net/?topic=3523.0
  *
  * @author xGhOsTkiLLeRx
  * 
  */
 
 public class CookMeCommands extends PluginListener {
     private CookMe plugin;
 
     public CookMeCommands(CookMe instance) {
 	plugin = instance;
     }
 
     // Commands; always check for permissions!
     public boolean onCommand (Player player, String[] args) {
 	if (args[0].equalsIgnoreCase("/cookme")) {
 	    // reload
 	    if (player.canUseCommand("/cookme") || !plugin.permissions) {
 		if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
 		    cookMeReload(player);
 		    return true;
 		}
 		// help
		else if (args.length > 1 && args[1].equalsIgnoreCase("help")) {
 		    cookMeHelp(player);	
 		    return true;
 		} else if (args.length > 1 && args[1].equalsIgnoreCase("debug") && plugin.debug) {
 		    player.setFoodLevel(10);
 		    player.sendMessage("§2Food level reduced!");
 		    return true;
 		}
 		// Set cooldown, duration or percentage of an effect
 		else if (args.length > 2 && args[1].equalsIgnoreCase("set")) {
 		    if (args[1].equalsIgnoreCase("cooldown")) {
 			if (args.length > 3) {
 			    int cooldown = 0;
 			    try {
 				cooldown = Integer.valueOf(args[3]);
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
 		    }
 		    // Duration
 		    else if (args[2].equalsIgnoreCase("duration") && args.length > 3) {
 			// Max or Min
 			if (args[3].equalsIgnoreCase("max") || args[3].equalsIgnoreCase("min")) {
 			    if (args.length > 4) {
 				int duration = 0;
 				try {
 				    duration = Integer.valueOf(args[4]);
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
 		    else if (Arrays.asList(plugin.effects).contains(args[2].toLowerCase())) {
 			String effect = args[2].toLowerCase();
 			if (args.length > 3) {
 			    double percentage = 0.0;
 			    try {
 				percentage = Double.valueOf(args[3]);
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
 		    } else {
 			return false;
 		    }
 		}
 		// enable
 		else if (args.length > 1 && args[1].equalsIgnoreCase("enable")) {
 		    // permissions
 		    if (args.length > 2 && args[2].equalsIgnoreCase("permissions")) {
 			cookMeEnablePermissions(player);
 			return true;
 		    }
 		    // messages
 		    if (args.length > 2 && args[2].equalsIgnoreCase("messages")) {
 			cookMeEnableMessages(player);
 			return true;
 		    } else {
 			return false;
 		    }
 		}
 		// disable
 		else if (args.length > 1 && args[1].equalsIgnoreCase("disable")) {
 		    // permissions
 		    if (args.length > 2 && args[2].equalsIgnoreCase("permissions")) {
 			cookMeDisablePermissions(player);
 			return true;
 		    }
 		    // messages
 		    if (args.length > 2 && args[2].equalsIgnoreCase("messages")) {
 			cookMeDisableMessages(player);
 			return true;
 		    } else {
 			return false;
 		    }
 		}
 	    } else {
 		String message = plugin.localization.getString("permission_denied");
 		plugin.message(player, message, null, null);
 		return true;
 	    }
 	}
 	return false;
     }
 
     // See the help with /cookme help
     private void cookMeHelp(Player player) {
 	for (int i = 1; i <= 11; i++) {
 	    String message = plugin.localization.getString("help_" + Integer.toString(i));
 	    plugin.message(player, message, null, null);
 	}
     }
 
     // Reloads the config with /cookme reload
     private void cookMeReload(Player player) {
 	plugin.loadConfigsAgain();		
 	String message = plugin.localization.getString("reload");
 	plugin.message(player, message, null, null);
     }
 
     // Enables permissions with /cookme enable permissions
     private void cookMeEnablePermissions(Player player) {
 	plugin.config.setBoolean("configuration.permissions", true);
 	plugin.config.save();
 	for (int i = 1; i <= 2; i++) {
 	    String message = plugin.localization.getString("enable_permissions_" + Integer.toString(i));
 	    plugin.message(player, message, null, null);
 	}
     }
 
     // Disables permissions with /cookme disable permissions
     private void cookMeDisablePermissions(Player player) {
 	plugin.config.setBoolean("configuration.permissions", false);
 	plugin.config.save();
 	for (int i = 1; i <= 2; i++) {
 	    String message = plugin.localization.getString("disable_permissions_" + Integer.toString(i));
 	    plugin.message(player, message, null, null);
 	}
     }
 
     // Enables messages with /cookme enable messages
     private void cookMeEnableMessages(Player player) {
 	plugin.config.setBoolean("configuration.messages", true);
 	plugin.config.save();
 	String message = plugin.localization.getString("enable_messages");
 	plugin.message(player, message, null, null);
     }
 
     // Disables messages with /cookme disable messages
     private void cookMeDisableMessages(Player player) {
 	plugin.config.setBoolean("configuration.messages", false);
 	plugin.config.save();
 	String message = plugin.localization.getString("disable_messages");
 	plugin.message(player, message, null, null);
     }
 }
