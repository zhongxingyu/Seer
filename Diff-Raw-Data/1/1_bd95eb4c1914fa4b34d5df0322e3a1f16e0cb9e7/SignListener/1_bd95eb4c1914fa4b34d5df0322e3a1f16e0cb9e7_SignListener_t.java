 package com.mistphizzle.donationpoints.plugin;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Hanging;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.block.Sign;
 
 public class SignListener implements Listener {
 
 	public static String SignMessage;
 	public static DonationPoints plugin;
 
 	public SignListener(DonationPoints instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void HangingEvent(HangingBreakByEntityEvent e) {
 		Hanging broken = e.getEntity();
 		Entity remover = e.getRemover();
 
 		if (remover instanceof Player) {
 			Player player = (Player) remover;
 			if (broken instanceof ItemFrame) {
 				Double x = broken.getLocation().getX();
 				Double y = broken.getLocation().getY();
 				Double z = broken.getLocation().getZ();
 				String world = broken.getWorld().getName();
 				if (PlayerListener.links.containsKey(player.getName())) {
 					String packName = PlayerListener.links.get(player.getName());
 					if (Methods.isFrameLinked(x, y, z, world)) {
 						player.sendMessage(Commands.Prefix + "cThis item frame is already linked.");
						PlayerListener.links.remove(player.getName());
 						e.setCancelled(true);
 						return;
 					}
 					Methods.linkFrame(packName, x, y, z, world);
 					player.sendMessage(Commands.Prefix + "cSuccessfully linked 3" + packName + "3.");
 					PlayerListener.links.remove(player.getName());
 					e.setCancelled(true);
 				} else if (!PlayerListener.links.containsKey(player.getName())) {
 					if (Methods.isFrameLinked(x, y, z, world)) {
 						if (player.isSneaking()) {
 							if (!DonationPoints.permission.has(player, "donationpoints.sign.break")) {
 								player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 								e.setCancelled(true);
 								return;
 							}
 							if (DonationPoints.permission.has(player, "donationpoints.sign.break")) {
 								Methods.unlinkFrame(x, y, z, world);
 								player.sendMessage(Commands.Prefix + "cItem Frame unlinked.");
 								e.setCancelled(false);
 								return;
 							}
 						}
 						e.setCancelled(true);
 						String packName = Methods.getLinkedPackage(x, y, z, world);
 						Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
 						String packDesc = plugin.getConfig().getString("packages." + packName + ".description");
 						if (!DonationPoints.permission.has(player, "donationpoints.sign.use")) {
 							player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 							return;
 						}
 						if (DonationPoints.permission.has(player, "donationpoints.free")) {
 							price = 0.0;
 						}
 						if (plugin.getConfig().getBoolean("General.SpecificPermissions", true)) {
 							if (!DonationPoints.permission.has(player, "donationpoints.sign.use." + packName)) {
 								player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 								e.setCancelled(true);
 								return;
 							}
 							player.sendMessage(Commands.Prefix + "cRight Clicking this sign will allow you to purchase 3" + packName + "c for 3" + price + "c.");
 							player.sendMessage(Commands.Prefix + "cDescription: 3" + packDesc);
 						}
 						if (!plugin.getConfig().getBoolean("General.SpecificPermissions")) {
 							player.sendMessage(Commands.Prefix + "cRight Clicking this sign will allow you to purchase 3" + packName + "c for 3" + price + "c.");
 							player.sendMessage(Commands.Prefix + "cDescription: 3" + packDesc);
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent e) {
 		Player player = e.getPlayer();
 		Block block = e.getBlock();
 
 		if (block.getState() instanceof Sign) {
 			Sign s = (Sign) block.getState();
 			String signline1 = s.getLine(0);
 			if (signline1.equalsIgnoreCase("[" + SignMessage + "]") && DonationPoints.permission.has(player, "donationpoints.sign.break")) {
 				if (player.getGameMode() == GameMode.CREATIVE) {
 					if (!player.isSneaking()) {
 						player.sendMessage(Commands.Prefix + "cYou must sneak to break DonationPoints signs while in Creative.");
 						e.setCancelled(true);
 					}
 				}
 			}
 			if (!DonationPoints.permission.has(player, "donationpoints.sign.break")) {
 				player.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 				e.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onSignChance(SignChangeEvent e) {
 		if (e.isCancelled()) return;
 		if (e.getPlayer() == null) return;
 		Player p = e.getPlayer();
 		String line1 = e.getLine(0);
 		Block block = e.getBlock();
 		String pack = e.getLine(1);
 
 		// Permissions
 		if (line1.equalsIgnoreCase("[" + SignMessage + "]") && !DonationPoints.permission.has(p, "donationpoints.sign.create")) {
 			e.setCancelled(true);
 			block.breakNaturally();
 			p.sendMessage(Commands.Prefix + Commands.noPermissionMessage);
 		} else if (DonationPoints.permission.has(p, "donationpoints.sign.create") && line1.equalsIgnoreCase("[" + SignMessage + "]")) {
 			if (block.getType() == Material.SIGN_POST) {
 				p.sendMessage(Commands.Prefix + "cDonationPoints signs must be placed on a wall.");
 				block.breakNaturally();
 				e.setCancelled(true);
 			} if (plugin.getConfig().getString("packages." + pack) == null) {
 				e.setCancelled(true);
 				p.sendMessage(Commands.Prefix + Commands.InvalidPackage);
 				block.breakNaturally();
 			} if (e.getLine(1).isEmpty()) {
 				e.setCancelled(true);
 				p.sendMessage(Commands.Prefix + Commands.InvalidPackage);
 				block.breakNaturally();
 			} else {
 				if (plugin.getConfig().getBoolean("General.AutoFillSigns", true)) {
 					Double price = plugin.getConfig().getDouble("packages." + pack + ".price");
 					e.setLine(2, (price + " Points"));
 				}
 				p.sendMessage(Commands.Prefix + "cYou have created a DonationPoints sign.");
 			}
 		} 
 	}
 }
