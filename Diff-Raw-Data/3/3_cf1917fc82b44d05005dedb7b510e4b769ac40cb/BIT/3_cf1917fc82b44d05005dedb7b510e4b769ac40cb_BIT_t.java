 package dk.gabriel333.BukkitInventoryTools;
 
 import java.lang.reflect.Field;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.alta189.sqlLibrary.MySQL.mysqlCore;
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 import dk.gabriel333.register.payment.Method;
 import dk.gabriel333.register.payment.Methods;
 
 import de.Keyle.MyWolf.MyWolfPlugin;
 import dk.gabriel333.BukkitInventoryTools.Commands.*;
 import dk.gabriel333.BukkitInventoryTools.Listeners.*;
 import dk.gabriel333.BukkitInventoryTools.Inventory.*;
 import dk.gabriel333.BukkitInventoryTools.Book.*;
 import dk.gabriel333.BukkitInventoryTools.DigiLock.*;
 import dk.gabriel333.Library.G333Config;
 import dk.gabriel333.Library.G333Messages;
 import dk.gabriel333.Library.G333Plugin;
 
 import me.neatmonster.spoutbackpack.SBHandler;
 
 public class BIT extends JavaPlugin {
 
 	public static BIT plugin;
 
 	public static Boolean spout = false;
 
 	// Hook into register
 	public static Boolean useEconomy = false;
 	public Methods Methods;
 	public Method Method;
 
 	// Hook into SpoutBackpack
 	public static SBHandler spoutBackpackHandler;
 	public static Boolean spoutbackpack = false;
 
 	// Hook into MyWolf
 	public static Boolean mywolf = false;
 	public static MyWolfPlugin myWolfPlugin;
 
 	@Override
 	public void onEnable() {
 		plugin = this;
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		if (!isSortInventoryInstalled()) {
 			G333Plugin.setupPlugin(this);
 			G333Config.bitSetupConfig();
 			setupSpout();
 			setupSQL();
 			setupRegister();
 			setupSpoutBackpack();
 			setupMyWolf();
 			registerEvents();
 			addCommands();
 			setupBook();
 			// BITPlayer.clearAllUserData();
 			G333Messages.showInfo("BIT version " + pdfFile.getVersion()
 					+ " is enabled!");
 		} else {
 			G333Messages.showError(pdfFile.getName() + " version "
 					+ pdfFile.getVersion() + " could not be installed!");
 		}
 	}
 
 	// Test for SortInventory
 	private Boolean isSortInventoryInstalled() {
 		Plugin sortInventoryPlugin = this.getServer().getPluginManager()
 				.getPlugin("SortInventory");
 		if (sortInventoryPlugin != null) {
 			G333Messages
 					.showError("SortInventory is outdated and conflicts with BukkitInventoryTools!");
 			return true;
 		}
 		return false;
 	}
 
 	public void registerEvents() {
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_BREAK, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGE, new BITBlockListener(),
 				Priority.Normal, this);
 		// REDSTONE_CHANGE is disabled because of memory leak
 		// pm.registerEvent(Type.REDSTONE_CHANGE, new BITBlockListener(),
 		// Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_PHYSICS, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_FROMTO, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_FORM, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_BURN, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_FADE, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_SPREAD, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_IGNITE, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.SIGN_CHANGE, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_PISTON_EXTEND, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_PISTON_RETRACT, new BITBlockListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, new BITPlayerListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, new BITPlayerListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, new BITPlayerListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, new BITPlayerListener(),
 				Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, new BITPlayerListener(),
 				Priority.Normal, this);
 		// BITDigiLock Listeners
 		pm.registerEvent(Event.Type.CUSTOM_EVENT,
 				new BITDigiLockInputListener(), Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.CUSTOM_EVENT,
 				new BITDigiLockSpoutListener(), Event.Priority.Normal, this);
 		// BITIventory Listeners
 		pm.registerEvent(Event.Type.CUSTOM_EVENT,
 				new BITInventorySpoutListener(), Event.Priority.Normal, this);
 
 		// BITBook Listeners
 		pm.registerEvent(Event.Type.CUSTOM_EVENT,
 				new BITInventoryListener(this), Event.Priority.Low, this);
 		pm.registerEvent(Event.Type.CUSTOM_EVENT, new BITBookInputListener(),
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.CUSTOM_EVENT, new BITBookSpoutListener(),
 				Event.Priority.Normal, this);
 
 		// BITKeyboardListener
 		pm.registerEvent(Event.Type.CUSTOM_EVENT, new BITKeyboardListener(),
 				Event.Priority.Normal, this);
 
 	}
 
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		G333Messages.showInfo(pdfFile.getName() + " version "
 				+ pdfFile.getVersion() + " is disabled!");
 	}
 
 	public void addCommands() {
 		// Register commands
 		getCommand("Sort").setExecutor(new BITCommandSort(this));
 		getCommand("Digilock").setExecutor(new BITCommandDigiLock(this));
 		getCommand("Bookshelf").setExecutor(new BITCommandBookshelf(this));
 	}
 
 	private void setupSpout() {
 		Plugin spoutPlugin = this.getServer().getPluginManager()
 				.getPlugin("Spout");
 		if (spoutPlugin != null) {
 			spout = true;
 			G333Messages.showInfo("Spout is detected.");
 		} else {
 			G333Messages.showError("BIT is dependend on Spout!");
 		}
 	}
 
 	private void setupRegister() {
 		Plugin iconomyPlugin = this.getServer().getPluginManager()
 				.getPlugin("iConomy");
 		Plugin boseconomyPlugin = this.getServer().getPluginManager()
 				.getPlugin("BOSEconomy");
 		Plugin essentialsPlugin = this.getServer().getPluginManager()
 				.getPlugin("Essentials");
 		Plugin multiCurrencyPlugin = this.getServer().getPluginManager()
 				.getPlugin("MultiCurrency");
 		if (iconomyPlugin != null || boseconomyPlugin != null
 				|| essentialsPlugin != null || multiCurrencyPlugin != null) {
 			getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE,
 					new BITServerListener(this), Priority.Monitor, this);
 			getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE,
 					new BITServerListener(this), Priority.Monitor, this);
 		}
 	}
 
 	public static boolean isPlayer(CommandSender sender) {
 		if (sender instanceof Player)
 			return true;
 		return false;
 	}
 
 	private void setupSpoutBackpack() {
 		if (spoutBackpackHandler == null) {
 			Plugin spoutBackpackPlugin = this.getServer().getPluginManager()
 					.getPlugin("SpoutBackpack");
 			if (spoutBackpackPlugin != null) {
 				if (spout == true) {
 					spoutBackpackHandler = new SBHandler();
 					spoutbackpack = true;
 					G333Messages.showInfo("SpoutBackpack is detected.");
 				} else {
 					G333Messages
 							.showWarning("SpoutBackpack is detected, but spout is not detected.");
 					spoutbackpack = false;
 				}
 			}
 		}
 	}
 
 	private void setupMyWolf() {
 		if (myWolfPlugin == null) {
 			myWolfPlugin = (MyWolfPlugin) this.getServer().getPluginManager()
 					.getPlugin("MyWolf");
 			if (myWolfPlugin != null) {
 				if (spout == true) {
 					mywolf = true;
 					G333Messages.showInfo("MyWolf is detected.");
 				} else {
 					G333Messages
 							.showWarning("MyWolf is detected, but spout is not detected.");
 					mywolf = false;
 				}
 			}
 		}
 		// you get access to MyWolf inventory with:
 		// CustomMCInventory inv = myWolfPlugin.getMyWolf(sPlayer).inv;
 	}
 
 	// SQLITE-MYSQL settings
 	public static mysqlCore manageMySQL; // MySQL handler
 	public static sqlCore manageSQLite; // SQLite handler
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static String digilockTable = "BukkitInventoryTools5";
 	public static String oldDigilockTable = "BukkitInventoryTools4";
 	public static String bitInventoryTable = "Bookshelf";
 	public static String oldBitInventoryTable = "Bookshelf_NONE";
 	public static String bookTable = "Book";
 	public static String oldBookTable = "Book_NONE";
 
 	private void setupSQL() {
 		if (G333Config.STORAGE_TYPE.equals("MYSQL")) {
 			// Declare MySQL Handler
 			manageMySQL = new mysqlCore(log,
 					"[" + G333Plugin.PLUGIN_NAME + "]",
 					G333Config.STORAGE_HOST, G333Config.STORAGE_DATABASE,
 					G333Config.STORAGE_USERNAME, G333Config.STORAGE_PASSWORD);
 			G333Messages.showInfo("MySQL Initializing");
 			// Initialize MySQL Handler
 			manageMySQL.initialize();
 			try {
 				if (manageMySQL.checkConnection()) {
 					// Check if the Connection was successful
 					String query;
 					G333Messages.showInfo("MySQL connection successful");
 
 					// Check DigiLockTable
 					if (!manageMySQL.checkTable(digilockTable)) {
 						if (manageMySQL.checkTable(oldDigilockTable)) {
 							G333Messages.showInfo("Upgrade " + oldDigilockTable
 									+ " to " + digilockTable + ".");
 							query = "CREATE TABLE "
 									+ digilockTable
 									+ " (x INT, y INT, z INT, world VARCHAR(255), "
 									+ "owner VARCHAR(255), pincode VARCHAR(255), "
 									+ " coowners VARCHAR(255), users VARCHAR(255), closetimer INT, "
 									+ "typeid INT, connectedto VARCHAR(255), usecost INT) "
 									+ "AS SELECT x, y, z, world, owner, pincode, "
 									+ "'none', coowners, closetimer, typeid, connectedto, usecost FROM "
 									+ oldDigilockTable + ";";
 						} else {
 							G333Messages.showInfo("Creating table "
 									+ digilockTable);
 							query = "CREATE TABLE "
 									+ digilockTable
 									+ " (x INT, y INT, z INT, world VARCHAR(255), owner VARCHAR(255), "
 									+ "pincode VARCHAR(255), coowners VARCHAR(255), users VARCHAR(255), closetimer INT, "
 									+ "typeid INT, connectedto VARCHAR(255), usecost INT);";
 						}
 						manageMySQL.createTable(query);
 					}
 
 					// Check BookshelfTable
 					if (!manageMySQL.checkTable(bitInventoryTable)) {
 						if (manageMySQL.checkTable(oldBitInventoryTable)) {
 							G333Messages.showInfo("Upgrade "
 									+ oldBitInventoryTable + " to "
 									+ bitInventoryTable + ".");
 							query = "CREATE TABLE "
 									+ bitInventoryTable
 									+ " (playername VARCHAR(255), x INT, y INT, z INT, world VARCHAR(255), "
 									+ "owner VARCHAR(255), "
 									+ "name VARCHAR(255), "
 									+ "coowners VARCHAR(255), "
 									+ "usecost INT, slotno INT, "
 									+ "itemstack_type INT, itemstack_amount INT, itemstack_durability INT) "
 									+ "AS SELECT plyername, x, y, z, world, owner, name, coowners, usecost, "
 									+ "itemstack_type, itemstack_amount, itemstack_durability FROM "
 									+ oldBitInventoryTable + ";";
 						} else {
 							G333Messages.showInfo("Creating table "
 									+ bitInventoryTable);
 							query = "CREATE TABLE "
 									+ bitInventoryTable
 									+ " (playername VARCHAR(255), x INT, y INT, z INT, world VARCHAR(255), "
 									+ "owner VARCHAR(255), "
 									+ "name VARCHAR(255), "
 									+ "coowners VARCHAR(255), "
 									+ "usecost INT, slotno INT, "
 									+ "itemstack_type INT, itemstack_amount INT, "
 									+ "itemstack_durability INT); ";
 						}
 						manageMySQL.createTable(query);
 					}
 
 					// Check BooksTable
 					if (!manageMySQL.checkTable(bookTable)) {
 						if (manageMySQL.checkTable(oldBookTable)) {
 							G333Messages.showInfo("Upgrade " + oldBookTable
 									+ " to " + bookTable + ".");
 							query = "CREATE TABLE "
 									+ bookTable
 									+ " (bookid INT, title TEXT,"
 									+ " author TEXT, coauthors TEXT, "
 									+ " numberofpages INT, pageno INT, bodytext TEXT,"
 									+ " mastercopy BOOLEAN, mastercopyid INT,"
 									+ " force BOOLEAN, moved BOOLEAN, copy BOOLEAN, usecost INT)"
 									+ " AS select bookid, title,"
 									+ " author, coauthors, "
 									+ " numberofpages, pageno, bodytext,"
 									+ " mastercopy, mastercopyid,"
 									+ " force, moved, copy, usecost FROM "
 									+ oldBookTable + ";";
 						} else {
 							G333Messages
 									.showInfo("Creating table " + bookTable);
 							query = "CREATE TABLE "
 									+ bookTable
 									+ " (bookid INT, title TEXT,"
 									+ " author TEXT, coauthors TEXT, "
 									+ " numberofpages INT, pageno INT, bodytext TEXT,"
 									+ " mastercopy BOOLEAN, mastercopyid INT,"
 									+ " force BOOLEAN, moved BOOLEAN, copy BOOLEAN, usecost INT);";
 						}
 						manageMySQL.createTable(query);
 					}
 				} else {
 					G333Messages.showError("MySQL connection failed");
 					G333Config.STORAGE_HOST = "SQLITE";
 				}
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			// SQLite
 			G333Messages.showInfo("SQLite Initializing");
 			// Declare SQLite handler
 			manageSQLite = new sqlCore(log, "[" + G333Plugin.PLUGIN_NAME + "]",
 					G333Plugin.PLUGIN_NAME, G333Plugin.PLUGIN_FOLDER);
 			// Initialize SQLite handler
 			manageSQLite.initialize();
 			// Check if the table exists, if it doesn't create it
 			String query = "";
 			String insert = "";
 			if (!manageSQLite.checkTable(digilockTable)) {
 				if (manageSQLite.checkTable(oldDigilockTable)) {
 					G333Messages.showInfo("Upgrade table " + oldDigilockTable
 							+ " to " + digilockTable + ".");
 					query = "CREATE TABLE "
 							+ digilockTable
 							+ " (x INTEGER, y INTEGER, z INTEGER, world TEXT, owner TEXT,"
 							+ " pincode TEXT, "
 							+ " coowners TEXT, users TEXT, closetimer INTEGER,"
 							+ " typeid INTEGER, connectedto TEXT, usecost INTEGER);";
 					insert = "insert into "
 							+ digilockTable
 							+ " (x, y, z, world, owner, pincode, "
 							+ "coowners, users, closetimer, usecost, connectedto, typeid) "
 							+ "select x, y, z, world, owner, pincode,"
 							+ "'none', coowners, closetimer, usecost, connectedto, typeid FROM "
 							+ oldDigilockTable + ";";
 					// G333Messages.showInfo("Create Table:" + query);
 					// G333Messages.showInfo("Insert:" + insert);
 					manageSQLite.createTable(query);
 					manageSQLite.insertQuery(insert);
 
 				} else {
 					G333Messages.showInfo("Creating table " + digilockTable);
 					query = "CREATE TABLE "
 							+ digilockTable
 							+ " (x INTEGER, y INTEGER, z INTEGER, world TEXT, owner TEXT,"
 							+ " pincode TEXT,"
 							+ " coowners TEXT, users TEXT, closetimer INTEGER,"
 							+ " typeid INTEGER, connectedto TEXT, usecost INTEGER);";
 					manageSQLite.createTable(query);
 				}
 			} else {
 				// G333Messages.showInfo(digilockTable + " exists.");
 			}
 
 			// Check BookshelfTable
 			if (!manageSQLite.checkTable(bitInventoryTable)) {
 				if (manageSQLite.checkTable(oldBitInventoryTable)) {
 					G333Messages.showInfo("Upgrade " + oldBitInventoryTable
 							+ " to " + bitInventoryTable + ".");
 					query = "CREATE TABLE "
 							+ bitInventoryTable
 							+ " (playername VARCHAR(255), x INT, y INT, z INT, world VARCHAR(255), "
 							+ "owner VARCHAR(255), "
 							+ "name VARCHAR(255), "
 							+ "coowners VARCHAR(255), "
 							+ "usecost INT, slotno INT, "
 							+ "itemstack_type INT, itemstack_amount INT, itemstack_durability INT); ";
 					insert = "insert into "
 							+ bitInventoryTable
 							+ " (playername, x, y, z, world, "
 							+ "owner, "
 							+ "name, "
 							+ "coowners, "
 							+ "usecost, slotno, "
 							+ "itemstack_type, itemstack_amount, itemstack_durability) "
 							+ "select playername, x, y, z, world, owner, name,"
 							+ "coowners, usecost, "
 							+ "itemstack_type, itemstack_amount, itemstack_durability FROM "
 							+ oldDigilockTable + ";";
 					manageSQLite.createTable(query);
 					manageSQLite.insertQuery(insert);
 				} else {
 					G333Messages
 							.showInfo("Creating table " + bitInventoryTable);
 					query = "CREATE TABLE "
 							+ bitInventoryTable
 							+ " (playername VARCHAR(255), x INT, y INT, z INT, world VARCHAR(255), "
 							+ "owner VARCHAR(255), "
 							+ "name VARCHAR(255), "
 							+ "coowners VARCHAR(255), "
 							+ "usecost INT, slotno int, "
 							+ "itemstack_type INT, itemstack_amount NT, itemstack_durability INT);";
 					manageSQLite.createTable(query);
 				}
 			} else {
 				// G333Messages.showInfo(bitInventoryTable + " exists.");
 			}
 
 			// Check BooksTable
 			if (!manageSQLite.checkTable(bookTable)) {
 				if (manageSQLite.checkTable(oldBookTable)) {
 					G333Messages.showInfo("Upgrade " + oldBookTable + " to "
 							+ bookTable + ".");
 					query = "CREATE TABLE "
 							+ bookTable
 							+ " (bookid INT, title TEXT,"
 							+ " author TEXT, coauthors TEXT, "
 							+ " numberofpages INT, pageno INT, bodytext TEXT,"
 							+ " mastercopy BOOLEAN, mastercopyid INT,"
 							+ " force BOOLEAN, moved BOOLEAN, copy BOOLEAN, usecost INT);";
 					insert = "insert into "
 							+ bookTable
 							+ " (bookid, title,"
 							+ " author, coauthors, "
 							+ " numberofpages, pageno, bodytext,"
 							+ " mastercopy, mastercopyid,"
 							+ " force, moved, copy, usecost) "
							+ "select bookid, title,"
 							+ " author, coauthors, "
 							+ " numberofpages, pageno, bodytext,"
 							+ " mastercopy, mastercopyid,"
 							+ " force, moved, copy, usecost FROM "
 							+ oldBookTable + ";";
 					manageSQLite.createTable(query);
 					manageSQLite.insertQuery(insert);
 				} else {
 					G333Messages.showInfo("Creating table " + bookTable);
 					query = "CREATE TABLE "
 							+ bookTable
 							+ " (bookid INT, title TEXT,"
 							+ " author TEXT, coauthors TEXT, "
 							+ " numberofpages INT, pageno INT, bodytext TEXT,"
 							+ " mastercopy BOOLEAN, mastercopyid INT,"
 							+ " force BOOLEAN, moved BOOLEAN, copy BOOLEAN, usecost INT);";
 
 					manageSQLite.createTable(query);
 				}
 			} else {
 				// G333Messages.showInfo(bookTable + " exists.");
 			}
 		}
 	}
 
 	// Playerdata
 	public static Map<Integer, String> holdingKey = new HashMap<Integer, String>();
 	public static Map<Integer, Integer> userno = new HashMap<Integer, Integer>();
 
 	public static void removeUserData(int id) {
 		if (userno.containsKey(id)) {
 			// DigiLock
 			holdingKey.remove(id);
 			userno.remove(id);
 		}
 	}
 
 	public static void addUserData(int id) {
 		if (!userno.containsKey(id)) {
 			// DigiLock
 			userno.put(id, new Integer(id));
 			holdingKey.put(id, "");
 		}
 	}
 
 	public static void setupBook() {
 		// THIS PREVENTS BOOK FROM STACKING
 		try {
 			boolean ok = false;
 			try {
 				// attempt to make books with different data values stack
 				// separately
 				Field field1 = net.minecraft.server.Item.class
 						.getDeclaredField("bs");
 				if (field1.getType() == boolean.class) {
 					field1.setAccessible(true);
 					field1.setBoolean(net.minecraft.server.Item.BOOK, true);
 					ok = true;
 				}
 			} catch (Exception e) {
 			}
 			if (!ok) {
 				// otherwise limit stack size to 1
 				Field field2 = net.minecraft.server.Item.class
 						.getDeclaredField("maxStackSize");
 				field2.setAccessible(true);
 				field2.setInt(net.minecraft.server.Item.BOOK, 1);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
