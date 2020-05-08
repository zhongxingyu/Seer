 package Lihad.Conflict;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.Date;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.dthielke.herochat.Herochat;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import Lihad.Conflict.Command.CommandHandler;
 import Lihad.Conflict.Information.BeyondInfo;
 import Lihad.Conflict.Listeners.BeyondBlockListener;
 import Lihad.Conflict.Listeners.BeyondEntityListener;
 import Lihad.Conflict.Listeners.BeyondPlayerListener;
 import Lihad.Conflict.Listeners.BeyondPluginListener;
 import Lihad.Conflict.Listeners.BeyondSafeModeListener;
 
 public class Conflict extends JavaPlugin {
 	/** Name of the plugin, used in output messages */
 	protected static String name = "Conflict";
 	/** Header used for console and player output messages */
 	protected static String header = "[" + name + "] ";
 	
     public static YamlConfiguration information;
     
 	public static PermissionHandler handler;
 	public static PermissionManager ex;
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	public static List<String> ABATTON_PLAYERS = new LinkedList<String>();
 	public static Location ABATTON_LOCATION;
 	public static Location ABATTON_LOCATION_SPAWN;
 	public static Location ABATTON_LOCATION_DRIFTER;
 	public static List<String> ABATTON_GENERALS = new LinkedList<String>();
 	public static List<String> ABATTON_TRADES = new LinkedList<String>();
 	public static List<String> ABATTON_TRADES_TEMP = new LinkedList<String>();
 	public static List<String> ABATTON_PERKS = new LinkedList<String>();
 	public static int ABATTON_WORTH;
 	public static int ABATTON_PROTECTION;
 	public static List<String> OCEIAN_PLAYERS = new LinkedList<String>();
 	public static Location OCEIAN_LOCATION;
 	public static Location OCEIAN_LOCATION_SPAWN;
 	public static Location OCEIAN_LOCATION_DRIFTER;
 	public static List<String> OCEIAN_GENERALS = new LinkedList<String>();
 	public static List<String> OCEIAN_TRADES = new LinkedList<String>();
 	public static List<String> OCEIAN_TRADES_TEMP = new LinkedList<String>();
 	public static List<String> OCEIAN_PERKS = new LinkedList<String>();
 	public static int OCEIAN_WORTH;
 	public static int OCEIAN_PROTECTION;
 	public static List<String> SAVANIA_PLAYERS = new LinkedList<String>();
 	public static Location SAVANIA_LOCATION;
 	public static Location SAVANIA_LOCATION_SPAWN;
 	public static Location SAVANIA_LOCATION_DRIFTER;
 	public static List<String> SAVANIA_GENERALS = new LinkedList<String>();
 	public static List<String> SAVANIA_TRADES = new LinkedList<String>();
 	public static List<String> SAVANIA_TRADES_TEMP = new LinkedList<String>();
 	public static List<String> SAVANIA_PERKS = new LinkedList<String>();
 	public static int SAVANIA_WORTH;
 	public static int SAVANIA_PROTECTION;
 	public static Location TRADE_BLACKSMITH;
 	public static int TRADE_BLACKSMITH_COUNTER;
 	public static Location TRADE_POTIONS;
 	public static int TRADE_POTIONS_COUNTER;
 	public static Location TRADE_ENCHANTMENTS;
 	public static int TRADE_ENCHANTMENTS_COUNTER;
 	public static Location TRADE_RICHPORTAL;
 	public static int TRADE_RICHPORTAL_COUNTER;
 	public static Location TRADE_MYSTPORTAL;
 	public static int TRADE_MYSTPORTAL_COUNTER;
 
 	public static List<String> UNASSIGNED_PLAYERS = new LinkedList<String>(); 
 	public static Map<String, String> PLAYER_SET_SELECT = new HashMap<String, String>();
 	
 	public static Map<String, Integer> TRADE_BLACKSMITH_PLAYER_USES = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_POTIONS_PLAYER_USES = new HashMap<String, Integer>();
 	public static Map<String, Integer> TRADE_ENCHANTMENTS_PLAYER_USES = new HashMap<String, Integer>();
 	
 	public static int TASK_ID_EVENT;
 	
 	public static Calendar cal = Calendar.getInstance();
 	public static boolean IS_EVENT_RUNNING = false;
 	
 	public static CommandExecutor cmd;
 	public static BeyondInfo info;
 	
 	private final BeyondPluginListener pluginListener = new BeyondPluginListener(this);
 	private final BeyondBlockListener blockListener = new BeyondBlockListener(this);
 	private final BeyondPlayerListener playerListener = new BeyondPlayerListener(this);
 	private final BeyondEntityListener entityListener = new BeyondEntityListener(this);
 	private final BeyondSafeModeListener safeListener = new BeyondSafeModeListener(this);
     
 	public static File infoFile = new File("plugins/Conflict/information.yml");
 
 
 	@Override
 	public void onDisable() {
 		BeyondInfo.writer();
 		try {
 			information.save(infoFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	@Override
 	public void onEnable() {
 		
 		//InfoManager
 		information = new YamlConfiguration();
 		try {
 			information.load(infoFile);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		info = new BeyondInfo(this);
 		BeyondInfo.loader();
 		//TimerManager
 		if(ABATTON_LOCATION == null || OCEIAN_LOCATION == null || SAVANIA_LOCATION == null){
 			severe("Unable to find all Capital Locations.  Booted in SAFE MODE for Timers");
 		}else if(TRADE_BLACKSMITH == null || TRADE_POTIONS == null || TRADE_ENCHANTMENTS == null
 				|| TRADE_RICHPORTAL == null || TRADE_MYSTPORTAL == null){
 			severe("Unable to find all Trade Locations.  Booted in SAFE MODE for Timers");
 		}else if(ABATTON_LOCATION_DRIFTER == null || OCEIAN_LOCATION_DRIFTER == null || SAVANIA_LOCATION_DRIFTER == null){
 			severe("Unable to find all Drifter Locations.  Booted in SAFE MODE for Timers");
 		}else{
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 				public void run() {
 					TRADE_BLACKSMITH_PLAYER_USES.clear();
 					TRADE_POTIONS_PLAYER_USES.clear();
 					TRADE_ENCHANTMENTS_PLAYER_USES.clear();
 				}
 			},0L, 1728000L);
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 				public void run() {
 					cal.setTime(new Date(System.currentTimeMillis()));
 					if(!IS_EVENT_RUNNING){
 						if((cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && cal.get(Calendar.HOUR_OF_DAY) == 19) || (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && cal.get(Calendar.HOUR_OF_DAY) == 13) ){
 							ABATTON_TRADES.clear();
 							OCEIAN_TRADES.clear();
 							SAVANIA_TRADES.clear();
 							IS_EVENT_RUNNING = true;
 						}else if((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && cal.get(Calendar.HOUR_OF_DAY) == 12)){
 							System.out.println("Preparing..");
 						}
 					}else{
 						if((cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && cal.get(Calendar.HOUR_OF_DAY) != 19 && cal.get(Calendar.HOUR_OF_DAY) != 20)
 								|| (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && cal.get(Calendar.HOUR_OF_DAY) != 13 && cal.get(Calendar.HOUR_OF_DAY) != 14)
 								|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY
 								|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
 							ABATTON_TRADES.addAll(ABATTON_TRADES_TEMP);
 							OCEIAN_TRADES.addAll(OCEIAN_TRADES_TEMP);
 							SAVANIA_TRADES.addAll(SAVANIA_TRADES_TEMP);
 							ABATTON_TRADES_TEMP.clear();
 							OCEIAN_TRADES_TEMP.clear();
 							SAVANIA_TRADES_TEMP.clear();
 							IS_EVENT_RUNNING = false;
 							return;
 						}
 						Player[] players = getServer().getOnlinePlayers();
						String mystportal_temp = null;
						String richportal_temp = null;
						String blacksmith_temp = null;
						String potions_temp = null;
						String enchantments_temp = null;

 						for(int i=0;i<players.length;i++){
 							if(players[i].getLocation().distance(TRADE_MYSTPORTAL) < 3){
 								if(mystportal_temp == null){
 									if(ABATTON_PLAYERS.contains(players[i].getName()))mystportal_temp = "Abatton";			
 									else if(OCEIAN_PLAYERS.contains(players[i].getName()))mystportal_temp = "Oceian";									
 									else if(SAVANIA_PLAYERS.contains(players[i].getName()))mystportal_temp = "Savania";
 								}else if(mystportal_temp.equals("contested")){
 									continue;
 								}else if((ABATTON_PLAYERS.contains(players[i].getName()) && mystportal_temp.equals("Abatton"))
 										|| (OCEIAN_PLAYERS.contains(players[i].getName()) && mystportal_temp.equals("Oceian"))
 										|| (SAVANIA_PLAYERS.contains(players[i].getName()) && mystportal_temp.equals("Savania"))){
 									TRADE_MYSTPORTAL_COUNTER++;
 									players[i].sendMessage(ChatColor.GOLD+"Taking point...");
 								}else{
 									mystportal_temp = "contested";
 									TRADE_MYSTPORTAL_COUNTER = 0;
 								}
 							}else if(players[i].getLocation().distance(TRADE_RICHPORTAL) < 3){
 								if(richportal_temp == null){
 									if(ABATTON_PLAYERS.contains(players[i].getName()))richportal_temp = "Abatton";
 									else if(OCEIAN_PLAYERS.contains(players[i].getName()))richportal_temp = "Oceian";									
 									else if(SAVANIA_PLAYERS.contains(players[i].getName()))richportal_temp = "Savania";
 								}else if(richportal_temp.equals("contested")){
 									continue;
 								}else if((ABATTON_PLAYERS.contains(players[i].getName()) && richportal_temp.equals("Abatton"))
 										|| (OCEIAN_PLAYERS.contains(players[i].getName()) && richportal_temp.equals("Oceian"))
 										|| (SAVANIA_PLAYERS.contains(players[i].getName()) && richportal_temp.equals("Savania"))){
 									TRADE_RICHPORTAL_COUNTER++;
 									players[i].sendMessage(ChatColor.GOLD+"Taking point...");
 								}else{
 									richportal_temp = "contested";
 									TRADE_RICHPORTAL_COUNTER = 0;
 								}
 							}else if(players[i].getLocation().distance(TRADE_BLACKSMITH) < 3){
 								if(blacksmith_temp == null){
 									if(ABATTON_PLAYERS.contains(players[i].getName()))blacksmith_temp = "Abatton";			
 									else if(OCEIAN_PLAYERS.contains(players[i].getName()))blacksmith_temp = "Oceian";									
 									else if(SAVANIA_PLAYERS.contains(players[i].getName()))blacksmith_temp = "Savania";
 								}else if(blacksmith_temp.equals("contested")){
 									continue;
 								}else if((ABATTON_PLAYERS.contains(players[i].getName()) && blacksmith_temp.equals("Abatton"))
 										|| (OCEIAN_PLAYERS.contains(players[i].getName()) && blacksmith_temp.equals("Oceian"))
 										|| (SAVANIA_PLAYERS.contains(players[i].getName()) && blacksmith_temp.equals("Savania"))){
 									TRADE_BLACKSMITH_COUNTER++;
 									players[i].sendMessage(ChatColor.GOLD+"Taking point...");
 								}else{
 									blacksmith_temp = "contested";
 									TRADE_BLACKSMITH_COUNTER = 0;
 								}
 							}else if(players[i].getLocation().distance(TRADE_POTIONS) < 3){
 								if(potions_temp == null){
 									if(ABATTON_PLAYERS.contains(players[i].getName()))potions_temp = "Abatton";			
 									else if(OCEIAN_PLAYERS.contains(players[i].getName()))potions_temp = "Oceian";									
 									else if(SAVANIA_PLAYERS.contains(players[i].getName()))potions_temp = "Savania";
 								}else if(potions_temp.equals("contested")){
 									continue;
 								}else if((ABATTON_PLAYERS.contains(players[i].getName()) && potions_temp.equals("Abatton"))
 										|| (OCEIAN_PLAYERS.contains(players[i].getName()) && potions_temp.equals("Oceian"))
 										|| (SAVANIA_PLAYERS.contains(players[i].getName()) && potions_temp.equals("Savania"))){
 									TRADE_POTIONS_COUNTER++;
 									players[i].sendMessage(ChatColor.GOLD+"Taking point...");
 								}else{
 									potions_temp = "contested";
 									TRADE_POTIONS_COUNTER = 0;
 								}
 							}else if(players[i].getLocation().distance(TRADE_ENCHANTMENTS) < 3){
 								if(enchantments_temp == null){
 									if(ABATTON_PLAYERS.contains(players[i].getName()))enchantments_temp = "Abatton";			
 									else if(OCEIAN_PLAYERS.contains(players[i].getName()))enchantments_temp = "Oceian";									
 									else if(SAVANIA_PLAYERS.contains(players[i].getName()))enchantments_temp = "Savania";
 								}else if(enchantments_temp.equals("contested")){
 									continue;
 								}else if((ABATTON_PLAYERS.contains(players[i].getName()) && enchantments_temp.equals("Abatton"))
 										|| (OCEIAN_PLAYERS.contains(players[i].getName()) && enchantments_temp.equals("Oceian"))
 										|| (SAVANIA_PLAYERS.contains(players[i].getName()) && enchantments_temp.equals("Savania"))){
 									TRADE_ENCHANTMENTS_COUNTER++;
 									players[i].sendMessage(ChatColor.GOLD+"Taking point...");
 								}else{
 									enchantments_temp = "contested";
 									TRADE_ENCHANTMENTS_COUNTER = 0;
 								}
 							}
 						}
 						if(!mystportal_temp.equals("contested") && TRADE_MYSTPORTAL_COUNTER >= 30){
 							if(mystportal_temp.equals("Abatton") && !ABATTON_TRADES_TEMP.contains("mystportal")){
 								ABATTON_TRADES_TEMP.add("mystportal");
 								OCEIAN_TRADES_TEMP.remove("mystportal");
 								SAVANIA_TRADES_TEMP.remove("mystportal");
 								getServer().broadcastMessage(ChatColor.RED+"Abatton has taken control of the Myst Portal!");
 							}else if(mystportal_temp.equals("Oceian") && !OCEIAN_TRADES_TEMP.contains("mystportal")){
 								ABATTON_TRADES_TEMP.remove("mystportal");
 								OCEIAN_TRADES_TEMP.add("mystportal");
 								SAVANIA_TRADES_TEMP.remove("mystportal");
 								getServer().broadcastMessage(ChatColor.RED+"Oceian has taken control of the Myst Portal!");
 							}else if(mystportal_temp.equals("Savania") && !SAVANIA_TRADES_TEMP.contains("mystportal")){
 								ABATTON_TRADES_TEMP.remove("mystportal");
 								OCEIAN_TRADES_TEMP.remove("mystportal");
 								SAVANIA_TRADES_TEMP.add("mystportal");
 								getServer().broadcastMessage(ChatColor.RED+"Savania has taken control of the Myst Portal!");
 							}
 						}else if(!richportal_temp.equals("contested") && TRADE_RICHPORTAL_COUNTER >= 30){
 							if(richportal_temp.equals("Abatton") && !ABATTON_TRADES_TEMP.contains("richportal")){
 								ABATTON_TRADES_TEMP.add("richportal");
 								OCEIAN_TRADES_TEMP.remove("richportal");
 								SAVANIA_TRADES_TEMP.remove("richportal");
 								getServer().broadcastMessage(ChatColor.RED+"Abatton has taken control of the Rich Portal!");
 							}else if(richportal_temp.equals("Oceian") && !OCEIAN_TRADES_TEMP.contains("richportal")){
 								ABATTON_TRADES_TEMP.remove("richportal");
 								OCEIAN_TRADES_TEMP.add("richportal");
 								SAVANIA_TRADES_TEMP.remove("richportal");
 								getServer().broadcastMessage(ChatColor.RED+"Oceian has taken control of the Rich Portal!");
 							}else if(richportal_temp.equals("Savania") && !SAVANIA_TRADES_TEMP.contains("richportal")){
 								ABATTON_TRADES_TEMP.remove("richportal");
 								OCEIAN_TRADES_TEMP.remove("richportal");
 								SAVANIA_TRADES_TEMP.add("richportal");
 								getServer().broadcastMessage(ChatColor.RED+"Savania has taken control of the Rich Portal!");
 							}
 						}else if(!blacksmith_temp.equals("contested") && TRADE_BLACKSMITH_COUNTER >= 30){
 							if(blacksmith_temp.equals("Abatton") && !ABATTON_TRADES_TEMP.contains("blacksmith")){
 								ABATTON_TRADES_TEMP.add("blacksmith");
 								OCEIAN_TRADES_TEMP.remove("blacksmith");
 								SAVANIA_TRADES_TEMP.remove("blacksmith");
 								getServer().broadcastMessage(ChatColor.RED+"Abatton has taken control of the Blacksmith!");
 							}else if(blacksmith_temp.equals("Oceian") && !OCEIAN_TRADES_TEMP.contains("blacksmith")){
 								ABATTON_TRADES_TEMP.remove("blacksmith");
 								OCEIAN_TRADES_TEMP.add("blacksmith");
 								SAVANIA_TRADES_TEMP.remove("blacksmith");
 								getServer().broadcastMessage(ChatColor.RED+"Oceian has taken control of the Blacksmith!");
 							}else if(blacksmith_temp.equals("Savania") && !SAVANIA_TRADES_TEMP.contains("blacksmith")){
 								ABATTON_TRADES_TEMP.remove("blacksmith");
 								OCEIAN_TRADES_TEMP.remove("blacksmith");
 								SAVANIA_TRADES_TEMP.add("blacksmith");
 								getServer().broadcastMessage(ChatColor.RED+"Savania has taken control of the Blacksmith!");
 							}
 						}else if(!potions_temp.equals("contested") && TRADE_POTIONS_COUNTER >= 30){
 							if(potions_temp.equals("Abatton") && !ABATTON_TRADES_TEMP.contains("potions")){
 								ABATTON_TRADES_TEMP.add("potions");
 								OCEIAN_TRADES_TEMP.remove("potions");
 								SAVANIA_TRADES_TEMP.remove("potions");
 								getServer().broadcastMessage(ChatColor.RED+"Abatton has taken control of the Potion Chest!");
 							}else if(potions_temp.equals("Oceian") && !OCEIAN_TRADES_TEMP.contains("potions")){
 								ABATTON_TRADES_TEMP.remove("potions");
 								OCEIAN_TRADES_TEMP.add("potions");
 								SAVANIA_TRADES_TEMP.remove("potions");
 								getServer().broadcastMessage(ChatColor.RED+"Oceian has taken control of the Potion Chest!");
 							}else if(potions_temp.equals("Savania") && !SAVANIA_TRADES_TEMP.contains("potions")){
 								ABATTON_TRADES_TEMP.remove("potions");
 								OCEIAN_TRADES_TEMP.remove("potions");
 								SAVANIA_TRADES_TEMP.add("potions");
 								getServer().broadcastMessage(ChatColor.RED+"Savania has taken control of the Potion Chest!");
 							}
 						}else if(!enchantments_temp.equals("contested") && TRADE_ENCHANTMENTS_COUNTER >= 30){
 							if(enchantments_temp.equals("Abatton") && !ABATTON_TRADES_TEMP.contains("enchantments")){
 								ABATTON_TRADES_TEMP.add("enchantments");
 								OCEIAN_TRADES_TEMP.remove("enchantments");
 								SAVANIA_TRADES_TEMP.remove("enchantments");
 								getServer().broadcastMessage(ChatColor.RED+"Abatton has taken control of the Enchantment Chest!");
 							}else if(enchantments_temp.equals("Oceian") && !OCEIAN_TRADES_TEMP.contains("enchantments")){
 								ABATTON_TRADES_TEMP.remove("enchantments");
 								OCEIAN_TRADES_TEMP.add("enchantments");
 								SAVANIA_TRADES_TEMP.remove("enchantments");
 								getServer().broadcastMessage(ChatColor.RED+"Oceian has taken control of the Enchantment Chest!");
 							}else if(enchantments_temp.equals("Savania") && !SAVANIA_TRADES_TEMP.contains("enchantments")){
 								ABATTON_TRADES_TEMP.remove("enchantments");
 								OCEIAN_TRADES_TEMP.remove("enchantments");
 								SAVANIA_TRADES_TEMP.add("enchantments");
 								getServer().broadcastMessage(ChatColor.RED+"Savania has taken control of the Enchantment Chest!");
 							}
 						}
 					}
 				}
 			},0L, 20L);
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
 		getCommand("nulls").setExecutor(cmd);
 		getCommand("protectcity").setExecutor(cmd);
 		getCommand("myst").setExecutor(cmd);
 
 		
 		//PermsManager
 		setupPermissions();
 		setupPermissionsEx();
 		
 		//PluginManager
 		if(ABATTON_LOCATION == null || OCEIAN_LOCATION == null || SAVANIA_LOCATION == null){
 			severe("Unable to find all Capital Locations.  Booted in SAFE MODE for Listeners");
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(safeListener, this);
 		}else if(TRADE_BLACKSMITH == null || TRADE_POTIONS == null || TRADE_ENCHANTMENTS == null
 				|| TRADE_RICHPORTAL == null || TRADE_MYSTPORTAL == null){
 			PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(safeListener, this);
 			severe("Unable to find all Trade Locations.  Booted in SAFE MODE for Listeners");
 		}else if(ABATTON_LOCATION_DRIFTER == null || OCEIAN_LOCATION_DRIFTER == null || SAVANIA_LOCATION_DRIFTER == null){
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
 		}
 	}
 	public void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
 		if (permissionsPlugin != null) {
 			info("Succesfully connected to Permissions!");
 			handler = ((Permissions) permissionsPlugin).getHandler();
 			
 		} else {
 			handler = null;
 			warning("Disconnected from Permissions...what could possibly go wrong?");
 		}
 	}
 	public void setupPermissionsEx() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("PermissionsEx");
 		
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
 	public static void saveInfoFile() throws IOException{
 		information.save(infoFile);
 	}
 }
