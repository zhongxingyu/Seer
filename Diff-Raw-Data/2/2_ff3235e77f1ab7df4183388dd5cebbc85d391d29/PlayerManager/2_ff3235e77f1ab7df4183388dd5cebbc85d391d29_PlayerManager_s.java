 /**
  *  Name: PlayerManager.java
  *  Date: 20:44:47 - 8 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
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
  *  Filedescription:
  *  
  *  Manages playerlists etc.
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.managers;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 
 import me.lucasemanuel.survivalgamesmultiverse.Main;
 import me.lucasemanuel.survivalgamesmultiverse.events.PlayerRemoveEvent;
 import me.lucasemanuel.survivalgamesmultiverse.utils.ConsoleLogger;
 
 public class PlayerManager {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	private ConcurrentHashMap<String, PlayerList> playerlists;
 	
 	public PlayerManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger(instance, "PlayerManager");
 		
 		playerlists = new ConcurrentHashMap<String, PlayerList>();
 		
 		logger.debug("Initiated");
 	}
 	
 	public void addWorld(String worldname) {
 		logger.debug("Adding world - " + worldname);
 		playerlists.put(worldname, new PlayerList());
 	}
 
 	public void addPlayer(String worldname, final Player player) {
 		
 		if(playerlists.containsKey(worldname)) {
 			PlayerList playerlist = playerlists.get(worldname);
 			
 			playerlist.addPlayer(player);
 			
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				public void run() {
 					resetPlayer(player);
 				}
 			}, 5L);
 			
 			logger.debug("Added - " + player.getName() + " - to world - " + worldname);
 		}
 		else
 			logger.warning("Error! Tried to add player to non existing worldname! - " + worldname);
 	}
 
 	@SuppressWarnings("deprecation")
 	public void resetPlayer(Player player) {
 		
 		logger.debug("Resetting player: " + player.getName());
 		
 		if(!player.hasPermission("survivalgames.ignore.clearinv")){
 			PlayerInventory inventory = player.getInventory();
 			
 			inventory.clear();
 			
 			inventory.setHelmet(null);
 			inventory.setChestplate(null);
 			inventory.setLeggings(null);
 			inventory.setBoots(null);
 		
 			
 			if(plugin.getConfig().getBoolean("halloween.enabled"))
 				player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
 			
 			// Doesn't work without this!
 			player.updateInventory();
 		}
 		
 		for(PotionEffect potion : player.getActivePotionEffects()) {
 			player.removePotionEffect(potion.getType());
 		}
 		
 		player.setHealth(20);
 		player.setFoodLevel(20);
 		player.setLevel(0);
 		player.setTotalExperience(0);
 	}
 
 	public boolean isInGame(Player player) {
 		
 		for(PlayerList playerlist : playerlists.values()) {
 			if(playerlist.containsPlayer(player))
 				return true;
 		}
 		
 		return false;
 	}
 
 	public void removePlayer(String worldname, Player player) {
 		
 		if(playerlists.containsKey(worldname)) {
 			
 			PlayerList playerlist = playerlists.get(worldname);
 			
 			if(playerlist.removePlayer(player) == false)
 				logger.debug("Tried to remove player from world where he was not listed! Worldname = " + worldname + " - Playername = " + player.getName());
 			else
				plugin.getServer().getPluginManager().callEvent(new PlayerRemoveEvent(player.getName()));
 		}
 		else
 			logger.warning("Tried to remove player '" + player.getName() + "' from incorrect world '" + worldname + "'!");
 	}
 
 	public boolean isGameOver(World world) {
 		
 		Player[] playerlist = getPlayerList(world.getName());
 		
 		if(playerlist != null && playerlist.length <= 1) {
 			return true;
 		}
 		
 		return false;
 	}
 
 	public Player getWinner(World world) {
 		
 		if(isGameOver(world)) {
 			
 			Player[] playerlist = getPlayerList(world.getName());
 			
 			if(playerlist != null && playerlist.length == 1) {
 				return playerlist[0];
 			}
 		}
 		
 		return null;
 	}
 
 	public int getPlayerAmount(String worldname) {
 		
 		if(playerlists.containsKey(worldname)) {
 			return playerlists.get(worldname).getAmountOfPlayers();
 		}
 		
 		return 0;
 	}
 
 	private void clearList(String worldname) {
 		playerlists.get(worldname).clear();
 	}
 
 	public void killAndClear(String worldname) {
 		
 		logger.debug("Initated killAndClear on world: " + worldname);
 		
 		Player[] playerlist = playerlists.get(worldname).toArray();
 		
 		for(Player player : playerlist) {
 			
 			if(player != null) {
 				resetPlayer(player);
 				player.setHealth(0);
 			}
 			else
 				logger.warning("Tried to reset/kill null player!");
 		}
 		
 		clearList(worldname);
 	}
 
 	public Player[] getPlayerList(String worldname) {
 		
 		logger.debug("getPlayerList called for world: " + worldname);
 		
 		if(playerlists.containsKey(worldname)) {
 			logger.debug("Returning player array.");
 			return playerlists.get(worldname).toArray();
 		}
 		else {
 			logger.debug("Returning null!");
 			return null;
 		}
 	}
 }
 
 class PlayerList {
 	
 	private Set<Player> players;
 	
 	public PlayerList() {
 		players = new HashSet<Player>();
 	}
 	
 	public boolean containsPlayer(Player player) {
 		return players.contains(player);
 	}
 
 	public boolean addPlayer(Player player) {
 		return players.add(player);
 	}
 	
 	public boolean removePlayer(Player player) {
 		return players.remove(player);
 	}
 	
 	public int getAmountOfPlayers() {
 		return players.size();
 	}
 	
 	public void clear() {
 		players.clear();
 	}
 	
 	// It didn't let me use the set.toArray() and parse like (Player[]) set.toArray()
 	public Player[] toArray() {
 		
 		Player[] array = new Player[players.size()];
 		
 		int i = 0;
 		for(Player player : players) {
 			array[i] = player;
 			i++;
 		}
 		
 		return array;
 	}
 }
