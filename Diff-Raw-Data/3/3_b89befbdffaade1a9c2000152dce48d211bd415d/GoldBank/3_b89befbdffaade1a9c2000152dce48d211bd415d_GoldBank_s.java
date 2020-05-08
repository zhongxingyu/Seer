 package net.amigocraft.GoldBank;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.PotionMeta;
 import org.bukkit.material.MaterialData;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 
 @SuppressWarnings("unused")
 public class GoldBank extends JavaPlugin implements Listener {
 	public static GoldBank plugin;
 	public final static Logger log = Logger.getLogger("Minecraft");
 	public static final String ANSI_RED = "\u001B[31m";
 	public static final String ANSI_GREEN = "\u001B[32m";
 	public static final String ANSI_WHITE = "\u001B[37m";
 	private String[] openPlayer = new String[256];
 	private String[] openingPlayer = new String[256];
 	private String[] openType = new String[256];
 	private int[] openWalletNo = new int[256];
 	private int nextIndex = 0;
 	public HashMap<String, Integer> shopLog = new HashMap<String, Integer>();
 	public String header = "########################## #\n# GoldBank Configuration # #\n########################## #";
 
 	@Override
 	public void onEnable(){
 
 		// autoupdate
 		if (getConfig().getBoolean("enable-auto-update")){
 			try {new AutoUpdate(this);}
 			catch (Exception e){e.printStackTrace();}
 		}
 
 		// submit metrics
 		if (getConfig().getBoolean("enable-metrics")){
 			try {
 				Metrics metrics = new Metrics(this);
 				metrics.start();
 			}
 			catch (IOException e) {log.warning("[GoldBank] Failed to submit statistics to Plugin Metrics");}
 		}
 
 		// register events and the plugin variable
 		getServer().getPluginManager().registerEvents(this, this);
 		GoldBank.plugin = this;
 
 		for (int i = 0; i < 256; i++){
 			openPlayer[i] = null;
 			openingPlayer[i] = null;
 			openType[i] = null;
 			openWalletNo[i] = -1;
 		}
 
 		// add the crafting recipe for wallets
 		ItemStack is = new ItemStack(Material.BOOK, 1);
 		ItemMeta meta = is.getItemMeta();
 		meta.setDisplayName("2Wallet");
 		is.setItemMeta(meta);
 		final ShapedRecipe walletRecipe1 = new ShapedRecipe(is);
 		walletRecipe1.shape("XXX", "LXL", "LLL");
 		walletRecipe1.setIngredient('L', Material.LEATHER);
 		getServer().addRecipe(walletRecipe1);
 		final ShapedRecipe walletRecipe2 = new ShapedRecipe(is);
 		walletRecipe2.shape("LXL", "LLL", "XXX");
 		walletRecipe2.setIngredient('L', Material.LEATHER);
 		getServer().addRecipe(walletRecipe2);
 
 		// create the data folders
 		this.getDataFolder().mkdir();
 		File invDir = new File(this.getDataFolder() + File.separator + "inventories");
 		invDir.mkdir();
 		File walletDir = new File(this.getDataFolder() + File.separator + "wallets");
 		walletDir.mkdir();
 
 		// check config values
 		ConfigCheck.check();
 
 		// create the variable storage file 
 		File file = new File(getDataFolder(), "filled.txt");
 		if (!(file.exists())){
 			try {
 				file.createNewFile();
 				PrintWriter pw = new PrintWriter(file);
 				pw.print("0");
 				pw.close();
 			}
 			catch (IOException e){
 				e.printStackTrace();
 			}
 		}
 
 		// create the plugin table if it does not exist and update older tables to take several updates into account
 		Connection conn = null;
 		Statement st = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 			conn = DriverManager.getConnection(dbPath);
 			st = conn.createStatement();
 			st.executeUpdate("CREATE TABLE IF NOT EXISTS chestdata (" +
 					"id INTEGER NOT NULL PRIMARY KEY," +
 					"username VARCHAR(20) NOT NULL," +
 					"world VARCHAR(100) NOT NULL," +
 					"x INTEGER NOT NULL," +
 					"y INTEGER NOT NULL," +
 					"z INTEGER NOT NULL," +
 					"sign BOOLEAN NOT NULL," +
 					"tier INTEGER NOT NULL)");
 			if (!colExists("chestdata", "sign")){
 				st.executeUpdate("ALTER TABLE chestdata ADD sign BOOLEAN DEFAULT 'false' NOT NULL");
 				st.executeUpdate("UPDATE chestdata SET y='y+1', sign='true'");
 			}
 			if (!colExists("chestdata", "tier"))
 				st.executeUpdate("ALTER TABLE chestdata ADD tier BOOLEAN DEFAULT '1' NOT NULL");
 			if (!colExists("chestdata", "world")){
 				String world = getServer().getWorlds().get(0).getName();
 				st.executeUpdate("ALTER TABLE chestdata ADD world VARCHAR(100) DEFAULT 'world' NOT NULL");
 				st.executeUpdate("UPDATE chestdata SET world = '" + world + "'");
 			}
 			st.executeUpdate("CREATE TABLE IF NOT EXISTS shops (" +
 					"id INTEGER NOT NULL PRIMARY KEY," +
 					"creator VARCHAR(20) NOT NULL," +
 					"world VARCHAR(100) NOT NULL," +
 					"x INTEGER NOT NULL," +
 					"y INTEGER NOT NULL," +
 					"z INTEGER NOT NULL," +
 					"material INTEGER," +
 					"data INTEGER NOT NULL," +
 					"buyamount INTEGER NOT NULL," +
 					"buyprice INTEGER NOT NULL," +
 					"sellamount INTEGER NOT NULL," +
 					"sellprice INTEGER NOT NULL," +
 					"admin BOOLEAN NOT NULL)");
 			st.executeUpdate("DROP TABLE IF EXISTS nbt");
 			if (!colExists("shops", "world")){
 				String world = getServer().getWorlds().get(0).getName();
 				st.executeUpdate("ALTER TABLE chestdata ADD world VARCHAR(100) DEFAULT 'world' NOT NULL");
 				st.executeUpdate("UPDATE chestdata SET world = '" + world + "'");
 			}
 			st.executeUpdate("CREATE TABLE IF NOT EXISTS shoplog (" +
 					"id INTEGER NOT NULL PRIMARY KEY," +
 					"shop INTEGER NOT NULL," +
 					"player VARCHAR(20) NOT NULL," +
 					"action INTEGER NOT NULL," +
 					"material INTEGER," +
 					"data INTEGER," +
 					"quantity INTEGER," +
 					"time INTEGER)");
 		}
 		catch (Exception e){
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				st.close();
 				conn.close();
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 		}
 		log.info(ANSI_GREEN + this + " has been enabled!" + ANSI_WHITE);
 	}
 	public void onDisable(){
 		log.info(ANSI_GREEN + "[GoldBank] " + ANSI_WHITE + "Please wait, purging variables...");
 		boolean first = true;
 		for (int i = 0; i < openingPlayer.length; i++){
 			if (openType[i] != null){
 				if (openType[i].equals("wallet")){
 					Player p = getServer().getPlayer(openingPlayer[i]);
 					if (p != null){
 						p.closeInventory();
 						p.sendMessage(ChatColor.RED + "Wallet automatically closed by reload");
 					}
 					openType[i] = null;
 					openingPlayer[i] = null;
 					openPlayer[i] = null;
 					openWalletNo = null;
 					if (first){
 						if (nextIndex > i)
 							nextIndex = i;
 						first = false;
 					}
 				}
 			}
 		}
 		log.info(ANSI_GREEN + this + " has been disabled!" + ANSI_WHITE);
 	}
 
 	// initiate function for detecting player clicking sign
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onClick(PlayerInteractEvent e){
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
 			// check if wallet is in hand
 			boolean wallet = false;
 			if (e.getPlayer().getItemInHand().getType() == Material.BOOK){
 				ItemStack is = e.getPlayer().getItemInHand();
 				ItemMeta meta = is.getItemMeta();
 				if (!(meta.getDisplayName() == null) && meta.getLore() != null){
 					if (meta.getLore().size() >= 4){
 						if (meta.getDisplayName().equals("2Wallet") && meta.getLore().get(3).equals("2GoldBank")){
 							// cancel the event because the item in hand is a wallet
 							e.setCancelled(true);
 							wallet = true;
 							boolean own = false;
 							if (meta.getLore().get(1).equals(e.getPlayer().getName()))
 								own = true;
 							String node = "goldbank.wallet.open";
 							if (own)
 								node = "goldbank.wallet.open.own";
 							if (e.getPlayer().hasPermission(node)){
 								String owner = meta.getLore().get(1);
 								String numLine = meta.getLore().get(2);
 								char[] chars = numLine.toCharArray();
 								int length = numLine.length();
 								String numStr = "";
 								for (int i = 10; i < length; i++){
 									numStr = numStr + Character.toString(chars[i]);
 								}
 								int num = Integer.parseInt(numStr);
 								File invF = new File(getDataFolder() + File.separator + "wallets", owner + ".inv");
 								if(invF.exists()){
 									YamlConfiguration invY = new YamlConfiguration();
 									try {
 										invY.load(invF);
 										if (invY.isSet(Integer.toString(num))){
 											Set<String> keys = invY.getKeys(false);
 											ItemStack[] invI = new ItemStack[this.getConfig().getInt("walletsize")];
 											for (int i = 0; i < invI.length; i++){
 												String key = Integer.toString(num) + "." + i;
 												invI[i] =  invY.getItemStack(key);
 											}
 											Inventory inv = this.getServer().createInventory(null, this.getConfig().getInt("walletsize"), owner + "'s Wallet - #" + numStr);
 											inv.setContents(invI);
 											e.getPlayer().openInventory(inv);
 											openPlayer[nextIndex] = owner;
 											openingPlayer[nextIndex] = e.getPlayer().getName();
 											openType[nextIndex] = "wallet";
 											openWalletNo[nextIndex] = num;
 											nextIndex += 1;
 										}
 										else {
 											e.getPlayer().sendMessage(ChatColor.RED + "Error: This wallet does not have an associated YAML configuration section. Attempting to create one...");
 											try {
 												invY.set(num + ".size", this.getConfig().getInt("walletsize"));
 												invY.save(invF);
 												e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Success!");
 											}
 											catch (Exception ex){ex.printStackTrace();}
 										}
 									}
 									catch (Exception ex){
 										ex.printStackTrace();
 										e.getPlayer().sendMessage(ChatColor.RED + "An error occurred while attempting to open this wallet.");
 									}
 								}
 								else {
 									e.getPlayer().sendMessage(ChatColor.RED + "Error: This wallet does not have an associated YAML file. Attempting to create one...");
 									try {
 										invF.createNewFile();
 										YamlConfiguration invY = new YamlConfiguration();
 										invY.load(invF);
 										invY.set(num + ".size", this.getConfig().getInt("walletsize"));
 										invY.save(invF);
 										e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Success!");
 									}
 									catch (Exception exc){
 										exc.printStackTrace();
 										String cookieMsg = "";
 										if (this.getConfig().getBoolean("give-cookie-when-wallet-creation-fails")){
 											cookieMsg = " Here's a cookie to make up for it. :)";
 											e.getPlayer().getInventory().addItem(new ItemStack(Material.COOKIE, 1));
 										}
 										try {
 											invF.createNewFile();
 										}
 										catch (Exception ex){
 											ex.printStackTrace();
 											e.getPlayer().sendMessage(ChatColor.RED + "An error occurred while attempting to add this wallet to the YAML configuration." + cookieMsg);
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK){
 			// check if player is checking shop
 			if (shopLog.containsKey(e.getPlayer().getName())){
 				if (shopLog.get(e.getPlayer().getName()) <= 0){
 					e.setCancelled(true);
 					shopLog.remove(e.getPlayer().getName());
 					if (e.getClickedBlock().getState() instanceof Sign){
 						Connection conn = null;
 						Statement st = null;
 						ResultSet rs = null;
 						try {
 							Class.forName("org.sqlite.JDBC");
 							String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 							conn = DriverManager.getConnection(dbPath);
 							st = conn.createStatement();
 							String world = e.getClickedBlock().getWorld().getName();
 							int x = e.getClickedBlock().getX();
 							int y = e.getClickedBlock().getY();
 							int z = e.getClickedBlock().getZ();
 							rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + world + "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
 							int count = 0;
 							while (rs.next()){
 								count = rs.getInt(1);
 							}
 							if (count != 0){
 								rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + world + "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
 								int shopId = rs.getInt("id");
 								shopLog.put(e.getPlayer().getName(), shopId);
 								rs = st.executeQuery("SELECT COUNT(*) FROM shoplog WHERE shop = '" + shopId + "' AND action < '2'");
 								int total = 0;
 								while (rs.next()){
 									total = rs.getInt(1);
 								}
 								if (total != 0){
 									int perPage = 10;
 									int pages = total / perPage;
 									if (pages * perPage != total)
 										pages += 1;
 									e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Page 1/" + pages);
 									rs = st.executeQuery("SELECT * FROM shoplog WHERE shop = '" + shopId + "' AND action < '2' ORDER BY id DESC");
 									for (int i = 1; i <= perPage; i++){
 										if (i <= total){
 											String action = "";
 											ChatColor actionColor = ChatColor.DARK_GREEN;
 											if (rs.getInt("action") == 0)
 												action = "bought";
 											else if (rs.getInt("action") == 1){
 												action = "sold";
 												actionColor = ChatColor.DARK_RED;
 											}
 											String data = "";
 											if (rs.getInt("data") > 0)
 												data = ":" + rs.getInt("data");
 											Calendar cal = Calendar.getInstance();
 											cal.setTimeInMillis((long)rs.getInt("time") * 1000);
 											String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
 											String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
 											String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
 											String min = Integer.toString(cal.get(Calendar.MINUTE));
 											String sec = Integer.toString(cal.get(Calendar.SECOND));
 											if (month.length() < 2)
 												month = "0" + month;
 											if (day.length() < 2)
 												day = "0" + day;
 											while (hour.length() < 2)
 												hour = "0" + hour;
 											while (min.length() < 2)
 												min = "0" + min;
 											while (sec.length() < 2)
 												sec = "0" + sec;
 											String dateStr = cal.get(Calendar.YEAR) + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
 											e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + Integer.toString(i) + ") " + ChatColor.DARK_AQUA + dateStr + " " + ChatColor.LIGHT_PURPLE + rs.getString("player") + " " + actionColor + action + " " + ChatColor.GOLD + rs.getInt("quantity") + " " + Material.getMaterial(rs.getInt("material")).toString() + data);
 											rs.next();
 										}
 										else
 											break;
 									}
 									if (pages > 1)
 										e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Type " + ChatColor.DARK_GREEN + "/gb shop log page 2 " + ChatColor.DARK_PURPLE + "to view the next page");
 								}
 								else
 									e.getPlayer().sendMessage(ChatColor.RED + "Error: The selected shop does not have any logged transactions!");
 							}
 							else {
 								e.getPlayer().sendMessage(ChatColor.RED + "Selected block is not a GoldShop! Operation aborted.");
 							}
 						}
 						catch (Exception ex){
 							ex.printStackTrace();
 						}
 						finally {
 							try {
 								conn.close();
 								st.close();
 								rs.close();
 							}
 							catch (Exception exc){
 								exc.printStackTrace();
 							}
 						}
 					}
 					else {
 						e.getPlayer().sendMessage(ChatColor.RED + "Selected block is not a GoldShop! Operation aborted.");
 					}
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onOneClick(PlayerInteractEvent e){
 		boolean wallet = false;
 		// check for right click
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
 			if (!wallet){
 				// check if clicked block is sign
 				if (e.getClickedBlock() != null){
 					if (e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST){
 						Player player = e.getPlayer();
 						String p = player.getName();
 						Sign sign = (Sign) e.getClickedBlock().getState();
 						String fline = sign.getLine(0);
 						if (fline.equalsIgnoreCase("2[GoldBank]")){
 							e.setCancelled(true);
 							if (player.hasPermission("goldbank.sign.bank.use")){
 								Connection conn = null;
 								ResultSet rs = null;
 								Statement st = null;
 								try {
 									Class.forName("org.sqlite.JDBC");
 									String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 									conn = DriverManager.getConnection(dbPath);
 									st = conn.createStatement();
 									String checkWorld = e.getClickedBlock().getWorld().getName();
 									int checkX = e.getClickedBlock().getX();
 									int checkY = e.getClickedBlock().getY();
 									int checkZ = e.getClickedBlock().getZ();
 									rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE world = '" + checkWorld + "' AND x = '" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "'");
 									int regcount = 0;
 									while (rs.next()){
 										regcount = rs.getInt(1);
 									}
 									boolean master = false;
 									rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE world = '" + checkWorld + "' AND x = '" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "' AND username = 'MASTER'");
 									int masterCount = 0;
 									while (rs.next()){
 										masterCount = rs.getInt(1);
 									}
 									if (masterCount != 0)
 										master = true;
 									rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE username = '" + p + "'");
 									int fpcount = 0;
 									while (rs.next()){
 										fpcount = rs.getInt(1);
 									}
 									if (regcount == 0 || (master && fpcount == 0)){
 										if (fpcount == 0){
 											int tier = 1;
 											if (sign.getLine(1).length() >= 6){
 												if (sign.getLine(1).substring(0, 6).equalsIgnoreCase("4Tier")){
 													if (isInt(sign.getLine(1).substring(7, 8))){
 														if (getConfig().isSet("tiers." + Integer.parseInt(sign.getLine(1).substring(7, 8)) + ".size")){
 															if (getConfig().isSet("tiers." + Integer.parseInt(sign.getLine(1).substring(7, 8)) + ".fee")){
 																tier = Integer.parseInt(sign.getLine(1).substring(7, 8));
 															}
 														}
 													}
 												}
 											}
 											int fee = getConfig().getInt("tiers." + Integer.toString(tier) + ".fee");
 											boolean free = false;
 											if (fee == 0 || player.hasPermission("goldbank.fee.bank.exempt"))
 												free = true;
 											ItemStack hand = player.getItemInHand();
 											if (hand.getType() == Material.GOLD_INGOT || free){
 												if (hand.getAmount() >= fee || free){
 													sign.setLine(2, "");
 													if (master)
 														sign.setLine(3, "dMaster");
 													else
 														sign.setLine(3, "5" + p);
 													sign.update();
 													int signX = sign.getX();
 													int signY = sign.getY();
 													int signZ = sign.getZ();
 													Location signLoc = new Location(player.getWorld(), signX, signY, signZ);
 													st.executeUpdate("INSERT INTO chestdata (username, world, x, y, z, sign, tier) VALUES ('" + p + "', '" + player.getWorld().getName() + "', '" + signX + "', '" + signY + "', '" + signZ + "', 'true', '" + tier + "')");
 													try {
 														File invF = new File(getDataFolder() + File.separator + "inventories", p + ".inv");
 														if (!invF.exists()){
 															invF.createNewFile();
 														}
 														YamlConfiguration invY = new YamlConfiguration();
 														invY.load(invF);
 														Inventory inv = this.getServer().createInventory(null, getConfig().getInt("tiers." + tier + ".size"), p + "'s GoldBank Sign");
 														invY.set("size", inv.getSize());
 														for (int i = 0; i < inv.getSize(); i++){
 															invY.set("" + i, inv.getItem(i));
 														}
 														invY.save(invF);
 													}
 													catch (Exception ex){
 														log.info("[GoldBank] WARNING: Couldn't save inventory for " + p);
 														ex.printStackTrace();
 													}
 													finally {
 														try {
 															conn.close();
 															st.close();
 															rs.close();
 														}
 														catch (Exception g){
 															g.printStackTrace();
 														}
 													}
 													if (!free){
 														ItemStack newstack = new ItemStack(Material.GOLD_INGOT, hand.getAmount() - fee);
 														player.getInventory().setItemInHand(newstack);
 														player.updateInventory();
 														player.sendMessage(ChatColor.DARK_PURPLE + "Charged " + Integer.toString(fee) + " golden ingots");
 													}
 													else {
 														player.sendMessage(ChatColor.DARK_PURPLE + "This one's on us!");
 													}
 													player.sendMessage(ChatColor.DARK_GREEN + "Thanks for registering!");
 												}
 												else {
 													player.sendMessage(ChatColor.RED + "You must have " + Integer.toString(fee) + " golden ingots to buy a Bank Sign!");
 												}
 											}
 											else {
 												player.sendMessage(ChatColor.RED + "You must have golden ingots in your hand to buy a Bank Sign!");
 											}
 										}
 										else {
 											player.sendMessage(ChatColor.RED + "You already have a sign!");
 										}
 									}
 									else {
 										if (player.hasPermission("goldbank.sign.bank.use")){
 											try {
 												rs.close();
 												rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE x = '" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "' AND username = '" + p + "'");
 												int pcount = 0;
 												while (rs.next()){
 													pcount = rs.getInt(1);
 												}
 												if (pcount == 1 || (player.hasPermission("goldbank.sign.bank.use.others") && !master)){
 													rs = st.executeQuery("SELECT * FROM chestdata WHERE username = '" + p + "'");
 													if (!master)
 														rs = st.executeQuery("SELECT * FROM chestdata WHERE world = '" + checkWorld + "' AND x = '" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "'");
 													String dp = rs.getString("username");
 													File invF = new File(getDataFolder() + File.separator + "inventories", dp + ".inv");
 													if(invF.exists()){
 														YamlConfiguration invY = new YamlConfiguration();
 														invY.load(invF);
 														int size = invY.getInt("size");
 														Set<String> keys = invY.getKeys(false);
 														ItemStack[] invI = new ItemStack[size];
 														for (String invN : keys){
 															if (!invN.equalsIgnoreCase("size")){
 																int i = Integer.parseInt(invN);
 																invI[i] =  invY.getItemStack(invN);
 															}
 														}
 														Inventory inv = this.getServer().createInventory(null, size, dp + "'s GoldBank Sign");
 														inv.setContents(invI);
 														player.openInventory(inv);
 														openPlayer[nextIndex] = dp;
 														openingPlayer[nextIndex] = p;
 														openType[nextIndex] = "bank";
 														nextIndex += 1;
 													}
 												}
 												else {
 													if (!master)
 														player.sendMessage(ChatColor.RED + "This Bank Sign does not belong to you!");
 													else
 														player.sendMessage(ChatColor.RED + "You have not registered a Bank Sign with this Master Sign!");
 												}
 											}
 											catch (Exception h){
 												h.printStackTrace();
 											}
 											finally {
 												try {
 													conn.close();
 													st.close();
 													rs.close();
 												}
 												catch (Exception u){
 													u.printStackTrace();
 												}
 											}
 										}
 									}
 								}
 								catch(Exception q){
 									q.printStackTrace();
 								}
 								finally {
 									try {
 										rs.close();
 										st.close();
 										conn.close();
 									}
 									catch (Exception g){
 										g.printStackTrace();
 									}
 								}
 							}
 							else {
 								player.sendMessage(ChatColor.RED + "Oh noes! You don't have permission to do this! :(");
 							}
 						}
 						else if (fline.equalsIgnoreCase("2[GoldATM]")){
 							e.setCancelled(true);
 							if (player.hasPermission("goldbank.sign.atm.use")){
 								int atmfee = getConfig().getInt("atmfee");
 								boolean enough = false;
 								boolean notzero = false;
 								if (atmfee != 0){
 									notzero = true;
 									Inventory pInv = player.getInventory();
 									int nuggets = getAmountInInv(pInv, Material.GOLD_NUGGET);
 									if (nuggets >= atmfee){
 										enough = true;
 									}
 								}
 								else {
 									enough = true;
 								}
 								if (player.hasPermission("goldbank.fee.atm.exempt")){
 									notzero = false;
 									enough = true;
 								}
 								Connection conn = null;
 								ResultSet rs = null;
 								Statement st = null;
 								try {
 									Class.forName("org.sqlite.JDBC");
 									String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 									conn = DriverManager.getConnection(dbPath);
 									st = conn.createStatement();
 									rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE username = '" + p + "'");
 									int count = 0;
 									while (rs.next()){
 										count = rs.getInt(1);
 									}
 									if (count == 1){
 										if (enough == true){
 											if (notzero == true){
 												removeFromPlayerInv(player, Material.GOLD_NUGGET, 0, atmfee);
 												player.sendMessage(ChatColor.DARK_PURPLE + "Charged " + atmfee + " golden nuggets");
 											}
 											else {
 												player.sendMessage(ChatColor.DARK_PURPLE + "This one's on us!");
 											}
 											File invF = new File(getDataFolder() + File.separator + "inventories", p + ".inv");
 											if(invF.exists()){
 												YamlConfiguration invY = new YamlConfiguration();
 												invY.load(invF);
 												int size = invY.getInt("size");
 												Set<String> keys = invY.getKeys(false);
 												ItemStack[] invI = new ItemStack[size];
 												for (String invN : keys){
 													if (!invN.equalsIgnoreCase("size")){
 														int i = Integer.parseInt(invN);
 														invI[i] =  invY.getItemStack(invN);
 													}
 												}
 												Inventory inv = this.getServer().createInventory(null, size, p + "'s GoldBank Sign");
 												inv.setContents(invI);
 												player.openInventory(inv);
 												openPlayer[nextIndex] = p;
 												openingPlayer[nextIndex] = p;
 												openType[nextIndex] = "bank";
 												nextIndex += 1;
 											}
 										}
 										else {
 											player.sendMessage(ChatColor.RED + "You don't have enough golden nuggets to use that!");
 										}
 									}
 									else {
 										player.sendMessage(ChatColor.RED + "You don't have a GoldBank Sign!");
 									}
 								}
 								catch (Exception f){
 									f.printStackTrace();
 								}
 								finally {
 									try {
 										conn.close();
 										st.close();
 										rs.close();
 									}
 									catch (Exception q){
 										q.printStackTrace();
 									}
 								}
 							}
 							else {
 								player.sendMessage(ChatColor.RED + "Oh noes! You don't have permission to do this! :(");
 							}
 						}
 						int i = 0;
 						Connection conn = null;
 						Statement st = null;
 						ResultSet rs = null;
 						try {
 							Class.forName("org.sqlite.JDBC");
 							String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 							conn = DriverManager.getConnection(dbPath);
 							st = conn.createStatement();
 							rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + e.getClickedBlock().getWorld().getName() + "' AND x = '" + e.getClickedBlock().getX() + "' AND y = '" + e.getClickedBlock().getY() + "' AND z = '" + e.getClickedBlock().getZ() + "'");
 							while (rs.next()){
 								i = rs.getInt(1);
 							}
 						}
 						catch (Exception q){
 							q.printStackTrace();
 						}
 						finally {
 							try {
 								conn.close();
 								st.close();
 								rs.close();
 							}
 							catch (Exception k){
 								k.printStackTrace();
 							}
 						}
 						if (i != 0){
 							if (player.hasPermission("goldbank.sign.shop.use")){
 								try {
 									Class.forName("org.sqlite.JDBC");
 									String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 									conn = DriverManager.getConnection(dbPath);
 									st = conn.createStatement();
 									rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + e.getClickedBlock().getWorld().getName() + "' AND x = '" + e.getClickedBlock().getX() + "' AND y = '" + e.getClickedBlock().getY() + "' AND z = '" + e.getClickedBlock().getZ() + "'");
 									String adminS = rs.getString("admin");
 									boolean admin = false;
 									if (adminS.equals("true"))
 										admin = true;
 									int shopId = rs.getInt("id");
 									int matId = rs.getInt("material");
 									Material mat = Material.getMaterial(matId);
 									String matName = mat.toString();
 									int dataValue = rs.getInt("data");
 									String forMatName = matName.toLowerCase().replace("_", " ");
 									int buyPrice = rs.getInt("buyprice");
 									int buyAmount = rs.getInt("buyamount");
 									int sellPrice = rs.getInt("sellprice");
 									int sellAmount = rs.getInt("sellamount");
 									ItemStack buyIs = new ItemStack(mat, buyAmount);
 									buyIs.setDurability((short)dataValue);
 									ItemStack sellIs = new ItemStack(mat, sellAmount);
 									sellIs.setDurability((short)dataValue);
 									Location chestLoc = new Location(e.getClickedBlock().getWorld(), e.getClickedBlock().getX(), (e.getClickedBlock().getY() - 1), e.getClickedBlock().getZ());
 									boolean valid = true;
 									if (chestLoc.getBlock().getType() != Material.CHEST && !admin)
 										valid = false;
 									if (valid){
 										Chest chest = null;
 										Inventory chestInv = null;
 										if (!admin){
 											chest = (Chest)chestLoc.getBlock().getState();
 											chestInv = chest.getInventory();
 										}
 										// buy
 										if (player.getItemInHand().getType() == Material.GOLD_BLOCK || player.getItemInHand().getType() == Material.GOLD_INGOT || player.getItemInHand().getType() == Material.GOLD_NUGGET){
 											e.setCancelled(true);
 											boolean enough = true;
 											if (chestInv != null)
 												if ((getAmountInInv(chestInv, mat) < buyAmount && !admin) || admin)
 													enough = false;
 											if (enough){
 												Inventory inv = player.getInventory();
 												int blocks = getAmountInInv(inv, Material.GOLD_BLOCK);
 												int ingots = getAmountInInv(inv, Material.GOLD_INGOT);
 												int nuggets = getAmountInInv(inv, Material.GOLD_NUGGET);
 												int totalblocks = (blocks * 81);
 												int totalingots = (ingots * 9);
 												int total = totalblocks + totalingots + nuggets;
 												total = total / 9;
 												if (total >= buyPrice){
 													if (getNullsInInv(inv) >= (buyAmount / 64) + 1){
 														int remaining = buyPrice;
 														int removeB = 0;
 														if (buyPrice >= 9 && getAmountInInv(inv, Material.GOLD_BLOCK) >= 1){
 															int remove = 0;
 															if (blocks >= remaining / 9){
 																remove = buyPrice / 9;
 															}
 															else
 																remove = blocks;
 															removeB = remove;
 															removeFromPlayerInv(player, Material.GOLD_BLOCK, 0, remove);
 															remaining = buyPrice - (remove / 9);
 														}
 														if (remaining >= 1 && getAmountInInv(inv, Material.GOLD_INGOT) >= 1){
 															int remove = 0;
 															if (ingots >= remaining){
 																remove = remaining;
 															}
 															else {
 																remove = ingots;
 															}
 															removeFromPlayerInv(player, Material.GOLD_INGOT, 0, remove);
 															remaining = remaining - remove;
 														}
 														else if (remaining >= 1){
 															removeFromPlayerInv(player, Material.GOLD_BLOCK, 0, 1);
 															inv.addItem(new ItemStack[] {
 																	new ItemStack(Material.GOLD_INGOT, 9 - remaining)});
 														}
 														if (remaining >= 1){
 															removeFromPlayerInv(player, Material.GOLD_NUGGET, 0, remaining * 9);
 														}
 														if (!admin){
 															removeFromInv(chestInv, buyIs.getType(), 0, buyIs.getAmount());
 															int newBlocks = buyPrice / 9;
 															int blockRemainder = buyPrice - newBlocks * 9;
 															int newIngots = blockRemainder;
 															ItemStack addBlocks = new ItemStack(Material.GOLD_BLOCK, newBlocks);
 															ItemStack addIngots = new ItemStack(Material.GOLD_INGOT, newIngots);
 															if (addBlocks.getAmount() != 0)
 																chestInv.addItem(new ItemStack[] {
 																		addBlocks});
 															if (addIngots.getAmount() != 0)
 																chestInv.addItem(new ItemStack[] {
 																		addIngots});
 															if (getAmountInInv(chestInv, Material.GOLD_INGOT) >= 9){
 																int extraIngots = getAmountInInv(chestInv, Material.GOLD_INGOT);
 																int blockNum = extraIngots / 9;
 																removeFromInv(chestInv, Material.GOLD_INGOT, 0, blockNum * 9);
 																chestInv.addItem(new ItemStack[] {
 																		new ItemStack(Material.GOLD_BLOCK, blockNum)});
 															}
 														}
 														inv.addItem(new ItemStack[] {buyIs});
 														player.updateInventory();
 														st.executeUpdate("INSERT INTO shoplog (shop, player, action, material, data, quantity, time) VALUES ('" + shopId + "', '" + player.getName() + "', '0', '" + mat.getId() + "', '" + dataValue + "', '" + buyIs.getAmount() + "', '" + System.currentTimeMillis() / 1000 + "')");
 														String buyPriceS = "s";
 														if (buyPrice == 1)
 															buyPriceS = "";
 														player.sendMessage(ChatColor.DARK_PURPLE + "You bought " + buyAmount + " " + forMatName + " for " + buyPrice + " golden ingot" + buyPriceS + "!");
 													}
 													else
 														player.sendMessage(ChatColor.RED + "Oh noes! You don't have enough open slots in your inventory!");
 												}
 												else
 													player.sendMessage(ChatColor.RED + "Oh noes! You don't have enough gold to buy that!");
 											}
 											else
 												player.sendMessage(ChatColor.RED + "Error: The associated chest does not have enough " + matName + "!");
 										}
 										// sell
 										else if (player.getItemInHand().getType() == mat){
 											e.setCancelled(true);
 											Material[] tools = new Material[]{
 													Material.DIAMOND_PICKAXE,
 													Material.DIAMOND_SWORD,
 													Material.DIAMOND_SPADE,
 													Material.DIAMOND_AXE,
 													Material.DIAMOND_HOE,
 													Material.DIAMOND_HELMET,
 													Material.DIAMOND_CHESTPLATE,
 													Material.DIAMOND_LEGGINGS,
 													Material.DIAMOND_BOOTS,
 													Material.IRON_PICKAXE,
 													Material.IRON_SWORD,
 													Material.IRON_SPADE,
 													Material.IRON_AXE,
 													Material.IRON_HOE,
 													Material.IRON_HELMET,
 													Material.IRON_CHESTPLATE,
 													Material.IRON_LEGGINGS,
 													Material.IRON_BOOTS,
 													Material.GOLD_PICKAXE,
 													Material.GOLD_SWORD,
 													Material.GOLD_SPADE,
 													Material.GOLD_AXE,
 													Material.GOLD_HOE,
 													Material.GOLD_HELMET,
 													Material.GOLD_CHESTPLATE,
 													Material.GOLD_LEGGINGS,
 													Material.GOLD_BOOTS,
 													Material.STONE_PICKAXE,
 													Material.STONE_SWORD,
 													Material.STONE_SPADE,
 													Material.STONE_AXE,
 													Material.STONE_HOE,
 													Material.CHAINMAIL_HELMET,
 													Material.CHAINMAIL_CHESTPLATE,
 													Material.CHAINMAIL_LEGGINGS,
 													Material.CHAINMAIL_BOOTS,
 													Material.WOOD_PICKAXE,
 													Material.WOOD_SWORD,
 													Material.WOOD_SPADE,
 													Material.WOOD_AXE,
 													Material.WOOD_HOE,
 													Material.LEATHER_HELMET,
 													Material.LEATHER_CHESTPLATE,
 													Material.LEATHER_LEGGINGS,
 													Material.LEATHER_BOOTS,
 													Material.FLINT_AND_STEEL,
 													Material.SHEARS,
 													Material.BOW,
 													Material.FISHING_ROD,
 													Material.ANVIL};
 											boolean newTool = true;
 											if (Arrays.asList(tools).contains(mat) && getConfig().getBoolean("selldamageditems") == false){
 												if (player.getItemInHand().getDurability() != 0){
 													newTool = false;
 												}
 											}
 											if (newTool){ 
 												boolean validSell = true;
 												if (!admin)
 													if (((getAmountInInv(chestInv, Material.GOLD_NUGGET)) + (getAmountInInv(chestInv, Material.GOLD_INGOT) * 9) + (getAmountInInv(chestInv, Material.GOLD_BLOCK) * 81)) / 9 < sellPrice)
 														validSell = false;
 												if (validSell){
 													Inventory inv = player.getInventory();
 													if (getAmountInInv(inv, mat) >= sellAmount){
 														removeFromPlayerInv(player, sellIs.getType(), sellIs.getDurability(), sellIs.getAmount());
 														if (!admin){
 															int remaining = sellPrice;
 															int removeB = 0;
 															int blocks = getAmountInInv(chestInv, Material.GOLD_BLOCK);
 															int ingots = getAmountInInv(chestInv, Material.GOLD_INGOT);
 															int nuggets = getAmountInInv(chestInv, Material.GOLD_NUGGET);
 															if (sellPrice >= 9 && blocks >= 1){
 																int remove = 0;
 																if (blocks >= remaining / 9){
 																	remove = sellPrice / 9;
 																}
 																else {
 																	remove = blocks;
 																}
 																removeB = remove;
 																removeFromInv(chestInv, Material.GOLD_BLOCK, 0, remove);
 																remaining = sellPrice - (remove / 9);
 															}
 															if (remaining >= 1 && ingots >= 1){
 																int remove = 0;
 																if (ingots >= remaining){
 																	remove = remaining;
 																}
 																else {
 																	remove = ingots;
 																}
 																removeFromInv(chestInv, Material.GOLD_INGOT, 0, remove);
 																remaining = remaining - remove;
 															}
 															else if (remaining >= 1){
 																removeFromInv(chestInv, Material.GOLD_BLOCK, 0, 1);
 																chestInv.addItem(new ItemStack[] {
 																		new ItemStack(Material.GOLD_INGOT, 9 - remaining)});
 															}
 															if (remaining >= 1){
 																removeFromInv(chestInv, Material.GOLD_NUGGET, 0, remaining * 9);
 															}
 															chestInv.addItem(new ItemStack[] {sellIs});
 														}
 														inv.addItem(new ItemStack[] {
 																new ItemStack(Material.GOLD_INGOT, sellPrice)});
 														player.updateInventory();
 														st.executeUpdate("INSERT INTO shoplog (shop, player, action, material, data, quantity, time) VALUES ('" + shopId + "', '" + player.getName() + "', '1', '" + mat.getId() + "', '" + dataValue + "', '" + sellIs.getAmount() + "', '" + System.currentTimeMillis() / 1000 + "')");
 														String sellAmountS = "s";
 														if (sellAmount == 1)
 															sellAmountS = "";
 														String sellPriceS = "s";
 														if (sellPrice == 1)
 															sellPriceS = "";
 														player.sendMessage(ChatColor.DARK_PURPLE + "You sold " + sellAmount + " " + forMatName + sellAmountS + " for " + sellPrice + " golden ingot" + sellPriceS + "!");
 													}
 													else
 														player.sendMessage(ChatColor.RED + "You do not have enough " + forMatName + "!");
 												}
 												else
 													player.sendMessage(ChatColor.RED + "Error: The associated chest does not have enough gold!");
 											}
 											else
 												player.sendMessage(ChatColor.RED + "You may not sell damaged tools!");
 										}
 										else
 											player.sendMessage(ChatColor.RED + "You must have gold or " + forMatName + " in your hand to use this sign!");
 									}
 									else {
 										player.sendMessage(ChatColor.RED + "Error: This player shop does not have an associated chest! Attempting to create one...");
 										if (chestLoc.getBlock().getType() == Material.AIR)
 											chestLoc.getBlock().setType(Material.CHEST);
 										else
 											player.sendMessage(ChatColor.RED + "Could not create the chest because the block is not air! Ask the shop owner to change the block below this sign to air.");
 									}
 								}
 								catch (Exception f){
 									f.printStackTrace();
 								}
 								finally {
 									try {
 										conn.close();
 										st.close();
 										rs.close();
 									}
 									catch (Exception t){
 										t.printStackTrace();
 									}
 								}
 							}
 							else
 								player.sendMessage(ChatColor.RED + "Oh noes! You don't have permission to use this sign! :(");
 						}
 					}
 				}
 			}
 		}
 		// check for left click
 		if (e.getAction() == Action.LEFT_CLICK_BLOCK){
 			if (e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST){
 				String blockWorld = e.getClickedBlock().getWorld().getName();
 				int blockX = e.getClickedBlock().getX();
 				int blockY = e.getClickedBlock().getY();
 				int blockZ = e.getClickedBlock().getZ();
 				Player player = e.getPlayer();
 				String p = player.getName();
 				Connection conn = null;
 				Statement st = null;
 				ResultSet rs = null;
 				try {
 					Class.forName("org.sqlite.JDBC");
 					String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 					conn = DriverManager.getConnection(dbPath);
 					st = conn.createStatement();
 					rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + blockY + "' AND z = '" + blockZ + "'");
 					int count = 0;
 					while (rs.next()){
 						count = rs.getInt(1);
 					}
 					boolean master = false;
 					rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + blockY + "' AND z = '" + blockZ + "' AND username = 'MASTER'");
 					int masterCount = 0;
 					while (rs.next()){
 						masterCount = rs.getInt(1);
 					}
 					if (masterCount != 0)
 						master = true;
 					// check if a sign is registered at the same location
 					if (count != 0){
 						rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + blockY + "' AND z = '" + blockZ + "' AND username = '" + p + "'");
 						int newcount = 0;
 						while (rs.next()){
 							newcount = rs.getInt(1);
 						}
 						// verify that user has proper permissions
 						if (player.hasPermission("goldbank.sign.bank.unclaim")){
 							// check if player owns sign at location or if they have proper permissions to unclaim the signs of others
 							if (newcount != 0 || player.hasPermission("goldbank.sign.bank.unclaim.others")){
 								rs = st.executeQuery("SELECT * FROM chestdata WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + blockY + "' AND z = '" + blockZ + "'");
 								String dp = rs.getString("username");
 								if (master)
 									dp = p;
 								Location signLoc = new Location(e.getClickedBlock().getWorld(), blockX, blockY, blockZ);
 								if (signLoc.getBlock().getType() == Material.WALL_SIGN || signLoc.getBlock().getType() == Material.SIGN_POST){
 									Sign sign = (Sign)signLoc.getBlock().getState();
 									if (!master){
 										sign.setLine(2, "5Claim this");
 										sign.setLine(3, "5sign!");
 									}
 									sign.update();
 									// check if sign is master or if player owns sign at location
 									if (!master || newcount != 0){
 										e.setCancelled(true);
 										st.executeUpdate("DELETE FROM chestdata WHERE username = '" + dp + "'");
 										File file = new File(this.getDataFolder() + File.separator + "inventories" + File.separator + dp + ".inv");
 										World world = player.getWorld();
 										YamlConfiguration invY = new YamlConfiguration();
 										invY.load(file);
 										Set<String> keys = invY.getKeys(false);
 										for (String invN : keys){
 											if (!invN.equalsIgnoreCase("size")){
 												world.dropItem(player.getLocation(), invY.getItemStack(invN));
 											}
 										}
 										file.delete();
 										player.sendMessage(ChatColor.DARK_PURPLE + "Bank Sign unclaimed!");
 									}
 									else if (player.hasPermission("goldbank.sign.bank.destroy.master")){
 										rs = st.executeQuery("SELECT * FROM chestdata WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + blockY + "' AND z = '" + blockZ + "'");
 										while (rs.next()){
 											String owner = rs.getString("username");
 											File file = new File(this.getDataFolder() + File.separator + "inventories" + File.separator + owner + ".inv");
 											file.delete();
 										}
 										st.executeUpdate("DELETE FROM chestdata WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + blockY + "' AND z = '" + blockZ + "'");
 										player.sendMessage(ChatColor.DARK_PURPLE + "Master sign destroyed!");
 									}
 								}
 							}
 							else {
 								if (!master)
 									player.sendMessage(ChatColor.RED + "This Bank Sign does not belong to you!");
 								else
 									player.sendMessage(ChatColor.RED + "You have not registered a Bank Sign with this Master Sign!");
 							}
 						}
 						else {
 							player.sendMessage(ChatColor.RED + "Oh noes! You don't have permission to unclaim this!");
 						}
 					}
 				}
 				catch (Exception f){
 					f.printStackTrace();
 				}
 				finally {
 					try {
 						conn.close();
 						st.close();
 						rs.close();
 					}
 					catch (Exception q){
 						q.printStackTrace();
 					}
 				}
 			}
 			if (e.getClickedBlock().getType() == Material.CHEST){
 				String blockWorld = e.getClickedBlock().getWorld().getName();
 				int blockX = e.getClickedBlock().getX();
 				int blockY = e.getClickedBlock().getY();
 				int blockZ = e.getClickedBlock().getZ();
 				Player player = e.getPlayer();
 				String p = player.getName();
 				Connection conn = null;
 				Statement st = null;
 				ResultSet rs = null;
 				try {
 					Class.forName("org.sqlite.JDBC");
 					String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 					conn = DriverManager.getConnection(dbPath);
 					st = conn.createStatement();
 					rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + (blockY + 1) + "' AND z = '" + blockZ + "' AND admin = 'false'");
 					int count = 0;
 					while (rs.next()){
 						count = rs.getInt(1);
 					}
 					if (count == 1){
 						e.setCancelled(true);
 						rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE x = '" + blockX + "' AND y = '" + (blockY + 1) + "' AND z = '" + blockZ + "' AND creator = '" + p + "' AND admin = 'false'");
 						int newcount = 0;
 						while (rs.next()){
 							newcount = rs.getInt(1);
 						}
 						if (newcount > 0 || player.hasPermission("goldbank.sign.shop.destroy.*")){
 							player.sendMessage(ChatColor.RED + "Please left-click the Shop sign to destroy your shop!");
 						}
 						else {
 							player.sendMessage(ChatColor.RED + "That chest is part of a player shop!!");
 						}
 					}
 				}
 				catch (Exception f){
 					f.printStackTrace();
 				}
 				finally {
 					try {
 						conn.close();
 						st.close();
 						rs.close();
 					}
 					catch (Exception u){
 						u.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	// check if destroyed block is or holds GoldBank sign
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent b){
 		if (b.getBlock().getType() == Material.WALL_SIGN || b.getBlock().getType() == Material.SIGN_POST){
 			Sign sign = (Sign)b.getBlock().getState();
 			Block adjBlock = null;
 			if (getAdjacentBlock(b.getBlock(), Material.WALL_SIGN) != null){
 				adjBlock = getAdjacentBlock(b.getBlock(), Material.WALL_SIGN);
 			}
 			else if (getAdjacentBlock(b.getBlock(), Material.SIGN_POST)!= null){
 				adjBlock = getAdjacentBlock(b.getBlock(), Material.SIGN_POST);
 			}
 			Connection conn = null;
 			Statement st = null;
 			ResultSet rs = null;
 			try {
 				Class.forName("org.sqlite.JDBC");
 				String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 				conn = DriverManager.getConnection(dbPath);
 				st = conn.createStatement();
 				rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + b.getBlock().getWorld().getName() + "' AND x = '" + b.getBlock().getX() + "' AND y = '" + b.getBlock().getY() + "' AND z = '" + b.getBlock().getZ() + "'");
 				int i = 0;
 				while (rs.next())
 					i = rs.getInt(1);
 				if (i != 0){
 					rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + b.getBlock().getWorld().getName() + "' AND x = '" + b.getBlock().getX() + "' AND y = '" + b.getBlock().getY() + "' AND z = '" + b.getBlock().getZ() + "'");
 					int shopId = rs.getInt("id");
 					String admin = rs.getString("admin");
 					rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + b.getBlock().getWorld().getName() + "' AND x = '" + b.getBlock().getX() + "' AND y = '" + b.getBlock().getY() + "' AND z = '" + b.getBlock().getZ() + "' AND creator = '" + b.getPlayer().getName() + "'");
 					i = 0;
 					while (rs.next())
 						i = rs.getInt(1);
 					if (i != 0){
 						if (!b.getPlayer().hasPermission("goldbank.sign.shop.destroy")){
 							b.setCancelled(true);
 							b.getPlayer().sendMessage(ChatColor.RED +"Oh noes! You don't have permission to break that block! :(");
 						}
 						else {
 							st.executeUpdate("DELETE FROM shops WHERE world = '" + b.getBlock().getWorld().getName() + "' AND x ='" + b.getBlock().getX() + "' AND y = '" + b.getBlock().getY() + "' AND z = '" + b.getBlock().getZ() + "'");
 							st.executeUpdate("INSERT INTO shoplog (shop, player, action, time) VALUES ('" + shopId + "', '" + b.getPlayer().getName() + "', '3', '" + System.currentTimeMillis() / 1000 + "')");
 							b.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "GoldShop successfully unregistered!");
 							if (admin.equalsIgnoreCase("false")){
 								Location chestLoc = new Location(b.getBlock().getWorld(), b.getBlock().getX(), (b.getBlock().getY() - 1), b.getBlock().getZ());
 								chestLoc.getBlock().setType(Material.AIR);
 							}
 						}
 					}
 					else {
 						if (!b.getPlayer().hasPermission("goldbank.sign.shop.destroy.*")){
 							b.setCancelled(true);
 							b.getPlayer().sendMessage(ChatColor.RED +"Oh noes! You don't have permission to break that block! :(");
 						}
 						else {
 							st.executeUpdate("DELETE FROM shops WHERE world = '" + b.getBlock().getWorld().getName() + "' AND x ='" + b.getBlock().getX() + "' AND y = '" + b.getBlock().getY() + "' AND z = '" + b.getBlock().getZ() + "'");
 							st.executeUpdate("INSERT INTO shoplog (shop, player, action, time) VALUES ('" + shopId + "', '" + b.getPlayer().getName() + "', '3', '" + System.currentTimeMillis() / 1000 + "')");
 							b.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "GoldShop successfully unregistered!");
 							if (admin.equalsIgnoreCase("false")){
 								Location chestLoc = new Location(b.getBlock().getWorld(), b.getBlock().getX(), (b.getBlock().getY() - 1), b.getBlock().getZ());
 								chestLoc.getBlock().setType(Material.AIR);
 							}
 						}
 					}
 				}
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 			finally {
 				try {
 					conn.close();
 					st.close();
 					rs.close();
 				}
 				catch (Exception u){
 					u.printStackTrace();
 				}
 			}
 			if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]")){
 				boolean master = false;
 				conn = null;
 				st = null;
 				rs = null;
 				try {
 					Class.forName("org.sqlite.JDBC");
 					String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 					conn = DriverManager.getConnection(dbPath);
 					st = conn.createStatement();
 					rs = st.executeQuery("SELECT * FROM chestdata WHERE world = '" + b.getBlock().getWorld().getName() + "' AND x = '" + b.getBlock().getX() + "' AND y = '" + b.getBlock().getY() + "' AND z = '" + b.getBlock().getZ() + "' AND username = 'MASTER'");
 					int masterCount = 0;
 					while (rs.next()){
 						masterCount = rs.getInt(1);
 					}
 					if (masterCount != 0)
 						master = true;
 				}
 				catch (Exception ex){
 					ex.printStackTrace();
 				}
 				finally {
 					try {
 						conn.close();
 						st.close();
 						rs.close();
 					}
 					catch (Exception e){
 						e.printStackTrace();
 					}
 				}
 				String node = "goldbank.sign.bank.destroy";
 				if (master)
 					node = "goldbank.sign.bank.destroy.master";
 				if (!b.getPlayer().hasPermission(node)){
 					b.setCancelled(true);
 					b.getPlayer().sendMessage(ChatColor.RED + "Oh noes! You don't have permission to break that sign! :(");
 				}
 			}
 			else if (sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 				if (!b.getPlayer().hasPermission("goldbank.sign.atm.destroy")){
 					b.setCancelled(true);
 					b.getPlayer().sendMessage(ChatColor.RED + "Oh noes! You don't have permission to break that sign! :(");
 				}
 			}
 		}
 		if (getAdjacentBlock(b.getBlock(), Material.WALL_SIGN) != null || getAdjacentBlock(b.getBlock(), Material.SIGN_POST) != null){
 			Block adjBlock = null;
 			if (getAdjacentBlock(b.getBlock(), Material.WALL_SIGN) != null){
 				adjBlock = getAdjacentBlock(b.getBlock(), Material.WALL_SIGN);
 			}
 			else if (getAdjacentBlock(b.getBlock(), Material.SIGN_POST)!= null){
 				adjBlock = getAdjacentBlock(b.getBlock(), Material.SIGN_POST);
 			}
 			Sign sign = (Sign)adjBlock.getState();
 			Connection conn = null;
 			Statement st = null;
 			ResultSet rs = null;
 			try {
 				Class.forName("org.sqlite.JDBC");
 				String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 				conn = DriverManager.getConnection(dbPath);
 				st = conn.createStatement();
 				rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + adjBlock.getWorld().getName() + "' AND x = '" + adjBlock.getX() + "' AND y = '" + adjBlock.getY() + "' AND z = '" + adjBlock.getZ() + "'");
 				String admin = "false";
 				try {admin = rs.getString("admin");}
 				catch (Exception e){}
 				int i = 0;
 				while (rs.next())
 					i = rs.getInt(1);
 				if (i != 0){
 					rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + adjBlock.getWorld().getName() + "' AND x = '" + adjBlock.getX() + "' AND y = '" + adjBlock.getY() + "' AND z = '" + adjBlock.getZ() + "'");
 					int shopId = rs.getInt("id");
 					rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + adjBlock.getWorld().getName() + "' AND x = '" + adjBlock.getX() + "' AND y = '" + adjBlock.getY() + "' AND z = '" + adjBlock.getZ() + "' AND creator = '" + b.getPlayer().getName() + "'");
 					i = 0;
 					while (rs.next())
 						i = rs.getInt(1);
 					if (i != 0 || b.getPlayer().hasPermission("goldbank.sign.shop.destroy.*")){
 						if (!b.getPlayer().hasPermission("goldbank.sign.shop.destroy")){
 							b.setCancelled(true);
 							b.getPlayer().sendMessage(ChatColor.RED +"Oh noes! You don't have permission to break that block! :(");
 						}
 						else {
 							st.executeUpdate("DELETE FROM shops WHERE world = '" + adjBlock.getWorld().getName() + "' AND x ='" + adjBlock.getX() + "' AND y = '" + adjBlock.getY() + "' AND z = '" + adjBlock.getZ() + "'");
 							st.executeUpdate("INSERT INTO shoplog (shop, player, action, time) VALUES ('" + shopId + "', '" + b.getPlayer().getName() + "', '3', '" + System.currentTimeMillis() / 1000 + "')");
 							b.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "GoldShop successfully unregistered!");
 							if (admin.equalsIgnoreCase("false")){
 								Location chestLoc = new Location(adjBlock.getWorld(), adjBlock.getX(), (adjBlock.getY() - 1), adjBlock.getZ());
 								chestLoc.getBlock().setType(Material.AIR);
 							}
 						}
 					}
 					else {
 						if (!b.getPlayer().hasPermission("goldbank.sign.shop.destroy.*")){
 							b.setCancelled(true);
 							b.getPlayer().sendMessage(ChatColor.RED +"Oh noes! You don't have permission to break that block! :(");
 						}
 						else {
 							st.executeUpdate("DELETE FROM shops WHERE world = '" + adjBlock.getWorld().getName() + "' AND x ='" + adjBlock.getX() + "' AND y = '" + adjBlock.getY() + "' AND z = '" + adjBlock.getZ() + "'");
 							st.executeUpdate("INSERT INTO shoplog (shop, player, action, time) VALUES ('" + shopId + "', '" + b.getPlayer().getName() + "', '3', '" + System.currentTimeMillis() / 1000 + "')");
 							b.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "GoldShop successfully unregistered!");
 							if (admin.equalsIgnoreCase("false")){
 								Location chestLoc = new Location(b.getBlock().getWorld(), b.getBlock().getX(), (b.getBlock().getY() - 1), b.getBlock().getZ());
 								chestLoc.getBlock().setType(Material.AIR);
 							}
 						}
 					}
 				}
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 			finally {
 				try {
 					conn.close();
 					st.close();
 					rs.close();
 				}
 				catch (Exception u){
 					u.printStackTrace();
 				}
 			}
 			if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]")){
 				if (!b.getPlayer().hasPermission("goldbank.sign.bank.destroy")){
 					b.setCancelled(true);
 					b.getPlayer().sendMessage(ChatColor.RED +"Oh noes! You don't have permission to break that block! :(");
 				}
 				else {
 					try {
 						Class.forName("org.sqlite.JDBC");
 						String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 						conn = DriverManager.getConnection(dbPath);
 						st = conn.createStatement();
 						String checkWorld = adjBlock.getWorld().getName();
 						int checkX = adjBlock.getX();
 						int checkY = adjBlock.getY();
 						int checkZ = adjBlock.getZ();
 						rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE world = '" + checkWorld + "' AND x = '" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "'");
 						int i = 0;
 						while (rs.next()){
 							i = rs.getInt(1);
 						}
 						if (i > 0){
 							int masterCount = 0;
 							rs = st.executeQuery("SELECT COUNT(*) FROM chestdata WHERE world = '" + checkWorld + "' AND x = '" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "' AND username = 'MASTER'");
 							while (rs.next()){
 								masterCount = rs.getInt(1);
 							}
 							boolean master = false;
 							if (masterCount != 0){
 								master = true;
 								rs = st.executeQuery("SELECT * FROM chestdata WHERE world = '" + checkWorld + "' AND x = '" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "'");
 								while (rs.next()){
 									String owner = rs.getString("username");
 									File file = new File(this.getDataFolder() + File.separator + "inventories" + File.separator + owner + ".inv");
 									file.delete();
 								}
 								b.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Master Sign Unregistered!");
 							}
 							else {
 								b.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "GoldBank sign unregistered!");
 							}
 							if (!master || b.getPlayer().hasPermission("goldbank.sign.bank.destroy.master")){
 								st.executeUpdate("DELETE FROM chestdata WHERE world = '" + checkWorld + "' AND x ='" + checkX + "' AND y = '" + checkY + "' AND z = '" + checkZ + "'");
 							}
 						}
 					}
 					catch (Exception e){
 						e.printStackTrace();
 					}
 					finally {
 						try {
 							conn.close();
 							st.close();
 							rs.close();
 						}
 						catch (Exception u){
 							u.printStackTrace();
 						}
 					}
 				}
 			}
 			else if (sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 				if (!b.getPlayer().hasPermission("goldbank.sign.atm.destroy")){
 					b.setCancelled(true);
 					b.getPlayer().sendMessage(ChatColor.RED + "Oh noes! You don't have permission to break that block! :(");
 				}
 			}
 		}
 		else if (getAdjacentBlock(b.getBlock(), Material.SIGN_POST) != null){
 			Block adjblock = getAdjacentBlock(b.getBlock(), Material.SIGN_POST);
 			Sign sign = (Sign)adjblock.getState();
 			if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]")){
 				if (!b.getPlayer().hasPermission("goldbank.sign.bank.destroy")){
 					b.setCancelled(true);
 					b.getPlayer().sendMessage(ChatColor.RED + "Oh noes! You don't have permission to break that block! :(");
 				}
 			}
 			else if (sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 				if (!b.getPlayer().hasPermission("goldbank.sign.atm.destroy")){
 					b.setCancelled(true);
 					b.getPlayer().sendMessage(ChatColor.RED + "Oh noes! You don't have permission to break that block! :(");
 				}
 			}
 		}
 		if (b.getBlock().getType() == Material.CHEST){
 			String blockWorld = b.getBlock().getWorld().getName();
 			int blockX = b.getBlock().getX();
 			int blockY = b.getBlock().getY();
 			int blockZ = b.getBlock().getZ();
 			Player player = b.getPlayer();
 			String p = player.getName();
 			Connection conn = null;
 			Statement st = null;
 			ResultSet rs = null;
 			try {
 				Class.forName("org.sqlite.JDBC");
 				String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 				conn = DriverManager.getConnection(dbPath);
 				st = conn.createStatement();
 				rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + blockWorld + "' AND x = '" + blockX + "' AND y = '" + (blockY + 1) + "' AND z = '" + blockZ + "' AND admin = 'false'");
 				int count = 0;
 				while (rs.next()){
 					count = rs.getInt(1);
 				}
 				if (count == 1){
 					b.setCancelled(true);
 					rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE x = '" + blockX + "' AND y = '" + (blockY + 1) + "' AND z = '" + blockZ + "' AND creator = '" + p + "' AND admin = 'false'");
 					int newcount = 0;
 					while (rs.next()){
 						newcount = rs.getInt(1);
 					}
 					if (newcount > 0 || player.hasPermission("goldbank.sign.shop.destroy.*")){
 						player.sendMessage(ChatColor.RED + "Please left-click the Shop sign to destroy this shop!");
 					}
 					else {
 						player.sendMessage(ChatColor.RED + "That chest is part of a player shop!!");
 					}
 				}
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 			finally {
 				try {
 					conn.close();
 					st.close();
 					rs.close();
 				}
 				catch (Exception u){
 					u.printStackTrace();
 				}
 			}
 		}
 	}
 
 	// listen for block place event below player shop
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent c){
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 			conn = DriverManager.getConnection(dbPath);
 			st = conn.createStatement();
 			rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + c.getBlock().getWorld().getName() + "' AND x = '" + c.getBlock().getX() + "' AND y = '" + (c.getBlock().getY() + 1) + "' AND z = '" + c.getBlock().getZ() + "' AND admin = 'false'");
 			int i = 0;
 			while (rs.next()){
 				i = rs.getInt(1);
 			}
 			if (i > 0){
 				ResultSet res = st.executeQuery("SELECT * FROM shops WHERE world = '" + c.getBlock().getWorld().getName() + "' AND x = '" + c.getBlock().getX() + "' AND y = '" + (c.getBlock().getY() + 1) + "' AND z = '" + c.getBlock().getZ() + "' AND admin = 'false'");
 				String creator = res.getString("creator");
 				if (!creator.equalsIgnoreCase(c.getPlayer().getName())){
 					c.setCancelled(true);
 					c.getPlayer().sendMessage(ChatColor.RED + "This spot is owned by " + creator + "!");
 				}
 			}
 		}
 		catch (Exception e){
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				conn.close();
 				st.close();
 				rs.close();
 			}
 			catch (Exception u){
 				u.printStackTrace();
 			}
 		}
 	}
 
 	// listen for chest open
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onChestOpen(PlayerInteractEvent o){
 		if (o.getAction() == Action.RIGHT_CLICK_BLOCK){
 			if (o.getClickedBlock().getType() == Material.CHEST){
 				Player player = o.getPlayer();
 				String p = player.getName();
 				Chest chest = (Chest) o.getClickedBlock().getState();
 				String chestWorld = chest.getBlock().getWorld().getName();
 				int chestX = chest.getBlock().getX();
 				int chestY = chest.getBlock().getY();
 				int chestZ = chest.getBlock().getZ();
 				Connection conn = null;
 				Statement st = null;
 				ResultSet rs = null;
 				try {
 					Class.forName("org.sqlite.JDBC");
 					String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 					conn = DriverManager.getConnection(dbPath);
 					st = conn.createStatement();
 					rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + chestWorld + "' AND x = '" + chestX + "' AND y = '" + (chestY + 1) + "' AND z = '" + chestZ + "'");
 					int count = 0;
 					while (rs.next()){
 						count = rs.getInt(1);
 					}
 					if (count == 1){
 						rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + chestWorld + "' AND x = '" + chestX + "' AND y = '" + (chestY + 1) + "' AND z = '" + chestZ + "' AND creator = '" + p + "'");
 						int seccount = 0;
 						while (rs.next()){
 							seccount = rs.getInt(1);
 						}
 						if (seccount == 0){
 							if (!o.getPlayer().hasPermission("goldbank.sign.shop.access")){
 								o.setCancelled(true);
 								player.sendMessage(ChatColor.RED + "You don't have permission to open that GoldShop chest!");
 							}
 						}
 					}
 				}
 				catch (Exception e){
 					e.printStackTrace();
 				}
 				finally {
 					try {
 						conn.close();
 						st.close();
 						rs.close();
 					}
 					catch (Exception u){
 						u.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	// watch out for TNT and creepers
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onExplosion(EntityExplodeEvent e){
 		List<Block> blocks = e.blockList();
 		Iterator<Block> it = blocks.iterator();
 		while (it.hasNext()){
 			Block block = it.next();
 			if (block.getType() == Material.WALL_SIGN){
 				Sign sign = (Sign)block.getState();
 				if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]") || sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 					it.remove();
 				}
 				String line = sign.getLine(0);
 				String rline = line.replace("[", "");
 				rline = rline.replace("]", "");
 				rline = rline.replace("2", "");
 				rline = rline.toUpperCase();
 				String[] matInfo = new String[2];
 				String data = null;
 				if (rline.contains(":")){
 					matInfo = rline.split(":");
 					rline = matInfo[0];
 					data = matInfo[1];
 				}
 				boolean isValidInt = false;
 				if (isInt(rline)){
 					if (isMat(Integer.parseInt(rline))){
 						isValidInt = true;
 					}
 				}
 				if (isMat(rline) || isValidInt){
 					it.remove();
 				}
 			}
 			else if (block.getType() == Material.SIGN_POST){
 				Sign sign = (Sign)block.getState();
 				if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]") || sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 					it.remove();
 				}
 				String line = sign.getLine(0);
 				String rline = line.replace("[", "");
 				rline = rline.replace("]", "");
 				rline = rline.replace("2", "");
 				rline = rline.toUpperCase();
 				String[] matInfo = new String[2];
 				String data = null;
 				if (rline.contains(":")){
 					matInfo = rline.split(":");
 					rline = matInfo[0];
 					data = matInfo[1];
 				}
 				boolean isValidInt = false;
 				if (isInt(rline)){
 					if (isMat(Integer.parseInt(rline))){
 						isValidInt = true;
 					}
 				}
 				if (isMat(rline) || isValidInt){
 					it.remove();
 				}
 			}
 			else if (getAdjacentBlock(block, Material.WALL_SIGN) != null || getAdjacentBlock(block, Material.SIGN_POST) != null){
 				Block adjBlock = null;
 				if (getAdjacentBlock(block, Material.WALL_SIGN) != null){
 					adjBlock = getAdjacentBlock(block, Material.WALL_SIGN);
 				}
 				else if (getAdjacentBlock(block, Material.SIGN_POST) != null){
 					adjBlock = getAdjacentBlock(block, Material.SIGN_POST);
 				}
 				Sign sign = (Sign)adjBlock.getState();
 				if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]")){
 					it.remove();
 				}
 				else if (sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 					it.remove();
 				}
 				if (adjBlock != null || block.getType() == Material.CHEST){
 					String line = ((Sign)adjBlock.getState()).getLine(0);
 					String rline = line.replace("[", "");
 					rline = rline.replace("]", "");
 					rline = rline.replace("2", "");
 					rline = rline.toUpperCase();
 					String[] matInfo = new String[2];
 					String data = null;
 					if (rline.contains(":")){
 						matInfo = rline.split(":");
 						rline = matInfo[0];
 						data = matInfo[1];
 					}
 					boolean isValidInt = false;
 					if (isInt(rline)){
 						if (isMat(Integer.parseInt(rline))){
 							isValidInt = true;
 						}
 					}
 					if (isMat(rline) || isValidInt){
 						it.remove();
 					}
 				}
 			}
 		}
 	}
 
 	// check if placed sign meets criteria
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onSignChange(SignChangeEvent p){
 		Player player = p.getPlayer();
 		String line = p.getLine(0);
 		String rline = line;
 		char[] lineChar = rline.toCharArray();
 		if (charKeyExists(lineChar, 0)){
 			if (Character.toString(lineChar[0]).equals("[") && Character.toString(lineChar[lineChar.length - 1]).equals("]")){
 				rline = rline.replace("[", "");
 				rline = rline.replace("]", "");
 			}
 		}
 		rline = rline.toUpperCase();
 		if (line.equalsIgnoreCase("[GoldBank]") || line.equalsIgnoreCase("[GB]")){
 			boolean master = false;
 			String node = "goldbank.sign.bank.create";
 			if (p.getLine(3).equalsIgnoreCase("Master")){
 				master = true;
 				node = "goldbank.sign.bank.create.master";
 			}
 			Connection conn = null;
 			Statement st = null;
 			ResultSet rs = null;
 			try {
 				Class.forName("org.sqlite.JDBC");
 				String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 				conn = DriverManager.getConnection(dbPath);
 				st = conn.createStatement();
 				rs = st.executeQuery("SELECT * FROM chestdata WHERE world = '" + p.getBlock().getWorld().getName() + "' AND x = '" + p.getBlock().getX() + "' AND y = '" + p.getBlock().getY() + "' AND z = '" + p.getBlock().getZ() + "'");
 				int i = 0;
 				while (rs.next()){
 					i = i + 1;
 				}
 				if (i != 0){
 					p.getPlayer().sendMessage(ChatColor.RED + "Error: One or more signs were found registered at this location. Attempting to overwrite...");
 					st.executeUpdate("DELETE FROM chestdata WHERE world = '" + p.getBlock().getWorld().getName() + "' AND x = '" + p.getBlock().getX() + "' AND y = '" + p.getBlock().getY() + "' AND z = '" + p.getBlock().getZ() + "'");
 				}
 			}
 			catch (Exception ex){
 				ex.printStackTrace();
 			}
 			finally {
 				try {
 					conn.close();
 					st.close();
 					rs.close();
 				}
 				catch (Exception exc){
 					exc.printStackTrace();
 				}
 			}
 			if (player.hasPermission(node)){
 				p.setLine(0, "2[GoldBank]");
 				if (!master){
 					p.setLine(2, "5Claim this");
 					p.setLine(3, "5sign!");
 				}
 				else {
 					p.setLine(2, "");
 					p.setLine(3, "dMaster");
 				}
 				int tier = 1;
 				if (p.getLine(1).length() >= 5){
 					if (p.getLine(1).substring(0, 4).equalsIgnoreCase("Tier") && isInt(p.getLine(1).substring(5, 6))){
 						if (getConfig().isSet("tiers." + p.getLine(1).substring(0, 4) + ".size") && getConfig().isSet("tiers." + p.getLine(1).substring(0, 4) + ".fee")){
 							tier = Integer.parseInt(p.getLine(1).substring(5, 6));
 							p.setLine(1, "4Tier " + p.getLine(1).substring(5, 6));
 						}
 						else {
 							p.setLine(1, "4Tier 1");
 						}
 					}
 					else {
 						p.setLine(1, "4Tier 1");
 					}
 				}
 				else if (p.getLine(1).length() >= 1){
 					if (isInt(p.getLine(1).substring(0, 1))){
 						if (getConfig().isSet("tiers." + Integer.parseInt(p.getLine(1).substring(0, 1)) + ".size")){
 							if (getConfig().isSet("tiers." + Integer.parseInt(p.getLine(1).substring(0, 1)) + ".fee")){
 								tier = Integer.parseInt(p.getLine(1).substring(0, 1));
 								p.setLine(1, "4Tier " + p.getLine(1).substring(0, 1));
 							}
 							else {
 								p.setLine(1, "4Tier 1");
 							}
 						}
 					}
 					else {
 						p.setLine(1, "4Tier 1");
 					}
 				}
 				else {
 					p.setLine(1, "4Tier 1");
 				}
 				if (master){
 					conn = null;
 					st = null;
 					try {
 						Class.forName("org.sqlite.JDBC");
 						String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 						conn = DriverManager.getConnection(dbPath);
 						st = conn.createStatement();
 						st.executeUpdate("INSERT INTO chestdata (username, world, x, y, z, sign, tier) VALUES ('MASTER', '" + p.getBlock().getWorld().getName() + "', '" + p.getBlock().getX() + "', '" + p.getBlock().getY() + "', '" + p.getBlock().getZ() + "', 'true', '" + Integer.toString(tier) + "')");
 					}
 					catch (Exception e){
 						e.printStackTrace();
 					}
 					finally {
 						try {
 							conn.close();
 							st.close();
 						}
 						catch (Exception ex){
 							ex.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		else if (line.equalsIgnoreCase("2[GoldBank]")){
 			if (!player.hasPermission("goldbank.sign.bank.create")){
 				p.setLine(0, "[GoldBank]");
 			}
 		}
 		else if (line.equalsIgnoreCase("[GoldATM]")){
 			if (player.hasPermission("goldbank.sign.atm.create")){
 				p.setLine(0, "2[GoldATM]");
 			}
 		}
 		else if (line.equalsIgnoreCase("2[GoldATM]")){
 			if (!player.hasPermission("goldbank.sign.atm.create")){
 				p.setLine(0, "[GoldATM]");
 			}
 		}
 		String[] matInfo = new String[2];
 		String data = null;
 		if (rline.contains(":")){
 			matInfo = rline.split(":");
 			rline = matInfo[0];
 			if (matInfo.length > 1)
 				data = matInfo[1];
 		}
 		rline = rline.replace(" ", "_");
 		boolean isValidInt = false;
 		if (isInt(rline)){
 			if (isMat(Integer.parseInt(rline))){
 				isValidInt = true;
 			}
 		}
 		if (isMat(rline) || isValidInt){
 			String mat = "";
 			if (isValidInt){
 				mat = WordUtils.capitalize(Material.getMaterial(Integer.parseInt(rline)).toString().toLowerCase());
 			}
 			else {
 				mat = WordUtils.capitalize(rline.toLowerCase());
 			}
 			if (player.hasPermission("goldbank.sign.shop.create")){
 				boolean normal = false;
 				if (!p.getLine(3).equalsIgnoreCase("Admin")){
 					normal = true;
 				}
 				if (player.hasPermission("goldbank.sign.shop.create.admin") || normal){
 					String[] buys = new String[]{"blank", "blank"};
 					String[] sells = new String[]{"blank", "blank"};
 					String buy = p.getLine(1);
 					boolean validBuy = false;
 					boolean validSell = false;
 					if (buy.contains(";")){
 						buy = buy.replace(" ", "");
 						buys = buy.split(";");
 						if (isInt(buys[0]) && isInt(buys[1])){
 							validBuy = true;
 						}
 					}
 					String sell = p.getLine(2);
 					if (sell.contains(";")){
 						sell = sell.replace(" ", "");
 						sells = sell.split(";");
 						if (isInt(sells[0]) && isInt(sells[1])){
 							validSell = true;
 						}
 					}
 					if (validBuy && validSell){
 						int dataNum = 0;
 						if (data != null){
 							if (isInt(data)){
 								dataNum = Integer.parseInt(data);
 								if (mat.equalsIgnoreCase("Wool")){
 									if (dataNum == 0)
 										mat = "White Wool";
 									else if (dataNum == 1)
 										mat = "Orange Wool";
 									else if (dataNum == 2)
 										mat = "Magenta Wool";
 									else if (dataNum == 3)
 										mat = "LBlue Wool";
 									else if (dataNum == 4)
 										mat = "Yellow Wool";
 									else if (dataNum == 5)
 										mat = "Lime Wool";
 									else if (dataNum == 6)
 										mat = "Pink Wool";
 									else if (dataNum == 7)
 										mat = "Gray Wool";
 									else if (dataNum == 8)
 										mat = "LGray Wool";
 									else if (dataNum == 9)
 										mat = "Cyan Wool";
 									else if (dataNum == 10)
 										mat = "Purple Wool";
 									else if (dataNum == 11)
 										mat = "Blue Wool";
 									else if (dataNum == 12)
 										mat = "Brown Wool";
 									else if (dataNum == 13)
 										mat = "Green Wool";
 									else if (dataNum == 14)
 										mat = "Red Wool";
 									else if (dataNum == 15)
 										mat = "Black Wool";
 									else {
 										mat = "White Wool";
 									}
 								}
 							}
 							else {
 								data = null;
 							}
 						}
 						Connection conn = null;
 						Statement st = null;
 						ResultSet rs = null;
 						try {
 							Class.forName("org.sqlite.JDBC");
 							String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 							conn = DriverManager.getConnection(dbPath);
 							st = conn.createStatement();
 							boolean admin = true;
 							if (normal)
 								admin = false;
 							String world = p.getBlock().getWorld().getName();
 							int x = p.getBlock().getX();
 							int y = p.getBlock().getY();
 							int z = p.getBlock().getZ();
 							rs = st.executeQuery("SELECT COUNT(*) FROM shops WHERE world = '" + world + "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
 							int i = 0;
 							while (rs.next())
 								i = rs.getInt(1);
 							if (i == 0){
 								if (buys[0].length() + buys[1].length() <= 5 && sells[0].length() + sells[1].length() <= 4){
 									Location chestLoc = new Location(p.getBlock().getWorld(), p.getBlock().getX(), p.getBlock().getY() - 1, p.getBlock().getZ());
 									if (chestLoc.getBlock().getType() == Material.AIR || admin){
 										int matId = 0;
 										if (isValidInt){
 											matId = Integer.parseInt(rline);
 										}
 										else {
 											matId = Material.getMaterial(rline).getId();
 										}
 										st.executeUpdate("INSERT INTO shops (creator, world, x, y, z, material, data, buyamount, buyprice, sellamount, sellprice, admin) VALUES (" +
 												"'" + player.getName() +
 												"', '" + player.getWorld().getName() +
 												"', '" + x +
 												"', '" + y +
 												"', '" + z +
 												"', '" + matId +
 												"', '" + dataNum +
 												"', '" + buys[0] +
 												"', '" + buys[1] +
 												"', '" + sells[0] +
 												"', '" + sells[1] +
 												"', '" + admin + "')");
 										rs = st.executeQuery("SELECT * FROM shops WHERE world = '" + player.getWorld().getName() + "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
 										int shopId = rs.getInt("id");
 										st.executeUpdate("INSERT INTO shoplog (shop, player, action, time) VALUES ('" + shopId + "', '" + player.getName() + "', '2', '" + System.currentTimeMillis() / 1000 + "')");
 										int dataLength = 0;
 										if (dataNum != 0 && Material.getMaterial(matId) != Material.WOOL){
 											dataLength = Integer.toString(dataNum).length() + 1;
 										}
 										if ((mat.length() + dataLength) <= 11 || Material.getMaterial(matId) == Material.WOOL){
 											if (dataNum == 0 || Material.getMaterial(matId) == Material.WOOL){
 												String forMat = WordUtils.capitalize(mat.replace("_", " "));
 												p.setLine(0, "2[" + forMat + "]");
 											}
 											else {
 												String forMat = WordUtils.capitalize(mat.replace("_", " "));
 												p.setLine(0, "2[" + forMat + ":" + dataNum + "]");
 											}
 										}
 										else {
 											if (dataNum == 0 || Material.getMaterial(rline) == Material.WOOL){
 												p.setLine(0, "2[" + matId + "]");
 											}
 											else
 												p.setLine(0, "2[" + matId + ":" + dataNum + "]");
 										}
 										if (buys[0].length() + buys[1].length() <= 3)
 											p.setLine(1, "5" + "Buy " + buys[0] + " for " + buys[1] + "g");
 										else
 											p.setLine(1, "Buy " + buys[0] + " for " + buys[1] + "g");
 										if (sells[0].length() + sells[1].length() <= 2)
 											p.setLine(2, "5" + "Sell " + sells[0] + " for " + sells[1] + "g");
 										else
 											p.setLine(2, "Sell " + sells[0] + " for " + sells[1] + "g");
 										if (normal)
 											p.setLine(3, "9" + player.getName());
 										else
 											p.setLine(3, "4Admin");
 										if (normal)
 											chestLoc.getBlock().setType(Material.CHEST);
 										player.sendMessage(ChatColor.DARK_PURPLE + "Successfully created GoldShop sign!");
 									}
 									else {
 										player.sendMessage(ChatColor.RED + "Error: Block below sign must be air!");
 									}
 								}
 								else {
 									if (buys[0].length() + buys[1].length() > 5)
 										player.sendMessage(ChatColor.RED + "Invalid sign! The length of the buy amount plus the length of the buy price must be less than or equal to 5!");
 									if (sells[0].length() + sells[1].length() > 4)
 										player.sendMessage(ChatColor.RED + "Invalid sign! The length of the sell amount plus the length of the sell price must be less than or equal to 4!");
 								}
 							}
 							else {
 								player.sendMessage(ChatColor.RED + "There's somehow already a sign registered at this location. Perhaps it was WorldEdited away?");
 							}
 						}
 						catch (Exception e){
 							e.printStackTrace();
 							player.sendMessage(ChatColor.RED + "An error occurred while registering your sign. Please contact a server administrator.");
 						}
 						finally {
 							try {
 								conn.close();
 								st.close();
 								rs.close();
 							}
 							catch (Exception u){
 								u.printStackTrace();
 							}
 						}
 					}
 					else
 						player.sendMessage(ChatColor.RED + "Invalid sign! Buy and sell signs nust contain delimiter (;) or be left blank!");
 				}
 			}
 		}
 	}
 
 	// read the text file
 	public static String readFile(String fileName){
 		File file = new File(fileName);
 		char[] buffer = null;
 		try {
 			BufferedReader bufferedReader = new BufferedReader(
 					new FileReader(file));
 			buffer = new char[(int)file.length()];
 			int i = 0;
 			int c = bufferedReader.read();
 			while (c != -1){
 				buffer[i++] = (char)c;
 				c = bufferedReader.read();
 			}
 			bufferedReader.close();
 		}
 		catch (FileNotFoundException e){
 			e.printStackTrace();
 		}
 		catch (IOException e){
 			e.printStackTrace();
 		}
 		return new String(buffer);
 	}
 
 	// check amount of item in inventory
 	public static int getAmountInInv(Inventory inv, Material item){
 		ItemStack[] contents = inv.getContents();
 		int total = 0;
 		for (ItemStack slot : contents){
 			if (slot != null){
 				if (slot.getType() == item){
 					total = total + slot.getAmount();
 				}
 			}
 		}
 		return total;
 	}
 
 	// check empty slots in inventory
 	public static int getNullsInInv(Inventory inv){
 		ItemStack[] contents = inv.getContents();
 		int total = 0;
 		for (ItemStack slot : contents){
 			if (slot == null){
 				total = total + 1;
 			}
 		}
 		return total;
 	}
 
 	// method to fill the inventories
 	public void fill(){
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			String dbPath = "jdbc:sqlite:" + plugin.getDataFolder() + File.separator + "chestdata.db";
 			conn = DriverManager.getConnection(dbPath);
 			st = conn.createStatement();
 			rs = st.executeQuery("SELECT * FROM chestdata");
 			while (rs.next()){
 				String p = rs.getString("username");
 				Player player = Bukkit.getServer().getPlayer(p);
 				File invF = new File(this.getDataFolder() + File.separator + "inventories" + File.separator + p + ".inv");
 				YamlConfiguration invY = new YamlConfiguration();
 				invY.load(invF);
 				int size = invY.getInt("size");
 				Set<String> keys = invY.getKeys(false);
 				ItemStack[] invI = new ItemStack[size];
 				for (String invN : keys){
 					if (!invN.equalsIgnoreCase("size")){
 						int i = Integer.parseInt(invN);
 						invI[i] =  invY.getItemStack(invN);
 					}
 				}
 				Inventory inv = this.getServer().createInventory(null, size, p + "'s GoldBank Sign");
 				inv.setContents(invI);
 				if (inv.contains(Material.GOLD_BLOCK) || inv.contains(Material.GOLD_INGOT) || inv.contains(Material.GOLD_NUGGET)){
 					int blocks = getAmountInInv(inv, Material.GOLD_BLOCK);
 					int ingots = getAmountInInv(inv, Material.GOLD_INGOT);
 					int nuggets = getAmountInInv(inv, Material.GOLD_NUGGET);
 					int totalblocks = (blocks * 81);
 					int totalingots = (ingots * 9);
 					double total = (double)(totalblocks + totalingots + nuggets);
 					double rate = getConfig().getDouble("interest");
 					double doubleinterest = (total * rate);
 					int interest = (int)Math.round(doubleinterest);
 					int newBlocks = interest / 81;
 					int blockRemainder = interest - newBlocks * 81;
 					int newIngots = blockRemainder / 9;
 					int newNuggets = blockRemainder - newIngots * 9;
 					ItemStack addBlocks = new ItemStack(Material.GOLD_BLOCK, newBlocks);
 					ItemStack addIngots = new ItemStack(Material.GOLD_INGOT, newIngots);
 					ItemStack addNuggets = new ItemStack(Material.GOLD_NUGGET, newNuggets);
 					if (newBlocks != 0){
 						inv.addItem(addBlocks);
 					}
 					if (newIngots != 0){
 						inv.addItem(addIngots);
 					}
 					if (newNuggets != 0){
 						inv.addItem(addNuggets);
 					}
 					invY.load(invF);
 					for (int i = 0; i < inv.getSize(); i++){
 						invY.set("" + i, inv.getItem(i));
 					}
 					invY.save(invF);
 				}
 			}
 		}
 		catch (Exception e){
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				conn.close();
 				st.close();
 				rs.close();
 			}
 			catch (Exception u){
 				u.printStackTrace();
 			}
 		}
 	}
 
 	// call the inventory filling function
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent j) throws IOException {
 		Calendar cal = Calendar.getInstance();
 		int dow = cal.get(Calendar.DAY_OF_WEEK);
 		// check value dayofweek
 		boolean invalidday = false;
 		// check for invalid day value
 		String daycheck = getConfig().getString("dayofweek");
 		if (!daycheck.equalsIgnoreCase("Sunday") && !daycheck.equalsIgnoreCase("Monday") && !daycheck.equalsIgnoreCase("Tuesday") && !daycheck.equalsIgnoreCase("Wednesday") && !daycheck.equalsIgnoreCase("Thursday") && !daycheck.equalsIgnoreCase("Friday") && !daycheck.equalsIgnoreCase("Saturday")){
 			log.warning(ANSI_RED + "[GoldBank] Error detected in config value \"dayofweek\"! We'll take care of it..." + ANSI_WHITE);
 		}
 		String check = "";
 		if (!invalidday){
 			check = getConfig().getString("dayofweek");
 			int daynum = checkDay(check);
 			if (dow == daynum){
 				File file = new File(getDataFolder(), "filled.txt");
 				String last = readFile(getDataFolder() + File.separator + "filled.txt");
 				String fill;
 				fill = last.replaceAll("(\\r|\\n)", "");
 				int filled = Integer.parseInt(fill);
 				// Fill
 				if (filled == 0){
 					fill();
 					PrintWriter pw = new PrintWriter(file);
 					pw.print("1");
 					pw.close();
 				}
 			}
 			if (dow == daynum + 2){
 				File file = new File(getDataFolder(), "filled.txt");
 				String last = readFile(getDataFolder() + File.separator + "filled.txt");
 				String fill;
 				fill = last.replaceAll("(\\r|\\n)", "");
 				int filled = Integer.parseInt(fill);
 				if (filled == 1){
 					fill();
 					PrintWriter pw = new PrintWriter(file);
 					pw.print("0");
 					pw.close();
 				}
 			}
 		}
 	}
 
 	// commands and stuff :D
 	@SuppressWarnings("deprecation")
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if (commandLabel.equalsIgnoreCase("gb")){
 			if (args.length >= 1){
 				if (args[0].equalsIgnoreCase("reload")){
 					this.reloadConfig();
 					Bukkit.getPluginManager().disablePlugin(this);
 					Bukkit.getPluginManager().enablePlugin(this);
 					log.info(ANSI_GREEN + "[GoldBank] has been reloaded!" + ANSI_WHITE);
 					if (sender instanceof Player)
 						sender.sendMessage(ChatColor.DARK_AQUA + "GoldBank has been reloaded!");
 				}
 				// bank
 				else if (args[0].equalsIgnoreCase("bank")){
 					// view
 					if (args[0].equalsIgnoreCase("view")){
 						if (sender instanceof Player){
 							if (args.length == 1 && sender.hasPermission("goldbank.view")){
 								String user = sender.getName();
 								File invF = new File(getDataFolder() + File.separator + "inventories", user + ".inv");
 								if(invF.exists()){
 									YamlConfiguration invY = new YamlConfiguration();
 									try {
 										invY.load(invF);
 										int size = invY.getInt("size");
 										Set<String> keys = invY.getKeys(false);
 										ItemStack[] invI = new ItemStack[size];
 										for (String invN : keys){
 											if (!invN.equalsIgnoreCase("size")){
 												int i = Integer.parseInt(invN);
 												invI[i] =  invY.getItemStack(invN);
 											}
 										}
 										Inventory inv = this.getServer().createInventory(null, size, user + "'s GoldBank Sign");
 										inv.setContents(invI);
 										((Player)sender).openInventory(inv);
 										openPlayer[nextIndex] = user;
 										openingPlayer[nextIndex] = user;
 										openType[nextIndex] = "wallet";
 										nextIndex += 1;
 									}
 									catch (Exception ex){
 										ex.printStackTrace();
 									}
 								}
 								else
 									sender.sendMessage(ChatColor.RED + "Oh noes! You don't have a Bank inventory!");
 							}
 							else if (sender.hasPermission("goldbank.view.others")){
 								String user = args[1];
 								File invF = new File(getDataFolder() + File.separator + "inventories", user + ".inv");
 								if(invF.exists()){
 									YamlConfiguration invY = new YamlConfiguration();
 									try {
 										invY.load(invF);
 										int size = invY.getInt("size");
 										Set<String> keys = invY.getKeys(false);
 										ItemStack[] invI = new ItemStack[size];
 										for (String invN : keys){
 											if (!invN.equalsIgnoreCase("size")){
 												int i = Integer.parseInt(invN);
 												invI[i] =  invY.getItemStack(invN);
 											}
 										}
 										Inventory inv = this.getServer().createInventory(null, size, user + "'s GoldBank Sign");
 										inv.setContents(invI);
 										((Player)sender).openInventory(inv);
 										openPlayer[nextIndex] = user;
 										openingPlayer[nextIndex] = sender.getName();
 										openType[nextIndex] = "bank";
 										nextIndex += 1;
 									}
 									catch (Exception ex){
 										ex.printStackTrace();
 									}
 								}
 								else
 									sender.sendMessage(ChatColor.RED + "Oh noes! This player doesn't have a Bank inventory!");
 							}
 							else
 								log.info(ChatColor.RED + "Oh noes! You don't have permission to do this!");
 						}
 						else
 							sender.sendMessage(ChatColor.RED + "You must be an in-game player to perform this command!");
 					}
 					else
 						sender.sendMessage(ChatColor.RED + "Invalid argument! Usage: /gb bank [command]");
 				}
 				// wallet
 				else if (args[0].equalsIgnoreCase("wallet")){
 					if (sender instanceof Player){
 						if (args.length >= 4){
 							// view
 							if (args[1].equalsIgnoreCase("view")){
 								if (sender.hasPermission("goldbank.wallet.view")){
 									String user = args[2];
 									if (isInt(args[3])){
 										File invF = new File(getDataFolder() + File.separator + "wallets", user + ".inv");
 										if(invF.exists()){
 											YamlConfiguration invY = new YamlConfiguration();
 											try {
 												invY.load(invF);
 												if (invY.isSet(args[3])){
 													int size = invY.getInt(args[3] + ".size");
 													ItemStack[] invI = new ItemStack[size];
 													for (int i = 0; i < invI.length; i++){
 														if (!(args[3] + "." + i).equalsIgnoreCase("size")){
 															invI[i] =  invY.getItemStack(args[3] + "." + i);
 														}
 													}
 													Inventory inv = this.getServer().createInventory(null, size, user + "'s Wallet");
 													inv.setContents(invI);
 													((Player)sender).openInventory(inv);
 													openPlayer[nextIndex] = user;
 													openingPlayer[nextIndex] = sender.getName();
 													openType[nextIndex] = "wallet";
 													openWalletNo[nextIndex] = Integer.parseInt(args[2]);
 													nextIndex += 1;
 												}
 												else
 													sender.sendMessage(ChatColor.RED + "Error: The wallet specified does not exist!");
 											}
 											catch (Exception ex){
 												ex.printStackTrace();
 											}
 										}
 										else
 											sender.sendMessage(ChatColor.RED + "Oh noes! This player doesnt have any wallets!");
 									}
 									else
 										sender.sendMessage(ChatColor.RED + "Error: Wallet number must be an integer!");
 								}
 							}
 							// spawn
 							else if (args[1].equalsIgnoreCase("spawn")){
 								if (sender.hasPermission("goldbank.wallet.spawn")){
 									if (isInt(args[3])){
 										ItemStack is = new ItemStack(Material.BOOK, 1);
 										ItemMeta meta = is.getItemMeta();
 										meta.setDisplayName("2Wallet");
 										is.setItemMeta(meta);
 										try {
 											File invF = new File(getDataFolder() + File.separator + "wallets", args[2] + ".inv");
 											if (!invF.exists()){
 												invF.createNewFile();
 												sender.sendMessage(ChatColor.DARK_PURPLE + "Specified player does not yet have a wallets file. Attempting to create...");
 											}
 											YamlConfiguration invY = new YamlConfiguration();
 											invY.load(invF);
 											if (!invY.isSet(args[3])){
 												sender.sendMessage(ChatColor.DARK_PURPLE + "Specified wallet number does not yet exist. Attempting to create...");
 												invY.set(args[3] + ".size", this.getConfig().getInt("walletsize"));
 												invY.save(invF);
 											}
 										}
 										catch (Exception ex){
 											ex.printStackTrace();
 											sender.sendMessage(ChatColor.RED + "An error occurred while creating the wallet.");
 										}
 										meta = is.getItemMeta();
 										List<String> lore = new ArrayList<String>();
 										lore.add("Owned by");
 										lore.add(args[2]);
 										lore.add("9Wallet #" + args[3]);
 										lore.add("2GoldBank");
 										meta.setLore(lore);
 										is.setItemMeta(meta);
 										((Player)sender).getInventory().addItem(is);
 										((Player)sender).updateInventory();
 									}
 									else
 										sender.sendMessage(ChatColor.RED + "Error: Wallet number must be an integer!");
 								}
 								else
 									sender.sendMessage(ChatColor.RED + "Oh noes! You don't have permission to perform this command! :(");
 							}
 							else
 								sender.sendMessage(ChatColor.RED + "Invalid argument! Usage: /gb wallet [command]");
 						}
 						else
 							sender.sendMessage(ChatColor.RED + "You don't have permisison to perform this command!");
 					}
 					else
 						sender.sendMessage(ChatColor.RED + "You must be an in-game player to perform this command!");
 				}
 				else if (args[0].equalsIgnoreCase("shop")){
 					if (args.length >= 2){
 						if (args[1].equalsIgnoreCase("log")){
 							if (sender instanceof Player){
 								if (sender.hasPermission("goldbank.sign.shop.log")){
 									if (args.length == 2){
										if (!shopLog.containsKey(((Player)sender).getName()))
											shopLog.put(((Player)sender).getName(), 0);
 										sender.sendMessage(ChatColor.DARK_PURPLE + "Click a GoldShop to view its history");
 									}
 									else if (args[2].equalsIgnoreCase("page")){
 										if (args.length >= 4){
 											if (isInt(args[3])){
 												if (shopLog.containsKey(sender.getName())){
 													if (shopLog.get(sender.getName()) > 0){
 														int shopId = shopLog.get(sender.getName());
 														Connection conn = null;
 														Statement st = null;
 														ResultSet rs = null;
 														try {
 															Class.forName("org.sqlite.JDBC");
 															String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 															conn = DriverManager.getConnection(dbPath);
 															st = conn.createStatement();
 															int count = 0;
 															shopLog.put(sender.getName(), shopId);
 															rs = st.executeQuery("SELECT COUNT(*) FROM shoplog WHERE shop = '" + shopId + "' AND action < '2'");
 															int total = 0;
 															while (rs.next()){
 																total = rs.getInt(1);
 															}
 															if (total != 0){
 																int perPage = 10;
 																int pages = total / perPage;
 																if (pages * perPage != total)
 																	pages += 1;
 																if (pages >= Integer.parseInt(args[3])){
 																	int thisPage = total - ((Integer.parseInt(args[3]) - 1) * perPage);
 																	sender.sendMessage(ChatColor.DARK_PURPLE + "Page " + args[3] + "/" + pages);
 																	rs = st.executeQuery("SELECT * FROM shoplog WHERE shop = '" + shopId + "' AND action < '2' ORDER BY id DESC");
 																	for (int i = 1; i <= (Integer.parseInt(args[3]) - 1) * perPage; i++)
 																		rs.next();
 																	for (int i = 1; i <= perPage; i++){
 																		if (i <= thisPage){
 																			String action = "";
 																			ChatColor actionColor = ChatColor.DARK_GREEN;
 																			if (rs.getInt("action") == 0)
 																				action = "bought";
 																			else if (rs.getInt("action") == 1){
 																				action = "sold";
 																				actionColor = ChatColor.DARK_RED;
 																			}
 																			String data = "";
 																			if (rs.getInt("data") > 0)
 																				data = ":" + rs.getInt("data");
 																			Calendar cal = Calendar.getInstance();
 																			cal.setTimeInMillis((long)rs.getInt("time") * 1000);
 																			String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
 																			String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
 																			String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
 																			String min = Integer.toString(cal.get(Calendar.MINUTE));
 																			String sec = Integer.toString(cal.get(Calendar.SECOND));
 																			if (month.length() < 2)
 																				month = "0" + month;
 																			if (day.length() < 2)
 																				day = "0" + day;
 																			while (hour.length() < 2)
 																				hour = "0" + hour;
 																			while (min.length() < 2)
 																				min = "0" + min;
 																			while (sec.length() < 2)
 																				sec = "0" + sec;
 																			String dateStr = cal.get(Calendar.YEAR) + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
 																			sender.sendMessage(ChatColor.DARK_PURPLE + Integer.toString(i + ((Integer.parseInt(args[3]) - 1) * perPage)) + ") " + ChatColor.DARK_AQUA + dateStr + " " + ChatColor.LIGHT_PURPLE + rs.getString("player") + " " + actionColor + action + " " + ChatColor.GOLD + rs.getInt("quantity") + " " + Material.getMaterial(rs.getInt("material")).toString() + data);
 																			rs.next();
 																		}
 																		else
 																			break;
 																	}
 																	if (Integer.parseInt(args[3]) < pages)
 																		sender.sendMessage(ChatColor.DARK_PURPLE + "Type " + ChatColor.DARK_GREEN + "/gb shop log page " + (Integer.parseInt(args[3]) + 1) + ChatColor.DARK_PURPLE + " to view the next page");
 																}
 																else
 																	sender.sendMessage(ChatColor.RED + "Invalid page number!");
 															}
 															else
 																sender.sendMessage(ChatColor.RED + "Error: The selected shop does not have any logged transactions!");
 														}
 														catch (Exception ex){
 															ex.printStackTrace();
 														}
 														finally {
 															try {
 																conn.close();
 																st.close();
 																rs.close();
 															}
 															catch (Exception exc){
 																exc.printStackTrace();
 															}
 														}
 													}
 													else
 														sender.sendMessage(ChatColor.RED + "Please select a shop first!");
 												}
 												else
 													sender.sendMessage(ChatColor.RED + "Please select a shop first!");
 											}
 											else
 												sender.sendMessage(ChatColor.RED + "Page number must be an integer!");
 										}
 									}
 									else
 										sender.sendMessage(ChatColor.RED + "Invalid arguments! Usage: /gb shop log [page]");
 								}
 							}
 							else
 								sender.sendMessage(ChatColor.RED + "You must be an in-game player to perform this command!");
 						}
 						else
 							sender.sendMessage(ChatColor.RED + "Invalid argument! Usage: /gb shop [command]");
 					}
 					else
 						sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /gb shop [command]");
 				}
 				else
 					sender.sendMessage(ChatColor.RED + "Invalid argument! Usage: /gb [command] [args]");
 			}
 			else if (args.length < 1)
 				sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /gb [command] [args]");
 			return true;
 		}
 		return false;
 	}
 
 	// get the number of a day of the week
 	public static int checkDay(String day){
 		Map<String,Integer> mp=new HashMap<String,Integer>();
 		mp.put("Sunday",1);
 		mp.put("Monday",2);
 		mp.put("Tuesday",3);
 		mp.put("Wednesday",4);
 		mp.put("Thursday",5);
 		mp.put("Friday",6);
 		mp.put("Saturday",7);
 		return mp.get(day).intValue();
 	}
 	public boolean colExists(String table, String col){
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			String dbPath = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "chestdata.db";
 			conn = DriverManager.getConnection(dbPath);
 			st = conn.createStatement();
 			rs = st.executeQuery("SELECT " + col + " FROM " + table + " LIMIT 1");
 			return true;
 		}
 		catch (Exception e){
 			return false;
 		}
 		finally {
 			try {
 				conn.close();
 				st.close();
 				rs.close();
 			}
 			catch (Exception n){
 				n.printStackTrace();
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onInventoryClose(InventoryCloseEvent c){
 		boolean check = false;
 		int index = -1;
 		int n = 0;
 		for (n = 0; n < 256; n++){
 			if (c.getPlayer().getName() == openingPlayer[n]){
 				check = true;
 				index = n;
 				break;
 			}
 		}
 		if (check == true){
 			try {
 				String dir = "inventories";
 				if (openType[index].equals("bank"))
 					dir = "inventories";
 				else if (openType[index].equals("wallet"))
 					dir = "wallets";
 				File invF = new File(getDataFolder() + File.separator + dir, openPlayer[index] + ".inv");
 				if (!invF.exists()){
 					invF.createNewFile();
 				}
 				YamlConfiguration invY = new YamlConfiguration();
 				invY.load(invF);
 				int size = this.getConfig().getInt("walletsize");
 				String root = "";
 				if (openType[index].equals("wallet"))
 					root = openWalletNo[index] + ".";
 				else
 					size = invY.getInt("size");
 				Inventory inv = c.getInventory();
 				for (int i = 0; i < c.getInventory().getSize(); i++){
 					invY.set(root + i, inv.getItem(i));
 				}
 				invY.save(invF);
 				openPlayer[index] = null;
 				openingPlayer[index] = null;
 				openType[index] = null;
 				nextIndex = index;
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public boolean isInt(String i){
 		try {
 			Integer.parseInt(i);
 			return true;
 		}
 		catch(NumberFormatException nfe){
 			return false;
 		}
 	}
 
 	public boolean isMat(String m){
 		if (Material.getMaterial(m) != null)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean isMat(int m){
 		if (Material.getMaterial(m) != null)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean isBool(String b){
 		try {
 			Boolean.parseBoolean(b);
 			return true;
 		}
 		catch (Exception e){
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean charKeyExists(char[] array, int key){
 		try {
 			Character.toString(array[key]);
 			return true;
 		}
 		catch (Exception e){
 			return false;
 		}
 	}
 
 	public Block getAdjacentBlock(Block block, Material material){
 		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP};
 		for (BlockFace face : faces){
 			Block adjBlock = block.getRelative(face);
 			if (adjBlock.getType() == material){
 				if (face != BlockFace.UP){
 					byte data = adjBlock.getData();
 					byte north = 0x2;
 					byte south = 0x3;
 					byte west = 0x4;
 					byte east = 0x5;
 					BlockFace attached = null;
 					if (data == east){
 						attached = BlockFace.WEST;
 					}
 					else if (data == west){
 						attached = BlockFace.EAST;
 					}
 					else if (data == north){
 						attached = BlockFace.SOUTH;
 					}
 					else if (data == south){
 						attached = BlockFace.NORTH;
 					}
 					if (adjBlock.getType() == Material.SIGN_POST){
 						attached = BlockFace.DOWN;
 					}
 					// I had to be a bit creative with the comparison...
 					if (block.getX() == adjBlock.getRelative(attached).getX() && block.getY() == 
 							adjBlock.getRelative(attached).getY() && block.getZ() == adjBlock.getRelative(attached).getZ()){
 						return adjBlock;
 					}
 				}
 				else if (material == Material.SIGN_POST){
 					return adjBlock;
 				}
 			}
 		}
 		return null;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onEntityDamage(EntityDamageEvent d){
 		if (d.getEntityType() != EntityType.PLAYER){
 			if (d.getCause() == DamageCause.FALL){
 				if (d.getEntity().getFallDistance() >= 10)
 					d.getEntity().setMetadata("disableGoldDrop", new FixedMetadataValue(this, true));
 			}
 			if (this.getConfig().getBoolean("disable-drops-on-external-damage")){
 				if (!(d instanceof EntityDamageByEntityEvent)){
 					if (d.getEntity().getMetadata("externalDamage").isEmpty())
 						d.getEntity().setMetadata("externalDamage", new FixedMetadataValue(this, d.getDamage()));
 					else
 						d.getEntity().setMetadata("externalDamage", new FixedMetadataValue(this, d.getEntity().getMetadata("externalDamage").get(0).asInt() + d.getDamage()));
 				}
 				else if (((EntityDamageByEntityEvent)d).getDamager().getType() != EntityType.PLAYER){
 					if (d.getEntity().getMetadata("externalDamage").isEmpty())
 						d.getEntity().setMetadata("externalDamage", new FixedMetadataValue(this, d.getDamage()));
 					else
 						d.getEntity().setMetadata("externalDamage", new FixedMetadataValue(this, d.getEntity().getMetadata("externalDamage").get(0).asInt() + d.getDamage()));
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onMobDeath(EntityDeathEvent d){
 		if (d.getEntity() instanceof LivingEntity && d.getEntity().getType() != EntityType.PLAYER){
 			if (d.getEntity().getKiller() != null){
 				if (d.getEntity().getKiller().getType() == EntityType.PLAYER){
 					if (!getConfig().getList("disable-drops-in").contains(d.getEntity().getWorld().getName())){
 						boolean farm = false;
 						if (getConfig().getBoolean("disablefarms") == false)
 							farm = true;
 						boolean spawner = false;
 						if (d.getEntity().hasMetadata("disableGoldDrop"))
 							spawner = true;
 						boolean exDamage = false;
 						if (d.getEntity().hasMetadata("externalDamage")){
 							if (d.getEntity().getMetadata("externalDamage").get(0).asInt() > (d.getEntity().getMaxHealth() / 2))
 								exDamage = true;
 						}
 						if ((!spawner || farm) && !exDamage){
 							Player player = d.getEntity().getKiller();
 							World world = player.getWorld();
 							EntityType eType = d.getEntity().getType();
 							Location mobLoc = d.getEntity().getLocation();
 							Location loc = new Location(world, mobLoc.getX(), mobLoc.getY() + 1, mobLoc.getZ());
 							int loot = 0;
 							if (player.getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)){
 								loot = player.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
 							}
 							HashMap<EntityType, String> cNames = new HashMap<EntityType, String>();
 							cNames.put(EntityType.CREEPER, "creeper");
 							cNames.put(EntityType.ZOMBIE, "zombie");
 							cNames.put(EntityType.SKELETON, "skeleton");
 							cNames.put(EntityType.BLAZE, "blaze");
 							cNames.put(EntityType.SPIDER, "spider");
 							cNames.put(EntityType.ENDERMAN, "enderman");
 							cNames.put(EntityType.WITCH, "witch");
 							cNames.put(EntityType.SLIME, "slime");
 							cNames.put(EntityType.MAGMA_CUBE, "magmacube");
 							cNames.put(EntityType.GHAST, "ghast");
 							cNames.put(EntityType.CAVE_SPIDER, "cavespider");
 							cNames.put(EntityType.ENDER_DRAGON, "enderdragon");
 							cNames.put(EntityType.PIG_ZOMBIE, "zombiepigman");
 							cNames.put(EntityType.SILVERFISH, "silverfish");
 							cNames.put(EntityType.WITHER_SKULL, "witherskeleton");
 							cNames.put(EntityType.WITHER, "wither");
 							cNames.put(EntityType.PIG, "pig");
 							cNames.put(EntityType.COW, "cow");
 							cNames.put(EntityType.MUSHROOM_COW, "mooshroomcow");
 							cNames.put(EntityType.CHICKEN, "chicken");
 							cNames.put(EntityType.SQUID, "squid");
 							cNames.put(EntityType.SHEEP, "sheep");
 							cNames.put(EntityType.SNOWMAN, "snowgolem");
 							cNames.put(EntityType.IRON_GOLEM, "irongolem");
 							cNames.put(EntityType.OCELOT, "ocelot");
 							cNames.put(EntityType.BAT, "bat");
 							cNames.put(EntityType.WOLF, "wolf");
 							cNames.put(EntityType.GIANT, "giant");
 
 							dropItems(cNames.get(eType), world, loc, loot);
 							if (this.getConfig().getDouble("rare-drop-rate") != 0 && this.getConfig().getInt("mobdrops." + cNames.get(eType)) != 0 && !this.getConfig().getList("disable-rare-drops-for").contains(cNames.get(eType))){
 								double rand = Math.random();
 								double lootAdd = 1 + (loot * .5);
 								double rate = this.getConfig().getDouble("rare-drop-rate") * lootAdd;
 								if (rand <= this.getConfig().getDouble("rare-drop-rate")){
 									List<Material> rareGold = new ArrayList<Material>();
 									rareGold.add(Material.GOLD_INGOT);
 									rareGold.add(Material.GOLD_BLOCK);
 									rareGold.add(Material.GOLD_PICKAXE);
 									rareGold.add(Material.GOLD_SWORD);
 									rareGold.add(Material.GOLD_SPADE);
 									rareGold.add(Material.GOLD_AXE);
 									rareGold.add(Material.GOLD_HOE);
 									rareGold.add(Material.GOLD_HELMET);
 									rareGold.add(Material.GOLD_CHESTPLATE);
 									rareGold.add(Material.GOLD_LEGGINGS);
 									rareGold.add(Material.GOLD_BOOTS);
 									rareGold.add(Material.GOLDEN_CARROT);
 									rareGold.add(Material.GOLDEN_APPLE);
 									int min2 = 0;
 									int max2 = rareGold.size() - 1;
 									int rand2 = min2 + (int)(Math.random() * ((max2 - min2) + 1));
 									boolean uberApple = false;
 									if (rareGold.get(rand2) == Material.GOLDEN_APPLE){
 										int min3 = 1;
 										int max3 = 10;
 										int rand3 = min3 + (int)(Math.random() * (max3 - min3) + 1);
 										if (rand3 == 1)
 											uberApple = true;
 									}
 									ItemStack rareDrop = new ItemStack(rareGold.get(rand2), 1);
 									if (uberApple)
 										rareDrop.setDurability((short)1);
 									world.dropItem(loc, rareDrop);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void dropItems(String cName, World world, Location loc, int loot){
 		int max = getConfig().getInt("mobdrops." + cName);
 		if (max != 0){
 			max = max + loot;
 		}
 		int amount = (int)(Math.random() * (max + 1));
 		if (amount != 0){
 			world.dropItem(loc, new ItemStack(Material.GOLD_NUGGET, amount));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPistonRetract(BlockPistonRetractEvent r){
 		Block block = r.getRetractLocation().getBlock();
 		if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST){
 			Sign sign = (Sign)block.getState();
 			if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]")){
 				r.setCancelled(true);
 			}
 			else if (sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){	
 				r.setCancelled(true);
 			}
 			String line = sign.getLine(0);
 			String rline = line.replace("[", "");
 			rline = rline.replace("]", "");
 			rline = rline.replace("2", "");
 			rline = rline.toUpperCase();
 			String[] matInfo = new String[2];
 			String data = null;
 			if (rline.contains(":")){
 				matInfo = rline.split(":");
 				rline = matInfo[0];
 				data = matInfo[1];
 			}
 			boolean isValidInt = false;
 			if (isInt(rline)){
 				if (isMat(Integer.parseInt(rline))){
 					isValidInt = true;
 				}
 			}
 			if (isMat(rline) || isValidInt){
 				r.setCancelled(true);
 			}
 		}
 		if (getAdjacentBlock(block, Material.WALL_SIGN) != null || getAdjacentBlock(r.getBlock(), Material.SIGN_POST) != null){
 			Block adjblock = null;
 			if (getAdjacentBlock(block, Material.WALL_SIGN) != null){
 				adjblock = getAdjacentBlock(block, Material.WALL_SIGN);
 			}
 			else if (getAdjacentBlock(block, Material.SIGN_POST)!= null){
 				adjblock = getAdjacentBlock(block, Material.SIGN_POST);
 			}
 			Sign sign = (Sign)adjblock.getState();
 			if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]")){
 				r.setCancelled(true);
 			}
 			else if (sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 				r.setCancelled(true);
 			}
 			String line = sign.getLine(0);
 			String rline = line.replace("[", "");
 			rline = rline.replace("]", "");
 			rline = rline.replace("2", "");
 			rline = rline.toUpperCase();
 			String[] matInfo = new String[2];
 			String data = null;
 			if (rline.contains(":")){
 				matInfo = rline.split(":");
 				rline = matInfo[0];
 				data = matInfo[1];
 			}
 			boolean isValidInt = false;
 			if (isInt(rline)){
 				if (isMat(Integer.parseInt(rline))){
 					isValidInt = true;
 				}
 			}
 			if (isMat(rline) || isValidInt){
 				r.setCancelled(true);
 			}
 		}
 		else if (getAdjacentBlock(block, Material.SIGN_POST) != null){
 			Block adjblock = getAdjacentBlock(block, Material.SIGN_POST);
 			Sign sign = (Sign)adjblock.getState();
 			if (sign.getLine(0).equalsIgnoreCase("2[GoldBank]")){
 				r.setCancelled(true);
 			}
 			else if (sign.getLine(0).equalsIgnoreCase("2[GoldATM]")){
 				r.setCancelled(true);
 			}
 			String line = sign.getLine(0);
 			String rline = line.replace("[", "");
 			rline = rline.replace("]", "");
 			rline = rline.replace("2", "");
 			rline = rline.toUpperCase();
 			String[] matInfo = new String[2];
 			String data = null;
 			if (rline.contains(":")){
 				matInfo = rline.split(":");
 				rline = matInfo[0];
 				data = matInfo[1];
 			}
 			boolean isValidInt = false;
 			if (isInt(rline)){
 				if (isMat(Integer.parseInt(rline))){
 					isValidInt = true;
 				}
 			}
 			if (isMat(rline) || isValidInt){
 				r.setCancelled(true);
 			}
 		}
 	}
 
 	public String escape(String s){
 		s = s.replace("'", "''");
 		return s;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onCreatureSpawn(CreatureSpawnEvent s){
 		if (s.getSpawnReason() == SpawnReason.SPAWNER)
 			s.getEntity().setMetadata("disableGoldDrop", new FixedMetadataValue(this, true));
 	}
 
 	@EventHandler
 	public void onCraftPrepare(PrepareItemCraftEvent e){
 		ItemStack is = new ItemStack(Material.BOOK, 1);
 		ItemMeta meta = is.getItemMeta();
 		meta.setDisplayName("2Wallet");
 		is.setItemMeta(meta);
 		if (e.getRecipe() instanceof ShapedRecipe){
 			if (((ShapedRecipe)e.getRecipe()).getResult().equals(is)){
 				if (e.getViewers().get(0).hasPermission("goldbank.wallet.craft")){
 					try {
 						File invF = new File(getDataFolder() + File.separator + "wallets", ((Player)e.getViewers().get(0)).getName() + ".inv");
 						if (!invF.exists()){
 							invF.createNewFile();
 						}
 						YamlConfiguration invY = new YamlConfiguration();
 						invY.load(invF);
 						int nextKey = 1;
 						while (invY.isSet(Integer.toString(nextKey))){
 							nextKey += 1;
 						}
 						List<String> lore = new ArrayList<String>();
 						lore.add("Owned by");
 						lore.add(e.getViewers().get(0).getName());
 						lore.add("9Wallet #" + nextKey);
 						lore.add("2GoldBank");
 						meta.setLore(lore);
 						is.setItemMeta(meta);
 						e.getInventory().setResult(is);
 					}
 					catch (Exception ex){
 						ex.printStackTrace();
 						boolean cookie = this.getConfig().getBoolean("give-cookie-if-wallet-creation-fails");
 						String msg = "An error occurred while loading the next available key for your wallet.";
 						if (cookie){
 							msg = "An error occurred while loading the next available key for your wallet. Here's a cookie to make up for it... :)";
 							((Player)e.getViewers().get(0)).getInventory().addItem(new ItemStack(Material.COOKIE, 1));
 						}
 						((Player)e.getViewers().get(0)).sendMessage(ChatColor.RED + msg);
 					}
 				}
 				else {
 					e.getInventory().setResult(null);
 					((Player)e.getViewers().get(0)).sendMessage(ChatColor.RED + "Oh noes! You don't have permission to craft a wallet!");
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent e){
 		if (e instanceof CraftItemEvent){
 			ItemStack is = new ItemStack(Material.BOOK, 1);
 			ItemMeta meta = is.getItemMeta();
 			meta.setDisplayName("2Wallet");
 			is.setItemMeta(meta);
 			if (((CraftItemEvent)e).getRecipe().getResult().equals(is)){
 				if (e.isShiftClick())
 					e.setCancelled(true);
 				else {
 					try {
 						File invF = new File(getDataFolder() + File.separator + "wallets", ((Player)e.getViewers().get(0)).getName() + ".inv");
 						if (!invF.exists()){
 							invF.createNewFile();
 						}
 						YamlConfiguration invY = new YamlConfiguration();
 						invY.load(invF);
 						int nextKey = 1;
 						while (invY.isSet(Integer.toString(nextKey))){
 							nextKey += 1;
 						}
 						invY.set(nextKey + ".size", this.getConfig().getInt("walletsize"));
 						invY.save(invF);
 					}
 					catch (Exception ex){
 						ex.printStackTrace();
 						boolean cookie = this.getConfig().getBoolean("give-cookie-if-wallet-creation-fails");
 						String msg = "An error occurred while creating your wallet.";
 						if (cookie){
 							msg = "An error occurred while creating your wallet. Here's a cookie to make up for it. :)";
 							((Player)e.getViewers().get(0)).getInventory().addItem(new ItemStack(Material.COOKIE, 1));
 						}
 						((Player)e.getViewers().get(0)).sendMessage(ChatColor.RED + msg);
 					}
 				}
 			}
 		}
 		if (!e.getViewers().isEmpty()){
 			if (((Player)e.getViewers().get(0)).getGameMode() != GameMode.CREATIVE){
 				if (e.getInventory().getType() == InventoryType.CHEST){
 					String p = ((Player)e.getViewers().get(0)).getName();
 					int index = -1;
 					for (int i = 0; i < openingPlayer.length; i++){
 						if (openingPlayer[i] != null){
 							if (openingPlayer[i].equals(p)){
 								index = i;
 								break;
 							}
 						}
 					}
 					if (index != -1){
 						if (openType[index].equals("wallet")){
 							if (getConfig().getBoolean("only-gold-in-wallets")){
 								if (!(e.getCursor().getType() == Material.GOLD_BLOCK || e.getCurrentItem().getType() == Material.GOLD_BLOCK) && 
 										!(e.getCursor().getType() == Material.GOLD_INGOT || e.getCurrentItem().getType() == Material.GOLD_INGOT) && 
 										!(e.getCursor().getType() == Material.GOLD_NUGGET || e.getCurrentItem().getType() == Material.GOLD_NUGGET)){
 									e.setCancelled(true);
 								}
 							}
 							if (e.getCurrentItem().getType() == Material.BOOK || e.getCursor().getType() == Material.BOOK){
 								ItemStack is = null;
 								if (e.getCurrentItem().getType() == Material.BOOK){
 									is = e.getCurrentItem();
 								}
 								else if (e.getCurrentItem().getType() == Material.BOOK){
 									is = e.getCursor();
 								}
 								ItemMeta meta = is.getItemMeta();
 								if (meta.getDisplayName().equals("2Wallet"))
 									e.setCancelled(true);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void removeFromInv(Inventory inv, Material mat, int dmgValue, int amount){
 		if(inv.contains(mat)){
 			int remaining = amount;
 			ItemStack[] contents = inv.getContents();
 			for (ItemStack is : contents){
 				if (is != null){
 					if (is.getType() == mat){
 						if (is.getDurability() == dmgValue || dmgValue <= 0){
 							if(is.getAmount() > remaining){
 								is.setAmount(is.getAmount() - remaining);
 								remaining = 0;
 							}
 							else if(is.getAmount() <= remaining){
 								if (remaining > 0){
 									remaining -= is.getAmount();
 									is.setType(Material.AIR);
 								}
 							}
 						}
 					}
 				}
 			}
 			inv.setContents(contents);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public void removeFromPlayerInv(Player p, Material mat, int dmgValue, int amount){
 		removeFromInv(p.getInventory(), mat, dmgValue, amount);
 		p.updateInventory();
 	}
 }
