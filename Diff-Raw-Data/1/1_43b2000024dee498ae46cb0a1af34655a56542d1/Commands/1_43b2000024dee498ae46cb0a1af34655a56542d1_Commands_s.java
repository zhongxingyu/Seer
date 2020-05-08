 package com.mistphizzle.donationpoints.plugin;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 
 public class Commands {
 
 	DonationPoints plugin;
 
 	public Commands(DonationPoints instance) {
 		this.plugin = instance;
 		init();
 	}
 	
 	// Strings
 	static String Prefix;
 	static String noPermissionMessage;
 	static String InvalidArguments;
 	static String NoCommandExists;
 	static String DPConfirm;
 	static String DPActivate;
 	static String DPSuccessfulActivation;
 	static String DPFailedActivation;
 	static String ExpireDate;
 	static String DPGive;
 	static String DPTake;
 	static String NoAccount;
 	static String AccountCreated;
 	static String TransferOff;
 	static String NoTransfer;
 	static String TransferSent;
 	static String TransferReceive;
 	static String PlayerOnly;
 	static String ReloadSuccessful;
 	static String PlayerBalance;
 	static String OtherBalance;
 	static String AccountAlreadyExists;
 	static String NoPurchaseStarted;
 	static String NeedActivation;
 	static String PurchaseSuccessful;
 	static String LimitReached;
 	static String PackageActivated;
 	static String DPSet;
 	static String NotEnoughPoints;
 	static String InvalidPackage;
 
 	private void init() {
 		PluginCommand donationpoints = plugin.getCommand("donationpoints");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (args.length < 1) {
 					// Base Command
 					s.sendMessage("-----4DonationPoints Commandsf-----");
 					s.sendMessage("3/dp basicf - Show basic DonationPoints commands.");
 					s.sendMessage("3/dp packagesf - Show the DonationPoints packages commands.");
 					s.sendMessage("3/dp adminf - Show the DonationPoints Admin Commands.");
 					return true;
 					// Packages Commands
 				} else if (args[0].equalsIgnoreCase("packages")) {
 					s.sendMessage("-----4DonationPoints Package Commandsf-----");
 					if (DonationPoints.permission.has(s, "donationpoints.package.info")) {
 						s.sendMessage("3/dp package info <packageName>f - Shows package information.");
 					} if (DonationPoints.permission.has(s, "donationpoints.package.list")) {
 						s.sendMessage("3/dp package listf - Lista all packages.");
 					} else {
 						s.sendMessage("cYou don't have permission to use any of the packages commands.");
 					}
 					// Admin Commands
 				} else if (args[0].equalsIgnoreCase("admin")) {
 					s.sendMessage("-----4DonationPoints Admin Commandsf-----");
 					if (DonationPoints.permission.has(s, "donationpoints.give")) {
 						s.sendMessage("3/dp give <player> <amount>f - Give points to a player.");
 					} if (DonationPoints.permission.has(s, "donationpoints.take")) {
 						s.sendMessage("3/dp take <player> <amount>f - Take points from a player.");
 					} if (DonationPoints.permission.has(s, "donationpoints.set")) {
 						s.sendMessage("3/dp set <player> <amount>f - Set a player's balance.");
 					} if (DonationPoints.permission.has(s, "donationpoints.version")) {
 						s.sendMessage("3/dp versionf - Shows the version of the plugin you're running.");
 					} if (DonationPoints.permission.has(s, "donationpoints.update")) {
 						s.sendMessage("3/dp updatef - Checks if there is an update available.");
 					} if (DonationPoints.permission.has(s, "donationpoints.reload")) {
 						s.sendMessage("3/dp reloadf - Reloads the Configuration / Packages.");
 					} else {
 						s.sendMessage("cYou don't have any permission for ANY DonationPoints Admin Commands.");
 					}
 				} else if (args[0].equalsIgnoreCase("basic")) {
 					s.sendMessage("-----4DonationPoints Basic Commandsf-----");
 					if (DonationPoints.permission.has(s, "donationpoints.create")) {
 						s.sendMessage("3/dp createf - Creates a points account for you.");
 					}
 					if (DonationPoints.permission.has(s, "donationpoints.balance")) {
 						s.sendMessage("3/dp balancef - Checks your points balance.");
 					} if (DonationPoints.permission.has(s, "donationpoints.transfer")) {
 						s.sendMessage("3/dp transfer <player> <amount>f - Transfer Points.");
 					} if (DonationPoints.permission.has(s, "donationpoints.transfer")) {
 						s.sendMessage("3/dp purchase <package>f - Purchase a package.");
 					} else {
 						s.sendMessage("cYou don't have permission for any DonationPoints Basic Commands.");
 					}
 				} else if (args[0].equalsIgnoreCase("transfer")) {
 					if (!plugin.getConfig().getBoolean("General.Transferrable")) {
 						s.sendMessage(Prefix + TransferOff);
 						return true;
 					}
 					if (!DonationPoints.permission.has(s, "donationpoints.transfer")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (!(s instanceof Player)) {
 						s.sendMessage(Prefix + PlayerOnly);
 						return true;
 					}
 					if (args.length < 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					else {
 						String sender = s.getName();
 						String target = args[1];
 						Double transferamount = Double.parseDouble(args[2]);
 						if (!Methods.hasAccount(sender)) {
 							s.sendMessage(Prefix + NoAccount.replace("%player", sender));
 							return true;
 						}
 						if (!Methods.hasAccount(target)) {
 							s.sendMessage(Prefix + NoAccount.replace("%player", target));
 							return true;
 						} if (target.equalsIgnoreCase(sender)) {
 							s.sendMessage(Prefix + NoTransfer);
 							return true;
 						} else {
 							if (transferamount > Methods.getBalance(sender)) {
 								s.sendMessage(Prefix + NoTransfer);
 								return true;
 							} if (transferamount == 0) {
 								s.sendMessage(Prefix + NoTransfer);
 								return true;
 							}
 						}
 						Methods.addPoints(transferamount, target);
 						Methods.removePoints(transferamount, sender);
 						String transferamount2 = transferamount.toString();
 						s.sendMessage(Prefix + TransferSent.replace("%player", target).replace("%amount", transferamount2));
 						for (Player player: Bukkit.getOnlinePlayers()) {
 							if (player.getName().equalsIgnoreCase(args[1])) {
 								player.sendMessage(Prefix + TransferReceive.replace("%player", sender).replace("%amount", transferamount2));
 							}
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("reload")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.reload")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					plugin.reloadConfig();
 					try {
 						plugin.firstRun();
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 					s.sendMessage(Prefix + ReloadSuccessful);
 				} else if (args[0].equalsIgnoreCase("balance")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.balance")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length == 1) {
 						if (!Methods.hasAccount(s.getName())) {
 							s.sendMessage(Prefix + NoAccount.replace("%player", s.getName()));
 						} else {
 							Double balance = Methods.getBalance(s.getName());
 							String balance2 = balance.toString();
 							s.sendMessage(Prefix + PlayerBalance.replace("%amount", balance2));
 						}
 					} else if (args.length == 2) {
 						if (!DonationPoints.permission.has(s, "donationpoints.balance.others")) {
 							s.sendMessage(Prefix + noPermissionMessage);
 							return true;
 						}
 						String string = args[1];
 						if (!Methods.hasAccount(string)) {
 							s.sendMessage(Prefix + NoAccount.replace("%player", string));
 						} else {
 							Double balance = Methods.getBalance(string);
 							String balance2 = balance.toString();
 							s.sendMessage(Prefix + OtherBalance.replace("%player", string).replace("%amount", balance2));
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("create")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.create")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length == 1) {
 						String string = s.getName();
 						if (!Methods.hasAccount(string)) {
 							Methods.createAccount(string);
 							s.sendMessage(Prefix + AccountCreated.replace("%player", string));
 						} else {
 							s.sendMessage(Prefix + AccountAlreadyExists.replace("%player", string));
 						}
 					} if (args.length == 2) {
 						if (!DonationPoints.permission.has(s, "donationpoints.create.others")) {
 							s.sendMessage(Prefix + noPermissionMessage);
 						}
 						String string = args[1];
 						if (!Methods.hasAccount(string)) {
 							Methods.createAccount(string);
 							s.sendMessage(Prefix + AccountCreated.replace("%player", string));
 						} else if (Methods.hasAccount(string)) {
 							s.sendMessage(Prefix + AccountAlreadyExists.replace("%player", string));
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("give")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.give")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length != 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					Double addamount = Double.parseDouble(args[2]);
 					String target = args[1].toLowerCase();
 					Methods.addPoints(addamount, target);
 					String addamount2 = addamount.toString();
 					s.sendMessage(Prefix + DPGive.replace("%amount", addamount2).replace("%player", target));
 				} else if (args[0].equalsIgnoreCase("take")) {
 					if (args.length != 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					if (!DonationPoints.permission.has(s, "donationpoints.take")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					Double takeamount = Double.parseDouble(args[2]);
 					String target = args[1].toLowerCase();
 					Methods.removePoints(takeamount, target);
 					String takeamount2 = takeamount.toString();
 					s.sendMessage(Prefix + DPTake.replace("%amount", takeamount2).replace("%player", target));
 				} else if (args[0].equalsIgnoreCase("confirm")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.confirm")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					String sender = s.getName();
 					if (!PlayerListener.purchases.containsKey(s.getName().toLowerCase())) {
 						s.sendMessage(Prefix + NoPurchaseStarted);
 					} else if (PlayerListener.purchases.containsKey(s.getName().toLowerCase())) {
 						String pack2 = PlayerListener.purchases.get(s.getName().toLowerCase());
 						Double price2 = plugin.getConfig().getDouble("packages." + pack2 + ".price");
 						String date = Methods.getCurrentDate();
 						String expiredate = getExpireDate(pack2);
 						Boolean haslimit = plugin.getConfig().getBoolean("packages." + pack2 + ".haslimit");
 						int limit = plugin.getConfig().getInt("packages." + pack2 + ".limit");
 						Boolean activateimmediately = plugin.getConfig().getBoolean("packages." + pack2 + ".activateimmediately");
 						Boolean expires = plugin.getConfig().getBoolean("packages." + pack2 + ".expires");
 
 						if (Methods.NeedActive(sender, pack2)) {
 							s.sendMessage(Prefix + NeedActivation.replace("%pack", pack2));
 							s.sendMessage(Prefix + DPActivate.replace("%pack", pack2));
 							return true;
 						}
 						if (haslimit.equals(false) && activateimmediately.equals(false)) {
 							if (!DonationPoints.permission.has(s, "donationpoints.free")) {
 								Methods.removePoints(price2, sender);
 								String price3 = price2.toString();
 								s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", price3));
 								Methods.logTransaction(sender, price2, pack2, date, "false", "false", null, "false");
 							}
 							if (DonationPoints.permission.has(s, "donationpoints.free")) {
 								s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", "0.00"));
 							}
 							DonationPoints.log.info(s.getName().toLowerCase() + " has made a transaction.");
 							s.sendMessage(Prefix + DPActivate.replace("%pack", pack2));
 							return true;
 						} if (haslimit.equals(false) && activateimmediately.equals(true)) {
 							if (activateimmediately.equals(true)) {
 								List<String> commands = plugin.getConfig().getStringList("packages." + pack2 + ".commands");
 								for (String cmd : commands) {
 									plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", sender));
 								}
 								if (!DonationPoints.permission.has(s, "donationpoints.free")) {
 									Methods.removePoints(price2, sender);
 									String price3 = price2.toString();
 									s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", price3));
 								}
 								if (DonationPoints.permission.has(s, "donationpoints.free")) {
 									s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", "0.00"));
 								}
 								s.sendMessage(Prefix + PackageActivated.replace("%pack", pack2));
 							} if (expires.equals(true)) {
 								Methods.logTransaction(sender, price2, pack2, date, "true", "true", expiredate, "false");
 								s.sendMessage(Prefix + ExpireDate.replace("%pack", pack2).replace("%expiredate", expiredate));
 								DonationPoints.log.info(s.getName().toLowerCase() + " has made a purchase.");
 								return true;
 							} else if (expires.equals(false)) {
 								Methods.logTransaction(sender, price2, pack2, date, "true", "false", null, null);
 								DonationPoints.log.info(s.getName().toLowerCase() + " has made a purchase.");
 								return true;
 							} return true;
 						} else if (haslimit.equals(true)) {
 							ResultSet numberpurchased = DBConnection.sql.readQuery("SELECT COUNT(*) AS size FROM dp_transactions WHERE player = '" + s.getName().toLowerCase() + "' AND package = '" + pack2 + "';");
 							try {
 								numberpurchased.next();
 								int size = numberpurchased.getInt("size");
 								if (size >= limit) {
 									String limit2 = String.valueOf(limit);
 									s.sendMessage(Prefix + LimitReached.replace("%pack", pack2).replace("%limit", limit2));
 								} else if (size < limit) {
 									if (activateimmediately.equals(true)) {
 										List<String> commands = plugin.getConfig().getStringList("packages." + pack2 + ".commands");
 										for (String cmd : commands) {
 											plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", s.getName().toLowerCase()));
 										}
 										String price3 = price2.toString();
 										if (!DonationPoints.permission.has(s, "donationpoints.free")) {
 											Methods.removePoints(price2, sender);
 											s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", price3));
 
 										}
 										if (DonationPoints.permission.has(s, "donationpoints.free")) {
 											s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", "0.00"));
 										}
 										PlayerListener.purchases.remove(s.getName().toLowerCase());
 										s.sendMessage(Prefix + PackageActivated.replace("%pack", pack2));
 										if (expires.equals(true)) {
 											Methods.logTransaction(sender, price2, pack2, date, "true", "true", expiredate, "false");
 											s.sendMessage(Prefix + ExpireDate.replace("%pack", pack2).replace("%expiredate", expiredate));
 											DonationPoints.log.info(s.getName().toLowerCase() + " has made a purchase.");
 											return true;
 										} else if (expires.equals(false)) {
 											Methods.logTransaction(sender, price2, pack2, date, "true", "false", expiredate, "false");
 											DonationPoints.log.info(s.getName().toLowerCase() + " has made a purchase.");
 											return true;
 										} return true;
 									}
 									if (activateimmediately.equals(false))  {
 										if (!DonationPoints.permission.has(s, "donationpoints.free")) {
 											Methods.removePoints(price2, sender);
 											String price3 = price2.toString();
 											s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", price3));
 										}
 										if (DonationPoints.permission.has(s, "donationpoints.free")) {
 											s.sendMessage(Prefix + PurchaseSuccessful.replace("%pack", pack2).replace("%amount", "0.00"));
 										}
 										PlayerListener.purchases.remove(s.getName().toLowerCase());
 										s.sendMessage(Prefix + DPActivate.replace("%pack", pack2));
 										if (expires.equals(true)) {
 											Methods.logTransaction(sender, price2, pack2, date, "false", "true", null, "false");
 										} else if (expires.equals(false)) {
 											Methods.logTransaction(sender, price2, pack2, date, "false", "false", null, "false");
 										}
 										DonationPoints.log.info(s.getName().toLowerCase() + " has made a transaction.");
 										return true;
 									}
 								}
 							} catch (SQLException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("activate")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.activate")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args.length != 2) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					String pack2 = args[1];
 					String expiredate = getExpireDate(pack2);
 					String sender = s.getName();
 					Boolean expires = plugin.getConfig().getBoolean("packages." + pack2 + ".expires");
 					Double ActualPrice = plugin.getConfig().getDouble("packages." + pack2 + ".price");
 
 					ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM dp_transactions WHERE player = '" + sender + "' AND package = '" + pack2 + "' AND activated = 'false';");
 					try {
 						if(rs2.next()) {
 							if (ActualPrice == 0) {
 								s.sendMessage(Prefix + DPFailedActivation.replace("%pack", pack2));
 								s.sendMessage(Prefix + "cPackage names are case sensitive.");
 								return true;
 							}
 							DBConnection.sql.modifyQuery("UPDATE dp_transactions SET activated = 'true' WHERE player = '" + sender + "' AND package = '" + pack2 + "';");
 							s.sendMessage(Prefix + PackageActivated.replace("%pack", pack2));
 							List<String> commands = plugin.getConfig().getStringList("packages." + pack2 + ".commands");
 							for (String cmd : commands) {
 								plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", sender));
 							}
 
 							if (expires.equals(true)) {
 								DBConnection.sql.modifyQuery("UPDATE dp_transactions SET expiredate = '" + expiredate + "' WHERE player = '" + sender + "' AND package = '" + pack2 + "';");
 								s.sendMessage(Prefix + ExpireDate.replace("%pack", pack2).replace("%expiredate", expiredate));
 							} return true;
 						} if (!rs2.next()) {
 							s.sendMessage(Prefix + DPFailedActivation.replace("%pack", pack2));
 							s.sendMessage(Prefix + "cPackage names are case sensitive.");
 							return true;
 						}
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 
 				} else if (args[0].equalsIgnoreCase("set")) {
 					if (args.length != 3) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					if (!DonationPoints.permission.has(s, "donationpoints.set")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					String target = args[1].toLowerCase();
 					Double amount = Double.parseDouble(args[2]);
 					Methods.setPoints(amount, target);
 					String amount2 = amount.toString();
 					s.sendMessage(Prefix + DPSet.replace("%player", target).replace("%amount", amount2));
 				} else if (args[0].equalsIgnoreCase("package")) {
 					if (args.length < 2 | args.length > 4) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					}
 					if (!DonationPoints.permission.has(s, "donationpoints.package.info")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("info")) {
 						String packName = args[2];
 						Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
 						String description = plugin.getConfig().getString("packages." + packName + ".description");
 						s.sendMessage("-----e" + packName + " Infof-----");
 						s.sendMessage("aPackage Name:3 " + packName);
 						s.sendMessage("aPrice:3 " + price + "0");
 						s.sendMessage("aDescription:3 " + description);
 						if (plugin.getConfig().getBoolean("packages." + packName + ".expires") == false) {
 							s.sendMessage("aExpires: 3Never");
 						} else if (plugin.getConfig().getBoolean("packages." + packName + ".expires") == true) {
 							int expiretime = plugin.getConfig().getInt("packages." + packName + ".expiretime");
 							if (expiretime == 1) {
 								s.sendMessage("aExpires After: 3" + expiretime + " Day");
 							} else if (expiretime != 1) {
 								s.sendMessage("aExpires After: 3" + expiretime + " Days");
 							}
 						}
 					}
 					if (args[1].equalsIgnoreCase("list")) {
 						if (!DonationPoints.permission.has(s, "donationpoints.package.list")) {
 							s.sendMessage(Prefix + noPermissionMessage);
 							return true;
 						}
 						Set<String> packs = plugin.getConfig().getConfigurationSection("packages").getKeys(false);
 						s.sendMessage(Prefix + "3Available Packages: a" + packs.toString());
 						return true;
 						
 						
 					}
 				} else if (args[0].equalsIgnoreCase("purchase")) {
 					if (args.length != 2) {
 						s.sendMessage(Prefix + InvalidArguments);
 						return true;
 					} if (!DonationPoints.permission.has(s, "donationpoints.purchase")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					} else {
 						String packName = args[1];
 						Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
 						if (price == 0) {
 							s.sendMessage(Prefix + InvalidPackage);
 							return true;
 						}
 						String username = s.getName();
 						Double balance = Methods.getBalance(username.toLowerCase());
 
 						if (plugin.getConfig().getBoolean("General.SpecificPermissions", true)) {
 							if (!DonationPoints.permission.has(s, "donationpoints.purchase." + packName)) {
 								s.sendMessage(Prefix + noPermissionMessage);
 								return true;
 							}
 							if (DonationPoints.permission.has(s, "donationpoints.purchase." + packName)) {
 								if (DonationPoints.permission.has(s, "donationpoints.free")) {
 									PlayerListener.purchases.put(username.toLowerCase(), packName);
 									if (PlayerListener.purchases.containsKey(username.toLowerCase())) {
 										s.sendMessage(Prefix + DPConfirm.replace("%amount", "0.00").replace("%pack", packName));
 									}
 								} if (!DonationPoints.permission.has(s, "donationpoints.free")) {
 									if (!(balance >= price)) {
 										s.sendMessage(Prefix + NotEnoughPoints);
 									} else if (balance >= price) {
 										PlayerListener.purchases.put(username.toLowerCase(), packName);
 										if (PlayerListener.purchases.containsKey(username.toLowerCase())) {
 											String price2 = price.toString();
 											s.sendMessage(Prefix + DPConfirm.replace("%amount", price2).replace("%pack", packName));
 										}
 									}
 								}
 							}
 						} if (!plugin.getConfig().getBoolean("General.SpecificPermissions")) {
 							if (DonationPoints.permission.has(s, "donationpoints.free")) {
 								PlayerListener.purchases.put(username.toLowerCase(), packName);
 								if (PlayerListener.purchases.containsKey(username.toLowerCase())) {
 									s.sendMessage(Prefix + DPConfirm.replace("%amount", "0.00").replace("%pack", packName));
 								} return true;
 							} else {
 								if (!(balance >= price)) {
 									s.sendMessage(Prefix + NotEnoughPoints);
 									return true;
 								} else if (balance >= price) {
 									PlayerListener.purchases.put(username.toLowerCase(), packName);
 									if (PlayerListener.purchases.containsKey(username.toLowerCase())) {
 										String price2 = price.toString();
 										s.sendMessage(Prefix + DPConfirm.replace("%amount", price2).replace("%pack", packName));
 										return true;
 									}
 								}
 							}
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("version")) {
 					if (!DonationPoints.permission.has(s, "donationpoints.version")) {
 						s.sendMessage(Prefix + noPermissionMessage);
 						return true;
 					}
 					s.sendMessage(Prefix + plugin.getDescription().getVersion());
 					s.sendMessage(Prefix + "http://dev.bukkit.org/server-mods/DonationPoints");
 					s.sendMessage(Prefix + "Created by: MistPhizzle");
 				} else {
 					s.sendMessage(Prefix + NoCommandExists);
 				} return true;
 			}
 		}; donationpoints.setExecutor(exe);
 	}
 
 	public String getExpireDate(String packagename) {
 		int days = plugin.getConfig().getInt("packages." + packagename + ".expiretime");
 		if (!(days == 0)) {
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 			Calendar c = Calendar.getInstance();
 			try {
 				c.setTime(sdf.parse(Methods.getCurrentDate()));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 
 			c.add(Calendar.DATE, days);
 			String exp = sdf.format(c.getTime());
 			return exp;
 		}
 		return null;
 	}
 
 }
