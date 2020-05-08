 package me.coolblinger.swordsgame;
 
 import org.bukkit.util.config.Configuration;
 
 import java.io.File;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class SwordsGameLocalisation {
 	private final File configFile = new File("plugins" + File.separator + "SwordsGame" + File.separator + "localisation.yml");
 
 	public SwordsGameLocalisation() {
 		initConfig();
 	}
 
 	Configuration config() {
 		try {
 			Configuration config = new Configuration(configFile);
 			config.load();
 			return config;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public ConcurrentHashMap<String, String> getLocalisation() {
 		ConcurrentHashMap<String, String> returnMap = new ConcurrentHashMap<String, String>();
 		Configuration config = config();
 		Map<String, Object> localisationMap = config.getAll();
 		Object[] keyArray = localisationMap.keySet().toArray();
 		Object[] valueArray = localisationMap.values().toArray();
 		for (int i = 0; i < keyArray.length; i++) {
 			returnMap.put(keyArray[i].toString(), valueArray[i].toString());
 		}
 		return returnMap;
 	}
 
 	@SuppressWarnings({"ResultOfMethodCallIgnored"})
 	void initConfig() {
 		configFile.getParentFile().mkdir();
 		if (!configFile.exists()) {
 			try {
 				configFile.createNewFile();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		Configuration config = config();
 		config.setHeader("#Keep in mind that spaces and other symbols are there for a reason.\n#Just modify the text itself and you should be fine.");
 		// SwordsGame
 		if (config.getProperty("errors.lobby.create.invalidLocation") == null) {
 			config.setProperty("errors.lobby.create.invalidLocation", "You can't place a lobby here.");
 			config.save();
 		}
 		if (config.getProperty("lobby.players") == null) {
 			config.setProperty("lobby.players", "players");
 			config.save();
 		}
 		if (config.getProperty("lobby.success") == null) {
 			config.setProperty("lobby.success", "The lobby has been created.");
 			config.save();
 		}
 		if (config.getProperty("lobby.remove1") == null) {
 			config.setProperty("lobby.remove1", "The lobby for arena '");
 			config.save();
 		}
 		if (config.getProperty("lobby.remove2") == null) {
 			config.setProperty("lobby.remove2", "' has been removed.");
 			config.save();
 		}
 		// End of SwordsGame
 		// SwordsGameCommand
 		if (config.getProperty("error.lobbyOnly") == null) {
 			config.setProperty("error.lobbyOnly", "Lobby-only mode has been enabled on this server.");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.define") == null) {
 			config.setProperty("commandDesc.define", "Define a new arena");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.remove") == null) {
 			config.setProperty("commandDesc.remove", "Remove an arena");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.lobby.create") == null) {
 			config.setProperty("commandDesc.lobby.create", "Create a lobby for an arena at   your current location");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.lobby.remove") == null) {
 			config.setProperty("commandDesc.lobby.remove", "Remove the arena's lobby");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.setspawns") == null) {
 			config.setProperty("commandDesc.setspawns", "Set the spawns for an arena");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.resetspawns") == null) {
 			config.setProperty("commandDesc.resetspawns", "Reset the spawns for an arena");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.list") == null) {
 			config.setProperty("commandDesc.list", "Arena list");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.game") == null) {
 			config.setProperty("commandDesc.game", "Create or join a game in the specified     arena");
 			config.save();
 		}
 		if (config.getProperty("commandDesc.leave") == null) {
 			config.setProperty("commandDesc.leave", "Leave the current game");
 			config.save();
 		}
 		if (config.getProperty("errors.consoleCommand") == null) {
 			config.setProperty("errors.consoleCommand", "This command is only executable by players.");
 			config.save();
 		}
 		if (config.getProperty("errors.defining.spawnsWhileDefining") == null) {
 			config.setProperty("errors.defining.spawnsWhileDefining", "You can't define arenas while setting spawns.");
 			config.save();
 		}
 		if (config.getProperty("errors.defining.alreadyExists") == null) {
 			config.setProperty("errors.defining.alreadyExists", "An arena with that name already exists.");
 			config.save();
 		}
 		if (config.getProperty("errors.defining.noName") == null) {
 			config.setProperty("errors.defining.noName", "You need to specify a name for the new arena.");
 			config.save();
 		}
 		if (config.getProperty("errors.defining.alreadyDefining") == null) {
 			config.setProperty("errors.defining.alreadyDefining", "You're already defining an arena.");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.BukkitContrib.notificationTitle1") == null) {
 			config.setProperty("defining.defining.BukkitContrib.notificationTitle1", "Defining");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.BukkitContrib.notificationText1") == null) {
 			config.setProperty("defining.defining.BukkitContrib.notificationText1", "Right click both corners");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.text1") == null) {
 			config.setProperty("defining.defining.text1", "Right click at both corners of the arena. You can type          ");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.text2") == null) {
 			config.setProperty("defining.defining.text2", " when you're done, or left click to cancel.");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.arenaCreated") == null) {
 			config.setProperty("defining.defining.arenaCreated", "' has been created!");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.setSpawns") == null) {
			config.setProperty("defining.defining.setSpawns", "You should set spawnpoints now, using ");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.BukkitContrib.notificationTitle2") == null) {
 			config.setProperty("defining.defining.BukkitContrib.notificationTitle2", "Setting spawnpoints");
 			config.save();
 		}
 		if (config.getProperty("defining.removing.success") == null) {
 			config.setProperty("defining.removing.success", "' has been removed.");
 			config.save();
 		}
 		if (config.getProperty("errors.removing.invalidName") == null) {
 			config.setProperty("errors.removing.invalidName", "Invalid arena name specified.");
 			config.save();
 		}
 		if (config.getProperty("errors.removing.noName") == null) {
 			config.setProperty("errors.removing.noName", "You need to specify the name of the arena you want to         remove.");
 			config.save();
 		}
 		if (config.getProperty("errors.lobby.create.noName") == null) {
 			config.setProperty("errors.lobby.create.noName", "You need to specify the name of the arena you want to         create a lobby for.");
 			config.save();
 		}
 		if (config.getProperty("errors.lobby.create.invalidName") == null) {
 			config.setProperty("errors.lobby.create.invalidName", "Invalid arena name specified.");
 			config.save();
 		}
 		if (config.getProperty("errors.lobby.create.alreadyExists") == null) {
 			config.setProperty("errors.lobby.create.alreadyExists", "A lobby for this arena already exists.");
 			config.save();
 		}
 		if (config.getProperty("errors.lobby.remove.noName") == null) {
 			config.setProperty("errors.lobby.remove.noName", "You need to specify the name of the arena you want to remove.");
 			config.save();
 		}
 		if (config.getProperty("errors.lobby.remove.invalidName") == null) {
 			config.setProperty("errors.lobby.remove.invalidName", "Invalid arena name specified.");
 			config.save();
 		}
 		if (config.getProperty("errors.lobby.remove.noLobby") == null) {
 			config.setProperty("errors.lobby.remove.noLobby", "The specified arena has not got a lobby.");
 			config.save();
 		}
 		if (config.getProperty("defining.settingSpawns.BukkitContrib.notificationTitle1") == null) {
 			config.setProperty("defining.settingSpawns.BukkitContrib.notificationTitle1", "Setting spawns");
 			config.save();
 		}
 		if (config.getProperty("defining.settingSpawns.BukkitContrib.notificationText1") == null) {
 			config.setProperty("defining.settingSpawns.BukkitContrib.notificationText1", "Right click");
 			config.save();
 		}
 		if (config.getProperty("defining.settingSpawns.help") == null) {
 			config.setProperty("defining.settingSpawns.help", "Right click inside an arena at the desired location. You can leftclick when you're done.");
 			config.save();
 		}
 		if (config.getProperty("errors.settingSpawns.alreadySettingSpawns") == null) {
 			config.setProperty("errors.settingSpawns.alreadySettingSpawns", "You're already setting spawns.");
 			config.save();
 		}
 		if (config.getProperty("errors.settingSpawns.alreadyDefiningArenas") == null) {
 			config.setProperty("errors.settingSpawns.alreadyDefiningArenas", "You can't set spawns while defining arenas.");
 			config.save();
 		}
 		if (config.getProperty("defining.resettingSpawns.success1") == null) {
 			config.setProperty("defining.resettingSpawns.success1", "Spawns for arena '");
 			config.save();
 		}
 		if (config.getProperty("defining.resettingSpawns.success2") == null) {
 			config.setProperty("defining.resettingSpawns.success2", "' have been reset.");
 			config.save();
 		}
 		if (config.getProperty("errors.settingSpawns.invalidName") == null) {
 			config.setProperty("errors.settingSpawns.invalidName", "Invalid arena name specified.");
 			config.save();
 		}
 		if (config.getProperty("defining.settingSpawns.noName") == null) {
 			config.setProperty("defining.settingSpawns.noName", "You need to specify the name of an arena.");
 			config.save();
 		}
 		if (config.getProperty("defining.list.arenaList") == null) {
 			config.setProperty("defining.list.arenaList", "list: (showing ");
 			config.save();
 		}
 		if (config.getProperty("defining.list.outOf") == null) {
 			config.setProperty("defining.list.outOf", " out of ");
 			config.save();
 		}
 		if (config.getProperty("defining.list.players") == null) {
 			config.setProperty("defining.list.players", " players)");
 			config.save();
 		}
 		if (config.getProperty("defining.list.empty") == null) {
 			config.setProperty("defining.list.empty", " (empty)");
 			config.save();
 		}
 		if (config.getProperty("defining.list.missingSpawns") == null) {
 			config.setProperty("defining.list.missingSpawns", " (missing spawnpoints)");
 			config.save();
 		}
 		if (config.getProperty("errors.game.full") == null) {
 			config.setProperty("errors.game.full", "This game is full.");
 			config.save();
 		}
 		if (config.getProperty("errors.game.alreadyInAGame") == null) {
 			config.setProperty("errors.game.alreadyInAGame", "You're already in a game.");
 			config.save();
 		}
 		if (config.getProperty("errors.game.missingSpawns") == null) {
 			config.setProperty("errors.game.missingSpawns", "The specified arena is missing spawnpoints.");
 			config.save();
 		}
 		if (config.getProperty("errors.game.invalidName") == null) {
 			config.setProperty("errors.game.invalidName", "Invalid arena name.");
 			config.save();
 		}
 		if (config.getProperty("errors.game.noName") == null) {
 			config.setProperty("errors.game.noName", "You need to specify the name of the arena you want to play in.");
 			config.save();
 		}
 		if (config.getProperty("errors.leave.notInAGame") == null) {
 			config.setProperty("errors.leave.notInAGame", "You're not in a game.");
 			config.save();
 		}
 		// End of SwordsGameCommand
 		// SwordsGamePlayerListener
 		if (config.getProperty("errors.command.notAllowedInGame") == null) {
 			config.setProperty("errors.command.notAllowedInGame", "You can't execute this command while in a SwordsGame game.");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.firstCorner") == null) {
 			config.setProperty("defining.defining.firstCorner", "First corner has been set to ");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.secondCorner") == null) {
 			config.setProperty("defining.defining.secondCorner", "Second corner has been set to ");
 			config.save();
 		}
 		if (config.getProperty("defining.defining.canceled") == null) {
 			config.setProperty("defining.defining.canceled", "Defining has been canceled.");
 			config.save();
 		}
 		if (config.getProperty("defining.settingSpawns.set") == null) {
 			config.setProperty("defining.settingSpawns.set", "Added spawn ");
 			config.save();
 		}
 		if (config.getProperty("defining.settingSpawns.for") == null) {
 			config.setProperty("defining.settingSpawns.for", " for ");
 			config.save();
 		}
 		if (config.getProperty("errors.settingSpawns.alreadyFourSpawns") == null) {
 			config.setProperty("errors.settingSpawns.alreadyFourSpawns", "You've already created four spawns.");
 			config.save();
 		}
 		if (config.getProperty("errors.settingSpawns.inValid") == null) {
 			config.setProperty("errors.settingSpawns.inValid", "This is not a valid arena.");
 			config.save();
 		}
 		if (config.getProperty("defining.settingSpawns.canceled") == null) {
 			config.setProperty("defining.settingSpawns.canceled", "Setting spawns has been canceled.");
 			config.save();
 		}
 		// End of SwordsGamePlayerListener
 		// SwordsGameClass
 		if (config.getProperty("games.created") == null) {
 			config.setProperty("games.created", "Game successfully created!");
 			config.save();
 		}
 		if (config.getProperty("games.createMessage") == null) {
 			config.setProperty("games.createMessage", "Unfortunaly, there are currently too few people, so you'll     have to wait until someone joins you.");
 			config.save();
 		}
 		if (config.getProperty("games.playerJoined") == null) {
 			config.setProperty("games.playerJoined", " has joined the game!");
 			config.save();
 		}
 		if (config.getProperty("games.playerLeft") == null) {
 			config.setProperty("games.playerLeft", " has left the game!");
 			config.save();
 		}
 		if (config.getProperty("games.leaveCommand") == null) {
 			config.setProperty("games.leaveCommand", "You can leave using ");
 			config.save();
 		}
 		if (config.getProperty("games.started") == null) {
 			config.setProperty("games.started", "The game has been started, good luck!");
 			config.save();
 		}
 		if (config.getProperty("games.aborted") == null) {
 			config.setProperty("games.aborted", "The game has been aborted because there is only one player left.");
 			config.save();
 		}
 		if (config.getProperty("games.rank") == null) {
 			config.setProperty("games.rank", "Rank ");
 			config.save();
 		}
 		if (config.getProperty("games.downRank") == null) {
 			config.setProperty("games.downRank", "You've been demoted to rank ");
 			config.save();
 		}
 		if (config.getProperty("games.newMatch.15") == null) {
 			config.setProperty("games.newMatch.15", "A new match will start in fifteen seconds.");
 			config.save();
 		}
 		if (config.getProperty("games.newMatch.5") == null) {
 			config.setProperty("games.newMatch.5", "Five seconds left!");
 			config.save();
 		}
 		if (config.getProperty("games.BukkitContrib.lead") == null) {
 			config.setProperty("games.BukkitContrib.lead", "<LEAD> ");
 			config.save();
 		}
 		if (config.getProperty("games.BukkitContrib.winner") == null) {
 			config.setProperty("games.BukkitContrib.winner", "<WINNER> ");
 			config.save();
 		}
 		if (config.getProperty("games.BukkitContrib.tie") == null) {
 			config.setProperty("games.BukkitContrib.tie", "<TIE> ");
 			config.save();
 		}
 		// End of SwordsGameClass
 	}
 }
