 package pgDev.bukkit.DisguiseCraft;
 
 import java.util.Arrays;
 
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Player;
 
 import pgDev.bukkit.DisguiseCraft.Disguise.MobType;
 import pgDev.bukkit.DisguiseCraft.api.*;
 
 public class DCCommandListener implements CommandExecutor {
 	final DisguiseCraft plugin;
 	
 	public DCCommandListener(final DisguiseCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		// Differentiate console input
 		boolean isConsole = false;
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			if (args.length == 0 || (player = plugin.getServer().getPlayer(args[0])) == null) {
 				System.out.println("Because you are using the console, you must specify a player as your first argyment.");
 				return true;
 			} else {
 				isConsole = true;
 				args = Arrays.copyOfRange(args, 1, args.length);
 			}
 		}
 		
 		// Start command parsing
 		if (label.toLowerCase().startsWith("d")) {
 			if (args.length == 0) { // He needs help!
 				if (isConsole) { // Console output
 					sender.sendMessage("Usage: /" + label + " " + player.getName() + " [subtype] <mob/playername>");
 					String types = MobType.values().toString();
 					sender.sendMessage("Available types: " + types.substring(1, types.length() - 1));
 					String subTypes = MobType.subTypes.toString();
 					sender.sendMessage("Available subtypes: " + subTypes.substring(1, subTypes.length() - 1));
 				} else { // Player output
 					player.sendMessage(ChatColor.DARK_GREEN + "Usage: " + ChatColor.GREEN + "/" + label + " [subtype] <mob/playername>");
 					String types = "";
 					for (MobType type : MobType.values()) {
 						if (plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase())) {
 							if (types.equals("")) {
 								types = type.name();
 							} else {
 								types = types + ", " + type.name();
 							}
 						}
 					}
 					if (!types.equals("")) {
 						player.sendMessage(ChatColor.DARK_GREEN + "Available types: " + ChatColor.GREEN + types);
 					}
 					player.sendMessage(ChatColor.DARK_GREEN + "Available subtypes: " + ChatColor.GREEN + MobType.subTypes);
 				}
 			} else if (args[0].equalsIgnoreCase("baby")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type.isSubclass(Animals.class) || type == MobType.Villager) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".baby")) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setMob(type).setSingleData("baby");
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.changeDisguise(player, disguise);
 								} else {
 									Disguise disguise = new Disguise(plugin.getNextAvailableID(), "baby", type);
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.disguisePlayer(player, disguise);
 								}
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a Baby " + plugin.disguiseDB.get(player.getName()).mob.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a Baby " + plugin.disguiseDB.get(player.getName()).mob.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have permission to disguise as a Baby " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "No baby form for: " + type.name());
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains("baby")) {
 							sender.sendMessage(ChatColor.RED + "Already in baby form.");
 						} else {
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot turn into babies.");
 							} else {
 								if (disguise.mob.isSubclass(Animals.class) || disguise.mob == MobType.Villager) {
 									disguise.addSingleData("baby");
 									
 									// Check for permissions
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".baby")) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a Baby " + disguise.mob.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a Baby " + disguise.mob.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a Baby " + disguise.mob.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "No baby form for: " + disguise.mob.name());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("black") || args[0].equalsIgnoreCase("blue")|| args[0].equalsIgnoreCase("brown")
 				|| args[0].equalsIgnoreCase("cyan") || args[0].equalsIgnoreCase("gray") || args[0].equalsIgnoreCase("green")
 				|| args[0].equalsIgnoreCase("lightblue") || args[0].equalsIgnoreCase("lime") || args[0].equalsIgnoreCase("magenta")
 				|| args[0].equalsIgnoreCase("orange") || args[0].equalsIgnoreCase("pink") || args[0].equalsIgnoreCase("purple")
 				|| args[0].equalsIgnoreCase("red") || args[0].equalsIgnoreCase("silver") || args[0].equalsIgnoreCase("white")
 				|| args[0].equalsIgnoreCase("yellow")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == MobType.Sheep) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".color." + args[0].toLowerCase())) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setMob(type).setSingleData(args[0].toLowerCase());
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.changeDisguise(player, disguise);
 								} else {
 									Disguise disguise = new Disguise(plugin.getNextAvailableID(), args[0].toLowerCase(), type);
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.disguisePlayer(player, disguise);
 								}
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).mob.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).mob.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " cannot be colored.");
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains(args[0].toLowerCase())) {
 							sender.sendMessage(ChatColor.RED + "Already " + args[0] + ".");
 						} else {
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot change colors.");
 							} else {
 								if (disguise.mob == MobType.Sheep) {
 									String currentColor = disguise.getColor();
 									if (currentColor != null) {
 										disguise.data.remove(currentColor);
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".color." + args[0].toLowerCase())) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.mob.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.mob.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.mob.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.mob.name() + " cannot be colored.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
			} else if (args[0].toLowerCase().startsWith("p")) {
 				if (isConsole || plugin.hasPermissions(player, "disguisecraft.player")) {
 					if (args.length > 1) {
 						if (args[1].length() <= 16) {
 							if (plugin.disguiseDB.containsKey(player.getName())) {
 								Disguise disguise = plugin.disguiseDB.get(player.getName());
 								
 								// Temporary fix
 								if (disguise.isPlayer()) {
 									player.sendMessage(ChatColor.RED + "You'll have to undisguise first. We're still having unusual issues updating the player list when you switch between player disguises.");
 									return true;
 								}
 								
 								disguise.setSingleData(args[1]).setMob(null);
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.changeDisguise(player, disguise);
 							} else {
 								Disguise disguise = new Disguise(plugin.getNextAvailableID(), args[1], null);
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.disguisePlayer(player, disguise);
 							}
 							player.sendMessage(ChatColor.GOLD + "You have been disguised as player: " + args[1]);
 							if (isConsole) {
 								sender.sendMessage(player.getName() + " was disguised as player: " + args[1]);
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "The specified player name is too long. (Must be 16 characters or less)");
 						}
 					} else {
						sender.sendMessage(ChatColor.RED + "You must specify the player to disguis as.");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You do not have the permission to diguise as another player.");
 				}
 			} else {
 				MobType type = MobType.fromString(args[0]);
 				if (type == null) {
 					sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 				} else {
 					// Check for permissions
 					if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase())) {
 						if (plugin.disguiseDB.containsKey(player.getName())) {
 							Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 							disguise.setData(null).setMob(type);
 							
 							// Pass the event
 							PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 							plugin.getServer().getPluginManager().callEvent(ev);
 							if (ev.isCancelled()) return true;
 							
 							plugin.changeDisguise(player, disguise);
 						} else {
 							Disguise disguise = new Disguise(plugin.getNextAvailableID(), type);
 							
 							// Pass the event
 							PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 							plugin.getServer().getPluginManager().callEvent(ev);
 							if (ev.isCancelled()) return true;
 							
 							plugin.disguisePlayer(player, disguise);
 						}
 						player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + type.name());
 						if (isConsole) {
 							sender.sendMessage(player.getName() + " was disguised as a " + type.name());
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have permission to disguise as a " + type.name());
 					}
 				}
 			}
 		} else if (label.toLowerCase().startsWith("u")) {
 			if (plugin.disguiseDB.containsKey(player.getName())) {
 				// Pass the event
 				PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(player);
 				plugin.getServer().getPluginManager().callEvent(ev);
 				if (ev.isCancelled()) return true;
 				
 				plugin.unDisguisePlayer(player);
 				player.sendMessage(ChatColor.GOLD + "You were undisguised.");
 				if (isConsole) {
 					sender.sendMessage(player.getName() + " was undisguised.");
 				}
 			} else {
 				if (isConsole) {
 					sender.sendMessage(player.getName() + " is not disguised.");
 				} else {
 					player.sendMessage(ChatColor.RED + "You are not disguised.");
 				}
 			}
 		}
 		return true;
 	}
 }
