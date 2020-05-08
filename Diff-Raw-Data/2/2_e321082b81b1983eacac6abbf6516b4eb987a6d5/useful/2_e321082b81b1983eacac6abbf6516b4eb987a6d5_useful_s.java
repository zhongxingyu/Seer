 package com.useful.useful;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLConnection;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import javax.crypto.KeyGenerator;
 import javax.crypto.SecretKey;
 
 import lib.PatPeter.SQLibrary.SQLite;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.SimpleCommandMap;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.useful.useful.utils.ColoredLogger;
 import com.useful.useful.utils.Colors;
 import com.useful.useful.utils.Copier;
 import com.useful.useful.utils.CustomRecipes;
 import com.useful.useful.utils.Encrpyter;
 import com.useful.useful.utils.GetStringFromUrl;
 import com.useful.useful.utils.JarUtils;
 import com.useful.useful.utils.ListStore;
 import com.useful.useful.utils.Performance;
 import com.useful.useful.utils.SerializableLocation;
 import com.useful.useful.utils.UConnect;
 import com.useful.useful.utils.UConnectDataRequest;
 import com.useful.useful.utils.UniqueString;
 import com.useful.useful.utils.uConnectConnect;
  
 public class useful extends JavaPlugin {
 	public static String pluginFolder;
     public SQLite sqlite;
 	public ListStore heros;
 	public ListStore commandViewers;
 	public ListStore warns;
 	public ListStore rules;
 	public ListStore info;
 	public ListStore warnsplayer;
 	public ListStore auths;
 	public ListStore changelog;
 	public ListStore uconnectAds;
 	public uPerms permManager = null;
 	public int number;
 	public int numberorig;
 	public Encrpyter encrypter = null;
 	public static HashMap<String, ArrayList<String>> jailed = new HashMap<String, ArrayList<String>>();
 	public static HashMap<String, ArrayList<String>> mail = new HashMap<String, ArrayList<String>>();
 	public static HashMap<String, SerializableLocation> warps = new HashMap<String, SerializableLocation>();
 	public static HashMap<String, String> warpowners = new HashMap<String, String>();
 	public static ArrayList<String> invsee = new ArrayList<String>();
 	public static ArrayList<String> blockedCmds = new ArrayList<String>();
 	public static HashMap<String, Boolean> uhost_settings = new HashMap<String, Boolean>();
 	public static HashMap<String, Boolean> updateManager = new HashMap<String, Boolean>();
 	public static HashMap<String, Double> carBoosts = new HashMap<String, Double>();
 	public static HashMap<String, Boolean> authed = new HashMap<String, Boolean>();
 	public BukkitTask broadcaster = null;
 	public BukkitTask backup = null;
 	public BukkitTask idle = null;
 	public BukkitTask reloadUcMsg = null;
 	public BukkitTask UcMsgNotify = null;
 	public static useful plugin;
 	public Colors colors = null;
 	public double pluginVersion = 0;
 	public UConnect uconnect = null;
 	private String pluginAuth = null;
 	static File ranksFile;
 	static FileConfiguration ranks;
 	static File upermsFile;
 	static FileConfiguration uperms;
 	//static FileConfiguration config;
 	public static FileConfiguration config;
 	boolean idleRunning = false;
 	boolean ucReloadMsgRun = false;
 	public ColoredLogger colLogger;
 
 	public static String  colorise(String prefix){
 		prefix = prefix.replace("&0", "" + ChatColor.BLACK);
 		prefix = prefix.replace("&1", "" + ChatColor.DARK_BLUE);
 		prefix = prefix.replace("&2", "" + ChatColor.DARK_GREEN);
 		prefix = prefix.replace("&3", "" + ChatColor.DARK_AQUA);
 		prefix = prefix.replace("&4", "" + ChatColor.DARK_RED);
 		prefix = prefix.replace("&5", "" + ChatColor.DARK_PURPLE);
 		prefix = prefix.replace("&6", "" + ChatColor.GOLD);
 		prefix = prefix.replace("&7", "" + ChatColor.GRAY);
 		prefix = prefix.replace("&8", "" + ChatColor.DARK_GRAY);
 		prefix = prefix.replace("&9", "" + ChatColor.BLUE);
 		prefix = prefix.replace("&a", "" + ChatColor.GREEN);
 		prefix = prefix.replace("&b", "" + ChatColor.AQUA);
 		prefix = prefix.replace("&c", "" + ChatColor.RED);
 		prefix = prefix.replace("&d", "" + ChatColor.LIGHT_PURPLE);
 		prefix = prefix.replace("&e", "" + ChatColor.YELLOW);
 		prefix = prefix.replace("&f", "" + ChatColor.WHITE);
 		prefix = prefix.replace("&r", "" + ChatColor.RESET);
 		prefix = prefix.replace("&l", "" + ChatColor.BOLD);
 		prefix = prefix.replace("&i", "" + ChatColor.ITALIC);
 		prefix = prefix.replace("&m", "" + ChatColor.MAGIC);
 		return prefix;
 	}
 	private void addClassPath(final URL url) throws IOException {
         final URLClassLoader sysloader = (URLClassLoader) ClassLoader
                 .getSystemClassLoader();
         final Class<URLClassLoader> sysclass = URLClassLoader.class;
         try {
             final Method method = sysclass.getDeclaredMethod("addURL",
                     new Class[] { URL.class });
             method.setAccessible(true);
             method.invoke(sysloader, new Object[] { url });
         } catch (final Throwable t) {
             t.printStackTrace();
             throw new IOException("Error adding " + url
                     + " to system classloader");
         }
     }
 	@SuppressWarnings("unchecked")
 	public HashMap<String, ArrayList<String>> load(String path)
 	{
 		try
 		{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
 			Object result = ois.readObject();
 			ois.close();
 			//you can feel free to cast result to HashMap<String, Integer> if you know there's that HashMap in the file
 			return (HashMap<String, ArrayList<String>>)result;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public HashMap<String, String> loadHashMapString(String path)
 	{
 		try
 		{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
 			Object result = ois.readObject();
 			ois.close();
 			//you can feel free to cast result to HashMap<String, Integer> if you know there's that HashMap in the file
 			return (HashMap<String, String>) result;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public HashMap<String, Boolean> loadHashMapBoolean(String path)
 	{
 		try
 		{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
 			Object result = ois.readObject();
 			ois.close();
 			//you can feel free to cast result to HashMap<String, Integer> if you know there's that HashMap in the file
 			return (HashMap<String, Boolean>) result;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public HashMap<String, SerializableLocation> loadHashObj(String path)
 	{
 		try
 		{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
 			Object result = ois.readObject();
 			ois.close();
 			//you can feel free to cast result to HashMap<String, Integer> if you know there's that HashMap in the file
 			return (HashMap<String, SerializableLocation>)result;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public double getJailTime(Player p) {
 		String name = p.getName();
 		if (useful.jailed.containsKey(name)){
 			ArrayList<String> array = useful.jailed.get(name);
 			String time = array.get(0);
 			double time2 = Double.parseDouble(time);
 			return time2;
 		}
 		else {
 		return 0;
 		}
 		
 	}
 
 	public boolean playerIsJailed(Player p) {
 		String name = p.getName();
 		if (useful.jailed.containsKey(name)){
 			return true;
 		}
 		else {
 		return false;
 		}
 	}  
 	
 	 private Object getPrivateField(Object object, String field) throws SecurityException,
      NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
  Class<?> clazz = object.getClass();
  Field objectField = clazz.getDeclaredField(field);
  objectField.setAccessible(true);
  Object result = objectField.get(object);
  objectField.setAccessible(false);
  return result;
 }
 
 /**
 * Unregister a command from bukkit.
 * 
 * @param cmd
 */
 private void unRegisterBukkitCommand(PluginCommand cmd) {
  try {
      Object result = getPrivateField(getServer().getPluginManager(), "commandMap");
      SimpleCommandMap commandMap = (SimpleCommandMap) result;
      Object map = getPrivateField(commandMap, "knownCommands");
      @SuppressWarnings("unchecked")
      HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
      knownCommands.remove(cmd.getName());
      for (String alias : cmd.getAliases())
          knownCommands.remove(alias);
  } catch (SecurityException e) {
      e.printStackTrace();
  } catch (IllegalArgumentException e) {
      e.printStackTrace();
  } catch (NoSuchFieldException e) {
      e.printStackTrace();
  } catch (IllegalAccessException e) {
      e.printStackTrace();
  }
 }
 
 //registering commands
 
 public void saveYamls() {
     try {
         //DO NOT SAVE ranks.save(ranksFile);
     } catch (Exception e) {
         e.printStackTrace();
     }
 }
 public void loadYamls() {
     try {
         ranks.load(ranksFile);
         uperms.options().pathSeparator('/');
         uperms.load(upermsFile);
         uperms.options().pathSeparator('/');
     } catch (Exception e) {
         e.printStackTrace();
     }
 }
 private void copy(InputStream in, File file) {
     try {
         OutputStream out = new FileOutputStream(file);
         byte[] buf = new byte[1024];
         int len;
         while((len=in.read(buf))>0){
             out.write(buf,0,len);
         }
         out.close();
         in.close();
     } catch (Exception e) {
         e.printStackTrace();
     }
 }
 public void sqlConnection() {
 sqlite = new SQLite(plugin.getLogger(),
 		        "useful",
                 //"useful",
 		        plugin.getDataFolder().getAbsolutePath(),
                 "data"
                 );
 //Make sure sqlite is the same as the variable you specified at the top of the plugin!
 try {
 sqlite.open();
     } catch (Exception e) {
         plugin.getLogger().info(e.getMessage());
         getPluginLoader().disablePlugin(plugin);
     }
 }
 @SuppressWarnings("deprecation")
 public void sqlTableCheck() {
     if(sqlite.checkTable("warps")){
     }else{
   try {
 	sqlite.query("CREATE TABLE warps (playername VARCHAR(50),  warpname VARCHAR(50), locWorld VARCHAR(50), locX VARCHAR(50), locY VARCHAR(50), locZ VARCHAR(50), locYaw VARCHAR(50), locPitch VARCHAR(50));");
 } catch (SQLException e) {
 	e.printStackTrace();
 }
  
         //example way to save it:  sqlite.query("INSERT INTO warps VALUES('storm345', 'mywarp', 'world', 5, 5, 5, 5, 5);"); //This is optional. You can do this later if you want.
     }
     //Now make the jails database!
     if(sqlite.checkTable("jails")){
     	    }else{
     	  try {
 			sqlite.query("CREATE TABLE jails (jailname VARCHAR(50), locWorld VARCHAR(50), locX VARCHAR(50), locY VARCHAR(50), locZ VARCHAR(50), locYaw VARCHAR(50), locPitch VARCHAR(50));");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
     	 
     	        //example way to save it:  sqlite.query("INSERT INTO warps VALUES('storm345', 'mywarp', 'world', 5, 5, 5, 5, 5);"); //This is optional. You can do this later if you want.
     	    }
     if(sqlite.checkTable("wir")){
   	    }else{
   	  try {
 		sqlite.query("CREATE TABLE wir (signNo VARCHAR(50), locWorld VARCHAR(50), locX VARCHAR(50), locY VARCHAR(50), locZ VARCHAR(50), locYaw VARCHAR(50), locPitch VARCHAR(50));");
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
   	 
   	        //example way to save it:  sqlite.query("INSERT INTO warps VALUES('storm345', 'mywarp', 'world', 5, 5, 5, 5, 5);"); //This is optional. You can do this later if you want.
   	    }
     if(sqlite.checkTable("worldgm")){
 	    }else{
 	  try {
 		sqlite.query("CREATE TABLE worldgm (world VARCHAR(50), gamemode VARCHAR(50));");
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
 	 
 	        //example way to save it:  sqlite.query("INSERT INTO warps VALUES('storm345', 'mywarp', 'world', 5, 5, 5, 5, 5);"); //This is optional. You can do this later if you want.
 	    }
 }
 public void warpsConverter(){
 	HashMap<String, SerializableLocation> oldWarps = new HashMap<String, SerializableLocation>();
 	String path3 = getDataFolder() + File.separator + "warps.bin";
 	File file3 = new File(path3);
 	 
 	if(file3.exists()){ // check if file exists before loading to avoid errors!
 		oldWarps  = loadHashObj(path3);
 	}
 	HashMap<String, String> oldWarpowners = new HashMap<String, String>();
 	String path35 = getDataFolder() + File.separator + "warpowners.bin";
 	File file35 = new File(path35);
 	 
 	if(file35.exists()){ // check if file exists before loading to avoid errors!
 		oldWarpowners  = loadHashMapString(path35);
 	}
 	Set<String> warpnames = oldWarps.keySet();
 	Object[] warps = warpnames.toArray();
 	Set<String> thewarpowners = oldWarpowners.keySet();
 	Object[] owners = thewarpowners.toArray();
 	for(int i=0;i<warps.length;i++){
 		// go through each warp
 		String owner = "Unknown player";
 		String world;
 		double x;
 		double y;
 		double z;
 		double yaw;
 		double pitch;
 		String warp = ((String) warps[i]).toLowerCase();
 		//we now have warp set!
 		for(int r=0;r<owners.length;r++){
 			// Go through each warps owners
 			if(((String)owners[r]).equalsIgnoreCase(warp)){
 				owner = oldWarpowners.get(owners[r]);
 			}
 		}
 		//we now have owner set!
 	Location loc = oldWarps.get(warp).getLocation(getServer());
 	world = loc.getWorld().getName();
 	x = loc.getX();
 	y = loc.getY();
 	z = loc.getZ();
 	yaw = loc.getYaw();
 	pitch = loc.getPitch();
 	//We now have all the location details set!
 	String theData = "INSERT INTO warps VALUES('"+owner+"', '"+warp+"', '"+world+"', "+x+", "+y+", "+z+", "+yaw+", "+pitch+");";
 	try {
 		sqlite.query(theData);
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
 	getLogger().info("Successfully updated " + warp + " to the new data storage format!");
 	}
 	file3.delete();
 	file35.delete();
 }
 public void jailsConverter(){
 	HashMap<String, SerializableLocation> oldJails = new HashMap<String, SerializableLocation>();
 	String path3 = getDataFolder() + File.separator + "jails.bin";
 	File file3 = new File(path3);
 	 
 	if(file3.exists()){ // check if file exists before loading to avoid errors!
 		oldJails  = loadHashObj(path3);
 	}
 	Set<String> jailnames = oldJails.keySet();
 	Object[] jails = jailnames.toArray();
 	for(int i=0;i<jails.length;i++){
 		// go through each jail
 		String world;
 		double x;
 		double y;
 		double z;
 		double yaw;
 		double pitch;
 		String jail = ((String) jails[i]).toLowerCase();
 		//we now have jail set!
 	Location loc = oldJails.get(jail).getLocation(getServer());
 	world = loc.getWorld().getName();
 	x = loc.getX();
 	y = loc.getY();
 	z = loc.getZ();
 	yaw = loc.getYaw();
 	pitch = loc.getPitch();
 	//We now have all the location details set!
 	String theData = "INSERT INTO jails VALUES('"+jail.toLowerCase()+"', '"+world+"', "+x+", "+y+", "+z+", "+yaw+", "+pitch+");";
 	try {
 		sqlite.query(theData);
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
 	getLogger().info("Successfully updated " + jail + " to the new data storage format!");
 	}
 	new File(getDataFolder() + File.separator + "jails.bin").delete();
 }
 	//updated eclipse to juno!
 	public void onEnable(){
 		plugin = this;
 		pluginAuth = UniqueString.generate();
 		colLogger = new ColoredLogger(this);
 		try{
 			colLogger.info(ChatColor.GREEN + "Loading useful...");
 //LOAD JARS!
 			
 			try {
 	            final File[] libs = new File[] {
 	                    new File(getDataFolder() + File.separator + "uConnect", "httpclient.jar"),
 	                    new File(getDataFolder() + File.separator + "uConnect", "httpmime.jar"),
 	                    new File(getDataFolder() + File.separator + "uConnect", "httpcore.jar"),
 	                    new File(getDataFolder() + File.separator + "uConnect", "dropbox.jar"),
 	                    //new File(getDataFolder() + File.separator + "uConnect", "apachelogging.jar"),
 	                    //new File(getDataFolder() + File.separator + "uConnect", "junit.jar"),
 	                    new File(getDataFolder() + File.separator + "uConnect", "commons-logging.jar"),
 	                    new File(getDataFolder() + File.separator + "lib", "SQLibrary.jar"),
 	                    new File(getDataFolder() + File.separator + "uConnect", "json_simple.jar")};
 	            
 	            for (final File lib : libs) {
 	                if (!lib.exists() || lib.length() < 2) {
 	                    JarUtils.extractFromJar(lib.getName(),
 	                            lib.getAbsolutePath());
 	                }
 	            }
 	            for (final File lib : libs) {
 	                if (!lib.exists() || lib.length() < 2) {
 	                    getLogger().warning(
 	                            "There was a critical error loading My plugin! Could not find lib: "
 	                                    + lib.getName());
 	                    Bukkit.getServer().getPluginManager().disablePlugin(this);
 	                    return;
 	                }
 	                addClassPath(JarUtils.getJarUrl(lib));
 	            }
 	        } catch (final Exception e) {
 	            e.printStackTrace();
 	        }
 			sqlConnection();
 	        sqlTableCheck();
 	        copy(getResource("changelog.txt"), new File(getDataFolder().getAbsolutePath() + File.separator + "changelog.txt"));
 			if(new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml").exists() == false){
 				copy(getResource("config.yml"), new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
 			}
 			pluginFolder = this.getDataFolder().getAbsolutePath();
 			(new File(pluginFolder)).mkdirs();
 			config = getConfig();
 			ranksFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "ranks.yml");
 			//File useful = new File(getDataFolder() + "config.yml");
 			//useful.mkdir();
 			if(!ranksFile.exists()){
 		        ranksFile.getParentFile().mkdirs();
 		        ranksFile.createNewFile();
 		    }
 			ranks = new YamlConfiguration();
 			upermsFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "uperms.yml");
 			//File useful = new File(getDataFolder() + "config.yml");
 			//useful.mkdir();
 			if(!upermsFile.exists()){
 		        upermsFile.getParentFile().mkdirs();
 		        upermsFile.createNewFile();
 		    }
 			uperms = new YamlConfiguration();
 			uperms.options().pathSeparator('/');
 			loadYamls();
 			if(!config.contains("general.burn.enable")){
 				copy(getResource("config.yml"), new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
 				saveConfig();
 				config = getConfig();
 			}
 			String verS = getServer().getPluginManager().getPlugin("useful").getDescription().getVersion();
 			double version = 0;
 			try {
 				version = Double.parseDouble(verS);
 				config.set("version.current", version);
 				pluginVersion = version;
 			} catch (Exception e2) {
 				getLogger().log(Level.SEVERE, "ERROR: Error retrieving version! Autoupdate will not work!");
 			}
 			uperms.options().pathSeparator('/');
 			if(upermsFile.length() < 1){
 				uperms.options().pathSeparator('/');
 				uperms.set("groups/default/permissions/example.PermissionNode", true);
 				uperms.set("users/playerName/permissions/example.PermissionNode", true);
 				uperms.set("users/playerName/groups", Arrays.asList("default"));
 				uperms.set("groups/Admin/permissions/example.perm", true);
 				uperms.set("groups/Admin/inheritance", Arrays.asList("default"));
 			}
 			uperms.options().pathSeparator('/');
 			uperms.save(upermsFile);
 			if(!config.contains("version.autoupdate.# description")) {
 				config.set("version.autoupdate.# description", "If enabled this will check for updates at an interval of roughly once every hour and if it finds one it will automatically install it.");
 				}
 			if(!config.contains("version.autoupdate.enable")) {
 				config.set("version.autoupdate.enable", true);
 				}
 			if(!config.contains("version.update.# description")) {
 				config.set("version.update.# description", "If enabled this will tell operators when a different version of useful is being used to that previously so they know to configure it");
 				}
 			if(!config.contains("version.update.notify")) {
 				config.set("version.update.notify", true);
 				}
 			if(!config.contains("general.uchat.# description")) {
 				config.set("general.uchat.# description", "If enabled this will add chat colors and the prefixes set with ranks.");
 				}
 			if(!config.contains("general.uchat.enable")) {
 				config.set("general.uchat.enable", true);
 				}
 			if(!config.contains("general.infiniteDispenserSign.# description")) {
 				config.set("general.infiniteDispenserSign.# description", "If enabled this will add infinite dispenser item signs.");
 				}
 			if(!config.contains("general.infiniteDispenserSign.enable")) {
 				config.set("general.infiniteDispenserSign.enable", true);
 				}
 			if(!config.contains("general.cars.# description")) {
 				config.set("general.cars.# description", "If enabled this will allow for drivable cars(Minecarts not on rails)");
 				}
 			if(!config.contains("general.cars.enable")) {
 				config.set("general.cars.enable", true);
 				}
 			if(!config.contains("general.cars.defSpeed")) {
 				config.set("general.cars.defSpeed", (double)30);
 				}
 			if(!config.contains("general.cars.lowBoost")) {
 				config.set("general.cars.lowBoost", 263);
 				}
 			if(!config.contains("general.cars.medBoost")) {
 				config.set("general.cars.medBoost", 265);
 				}
 			if(!config.contains("general.cars.highBoost")) {
 				config.set("general.cars.highBoost", 264);
 				}
 			if(!config.contains("general.cars.blockBoost")) {
 				config.set("general.cars.blockBoost", 41);
 				}
 			if(!config.contains("general.cars.HighblockBoost")) {
 				config.set("general.cars.HighblockBoost", 57);
 				}
 			if(!config.contains("general.cars.ResetblockBoost")) {
 				config.set("general.cars.ResetblockBoost", 133);
 				}
 			if(!config.contains("general.cars.jumpBlock")) {
 				config.set("general.cars.jumpBlock", 42);
 				}
 			if(!config.contains("general.cars.jumpAmount")) {
 				config.set("general.cars.jumpAmount", 60);
 				}
 			if(!config.contains("general.rules.# description")) {
 				config.set("general.rules.# description", "If enabled this will allow for /rules.");
 				}
 			if(!config.contains("general.rules.enable")) {
 				config.set("general.rules.enable", true);
 				}
 			if(!config.contains("general.head.# description")) {
 				config.set("general.head.# description", "If enabled this will allow for /head.");
 				}
 			if(!config.contains("general.head.enable")) {
 				config.set("general.head.enable", true);
 				}
 			if(!config.contains("general.back.# description")) {
 				config.set("general.back.# description", "If enabled this will allow for /back.");
 				}
 			if(!config.contains("general.back.enable")) {
 				config.set("general.back.enable", true);
 				}
 			if(!config.contains("general.enchant.# description")) {
 				config.set("general.enchant.# description", "If enabled this will allow for /enchant.");
 				}
 			if(!config.contains("general.enchant.enable")) {
 				config.set("general.enchant.enable", true);
 				}
 			if(!config.contains("general.compass.# description")) {
 				config.set("general.compass.# description", "If enabled this will allow for /compass.");
 				}
 			if(!config.contains("general.compass.enable")) {
 				config.set("general.compass.enable", true);
 				}
 			if(!config.contains("general.worldgm.# description")) {
 				config.set("general.worldgm.# description", "If enabled this will allow for default gamemodes set for each world and bypassed with the permission   useful.worldgm.bypass.");
 				}
 			if(!config.contains("general.worldgm.enable")) {
 				config.set("general.worldgm.enable", true);
 				}
 			if(!config.contains("general.shelter.# description")) {
 				config.set("general.shelter.# description", "If enabled this will allow for /shelter which makes an instant shelter at the players location.");
 				}
 			if(!config.contains("general.shelter.enable")) {
 				config.set("general.shelter.enable", true);
 				}
 			if(!config.contains("general.info.# description")) {
 				config.set("general.info.# description", "If enabled it will allow for /info or /about or /information");
 				}
 			if(!config.contains("general.info.enable")) {
 				config.set("general.info.enable", true);
 				}
 			if(!config.contains("general.firework.# description")) {
 				config.set("general.firework.# description", "If enabled it will allow for /firework");
 				}
 			if(!config.contains("general.firework.enable")) {
 				config.set("general.firework.enable", true);
 				}
 			if(!config.contains("general.potion.# description")) {
 				config.set("general.potion.# description", "If enabled it will allow for /potion");
 				}
 			if(!config.contains("general.potion.enable")) {
 				config.set("general.potion.enable", true);
 				}
 			if(!config.contains("general.craft.# description")) {
 				config.set("general.craft.# description", "If enabled it will allow for /craft");
 				}
 			if(!config.contains("general.craft.enable")) {
 				config.set("general.craft.enable", true);
 				}
 			if(!config.contains("signs.warpSigns.enable")) {
 				config.set("signs.warpSigns.enable", true);
 				}
 			if(!config.contains("signs.warpsSigns.enable")) {
 				config.set("signs.warpsSigns.enable", true);
 				}
 			if(!config.contains("signs.spawnSigns.enable")) {
 				config.set("signs.spawnSigns.enable", true);
 				}
 			if(!config.contains("signs.spawnpointSigns.enable")) {
 				config.set("signs.spawnpointSigns.enable", true);
 				}
 			if(!config.contains("signs.worldsSigns.enable")) {
 				config.set("signs.worldsSigns.enable", true);
 				}
 			if(!config.contains("signs.worldSigns.enable")) {
 				config.set("signs.worldSigns.enable", true);
 				}
 			if(!config.contains("signs.jailsSigns.enable")) {
 				config.set("signs.jailsSigns.enable", true);
 				}
 			if(!config.contains("signs.uCommandsSigns.enable")) {
 				config.set("signs.uCommandsSigns.enable", true);
 				}
 			if(!config.contains("signs.CommandSigns.enable")) {
 				config.set("signs.CommandSigns.enable", true);
 				}
 			if(!config.contains("signs.GamemodeSigns.enable")) {
 				config.set("signs.GamemodeSigns.enable", true);
 				}
 			if(!config.contains("signs.LiftSigns.enable")) {
 				config.set("signs.LiftSigns.enable", true);
 				}
 			if(!config.contains("signs.onlineSigns.enable")) {
 				config.set("signs.onlineSigns.enable", true);
 				}
 			if(!ranks.contains("ranks.default.permission")) {
 				ranks.set("ranks.default.permission", "uranks.default");
 				}
 			if(!ranks.contains("ranks.default.prefix")) {
 				ranks.set("ranks.default.prefix", "&a[Player]&f*name*&f");
 				}
 			if(!config.contains("general.burn.# description")) {
 				config.set("general.burn.# description", "If enabled this allows for /burn");
 				}
 			if(!config.contains("general.burn.enable")) {
 				config.set("general.burn.enable", true);
 				}
 			if(!config.contains("general.murder.# description")) {
 				config.set("general.murder.# description", "If enabled this allows for /murder");
 				}
 			if(!config.contains("general.murder.enable")) {
 				config.set("general.murder.enable", true);
 				}
 			if(!config.contains("general.genocide.# description")) {
 				config.set("general.genocide.# description", "If enabled this allows for /genocide");
 				}
 			if(!config.contains("general.genocide.enable")) {
 				config.set("general.genocide.enable", true);
 				}
 			if(!config.contains("general.log_commands_to_console")) {
 				config.set("general.log_commands_to_console", true);
 				}
 			if(!config.contains("general.mobtypes.# description")) {
 				config.set("general.mobtypes.# description", "If enabled this allows for /mobtypes");
 				}
 			if(!config.contains("general.mobtypes.enable")) {
 				config.set("general.mobtypes.enable", true);
 				}
 			if(!config.contains("general.spawnmob.# description")) {
 				config.set("general.spawnmob.# description", "If enabled this allows for /spawnmob");
 				}
 			if(!config.contains("general.spawnmob.enable")) {
 				config.set("general.spawnmob.enable", true);
 				}
 			if(!config.contains("general.mobset.# description")) {
 				config.set("general.mobset.# description", "If enabled this allows for /mobset");
 				}
 			if(!config.contains("general.mobset.enable")) {
 				config.set("general.mobset.enable", true);
 				}
 			if(!config.contains("general.mail.# description")) {
 				config.set("general.mail.# description", "If enabled this will allow use of the mail system");
 				}
 			if(!config.contains("general.mail.enable")) {
 				config.set("general.mail.enable", true);
 				}
 			if(!config.contains("general.smite.# description")) {
 				config.set("general.smite.# description", "If enabled this allows for /smite and damage: allows for it to strike real fire if true or fake fire if false");
 				}
 			if(!config.contains("general.smite.enable")) {
 				config.set("general.smite.enable", true);
 				}
 			if(!config.contains("general.smite.damage")) {
 				config.set("general.smite.damage", true);
 				}
 			if(!config.contains("general.spawn.# description")) {
 				config.set("general.spawn.# description", "If enabled this allows for /spawn");
 				}
 			if(!config.contains("general.spawn.enable")) {
 				config.set("general.spawn.enable", true);
 				}
 			if(!config.contains("general.setspawn.# description")) {
 				config.set("general.setspawn.# description", "If enabled this will allow for /setspawn which sets the WORLD spawn point and also the JAIL unjail point.");
 				}
 			if(!config.contains("general.setspawn.enable")) {
 				config.set("general.setspawn.enable", true);
 				}
 			if(!config.contains("general.timeget.# description")) {
 				config.set("general.timeget.# description", "If enabled this allows for /timeget");
 				}
 			if(!config.contains("general.timeget.enable")) {
 				config.set("general.timeget.enable", true);
 				}
 			if(!config.contains("general.warps.# description")) {
 				config.set("general.warps.# description", "If enabled this allows for /warps");
 				}
 			if(!config.contains("general.warps.enable")) {
 				config.set("general.warps.enable", true);
 				}
 			if(!config.contains("general.time.# description")) {
 				config.set("general.time.# description", "If enabled this allows for useful's /time or /timeset command");
 				}
 			if(!config.contains("general.time.enable")) {
 				config.set("general.time.enable", true);
 				}
 			if(!config.contains("general.gamemode.# description")) {
 				config.set("general.gamemode.# description", "If enabled this allows for /gm");
 				}
 			if(!config.contains("general.gamemode.enable")) {
 				config.set("general.gamemode.enable", true);
 				}
 			if(!config.contains("general.jail.# description")) {
 				config.set("general.jail.# description", "If enabled this allows for the useful plugins jail system");
 				}
 			if(!config.contains("general.jail.enable")) {
 				config.set("general.jail.enable", true);
 				}
 			if(!config.contains("general.killmobs.# description")) {
 				config.set("general.killmobs.# description", "If enabled this allows for /killmobs");
 				}
 			if(!config.contains("general.killmobs.enable")) {
 				config.set("general.killmobs.enable", true);
 				}
 			if(!config.contains("general.tp.# description")) {
 				config.set("general.tp.# description", "If enabled this allows for useful's /tp");
 				}
 			if(!config.contains("general.tp.enable")) {
 				config.set("general.tp.enable", true);
 				}
 			if(!config.contains("general.tpa.# description")) {
 				config.set("general.tpa.# description", "If enabled this allows for useful's /tpa or /tpahere");
 				}
 			if(!config.contains("general.tpa.enable")) {
 				config.set("general.tpa.enable", true);
 				}
 			if(!config.contains("general.tphere.# description")) {
 				config.set("general.tphere.# description", "If enabled this allows for useful's /tphere");
 				}
 			if(!config.contains("general.tphere.enable")) {
 				config.set("general.tphere.enable", true);
 				}
 			if(!config.contains("general.hat.# description")) {
 				config.set("general.hat.# description", "If enabled this allows for /hat and /hat off");
 				}
 			if(!config.contains("general.hat.enable")) {
 				config.set("general.hat.enable", true);
 				}
 			if(!config.contains("general.hero.# description")) {
 				config.set("general.hero.# description", "If enabled this allows for /hero");
 				}
 			if(!config.contains("general.hero.enable")) {
 				config.set("general.hero.enable", true);
 				}
 			if(!config.contains("general.count.# description")) {
 				config.set("general.count.# description", "If enabled this allows for /count up/down");
 				}
 			if(!config.contains("general.count.enable")) {
 				config.set("general.count.enable", true);
 				}
 			if(!config.contains("general.warning.# description")) {
 				config.set("general.warning.# description", "If enabled this allows for the warning system and also if sentoall: if true it will broadcast the warning to everyone on the server.");
 				}
 			if(!config.contains("general.warning.sendtoall")) {
 				config.set("general.warning.sendtoall", true);
 				}
 			if(!config.contains("general.eat.# description")) {
 				config.set("general.eat.# description", "If enabled this allows for /eat");
 			}
 			if(!config.contains("general.eat.enable")) {
 				config.set("general.eat.enable", true);
 			}
 			if(!config.contains("general.feast.# description")) {
 				config.set("general.feast.# description", "If enabled this allows for /feast");
 			}
 			if(!config.contains("general.feast.enable")) {
 				config.set("general.feast.enable", true);
 			}
 			if(!config.contains("general.levelup.# description")) {
 				config.set("general.levelup.# description", "If enabled this allows for /levelup");
 				}	
 	        if(!config.contains("general.levelup.enable")) {
 					config.set("general.levelup.enable", true);
 					}	
 	        if(!config.contains("general.levelup.enable")) {
 				config.set("general.levelup.enable", true);
 				}	
 	        if(!config.contains("general.setlevel.# description")) {
 				config.set("general.setlevel.# description", "If enabled this allows for /setlevel");
 				}	
 	        if(!config.contains("general.setlevel.enable")) {
 				config.set("general.setlevel.enable", true);
 				}	
 	        if(!config.contains("general.getid.# description")) {
 				config.set("general.getid.# description", "If enabled this allows for /getid");
 				}	
 	        if(!config.contains("general.getid.enable")) {
 				config.set("general.getid.enable", true);
 				}
 	        if(!config.contains("general.message.# description")) {
 				config.set("general.message.# description", "If enabled this allows for /message or /msg");
 				}
 	        if(!config.contains("general.message.enable")) {
 				config.set("general.message.enable", true);
 				}	
 	        if(!config.contains("general.magicmessage.# description")) {
 				config.set("general.magicmessage.# description", "If enabled this allows for /magicmessage");
 				}
 	        if(!config.contains("general.magicmessage.enable")) {
 				config.set("general.magicmessage.enable", true);
 				}	
 	        if(!config.contains("general.listplayers.# description")) {
 				config.set("general.listplayers.# description", "If enabled this allows for /listplayers to list all players that have ever been on the server");
 				}
 	        if(!config.contains("general.listplayers.enable")) {
 				config.set("general.listplayers.enable", true);
 				}	
 	        if(!config.contains("general.warning.enable")) {
 				config.set("general.warning.enable", true);
 				}
 	        if(!config.contains("general.disabledmessage")) {
 				config.set("general.disabledmessage", "Sorry that feature is blocked!");
 				}
 	        if(!config.contains("general.disable_wither_spawning")) {
 				config.set("general.disable_wither_spawning", false);
 				}	
 	        if(!config.contains("general.disable_enderdragon_player_spawning")) {
 				config.set("general.disable_enderdragon_player_spawning", false);
 				}	
 	        if(!config.contains("general.ban.# description")) {
 				config.set("general.ban.# description", "If enabled this allows for useful's /ban");
 				}
 	        if(!config.contains("general.ban.enable")) {
 				config.set("general.ban.enable", true);
 				}
 	        if(!config.contains("general.kick.# description")) {
 				config.set("general.kick.# description", "If enabled this allows for useful's /kick");
 				}
 	        if(!config.contains("general.kick.enable")) {
 				config.set("general.kick.enable", true);
 				}
 	        if(!config.contains("general.ci.# description")) {
 				config.set("general.ci.# description", "If enabled this allows for /ci");
 				}
 	        if(!config.contains("general.ci.enable")) {
 				config.set("general.ci.enable", true);
 				}
 	        if(!config.contains("general.world.# description")) {
 				config.set("general.world.# description", "If enabled this allows for /world");
 				}
 	        if(!config.contains("general.world.enable")) {
 				config.set("general.world.enable", true);
 				}
 	        if(!config.contains("general.worlds.# description")) {
 				config.set("general.worlds.# description", "If enabled this allows for /worlds");
 				}
 	        if(!config.contains("general.worlds.enable")) {
 				config.set("general.worlds.enable", true);
 				}
 	        if(!config.contains("general.canfly.# description")) {
 				config.set("general.canfly.# description", "If enabled this allows for /canfly");
 				}
 	        if(!config.contains("general.canfly.enable")) {
 				config.set("general.canfly.enable", true);
 				}
 	        if(!config.contains("general.rename.# description")) {
 				config.set("general.rename.# description", "If enabled this allows for /rename");
 				}
 	        if(!config.contains("general.rename.enable")) {
 				config.set("general.rename.enable", true);
 				}
 	        if(!config.contains("general.invsee.# description")) {
 				config.set("general.invsee.# description", "If enabled this allows for /invsee and if allow_edit: is true the player can edit the others inventory");
 				}
 	        if(!config.contains("general.invsee.enable")) {
 				config.set("general.invsee.enable", true);
 				}
 	        if(!config.contains("general.invsee.allow-edit")) {
 				config.set("general.invsee.allow-edit", false);
 				}
 	        if(!config.contains("general.creativecommand.# description")) {
 				config.set("general.creativecommand.# description", "If enabled this allows for /creative");
 				}
 	        if(!config.contains("general.creativecommand.enable")) {
 				config.set("general.creativecommand.enable", true);
 				}
 	        if(!config.contains("general.survivalcommand.# description")) {
 				config.set("general.survivalcommand.# description", "If enabled this allows for /survival");
 				}
 	        if(!config.contains("general.survivalcommand.enable")) {
 				config.set("general.survivalcommand.enable", true);
 				}
 	        if(!config.contains("general.adventurecommand.# description")) {
 				config.set("general.adventurecommand.# description", "If enabled this allows for /adventure");
 				}
 	        if(!config.contains("general.adventurecommand.enable")) {
 				config.set("general.adventurecommand.enable", true);
 				}
 	        if(!config.contains("general.broadcast.# description")) {
 				config.set("general.broadcast.# description", "If enabled this allows for the broadcast message to be sent every [X(Set below)] minutes.");
 				}
 	        if(!config.contains("general.broadcast.enable")) {
 				config.set("general.broadcast.enable", true);
 				}
 	        if(!config.contains("general.broadcast.delay(minutes)")) {
 				config.set("general.broadcast.delay(minutes)", 5);
 				}
 	        if(!config.contains("general.authentication.# description")) {
 				config.set("general.authentication.description", "If enabled this will tell the plugin to use the authentication system!");
 				}
 	        if(!config.contains("general.authentication.enable")) {
 				config.set("general.authentication.enable", true);
 				}
 	        if(!config.contains("general.uhost.# description")) {
 				config.set("general.uhost.# description", "If enabled this allows for the /uhost system to be used.");
 				}
 	        if(!config.contains("general.uhost.enable")) {
 				config.set("general.uhost.enable", true);
 				}
 	        if(!config.contains("general.backup.# description")) {
 				config.set("general.backup.# description", "If enabled this backs up all of your world to a folder within the useful directory");
 				}
 	        if(!config.contains("general.backup.enable")) {
 				config.set("general.backup.enable", true);
 				}
 	        if(!config.contains("general.backup.auto")) {
 				config.set("general.backup.auto", true);
 				}
 	        if(!config.contains("general.welcome_message.# description")) {
 				config.set("general.welcome_message.# description", "If enabled, server players will be sent this msg when they join");
 				}
 	        if(!config.contains("general.welcome_message.msg")) {
 				config.set("general.welcome_message.msg", "&aWelcome to the server! Do /information for info and /rules for rules!");
 				}
 	        if(!config.contains("general.welcome_message.enable")) {
 				config.set("general.welcome_message.enable", true);
 				}
 	        if(!config.contains("general.enableCustomLoginMessage")){
 	        	config.set("general.enableCustomLoginMessage", true);
 	        }
 	        if(!config.contains("general.enableCustomQuitMessage")){
 	        	config.set("general.enableCustomQuitMessage", true);
 	        }
 	        if(!config.contains("general.loginmessage")) {
 				config.set("general.loginmessage", "*name* has joined the game!");
 				}
 	        if(!config.contains("general.quitmessage")) {
 				config.set("general.quitmessage", "*name* has left the game!");
 				}
 	        if(!config.contains("general.disable_tnt_damage")) {
 				config.set("general.disable_tnt_damage", false);
 				}
 	        if(!config.contains("general.tnt_radius")) {
 				config.set("general.tnt_radius", 10);
 				}
 	        if(!config.contains("general.tnt_fire")) {
 				config.set("general.tnt_fire", false);
 				}
 	        if(!config.contains("general.tnt_richochet")) {
 				config.set("general.tnt_richochet", false);
 				}
 	        if(!config.contains("general.death.keepitems.enable")) {
 				config.set("general.death.keepitems.enable", true);
 				}
 	        if(!config.contains("general.death.keepitems.usepermission")) {
 				config.set("general.death.keepitems.usepermission", true);
 				}
 	        if(!config.contains("general.death.keepitems.permission")) {
 				config.set("general.death.keepitems.permission", "useful.death.keepitems");
 				}
 	        if(!config.contains("general.prefixes.# description")) {
 				config.set("general.prefixes.# description", "If enabled this will use the prefix system in uchat from the ranks system.");
 				}
 	        if(!config.contains("general.prefixes.enable")) {
 				config.set("general.prefixes.enable", true);
 				}
 	        if(!config.contains("general.wirelessRedstone.enable")) {
 				config.set("general.wirelessRedstone.enable", true);
 				}
 	        if(!config.contains("general.broadcast.message")) {
 				config.set("general.broadcast.message", "'useful' default broadcast message! (Change or disable in the 'useful' configuration file!)");
 				}
 	        if(config.contains("general.disabled_commands(separated_by_commas_)")) {
 	        	//rename to blocked_commands
 				config.set("general.blocked_commands(separated_by_commas_)", config.get("general.disabled_commands(separated_by_commas_)"));
 			    config.set("general.disabled_commands(separated_by_commas_)", null);
 	        }
 	        if(!config.contains("general.blocked_commands(separated_by_commas_)")) {
 				config.set("general.blocked_commands(separated_by_commas_)", "nuke,antioch");
 				}
 	        if(!config.contains("general.default_blocked_item_ids(separated_by_commas_)")) {
 				config.set("general.default_blocked_item_ids(separated_by_commas_)", "46,51,10,11,259");
 				}
 	        if(!config.contains("colorScheme.success")) {
 				config.set("colorScheme.success", "&a");
 				}
 	        if(!config.contains("colorScheme.error")) {
 				config.set("colorScheme.error", "&c");
 				}
 	        if(!config.contains("colorScheme.info")) {
 				config.set("colorScheme.info", "&e");
 				}
 	        if(!config.contains("colorScheme.title")) {
 				config.set("colorScheme.title", "&9");
 				}
 	        if(!config.contains("colorScheme.tp")) {
 				config.set("colorScheme.tp", "&5");
 				}
 	        if(!config.contains("uperms.enable")) {
 				config.set("uperms.enable", false);
 				}
 	        if(!config.contains("customCrafting.doubleSlab.# description")){
 	        	config.set("customCrafting.doubleSlab.# description", "This allows for the crafting of a double slab.");
 	        }
 	        if(!config.contains("customCrafting.doubleSlab.enable")){
 	        	config.set("customCrafting.doubleSlab.enable", true);
 	        }
 	        if(!config.contains("customCrafting.waterSource.# description")){
 	        	config.set("customCrafting.waterSource.# description", "This allows for the crafting of a water source block (can be used to craft ice).");
 	        }
 	        if(!config.contains("customCrafting.waterSource.enable")){
 	        	config.set("customCrafting.waterSource.enable", true);
 	        }
 	        if(!config.contains("customCrafting.lavaSource.# description")){
 	        	config.set("customCrafting.lavaSource.# description", "This allows for the crafting of a lava source block.");
 	        }
 	        if(!config.contains("customCrafting.lavaSource.enable")){
 	        	config.set("customCrafting.lavaSource.enable", true);
 	        }
 	        if(!config.contains("customCrafting.mobSpawner.# description")){
 	        	config.set("customCrafting.mobSpawner.# description", "This allows for the crafting of a mob spawner.");
 	        }
 	        if(!config.contains("customCrafting.mobSpawner.enable")){
 	        	config.set("customCrafting.mobSpawner.enable", true);
 	        }
 	        if(!config.contains("customCrafting.cmdBlock.# description")){
 	        	config.set("customCrafting.cmdBlock.# description", "This allows for the crafting of a command block.");
 	        }
 	        if(!config.contains("customCrafting.cmdBlock.enable")){
 	        	config.set("customCrafting.cmdBlock.enable", true);
 	        }
 	        if(!config.contains("customCrafting.snowBlock.# description")){
 	        	config.set("customCrafting.snowBlock.# description", "This allows for the crafting of a snow block.");
 	        }
 	        if(!config.contains("customCrafting.snowBlock.enable")){
 	        	config.set("customCrafting.snowBlock.enable", true);
 	        }
 	        if(!config.contains("customCrafting.iceBlock.# description")){
 	        	config.set("customCrafting.iceBlock.# description", "This allows for the crafting of an ice block.");
 	        }
 	        if(!config.contains("customCrafting.iceBlock.enable")){
 	        	config.set("customCrafting.iceBlock.enable", true);
 	        }
 	        if(!config.contains("customCrafting.fire.# description")){
 	        	config.set("customCrafting.fire.# description", "This allows for the crafting fire!!.");
 	        }
 	        if(!config.contains("customCrafting.fire.enable")){
 	        	config.set("customCrafting.fire.enable", true);
 	        }
 	        if(!config.contains("uConnect.# description")){
 	        	config.set("uConnect.# description", "This allows for the uConnect inta-server system.");
 	        }
 	        if(!config.contains("uConnect.enable")){
 	        	config.set("uConnect.enable", true);
 	        }
 			saveConfig();
 			pluginFolder = this.getDataFolder().getAbsolutePath();
 			(new File(pluginFolder)).mkdirs();
 			(new File(pluginFolder + File.separator + "warns")).mkdirs();
 			(new File(pluginFolder + File.separator + "player-data")).mkdirs();
 			String path = getDataFolder() + File.separator + "jailed.bin";
 			File file = new File(path);
 			if(file.exists()){ // check if file exists before loading to avoid errors!
 				jailed  = load(path);
 			}
 			String path355 = getDataFolder() + File.separator + "uhost_settings.bin";
 			File file355 = new File(path355);
 			 
 			if(file355.exists()){ // check if file exists before loading to avoid errors!
 				uhost_settings  = loadHashMapBoolean(path355);
 			}
 			String pathUp = getDataFolder() + File.separator + "updateManager.bin";
 			File fileUp = new File(pathUp);
 			 
 			if(fileUp.exists()){ // check if file exists before loading to avoid errors!
 				updateManager  = loadHashMapBoolean(pathUp);
 			}
 			String path4 = getDataFolder() + File.separator + "mail.bin";
 			File file4 = new File(path4);
 			 
 			if(file4.exists()){ // check if file exists before loading to avoid errors!
 				mail  = load(path4);
 			}
 			
 			this.warns = new ListStore(new File(pluginFolder + File.separator + "warns.log"));
 			this.warns.load();
 			this.rules = new ListStore(new File(pluginFolder + File.separator + "rules.txt"));
 			this.rules.load();
 			this.changelog = new ListStore(new File(pluginFolder + File.separator + "changelog.txt"));
 			this.changelog.load();
 			this.auths = new ListStore(new File(pluginFolder + File.separator + "auths.txt"));
 			this.auths.load();
 			this.info = new ListStore(new File(pluginFolder + File.separator + "info.txt"));
 			this.info.load();
 			this.commandViewers = new ListStore(new File(pluginFolder + File.separator + "commandViewers.db"));
 			this.commandViewers.load();
 			this.heros = new ListStore(new File(pluginFolder + File.separator + "heros.dat"));
 			this.heros.load();
 			 try {
 					SecretKey key = KeyGenerator.getInstance("DES").generateKey();
 					   encrypter = new Encrpyter(key, pluginAuth);
 				} catch (Exception e2) {
 					e2.printStackTrace();
 				}
 			 Boolean serverOnline = getServer().getOnlineMode();
 			 if(serverOnline){
 			if(config.getBoolean("uConnect.enable")){
 			plugin.colLogger.info("Loading uConnect...");
 			uconnect = new UConnect(pluginAuth);
 			// url for ads: http://dl.dropbox.com/u/147363358/ads.txt
 			URL ads = new URL("http://dl.dropbox.com/u/147363358/ads.txt");
 				 InputStream adsStream = ads.openStream();
 				 File dir = new File(pluginFolder+File.separator+"uConnect"+File.separator+"Data cache"+File.separator+"Ads cache");
 				 dir.mkdirs();
 				 File toSave = new File(dir + File.separator + "ads.uc");
 				 this.uconnectAds = new ListStore(toSave);
 				 uconnectAds.loadFromInputStream(adsStream);
 				 uconnectAds.save();
 				 toSave.delete();
 			plugin.colLogger.info("uConnect loaded!");
 			}
 			 }
 			 else{
 				 config.set("uConnect.enable", false);
				 plugin.colLogger.info("UConnect not allowed on secured servers with online-mode set to false. To use uconnect please enable online-mode in your server.properties file!");
 			     saveConfig();
 			 }
 			this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
 	            @Override
 	            public void run() {
 	                long currentTime = System.currentTimeMillis();
 	                
 	                for (Player p : getServer().getOnlinePlayers()) {
 	                    if (!playerIsJailed(p)) continue;
 
 	                   
 	                    double sentence = getJailTime(p);
 	                    
 
 	                    if (sentence <= currentTime) {
 	                        try {
 	                        	String name = p.getName();
 	                            jailed.remove(name);
 	                            saveHashMap(jailed, getDataFolder() + File.separator + "jailed.bin");
 	                            String path = getDataFolder() + File.separator + "jailed.bin";
 	                			File file = new File(path);
 	                			if(file.exists()){ // check if file exists before loading to avoid errors!
 	                				jailed  = load(path);
 	                			}
 	                			Location spawn = p.getWorld().getSpawnLocation();
 	                			p.teleport(spawn);
 	                            getLogger().info(name + " has been unjailed.");
 	                            p.sendMessage(ChatColor.GREEN + "You have been unjailed.");
 	                        } catch (Exception ex) {
 	                            // Should never happen
 	                            ex.printStackTrace();
 	                        }
 	                       
 	                    }
 	                }
 	                
 	            }
 
 				
 	            
 	        }, 200, 200);
 			double mins = config.getDouble("general.broadcast.delay(minutes)");
     		double delaySecs = mins * 60;
     		double delayMillis = delaySecs * 20;
     		long delay = Math.round(delayMillis);
 			broadcaster = this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
 
 	            @Override
 	            public void run() {
 	            	if (config.getBoolean("general.broadcast.enable")){
 	            		String broadcast = config.getString("general.broadcast.message");
 	            		Bukkit.broadcastMessage(ChatColor.BLUE + "[" + ChatColor.GREEN + ChatColor.BOLD + "Broadcast:" + ChatColor.RESET + ChatColor.BLUE + "] " + ChatColor.YELLOW + broadcast);
 	            	}
 	            }
 
 				
 	            
 	        }, delay, delay);
 			backup = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
 
 	            @Override
 	            public void run() {
 	            	if (config.getBoolean("general.backup.auto")){
 	            		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 	            		   //get current date time with Date()
 	            		java.util.Date dateTime = new java.util.Date();
 	            		String date = dateFormat.format(dateTime);
 	            		getLogger().info(ChatColor.GREEN + "Starting backup procedure...");
 	            		List<World> worlds = plugin.getServer().getWorlds();
 	            		Object[] theWorlds = worlds.toArray();
 	            		String path = new File(".").getAbsolutePath();
 	            		for(int i=0;i<theWorlds.length; i++){
 	            			World w = (World) theWorlds[i];
 	            			try {
 								w.save();
 							} catch (Exception e1) {
 							}
 	            			String wNam = w.getName();
 	            		File srcFolder = new File(path + File.separator + wNam);
 	            		File destFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "World Backups" + File.separator + date + File.separator + wNam);
 	            	    destFolder.mkdirs();
 	            		//make sure source exists
 	            		if(!srcFolder.exists()){
 	            	       getLogger().info(ChatColor.RED + "Failed to find world " + wNam);
 
 	            	    }else{
 
 	            	       try{
 	            	    	Copier.copyFolder(srcFolder,destFolder);
 	            	       }catch(IOException e){
 	            	    	getLogger().info(ChatColor.RED + "Error copying world " + wNam);
 	            	       }
 	            	    }
 
 	            		}
 	            		getLogger().info(ChatColor.GREEN + "Backup procedure complete!");
 	            	}
 	            }
 
 				
 	            
 	        }, 60000, 60000);
 			if(ucReloadMsgRun == false){
 				ucReloadMsgRun= true;
 			reloadUcMsg = this.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){
 
 				@Override
 				public void run() {
 					if(useful.config.getBoolean("uConnect.enable")){
 					plugin.uconnect.load(new UConnectDataRequest("reloadMain", null, null, pluginAuth));
 					}
 				}}, 18000, 18000);
 			}
 			UcMsgNotify = this.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){
 
 				@Override
 				public void run() {
 					if(useful.config.getBoolean("uConnect.enable")){
 					YamlConfiguration data = plugin.uconnect.main;
 					Player[] online = getServer().getOnlinePlayers();
 					for(int i=0;i<online.length;i++){
 						Player p = online[i];
 						String name = p.getName();
 						if(data.contains("messaging."+name)){
 							List<String> msgs = data.getStringList("messaging."+name);
 							if(msgs.size() > 0){
 								p.sendMessage(ChatColor.BLUE + "[uConnect]" + plugin.colors.getInfo() + "You have " + plugin.colors.getSuccess() + msgs.size() + plugin.colors.getInfo() + " unread messages!");
 							}
 						}
 					}
 					}
 				}}, 5000, 5000);
 			if(idleRunning == false){
 				idleRunning = true;
 			idle = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
 
 	            @Override
 	            public void run() {
 	            	getLogger().info("Running idle system check, updater and saving changed data.");
 	            	boolean autoupdate = config.getBoolean("version.autoupdate.enable");
 	            	if(autoupdate == false){
 	            		saveYamls();
 						plugin.getServer().getPluginManager().getPlugin("useful").reloadConfig();
 						getLogger().info("Idle system check complete!");
 	            		return;
 	            	}
 	            	if(autoupdate){
 	           		    //getLogger().info("Checking version");
 	           		 getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW+ "Checking version...");
 	           			URL url = null;
 	           			InputStream in = null;
 	        			try {
 	        				url = new URL("https://dl.dropbox.com/u/50672767/usefulplugin/version.txt");
 	        			} catch (MalformedURLException e1) {
 	        			}
 	           			try {
 	        				in = new BufferedInputStream(url.openStream());
 	        			} catch (Exception e1) {
 	        				
 	        			}
 	           			try{
 	           				BufferedReader reader = new BufferedReader(new InputStreamReader(in));   				
 	           				String line;
 	           				Boolean open = true;
 	           				while(open && reader.ready() && (line = reader.readLine()) != null){
 	           					double latest = 0;
 	           					double current = config.getDouble("version.current");
 	           					try {
 	           						latest = Double.parseDouble(line);
 	           					} catch (Exception e) {
 	           						//getLogger().info("Error checking version");
 	           						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.RED + "Error checking version...");
 	           						latest = current;
 	           					}
 	           					if(latest <= current){
 	           						//getLogger().info("Current version: " + current + " latest version: " + latest);
 	           						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Current version: " + current + " latest version: " + latest);
 	           						//getLogger().info("Plugin up to date!");
 	           						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.GREEN + "Plugin up to date!");
 	           						open = false;
 	           					}
 	           					else{
 	           						//getLogger().info("Current version: " + current + " latest version: " + latest);
 	           						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Current version: " + current + " latest version: " + latest);
 	           						//getLogger().info("Plugin outdated!");
 	           						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.RED + "Plugin outdated!");
 	           						//getLogger().info("Attempting to update plugin...");
 	           						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Attempting to update the plugin...");
 	           						try{
 	           							BufferedReader buff;
 	           					        InputStreamReader inStream;
 	           					        String htmlCode = null;
 	           					        String latestFilePage = "";
 	           					        try{
 	           					            URL bukkiturl = new URL("http://dev.bukkit.org/server-mods/useful");
 	           					            URLConnection urlConnection = (URLConnection)bukkiturl.openConnection();
 	           					            inStream = new InputStreamReader(urlConnection.getInputStream());
 	           					            buff = new BufferedReader(inStream);
 	           					            while(true){
 	           					                if (buff.readLine()!=null){
 	           					                    htmlCode += buff.readLine() + "\n";
 	           					                }else{
 	           					                    break;
 	           					                }
 	           					            }
 	           					            htmlCode = htmlCode.replaceAll(" ", "");
 	           					         htmlCode = htmlCode.replaceAll("	", "");
 	           					      htmlCode = htmlCode.replaceAll("\n", "").replace("\r", "");
 	           					      //<liclass="user-actionuser-action-download"><ahref="/server-mods/useful/files/17-useful-v2-1/">Download</a>
 	           					            int startFrom = htmlCode.indexOf("<liclass=\"user-actionuser-action-download\"><ahref=\"");
 	           					            int endFrom = htmlCode.indexOf("\">Download</a>", startFrom);
 	           					            latestFilePage = htmlCode.substring(startFrom, endFrom); //This gets bukkit's link to the latest APPROVED file for security
 	           					            latestFilePage = latestFilePage.replaceFirst("<liclass=\"user-actionuser-action-download\"><ahref=\"", "");
 	           					            latestFilePage = "http://dev.bukkit.org" + latestFilePage; //If not a bukkit download (bukkit hacked or summin??) then will come up with error
 	           					            plugin.colLogger.info("For information on this version visit:"+latestFilePage); //Page for the latest version...
 	           					        }catch(Exception e){
 	           					        	e.printStackTrace();
 	           					        	plugin.colLogger.info("Failed to retrieve the url. Please download the latest version manually from http://dev.bukkit.org/server-mods/useful");
 	           					        };  
 	           					        String FilePath = "";
 	           					     try{
 	        					            URL bukkiturl = new URL(latestFilePage); //Open latest version's page...
 	        					            URLConnection urlConnection = (URLConnection)bukkiturl.openConnection();
 	        					            inStream = new InputStreamReader(urlConnection.getInputStream());
 	        					            buff = new BufferedReader(inStream);
 	        					            while(true){
 	        					                if (buff.readLine()!=null){
 	        					                    htmlCode += buff.readLine() + "\n";
 	        					                }else{
 	        					                    break;
 	        					                }
 	        					            }
 	        					            htmlCode = htmlCode.replaceAll(" ", "");
 	        					         htmlCode = htmlCode.replaceAll("	", "");
 	        					      htmlCode = htmlCode.replaceAll("\n", "").replace("\r", "");
 	        					      //<dd><spanclass="standard-date"title="Jan29,2013at17:04UTC"data-epoch="1359479056"data-shortdate="true">Jan29,2013</span></dd><dt>Gameversion</dt><dd><ahref="http://dev.bukkit.org/media/files/668/500/useful.jar">useful.jar</a>
 	        					            int startFrom = htmlCode.indexOf("</span></dd><dt>Gameversion</dt><dd><ahref=\""); //Find the .jar's download url
 	        					            int endFrom = htmlCode.indexOf("\">", startFrom);
 	        					            FilePath = htmlCode.substring(startFrom, endFrom);
 	        					            FilePath = FilePath.replaceFirst("</span></dd><dt>Gameversion</dt><dd><ahref=\"", "");
 	        					        }catch(Exception e){
 	        					        	e.printStackTrace();
 	        					        	plugin.colLogger.info("Failed to retrieve the file. Please download the latest version manually from http://dev.bukkit.org/server-mods/useful");
 	        					        };  
 	           							URL update = new URL(FilePath); //Update the plugin!
 	           							getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW+ "Downloading update from " + FilePath);
 	           							 InputStream inUp = new BufferedInputStream(update.openStream());
 	           							 ByteArrayOutputStream outUp = new ByteArrayOutputStream();
 	           							 byte[] buf = new byte[1024];
 	           							 int n = 0;
 	           							 while (-1!=(n=inUp.read(buf)))
 	           							 {
 	           							    outUp.write(buf, 0, n);
 	           							 }
 	           							 outUp.close();
 	           							 inUp.close();
 	           							 byte[] responseUp = outUp.toByteArray();
 	           							 (new File(getDataFolder().getParent() + File.separator + getServer().getUpdateFolder())).mkdirs();
 	           							 //FileOutputStream fos = new FileOutputStream(pluginFolder + File.separator + "Plugin updates" + File.separator +  "useful.jar");
 	           							 //FileOutputStream fos = new FileOutputStream(new File(getServer().getUpdateFolder() + File.separator + "useful.jar"));
 	           							 FileOutputStream fos = new FileOutputStream(new File(getDataFolder().getParent() + File.separator + getServer().getUpdateFolder() + File.separator + "useful.jar"));
 	           							     fos.write(responseUp);
 	           							     fos.close();
 	           							     updateManager.clear();
 	           							     saveHashMapBoolean(updateManager, getDataFolder() + File.separator + "updateManager.bin");
 	           							     //getLogger().info("Successfully updated to version " + latest + " attempting to reload server...");
 	           							  getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.GREEN + "Successfully updated to version " + latest + " attempting to reload server...");
 	           							     getServer().reload();
 	       						}catch(Exception e){
 	       							getLogger().info("Failed to update the plugin!");
 	       							e.printStackTrace();
 	       						}
 	           					
 	       					
 	       				}
 	       			
 	       				reader.close();
 	       				in.close();
 	       				}
 	       			}catch (Exception e){
 	       			     e.printStackTrace();
 	       			}
 	       			
 	       			/*
 	       			ByteArrayOutputStream out = new ByteArrayOutputStream();
 	       			byte[] buf = new byte[1024];
 	       			int n = 0;
 	       			while (-1!=(n=in.read(buf)))
 	       			{
 	       			   out.write(buf, 0, n);
 	       			}
 	       			out.close();
 	       			in.close();
 	       			byte[] response = out.toByteArray();
 	       			
 	       			FileOutputStream fos = new FileOutputStream(this.getDataFolder() + File.separator + "test.txt");
 	       			    fos.write(response);
 	       			    fos.close();
 	       			    */
 	       			}
 	            
 	       			saveYamls();
 					plugin.getServer().getPluginManager().getPlugin("useful").reloadConfig();
 					getLogger().info("Idle system check complete!");
 	            }
 			
 				
 			    
 	        }, 60000, 60000);
 			}
 		}catch(Exception e1){
 		e1.printStackTrace();
 		}
 		
 		getServer().getPluginManager().registerEvents(new UsefulListener(this, pluginAuth), this);
 		Set<String> ver = warps.keySet();
         for (String v : ver) {
         	if (v.toLowerCase() != v){
         		getLogger().info("Found an old warp, attempting to convert to new format...");
     			SerializableLocation loc = warps.get(v);
     			warps.remove(v);
     			getLogger().info("Old warp successfully removed...");
     			warps.put(v.toLowerCase(), loc);
     			getLogger().info("Warp converted!");
         	}
         	
 				
         }
         PluginDescriptionFile pldesc = plugin.getDescription();
         Map<String, Map<String, Object>> commands = pldesc.getCommands();
         Set<String> keys = commands.keySet();
         for(String k : keys){
         	try {
 				getCommand(k).setExecutor(new UsefulCommandExecutor(this, pluginAuth));
 			} catch (Exception e) {
 				getLogger().log(Level.SEVERE, "Error registering command " + k.toString());
 				e.printStackTrace();
 			}
         }
         checkRegister("general.burn.enable", "burn");
         checkRegister("general.spawnmob.enable", "spawnmob");
         checkRegister("general.mobset.enable", "mobset");
         checkRegister("general.mail.enable", "mail");
         checkRegister("general.smite.enable", "smite");
 	    checkRegister("general.spawn.enable", "spawn");
 	    checkRegister("general.setspawn.enable", "setspawn");
 	    checkRegister("general.timeget.enable", "timeget");
 		checkRegister("general.warps.enable", "warps");
 		checkRegister("general.warps.enable", "setwarp");
 		checkRegister("general.warps.enable", "delwarp");
 		checkRegister("general.warps.enable", "warp");
 		checkRegister("general.time.enable", "time");
 		checkRegister("general.gamemode.enable", "gm");
 		checkRegister("general.jail.enable", "jail");
 		checkRegister("general.jail.enable", "jails");
 		checkRegister("general.jail.enable", "jailed");
 		checkRegister("general.jail.enable", "unjail");
 		checkRegister("general.jail.enable", "jailtime");
 		checkRegister("general.jail.enable", "setjail");
 		checkRegister("general.jail.enable", "deljail");
 		checkRegister("general.killmobs.enable", "killmobs");
 		checkRegister("general.tp.enable", "tp");
 		checkRegister("general.tphere.enable", "tphere");
 		checkRegister("general.hat.enable", "hat");
 		checkRegister("general.hero.enable", "hero");
 		checkRegister("general.count.enable", "count");
 		checkRegister("general.eat.enable", "eat");
 		checkRegister("general.levelup.enable", "levelup");
 		checkRegister("general.setlevel.enable", "setlevel");
         checkRegister("general.getid.enable", "getid");
         checkRegister("general.getid.enable", "look");
         checkRegister("general.message.enable", "message");
         checkRegister("general.magicmessage.enable", "magicmessage");
         checkRegister("general.listplayers.enable", "listplayers");
         checkRegister("general.warning.enable", "warn");
         checkRegister("general.warning.enable", "warnslog");
         checkRegister("general.warning.enable", "delete-warns");
         checkRegister("general.warning.enable", "view-warns");
         checkRegister("general.ban.enable", "ban");
         checkRegister("general.ban.enable", "unban");
         checkRegister("general.kick.enable", "kick");
         checkRegister("general.ci.enable", "ci");
         checkRegister("general.invsee.enable", "invsee");
         checkRegister("general.creativecommand.enable", "creative");
         checkRegister("general.survivalcommand.enable", "survival");
         checkRegister("general.adventurecommand.enable", "adventure");
         checkRegister("general.uhost.enable", "uhost");
         checkRegister("general.tpa.enable", "tpa");
         checkRegister("general.tpa.enable", "tpaccept");
         checkRegister("general.tpa.enable", "tpahere");
         checkRegister("general.feast.enable", "feast");
         checkRegister("general.murder.enable", "murder");
         checkRegister("general.genocide.enable", "genocide");
         checkRegister("general.rules.enable", "rules");
         checkRegister("general.info.enable", "information");
         checkRegister("general.mobtypes.enable", "mobtypes");
         checkRegister("general.world.enable", "world");
         checkRegister("general.worlds.enable", "worlds");
         checkRegister("general.backup.enable", "backup");
         checkRegister("general.shelter.enable", "shelter");
         checkRegister("general.authentication.enable", "needauth");
         checkRegister("general.authentication.enable", "notneedauth");
         checkRegister("general.authentication.enable", "login");
         checkRegister("general.firework.enable", "firework");
         checkRegister("general.potion.enable", "potion");
         checkRegister("general.worldgm.enable", "worldgm");
         checkRegister("general.compass.enable", "compass");
         checkRegister("general.back.enable", "back");
         checkRegister("general.enchant.enable", "enchant");
         checkRegister("general.canfly.enable", "canfly");
         checkRegister("general.rename.enable", "rename");
         checkRegister("general.head.enable", "head");
         checkRegister("general.craft.enable", "craft");
         checkRegister("uConnect.enable", "uconnect");
         if(new File(getDataFolder() + File.separator + "warps.bin").exists() || new File(getDataFolder() + File.separator + "warpowners.bin").exists() && new File(getDataFolder() + File.separator + "warps.bin") != null && new File(getDataFolder() + File.separator + "warps.bin").length() > 0){
  	       //getLogger().info("Old warp data found, converting now...");
  	       getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Found old warp data, converting now...");
  		   warpsConverter();
  	   }
  	   if(new File(getDataFolder() + File.separator + "jails.bin").exists() && new File(getDataFolder() + File.separator + "jails.bin") != null && new File(getDataFolder() + File.separator + "jails.bin").length() > 0){
  		   getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Found old jail data, converting now...");
  		   jailsConverter();
  	   }
  	   
  	   Object[] authVal = auths.values.toArray();
  	   for(int i=0;i<authVal.length;i++){
  		   String val = (String) authVal[i];
  		   String[] parts = val.split(" ");
  		   String pname = parts[0];
  		   authed.put(pname, false);
  	   }
  	   colors = new Colors(config.getString("colorScheme.success"), config.getString("colorScheme.error"), config.getString("colorScheme.info"), config.getString("colorScheme.title"), config.getString("colorScheme.title"));
  	   if(config.getBoolean("uperms.enable")){
  		   getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Setting up uhost's perm system...");
  		   permManager=new uPerms(this);
  		   Player[] player = getServer().getOnlinePlayers();
  		   for(int i=0;i<player.length;i++){
  			   permManager.refreshPerms(player[i]);
  		   }
  	   }
        String discmds = config.getString("general.blocked_commands(separated_by_commas_)");
         	   String[] cmds = discmds.split(",");
         	   for(int x=0 ; x<cmds.length ; x++) {
 					disableCommand(cmds[x]);
 					}
         	   if(uhost_settings.containsKey("performance") == false){
         		   uhost_settings.put("performance", false);
         	   }
         	   Performance.performanceMode(uhost_settings.get("performance"));
         	   if(uhost_settings.get("performance")){
         		   //getLogger().info("Useful performance mode is enabled!");
         		   getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW+ "Performance mode is enabled...");
         	   }
         	   else {
         		   //getLogger().info("Useful performance mode is disabled!");  
         		   getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW+ "Performance mode is disabled...");
         	   }
         	   boolean autoupdate = config.getBoolean("version.autoupdate.enable");
    			if(autoupdate){
    		    //getLogger().info("Checking version");
    		 getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW+ "Checking version...");
    			URL url = null;
    			InputStream in = null;
 			try {
 				url = new URL("https://dl.dropbox.com/u/50672767/usefulplugin/version.txt");
 			} catch (MalformedURLException e1) {
 			}
    			try {
 				in = new BufferedInputStream(url.openStream());
 			} catch (Exception e1) {
 				
 			}
    			try{
    				BufferedReader reader = new BufferedReader(new InputStreamReader(in));   				
    				String line;
    				while((line = reader.readLine()) != null){
    					double latest = 0;
    					double current = config.getDouble("version.current");
    					try {
    						latest = Double.parseDouble(line);
    					} catch (Exception e) {
    						//getLogger().info("Error checking version");
    						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.RED + "Error checking version...");
    						latest = current;
    					}
    					if(latest <= current){
    						//getLogger().info("Current version: " + current + " latest version: " + latest);
    						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Current version: " + current + " latest version: " + latest);
    						//getLogger().info("Plugin up to date!");
    						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.GREEN + "Plugin up to date!");
    					}
    					else{
    						//getLogger().info("Current version: " + current + " latest version: " + latest);
    						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Current version: " + current + " latest version: " + latest);
    						//getLogger().info("Plugin outdated!");
    						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.RED + "Plugin outdated!");
    						//getLogger().info("Attempting to update plugin...");
    						getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Attempting to update the plugin...");
    						try{
    							BufferedReader buff;
    					        InputStreamReader inStream;
    					        String htmlCode = null;
    					        String latestFilePage = "";
    					        try{
    					            URL bukkiturl = new URL("http://dev.bukkit.org/server-mods/useful");
    					            HttpURLConnection urlConnection = (HttpURLConnection)bukkiturl.openConnection();
    					            urlConnection.setRequestMethod("GET");
    					            urlConnection.setUseCaches(false);
    					            urlConnection.setReadTimeout(25*1000);
    					            urlConnection.setConnectTimeout(25*1000);
    					         urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.60 Safari/537.17");
    					            urlConnection.setInstanceFollowRedirects(true);
    					            urlConnection.connect();
    					            inStream = new InputStreamReader(urlConnection.getInputStream());
    					            buff = new BufferedReader(inStream);
    					         StringBuilder builder = new StringBuilder();
    				            int byteRead;
    				            while ((byteRead = buff.read()) != -1)
    				                builder.append((char) byteRead);
    				            
    				            buff.close();
    				            htmlCode = builder.toString();
    					            htmlCode = htmlCode.replaceAll(" ", "");
    					         htmlCode = htmlCode.replaceAll("	", "");
    					      htmlCode = htmlCode.replaceAll("\n", "").replace("\r", "");
    					      if(htmlCode.length() < 5){
    					    	  plugin.colLogger.info("Failed to connect to bukkit.org - expect errors below:");
    					      }
    					      boolean debug = false;
    					      if(debug){
    					      ListStore debugFile = new ListStore(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "updateDebug.txt"));
    					      debugFile.add(htmlCode);
    					      debugFile.save();
    					      }
    					      //<liclass="user-actionuser-action-download"><ahref="/server-mods/useful/files/21-useful-v2-4/">Download</a></li>
    					      String startPoint = GetStringFromUrl.get("https://dl.dropbox.com/u/50672767/usefulplugin/updaterStartpoint.txt");      
    					      String endPoint = GetStringFromUrl.get("https://dl.dropbox.com/u/50672767/usefulplugin/updaterEndpoint.txt");
    					      if(startPoint == null || endPoint == null){
    					    	  plugin.colLogger.info("Error fetching points!");
    					    	  break;
    					      }
    					      //int startFrom = htmlCode.indexOf("<liclass=\"user-actionuser-action-download\"><ahref=\"");
    					      //int endFrom = htmlCode.indexOf("\">Download</a>", startFrom);
    					      int startFrom = htmlCode.indexOf(startPoint);
    					            int endFrom = htmlCode.indexOf(endPoint, startFrom);
    					            plugin.colLogger.info("Data 1: " + startFrom + " Data 2: " + endFrom);
    					            latestFilePage = htmlCode.substring(startFrom, endFrom);
    					            latestFilePage = latestFilePage.replaceFirst(startPoint, "");
    					            latestFilePage = "http://dev.bukkit.org" + latestFilePage;
    					            plugin.colLogger.info("For information on this version visit:"+latestFilePage);
    					        }catch(Exception e){
    					        	e.printStackTrace();
    					        	plugin.colLogger.info("Failed to retrieve the url. Please download the latest version manually from http://dev.bukkit.org/server-mods/useful");
    					        };  
    					        String FilePath = "";
    					     try{
 					            URL bukkiturl = new URL(latestFilePage);
 					            URLConnection urlConnection = (URLConnection)bukkiturl.openConnection();
 					            inStream = new InputStreamReader(urlConnection.getInputStream());
 					            buff = new BufferedReader(inStream);
 					            StringBuilder builder = new StringBuilder();
 	   				            int byteRead;
 	   				            while ((byteRead = buff.read()) != -1)
 	   				                builder.append((char) byteRead);
 	   				            
 	   				            buff.close();
 	   				            htmlCode = builder.toString();
 	   				            ListStore test = new ListStore(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "test.txt"));
 					            test.add(htmlCode);
 					            test.save();
 	   				            htmlCode = htmlCode.replaceAll(" ", "");
 					         htmlCode = htmlCode.replaceAll("	", "");
 					      htmlCode = htmlCode.replaceAll("\n", "").replace("\r", "");
 					      //<dd><spanclass="standard-date"title="Jan29,2013at17:04UTC"data-epoch="1359479056"data-shortdate="true">Jan29,2013</span></dd><dt>Gameversion</dt><dd><ahref="http://dev.bukkit.org/media/files/668/500/useful.jar">useful.jar</a>
 					      //<li class="user-action user-action-download"><span><a href="http://dev.bukkit.org/media/files/674/587/useful.jar">Download</a></span></li>      
 					      //<liclass="user-actionuser-action-download"><span><ahref="http://dev.bukkit.org/media/files/674/587/useful.jar">Download</a></span></li>
 					      int startFrom = htmlCode.indexOf("<liclass=\"user-actionuser-action-download\"><span><ahref=\"");
 					            int endFrom = htmlCode.indexOf("\">Download</a>", startFrom);
 					            FilePath = htmlCode.substring(startFrom, endFrom);
 					            FilePath = FilePath.replaceFirst("<liclass=\"user-actionuser-action-download\"><span><ahref=\"", "");
 					            plugin.colLogger.info("Latest version url: " + FilePath);
 					        }catch(Exception e){
 					        	e.printStackTrace();
 					        	plugin.colLogger.info("Failed to retrieve the file. Please download the latest version manually from http://dev.bukkit.org/server-mods/useful");
 					        };  
    							URL update = new URL(FilePath);
    							getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW+ "Downloading update from " + FilePath);
    							 InputStream inUp = new BufferedInputStream(update.openStream());
    							 ByteArrayOutputStream outUp = new ByteArrayOutputStream();
    							 byte[] buf = new byte[1024];
    							 int n = 0;
    							 while (-1!=(n=inUp.read(buf)))
    							 {
    							    outUp.write(buf, 0, n);
    							 }
    							 outUp.close();
    							 inUp.close();
    							 byte[] responseUp = outUp.toByteArray();
    							 (new File(getDataFolder().getParent() + File.separator + getServer().getUpdateFolder())).mkdirs();
    							 //FileOutputStream fos = new FileOutputStream(pluginFolder + File.separator + "Plugin updates" + File.separator +  "useful.jar");
    							 //FileOutputStream fos = new FileOutputStream(new File(getServer().getUpdateFolder() + File.separator + "useful.jar"));
    							 FileOutputStream fos = new FileOutputStream(new File(getDataFolder().getParent() + File.separator + getServer().getUpdateFolder() + File.separator + "useful.jar"));
    							     fos.write(responseUp);
    							     fos.close();
    							     updateManager.clear();
    							     saveHashMapBoolean(updateManager, getDataFolder() + File.separator + "updateManager.bin");
    							     //getLogger().info("Successfully updated to version " + latest + " attempting to reload server...");
    							  getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.GREEN + "Successfully updated to version " + latest + " attempting to reload server...");
    							     getServer().reload();
    						}catch(Exception e){
    							//getLogger().info("Failed to update the plugin!");
    							getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.RED + "useful plugin failed to update!");
    							e.printStackTrace();
    						}
    					}
    				}
    				if(reader != null){
    				reader.close();
    				}
    				in.close();
    			}catch (Exception e){
    				e.printStackTrace();
    			}
    			/*
    			ByteArrayOutputStream out = new ByteArrayOutputStream();
    			byte[] buf = new byte[1024];
    			int n = 0;
    			while (-1!=(n=in.read(buf)))
    			{
    			   out.write(buf, 0, n);
    			}
    			out.close();
    			in.close();
    			byte[] response = out.toByteArray();
    			
    			FileOutputStream fos = new FileOutputStream(this.getDataFolder() + File.separator + "test.txt");
    			    fos.write(response);
    			    fos.close();
    			    */
    			}
         	   //getLogger().info("Running update manager for info...");
         	   getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Running update manager for notfications...");
         	   OfflinePlayer[] players = getServer().getOfflinePlayers();
         	   for(int i = 0; i < players.length; i++){
         		   boolean info = players[i].isOp();
         		   if(info){
         			   if(!updateManager.containsKey(players[i].getName())){
         			   updateManager.put(players[i].getName(), true);
         			   saveHashMapBoolean(updateManager, getDataFolder() + File.separator + "updateManager.bin");
         			   }
         		   }
         	   }
         	   Plugin[] pluginsOn = getServer().getPluginManager().getPlugins();
         	   for(int i=0;i<pluginsOn.length;i++){
         		   if(pluginsOn[i].getName().equalsIgnoreCase("uCars")){
         			   plugin.colLogger.info("Hooked into UCars...");
         			   config.set("general.cars.enable", false);
         			   saveConfig();
         			   plugin.colLogger.info("Successfully given UCars car control!");
         		   }
         	   }
         	   System.gc();
         	   getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.YELLOW + "Registering recipes...");
         	   new CustomRecipes().Register();
         	   getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "[useful] " + ChatColor.RESET + "" + ChatColor.GREEN + "useful plugin v"+pluginVersion+" has been enabled!");
 	}
 	public void disableCommand(String cmdname) {
 		blockedCmds.add(cmdname);
 		return;
 		}
 	public void checkRegister(String path, String cmdname) {
 		Plugin pl = getServer().getPluginManager().getPlugin("useful");
         try {
 			boolean enable = config.getBoolean(path);
 			PluginCommand cmd = getServer().getPluginCommand(cmdname);
 			if (cmd.getPlugin() == pl && enable == false) {
 				unRegisterBukkitCommand(cmd);
 			}
 		} catch (Exception e) {
 			getLogger().log(Level.SEVERE, "Error with checking/disabling command /" + cmdname + " in plugin 'useful'");
 			e.printStackTrace();
 		}
         
 		
 	}
 	public void onDisable(){
 		if(config.getBoolean("uperms.enable")){
 		Player[] players = getServer().getOnlinePlayers();
 		for(int i=0;i<players.length;i++){
 			String name = players[i].getName();
 			plugin.permManager.unLoadPerms(name);
 		}
 		}
 		saveYamls();
 		auths.save();
 		if(broadcaster != null){
 		broadcaster.cancel();
 		}
 		backup.cancel();
 		idle.cancel();
 		idleRunning = false;
 		reloadUcMsg.cancel();
 		ucReloadMsgRun = false;
 		this.getServer().getScheduler().cancelTasks(getServer().getPluginManager().getPlugin("useful"));
         sqlite.close();
         authed.clear();
         System.gc();
         System.gc();
         colLogger.info(ChatColor.GREEN + "useful plugin v"+pluginVersion+" has been disabled.");
 		getLogger().info("useful plugin v"+pluginVersion+" has been disabled.");
 	}
 	
 	
 	public void saveHashMap(HashMap<String, ArrayList<String>> map, String path)
 	{
 		try
 		{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
 			oos.writeObject(map);
 			oos.flush();
 			oos.close();
 			//Handle I/O exceptions
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	public void saveHashMapString(HashMap<String, String> map, String path)
 	{
 		try
 		{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
 			oos.writeObject(map);
 			oos.flush();
 			oos.close();
 			//Handle I/O exceptions
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	public void saveHashMapBoolean(HashMap<String, Boolean> map, String path)
 	{
 		try
 		{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
 			oos.writeObject(map);
 			oos.flush();
 			oos.close();
 			//Handle I/O exceptions
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void saveHashMapObj(HashMap<String, SerializableLocation> jails2, String path)
 	{
 		try
 		{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
 			oos.writeObject(jails2);
 			oos.flush();
 			oos.close();
 			//Handle I/O exceptions
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public static <T extends Object> void saveHashMapObj2(T obj,String path) throws Exception
 	{
 		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
 		oos.writeObject(obj);
 		oos.flush();
 		oos.close();
 	}
 	public static <T extends Object> T loadHashMapObj2(String path) throws Exception
 	{
 		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
 		@SuppressWarnings("unchecked")
 		T result = (T)ois.readObject();
 		ois.close();
 		return result;
 	}
 
 
 	
 	
 	
 	
 		}
