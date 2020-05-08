 /**
  *  Name: Main.java
  *  Date: 10:39:48 - 8 sep 2012
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
  *  This is the object that gets initialized by the server.
  *  
  *  The onEnable() method initializes all managers, loads all worlds, registers all commands etc.
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse;
 
 import java.io.IOException;
 
 import me.desht.dhutils.nms.NMSHelper;
 import me.lucasemanuel.survivalgamesmultiverse.listeners.Blocks;
 import me.lucasemanuel.survivalgamesmultiverse.listeners.Misc;
 import me.lucasemanuel.survivalgamesmultiverse.listeners.Players;
 import me.lucasemanuel.survivalgamesmultiverse.listeners.Worlds;
 import me.lucasemanuel.survivalgamesmultiverse.managers.ChestManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.LanguageManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.LocationManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.PlayerManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.SignManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.StatsManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.StatusManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.WorldManager;
 import me.lucasemanuel.survivalgamesmultiverse.managers.StatusManager.StatusFlag;
 import me.lucasemanuel.survivalgamesmultiverse.threading.SQLiteInterface;
 import me.lucasemanuel.survivalgamesmultiverse.utils.ConsoleLogger;
 import me.lucasemanuel.survivalgamesmultiverse.utils.Updater;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 
 public class Main extends JavaPlugin {
 	
 	//TODO add command to freeze the game
 	//TODO add command to block players from entering, for maintenance purpose
 	
 	private ConsoleLogger logger;
 	
 	private PlayerManager   playermanager;
 	private WorldManager    worldmanager;
 	private ChestManager    chestmanager;
 	private StatsManager    statsmanager;
 	private LanguageManager languagemanager;
 	private LocationManager locationmanager;
 	private StatusManager   statusmanager;
 	private SignManager     signmanager;
 	private SQLiteInterface sqlite;
 	
 	public void onEnable() {
 		
 		ConsoleLogger.init(this);
 		
 		logger = new ConsoleLogger("Main");
 		logger.debug("Initiating startup sequence...");
 		
 		// Config
 		logger.debug("Loading configurationfile...");
 		Config.load(this);
 		
 		// Update
 		if(getConfig().getBoolean("auto-update")) {
 			logger.info("Auto-Updating enabled!");
 			new Updater(this, "survivalgamesmultiverse", this.getFile(), Updater.UpdateType.DEFAULT, true);
 		}
 		
 		// DHUtils
 		try {
 			logger.info("Checking compatability...");
 			NMSHelper.init(this);
 			logger.info("Server is compatible.");
 		}
 		catch(Exception e) {
 			logger.severe("Unsupported server version! Disabling plugin.");
 			this.setEnabled(false);
 			return;
 		}
 		
 		// Metrics
 		try {
 			logger.debug("Initiating metrics...");
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		}
 		catch (IOException e) {
 			logger.severe("Failed to submit stats to MCStats.org! Please contact author of this plugin!");
 		}
 		
 		// Managers
 		logger.debug("Initiating managers...");
 		
 		playermanager   = new PlayerManager(this);
 		worldmanager    = new WorldManager(this);
 		chestmanager    = new ChestManager(this);
 		statsmanager    = new StatsManager(this);
 		languagemanager = new LanguageManager(this);
 		locationmanager = new LocationManager(this);
 		statusmanager   = new StatusManager(this);
 		signmanager     = new SignManager(this);
 		sqlite          = new SQLiteInterface(this);
 		
 		// Events
 		logger.debug("Finished! Moving on to event listeners...");
 		
 		this.getServer().getPluginManager().registerEvents(new Players(this), this);
 		this.getServer().getPluginManager().registerEvents(new Blocks(this), this);
 		this.getServer().getPluginManager().registerEvents(new Worlds(this), this);
 		this.getServer().getPluginManager().registerEvents(new Misc(this), this);
 		
 		// Commands
 		logger.debug("Finished! Registering commands...");
 		
 		Commands commands = new Commands(this);
 		
 		this.getCommand("sginfo").setExecutor(commands);
 		this.getCommand("sgdebug").setExecutor(commands);
 		this.getCommand("sglocation").setExecutor(commands);
 		this.getCommand("sgactivate").setExecutor(commands);
 		this.getCommand("sgreset").setExecutor(commands);
 		this.getCommand("sgplayers").setExecutor(commands);
 		this.getCommand("sgleave").setExecutor(commands);
 		
 		// Worlds
 		logger.debug("Finished! Lets load some worlds...");
 		
 		for(String key : getConfig().getConfigurationSection("worlds").getKeys(false)) {
 			
 			logger.debug("Loading world - " + key);
 			
 			worldmanager.addWorld(Bukkit.createWorld(new WorldCreator(key)));
 			playermanager.addWorld(key);
 			locationmanager.addWorld(key);
 			statusmanager.addWorld(key);
 		}
 		
 		// Signs
 		logger.debug("Finished! Schedules sign update...");
 		
 		// Runs a little while after the server is setup to let the signs be registered in time.
 		// Without this delay, the world will say that the signs do not exist. 
 		this.getServer().getScheduler().runTaskLater(this, new Runnable() {
 			public void run() {
 				signmanager.loadsigns();
 				signmanager.updateSigns();
 			}
 		}, 40L);
 		
 		logger.debug("Startup sequence finished!");
 	}
 	
 	public void onDisable() {
 		logger.debug("Disabling plugin!");
 		
 		getServer().getScheduler().cancelTasks(this);
 		
 		if(sqlite != null)
 			sqlite.closeConnection();
 	}
 	
 	public PlayerManager getPlayerManager() {
 		return playermanager;
 	}
 	
 	public WorldManager getWorldManager() {
 		return worldmanager;
 	}
 	
 	public ChestManager getChestManager() {
 		return chestmanager;
 	}
 	
 	public StatsManager getStatsManager() {
 		return statsmanager;
 	}
 	
 	public LanguageManager getLanguageManager() {
 		return languagemanager;
 	}
 
 	public LocationManager getLocationManager() {
 		return locationmanager;
 	}
 	
 	public StatusManager getStatusManager() {
 		return statusmanager;
 	}
 	
 	public SignManager getSignManager() {
 		return signmanager;
 	}
 	
 	public SQLiteInterface getSQLiteConnector() {
 		return sqlite;
 	}
 	
 	public void gameover(World world) {
 		
 		if(playermanager.isGameOver(world)) {
 			
 			if(statusmanager.getStatusFlag(world.getName()) == StatusFlag.STARTED) {
 				
 				// Broadcast a message to all players in that world that the game is over.
 				worldmanager.broadcast(world, languagemanager.getString("gameover"));
 				
 				// Do we have a winner?
 				Player winner = playermanager.getWinner(world);
 				
 				if(winner != null) {
 					
					worldmanager.broadcast(world, ChatColor.GOLD + winner.getName() 
							+ ChatColor.WHITE + " " + languagemanager.getString("wonTheGame"));
 					
					if(!winner.hasPermission("survivalgames.ignore.stats")) 
						statsmanager.addWinPoints(winner.getName(), 1, true);
 					
 					statsmanager.removeScoreboard(winner.getName());
					
 					playermanager.removePlayer(winner.getWorld().getName(), winner);
					playermanager.clearInventory(winner);
					playermanager.restoreInventory(winner);
					
 					worldmanager.sendPlayerToLobby(winner);
 				}
 			}
 			
 			// Resets
 			resetWorld(world);
 		}
 	}
 	
 	public void resetWorld(World world) {
 		playermanager.killAndClear(world.getName());
 		worldmanager.resetWorld(world);
 		locationmanager.resetLocationStatuses(world);
 		chestmanager.clearLogs(world.getName());
 		statusmanager.reset(world.getName());
 		signmanager.updateSigns();
 	}
 }
