 package com.cyprias.csstats;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.Acrobot.ChestShop.Chests.ChestObject;
 import com.Acrobot.ChestShop.Chests.MinecraftChest;
 import com.Acrobot.ChestShop.Items.Items;
 import com.Acrobot.ChestShop.Utils.uInventory;
 import com.Acrobot.ChestShop.Utils.uSign;
 import com.cyprias.csstats.Database.adminShopStats;
 import com.cyprias.csstats.ItemDb.itemData;
 import com.cyprias.csstats.csStats.signShop;
 
 public class csStats extends JavaPlugin {
 	public static File folder = new File("plugins/csStats");
 	public static String chatPrefix = "f[acssf] ";
 	public String name;
 	public String version;
 	public Config config;
 	public Seller seller;
 	public ItemDb iDB;
 	public Events events;
 	public Warps warps;
 	public Database database;
 	public static Server server;
 
 	public static Logger log = Logger.getLogger("Minecraft");
 
 	private String stPluginEnabled = chatPrefix + "f%s 7vf%s 7is enabled.";
 	private csStats plugin;
 	public static Economy econ = null;
 
 	public void onEnable() {
 		plugin = this;
 		server = getServer();
 		name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 
 		// Load config and permissions
 		config = new Config(this);
 		database = new Database(this);
 		iDB = new ItemDb(this);
 		warps = new Warps(this);
 		events = new Events(this);
 		seller = new Seller(this);
 
 		server.getPluginManager().registerEvents(events, this);
 
 		log.info(String.format(stPluginEnabled, name, version));
 	}
 
 	public void info(String msg) {
 		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
 	}
 
 	private String getItemStatsMsg(Database.itemStats stats, int stackCount) {
 		int roundTo = 1;
 		if (stats.total == 0)
 			return "7sales: f" + stats.total;
 
 		return "7sales: f" + stats.total
 			+ "7, items: f"
 			+ Database.Round(stats.totalAmount, 0)
 			+
 			// "7, price: $f" + Database.Round(stats.avgPrice * stackCount,
 			// roundTo) + "/" + Database.Round(stats.median * stackCount,
 			// roundTo) + "/" + Database.Round(stats.mode * stackCount, roundTo)
 			"7, avg: $f" + Database.Round(stats.avgPrice * stackCount, roundTo) + "7, median: $f" + Database.Round(stats.median * stackCount, roundTo)
 			+ "7, mode: $f" + Database.Round(stats.mode * stackCount, roundTo);
 	}
 
 	public boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 		if (rsp == null) {
 			return false;
 		}
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 
 	public double getBalance(String pName) {
 		if (setupEconomy()) {
 			return econ.getBalance(pName.toLowerCase());
 		}
 		return 0;
 	}
 
 	public boolean payPlayer(String pName, double amount) {
 		pName = pName.toLowerCase();
 		if (setupEconomy()) {
 			// return econ.getBalance(pName);
 			if (!econ.hasAccount(pName))
 				econ.createPlayerAccount(pName);
 
 			econ.depositPlayer(pName.toLowerCase(), amount);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean debtPlayer(String pName, double amount) {
 		pName = pName.toLowerCase();
 		if (setupEconomy()) {
 
 			if (!econ.hasAccount(pName))
 				econ.createPlayerAccount(pName);
 
 			econ.withdrawPlayer(pName, amount);
 
 			return true;
 		}
 		return false;
 	}
 
 	public static String encodeEnchantment(Map<Enchantment, Integer> map) {
 		int integer = 0;
 		for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
 			integer = integer * 1000 + (entry.getKey().getId()) * 10 + entry.getValue();
 		}
 		return (integer != 0 ? Integer.toString(integer, 32) : null);
 	}
 
 	public List<statRequest> queuedRequests = new ArrayList<statRequest>();
 
 	static class statRequest {
 		CommandSender sender;
 		int itemID;
 		int dur;
 		int stack;
 	}
 
 	public boolean hasPermission(CommandSender sender, String node) {
 		if (!(sender instanceof Player)) {
 			return true;
 		}
 		Player player = (Player) sender;
 		if (player.isOp()) {
 			return true;
 		}
 
 		if (player.isPermissionSet(node)) {
 			return player.hasPermission(node);
 		}
 
 		return player.hasPermission("csstats.*");
 	}
 
 	private boolean command_admin_buys(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// database.adminShopBuys();
 		int seconds = 0;
 
 		ArrayList<Database.adminShopStats> all;
 		if (args.length == 3) {
 			seconds = Integer.parseInt(args[2]) * 60;
 			all = database.adminShopTrans(sender, getUnixTime() - seconds, true);
 		} else {
 			all = database.adminShopTrans(sender, 0, true);
 		}
 
 		// sender.sendMessage(chatPrefix + "Lenth: " + all.size());
 
 		for (adminShopStats k : all) { // d gets successively each value in ar.
 			sender.sendMessage(chatPrefix
 				+ String.format("f%s 7trans: f%s7, amount: f%s7, value: f$%s", iDB.getItemName(k.id, k.durability), k.transactions,
 					Database.Round(k.totalAmount, 0), Database.Round(k.totalPrice, 0)));
 
 		}
 
 		return true;
 	}
 
 	private boolean command_admin_sells(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// database.adminShopBuys();
 		int seconds = 0;
 
 		ArrayList<Database.adminShopStats> all;
 		if (args.length == 3) {
 			seconds = Integer.parseInt(args[2]) * 60;
 			all = database.adminShopTrans(sender, getUnixTime() - seconds, false);
 		} else {
 			all = database.adminShopTrans(sender, 0, false);
 		}
 
 		// sender.sendMessage(chatPrefix + "Lenth: " + all.size());
 
 		for (adminShopStats k : all) { // d gets successively each value in ar.
 			sender.sendMessage(chatPrefix
 				+ String.format("f%s 7trans: f%s7, amount: f%s7, value: f$%s", iDB.getItemName(k.id, k.durability), k.transactions,
 					Database.Round(k.totalAmount, 0), Database.Round(k.totalPrice, 0)));
 
 		}
 
 		return true;
 	}
 
 	public void command_player(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (args.length == 1) {
 			sender.sendMessage(chatPrefix + "7You need to include a player name.");
 			return;
 		}
 
 		String targetName = args[1];
 
 		ArrayList<Warps.warpInfo> sellerWarps = warps.getPlayerWarps(targetName);
 
 		if (sellerWarps.size() == 0) {
 			sender.sendMessage(chatPrefix + "7That seller has no warps.");
 			return;
 		}
 
 		sender.sendMessage(chatPrefix + String.format("f%s 7has the following public warps...", targetName));
 
 		for (int i = 0; i < sellerWarps.size(); i++) {
 			sender.sendMessage("    "
 				+ String.format("f%s7 in f%s 7at f%s %s %s", sellerWarps.get(i).name, sellerWarps.get(i).world, database.Round(sellerWarps.get(i).x, 0),
 					database.Round(sellerWarps.get(i).y, 0), database.Round(sellerWarps.get(i).z, 0))
 
 			);
 		}
 
 	}
 
 	public ItemDb.itemData getItemDataFromInput(String id) {
 		id = id.toLowerCase();
 
 		int itemid = 0;
 		String itemname = null;
 		short metaData = 0;
 		if (id.matches("^\\d+[:+',;.]\\d+$")) {
 			itemid = Integer.parseInt(id.split("[:+',;.]")[0]);
 			metaData = Short.parseShort(id.split("[:+',;.]")[1]);
 		} else if (id.matches("^\\d+$")) {
 			itemid = Integer.parseInt(id);
 		} else if (id.matches("^[^:+',;.]+[:+',;.]\\d+$")) {
 			itemname = id.split("[:+',;.]")[0].toLowerCase();
 			metaData = Short.parseShort(id.split("[:+',;.]")[1]);
 		} else {
 			itemname = id;
 		}
 
 		// info ("getItemDataFromInput itemname: " + itemname);
 		// info ("getItemDataFromInput itemid: " + itemid);
 		// info ("getItemDataFromInput metaData: " + metaData);
 
 		ItemDb.itemData iD = new ItemDb.itemData(itemname, itemid, metaData);
 
 		if (iD.itemID == 0 && itemname != null) {
 			iD = iDB.getItemID(itemname);
 		}
 		if (iD == null)
 			return null;
 
 		if (iD.itemName == null && itemid > 0) {
 			iD.itemName = iDB.getItemName(itemid, metaData);
 		}
 
 		return iD;
 	}
 
 	String boughtItem = "7Buying f%s7xf%s 7for $f%s7+f%s 7from f%s7.";
 
 	public static class buyInfo {
 		public buyInfo(int itemID2, short dur2, int amountWanted2) {
 			itemID = itemID2;
 			dur = dur2;
 			amountWanted = amountWanted2;
 		}
 
 		int itemID = 0;
 		short dur = 0;
 		int amountWanted = 0;
 	}
 
 	HashMap<String, buyInfo> lastBuyCommand = new HashMap<String, buyInfo>();
 
 	public void command_buy_run(Player player, int itemID, short dur, int amountWanted, boolean confirmed) {
 
 		lastBuyCommand.put(player.getName(), new buyInfo(itemID, dur, amountWanted));
 
 		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();
 
 		String world = player.getWorld().getName();
 
 		String SQL = "SELECT * FROM `" + plugin.database.shopTable
 			+ "` WHERE `TypeId` = ? AND `Durability` = ? AND `world` = ? AND `enchantments` = 0 ORDER BY unixtime DESC LIMIT 100";
 		// SQL = String.format(SQL, plugin.database.shopTable, itemID, dur);
 
 		// info("command_buy SQL:" + SQL);
 
 		List<signShop> shops = new ArrayList<signShop>();
 		Block block;
 		Chest chest;
 
 		try {
 			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
 			PreparedStatement statement = con.prepareStatement(SQL);
 
 			statement.setInt(1, itemID);
 			statement.setInt(2, dur);
 			statement.setString(3, world);
 
 			ResultSet result = statement.executeQuery();
 
 			int id, X, Y, Z;
 
 			int chestAmount;
 			Block blockbelow;
 			String owner;
 
 			boolean shopFound = false;
 
 			while (result.next()) {
 				shopFound = true;
 				id = result.getInt(1);
 				X = result.getInt(7);
 				Y = result.getInt(8);
 				Z = result.getInt(9);
 
 				// plugin.info("id " + id + " " + X + " " + Y + " " + Z);
 
 				block = server.getWorlds().get(0).getBlockAt(X, Y, Z);
 
 				Sign sign = (Sign) block.getState();
 
 				int signAmount = Integer.parseInt(sign.getLine(1));
 
 				float buyPrice = uSign.buyPrice(sign.getLine(2)) / signAmount;
 				float sellPrice = uSign.sellPrice(sign.getLine(2)) / signAmount;
 
 				if (buyPrice > 0) {
 
 					// plugin.info("buyPrice: " + buyPrice);
 					// plugin.info("sellPrice: " + sellPrice);
 					blockbelow = block.getRelative(0, -1, 0);
 					owner = sign.getLine(0);
 
 					if ((blockbelow.getState() instanceof Chest)) {
 
 						chest = (Chest) blockbelow.getState();
 						chestAmount = chestContainsItem(chest, itemID, dur);
 
 						if (chestAmount > 0) {
 							// plugin.info("Chest has item!");
 
 							shops.add(shops.size(), new signShop(world, X, Y, Z, itemID, dur, chestAmount, buyPrice, sellPrice, owner, chest));
 							//
 
 						}
 
 					}
 				}
 
 			}
 			result.close();
 			statement.close();
 			con.close();
 
 			if (shopFound == false) {
 				sendMessage(player, "No shops found containing " + plugin.iDB.getItemName(itemID, dur) + ".");
 
 				return;
 			}
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Sort cheapest first.
 		CompareShopBuyPrice comparator = new CompareShopBuyPrice();
 		Collections.sort(shops, comparator);
 
 		signShop shop;
 		ItemStack slot;
 		ItemStack items = new ItemStack(itemID, dur);
 		items.setDurability(dur);
 
 		Inventory inventory;
 		float price;
 		double taxAmount;
 		int bought = 0;
 		double totalPrice = 0;
 		int amountToBuy;
 
 		double taxPU; // tax per unit.
 		double totalPU; // total per unit.
 
 		int invAmount;
 		int totalBuying = 0;
 
 		double playerBalance = getBalance(player.getName());
 
 		for (int i = 0; i < shops.size() && amountWanted > 0; i++) {
 			// if (playerHasEmptySlot(player) == false) {
 			// sendMessage(player, "You need more empty slots.");
 			// break;
 			// }
 
 			shop = shops.get(i);
 
 			inventory = shop.chest.getInventory();
 
 			/*
 			 * for (int s = 0; s < inventory.getSize() && amountWanted > 0; s++)
 			 * { if (playerHasEmptySlot(player) == false) { break; }
 			 * 
 			 * 
 			 * slot = inventory.getItem(s);
 			 * 
 			 * 
 			 * 
 			 * 
 			 * 
 			 * if (slot != null) {
 			 * 
 			 * if (slot.getTypeId() == itemID && slot.getDurability() == dur) {
 			 * 
 			 * if (slot.getAmount() > amountWanted) {
 			 * slot.setAmount(slot.getAmount() - amountWanted);
 			 * 
 			 * price = (shop.buyPrice * amountWanted); taxAmount = price *
 			 * Config.convenienceTax;
 			 * 
 			 * if (getBalance(player.getName()) > (price+taxAmount)){ bought +=
 			 * 1; totalPrice += (price+taxAmount);
 			 * 
 			 * amountWanted = 0;
 			 * 
 			 * if (confirmed == true){ sendMessage(player,
 			 * String.format(boughtItem, amountWanted,
 			 * plugin.iDB.getItemName(itemID, dur), price,
 			 * Database.Round(taxAmount,2), shop.owner));
 			 * uInventory.add(player.getInventory(), items, amountWanted);
 			 * 
 			 * 
 			 * 
 			 * notifyOwnerOfPurchase(shop.owner, player, itemID, dur,
 			 * amountWanted, price); debtPlayer(player.getName(),
 			 * (price+taxAmount)); payPlayer(shop.owner, price); }
 			 * 
 			 * 
 			 * } break; } price = (shop.buyPrice * slot.getAmount()); taxAmount
 			 * = price * Config.convenienceTax;
 			 * 
 			 * //info(player.getName() + " balance: " +
 			 * getBalance(player.getName())); if (getBalance(player.getName()) >
 			 * (price+taxAmount)){ bought += 1; totalPrice += (price+taxAmount);
 			 * 
 			 * 
 			 * if (confirmed == true){ sendMessage(player,
 			 * String.format(boughtItem, slot.getAmount(),
 			 * plugin.iDB.getItemName(itemID, dur), price,
 			 * Database.Round(taxAmount,2), shop.owner));
 			 * debtPlayer(player.getName(), price+taxAmount);
 			 * payPlayer(shop.owner, price);
 			 * uInventory.add(player.getInventory(), items, slot.getAmount());
 			 * notifyOwnerOfPurchase(shop.owner, player, itemID, dur,
 			 * slot.getAmount(), price); inventory.removeItem(slot); }
 			 * amountWanted -= slot.getAmount();
 			 * 
 			 * }
 			 * 
 			 * 
 			 * }
 			 * 
 			 * }
 			 * 
 			 * }
 			 */
 
 			invAmount = uInventory.amount(inventory, items, dur);
 			// sendMessage(player, "Chest invAmount: " + invAmount);
 
 			if (invAmount > 0) {
 				totalPU = (shop.buyPrice * (1 + Config.convenienceTax));
 
 				// sendMessage(player, "Price per unit: " + shop.buyPrice);
 				// sendMessage(player, "Tax per unit: " + taxPU);
 				// sendMessage(player, "Total per unit: " + totalPU);
 
 				amountToBuy = (int) (playerBalance / totalPU); // How many we
 																// can afford.
 				// sendMessage(player, "amountCanAfford: " + amountToBuy);
 				amountToBuy = Math.min(amountToBuy, invAmount); // Max amount
 																// available.
 				// sendMessage(player, "max amount available: " + amountToBuy);
 				amountToBuy = Math.min(amountToBuy, amountWanted); // Max amount
 																	// wanted.
 				// sendMessage(player, "max amount wanted: " + amountToBuy);
 
 				amountToBuy = Math.min(amountToBuy, amountToBuy - uInventory.fits(player.getInventory(), items, amountToBuy, dur)); // Max
 																																	// amount
 																																	// able
 																																	// to
 																																	// fit.
 				// sendMessage(player, "max amount can fit: " + amountToBuy);
 
 				// sendMessage(player, "amountToBuy2: " + amountToBuy);
 
 				if (amountToBuy > 0) {
 					price = (shop.buyPrice * amountToBuy);
 					taxAmount = price * Config.convenienceTax;
 					playerBalance -= (price + taxAmount);
 					totalBuying += amountToBuy;
 
 					if (confirmed == true) {
 
 						// sendMessage(player, "Removing " + amountToBuy +
 						// " from " + shop.owner + "'s " + invAmount);
 
 						uInventory.remove(inventory, items, amountToBuy, dur);
 						uInventory.add(player.getInventory(), items, amountToBuy);
 
 						debtPlayer(player.getName(), price + taxAmount);
 
 						payPlayer(shop.owner, price);
 						
 					}
					sendMessage(player,
						String.format(boughtItem, plugin.iDB.getItemName(itemID, dur), amountToBuy, price, Database.Round(taxAmount, 2), shop.owner));
 
 					bought += 1;
 					totalPrice += (price + taxAmount);
 					amountWanted -= amountToBuy;
 				}
 
 			}
 
 		}
 
 		/**/
 		if (bought == 0) {
 			sendMessage(player, "7Unable to locate/afford f" + plugin.iDB.getItemName(itemID, dur) + "7.");
 		} else {
 			if (confirmed == false) {
				sendMessage(player, "7Attempting f" + bought + " 7transactions buying f"+plugin.iDB.getItemName(itemID, dur)+"7xf"+totalBuying+" 7valuing $f" + Database.Round(totalPrice,2) + "7.");
 				sendMessage(player, "7Type f/css confirm 7to make the purchase.");
 
 			} else {
				sendMessage(player, "7Made f" + bought + " 7transactions valuing $f" + Database.Round(totalPrice,2) + "7.");
 			}
 		}
 	}
 
 	//
 	public void command_confirm(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		if (!lastBuyCommand.containsKey(sender.getName())) {
 			plugin.sendMessage(sender, "Can't find your previous command to confirm, try again.");
 			return;
 		}
 
 		Player player;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage("7You need to be a player to use this command.");
 			return;
 		}
 
 		command_buy_run(player, lastBuyCommand.get(sender.getName()).itemID, lastBuyCommand.get(sender.getName()).dur,
 			lastBuyCommand.get(sender.getName()).amountWanted, true);
 
 		lastBuyCommand.remove(sender.getName());
 
 	}
 
 	public void command_buy(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		// 7
 		if (args.length == 1) {
 			sender.sendMessage(chatPrefix + "You need to add a itemID/name.");
 			return;
 		}
 
 		Player player;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage("7You need to be a player to use this command.");
 			return;
 		}
 
 		int itemID = 0;// = player.getItemInHand().getTypeId();
 		short dur = 0;// = player.getItemInHand().getDurability();
 		String itemName = null;// = iDB.getItemName(itemID, dur);
 
 		int amountWanted = 1;
 
 		if (args.length >= 2) {
 			ItemDb.itemData iD = getItemDataFromInput(args[1]);
 			if (iD == null) {
 				sender.sendMessage(chatPrefix + "Cannot find itemID for '" + args[1] + "', Try again.");
 				return;
 			}
 			itemID = iD.itemID;
 			dur = iD.itemDur;
 			itemName = iD.itemName;
 		}
 		if (args.length >= 3) {
 			amountWanted = Integer.parseInt(args[2].toString());
 		}
 
 		// info("command_buy itemID:" + itemID);
 		// info("command_buy dur:" + dur);
 		// info("command_buy itemName:" + itemName);
 		// info("command_buy amountWanted: " + amountWanted);
 
 		// ////////////////////////////////////////////////////
 
 		command_buy_run(player, itemID, dur, amountWanted, false);
 	}
 
 	public void notifyOwnerOfPurchase(String ownerName, Player user, int itemID, int dur, int Amount, double price) {
 		Player owner = server.getPlayer(ownerName);
 
 		if (owner != null) {
 			String notifyBuy = "f%s 7bought f%s %s7 7($f%s7) 7from you.";
 			plugin.sendMessage(owner, String.format(notifyBuy, user.getDisplayName(), Amount, plugin.iDB.getItemName(itemID, dur), Database.Round(price, 2)));
 
 		}
 
 	}
 
 	public boolean playerHasEmptySlot(Player player) {
 
 		PlayerInventory inventory = player.getInventory();
 
 		for (int s = 0; s < inventory.getSize(); s++) {
 			if (inventory.getItem(s) == null)
 				return true;
 		}
 		return false;
 	}
 
 	public class CompareShopBuyPrice implements Comparator<signShop> {
 
 		@Override
 		public int compare(signShop o1, signShop o2) {
 			// TODO Auto-generated method stub
 			double rank1 = o1.buyPrice;
 			double rank2 = o2.buyPrice;
 
 			if (rank1 > rank2) {
 				return +1;
 			} else if (rank1 < rank2) {
 				return -1;
 			} else {
 				return 0;
 			}
 		}
 	}
 
 	public void sendMessage(CommandSender sender, String message) {
 		// final String message = Util.getFinalArg(string, 0);
 		if (sender instanceof Player) {
 			info(sender.getName() + "->" + message);
 		}
 		sender.sendMessage(chatPrefix + message);
 	}
 
 	public static class signShop {
 		public signShop(String world2, int x2, int y2, int z2, int itemID2, short dur2, int chestAmount, float buyPrice2, float sellPrice2, String owner2,
 			Chest chest2) {
 			world = world2;
 			X = x2;
 			Y = y2;
 			Z = z2;
 			itemID = itemID2;
 			dur = dur2;
 			amount = chestAmount;
 			buyPrice = buyPrice2;
 			sellPrice = sellPrice2;
 			owner = owner2;
 			chest = chest2;
 		}
 
 		int X = 0;
 		int Y = 0;
 		int Z = 0;
 		String world = "world";
 		int amount = 0;
 		float buyPrice = -1;
 		float sellPrice = -1;
 		int itemID = 0;
 		int dur = 0;
 		String owner = "Unknown";
 		Chest chest;
 
 		// public signShop(int id2, int Idurability, int i, double price, int
 		// amount) {
 		// id = id2;
 		// durability = Idurability;
 		// transactions = i;
 		// totalPrice = price;
 		// totalAmount = amount;
 		// }
 	}
 
 	public int chestContainsItem(Chest chest, int itemID, short itemDur) {
 		ItemStack slot;
 		int size = 0;
 
 		for (int i = 0; i < chest.getInventory().getSize(); i++) {
 			slot = chest.getInventory().getItem(i);
 			if (slot != null) {
 				if (slot.getTypeId() == itemID && slot.getDurability() == itemDur)
 					size += slot.getAmount();
 			}
 		}
 		return size;
 	}
 
 	public void command_buyers(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage("7You need to be a player to use this command.");
 			return;
 		}
 
 		int itemID = player.getItemInHand().getTypeId();
 		int dur = player.getItemInHand().getDurability();
 		String itemName = iDB.getItemName(itemID, dur);
 
 		if (args.length == 2) {
 			ItemDb.itemData iD = getItemDataFromInput(args[1]);
 			if (iD == null) {
 				sender.sendMessage(chatPrefix + "Cannot find itemID for '" + args[1] + "', Try again.");
 				return;
 			}
 			itemID = iD.itemID;
 			dur = iD.itemDur;
 			itemName = iD.itemName;
 		}
 
 		ArrayList<Seller.sellerInfo> recentTrades = seller.getRecentBuyers(sender, itemID, dur);
 
 		if (recentTrades.size() == 0) {
 			sender.sendMessage(chatPrefix + String.format("7No one has bought f%s 7yet.", itemName));
 			return;
 		}
 
 		sender.sendMessage(chatPrefix + String.format("7The following users have bought f%s7.", itemName));
 
 		for (int i = 0; i < recentTrades.size(); i++) {
 			if (recentTrades.get(i).pricePerUnit >= 0.01) {
 				sender.sendMessage(chatPrefix
 					+ String.format("f%s7: f$%s 7per unit f%s 7ago.", recentTrades.get(i).shop_owner,
 						Database.Round(recentTrades.get(i).pricePerUnit, 2), secondsToString(recentTrades.get(i).age)));
 			} else {
 				sender.sendMessage(chatPrefix
 					+ String.format("f%s7: f$%s 7per 64 f%s 7ago.", recentTrades.get(i).shop_owner,
 						Database.Round(recentTrades.get(i).pricePerUnit * 64, 2), secondsToString(recentTrades.get(i).age)));
 			}
 
 		}
 		// dPrices[i1] = prices.get(i1);
 
 	}
 
 	public void command_sellers(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage("7You need to be a player to use this command.");
 			return;
 		}
 
 		int itemID = player.getItemInHand().getTypeId();
 		int dur = player.getItemInHand().getDurability();
 		String itemName = iDB.getItemName(itemID, dur);
 
 		if (args.length == 2) {
 			ItemDb.itemData iD = getItemDataFromInput(args[1]);
 			if (iD == null) {
 				sender.sendMessage(chatPrefix + "Cannot find itemID for '" + args[1] + "', Try again.");
 				return;
 			}
 			itemID = iD.itemID;
 			dur = iD.itemDur;
 			itemName = iD.itemName;
 		}
 
 		ArrayList<Seller.sellerInfo> recentSellers = seller.getRecentSellers(sender, itemID, dur);
 
 		if (recentSellers.size() == 0) {
 			sender.sendMessage(chatPrefix + String.format("7No one has sold f%s 7yet.", itemName));
 			return;
 		}
 
 		sender.sendMessage(chatPrefix + String.format("7The following users have sold f%s7.", itemName));
 
 		for (int i = 0; i < recentSellers.size(); i++) {
 			if (recentSellers.get(i).pricePerUnit >= 0.01) {
 				sender.sendMessage(chatPrefix
 					+ String.format("f%s7: f$%s 7per unit f%s 7ago.", recentSellers.get(i).shop_owner,
 						Database.Round(recentSellers.get(i).pricePerUnit, 2), secondsToString(recentSellers.get(i).age)));
 			} else {
 				sender.sendMessage(chatPrefix
 					+ String.format("f%s7: f$%s 7per 64 f%s 7ago.", recentSellers.get(i).shop_owner,
 						Database.Round(recentSellers.get(i).pricePerUnit * 64, 2), secondsToString(recentSellers.get(i).age)));
 			}
 
 		}
 		// dPrices[i1] = prices.get(i1);
 
 	}
 
 	static class cmdRequest {
 		CommandSender sender;
 		Command cmd;
 		String commandLabel;
 		String[] args;
 	}
 
 	public ArrayList<cmdRequest> queuedCommands = new ArrayList<cmdRequest>();
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		cmdRequest newCmd = new cmdRequest();
 		newCmd.sender = sender;
 		newCmd.cmd = cmd;
 		newCmd.commandLabel = commandLabel;
 		newCmd.args = args;
 
 		queuedCommands.add(newCmd);
 
 		this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 			public void run() {
 
 				for (int i = queuedCommands.size() - 1; i >= 0; i--) {
 					try {
 						commandHandler(queuedCommands.get(i).sender, queuedCommands.get(i).cmd, queuedCommands.get(i).commandLabel, queuedCommands.get(i).args);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					queuedCommands.remove(i);
 				}
 			}
 		}, 0L);
 		return true;
 	}
 
 	/**/
 	public boolean commandHandler(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		String senderName = "[SERVER]";
 		if (sender instanceof Player) {
 			player = (Player) sender;
 			senderName = player.getDisplayName();
 		}
 		final String message = getFinalArg(args, 0);
 		info(senderName + " " + cmd.getName() + ": " + message.toString());
 
 		if (cmd.getName().equalsIgnoreCase("css")) {
 			if (args.length == 0) {
 
 				// sender.sendMessage(chatPrefix + "/css test");
 				sender.sendMessage(chatPrefix + "  aChestShop Stats");
 				sender.sendMessage(chatPrefix + "/css stats [itemID/Name] [stackSize]");
 				sender.sendMessage(chatPrefix + "/css sellers [itemID] - Who sells the item in your hand.");
 				sender.sendMessage(chatPrefix + "/css buyers [itemID] - Who buys the item in your hand.");
 				sender.sendMessage(chatPrefix + "/css seller <player> - Public warps for that player.");
 				sender.sendMessage(chatPrefix + "/css buy [itemID] <count> - Buy that item from the cheapest shop available.");
 
 				if (hasPermission(sender, "css.admin"))
 					sender.sendMessage(chatPrefix + "/css admin");
 
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("db") && hasPermission(sender, "csstats.test")) {
 				database.connectToDB();
 				return true;
 			} else if (args[0].equalsIgnoreCase("price") && hasPermission(sender, "csstats.price")) {
 				if (args.length == 1) {
 					sender.sendMessage("7You need to include a itemID. /css price 4");
 					return true;
 				}
 				//
 				// log.info("item: " + itemID,dur );
 
 				database.tellItemPrice(sender, Integer.parseInt(args[1]));
 
 				return true;
 			} else if (args[0].equalsIgnoreCase("reload") && hasPermission(sender, "csstats.reload")) {
 				config.reloadOurConfig();
 				sender.sendMessage(chatPrefix + " plugin reloaded.");
 				return true;
 			} else if (args[0].equalsIgnoreCase("admin") && hasPermission(sender, "csstats.admin")) {
 				if (args.length == 1) {
 					sender.sendMessage("/css admin buys [lastMinutes] - Stats on admin shop buys.");
 					sender.sendMessage("/css admin sells [lastMinutes] - Stats on admin shop sells.");
 					return true;
 				}
 
 				if (args[1].equalsIgnoreCase("buys")) {
 					command_admin_buys(sender, cmd, commandLabel, args);
 					return true;
 				}
 				if (args[1].equalsIgnoreCase("sells")) {
 					command_admin_sells(sender, cmd, commandLabel, args);
 					return true;
 				}
 
 			} else if (args[0].equalsIgnoreCase("sellers")) {
 				command_sellers(sender, cmd, commandLabel, args);
 				return true;
 			} else if (args[0].equalsIgnoreCase("buyers")) {
 				command_buyers(sender, cmd, commandLabel, args);
 				return true;
 			} else if (args[0].equalsIgnoreCase("player")) {
 				command_player(sender, cmd, commandLabel, args);
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("buy") && hasPermission(sender, "csstats.buy")) {
 				command_buy(sender, cmd, commandLabel, args);
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("confirm")) {
 				command_confirm(sender, cmd, commandLabel, args);
 				return true;
 
 				//
 			} else if (args[0].equalsIgnoreCase("stats")) {
 
 				int itemID = player.getItemInHand().getTypeId();
 				int dur = player.getItemInHand().getDurability();
 				int stack = player.getItemInHand().getAmount();
 				String itemName = null;
 
 				if (args.length >= 2) {
 
 					if (args[1].equalsIgnoreCase("help")) {
 						sender.sendMessage(chatPrefix + "/css stats [stack] [itemID]");
 						return true;
 					}
 
 					// ItemDb.itemData iD =
 					// iDB.getItemID(args[1].toLowerCase());
 					ItemDb.itemData iD = getItemDataFromInput(args[1]);
 
 					if (iD != null) {
 						itemID = iD.itemID;
 						dur = iD.itemDur;
 						stack = 1;
 						itemName = iD.itemName;
 					} else {
 						sender.sendMessage(chatPrefix + "Cannot find ID for '" + args[1] + "', try again.");
 						return true;
 					}
 
 				}
 
 				if (args.length >= 3) {
 					/*
 					 * ItemDb.itemData iD =
 					 * iDB.getItemID(args[2].toLowerCase());
 					 * 
 					 * if (iD != null){ itemID = iD.itemID; dur = iD.itemDur;
 					 * stack = 1; }else{
 					 * 
 					 * if (args[2].contains(":")) { String[] temp =
 					 * args[1].split(":"); itemID = Integer.parseInt(temp[0]);
 					 * dur = Integer.parseInt(temp[1]); } else { itemID =
 					 * Integer.parseInt(args[2]); } }
 					 */
 
 					if (args[2].matches("^\\d+$")) {
 						stack = Integer.parseInt(args[2]);
 					}
 
 					/*
 					 * ItemDb.itemData iD = getItemDataFromInput(args[2]); if
 					 * (iD == null) { sender.sendMessage(chatPrefix +
 					 * "Cannot find itemID for '" + args[2] + "', Try again.");
 					 * return true; } itemID = iD.itemID; dur = iD.itemDur;
 					 */
 				}
 
 				sender.sendMessage(chatPrefix + String.format("7item: f%s7, stack: f%s", iDB.getItemName(itemID, dur), stack));
 				// }
 				Database.itemStats stats;
 
 				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24));
 				// if (stats.total > 0)
 				sender.sendMessage("1d " + getItemStatsMsg(stats, stack));
 
 				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 3));
 				// if (stats.total > 0)
 				sender.sendMessage("3d " + getItemStatsMsg(stats, stack));
 
 				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 7));
 				// if (stats.total > 0)
 				sender.sendMessage("1w " + getItemStatsMsg(stats, stack));
 
 				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 30));
 				// if (stats.total > 0)
 				sender.sendMessage("1m " + getItemStatsMsg(stats, stack));
 
 				stats = database.getItemStats(itemID, dur, 0);
 				// if (stats.total > 0)
 				sender.sendMessage("all " + getItemStatsMsg(stats, stack));
 
 				/*
 				 * newReq.sender = sender; newReq.itemID = itemID; newReq.dur =
 				 * dur; newReq.stack = stack;
 				 * 
 				 * queuedRequests.add(newReq);
 				 * 
 				 * this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 				 * new Runnable() { public void run() { Database.itemStats
 				 * stats;
 				 * 
 				 * for (int i = queuedRequests.size() - 1; i >= 0; i--) { //
 				 * plugin.getServer().broadcastMessage(i + ": " + //
 				 * usedCommands.get(i).playerName + " said " + //
 				 * usedCommands.get(i).message);
 				 * 
 				 * try { //
 				 * plugin.database.commandUsed(queuedRequests.get(i).player, //
 				 * queuedRequests.get(i).message);
 				 * 
 				 * // if //
 				 * (!Integer.valueOf(queuedRequests.get(i).dur).equals(0)){ //
 				 * queuedRequests.get(i).sender.sendMessage(String.format(
 				 * "7item: f%s7:f%s7, stack: f%s", //
 				 * queuedRequests.get(i).itemID, // queuedRequests.get(i).dur,
 				 * // queuedRequests.get(i).stack)); // }else{
 				 * queuedRequests.get
 				 * (i).sender.sendMessage(String.format("item: %s, stack: %s",
 				 * iDB.getItemName(queuedRequests.get(i).itemID,
 				 * queuedRequests.get(i).dur), queuedRequests.get(i).stack)); //
 				 * }
 				 * 
 				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
 				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24));
 				 * // if (stats.total > 0)
 				 * queuedRequests.get(i).sender.sendMessage("1d " +
 				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
 				 * 
 				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
 				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24 *
 				 * 3)); // if (stats.total > 0)
 				 * queuedRequests.get(i).sender.sendMessage("3d " +
 				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
 				 * 
 				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
 				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24 *
 				 * 7)); // if (stats.total > 0)
 				 * queuedRequests.get(i).sender.sendMessage("1w " +
 				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
 				 * 
 				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
 				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24 *
 				 * 30)); // if (stats.total > 0)
 				 * queuedRequests.get(i).sender.sendMessage("1m " +
 				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
 				 * 
 				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
 				 * queuedRequests.get(i).dur, 0); // if (stats.total > 0)
 				 * queuedRequests.get(i).sender.sendMessage("all " +
 				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
 				 * 
 				 * } catch (Exception e) { // TODO Auto-generated catch block
 				 * e.printStackTrace(); }
 				 * 
 				 * queuedRequests.remove(i); }
 				 * 
 				 * } }, 0L);
 				 */
 				return true;
 
 			}
 		}
 
 		return false;
 
 	}
 
 	public static String getFinalArg(final String[] args, final int start) {
 		final StringBuilder bldr = new StringBuilder();
 		for (int i = start; i < args.length; i++) {
 			if (i != start) {
 				bldr.append(" ");
 			}
 			bldr.append(args[i]);
 		}
 		return bldr.toString();
 	}
 
 	public long getUnixTime() {
 		return System.currentTimeMillis() / 1000L;
 	}
 
 	public String secondsToString(long totalSeconds) {
 
 		long days = totalSeconds / 86400;
 		long remainder = totalSeconds % 86400;
 
 		long hours = remainder / 3600;
 		remainder = totalSeconds % 3600;
 		long minutes = remainder / 60;
 		long seconds = remainder % 60;
 
 		if (days > 0)
 			return days + "d" + hours + "h" + minutes + "m" + seconds + "s";
 		else if (hours > 0)
 			return hours + "h" + minutes + "m" + seconds + "s";
 		else if (minutes > 0)
 			return minutes + "m" + seconds + "s";
 		else
 			return seconds + "s";
 	}
 }
