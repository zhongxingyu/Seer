 package com.massivecraft.factions.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.Conf;
 import com.massivecraft.factions.FLocation;
 import com.massivecraft.factions.FPlayer;
 import com.massivecraft.factions.FPlayers;
 import com.massivecraft.factions.FWar;
 import com.massivecraft.factions.FWars;
 import com.massivecraft.factions.P;
 
 public class FactionsInventoryListener implements Listener {
 	public P p;
 
 	public FactionsInventoryListener(P p) {
 		this.p = p;
 	}
 
	@EventHandler(priority = EventPriority.NORMAL)
 	public void onInventoryOpenEvent(InventoryOpenEvent event){
 		InventoryHolder holder = event.getInventory().getHolder();
 		
 		if(Conf.safeDenyChestsInWar){
 			if(holder instanceof Chest){
 				Chest chest = (Chest) holder;
 				
 				if(Board.getFactionAt(new FLocation(chest.getBlock())).isSafeZone()){
 					FPlayer me = FPlayers.i.get((Player) event.getPlayer());
 					if(me.getFaction().isInWar()){
 						event.setCancelled(true);
 						me.sendMessage("<i>You can not open chests in the safezone while you are in a war!");
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onInventoryClose(InventoryCloseEvent event) {
 		InventoryView inv = event.getView();
 		Player player = (Player) event.getPlayer();
 
 		if (inv != null) {
 			for (FWar fwar : FWars.i.get()) {
 				if (fwar != null) {
 					if (fwar.tempInvs != null) {
 						if (fwar.tempInvs.contains(inv)) {
 							fwar.removeTempInventory(inv);
 							player.sendMessage(ChatColor.GREEN + "Items hinzugefgt!");
 						}
 					}
 
 					if (fwar.tempInvsFromTarget != null) {
 						if (fwar.tempInvsFromTarget.contains(inv)) {
 							fwar.removeTempInventoryFromTarget(inv);
 							player.sendMessage(ChatColor.GREEN + "Items hinzugefgt!");
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onInventoryClick(InventoryClickEvent event) {
 		InventoryView inv = event.getView();
 		Player player = (Player) event.getWhoClicked();
 		ItemStack istack = event.getCurrentItem();
 
 		/* Faction Inventory */
 		if (istack != null) {
 			for (FPlayer fpl : FPlayers.i.get()) {
 				if (inv.equals(fpl.playerInventoryView)) {
 					event.setCancelled(true);
 
 					if (event.getRawSlot() < 54) {
 						ItemStack dataStack = istack.clone();
 
 						ItemStack lostItems = player.getInventory().addItem(istack).get(0);
 						event.setCurrentItem(lostItems);
 
 						for (String matString : fpl.getFaction().factionInventory.keySet()) {
 							MaterialData mat = FWar.convertStringToMaterialData(matString);
 							Integer args = fpl.getFaction().factionInventory.get(matString);
 
 							if (mat.equals(dataStack.getData())) {
 								if (lostItems != null) {
 									args = args - (dataStack.getAmount() - lostItems.getAmount());
 								} else {
 									args = args - dataStack.getAmount();
 								}
 
 								fpl.getFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), args);
 							}
 						}
 					}
 				}
 			}
 
 			/* War Pay-Inventory */
 			if (inv != null) {
 				for (FWar fwar : FWars.i.get()) {
 					if (fwar != null) {
 						if (fwar.tempInvsFromTarget != null) {
 							if (fwar.tempInvsFromTarget.contains(inv)) {
 								event.setCancelled(true);
 
 								if (event.getRawSlot() > 53) {
 									int slot = 0;
 									for (slot = 0; slot < 54; slot++) {
 										ItemStack tempIStack = inv.getItem(slot);
 
 										if (tempIStack != null) {
 											if (tempIStack.getData().equals(istack.getData())) {
 												int amount;
 												MaterialData mData = istack.getData();
 
 												if (istack.getAmount() > tempIStack.getAmount()) {
 													amount = tempIStack.getAmount();
 
 													if (istack.getAmount() - tempIStack.getAmount() > 0) {
 														istack.setAmount(istack.getAmount() - tempIStack.getAmount());
 													} else {
 														event.setCurrentItem(null);
 													}
 
 													tempIStack.setTypeId(0);
 													inv.setItem(slot, tempIStack);
 												} else {
 													amount = istack.getAmount();
 
 													if (tempIStack.getAmount() - istack.getAmount() > 0) {
 														tempIStack.setAmount(tempIStack.getAmount() - istack.getAmount());
 													} else {
 														tempIStack.setTypeId(0);
 														inv.setItem(slot, tempIStack);
 													}
 
 													event.setCurrentItem(null);
 												}
 
 												for (String matString : fwar.itemsToPay.keySet()) {
 													MaterialData mat = FWar.convertStringToMaterialData(matString);
 													int oldAmount = fwar.itemsToPay.get(matString);
 
 													if (mat.equals(mData)) {
 														fwar.itemsToPay.put(matString, oldAmount - amount);
 														break;
 													}
 												}
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
