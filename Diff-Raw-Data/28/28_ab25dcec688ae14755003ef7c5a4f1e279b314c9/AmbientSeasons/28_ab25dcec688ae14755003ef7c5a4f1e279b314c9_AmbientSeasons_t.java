 package me.ambientseasons;
 
 import java.awt.Color;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import me.ambientseasons.listener.BlockGrow;
 import me.ambientseasons.listener.BlockPlaceListener;
 import me.ambientseasons.listener.Players;
 import me.ambientseasons.listener.SListener;
 import me.ambientseasons.util.Config;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /**
  * Seasons Plugin for Bukkit and Spout
  * 
  * Let's the server have a custom calendar and rotating seasons based on it.
  * 
  * @author Olloth, LordNed
  * 
  */
 public class AmbientSeasons extends JavaPlugin {
 
 	// Setup Bukkit's Logger
 	public static final Logger log = Logger.getLogger("Minecraft");
         public static boolean DEBUG = true;
 
 	// Wheat Modifier (Modified growing times based on season)
 	public static boolean WHEAT_MOD;
 	public List<Location> WheatBlockLocations;
 	public WheatMod wheatMod;
 
 	public static String PREFIX = "[AmbientSeasons] ";
 	private PluginDescriptionFile info;
 	private PluginManager pm;
 	private File directory;
 	private Players players;
 	private SListener sListener;
 	private BlockPlaceListener blockPlace;
 	private BlockGrow blockGrow;
 	public static HashMap<String, UUID> labels;
 	public static HashMap<String, Boolean> HUDEnable;
 
 	/**
 	 * Calls when the plugin disables.
 	 */
 	public void onDisable() {
 
 		Config.saveMap();
 		System.out.println("[" + info.getName() + "] is now disabled!");
 	}
 
 	/**
 	 * Calls on activation of the plugin.
 	 */
 	public void onEnable() {
 		directory = getDataFolder();
 		info = getDescription();
 		pm = getServer().getPluginManager();
 
 		// Initialize listeners
 		players = new Players(this);
 		sListener = new SListener(this);
 		blockPlace = new BlockPlaceListener(this);
 		blockGrow = new BlockGrow(this);
 
 		WHEAT_MOD = true; // TEMP (Load from config in future)
 		WheatBlockLocations = new ArrayList<Location>();
 		wheatMod = new WheatMod(this);
 
 		labels = new HashMap<String, UUID>();
 		HUDEnable = new HashMap<String, Boolean>();
 
 		// Load the Config
 		Config.configSetup(directory);
 
 		// Register events
 		pm.registerEvent(Type.PLAYER_JOIN, players, Priority.Low, this);
 		pm.registerEvent(Type.CUSTOM_EVENT, sListener, Priority.Low, this);
 		pm.registerEvent(Type.BLOCK_PLACE, blockPlace, Priority.Low, this);
 		pm.registerEvent(Type.BLOCK_PHYSICS, blockGrow, Priority.Highest, this);
 
 		// Update any players who were online (In case of /reload)
 		players.playersInit(getServer().getOnlinePlayers());
 		sListener.updateHud();
 
 		// Send an enable message
 		System.out.println("[" + info.getName() + "] version " + info.getVersion() + " is now enabled!");
 
 	}
 
 	/**
 	 * Calls when a player uses a command.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		String commandName = command.getName().toLowerCase();
 
                //Only Players
                if (!(sender instanceof Player))
                {
                    sender.sendMessage(PREFIX + "/" + commandName + " can only be run from in game.");
                    return true;
                }
 		/*
 		 * Checks to see if the command is /ashud If so, it toggles the players
 		 * HUD on or off.
 		 */
 		if (commandName.equals("ashud")) {
 			Player player = (Player) sender;
 			if (HUDEnable.containsKey(player.getName())) {
 				if (HUDEnable.get(player.getName())) {
 					HUDEnable.put(player.getName(), false);
 					UUID labelId = labels.get(player.getName());
 					SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
 					sPlayer.getMainScreen().getWidget(labelId).setVisible(false).setDirty(true);
                                         sender.sendMessage(ChatColor.GREEN + "AmbientSeason's HUD disabled.");
                                         sender.sendMessage(ChatColor.WHITE + "Type " + ChatColor.GREEN + "/ashud" + ChatColor.WHITE + " to enable it again.");
 				} else {
 					HUDEnable.put(player.getName(), true);
 					UUID labelId = labels.get(player.getName());
 					SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
 					sPlayer.getMainScreen().getWidget(labelId).setVisible(true).setDirty(true);
                                         sender.sendMessage(ChatColor.GREEN + "AmbientSeason's HUD enabled.");
                                         sender.sendMessage(ChatColor.WHITE + "Type " + ChatColor.GREEN + "/ashud" + ChatColor.WHITE + " to disable it.");
 				}
 			}
                        return true;
 		}
                 
                 if (commandName.equals("ashelp"))
                 {
                     Player player = (Player) sender;
                    sender.sendMessage("");
                     sender.sendMessage(ChatColor.GREEN + "Welcome to " + ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "AmbientSeasons" + ChatColor.WHITE + "]" + ChatColor.GREEN + ".");
                    sender.sendMessage(ChatColor.RED + "/asHUD" + ChatColor.WHITE + " Command toggles your clientside HUD.");
                     sender.sendMessage(ChatColor.RED + "Crops not growing?" + ChatColor.WHITE + " Crops prefer moderate biomes,");
                     sender.sendMessage(ChatColor.WHITE + "and will grow slower in extreme biomes.");
                     
                    return true;
                    
                     
                 }
 		return false;
 	}
 }
