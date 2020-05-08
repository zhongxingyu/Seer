 package com.judoguys.bukkit.gps;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.judoguys.bukkit.commands.Action;
 import com.judoguys.bukkit.commands.CommandHandler;
 import com.judoguys.bukkit.commands.InvalidCommandException;
 import com.judoguys.bukkit.gps.actions.FindAction;
 import com.judoguys.bukkit.gps.actions.FollowAction;
 import com.judoguys.bukkit.gps.actions.PointToAction;
 import com.judoguys.bukkit.gps.actions.ResetAction;
 import com.judoguys.bukkit.gps.configuration.GPSConfiguration;
 
 /**
  * Please excuse my Java. I haven't touched the language in
  * quite some time.
  */
 public class GPS extends JavaPlugin
 {
 	public static String COMMAND_NAME = "gps";
 	
 	public Logger log;
 	public PluginManager pm;
 	
 	public HashMap<String, GPSConfiguration> configurations;
 	
 	private GPSPlayerListener playerListener;
 	private CommandHandler commandHandler;
 	
 	private PluginDescriptionFile desc;
 	private String version;
 	
 	private String label;
 	
 	public GPS ()
 	{
 		// TODO: Do plugin initialization stuff.
 	}
 	
 	public String getLabel ()
 	{
 		return label;
 	}
 	
 	/**
 	 * Register for events, setup logging, etc.
 	 */
 	public void onEnable ()
 	{
 		log = getServer().getLogger();
 		
 		desc = getDescription();
 		version = desc.getVersion();
 		label = "[" + desc.getName() + "]";
 		log.info(getLabel() + " version " + version + " by willurd");
 		
 		pm = getServer().getPluginManager();
 		playerListener = new GPSPlayerListener(this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
 		
 		loadSettings();
 		setupPermissions();
 		setupCommands();
 		
 		log.info(getLabel() + " enabled!");
 	}
 	
 	/**
 	 * Remove listeners?, save?, etc?
 	 */
 	@Override
 	public void onDisable ()
 	{
 		// TODO: Do stuff.
 		
 		log.info(getLabel() + " disabled");
 	}
 	
 	@Override
 	public boolean onCommand (CommandSender sender, Command cmd,
 			String label, String[] args)
 	{
 		String name = cmd.getName();
 		
 		if (name.equalsIgnoreCase(COMMAND_NAME)) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("GPS can only be used by players");
 				return true;
 			}
 			
 			try {
 				boolean handled = commandHandler.handleCommand(sender, cmd, label, args);
 				
 				if (!handled) {
 					throw new InvalidCommandException("Invalid command");
 				}
 				
 				return handled;
 			} catch (InvalidCommandException ex) {
 				// FIXME: What is label and how do I get the full command?
 				sender.sendMessage("Invalid command: " + label);
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	// public void printUsage (CommandSender sender)
 	// {
 	// 	sender.sendMessage("Usage of /<command>:");
 	// 	
 	// 	Collection<Action> actions = commandHandler.getActions();
 	// 	Iterator<Action> it = actions.iterator();
 	// 	
 	// 	while (it.hasNext()) {
 	// 		Action action = it.next();
 	// 		// FIXME: Should this do something special for multi-line usage strings?
 	// 		sender.sendMessage(" - " + action.getUsage());
 	// 	}
 	// }
 	
 	/**
 	 * Load the settings file(s), which make gps settings persistent
 	 * across server runs.
 	 */
 	private void loadSettings ()
 	{
 		// Make sure we have all the folders we need for this to work.
 		ensureDataDirectoriesExist();
 		
 		// TODO: Load plugin-wide settings here.
 		
 		loadPlayerConfigurations();
 	}
 	
 	private void loadPlayerConfigurations ()
 	{
 		configurations = new HashMap<String, GPSConfiguration>();
 		
 		String dataPath = getDataFolder().getAbsolutePath();
 		
 		// TODO: Iterate through the player files and load the settings.
 	}
 	
 	private void ensureDataDirectoriesExist ()
 	{
 		File dataFolder = getDataFolder();
 		
 		if (!dataFolder.exists()) {
 			log.info(getLabel() + " GPS data folder does not exist. Creating it now.");
 			dataFolder.mkdir();
 		}
 		
 		File playersFolder = getPlayersFolder();
 		
 		if (!playersFolder.exists()) {
 			log.info(getLabel() + " GPS players folder does not exist. Creating it now.");
 			playersFolder.mkdir();
 		}
 	}
 	
 	private File getPlayersFolder ()
 	{
 		File dataFolder = getDataFolder();
 //		return new File(dataFolder, "players");
 		return new File(dataFolder.getAbsolutePath() + "/players/");
 	}
 	
 	private void setupPermissions ()
 	{
 		// NOTE: Permissions is not supported...yet.
 	}
 	
 	private void setupCommands ()
 	{
 		commandHandler = new CommandHandler(this);
 		
 		commandHandler.addAction(new FindAction(this));
 		commandHandler.addAction(new FollowAction(this));
 		commandHandler.addAction(new PointToAction(this));
 		commandHandler.addAction(new ResetAction(this));
 	}
 }
