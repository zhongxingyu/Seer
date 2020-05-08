 package com.sparkedia.valrix.AutoReplace;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class AutoBlockListener extends BlockListener {
 	public static AutoReplace plugin;
 	public AutoBlockListener(AutoReplace instance) {
 		plugin = instance;
 	}
 	
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		PlayerInventory inv = player.getInventory();
 		Material tool = Material.getMaterial(inv.getItemInHand().getTypeId());
 		// If the tool has broken
 		if (inv.getItemInHand().getDurability() == tool.getMaxDurability()) {
 			//check to see if what we're using matched the registered object
 			int slot = inv.getHeldItemSlot();
 			String key = "slot"+slot;
 			int ID = inv.getItemInHand().getTypeId();
 			if ((AutoReplace.preset.keyExists(key)) && (AutoReplace.preset.getInt(key) == ID)) {
 				//replace it, otherwise they're out, message them
 				ItemStack[] items = inv.getContents();
 				boolean found = false;
 				for (int i = (slot+1); i < items.length; i++) {
 					ItemStack obj = items[i];
 					//for each item in inventory, check if it's what we want, then we can break
 					if ((obj != null) && (obj.getTypeId() == ID) && (obj.getAmount() > 0)) {
						//we found what we need, now replace in-hand tool durability with found and clear found tool
						inv.getItemInHand().setDurability((short) (obj.getDurability()-1));
						inv.clear(i);
 						found = true;
 						break;
 					}
 				}
 				if (!found) {
 					String str = tool.name().toString();
 					player.sendMessage("You have no more "+str+" in your inventory.");
 				}
 			}
 		}
 	}
 	
 	public void onBlockPlace(BlockPlaceEvent event) {
 		// Until the method to update inventory is added, this can't be used
 		Player player = event.getPlayer();
 		Block block = event.getBlockPlaced();
 		PlayerInventory inv = player.getInventory();
 		int count = (player.getItemInHand().getAmount()-1);
 		int slot = inv.getHeldItemSlot();
 		if (count < 1) {
 			String key = "slot"+slot;
 			if (AutoReplace.preset.keyExists(key)) {
 				if (block.getTypeId() == AutoReplace.preset.getInt(key)) {
 					//replace it, otherwise they're out, message them
 					ItemStack[] items = inv.getContents();
 					boolean found = false;
 					for (int i = (slot+1); i < items.length; i++) {
 						ItemStack obj = items[i];
 						//for each item in inventory, check if it's what we want, then we can break
 						if ((obj != null) && (obj.getTypeId() == block.getTypeId()) && (obj.getAmount() > 0)) {
							//we found what we need, now change in-hand stack count to found and clear found item
							inv.getItemInHand().setAmount(obj.getAmount()+1);
							inv.clear(i);
 							found = true;
 							break;
 						}
 					}
 					if (!found) {
 						String str = block.getType().toString();
 						player.sendMessage("You have no more "+str+" in your inventory.");
 					}
 				}
 			}
 		}
 	}
 }
