 package com.github.dreadslicer.tekkitrestrict;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryClickEvent;
 
 public class TRNoDupe {
 
 	@SuppressWarnings("unused")
 	private static boolean preventAlcDupe, preventRMDupe, preventTransmuteDupe;
 	public static int dupeAttempts = 0;
 	public static String lastPlayer = "";
 
 	public static void reload() {
 		preventAlcDupe = tekkitrestrict.config.getBoolean("PreventAlcDupe");
 		preventRMDupe = tekkitrestrict.config
 				.getBoolean("PreventRMFurnaceDupe");
 		preventTransmuteDupe = tekkitrestrict.config
 				.getBoolean("PreventTransmuteDupe");
 	}
 
 	public static void handleDupes(InventoryClickEvent event) {
 		// event.getInventory();
 		Player player = tekkitrestrict.getInstance().getServer()
 				.getPlayer(event.getWhoClicked().getName());
 		int slot = event.getSlot();
 
 		String title = event.getView().getTopInventory().getTitle()
 				.toLowerCase();
 		
 		
		//tekkitrestrict.log.info("t0-"+title+"-"+slot+"-"+event.isShiftClick());
 		// RMDupe Slot35
 		if (!TRPermHandler.hasPermission(player, "dupe", "bypass", "")) {
 			// tekkitrestrict.log.info("t0-"+title+"-");
 			if (title.equals("rm furnace")) {
 				// tekkitrestrict.log.info("t1");
 				if (slot == 35) {
 					// tekkitrestrict.log.info("t2");
 					if (event.isShiftClick()) {
 						// tekkitrestrict.log.info("t3");
 						if (preventRMDupe) {
 							event.setCancelled(true);
 							player.sendRawMessage("[TRDupe] you are not allowed to Shift+Click here while using a RM Furnace!");
 
 							TRLogger.Log("Dupe", player.getName()
 									+ " attempted to dupe using a RM Furnace!");
 						} else {
 							TRLogger.Log("Dupe", player.getName()
 									+ " duped using a RM Furnace!");
 						}
 						dupeAttempts++;
 						TRLogger.broadcastDupe(player.getName(),
 								"the RM Furnace", "RMFurnace");
 					}
 				}
 			} else if (title.equals("tank cart")) {
 				// tekkitrestrict.log.info("t1");
 				if (slot == 35) {
 					// tekkitrestrict.log.info("t2");
 					if (event.isShiftClick()) {
 						event.setCancelled(true);
 						player.sendRawMessage("[TRDupe] you are not allowed to Shift+Click here while using a Tank Cart!");
 
 						TRLogger.Log("Dupe", player.getName()
 								+ " attempted to dupe using a Tank Cart!");
 						dupeAttempts++;
 						TRLogger.broadcastDupe(player.getName(),
 								"the Tank Cart", "TankCart");
 					}
 				}
 			} else if (title.equals("trans tablet")) {
 				// slots-6 7 5 3 1 0 2
 				int item = event.getCurrentItem().getTypeId();
 				if (item == 27557) {
 				}
 				if (item == 27558) {
 				}
 				if (item == 27559) {
 				}
 				if (item == 27560) {
 				}
 				if (item == 27561) {
 				}
 				if (item == 27591) {
 				}
 				if (event.isShiftClick()) {
 					// if (isKlein) {
 					boolean isslot = slot == 0 || slot == 1 || slot == 2
 							|| slot == 3 || slot == 4 || slot == 5 || slot == 6
 							|| slot == 7;
 					if (isslot) {
 						if (preventTransmuteDupe) {
 							event.setCancelled(true);
 							player.sendRawMessage("[TRDupe] you are not allowed to Shift+Click any ");
 							player.sendRawMessage("           item out of the transmutation table!");
 
 							TRLogger.Log("Dupe", player.getName()
 									+ " attempted to transmute dupe!");
 						} else {
 							TRLogger.Log("Dupe", player.getName()
 									+ " attempted to transmute dupe!");
 						}
 						dupeAttempts++;
 						TRLogger.broadcastDupe(player.getName(),
 								"the Transmutation Table", "transmute");
 					}
 					// }
 				}
 			}
 		}
 	}
 
 	public static void handleDropDupes(
 			org.bukkit.event.player.PlayerDropItemEvent e) {
 		Player player = e.getPlayer();
 		TRNoDupe_BagCache cache;
 		if ((cache = TRNoDupe_BagCache.check(player)) != null) {
 			if (cache.hasBHBInBag) {
 				try {
 					TRNoDupe_BagCache.expire(cache);
 					e.setCancelled(true);
 					player.kickPlayer("[TRDupe] you have a Black Hole Band in your ["
 							+ cache.inBagColor
 							+ "] Alchemy Bag! Please remove it NOW!");
 
 					
 					TRLogger.Log("Dupe", player.getName() + " ["
 							+ cache.inBagColor
 							+ " bag] attempted to dupe with the "
 							+ cache.dupeItem + "!");
 					TRLogger.broadcastDupe(player.getName(),
 							"the Alchemy Bag and " + cache.dupeItem, "alc");
 				} catch (Exception err) {
 				}
 			}
 		}
 	}
 }
