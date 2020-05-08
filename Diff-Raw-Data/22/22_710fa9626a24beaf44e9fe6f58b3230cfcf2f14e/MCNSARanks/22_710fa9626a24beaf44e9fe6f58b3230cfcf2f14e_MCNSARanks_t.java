 package com.hamaluik.MCNSARanks;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.hamaluik.MCNSARanks.commands.*;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class MCNSARanks extends JavaPlugin {
 	// the basics
 	public Logger log = Logger.getLogger("Minecraft");
 	public PermissionManager permissions = null;
 	
 	// the commands..
	public HashMap<String, Command> commands = null;
 	
 	// listeners
 	public PlayerListener playerListener;
	MCNSARanksCommandExecutor commandExecutor;
 	
 	// startup routine..
 	public void onEnable() {		
 		// set up the plugin..
 		this.setupPermissions();
 		
		 commands = new HashMap<String, Command>();
		
 		// setup listeners
 		playerListener = new PlayerListener(this);
 		
		commandExecutor = new MCNSARanksCommandExecutor(this);
		
 		// register commands
 		registerCommand(new CommandPlayers(this));
 		registerCommand(new CommandRank(this));
 		registerCommand(new CommandRanks(this));
 		
 		// and set everyones colours
 		Player[] players = getServer().getOnlinePlayers();
 		for(int i = 0; i < players.length; i++) {			
 			// set their rank colour for the display list
 			String result = processColours(permissions.getUser(players[i]).getPrefix() + players[i].getName());
 			if(result.length() > 16) {
 				result = result.substring(0, 16);
 			}
 			players[i].setPlayerListName(result);
 		}
 		
 		log.info("[MCNSARanks] plugin enabled");
 	}
 
 	// shutdown routine
 	public void onDisable() {
 		log.info("[MCNSARanks] plugin disabled");
 	}
 	
 	// register a command
	private void registerCommand(Command command) {
 		// add the command to the commands list and register it with Bukkit
		this.commands.put(command.getCommand(), command);
		String commandString = command.getCommand();
		if(this.getCommand(commandString) != null) {
			this.getCommand(commandString).setExecutor(this.commandExecutor);
		}
 	}
 
 	// load the permissions plugin
 	public void setupPermissions() {
 		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
 			this.permissions = PermissionsEx.getPermissionManager();
 			log.info("[MCNSARanks] PermissionsEx successfully loaded!");
 		}
 		else {
 			log.warning("[MCNSARanks] ERROR: PermissionsEx not found!");
 		}
 	}
 	
 	// just an interface function for checking permissions
 	// if permissions are down, default to OP status.
 	public boolean hasPermission(Player player, String permission) {
 		if(permissions != null) {
 			return permissions.has(player, permission);
 		}
 		else {
 			return player.isOp();
 		}
 	}
 	
 	// allow for colour tags to be used in strings..
 	public String processColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
 	}
 	
 	// strip colour tags from strings..
 	public String stripColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "");
 	}
 }
