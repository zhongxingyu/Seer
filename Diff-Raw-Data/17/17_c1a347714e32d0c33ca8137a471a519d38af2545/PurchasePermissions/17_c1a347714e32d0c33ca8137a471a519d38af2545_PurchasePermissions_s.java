 package com.cyprias.purchasepermissions;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 //import com.cyprias.purchasepermissions.economy;
 public class PurchasePermissions extends JavaPlugin {
 	public static File folder = new File("plugins/PurchasePermissions");
 
 	public static String chatPrefix = "f[aPPf] ";
 
 	public static String perm_create = "purchasepermissions.create";
 	public static String perm_modify = "purchasepermissions.modify";
 	public static String perm_remove = "purchasepermissions.remove";
 	public static String perm_loadlocales = "purchasepermissions.loadlocales";
 
 	Logger log = Logger.getLogger("Minecraft");
 	public String name;
 	public String version;
 	public Config config;
 	public Database database;
 	public PlayerListener playerListener;
 	public Localization localization;
 
 	// private HashMap<String, String> L; // = localization.L;
 
 	public static Server server;
 
 	public static Economy econ = null;
 
 	HashMap<String, PermissionAttachment> activePermissions;
 
 	public static HashMap<String, PermissionAttachment> permissions = new HashMap<String, PermissionAttachment>();
 
 	public void onEnable() {
 		server = getServer();
 		name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 
 		// Load config and permissions
 		config = new Config(this);
 		database = new Database(this);
 		playerListener = new PlayerListener(this);
 		localization = new Localization(this);
 		// L=localization.L;
 
 		// Register stuff
 		getServer().getPluginManager().registerEvents(playerListener, this);
 
 		try {
 			database.setupMysql();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 
 			public void run() {
 				// System.out.println("This message is printed by an async thread");
 				try {
 					database.cleanOldEntries();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}, 100L, 200L); // 1200
 
 		for (Player p : getServer().getOnlinePlayers()) {
 			registerPlayer(p);
 		}
 
 		log.info(F("stPluginEnabled", name, version));
 	}
 
 	protected void registerPlayer(Player player) {
 		// log.info(chatPrefix + "Registering " + player.getName());
 		if (permissions.containsKey(player.getName())) {
 			log.info(chatPrefix + "Registering " + player.getName() + ": was already registered");
 			unregisterPlayer(player);
 		}
 		PermissionAttachment attachment = player.addAttachment(this);
 
 		permissions.put(player.getName(), attachment);
 
 		try {
 			database.retrieveActivePermissions(player);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void unregisterPlayer(Player player) {
 		// log.info(chatPrefix + "Unegistering " + player.getName());
 		if (permissions.containsKey(player.getName())) {
 
 			try {
 				database.removeActivePermissions(player.getName());
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			try {
 				player.removeAttachment(permissions.get(player.getName()));
 			} catch (IllegalArgumentException ex) {
 				log.info("Unregistering " + player.getName() + ": player did not have attachment");
 			}
 
 			permissions.remove(player.getName());
 		}
 	}
 
 	public void resetAllPermissions() {
 		for (Player p : getServer().getOnlinePlayers()) {
 			resetPlayerPermissions(p);
 		}
 	}
 
 	public void resetPlayerPermissions(Player player) {
 		String playerName = player.getName();
 		if (permissions.containsKey(playerName)) {
 
 			try {
 				// log.info(playerName +
 				// " changed worlds, reloading permissions");
 				database.removeActivePermissions(playerName);
 				database.retrieveActivePermissions(player);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void onDisable() {
 		for (Player p : getServer().getOnlinePlayers()) {
 			unregisterPlayer(p);
 		}
 		log.info(chatPrefix + name + " v" + version + " is disabled.");
 
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
 			return econ.getBalance(pName);
 		}
 		return 0;
 	}
 
 	public boolean payPlayer(String pName, double amount) {
 		if (setupEconomy()) {
 			// return econ.getBalance(pName);
 
 			econ.depositPlayer(pName, amount);
 			return true;
 		}
 		return false;
 	}
 
 	public void notifyUsersOfPurchase(Player sender, String permissionName, String target) {
 		for (Player oPlayer : getServer().getOnlinePlayers()) {
 			if (sender != oPlayer) {
 				if (oPlayer.hasPermission(Config.notifyPurchase)) {
 
 					oPlayer.sendMessage(String.format(L("stPurchaseNotify"), sender.getDisplayName(), permissionName, target));
 					// } else {
 					// log.info(oPlayer.getName() + " does not have access to "
 					// +
 					// Config.notifyPurchase);
 
 				}
 			}
 
 		}
 	}
 
 	public boolean canBuyPermission(CommandSender sender, Config.permissionInfo info) {
 		if (!(sender instanceof Player)) {
 			return true;
 		}
 		Player player = (Player) sender;
 		if (player.isOp()) {
 			return true;
 		}
 
 		//log.info("canBuyPermission  info.name: " + info.name);
 		//log.info("canBuyPermission  config.useBuyPermission: " + config.useBuyPermission);
 		//log.info("canBuyPermission  info.requirebuypermission: " + info.requirebuypermission);
 
 		if (config.useBuyPermission == false && info.requirebuypermission == false) {
 			// no buy permission required to buy this permission.
 			return true;
 		}
 
 		String node = "purchasepermissions.buy." + info.name;
 		// log.info("node: " + node);
 		// log.info("isPermissionSet: " + player.isPermissionSet(node));
 		// log.info("hasPermission: " + player.hasPermission(node));
 
 		if (player.isPermissionSet(node)) {
 			return player.hasPermission(node);
 		}
 
 		return (info.requirebuypermission == false && player.hasPermission("purchasepermissions.buy.*"));
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 
 		String senderName = "[CONSOLE]";
 
 		if (sender instanceof Player) {
 			player = (Player) sender;
 			senderName = player.getDisplayName();
 		}
 
 		final String message = getFinalArg(args, 0);
 
 		log.info(chatPrefix + senderName + " " + cmd.getName() + ": " + message.toString());
 
 		if (cmd.getName().equalsIgnoreCase("pp")) {
 			if (args.length == 0) {
 				// sender.sendMessage(chatPrefix + "Include a message.");
 				// log.info(chatPrefix + "/pp list - List all permissions.");
 
 				sender.sendMessage(chatPrefix + L("stAvailableCommands1"));
 				sender.sendMessage(chatPrefix + L("stAvailableCommands2"));
 				sender.sendMessage(chatPrefix + L("stAvailableCommands3"));
 				sender.sendMessage(chatPrefix + L("stAvailableCommands4"));
 
 				if (player.hasPermission(perm_create))
 					player.sendMessage(chatPrefix + L("stAvailableCommandsCreate"));
 				if (player.hasPermission(perm_modify))
 					player.sendMessage(chatPrefix + L("stAvailableCommandsModify"));
 				if (player.hasPermission(perm_remove))
 					player.sendMessage(chatPrefix + L("stAvailableCommandsRemove"));
 
 				return true;
 			}
 
 			if (args[0].equalsIgnoreCase("list")) {
 
 				Set<String> permissions = config.getPermissions();
 
 				sender.sendMessage(chatPrefix + L("stAvailablePermissions"));
 
 				Config.permissionInfo info;
 
 				for (Object o : permissions) {
 					String e = o.toString();
 					// sender.sendMessage("  " + e);
 
 					try {
 						info = Config.getPermissionInfo(e);
 
 						String node = ChatColor.WHITE + info.name;
 
 						if (canBuyPermission(sender, info)) {
 
 							String price = "$" + Integer.toString(info.price);
 
 							String duration = "";
 
 							if (info.duration > 0) {
 								duration = " " + database.secondsToString(info.duration * 60);
 							}
 
 							String uses = "";
 							if (info.uses > 0) {
 								uses = " x" + info.uses;
 							}
 
 							// if (sender.hasPermission(info.node)) {
 							// node = ChatColor.GRAY + info.name;
 							// }
 
 							ChatColor msgColour = ChatColor.GRAY;
 
 							if (player != null) {
 
 								if (playerHasPermissions(player, info.node)) {
 									// node = ChatColor.DARK_GRAY + info.name;
 									// price = ChatColor.DARK_GRAY +
 									// "$"+Integer.toString(info.price);
 
 									msgColour = ChatColor.DARK_GRAY;
 								} else if (getBalance(sender.getName()) >= info.price) {
 
 									// node = ChatColor.GREEN + info.name;
 									// price = ChatColor.GREEN +
 									// "$"+Integer.toString(info.price);
 									msgColour = ChatColor.GREEN;
 								} else if (getBalance(sender.getName()) < info.price) {
 									// node = ChatColor.RED + info.name;
 									// price = ChatColor.RED +
 									// "$"+Integer.toString(info.price);
 									msgColour = ChatColor.RED;
 								}
 							}
 
 							sender.sendMessage("  " + node + msgColour + uses + " " + price + duration);
 						}
 					} catch (Exception e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 
 				}
 
 				return true;
 			} else if (args[0].equalsIgnoreCase("info")) {
 				if (args.length == 1) {
 
 					sender.sendMessage(chatPrefix + L("stIncludePermissionName"));
 					return true;
 				}
 
 				Config.permissionInfo info;
 				try {
 					info = Config.getPermissionInfo(args[1]);
 					if (info != null) {
 
 						sender.sendMessage(chatPrefix + F("stPermissionInfo", args[1]));
 						sender.sendMessage(F("stPermissionInfoNode", info.node));
 						if (info.command != null)
 							sender.sendMessage(F("stPermissionInfoCommand", info.command));
 						sender.sendMessage(F("stPermissionInfoPrice", info.price));
 
 						if (info.duration > 0) {
 							sender.sendMessage(F("stPermissionInfoDuration", database.secondsToString(info.duration * 60)));
 
 						} else {
 							sender.sendMessage(F("stPermissionInfoDuration", "Unlimited"));
 						}
 
 						if (info.uses > 0) {
 							sender.sendMessage(F("stPermissionInfoUse", info.uses));
 
 						} else {
 							sender.sendMessage(F("stPermissionInfoUse", "Unlimited"));
 						}
 
 						if (info.world != null)
 							sender.sendMessage(F("stPermissionInfoWorld", info.world));
 
 						// sender.sendMessage("  You have permission: " +
 						// database.isPermissionActive(player.getName().toString(),
 						// args[1].toString()));
 
 					} else {
 
 						sender.sendMessage(F("stPermNoExist", args[1]));
 					}
 
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("load")) {
 
 				log.info("Load: " + player.getName());
 				// PB.unloadPlayerPermissions(player.getName());
 				try {
 
 					database.removeActivePermissions(player.getName());
 					database.retrieveActivePermissions(player);
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				// PB.loadPlayerPermissions(player.getName());
 
 				return true;
 			} else if (args[0].equalsIgnoreCase("active")) {
 				database.showActivePermissions(player);
 				return true;
 			} else if (args[0].equalsIgnoreCase("reload")) {
 
 				if (sender.isOp()) {
					this.reloadConfig();
 
 					sender.sendMessage(chatPrefix + " plugin reloaded.");
 					return true;
 				}
 
 				// database.showActivePermissions(player);
 			} else if (args[0].equalsIgnoreCase("create") && player.hasPermission(perm_create)) {
 				// purchasepermissions.create
 				if (args.length == 1) {
 
 					sender.sendMessage(L("stIncludeName"));
 					return true;
 				}
 
 				log.info(chatPrefix + " create 1");
 
 				if (config.createPermission(player, args[1])) {
 					sender.sendMessage(F("stPermCreated", args[1], args[1]));
 				}
 
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("modify") && sender.hasPermission(perm_modify)) {
 				if (args.length == 1) {
 
 					sender.sendMessage(L("stAvailableSettings"));
 					sender.sendMessage(L("stAvailableSettingsNode"));
 					sender.sendMessage(L("stAvailableSettingsPrice"));
 					sender.sendMessage(L("stAvailableSettingsDuration"));
 					sender.sendMessage(L("stAvailableSettingsCommand"));
 					sender.sendMessage(L("stAvailableSettingsUses"));
 					sender.sendMessage(L("stAvailableSettingsPayto"));
 					return true;
 				}
 
 				// SQL = String.format(SQL, Config.DbTable);
 
 				if (!config.permissionExists(args[1].toString())) {
 					sender.sendMessage(F("stPermNoExists", args[1].toString()));
 					return true;
 				}
 
 				if (args.length == 2) {
 					sender.sendMessage(L("stIncludeSettingName"));
 					return true;
 				}
 
 				if (config.isValidSetting(args[2].toString()) == false) {
 					sender.sendMessage(F("stNotValidSetting", args[2].toString()));
 					return true;
 				}
 
 				if (args.length == 3) {
 
 					sender.sendMessage(F("stIncludeSettingPerm", args[2]));
 					return true;
 				}
 
 				if (config.modifyPermissionSetting(player, args[1], args[2], args[3])) {
 					resetAllPermissions();
 					sender.sendMessage(String.format(L("stPermModifed"), args[1], args[2], args[3]));
 				}
 
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("remove") && player.hasPermission(perm_remove)) {
 				if (args.length == 1) {
 					player.sendMessage(L("stIncludeName"));
 					return true;
 				}
 
 				if (!config.permissionExists(args[1].toString())) {
 					sender.sendMessage(chatPrefix + F("stPermNoExists", args[1].toString()));
 					return true;
 				}
 				// sender.sendMessage(stPermNoExists.format(stPermNoExists,
 				// args[1].toString()));
 
 				if (config.removePermission(player, args[1])) {
 					sender.sendMessage(chatPrefix + F("stPermRemoved", args[1].toString()));
 					return true;
 				}
 
 			} else if (args[0].equalsIgnoreCase("buy")) {
 				if (args.length == 1) {
 
 					sender.sendMessage(L("stIncludePermissionName"));
 					return true;
 				}
 
 				Config.permissionInfo info;
 				try {
 					info = Config.getPermissionInfo(args[1]);
 					if (info != null) {
 
 						if (database.isPermissionActive(player.getName(), args[1].toString())) {
 
 							sender.sendMessage(chatPrefix + F("stAlreadyOwnPerm", args[1].toString()));
 							return true;
 						}
 
 						// if ((info.requirebuypermission == true ||
 						// config.useBuyPermission == true) &&
 						// !canBuyPermission(sender, info)){
 						if (!canBuyPermission(sender, info)) {
 							sender.sendMessage(chatPrefix + F("stBuyNotPermitted", args[1].toString()));
 							return true;
 						}
 
 						if (getBalance(sender.getName()) > info.price) {
 
 							String target = player.getName();
 
 							if (args.length == 3) {
 								target = args[2];
 
 								log.info(F("stUserBoughtPerm", sender.getName(), info.name, target));
 							}
 
 							database.removeActivePermissions(sender.getName());
 							if (database.addPlayer(target, info)) {
 								econ.withdrawPlayer(sender.getName(), info.price);
 
 								String payTo = Config.getPermisionPayTo(info.name);
 
 								// log.info("payto: " + payTo);
 								if (payTo != null) {
 									if (payPlayer(payTo, info.price)) {
 
 										sender.sendMessage(F("stPaidUser", payTo, info.price));
 									}
 								}
 
 								sender.sendMessage(chatPrefix + F("stPurchasedPerm", info.name, target));
 
 								notifyUsersOfPurchase(player, info.name, target);
 
 							} else {
 
 								sender.sendMessage(F("stFailedToBuyPerm", info.name));
 							}
 							database.retrieveActivePermissions(player);
 							// pb.loadPlayerPermissions(sender.getName());
 
 							// econ.withdrawPlayer(sender.getName(), info.price
 
 						} else {
 
 							sender.sendMessage(F("stNotEnoughFunds", info.name));
 						}
 						// econ.withdrawPlayer(sender.getName(), info.price);
 
 					} else {
 
 						sender.sendMessage(F("stPermNoExist", args[1]));
 					}
 
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return true;
 			} else if (args[0].equalsIgnoreCase("locales") && player.hasPermission(perm_loadlocales)) {
 				localization.loadDefaultLocales(true);
 				return true;
 			} else if (args[0].equalsIgnoreCase("test")) {
 				// sender.sendMessage("currentTimeMillis: " +
 				// System.currentTimeMillis());
 				// long unixTime = System.currentTimeMillis() / 1000L;
 				// sender.sendMessage("unixTime: " + unixTime);
 
 				// Config.testList();
 
 				// localization.listLocales();
 
 				// sender.sendMessage(String.format(L("testMsg"), senderName));
 				// sender.sendMessage(L("testMsg"));
 
 				// L.testLocales();
 
 				return true;
 			}
 
 		}
 
 		return false;
 	}
 
 	public String L(String key) {
 		return localization.L.get(key);
 	}
 
 	public String F(String key, Object... args) {
 		String value = localization.L.get(key).toString();
 		try {
 			if (value != null || args != null)
 				value = String.format(value, args); // arg.toString()
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return value;
 	}
 
 	public boolean playerHasPermissions(Player player, List<String> nodes) {
 
 		if (nodes != null)
 			for (String permissionName : nodes) {
 				if (!player.hasPermission(permissionName)) {
 					return false;
 				}
 			}
 
 		return true;
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
 
 	public static String resourceToString(String name) {
 
 		InputStream input = PurchasePermissions.class.getResourceAsStream("/" + name);
 		Writer writer = new StringWriter();
 		char[] buffer = new char[1024];
 
 		if (input != null) {
 			try {
 				int n;
 				Reader reader = new BufferedReader(new InputStreamReader(input));
 				while ((n = reader.read(buffer)) != -1)
 					writer.write(buffer, 0, n);
 			} catch (IOException e) {
 				try {
 					input.close();
 				} catch (IOException ex) {
 				}
 				return null;
 			} finally {
 				try {
 					input.close();
 				} catch (IOException e) {
 				}
 			}
 		} else {
 			return null;
 		}
 
 		String text = writer.toString().trim();
 		text = text.replace("\r\n", " ").replace("\n", " ");
 		return text.trim();
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
