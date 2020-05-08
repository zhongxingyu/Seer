 package me.limebyte.battlenight.core;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import me.limebyte.battlenight.core.API.BattleEndEvent;
 import me.limebyte.battlenight.core.Hooks.Metrics;
 import me.limebyte.battlenight.core.Listeners.CheatListener;
 import me.limebyte.battlenight.core.Listeners.CommandBlocker;
 import me.limebyte.battlenight.core.Listeners.DamageListener;
 import me.limebyte.battlenight.core.Listeners.DeathListener;
 import me.limebyte.battlenight.core.Listeners.DisconnectListener;
 import me.limebyte.battlenight.core.Listeners.DropListener;
 import me.limebyte.battlenight.core.Listeners.ReadyListener;
 import me.limebyte.battlenight.core.Listeners.RespawnListener;
 import me.limebyte.battlenight.core.Listeners.SignChanger;
 import me.limebyte.battlenight.core.Listeners.SignListener;
 import me.limebyte.battlenight.core.Other.Tracks.Track;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class BattleNight extends JavaPlugin {
 
 	// Variables
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public static final String BNTag = ChatColor.GRAY + "[BattleNight] "
 			+ ChatColor.WHITE;
 	public static final String BNKTag = ChatColor.GRAY
 			+ "[BattleNight KillFeed] " + ChatColor.WHITE;
 	public static final String Version = "v1.2.1"; // TODO Update
 	public Set<String> ClassList;
 
 	// HashMaps
 	public final Map<String, String> BattleUsersTeam = new HashMap<String, String>();
 	public final Map<String, String> BattleUsersClass = new HashMap<String, String>();
 	public final Map<String, String> BattleClasses = new HashMap<String, String>();
 	public final Map<String, String> BattleArmor = new HashMap<String, String>();
 	public final Map<String, Sign> BattleSigns = new HashMap<String, Sign>();
 	public final Map<String, String> BattleUsersRespawn = new HashMap<String, String>();
 	public final Map<String, String> BattleTelePass = new HashMap<String, String>();
 	public final Map<String, String> BattleSpectators = new HashMap<String, String>();
 
 	// Other Classes
 	private final SignListener signListener = new SignListener(this);
 	private final ReadyListener readyListener = new ReadyListener(this);
 	private final RespawnListener respawnListener = new RespawnListener(this);
 	private final DeathListener deathListener = new DeathListener(this);
 	private final DamageListener damageListener = new DamageListener(this);
 	private final DropListener dropListener = new DropListener(this);
 	private final DisconnectListener disconnectListener = new DisconnectListener(this);
 	private final SignChanger blockListener = new SignChanger(this);
 	private final CheatListener cheatListener = new CheatListener(this);
 	private final CommandBlocker commandBlocker = new CommandBlocker(this);
 	
 	public boolean redTeamIronClicked = false;
 	public boolean blueTeamIronClicked = false;
 	public boolean battleInProgress = false;
 	public boolean playersInLounge = false;
 
 	// config.yml Values
 	public boolean configUsePermissions = false;
 	public boolean configFriendlyFire = false;
 	public boolean configStopHealthRegen = true;
 	public String configInventoryType = "prompt";
 	public int configReadyBlock = 42;
 	public boolean configDebug = false;
 
 	// classes.yml Values
 	public int classesDummyItem = 6;
 
 	// Declare Files and FileConfigurations
 	File configFile;
 	File classesFile;
 	File waypointsFile;
 	File playerFile;
 	public FileConfiguration config;
 	FileConfiguration classes;
 	FileConfiguration waypoints;
 	FileConfiguration players;
 
 	public int redTeam = 0;
 	public int blueTeam = 0;
 
 	// ////////////////////
 	// Plug-in Disable //
 	// ////////////////////
 	@Override
 	public void onDisable() {
 		if (battleInProgress || playersInLounge) {
 			log.info("[BattleNight] Ending current Battle...");
 			endBattle();
 		}
 		this.cleanSigns();
 		PluginDescriptionFile pdfFile = getDescription();
 		log.info("[BattleNight] Version " + pdfFile.getVersion()
 				+ " has been disabled.");
 	}
 
 	// ///////////////////
 	// Plug-in Enable //
 	// ////////////////////
 	@Override
 	public void onEnable() {
 
 		// Initialise Files and FileConfigurations
 		configFile = new File(getDataFolder(), "config.yml");
 		classesFile = new File(getDataFolder(), "classes.yml");
 		waypointsFile = new File(getDataFolder() + "/PluginData", "waypoints.dat");
 		playerFile = new File(getDataFolder() + "/PluginData", "players.dat");
 
 		// Use firstRun(); method
 		try {
 			firstRun();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Declare and Load the FileConfigurations
 		config = new YamlConfiguration();
 		classes = new YamlConfiguration();
 		waypoints = new YamlConfiguration();
 		players = new YamlConfiguration();
 		loadYamls();
 
 		// Event Registration
 		PluginManager pm = getServer().getPluginManager();
 		PluginDescriptionFile pdfFile = getDescription();
 		pm.registerEvents(signListener, this);
 		pm.registerEvents(readyListener, this);
 		pm.registerEvents(respawnListener, this);
 		pm.registerEvents(deathListener, this);
 		pm.registerEvents(dropListener, this);
 		pm.registerEvents(damageListener, this);
 		pm.registerEvents(disconnectListener, this);
 		pm.registerEvents(blockListener, this);
 		pm.registerEvents(cheatListener, this);
 		pm.registerEvents(commandBlocker, this);
 
 		// Metrics
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 		}
 
 		// Configuration
 		configUsePermissions = config.getBoolean("UsePermissions");
 		configFriendlyFire = config.getBoolean("FriendlyFire");
 		configStopHealthRegen = config.getBoolean("StopHealthRegen");
 		configInventoryType = config.getString("InventoryType").toLowerCase();
 		configReadyBlock = config.getInt("ReadyBlock");
 		configDebug = config.getBoolean("Debug");
 
 		classesDummyItem = classes.getInt("DummyItem");
 		for (String className : classes.getConfigurationSection("Classes")
 				.getKeys(false)) {
 			BattleClasses.put(className,
 					classes.getString("Classes." + className + ".Items", null));
 		}
 		for (String className : classes.getConfigurationSection("Classes")
 				.getKeys(false)) {
 			BattleArmor.put(className,
 					classes.getString("Classes." + className + ".Armor", null));
 		}
 		ClassList = classes.getConfigurationSection("Classes").getKeys(false);
 
 		// Debug
 		if (configDebug) {
 			if (configUsePermissions) {
 				log.info("[BattleNight] Permissions Enabled.");
 			} else if (!configUsePermissions) {
 				log.info("[BattleNight] Permissions Disabled, using Op.");
 			} else {
 				log.warning("[BattleNight] Permissions not setup in config!");
 			}
 			log.info("[BattleNight] Classes: " + BattleClasses);
 			log.info("[BattleNight] Armor: " + BattleArmor);
 		}
 
 		// Enable Message
 		log.info("[BattleNight] Version " + pdfFile.getVersion()
 				+ " enabled successfully.");
 		log.info("[BattleNight] Made by LimeByte.");
 	}
 
 	// Fill Configuration Files with Defaults
 	private void firstRun() throws Exception {
 		if (!configFile.exists()) { // Checks If The YAML File Does Not Exist
 			configFile.getParentFile().mkdirs(); // Creates the
 													// /Plugins/BattleNight/
 													// Directory If Not Found
 			copy(getResource("config.yml"), configFile); // Copies the YAML From
 															// Your Jar to the
 															// Folder
 		}
 		if (!classesFile.exists()) {
 			classesFile.getParentFile().mkdirs();
 			copy(getResource("classes.yml"), classesFile);
 		}
 		if (!waypointsFile.exists()) {
 			waypointsFile.getParentFile().mkdirs();
 			copy(getResource("waypoints.dat"), waypointsFile);
 		}
 		if (!playerFile.exists()) {
 			playerFile.getParentFile().mkdirs();
 			copy(getResource("players.dat"), playerFile);
 		}
 	}
 
 	// YAML Copy Method
 	public void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) != -1) {
 				out.write(buf, 0, len);
 			}
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// YAML Load Method
 	public void loadYamls() {
 		try {
 			config.load(configFile);
 			classes.load(classesFile);
 			waypoints.load(waypointsFile);
 			players.load(playerFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void reloadConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException {
         config.load(configFile);
         classes.load(classesFile);
         configUsePermissions = config.getBoolean("UsePermissions");
         configFriendlyFire = config.getBoolean("FriendlyFire");
         configStopHealthRegen = config.getBoolean("StopHealthRegen");
         configInventoryType = config.getString("InventoryType").toLowerCase();
         configReadyBlock = config.getInt("ReadyBlock");
         configDebug = config.getBoolean("Debug");
         classesDummyItem = classes.getInt("DummyItem");
         for (String className : classes.getConfigurationSection("Classes")
                 .getKeys(false)) {
             BattleClasses.put(className,
                     classes.getString("Classes." + className + ".Items", null));
         }
         for (String className : classes.getConfigurationSection("Classes")
                 .getKeys(false)) {
             BattleArmor.put(className,
                     classes.getString("Classes." + className + ".Armor", null));
         }
         ClassList = classes.getConfigurationSection("Classes").getKeys(false);
 	}
 
 	// Waypoints Load Method
 	public void loadWaypoints() {
 		try {
 			waypoints.load(waypointsFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// YAML Save Method
 	public void saveYamls() {
 		try {
 			config.save(configFile);
 			classes.save(classesFile);
 			waypoints.save(waypointsFile);
 			players.save(playerFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void saveYAML(ConfigFile file) {
 		try {
 			if (file.equals(ConfigFile.Main))
 				config.save(configFile);
 			if (file.equals(ConfigFile.Classes))
 				classes.save(classesFile);
 			if (file.equals(ConfigFile.Waypoints))
 				waypoints.save(waypointsFile);
 			if (file.equals(ConfigFile.Players))
 				players.save(playerFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public enum ConfigFile {
 		Main, Classes, Waypoints, Players
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String commandLabel, String[] args) {
 
 		// Player check
 		Player player = null;
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("This command can only be run by a Player.");
 		    return true;
 		}
 		
 		player = (Player) sender;
 
 		if (commandLabel.equalsIgnoreCase("bn")) {
 			if (args.length < 1) {
 				tellPlayer(player, "Type '/bn help' to show the help menu");
 			}
 			if (args.length == 1) {
 
 				if (args[0].equalsIgnoreCase("help")) {
 					if (hasPerm(Perm.ADMIN, player)) {
 						player.sendMessage(ChatColor.DARK_GRAY + " ---------- "
 								+ ChatColor.WHITE + "BattleNight Help Menu"
 								+ ChatColor.DARK_GRAY + " ---------- ");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn help - Shows general help.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn waypoints - Shows set/unset waypoints.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn version - Shows the version of BattleNight in use.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn join - Join the Battle.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn leave - Leave the Battle.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn watch - Watch the Battle.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn kick [player] - Kick a player from the Battle.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn kickall - Kick all players in the Battle.");
 						player.sendMessage(ChatColor.DARK_GRAY
 								+ " --------------------------------------- ");
 					} else if (hasPerm(Perm.USER, player)) {
 						player.sendMessage(ChatColor.DARK_GRAY + " ---------- "
 								+ ChatColor.WHITE + "BattleNight Help Menu"
 								+ ChatColor.DARK_GRAY + " ---------- ");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn help - Shows general help.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn version - Shows the version of BattleNight in use.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn join - Join the Battle.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn leave - Leave the Battle.");
 						player.sendMessage(ChatColor.WHITE
 								+ " /bn watch - Watch the Battle");
 						player.sendMessage(ChatColor.DARK_GRAY
 								+ " --------------------------------------- ");
 					} else {
 						tellPlayer(player, Track.NO_PERMISSION);
 					}
 				}
 
 				else if (args[0].equalsIgnoreCase("waypoints")
 						&& hasPerm(Perm.ADMIN, player)) {
 					player.sendMessage(ChatColor.DARK_GRAY + " ---------- "
 							+ ChatColor.WHITE + "BattleNight Waypoints"
 							+ ChatColor.DARK_GRAY + " ---------- ");
 					player.sendMessage(ChatColor.WHITE + " Setup points: "
 							+ numSetupPoints() + "/6");
 					if (pointSet(WPoint.RED_LOUNGE)) {
 						player.sendMessage(ChatColor.GREEN + " Red Lounge"
 								+ ChatColor.WHITE + " (/bn redlounge)");
 					} else {
 						player.sendMessage(ChatColor.RED + " Red Lounge"
 								+ ChatColor.WHITE + " (/bn redlounge)");
 					}
 					if (pointSet(WPoint.BLUE_LOUNGE)) {
 						player.sendMessage(ChatColor.GREEN + " Blue Lounge"
 								+ ChatColor.WHITE + " (/bn bluelounge)");
 					} else {
 						player.sendMessage(ChatColor.RED + " Blue Lounge"
 								+ ChatColor.WHITE + " (/bn bluelounge)");
 					}
 					if (pointSet(WPoint.RED_SPAWN)) {
 						player.sendMessage(ChatColor.GREEN + " Red Spawn"
 								+ ChatColor.WHITE + " (/bn redspawn)");
 					} else {
 						player.sendMessage(ChatColor.RED + " Red Spawn"
 								+ ChatColor.WHITE + " (/bn redspawn)");
 					}
 					if (pointSet(WPoint.BLUE_SPAWN)) {
 						player.sendMessage(ChatColor.GREEN + " Blue Spawn"
 								+ ChatColor.WHITE + " (/bn bluespawn)");
 					} else {
 						player.sendMessage(ChatColor.RED + " Blue Spawn"
 								+ ChatColor.WHITE + " (/bn bluespawn)");
 					}
 					if (pointSet(WPoint.SPECTATOR)) {
 						player.sendMessage(ChatColor.GREEN + " Spectator"
 								+ ChatColor.WHITE + " (/bn spectator)");
 					} else {
 						player.sendMessage(ChatColor.RED + " Spectator"
 								+ ChatColor.WHITE + " (/bn spectator)");
 					}
 					if (pointSet(WPoint.EXIT)) {
 						player.sendMessage(ChatColor.GREEN + " Exit"
 								+ ChatColor.WHITE + " (/bn exit)");
 					} else {
 						player.sendMessage(ChatColor.RED + " Exit"
 								+ ChatColor.WHITE + " (/bn exit)");
 					}
 					player.sendMessage(ChatColor.DARK_GRAY
 							+ " --------------------------------------- ");
 				}
 
 				else if (args[0].equalsIgnoreCase("join")
 						&& hasPerm(Perm.USER, player)) {
 					if (isSetup() && !battleInProgress
 							&& !BattleUsersTeam.containsKey(player.getName())) {
 						addPlayer(player);
 					} else if (!isSetup()) {
 						tellPlayer(player, Track.WAYPOINTS_UNSET);
 					} else if (battleInProgress) {
 						tellPlayer(player, Track.BATTLE_IN_PROGRESS);
 					} else if (BattleUsersTeam.containsKey(player.getName())) {
 						tellPlayer(player, Track.ALREADY_IN_TEAM);
 					}
 				}
 
 				else if ((args[0].equalsIgnoreCase("watch"))
 						&& hasPerm(Perm.USER, player)) {
 					addSpectator(player, "command");
 				} else if (args[0].equalsIgnoreCase("leave")
 						&& hasPerm(Perm.USER, player)) {
 					if (BattleUsersTeam.containsKey(player.getName())) {
 						removePlayer(player, player.getName(), "has left the Battle.",
 								"You have left the Battle.", true);
 					} else if (BattleSpectators.containsKey(player.getName())) {
 						removeSpectator(player);
 					} else {
 						tellPlayer(player, Track.NOT_IN_TEAM);
 					}
 				}
 
 				else if (args[0].equalsIgnoreCase("kick")
 						&& hasPerm(Perm.MOD, player)) {
 					tellPlayer(player, Track.SPECIFY_PLAYER);
 				}
 
 				else if ((args[0].equalsIgnoreCase("kickall") || args[0]
 						.equalsIgnoreCase("endgame"))
 						&& hasPerm(Perm.MOD, player)) {
 					endBattle();
 					tellPlayer(player, Track.BATTLE_ENDED);
 				}
 
 				else if (args[0].equalsIgnoreCase("redlounge")
 						&& hasPerm(Perm.ADMIN, player)) {
 					setCoords(player, "redlounge");
 					tellPlayer(player, Track.RED_LOUNGE_SET);
 				}
 
 				else if (args[0].equalsIgnoreCase("redspawn")
 						&& hasPerm(Perm.ADMIN, player)) {
 					setCoords(player, "redspawn");
 					tellPlayer(player, Track.RED_SPAWN_SET);
 				}
 
 				else if (args[0].equalsIgnoreCase("bluelounge")
 						&& hasPerm(Perm.ADMIN, player)) {
 					setCoords(player, "bluelounge");
 					tellPlayer(player, Track.BLUE_LOUNGE_SET);
 				}
 
 				else if (args[0].equalsIgnoreCase("bluespawn")
 						&& hasPerm(Perm.ADMIN, player)) {
 					setCoords(player, "bluespawn");
 					tellPlayer(player, Track.BLUE_SPAWN_SET);
 				}
 
 				else if (args[0].equalsIgnoreCase("spectator")
 						&& hasPerm(Perm.ADMIN, player)) {
 					setCoords(player, "spectator");
 					tellPlayer(player, Track.SPECTATOR_SET);
 				}
 
 				else if (args[0].equalsIgnoreCase("exit")
 						&& hasPerm(Perm.ADMIN, player)) {
 					setCoords(player, "exit");
 					tellPlayer(player, Track.EXIT_SET);
 				}
 
 				else if (args[0].equalsIgnoreCase("version")
 						&& hasPerm(Perm.USER, player)) {
 					PluginDescriptionFile pdfFile = getDescription();
 					tellPlayer(
 							player,
 							"This server is currently using Battlenight Version "
 									+ pdfFile.getVersion()
 									+ ".   For more information about Battlenight and the features included in this version, please visit: ");
 					player.sendMessage(pdfFile.getWebsite());
 				}
 				
 				else if (args[0].equalsIgnoreCase("reload") && hasPerm(Perm.ADMIN, player)) {
 				    player.sendMessage(BNTag + "Reloading config...");
 				    try {
                         reloadConfigFiles();
                         player.sendMessage(BNTag + ChatColor.GREEN + "Reloaded successfully.");
                     } catch (Exception e) {
                         e.printStackTrace();
                         player.sendMessage(BNTag + ChatColor.RED + "Reloaded failed.");
                     }
 				}
 
 				else {
 					tellPlayer(player, Track.INVALID_COMAND);
 				}
 			}
 			if (args.length == 2) {
 				if (args[0].equalsIgnoreCase("kick")
 						&& hasPerm(Perm.MOD, player)) {
 					Player badplayer = Bukkit.getPlayerExact(args[1]);
 					if (badplayer.isOnline()) {
 						if (BattleUsersTeam.containsKey(badplayer.getName())) {
 							removePlayer(
 									badplayer, badplayer.getName(),
 									"has been kicked from the current Battle.",
 									"You have been kicked from the current Battle.",
 									true);
 						} else {
 							tellPlayer(player, "Player: " + badplayer.getName()
 									+ " is not in the current Battle.");
 						}
 					} else {
 						tellPlayer(player,
 								"Can't find user " + badplayer.getName()
 										+ ". No kick.");
 					}
 				}
 				
                 else if (args[0].equalsIgnoreCase("test")) {
                     if (player.getName().equals("limebyte")) {
                         // I enjoy jumping extra high :)
                         removePotionEffects(player);
                         player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 4800, Integer.parseInt(args[1])));
                     }
                 }
 			}
 			if (args.length > 2) {
 				tellPlayer(player, Track.INVALID_COMAND);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	// Set Coords and put in waypoints.data
 	public void setCoords(Player player, String place) {
 		Location location = player.getLocation();
 		loadWaypoints();
 		waypoints.set("coords." + place + ".world", location.getWorld().getName());
 		waypoints.set("coords." + place + ".x", location.getX());
 		waypoints.set("coords." + place + ".y", location.getY());
 		waypoints.set("coords." + place + ".z", location.getZ());
 		waypoints.set("coords." + place + ".yaw", location.getYaw());
 		waypoints.set("coords." + place + ".pitch", location.getPitch());
 		saveYAML(ConfigFile.Waypoints);
 	}
 
 	// Get Coords from waypoints.data
 	public Location getCoords(String place) {
 		loadWaypoints();
 		Double x = waypoints.getDouble("coords." + place + ".x", 0);
 		Double y = waypoints.getDouble("coords." + place + ".y", 0);
 		Double z = waypoints.getDouble("coords." + place + ".z", 0);
 		String yawToParse = waypoints.getString("coords." + place + ".yaw");
 		float yaw = 0;
 		if (yawToParse != null) {
 			try {
 				yaw = Float.parseFloat(yawToParse);
 			} catch (NumberFormatException nfe) {
 				// log it, do whatever you want, it's not a float. Maybe give it
 				// a default value
 			}
 		}
 		String pitchToParse = waypoints.getString("coords." + place + ".pitch");
 		float pitch = 0;
 		if (pitchToParse != null) {
 			try {
 				pitch = Float.parseFloat(pitchToParse);
 			} catch (NumberFormatException nfe) {
 				// log it, do whatever you want, it's not a float. Maybe give it
 				// a default value
 			}
 		}
 		World world = Bukkit.getServer().getWorld(
 				waypoints.getString("coords." + place + ".world"));
		return new Location(world, x, y, z, yaw, pitch);
 	}
 
 	private enum WPoint {
 		RED_LOUNGE("redlounge"), RED_SPAWN("redspawn"), BLUE_LOUNGE(
 				"bluelounge"), BLUE_SPAWN("bluespawn"), SPECTATOR("spectator"), EXIT(
 				"exit");
 
 		private WPoint(String name) {
 			this.name = name;
 		}
 
 		private final String name;
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 
 	public boolean pointSet(WPoint waypoint) {
 		loadWaypoints();
 		try {
 			Set<String> set = waypoints.getConfigurationSection("coords")
 					.getKeys(false);
 			List<String> setpoints = new ArrayList<String>(set);
 			if (setpoints.contains(waypoint.name)) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (NullPointerException e) {
 			return false;
 		}
 	}
 
 	// Check if all Waypoints have been set.
 	public Boolean isSetup() {
 		loadWaypoints();
 		if (!waypoints.isSet("coords")) {
 			return false;
 		} else {
 			Set<String> set = waypoints.getConfigurationSection("coords")
 					.getKeys(false);
 			List<String> list = new ArrayList<String>(set);
 			if (list.size() == 6) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 
 	public int numSetupPoints() {
 		loadWaypoints();
 		if (!waypoints.isSet("coords")) {
 			return 0;
 		} else {
 			Set<String> set = waypoints.getConfigurationSection("coords")
 					.getKeys(false);
 			List<String> list = new ArrayList<String>(set);
 			return list.size();
 		}
 	}
 
 	// Give Player Class Items
 	public void giveItems(Player player) {
 		String playerClass = BattleUsersClass.get(player.getName());
 		String rawItems = BattleClasses.get(playerClass);
 		String ArmorList = BattleArmor.get(playerClass);
 		String[] items;
 		items = rawItems.split(",");
 		for (int i = 0; i < items.length; i++) {
 			String item = items[i];
 			player.getInventory().setItem(i, parseItem(item));
 			if (player.getInventory().contains(classesDummyItem)) {
 				player.getInventory().remove(classesDummyItem);
 			}
 		}
 		// Set Armor
 		// Helmets
 		if (ArmorList.contains("298")) {
 			player.getInventory().setHelmet(new ItemStack(298, 1));
 		} else if (ArmorList.contains("302")) {
 			player.getInventory().setHelmet(new ItemStack(302, 1));
 		} else if (ArmorList.contains("306")) {
 			player.getInventory().setHelmet(new ItemStack(306, 1));
 		} else if (ArmorList.contains("310")) {
 			player.getInventory().setHelmet(new ItemStack(310, 1));
 		} else if (ArmorList.contains("314")) {
 			player.getInventory().setHelmet(new ItemStack(314, 1));
 		}
 		// Chestplates
 		if (ArmorList.contains("299")) {
 			player.getInventory().setChestplate(new ItemStack(299, 1));
 		} else if (ArmorList.contains("303")) {
 			player.getInventory().setChestplate(new ItemStack(303, 1));
 		} else if (ArmorList.contains("307")) {
 			player.getInventory().setChestplate(new ItemStack(307, 1));
 		} else if (ArmorList.contains("311")) {
 			player.getInventory().setChestplate(new ItemStack(311, 1));
 		} else if (ArmorList.contains("315")) {
 			player.getInventory().setChestplate(new ItemStack(315, 1));
 		}
 		// Leggings
 		if (ArmorList.contains("300")) {
 			player.getInventory().setLeggings(new ItemStack(300, 1));
 		} else if (ArmorList.contains("304")) {
 			player.getInventory().setLeggings(new ItemStack(304, 1));
 		} else if (ArmorList.contains("308")) {
 			player.getInventory().setLeggings(new ItemStack(308, 1));
 		} else if (ArmorList.contains("312")) {
 			player.getInventory().setLeggings(new ItemStack(312, 1));
 		} else if (ArmorList.contains("316")) {
 			player.getInventory().setLeggings(new ItemStack(316, 1));
 		}
 		// Boots
 		if (ArmorList.contains("301")) {
 			player.getInventory().setBoots(new ItemStack(301, 1));
 		} else if (ArmorList.contains("305")) {
 			player.getInventory().setBoots(new ItemStack(305, 1));
 		} else if (ArmorList.contains("309")) {
 			player.getInventory().setBoots(new ItemStack(309, 1));
 		} else if (ArmorList.contains("313")) {
 			player.getInventory().setBoots(new ItemStack(313, 1));
 		} else if (ArmorList.contains("317")) {
 			player.getInventory().setBoots(new ItemStack(317, 1));
 		}
 	}
 
 	// Clean Up All Signs People Have Used For Classes
 	public void cleanSigns() {
 		Set<String> set = BattleSigns.keySet();
 		Iterator<String> iter = set.iterator();
 		while (iter.hasNext()) {
 			Object o = iter.next();
 			Sign sign = BattleSigns.get(o.toString());
 			sign.setLine(2, "");
 			sign.setLine(3, "");
 			sign.update();
 		}
 	}
 
 	// Clean Up Signs Specific Player Has Used For Classes
 	public void cleanSigns(String player) {
 		Set<String> set = BattleSigns.keySet();
 		Iterator<String> iter = set.iterator();
 		while (iter.hasNext()) {
 			Object o = iter.next();
 			Sign sign = BattleSigns.get(o.toString());
 			if (sign.getLine(2) == player) {
 				sign.setLine(2, "");
 				sign.update();
 			}
 			if (sign.getLine(3) == player) {
 				sign.setLine(3, "");
 				sign.update();
 			}
 		}
 	}
 
 	public boolean teamReady(String colour) {
 		int members = 0;
 		int membersReady = 0;
 		Set<String> set = BattleUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while (iter.hasNext()) {
 			Object o = iter.next();
 			if (BattleUsersTeam.get(o.toString()) == colour) {
 				members++;
 				if (BattleUsersClass.containsKey(o.toString())) {
 					membersReady++;
 				}
 			}
 		}
 		if (members == membersReady && members > 0) {
 			if (colour == "red") {
 				return true;
 			}
 			if (colour == "blue") {
 				return true;
 			}
 		} else {
 			return false;
 		}
 		return false;
 	}
 
 	public void tellEveryone(String msg) {
 		for (String name : BattleUsersTeam.keySet()) {
 			if (Bukkit.getPlayer(name) != null) Bukkit.getPlayer(name).sendMessage(BNTag + msg);
 		}
 	}
 	
 	public void tellEveryone(Track track) {
 		for (String name : BattleUsersTeam.keySet()) {
 			if (Bukkit.getPlayer(name) != null) Bukkit.getPlayer(name).sendMessage(BNTag + track.msg);
 		}
 	}
 
 	public void killFeed(String msg) {
 		LinkedList<Player> told = new LinkedList<Player>();
 
 		for (String name : BattleUsersTeam.keySet()) {
 			if (Bukkit.getPlayer(name) != null) {
 				Player currentPlayer = Bukkit.getPlayer(name);
 				currentPlayer.sendMessage(BNTag + msg);
 				told.add(currentPlayer);
 			}
 		}
 
 		for (String name : BattleSpectators.keySet()) {
 			if (Bukkit.getPlayer(name) != null) {
 				Player currentPlayer = Bukkit.getPlayer(name);
 				if (!told.contains(currentPlayer)) {
 					currentPlayer.sendMessage(BNTag + msg);
 					told.add(currentPlayer);
 				}
 			}
 		}
 		
 		told.clear();
 	}
 
 	public void tellEveryoneExcept(Player player, String msg) {
 		for (String name : BattleUsersTeam.keySet()) {
 			if (Bukkit.getPlayer(name) != null) {
 				Player currentPlayer = Bukkit.getPlayer(name);
 				if (currentPlayer != player) currentPlayer.sendMessage(BNTag + msg);
 			}
 		}
 	}
 
 	public void tellTeam(String colour, String msg) {
 		for (String name : BattleUsersTeam.keySet()) {
 			if (Bukkit.getPlayer(name) != null) {
 				Player currentPlayer = Bukkit.getPlayer(name);
 				if (BattleUsersTeam.get(name) == colour) currentPlayer.sendMessage(BNTag + msg);
 			}
 		}
 	}
 
 	public void tellTeam(String colour, Track track) {
 		for (String name : BattleUsersTeam.keySet()) {
 			if (Bukkit.getPlayer(name) != null) {
 				Player currentPlayer = Bukkit.getPlayer(name);
 				if (BattleUsersTeam.get(name) == colour) currentPlayer.sendMessage(BNTag + track.msg);
 			}
 		}
 	}
 	
 	public void tellPlayer(Player player, String msg) {
 		player.sendMessage(BNTag + msg);
 	}
 
 	public void tellPlayer(Player player, Track track) {
 		player.sendMessage(BNTag + track.msg);
 	}
 
 	public void teleportAllToSpawn() {
 		Set<String> set = BattleUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while (iter.hasNext()) {
 			Object o = iter.next();
 			if (BattleUsersTeam.get(o.toString()) == "red") {
 				Player z = getServer().getPlayer(o.toString());
 				goToWaypoint(z, "redspawn");
 			}
 			if (BattleUsersTeam.get(o.toString()) == "blue") {
 				Player z = getServer().getPlayer(o.toString());
 				goToWaypoint(z, "bluespawn");
 			}
 		}
 	}
 
 	public boolean emptyInventory(Player player) {
 		ItemStack[] invContents = player.getInventory().getContents();
 		ItemStack[] armContents = player.getInventory().getArmorContents();
 		int invNullCounter = 0;
 		int armNullCounter = 0;
 		for (int i = 0; i < invContents.length; i++) {
 			if (invContents[i] == null) {
 				invNullCounter++;
 			}
 		}
 		for (int i = 0; i < armContents.length; i++) {
 			if (armContents[i].getType() == Material.AIR) {
 				armNullCounter++;
 			}
 		}
 		return (invNullCounter == invContents.length)
 				&& (armNullCounter == armContents.length);
 	}
 
 	public void goToWaypoint(Player player, String place) {
 		BattleTelePass.put(player.getName(), "yes");
 		player.teleport(getCoords(place));
 		BattleTelePass.remove(player.getName());
 	}
 
 	public enum Perm {
 		ADMIN, MOD, USER
 	}
 
 	public boolean hasPerm(BattleNight.Perm perm, Player player) {
 		if (perm.equals(Perm.ADMIN)) {
 			if ((configUsePermissions && player
 					.hasPermission("battlenight.admin"))
 					|| (!configUsePermissions && player.isOp())) {
 				return true;
 			} else if ((configUsePermissions && !player
 					.hasPermission("battlenight.admin"))
 					|| (!configUsePermissions && !player.isOp())) {
 				tellPlayer(player, Track.NO_PERMISSION);
 				return false;
 			} else {
 				tellPlayer(player, Track.CONFIG_UNSET);
 				return false;
 			}
 		}
 		if (perm.equals(Perm.MOD)) {
 			if ((configUsePermissions && player
 					.hasPermission("battlenight.moderator"))
 					|| (!configUsePermissions && player.isOp())) {
 				return true;
 			} else if ((configUsePermissions && !player
 					.hasPermission("battlenight.moderator"))
 					|| (!configUsePermissions && !player.isOp())) {
 				tellPlayer(player, Track.NO_PERMISSION);
 				return false;
 			} else {
 				tellPlayer(player, Track.CONFIG_UNSET);
 				return false;
 			}
 		} else if (perm.equals(Perm.USER)) {
 			if ((configUsePermissions && player
 					.hasPermission("battlenight.user"))
 					|| !configUsePermissions) {
 				return true;
 			} else if (configUsePermissions
 					&& !player.hasPermission("battlenight.user")) {
 				tellPlayer(player, Track.NO_PERMISSION);
 				return false;
 			} else {
 				tellPlayer(player, Track.CONFIG_UNSET);
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public static ItemStack parseItem(String rawItem) {
 		if (rawItem == null || rawItem.equals(""))
 			return null;
 
 		String[] part1 = rawItem.split("x");
 		String[] part2 = part1[0].split(":");
 		String item = part2[0];
 		if (part1.length == 1) {
 			if (part2.length == 1) {
 				return parseItemWithoutData(item, "1");
 			} else if (part2.length == 2) {
 				String data = part2[1];
 				return parseItemWithData(item, data);
 			}
 		} else if (part1.length == 2) {
 			String amount = part1[1];
 			if (part2.length == 1) {
 				return parseItemWithoutData(item, amount);
 			} else if (part2.length == 2) {
 				String data = part2[1];
 				return parseItemWithData(item, data, amount);
 			}
 		}
 		return null;
 	}
 
 	private static ItemStack parseItemWithoutData(String item, String amount) {
 		Material m = Material.getMaterial(Integer.parseInt(item));
 		int a = Integer.parseInt(amount);
 		if (a > m.getMaxStackSize()) {
 			log.warning("[BattleNight] You attempted to set the item:" + m
 					+ " to have a greater stack size than possible.");
 			a = m.getMaxStackSize();
 		}
 		return new ItemStack(m, a);
 	}
 
 	private static ItemStack parseItemWithData(String item, String data) {
 		int i = Integer.parseInt(item);
 		short d = Short.parseShort(data);
 
 		return new ItemStack(i, 1, d);
 	}
 
 	private static ItemStack parseItemWithData(String item, String data,
 			String amount) {
 		Material m = Material.getMaterial(Integer.parseInt(item));
 		byte d = Byte.parseByte(data);
 		int a = Integer.parseInt(amount);
 		if (a > m.getMaxStackSize()) {
 			log.warning("[BattleNight] You attempted to set the item:" + m
 					+ " to have a greater stack size than possible.");
 			a = m.getMaxStackSize();
 		}
 		return new ItemStack(m, a, d);
 	}
 
 	public void addSpectator(Player player, String type) {
 		if (type.equals("death")) {
 			BattleSpectators.put(player.getName(), "death");
 			tellPlayer(player, Track.WELCOME_SPECTATOR_DEATH);
 		} else {
 			if (isSetup() && battleInProgress) {
 				if (BattleUsersTeam.containsKey(player.getName())) {
 					removePlayer(player, player.getName(), "has left the Battle.",
 							"You have left the Battle.", false);
 				}
 				goToWaypoint(player, "spectator");
 				BattleSpectators.put(player.getName(), "command");
 				tellPlayer(player, Track.WELCOME_SPECTATOR);
 				return;
 			} else if (!isSetup()) {
 				tellPlayer(player, Track.WAYPOINTS_UNSET);
 				return;
 			} else if (!battleInProgress) {
 				tellPlayer(player, Track.BATTLE_NOT_IN_PROGRESS);
 				return;
 			}
 		}
 	}
 
 	public void addPlayer(Player player) {
 		if (preparePlayer(player)) {
 			if (blueTeam > redTeam) {
 				goToWaypoint(player, "redlounge");
 				BattleUsersTeam.put(player.getName(), "red");
 				tellPlayer(player, "Welcome! You are on team " + ChatColor.RED
 						+ "<Red>");
 				tellEveryoneExcept(player, player.getName()
 						+ " has joined team " + ChatColor.RED + "<Red>");
 				redTeam += 1;
 				playersInLounge = true;
 			} else {
 				goToWaypoint(player, "bluelounge");
 				BattleUsersTeam.put(player.getName(), "blue");
 				tellPlayer(player, "Welcome! You are on team " + ChatColor.BLUE
 						+ "<Blue>");
 				tellEveryoneExcept(player, player.getName()
 						+ " has joined team " + ChatColor.BLUE + "<Blue>");
 				blueTeam += 1;
 				playersInLounge = true;
 			}
 		} else {
 			tellPlayer(player, Track.MUST_HAVE_EMPTY);
 		}
 	}
 
 	public void removePlayer(Player player, String name, String message1, String message2,
 			boolean teleport) {
 		if (BattleUsersTeam.containsKey(player.getName())) {
 			if (BattleUsersTeam.get(player.getName()) == "red") {
 				redTeam = redTeam - 1;
 				if (message1 != null) {
 					tellEveryoneExcept(player, ChatColor.RED + player.getName()
 							+ ChatColor.WHITE + " " + message1);
 				}
 			}
 			if (BattleUsersTeam.get(player.getName()) == "blue") {
 				blueTeam = blueTeam - 1;
 				if (message1 != null) {
 					tellEveryoneExcept(player,
 							ChatColor.BLUE + player.getName() + ChatColor.WHITE
 									+ " " + message1);
 				}
 			}
 			if (message2 != null) {
 				tellPlayer(player, message2);
 			}
 			
 			// If red or blue won
 			if (((redTeam > 0) && (blueTeam == 0)) || ((redTeam == 0) && (blueTeam > 0))) {
 				if ((redTeam > 0) && (blueTeam == 0) && (BattleUsersTeam.get(player.getName()) == "blue")) {
 					tellEveryone(Track.RED_WON);
 					Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("red", "blue", BattleUsersTeam));
 				} else if ((redTeam == 0) && (blueTeam > 0) && (BattleUsersTeam.get(player.getName()) == "red")) {
 					tellEveryone(Track.BLUE_WON);
 					Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("blue", "red", BattleUsersTeam));
 				}
 				try{player.getInventory().clear();} catch (Exception e) {}
 				try {clearArmorSlots(player);} catch (Exception e) {}
 				try{removePotionEffects(player);} catch (Exception e) {}
 				BattleUsersTeam.remove(player.getName());
 				BattleUsersClass.remove(player.getName());
 				if (teleport) goToWaypoint(player, "exit");
 				restorePlayer(player, name);
 				Set<String> set = BattleUsersTeam.keySet();
 				Iterator<String> iter = set.iterator();
 				while (iter.hasNext()) {
 					Object o = iter.next();
 					Player z = getServer().getPlayer(o.toString());
 					try{z.getInventory().clear();} catch (Exception e) {}
 					try {clearArmorSlots(z);} catch (Exception e) {}
 					try{removePotionEffects(z);} catch (Exception e) {}
 					if (teleport) goToWaypoint(z, "exit");
 					restorePlayer(z, z.getName());
 				}
 				removeAllSpectators();
 				cleanSigns();
 				battleInProgress = false;
 				redTeamIronClicked = false;
 				blueTeamIronClicked = false;
 				BattleUsersTeam.clear();
 				BattleUsersClass.clear();
 				redTeam = 0;
 				blueTeam = 0;
 				BattleSigns.clear();
 			// There was only one player
 			} else if ((redTeam == 0) && (blueTeam == 0)) {
 				Set<String> set = BattleUsersTeam.keySet();
 				Iterator<String> iter = set.iterator();
 				while (iter.hasNext()) {
 					Object o = iter.next();
 					Player z = getServer().getPlayer(o.toString());
 					try {clearArmorSlots(z);} catch (Exception e) {}
 					try{removePotionEffects(z);} catch (Exception e) {}
 					try{z.getInventory().clear();} catch (Exception e) {}
 					if (teleport) goToWaypoint(z, "exit");
 					restorePlayer(z, name);
 				}
 				Bukkit.getServer().getPluginManager()
 						.callEvent(new BattleEndEvent("draw", "draw", null));
 				cleanSigns();
 				battleInProgress = false;
 				redTeamIronClicked = false;
 				blueTeamIronClicked = false;
 				BattleUsersTeam.clear();
 				BattleUsersClass.clear();
 				redTeam = 0;
 				blueTeam = 0;
 				BattleSigns.clear();
 			} else {
 				cleanSigns(player.getName());
 				try{player.getInventory().clear();} catch (Exception e) {}
 				try {clearArmorSlots(player);} catch (Exception e) {}
 				try{removePotionEffects(player);} catch (Exception e) {}
 				BattleUsersTeam.remove(player.getName());
 				BattleUsersClass.remove(player.getName());
 				restorePlayer(player, name);
 			}
 		} else {
 			BattleNight.log.info("[BattleNight] Failed to remove player '"
 					+ player.getName()
 					+ "' from the Battle as they are not in it.");
 		}
 	}
 
 	public void removeSpectator(Player player) {
 		goToWaypoint(player, "exit");
 		BattleSpectators.remove(player.getName());
 		tellPlayer(player, Track.GOODBYE_SPECTATOR);
 	}
 
 	// TODO Isolate winning players
 	public void removeAllPlayers() {
 		if (redTeam > blueTeam) {
 			tellEveryone(Track.RED_WON);
 			Bukkit.getServer().getPluginManager()
 					.callEvent(new BattleEndEvent("red", "blue", null));
 		} else if (redTeam < blueTeam) {
 			tellEveryone(Track.BLUE_WON);
 			Bukkit.getServer().getPluginManager()
 					.callEvent(new BattleEndEvent("blue", "red", null));
 		} else if ((redTeam == blueTeam) && ((redTeam > 0) || (blueTeam > 0))) {
 			tellEveryone(Track.DRAW);
 			Bukkit.getServer().getPluginManager()
 					.callEvent(new BattleEndEvent("draw", "draw", null));
 		} else {
 			return;
 		}
 		Set<String> set = BattleUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while (iter.hasNext()) {
 			Object o = iter.next();
 			Player z = getServer().getPlayer(o.toString());
 			z.getInventory().clear();
 			clearArmorSlots(z);
 			removePotionEffects(z);
 			goToWaypoint(z, "exit");
 			restorePlayer(z, z.getName());
 		}
 		cleanSigns();
 		battleInProgress = false;
 		redTeamIronClicked = false;
 		blueTeamIronClicked = false;
 		BattleUsersTeam.clear();
 		BattleUsersClass.clear();
 		redTeam = 0;
 		blueTeam = 0;
 		BattleSigns.clear();
 	}
 
 	public void removeAllSpectators() {
 		Set<String> set = BattleSpectators.keySet();
 		Iterator<String> iter = set.iterator();
 		while (iter.hasNext()) {
 			Object o = iter.next();
 			Player z = getServer().getPlayer(o.toString());
 			goToWaypoint(z, "exit");
 		}
 		BattleSpectators.clear();
 	}
 
 	public void endBattle() {
 		removeAllPlayers();
 		removeAllSpectators();
 	}
 
 	public void clearArmorSlots(Player player) {
 	    PlayerInventory inv = player.getInventory();
 		inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
 	}
 
 	private boolean preparePlayer(Player p) {
 		if (config.getString("InventoryType").equalsIgnoreCase("prompt")
 				&& !emptyInventory(p))
 			return false;
 
 		String name = p.getName();
 
 		if (!players.contains(name)) {
 			players.set(name + ".stats.games", 0);
 			players.set(name + ".stats.kills", 0);
 			players.set(name + ".stats.deaths", 0);
 		}
 
 		int Gamemode = 0;
 		if (p.getGameMode().equals(GameMode.CREATIVE))
 			Gamemode = 1;
 
 		players.set(name + ".saves.exp", p.getExp());
 		players.set(name + ".saves.fireticks", p.getFireTicks());
 		players.set(name + ".saves.foodlevel", p.getFoodLevel());
 		players.set(name + ".saves.gamemode", Gamemode);
 		players.set(name + ".saves.health", p.getHealth());
 		players.set(name + ".saves.level", p.getLevel());
 		players.set(name + ".saves.remainingair", p.getRemainingAir());
 		players.set(name + ".saves.saturation", p.getSaturation());
 		players.set(name + ".saves.totalexperience", p.getTotalExperience());
 
 		if (config.getString("InventoryType").equalsIgnoreCase("save")) {
 			players.set(name + ".saves.inventory.main", p.getInventory()
 					.getContents());
 			players.set(name + ".saves.inventory.armor", p.getInventory()
 					.getArmorContents());
 		}
 
 		saveYAML(ConfigFile.Players);
 
 		// Reset Player
 		p.setExp(0);
 		p.setFireTicks(-20);
 		if (config.getBoolean("StopHealthRegen"))
 			p.setFoodLevel(16);
 		else
 			p.setFoodLevel(18);
 		p.setGameMode(GameMode.SURVIVAL);
 		p.setHealth(p.getMaxHealth());
 		p.setLevel(0);
 		p.setRemainingAir(300);
 		p.setSaturation(5);
 		p.setTotalExperience(0);
 		removePotionEffects(p);
 		p.getInventory().clear();
 		clearArmorSlots(p);
 		return true;
 	}
 
 	private void restorePlayer(Player p, String name) {
 		try {
 			GameMode Gamemode = GameMode.SURVIVAL;
 			if (players.getInt(name + ".saves.gamemode", 0) == 1)
 				Gamemode = GameMode.CREATIVE;
 
 			p.setExp((Float) players.get(name + ".saves.exp", 0));
 			p.setFireTicks(players.getInt(name + ".saves.fireticks", -20));
 			p.setFoodLevel(players.getInt(name + ".saves.foodlevel", 18));
 			p.setGameMode(Gamemode);
 			p.setHealth(players.getInt(name + ".saves.health", p.getMaxHealth()));
 			p.setLevel(players.getInt(name + ".saves.level", 0));
 			p.setRemainingAir(players.getInt(name + ".saves.remainingair", 300));
 			p.setSaturation(players.getInt(name + ".saves.saturation", 5));
 			p.setTotalExperience(players.getInt(
 					name + ".saves.totalexperience", 0));
 
 			if (config.getString("InventoryType").equalsIgnoreCase("save")) {
 				p.getInventory().setContents(
 						(ItemStack[]) players.get(name
 								+ ".saves.inventory.main"));
 				p.getInventory().setArmorContents(
 						(ItemStack[]) players.get(name
 								+ ".saves.inventory.armor"));
 			}
 		} catch (NullPointerException e) {
 			log.warning("[BattleNight] Failed to restore data for player: "
 					+ name + ".");
 		}
 	}
 	
 	public void removePotionEffects(Player p) {
 	    for(PotionEffect effect : p.getActivePotionEffects()) {
 	        p.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);
 	    }
     }
 }
