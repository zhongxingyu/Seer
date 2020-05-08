 /*
  *  Copyright 2011 Zach Hinchy.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package us.hinchy.SuperCauldrons;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class SuperCauldronsListener implements Listener {
 	
 	public SuperCauldrons plugin;
 	 
 	public SuperCauldronsListener(SuperCauldrons instance) {
 	    plugin = instance;
 	}
 	
 	@EventHandler
 	public void onBlockFromTo(BlockFromToEvent event) {
 		if (plugin.getConfig().getBoolean("supercauldrons.flow-fill") == true) {
 			if ((event.getBlock().getType() == Material.WATER) || (event.getBlock().getType() == Material.STATIONARY_WATER)) {
 	
 				Block targetBlock = event.getBlock().getRelative(BlockFace.DOWN);
 				
 				if (targetBlock.getType() == Material.CAULDRON) {
 					
 					boolean cauldronUnpowered = true;
 					if (plugin.getConfig().getBoolean("supercauldrons.redstone") == true) {
 						if ((targetBlock.getRelative(BlockFace.NORTH).getType() == Material.REDSTONE_WIRE) && ((int)targetBlock.getRelative(BlockFace.NORTH).getData() > 0)) cauldronUnpowered = false;
 						if ((targetBlock.getRelative(BlockFace.SOUTH).getType() == Material.REDSTONE_WIRE) && ((int)targetBlock.getRelative(BlockFace.SOUTH).getData() > 0)) cauldronUnpowered = false;
 						if ((targetBlock.getRelative(BlockFace.WEST).getType() == Material.REDSTONE_WIRE) && ((int)targetBlock.getRelative(BlockFace.WEST).getData() > 0)) cauldronUnpowered = false;
 						if ((targetBlock.getRelative(BlockFace.EAST).getType() == Material.REDSTONE_WIRE) && ((int)targetBlock.getRelative(BlockFace.EAST).getData() > 0)) cauldronUnpowered = false;
 						//if (targetBlock.getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_TORCH_ON) cauldronUnpowered = false;
 						//if ((redstoneA.getBlock().getType() == Material.REDSTONE_WIRE) && ((int)redstoneA.getBlock().getData() > 0)) cauldronUnpowered = false;
 						//if ((redstoneB.getBlock().getType() == Material.REDSTONE_WIRE) && ((int)redstoneB.getBlock().getData() > 0)) cauldronUnpowered = false;
 						//if ((redstoneC.getBlock().getType() == Material.REDSTONE_WIRE) && ((int)redstoneC.getBlock().getData() > 0)) cauldronUnpowered = false;
 						//if ((redstoneD.getBlock().getType() == Material.REDSTONE_WIRE) && ((int)redstoneD.getBlock().getData() > 0)) cauldronUnpowered = false;
 					}
 					
 					if (cauldronUnpowered) targetBlock.setData((byte)3);
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
 		if (plugin.getConfig().getBoolean("supercauldrons.redstone") == true) {
 			
 			if (event.getBlock().getRelative(BlockFace.NORTH).getType() == Material.CAULDRON) {
 				if (((int)event.getBlock().getData() == 0) &&
 						((int)event.getBlock().getRelative(BlockFace.NORTH).getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType() == Material.AIR)) {
 							event.getBlock().getRelative(BlockFace.NORTH).setData((byte)0);
 							event.getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).setType(Material.WATER);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType() == Material.WATER) &&
 						((int)event.getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getData() == 0)) {
 							event.getBlock().getRelative(BlockFace.NORTH).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType() == Material.STATIONARY_WATER)) {
 							event.getBlock().getRelative(BlockFace.NORTH).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 			}
 			
 			if (event.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.CAULDRON) {
 				if (((int)event.getBlock().getData() == 0) &&
 						((int)event.getBlock().getRelative(BlockFace.SOUTH).getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType() == Material.AIR)) {
 							event.getBlock().getRelative(BlockFace.SOUTH).setData((byte)0);
 							event.getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).setType(Material.WATER);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType() == Material.WATER) &&
 						((int)event.getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getData() == 0)) {
 							event.getBlock().getRelative(BlockFace.SOUTH).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType() == Material.STATIONARY_WATER)) {
 							event.getBlock().getRelative(BlockFace.SOUTH).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 			}
 			
 			if (event.getBlock().getRelative(BlockFace.WEST).getType() == Material.CAULDRON) {
 				if (((int)event.getBlock().getData() == 0) &&
 						((int)event.getBlock().getRelative(BlockFace.WEST).getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType() == Material.AIR)) {
 							event.getBlock().getRelative(BlockFace.WEST).setData((byte)0);
 							event.getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP).setType(Material.WATER);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType() == Material.WATER) &&
 						((int)event.getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getData() == 0)) {
 							event.getBlock().getRelative(BlockFace.WEST).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType() == Material.STATIONARY_WATER)) {
 							event.getBlock().getRelative(BlockFace.WEST).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 			}
 			
 			if (event.getBlock().getRelative(BlockFace.EAST).getType() == Material.CAULDRON) {
 				if (((int)event.getBlock().getData() == 0) &&
 						((int)event.getBlock().getRelative(BlockFace.EAST).getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType() == Material.AIR)) {
 							event.getBlock().getRelative(BlockFace.EAST).setData((byte)0);
 							event.getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP).setType(Material.WATER);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType() == Material.WATER) &&
 						((int)event.getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getData() == 0)) {
 							event.getBlock().getRelative(BlockFace.EAST).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 				else if (((int)event.getBlock().getData() > 0) &&
 						(event.getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType() == Material.STATIONARY_WATER)) {
 							event.getBlock().getRelative(BlockFace.EAST).setData((byte)3);
 							event.getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP).setType(Material.AIR);
 						}
 			}
 			
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
 		if (event.getClickedBlock().getType() == Material.CAULDRON) {
 
 			int metadata = (int)event.getClickedBlock().getData();
 
 			if (event.getPlayer().getItemInHand().getType() == Material.WATER_BUCKET) {
 				event.getPlayer().setItemInHand(new ItemStack(Material.BUCKET,1));
 				event.getClickedBlock().setData((byte)3);
 			} else {
 				if (metadata != 0) {
 					if (event.getPlayer().getItemInHand().getType() == Material.BUCKET) {
 						if ((plugin.getConfig().getBoolean("supercauldrons.infinite") == true) || (metadata > 2)) {
							if (event.getPlayer().getInventory().firstEmpty() > -1) {
								if (event.getPlayer().getItemInHand().getAmount() > 1) {
									event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
									event.getPlayer().getInventory().addItem(new ItemStack(Material.WATER_BUCKET,1,(byte)0));
								} else event.getPlayer().setItemInHand(new ItemStack(Material.WATER_BUCKET,1,(byte)0));
								if (plugin.getConfig().getBoolean("supercauldrons.infinite") == false) event.getClickedBlock().setData((byte)(metadata-1));
							}
 						}
 						if ((plugin.getConfig().getBoolean("supercauldrons.infinite") == false) && (metadata > 2)) {
 							event.getClickedBlock().setData((byte)0);
 							event.setCancelled(true);
 						}
 					} else if (event.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE) {
 						event.setCancelled(true);
 						if (event.getPlayer().getInventory().firstEmpty() > -1) {
 							if (event.getPlayer().getItemInHand().getAmount() > 1) {
 								event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
 								event.getPlayer().getInventory().addItem(new ItemStack(Material.POTION,1,(byte)0));
 							} else event.getPlayer().setItemInHand(new ItemStack(Material.POTION,1,(byte)0));
 							if (plugin.getConfig().getBoolean("supercauldrons.infinite") == false) event.getClickedBlock().setData((byte)(metadata-1));
 						}
 					}
 					event.getPlayer().updateInventory();
 	
 					if (plugin.getConfig().getBoolean("supercauldrons.infinite") == true) {
 						event.getClickedBlock().setData((byte)3);
 					}
 				}
 			}
 
 			// Don't do stuff with cauldron metadata for bottle operations in non-infinite mode;
 			// the game handles those perfectly well on its own.
 			if (((event.getPlayer().getItemInHand().getType() != Material.POTION)
 			&& (event.getPlayer().getItemInHand().getType() != Material.GLASS_BOTTLE))
 			|| (plugin.getConfig().getBoolean("supercauldrons.infinite") == true)) {
 				new SuperCauldronUpdate(plugin,event.getPlayer(),event.getClickedBlock());
 			}			
 		}
 	}
 	
 }
