 /**
  *  Name: SignManager.java
  *  Date: 21:35:06 - 9 nov 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Description:
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
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import me.lucasemanuel.survivalgamesmultiverse.Main;
 import me.lucasemanuel.survivalgamesmultiverse.utils.ConsoleLogger;
 import me.lucasemanuel.survivalgamesmultiverse.utils.SLAPI;
 import me.lucasemanuel.survivalgamesmultiverse.utils.SerializedLocation;
 
 public class SignManager {
 	
 	private final Main plugin;
 	private final ConsoleLogger logger;
 	
 	private HashMap<Sign, String> signs;
 	
 	public SignManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger(instance, "SignManager");
 		
 		logger.debug("Initiated");
 	}
 
 	@SuppressWarnings("unchecked")
 	public synchronized void loadsigns() {
 		
 		logger.debug("Loading signlocations...");
 		
 		this.signs = new HashMap<Sign, String>();
 		
 		HashMap<SerializedLocation, String> tempmap = new HashMap<SerializedLocation, String>();
 		
 		try {
 			tempmap = (HashMap<SerializedLocation, String>) SLAPI.load(plugin.getDataFolder() + "/" + "signlocations.dat");
 		}
 		catch(Exception e) {
 			logger.severe("Error while loading signlocations! Message: " + e.getMessage());
 		}
 		
 		if(tempmap != null) {
 			logger.debug("Saved amount: " + tempmap.size());
 			
 			Block block = null;
 			
 			for(Entry<SerializedLocation, String> entry : tempmap.entrySet()) {
 				
 				block = entry.getKey().deserialize().getBlock();
 				
 				if(block != null) {
 					if(block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
 						signs.put((Sign) block.getState(), entry.getValue());
 					}
 					else
 						logger.warning("Loaded block not a sign! Material: " + block.getType());
 				}
 				else
 					logger.warning("Loaded block is null!");
 				
 				
 			}
 		}
 		else {
 			logger.debug("No saved signs!");
 		}
 	}
 	
 	private synchronized void saveSigns() {
 		
 		logger.debug("Saving signlocations...");
 		
 		final HashMap<SerializedLocation, String> tempmap = new HashMap<SerializedLocation, String>();
 		
 		for(Entry<Sign, String> entry : signs.entrySet()) {
 			tempmap.put(new SerializedLocation(entry.getKey().getLocation()), entry.getValue());
 		}
 		
 		Thread thread = new Thread() {
 			public void run() {
 				
 				try {
 					SLAPI.save(tempmap, plugin.getDataFolder() + "/" + "signlocations.dat");
 				}
 				catch(Exception e) {
 					logger.severe("Error while saving signlocations! Message: " + e.getMessage());
 				}
 				
 				logger.debug("Finished");
 			}
 		};
 		
 		thread.start();
 	}
 
 	public synchronized void updateSigns() {
 		for(String worldname : signs.values()) {
 			updateInfoSign(worldname);
 		}
 	}
 
 	private synchronized void updateInfoSign(String worldname) {
 		
 		Sign sign = getSign(worldname);
 		
 		if(sign != null) {
 			
 			String output = "";
 			
 			int status = plugin.getStatusManager().getStatus(worldname);
 			switch(status) {
 				case 0: output = ChatColor.GREEN + plugin.getLanguageManager().getString("signs.waiting"); break;
 				case 1: output = ChatColor.DARK_GREEN  + plugin.getLanguageManager().getString("signs.started"); break;
				case 2: output = ChatColor.RED + plugin.getLanguageManager().getString("signs.frozen"); break;
 			}
 					
 			Player[] playerlist = plugin.getPlayerManager().getPlayerList(worldname);
 				
 			sign.setLine(0, ChatColor.DARK_GREEN + worldname);
 			sign.setLine(1, output);
 			sign.setLine(2, plugin.getLanguageManager().getString("signs.playersIngame"));
 			sign.setLine(3, "" + ChatColor.WHITE + playerlist.length + 
 					"/" + plugin.getLocationManager().getLocationAmount(worldname));
 			
 			sign.update();
 		}
 		else
 			logger.warning("Sign is null! Worldname: " + worldname);
 	}
 
 	private synchronized Sign getSign(String worldname) {
 		
 		for(Entry<Sign, String> entry : signs.entrySet()) {
 			if(entry.getValue().equals(worldname))
 				return entry.getKey();
 		}
 		
 		return null;
 	}
 
 	public synchronized String getGameworldName(Sign sign) {
 		
 		for(Entry<Sign, String> entry : signs.entrySet()) {
 			if(entry.getKey().equals(sign))
 				return entry.getValue();
 		}
 		
 		return null;
 	}
 
 	public synchronized void registerSign(Sign sign) {
 		
 		if(sign != null) {
 			
 			String firstline  = sign.getLine(0);
 			String secondline = sign.getLine(1);
 			
 			if(firstline.equalsIgnoreCase("[sginfo]") && secondline != null) {
 				
 				World world = Bukkit.getWorld(secondline);
 				
 				if(world != null) {
 					signs.put(sign, world.getName());
 					updateInfoSign(world.getName());
 					saveSigns();
 				}
 				else
 					logger.warning("Tried to register sign for null world! Worldname used: " + secondline);
 			}
 		}
 	}
 }
