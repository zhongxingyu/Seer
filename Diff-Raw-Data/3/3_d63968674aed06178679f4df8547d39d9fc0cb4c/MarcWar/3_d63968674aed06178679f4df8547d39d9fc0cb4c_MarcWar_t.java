 package com.mcprohosting.plugins.marcwar;
 
 import com.mcprohosting.plugins.marcwar.commands.SetCapture;
 import com.mcprohosting.plugins.marcwar.commands.SetFlag;
import com.mcprohosting.plugins.marcwar.commands.SetLobby;
 import com.mcprohosting.plugins.marcwar.commands.SetSpawn;
 import com.mcprohosting.plugins.marcwar.listeners.PlayerListener;
 import com.mcprohosting.plugins.marcwar.utilities.TeamHandler;
 import lilypad.client.connect.api.Connect;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MarcWar extends JavaPlugin {
 
 	private static Plugin plugin;
 	private static Connect connect;
 	private static String gameProgress;
 
 	public void onEnable() {
 		// Allow this to be accessed statically
 		plugin = this;
 
 		// Set game progress
 		gameProgress = "Starting";
 
 		// Initialize teams
 		TeamHandler.inititializeTeams();
 
 		// Save the default config to file
 		saveDefaultConfig();
 
 		// Load team spawns from config
 		TeamHandler.setupSpawnsFromConfiguration();
 
 		// Register listeners
 		registerListeners();
 
 		// Register commands
 		registerCommands();
 
 		// Register lilypad connection
 		//connect = plugin.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
 	}
 
 	public void onDisable() {
 
 	}
 
 	public static Plugin getPlugin() {
 		return plugin;
 	}
 
 	public static Connect getConnect() {
 		return connect;
 	}
 
 	public void registerListeners() {
 		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
 	}
 
 	public void registerCommands() {
 		getCommand("setspawn").setExecutor(new SetSpawn());
 		getCommand("setflag").setExecutor(new SetFlag());
 		getCommand("setcapture").setExecutor(new SetCapture());
		getCommand("setlobby").setExecutor(new SetLobby());
 	}
 
 	public static String getGameProgress() {
 		return gameProgress;
 	}
 
 	public static void setGameProgress(String status) {
 		gameProgress = status;
 	}
 
 }
