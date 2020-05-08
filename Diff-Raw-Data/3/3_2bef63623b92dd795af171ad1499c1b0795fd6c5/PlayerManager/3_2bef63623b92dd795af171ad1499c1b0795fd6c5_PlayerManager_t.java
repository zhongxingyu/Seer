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
 
 package se.lucasarnstrom.survivalgamesmultiverse.managers;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import se.lucasarnstrom.survivalgamesmultiverse.Main;
 import se.lucasarnstrom.survivalgamesmultiverse.events.PlayerAddEvent;
 import se.lucasarnstrom.survivalgamesmultiverse.events.PlayerRemoveEvent;
 import se.lucasarnstrom.survivalgamesmultiverse.utils.ConsoleLogger;
 import se.lucasarnstrom.survivalgamesmultiverse.utils.Serialize;
 
 public class PlayerManager {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	private ConcurrentHashMap<String, PlayerList> playerlists;
 	
 	public PlayerManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger("PlayerManager");
 		
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
 			
 			plugin.getServer().getPluginManager().callEvent(new PlayerAddEvent(player));
 			
 			logger.debug("Added - " + player.getName() + " - to world - " + worldname);
 		}
 		else
 			logger.warning("Error! Tried to add player to non existing worldname! - " + worldname);
 	}
 
 	@SuppressWarnings("deprecation")
 	public void resetPlayer(Player player) {
 		
 		logger.debug("Resetting player: " + player.getName());
 		
 		if(!player.hasPermission("survivalgames.ignore.clearinv")){
 			backupInventory(player);
 			clearInventory(player);
 			
 			if(plugin.getConfig().getBoolean("halloween.enabled"))
 				player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
 			
 			player.updateInventory();
 		}
 		
 		for(PotionEffect potion : player.getActivePotionEffects()) {
 			player.removePotionEffect(potion.getType());
 		}
 		
		// Giving setHealth an integer to make it work with older versions of bukkit
		player.setHealth((int)20);
 		player.setFoodLevel(20);
 		player.setLevel(0);
 		player.setTotalExperience(0);
 	}
 	
 	public void backupInventory(Player p) {
 		if(plugin.getConfig().getBoolean("backup.inventories")) {
 			PlayerInventory inv = p.getInventory();
 			ItemStack[] c = inv.getContents();
 			
 			ItemStack[] contents = new ItemStack[p.getInventory().getSize() + 4];
 			
 			for(int i = 0 ; i < c.length ; i++) {
 				contents[i] = c[i];
 			}
 			
 			contents[c.length + 3] = inv.getHelmet();
 			contents[c.length + 2] = inv.getChestplate();
 			contents[c.length + 1] = inv.getLeggings();
 			contents[c.length + 0] = inv.getBoots();
 			
 			final String serial = Serialize.inventoryToString(contents);
 			final String name   = p.getName();
 			
 			new BukkitRunnable() {
 				@Override
 				public void run() {
 					plugin.getSQLiteConnector().saveInventory(name, serial);
 				}
 			}.runTaskAsynchronously(plugin);
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void restoreInventory(Player p) {
 		String serial = plugin.getSQLiteConnector().loadInventory(p.getName());
 		if(serial == null) return;
 		
 		Map<Integer, ItemStack> map = Serialize.stringToInventory(serial);
 		
 		if(map != null && map.size() > 0) {
 			PlayerInventory inv = p.getInventory();
 			
 			clearInventory(p);
 			
 			for(Entry<Integer, ItemStack> entry : map.entrySet()) {
 				int key = entry.getKey().intValue();
 				int size = inv.getSize();
 				
 				if     (key <= size)     inv.setItem(key, entry.getValue());
 				else if(key == size + 3) inv.setHelmet(entry.getValue());
 				else if(key == size + 2) inv.setChestplate(entry.getValue());
 				else if(key == size + 1) inv.setLeggings(entry.getValue());
 				else if(key == size + 0) inv.setBoots(entry.getValue());
 			}
 			
 			p.updateInventory();
 		}
 	}
 	
 	public void clearInventory(Player p) {
 		PlayerInventory inv = p.getInventory();
 		inv.clear();
 		inv.setHelmet(null);
 		inv.setChestplate(null);
 		inv.setLeggings(null);
 		inv.setBoots(null);
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
 			else {
 				plugin.getServer().getPluginManager().callEvent(new PlayerRemoveEvent(player));
 			}
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
 				clearInventory(player);
 				player.setHealth(0D);
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
 	
 	private Set<String> players;
 	
 	public PlayerList() {
 		players = new HashSet<String>();
 	}
 	
 	public boolean containsPlayer(Player player) {
 		return players.contains(player.getName());
 	}
 
 	public boolean addPlayer(Player player) {
 		return players.add(player.getName());
 	}
 	
 	public boolean removePlayer(Player player) {
 		return players.remove(player.getName());
 	}
 	
 	public int getAmountOfPlayers() {
 		return players.size();
 	}
 	
 	public void clear() {
 		players.clear();
 	}
 	
 	public Player[] toArray() {
 		
 		Player[] array = new Player[players.size()];
 		
 		Iterator<String> iterator = players.iterator();
 		int i = 0;
 		while(iterator.hasNext()) {
 			Player p = Bukkit.getPlayerExact(iterator.next());
 			if(p != null) {
 				array[i] = p;
 				i++;
 			}
 			else
 				iterator.remove();
 		}
 		
 		return array;
 	}
 }
