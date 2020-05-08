 package nl.lolmen.sortal;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.logging.Logger;
 //import nl.lolmen.database.MySQL;
 //import nl.lolmen.database.SQLite;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin{
 	
 	public Logger log = Logger.getLogger("Minecraft");
 	public String maindir = "plugins/Sortal/";
 	public File settings = new File(maindir + "settings.yml");
 	public File warps = new File(maindir + "warps.txt");
 	public File locs = new File(maindir + "signs.txt");
 	public String logPrefix = "[Sortal] ";
 	//Contains the Warps, with the locations.
 	public HashMap<String, Warp> warp = new HashMap<String, Warp>();
 	//Contains the Sign Locations with the warps.
 	public HashMap<Location, String> loc = new HashMap<Location, String>();
 	public HashMap<Player, String> register = new HashMap<Player, String>();
 	public HashMap<Player, Integer> cost = new HashMap<Player, Integer>();
 	public HashMap<Player, Object> unreg = new HashMap<Player, Object>();
 	public Warp Warps = new Warp(this);
 	public SBlockListener block = new SBlockListener(this);
 	public SPlayerListener player = new SPlayerListener(this);
 	//public MySQL mysql;
 	//public SQLite sql;
 	
 	//Economy Plugins
 	//public iConomy iCo;
 	
 	//Settings
 	public boolean usePerm;
 	public boolean useVault;
 	public String noPerm;
 	public String warpCreateNameForgotten;
 	public String warpCreateCoordsForgotten;
 	public String warpDeleteNameForgotten;
 	public String nameInUse;
 	public String moneyPayed;
 	public String warpCreated;
 	public String notEnoughMoney;
 	public String warpDeleted;
 	public String warpDoesNotExist;
 	public String notAplayer;
 	//public boolean useSQL;
 	//public boolean useMySQL;
 	public String dbUser;
 	public String dbPass;
 	public String dbDB;
 	public String dbHost;
 	public int warpCreatePrice;
 	public int warpUsePrice;
 	public boolean onNoCoords;
 	public String signContains;
 	public boolean update;
 	public double version;
 	public double latestVersion;
 	public boolean showLoaded;
 	public boolean updateAvailable;
 	double start;
 	double end;
 	
 	boolean converting;
 	HashMap<String, String> map = new HashMap<String, String>();
 
 	public void onDisable() {
 		if(updateAvailable){
 			downloadFile("http://dl.dropbox.com/u/7365249/Skillz.jar");
 		}
 		log.info(logPrefix + "Disabled!");
 	}
 	public void downloadFile(String site){
 		try {
 			log.info("Updating Sortal.. Please wait.");
 			BufferedInputStream in = new BufferedInputStream(new URL(site).openStream());
 			FileOutputStream fout = new FileOutputStream(nl.lolmen.sortal.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
 			byte data[] = new byte[1024]; //Download 1 KB at a time
 			int count;
 			while((count = in.read(data, 0, 1024)) != -1)
 			{
 				fout.write(data, 0, count);
 			}
 			log.info("Sortal has been updated!");
 			in.close();
 			fout.close();
 		} catch(MalformedURLException e) {
 			e.printStackTrace();
 		} catch(IOException e) {
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}finally{
 			YamlConfiguration c = new YamlConfiguration();
 			try{
 				c.load(settings);
 				c.set("version", latestVersion);
 				c.save(settings);
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void onEnable() {
 		start = System.nanoTime();
 		new File(maindir).mkdir();
 		makeSettings();
 		loadSettings();
 		if(update){
 			checkUpdate();
 		}
 		loadDB();
 		loadWarps();
 		loadSigns();
 		loadPlugins();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents( player, this);
 		pm.registerEvents( block, this);
 		end = System.nanoTime();
 		double taken = (end-start)/1000000;
 		log.info(logPrefix + "Enabled! It took " + Double.toString(taken) + "ms!");
 	}
 	private void checkUpdate() {
 		try {
 			URL url = new URL("http://dl.dropbox.com/u/7365249/sortal.txt");
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 			String str;
 			while((str = in.readLine()) != null)
 			{
 				if(version < Double.parseDouble(str)){
 					latestVersion = Double.parseDouble(str);
 					updateAvailable = true;
 					log.info(logPrefix + "An update is available! Will be downloaded on Disable! New version: " + str);
 				}
 			}
 			in.close();
 		} catch(MalformedURLException e) {
 			e.printStackTrace();
 		} catch(IOException e) {
 			e.printStackTrace();
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadSigns() {
 		//if(!useSQL && !useMySQL){
 			try {
 				if(!locs.exists()){
 					locs.createNewFile();
 					return;
 				}
 				log.info(logPrefix + "Starting to load signs..");
 				BufferedReader in1 = new BufferedReader(new FileReader(locs));
 				String str;
 				while ((str = in1.readLine()) != null){
 					processLocs(str);
 				}
 				in1.close();
 				log.info(logPrefix + Integer.toString(loc.size()) + " signs loaded!");
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		//}
 		/*if(useSQL){
 			if(sql.checkConnection()){
 				ResultSet set = sql.query("SELECT * FROM Sortal WHERE warp = 0;");
 				if(!(set == null)){
 					try {
 						while(set.next()){
 							String name = set.getString("name");
 							String world = set.getString("world");
 							int x = set.getInt("x");
 							int y = set.getInt("y");
 							int z = set.getInt("z");
 							loc.put(new Location(getServer().getWorld(world), x, y, z), name);
 							if(showLoaded){
 								log.info("Sign pointing to " + name + " loaded!");
 							}
 						}
 					} catch (SQLException e) {
 						e.printStackTrace();
 						log.info(logPrefix + "a sign could not be loaded!");
 					}
 				}
 			}
 		}
 		if(useMySQL){
 			if(mysql.checkConnection()){
 				try {
 					ResultSet set = mysql.query("SELECT * FROM Sortal WHERE warp = 0;");
 					if(!(set == null)){
 						while(set.next()){
 							String name = set.getString("name");
 							String world = set.getString("world");
 							int x = set.getInt("x");
 							int y = set.getInt("y");
 							int z = set.getInt("z");
 							loc.put(new Location(getServer().getWorld(world), x, y, z), name);
 							log.info("Sign pointing to " + name + " loaded!");
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					log.info(logPrefix + "a sign could not be loaded!");
 				}
 			}
 		}*/
 	}
 
 	private void processLocs(String str) {
 		if(str.startsWith("#")){
 			return;
 		}
 		if(!str.contains("=")){
 			return;
 		}
 		String[] split = str.split("=");
 		String warp = split[1];
 		String[] rest = split[0].split(",");
 		if(rest.length == 3){
 			if(isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])){
 				loc.put(new Location(getServer().getWorld("world"), Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2])), warp);
 				if(showLoaded){
 					log.info(logPrefix + "Sign pointing to " + warp + " loaded!");
 				}
 				return;
 			}else{
 				if(showLoaded){
 					log.info(logPrefix + "Sign pointing to " + warp + " could not be loaded!");
 				}
 				return;
 			}
 		}
 		if(rest.length == 4){
 			if(isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])){
 				loc.put(new Location(getServer().getWorld(rest[3]), Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2])), warp);
 				if(showLoaded){
 					log.info(logPrefix + "Sign pointing to " + warp + " loaded!");
 				}
 				return;
 			}else if(isInt(rest[3]) && isInt(rest[1]) && isInt(rest[2])){
 				loc.put(new Location(getServer().getWorld(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2]), Integer.parseInt(rest[3])), warp);
 				if(showLoaded){
 					log.info(logPrefix + "Sign pointing to " + warp + " loaded!");
 				}
 				return;
 			}else{
 				if(showLoaded){
 					log.info(logPrefix + "Sign pointing to " + warp + " could not be loaded!");
 				}
 			}
 		}
 	}
 
 	private void loadWarps() {
 		//if(!useSQL && !useMySQL){
 			try {
 				if(!warps.exists()){
 					warps.createNewFile();
 					return;
 				}
 				log.info(logPrefix + "Starting to load warps..");
 				BufferedReader in1 = new BufferedReader(new FileReader(warps));
 				String str;
 				while ((str = in1.readLine()) != null){
 					if(!converting){
 						process(str);
 					}else{
 						converting = false;
 						convert();
 						return;
 					}
 				}
 				in1.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		//}
 		/*if(useSQL){
 			if(sql.checkConnection()){
 				ResultSet set = sql.query("SELECT * FROM Sortal WHERE warp = 1;");
 				if(!(set == null)){
 					try {
 						while(set.next()){
 							String name = set.getString("name");
 							String world = set.getString("world");
 							int x = set.getInt("x");
 							int y = set.getInt("y");
 							int z = set.getInt("z");
 							int cost = set.getInt("cost");
 							warp.put(name, new Warp(this, name, new Location(getServer().getWorld(world), x, y, z), cost));
 				    		if(showLoaded){
 				    			log.info(logPrefix + "Warp " + name + " loaded!");
 				    		}
 						}
 					} catch (SQLException e) {
 						e.printStackTrace();
 						log.info(logPrefix + "a warp could not be loaded!");
 					}
 				}
 			}
 		}
 		if(useMySQL){
 			if(mysql.checkConnection()){
 				try {
 					ResultSet set = mysql.query("SELECT * FROM Sortal WHERE warp = 1;");
 					if(!(set == null)){
 						while(set.next()){
 							String name = set.getString("name");
 							String world = set.getString("world");
 							int x = set.getInt("x");
 							int y = set.getInt("y");
 							int z = set.getInt("z");
 							int cost = set.getInt("cost");
 							warp.put(name, new Warp(this, name, new Location(getServer().getWorld(world), x, y, z), cost));
 				    		if(showLoaded){
 				    			log.info(logPrefix + "Warp " + name + " loaded!");
 				    		}
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					log.info(logPrefix + "a warp could not be loaded!");
 				}
 			}
 		}*/
 
 		log.info(logPrefix + Integer.toString(warp.size()) + " warps loaded!");
 	}
 
 	private void process(String str) {
 		if(str.startsWith("#")){
 			return;
 		}
 		String[] split = str.split("=");
 	    String warp = split[0];
 	    String[] restsplit = split[1].split(",");
 	    if(isInt(restsplit[0])){
 	    	log.info(logPrefix + "You seem to have an old version of warps.txt! Converting!");
 	    	converting = true;
 	    }else{
 	    	if(restsplit.length == 4){
 	    		String wname = restsplit[0];
 		    	double x = Double.parseDouble(restsplit[1]);
 	    		double y = Double.parseDouble(restsplit[2]);
 	    		double z = Double.parseDouble(restsplit[3]);
 	    		this.warp.put(warp, new Warp(this, warp, getServer().getWorld(wname), x, y, z));
 	    		if(showLoaded){
 	    			log.info(logPrefix + "Warp " + warp + " loaded!");
 	    		}
 	    	}else{
 	    		log.info(logPrefix + "A Warp couldn't be loaded!");
 	    	}
 	    }
 	}
 	private void convert() {
 		File f = new File(maindir + "warps_old.txt");
 		warps.renameTo(f);
 		BufferedReader in1;
 		try {
 			in1 = new BufferedReader(new FileReader(warps));
 			String str;
 			while ((str = in1.readLine()) != null){
 				convertLine(str);
 			}
 			Properties prop = new Properties();
 			new File(maindir + "warps.txt").createNewFile();
 			prop.putAll(map);
 			FileOutputStream out = new FileOutputStream(new File(maindir + "warps.txt"));
 			prop.store(out, "[WarpName]=[World],[X],[Y],[Z]");
 			in1.close();
 			out.flush();
 			out.close();
 			loadWarps();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void convertLine(String str) {
 		if(str.startsWith("#")){
 			return;
 		}
 		String[] split = str.split("=");
 	    String warp = split[0];
 	    String[] restsplit = split[1].split(",");
 	    if(restsplit.length == 3){
 	    	//No world specified, defaulting to "world"
 	    	String back = "world," + split[1];
 	    	map.put(warp, back);
 	    }
 	    if(restsplit.length == 4){
 	    	String back = restsplit[3] + "," + restsplit[0] + "," + restsplit[1] + "," + restsplit[2];
 	    	map.put(warp, back);
 	    }
 	}
 
 	private void loadPlugins() {
 		Plugin test;
 		test = getServer().getPluginManager().getPlugin("Vault");
 		if(test != null){
 			if(useVault){
 				log.info("[Sortal] Hooked into Vault!");
 				return;
 			}else{
 				log.info("[Sortal] iConomy found but not used due to settings");
 			}
 		}
 	}
 	
 
 	private void loadDB() {
 		/*if(!useMySQL && !useSQL){
 			return;
 		}
 		if(useMySQL && useSQL){
 			log.info("MySQL and SQLite are both set, but only one can be used! Using flatfile until done!");
 			useMySQL = false;
 			useSQL = false;
 			return;
 		}
 		if(useSQL){
 			sql = new SQLite(log, logPrefix, "Sortal", "plugins/Sortal/");
 			sql.checkConnection();
 			if (!sql.checkTable("Skillz")) {
 				String query = "CREATE TABLE Sortal ('id' INT PRIMARY KEY, 'name' TEXT NOT NULL, 'world' TEXT, 'x' INT NOT NULL, 'y' int , 'z' int , 'warp' INT NOT NULL, 'cost' INT) ;";
 				sql.createTable(query);
 				log.info("[Sortal] SQL Warpbase created!");
 			}
 		}
 		if(useMySQL){
 			mysql = new MySQL(log, logPrefix, dbHost, "3306", dbDB, dbUser, dbPass);
 			if (mysql.checkConnection()) {
 				log.info(logPrefix + "MySQL connection successful");
 				log.info(logPrefix + "Creating table Sortal...");
 				String query = "CREATE TABLE IF NOT EXISTS Sortal ('id' INT PRIMARY KEY, 'name' TEXT NOT NULL, 'world' TEXT, 'x' INT NOT NULL, 'y' int , 'z' int , 'warp' INT NOT NULL, 'cost' INT) ;";
 				mysql.createTable(query);
 			} else {
 				log.severe(logPrefix + "MySQL connection failed");
 				useMySQL = false;
 			}
 		}*/
 	}
 
 
 	private void loadSettings() {
 		YamlConfiguration c = new YamlConfiguration();
 		try{
 			c.load(settings);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		useVault = c.getBoolean("plugins.useVault");
 		usePerm = c.getBoolean("plugins.usePermissions", false);
 		noPerm = c.getString("no-permissions", "You do not have permissions to do that!");
 		warpCreateNameForgotten = c.getString("warpCreateNameForgotten", "You must give a name to this warp!");
 		warpCreateCoordsForgotten = c.getString("warpCreateCoordsForgotten", "You must specify the coords for this warp!");
 		warpDeleteNameForgotten = c.getString("warpDeleteNameForgotten", "You forgot to name the warp you want to delete!");
 		nameInUse = c.getString("nameInUse", "Sorry, that name is in use!");
 		moneyPayed = c.getString("moneyPayed", "This has cost you MONEY!");
 		warpCreated = c.getString("warpCreated", "Warp WARPNAME set up!");
 		notEnoughMoney = c.getString("notEnoughMoney", "You do not have enough money to do that!");
 		warpDeleted = c.getString("warpDeleted", "Warp WARPNAME deleted!");
 		warpDoesNotExist = c.getString("warpDoesNotExist", "This warp does not exist!");
 		notAplayer = c.getString("notAplayer", "You must be a player to use this command!");
 		//useSQL = c.getBoolean("useSQLite", false);
 		//useMySQL = c.getBoolean("useMySQL", false);
 		dbUser = c.getString("MySQL.username");
 		dbPass = c.getString("MySQL.password");
 		dbHost = c.getString("MySQL.host");
 		dbDB = c.getString("MySQL.Warpbase");
 		warpCreatePrice = c.getInt("warpCreatePrice", 0);
 		warpUsePrice = c.getInt("warpUsePrice", 0);
 		onNoCoords = c.getBoolean("ifNoCoordsUsePlayerCoords", true);
 		signContains = c.getString("signContains", "[Sortal]");
 		update = c.getBoolean("auto-update", false);
 		version = c.getDouble("version", 4.4);
 		showLoaded = c.getBoolean("showWhenWarpGetsLoaded", true);
 
 	}
 
 	private void makeSettings() {
 		if(!settings.exists()){
 			try {
 				settings.createNewFile();
 				log.info(logPrefix + "Trying to create default config...");
 				try {
 					File efile = new File(maindir, "settings.yml");
 					
 					InputStream in = this.getClass().getClassLoader().getResourceAsStream("settings.yml");
 					OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
 					int c;
 					while((c = in.read()) != -1){
 						out.write(c);
 					}
 					out.flush();
 					out.close();
 					in.close();
 					log.info(logPrefix + "Default config created succesfully!");
 				}catch (Exception e) {
 					e.printStackTrace();
 					log.warning(logPrefix + "Error creating settings file! Using default settings!");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean isInt(String i) {
 		try {
 			Integer.parseInt(i);
 			return true;
 		} catch (NumberFormatException nfe) {
 			return false;
 		}
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args){
 		if(s.equalsIgnoreCase("sortal")){
 			if(args.length == 0){
 				sender.sendMessage("======[Sortal]======");
 				sender.sendMessage("=Sign Based Teleportation=");
 				sender.sendMessage("Type /sortal help for the help page!");
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("test")){
 				if(sender instanceof Player){
 					Player p = (Player)sender;
 					if(p.getName().equalsIgnoreCase("lolmewn")){
 						Warp d = new Warp(this, "lol", p.getLocation());
 						Warp dd = new Warp(this, "lool", p.getLocation());
 						p.sendMessage(d.warp());
 						p.sendMessage(dd.warp());
 						return true;
 					}else{
 						p.sendMessage("You cannot test this plugin!");
 						return true;
 					}
 				}
 			}				
 			if(args[0].equalsIgnoreCase("warp") || args[0].equalsIgnoreCase("setwarp")){
 				if(sender instanceof Player){
 					if(sender.hasPermission("sortal.createwarp")){
 						//log.info(Integer.toString(args.length));
 						if(args.length == 1){
 							sender.sendMessage(warpCreateNameForgotten);
 							return true;
 						}
 						if(args.length == 2){
 							if(onNoCoords){
 								if(warp.containsKey(args[1])){
 									sender.sendMessage(nameInUse);
 									return true;
 								}
 								Warp d = new Warp(this, args[1], ((Player)sender).getLocation());
 								d.saveWarp();
 								sender.sendMessage(warpCreated(args[1]));
 								return true;
 							}else{
 								sender.sendMessage(warpCreateCoordsForgotten);
 								return true;
 							}
 						}
 						if(args.length == 3){
 							if(args[2].equalsIgnoreCase("here") || args[2].equalsIgnoreCase("this")){
 								if(warp.containsKey(args[1])){
 									sender.sendMessage(nameInUse);
 									return true;
 								}
 								Warp d = new Warp(this, args[1], ((Player)sender).getLocation());
 								d.saveWarp();
 								sender.sendMessage(warpCreated(args[1]));
 								return true;
 							}
 						}
 						if(args.length > 4){
 							if(warp.containsKey(args[1])){
 								sender.sendMessage(nameInUse);
 								return true;
 							}
 							if(isInt(args[2])){
 								if(isInt(args[3])){
 									if(isInt(args[4])){
 										if(args.length == 6){
 											World w = getServer().getWorld(args[5]);
 											Warp d = new Warp(this, args[1], w, Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
 											d.saveWarp();
 											sender.sendMessage(warpCreated(args[1]));
 											return true;
 										}else{
 											World w = ((Player)sender).getWorld();
 											Warp d = new Warp(this, args[1], w, Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
 											d.saveWarp();
 											sender.sendMessage(warpCreated(args[1]));
 											return true;
 										}
 										
 									}else{
 										sender.sendMessage("Error: Number expected, String given");
 										return true;
 									}
 								}else{
 									sender.sendMessage("Error: Number expected, String given");
 									return true;
 								}
 							}else{
 								sender.sendMessage("Error: Number expected, String given");
 								return true;
 							}
 						}
 						sender.sendMessage("Too little or too many arguments! Try /sortal setwarp " + args[1] + " here");
 						return true;
 					}else{
 						sender.sendMessage(noPerm);
 						return true;
 					}
 				}else{
 					sender.sendMessage(notAplayer);
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("delwarp")){
 				if(sender.hasPermission("sortal.delwarp")){
 					if(args.length == 1){
 						sender.sendMessage(warpDeleteNameForgotten);
 						return true;
 					}
 					if(args.length == 2){
 						if(warp.containsKey(args[1])){
 							Warp d = warp.get(args[1]);
 							if(d.delWarp()){
 								sender.sendMessage(warpDeleted(args[1]));
 								return true;
 							}else{
 								sender.sendMessage("An error occured while deleting!");
 								return true;
 							}
 						}else{
 							sender.sendMessage(warpDoesNotExist);
 							return true;
 						}
 					}
 				}else{
 					sender.sendMessage(noPerm);
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("list")){
 				sender.sendMessage("Warps: " + Integer.toString(warp.size()));
 				Collection<Warp> c = warp.values();
 				Object[] array = c.toArray();
 				for(Object str: array){
 					if(str instanceof Warp){
 						Warp d = (Warp)str;
 						sender.sendMessage(d.warp() + ": " + d.toString());
 					}
 				}
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("version")){
 				PluginDescriptionFile pdfFile = this.getDescription();
 				sender.sendMessage("[Sortal] Version is " + pdfFile.getVersion());
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("help")){
 				sender.sendMessage("===Sortal Help Page===");
 				sender.sendMessage("/sortal warp <name> <here|X Y Z>");
 				sender.sendMessage("/sortal delwarp <name>");
 				sender.sendMessage("/sortal version");
 				sender.sendMessage("/sortal help");
 				sender.sendMessage("/sortal register <name>");
 				sender.sendMessage("/sortal setprice <price>");
 				sender.sendMessage("/sortal unregister");
 				sender.sendMessage("/sortal list");
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("register")){
 				if(!sender.hasPermission("sortal.register")){
 					sender.sendMessage(noPerm);
 					return true;
 				}
 				if(!(sender instanceof Player)){
 					sender.sendMessage(notAplayer);
 					return true;
 				}
 				if(args.length == 1){
 					
 					if(register.containsKey((Player)sender)){
 						sender.sendMessage("No longer registering warp " + register.get((Player)sender));
 						register.remove(((Player)sender));
 						return true;
 					}
 					sender.sendMessage("You must also give the warpname!");
 					return true;
 				}
 				if(!warp.containsKey(args[1])){
 					sender.sendMessage("This warp does not exist! Can't register.");
 					return true;
 				}
 				register(((Player)sender), args[1]);
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("setprice")){
 				if(!(sender instanceof Player)){
 					sender.sendMessage(notAplayer);
 					return true;
 				}
 				if(!sender.hasPermission("sortal.setprice")){
 					sender.sendMessage(noPerm);
 					return true;
 				}
 				if(args.length == 1){
 					if(cost.containsKey((Player)sender)){
 						sender.sendMessage("No longer setting a cost!");
 						cost.remove((Player)sender);
 						return true;
 					}
 					sender.sendMessage("You must also give a price!");
 					return true;
 				}
 				if(isInt(args[1])){
 					setPrice(((Player)sender), args[1]);
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("unregister")){
 				if(sender instanceof Player){
					if(sender.hasPermission("sortal.unregister")){
 						sender.sendMessage(noPerm);
 						return true;
 					}
 					unregister((Player)sender);
 					return true;
 				}else{
 					sender.sendMessage("You have to be a player to do this!");
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	private void unregister(Player player){
 		if(unreg.containsKey(player)){
 			player.sendMessage("No longer unregistering a sign!");
 			unreg.remove(player);
 		}else{
 			unreg.put(player, null);
 			player.sendMessage("Now punch the sign you want to unregister!");
 		}
 	}
 	private void setPrice(Player player, String string) {
 		if(cost.containsKey(player)){
 			player.sendMessage("No longer settign a price.");
 			cost.remove(player);
 		}else{
 			cost.put(player, Integer.parseInt(string));
 			player.sendMessage("Now punch the sign you wish to cost " + string + "!");
 		}
 	}
 
 	private void register(Player player, String string) {
 		if(register.containsKey(player)){
 			if(string.equalsIgnoreCase(register.get(player))){
 				player.sendMessage("No longer registering warp " + register.get(player));
 				register.remove(player);
 			}else{
 				player.sendMessage("Already registering a warp: " + register.get(player));
 				player.sendMessage("To stop registering, type /sortal register");
 			}
 		}else{
 			register.put(player, string);
 			player.sendMessage("Now punch the sign you wish to be pointing at " + string + "!");
 		}
 	}
 
 	private String warpDeleted(String name) {
 		String get = warpDeleted.replace("WARPNAME", name);
 		return get;
 	}
 
 	private String warpCreated(String name) {
 		String get = warpCreated.replace("WARPNAME", name);
 		return get;
 	}
 }
 
