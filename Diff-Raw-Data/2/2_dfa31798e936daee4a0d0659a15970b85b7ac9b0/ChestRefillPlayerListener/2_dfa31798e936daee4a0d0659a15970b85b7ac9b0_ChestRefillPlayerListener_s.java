 package com.xhizors.ChestRefill;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ChestRefillPlayerListener extends PlayerListener{
 	
 	private ChestRefill instance;
 	
 	public ChestRefillPlayerListener(ChestRefill instance) {
 		this.instance = instance;
 	}
 	
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 		Interaction i = instance.checkInteraction(p);
 		if (i != null) {
 			if (i instanceof AddChestRefill) {
 				AddChestRefill com = (AddChestRefill) i;
 				Block b = event.getClickedBlock();
 				if (b.getTypeId() == 54
 						&& b.getRelative(BlockFace.EAST).getTypeId() != 54
 						&& b.getRelative(BlockFace.WEST).getTypeId() != 54
 						&& b.getRelative(BlockFace.NORTH).getTypeId() != 54
 						&& b.getRelative(BlockFace.SOUTH).getTypeId() != 54) {
 					ItemStack[] items = ((Chest)b.getState()).getInventory().getContents();
 					ChestData c = new ChestData();
 					c.setDelay(com.getDelay());
 					for (int k = 0; k < items.length; k++) {
 						if (items[k] != null)
 							c.addItem(k, items[k].getTypeId(), items[k].getAmount(), items[k].getDurability());
 					}
 					instance.getChestMap().put(instance.findRepeatKey(new BlockData(b.getWorld().getName(), b.getX(), b.getY(), b.getZ())), c);
 					instance.saveFile();
 					p.sendMessage("Chest refill created.");
 				} else {
 					p.sendMessage("Not chest.");
 				}
 			}
 		}
 		
		if (event.getClickedBlock().getTypeId() == 54) {
 			Block b = event.getClickedBlock();
 			ChestData chest = instance.getChestMap().get(instance.findRepeatKey(new BlockData(b.getWorld().getName(), b.getX(), b.getY(), b.getZ())));
 			if (chest != null && chest.canRefill()) {
 				Inventory inv = ((Chest)b.getState()).getInventory();
 				inv.clear();
 				for (Integer index : chest.getMap().keySet()) {
 					inv.setItem(index.intValue(), chest.getMap().get(index));
 				}
 				chest.nextRefill();
 				instance.saveFile();
 				p.sendMessage("Chest refilled!");
 			}
 		}
 	}
 }
