 package com.mrockey28.bukkit.ItemRepair;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.mrockey28.bukkit.ItemRepair.AutoRepairPlugin.operationType;
 
 
 /**
  * 
  * @author lostaris, mrockey28
  */
 public class AutoRepairSupport {
 	private final AutoRepairPlugin plugin;
 	protected static Player player;
 
 	public AutoRepairSupport(AutoRepairPlugin instance, Player player) {
 		plugin = instance;
 		AutoRepairSupport.player = player;
 	}
 
 	private boolean warning = false;
 	private boolean lastWarning = false;
 
 	public float CalcPercentUsed(ItemStack tool, int durability)
 	{
 			float percentUsed = -1;
 			percentUsed = (float)tool.getDurability() / (float)durability;
 			return percentUsed;
 	}
 	
 	public boolean accountForRoundingType (int slot, ArrayList<ItemStack> req, String itemName)
 	{
 
 		return true;
 	}
 	
 	public void toolReq(ItemStack tool) {	
 		doRepairOperation(tool, operationType.QUERY);
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
 	
 	class costClass
 	{
 		double cost;
 		public costClass ()
 		{
 			cost = 0;
 		}
 		public costClass (double costInit)
 		{
 			cost = costInit;
 		}
 		
 	}
 	
 	public boolean getRepairCost (ItemStack tool, ArrayList<ItemStack> req, costClass cost, StringBuilder costType, boolean ignoreRounding) {
 		
 		String itemName = Material.getMaterial(tool.getTypeId()).toString();
 		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
 		HashMap<String, Integer> econCost = AutoRepairPlugin.getiConCosts();
 		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
 		
 		if (!recipies.containsKey(itemName))
 		{
 			if (costType.toString().equals("item_only") || costType.toString().equals("both")) return false;
 			else if (costType.toString().equals("config")) {
 				costType.replace(0, costType.length(), "econ_only");
 			}
 			
 		}
 		else
 		{
 			//do a deep copy of the required items list so we can modify it temporarily for rounding purposes
 			for (ItemStack i: recipies.get(itemName)) {
 			  req.add((ItemStack)i.clone());
 			}
 		}
 			
 		if (!econCost.containsKey(itemName))
 		{
 			if (costType.toString().equals("econ_only") || costType.toString().equals("both")) return false;
 			else if (costType.toString().equals("config")) {
 				costType.replace(0, costType.length(), "item_only");
 			}
 			
 		}
 		else
 		{
 			cost.cost = (double)AutoRepairPlugin.getiConCosts().get(itemName);
 		}
 		
 		//By this point, if we haven't set the costtype to something other than "config" or returned,
 		//we should set it to "Both".
 		if (costType.toString().equals("config"))
 		{
 			costType.replace(0, costType.length(), "both");
 		}
 		
 		int durability = durabilities.get(itemName);
 		
 		//do rounding based on dmg already done to item, if called for by config
 		if (ignoreRounding == false && (AutoRepairPlugin.item_rounding != "flat" || AutoRepairPlugin.econ_fractioning != "off"))
 		{
 			
 			float percentUsed = CalcPercentUsed(tool, durability);
 			for (int index = 0; index < req.size(); index++) {
 				float amnt = req.get(index).getAmount();
 				int amntInt;
 				
 				if (AutoRepairPlugin.item_rounding == "min" || AutoRepairPlugin.item_rounding == "round")
 				{
 					amnt = amnt * percentUsed;
 					amnt = Math.round(amnt);
 					amntInt = (int)amnt;
 					if (AutoRepairPlugin.item_rounding == "min")
 					{
 						if (amntInt == 0)
 						{
 							amntInt = 1;
 						}
 					}
 					req.get(index).setAmount(amntInt);
 				}
 					
 			}
 			//If they turned on economy cost and econ fractioning, round the cost to the correct amount
 			if (!costType.toString().equals("item_only") && AutoRepairPlugin.econ_fractioning == "on")
 			{
 				cost.cost = cost.cost * percentUsed;
 			}
 		}
 		return true;
 	}
 	
 	
 	public boolean isEnchanted(ItemStack tool)
 	{
 		return !tool.getEnchantments().isEmpty();
 	}
 	
 	public void setLocalCostType(StringBuilder costType) {
 	
 		if (AutoRepairPlugin.getUseEcon().equalsIgnoreCase("true")) {
 			costType.append("econ_only");
 		}
 		else if (AutoRepairPlugin.getUseEcon().equalsIgnoreCase("false")) {
 			costType.append("item_only");
 		}
 		else if (AutoRepairPlugin.getUseEcon().equalsIgnoreCase("both")) {
 			costType.append("both");
 		}
 		else if (AutoRepairPlugin.getUseEcon().equalsIgnoreCase("config")) {
 			costType.append("config");
 		}
 		else {
 			costType.append("item_only");
 		}
 	}
 	
 	public void doRepairOperation(ItemStack tool, AutoRepairPlugin.operationType op)
 	{
 		//If you don't have repair, warn, access, repair.enchanted permissions (whatever necessary to get in here)
 		//this won't let you in. If autorepair is turned off, and you're trying to do an auto-repair operation,
 		//this won't let you past
 		if (!AutoRepairPlugin.isOpAllowed(getPlayer(), op, isEnchanted(tool))) {
 			return;
 		}
 		
 		//handle warning flag stuff, to prevent log spam
 		if (op == operationType.WARN && !warning) warning = true;					
 		else if (op == operationType.WARN) return;
 		
 		//Prevent manual repairs on fully repaired tools
 		if (op == operationType.MANUAL_REPAIR && tool.getDurability() == 0)
 		{
 			player.sendMessage("3" + tool.getType().toString() + " is already fully repaired.");
 			return;
 		}
 		
 		StringBuilder costType = new StringBuilder();
 		//Handle switching the cost type, since it varies for each item we have to do it here so that we don't mess up the actual config setting
 		setLocalCostType(costType);
 		
 		costClass cost = new costClass();
 		ArrayList<ItemStack> req = new ArrayList<ItemStack> (0);
 		double balance = 0;
 		
 		//It only makes sense calculate adjusted cost if there IS a cost to repairing an item
 		if (AutoRepairPlugin.isRepairCosts()) {
 			if (op == operationType.AUTO_REPAIR || op == operationType.WARN) {
 				if (getRepairCost (tool, req, cost, costType, true) == false) return;
 			} else {
 				if (getRepairCost (tool, req, cost, costType, false) == false) {
 					if (op != operationType.FULL_REPAIR) player.sendMessage("6Can't that perform operation on this item.");
 					return;
 				}
 			}
			if (!costType.toString().equals("item_only")) {
 				balance = AutoRepairPlugin.econ.getBalance(player.getName());
 			}
 		}
 		
 		String itemName = Material.getMaterial(tool.getTypeId()).toString();
 		ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>(0);
 		
 		try {
 			//No repair costs
 			if (!AutoRepairPlugin.isRepairCosts()) {
 				switch (op)
 				{
 					case WARN:
 						if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 						}
 						break;
 					case AUTO_REPAIR:
 					case MANUAL_REPAIR:
 						getPlayer().sendMessage("3Repaired " + itemName);
 					case FULL_REPAIR:
 						repItem(tool);
 						break;
 					case QUERY:
 						getPlayer().sendMessage("3No materials needed to repair.");
 						break;
 						
 				}
 				
 			}
 			
 			//Using economy to pay only
 			else if (costType.toString().equals("econ_only"))
 			{
 				switch (op)
 				{	case QUERY:
 						player.sendMessage("6It costs " +  AutoRepairPlugin.econ.format(cost.cost)
 							+ " to repair " + tool.getType());
 						break;
 					case AUTO_REPAIR:
 					case MANUAL_REPAIR:
 					case FULL_REPAIR:
 						if (cost.cost <= balance) {
 							//balance = iConomy.db.get_balance(player.getName());
 							AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost.cost);
 							if (op != operationType.FULL_REPAIR) {
 								player.sendMessage("3Using " + AutoRepairPlugin.econ.format(cost.cost) + " to repair " + itemName);
 							}
 							//inven.setItem(slot, repItem(tool));
 							repItem(tool);
 						} else if (op != operationType.FULL_REPAIR){
 							if (op == operationType.MANUAL_REPAIR || !getLastWarning()) {
 								if (AutoRepairPlugin.isAllowed(player, "warn")) {
 									iConWarn(itemName, cost.cost);				
 								}
 								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
 							}
 						}
 						break;
 					case WARN:
 						if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
 						}
 						else if (cost.cost > balance) {
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 							iConWarn(itemName, cost.cost);
 						}	
 						break;
 				} 
 			} 
 			
 			//Using both economy and item costs to pay
 			else if (costType.toString().equals("both")) 
 			{	
 				switch (op)
 				{
 					case QUERY:
 						player.sendMessage("6To repair " + tool.getType() + " you need: " 
 								+ AutoRepairPlugin.econ.format(cost.cost) + " and");
 						player.sendMessage("6" + printFormatReqs(req));
 						break;
 					case AUTO_REPAIR:
 					case MANUAL_REPAIR:
 					case FULL_REPAIR:
 						if (cost.cost <= balance && isEnoughItems(req, neededItems)) {
 							//balance = iConomy.db.get_balance(player.getName());
 							AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost.cost);
 							deduct(req);
 							repItem(tool);
 							if (op != operationType.FULL_REPAIR) {
 								player.sendMessage("3Using " + AutoRepairPlugin.econ.format(cost.cost) + " and");
 								player.sendMessage("3" + printFormatReqs(req) + " to repair "  + itemName);
 							}
 							
 						} else if (op != operationType.FULL_REPAIR){
 							if (op == operationType.MANUAL_REPAIR || !getLastWarning()) {
 								if (AutoRepairPlugin.isAllowed(player, "warn")) {
 									if (cost.cost > balance && !isEnoughItems(req, neededItems)) bothWarn(itemName, cost.cost, neededItems);
 									else if (cost.cost > balance) iConWarn(itemName, cost.cost);
 									else justItemsWarn(itemName, neededItems);
 								}
 								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
 							}
 						}
 						break;
 					case WARN:
 							if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
 								player.sendMessage("6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
 							}
 							else if (cost.cost > balance || !isEnoughItems(req, neededItems)) {
 								player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 								if (cost.cost > balance && !isEnoughItems(req, neededItems)) bothWarn(itemName, cost.cost, neededItems);
 								else if (cost.cost > balance) iConWarn(itemName, cost.cost);
 								else justItemsWarn(itemName, neededItems);
 							}
 						break;
 				}
 			} 
 			
 			//Just using item costs to pay
 			else 
 			{
 				switch (op)
 				{
 					case QUERY:
 						player.sendMessage("6To repair " + tool.getType() + " you need:");
 						player.sendMessage("6" + printFormatReqs(req));
 						break;
 					case AUTO_REPAIR:
 					case MANUAL_REPAIR:
 					case FULL_REPAIR:
 						if (isEnoughItems(req, neededItems)) {
 							deduct(req);
 							repItem(tool);
 							if (op != operationType.FULL_REPAIR) {
 								player.sendMessage("3Using " + printFormatReqs(req) + " to repair " + itemName);
 							}
 						} else if (op != operationType.FULL_REPAIR){
 							if (op == operationType.MANUAL_REPAIR || !getLastWarning()) {
 								if (AutoRepairPlugin.isAllowed(player, "warn")) {
 									justItemsWarn(itemName, neededItems);					
 								}
 								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
 							}
 						}
 						break;
 					case WARN:
 						if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
 						}
 						else if (!isEnoughItems(req, neededItems)) {
 							player.sendMessage("6WARNING: " + tool.getType() + " will break soon");
 							justItemsWarn(itemName, neededItems);
 						}
 						break;
 				
 				}
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void repairWarn(ItemStack tool) {
 		doRepairOperation(tool, operationType.WARN);
 	}
 
 	public boolean wrapRepeatedGetRepairCostCalls (ItemStack armor, ArrayList<ItemStack> req, costClass cost)
 	{
 		costClass costTemp = new costClass();
 		ArrayList<ItemStack> reqTemp = new ArrayList<ItemStack> (0);
 		StringBuilder costType = new StringBuilder();
 		//Handle switching the cost type, since it varies for each item we have to do it here so that we don't mess up the actual config setting
 		setLocalCostType(costType);
 		
 		if (getRepairCost (armor, reqTemp, costTemp, costType, false) == false) return false;
 		
 		boolean replaced = false;
 		for (ItemStack i : reqTemp)
 		{
 			for (ItemStack j : req)
 			{
 				if (j.getType() == i.getType())
 				{
 					j.setAmount(j.getAmount() + i.getAmount());
 					replaced = true;
 					break;
 				}
 			}
 			if (replaced == false)
 			{
 				req.add(i.clone());
 			}
 		}
 		reqTemp.clear();
 		cost.cost += costTemp.cost;
 		return true;
 	}
 	
 	public boolean repArmourInfo(String query) {
 		
 		try {
 			char getRecipe = query.charAt(0);
 			if (getRecipe == '?') {
 				if (AutoRepairPlugin.isRepairCosts()) {
 					
 					ArrayList<ItemStack> req = new ArrayList<ItemStack> (0);
 					PlayerInventory inven = player.getInventory();
 					costClass cost = new costClass();
 					
 					for (ItemStack armor : inven.getArmorContents()) {
 						
 						 if (wrapRepeatedGetRepairCostCalls (armor, req, cost) == false)
 						 {
 							 continue;
 						 }
 					}
 					
 					//There were econ and item costs
 					if (!req.isEmpty() && cost.cost != 0) {					
 						player.sendMessage("6To repair all your armour you need: "
 								+ AutoRepairPlugin.econ.format(cost.cost));
 						player.sendMessage("6and " + this.printFormatReqs(req));		
 
 					} 
 					//There were only econ costs
 					else if (req.isEmpty()){
 						player.sendMessage("6To repair all your armour you need: "
 								+ AutoRepairPlugin.econ.format(cost.cost));					
 
 					}
 					//There were only item costs
 					else {
 						player.sendMessage("6To repair all your armour you need:");
 						player.sendMessage("6" + this.printFormatReqs(req));
 					}
 				} else {
 					player.sendMessage("3No materials needed to repair");
 				}
 			}
 				
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	public ItemStack repItem(ItemStack item) {
 		item.setDurability((short) 0);
 		return item;
 	}
 
 	//prints the durability left of the current tool to the player
 	public void durabilityLeft(ItemStack tool) {
 		if (AutoRepairPlugin.isAllowed(player, "info")) {
 			int usesLeft = this.returnUsesLeft(tool);
 			if (usesLeft != -1) {
 				player.sendMessage("3" + usesLeft + " uses left untill this tool breaks." );
 			} else {
 				player.sendMessage("6Can't get uses left for this item.");
 			}
 		} else {
 			player.sendMessage("cYou dont have permission to do the ? or dmg commands.");
 		}
 
 	}
 
 	public int returnUsesLeft(ItemStack tool) {
 		int usesLeft = -1;
 		int durability = 0;
 		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
 		String itemName = Material.getMaterial(tool.getTypeId()).toString();
 		
 		if (durabilities.containsKey(itemName))
 		{
 			durability = durabilities.get(itemName);
 			usesLeft = durability - tool.getDurability();
 		}
 		
 		return usesLeft;
 	}
 
 	@SuppressWarnings("rawtypes")
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
 
 	@SuppressWarnings("rawtypes")
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
 
 	// checks to see if the player has enough of a list of items
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
 
 	public boolean isEnoughItems (ArrayList<ItemStack> req, ArrayList<ItemStack> neededItems) {
 		boolean enough = true;
 		for (int i =0; i<req.size(); i++) {
 			ItemStack currItem = new ItemStack(req.get(i).getTypeId(), req.get(i).getAmount());
 			int neededAmount = req.get(i).getAmount();
 			int currTotal = getTotalItems(currItem);
 			if (neededAmount > currTotal) {
 				neededItems.add(req.get(i).clone());
 				enough = false;
 			}
 		}
 		return enough;
 	}
 
 	public void iConWarn(String itemName, double total) {
 		getPlayer().sendMessage("cYou cannot afford to repair "  + itemName);
 		getPlayer().sendMessage("cYou need: " + AutoRepairPlugin.econ.format((double)total));
 	}
 
 	public void bothWarn(String itemName, double total, ArrayList<ItemStack> req) {
 		getPlayer().sendMessage("cYou still need the following to be able to repair ");
 		getPlayer().sendMessage("cyour " + itemName + ": " + printFormatReqs(req) + " and " +
 				AutoRepairPlugin.econ.format((double)total));
 	}
 	
 	public void justItemsWarn(String itemName, ArrayList<ItemStack> req) {
 		player.sendMessage("cYou still need the following to be able to repair ");
 		player.sendMessage("cyour " + itemName + ": " + printFormatReqs(req));
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
 
