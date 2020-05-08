 package com.mistphizzle.donationpoints.plugin;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.Rotation;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 @SuppressWarnings("unused")
 public class PlayerListener implements Listener {
 
 	public static String SignMessage;
 
 	public static DonationPoints plugin;
 	
 	public static int confirmTask;
 
 	public PlayerListener(DonationPoints instance) {
 		plugin = instance;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static HashMap<String, String> purchases = new HashMap();
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static HashMap<String, String> links = new HashMap();
 
 
 	@EventHandler
 	public void onPlayerTeleport(PlayerTeleportEvent e) {
 		if (purchases.containsKey(e.getPlayer().getName().toLowerCase())) {
 			purchases.remove(e.getPlayer().getName().toLowerCase());
 			e.getPlayer().sendMessage(Commands.Prefix + Commands.TooLongOnConfirm);
 		}
 	}
 	
 	@EventHandler 
 	public void playerEntityInteract(PlayerInteractEntityEvent event) {
 		Player player = event.getPlayer();
 		Entity entity = event.getRightClicked();
 		if (entity instanceof ItemFrame) {
 			Double x = entity.getLocation().getX();
 			Double y = entity.getLocation().getY();
 			Double z = entity.getLocation().getZ();
 			String world = entity.getWorld().getName();
 			if (Methods.isFrameLinked(x, y, z, world, Commands.Server)) {
 				((ItemFrame) entity).setRotation(Rotation.NONE);
 				if (!DonationPoints.permission.has(player, "donationpoints.sign.use")) {
 					player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 					event.setCancelled(true);
 					return;
 				}
 				String packName = Methods.getLinkedPackage(x, y, z, world, Commands.Server);
 				if (DonationPoints.permission.has(player, "donationpoints.sign.use")) {
 					if (!plugin.getConfig().contains("packages." + packName + ".requireprerequisite")) {
 						plugin.getConfig().set("packages." + packName + ".requireprerequisite", false);
 						plugin.saveConfig();
 					}
 					if (plugin.getConfig().getBoolean("packages." + packName + ".requireprerequisite")) {
 						String prerequisite = plugin.getConfig().getString("packages." + packName + ".prerequisite");
 						if (!Methods.hasPurchased(player.getName(),  prerequisite, Commands.Server)) {
 							player.sendMessage(Commands.Prefix + Commands.DPPrerequisite.replace("%pack",  prerequisite));
 							event.setCancelled(true);
 							return;
 						}
 					}
 					if (plugin.getConfig().getBoolean("General.SpecificPermissions", true)) {
 						if (!DonationPoints.permission.has(player, "donationpoints.sign.use." + packName)) {
 							player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 							event.setCancelled(true);
 							return;
 						}
 						if (DonationPoints.permission.has(player, "donationpoints.sign.use." + packName)) {
 							Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
 							String username = player.getName().toLowerCase();
 							Double balance = Methods.getBalance(username);
 							if (DonationPoints.permission.has(player, "donationpoints.free")) {
 								price = 0.0;
 								purchases.put(username, packName);
 								if (purchases.containsKey(username)) {
 									player.sendMessage(Commands.Prefix + Commands.DPConfirm.replace("%amount", "0.00").replace("%pack", packName));
 									event.setCancelled(true);
 									return;
 								}
 							}
 							if (!DonationPoints.permission.has(player, "donationpoints.free")) {
 								if (!(balance >= price)) {
 									player.sendMessage(Commands.Prefix + Commands.NotEnoughPoints);
 									event.setCancelled(true);
 								} else if (balance >= price) {
 									purchases.put(username, packName);
 									if (purchases.containsKey(username)) {
 										String price2 = price.toString();
 										player.sendMessage(Commands.Prefix + Commands.DPConfirm.replace("%pack",  packName).replace("%amount", price2));
 										event.setCancelled(true);
 										return;
 									}
 								}
 							}
 						}
 					} if (!plugin.getConfig().getBoolean("General.SpecificPermissions")) {
 						Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
 						String username = player.getName().toLowerCase();
 						Double balance = Methods.getBalance(username);
 
 						if (DonationPoints.permission.has(player, "donationpoints.free")) {
 							purchases.put(username, packName);
 							if (purchases.containsKey(username)) {
 								player.sendMessage(Commands.Prefix + "§cUse §3/dp confirm §cto confirm.");
 								event.setCancelled(true);
 								return;
 							}
 						} else {
 							if (!(balance >= price)) {
 								player.sendMessage(Commands.Prefix + Commands.NotEnoughPoints);
 								event.setCancelled(true);
 								return;
 							} else if (balance >= price) {
 								purchases.put(username, packName);
 								if (purchases.containsKey(username)) {
 									String price2 = price.toString();
 									player.sendMessage(Commands.Prefix + Commands.DPConfirm.replace("%pack",  packName).replace("%amount", price2));
 									event.setCancelled(true);
 									return;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		final Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 		if (block.getState() instanceof Sign) {
 			Sign s = (Sign) block.getState();
 			String signline1 = s.getLine(0);
 			if (signline1.equalsIgnoreCase("[" + SignMessage + "]")
 					&& event.getAction().equals(Action.LEFT_CLICK_BLOCK)
 					&& block.getType() == Material.WALL_SIGN) {
 				if (!DonationPoints.permission.has(player, "donationpoints.sign.use")) {
 					player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 				}
 				if (DonationPoints.permission.has(player, "donationpoints.sign.use")) {
 					String purchasedPack = s.getLine(1);
 					Double price = plugin.getConfig().getDouble("packages." + purchasedPack + ".price");
 					String packDesc = plugin.getConfig().getString("packages." + purchasedPack + ".description");
 					player.sendMessage(Commands.Prefix + Commands.SignLeftClick.replace("%pack", purchasedPack).replace("%price", price.toString()));
 					player.sendMessage(Commands.Prefix + Commands.SignLeftClickDescription.replace("%desc", packDesc));
 				}
 			}
 			if (signline1.equalsIgnoreCase("[" + SignMessage + "]")
 					&& event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
 					&& block.getType() == Material.WALL_SIGN) {
 				if (!DonationPoints.permission.has(player, "donationpoints.sign.use")) {
 					player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 				}
 				if (DonationPoints.permission.has(player, "donationpoints.sign.use")) {
 					String purchasedPack = s.getLine(1);
 					if (!plugin.getConfig().contains("packages." + purchasedPack + ".requireprerequisite")) {
 						plugin.getConfig().set("packages." + purchasedPack + ".requireprerequisite", false);
 						plugin.saveConfig();
 					}
 					if (plugin.getConfig().getBoolean("packages." + purchasedPack + ".requireprerequisite")) {
 						String prerequisite = plugin.getConfig().getString("packages." + purchasedPack + ".prerequisite");
 						if (!Methods.hasPurchased(player.getName(), prerequisite, Commands.Server)) {
 							player.sendMessage(Commands.Prefix + Commands.DPPrerequisite.replace("%pack", prerequisite));
 							return;
 						}
 					}
 					if (plugin.getConfig().getBoolean("General.SpecificPermissions", true)) {
 						if (!DonationPoints.permission.has(player, "donationpoints.sign.use." + purchasedPack)) {
 							player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 							return;
 						}
 						if (DonationPoints.permission.has(player, "donationpoints.sign.use." + purchasedPack)) {
 							Double price = plugin.getConfig().getDouble("packages." + purchasedPack + ".price");
 							String username = player.getName().toLowerCase();
 							Double balance = Methods.getBalance(username);
 							if (DonationPoints.permission.has(player, "donationpoints.free")) {
 								purchases.put(username, purchasedPack);
 								if (purchases.containsKey(username)) {
 									player.sendMessage(Commands.Prefix + Commands.DPConfirm.replace("%amount", "0.00").replace("%pack", purchasedPack));
 								}
 							} if (!DonationPoints.permission.has(player, "donationpoints.free")) {
 								if (!(balance >= price)) {
 									player.sendMessage(Commands.Prefix + Commands.NotEnoughPoints);
 								} else if (balance >= price) {
 									purchases.put(username, purchasedPack);
 									if (purchases.containsKey(username)) {
 										String price2 = price.toString();
 										player.sendMessage(Commands.Prefix + Commands.DPConfirm.replace("%pack", purchasedPack).replace("%amount", price2));
 									}
 
 								}
 							}
 						}
 
 					} 
 					if (!plugin.getConfig().getBoolean("General.SpecificPermissions")) {
 						Double price = plugin.getConfig().getDouble("packages." + purchasedPack + ".price");
 						String username = player.getName().toLowerCase();
 						Double balance = Methods.getBalance(username);
 
 						if (DonationPoints.permission.has(player, "donationpoints.free")) {
 							purchases.put(username, purchasedPack);
 							if (purchases.containsKey(username)) {
 								player.sendMessage(Commands.Prefix + "§cUse §3/dp confirm §cto confirm.");
 							}
 						} else {
 							if (!(balance >= price)) {
 								player.sendMessage(Commands.Prefix + Commands.NotEnoughPoints);
 							} else if (balance >= price) {
 								purchases.put(username, purchasedPack);
 								if (purchases.containsKey(username)) {
 									String price2 = price.toString();
 									player.sendMessage(Commands.Prefix + Commands.DPConfirm.replace("%pack", purchasedPack).replace("%amount", price2));
 									confirmTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 										public void run() {
 											if (purchases.containsKey(player.getName().toLowerCase())) {
 												purchases.remove(player.getName().toLowerCase());
 												player.sendMessage(Commands.Prefix + Commands.TooLongOnConfirm);
 											}
 										}
 									}, 300L);
 								}
 							}
 						}
 					}
 					event.setUseItemInHand(Result.DENY);
 					event.setUseInteractedBlock(Result.DENY);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	@EventHandler
 	public void PlayerJoinEvent(PlayerJoinEvent e) {
 		Player p = e.getPlayer();
 		String user = p.getName();
 		if (plugin.getConfig().getBoolean("General.AutoCreateAccounts", true)) {
 			if (!Methods.hasAccount(user.toLowerCase())) {
 				Methods.createAccount(user.toLowerCase());
 				plugin.log.info("Created an account for " + user.toLowerCase());
 			}
 		}
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable + " WHERE player = '" + user + "' AND expiredate = '" + Methods.getCurrentDate() + "';");
 		try {
 			if (rs2.next()) {
 				String pack2 = rs2.getString("package");
 
 				List<String> commands = plugin.getConfig().getStringList("packages." + pack2 + ".expirecommands");
 				for (String cmd : commands) {
 					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", user));
 				}
 				DBConnection.sql.modifyQuery("UPDATE " + DBConnection.transactionTable + " SET expired = 'true' WHERE player = '" + user + "' AND expiredate = '" + Methods.getCurrentDate() + "' AND package = '" + pack2 + "';");
 			} else if (!rs2.next()) {
 			}
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 }
