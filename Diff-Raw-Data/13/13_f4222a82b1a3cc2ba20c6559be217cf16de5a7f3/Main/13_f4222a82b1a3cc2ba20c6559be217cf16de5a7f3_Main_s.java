 /*
  * TODO List:
  * - Add admin command to clear all database entries (separate commands for each db?)
  * - Add methods to set the spawn, death point and jail locations. (Commands in game, all they do is send message)
  * - Get nearest player entity to player on player death
  * - Sheriff Stick, right click on player to arrest (player sent to jail)
  * 		-> if player is roughian, Sheriff gets karma; if player is innocent, Sheriff loses karma
  * 		-> Sheriff Stick gets added damage value on each use
  * - On player death, player spawns in completely crazy stylised death place
  * - Add player stats (from constant db) command
  * - Change player name in chat according to whether they are in game, dead, in jail, or sheriff.
  * - Add /leave command. Player will leave match and go back to spawn.
  * - When a match ends the players are teleported to a different map.
  * 		-> Random maps keeps things interesting.
  * 		-> Message saying "Welcome to [map name here]!"
  * 
  * EXPLINATIONS:
  * 	- Number 1
  * 		This is where you can start to add new commands in with an else if statement.
  * 		In between the START and the END comment lines.
  */
 package com.tammcd.troubleinminecraft;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import lib.PatPeter.SQLibrary.SQLite;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public static Main plugin;
 	public SQLite db;
 	private boolean gameRunning = false;
 	public final Location[] warpLocations = new Location[100];
 	public final String[] warpName = new String[100];
 	public int warpCounter = 0;
 
 	// STARTUP
 	public void onEnable() {
 		this.getDataFolder().mkdir();
 		this.saveDefaultConfig();
 
 		sqlConnection();
 		sqlTableCheck();
 		logConstant();
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		this.getLogger().info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been enabled!");
 
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 		}
 
 		getServer().getPluginManager().registerEvents(new Listener() {
 			@EventHandler
 			public void playerJoin(PlayerJoinEvent event) {
 				event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + Main.this.getConfig().getString("motd"));
 				putPlayerConstant(event.getPlayer().getName());
 			}
 
 			@EventHandler
 			public void playerLeave(PlayerQuitEvent event) {
 				String playerName = event.getPlayer().getName();
 				playerLeaveGame(playerName);
 			}
 		}, this);
 	}
 
 	// SHUTDOWN
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " (version: " + pdfFile.getVersion() + ") has been disabled!");
 		db.close();
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = (Player) sender;
 		String playerName = player.getName();
 
 		int alreadyPlayer = 0;
 		try {
 			ResultSet isPlayerInGame = db.query("SELECT EXISTS(SELECT * FROM game WHERE playername='" + playerName + "')");
 			while (isPlayerInGame.next()) {
 				alreadyPlayer = alreadyPlayer + isPlayerInGame.getInt(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		// START (Refer to EXPLINATIONS in the to do list) NUMBER 1
 
 		if (label.equalsIgnoreCase("timhelp")) {
 			if (player.isOp()) {
 				// Might set the help as a list in config and pull it from
 				// there. Easier to edit when a plugin.
 				player.sendMessage(ChatColor.GOLD + "Trouble in Minecraft Help " + ChatColor.RED + "(Admin)" + ChatColor.RED + ":");
 				player.sendMessage(ChatColor.GOLD + "----------------------------------------------");
 				player.sendMessage(ChatColor.GOLD + "/join - Player will join ongoing match.");
 				player.sendMessage(ChatColor.GOLD + "/stats - Look at your stats (Not implemented)");
 				player.sendMessage(ChatColor.GOLD + "/timfo - Displays the version of TIM.");
 				player.sendMessage(ChatColor.GOLD + "/timspawn - Sets the spawn for matches.");
 				player.sendMessage(ChatColor.GOLD + "/timdeath - Sets the death location.");
 				player.sendMessage(ChatColor.GOLD + "/timarrest - Sets the jail location.");
 
 			} else {
 				player.sendMessage(ChatColor.GOLD + "Trouble in Minecraft Help (Reg):");
 				player.sendMessage(ChatColor.GOLD + "----------------------------------------------");
 				player.sendMessage(ChatColor.GOLD + "/join - Player will join ongoing match.");
 				player.sendMessage(ChatColor.GOLD + "/stats - Look at your stats (Not implemented)");
 			}
 		}
 
 		if (label.equalsIgnoreCase("timspawn"))
 			if (player.isOp()) {
 				if (args.length == 0) {
 					player.sendMessage(ChatColor.RED + "/timspawn <spawnname>");
 				} else {
 					Location location = player.getLocation();
 					if (!(warpCounter > 100)) {
 						warpLocations[warpCounter] = location;
 						warpName[warpCounter] = args[0];
 						warpCounter++;
 						player.sendMessage(ChatColor.RED + "Spawn " + args[0] + " has been set.");
 					} else {
 						player.sendMessage(ChatColor.RED + "Spawn limmit exceeded, unable to create spawn.");
 					}
 				}
 
 			} else {
 				player.sendMessage(ChatColor.GOLD + "You do not have the permissions to use that command.");
 			}
 
 		if (label.equalsIgnoreCase("timdeath")) {
 			if (player.isOp()) {
 				player.sendMessage(ChatColor.RED + "Death point has been set.");
 			} else {
 				player.sendMessage(ChatColor.GOLD + "You do not have the permissions to use that command.");
 			}
 			if (label.equalsIgnoreCase("timarrest")) {
 				if (player.isOp()) {
 					player.sendMessage(ChatColor.RED + "Jail spawn has been set.");
 				} else {
 					player.sendMessage(ChatColor.GOLD + "You do not have the permissions to use that command.");
 				}
 			}
 		}
 
 		if (label.equalsIgnoreCase("timfo")) {
 			PluginDescriptionFile pdfFile = this.getDescription();
 			player.sendMessage(ChatColor.RED + "Trouble In Minecraft Version " + pdfFile.getVersion());
 			player.sendMessage(ChatColor.RED + "Code: Tamfoolery, Emmsii");
 		}
 
 		if (label.equalsIgnoreCase("stats")) {
 			player.sendMessage(ChatColor.GOLD + "-----Stats-----");
 			player.sendMessage(ChatColor.GOLD + "Kills: 0");
 			player.sendMessage(ChatColor.GOLD + "Deaths: 0");
 			player.sendMessage(ChatColor.GOLD + "Arrests: 0");
 
 		}
 
 		if (label.equalsIgnoreCase("leave")) {
 			player.sendMessage(ChatColor.GOLD + "You have left the game.");
 			// USE removePlayer method.
 		}
 		// END
 
		if (alreadyPlayer != 1) {

			if (label.equalsIgnoreCase("join")) {
 				if (gameRunning) {
 					player.sendMessage(ChatColor.GOLD + "You have joined a game in progress.");
 					Random object = new Random();
 					int test;
 					for (int counter = 1; counter <= 1; counter++) {
 						test = 1 + object.nextInt(3);
 						if (test == 1) {
 							player.sendMessage(ChatColor.GREEN + "You are innocent.");
 						} else if (test == 2) {
 							player.sendMessage(ChatColor.RED + "You are a roughian");
 						} else if (test == 3) {
 							player.sendMessage(ChatColor.GOLD + "You are a sheriff");
 						}
 					}
 				} else {
 					player.sendMessage(ChatColor.GOLD + "You have joined! The game will start shortly...");
 					Random object = new Random();
 					int test;
 					for (int counter = 1; counter <= 1; counter++) {
 						test = 1 + object.nextInt(3);
 						if (test == 1) {
 							player.sendMessage(ChatColor.GREEN + "You are innocent.");
 						} else if (test == 2) {
 							player.sendMessage(ChatColor.RED + "You are a roughian");
 						} else if (test == 3) {
 							player.sendMessage(ChatColor.GOLD + "You are a sheriff");
 						}
 					}
 				}
 				addPlayer(player, playerName);
 				if (gamePlayerCount() == 1) {
 					getServer().broadcastMessage("There is now " + gamePlayerCount() + " player in game.");
 				} else {
 					getServer().broadcastMessage("There are now " + gamePlayerCount() + " players in game.");
 				}
			}
 
		} else {
			player.sendMessage(ChatColor.RED + "You have already joined the game!");
 		}
 
 		return false;
 	}
 
 	public void addPlayer(Player player, String playerName) {
 		int playerCount = 0;
 
 		try {
 			ResultSet totalPlayers = db.query("SELECT COUNT(*) FROM game");
 			while (totalPlayers.next()) {
 				playerCount = playerCount + totalPlayers.getInt(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		db.query("INSERT INTO game (id, playername, isSheriff, isDead, isRoughian) VALUES(" + (playerCount + 1) + ", '" + playerName + "', 'false', 'false', 'false')");
 	}
 
 	public void removePlayer(Player player, String playerName) {
 		playerLeaveGame(playerName);
 	}
 
 	public int gamePlayerCount() {
 		int playerCount = 0;
 
 		try {
 			ResultSet totalPlayers = db.query("SELECT COUNT(*) FROM game");
 			while (totalPlayers.next()) {
 				playerCount = playerCount + totalPlayers.getInt(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return (playerCount);
 	}
 
 	public void sqlConnection() {
 		db = new SQLite(this.getLogger(), "Trouble in Minecraft", "game", this.getDataFolder().getPath());
 
 		try {
 			db.open();
 		} catch (Exception e) {
 			this.getLogger().info(e.getMessage());
 			getPluginLoader().disablePlugin(plugin);
 		}
 	}
 
 	public void sqlTableCheck() {
 		if (db.checkTable("game") && db.checkTable("constant")) {
 			return;
 		} else {
 			db.query("CREATE TABLE game (id INT PRIMARY KEY, playername VARCHAR(255), isSheriff VARCHAR(5), isDead VARCHAR(5), isRoughian VARCHAR(5))");
 			this.getLogger().info("[Trouble in Minecraft] 'game' table has been created!");
 			db.query("CREATE TABLE constant (id INT PRIMARY KEY, playername VARCHAR(255), karma INT, playCount INT, sheriffCount INT, roughianCount INT, deathCount INT)");
 			this.getLogger().info("[Trouble in Minecraft] 'constant' table has been created!");
 		}
 	}
 
 	public void putPlayerConstant(String playerName) {
 		int alreadyPlayer = 0;
 		int playerCount = 0;
 
 		try {
 			ResultSet isPlayerInGame = db.query("SELECT EXISTS(SELECT * FROM constant WHERE playername='" + playerName + "')");
 			while (isPlayerInGame.next()) {
 				alreadyPlayer = alreadyPlayer + isPlayerInGame.getInt(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		if (alreadyPlayer != 1) {
 			try {
 				ResultSet totalPlayers = db.query("SELECT COUNT(*) FROM constant");
 				while (totalPlayers.next()) {
 					playerCount = playerCount + totalPlayers.getInt(1);
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			db.query("INSERT INTO constant (id, playername, karma, playCount, sheriffCount, roughianCount, deathCount) VALUES(" + (playerCount + 1) + ", '" + playerName + "', 1000, 0, 0, 0, 0)");
 			this.getLogger().info("Player " + playerName + " has been added to constant.db");
 		} else {
 			this.getLogger().info("Player " + playerName + " has been found in constant.db, not adding.");
 		}
 		this.getLogger().info("There are " + (playerCount + 1) + " players stored in constant.db.");
 	}
 
 	public void logConstant() {
 		int playerCount = 0;
 		try {
 			ResultSet totalPlayers = db.query("SELECT COUNT(*) FROM constant");
 			while (totalPlayers.next()) {
 				playerCount = playerCount + totalPlayers.getInt(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.getLogger().info("There are " + playerCount + " players stored in constant.db.");
 	}
 
 	// Starting of the stick Sheriff's get.
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		int itemId = player.getItemInHand().getType().getId();
 		if (itemId == 280) {
 			arrestPlayer();
 		}
 	}
 
 	public void arrestPlayer() {
 
 	}
 
 	public void playerDeath() {
 
 	}
 
 	public void playerLeaveGame(String playerName) {
 		db.query("DELETE FROM game WHERE playername='" + playerName + "'");
 		if (gamePlayerCount() == 1) {
 			getServer().broadcastMessage("Player " + playerName + " left the game, leaving " + gamePlayerCount() + " player left!");
 		} else {
 			getServer().broadcastMessage("Player " + playerName + " left the game, leaving " + gamePlayerCount() + " players left.");
 		}
 	}
 }
