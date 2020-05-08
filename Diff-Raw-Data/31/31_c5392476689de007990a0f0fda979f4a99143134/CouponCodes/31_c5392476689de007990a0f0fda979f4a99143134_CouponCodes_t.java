 package net.lala.CouponCodes;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.lala.CouponCodes.api.CouponAPI;
 import net.lala.CouponCodes.api.CouponManager;
 import net.lala.CouponCodes.api.SQLAPI;
 import net.lala.CouponCodes.api.coupon.Coupon;
 import net.lala.CouponCodes.api.coupon.EconomyCoupon;
 import net.lala.CouponCodes.api.coupon.ItemCoupon;
 import net.lala.CouponCodes.api.events.example.CouponMaster;
 import net.lala.CouponCodes.api.events.example.DatabaseMaster;
 import net.lala.CouponCodes.config.Config;
 import net.lala.CouponCodes.misc.SQLType;
 import net.lala.CouponCodes.sql.DatabaseOptions;
 import net.lala.CouponCodes.sql.SQL;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * CouponCodes.java - Main class
  * @author mike101102
  */
 public class CouponCodes extends JavaPlugin {
 	
 	private static CouponManager cm = null;
 	
 	private DatabaseOptions dataop = null;
 	private Config config = null;
 	
 	private boolean ec = false;
 	private boolean debug = false;
 	
 	private SQLType sqltype;
 	private SQL sql;
 	
 	public Server server = null;
 	public Economy econ = null;
 	
 	@Override
 	public void onEnable() {
 		if (cm == null)
 			cm = new CouponManager(this);
 		
 		if (server == null)
 			server = getServer();
 		
 		if (!setupEcon()) {
 			send("Economy support is disabled.");
 			ec = false;
 		} else {
 			ec = true;
 			if (!econ.isEnabled())
 				send("Economy support is disabled.");
 			    ec = false;
 		}
 		
 		// This is for this plugin's own events!
 		server.getPluginManager().registerEvent(Type.CUSTOM_EVENT, new CouponMaster(this), Priority.Lowest, this);
 		server.getPluginManager().registerEvent(Type.CUSTOM_EVENT, new DatabaseMaster(this), Priority.Lowest, this);
 		
 		config = new Config(this);
 		sqltype = config.getSQLType();
 		
 		debug = config.getDebug();
 		
 		switch (sqltype) {
 		case MySQL: dataop = new DatabaseOptions(config.getHostname(),
 				config.getPort(),
 				config.getDatabase(),
 				config.getUsername(),
 				config.getPassword());
 		case SQLite: dataop = new DatabaseOptions(new File(this.getDataFolder()+"/coupon_data.db"));
 		case Unknown:
 			sendErr("The SQLType has the unknown value of: "+config.getSQLValue()+" CouponCodes will now disable.");
 			this.setEnabled(false);
 			return;
 		}
 		
 		sql = new SQL(this, dataop);
 		
 		try {
 			sql.open();
 			sql.createTable("CREATE TABLE IF NOT EXISTS couponcodes (name VARCHAR(24), ctype VARCHAR(10), usetimes INT(10), usedplayers TEXT(1024), ids VARCHAR(255), money INT(10))");
 		} catch (SQLException e) {
 			sendErr("SQLException while creating couponcodes table. CouponCodes will now disable.");
 			e.printStackTrace();
 			this.setEnabled(false);
 			return;
 		}
 		
 		this.saveConfig();
 		send("is now enabled! Version: "+this.getDescription().getVersion());
 	}
 	
 	@Override
 	public void onDisable() {
 		this.saveConfig();
 		try {
 			sql.close();
 		} catch (SQLException e) {
 			sendErr("Could not close SQL connection");
 		} catch (NullPointerException e) {
 			sendErr("SQL is null. Connection doesn't exist");
 		}
 		send("is now disabled.");
 	}
 	
 	private boolean setupEcon() {
 		try {
 			RegisteredServiceProvider<Economy> ep = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			if (ep == null)
 				return false;
 			else
 				econ = ep.getProvider();
 				return true;
 		} catch (NoClassDefFoundError e) {
 			return false;
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		boolean pl = false;
 		if (sender instanceof Player) pl = true;
 		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
 			help(sender);
 			return true;
 		}
 		CouponAPI api = CouponCodes.getCouponAPI();
 		
 		// Add command
 		if (args[0].equalsIgnoreCase("add")) {
 			if (sender.hasPermission("cc.add")) {
 				if (args[0].equalsIgnoreCase("item")) {
 					if (args.length == 5) {
 						try {
 							Coupon coupon = api.createNewItemCoupon(args[2], Integer.parseInt(args[4]), this.convertStringToHash(args[3]), new ArrayList<String>());
 							if (coupon.isInDatabase()) {
 								sender.sendMessage(ChatColor.RED+"This coupon already exists!");
 								return true;
 							} else {
 								coupon.addToDatabase();
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Expected a number, but got "+ChatColor.YELLOW+args[4]);
 							return true;
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Error while adding coupon to database. Please check console for more info.");
 							sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED+"Invalid syntax length");
 						sender.sendMessage(ChatColor.YELLOW+"/c add item [name] [item1,item2...] [usetimes]");
 						return true;
 					}
 				}
 				else if (args[0].equalsIgnoreCase("econ")) {
 					if (args.length == 5) {
 						if (!econ.isEnabled()) {
 							sender.sendMessage(ChatColor.DARK_RED+"Economy support is currently disabled. You cannot add an economy coupon");
 							return true;
 						} else {
 							try {
 								Coupon coupon = api.createNewEconomyCoupon(args[2], Integer.parseInt(args[4]), new ArrayList<String>(), Integer.parseInt(args[3]));
 								if (coupon.isInDatabase()) {
 									sender.sendMessage(ChatColor.DARK_RED+"This coupon already exists!");
 									return true;
 								} else {
 									coupon.addToDatabase();
 									return true;
 								}
 							} catch (NumberFormatException e) {
 								sender.sendMessage(ChatColor.DARK_RED+"Expected a number, but got "+ChatColor.YELLOW+args[3]);
 								return true;
 							} catch (SQLException e) {
 								sender.sendMessage(ChatColor.DARK_RED+"Error while adding coupon to database. Please check console for more info.");
 								sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 								e.printStackTrace();
 								return true;
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED+"Invalid syntax length");
 						sender.sendMessage(ChatColor.YELLOW+"/c add econ [name] [money] [usetimes]");
 						return true;
 					}
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
 				return true;
 			}
 		}
 		
 		// Remove command
 		else if (args[0].equalsIgnoreCase("remove")) {
 			if (sender.hasPermission("cc.remove")) {
 				if (args.length == 2) {
 					try {
 						api.removeCouponFromDatabase(api.createNewItemCoupon(args[1], 0, null, null));
 						sender.sendMessage(ChatColor.GREEN+"The coupon "+ChatColor.GOLD+args[1]+ChatColor.GREEN+" has been removed.");
 						return true;
 					} catch (SQLException e) {
 						sender.sendMessage(ChatColor.DARK_RED+"Error while removing coupon from the database. Please check the console for more info.");
 						sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 						e.printStackTrace();
 						return true;
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED+"Invalid syntax length");
 					sender.sendMessage(ChatColor.YELLOW+"/c remove [name]");
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
 				return true;
 			}
 		}
 		
 		// Redeem command
 		else if (args[0].equalsIgnoreCase("redeem")) {
 			if (!pl) {
 				sender.sendMessage("You must be a player to redeem a coupon");
 				return true;
 			} else {
 				Player player = (Player) sender;
 				if (player.hasPermission("cc.redeem")) {
 					if (args.length == 2) {
 						try {
 							Coupon coupon = api.getCoupon(args[1]);
							if (coupon.getUseTimes() <= 0 || coupon.getUsedPlayers().contains(player.getName())) {
								player.sendMessage(ChatColor.RED+"You cannot use this coupon as it is expired for you.");
								return true;
							}
 							if (coupon instanceof ItemCoupon) {
 								ItemCoupon c = (ItemCoupon) coupon;
 								for (Map.Entry<Integer, Integer> en : c.getIDs().entrySet()) {
 									player.getInventory().addItem(new ItemStack(en.getValue(), en.getKey()));
 								}
 								player.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+c.getName()+ChatColor.GREEN+" has been redeemed, and the items added to your inventory!");
 								return true;
 							}
 							else if (coupon instanceof EconomyCoupon) {
 								if (!econ.isEnabled()) {
									player.sendMessage(ChatColor.DARK_RED+"Economy support is currently disabled. You cannot redeem an economy coupon.");
 									return true;
 								} else {
 									EconomyCoupon c = (EconomyCoupon) coupon;
 									econ.depositPlayer(player.getName(), c.getMoney());
 									player.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+c.getName()+ChatColor.GREEN+" has been redeemed, and the money added to your account!");
 									return true;
 								}
 							} else {
 								player.sendMessage(ChatColor.DARK_RED+"That doesn't look good! Your coupon isn't a coupon from CouponCodes, perhaps another plugin?");
 								return true;
 							}
 						} catch (SQLException e) {
 							player.sendMessage(ChatColor.DARK_RED+"Error while trying to find "+args[1]+" in the database. Please check the console for more info.");
 							player.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						player.sendMessage(ChatColor.RED+"Invalid syntax length");
 						player.sendMessage(ChatColor.YELLOW+"/c redeem [name]");
 						return true;
 					}
 				} else {
 					player.sendMessage(ChatColor.RED+"You do not have permission to use this command");
 					return true;
 				}
 			}
 		}
 		
 		// List command
 		else if (args[0].equalsIgnoreCase("list")) {
 			if (sender.hasPermission("cc.list")) {
 				StringBuilder sb = new StringBuilder();
 				try {
 					ArrayList<String> c = api.getCoupons();
 					if (c.isEmpty() || c.size() <= 0 || c.equals(null)) {
 						sender.sendMessage(ChatColor.RED+"No coupons found.");
 						return true;
 					} else {
 						sb.append(ChatColor.DARK_PURPLE+"Coupon list:"+ChatColor.GOLD);
 						for (int i = 0; i < c.size(); i++) {
 							sb.append(c.get(i));
 							if (!(Integer.valueOf(i+1).equals(c.size()))){
 								sb.append(", ");
 							}
 						}
 						sender.sendMessage(sb.toString());
 						return true;
 					}
 				} catch (SQLException e) {
 					sender.sendMessage(ChatColor.DARK_RED+"Error while getting the coupon list from the database. Please check the console for more info.");
 					sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 					e.printStackTrace();
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
 				return true;
 			}
 		} else{
 			help(sender);
 			return true;
 		}
 		return false;
 	}
 	
 	private void help(CommandSender sender) {
 		sender.sendMessage(ChatColor.GOLD+"|---------------------|");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.DARK_RED+"CouponCodes Help"+ChatColor.GOLD+"--|");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"/c help"+ChatColor.GOLD+"--|");
 		sender.sendMessage(ChatColor.GOLD+"|---------------------|");
 	}
 	
 	public HashMap<Integer, Integer> convertStringToHash(String args) {
 		HashMap<Integer, Integer> ids = new HashMap<Integer, Integer>();
 		String[] sp = args.split(",");
 		try {
 			for (int i = 0; i < sp.length; i++) {
 				int a = Integer.parseInt(sp[i].split(":")[0]);
 				int b = Integer.parseInt(sp[i].split(":")[1]);
 				ids.put(a, b);
 			}
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		}
 		return ids;
 	}
 	
	public String convertHashToString(HashMap<Integer, Integer> hash) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Integer> en : hash.entrySet()) {
			sb.append(en.getKey()+":"+en.getValue()+",");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
 	public ArrayList<String> convertStringToArrayList(String args) {
 		ArrayList<String> list = new ArrayList<String>();
 		String[] slist = args.split(",");
 		for (int i = 0; i < slist.length; i++) {
 			list.add(slist[i]);
 		}
 		return list;
 	}
 	
 	public String convertArrayListToString(ArrayList<String> args) {
 		ArrayList<String> list = new ArrayList<String>();
 		list.add("player1");
 		list.add("playerfag");
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < list.size(); i++) {
 			sb.append(list.get(i));
 			if (!(i+1 == list.size())) sb.append(", ");
 		}
 		return sb.toString();
 	}
 	
 	public void send(String message) {
 		System.out.println("[CouponCodes] "+message);
 	}
 	
 	public void sendErr(String message) {
 		System.err.println("[CouponCodes] [Error] "+message);
 	}
 	
 	public void debug(String message) {
 		if (!isDebug()) return;
 		System.out.println("[CouponCodes] [Debug] "+message);
 	}
 	
 	public static CouponAPI getCouponAPI() {
 		return (CouponAPI) cm;
 	}
 	
 	public SQLAPI getSQLAPI() {
 		return (SQLAPI) sql;
 	}
 	
 	public DatabaseOptions getDatabaseOptions() {
 		return dataop;
 	}
 	
 	public boolean isEconomyEnabled() {
 		return ec;
 	}
 	
 	public SQLType getSQLType() {
 		return sqltype;
 	}
 	
 	public boolean isDebug() {
 		return debug;
 	}
 }
