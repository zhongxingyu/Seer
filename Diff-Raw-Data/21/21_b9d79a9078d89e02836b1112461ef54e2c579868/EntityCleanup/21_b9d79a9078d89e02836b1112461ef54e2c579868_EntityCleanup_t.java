 package eu.creationation.lukeer31;
 
 import java.util.Iterator;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EntityCleanup extends JavaPlugin {
 
 	// Config options
 	public static boolean PauseCleanup;
 	public static java.util.List<Integer> Entities;
 	public static Long RepeatTime;
 	public static Long RunTime;
 	public static java.util.List<String> Worlds;
 	public static String CommandPrefix;
 	public static String CommandPrefixColor;
 	public static boolean EnableLog;
 
 	public void onEnable() {
 		// Load the config
 		loadConfig();
 		// Start the clearing loop
 		startCleanup();
 	}
 
 	public void onDisable() {
 		// Stop the cleanup loop
 		PauseCleanup = true;
		// Sort out the config
		unloadConfig();
 	}
 
 	public void loadConfig() {
 		// Save the default config if it does not exist already
 		saveDefaultConfig();
 		// Save the config incase of changes
 		saveConfig();
 		// Load the configuration options
 		PauseCleanup = false;
 		RepeatTime = getConfig().getLong("Interval");
 		RunTime = getConfig().getLong("RunTime");
 		// Backup measure just in case a noob sets the run time higher than
 		// The interval
 		if (RunTime >= RepeatTime) {
 			RunTime = RepeatTime / 2;
 		}
 		// Get the list of worlds it will run on
 		Worlds = getConfig().getStringList("Worlds");
 		// Get the list of ignored entities
 		Entities = getConfig().getIntegerList("Entities");
 		// Command colors etc
 		CommandPrefix = getConfig().getString("CommandPrefix");
 		CommandPrefixColor = getConfig().getString("CommandPrefixColor");
 		// Logging
 		EnableLog = getConfig().getBoolean("EnableLog");
 	}
	
	public void unloadConfig(){
		//Reload the configuration incase of changes
		reloadConfig();
		//Save the config
		saveConfig();
	}
 
 	// Commands for the plugin
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getName().equalsIgnoreCase("EC")) {
 			if (args.length == 0) {
 				return false;
 			}
 			String command = args[0];
 			if (command.equalsIgnoreCase("toggle")) {
 				// Change the values of PausedCleanup to true to stop it
 				// removing entities
 				if (PauseCleanup == false) {
 					PauseCleanup = true;
 					sender.sendMessage("" + CommandPrefixColor + "["
 							+ CommandPrefix
 							+ "] Entity removal has been paused");
 					return true;
 				} else if (PauseCleanup == true) {
 					PauseCleanup = false;
 					sender.sendMessage("" + CommandPrefixColor + "["
 							+ CommandPrefix
 							+ "] Entity removal has been resumed");
 					return true;
 				}
 			} else if (command.equalsIgnoreCase("reload")) {
 				// reload the config and resume the cleanup
 				loadConfig();
 				sender.sendMessage(""
 						+ CommandPrefixColor
 						+ "["
 						+ CommandPrefix
 						+ "] Configuration reloaded, and entity removal has been resumed");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void startCleanup() {
 		// Start the cleanup loop
 		getServer().getScheduler().scheduleSyncRepeatingTask(this,
 				new Runnable() {
 					@Override
 					public void run() {
 						// Run the cleanup
 						cleanup();
 					}
 				}, RepeatTime, RepeatTime);
 	}
 
 	public void cleanup() {
 		// Check that cleanup is not paused
 		if (!PauseCleanup) {
 			// Loop through the worlds
 			Iterator<String> worldit = Worlds.iterator();
 			while (worldit.hasNext()) {
 				// Get the world
 				World world = getServer().getWorld(worldit.next());
 				// Check it exists
 				if (world != null) {
 					// Pass the world to the other method to clear the entities
 					entityRemoval(world);
 				}
 			}
 		}
 	}
 
 	public void entityRemoval(World world) {
 		// Loop through the entities
 		for (Entity e : world.getEntities()) {
 			int id = e.getType().getTypeId();
 			// Make sure it's not a player
 			if (!(e instanceof Player)) {
 				// Check it is in the list
 				if (canBeRemoved(id)) {
 					e.remove();
 				}
 			}
 		}
 	}
 
 	public boolean canBeRemoved(int id) {
 		Iterator<Integer> iter = Entities.iterator();
 		while (iter.hasNext()) {
 			int i = iter.next();
 			if (id == i) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
