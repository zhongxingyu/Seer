 /**
  *  Name: LanguageManager.java
  *  Date: 23:22:06 - 13 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Description:
  *  
  *  Manages all of the configurable broadcasts etc.
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.managers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import me.lucasemanuel.survivalgamesmultiverse.Main;
 import me.lucasemanuel.survivalgamesmultiverse.utils.ConsoleLogger;
 
 public class LanguageManager {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	private HashMap<String, String> language;
 	
 	public LanguageManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger(instance, "LanguageManager");
 		
 		language = new HashMap<String, String>();
 		
 		loadLanguage();
 	}
 	
 	private synchronized void loadLanguage() {
 		
 		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "language.yml"));
 		
 		checkDefaults(config);
 		
 		for(String key : config.getKeys(true)) {
 			language.put(key, config.getString(key));
 		}
 	}
 
 	private synchronized void checkDefaults(FileConfiguration config) {
 		
 		boolean save = false;
 		
 		if(!config.contains("gameover")) {
 			config.set("gameover", "Game over!");
 			save = true;
 		}
 		
 		if(!config.contains("wonTheGame")) {
 			config.set("wonTheGame", "won the game!");
 			save = true;
 		}
 		
 		if(!config.contains("isOutOfTheGame")) {
 			config.set("isOutOfTheGame", "is out of the game!");
 			save = true;
 		}
 		
 		if(!config.contains("wasKilledBy")) {
 			config.set("wasKilledBy", "was killed by");
 			save = true;
 		}
 		
 		if(!config.contains("youJoinedTheGame")) {
 			config.set("youJoinedTheGame", "You joined the game!");
 			save = true;
 		}
 		
 		if(!config.contains("playerJoinedGame")) {
 			config.set("playerJoinedGame", "joined the game!");
 			save = true;
 		}
 		
 		if(!config.contains("alreadyPlaying")) {
 			config.set("alreadyPlaying", "You are already playing!");
 			save = true;
 		}
 		
 		if(!config.contains("anticheatRemoval.disconnect")) {
 			config.set("anticheatRemoval.disconnect", "was removed due to disconnect!");
 			save = true;
 		}
 		
 		if(!config.contains("anticheatRemoval.teleported")) {
 			config.set("anticheatRemoval.teleported", "was removed due to teleportation!");
 			save = true;
 		}
 		
 		if(!config.contains("gameIsFull")) {
 			config.set("gameIsFull", "Game is full!");
 			save = true;
 		}
 		
 		if(!config.contains("blockedMovement")) {
 			config.set("blockedMovement", "You are not allowed to move yet!");
 			save = true;
 		}
 		
 		if(!config.contains("timeleft")) {
 			config.set("timeleft","seconds left until game starts.");
 			save = true;
 		}
 		
 		if(!config.contains("gamestarted")) {
 			config.set("gamestarted", "Game started! GO GO GO!");
 			save = true;
 		}
 		
 		if(!config.contains("waitingForPlayers")) {
 			config.set("waitingForPlayers", "Atleast one more player has to join!");
 			save = true;
 		}
 		
 		if(!config.contains("blockedCommand")) {
 			config.set("blockedCommand", "You are not allowed to use that command ingame!");
 			save = true;
 		}
 		
 		if(!config.contains("movedOutsideOfSpawn")) {
 			config.set("movedOutsideOfSpawn", "You are not allowed to be there!");
 			save = true;
 		}
 		
 		if(!config.contains("sgplayersHeading")) {
 			config.set("sgplayersHeading", "Alive Players");
 			save = true;
 		}
 		
 		if(!config.contains("sgleaveNotIngame")) {
 			config.set("sgleaveNotIngame", "You are not in the game!");
 			save = true;
 		}
 		
 		if(!config.contains("sendingEveryoneToArena")) {
 			config.set("sendingEveryoneToArena", "The game took to long to finish! Sending everyone to the arena!");
 			save = true;
 		}
 		
 		if(!config.contains("sentYouToArena")) {
 			config.set("sentYouToArena", "You where sent to the arena!");
 			save = true;
 		}
 		
 		if(!config.contains("secondsTillTheGameEnds")) {
 			config.set("secondsTillTheGameEnds", "seconds until the game is cancelled!");
 			save = true;
 		}
 		
 		if(!config.contains("killedSendingArena")) {
 			config.set("killedSendingArena", "No locations left in the arena! You where killed.");
 			save = true;
 		}
 		
 		if(!config.contains("forcedPumpkin")) {
 			config.set("forcedPumpkin", "You have to wear that pumpkin!");
 			save = true;
 		}
 		
 		if(!config.contains("gameHasNotStartedYet")) {
 			config.set("gameHasNotStartedYet", "The game hasn't started yet!");
 			save = true;
 		}
 		
 		if(!config.contains("gameHasAlreadyStarted")) {
 			config.set("gameHasAlreadyStarted", "The game has already started, try another world!");
 			save = true;
 		}
 		
 		if(!config.contains("sgplayersNoonealive")) {
 			config.set("sgplayersNoonealive", "No players alive!");
 			save = true;
 		}
 		
 		if(!config.contains("sgplayersIncorrect")) {
 			config.set("sgplayersIncorrect", "You need to be in a gameworld, or enter the name of one!");
 			save = true;
 		}
 		
 		if(!config.contains("signs.started")) {
 			config.set("signs.started", "Started");
 			save = true;
 		}
 		
 		if(!config.contains("signs.waiting")) {
 			config.set("signs.waiting", "Waiting");
 			save = true;
 		}
 		
 		if(!config.contains("signs.playersIngame")) {
 			config.set("signs.playersIngame", "Players Ingame");
 			save = true;
 		}
 		
 		if(!config.contains("signs.frozen")) {
 			config.set("signs.frozen", "Frozen");
 			save = true;
 		}
 		
 		if(save) {
 			try {
 	            config.save(this.plugin.getDataFolder() + File.separator + "language.yml");
 	        } catch (IOException e) {
 	        	this.logger.severe("Could not save language.yml!");
 	        }
 		}
 	}
 	
 	public synchronized String getString(String key) {
 		
 		if(language.containsKey(key)) {
 			return language.get(key);
 		}
 		
 		return null;
 	}
 }
