 package au.com.addstar.truehardcore;
 /*
 * TrueHardcore
 * Copyright (C) 2013 add5tar <copyright at addstar dot com dot au>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 import java.util.ArrayList;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import au.com.addstar.truehardcore.HardcorePlayers.HardcorePlayer;
 
 public class CommandTH implements CommandExecutor {
 	private TrueHardcore plugin;
 	
 	public CommandTH(TrueHardcore instance) {
 		plugin = instance;
 	}
 	
 	/*
 	 * Handle the /truehardcore command
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		String action = "";
 		if (args.length > 0) {
 			action = args[0].toUpperCase();
 		}
 
 		if (action.equals("PLAY")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.use")) { return true; }
 			}
 
 			if (args.length > 1) {
 				World world = plugin.getServer().getWorld(args[1]);
 				if (world == null) {
 					sender.sendMessage(ChatColor.RED + "Error: Unknown world!");
 					return true;
 				}
 				
 				if (plugin.IsHardcoreWorld(world)) {
 					plugin.PlayGame(world.getName(), (Player) sender);
 				} else {
 					sender.sendMessage(ChatColor.RED + "Error: That is not a hardcore world!");
 				}
 			} else {
 				sender.sendMessage(ChatColor.YELLOW + "Usage: /th play <world>");
 			}
 		}
 		else if (action.equals("LEAVE")) {
 			plugin.LeaveGame((Player) sender);
 		}
 		else if (action.equals("INFO")) {
 			HardcorePlayer hcp = null;
 			if (args.length == 1) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.info")) { return true; }
 
 					Player player = (Player) sender;
 					hcp = plugin.HCPlayers.Get(player);
					if (hcp != null) {
						hcp.updatePlayer(player);
					} else {
						sender.sendMessage(ChatColor.RED + "You must be in the hardcore world to use this command");
					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Usage: /th info <player> [world]");
 				}
 			}
 			else if (args.length == 2) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.info.other")) { return true; }
 				}
 				Player player = (Player) plugin.getServer().getPlayer(args[1]);
 				if (player != null) {
 					hcp = plugin.HCPlayers.Get(player);
 					if (plugin.IsHardcoreWorld(player.getWorld())) {
 						hcp.updatePlayer(player);
					} else {
						sender.sendMessage(ChatColor.RED + "Error: Unknown player!");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Unknown player");
 				}
 			}
 			else if (args.length == 3) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.info.other")) { return true; }
 				}
 				hcp = plugin.HCPlayers.Get(args[2], args[1]);
				if (hcp != null) {
					Player player = (Player) plugin.getServer().getPlayer(args[2]);
					if (player != null) {
						if (plugin.IsHardcoreWorld(player.getWorld())) {
							if (args[1] == player.getWorld().getName()) {
								hcp.updatePlayer(player);
							}
 						}
 					}
				} else {
					sender.sendMessage(ChatColor.RED + "Error: Unknown player!");
 				}
 			}
 			
 			if (hcp != null) {
 				sender.sendMessage(ChatColor.GREEN + "Hardcore player information:");
 				sender.sendMessage(ChatColor.YELLOW + "Player: "        + ChatColor.AQUA + hcp.getPlayerName());
 				sender.sendMessage(ChatColor.YELLOW + "World: "         + ChatColor.AQUA + hcp.getWorld());
 				sender.sendMessage(ChatColor.YELLOW + "State: "         + ChatColor.AQUA + hcp.getState());
 				sender.sendMessage(ChatColor.YELLOW + "Current Level: " + ChatColor.AQUA + hcp.getLevel());
 				sender.sendMessage(ChatColor.YELLOW + "Total Score: "   + ChatColor.AQUA + hcp.getScore());
 				sender.sendMessage(ChatColor.YELLOW + "Total Deaths: "  + ChatColor.AQUA + hcp.getDeaths());
 				sender.sendMessage(ChatColor.YELLOW + "Top Score: "     + ChatColor.AQUA + hcp.getTopScore());
 			}
 		}
 		else if (action.equals("DUMP")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.dump")) { return true; }
 			}
 			for (String key : plugin.HCPlayers.AllRecords().keySet()) {
 				HardcorePlayer hcp = plugin.HCPlayers.Get(key);
 				sender.sendMessage(Util.padRight(key, 30) + " " + hcp.getState());
 			}
 		}
 		else if (action.endsWith("LIST")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.list")) { return true; }
 			}
 			
 			for (String w : plugin.HardcoreWorlds) {
 				World world = plugin.getServer().getWorld(w);
 				if ((world != null) && (world.getPlayers().size() > 0)) {
 					ArrayList<String> players = new ArrayList<String>();
 					for (Player p : world.getPlayers()) {
 						players.add(p.getName());
 					}
 					sender.sendMessage(ChatColor.YELLOW + world.getName() + ": " + ChatColor.AQUA + StringUtils.join(players, ", "));
 				}
 			}
 		}
 		else if (action.equals("SAVE")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.save")) { return true; }
 			}
 			plugin.SaveAllPlayers();
 		}
 		else {
 			sender.sendMessage(ChatColor.LIGHT_PURPLE + "TrueHardcore Commands:");
 			sender.sendMessage(ChatColor.AQUA + "/th play   " + ChatColor.YELLOW + ": Start or resume your hardcore game");
 			sender.sendMessage(ChatColor.AQUA + "/th leave  " + ChatColor.YELLOW + ": Leave the hardcore game (progress is saved)");
 		}
 		
 		return true;
 	}
 }
