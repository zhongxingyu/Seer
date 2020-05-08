 package com.github.dreadslicer.tekkitrestrict;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Listeners;
 
 public class TRLimitedCreative {
 	public static void handleCreativeInvClick(InventoryClickEvent event) {
 		Player player = (Player) event.getWhoClicked();
 		
 		if (Util.hasBypass(player, "creative")) return;
 		
 		try {
 			if (event.getView().getTopInventory() == null) return;
 			String invname = event.getView().getTopInventory().getName();
 			if (Listeners.BlockCreativeContainer) {
 				if (!invname.equals("container.inventory")) {
 					// player.sendRawMessage("[TRLimitedCreative] You may not interact with other inventories");
 					player.sendMessage(ChatColor.RED + "[TRLimitedCreative] You may not interact with other inventories");
					event.setCancelled(true);
					//if (event.getCurrentItem() != null) {
					//	event.setCurrentItem(null);
					//	player.getInventory().addItem(player.getItemOnCursor());
					//	player.setItemOnCursor(null);
					//}
 					return;
 				}
 			}
 
 			try {
 				// determine of player attempted to pick an item up...
 				ItemStack ccc = event.getCurrentItem();
 				if (ccc == null) return;
 				if (TRNoItem.isCreativeItemBanned(player,
 								new com.github.dreadslicer.tekkitrestrict.ItemStack(ccc.getTypeId(), 0, ccc.getData().getData()))) {
 					player.sendMessage(ChatColor.RED + "[TRLimitedCreative] You cannot obtain/modify this item type!");
					event.setCancelled(true);
					//event.setCurrentItem(null);
					//player.getInventory().addItem(player.getItemOnCursor());
					//player.setItemOnCursor(null);
 					return;
 				}
 				
 			} catch (Exception ex) {
 				TRLogger.Log("debug", "Error! [TRLimitedCreative IBlock] : " + ex.getMessage());
 				Log.Exception(ex);
 			}
 		} catch (Exception ex) {
 			TRLogger.Log("debug", "Error! [TRLimitedCreative CBlock] : " + ex.getMessage());
 			Log.Exception(ex);
 		}
 	}
 }
