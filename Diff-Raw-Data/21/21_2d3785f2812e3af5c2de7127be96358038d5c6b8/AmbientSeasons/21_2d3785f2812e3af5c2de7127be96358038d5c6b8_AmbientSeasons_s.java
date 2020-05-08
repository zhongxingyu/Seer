 /*
  * This file is part of AmbientSeasons (https://github.com/Olloth/AmbientSeasons).
  * 
  * AmbientSeasons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.ambientseasons;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import me.ambientseasons.listener.BlockGrow;
 import me.ambientseasons.listener.BlockPlaceListener;
 import me.ambientseasons.listener.SListener;
 import me.ambientseasons.util.ASConfig;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
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
 	public static String PREFIX = "[AmbientSeasons] ";
 	public static boolean DEBUG = false;
 
 	// Wheat Modifier (Modified growing times based on season)
 	public static boolean WHEAT_MOD;
 	public Set<Location> WheatBlockLocations;
 	public WheatMod wheatMod;
 
 	private PluginDescriptionFile info;
 	private PluginManager pm;
 	private SListener sListener;
 	private Calendar calendar;
 	private ASConfig config;
 	private BlockPlaceListener blockPlace;
 	private BlockGrow blockGrow;
 	private HashMap<String, Boolean> HUDEnable;
 
 	/**
 	 * Calls when the plugin disables.
 	 */
 	public void onDisable() {
 
 		config.saveMap();
 		info("version " + info.getVersion() + " is now Disabled.");
 	}
 
 	/**
 	 * Calls on activation of the plugin.
 	 */
 	public void onEnable() {
 		info = getDescription();
 		pm = getServer().getPluginManager();
 
 		HUDEnable = new HashMap<String, Boolean>();
 
 		config = new ASConfig(this);
 
 		WHEAT_MOD = false; // TEMP (Load from config in future)
 
 		if (WHEAT_MOD) {
 			blockPlace = new BlockPlaceListener(this);
 			blockGrow = new BlockGrow(this);
 
 			WheatBlockLocations = new HashSet<Location>();
 			wheatMod = new WheatMod(this);
 		}
 
 		// Initialize listeners
 		calendar = new Calendar(this);
 		sListener = new SListener(this, calendar);
 
 		// Register events
 		pm.registerEvent(Type.CUSTOM_EVENT, sListener, Priority.Low, this);
 		if (WHEAT_MOD) {
 			pm.registerEvent(Type.BLOCK_PLACE, blockPlace, Priority.Low, this);
 			pm.registerEvent(Type.BLOCK_PHYSICS, blockGrow, Priority.Highest, this);
 		}
 
 		// Send an enable message
 		info("version " + info.getVersion() + " is now Enabled.");
 
 	}
 
 	/**
 	 * Calls when a player uses a command.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		String commandName = command.getName().toLowerCase();
 		boolean commandSuccess = false;
 
 		// Only Players
 		if (!(sender instanceof Player)) {
 			sender.sendMessage(PREFIX + "/" + commandName + " can only be run from in game.");
 			commandSuccess = true;
 		}
 
 		if (commandName.equals("ashud")) {
 			commandSuccess = toggleHUD((Player) sender);
 
 		}
 
 		if (commandName.equals("ashelp")) {
 			commandSuccess = help((Player) sender);
 		}
 
 		if (commandName.equals("asdate")) {
 			commandSuccess = date((Player) sender);
 		}
 
 		return commandSuccess;
 
 	}
 
 	public boolean toggleHUD(Player player) {
 		if (HUDEnable.containsKey(player.getName())) {
 			if (HUDEnable.get(player.getName())) {
 				HUDEnable.put(player.getName(), false);
 				player.sendMessage(ChatColor.GREEN + "AmbientSeason's HUD disabled.");
 				player.sendMessage(ChatColor.WHITE + "Type " + ChatColor.GREEN + "/ashud" + ChatColor.WHITE + " to enable it again.");
 			} else {
 				HUDEnable.put(player.getName(), true);
 				player.sendMessage(ChatColor.GREEN + "AmbientSeason's HUD enabled.");
 				player.sendMessage(ChatColor.WHITE + "Type " + ChatColor.GREEN + "/ashud" + ChatColor.WHITE + " to disable it.");
 			}
 		}
 		return true;
 	}
 
 	public boolean help(Player player) {
 		player.sendMessage("");
 		player.sendMessage(ChatColor.GREEN + "Welcome to " + ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "AmbientSeasons" + ChatColor.WHITE + "]" + ChatColor.GREEN + ".");
 		player.sendMessage(ChatColor.RED + "/asHUD" + ChatColor.WHITE + " Command toggles your clientside HUD.");
 		player.sendMessage(ChatColor.RED + "/asDate" + ChatColor.WHITE + " Command displays your current date.");
 
 		return true;
 
 	}
 
 	public boolean date(Player player) {
 		SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
 		if(getASConfig().isWorldEnabled(sPlayer.getWorld())) {
 			String date = calendar.getTimes(player).getDate();
 			if (sPlayer.isSpoutCraftEnabled()) {
 				date = calendar.getTimes(player).getShortDate();
 				sPlayer.sendNotification("Current Date", date, Material.WATCH);
 			} else {
 				sPlayer.sendMessage("Current Date: " + date);
 			}
 
 			return true;
 		}
 		
 		sPlayer.sendMessage("No calendar for this world: " + sPlayer.getWorld().getName()); 
 		return true;
 	}
 
 	public void info(String info) {
 		log.log(Level.INFO, PREFIX + info);
 	}
 	public ASConfig getASConfig() {
 		return config;
 	}
 
 	public Calendar getCalendar() {
 		return calendar;
 	}
 
 	public Map<String, Boolean> getHUDEnable() {
 		return HUDEnable;
 	}
 
 	public void setHUDEnable(HashMap<String, Boolean> HUDEnable) {
 		this.HUDEnable = HUDEnable;
 	}

 }
