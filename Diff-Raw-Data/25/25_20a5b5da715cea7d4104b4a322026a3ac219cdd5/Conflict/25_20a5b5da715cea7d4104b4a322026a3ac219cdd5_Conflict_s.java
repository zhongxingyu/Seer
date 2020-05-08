 package Lihad.Conflict;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 import Lihad.Conflict.Command.CommandHandler;
 import Lihad.Conflict.Information.BeyondInfo;
 import Lihad.Conflict.Listeners.*;
 import Lihad.Conflict.Perk.*;
 import Lihad.Conflict.Util.BeyondUtil;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Conflict extends JavaPlugin {
 	/** Name of the plugin, used in output messages */
 	protected static String PLUGIN_NAME = "Conflict";
 	/** Header used for console and player output messages */
 	protected static String header = "[" + PLUGIN_NAME + "] ";
 
     // Plugin instance
     public static Conflict plugin;
     
 	public static YamlConfiguration information;
 	
 	public static PermissionHandler handler;
 	public static PermissionManager ex;
 	private static Logger log = Logger.getLogger("Minecraft");
 
     public static City Abatton = new City("Abatton");
     public static City Oceian = new City("Oceian");
     public static City Savania = new City("Savania");
     public static City[] cities = {Abatton, Oceian, Savania};
     
     public static Perk Blacksmith = new BlacksmithPerk("blacksmith");
     public static Perk Potions = new PotionPerk("potions");
     public static Perk Enchantments = new EnchantmentPerk("enchantments");
     public static Perk MystPortal = new PortalPerk("mystportal");
     public static Perk WeaponDrop = new WeaponDropPerk();
     public static Perk ArmorDrop = new ArmorDropPerk();
     public static Perk PotionDrop = new PotionDropPerk();
     public static Perk ToolDrop = new ToolDropPerk();
     public static Perk BowDrop = new BowDropPerk();
     public static Perk Shield = new ShieldPerk();
     public static Perk Strike = new StrikePerk();
     public static Perk EnchantUp = new EnchantUpPerk();
     public static Perk GoldDrop = new GoldDropPerk();
     public static Perk Tpa = new TpaPerk();
     
     public static Perk[] perks = {Blacksmith, Potions, Enchantments, MystPortal, WeaponDrop, ArmorDrop, PotionDrop, ToolDrop, BowDrop, Shield, Strike, EnchantUp, GoldDrop};
     
     public static List<Node> nodes = new LinkedList<Node>();
 
 	private static List<String> UNASSIGNED_PLAYERS = new LinkedList<String>(); 
 	public static Map<String, String> PLAYER_SET_SELECT = new HashMap<String, String>();
 
     public static War war = null;
     static Date nextWartime = null;
 
 	public static int TASK_ID_EVENT;
 
 	public static boolean IS_EVENT_RUNNING = false;
 
 	public static CommandExecutor cmd;
     public static Random random = new Random();
     
     /**
      * The color used for headers in chat text.
      */
     public static ChatColor HEADERCOLOR = ChatColor.MAGIC;
     
     /**
      * The color used for headers in chat text.
      */
     public static ChatColor MAYORCOLOR = ChatColor.GOLD;
 
     /**
      * The color used for perks in chat text.
      */
     public static ChatColor PERKCOLOR = ChatColor.DARK_AQUA;
 
     /**
      * The color used for city names in chat text.
      */
     public static ChatColor CITYCOLOR = ChatColor.GREEN;
 
     /**
      * The color used for player names in chat text.
      */
     public static ChatColor PLAYERCOLOR = ChatColor.YELLOW;
 
     /**
      * The color used for generic chat text.
      */
     public static ChatColor TEXTCOLOR = ChatColor.BLUE;
 
     /**
      * The color used for items in chat text.
      */
     public static ChatColor ITEMCOLOR = ChatColor.AQUA;
 
     /**
      * The color used for notices in chat text.
      */
     public static String NOTICECOLOR = "" + ChatColor.BOLD + ChatColor.LIGHT_PURPLE;
 
     /**
      * The color used for prompts in chat text.
      */
     public static ChatColor PROMPTCOLOR = ChatColor.LIGHT_PURPLE;
 
     /**
      * The color used for messed up stuff in chat text.
      */
     public static ChatColor ERRORCOLOR = ChatColor.RED;
     
     /**
      * The color used for messed up stuff in chat text.
      */
     public static ChatColor MONEYCOLOR = ChatColor.YELLOW;
     
     /**
      * The color used for "yes" (e.g. is online)
      */
     public static ChatColor YESCOLOR = ChatColor.GREEN;
     
     /**
      * The color used for "no" (e.g. is offline)
      */
     public static ChatColor NOCOLOR = ChatColor.RED;
 
     /**
      * The amount of time in milliseconds players have to wait to switch Cities
      * @param playerName
      * @return long - milliseconds
      */
     public static long switchCooldown = 1209600000;
 
     public static City getPlayerCity(String playerName) {
     	for (int i=0; i<cities.length; i++)
     		if (cities[i].hasPlayer(playerName))
     			return cities [i];
         return null;
     }
     
     /**
      * Returns the City with the given name, or null if nonexistent.  e.g. Use this as an existence test
      * for processing commands.
      * @param cityName - The name of the city you are looking for
      * @return City - The City with the given name, or null if not found.
      */
     public static City getCity(String cityName) {
     	for (int i=0; i<cities.length; i++)
     		if (cities[i].getName().equalsIgnoreCase(cityName))
     			return cities [i];
         return null;
     }
     
     /**
      * Returns the (hopefully) formatted player name, null if does not exist.  e.g. Use this as an existence test
      * to see if a player with that name has ever logged on before.
      * @param playerName - the name you want to search for
      * @return String - the name of the online or offline player, null if none.
      */
     public static String getFormattedPlayerName(String playerName) {
     	String returnMe = null;
     	Player player = Bukkit.getServer().getPlayer(playerName);
     	if (player != null) {
     		returnMe = player.getName();
     	} else {
     		OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(playerName);
     		if (offlinePlayer != null && offlinePlayer.getFirstPlayed() > 0)
     			returnMe = offlinePlayer.getName();
     	}
     	return returnMe;
     }
     
     public static boolean playerCanUsePerk(Player player, Perk perk) {
         City c = getPlayerCity(player.getName());
         if (c != null)
         	return (c.getPerks().contains(perk));
         return false;
     }
     
     /**
      * Switches target player to target city.
      * @param playerName - The player's name.
      * @param city - The target city.
      * @param force - If true, will ignore any limitations that would prevent that player from switching.
      * @return boolean - True if successful, false if not.
      */
     public static boolean joinCity(CommandSender sender, String playerName, String cityName, boolean force) {
     	playerName = getFormattedPlayerName(playerName);
     	if (playerName == null) {
     		sender.sendMessage(TEXTCOLOR + "This player has not yet played on our server, and yes, this plugin's too lame to switch it for you anyway :P");
     		return false;
     	}
     	City city = getCity(cityName);
     	if (city == null) {
     		sender.sendMessage(TEXTCOLOR + "Could not find target city: " + ERRORCOLOR + cityName + TEXTCOLOR + ".  Please try one of: " + CITYCOLOR + Conflict.cities);
     		return false;
     	}
     	if (!force) {
 			int least = Integer.MAX_VALUE;
 			for (int i=0; i<Conflict.cities.length; i++) {
 				if (Conflict.cities[i].getPopulation() < least)
 					least = Conflict.cities[i].getPopulation();
 			}
 			if (least < (city.getPopulation() - 10)) {
 				sender.sendMessage(CITYCOLOR + city.getName() + TEXTCOLOR + " is over capacity!  Try one of the others, or wait and try again later.");
 				return false;
 			}
 			if (!isUnassigned(playerName) && !Conflict.cooldownExpired(playerName))
 			{
 				sender.sendMessage(Conflict.getFormattedRemainingCooldown(playerName));
 				return false;
 			}
     	}
     	City oldCity = null;
     	while (Conflict.getPlayerCity(playerName) != null) {
     		oldCity = Conflict.getPlayerCity(playerName);
     		if (oldCity != null) {
     			if (oldCity.equals(city)) {
     				sender.sendMessage(TEXTCOLOR + "Already a member of "
     						+ CITYCOLOR + city.getName() + TEXTCOLOR + "!");
     				return false;
     			}
     			oldCity.removePlayer(playerName);
         		leaveChat(playerName, oldCity.getName());
     			sender.sendMessage(TEXTCOLOR + "Removing from " + CITYCOLOR + oldCity.getName());
     		}
     	};
     	
     	city.addPlayer(playerName);
     	
     	//Want to be sure to remove any dupes or different capitalizations
         removeUnassigned(playerName);
 
     	if (oldCity != null) {
         	Bukkit.getServer().broadcastMessage(PLAYERCOLOR + playerName + TEXTCOLOR 
         			+ " has abandoned " + CITYCOLOR + oldCity.getName() + TEXTCOLOR
         			+ " and is now a member of " + CITYCOLOR + city.getName() + TEXTCOLOR + "!");
     		
     	} else {
 	    	Bukkit.getServer().broadcastMessage(PLAYERCOLOR + playerName + TEXTCOLOR + " is now a member of "
 	    		+ CITYCOLOR + city.getName() + TEXTCOLOR + "!");
     	}	
     	return true;
     }
 
     /**
      * Returns whether or not the cooldown has expired to allow the palyer to switch cities.
      * @param correctlyCapitalizedPlayerName - Target player's name, correctly capitalized.
      * @return boolean - true if cooldown has expired, false otherwise.
      */
 	private static boolean cooldownExpired(String correctlyCapitalizedPlayerName) {
 		City city = getPlayerCity(correctlyCapitalizedPlayerName);
 		if (city == null) {
 			return true;
 		}
 		return (System.currentTimeMillis() > (switchCooldown + city.getJoinedTime(correctlyCapitalizedPlayerName)));
 	}
 
     /**
      * Returns the remaining cooldown in friendly format.
      * @param playerName - Target player's name, correctly capitalized.
      * @return boolean - true if cooldown has expired, false otherwise.
      */
 	private static String getFormattedRemainingCooldown(String playerName) {
 		City city = getPlayerCity(playerName);
 		if (city == null) {
 			return Conflict.PLAYERCOLOR + playerName + Conflict.NOTICECOLOR + " is not in a City!";
 		}
 	    long remaining = (System.currentTimeMillis() + city.getJoinedTime(playerName) - switchCooldown)/1000;
 	    if (remaining <= 0) {
 	    	return Conflict.PLAYERCOLOR + playerName + Conflict.NOTICECOLOR + " can switch now!";
 	    }
 	    return Conflict.PLAYERCOLOR + playerName + " has " + BeyondUtil.formatMillis(remaining) + " remaining.";
 	}
 
 	
 	private final BeyondPluginListener pluginListener = new BeyondPluginListener();
 	private final BeyondBlockListener blockListener = new BeyondBlockListener();
 	private final BeyondPlayerListener playerListener = new BeyondPlayerListener();
 	private final BeyondEntityListener entityListener = new BeyondEntityListener();
 	private final BeyondSafeModeListener safeListener = new BeyondSafeModeListener();
 
 	public static File infoFile = new File("plugins/Conflict/information.yml");
 
 
 	@Override
 	public void onDisable() {
         saveInfoFile();
 	}
 	@Override
 	public void onEnable() {
 
         // Global plugin instance
         plugin = this;
 
 		//InfoManager
 		information = new YamlConfiguration();
         loadInfoFile(information, infoFile);
         BeyondInfo.loadConfig();
 		//TimerManager
         boolean safeMode = false;
         for (int i=0; i<cities.length; i++) {
         	if (cities[i].getLocation() == null) {
         		safeMode = true;
         		severe("Unable to find capital location for " + cities[i].name);
         	}
         	else if (cities[i].getSpongeLocation() == null) {
         		safeMode = true;
         		severe("Unable to find drifter location for " + cities[i].name);
         	}
         }
 		if (!safeMode) {
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 				public void run() {
                     // Maintenance timer - runs every 5 mins
                     if(war != null){
                         war.executeMaintenanceTick();
                     }
                     purgeInactivePlayers();
                     saveInfoFile();
 				}
 			},1200L, 5500L);
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 				public void run() {
                     // War timer - runs every second
                     if(war == null){
                         if (nextWartime == null) {
                             nextWartime = War.getNextWartime();
                         }
 
                         Date now = new Date();
                         if (now.after(nextWartime)) {
                             war = new War();
                             nextWartime = null;
                         }
                     }
                     else{
                         war.executeWarTick();
 
                         if(war.warShouldEnd()) {
                             war.endWar();  // This should make the peace protesters happy.
                             war = null;
                         }
 					}
 				}
 			},200L, 20L);
 		}
 		//CommandManager
 		cmd = new CommandHandler(this);
 		getCommand("abatton").setExecutor(cmd);
 		getCommand("oceian").setExecutor(cmd);
 		getCommand("savania").setExecutor(cmd);
 		getCommand("point").setExecutor(cmd);
 		getCommand("purchase").setExecutor(cmd);
 		getCommand("spawn").setExecutor(cmd);
 		getCommand("setcityspawn").setExecutor(cmd);
 		getCommand("rarity").setExecutor(cmd);
 		getCommand("post").setExecutor(cmd);
 		getCommand("look").setExecutor(cmd);
 		getCommand("gear").setExecutor(cmd);
 		getCommand("cc").setExecutor(cmd);
 		getCommand("cca").setExecutor(cmd);
 		getCommand("ccd").setExecutor(cmd);
 		getCommand("nulls").setExecutor(cmd);
 		getCommand("protectcity").setExecutor(cmd);
 		getCommand("myst").setExecutor(cmd);
 		getCommand("cwho").setExecutor(cmd);
 		getCommand("perks").setExecutor(cmd);
 		getCommand("war").setExecutor(new War());
 		getCommand("join").setExecutor(cmd);
 
 
 		//PermsManager
 		setupPermissions();
 		setupPermissionsEx();
 
 		//PluginManager
 		if(Abatton.getLocation() == null || Oceian.getLocation() == null || Savania.getLocation() == null){
 			severe("Unable to find all Capital Locations.  Booted in SAFE MODE for Listeners");
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(safeListener, this);
		}else if(Blacksmith.getNode() == null || Potions.getNode() == null || Enchantments.getNode() == null || MystPortal.getNode() == null){
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvents(safeListener, this);
			severe("Unable to find all warzone Locations.  Booted in SAFE MODE for Listeners");
 		}else if(Abatton.getSpongeLocation() == null || Oceian.getSpongeLocation() == null || Savania.getSpongeLocation() == null){
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(safeListener, this);
 			severe("Unable to find all Drifter Locations.  Booted in SAFE MODE for Listeners");
 		}else{
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(blockListener, this);
 			pm.registerEvents(pluginListener, this);
 			pm.registerEvents(playerListener, this);
 			pm.registerEvents(entityListener, this);
 			pm.registerEvents(safeListener, this);
             pm.registerEvents(new War(), this);
             pm.registerEvents(new Perk("eventHandlerPerk"), this);
 		}
         
         // Init perks
         for (Perk p : perks) {
             p.Initialize(this);
         }
 	}
 	public static void setupPermissions() {
 		Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");
 
 		if (permissionsPlugin != null) {
 			info("Succesfully connected to Permissions!");
 			handler = ((Permissions) permissionsPlugin).getHandler();
 
 		} else {
 			handler = null;
 			warning("Disconnected from Permissions...what could possibly go wrong?");
 		}
 	}
 	public static void setupPermissionsEx() {
 		Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx");
 
 		if (permissionsPlugin != null) {
 			info("Succesfully connected to PermissionsEx!");
 			ex = PermissionsEx.getPermissionManager();
 
 		} else {
 			ex = null;
 			warning("Disconnected from PermissionsEx...what could possibly go wrong?");
 		}
 	}
 	public void notification(String message){
 		getServer().broadcastMessage(ChatColor.LIGHT_PURPLE.toString() +"[Religion Notification] " + ChatColor.AQUA.toString() + message);
 	}
 	/**
 	 * Logs an informative message to the console, prefaced with this plugin's header
 	 * @param message: String
 	 */
 	public static void info(String message)
 	{ 
 		log.info(header + ChatColor.WHITE + message);
 	}
 
 	/**
 	 * Logs a severe error message to the console, prefaced with this plugin's header
 	 * Used to log severe problems that have prevented normal execution of the plugin
 	 * @param message: String
 	 */
 	public static void severe(String message)
 	{
 		log.severe(header + ChatColor.RED + message);
 	}
 
 	/**
 	 * Logs a warning message to the console, prefaced with this plugin's header
 	 * Used to log problems that could interfere with the plugin's ability to meet admin expectations
 	 * @param message: String
 	 */
 	public static void warning(String message)
 	{
 		log.warning(header + ChatColor.YELLOW + message);
 	}
 
 	/**
 	 * Logs a message to the console, prefaced with this plugin's header
 	 * @param level: Logging level under which to send the message
 	 * @param message: String
 	 */
 	public static void log(java.util.logging.Level level, String message)
 	{
 		log.log(level, header + message);
 	}
 	public static void saveInfoFile() {
 		try {
             BeyondInfo.saveConfig();
             information.save(infoFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
     
     public static void loadInfoFile(YamlConfiguration config, File file) {
         try {
             config.load(file);
         } catch (java.io.FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InvalidConfigurationException e) {
             e.printStackTrace();
         }
     }
 
     public static String theerror = "error not set";
     
     private static boolean purgeHasRunAlready = false;
     public static void purgeInactivePlayers() {
         // Only need to do this once per server restart.
         if (purgeHasRunAlready) return;
 
         Abatton.purgeInactivePlayers();
         Oceian.purgeInactivePlayers();
         Savania.purgeInactivePlayers();
 
         purgeHasRunAlready = true;
     }
     
     public static long getPlayerLastSeenTime(String playerName) {
         // Implementation using FirstLastSeen
         String filename = "plugins/FirstLastSeen/data/"+playerName.toLowerCase();
 
         java.io.BufferedReader reader = null;
         try {
             reader = new java.io.BufferedReader(new java.io.FileReader(filename));
             String text = null;
 
             // repeat until all lines are read
             while ((text = reader.readLine()) != null) {
                 String[] s = text.split("\\=");
                 if (s.length != 2) continue;
                 if (s[0].equals("LastSeen")) {
                     return Long.parseLong(s[1]);
                 }
             }
         } catch (java.io.FileNotFoundException e) {
             severe("Can't find last seen file for " + filename);
             return 0;
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try { if (reader != null) { reader.close(); } } catch (IOException e) { e.printStackTrace(); }
         }
         return 0;
     }
 
     /**
      * Forces the player to join their city's channel
      * @param playerName - The player name.
      */
 	public boolean joinChat(String playerName) {
 	
     	playerName = getFormattedPlayerName(playerName);
     	if (playerName == null) {
     		return false;
     	}
 		City city = Conflict.getPlayerCity(playerName);
 		if (city == null)
 			return false;
 		
 		Player player = getServer().getPlayer(playerName);
 		if (player != null)
 		{
 			player.performCommand("ch " + city.getName() + " " + city.getPassword());
 		}
 		getServer().dispatchCommand(getServer().getConsoleSender(), "pex user " + playerName + " add herochat.force.join." + city.getName());
 		getServer().dispatchCommand(getServer().getConsoleSender(), "pex user " + playerName + " add herochat.join." + city.getName());
 		return true;
 	}
 	
     /**
      * Forces the player to leave their city's channel
      * @param playerName - The player name.
      * @param cityName - The city name.
      */
 	public static boolean leaveChat(String playerName, String cityName) {
 	
     	playerName = getFormattedPlayerName(playerName);
     	if (playerName == null) {
     		return false;
     	}
 		City city = Conflict.getCity(cityName);
 		if (city == null)
 			return false;		
 		
 		Player player = Bukkit.getServer().getPlayer(playerName);
 		if (player != null)
 		{
 			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "ch kick " + city.getName() + " " + playerName);
 		}
 		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "pex user " + playerName + " remove herochat.force.join." + city.getName());
 		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "pex user " + playerName + " remove herochat.join." + city.getName());
 		return true;
 	}
 
 	/**
 	 * Resets target player, removing them from all cities and making them choose a city to join.
 	 * @param sender - The command sender.
 	 * @param playerName - The name of the target player.
 	 */
 	public static boolean reset(CommandSender sender, String playerName) {
     	playerName = getFormattedPlayerName(playerName);
     	if (playerName == null) {
     		sender.sendMessage(TEXTCOLOR + "This player has not yet played on our server, and yes, this plugin's too lame to switch it for you anyway :P");
     		return false;
     	}
     	City oldCity = null;
     	while (Conflict.getPlayerCity(playerName) != null) {
     		oldCity = Conflict.getPlayerCity(playerName);
     		if (oldCity != null) {
     			oldCity.removePlayer(playerName);
         		leaveChat(playerName, oldCity.getName());
     			sender.sendMessage(TEXTCOLOR + "Removing from " + CITYCOLOR + oldCity.getName());
     		}
     	};
     	
     	for (int i=0; i < cities.length; i++) {
     		leaveChat(playerName, cities[i].getName());
     	}
     	
     	//Remove all entries first, then add correctly capitalized entry (just in case)
 	    removeUnassigned(playerName);
     	addUnassigned(playerName);
     	
         Bukkit.getServer().broadcastMessage(PLAYERCOLOR + playerName + ERRORCOLOR 
        			+ ChatColor.BOLD + " screwed up " + TEXTCOLOR + " and had to get an admin to reset them!");
     	return true;
 	}
 
 	/**
 	 * Tests to see if target player is unassigned (case insensitive).
 	 * @param name - target player's name.
 	 * @return boolean - true if so, false if not.
 	 */
 	public static boolean isUnassigned(String name) {
     	//Want to be sure to remove any dupes or different capitalizations
         for (String str : Conflict.UNASSIGNED_PLAYERS) {
         	if (str.equalsIgnoreCase(name)) {
         		return true;
         	}
         }
         return false;
 	}
 	
 	/**
 	 * Unassigns target player if not already unassigned.
 	 * @param name - preferably correctly capitalized, not strictly necessary.
 	 */
 	public static void addUnassigned(String name) {
     	if (!isUnassigned(name))
     		Conflict.UNASSIGNED_PLAYERS.add(name);
 	}
 	
 	/**
 	 * Unassigns target player if not already unassigned.
 	 * @param name - preferably correctly capitalized, not strictly necessary.
 	 */
 	public static void removeUnassigned(String name) {
         for (java.util.Iterator<String> it = Conflict.UNASSIGNED_PLAYERS.iterator(); it.hasNext();) {
         	String str = it.next();
         	if (str.equalsIgnoreCase(name)) {
         		it.remove();
         	}
         }
 	}
     
     // Make sure permission groups are correct for this player.  Run on player join, and city change
     public static void checkPlayerCityPermissions(Player p) {
         // TODO
         // Check perms for city membership
         // If necessary, add perm node for current city.  Remove perms for other cities.
     }
 }
