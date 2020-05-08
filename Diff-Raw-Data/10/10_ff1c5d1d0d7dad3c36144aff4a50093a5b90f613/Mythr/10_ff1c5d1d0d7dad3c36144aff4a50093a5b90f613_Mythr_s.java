 package org.andfRa.mythr;
 
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Set;
 
 import org.andfRa.mythr.commands.AdminCommands;
 import org.andfRa.mythr.commands.StatsCommands;
 import org.andfRa.mythr.config.AttributeConfiguration;
 import org.andfRa.mythr.config.CreatureConfiguration;
 import org.andfRa.mythr.config.ItemConfiguration;
 import org.andfRa.mythr.config.ResponseConfiguration;
 import org.andfRa.mythr.config.SkillConfiguration;
 import org.andfRa.mythr.dependencies.PermissionsDependency;
 import org.andfRa.mythr.inout.Directory;
 import org.andfRa.mythr.inout.FileIO;
 import org.andfRa.mythr.listeners.EntityListener;
 import org.andfRa.mythr.listeners.InventoryListener;
 import org.andfRa.mythr.listeners.PlayerListener;
 import org.andfRa.mythr.player.MythrPlayer;
 import org.andfRa.mythr.player.SpellManager;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.sk89q.CommandPermissionsException;
 import org.sk89q.CommandUsageException;
 import org.sk89q.CommandsManager;
 import org.sk89q.MissingNestedCommandException;
 import org.sk89q.UnhandledCommandException;
 import org.sk89q.WrappedCommandException;
 
 
 
 public class Mythr extends JavaPlugin{
 
 	/** Plugin instance. */
     private static Mythr plugin;
 
     /**
      * Gets the plugin instance.
      * 
      * @return plugin instance
      */
     public static Mythr plugin()
      { return plugin; }
 
     
 	/** All Mythr players. */
 	private Hashtable<String, MythrPlayer> mplayers = new Hashtable<String, MythrPlayer>();
 	
 	
 	// INITIALISATION:
     /* 
      * Disables the plugin.
      * 
      * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
      */
     @Override
     public void onDisable()
      {
     	// Notify:
 		MythrLogger.info("Disabling Mythr.");
 
 		// Players:
 		unloadAllMythrPlayers();
 
 		// Managers:
 		SpellManager.unload();
 		
 		// Dependencies:
 		PermissionsDependency.disable();
 
 		// Configuration:
 		ItemConfiguration.load();
 		ResponseConfiguration.unload();
 		CreatureConfiguration.unload();
 		SkillConfiguration.unload();
 		AttributeConfiguration.load();
 		
 		// Instances:
 		Mythr.plugin = null;
 
 		// Notify:
 		MythrLogger.info("Mythr disabled.");
 
 		// Logger:
 		MythrLogger.unload();
     }
 
     /* 
      * (non-Javadoc)
      * 
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public void onEnable()
      {
 		// Instance:
 		Mythr.plugin = this;
 
 		// Logger:
 		MythrLogger.load();
 
 		// Notify:
 		MythrLogger.info("Enabling Mythr.");
 
 		// Configuration:
 		AttributeConfiguration.load();
 		SkillConfiguration.load();
 		CreatureConfiguration.load();
 		ResponseConfiguration.load();
 		ItemConfiguration.load();
 		
 		// Dependencies:
 		PermissionsDependency.enable();
 		
 		// Managers:
 		SpellManager.load();
 		
 		// Register events:
 		final PluginManager pluginManager = getServer().getPluginManager();
 		pluginManager.registerEvents(new PlayerListener(), this);
 		pluginManager.registerEvents(new EntityListener(), this);
 		pluginManager.registerEvents(new InventoryListener(), this);
 
 		// Register commands:
 		CommandsManager<Player> commandMap = PermissionsDependency.getCommandMap();
 		commandMap.register(AdminCommands.class);
 		commandMap.register(StatsCommands.class);
 
 		// Load players:
 		loadAllMythrPlayers();
 		
 		// Enable automatic saving:
 
 		// Notify:
 		MythrLogger.info("Mythr enabled.");
 		
 		
 	}
 
     
     // PLAYERS:
     /**
      * Gets loaded player.
      * 
      * @param name player name
      * @return loaded player, null if none
      */
     public MythrPlayer getLoadedPlayer(String name)
      { return mplayers.get(name.toLowerCase()); }
     
     /**
      * Gets player. Reads the player data if needed.
      * 
      * @param name player name
      * @return requested player, null if doesn't exist
      */
     public MythrPlayer requestPlayer(String name)
      {
     	MythrPlayer mplayer = getLoadedPlayer(name);
     	if(mplayer != null) return mplayer;
     	
     	if(!FileIO.exists(Directory.PLAYER_DATA, name)) return null;
     	
     	return MythrPlayer.load(name);
      }
     
 	/**
 	 * Loads a Mythr player.
 	 * Creates a new player if needed.
 	 * 
 	 * @param name player name
 	 * @return loaded player
 	 */
 	public MythrPlayer loadMythrPlayer(String name)
 	 {
 		MythrPlayer mplayer = mplayers.get(name.toLowerCase());
 
 		// Already loaded:
 		if (mplayer != null) return mplayer;
 
 		// Load:
 		mplayer = MythrPlayer.load(name);
		MythrLogger.info("Loading Mythr player for " + name + ".");
 		mplayers.put(name.toLowerCase(), mplayer);
 		
 		return mplayer;
 	 }
 
 	/**
 	 * Unloads a Mythr player.
 	 * 
 	 * @param name player name
 	 * @return unloaded player, null if not loaded
 	 */
 	public MythrPlayer unloadMythrPlayer(String name)
 	 {
 		MythrPlayer mplayer = mplayers.get(name.toLowerCase());
 
 		// Already unloaded:
 		if (mplayer == null) return mplayer;
 
 		// Unload:
		MythrLogger.info("Unloading Mythr player for " + name + ".");
 		mplayers.remove(name.toLowerCase());
 
 		// Unload:
 		mplayer.unload();
 
 		return mplayer;
 	 }
 
 	/**
 	 * Unloads all Mythr players.
 	 */
 	private void unloadAllMythrPlayers()
 	 {
		Set<String> names = mplayers.keySet();
 		
 		for (String name : names) {
 			MythrPlayer mplayer = unloadMythrPlayer(name);
 			if(mplayer != null) mplayer.unwrap();
 		}
 		
 		mplayers.clear();
 	 }
 
 	/**
 	 * Loads all Mythr players.
 	 */
 	private void loadAllMythrPlayers()
 	 {
 		Player[] players = getServer().getOnlinePlayers();
 
 		for (int i = 0; i < players.length; i++) {
 			MythrPlayer mplayer = loadMythrPlayer(players[i].getName());
 			if (mplayer != null) mplayer.wrapPlayer(players[i]);
 		}
 	 }
     
 	/**
 	 * Matches a Mythr player.
 	 * 
 	 * @param name name to match to
 	 * @return matched Mythr player, null if not found
 	 */
 	public MythrPlayer matchPlayer(String name)
 	 {
 		Collection<MythrPlayer> mplayers = this.mplayers.values();
 		
 		// Try full match:
 		for (MythrPlayer mplayer : mplayers) {
 			if(mplayer.getName().equalsIgnoreCase(name)) return mplayer;
 		}
 
 		// Try starts with:
 		for (MythrPlayer mplayer : mplayers) {
 			if(mplayer.getName().toLowerCase().startsWith(name.toLowerCase())) return mplayer;
 		}
 		
 		return null;
 	 }
 	
 	
     
     // COMMANDS:
     /**
      * Handles a command
      * 
      * @param player player
      * @param split split arguments
      * @param command command
      * @return true if successful
      */
     public static boolean handleCommand(Player player, String[] split, String command) {
 
 		CommandsManager<Player> commandMap = PermissionsDependency.getCommandMap();
 		
 		// Handle:
 		try {
 
 			split[0] = split[0].substring(1);
 
 			// Quick script shortcut:
 			if (split[0].matches("^[^/].*\\.js$")) {
 
 				String[] newSplit = new String[split.length + 1];
 				System.arraycopy(split, 0, newSplit, 1, split.length);
 				newSplit[0] = "cs";
 				newSplit[1] = newSplit[1];
 				split = newSplit;
 
 			}
 
 			// Check for command:
 			if (!commandMap.hasCommand(split[0])) return false;
 
 			try {
 				
 				MythrPlayer mplayer = plugin().getLoadedPlayer(player.getName());
 
 				commandMap.execute(split, player, mplayer);
 				MythrLogger.info("[Mythr Command] " + player.getName() + ": " + command);
 
 			}
 			catch (CommandPermissionsException e) {
 
 				player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
 
 			}
 			catch (MissingNestedCommandException e) {
 
 				player.sendMessage(e.getUsage());
 
 			}
 			catch (CommandUsageException e) {
 
 				if (e.getMessage() != null) player.sendMessage(e.getMessage());
 				player.sendMessage(e.getUsage());
 
 			}
 			catch (WrappedCommandException e) {
 
 				if (e.getMessage() != null) player.sendMessage(ChatColor.RED + e.getMessage());
 				throw e;
 
 			}
 			catch (UnhandledCommandException e) {
 
 				player.sendMessage(ChatColor.RED + "Unhandled command exception");
 				return false;
 
 			}
 			finally {
 
 			}
 
 		}
 		catch (Throwable t) {
 
 			player.sendMessage("Failed to handle command: " + command);
 			if (t.getMessage() != null) player.sendMessage(t.getMessage());
 			t.printStackTrace();
 			return false;
 
 		}
 
 		return true;
 
 		
     }
 
 	
 }
