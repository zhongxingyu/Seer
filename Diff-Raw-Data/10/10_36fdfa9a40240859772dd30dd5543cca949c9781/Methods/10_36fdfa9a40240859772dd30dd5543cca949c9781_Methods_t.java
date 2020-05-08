 package com.mistphizzle.donationpoints.plugin;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class Methods {
 
 	static DonationPoints plugin;
 
 	public Methods(DonationPoints instance) {
 		plugin = instance;
 	}
 	
 	public static HashMap<String, Double> accounts = new HashMap<String, Double>();
 	
 	public static void loadAccounts() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.playerTable);
 		try {
 			if (!rs2.next()) {
 				return;
 			}
 			do {
 				accounts.put(rs2.getString("player"), rs2.getDouble("balance"));
 			} while (rs2.next());
 		}  catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static Double getBalance(String accountName) {
 		return accounts.get(accountName);
 //		if (DBConnection.engine.equalsIgnoreCase("mysql") | DBConnection.engine.equalsIgnoreCase("sqlite")) {
 //			ResultSet rs2 = DBConnection.sql.readQuery("SELECT balance FROM " + DBConnection.playerTable + " WHERE player LIKE '" + accountName.toLowerCase() + "';");
 //			try {
 //				if (rs2.next()) {
 //					Double balance = rs2.getDouble("balance");
 //					Double balance2 = Methods.roundTwoDecimals(balance);
 //					return balance2;
 //				}
 //			} catch (SQLException e) {
 //				e.printStackTrace();
 //			}
 //		}
 //		return null;
 	}
 
 	public static boolean hasAccount(String accountName) {
 		return accounts.containsKey(accountName);
 //		if (DBConnection.engine.equalsIgnoreCase("mysql") | DBConnection.engine.equalsIgnoreCase("sqlite")) {
 //			ResultSet rs2 = DBConnection.sql.readQuery("SELECT player FROM " + DBConnection.playerTable + " WHERE player = '" + accountName.toLowerCase() + "';");
 //			try {
 //				if (rs2.next()) {
 //					return true;
 //				} else {
 //					return false;
 //				}
 //			} catch (SQLException e) {
 //				e.printStackTrace();
 //			}
 //		}
 //		return true;
 	}
 	
 	public static void createAccount(String accountName) {
 		DBConnection.sql.modifyQuery("INSERT INTO " + DBConnection.playerTable + "(player, balance) VALUES ('" + accountName.toLowerCase() + "', 0)");
 		accounts.put(accountName.toLowerCase(), 0.0);
 	}
 
 	public static void addPoints (Double amount, String accountName) {
 		Double balance = accounts.get(accountName);
 		DBConnection.sql.modifyQuery("UPDATE " + DBConnection.playerTable + " SET balance = balance + " + amount + " WHERE player = '" + accountName.toLowerCase() + "';");
 		accounts.put(accountName, balance + amount);
 	}
 
 	public static void removePoints (Double amount, String accountName) {
 		Double balance = accounts.get(accountName);
 		DBConnection.sql.modifyQuery("UPDATE " + DBConnection.playerTable + " SET balance = balance - " + amount + " WHERE player = '" + accountName.toLowerCase() + "';");
 		accounts.put(accountName, balance - amount);
 	}
 	public static void setPoints (Double amount, String accountName) {
 		DBConnection.sql.modifyQuery("UPDATE " + DBConnection.playerTable + " SET balance = " + amount + " WHERE player = '" + accountName.toLowerCase() + "';");
 		accounts.put(accountName, amount);
 	}
 
 	public static void logTransaction(String player, Double price, String packageName, String date, String activated, String expires, String expiredate, String expired, String server) {
 		DBConnection.sql.modifyQuery("INSERT INTO " + DBConnection.transactionTable + "(player, package, price, date, activated, expires, expiredate, expired, server) VALUES ('" + player + "', '" + packageName + "', " + price + ", '" + date + "', '" + activated + "', '" + expires + "', '" + expiredate + "', '" + expired + "', '" + server + "');");
 	}
 	
 	public static Date getCurrentDateAsDate() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		Date date = new Date();
 		String date2 = dateFormat.format(date);
 		try {
 			return dateFormat.parse(date2);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	public static void checkForExpiredPackages() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable + " WHERE expired = 'false';");
 		try {
 			if (!rs2.next()) return;
 			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			do {
 				String expireDate = rs2.getString("expiredate");
 				if ("expiredate" != null) { // The package will expire.
 					Date currentDate = Methods.getCurrentDateAsDate();
 					Date expireDate2 = null;
 					try {
 						expireDate2 = dateFormat.parse(expireDate);
 					} catch (ParseException e) {
 						DonationPoints.log.info("Unable to parse date: " + expireDate + ". Skipping Entry.");
 					}
 					if (expireDate2 == null) continue;
 					long timeUntilExpiration = (expireDate2.getTime() - currentDate.getTime()); // Returns time
 					if (timeUntilExpiration <= 0) {
 						String user = rs2.getString("player");
 						String pack = rs2.getString("package");
 						List<String> commands = plugin.getConfig().getStringList("packages." + pack + ".expirecommands");
 						for (String cmd: commands) {	
 							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", user));
 						}
 						DBConnection.sql.modifyQuery("UPDATE " + DBConnection.transactionTable + " SET expired = 'true' WHERE player = '" + user + "' AND package = '" + pack + "';");
 						DonationPoints.log.info("Expired " + user + "'s " + pack + " package.");
 					}
 				}
 			} while (rs2.next());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static boolean NeedActive(String player, String packageName) {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable + " WHERE player LIKE '" + player + "' AND package = '" + packageName + "' AND activated = 'false';");
 		try {
 			if (rs2.next()) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	public static String getCurrentDate() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		
 		Date date = new Date();
 		return dateFormat.format(date);
 	}
 
 	public static String colorize(String message) {
 		return message.replaceAll("(?i)&([a-fk-or0-9])", "\u00A7$1");
 	}
 
 	public static boolean hasPurchased(String player, String packName, String server) {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable + " WHERE player = '" + player + "' AND package = '" + packName + "' AND SERVER = '" + server + "';");
 		try {
 			if (rs2.next()) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	public static double roundTwoDecimals(double d) {
 		DecimalFormat twoDForm = new DecimalFormat("#.##");
 		return Double.valueOf(twoDForm.format(d));
 	}
 
 	public static void purgeEmptyAccounts() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.playerTable + " WHERE balance = 0");
 		try {
 			if (rs2.next()) {
 				DBConnection.sql.modifyQuery("DELETE FROM " + DBConnection.playerTable + " WHERE balance = 0");
 			}
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public static boolean getPackageExists(String pack) {
 		if (DonationPoints.instance.getConfig().getDouble("packages." + pack + ".price") == 0) {
 			return false;
 		}
 		return true;
 	}
 
 	public static void linkFrame(String pack, Double x, Double y, Double z, String world, String server) {
 		DBConnection.sql.modifyQuery("INSERT INTO " + DBConnection.frameTable + " (x,y,z,world,package,server) VALUES ('" + x + "','" + y + "','" + z + "','" + world + "','" + pack + "', '" + server + "');");
 	}
 
 	public static boolean isFrameLinked(Double x, Double y, Double z, String world, String server) {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.frameTable + " WHERE x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "' AND world = '" + world + "' AND server = '" + server + "';");
 		try {
 			if (rs2.next()) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static String getLinkedPackage(Double x, Double y, Double z, String world, String server) {
 		String linkedPack = null;
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT package FROM " + DBConnection.frameTable + " WHERE x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "' AND world = '" + world + "' AND server = '" + server + "';");
 		try {
 			if (rs2.next()) {
 				linkedPack = rs2.getString("package");
 				return linkedPack;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return linkedPack;
 	}
 	
 	public static void deleteAccount(String accountName) {
 		DBConnection.sql.modifyQuery("DELETE FROM " + DBConnection.playerTable + " WHERE player = '" + accountName + "';");
 		accounts.remove(accountName);
 	}
 	
 	public static void unlinkFrame(Double x, Double y, Double z, String world, String server) {
 		DBConnection.sql.modifyQuery("DELETE FROM " + DBConnection.frameTable + " WHERE x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "' AND world = '" + world + "' AND server = '"+ server + "';");
 	}
 	
 	public static boolean hasInventorySpace(Player player, int requiredSlots) {
 		int emptySlots = 0;
 		for (ItemStack i : player.getInventory().getContents()) {
 			if (i == null) {
 				emptySlots++;
 			}
 		}
 		if (emptySlots >= requiredSlots) {
 			return true;
 		}
 		return false;
 	}
 	
 	public static void createExamplePackage() {
 		List<String> exampleCommands = new ArrayList<String>();
 		exampleCommands.add("say %player has purchased the Example Package.");
 		List<String> expireCommands = new ArrayList<String>();
 		expireCommands.add("say %player's Example package has expired.");
 		
 		plugin.getConfig().set("packages.ExamplePackage.price", 100);
 		plugin.getConfig().set("packages.ExamplePackage.description", "This is an exmaple package.");
 		plugin.getConfig().set("packages.ExamplePackage.haslimit", false);
 		plugin.getConfig().set("packages.ExamplePackage.limit", 3);
 		plugin.getConfig().set("packages.ExamplePackage.activateimmediately", true);
 		plugin.getConfig().set("packages.ExamplePackage.expires", false);
 		plugin.getConfig().set("packages.ExamplePackage.expiretime", 3);
 		plugin.getConfig().set("packages.ExamplePackage.commands", exampleCommands);
		plugin.getConfig().set("packages.ExamplePackage.expirecommands", expireCommands);
 		plugin.getConfig().set("packages.ExamplePackage.requireprerequisite", false);
 		plugin.getConfig().set("packages.ExamplePackage.RequiredInventorySpace", 0);
 		
 		plugin.saveConfig();
 	}
 }
