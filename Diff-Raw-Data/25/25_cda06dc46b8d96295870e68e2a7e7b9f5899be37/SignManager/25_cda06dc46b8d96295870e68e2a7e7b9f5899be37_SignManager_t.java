 /**
  *  Name: SignManager.java
  *  Date: 21:35:06 - 9 nov 2012
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
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.managers;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import me.lucasemanuel.survivalgamesmultiverse.Main;
 import me.lucasemanuel.survivalgamesmultiverse.managers.StatusManager.GameFlag;
 import me.lucasemanuel.survivalgamesmultiverse.utils.ConsoleLogger;
 import me.lucasemanuel.survivalgamesmultiverse.utils.SerializedLocation;
 
 public class SignManager {
 	
 	private final Main plugin;
 	private final ConsoleLogger logger;
 	
 	private HashMap<Block, String> signs;
 	
 	public SignManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger("SignManager");
 		
 		logger.debug("Initiated");
 	}
 
 	public void loadsigns() {
 		
 		logger.debug("Loading signlocations...");
 		
 		this.signs = new HashMap<Block, String>();
 		
		HashMap<String, String> locations = plugin.getSQLiteConnector().getSignlocations();
 		
 		if(locations != null && locations.size() > 0) {
 			for(Entry<String, String> entry : locations.entrySet()) {
 				
 				final Location l = SerializedLocation.deserializeString(entry.getKey());
 				
 				Block block = l.getBlock();
 				
 				if(block != null) {
 					if(block.getType().equals(Material.SIGN_POST) 
 							|| block.getType().equals(Material.WALL_SIGN)) {
 						signs.put(block, entry.getValue());
 					}
 					else {
 						logger.warning("Loaded block not a sign! Material: " + block.getType());
 						
 						new BukkitRunnable() {
 							public void run() {
								plugin.getSQLiteConnector().removeSign(new SerializedLocation(l));
 							}
 						}.runTaskAsynchronously(plugin);
 					}
 				}
 				else
 					logger.warning("Loaded block is null!");
 			}
 		}
 		else {
 			logger.debug("No saved signs!");
 		}
 	}
 	
 	private void saveSigns() {
 		
 		logger.debug("Saving signlocations...");
 		
 		final HashMap<SerializedLocation, String> locations = new HashMap<SerializedLocation, String>();
 		
 		for(Entry<Block, String> entry : signs.entrySet()) {
 			locations.put(new SerializedLocation(entry.getKey().getLocation()), entry.getValue());
 		}
 		
 		new BukkitRunnable() {
 			public void run() {
				plugin.getSQLiteConnector().saveSignLocations(locations);
 			}
 		}.runTaskAsynchronously(plugin);
 	}
 
 	public void updateSigns() {
 		logger.debug("Updating signs");
 		for(String worldname : signs.values()) {
 			updateInfoSign(worldname);
 		}
 	}
 
 	private void updateInfoSign(String worldname) {
 		
 		Block b = getSign(worldname);
 		
 		if(b != null && (b.getType().equals(Material.SIGN_POST) 
 				|| b.getType().equals(Material.WALL_SIGN))) {
 			
 			Sign sign = (Sign) b.getState();
 			
 			String output = "";
 			
 			GameFlag flag = plugin.getStatusManager().getStatusFlag(worldname);
 			switch(flag) {
				case WAITING: output = ChatColor.GREEN + plugin.getLanguageManager().getString("signs.waiting"); break;
				case STARTED: output = ChatColor.GOLD  + plugin.getLanguageManager().getString("signs.started"); break;
				case FROZEN:  output = ChatColor.RED   + plugin.getLanguageManager().getString("signs.frozen");  break;
 				default:      output = ChatColor.RED   + "ERROR"; break;
 			}
 					
 			int amount = plugin.getPlayerManager().getPlayerAmount(worldname);
 				
 			sign.setLine(0, worldname);
 			sign.setLine(1, output);
 			sign.setLine(2, plugin.getLanguageManager().getString("signs.playersIngame"));
 			sign.setLine(3, "" + ChatColor.WHITE + amount + 
 					"/" + plugin.getLocationManager().getLocationAmount(worldname));
 			
 			logger.debug("Updating sign :: " + worldname + " - " + flag + " - " + amount);
 			sign.update();
 		}
 		else
 			logger.warning("Sign is null! Worldname: " + worldname);
 	}
 
 	private Block getSign(String worldname) {
 		
 		for(Entry<Block, String> entry : signs.entrySet()) {
 			if(entry.getValue().equals(worldname))
 				return entry.getKey();
 		}
 		
 		return null;
 	}
 
 	public String getGameworldName(Block b) {
 		
 		for(Entry<Block, String> entry : signs.entrySet()) {
 			if(entry.getKey().equals(b))
 				return entry.getValue();
 		}
 		
 		return null;
 	}
 
 	public void registerSign(Block b) {
 		if(b != null && (b.getType().equals(Material.SIGN_POST) 
 				|| b.getType().equals(Material.WALL_SIGN))) {
 			
 			Sign sign = (Sign) b.getState();
 			
 			String firstline  = sign.getLine(0);
 			String secondline = sign.getLine(1);
 			
 			if(firstline.equalsIgnoreCase("[sginfo]") && secondline != null) {
 				
 				World world = Bukkit.getWorld(secondline);
 				
 				if(world != null) {
 					signs.put(sign.getBlock(), world.getName());
 					updateInfoSign(world.getName());
 					saveSigns();
 				}
 				else
 					logger.warning("Tried to register sign for null world! Worldname used: " + secondline);
 			}
 		}
 	}
 }
