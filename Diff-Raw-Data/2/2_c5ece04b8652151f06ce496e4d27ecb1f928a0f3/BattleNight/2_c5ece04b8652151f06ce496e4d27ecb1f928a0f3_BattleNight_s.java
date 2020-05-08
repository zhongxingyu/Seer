 package me.limebyte.battlenight.core;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import me.limebyte.battlenight.core.Hooks.Metrics;
 import me.limebyte.battlenight.core.Hooks.Nameplates;
 import me.limebyte.battlenight.core.Listeners.CheatListener;
 import me.limebyte.battlenight.core.Listeners.CommandBlocker;
 import me.limebyte.battlenight.core.Listeners.DamageListener;
 import me.limebyte.battlenight.core.Listeners.DeathListener;
 import me.limebyte.battlenight.core.Listeners.DisconnectListener;
 import me.limebyte.battlenight.core.Listeners.DropListener;
 import me.limebyte.battlenight.core.Listeners.NameplateListener;
 import me.limebyte.battlenight.core.Listeners.ReadyListener;
 import me.limebyte.battlenight.core.Listeners.RespawnListener;
 import me.limebyte.battlenight.core.Listeners.SignChanger;
 import me.limebyte.battlenight.core.Listeners.SignListener;
 import me.limebyte.battlenight.core.Other.Tracks.Track;
 import me.limebyte.battlenight.core.Other.Waypoint;
 import me.limebyte.battlenight.core.commands.WaypointCommand;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
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
 import org.kitteh.tag.TagAPI;
 
 public class BattleNight extends JavaPlugin {
 
     // Variables
     public static Logger log;
     public static final String BNTag = ChatColor.GRAY + "[BattleNight] " + ChatColor.WHITE;
     public static final String BNKTag = ChatColor.GRAY + "[BattleNight KillFeed] " + ChatColor.WHITE;
     public Set<String> ClassList;
 
     // HashMaps
     public final Map<String, Team> BattleUsersTeam = new HashMap<String, Team>();
     public final Map<String, String> BattleUsersClass = new HashMap<String, String>();
     public final Map<String, String> BattleClasses = new HashMap<String, String>();
     public final Map<String, String> BattleArmor = new HashMap<String, String>();
     public final Map<String, Sign> BattleSigns = new HashMap<String, Sign>();
     public final Map<String, String> BattleUsersRespawn = new HashMap<String, String>();
     public final Map<String, String> BattleTelePass = new HashMap<String, String>();
     public final Map<String, String> BattleSpectators = new HashMap<String, String>();
 
     // Other Classes
     public final Battle battle = new Battle(this);
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
     static File configFile;
     static File classesFile;
     static File waypointsFile;
     static File playerFile;
     public static FileConfiguration config;
     static FileConfiguration classes;
     static FileConfiguration waypoints;
     static FileConfiguration players;
 
     // ////////////////////
     // Plug-in Disable //
     // ////////////////////
     @Override
     public void onDisable() {
         if (battleInProgress || playersInLounge) {
             log.info("Ending current Battle...");
             battle.end();
         }
         this.cleanSigns();
         final PluginDescriptionFile pdfFile = getDescription();
         log.info("Version " + pdfFile.getVersion() + " has been disabled.");
     }
 
     // ///////////////////
     // Plug-in Enable //
     // ////////////////////
     @Override
     public void onEnable() {
 
         // Set instances
         log = getLogger();
 
         // Initialise Files and FileConfigurations
         configFile = new File(getDataFolder(), "config.yml");
         classesFile = new File(getDataFolder(), "classes.yml");
         waypointsFile = new File(getDataFolder() + "/PluginData", "waypoints.dat");
         playerFile = new File(getDataFolder() + "/PluginData", "players.dat");
 
         // Use firstRun(); method
         try {
             firstRun();
         } catch (final Exception e) {
             e.printStackTrace();
         }
 
         // Declare and Load the FileConfigurations
         config = new YamlConfiguration();
         classes = new YamlConfiguration();
         waypoints = new YamlConfiguration();
         players = new YamlConfiguration();
         loadYamls();
 
         // Metrics
         try {
             final Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (final IOException e) {
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
         for (final String className : classes.getConfigurationSection("Classes")
                 .getKeys(false)) {
             BattleClasses.put(className,
                     classes.getString("Classes." + className + ".Items", null));
         }
         for (final String className : classes.getConfigurationSection("Classes")
                 .getKeys(false)) {
             BattleArmor.put(className,
                     classes.getString("Classes." + className + ".Armor", null));
         }
         ClassList = classes.getConfigurationSection("Classes").getKeys(false);
 
         // Debug
         if (configDebug) {
             if (configUsePermissions) {
                 log.info("Permissions Enabled.");
             } else if (!configUsePermissions) {
                 log.info("Permissions Disabled, using Op.");
             } else {
                 log.warning("Permissions not setup in config!");
             }
             log.info("Classes: " + BattleClasses);
             log.info("Armor: " + BattleArmor);
         }
 
         final PluginManager pm = getServer().getPluginManager();
 
         if (Nameplates.init(this)) {
             // Event Registration
             final PluginDescriptionFile pdfFile = getDescription();
             pm.registerEvents(new NameplateListener(this), this);
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
 
             // Enable Message
             log.info("Version " + pdfFile.getVersion() + " enabled successfully.");
             log.info("Made by LimeByte.");
         } else {
             pm.disablePlugin(this);
         }
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
     public static void copy(InputStream in, File file) {
         try {
             final OutputStream out = new FileOutputStream(file);
             final byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) != -1) {
                 out.write(buf, 0, len);
             }
             out.close();
             in.close();
         } catch (final Exception e) {
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
         } catch (final Exception e) {
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
         for (final String className : classes.getConfigurationSection("Classes")
                 .getKeys(false)) {
             BattleClasses.put(className,
                     classes.getString("Classes." + className + ".Items", null));
         }
         for (final String className : classes.getConfigurationSection("Classes")
                 .getKeys(false)) {
             BattleArmor.put(className,
                     classes.getString("Classes." + className + ".Armor", null));
         }
         ClassList = classes.getConfigurationSection("Classes").getKeys(false);
     }
 
     // Waypoints Load Method
     public static void loadWaypoints() {
         try {
             waypoints.load(waypointsFile);
         } catch (final Exception e) {
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
         } catch (final IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void saveYAML(ConfigFile file) {
         try {
             if (file.equals(ConfigFile.Main))
                 config.save(configFile);
             if (file.equals(ConfigFile.Classes))
                 classes.save(classesFile);
             if (file.equals(ConfigFile.Waypoints))
                 waypoints.save(waypointsFile);
             if (file.equals(ConfigFile.Players))
                 players.save(playerFile);
         } catch (final IOException e) {
             e.printStackTrace();
         }
     }
 
     public enum ConfigFile {
         Main, Classes, Waypoints, Players
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 
         if (commandLabel.equalsIgnoreCase("bn")) {
             if (args.length < 1) {
                 sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "Incorrect usage.  Type '/bn help' to show the help menu.");
                 return true;
             } else if (args[0].equalsIgnoreCase("set")) {
                 WaypointCommand cmd = new WaypointCommand(sender, args);
                 cmd.perform();
             } else if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("help")) {
                     if (hasPerm(Perm.ADMIN, sender)) {
                         sender.sendMessage(ChatColor.DARK_GRAY + " ---------- "
                                 + ChatColor.WHITE + "BattleNight Help Menu"
                                 + ChatColor.DARK_GRAY + " ---------- ");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn help - Shows general help.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn waypoints - Shows set/unset waypoints.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn version - Shows the version of BattleNight in use.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn join - Join the Battle.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn leave - Leave the Battle.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn watch - Watch the Battle.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn kick [player] - Kick a player from the Battle.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn kickall - Kick all players in the Battle.");
                         sender.sendMessage(ChatColor.DARK_GRAY
                                 + " --------------------------------------- ");
                     } else if (hasPerm(Perm.USER, sender)) {
                         sender.sendMessage(ChatColor.DARK_GRAY + " ---------- "
                                 + ChatColor.WHITE + "BattleNight Help Menu"
                                 + ChatColor.DARK_GRAY + " ---------- ");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn help - Shows general help.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn version - Shows the version of BattleNight in use.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn join - Join the Battle.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn leave - Leave the Battle.");
                         sender.sendMessage(ChatColor.WHITE
                                 + " /bn watch - Watch the Battle");
                         sender.sendMessage(ChatColor.DARK_GRAY
                                 + " --------------------------------------- ");
                     } else {
                         sender.sendMessage(BattleNight.BNTag + Track.NO_PERMISSION.msg);
                     }
                 }
 
                 else if (args[0].equalsIgnoreCase("waypoints") && hasPerm(Perm.ADMIN, sender)) {
                     sender.sendMessage(ChatColor.DARK_GRAY + " ---------- "
                             + ChatColor.WHITE + "BattleNight Waypoints"
                             + ChatColor.DARK_GRAY + " ---------- ");
                     sender.sendMessage(ChatColor.WHITE + " Setup points: "
                             + numSetupPoints() + "/6");
                     if (pointSet(Waypoint.RED_LOUNGE)) {
                         sender.sendMessage(ChatColor.GREEN + " Red Lounge"
                                 + ChatColor.WHITE + " (/bn redlounge)");
                     } else {
                         sender.sendMessage(ChatColor.RED + " Red Lounge"
                                 + ChatColor.WHITE + " (/bn redlounge)");
                     }
                     if (pointSet(Waypoint.BLUE_LOUNGE)) {
                         sender.sendMessage(ChatColor.GREEN + " Blue Lounge"
                                 + ChatColor.WHITE + " (/bn bluelounge)");
                     } else {
                         sender.sendMessage(ChatColor.RED + " Blue Lounge"
                                 + ChatColor.WHITE + " (/bn bluelounge)");
                     }
                     if (pointSet(Waypoint.RED_SPAWN)) {
                         sender.sendMessage(ChatColor.GREEN + " Red Spawn"
                                 + ChatColor.WHITE + " (/bn redspawn)");
                     } else {
                         sender.sendMessage(ChatColor.RED + " Red Spawn"
                                 + ChatColor.WHITE + " (/bn redspawn)");
                     }
                     if (pointSet(Waypoint.BLUE_SPAWN)) {
                         sender.sendMessage(ChatColor.GREEN + " Blue Spawn"
                                 + ChatColor.WHITE + " (/bn bluespawn)");
                     } else {
                         sender.sendMessage(ChatColor.RED + " Blue Spawn"
                                 + ChatColor.WHITE + " (/bn bluespawn)");
                     }
                     if (pointSet(Waypoint.SPECTATOR)) {
                         sender.sendMessage(ChatColor.GREEN + " Spectator"
                                 + ChatColor.WHITE + " (/bn spectator)");
                     } else {
                         sender.sendMessage(ChatColor.RED + " Spectator"
                                 + ChatColor.WHITE + " (/bn spectator)");
                     }
                     if (pointSet(Waypoint.EXIT)) {
                         sender.sendMessage(ChatColor.GREEN + " Exit"
                                 + ChatColor.WHITE + " (/bn exit)");
                     } else {
                         sender.sendMessage(ChatColor.RED + " Exit"
                                 + ChatColor.WHITE + " (/bn exit)");
                     }
                     sender.sendMessage(ChatColor.DARK_GRAY
                             + " --------------------------------------- ");
                 }
 
                 else if (args[0].equalsIgnoreCase("join") && hasPerm(Perm.USER, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     if (isSetup() && !battleInProgress && !BattleUsersTeam.containsKey(player.getName())) {
                         battle.addPlayer(player);
                     } else if (!isSetup()) {
                         tellPlayer(player, Track.WAYPOINTS_UNSET);
                     } else if (battleInProgress) {
                         tellPlayer(player, Track.BATTLE_IN_PROGRESS);
                     } else if (BattleUsersTeam.containsKey(player.getName())) {
                         tellPlayer(player, Track.ALREADY_IN_TEAM);
                     }
                 }
 
                 else if ((args[0].equalsIgnoreCase("watch")) && hasPerm(Perm.USER, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     addSpectator(player, "command");
                 } else if (args[0].equalsIgnoreCase("leave") && hasPerm(Perm.USER, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     if (BattleUsersTeam.containsKey(player.getName())) {
                         battle.removePlayer(player, false, "has left the Battle.", "You have left the Battle.");
                     } else if (BattleSpectators.containsKey(player.getName())) {
                         removeSpectator(player);
                     } else {
                         tellPlayer(player, Track.NOT_IN_TEAM.msg);
                     }
                 }
 
                 else if (args[0].equalsIgnoreCase("kick") && hasPerm(Perm.MOD, sender)) {
                     sender.sendMessage(BattleNight.BNTag + ChatColor.RED + Track.SPECIFY_PLAYER.msg);
                 }
 
                 else if ((args[0].equalsIgnoreCase("kickall") || args[0].equalsIgnoreCase("endgame")) && hasPerm(Perm.MOD, sender)) {
                     battle.end();
                     sender.sendMessage(BattleNight.BNTag + ChatColor.RED + Track.BATTLE_ENDED.msg);
                 }
 
                 else if (args[0].equalsIgnoreCase("redlounge") && hasPerm(Perm.ADMIN, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     setCoords(player, "redlounge");
                     tellPlayer(player, Track.RED_LOUNGE_SET);
                 }
 
                 else if (args[0].equalsIgnoreCase("redspawn") && hasPerm(Perm.ADMIN, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     setCoords(player, "redspawn");
                     tellPlayer(player, Track.RED_SPAWN_SET);
                 }
 
                 else if (args[0].equalsIgnoreCase("bluelounge") && hasPerm(Perm.ADMIN, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     setCoords(player, "bluelounge");
                     tellPlayer(player, Track.BLUE_LOUNGE_SET);
                 }
 
                 else if (args[0].equalsIgnoreCase("bluespawn") && hasPerm(Perm.ADMIN, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     setCoords(player, "bluespawn");
                     tellPlayer(player, Track.BLUE_SPAWN_SET);
                 }
 
                 else if (args[0].equalsIgnoreCase("spectator") && hasPerm(Perm.ADMIN, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BattleNight.BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     setCoords(player, "spectator");
                     tellPlayer(player, Track.SPECTATOR_SET);
                 }
 
                 else if (args[0].equalsIgnoreCase("exit") && hasPerm(Perm.ADMIN, sender)) {
                     // Player check
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(BNTag + ChatColor.RED + "This command can only be run by a Player.");
                         return true;
                     }
                     Player player = (Player) sender;
                     
                     setCoords(player, "exit");
                     tellPlayer(player, Track.EXIT_SET);
                 }
 
                 else if (args[0].equalsIgnoreCase("version")) {
                     PluginDescriptionFile pdfFile = getDescription();
                     String[] msgs = {
                             BNTag + "This server is currently using Battlenight Version " + pdfFile.getVersion() + ".",
                            "For more information about Battlenight and the features included in this version, please visit: ",
                             ChatColor.DARK_AQUA + pdfFile.getWebsite()
                     };
                     sender.sendMessage(msgs);
                 }
 
                 else if (args[0].equalsIgnoreCase("reload") && hasPerm(Perm.ADMIN, sender)) {
                     sender.sendMessage(BNTag + "Reloading config...");
                     try {
                         reloadConfigFiles();
                         sender.sendMessage(BNTag + ChatColor.GREEN + "Reloaded successfully.");
                     } catch (final Exception e) {
                         e.printStackTrace();
                         sender.sendMessage(BNTag + ChatColor.RED + "Reload failed.");
                     }
                 }
 
                 else {
                     sender.sendMessage(BNTag + ChatColor.RED + Track.INVALID_COMAND.msg);
                 }
             }
             if (args.length == 2) {
                 if (args[0].equalsIgnoreCase("kick") && hasPerm(Perm.MOD, sender)) {
                     Player badplayer = Bukkit.getPlayerExact(args[1]);
                     if (badplayer.isOnline()) {
                         if (BattleUsersTeam.containsKey(badplayer.getName())) {
                             battle.removePlayer(badplayer, false, "has been kicked from the current Battle.", "You have been kicked from the current Battle.");
                         } else {
                             sender.sendMessage(BNTag + ChatColor.RED + "Player: " + badplayer.getName() + " is not in the current Battle.");
                         }
                     } else {
                         sender.sendMessage(BNTag + ChatColor.RED + "Can't find user " + badplayer.getName() + ". No kick.");
                     }
                 }
             }
             if (args.length > 2) {
                 sender.sendMessage(BNTag + ChatColor.RED + Track.INVALID_COMAND.msg);
             }
             return true;
         }
         return false;
     }
 
     // Set Coords and put in waypoints.data
     public void setCoords(Player player, String place) {
         final Location location = player.getLocation();
         loadWaypoints();
         waypoints.set("coords." + place + ".world", location.getWorld().getName());
         waypoints.set("coords." + place + ".x", location.getX());
         waypoints.set("coords." + place + ".y", location.getY());
         waypoints.set("coords." + place + ".z", location.getZ());
         waypoints.set("coords." + place + ".yaw", location.getYaw());
         waypoints.set("coords." + place + ".pitch", location.getPitch());
         saveYAML(ConfigFile.Waypoints);
     }
 
     // Set Coords and put in waypoints.data
     public static void setCoords(Waypoint waypoint, Location location) {
         String place = waypoint.getName();
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
         final Double x = waypoints.getDouble("coords." + place + ".x", 0);
         final Double y = waypoints.getDouble("coords." + place + ".y", 0);
         final Double z = waypoints.getDouble("coords." + place + ".z", 0);
         final String yawToParse = waypoints.getString("coords." + place + ".yaw");
         float yaw = 0;
         if (yawToParse != null) {
             try {
                 yaw = Float.parseFloat(yawToParse);
             } catch (final NumberFormatException nfe) {
                 // log it, do whatever you want, it's not a float. Maybe give it
                 // a default value
             }
         }
         final String pitchToParse = waypoints.getString("coords." + place + ".pitch");
         float pitch = 0;
         if (pitchToParse != null) {
             try {
                 pitch = Float.parseFloat(pitchToParse);
             } catch (final NumberFormatException nfe) {
                 // log it, do whatever you want, it's not a float. Maybe give it
                 // a default value
             }
         }
         final World world = Bukkit.getServer().getWorld(
                 waypoints.getString("coords." + place + ".world"));
         return new Location(world, x, y, z, yaw, pitch);
     }
 
     public boolean pointSet(Waypoint waypoint) {
         loadWaypoints();
         try {
             final Set<String> set = waypoints.getConfigurationSection("coords")
                     .getKeys(false);
             final List<String> setpoints = new ArrayList<String>(set);
             if (setpoints.contains(waypoint.getName())) {
                 return true;
             } else {
                 return false;
             }
         } catch (final NullPointerException e) {
             return false;
         }
     }
 
     // Check if all Waypoints have been set.
     public Boolean isSetup() {
         loadWaypoints();
         if (!waypoints.isSet("coords")) {
             return false;
         } else {
             final Set<String> set = waypoints.getConfigurationSection("coords")
                     .getKeys(false);
             final List<String> list = new ArrayList<String>(set);
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
             final Set<String> set = waypoints.getConfigurationSection("coords")
                     .getKeys(false);
             final List<String> list = new ArrayList<String>(set);
             return list.size();
         }
     }
 
     // Give Player Class Items
     public void giveItems(Player player) {
         final String playerClass = BattleUsersClass.get(player.getName());
         final String rawItems = BattleClasses.get(playerClass);
         final String ArmorList = BattleArmor.get(playerClass);
         String[] items;
         items = rawItems.split(",");
         for (int i = 0; i < items.length; i++) {
             final String item = items[i];
             player.getInventory().setItem(i, parseItem(item));
             if (player.getInventory().contains(classesDummyItem)) {
                 player.getInventory().remove(classesDummyItem);
             }
         }
         // Set Armour
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
         for (final Entry<String, Sign> entry : BattleSigns.entrySet()) {
             if (entry.getValue() != null) {
                 final Sign currentSign = entry.getValue();
                 currentSign.setLine(2, "");
                 currentSign.setLine(3, "");
                 currentSign.update();
             }
         }
     }
 
     // Clean Up Signs Specific Player Has Used For Classes
     public void cleanSigns(Player player) {
         for (final Entry<String, Sign> entry : BattleSigns.entrySet()) {
             if (entry.getValue() != null && player != null) {
                 final Sign currentSign = entry.getValue();
                 if (currentSign.getLine(2) == player.getName()) currentSign.setLine(2, "");
                 if (currentSign.getLine(3) == player.getName()) currentSign.setLine(3, "");
                 currentSign.update();
             }
         }
     }
 
     public boolean teamReady(Team team) {
         int members = 0;
         int membersReady = 0;
 
         for (final Entry<String, Team> entry : BattleUsersTeam.entrySet()) {
             if (Bukkit.getPlayer(entry.getKey()) != null) {
                 if (entry.getValue().equals(team)) {
                     members++;
                     if (BattleUsersClass.containsKey(entry.getKey())) membersReady++;
                 }
             }
         }
 
         if (members == membersReady && members > 0) {
             if (team.equals(Team.RED) || team.equals(Team.BLUE)) { return true; }
         }
 
         return false;
     }
 
     public void tellEveryone(String msg) {
         for (final String name : BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(name) != null) Bukkit.getPlayer(name).sendMessage(BNTag + msg);
         }
     }
 
     public void tellEveryone(Track track) {
         for (final String name : BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(name) != null) Bukkit.getPlayer(name).sendMessage(BNTag + track.msg);
         }
     }
 
     public void killFeed(String msg) {
         final LinkedList<Player> told = new LinkedList<Player>();
 
         for (final String name : BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(name) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(name);
                 currentPlayer.sendMessage(BNTag + msg);
                 told.add(currentPlayer);
             }
         }
 
         for (final String name : BattleSpectators.keySet()) {
             if (Bukkit.getPlayer(name) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(name);
                 if (!told.contains(currentPlayer)) {
                     currentPlayer.sendMessage(BNTag + msg);
                     told.add(currentPlayer);
                 }
             }
         }
 
         told.clear();
     }
 
     public void tellEveryoneExcept(Player player, String msg) {
         for (final String name : BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(name) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(name);
                 if (currentPlayer != player) currentPlayer.sendMessage(BNTag + msg);
             }
         }
     }
 
     public void tellTeam(Team team, String msg) {
         for (final String name : BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(name) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(name);
                 if (BattleUsersTeam.get(name).equals(team)) currentPlayer.sendMessage(BNTag + msg);
             }
         }
     }
 
     public void tellTeam(Team team, Track track) {
         for (final String name : BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(name) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(name);
                 if (BattleUsersTeam.get(name).equals(team)) currentPlayer.sendMessage(BNTag + track.msg);
             }
         }
     }
 
     public static void tellPlayer(Player player, String msg) {
         player.sendMessage(BNTag + msg);
     }
 
     public void tellPlayer(Player player, Track track) {
         player.sendMessage(BNTag + track.msg);
     }
 
     public void teleportAllToSpawn() {
         for (final String name : BattleUsersTeam.keySet()) {
             if (Bukkit.getPlayer(name) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(name);
                 if (BattleUsersTeam.get(name).equals(Team.RED)) {
                     goToWaypoint(currentPlayer, Waypoint.RED_SPAWN);
                 }
                 if (BattleUsersTeam.get(name).equals(Team.BLUE)) {
                     goToWaypoint(currentPlayer, Waypoint.BLUE_SPAWN);
                 }
             }
         }
     }
 
     public boolean hasEmptyInventory(Player player) {
         final ItemStack[] invContents = player.getInventory().getContents();
         final ItemStack[] armContents = player.getInventory().getArmorContents();
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
 
     public void goToWaypoint(Player player, Waypoint waypoint) {
         final Location destination = getCoords(waypoint.getName());
         final Chunk chunk = destination.getChunk();
 
         if (!chunk.isLoaded()) {
             chunk.load();
             while (!chunk.isLoaded()) {
                 // Wait until loaded
             }
         }
 
         BattleTelePass.put(player.getName(), "yes");
         player.teleport(destination);
         BattleTelePass.remove(player.getName());
     }
 
     public enum Perm {
         ADMIN, MOD, USER
     }
 
     public boolean hasPerm(BattleNight.Perm perm, CommandSender sender) {
         // Player check
         if (!(sender instanceof Player)) {
             return true;
         }
         Player player = (Player) sender;
         
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
 
         final String[] part1 = rawItem.split("x");
         final String[] part2 = part1[0].split(":");
         final String item = part2[0];
         if (part1.length == 1) {
             if (part2.length == 1) {
                 return parseItemWithoutData(item, "1");
             } else if (part2.length == 2) {
                 final String data = part2[1];
                 return parseItemWithData(item, data);
             }
         } else if (part1.length == 2) {
             final String amount = part1[1];
             if (part2.length == 1) {
                 return parseItemWithoutData(item, amount);
             } else if (part2.length == 2) {
                 final String data = part2[1];
                 return parseItemWithData(item, data, amount);
             }
         }
         return null;
     }
 
     private static ItemStack parseItemWithoutData(String item, String amount) {
         final Material m = Material.getMaterial(Integer.parseInt(item));
         int a = Integer.parseInt(amount);
         if (a > m.getMaxStackSize()) {
             log.warning("You attempted to set the item:" + m + " to have a greater stack size than possible.");
             a = m.getMaxStackSize();
         }
         return new ItemStack(m, a);
     }
 
     private static ItemStack parseItemWithData(String item, String data) {
         final int i = Integer.parseInt(item);
         final short d = Short.parseShort(data);
 
         return new ItemStack(i, 1, d);
     }
 
     private static ItemStack parseItemWithData(String item, String data,
             String amount) {
         final Material m = Material.getMaterial(Integer.parseInt(item));
         final byte d = Byte.parseByte(data);
         int a = Integer.parseInt(amount);
         if (a > m.getMaxStackSize()) {
             log.warning("You attempted to set the item:" + m + " to have a greater stack size than possible.");
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
                     battle.removePlayer(player, false, "has left the Battle.", "You have left the Battle.");
                 }
                 goToWaypoint(player, Waypoint.SPECTATOR);
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
 
     public void removeSpectator(Player player) {
         goToWaypoint(player, Waypoint.EXIT);
         BattleSpectators.remove(player.getName());
         tellPlayer(player, Track.GOODBYE_SPECTATOR);
     }
 
     public void removeAllSpectators() {
         for (final String pName : BattleSpectators.keySet()) {
             if (Bukkit.getPlayer(pName) != null) {
                 final Player currentPlayer = Bukkit.getPlayer(pName);
                 goToWaypoint(currentPlayer, Waypoint.EXIT);
             }
         }
 
         BattleSpectators.clear();
     }
 
     public boolean preparePlayer(Player p) {
         if (config.getString("InventoryType").equalsIgnoreCase("prompt") && !hasEmptyInventory(p)) return false;
 
         final String name = p.getName();
 
         // Inventory
         if (config.getString("InventoryType").equalsIgnoreCase("save")) {
             config.set(name + ".data.inv.main", Arrays.asList(p.getInventory().getContents()));
             config.set(name + ".data.inv.armor", Arrays.asList(p.getInventory().getArmorContents()));
         }
 
         // Health
         config.set(name + ".data.health", p.getHealth());
 
         // Hunger
         config.set(name + ".data.hunger.foodlevel", p.getFoodLevel());
         config.set(name + ".data.hunger.saturation", Float.toString(p.getSaturation()));
         config.set(name + ".data.hunger.exhaustion", Float.toString(p.getExhaustion()));
 
         // Experience
         config.set(name + ".data.exp.level", p.getLevel());
         config.set(name + ".data.exp.ammount", Float.toString(p.getExp()));
 
         // GameMode
         config.set(name + ".data.gamemode", p.getGameMode().getValue());
 
         // Flying
         config.set(name + ".data.flight.allowed", p.getAllowFlight());
         config.set(name + ".data.flight.flying", p.isFlying());
 
         // Sleep
         config.set(name + ".data.sleepignored", p.isSleepingIgnored());
 
         // Information
         config.set(name + ".data.info.displayname", p.getDisplayName());
         config.set(name + ".data.info.listname", p.getPlayerListName());
 
         // Statistics
         config.set(name + ".data.stats.tickslived", p.getTicksLived());
         config.set(name + ".data.stats.nodamageticks", p.getNoDamageTicks());
 
         // State
         config.set(name + ".data.state.remainingair", p.getRemainingAir());
         config.set(name + ".data.state.falldistance", Float.toString(p.getFallDistance()));
         config.set(name + ".data.state.fireticks", p.getFireTicks());
 
         saveYAML(ConfigFile.Players);
 
         // Reset Player
         reset(p, false);
         return true;
     }
 
     public void restorePlayer(Player p) {
         final String name = p.getName();
         reset(p, true);
 
         try {
             // Inventory
             if (config.getString("InventoryType").equalsIgnoreCase("save")) {
                 p.getInventory().setContents(config.getList(name + ".data.inv.main").toArray(new ItemStack[0]));
                 p.getInventory().setArmorContents(config.getList(name + ".data.inv.armor").toArray(new ItemStack[0]));
             }
 
             // Health
             p.setHealth(config.getInt(name + ".data.health"));
 
             // Hunger
             p.setFoodLevel(config.getInt(name + ".data.hunger.foodlevel"));
             p.setSaturation(Float.parseFloat(config.getString(name + ".data.hunger.saturation")));
             p.setExhaustion(Float.parseFloat(config.getString(name + ".data.hunger.exhaustion")));
 
             // Experience
             p.setLevel(config.getInt(name + ".data.exp.level"));
             p.setExp(Float.parseFloat(config.getString(name + ".data.exp.ammount")));
 
             // GameMode
             p.setGameMode(GameMode.getByValue(config.getInt(name + ".data.gamemode")));
 
             // Flying
             p.setAllowFlight(config.getBoolean(name + ".data.flight.allowed"));
             p.setFlying(config.getBoolean(name + ".data.flight.flying"));
 
             // Sleep
             p.setSleepingIgnored(config.getBoolean(name + ".data.sleepignored"));
 
             // Information
             p.setDisplayName(config.getString(name + ".data.info.displayname"));
             p.setPlayerListName(config.getString(name + ".data.info.listname"));
 
             // Statistics
             p.setTicksLived(config.getInt(name + ".data.stats.tickslived"));
             p.setNoDamageTicks(config.getInt(name + ".data.stats.nodamageticks"));
 
         } catch (final NullPointerException e) {
             log.warning("Failed to restore data for player: '" + name + "'.");
         }
     }
 
     public void reset(Player p, boolean light) {
         final PlayerInventory inv = p.getInventory();
         inv.clear();
         inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
 
         removePotionEffects(p);
 
         if (!light) {
             p.setHealth(p.getMaxHealth());
             p.setFoodLevel(16);
             p.setSaturation(1000);
             p.setExhaustion(0);
             p.setLevel(0);
             p.setExp(0);
             p.setGameMode(GameMode.SURVIVAL);
             p.setAllowFlight(false);
             p.setFlying(false);
             p.setSleepingIgnored(true);
 
             setNames(p);
 
             p.setTicksLived(1);
             p.setNoDamageTicks(0);
             p.setRemainingAir(300);
             p.setFallDistance(0.0f);
             p.setFireTicks(-20);
         }
     }
 
     public void setNames(Player player) {
         final String name = player.getName();
 
         final String pListName = ChatColor.GRAY + "[BN] " + name;
         ChatColor teamColour = ChatColor.WHITE;
         if (BattleUsersTeam.containsKey(name)) {
             teamColour = BattleUsersTeam.get(name).equals(Team.RED) ? ChatColor.RED : ChatColor.BLUE;
         }
 
         player.setPlayerListName(pListName.length() < 16 ? pListName : pListName.substring(0, 16));
         player.setDisplayName(ChatColor.GRAY + "[BN] " + teamColour + name + ChatColor.RESET);
         try {
             TagAPI.refreshPlayer(player);
         } catch (final Exception e) {
         }
     }
 
     private void removePotionEffects(Player p) {
         for (final PotionEffect effect : p.getActivePotionEffects()) {
             p.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);
         }
     }
 }
