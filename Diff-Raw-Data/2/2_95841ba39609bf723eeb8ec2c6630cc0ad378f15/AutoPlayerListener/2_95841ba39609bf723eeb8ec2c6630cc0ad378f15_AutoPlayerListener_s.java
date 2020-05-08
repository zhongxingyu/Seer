 package com.sparkedia.valrix.AutoReplace;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class AutoPlayerListener extends PlayerListener {
 	public AutoReplace plugin;
 	
 	public AutoPlayerListener(AutoReplace plugin) {
 		this.plugin = plugin;
 	}
 	
 	public boolean legal(int ID) {
 		switch (ID) {
 		case 55:
 			return true;
 		case 63:
 			return true;
 		case 295:
 			return true;
 		case 321:
 			return true;
 		case 323:
 			return true;
 		case 324:
 			return true;
 		case 330:
 			return true;
 		case 331:
 			return true;
 		case 338:
 			return true;
 		case 355:
 			return true;
 		case 356:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	public boolean food(int ID) {
 		switch (ID) {
 		case 260:
 			return true;
 		case 282:
 			return true;
 		case 297:
 			return true;
 		case 319:
 			return true;
 		case 320:
 			return true;
 		case 322:
 			return true;
 		case 349:
 			return true;
 		case 350:
 			return true;
 		case 357:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		if (e.getAction().toString().equals("RIGHT_CLICK_AIR") && food(e.getMaterial().getId())) {
 			Player player = e.getPlayer();
 			PlayerInventory inv = player.getInventory();
 			ItemStack item = e.getItem();
 			int slot = inv.getHeldItemSlot();
 			ItemStack[] items = inv.getContents();
 			for (int i = 0; i < items.length; i++) {
 				if (i != slot) {
 					ItemStack obj = items[i];
 					if ((obj != null) && (obj.getTypeId() == item.getTypeId()) && (obj.getAmount() > 0)) {
 						inv.getItemInHand().setAmount(obj.getAmount()+1);
 						inv.clear(i);
 						player.updateInventory();
 						break;
 					}
 				}
 			}
 		} else if (e.getAction().toString().equalsIgnoreCase("RIGHT_CLICK_BLOCK") && e.hasBlock() && e.hasItem() && !e.isBlockInHand() && legal(e.getItem().getTypeId())) {
 			Player player = e.getPlayer();
 			PlayerInventory inv = player.getInventory();
 			ItemStack item = e.getItem();
 			int count = (player.getItemInHand().getAmount()-1);
 			int slot = inv.getHeldItemSlot();
 			if (count < 1) {
 				ItemStack[] items = inv.getContents();
 				for (int i = 0; i < items.length; i++) {
 					if (i != slot) {
 						ItemStack obj = items[i];
 						if ((obj != null) && (obj.getTypeId() == item.getTypeId()) && (obj.getAmount() > 0)) {
 							inv.getItemInHand().setAmount(obj.getAmount()+1);
 							inv.clear(i);
 							player.updateInventory();
 							break;
 						}
 					}
 				}
 			}
		} else if (e.getMaterial().getId() != 325 && e.getAction().toString().equalsIgnoreCase("RIGHT_CLICK_BLOCK")) {
 			Player player = e.getPlayer();
 			PlayerInventory inv = player.getInventory();
 			int slot = inv.getHeldItemSlot();
 			ItemStack[] items = inv.getContents();
 			for (int i = 0; i < items.length; i++) {
 				if (i != slot) {
 					ItemStack obj = items[i];
 					if ((obj != null) && (obj.getTypeId() == 326)) {
 						e.setCancelled(true);
 						ItemStack b = new ItemStack(325);
 						b.setAmount(1);
 						inv.setItem(i, b);
 						player.updateInventory();
 						e.getClickedBlock().getRelative(e.getBlockFace()).setType(Material.WATER);
 						break;
 					}
 				}
 			}
 		}
 	}
 }
