 package de.dustplanet.cookme;
 
 import java.util.Arrays;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
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
 
 public class CookMeCommands implements CommandExecutor {
 	private CookMe plugin;
 	public CookMeCommands(CookMe instance) {
 		plugin = instance;
 	}
 
 	// Commands; always check for permissions!
 	public boolean onCommand (CommandSender sender, Command command, String commandLabel, String[] args) {
 		// reload
 		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
 			if (sender.hasPermission("cookme.reload") || !plugin.permissions) {
 				CookMeReload(sender);
 			}
 			else {
 				String message = plugin.localization.getString("permission_denied");
 				plugin.message(sender, null, message, null, null);
 			}
 			return true;
 		}
 		// help
 		if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
 			if (sender.hasPermission("cookme.help") || !plugin.permissions) {
 				CookMeHelp(sender);						
 			}
 			else {
 				String message = plugin.localization.getString("permission_denied");
 				plugin.message(sender, null, message, null, null);
 			}
 			return true;
 		}
 		// Set cooldown, duration or percentage of an effect
 		if (args.length > 1 && args[0].equalsIgnoreCase("set")) {
 			if (args[1].equalsIgnoreCase("cooldown")) {
 				if (sender.hasPermission("cookme.cooldown") || !plugin.permissions) {
 					if (args.length > 2) {
 						int cooldown = 0;
 						try {
 							cooldown = Integer.valueOf(args[2]);
 						}
 						// Cooldown not a number?
 						catch (NumberFormatException e) {
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
 				}
 				else {
 					String message = plugin.localization.getString("permission_denied");
 					plugin.message(sender, null, message, null, null);
 				}
 				return true;
 			}
 			// Duration
 			if (args[1].equalsIgnoreCase("duration")) {
 				if (args.length > 2) {
 					if (sender.hasPermission("cookme.duration") || !plugin.permissions) {
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
 									plugin.message(sender, null, message, null, null);
 									return true;
 								}
 								plugin.config.set("configuration.duration." + args[2].toLowerCase(), duration);
 								plugin.saveConfig();
 								String message = plugin.localization.getString("changed_duration_" + args[2].toLowerCase());
 								plugin.message(sender, null, message, Integer.toString(duration), null);
 							}
 						}
 						else return false;
 					}
 					else {
 						String message = plugin.localization.getString("permission_denied");
 						plugin.message(sender, null, message, null, null);
 					}
 					return true;
 				}
 			}
 			// Effect
 			if (Arrays.asList(plugin.effects).contains(args[1].toLowerCase())) {
 				String effect = args[1].toLowerCase();
 				if (args.length > 2) {
 					if (sender.hasPermission("cookme.set." + effect) || !plugin.permissions) {
 						double percentage = 0.0;
 						try {
 							percentage = Double.valueOf(args[2]);
 						}
 						// Percentage not a number?
 						catch (NumberFormatException e) {
 							String message = plugin.localization.getString("no_number");
 							plugin.message(sender, null, message, null, null);
 							return true;
 						}
 						plugin.config.set("effects." + effect.toLowerCase(), percentage);
 						plugin.saveConfig();
 						String message = plugin.localization.getString("changed_effect");
 						plugin.message(sender, null, message, effect, Double.toString(percentage));
 					}
 					else {
 						String message = plugin.localization.getString("permission_denied");
 						plugin.message(sender, null, message, null, null);
 					}
 					return true;
 				}
 				else return false;
 			}
 		}
 		// enable
 		if (args.length > 0 && args[0].equalsIgnoreCase("enable")) {
 			// permissions
 			if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 				if (sender.hasPermission("cookme.enable.permissions") || !plugin.permissions) {
 					CookMeEnablePermissions(sender);
 				}
 				else {
 					String message = plugin.localization.getString("permission_denied");
 					plugin.message(sender, null, message, null, null);
 				}
 				return true;
 			}
 			// messages
 			if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 				if (sender.hasPermission("cookme.enable.messages") || !plugin.permissions) {
 					CookMeEnableMessages(sender);
 				}
 				else {
 					String message = plugin.localization.getString("permission_denied");
 					plugin.message(sender, null, message, null, null);
 				}
 				return true;
 			}
 		}
 		// disable
 		if (args.length > 0 && args[0].equalsIgnoreCase("disable")) {
 			// permissions
 			if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 				if (sender.hasPermission("cookme.disable.permissions") || !plugin.permissions) {
 					CookMeDisablePermissions(sender);
 				}
 				else {
 					String message = plugin.localization.getString("permission_denied");
 					plugin.message(sender, null, message, null, null);
 				}
 				return true;
 			}
 			// messages
 			if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 				if (sender.hasPermission("cookme.disable.messages") || !plugin.permissions) {
 					CookMeDisableMessages(sender);
 				}
 				else {
 					String message = plugin.localization.getString("permission_denied");
 					plugin.message(sender, null, message, null, null);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// See the help with /cookme help
 	private void CookMeHelp(CommandSender sender) {
 		for (int i = 1; i <= 11; i++) {
 			String message = plugin.localization.getString("help_" + Integer.toString(i));
 			plugin.message(sender, null, message, null, null);
 		}
 	}
 
 	// Reloads the config with /cookme reload
 	private void CookMeReload(CommandSender sender) {
 		plugin.loadConfigsAgain();		
 		String message = plugin.localization.getString("reload");
 		plugin.message(sender, null, message, null, null);
 	}
 
 	// Enables permissions with /cookme enable permissions
 	private void CookMeEnablePermissions(CommandSender sender) {
 		plugin.config.set("configuration.permissions", true);
 		plugin.saveConfig();
 		for (int i = 1; i <= 2; i++) {
 			String message = plugin.localization.getString("enable_permissions_" + Integer.toString(i));
 			plugin.message(sender, null, message, null, null);
 		}
 	}
 
 	// Disables permissions with /cookme disable permissions
 	private void CookMeDisablePermissions(CommandSender sender) {
 		plugin.config.set("configuration.permissions", false);
 		plugin.saveConfig();
 		for (int i = 1; i <= 2; i++) {
 			String message = plugin.localization.getString("disable_permissions_" + Integer.toString(i));
 			plugin.message(sender, null, message, null, null);
 		}
 	}
 
 	// Enables messages with /cookme enable messages
 	private void CookMeEnableMessages(CommandSender sender) {
 		plugin.config.set("configuration.messages", true);
 		plugin.saveConfig();
 		String message = plugin.localization.getString("enable_messages");
 		plugin.message(sender, null, message, null, null);
 	}
 
 	// Disables messages with /cookme disable messages
 	private void CookMeDisableMessages(CommandSender sender) {
 		plugin.config.set("configuration.messages", false);
 		plugin.saveConfig();
 		String message = plugin.localization.getString("disable_messages");
 		plugin.message(sender, null, message, null, null);
 	}
 }
