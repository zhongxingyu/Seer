 package com.lostaris.bukkit.ItemRepair;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.nijikokun.bukkit.iConomy.Misc;
 import com.nijikokun.bukkit.iConomy.iConomy;
 
 /**
  * Supplementary methods for AutoRepair
  * @author lostaris
  */
 public class AutoRepairSupport {
 	private final AutoRepairPlugin plugin;
 	protected static Player player;
 
 	public AutoRepairSupport(AutoRepairPlugin instance, Player player) {
 		plugin = instance;
 		AutoRepairSupport.player = player;
 	}
 	// max durabilities for all tools 
 	private final int woodDurability = 59;
 	private final int goldDurability = 32;
 	private final int stoneDurability = 131;
 	private final int ironDurability = 250;
 	private final int diamondDurability = 1561;
 	private boolean warning = false;
 	private boolean lastWarning = false;
 	public static final Logger log = Logger.getLogger("Minecraft");
 
 	/**
 	 * Method to return to the player the required items and/or iConomy cash needed for a repair
 	 * @param tool - tool to return the repair requirements for
 	 */
 	public void toolReq(ItemStack tool) {
 		// if the player has permission to do this command
 		if (AutoRepairPlugin.isAllowed(player, "info")) {
 			String toolString = tool.getType().toString();
 			// just icon cost
 			if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0) {
 				if (AutoRepairPlugin.getiConCosts().containsKey(toolString)) {
 					player.sendMessage("6It costs " + Misc.formatCurrency(
 							AutoRepairPlugin.getiConCosts().get(toolString), iConomy.currency)
 							+ " to repair " + tool.getType());
 				}
 				//both icon cost and item cost
 			} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
 				if (AutoRepairPlugin.getRepairRecipies().containsKey(toolString) &&
 						AutoRepairPlugin.getiConCosts().containsKey(toolString)) {
 					player.sendMessage("6To repair " + tool.getType() + " you need: " +Misc.formatCurrency(
 							AutoRepairPlugin.getiConCosts().get(toolString), iConomy.currency) + " and");
 					player.sendMessage("6" + printFormatReqs(AutoRepairPlugin.getRepairRecipies().get(toolString)));
 				}
 				// just item cost
 			} else if (AutoRepairPlugin.isRepairCosts()) {
 				//tests to see if the config file has a repair reference to the item they wish to repair
 				if (AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
 					player.sendMessage("6To repair " + tool.getType() + " you need:");
 					player.sendMessage("6" + printFormatReqs(AutoRepairPlugin.getRepairRecipies().get(toolString)));
 				}
 			} else {
 				player.sendMessage("3No materials needed to repair");
 				//return true;
 			} 
 			if (!AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
 				player.sendMessage("6This is not a tool.");
 			}
 
 		} else {
 			player.sendMessage("cYou dont have permission to do the ? or dmg commands.");
 		}
 	}
 
 	/**
 	 * Method to warn a player their tool is close to breaking
 	 * If they do not have the required items and/or cash to repair warns them,
 	 * and lets them know they are missing required items and/or cash and prints what is needed
 	 * @param tool - tool to warn the player about
 	 * @param slot - slot this tool is in
 	 */
 	public void repairWarn(ItemStack tool, int slot) {
 		// if the player has permission to do this command
 		if (!AutoRepairPlugin.isAllowed(player, "warn")) { 
 			return;
 		}
 
 		HashMap<String, ArrayList<ItemStack>> repairRecipies;
 		// if they haven't already been warned
 		if (!warning) {					
 			warning = true;		
 			try {				
 				repairRecipies = AutoRepairPlugin.getRepairRecipies();
 				String toolString = tool.getType().toString();
 				//tests to see if the config file has a repair reference to the item they wish to repair
 				if (repairRecipies.containsKey(toolString)) {
 					// there is no repair costs and no auto repair
 					if (!AutoRepairPlugin.isRepairCosts() && !AutoRepairPlugin.isAutoRepair()) {
 						player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 						/* if there is repair costs  and no auto repair */
 					} else if (AutoRepairPlugin.isRepairCosts() && !AutoRepairPlugin.isAutoRepair()) {
 						// just iCon
 						if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
 							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon, no auto repairing");
 							iConWarn(toolString, cost);
 							// both iCon and item cost
 						} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
 							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
 							ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(toolString);
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon, no auto repairing");
 							bothWarn(toolString, cost, reqItems);
 							// just item cost
 						} else {
 							ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(toolString);
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon, no auto repairing");
 							justItemsWarn(toolString, reqItems);
 						}
 						/* there is auto repairing and repair costs */
 					} else {
 						int balance;
 						// just iCon
 						if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
 							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
 							balance = iConomy.db.get_balance(player.getName());
 							if (cost > balance) {
 								player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 								iConWarn(toolString, cost);
 							}
 							// both iCon and item cost
 						} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
 							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
 							ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(toolString);
 							balance = iConomy.db.get_balance(player.getName());
 							if (cost > balance || !isEnoughItems(reqItems)) {
 								player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 								bothWarn(toolString, cost, reqItems);
 							}
 							// just item cost
 						} else {
 							ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(toolString);
 							if (!isEnoughItems(reqItems)) {								
 								player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 								justItemsWarn(toolString, reqItems);
 							}
 						}
 					}
 				} else {
 					// item does not have a repair reference in config
 					player.sendMessage("6" +toolString + " not found in config file.");
 				}
 			} catch (Exception e) {
 				log.info("Error in AutoRepair config.properties file syntax");
 			}
 		}
 	}
 
 	/**
 	 * Method to return the total cost of repair a players worn armour
 	 * @param query
 	 * @return true if all is well, false if the command is miss typed
 	 */
 	public boolean repArmourInfo(String query) {
 		// if there is repair costs
 		if (AutoRepairPlugin.isRepairCosts()) {
 			try {
 				char getRecipe = query.charAt(0);
 				// if the command is ? - the correct one
 				if (getRecipe == '?') {
 					int total =0;
 					ArrayList<ItemStack> req = repArmourAmount();
 					PlayerInventory inven = player.getInventory();
 					// just iCon costs
 					if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
 						for (ItemStack i : inven.getArmorContents()) {				
 							if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
 								total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
 							}				
 						}
 						player.sendMessage("6To repair all your armour you need: "
 								+ Misc.formatCurrency(total, iConomy.currency));						
 						//both icon and item cost
 					} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
 						for (ItemStack i : inven.getArmorContents()) {				
 							if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
 								total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
 							}				
 						}						
 						player.sendMessage("6To repair all your armour you need: "
 								+ Misc.formatCurrency(total, iConomy.currency));
 						player.sendMessage("6" + this.printFormatReqs(req));		
 						// just item cost
 					} else {
 						player.sendMessage("6To repair all your armour you need:");
 						player.sendMessage("6" + this.printFormatReqs(req));
 					}
 				}
 			} catch (Exception e) {
 				return false;
 			}
 		} else {
 			player.sendMessage("3No materials needed to repair");
 		}
 		return true;
 	}
 
 	/**
 	 * Method to return the total item cost of repairing a players warn armour
 	 * @return req - total item costs of repaing a players warn armour
 	 */
 	public ArrayList<ItemStack> repArmourAmount() {
 		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
 		PlayerInventory inven = player.getInventory();
 		ItemStack[] armour = inven.getArmorContents();
 		// list of all the items needed to repair all warn armour
 		HashMap<String, Integer> totalCost = new HashMap<String, Integer>();
 		// for the players 4 armour slots
 		for (int i=0; i<armour.length; i++) {
 			String item = armour[i].getType().toString();
 			if (recipies.containsKey(item)) {
 				ArrayList<ItemStack> reqItems = recipies.get(item);
 				// get this armour piece's costs
 				for (int j =0; j<reqItems.size(); j++) {
 					// if we already have this cost, add to it
 					if(totalCost.containsKey(reqItems.get(j).getType().toString())) {
 						int amount = totalCost.get(reqItems.get(j).getType().toString());
 						totalCost.remove(reqItems.get(j).getType().toString());
 						int newAmount = amount + reqItems.get(j).getAmount();
 						totalCost.put(reqItems.get(j).getType().toString(), newAmount);
 						// otherwise add it to the list
 					} else {
 						totalCost.put(reqItems.get(j).getType().toString(), reqItems.get(j).getAmount());
 					}
 				}
 			}
 		}
 		// turn it back into a ItemStack array
 		ArrayList<ItemStack> req = new ArrayList<ItemStack>();
 		for (Object key: totalCost.keySet()) {
 			req.add(new ItemStack(Material.getMaterial(key.toString()), totalCost.get(key)));
 		}
 		return req;
 	}
 
 	// sets the durability of a item back to no damage
 	public ItemStack repItem(ItemStack item) {
 		item.setDurability((short) 0);
 		return item;
 	}
 
 	//prints the durability left of the current tool to the player
 	public void durabilityLeft(ItemStack tool) {
 		if (AutoRepairPlugin.isAllowed(player, "info")) { //!AutoRepairPlugin.isPermissions || AutoRepairPlugin.Permissions.has(player, "AutoRepair.info")) {
 			int usesLeft = this.returnUsesLeft(tool);
 			if (usesLeft != -1) {
 				player.sendMessage("3" + usesLeft + " blocks left untill this tool breaks." );
 			} else {
 				player.sendMessage("6This is not a tool.");
 			}
 		} else {
 			player.sendMessage("cYou dont have permission to do the ? or dmg commands.");
 		}
 
 	}
 
 	/**
 	 * Method to return the number of uses left in this tool
 	 * @param tool - tool to return uses left for
 	 * @return uses left of this tool
 	 */
 	public int returnUsesLeft(ItemStack tool) {
 		int usesLeft = -1;
 		if (tool.getType() == Material.WOOD_SPADE || tool.getType() == Material.WOOD_PICKAXE || 
 				tool.getType() == Material.WOOD_AXE || tool.getType() == Material.WOOD_SWORD ||
 				tool.getType() == Material.WOOD_HOE) {
 			usesLeft = woodDurability - tool.getDurability();
 		}
 		if (tool.getType() == Material.GOLD_SPADE || tool.getType() == Material.GOLD_PICKAXE || 
 				tool.getType() == Material.GOLD_AXE || tool.getType() == Material.GOLD_SWORD ||
 				tool.getType() == Material.GOLD_HOE) {
 			usesLeft = goldDurability - tool.getDurability();
 		}
 		if (tool.getType() == Material.STONE_SPADE || tool.getType() == Material.STONE_PICKAXE || 
 				tool.getType() == Material.STONE_AXE || tool.getType() == Material.STONE_SWORD ||
 				tool.getType() == Material.STONE_HOE) {
 			usesLeft = stoneDurability - tool.getDurability();
 		}
 		if (tool.getType() == Material.IRON_SPADE || tool.getType() == Material.IRON_PICKAXE || 
 				tool.getType() == Material.IRON_AXE || tool.getType() == Material.IRON_SWORD ||
 				tool.getType() == Material.IRON_HOE) {
 			usesLeft = ironDurability - tool.getDurability();
 
 		}
 		if (tool.getType() == Material.DIAMOND_SPADE || tool.getType() == Material.DIAMOND_PICKAXE || 
 				tool.getType() == Material.DIAMOND_AXE || tool.getType() == Material.DIAMOND_SWORD ||
 				tool.getType() == Material.DIAMOND_HOE) {
 			usesLeft = diamondDurability - tool.getDurability();
 		}
 		return usesLeft;
 	}
 
 	/**
 	 * Finds the smallest stack of an item in a players inventory
 	 * @param item - item to look for
 	 * @return slot the smallest stack is in
 	 */
 	@SuppressWarnings("unchecked")
 	public int findSmallest(ItemStack item) {
 		PlayerInventory inven = player.getInventory();
 		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
 		int slot = -1;
 		int smallest = 64;
 		//iterator for the hashmap
 		Set<?> set = items.entrySet();
 		Iterator<?> i = set.iterator();
 		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
 		while(i.hasNext()){
 			Map.Entry me = (Map.Entry)i.next();
 			ItemStack item1 = (ItemStack) me.getValue();
 			//if the player has doesn't not have enough of the item used to repair
 			if (item1.getAmount() <= smallest) {
 				smallest = item1.getAmount();
 				slot = (Integer)me.getKey();
 			}
 		}		
 		return slot;
 	}
 
 	/**
 	 * Method to return the total amount of an item a player has
 	 * @param item - item to look for
 	 * @return total number of this item the player has
 	 */
 	@SuppressWarnings("unchecked")
 	public int getTotalItems(ItemStack item) {
 		int total = 0;
 		PlayerInventory inven = player.getInventory();
 		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
 		//iterator for the hashmap
 		Set<?> set = items.entrySet();
 		Iterator<?> i = set.iterator();
 		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
 		while(i.hasNext()){
 			Map.Entry me = (Map.Entry)i.next();
 			ItemStack item1 = (ItemStack) me.getValue();
 			//if the player has doesn't not have enough of the item used to repair
 			total += item1.getAmount();					
 		}
 		return total;
 	}
 
 	// checks to see if the player has enough of an item
 	public boolean isEnough(String itemName) {
 		ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(itemName);
 		boolean enoughItemFlag = true;
 		for (int i =0; i < reqItems.size(); i++) {
 			ItemStack currItem = new ItemStack(reqItems.get(i).getTypeId(), reqItems.get(i).getAmount());
 
 			int neededAmount = reqItems.get(i).getAmount();
 			int currTotal = getTotalItems(currItem);
 			if (neededAmount > currTotal) {
 				enoughItemFlag = false;
 			}
 		}
 		return enoughItemFlag;
 	}
 
 	// checks to see if the player has enough of a list of items
 	public boolean isEnoughItems (ArrayList<ItemStack> req) {
 		boolean enough = true;
		if (req == null) {
			return false;
		}
 		for (int i =0; i<req.size(); i++) {
 			ItemStack currItem = new ItemStack(req.get(i).getTypeId(), req.get(i).getAmount());
 			int neededAmount = req.get(i).getAmount();
 			int currTotal = getTotalItems(currItem);
 			if (neededAmount > currTotal) {
 				enough = false;
 			}
 		}
 		return enough;
 	}
 
 	/*
 	 * Methods to print the warning for lacking items and/or iConomy money
 	 */
 	public void iConWarn(String itemName, int total) {
 		getPlayer().sendMessage("cYou cannot afford to repair "  + itemName);
 		getPlayer().sendMessage("cNeed: " + Misc.formatCurrency(total, iConomy.currency));
 	}
 
 	public void bothWarn(String itemName, int total, ArrayList<ItemStack> req) {
 		getPlayer().sendMessage("cYou are missing one or more items to repair " + itemName);
 		getPlayer().sendMessage("cNeed: " + printFormatReqs(req) + " and " +
 				Misc.formatCurrency(total, iConomy.currency));
 	}
 
 	public void justItemsWarn(String itemName, ArrayList<ItemStack> req) {
 		player.sendMessage("cYou are missing one or more items to repair " + itemName);
 		player.sendMessage("cNeed: " + printFormatReqs(req));
 	}
 
 	public String printFormatReqs(ArrayList<ItemStack> items) {
 		StringBuffer string = new StringBuffer();
 		string.append(" ");
 		for (int i = 0; i < items.size(); i++) {
 			string.append(items.get(i).getAmount() + " " + items.get(i).getType() + " ");
 		}
 		return string.toString();
 	}
 
 	public boolean getWarning() {
 		return warning;
 	}
 
 	public boolean getLastWarning() {
 		return lastWarning;
 	}
 
 	public void setWarning(boolean newValue) {
 		this.warning = newValue;
 	}
 
 	public void setLastWarning(boolean newValue) {
 		this.lastWarning = newValue;
 	}
 
 	public AutoRepairPlugin getPlugin() {
 		return plugin;
 	}
 
 	public static Player getPlayer() {
 		return player;
 	}
 	public void setPlayer(Player player) {
 		AutoRepairSupport.player = player;
 	}
 }
