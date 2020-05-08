 package net.lala.CouponCodes;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.lala.CouponCodes.api.CouponManager;
 import net.lala.CouponCodes.api.coupon.Coupon;
 import net.lala.CouponCodes.api.coupon.EconomyCoupon;
 import net.lala.CouponCodes.api.coupon.ItemCoupon;
 import net.lala.CouponCodes.api.coupon.RankCoupon;
 import net.lala.CouponCodes.api.coupon.XpCoupon;
 import net.lala.CouponCodes.api.events.EventHandle;
 import net.lala.CouponCodes.api.events.plugin.CouponCodesCommandEvent;
 import net.lala.CouponCodes.listeners.DebugListen;
 import net.lala.CouponCodes.listeners.PlayerListen;
 import net.lala.CouponCodes.misc.CommandUsage;
 import net.lala.CouponCodes.misc.Metrics;
 import net.lala.CouponCodes.misc.Misc;
 import net.lala.CouponCodes.runnable.CouponTimer;
 import net.lala.CouponCodes.sql.SQL;
 import net.lala.CouponCodes.sql.options.DatabaseOptions;
 import net.lala.CouponCodes.sql.options.MySQLOptions;
 import net.lala.CouponCodes.sql.options.SQLiteOptions;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CouponCodes extends JavaPlugin {
 	
 	private static CouponManager cm;
 	
 	private DatabaseOptions dataop;
 	private Config config;
 	
 	private boolean va = false;
 	private boolean debug = false;
 	private boolean usethread = true;
 	
 	private SQL sql;
 	
 	private Metrics mt = null;
 	
 	public Server server;
 	public Economy econ;
 	public Permission perm;
 	
 	public String version;
 	public String newversion;
 	public String verinfo;
 	
 	@Override
 	public void onEnable() {
 		server = getServer();
 		config = new Config(this);
 		debug = config.getDebug();
 		version = getDescription().getVersion();
 		usethread = config.getUseThread();
 		
 		setUpdateInfo();
 		
 		if (!setupVault()) {
 			send("Vault support is disabled.");
 			va = false;
 		} else {
 			send("Vault support is enabled.");
 			va = true;
 		}
 		
 		if (!version.equals(newversion) && !version.contains("TEST"))
 			send("New update is available for CouponCodes! Current version: "+version+" New version: "+newversion);
 		
 		// This is for this plugin's own events!
 		server.getPluginManager().registerEvents(new DebugListen(this), this);
 		
 		// Bukkit listeners
 		if (usethread) server.getPluginManager().registerEvents(new PlayerListen(this), this);
 		
 		if (!setupSQL()) {
 			send("Database could not be setup. CouponCodes will now disable");
 			server.getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		// Timers!
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new CouponTimer(), 100L, 100L);
 		
 		try {
 			mt = new Metrics();
 			mt.beginMeasuringPlugin(this);
 		} catch (IOException e) {
 			debug("Error while trying to measure plugin");
 		}
 		
 		send("is now enabled! Version: "+version);
 	}
 	
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		try {
 			sql.close();
 		} catch (SQLException e) {
 			sendErr("Could not close SQL connection");
 		} catch (NullPointerException e) {
 			sendErr("SQL is null. Connection doesn't exist");
 		}
 		cm = null;
 		send("is now disabled.");
 	}
 	
 	private boolean setupSQL() {
 		if (config.getSQLValue().equalsIgnoreCase("MySQL")) {
 			dataop = new MySQLOptions(config.getHostname(), config.getPort(), config.getDatabase(), config.getUsername(), config.getPassword());
 		}
 		else if (config.getSQLValue().equalsIgnoreCase("SQLite")) {
 			dataop = new SQLiteOptions(new File(getDataFolder()+"/coupon_data.db"));
 		}
 		else if (!config.getSQLValue().equalsIgnoreCase("MySQL") && !config.getSQLValue().equalsIgnoreCase("SQLite")) {
 			sendErr("The SQLType has the unknown value of: "+config.getSQLValue());
 			return false;
 		}
 		
 		sql = new SQL(this, dataop);
 		
 		try {
 			sql.open();
 			sql.createTable("CREATE TABLE IF NOT EXISTS couponcodes (name VARCHAR(24), ctype VARCHAR(10), usetimes INT(10), usedplayers TEXT(1024), ids VARCHAR(255), money INT(10), groupname VARCHAR(20), timeuse INT(100), xp INT(10))");
 			cm = new CouponManager(this, sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean setupVault() {
 		if (!config.getVault())
 			return false;
 		try {
 			RegisteredServiceProvider<Economy> ep = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			RegisteredServiceProvider<Permission> pe = server.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 			if (ep == null)
 				return false;
 			else if (pe == null)
 				return false;
 			else
 				econ = ep.getProvider();
 				perm = pe.getProvider();
 				return true;
 		} catch (NoClassDefFoundError e) {
 			return false;
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		// Event handling
 		CouponCodesCommandEvent ev = EventHandle.callCouponCodesCommandEvent(sender, command, commandLabel, args);
 		if (ev.isCancelled()) return true;
 		sender = ev.getSender();
 		command = ev.getCommand();
 		commandLabel = ev.getCommandLabel();
 		args = ev.getArgs();
 		
 		if (args.length == 0) {
 			help(sender);
 			return true;
 		}
 		
 		boolean pl = false;
 		if (sender instanceof Player) pl = true;
 		
 		CouponManager api = CouponCodes.getCouponManager();
 		
 		// Add command 2.0
 		if (args[0].equalsIgnoreCase("add")) {
 			if (args.length < 2) {
 				helpAdd(sender);
 				return true;
 			}
 			if (has(sender, "cc.add")) {
 				if (args[1].equalsIgnoreCase("item")) {
 					if (args.length >= 4) {
 						try {
 							String name = args[2];
 							int usetimes = 1;
 							int time = -1;
 							
 							if (name.equalsIgnoreCase("random")) name = Misc.generateName();
 							if (args.length >= 5) usetimes = Integer.parseInt(args[4]);
 							if (args.length >= 6) time = Integer.parseInt(args[5]);
 							if (args.length > 6) {
 								sender.sendMessage(CommandUsage.C_ADD_ITEM.toString());
 								return true;
 							}
 							
 							ItemCoupon ic = api.createNewItemCoupon(name, usetimes, time, convertStringToHash(args[3]), new HashMap<String, Boolean>());
 							
 							if (ic.addToDatabase()) {
 								sender.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+name+ChatColor.GREEN+" has been added!");
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.RED+"This coupon already exists!");
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Expected a number, but got a string. Please check your syntax.");
 							return true;
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Error while interacting with the database. See console for more info.");
 							sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						sender.sendMessage(CommandUsage.C_ADD_ITEM.toString());
 						return true;
 					}
 				}
 				
 				else if (args[1].equalsIgnoreCase("econ")) {
 					if (args.length >= 4) {
 						try {
 							String name = args[2];
 							int usetimes = 1;
 							int time = -1;
 							int money = Integer.parseInt(args[3]);
 							
 							if (name.equalsIgnoreCase("random")) name = Misc.generateName();
 							if (args.length >= 5) usetimes = Integer.parseInt(args[4]);
 							if (args.length >= 6) time = Integer.parseInt(args[5]);
 							if (args.length > 6) {
 								sender.sendMessage(CommandUsage.C_ADD_ECON.toString());
 								return true;
 							}
 							
 							EconomyCoupon ec = api.createNewEconomyCoupon(name, usetimes, time, new HashMap<String, Boolean>(), money);
 							
 							if (ec.addToDatabase()) {
 								sender.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+name+ChatColor.GREEN+" has been added!");
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.RED+"This coupon already exists!");
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Expected a number, but got a string. Please check your syntax.");
 							return true;
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Error while interacting with the database. See console for more info.");
 							sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						sender.sendMessage(CommandUsage.C_ADD_ECON.toString());
 						return true;
 					}
 				}
 				
 				else if (args[1].equalsIgnoreCase("rank")) {
 					if (args.length >= 4) {
 						try {
 							String name = args[2];
 							String group = args[3];
 							int usetimes = 1;
 							int time = -1;
 							
 							if (name.equalsIgnoreCase("random")) name = Misc.generateName();
 							if (args.length >= 5) usetimes = Integer.parseInt(args[4]);
 							if (args.length >= 6) time = Integer.parseInt(args[5]);
 							if (args.length > 6) {
 								sender.sendMessage(CommandUsage.C_ADD_RANK.toString());
 								return true;
 							}
 							
 							RankCoupon rc = api.createNewRankCoupon(name, group, usetimes, time, new HashMap<String, Boolean>());
 							
 							if (rc.addToDatabase()) {
 								sender.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+name+ChatColor.GREEN+" has been added!");
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.RED+"This coupon already exists!");
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Expected a number, but got a string. Please check your syntax.");
 							return true;
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Error while interacting with the database. See console for more info.");
 							sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						sender.sendMessage(CommandUsage.C_ADD_RANK.toString());
 						return true;
 					}
 				}
 				
 				else if (args[1].equalsIgnoreCase("xp")) {
 					if (args.length >= 4) {
 						try {
 							String name = args[2];
 							int xp = Integer.parseInt(args[3]);
 							int usetimes = 1;
 							int time = -1;
 							
 							if (name.equalsIgnoreCase("random")) name = Misc.generateName();
 							if (args.length >= 5) usetimes = Integer.parseInt(args[4]);
 							if (args.length >= 6) time = Integer.parseInt(args[5]);
 							if (args.length > 6) {
 								sender.sendMessage(CommandUsage.C_ADD_XP.toString());
 								return true;
 							}
 							
 							XpCoupon xc = api.createNewXpCoupon(name, xp, usetimes, time, new HashMap<String, Boolean>());
 							
 							if (xc.addToDatabase()) {
 								sender.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+name+ChatColor.GREEN+" has been added!");
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.RED+"This coupon already exists!");
 								return true;
 							}
 						} catch (NumberFormatException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Expected a number, but got a string. Please check your syntax.");
 							return true;
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.DARK_RED+"Error while interacting with the database. See console for more info.");
 							sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						sender.sendMessage(CommandUsage.C_ADD_XP.toString());
 						return true;
 					}
 				} else {
 					helpAdd(sender);
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
 				return true;
 			}
 		}
 		
 		// Remove command
 		else if (args[0].equalsIgnoreCase("remove")) {
 			if (has(sender, "cc.remove")) {
 				if (args.length == 2) {
 					try {
 						if (args[1].equalsIgnoreCase("all")) {
 							int j = 0;
 							ArrayList<String> cs = cm.getCoupons();
 							if (cs.size() > 20) sender.sendMessage(ChatColor.GOLD+"This may take a few moments..");
 							for (String i : cs) {
 								cm.removeCouponFromDatabase(i);
 								j++;
 							}
 							sender.sendMessage(ChatColor.GREEN+"A total of "+ChatColor.GOLD+j+ChatColor.GREEN+" coupons have been removed.");
 							return true;
 						}
 						if (!api.couponExists(args[1])) {
 							sender.sendMessage(ChatColor.RED+"That coupon doesn't exist!");
 							return true;
 						}
 						api.removeCouponFromDatabase(api.createNewItemCoupon(args[1], 0, -1, null, null));
 						sender.sendMessage(ChatColor.GREEN+"The coupon "+ChatColor.GOLD+args[1]+ChatColor.GREEN+" has been removed.");
 						return true;
 					} catch (SQLException e) {
 						sender.sendMessage(ChatColor.DARK_RED+"Error while removing coupon from the database. Please check the console for more info.");
 						sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 						e.printStackTrace();
 						return true;
 					}
 				} else {
 					sender.sendMessage(CommandUsage.C_REMOVE.toString());
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
 				if (has(player, "cc.redeem")) {
 					if (args.length == 2) {
 						try {
 							Coupon coupon = api.getCoupon(args[1]);
 							if (coupon == null) {
 								player.sendMessage(ChatColor.RED+"That coupon doesn't exist!");
 								return true;
 							}
 							
							if (!(coupon.getUsedPlayers() == null)) {
 								if (!coupon.getUsedPlayers().isEmpty()) {
									if (coupon.getUseTimes() < 1) {
										player.sendMessage(ChatColor.RED+"This coupon has been used up!");
										return true;
									}
 									if (coupon.getUsedPlayers().containsKey(player.getName())) {
 										if (coupon.getUsedPlayers().get(player.getName())) {
 											player.sendMessage(ChatColor.RED+"You have already used this coupon");
 											return true;
 										}
 									}
 								}
 							}
 							
 							if (coupon instanceof ItemCoupon) {
 								ItemCoupon c = (ItemCoupon) coupon;
 								if (player.getInventory().firstEmpty() == -1) {
 									for (Map.Entry<Integer, Integer> en : c.getIDs().entrySet()) {
 										player.getLocation().getWorld().dropItem(player.getLocation(), new ItemStack(en.getKey(), en.getValue()));
 									}
 									player.sendMessage(ChatColor.RED+"Your inventory is full, so the items have been dropped below you.");
 								} else {
 									for (Map.Entry<Integer, Integer> en : c.getIDs().entrySet()) {
 										player.getInventory().addItem(new ItemStack(en.getKey(), en.getValue()));
 									}
 									player.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+c.getName()+ChatColor.GREEN+" has been redeemed, and the items added to your inventory!");
 								}
 							}
 							
 							else if (coupon instanceof EconomyCoupon) {
 								if (!va) {
 									player.sendMessage(ChatColor.DARK_RED+"Vault support is currently disabled. You cannot redeem an economy coupon.");
 									return true;
 								} else {
 									EconomyCoupon c = (EconomyCoupon) coupon;
 									econ.depositPlayer(player.getName(), c.getMoney());
 									player.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+c.getName()+ChatColor.GREEN+" has been redeemed, and the money added to your account!");
 								}
 							}
 							
 							else if (coupon instanceof RankCoupon) {
 								if (!va) {
 									player.sendMessage(ChatColor.DARK_RED+"Vault support is currently disabled. You cannot redeem a rank coupon.");
 									return true;
 								} else {
 									RankCoupon c = (RankCoupon) coupon;
 									boolean permbuk = (perm.getName().equalsIgnoreCase("PermissionsBukkit"));
 									if (permbuk)
 										perm.playerAddGroup((String) null, player.getName(), c.getGroup());
 									else
 										perm.playerAddGroup(player, c.getGroup());
 									for (String i : perm.getPlayerGroups((String) null, player.getName())) {
 										if (i.equalsIgnoreCase(c.getGroup())) continue;
 										if (permbuk)
 											perm.playerRemoveGroup((String) null, player.getName(), i);
 										else
 											perm.playerRemoveGroup(player, i);
 									}
 									player.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+c.getName()+ChatColor.GREEN+" has been redeemed, and your group has been set to "+ChatColor.GOLD+c.getGroup());
 								}
 							}
 							
 							else if (coupon instanceof XpCoupon) {
 								XpCoupon c = (XpCoupon) coupon;
 								player.setLevel(player.getLevel()+c.getXp());
 								player.sendMessage(ChatColor.GREEN+"Coupon "+ChatColor.GOLD+c.getName()+ChatColor.GREEN+" has been redeemed, and you have received "+ChatColor.GOLD+c.getXp()+ChatColor.GREEN+" XP levels!");
 							}
 							
 							HashMap<String, Boolean> up = coupon.getUsedPlayers();
 							up.put(player.getName(), true);
 							coupon.setUsedPlayers(up);
 							coupon.setUseTimes(coupon.getUseTimes()-1);
 							coupon.updateWithDatabase();
 							return true;
 						} catch (SQLException e) {
 							player.sendMessage(ChatColor.DARK_RED+"Error while trying to find "+ChatColor.GOLD+args[1]+ChatColor.DARK_RED+" in the database. Please check the console for more info.");
 							player.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 							e.printStackTrace();
 							return true;
 						}
 					} else {
 						player.sendMessage(CommandUsage.C_REDEEM.toString());
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
 			if (has(sender, "cc.list")) {
 				StringBuilder sb = new StringBuilder();
 				try {
 					ArrayList<String> c = api.getCoupons();
 					if (c.isEmpty() || c.size() <= 0 || c.equals(null)) {
 						sender.sendMessage(ChatColor.RED+"No coupons found.");
 						return true;
 					} else {
 						sb.append(ChatColor.DARK_PURPLE+"Coupon list: "+ChatColor.GOLD);
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
 		}
 		
 		// Info command
 		else if (args[0].equalsIgnoreCase("info")) {
 			if (has(sender, "cc.info")) {
 				try {
 					if (args.length == 2) {
 						Coupon c = api.getCoupon(args[1]);
 						if (c != null) {
 							sender.sendMessage(ChatColor.GOLD+"|----------------------|");
 							sender.sendMessage(ChatColor.GOLD+"|---"+ChatColor.DARK_RED+"Coupon "+ChatColor.YELLOW+c.getName()+ChatColor.DARK_RED+" info"+ChatColor.GOLD+"---|");
 							sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Name: "+ChatColor.DARK_PURPLE+c.getName());
 							sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Type: "+ChatColor.DARK_PURPLE+c.getType());
 							sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Use times left: "+ChatColor.DARK_PURPLE+c.getUseTimes());
 							if (c.getTime() != -1)
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Time left: "+ChatColor.DARK_PURPLE+c.getTime()+ChatColor.YELLOW+" seconds");
 							else
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Time left: "+ChatColor.DARK_PURPLE+"Unlimited");
 							sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Expired: "+ChatColor.DARK_PURPLE+c.isExpired());
 							if (c.getUsedPlayers().isEmpty())
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Used players: "+ChatColor.DARK_PURPLE+"None");
 							else
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Used players: "+ChatColor.DARK_PURPLE+convertHashToString2(c.getUsedPlayers()));
 							if (c instanceof ItemCoupon)
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Items: "+ChatColor.DARK_PURPLE+convertHashToString(((ItemCoupon) c).getIDs()));
 							else if (c instanceof EconomyCoupon)
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Money: "+ChatColor.DARK_PURPLE+((EconomyCoupon) c).getMoney());
 							else if (c instanceof RankCoupon)
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Items: "+ChatColor.DARK_PURPLE+((RankCoupon) c).getGroup());
 							else if (c instanceof XpCoupon)
 								sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"XP: "+ChatColor.DARK_PURPLE+((XpCoupon) c).getXp());
 							sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Totally random name: "+ChatColor.DARK_PURPLE+Misc.generateName());
 							sender.sendMessage(ChatColor.GOLD+"|----------------------|");
 							return true;
 						} else {
 							sender.sendMessage(ChatColor.RED+"That coupon doesn't exist!");
 							return true;
 						}
 					} else {
 						StringBuilder sb1 = new StringBuilder();
 						StringBuilder sb2 = new StringBuilder();
 						ArrayList<String> co = api.getCoupons();
 						if (co.isEmpty() || co.equals(null)) {
 							sb1.append("None");
 							sb2.append("Out of those, 0% are item, 0% are economy, and 0% are rank coupons.");
 						} else {
 							double j = co.size();
 							double it = 0;
 							double ec = 0;
 							double ra = 0;
 							double xp = 0;
 							String it2 = null;
 							String ec2 = null;
 							String ra2 = null;
 							String xp2 = null;
 							DecimalFormat d1 = new DecimalFormat("#.##");
 							DecimalFormat d2 = new DecimalFormat("##.##");
 							for (int i = 0; i < co.size(); i++) {
 								sb1.append(co.get(i));
 								Coupon coo = api.getBasicCoupon(co.get(i));
 								if (coo instanceof ItemCoupon) it++;
 								if (coo instanceof EconomyCoupon) ec++;
 								if (coo instanceof RankCoupon) ra++;
 								if (coo instanceof XpCoupon) xp++;
 								if (!(Integer.valueOf(i+1).equals(co.size()))){
 									sb1.append(", ");
 								}
 							}
 							it2 = d2.format(it/j*100);
 							ec2 = d2.format(ec/j*100);
 							ra2 = d2.format(ra/j*100);
 							xp2 = d2.format(xp/j*100);
 							if (it < 10) it2 = d1.format(it/j*100);
 							if (ec < 10) ec2 = d1.format(ec/j*100);
 							if (ra < 10) ra2 = d1.format(ra/j*100);
 							if (xp < 10) xp2 = d1.format(xp/j*100);
 							sb2.append("Out of those, "+it2+"% are item, "+ec2+"% are economy, "+ra2+"% are rank, and "+xp2+"% are XP coupons.");
 						}
 						sender.sendMessage(ChatColor.GOLD+"|-----------------------|");
 						sender.sendMessage(ChatColor.GOLD+"|-"+ChatColor.DARK_RED+"Info on current coupons"+ChatColor.GOLD+"-|");
 						sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.GOLD+"Use /c info [name] to view a specific coupon");
 						sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Current coupons: "+ChatColor.DARK_PURPLE+sb1.toString());
 						sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+sb2.toString());
 						sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"Totally random name: "+ChatColor.DARK_PURPLE+Misc.generateName());
 						sender.sendMessage(ChatColor.GOLD+"|-----------------------|");
 						return true;
 					}
 				} catch (SQLException e) {
 					sender.sendMessage(ChatColor.DARK_RED+"Error while finding coupons in the database. Please check the console for more info.");
 					sender.sendMessage(ChatColor.DARK_RED+"If this error persists, please report it.");
 					e.printStackTrace();
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
 				return true;
 			}
 		}
 		
 		// Reload command
 		else if (args[0].equalsIgnoreCase("reload")) {
 			if (has(sender, "cc.reload")) {
 				if (!sql.reload())
 					sender.sendMessage(ChatColor.DARK_RED+"Could not reload the database");
 				else
 					sender.sendMessage(ChatColor.GREEN+"Database reloaded");
 				reloadConfig();
 				config = new Config(this);
 				sender.sendMessage(ChatColor.GREEN+"Config reloaded");
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
 				return true;
 			}
 		} else {
 			help(sender);
 			return true;
 		}
 	}
 	
 	public boolean has(CommandSender sender, String permission) {
 		if (sender instanceof ConsoleCommandSender) return true;
 		if (!va) return sender.hasPermission(permission);
 		return perm.has(sender, permission);
 	}
 	
 	private void help(CommandSender sender) {
 		sender.sendMessage(ChatColor.GOLD+"|-[] = required-"+ChatColor.DARK_RED+"CouponCodes Help"+ChatColor.GOLD+"-() = optional-|");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"add item [name] [item1:amount,item2:amount,..] (usetimes) (time)");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"add econ [name] [money] (usetimes) (time)");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"add rank [name] [group] (usetimes) (time)");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"add xp [name] [xp] (usetimes) (time)");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"redeem [name]");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"remove [name]");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"list");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"info (name)");
 		sender.sendMessage(ChatColor.GOLD+"|--"+ChatColor.YELLOW+"reload");
 	}
 	
 	private void helpAdd(CommandSender sender) {
 		sender.sendMessage(CommandUsage.C_ADD_ITEM.toString());
 		sender.sendMessage(CommandUsage.C_ADD_ECON.toString());
 		sender.sendMessage(CommandUsage.C_ADD_RANK.toString());
 		sender.sendMessage(CommandUsage.C_ADD_XP.toString());
 	}
 	
 	public boolean checkForUpdate() {
 		if (newversion == null)
 			return false;
 		else if (newversion.equals(version))
 			return false;
 		else
 			return true;
 	}
 	
 	public void setUpdateInfo() {
 		try {
 			URL url = new URL("http://www.craftmod.net/jar/CouponCodes/version.txt");
 			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
 			newversion = br.readLine();
 			
 			url = new URL("http://www.craftmod.net/jar/CouponCodes/info.txt");
 			br = new BufferedReader(new InputStreamReader(url.openStream()));
 			verinfo = br.readLine();
 			
 			br.close();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
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
 	
 	
 	public HashMap<String, Boolean> convertStringToHash2(String args) {
 		HashMap<String, Boolean> pl = new HashMap<String, Boolean>();
 		if (args.equals(null) || args.length() < 1) return pl;
 		String[] sp = args.split(",");
 		try {
 			for (int i = 0; i < sp.length; i++) {
 				String a = String.valueOf(sp[i].split(":")[0]);
 				Boolean b = Boolean.valueOf(sp[i].split(":")[1]);
 				pl.put(a, b);
 			}
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		}
 		return pl;
 	}
 	
 	public String convertHashToString2(HashMap<String, Boolean> hash) {
 		if (hash.isEmpty() || hash == null || hash.size() < 1) return "";
 		StringBuilder sb = new StringBuilder();
 		for (Map.Entry<String, Boolean> en : hash.entrySet()) {
 			sb.append(en.getKey()+":"+en.getValue()+",");
 		}
 		sb.deleteCharAt(sb.length()-1);
 		return sb.toString();
 	}
 	
 	public void send(String message) {
 		System.out.println("[CouponCodes] "+message);
 	}
 	
 	public void sendErr(String message) {
 		System.err.println("[CouponCodes] [Error] "+message);
 	}
 	
 	public void debug(String message) {
 		if (!debug) return;
 		System.out.println("[CouponCodes] [Debug] "+message);
 	}
 	
 	public static CouponManager getCouponManager() {
 		return cm;
 	}
 	
 	public SQL getSQLAPI() {
 		return sql;
 	}
 	
 	public DatabaseOptions getDatabaseOptions() {
 		return dataop;
 	}
 	
 	public boolean isVaultEnabled() {
 		return va;
 	}
 	
 	public boolean isDebug() {
 		return debug;
 	}
 	
 	public boolean useThread() {
 		return usethread;
 	}
 }
