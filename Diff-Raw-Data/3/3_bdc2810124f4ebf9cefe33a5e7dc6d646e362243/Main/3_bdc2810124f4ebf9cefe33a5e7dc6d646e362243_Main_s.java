 package nl.lolmen.sortal;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 //import nl.lolmen.database.MySQL;
 //import nl.lolmen.database.SQLite;
 
 import nl.lolmen.sortal.Metrics.Plotter;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin{
 
 	public Logger log;
 	public String maindir = "plugins/Sortal/";
 	public File settings = new File(maindir + "settings.yml");
 	public File warps = new File(maindir + "warps.txt");
 	public File locs = new File(maindir + "signs.txt");
 	//public String logPrefix = "[Sortal] ";
 	//Contains the Warps, with the locations.
 	public HashMap<String, Warp> warp = new HashMap<String, Warp>();
 	//Contains the Sign Locations with the warps.
 	public HashMap<Location, String> loc = new HashMap<Location, String>();
 	public HashMap<Player, String> register = new HashMap<Player, String>();
 	public HashMap<Player, Integer> cost = new HashMap<Player, Integer>();
 	public Set<Player> unreg = new HashSet<Player>();
 	private SBlockListener block = new SBlockListener(this);
 	private SPlayerListener player = new SPlayerListener(this);
 	//public MySQL mysql;
 	//public SQLite sql;
 
 	//Economy Plugins
 	//public iConomy iCo;
 
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
 	//private String dbUser;
 	//private String dbPass;
 	//private String dbDB;
 	//private String dbHost;
 	public int warpCreatePrice;
 	public int warpUsePrice;
 	public boolean onNoCoords;
 	public String signContains;
 	private boolean update;
 	private double version;
 	private double latestVersion;
 	private boolean showLoaded;
 	private boolean updateAvailable;
 	private double start;
 	private double end;
 
 	public void onDisable() {
 		this.saveLocations();
 		if(this.updateAvailable){
 			this.downloadFile("http://dl.dropbox.com/u/7365249/Sortal.jar");
 		}
 		this.log.info("Disabled!");
 	}
 	private void saveLocations() {
 		Properties prop = new Properties();
 		try{
 			if(!this.locs.exists()){
 				this.locs.createNewFile();
 			}
 			FileInputStream in = new FileInputStream(this.locs);
 			prop.load(in);
 			for(Location loc: this.loc.keySet()){
 				prop.put(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), this.loc.get(loc));
 			}
 			OutputStream out = new FileOutputStream(this.locs);
 			prop.store(out, "Location = warp");
 			this.getLogger().info("Saved " + this.loc.size() + " signs!");
 		}catch(Exception e){
 			e.printStackTrace();
 			this.getLogger().warning("Error while saving warps!");
 		}
 	}
 	public void downloadFile(String site){
 		try {
 			this.log.info("Updating Sortal.. Please wait.");
 			BufferedInputStream in = new BufferedInputStream(new URL(site).openStream());
 			FileOutputStream fout = new FileOutputStream(nl.lolmen.sortal.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
 			byte data[] = new byte[1024]; //Download 1 KB at a time
 			int count;
 			while((count = in.read(data, 0, 1024)) != -1)
 			{
 				fout.write(data, 0, count);
 			}
 			this.log.info("Sortal has been updated!");
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
 				c.load(this.settings);
 				c.set("version", this.latestVersion);
 				c.save(this.settings);
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void onEnable() {
 		this.start = System.nanoTime();
 		this.log = this.getLogger();
 		new File(this.maindir).mkdir();
 		this.makeSettings();
 		this.loadSettings();
 		try {
 			Metrics m = new Metrics();
 			m.addCustomData(this, new Plotter(){
 				@Override
 				public String getColumnName() {
 					return "Total Warps created";
 				}
 				@Override
 				public int getValue() {
 					return warp.size();
 				}
 			});
 			m.addCustomData(this, new Plotter(){
 				@Override
 				public String getColumnName() {
 					return "Total Signs registered";
 				}
 				@Override
 				public int getValue() {
 					return loc.size();
 				}
 			});
 			m.beginMeasuringPlugin(this);
 			this.log.info("Metrics loaded! View them @ http://metrics.griefcraft.com/plugin/Sortal");
 		} catch (IOException e) {
 			e.printStackTrace();
 			this.log.info("Failed to load Metrics!");
 		}
 		if(this.update){
 			this.checkUpdate();
 		}
 		this.loadDB();
 		this.loadWarps();
 		this.loadSigns();
 		this.loadPlugins();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents( this.player, this);
 		pm.registerEvents( this.block, this);
 		this.end = System.nanoTime();
 		double taken = (this.end-this.start)/1000000;
 		this.log.info("version " + this.version + " build " + this.getDescription().getVersion() + " enabled - took " + taken + "ms!");
 	}
 	private void checkUpdate() {
 		try {
 			URL url = new URL("http://dl.dropbox.com/u/7365249/sortal.txt");
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 			String str;
 			while((str = in.readLine()) != null)
 			{
 				if(this.version < Double.parseDouble(str)){
 					this.latestVersion = Double.parseDouble(str);
 					this.updateAvailable = true;
 					this.log.info("An update is available! Will be downloaded on Disable! New version: " + str);
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
 			if(!this.locs.exists()){
 				this.locs.createNewFile();
 				return;
 			}
 			this.log.info("Starting to load signs..");
 			BufferedReader in1 = new BufferedReader(new FileReader(this.locs));
 			String str;
 			while ((str = in1.readLine()) != null){
 				if(str.startsWith("#")){
 					continue;
 				}
 				if(!str.contains("=")){
 					continue;
 				}
 				String[] split = str.split("=");
 				String warp = split[1];
 				String[] rest = split[0].split(",");
 				if(rest.length == 3){
 					if(isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])){
 						this.loc.put(new Location(getServer().getWorlds().get(0), Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2])), warp);
 						if(this.showLoaded){
 							this.log.info("Sign pointing to " + warp + " loaded!");
 						}
 						continue;
 					}else{
 						this.log.info("Sign pointing to " + warp + " could not be loaded : Integers are off, length = 3!");
 						continue;
 					}
 				}
 				if(rest.length == 4){
 					if(isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])){
 						this.loc.put(new Location(getServer().getWorld(rest[3]), Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2])), warp);
 						if(this.showLoaded){
 							this.log.info("Sign pointing to " + warp + " loaded!");
 						}
 						continue;
 					}else if(isInt(rest[3]) && isInt(rest[1]) && isInt(rest[2])){
 						this.loc.put(new Location(getServer().getWorld(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2]), Integer.parseInt(rest[3])), warp);
 						if(this.showLoaded){
 							this.log.info("Sign pointing to " + warp + " loaded!");
 						}
 						continue;
 					}else{
 						this.log.info("Sign pointing to " + warp + " could not be loaded : Integers are off, length = 4!");
 					}
 				}
 			}
 			in1.close();
 			this.log.info(Integer.toString(this.loc.size()) + " signs loaded!");
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
 						log.info("a sign could not be loaded!");
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
 					log.info("a sign could not be loaded!");
 				}
 			}
 		}*/
 	}
 
 	private void loadWarps() {
 		//if(!useSQL && !useMySQL){
 		try {
 			if(!this.warps.exists()){
 				this.warps.createNewFile();
 				return;
 			}
 			this.log.info("Starting to load warps..");
 			BufferedReader in1 = new BufferedReader(new FileReader(warps));
 			String str;
 			while ((str = in1.readLine()) != null){
 				if(str.startsWith("#")){
 					continue;
 				}
 				String[] split = str.split("=");
 				String warp = split[0];
 				String[] restsplit = split[1].split(",");
 				if(isInt(restsplit[0])){
 					this.log.info("You seem to have an old version of warps.txt! Please use the following system: WARP=WORLD,X,Y,Z,PRICE!");
 					continue;
 				}else{
 					if(restsplit.length >= 4){
 						String wname = restsplit[0];
 						double x = Double.parseDouble(restsplit[1]);
 						double y = Double.parseDouble(restsplit[2]);
 						double z = Double.parseDouble(restsplit[3]);
 						int money;
 						if(restsplit.length == 5){
 							money = Integer.parseInt(restsplit[4]);
 						}else{
 							money = this.warpUsePrice;
 						}
 						this.warp.put(warp, new Warp(this, warp, getServer().getWorld(wname), x, y, z, money));
 						if(this.showLoaded){
 							this.log.info("Warp " + warp + " loaded!");
 						}
 					}else{
 						this.log.info("A Warp couldn't be loaded: " + warp);
 					}
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
 				    			log.info("Warp " + name + " loaded!");
 				    		}
 						}
 					} catch (SQLException e) {
 						e.printStackTrace();
 						log.info("a warp could not be loaded!");
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
 				    			log.info("Warp " + name + " loaded!");
 				    		}
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					log.info("a warp could not be loaded!");
 				}
 			}
 		}*/
 
 		this.log.info(Integer.toString(this.warp.size()) + " warps loaded!");
 	}
 
 	private void loadPlugins() {
 		Plugin test = getServer().getPluginManager().getPlugin("Vault");
 		if(test != null){
 			if(this.useVault){
 				this.log.info("Hooked into Vault!");
 			}else{
 				this.log.info("Vault found but not used due to settings");
 			}
 		}else if(this.useVault){
 			this.log.info("Vault not found, please download: http://dev.bukkit.org/server-mods/vault/files/");
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
 				log.info("MySQL connection successful");
 				log.info("Creating table Sortal...");
 				String query = "CREATE TABLE IF NOT EXISTS Sortal ('id' INT PRIMARY KEY, 'name' TEXT NOT NULL, 'world' TEXT, 'x' INT NOT NULL, 'y' int , 'z' int , 'warp' INT NOT NULL, 'cost' INT) ;";
 				mysql.createTable(query);
 			} else {
 				log.severe("MySQL connection failed");
 				useMySQL = false;
 			}
 		}*/
 	}
 
 
 	private void loadSettings() {
 		YamlConfiguration c = new YamlConfiguration();
 		try{
 			c.load(this.settings);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		this.useVault = c.getBoolean("plugins.useVault", true);
 		if(!c.contains("plugins.useVault")){
 			c.addDefault("plugins.useVault", true);
 		}
 		c.getBoolean("plugins.usePermissions", true);
 		if(!c.contains("plugins.usePermissions")){
 			c.addDefault("plugins.usePermissions", true);
 		}
 		this.noPerm = c.getString("no-permissions", "You do not have permissions to do that!");
 		this.warpCreateNameForgotten = c.getString("warpCreateNameForgotten", "You must give a name to this warp!");
 		this.warpCreateCoordsForgotten = c.getString("warpCreateCoordsForgotten", "You must specify the coords for this warp!");
 		this.warpDeleteNameForgotten = c.getString("warpDeleteNameForgotten", "You forgot to name the warp you want to delete!");
 		this.nameInUse = c.getString("nameInUse", "Sorry, that name is in use!");
 		this.moneyPayed = c.getString("moneyPayed", "This has cost you MONEY!");
 		this.warpCreated = c.getString("warpCreated", "Warp WARPNAME set up!");
 		this.notEnoughMoney = c.getString("notEnoughMoney", "You do not have enough money to do that!");
 		this.warpDeleted = c.getString("warpDeleted", "Warp WARPNAME deleted!");
 		this.warpDoesNotExist = c.getString("warpDoesNotExist", "This warp does not exist!");
 		this.notAplayer = c.getString("notAplayer", "You must be a player to use this command!");
 		//this.useSQL = c.getBoolean("useSQLite", false);
 		//this.useMySQL = c.getBoolean("useMySQL", false);
 		//this.dbUser = c.getString("MySQL.username");
 		//this.dbPass = c.getString("MySQL.password");
 		//this.dbHost = c.getString("MySQL.host");
 		//this.dbDB = c.getString("MySQL.Warpbase");
 		this.warpCreatePrice = c.getInt("warpCreatePrice", 0);
 		this.warpUsePrice = c.getInt("warpUsePrice", 0);
 		this.onNoCoords = c.getBoolean("ifNoCoordsUsePlayerCoords", true);
 		this.signContains = c.getString("signContains", "[Sortal]");
 		this.update = c.getBoolean("auto-update", false);
 		this.version = c.getDouble("version", 4.8);
 		if(!c.contains("version")){
 			c.addDefault("version", 4.8);
 		}
 		this.showLoaded = c.getBoolean("showWhenWarpGetsLoaded", true);
 		try {
 			c.save(this.settings);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void makeSettings() {
 		if(!this.settings.exists()){
 			try {
 				this.settings.createNewFile();
 				this.log.info("Trying to create default config...");
 				try {
 					File efile = new File(this.maindir, "settings.yml");
 
 					InputStream in = this.getClass().getClassLoader().getResourceAsStream("settings.yml");
 					OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
 					int c;
 					while((c = in.read()) != -1){
 						out.write(c);
 					}
 					out.flush();
 					out.close();
 					in.close();
 					this.log.info("Default config created succesfully!");
 				}catch (Exception e) {
 					e.printStackTrace();
 					this.log.warning("Error creating settings file! Using default settings!");
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
 			if(args[0].equalsIgnoreCase("warp") || args[0].equalsIgnoreCase("setwarp")){
 				if(sender instanceof Player){
 					if(sender.hasPermission("sortal.createwarp")){
 						//log.info(Integer.toString(args.length));
 						if(args.length == 1){
 							sender.sendMessage(this.warpCreateNameForgotten);
 							return true;
 						}
 						if(args.length == 2){
 							if(this.onNoCoords){
 								if(this.warp.containsKey(args[1])){
 									sender.sendMessage(this.nameInUse);
 									return true;
 								}
 								Warp d = new Warp(this, args[1], ((Player)sender).getLocation());
 								d.saveWarp();
 								sender.sendMessage(warpCreated(args[1]));
 								return true;
 							}
 							sender.sendMessage(this.warpCreateCoordsForgotten);
 							return true;
 						}
 						if(args.length == 3){
 							if(args[2].equalsIgnoreCase("here") || args[2].equalsIgnoreCase("this")){
 								if(this.warp.containsKey(args[1])){
 									sender.sendMessage(this.nameInUse);
 									return true;
 								}
 								Warp d = new Warp(this, args[1], ((Player)sender).getLocation());
 								d.saveWarp();
 								sender.sendMessage(warpCreated(args[1]));
 								return true;
 							}
 							sender.sendMessage("Not sure what to do with " + args[2]);
 							return true;
 						}
 						if(args.length == 4) {
 							sender.sendMessage("Missing some arguments! /sortal " + args[0] + " x y z <world> (world is optional)");
 							return true;
 						}
 						if(args.length > 4){
 							if(this.warp.containsKey(args[1])){
 								sender.sendMessage(this.nameInUse);
 								return true;
 							}
 							if(isInt(args[2]) && isInt(args[3]) && isInt(args[4])){
 								if(args.length == 6){
 									World w = getServer().getWorld(args[5]);
 									Warp d = new Warp(this, args[1], w, Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
 									d.saveWarp();
 									sender.sendMessage(this.warpCreated(args[1]));
 									return true;
 								}
 								World w = ((Player)sender).getWorld();
 								Warp d = new Warp(this, args[1], w, Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
 								d.saveWarp();
 								sender.sendMessage(this.warpCreated(args[1]));
 								return true;
 							}
 							sender.sendMessage("Error: Number expected, String given");
 							return true;
 						}
 						sender.sendMessage("Too little or too many arguments! Try /sortal setwarp " + args[1] + " here");
 						return true;
 					}
 					sender.sendMessage(this.noPerm);
 					return true;
 
 				}
 				sender.sendMessage(this.notAplayer);
 				return true;
 
 			}
 			if(args[0].equalsIgnoreCase("delwarp")){
 				if(sender.hasPermission("sortal.delwarp")){
 					if(args.length == 1){
 						sender.sendMessage(this.warpDeleteNameForgotten);
 						return true;
 					}
 					for(int i = 1; i < args.length;){
 						if(this.warp.containsKey(args[i])){
 							Warp d = this.warp.get(args[i]);
 							if(d.delWarp()){
 								sender.sendMessage(warpDeleted(args[i]));
 							}else{
 								sender.sendMessage("An error occured while deleting!");
 							}
 						}else{
 							sender.sendMessage(this.warpDoesNotExist);
 						}
 						i++;
 						return true;
 					}
 				}else{
 					sender.sendMessage(this.noPerm);
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("list")){
 				if(args.length == 1){
 					sender.sendMessage(ChatColor.GRAY + "Page 1/" + ((int)this.warp.size() / 9 + 1) + ChatColor.GREEN + " Warps: " + ChatColor.RED + Integer.toString(warp.size()));
 					int count = 1;
 					for(String entry: this.warp.keySet()){
 						if(count > 9){
 							return true;
 						}
 						Warp d = this.warp.get(entry);
 						sender.sendMessage(d.warp() + ": " + d.toString());
 						count++;
 					}
 					return true;
 				}
 				if(!this.isInt(args[1])){
 					sender.sendMessage("Page must be Integer, not something else!");
 					return true;
 				}
 				if(Integer.parseInt(args[1]) > (int)(this.warp.size() / 9) + 1){
 					sender.sendMessage("No page " + args[1] + " available!");
 					return true;
 				}
 				sender.sendMessage(ChatColor.GRAY + "Page " + args[1] + "/" + ((int)this.warp.size() / 9 + 1) + ChatColor.GREEN + " Warps: " + ChatColor.RED + Integer.toString(warp.size()));
 				int count = 1;
 				for(String entry: this.warp.keySet()){
 					if(count >= 9 * Integer.parseInt(args[1])){
 						return true;
 					}
 					if(count <= Integer.parseInt(args[1]) * 9 - 9){
 						count++;
 						continue;
 					}
 					Warp d = this.warp.get(entry);
 					sender.sendMessage(d.warp() + ": " + d.toString());
 					count++;
 				}
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("version")){
 				sender.sendMessage("[Sortal] Version is " + this.version);
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
 					sender.sendMessage(this.notAplayer);
 					return true;
 				}
 				if(args.length == 1){
 
 					if(this.register.containsKey((Player)sender)){
 						sender.sendMessage("No longer registering warp " + this.register.get((Player)sender));
 						this.register.remove(((Player)sender));
 						return true;
 					}
 					sender.sendMessage("You must also give the warpname!");
 					return true;
 				}
 				if(!this.warp.containsKey(args[1])){
 					sender.sendMessage("This warp does not exist! Can't register.");
 					return true;
 				}
 				this.register(((Player)sender), args[1]);
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("setprice")){
 				if(!(sender instanceof Player)){
 					sender.sendMessage(this.notAplayer);
 					return true;
 				}
 				if(!sender.hasPermission("sortal.setprice")){
 					sender.sendMessage(this.noPerm);
 					return true;
 				}
 				if(args.length == 1){
 					if(this.cost.containsKey((Player)sender)){
 						sender.sendMessage("No longer setting a cost!");
 						this.cost.remove((Player)sender);
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
 					if(!sender.hasPermission("sortal.unregister")){
 						sender.sendMessage(this.noPerm);
 						return true;
 					}
 					this.unregister((Player)sender);
 					return true;
 				}
 				sender.sendMessage(this.notAplayer);
 				return true;
 			}
 		}
 		return false;
 	}
 	private void unregister(Player player){
 		if(this.unreg.contains(player)){
 			player.sendMessage("No longer unregistering a sign!");
 			this.unreg.remove(player);
 		}else{
 			this.unreg.add(player);
 			player.sendMessage("Now punch the sign you want to unregister!");
 		}
 	}
 	private void setPrice(Player player, String string) {
 		if(this.cost.containsKey(player)){
 			player.sendMessage("No longer settign a price.");
 			this.cost.remove(player);
 		}else{
 			this.cost.put(player, Integer.parseInt(string));
 			player.sendMessage("Now punch the sign you wish to cost " + string + "!");
 		}
 	}
 
 	private void register(Player player, String string) {
 		if(this.register.containsKey(player)){
 			if(string.equalsIgnoreCase(register.get(player))){
 				player.sendMessage("No longer registering warp " + register.get(player));
 				this.register.remove(player);
 			}else{
 				player.sendMessage("Already registering a warp: " + register.get(player));
 				player.sendMessage("To stop registering, type /sortal register");
 			}
 		}else{
 			this.register.put(player, string);
 			player.sendMessage("Now punch the sign you wish to be pointing at " + string + "!");
 		}
 	}
 
 	private String warpDeleted(String name) {
 		String get = this.warpDeleted.replace("WARPNAME", name);
 		return get;
 	}
 
 	private String warpCreated(String name) {
 		String get = this.warpCreated.replace("WARPNAME", name);
 		return get;
 	}
 }
 
