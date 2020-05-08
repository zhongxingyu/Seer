 package com.skyost.jail;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.skyost.jail.tasks.ReleasePlayer;
 import com.skyost.jail.util.Utils;
 
 public class BukkitJail extends JavaPlugin {
 	
 	private static ConfigFile config;
 	
 	public void onEnable() {
 		try {
 			Bukkit.getPluginManager().registerEvents(new Listeners(), this);
 			config = new ConfigFile(this);
 			config.init();
 			if(Bukkit.getWorld(config.Jail_World) == null) {
 				Bukkit.createWorld(new WorldCreator(config.Jail_World));
 			}
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public static final boolean isJailed(Player player) {
 		if(config.JailedPlayers.contains(player.getName())) {
 			return true;
 		}
 		return false;
 	}
 	
 	public static final ConfigFile getBukkitJailConfig() {
 		return config;
 	}
 	
 	public static final Location getJailLocation() {
 		return new Location(Bukkit.getWorld(config.Jail_World), config.Jail_X, config.Jail_Y, config.Jail_Z, config.Jail_Yaw, config.Jail_Pitch);
 	}
 	
 	@Override
 	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
 		if(args.length >= 1) {
 			Player player = Bukkit.getPlayer(args[0]);
 			String message;
 			if(cmd.getName().equalsIgnoreCase("jail")) {
 				if(!config.JailedPlayers.contains(args[0])) {
 					try {
 						config.JailedPlayers.add(args[0]);
 						config.save();
 						message = config.JailedMessages_1.replaceAll("/player/", player.getName());
 						sender.sendMessage(message);
 						if(player != null && player.isOnline()) {
 							player.getInventory().setContents(new ItemStack[] {
 								new ItemStack(Material.AIR)	
 							});
 							player.teleport(new Location(Bukkit.getWorld(config.Jail_World), config.Jail_X, config.Jail_Y, config.Jail_Z, config.Jail_Yaw, config.Jail_Pitch));
 							player.sendMessage(config.JailedMessages_2);
 						}
						if(args.length == 2) {
							if(Utils.isNumeric(args[1])) {
								int time = Integer.parseInt(args[1]);
								message = config.JailedMessages_10.replaceAll("/n/", String.valueOf(Utils.roundTime(time)));
								message = message.replaceAll("/u/", Utils.getTimeUnit(time));
								player.sendMessage(message);
								Bukkit.getScheduler().runTaskLater(this, new ReleasePlayer(player.getName()), time * 20);
							}
							else {
								message = message.replaceAll("/reason/", args[1]);
								player.sendMessage(message);
							}
							return true;
						}
 						if(args.length >= 2) {
 							message = config.JailedMessages_9.replaceAll("/player/", sender.getName());
 							if(Utils.isNumeric(args[args.length - 1])) {
 								String reason = Utils.ArrayToStr(args, args.length - 1);
 								message = message.replaceAll("/reason/", reason.substring(args[0].length() + 1));
 								player.sendMessage(message);
 								int time = Integer.parseInt(args[args.length - 1]);
 								message = config.JailedMessages_10.replaceAll("/n/", String.valueOf(Utils.roundTime(time)));
 								message = message.replaceAll("/u/", Utils.getTimeUnit(time));
 								player.sendMessage(message);
 								Bukkit.getScheduler().runTaskLater(this, new ReleasePlayer(player.getName()), time * 20);
 							}
 							else {
 								String reason = Utils.ArrayToStr(args, args.length - 1);
 								message = message.replaceAll("/reason/", reason.substring(args[0].length() + 1));
 								player.sendMessage(message);
 							}
							return true;
 						}
 					}
 					catch(Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 				else {
 					sender.sendMessage(config.JailedMessages_3);
 				}
 			}
 			else if(cmd.getName().equalsIgnoreCase("release")) {
 				if(config.JailedPlayers.contains(args[0])) {
 					try {
 						config.JailedPlayers.remove(args[0]);
 						config.save();
 						message = config.JailedMessages_4.replaceAll("/player/", player.getName());
 						sender.sendMessage(message);
 						if(player != null && player.isOnline()) {
 							player.teleport(Utils.getMainWorld().getSpawnLocation());
 							player.sendMessage(config.JailedMessages_5);
 						}
 					}
 					catch(Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 				else {
 					sender.sendMessage(config.JailedMessages_6);
 				}
 			}
 		}
 		else {
 			if(cmd.getName().equalsIgnoreCase("setjail")) {
 				if(sender instanceof Player) {
 					try {
 						Player player = (Player)sender;
 						Location loc = player.getLocation();
 						config.Jail_World = loc.getWorld().getName();
 						config.Jail_X = loc.getBlockX();
 						config.Jail_Y = loc.getBlockY();
 						config.Jail_Z = loc.getBlockZ();
 						config.Jail_Yaw = Math.round(loc.getYaw());
 						config.Jail_Pitch = Math.round(loc.getPitch());
 						config.save();
 						sender.sendMessage(config.JailedMessages_11);
 					}
 					catch(Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 				else {
 					sender.sendMessage(ChatColor.RED + "Please do this from the game !");
 				}
 			}
 			else {
 				sender.sendMessage(config.JailedMessages_7);
 			}
 		}
 		return true;
 	}
 }
