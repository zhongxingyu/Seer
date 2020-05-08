 package com.nidefawl.Stats;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 import org.getspout.spoutapi.event.inventory.InventoryListener;
 
 public class StatsCraftListener extends InventoryListener{
 	
 	private Stats plugin;
 	
 	public StatsCraftListener(Stats plugin){
 		this.plugin = plugin;
 	}
 	
 	public void onInventoryCraft(InventoryCraftEvent event){
 		if (event.isCancelled())
 			return;
 		if (!(event.getPlayer() instanceof Player))
 			return;
 		ItemStack blockKey = event.getResult();
		int blockID = blockKey.getTypeId();
		MaterialData blockByte = blockKey.getData();
		int blockParam = blockByte.getData();
		String blockName = (new Integer(blockID)).toString();
		int amount = blockKey.getAmount();
		plugin.updateStat(event.getPlayer(), "crafting", "" + blockName + ":" + blockParam, amount, true);
		plugin.updateStat(event.getPlayer(), "crafting", "total", 1, true);
		plugin.updateStat(event.getPlayer(), "crafting", "total_sum", amount, true);
 	}
 }
