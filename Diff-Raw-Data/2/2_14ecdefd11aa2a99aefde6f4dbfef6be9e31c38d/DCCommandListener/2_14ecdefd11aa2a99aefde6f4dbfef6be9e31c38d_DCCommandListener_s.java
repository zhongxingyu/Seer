 package pgDev.bukkit.DisguiseCraft.listeners;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 
 import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
 import pgDev.bukkit.DisguiseCraft.disguise.*;
 import pgDev.bukkit.DisguiseCraft.api.*;
 
 public class DCCommandListener implements CommandExecutor, TabCompleter {
 	final DisguiseCraft plugin;
 	
 	public static String[] subCommands = new String[] {"subtypes", "send", "nopickup", "blocklock", "noarmor", "drop"};
 	
 	public DCCommandListener(final DisguiseCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		// Quick Output
 		if (args.length != 0 && args[0].equalsIgnoreCase("subtypes")) {
 			sender.sendMessage(ChatColor.DARK_GREEN + "Available subtypes: " + ChatColor.GREEN + DisguiseType.subTypes);
 			return true;
 		}
 		
 		// Differentiate console input
 		boolean isConsole = false;
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			if (args.length == 0 || (player = plugin.getServer().getPlayer(args[0])) == null) {
				sender.sendMessage("Because you are using the console, you must specify a player as your first argyment.");
 				return true;
 			} else {
 				isConsole = true;
 				args = Arrays.copyOfRange(args, 1, args.length);
 			}
 		}
 		
 		// Pass the event
 		DCCommandEvent cEv = new DCCommandEvent(sender, player, label, args);
 		plugin.getServer().getPluginManager().callEvent(cEv);
 		if (cEv.isCancelled()) return true;
 		
 		// Some conveniences
 		if (args.length != 0) {
 			for (int i=0; args.length > i; i++) {
 				if (args[i].equalsIgnoreCase("cat")) {
 					args[i] = "ocelot";
 				} else if (args[i].equalsIgnoreCase("snowgolem")) {
 					args[i] = "snowman";
 				} else if (args[i].equalsIgnoreCase("angry")) {
 					args[i] = "aggressive";
 				} else if (args[i].equalsIgnoreCase("pigman")) {
 					args[i] = "pigzombie";
 				} else if (args[i].equalsIgnoreCase("mooshroom")) {
 					args[i] = "mushroomcow";
 				} else if (args[i].equalsIgnoreCase("dog")) {
 					args[i] = "wolf";
 				} else if (args[i].equalsIgnoreCase("burn")) {
 					args[i] = "burning";
 				} else if (args[i].equalsIgnoreCase("saddle")) {
 					args[i] = "saddled";
 				}
 			}
 		}
 		
 		// Start command parsing
 		if (label.toLowerCase().startsWith("d")) {
 			if (args.length == 0) { // He needs help!
 				if (isConsole) { // Console output
 					sender.sendMessage("Usage: /" + label + " " + player.getName() + " [subtype] <mob/playername>");
 					String types = "";
 					for (DisguiseType type : DisguiseType.values()) {
 						if (DisguiseType.missingDisguises.contains(type)) continue; 
 						if (types.equals("")) {
 							types = type.name();
 						} else {
 							types = types + ", " + type.name();
 						}
 					}
 					if (!types.equals("")) {
 						sender.sendMessage("Available types: " + types);
 						sender.sendMessage("For a list of subtypes: /disguise subtypes");
 					}
 				} else { // Player output
 					player.sendMessage(ChatColor.DARK_GREEN + "Usage: " + ChatColor.GREEN + "/" + label + " [subtype] <mob/playername>");
 					String types = "";
 					for (DisguiseType type : DisguiseType.values()) {
 						if (DisguiseType.missingDisguises.contains(type)) continue; 
 						if (type.isMob() && !player.hasPermission("disguisecraft.mob." + type.name().toLowerCase())) continue;
 						if (type.isVehicle() && !player.hasPermission("disguisecraft.object.vehicle." + type.name().toLowerCase())) continue;
 						if (type.isBlock() && !player.hasPermission("disguisecraft.object.block." + type.name().toLowerCase())) continue;
 						if (types.equals("")) {
 							types = type.name();
 						} else {
 							types = types + ", " + type.name();
 						}
 					}
 					if (!types.equals("")) {
 						player.sendMessage(ChatColor.DARK_GREEN + "Available types: " + ChatColor.GREEN + types);
 						player.sendMessage(ChatColor.DARK_GREEN + "For a list of subtypes: " + ChatColor.GREEN + "/disguise subtypes");
 					}
 					
 					// Tell of current disguise
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName());
 						if (disguise.type.isPlayer()) {
 							player.sendMessage(ChatColor.GOLD + "You are currently disguised as " + ChatColor.DARK_RED + disguise.data.getFirst() + ".");
 						} else {
 							String subs = ".";
 							if (disguise.data != null) {
 								if (disguise.data.size() == 1) {
 									subs = " with the following subtype: " + ChatColor.DARK_RED + disguise.data.getFirst();
 								} else {
 									for (String sub : disguise.data) {
 										if (subs.equals(".")) {
 											subs = " with the following subtypes: " + ChatColor.DARK_RED + sub;
 										} else {
 											subs = subs + ChatColor.GOLD + ", " + ChatColor.DARK_RED + sub;
 										}
 									}
 								}
 							}
 							
 							if (beginsWithVowel(disguise.type.name())) {
 								player.sendMessage(ChatColor.GOLD + "You are currently disguised as an " + ChatColor.DARK_RED + disguise.type.name() + ChatColor.GOLD + subs);
 							} else {
 								player.sendMessage(ChatColor.GOLD + "You are currently disguised as a " + ChatColor.DARK_RED + disguise.type.name() + ChatColor.GOLD + subs);
 							}
 							
 						}
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("send") || args[0].equalsIgnoreCase("s")) {
 				if (isConsole) {
 					sender.sendMessage(ChatColor.RED + "You cannot send a disguise from the console. Just disguise the player instead.");
 				} else {
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						if (player.hasPermission("disguisecraft.other.disguise")) {
 							if (args.length < 2) {
 								sender.sendMessage(ChatColor.RED + "You must specify a player.");
 							} else {
 								if (args[1].equals("*")) {
 									for (Player receiver : plugin.getServer().getOnlinePlayers()) {
 										if (receiver == player) continue; 
 										Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 										disguise.entityID = plugin.getNextAvailableID();
 										
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(receiver, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) continue;
 										
 										if (plugin.disguiseDB.containsKey(receiver.getName())) {
 											plugin.changeDisguise(receiver, disguise);
 										} else {
 											plugin.disguisePlayer(receiver, disguise);
 										}
 										
 										String blockType = "";
 										if (disguise.type == DisguiseType.FallingBlock) {
 											blockType = Material.getMaterial(disguise.getBlockID()).name() + " ";
 										}
 										if (disguise.type.isPlayer()) {
 											receiver.sendMessage(ChatColor.GOLD + "You have been disguised as player " + ChatColor.GREEN + disguise.data.getFirst() + ChatColor.GOLD + " by " + ChatColor.DARK_GREEN + player.getName());
 										} else {
 											receiver.sendMessage(ChatColor.GOLD + "You have been disguised as a " + ChatColor.GREEN + blockType + disguise.type.name() + ChatColor.GOLD + " by " + ChatColor.DARK_GREEN + player.getName());
 										}
 									}
 									sender.sendMessage(ChatColor.GOLD + "Your disguise has been sent");
 								} else {
 									Player receiver = plugin.getServer().getPlayer(args[1]);
 									if (receiver == null) {
 										sender.sendMessage(ChatColor.RED + "The player you specified could not be found.");
 									} else {
 										Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 										disguise.entityID = plugin.getNextAvailableID();
 										
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(receiver, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										if (plugin.disguiseDB.containsKey(receiver.getName())) {
 											plugin.changeDisguise(receiver, disguise);
 										} else {
 											plugin.disguisePlayer(receiver, disguise);
 										}
 										
 										String blockType = "";
 										if (disguise.type == DisguiseType.FallingBlock) {
 											blockType = Material.getMaterial(disguise.getBlockID()).name() + " ";
 										}
 										if (disguise.type.isPlayer()) {
 											sender.sendMessage(ChatColor.GOLD + "You have disguised " + ChatColor.DARK_GREEN + receiver.getName() + ChatColor.GOLD + " as player " + ChatColor.GREEN + disguise.data.getFirst());
 											receiver.sendMessage(ChatColor.GOLD + "You have been disguised as player " + ChatColor.GREEN + disguise.data.getFirst() + ChatColor.GOLD + " by " + ChatColor.DARK_GREEN + player.getName());
 										} else {
 											sender.sendMessage(ChatColor.GOLD + "You have disguised " + ChatColor.DARK_GREEN + receiver.getName() + ChatColor.GOLD + " as a " + ChatColor.GREEN + blockType + disguise.type.name());
 											receiver.sendMessage(ChatColor.GOLD + "You have been disguised as a " + ChatColor.GREEN + blockType + disguise.type.name() + ChatColor.GOLD + " by " + ChatColor.DARK_GREEN + player.getName());
 										}
 									}
 								}
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "You do not have permission send a disguise to other players.");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You are not wearing a disguise to send.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("nopickup") || args[0].equalsIgnoreCase("np")) {
 				if (isConsole || player.hasPermission("disguisecraft.nopickup")) {
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName());
 						if (disguise.data.remove("nopickup")) {
 							sender.sendMessage(ChatColor.GOLD + "Item pickup enabled");
 						} else {
 							disguise.addSingleData("nopickup");
 							sender.sendMessage(ChatColor.GOLD + "Item pickup disabled");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Must first be disguised.");
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have permission to toggle nopickup");
 				}
 			} else if (args[0].equalsIgnoreCase("blocklock") || args[0].equalsIgnoreCase("bl")) {
 				if (isConsole || player.hasPermission("disguisecraft.blocklock")) {
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName());
 						if (disguise.data.remove("blocklock")) {
 							sender.sendMessage(ChatColor.GOLD + "Block lock disabled");
 						} else {
 							disguise.addSingleData("blocklock");
 							plugin.sendMovement(player, null, player.getVelocity(), player.getLocation());
 							sender.sendMessage(ChatColor.GOLD + "Block lock enabled");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Must first be disguised.");
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have permission to toggle blocklock");
 				}
 			} else if (args[0].equalsIgnoreCase("noarmor") || args[0].equalsIgnoreCase("na")) {
 				if (isConsole || player.hasPermission("disguisecraft.noarmor")) {
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName());
 						if (disguise.data.remove("noarmor")) {
 							sender.sendMessage(ChatColor.GOLD + "No-armor disabled");
 						} else {
 							disguise.addSingleData("noarmor");
 							plugin.sendPacketsToWorld(player.getWorld(), disguise.packetGenerator.getArmorPackets(null));
 							sender.sendMessage(ChatColor.GOLD + "No-armor enabled");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Must first be disguised.");
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have permission to toggle noarmor");
 				}
 			} else if (args[0].equalsIgnoreCase("baby")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type.canBeBaby()) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".baby")) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData("baby");
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a Baby " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a Baby " + plugin.disguiseDB.get(player.getName()).type.name());
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
 							if (disguise.type.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot turn into babies.");
 							} else {
 								if (disguise.type.canBeBaby()) {
 									disguise.addSingleData("baby");
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".baby")) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a Baby " + disguise.type.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a Baby " + disguise.type.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a Baby " + disguise.type.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "No baby form for: " + disguise.type.name());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("black") || args[0].equalsIgnoreCase("blue")|| args[0].equalsIgnoreCase("brown")
 				|| args[0].equalsIgnoreCase("cyan") || args[0].equalsIgnoreCase("gray") || args[0].equalsIgnoreCase("green")
 				|| args[0].equalsIgnoreCase("lightblue") || args[0].equalsIgnoreCase("lime") || args[0].equalsIgnoreCase("magenta")
 				|| args[0].equalsIgnoreCase("orange") || args[0].equalsIgnoreCase("pink") || args[0].equalsIgnoreCase("purple")
 				|| args[0].equalsIgnoreCase("red") || args[0].equalsIgnoreCase("silver") || args[0].equalsIgnoreCase("white")
 				|| args[0].equalsIgnoreCase("yellow") || args[0].equalsIgnoreCase("sheared")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Sheep) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".color." + args[0].toLowerCase())) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData(args[0].toLowerCase());
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
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
 							if (disguise.type.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot change colors.");
 							} else {
 								if (disguise.type == DisguiseType.Sheep) {
 									String currentColor = disguise.getColor();
 									if (currentColor != null) {
 										disguise.data.remove(currentColor);
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".color." + args[0].toLowerCase())) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.type.name() + " cannot be colored.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("charged")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Creeper) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".charged")) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData("charged");
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.changeDisguise(player, disguise);
 								} else {
 									Disguise disguise = new Disguise(plugin.getNextAvailableID(), "charged", type);
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.disguisePlayer(player, disguise);
 								}
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a Charged " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a Charged " + plugin.disguiseDB.get(player.getName()).type.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have permission to disguise as a Charged " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " cannot be charged.");
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains("charged")) {
 							sender.sendMessage(ChatColor.RED + "Already charged.");
 						} else {
 							if (disguise.type.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be charged.");
 							} else {
 								if (disguise.type == DisguiseType.Creeper) {
 									disguise.addSingleData("charged");
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".charged")) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a Charged " + disguise.type.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a Charged " + disguise.type.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a Charged " + disguise.type.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.type.name() + " cannot be charged.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("tiny") || args[0].equalsIgnoreCase("small") || args[0].equalsIgnoreCase("big")
 					|| args[0].equalsIgnoreCase("bigger") || args[0].equalsIgnoreCase("massive") || args[0].equalsIgnoreCase("godzilla")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Slime || type == DisguiseType.MagmaCube) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".size." + args[0].toLowerCase())) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData(args[0].toLowerCase());
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " has no special sizes.");
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains(args[0].toLowerCase())) {
 							sender.sendMessage(ChatColor.RED + "Already " + args[0] + ".");
 						} else {
 							if (disguise.type.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be resized.");
 							} else {
 								if (disguise.type == DisguiseType.Slime || disguise.type == DisguiseType.MagmaCube) {
 									String currentSize = disguise.getSize();
 									if (currentSize != null) {
 										disguise.data.remove(currentSize);
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".size." + args[0].toLowerCase())) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.type.name() + " has no special sizes.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("tamed") || args[0].equalsIgnoreCase("aggressive")) {
 				// Temporary patch
 				sender.sendMessage(ChatColor.RED + "As of 1.4.2, trying to disguise as a tamed or aggressive wolf will crash clients. This feature has been temporarily disabled.");
 				return true;
 				
 				/*
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Wolf) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + "." + args[0].toLowerCase())) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData(args[0].toLowerCase());
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " cannot be " + args[0].toLowerCase());
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains(args[0].toLowerCase())) {
 							sender.sendMessage(ChatColor.RED + "Already " + args[0] + ".");
 						} else {
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be " + args[0].toLowerCase());
 							} else {
 								if (disguise.mob == DisguiseType.Wolf) {
 									if (disguise.data != null) {
 										if (disguise.data.contains("tamed") && !args[0].equals("tamed")) {
 											disguise.data.remove("tamed");
 										} else if (disguise.data.contains("aggressive") && !args[0].equals("aggressive")) {
 											disguise.data.remove("aggressive");
 										}
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + "." + args[0].toLowerCase())) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.type.name() + " cannot be " + args[0].toLowerCase());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}*/
 			} else if (args[0].equalsIgnoreCase("tabby") || args[0].equalsIgnoreCase("tuxedo") || args[0].equalsIgnoreCase("siamese")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Ocelot) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".cat." + args[0].toLowerCase())) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData(args[0].toLowerCase());
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " Cat");
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " Cat");
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " Cat");
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "There is no " + args[0].toLowerCase() + " " + type.name());
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains(args[0].toLowerCase())) {
 							sender.sendMessage(ChatColor.RED + "Already a " + args[0] + " cat.");
 						} else {
 							if (disguise.type.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be " + args[0].toLowerCase());
 							} else {
 								if (disguise.type == DisguiseType.Ocelot) {
 									if (disguise.data != null) {
 										if (disguise.data.contains("tabby") && !args[0].equals("tabby")) {
 											disguise.data.remove("tabby");
 										} else if (disguise.data.contains("tuxedo") && !args[0].equals("tuxedo")) {
 											disguise.data.remove("tuxedo");
 										} else if (disguise.data.contains("siamese") && !args[0].equals("siamese")) {
 											disguise.data.remove("siamese");
 										}
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".cat." + args[0].toLowerCase())) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " Cat");
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " Cat");
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " Cat");
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "There is no " + args[0].toLowerCase() + " " + disguise.type.name());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("burning")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (isConsole || (player.hasPermission("disguisecraft.burning") && player.hasPermission("disguisecraft.mob." + type.name().toLowerCase()))) {
 							if (plugin.disguiseDB.containsKey(player.getName())) {
 								Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 								disguise.setType(type).setSingleData("burning");
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.changeDisguise(player, disguise);
 							} else {
 								Disguise disguise = new Disguise(plugin.getNextAvailableID(), "burning", type);
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.disguisePlayer(player, disguise);
 							}
 							player.sendMessage(ChatColor.GOLD + "You have been disguised as a Burning " + plugin.disguiseDB.get(player.getName()).type.name());
 							if (isConsole) {
 								sender.sendMessage(player.getName() + " was disguised as a Burning " + plugin.disguiseDB.get(player.getName()).type.name());
 							}
 						} else {
 							player.sendMessage(ChatColor.RED + "You do not have permission to disguise as a Burning " + type.name());
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains("burning")) {
 							sender.sendMessage(ChatColor.RED + "Already burning.");
 						} else {
 							// Check for permissions
 							if (isConsole || player.hasPermission("disguisecraft.burning")) {
 								disguise.addSingleData("burning");
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								if (disguise.type.isPlayer()) {
 									plugin.sendPacketToWorld(player.getWorld(), disguise.packetGenerator.getEntityMetadataPacket());
 								} else {
 									plugin.changeDisguise(player, disguise);
 								}
 								player.sendMessage(ChatColor.GOLD + "Your disguise is now burning");
 								if (isConsole) {
 									sender.sendMessage(player.getName() + "'s disguise is now burning");
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have the permissions to have a burning disguise");
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("saddled")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Pig) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".saddled")) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData("saddled");
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.changeDisguise(player, disguise);
 								} else {
 									Disguise disguise = new Disguise(plugin.getNextAvailableID(), "saddled", type);
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.disguisePlayer(player, disguise);
 								}
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a Saddled " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a Saddled " + plugin.disguiseDB.get(player.getName()).type.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have permission to disguise as a Saddled " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " cannot be saddled.");
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains("saddled")) {
 							sender.sendMessage(ChatColor.RED + "Already saddled.");
 						} else {
 							if (disguise.type.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be saddled.");
 							} else {
 								if (disguise.type == DisguiseType.Pig) {
 									disguise.addSingleData("saddled");
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".saddled")) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a Saddled " + disguise.type.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a Saddled " + disguise.type.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a Saddled " + disguise.type.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.type.name() + " cannot be saddled.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("hold")) {
 				if (isConsole || player.hasPermission("disguisecraft.mob.enderman.hold")) {
 					if (args.length > 1) {
 						String specification = remainingWords(args, 1);
 						Material type = Material.matchMaterial(specification);
 						if (type == null) {
 							sender.sendMessage(ChatColor.RED + "The block you specified could not be found");
 						} else {
 							if (type.isBlock()) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									if (disguise.type == DisguiseType.Enderman) {
 										Byte currentHold = disguise.getBlockID();
 										if (currentHold != null) {
 											disguise.data.remove("blockID:" + currentHold);
 										}
 										disguise.addSingleData("blockID:" + type.getId());
 										
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "Your disguise is now holding: " + type.toString());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + "'s disguise is now holding: " + type.toString());
 										}
 									} else {
 										sender.sendMessage(ChatColor.RED + "Only Enderman disguises can hold blocks");
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "Must first be disguised as an Enderman");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "Only blocks can be held");
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.DARK_GREEN + "Usage: " + ChatColor.GREEN + "/" + label + " hold <block/id>");
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have the permissions to hold blocks with your disguise");
 				}
 			} else if (args[0].equalsIgnoreCase("blockdata") || args[0].equalsIgnoreCase("bd")) {
 				if (plugin.disguiseDB.containsKey(player.getName())) {
 					Disguise disguise = plugin.disguiseDB.get(player.getName());
 					if (isConsole || (disguise.type == DisguiseType.Enderman && player.hasPermission("disguisecraft.mob.enderman.hold.metadata"))
 							|| (disguise.type == DisguiseType.FallingBlock && player.hasPermission("disguisecraft.object.block.fallingblock.material.metadata"))) {
 						if (args.length > 1) {
 							Byte block = disguise.getBlockID();
 							if (block == null) {
 								sender.sendMessage(ChatColor.RED + "No block is being held");
 							} else {
 								if (Material.getMaterial(block).getData() == null) {
 									sender.sendMessage(ChatColor.RED + "No metadata can be added to this block");
 								} else {
 									try {
 										Byte newData = Byte.decode(args[1]);
 										
 										Byte currentData = disguise.getBlockData();
 										if (currentData != null) {
 											disguise.data.remove("blockData:" + currentData);
 										}
 										disguise.addSingleData("blockData:" + newData);
 										
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "Your block's metadata is now: " + newData);
 										if (isConsole) {
 											sender.sendMessage(player.getName() + "'s block's metadata is now: " + newData);
 										}
 									} catch (NumberFormatException e) {
 										sender.sendMessage(ChatColor.RED + "Invalid byte");
 									}
 								}
 							}
 						} else {
 							sender.sendMessage(ChatColor.DARK_GREEN + "Usage: " + ChatColor.GREEN + "/" + label + " blockdata <metadata>");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have the permissions to change the metadata of your block");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Must first be disguised");
 				}
 			} else if (args[0].equalsIgnoreCase("librarian") || args[0].equalsIgnoreCase("priest") || args[0].equalsIgnoreCase("blacksmith") || args[0].equalsIgnoreCase("butcher") || args[0].equalsIgnoreCase("generic")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Villager) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".occupation." + args[0].toLowerCase())) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData(args[0].toLowerCase());
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + plugin.disguiseDB.get(player.getName()).type.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " cannot be a " + args[0].toLowerCase());
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains(args[0].toLowerCase())) {
 							sender.sendMessage(ChatColor.RED + "Already a " + args[0] + ".");
 						} else {
 							if (disguise.type.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises do not get occupations");
 							} else {
 								if (disguise.type == DisguiseType.Villager) {
 									if (disguise.data != null) {
 										if (disguise.data.contains("farmer") && !args[0].equals("farmer")) {
 											disguise.data.remove("farmer");
 										} else if (disguise.data.contains("librarian") && !args[0].equals("librarian")) {
 											disguise.data.remove("librarian");
 										} else if (disguise.data.contains("priest") && !args[0].equals("priest")) {
 											disguise.data.remove("priest");
 										} else if (disguise.data.contains("blacksmith") && !args[0].equals("blacksmith")) {
 											disguise.data.remove("blacksmith");
 										} else if (disguise.data.contains("butcher") && !args[0].equals("butcher")) {
 											disguise.data.remove("butcher");
 										} else if (disguise.data.contains("generic") && !args[0].equals("generic")) {
 											disguise.data.remove("generic");
 										}
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".occupation." + args[0].toLowerCase())) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a " + WordUtils.capitalize(args[0].toLowerCase()) + " " + disguise.type.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.type.name() + " cannot be a " + args[0].toLowerCase());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("infected")) {
 				if (args.length > 1) { // New disguise
 					DisguiseType type = DisguiseType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == DisguiseType.Villager) { // For convenience
 							type = DisguiseType.Zombie;
 						}
 						if (type == DisguiseType.Zombie || type == DisguiseType.PigZombie) {
 							if (isConsole || player.hasPermission("disguisecraft.mob." + type.name().toLowerCase() + ".infected")) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setType(type).setSingleData("infected");
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.changeDisguise(player, disguise);
 								} else {
 									Disguise disguise = new Disguise(plugin.getNextAvailableID(), "infected", type);
 									
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.disguisePlayer(player, disguise);
 								}
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as an Infected " + plugin.disguiseDB.get(player.getName()).type.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as an Infected " + plugin.disguiseDB.get(player.getName()).type.name());
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "You do not have permission to disguise as an Infected " + type.name());
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " cannot be infected.");
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains("infected")) {
 							sender.sendMessage(ChatColor.RED + "Already infected.");
 						} else {
 							if (disguise.type == DisguiseType.Zombie || disguise.type == DisguiseType.PigZombie) {
 								disguise.addSingleData("infected");
 								
 								// Check for permissions
 								if (isConsole || player.hasPermission("disguisecraft.mob." + disguise.type.name().toLowerCase() + ".infected")) {
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									plugin.changeDisguise(player, disguise);
 									player.sendMessage(ChatColor.GOLD + "You have been disguised as an Infected " + disguise.type.name());
 									if (isConsole) {
 										sender.sendMessage(player.getName() + " was disguised as an Infected " + disguise.type.name());
 									}
 								} else {
 									player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as an Infected " + disguise.type.name());
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "A " + disguise.type.name() + " cannot be infected.");
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A DisguiseType must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("drop")) {
 				if (isConsole || player.hasPermission("disguisecraft.drop")) {
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						plugin.dropDisguise(player);
 						player.sendMessage(ChatColor.GOLD + "Your disguise has been dropped");
 						if (isConsole) {
 							sender.sendMessage(player.getName() + "'s disguise was dropped");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "There is no disguise being worn");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You don't have permission to drop your disguise");
 				}
 			} else if (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("p") || args[0].equalsIgnoreCase("pl") || args[0].equalsIgnoreCase("plyr")) {
 				if (args.length > 1) {
 					// Dynamically add the player name as a child for the disguisecraft.player.* node
 					try {
 						plugin.getServer().getPluginManager().addPermission(new Permission("disguisecraft.player." + args[1].toLowerCase()).addParent("disguisecraft.player.*", true));
 					} catch (Exception e) {
 					}
 					
 					if (isConsole || player.hasPermission("disguisecraft.player." + args[1].toLowerCase())) {
 						if (args[1].length() <= 16) {
 							if (plugin.disguiseDB.containsKey(player.getName())) {
 								Disguise disguise = plugin.disguiseDB.get(player.getName());
 								
 								// Temporary fix
 								if (disguise.type.isPlayer()) {
 									player.sendMessage(ChatColor.RED + "You'll have to undisguise first. We're still having unusual issues updating the player list when you switch between player disguises.");
 									return true;
 								}
 								
 								disguise.setType(DisguiseType.Player).setSingleData(args[1]);
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.changeDisguise(player, disguise);
 							} else {
 								Disguise disguise = new Disguise(plugin.getNextAvailableID(), args[1], DisguiseType.Player);
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.disguisePlayer(player, disguise);
 							}
 							player.sendMessage(ChatColor.GOLD + "You have been disguised as the player: " + args[1]);
 							if (isConsole) {
 								sender.sendMessage(player.getName() + " was disguised as the player: " + args[1]);
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "The specified player name is too long. (Must be 16 characters or less)");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You do not have the permission to diguise as the player: " + args[1]);
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You must specify the player to disguise as.");
 				}
 			} else {
 				DisguiseType type = DisguiseType.fromString(args[0]);
 				if (type != null) {
 					// Check for permissions
 					if (isConsole || (type.isMob() && player.hasPermission("disguisecraft.mob." + type.name().toLowerCase()))
 							|| (type.isVehicle() && player.hasPermission("disguisecraft.object.vehicle." + type.name().toLowerCase()))
 							|| (type.isBlock() && player.hasPermission("disguisecraft.object.block." + type.name().toLowerCase()))) {
 						if (plugin.disguiseDB.containsKey(player.getName())) {
 							Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 							disguise.setType(type).clearData();
 							
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
 						if (beginsWithVowel(type.name())) {
 							player.sendMessage(ChatColor.GOLD + "You have been disguised as an " + type.name());
 						} else {
 							player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + type.name());
 						}
 						if (isConsole) {
 							if (beginsWithVowel(type.name())) {
 								sender.sendMessage(player.getName() + " was disguised as n " + type.name());
 							} else {
 								sender.sendMessage(player.getName() + " was disguised as a " + type.name());
 							}
 						}
 					} else {
 						if (beginsWithVowel(type.name())) {
 							player.sendMessage(ChatColor.RED + "You do not have permission to disguise as an " + type.name());
 						} else {
 							player.sendMessage(ChatColor.RED + "You do not have permission to disguise as a " + type.name());
 						}
 					}
 				} else {
 					Material block = Material.matchMaterial(remainingWords(args, 0));
 					if (block != null && block.isBlock() && block != Material.AIR) {
 						type = DisguiseType.FallingBlock;
 						if (isConsole || player.hasPermission("disguisecraft.object.block.fallingblock.material")) {
 							String newData = "blockID:" + block.getId();
 							if (plugin.disguiseDB.containsKey(player.getName())) {
 								Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 								disguise.setType(type).setSingleData(newData);
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.changeDisguise(player, disguise);
 							} else {
 								Disguise disguise = new Disguise(plugin.getNextAvailableID(), newData, type);
 								
 								// Pass the event
 								PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.disguisePlayer(player, disguise);
 							}
 							player.sendMessage(ChatColor.GOLD + "You have been disguised as " + block.name());
 							if (isConsole) {
 								sender.sendMessage(player.getName() + " was disguised as " + block.name());
 							}
 						} else {
 							player.sendMessage(ChatColor.RED + "You do not have permission to disguise as a block of that material");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "That disguise type was not recognized.");
 					}
 				}
 			}
 		} else if (label.toLowerCase().startsWith("u")) {
 			if (!isConsole && args.length > 0) {
 				if (player.hasPermission("disguisecraft.other.undisguise")) {
 					if (args[0].equals("*")) {
 						LinkedList<String> undisguisedPlayers = new LinkedList<String>();
 						for (Player currentPlayer : plugin.getServer().getOnlinePlayers()) {
 							if (currentPlayer == player) continue;
 							if (plugin.disguiseDB.containsKey(currentPlayer.getName())) {
 								// Pass the event
 								PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(currentPlayer);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) continue;
 								
 								plugin.unDisguisePlayer(currentPlayer);
 								undisguisedPlayers.add(currentPlayer.getName());
 								currentPlayer.sendMessage(ChatColor.GOLD + "You were undisguised by " + ChatColor.DARK_GREEN + player.getName());
 							}
 						}
 						
 						if (undisguisedPlayers.size() == 0) {
 							sender.sendMessage(ChatColor.RED + "There was no one to undisguise.");
 						} else {
 							String playerNames = "";
 							for (String name : undisguisedPlayers) {
 								if (playerNames.equals("")) {
 									playerNames = ChatColor.DARK_GREEN + name;
 								} else {
 									playerNames = playerNames + ChatColor.GOLD + ", " + ChatColor.DARK_GREEN + name;
 								}
 							}
 							sender.sendMessage(ChatColor.GOLD + "You have undisguised: " + ChatColor.DARK_GREEN + playerNames);
 						}
 					} else {
 						Player toUndisguise;
 						if ((toUndisguise = plugin.getServer().getPlayer(args[0])) == null) {
 							sender.sendMessage(ChatColor.RED + "The given player could not be found.");
 						} else {
 							if (plugin.disguiseDB.containsKey(toUndisguise.getName())) {
 								// Pass the event
 								PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(toUndisguise);
 								plugin.getServer().getPluginManager().callEvent(ev);
 								if (ev.isCancelled()) return true;
 								
 								plugin.unDisguisePlayer(toUndisguise);
 								sender.sendMessage(ChatColor.GOLD + "You have undisguised " + toUndisguise.getName());
 								toUndisguise.sendMessage(ChatColor.GOLD + "You were undisguised by " + player.getName());
 							} else {
 								sender.sendMessage(ChatColor.RED + player.getName() + " is not disguised.");
 							}
 						}
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You do not have the permission to undisguise other players.");
 				}
 			} else {
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
 		}
 		return true;
 	}
 	
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
     	LinkedList<String> output = new LinkedList<String>();
     	Player player = null;
     	String lastArg = "";
     	
     	// Check if player
     	if (sender instanceof Player) {
     		player = (Player) sender;
     	}
     	
     	// Get last argument
     	if (args.length > 0) {
     		lastArg = args[args.length - 1];
     	}
     	
 
 		if (command.getName().equals("disguise")) {
 			// Add subcommands
 			for (String sC : subCommands) {
 				if (sC.startsWith(lastArg.toLowerCase())) {
 					output.add(sC);
 				}
 			}
 			
 			// Add disguise names
 	    	if (!lastArg.equals("")) {
 	    		for (DisguiseType dis : DisguiseType.values()) {
 	    			if (dis.name().toLowerCase().startsWith(lastArg.toLowerCase())) {
 	    				output.add(dis.name());
 	    			}
 	    		}
 	    	}
     		
     		// Add player names
     		if (args.length > 1) {
     			output.addAll(playerNames(lastArg, player == null || DisguiseCraft.pluginSettings.noTabHide));
     		}
 		} else if (command.getName().equals("undisguise")) {
 			if (args.length == 1) {
 				output.addAll(playerNames(lastArg, player == null || DisguiseCraft.pluginSettings.noTabHide));
 			}
 		}
     	
     	return output;
     }
     
     public LinkedList<String> playerNames(String start, boolean disguised) {
     	LinkedList<String> names = new LinkedList<String>();
     	for (Player player : plugin.getServer().getOnlinePlayers()) {
     		if (!disguised && plugin.disguiseDB.containsKey(player.getName())) {
     			continue;
     		}
     		if (player.getDisplayName().toLowerCase().startsWith(start.toLowerCase())) {
     			names.add(player.getDisplayName());
     		}
     	}
     	return names;
     }
 	
 	// List Words After Specified Index
     public static String remainingWords(String[] wordArray, int startWord) {
     	String remaining = "";
     	for (int i=startWord; i<wordArray.length; i++) {
     		remaining = remaining.trim() + " " + wordArray[i];
     	}
     	return remaining.trim();
     }
     
     // First letter vowel?
     public static boolean beginsWithVowel(String s) {
     	s = s.toLowerCase();
     	return (s.startsWith ("a") || s.startsWith ("e") || s.startsWith ("i") || s.startsWith ("o") || s.startsWith ("u"));
     }
 }
