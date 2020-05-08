 package com.lostaris.bukkit.ItemRepair;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import com.nijikokun.bukkit.iConomy.Misc;
 import com.nijikokun.bukkit.iConomy.iConomy;
 
 public class Repair extends AutoRepairSupport{
 	public static final Logger log = Logger.getLogger("Minecraft");
 
 	public Repair(AutoRepairPlugin instance) {
 		super(instance, getPlayer());
 	}
 
 	public boolean manualRepair(ItemStack tool, int slot) {
 		if (!AutoRepairPlugin.isAllowed(getPlayer(), "repair")) {
 			getPlayer().sendMessage("cYou dont have permission to do the repair command.");
 			return false;
 		}
 
 		PlayerInventory inven = getPlayer().getInventory();
 		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
 		String itemName = Material.getMaterial(tool.getTypeId()).toString();
		ArrayList<ItemStack> req = recipies.get(itemName);	
		String toolString = tool.getType().toString();
 		int balance;
 
 		if (!AutoRepairPlugin.isRepairCosts()) {
 			getPlayer().sendMessage("3Repaired " + itemName);
 			inven.setItem(slot, repItem(tool));
 			// icon cost only
 		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
			if (AutoRepairPlugin.getiConCosts().containsKey(toolString)) {
 				balance = iConomy.db.get_balance(player.getName());
 				int cost = AutoRepairPlugin.getiConCosts().get(itemName);
 				if (cost <= balance) {
 					//balance = iConomy.db.get_balance(player.getName());
 					iConomy.db.set_balance(player.getName(), balance - cost);
 					player.sendMessage("3Using " + Misc.formatCurrency(cost, iConomy.currency) + " to repair " + itemName);
 					//inven.setItem(slot, repItem(tool));
 					inven.setItem(slot, repItem(tool));
 				} else {
 					iConWarn(itemName, cost);
 				}
 			} else {
 				player.sendMessage("cThis is not a tool");
 			}
 			//both icon and item cost
 		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
			if (AutoRepairPlugin.getiConCosts().containsKey(toolString)
					&& AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
 				balance = iConomy.db.get_balance(player.getName());
 
 				int cost = AutoRepairPlugin.getiConCosts().get(itemName);						
 				if (cost <= balance && isEnoughItems(req)) {
 					//balance = iConomy.db.get_balance(player.getName());
 					iConomy.db.set_balance(player.getName(), balance - cost);
 					deduct(req);
 					player.sendMessage("3Using " + Misc.formatCurrency(cost, iConomy.currency) + " and");
 					player.sendMessage("3" + printFormatReqs(req) + " to repair "  + itemName);
 					inven.setItem(slot, repItem(tool));
 				} else {
 					bothWarn(itemName, cost, req);
 				}
 			} else {
 				player.sendMessage("cThis is not a tool");
 			}
 			// just item cost
 		} else {
			if (AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
 				if (isEnoughItems(req)) {
 					deduct(req);
 					player.sendMessage("3Using " + printFormatReqs(req) + " to repair " + itemName);
 					inven.setItem(slot, repItem(tool));
 				} else {
 					justItemsWarn(itemName, req);
 				}
 			} else {
 				player.sendMessage("cThis is not a tool");
 			}
 		}
 
 		return false;		
 	}
 
 	public boolean autoRepairTool(ItemStack tool, int slot) {
 		if (!AutoRepairPlugin.isAllowed(player, "auto")) { 
 			return false;
 		}
 
 		PlayerInventory inven = player.getInventory();
 		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
 		String itemName = Material.getMaterial(tool.getTypeId()).toString();
 		ArrayList<ItemStack> req = recipies.get(itemName);		
 		int balance;
 
 		if (!AutoRepairPlugin.isRepairCosts()) {
 			player.sendMessage("3Repaired " + itemName);
 			inven.setItem(slot, repItem(tool));
 			// icon cost only
 		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
 			balance = iConomy.db.get_balance(player.getName());
 			int cost = AutoRepairPlugin.getiConCosts().get(itemName);
 			if (cost <= balance) {
 				balance = iConomy.db.get_balance(player.getName());
 				iConomy.db.set_balance(player.getName(), balance - cost);
 				player.sendMessage("3Using " + Misc.formatCurrency(cost, iConomy.currency) + " to repair " + itemName);
 				inven.setItem(slot, repItem(tool));
 			} else {
 				if (!getLastWarning()) {
 					if (AutoRepairPlugin.isAllowed(player, "warn")) {
 						iConWarn(itemName, cost);					
 					}
 					setLastWarning(true);							
 				}
 			}
 			//both icon and item cost
 		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
 			balance = iConomy.db.get_balance(player.getName());
 			int cost = AutoRepairPlugin.getiConCosts().get(itemName);						
 			if (cost <= balance && isEnoughItems(req)) {
 				balance = iConomy.db.get_balance(player.getName());
 				iConomy.db.set_balance(player.getName(), balance - cost);
 				deduct(req);
 				player.sendMessage("3Using " + Misc.formatCurrency(cost, iConomy.currency) + " and");
 				player.sendMessage("3" + printFormatReqs(req) + " to repair "  + itemName);
 				inven.setItem(slot, repItem(tool));
 			} else {
 				if (!getLastWarning()) {
 					if (AutoRepairPlugin.isAllowed(player, "warn")) {
 						bothWarn(itemName, cost, req);					
 					}
 					setLastWarning(true);							
 				}
 
 			}			
 			// just item cost
 		} else {
 			if (isEnoughItems(req)) {
 				deduct(req);
 				player.sendMessage("3Using " + printFormatReqs(req) + " to repair " + itemName);
 				inven.setItem(slot, repItem(tool));
 			} else {
 				if (!getLastWarning()) {
 					if (AutoRepairPlugin.isAllowed(player, "warn")) {
 						justItemsWarn(itemName, req);					
 					}
 					setLastWarning(true);							
 				}
 			}
 		}
 		return false;
 	}
 
 	public void repairArmour() {
 		if (!AutoRepairPlugin.isAllowed(player, "repair")) {
 			player.sendMessage("cYou dont have permission to do the repair command.");
 			return;
 		}
 
 		PlayerInventory inven = player.getInventory();
 		ArrayList<ItemStack> req = repArmourAmount();
 		int total =0;
 		int balance;
 
 		if (!AutoRepairPlugin.isRepairCosts()) {
 			player.sendMessage("3Repaired your armour");
 			repArm();
 			// icon cost only
 		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
 			balance = iConomy.db.get_balance(player.getName());
 
 			for (ItemStack i : inven.getArmorContents()) {				
 				if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
 					total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
 				}				
 			}
 			if (total <= balance) {
 				balance = iConomy.db.get_balance(player.getName());
 				iConomy.db.set_balance(player.getName(), balance - total);
 				player.sendMessage("3Using " + Misc.formatCurrency(total, iConomy.currency) + " to repair your armour");
 				repArm();
 			} else {
 				player.sendMessage("cYou are cannot afford to repair your armour");
 				player.sendMessage("cNeed: " + Misc.formatCurrency(total, iConomy.currency));
 			}
 			//both icon and item cost
 		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
 			balance = iConomy.db.get_balance(player.getName());
 			for (ItemStack i : inven.getArmorContents()) {				
 				if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
 					total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
 				}				
 			}						
 			if (total <= balance && isEnoughItems(req)) {
 				balance = iConomy.db.get_balance(player.getName());
 				iConomy.db.set_balance(player.getName(), balance - total);
 				deduct(req);
 				player.sendMessage("3Using " + Misc.formatCurrency(total, iConomy.currency) + " and");
 				player.sendMessage("3" + printFormatReqs(req) + " to repair your armour");
 				repArm();
 			} else {
 				player.sendMessage("cYou are missing one or more items to repair your armour");
 				player.sendMessage("cNeed: " + printFormatReqs(req) + " and " +
 						Misc.formatCurrency(total, iConomy.currency));
 			}			
 			// just item cost
 		} else {
 			if (isEnoughItems(req)) {
 				deduct(req);
 				player.sendMessage("3Using " + printFormatReqs(req) + " to repair your armour");
 				repArm();
 			} else {
 				player.sendMessage("cYou are missing one or more items to repair your armour");
 				player.sendMessage("cNeed: " + printFormatReqs(req));
 			}
 		}
 	}
 
 	public void repArm () {
 		PlayerInventory inven = player.getInventory();
 		if(inven.getBoots().getTypeId() != 0 ) {inven.setBoots(repItem(inven.getBoots()));}
 		if(inven.getChestplate().getTypeId() != 0 ) {inven.setChestplate(repItem(inven.getChestplate()));}
 		if(inven.getHelmet().getTypeId() != 0 ) {inven.setHelmet(repItem(inven.getHelmet()));}
 		if(inven.getLeggings().getTypeId() != 0 ) {inven.setLeggings(repItem(inven.getLeggings()));}
 	}
 
 	public void deduct(ArrayList<ItemStack> req) {
 		PlayerInventory inven = player.getInventory();
 		for (int i =0; i < req.size(); i++) {
 			ItemStack currItem = new ItemStack(req.get(i).getTypeId(), req.get(i).getAmount());
 			int neededAmount = req.get(i).getAmount();
 			int smallestSlot = findSmallest(currItem);
 			if (smallestSlot != -1) {
 				while (neededAmount > 0) {									
 					smallestSlot = findSmallest(currItem);
 					ItemStack smallestItem = inven.getItem(smallestSlot);
 					if (neededAmount < smallestItem.getAmount()) {
 						// got enough in smallest stack deal and done
 						ItemStack newSize = new ItemStack(currItem.getType(), smallestItem.getAmount() - neededAmount);
 						inven.setItem(smallestSlot, newSize);
 						neededAmount = 0;										
 					} else {
 						// need to remove from more than one stack, deal and continue
 						neededAmount -= smallestItem.getAmount();
 						inven.clear(smallestSlot);
 					}
 				}
 			}
 		}
 	}
 
 }
