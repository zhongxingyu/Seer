 package pgDev.bukkit.DisguiseCraft.listeners;
 
 import java.util.Arrays;
 
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Player;
 
 import pgDev.bukkit.DisguiseCraft.Disguise;
 import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
 import pgDev.bukkit.DisguiseCraft.Disguise.MobType;
 import pgDev.bukkit.DisguiseCraft.api.*;
 
 public class DCCommandListener implements CommandExecutor {
 	final DisguiseCraft plugin;
 	
 	public DCCommandListener(final DisguiseCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		// Quick Output
 		if (args.length != 0 && args[0].equalsIgnoreCase("subtypes")) {
 			sender.sendMessage(ChatColor.DARK_GREEN + "Available subtypes: " + ChatColor.GREEN + MobType.subTypes);
 			return true;
 		}
 		
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
 					String types = MobType.values().toString();
 					sender.sendMessage("Available types: " + types.substring(1, types.length() - 1));
 					sender.sendMessage("For a list of subtypes: /d subtypes");
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
 					player.sendMessage(ChatColor.DARK_GREEN + "For a list of subtypes: " + ChatColor.GREEN + "/d subtypes");
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
 				|| args[0].equalsIgnoreCase("yellow") || args[0].equalsIgnoreCase("sheared")) {
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
 			} else if (args[0].equalsIgnoreCase("charged")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == MobType.Creeper) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".charged")) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setMob(type).setSingleData("charged");
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a Charged " + plugin.disguiseDB.get(player.getName()).mob.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a Charged " + plugin.disguiseDB.get(player.getName()).mob.name());
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
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be charged.");
 							} else {
 								if (disguise.mob == MobType.Creeper) {
 									disguise.addSingleData("charged");
 									
 									// Check for permissions
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".charged")) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a Charged " + disguise.mob.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a Charged " + disguise.mob.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a Charged " + disguise.mob.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.mob.name() + " cannot be charged.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("tiny") || args[0].equalsIgnoreCase("small") || args[0].equalsIgnoreCase("big")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == MobType.Slime || type == MobType.MagmaCube) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".size." + args[0].toLowerCase())) {
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
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " has no special sizes.");
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains(args[0].toLowerCase())) {
 							sender.sendMessage(ChatColor.RED + "Already " + args[0] + ".");
 						} else {
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be resized.");
 							} else {
 								if (disguise.mob == MobType.Slime || disguise.mob == MobType.MagmaCube) {
 									String currentSize = disguise.getSize();
 									if (currentSize != null) {
 										disguise.data.remove(currentSize);
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".size." + args[0].toLowerCase())) {
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
 									sender.sendMessage(ChatColor.RED + "A " + disguise.mob.name() + " has no special sizes.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("tamed") || args[0].equalsIgnoreCase("aggressive")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == MobType.Wolf) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + "." + args[0].toLowerCase())) {
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
 								if (disguise.mob == MobType.Wolf) {
 									if (disguise.data != null) {
 										if (disguise.data.contains("tamed") && !args[0].equals("tamed")) {
 											disguise.data.remove("tamed");
 										} else if (disguise.data.contains("aggressive") && !args[0].equals("aggressive")) {
 											disguise.data.remove("aggressive");
 										}
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + "." + args[0].toLowerCase())) {
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
 									sender.sendMessage(ChatColor.RED + "A " + disguise.mob.name() + " cannot be " + args[0].toLowerCase());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("tabby") || args[0].equalsIgnoreCase("tuxedo") || args[0].equalsIgnoreCase("siamese")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == MobType.Ocelot) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".cat." + args[0].toLowerCase())) {
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
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be " + args[0].toLowerCase());
 							} else {
 								if (disguise.mob == MobType.Ocelot) {
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
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".cat." + args[0].toLowerCase())) {
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
 									sender.sendMessage(ChatColor.RED + "There is no " + args[0].toLowerCase() + " " + disguise.mob.name());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("burning")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
						if (isConsole || (plugin.hasPermissions(player, "disguisecraft.burning") && plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase()))) {
 							if (plugin.disguiseDB.containsKey(player.getName())) {
 								Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 								disguise.setMob(type).setSingleData("burning");
 								
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
 							player.sendMessage(ChatColor.GOLD + "You have been disguised as a Burning " + plugin.disguiseDB.get(player.getName()).mob.name());
 							if (isConsole) {
 								sender.sendMessage(player.getName() + " was disguised as a Burning " + plugin.disguiseDB.get(player.getName()).mob.name());
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
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises can't be set to burn");
 							} else {
 								disguise.addSingleData("burning");
 								
 								// Check for permissions
 								if (isConsole || plugin.hasPermissions(player, "disguisecraft.burning")) {
 									// Pass the event
 									PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 									plugin.getServer().getPluginManager().callEvent(ev);
 									if (ev.isCancelled()) return true;
 									
 									if (disguise.isPlayer()) {
 										plugin.sendPacketToWorld(player.getWorld(), disguise.getMetadataPacket());
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
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("saddled")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == MobType.Pig) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".saddled")) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									disguise.setMob(type).setSingleData("saddled");
 									
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
 								player.sendMessage(ChatColor.GOLD + "You have been disguised as a Saddled " + plugin.disguiseDB.get(player.getName()).mob.name());
 								if (isConsole) {
 									sender.sendMessage(player.getName() + " was disguised as a Saddled " + plugin.disguiseDB.get(player.getName()).mob.name());
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
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises cannot be saddled.");
 							} else {
 								if (disguise.mob == MobType.Pig) {
 									disguise.addSingleData("saddled");
 									
 									// Check for permissions
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".saddled")) {
 										// Pass the event
 										PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
 										plugin.getServer().getPluginManager().callEvent(ev);
 										if (ev.isCancelled()) return true;
 										
 										plugin.changeDisguise(player, disguise);
 										player.sendMessage(ChatColor.GOLD + "You have been disguised as a Saddled " + disguise.mob.name());
 										if (isConsole) {
 											sender.sendMessage(player.getName() + " was disguised as a Saddled " + disguise.mob.name());
 										}
 									} else {
 										player.sendMessage(ChatColor.RED + "You do not have the permissions to disguise as a Saddled " + disguise.mob.name());
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "A " + disguise.mob.name() + " cannot be saddled.");
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("hold")) {
 				if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob.enderman.hold")) {
 					if (args.length > 1) {
 						String specification = remainingWords(args, 1);
 						Material type = Material.matchMaterial(specification);
 						if (type == null) {
 							sender.sendMessage(ChatColor.RED + "The block you specified could not be found");
 						} else {
 							if (type.isBlock()) {
 								if (plugin.disguiseDB.containsKey(player.getName())) {
 									Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 									if (disguise.mob != null && disguise.mob == MobType.Enderman) {
 										Byte currentHold = disguise.getHolding();
 										if (currentHold != null) {
 											disguise.data.remove("holding:" + currentHold);
 										}
 										disguise.addSingleData("holding:" + type.getId());
 										
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
 									sender.sendMessage(ChatColor.RED + "You must first be disguised");
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
 			} else if (args[0].equalsIgnoreCase("librarian") || args[0].equalsIgnoreCase("priest") || args[0].equalsIgnoreCase("blacksmith") || args[0].equalsIgnoreCase("butcher")) {
 				if (args.length > 1) { // New disguise
 					MobType type = MobType.fromString(args[1]);
 					if (type == null) {
 						sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
 					} else {
 						if (type == MobType.Villager) {
 							if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + type.name().toLowerCase() + ".occupation." + args[0].toLowerCase())) {
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
 							sender.sendMessage(ChatColor.RED + "A " + type.name() + " cannot be a " + args[0].toLowerCase());
 						}
 					}
 				} else { // Current mob
 					if (plugin.disguiseDB.containsKey(player.getName())) {
 						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
 						if (disguise.data != null && disguise.data.contains(args[0].toLowerCase())) {
 							sender.sendMessage(ChatColor.RED + "Already a " + args[0] + ".");
 						} else {
 							if (disguise.isPlayer()) {
 								sender.sendMessage(ChatColor.RED + "Player disguises do not get occupations");
 							} else {
 								if (disguise.mob == MobType.Villager) {
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
 										}
 									}
 									disguise.addSingleData(args[0].toLowerCase());
 									
 									// Check for permissions
 									if (isConsole || plugin.hasPermissions(player, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".occupation." + args[0].toLowerCase())) {
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
 									sender.sendMessage(ChatColor.RED + "A " + disguise.mob.name() + " cannot be a " + args[0].toLowerCase());
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
 					}
 				}
 			} else if (args[0].toLowerCase().startsWith("p") && !args[0].toLowerCase().startsWith("pi")) {
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
 								
 								disguise.setMob(null).setSingleData(args[1]);
 								
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
 						sender.sendMessage(ChatColor.RED + "You must specify the player to disguise as.");
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
 							disguise.setMob(type).setData(null);
 							
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
 	
 	// List Words After Specified Index
     public static String remainingWords(String[] wordArray, int startWord) {
     	String remaining = "";
     	for (int i=startWord; i<wordArray.length; i++) {
     		remaining = remaining.trim() + " " + wordArray[i];
     	}
     	return remaining.trim();
     }
 }
