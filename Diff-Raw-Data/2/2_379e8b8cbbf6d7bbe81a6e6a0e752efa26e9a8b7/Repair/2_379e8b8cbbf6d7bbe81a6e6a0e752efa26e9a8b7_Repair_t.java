 package com.mrockey28.bukkit.ItemRepair;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.mrockey28.bukkit.ItemRepair.AutoRepairPlugin.operationType;
 
 
 public class Repair extends AutoRepairSupport{
 
 	public static final Logger log = Logger.getLogger("Minecraft");
 
 	public Repair(AutoRepairPlugin instance) {
 		super(instance, getPlayer());
 	}
 	
 	public boolean manualRepair(ItemStack tool) {
 		doRepairOperation(tool, operationType.MANUAL_REPAIR);
 		return false;		
 	}
 
 	public boolean autoRepairTool(ItemStack tool) {
 		
 		doRepairOperation(tool, operationType.AUTO_REPAIR);
 		return false;
 	}
 	
 	public void repairAll(Player player) {		
 		
 		ArrayList<ItemStack> couldNotRepair = new ArrayList<ItemStack> (0);
 		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
 		
 		for (ItemStack item : player.getInventory().getContents())
 		{
			if (item == null || item.getType() == Material.AIR || item.getType() == Material.WOOL)
 			{
 				continue;
 			}
 			
 			if (item.getDurability() != 0)
 			{
 				doRepairOperation(item, operationType.FULL_REPAIR);
 				if (item.getDurability() != 0 && durabilities.containsKey(item.getType().toString()))
 				{
 					couldNotRepair.add(item);
 				}
 			}
 		}
 		for (ItemStack item : player.getInventory().getArmorContents())
 		{
 			if (item == null || item.getType() == Material.AIR)
 			{
 				continue;
 			}
 			
 			if (item.getDurability() != 0)
 			{
 				doRepairOperation(item, operationType.FULL_REPAIR);
 				if (item.getDurability() != 0 && durabilities.containsKey(item.getType().toString()))
 				{
 					couldNotRepair.add(item);
 				}
 			}
 		}
 		
 		if (!couldNotRepair.isEmpty())
 		{
 			String itemsNotRepaired = "";
 			
 			for (ItemStack item : couldNotRepair)
 			{
 				itemsNotRepaired += (item.getType().toString() + ", ");
 			}
 			itemsNotRepaired = itemsNotRepaired.substring(0, itemsNotRepaired.length() - 2);
 			player.sendMessage("cDid not repair the following items: ");
 			player.sendMessage("c" + itemsNotRepaired);
 		}
 		
 	}
 	
 	public void repairArmor(Player player) {		
 		
 		ArrayList<ItemStack> couldNotRepair = new ArrayList<ItemStack> (0);
 		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
 		for (ItemStack item : player.getInventory().getArmorContents())
 		{
 			if (item == null || item.getType() == Material.AIR)
 			{
 				continue;
 			}
 			
 			if (item.getDurability() != 0)
 			{
 				doRepairOperation(item, operationType.FULL_REPAIR);
 				if (item.getDurability() != 0 && durabilities.containsKey(item.getType().toString()))
 				{
 					couldNotRepair.add(item);
 				}
 			}
 		}
 		
 		if (!couldNotRepair.isEmpty())
 		{
 			String itemsNotRepaired = "";
 			
 			for (ItemStack item : couldNotRepair)
 			{
 				itemsNotRepaired += (item.getType().toString() + ", ");
 			}
 			itemsNotRepaired = itemsNotRepaired.substring(0, itemsNotRepaired.length() - 2);
 			player.sendMessage("cDid not repair the following items: ");
 			player.sendMessage("c" + itemsNotRepaired);
 		}	
 	}
 }
 
