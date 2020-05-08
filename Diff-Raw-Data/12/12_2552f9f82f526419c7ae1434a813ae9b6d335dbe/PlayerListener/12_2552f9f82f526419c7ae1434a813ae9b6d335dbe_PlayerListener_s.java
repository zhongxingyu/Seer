 package com.mistphizzle.donationpoints.plugin;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
 
 	public static String SignMessage;
 
 	public static DonationPoints plugin;
 
 	public PlayerListener(DonationPoints instance) {
 		plugin = instance;
 	}
 
 	public static HashMap<String, String> purchases = new HashMap();
 	public static HashMap<String, String> purchasedPack = new HashMap();
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 		if (block.getState() instanceof Sign) {
 			Sign s = (Sign) block.getState();
 			String signline1 = s.getLine(0);
 			if (signline1.equalsIgnoreCase("[" + SignMessage + "]")
 					&& event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
 					&& block.getType() == Material.WALL_SIGN) {
 				if (!player.hasPermission("donationpoints.sign.use")) {
 					player.sendMessage("cYou don't have permission to use the DonationPoints signs.");
 				}
 				if (player.hasPermission("donationpoints.sign.use")) {
 					String purchasedPack = s.getLine(1);
 					Double price = plugin.getConfig().getDouble("packages." + purchasedPack + ".price");
					String username = player.getName();
 					ResultSet playerBalance1 = DBConnection.sql.readQuery("SELECT balance FROM points_players WHERE player = '" + username + "';");
 					try {
 						while (playerBalance1.next()) {
 							Double newbalance = playerBalance1.getDouble("balance");
 
 							if (!(newbalance >= price)) {
 								player.sendMessage("cYou don't have enough points for this package.");
 							} else if (newbalance >= price) {
 								purchases.put(username, purchasedPack);
 								if (purchases.containsKey(username)) {
 									player.sendMessage("aType 3/dp confirm a to purchase 3" + purchasedPack + "a for 3" + price + "a points.");
 								} else {
 									player.sendMessage("Hashmap / other error.");
 								}
 							}
 						}
 						event.setUseItemInHand(Result.DENY);
 						event.setUseInteractedBlock(Result.DENY);
 					} catch (SQLException ex) {
 						ex.printStackTrace();
 						player.sendMessage("4Something went wrong with your transaction and it was not completed.");
 						player.sendMessage("4Please contact an Administrator as soon as possible.");
 					}
 				}
 			}
 		}
 	}
 
 	// Update Checker
 	@EventHandler
 	public void playerUpdateCheck(PlayerJoinEvent e) {
 		Player p = e.getPlayer();
 		if (UpdateChecker.updateNeeded() && p.hasPermission("donationpoints.update") && plugin.getConfig().getBoolean("General.AutoCheckForUpdates", true)) {
 			p.sendMessage("cYour version of DonationPoints differs from the one on Bukkit.");
 			p.sendMessage("cPerhaps it's time for an update?");
 		}
 	}
 
 	@EventHandler
 	public void AutoCreateAccount(PlayerJoinEvent e) {
 		Player p = e.getPlayer();
 		String user = p.getName().toLowerCase();
 		if (plugin.getConfig().getBoolean("General.AutoCreateAccounts", true)) {
 			ResultSet rs2 = DBConnection.sql.readQuery("SELECT balance FROM points_players WHERE player = '" + user + "';");
 			try {
 				if (rs2.next()) {
 					do {
 						// Does nothing because the player already has an account.
 					} while (rs2.next());
 				} else if (!rs2.next()) {
 					DBConnection.sql.modifyQuery("INSERT INTO points_players(player, balance) VALUES ('" + user + "', 0)");
 					plugin.log.info("Created an account for " + user);
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 }
