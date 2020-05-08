 /** TheWalls2: The Walls 2 plugin.
   * Copyright (C) 2012  Andrew Stevanus (Hoot215) <hoot893@gmail.com>
   * 
   * This program is free software: you can redistribute it and/or modify
   * it under the terms of the GNU Affero General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   * 
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU Affero General Public License for more details.
   * 
   * You should have received a copy of the GNU Affero General Public License
   * along with this program.  If not, see <http://www.gnu.org/licenses/>.
   */
 
 package me.Hoot215.TheWalls2;
 
 import java.io.IOException;
 import java.util.Set;
 
 import me.Hoot215.TheWalls2.metrics.Metrics;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TheWalls2 extends JavaPlugin {
 	public static String worldName;
 	private TheWalls2PlayerQueue queue;
 	private TheWalls2GameTeams teams;
 	private TheWalls2GameList gameList;
 	private TheWalls2RespawnQueue respawnQueue;
 	private TheWalls2LocationData locData;
 	private TheWalls2World wallsWorld;
 	private Listener playerListener;
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("thewalls")) {
 			if (args.length == 0) {
 				if (sender.hasPermission("thewalls2.command.thewalls")) {
 					sender.sendMessage(ChatColor.GREEN + "****" + ChatColor.BOLD + ChatColor.AQUA + " The Walls 2" + ChatColor.RESET + ChatColor.GREEN + " ****");
 					sender.sendMessage(ChatColor.YELLOW + "/thewalls" + ChatColor.WHITE + " - Displays TheWalls2 help");
 					sender.sendMessage(ChatColor.YELLOW + "/thewalls join" + ChatColor.WHITE + " - Joins the game queue");
 					sender.sendMessage(ChatColor.YELLOW + "/thewalls leave" + ChatColor.WHITE + " - Leaves the game queue");
 					sender.sendMessage(ChatColor.YELLOW + "/thewalls team <1-4>" + ChatColor.WHITE + " - Joins a team");
 					sender.sendMessage(ChatColor.RED + "/thewalls start" + ChatColor.WHITE + " - Starts a game");
 					sender.sendMessage(ChatColor.RED + "/thewalls restoreworld" + ChatColor.WHITE + " - Restores the world");
 					return true;
 				}
 				sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 				return true;
 			}
 			else if (args.length == 1) {
 				if (args[0].equalsIgnoreCase("join")) {
 					Player player;
 					
 					if (sender instanceof Player) {
 						player = (Player) sender;
 					}
 					else {
 						sender.sendMessage(ChatColor.RED + "This command can only be run as a player");
 						return true;
 					}
 					
 					if (player.hasPermission("thewalls2.command.thewalls.join")) {
 						if (gameList != null) {
 							player.sendMessage(ChatColor.RED + "A game is already in progress!");
 							return true;
 						}
 						
 						if (TheWalls2World.isRestoring) {
 							player.sendMessage(ChatColor.RED + "The world is being restored");
 							return true;
 						}
 						
 						if (!queue.isInQueue(player.getName())) {
 							
 							if (queue.getSize() < 12) {
 								queue.addPlayer(player.getName(), player.getLocation());
 								player.teleport(locData.getLobby());
 								player.sendMessage(ChatColor.GREEN + "Successfully joined the game queue!");
 							}
 							else {
 								player.sendMessage(ChatColor.RED + "The game queue is full!");
 							}
 							return true;
 						}
 						player.sendMessage(ChatColor.RED + "You are already in the queue!");
 						return true;
 					}
 					player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("leave")) {
 					Player player;
 					
 					if (sender instanceof Player) {
 						player = (Player) sender;
 					}
 					else {
 						sender.sendMessage(ChatColor.RED + "This command can only be run as a player");
 						return true;
 					}
 					
 					if (player.hasPermission("thewalls2.command.thewalls.leave")) {
 						if (gameList != null) {
 							player.sendMessage(ChatColor.RED + "A game is already in progress!");
 							return true;
 						}
 						
 						if (queue.isInQueue(player.getName())) {
 							queue.removePlayer(player.getName(), true);
 							if (teams.isInTeam(player.getName())) {
 								teams.removePlayer(player.getName());
 							}
 							player.sendMessage(ChatColor.GREEN + "Successfully left the queue!");
 							return true;
 						}
 						player.sendMessage(ChatColor.RED + "You are not in the queue!");
 						return true;
 					}
 					player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("team")) {
 					Player player;
 					
 					if (sender instanceof Player) {
 						player = (Player) sender;
 					}
 					else {
 						sender.sendMessage(ChatColor.RED + "This command can only be run as a player");
 						return true;
 					}
 					
 					if (player.hasPermission("thewalls2.command.thewalls.team")) {
 						sender.sendMessage(ChatColor.YELLOW + "/thewalls team <1-4>" + ChatColor.WHITE + " - Joins a team");
 						return true;
 					}
 					player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("start")) {
 					if (sender.hasPermission("thewalls2.command.thewalls.start")) {
 						if (gameList != null) {
 							sender.sendMessage(ChatColor.RED + "A game is already in progress!");
 							return true;
 						}
 						
 						if (TheWalls2World.isRestoring) {
 							sender.sendMessage(ChatColor.RED + "The world is being restored");
 							return true;
 						}
 						
 						if (startGame()) {
 							sender.sendMessage(ChatColor.GREEN + "Game started!");
 						}
 						else
 							sender.sendMessage(ChatColor.RED + "There must be at least 2 teams!");
 						return true;
 					}
 					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("stop")) {
					if (sender.hasPermission("thewalls2.command.thewalls.start")) {
 						if (gameList == null) {
 							sender.sendMessage(ChatColor.RED + "There is no game currently in progress!");
 							return true;
 						}
 						
 						if (TheWalls2World.isRestoring) {
 							sender.sendMessage(ChatColor.RED + "The world is being restored");
 							return true;
 						}
 						
 						teams.reset();
 						queue.reset(true);
 						gameList = null;
 						restoreBackup();
 					}
 					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("restoreworld")) {
 					if (sender.hasPermission("thewalls2.command.thewalls.restoreWorld")) {
 						sender.sendMessage(ChatColor.GREEN + "Restoring world...");
 						restoreBackup();
 						return true;
 					}
 					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("debug")) {
 					System.out.println("Queue:");
 					for (String s : queue.getList()) {
 						System.out.println(s);
 					}
 					System.out.println("Team1:");
 					for (String s : teams.getTeam(1)) {
 						System.out.println(s);
 					}
 					System.out.println("Team2:");
 					for (String s : teams.getTeam(2)) {
 						System.out.println(s);
 					}
 					System.out.println("Team3:");
 					for (String s : teams.getTeam(3)) {
 						System.out.println(s);
 					}
 					System.out.println("Team4:");
 					for (String s : teams.getTeam(4)) {
 						System.out.println(s);
 					}
 					System.out.println("GameList:");
 					for (String s : gameList.getList()) {
 						System.out.println(s);
 					}
 					return true;
 				}
 				return false;
 			}
 			else if (args.length == 2) {
 				if (args[0].equalsIgnoreCase("team")) {
 					Player player;
 					
 					if (sender instanceof Player) {
 						player = (Player) sender;
 					}
 					else {
 						sender.sendMessage(ChatColor.RED + "This command can only be run as a player");
 						return true;
 					}
 					
 					if (player.hasPermission("thewalls2.command.thewalls.team")) {
 						if (queue.isInQueue(player.getName())) {
 							int i;
 							
 							try {
 								i = Integer.parseInt(args[1]);
 							} catch(NumberFormatException e) {
 								player.sendMessage(ChatColor.RED + "ERROR: Expected integer, got string!");
 								return false;
 							}
 							if (!teams.isInTeam(player.getName())) {
 								if (!teams.isTeamFull(i)) {
 									if (teams.addPlayerToTeam(i, player.getName())) {
 										player.sendMessage(ChatColor.GREEN + "Successfully joined team " + String.valueOf(i) + "!");
 										return true;
 									}
 									player.sendMessage(ChatColor.RED + "That team does not exist!");
 									return true;
 								}
 								player.sendMessage(ChatColor.RED + "That team does not exist or is full!");
 								return true;
 							}
 							player.sendMessage(ChatColor.RED + "You are already in a team!");
 							return true;
 						}
 						player.sendMessage(ChatColor.RED + "You are not in the queue!");
 						return true;
 					}
 					player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
 					return true;
 				}
 				return false;
 			}
 			return false;
 		}
 		return false;
 	}
 	
 	public boolean startGame() {
 		World world = getServer().getWorld(worldName);
 		
 		teams.cleanup();
 		
 		if (teams.getEmptyTeamCount() > 2)
 			return false;
 		gameList = new TheWalls2GameList(queue.getList());
 		
 		for (int t = 1; t < 5; t++) {
 			teleportTeamToGame(t, world);
 		}
 		
 		final Location loc = new Location(world, -781, 98, -58);
 		loc.getBlock().setType(Material.REDSTONE_TORCH_ON);
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			
 			public void run() {
 				loc.getBlock().setType(Material.AIR);
 			}
 		}, 20L);
 		
 		return true;
 	}
 	
 	public void teleportTeamToGame(int teamNumber, World world) {
 		int i = 0;
 		Set<String> team = teams.getTeam(teamNumber);
 		
 		if (team.size() == 0)
 			return;
 		
 		for (String s : team) {
 			Player player = getServer().getPlayer(s);
 			if (player == null)
 				continue;
 			player.teleport(locData.getSlot(teamNumber, i));
 			i++;
 		}
 	}
 	
 	public void checkIfGameIsOver() {
 		if (gameList.getPlayerCount() < 2) {
 			final String playerName = gameList.getLastPlayer();
 			Player player = getServer().getPlayer(playerName);
 			
 			getServer().broadcastMessage(ChatColor.YELLOW + playerName + ChatColor.GREEN + " has won The Walls 2!");
 			player.sendMessage(ChatColor.GOLD + "Congratulations! You have won The Walls 2!");
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				
 				public void run() {
 					Player futurePlayer = getServer().getPlayer(playerName);
 					if (futurePlayer != null) {
 						gameList.removeFromGame(playerName);
 						queue.removePlayer(playerName, true);
 						String[] prize = getConfig().getString("general.prize").split(":");
 						int prizeID = Integer.parseInt(prize[0]);
 						int prizeAmount = Integer.parseInt(prize[2]);
 						short prizeData = Short.parseShort(prize[1]);
 						ItemStack item = new ItemStack(prizeID, prizeAmount, prizeData);
 						futurePlayer.getWorld().dropItem(futurePlayer.getLocation(), item);
 					}
 					
 					teams.reset();
 					queue.reset(false);
 					gameList = null;
 					restoreBackup();
 				}
 			}, 100L);
 		}
 	}
 	
 	public void restoreBackup() {
 		TheWalls2World.restoreBackup(this, true);
 	}
 	
 	public TheWalls2PlayerQueue getQueue() {
 		return queue;
 	}
 	
 	public TheWalls2GameList getGameList() {
 		return gameList;
 	}
 	
 	public TheWalls2RespawnQueue getRespawnQueue() {
 		return respawnQueue;
 	}
 	
 	public TheWalls2LocationData getLocationData() {
 		return locData;
 	}
 	
 	public TheWalls2World getWorld() {
 		return wallsWorld;
 	}
 	
 	@Override
 	public void onDisable() {
 		if (gameList != null) {
 			System.out.println("[TheWalls2] Game interrupted! Restoring world...");
 			TheWalls2World.restoreBackup(this, false);
 		}
 		System.out.println("[TheWalls2] Unloading world...");
 		getServer().unloadWorld(worldName, false);
 		System.out.println(this + " is now disabled!");
 	}
 	
 	@Override
 	public void onEnable() {
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		worldName = getConfig().getString("general.world");
 		queue = new TheWalls2PlayerQueue(this);
 		teams = new TheWalls2GameTeams(queue);
 		TheWalls2World.createBackup();
 		System.out.println("[TheWalls2] Loading world...");
 		WorldCreator wc = new WorldCreator(worldName);
 		World world = getServer().createWorld(wc);
 		locData = new TheWalls2LocationData(world);
 		respawnQueue = new TheWalls2RespawnQueue(this);
 		String lobby = getConfig().getString("locations.lobby");
 		if (TheWalls2ConfigSetter.isLobbyDifferent(this, lobby)) {
 			TheWalls2ConfigSetter.updateLobbyLocation(this, lobby);
 		}
 		playerListener = new TheWalls2PlayerListener(this);
 		getServer().getPluginManager().registerEvents(playerListener, this);
 		
 		if (getConfig().getBoolean("timer.enabled")) {
 			long initialTime = getConfig().getLong("timer.initial-time");
 			long normalTime = getConfig().getLong("timer.normal-time");
 			new Thread(new TheWalls2Timer(this, initialTime, normalTime)).start();
 		}
 		
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		} catch (IOException e) {}
 		
 		System.out.println(this + " is now enabled!");
 	}
 }
