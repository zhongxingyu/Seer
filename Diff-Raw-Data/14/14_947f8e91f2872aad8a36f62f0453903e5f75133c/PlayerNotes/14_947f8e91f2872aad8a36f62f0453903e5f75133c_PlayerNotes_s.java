 package com.hamaluik.PlayerNotes;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.hamaluik.PlayerNotes.commands.*;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class PlayerNotes extends JavaPlugin {
 	// the basics
 	Logger log = Logger.getLogger("Minecraft");
 	public PermissionHandler permissionHandler;
 	
 	// the database manager..
 	public DBManager dbm = new DBManager(this);
 	
 	// the commands..
 	public HashMap<String, Command> commands = new HashMap<String, Command>();
 	
 	// and the command executor!
 	PlayerNotesCommandExecutor commandExecutor = new PlayerNotesCommandExecutor(this);
 	
 	// options
 	boolean useMYSQL = true;
 	String databaseName;
 	String mysqlUser;
 	String mysqlPass;
 	
 	// startup routine..
 	public void onEnable() {		
 		// set up the plugin..
 		this.setupPermissions();
 		this.loadConfiguration();
 		
 		// ensure the database table exists..
 		dbm.ensureTableExists();
 		
 		// register commands
 		registerCommand(new CommandNote(this));
 		registerCommand(new CommandNoteDelete(this));
 		registerCommand(new CommandNotes(this));
 		
 		log.info("[PlayerNotes] plugin enabled");
 	}
 
 	// shutdown routine
 	public void onDisable() {
 		log.info("[PlayerNotes] plugin disabled");
 	}
 	
 	// register a command
 	private void registerCommand(Command command) {
 		this.commands.put(command.getCommand(), command);
 		this.getCommand(command.getCommand()).setExecutor(this.commandExecutor);
 	}
 	
 	// load the permissions plugin..
 	private void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 		
 		if(this.permissionHandler == null) {
 			if(permissionsPlugin != null) {
 				this.permissionHandler = ((Permissions)permissionsPlugin).getHandler();
 				log.info("[PlayerNotes] permissions successfully loaded");
 			} else {
 				log.info("[PlayerNotes] permission system not detected, defaulting to OP");
 			}
 		}
 	}
 	
 	// just an interface function for checking permissions
 	// if permissions are down, default to OP status.
 	public boolean hasPermission(Player player, String permission) {
 		if(permissionHandler == null) {
 			return player.isOp();
 		}
 		else {
 			return (permissionHandler.has(player, permission));
 		}
 	}
 	
 	private void checkConfiguration() {
 		// first, check to see if the file exists
 		File configFile = new File(getDataFolder() + "/config.yml");
 		if(!configFile.exists()) {
 			// file doesn't exist yet :/
 			log.info("[PlayerNotes] config file not found, will attempt to create a default!");
 			new File(getDataFolder().toString()).mkdir();
 			try {
 				// create the file
 				configFile.createNewFile();
 				// and attempt to write the defaults to it
 				FileWriter out = new FileWriter(getDataFolder() + "/config.yml");
 				out.write("---\n");
 				out.write("# database can be either:\n");
 				out.write("# 'mysql' or 'sqlite'\n");
 				out.write("database: sqlite\n");
 				out.write("# if using sqlite, this should be: plugins/PlayerNotes/PlayerNotes.db\n");
 				out.write("# if using mysql, this is the mysql database you wish to use\n");
				out.write("database-name: plugins/PlayerNotes/PlayerNotes.db");
 				out.write("# only needed if using mysql\n");
				out.write("mysql-user: ''");
				out.write("mysql-pass: ''");
 				out.close();
 			} catch(IOException ex) {
 				// something went wrong :/
 				log.info("[PlayerNotes] error: config file does not exist and could not be created");
 			}
 		}
 	}
 
 	public void loadConfiguration() {
 		// make sure the config exists
 		// and if it doesn't, make it!
 		this.checkConfiguration();
 		
 		// get the configuration..
 		Configuration config = getConfiguration();
 		String database = config.getString("database");
 		if(database.equalsIgnoreCase("mysql")) useMYSQL = true;
 		else useMYSQL = false;
 		databaseName = config.getString("database-name");
 		mysqlUser = config.getString("mysql-user");
 		mysqlPass = config.getString("mysql-pass");
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
