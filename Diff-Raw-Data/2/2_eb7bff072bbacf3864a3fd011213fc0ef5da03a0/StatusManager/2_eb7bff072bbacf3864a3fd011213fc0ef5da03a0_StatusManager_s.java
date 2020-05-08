 /**
  *  Name:    StatusManager.java
  *  Created: 23:59:55 - 7 jun 2013
  * 
  *  Author:  Lucas Arnstrm - LucasEmanuel @ Bukkit forums
  *  Contact: lucasarnstrom(at)gmail(dot)com
  *  
  *
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *
  *  Filedescription:
  *
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.managers;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitTask;
 
 import me.lucasemanuel.survivalgamesmultiverse.Main;
 import me.lucasemanuel.survivalgamesmultiverse.utils.ConsoleLogger;
 
 public class StatusManager {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	private HashMap<String, Game> games;
 	
 	public StatusManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger("StatusManager");
 		
 		logger.debug("Loading configured times.");
 		Game.countdown_first = plugin.getConfig().getInt("timeoutTillStart");
 		Game.countdown_arena = plugin.getConfig().getInt("timeoutTillArenaInSeconds");
 		Game.countdown_end   = plugin.getConfig().getInt("timeoutAfterArena");
 		
 		games = new HashMap<String, Game>();
 		
 		logger.debug("Initiated");
 	}
 	
 	public void addWorld(String worldname) {
 		logger.debug("Adding world: " + worldname);
 		int ptwf = plugin.getConfig().getInt("worlds." + worldname + ".players_to_wait_for");
 		games.put(worldname, new Game(plugin, worldname, ptwf));
 	}
 	
 	public boolean startPlayerCheck(final String worldname) {
 		if(games.containsKey(worldname)) {
 			
 			final Game game = games.get(worldname);
 			
 			game.setTask(plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
 				public void run() {
 					game.checkPlayers();
 				}
 			}, 0L, 200L));
 			return true;
 		}
 		else 
 			return false;
 	}
 	
 	public boolean activate(String worldname) {
 		if(games.containsKey(worldname)) {
 			games.get(worldname).activate();
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	public int getStatusFlag(String worldname) {
 		if(games.containsKey(worldname))
 			return games.get(worldname).getFlag();
 		else
 			return -1;
 	}
 	
 	public void reset(String worldname) {
 		if(games.containsKey(worldname)) {
 			games.get(worldname).reset();
 		}
 	}
 }
 
 class Game {
 	
 	private Main plugin;
 	
 	private final String worldname;
 	private BukkitTask task = null;
 	private int flag = 0;
 	
 	private long time_of_initiation = 0;
 	private boolean started_10 = false;
 	
 	private int players_to_wait_for;
 	
 	// Configured times
 	public static int countdown_first;
 	public static int countdown_arena;
 	public static int countdown_end;
 	
 	public Game(Main plugin, String worldname, int ptwf) {
 		this.plugin = plugin;
 		this.worldname = worldname;
 		players_to_wait_for = ptwf;
 	}
 	
 	public void checkPlayers() {
 		int playeramount = plugin.getPlayerManager().getPlayerAmount(worldname);
 		
 		if(playeramount >= players_to_wait_for) {
 			cancelTask();
 			
 			setTask(plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
 				public void run() {
 					startCounter();
 				}
 			}, 0L, 200L));
 		}
 		else if (playeramount == 0) 
 			reset();
 		else
 			plugin.getWorldManager().broadcast(Bukkit.getWorld(worldname), ChatColor.GRAY + plugin.getLanguageManager().getString("waitingForPlayers"));
 	}
 	
 	private void startCounter() {
 		
 		if(plugin.getPlayerManager().getPlayerAmount(worldname) < players_to_wait_for) {
 			setTask(plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
 				public void run() {
 					checkPlayers();
 				}
 			}, 0L, 200L));
 			return;
 		}
 		
 		if(time_of_initiation == 0) {
 			time_of_initiation = System.currentTimeMillis();
 		}
 			
 		int timepassed = (int) (System.currentTimeMillis() - time_of_initiation) / 1000;
 		
 		if(timepassed >= countdown_first) {
 			activate();
 		}
 		else if(timepassed >= countdown_first - 12 && !started_10) {
 			cancelTask();
 			started_10 = true;
 			
 			setTask(plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
 				public void run() {
 					startCounter();
 				}
 			}, 20L, 20L));
 		}
 		else
 			plugin.getWorldManager().broadcast(worldname, (countdown_first - timepassed) + " " + plugin.getLanguageManager().getString("timeleft"));
 	}
 	
 	public void activate() {
 		cancelTask();
 		flag = 1;
 		
 		plugin.getWorldManager().broadcast(worldname, plugin.getLanguageManager().getString("gamestarted"));
 		
 		plugin.getSignManager().updateSigns();
 		
 		long delay = countdown_arena * 20; delay = delay <= 100 ? delay : delay-100;
 		
 		setTask(plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 			public void run() {
 				
 				plugin.getWorldManager().broadcast(worldname, plugin.getLanguageManager().getString("broadcast_before_arena"));
 				
 				setTask(plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 					public void run() {
 						arena();
 					}
 				}, 100L));
 			}
 		}, delay));
 	}
 	
 	private void arena() {
 		cancelTask();
 		
 		plugin.getWorldManager().broadcast(worldname, plugin.getLanguageManager().getString("sendingEveryoneToArena"));
 		
 		Player[] playerlist = plugin.getPlayerManager().getPlayerList(worldname);
 		
 		for(Player p : playerlist) {
 			if(p != null && p.isOnline()) {
 				if(plugin.getLocationManager().tpToArena(p)) {
 					p.sendMessage(ChatColor.GOLD + plugin.getLanguageManager().getString("sentYouToArena"));
 				}
 				else {
					p.setHealth(0);
 					p.sendMessage(ChatColor.RED + plugin.getLanguageManager().getString("killedSendingArena"));
 				}
 			}
 			else
 				plugin.getPlayerManager().removePlayer(worldname, p);
 		}
 		
 		plugin.getWorldManager().broadcast(worldname, countdown_end + " " + plugin.getLanguageManager().getString("secondsTillTheGameEnds"));
 		
 		setTask(plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 			public void run() {
 				endTheGame();
 			}
 		}, (long) countdown_end * 20));
 	}
 	
 	private void endTheGame() {
 		plugin.resetWorld(Bukkit.getWorld(worldname));
 	}
 	
 	public int getFlag() {
 		return flag;
 	}
 	
 	public void setTask(BukkitTask task) {
 		if(task != null)
 			cancelTask();
 		
 		this.task = task;
 	}
 	
 	public void cancelTask() {
 		if(task != null) {
 			task.cancel();
 			task = null;
 		}
 	}
 	
 	public void reset() {
 		cancelTask();
 		flag = 0;
 		started_10 = false;
 		time_of_initiation = 0;
 	}
 }
