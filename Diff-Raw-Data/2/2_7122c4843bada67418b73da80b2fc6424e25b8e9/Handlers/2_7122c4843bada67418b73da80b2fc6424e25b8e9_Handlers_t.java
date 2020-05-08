 package net.worldoftomorrow.noitem.events;
 
 import net.minecraft.server.v1_4_6.RecipesFurnace;
 import net.minecraft.server.v1_4_6.TileEntityFurnace;
 import net.worldoftomorrow.noitem.NoItem;
 import net.worldoftomorrow.noitem.permissions.Perm;
 import net.worldoftomorrow.noitem.util.InvUtil;
 import net.worldoftomorrow.noitem.util.Messenger;
 import net.worldoftomorrow.noitem.util.Messenger.AlertType;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class Handlers {
 
 	// Begin - PlayerPickupItemEvent //
 	protected static void handleItemPickup(PlayerPickupItemEvent event) {
 		ItemStack item = event.getItem().getItemStack();
 		Player p = event.getPlayer();
 		if (NoItem.getPermsManager().has(p, Perm.PICKUP, item)) {
 			Messenger.sendMessage(p, AlertType.PICK_UP, item);
 			Messenger.alertAdmins(p, AlertType.PICK_UP, item);
 			event.getItem().setPickupDelay(200);
 			event.setCancelled(true);
 		}
 	}
 
 	protected static void handleNoHavePickup(PlayerPickupItemEvent event) {
 		ItemStack item = event.getItem().getItemStack();
 		Player p = event.getPlayer();
 		if (NoItem.getPermsManager().has(p, Perm.HAVE, item)) {
 			Messenger.sendMessage(p, AlertType.HAVE, item);
 			Messenger.alertAdmins(p, AlertType.HAVE, item);
 			event.getItem().setPickupDelay(200);
 			event.setCancelled(true);
 		}
 	}
 	
 	protected static void handleNoHoldPickup(PlayerPickupItemEvent event) {
 		ItemStack item = event.getItem().getItemStack();
		Player p = event.getPlayer();	
 		PlayerInventory inv = p.getInventory();
 		if(inv.firstEmpty() == inv.getHeldItemSlot() && NoItem.getPermsManager().has(p, Perm.HOLD, item)) {
 			Messenger.sendMessage(p, AlertType.HOLD, item);
 			Messenger.alertAdmins(p, AlertType.HOLD, item);
 			event.getItem().setPickupDelay(200);
 			event.setCancelled(true);
 		}
 	}
 
 	// End - PlayerPickupItemEvent //
 
 	// Begin - PlayerDropItemEvent //
 	protected static void handleItemDrop(PlayerDropItemEvent event) {
 		ItemStack drop = event.getItemDrop().getItemStack();
 		Player p = event.getPlayer();
 		if (NoItem.getPermsManager().has(p, Perm.DROP, drop)) {
 			Messenger.sendMessage(p, AlertType.DROP, drop);
 			Messenger.alertAdmins(p, AlertType.DROP, drop);
 			event.setCancelled(true);
 		}
 	}
 
 	// End - PlayerDropItemEvent //
 
 	// Begin - PlayerItemHeld //
 	protected static void handleItemHeld(PlayerItemHeldEvent event) {
 		Player p = event.getPlayer();
 		PlayerInventory inv = p.getInventory();
 		ItemStack item = inv.getItem(event.getNewSlot());
 		if (item != null && item.getTypeId() != 0 && NoItem.getPermsManager().has(p, Perm.HOLD, item)) {
 			InvUtil.switchItems(event.getNewSlot(), event.getPreviousSlot(), inv);
 			Messenger.sendMessage(p, AlertType.HOLD, item);
 			Messenger.alertAdmins(p, AlertType.HOLD, item);
 		}
 	}
 
 	protected static void handleNoHaveItemHeld(PlayerItemHeldEvent event) {
 		Player p = event.getPlayer();
 		PlayerInventory inv = p.getInventory();
 		ItemStack item = inv.getItem(event.getNewSlot());
 		if (item.getTypeId() != 0 && NoItem.getPermsManager().has(p, Perm.HAVE, item)) {
 			inv.remove(item);
 			Messenger.sendMessage(p, AlertType.HAVE, item);
 			Messenger.alertAdmins(p, AlertType.HAVE, item);
 		}
 	}
 
 	// End - PlayerItemHeld //
 
 	// Begin - Player'Interact/InteractEntity'Event //
 	protected static void handleInteract(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 		// If the event is NOT a block place event and was not in air
 		Block clicked = event.getClickedBlock();
 		if (!event.isBlockInHand() && clicked != null) {
 			if (NoItem.getPermsManager().has(p, Perm.INTERACT, clicked)) {
 				event.setCancelled(true);
 				Messenger.sendMessage(p, AlertType.INTERACT, clicked);
 				Messenger.alertAdmins(p, AlertType.INTERACT, clicked);
 			}
 		}
 	}
 	
 	protected static void handlerInteractLR(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 		Block clicked = event.getClickedBlock();
 		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
 			if(NoItem.getPermsManager().has(p, Perm.INTERACT_L, clicked)) {
 				event.setCancelled(true);
 				Messenger.sendMessage(p, AlertType.INTERACT, clicked);
 				Messenger.alertAdmins(p, AlertType.INTERACT, clicked);
 			}
 		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			if(NoItem.getPermsManager().has(p, Perm.INTERACT_R, clicked)) {
 				event.setCancelled(true);
 				Messenger.sendMessage(p, AlertType.INTERACT, clicked);
 				Messenger.alertAdmins(p, AlertType.INTERACT, clicked);
 			}
 		}
 	}
 
 	protected static void handleInteractEntity(PlayerInteractEntityEvent event) {
 		Player p = event.getPlayer();
 		Entity e = event.getRightClicked();
 		if (NoItem.getPermsManager().has(p, Perm.INTERACT, e)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.INTERACT, e);
 			Messenger.alertAdmins(p, AlertType.INTERACT, e);
 		}
 	}
 	// End - Player'Interact/InteractEntity'Event //
 	
 	// Begin - BlockBreakEvent //
 	protected static void handleBlockBreak(BlockBreakEvent event) {
 		Player p = event.getPlayer();
 		Block b = event.getBlock();
 		if(NoItem.getPermsManager().has(p, Perm.BREAK, b)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.BREAK, b);
 			Messenger.alertAdmins(p, AlertType.BREAK, b);
 		}
 	}
 	// End - BlockBreakEvent //
 	
 	// Begin - BlockPlaceEvent //
 	protected static void handleBlockPlace(BlockPlaceEvent event) {
 		Player p = event.getPlayer();
 		Block b = event.getBlock();
 		if(NoItem.getPermsManager().has(p, Perm.PLACE, b)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.PLACE, b);
 			Messenger.alertAdmins(p, AlertType.PLACE, b);
 		}
 	}
 	// End - BlockPlaceEvent //
 	
 	// Begin - InventoryClickEvent //
 	protected static void handleNoHoldInvClick(InventoryClickEvent event) {
 		ItemStack cursor = event.getCursor();
 		Player p = getPlayerFromEntity(event.getWhoClicked());
 		PlayerInventory inv = p.getInventory();
 		if(cursor.getTypeId() != 0
 				&& event.getSlotType() == SlotType.QUICKBAR
 				&& event.getSlot() == inv.getHeldItemSlot()) {
 			if(NoItem.getPermsManager().has(p, Perm.HOLD, cursor)) {
 				event.setCancelled(true);
 				Messenger.sendMessage(p, AlertType.HOLD, cursor);
 				Messenger.alertAdmins(p, AlertType.HOLD, cursor);
 			}
 		}
 	}
 	
 	protected static void handleNoBrewInvClick(InventoryClickEvent event) {
 		InventoryView view = event.getView();
 		if(view.getType() == InventoryType.BREWING) {
 			ItemStack cursor = event.getCursor();
 			Player p = getPlayerFromEntity(event.getWhoClicked());
 			int slot = event.getRawSlot();
 			ItemStack item;
 			String recipe;
 			// First handle the ingredient slot
 			if(slot == 3 && cursor.getTypeId() != 0) {
 				for(int i = 0; i > 3; i++) {
 					item = view.getItem(i);
 					if(item.getTypeId() != 0 && NoItem.getPermsManager().has(p, item.getDurability(), cursor)) {
 						event.setCancelled(true);
 						recipe = getRecipe(item.getDurability(), cursor);
 						Messenger.sendMessage(p, AlertType.BREW, recipe);
 						Messenger.alertAdmins(p, AlertType.BREW, recipe);
 					}
 				}
 			// Then handle any potion slot
 			} else if (slot < 3 && slot >= 0 && cursor.getTypeId() != 0) {
 				item = view.getItem(3);
 				if(item.getTypeId() != 0 && NoItem.getPermsManager().has(p, cursor.getDurability(), item)) {
 					event.setCancelled(true);
 					recipe = getRecipe(cursor.getDurability(), item);
 					Messenger.sendMessage(p, AlertType.BREW, recipe);
 					Messenger.alertAdmins(p, AlertType.BREW, recipe);
 				}
 			// Then handle an inventory shift click
 			} else if (slot > 3 && event.isShiftClick()) {
 				item = view.getItem(slot);
 				if(item.getTypeId() != 0) {
 					ItemStack ingredient = view.getItem(3);
 					// If the item is potion and the ingredient IS NOT empty
 					if(item.getTypeId() == 373 && ingredient.getTypeId() != 0) {
 						// Get the ingredient and check permissions
 						 if(NoItem.getPermsManager().has(p, item.getDurability(), ingredient)) {
 							event.setCancelled(true);
 							recipe = getRecipe(item.getDurability(), ingredient);
 							Messenger.sendMessage(p, AlertType.BREW, recipe);
 							Messenger.alertAdmins(p, AlertType.BREW, recipe);
 						 }
 					// If the item is anything else and ingredient slot IS empty
 					} else if(item.getTypeId() != 373 && ingredient.getTypeId() == 0) {
 						ItemStack potion;
 						for(int i = 0; i < 3; i++) {
 							potion = view.getItem(i);
 							if(potion.getTypeId() != 0 && NoItem.getPermsManager().has(p, potion.getDurability(), item)) {
 								event.setCancelled(true);
 								recipe = getRecipe(potion.getDurability(), item);
 								Messenger.sendMessage(p, AlertType.BREW, recipe);
 								Messenger.alertAdmins(p, AlertType.BREW, recipe);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	protected static void handleNoWearInvClick(InventoryClickEvent event) {
 		InventoryView view = event.getView();
 		SlotType st = event.getSlotType();
 		int slot = event.getRawSlot();
 		if(view.getType() == InventoryType.CRAFTING && slot != -999) {
 			ItemStack cursor = event.getCursor();
 			ItemStack current = event.getCurrentItem();
 			Player p = getPlayerFromEntity(event.getWhoClicked());
 			// Handle direct clicking
 			if(st == SlotType.ARMOR && cursor.getTypeId() != 0) {
 				if(NoItem.getLists().isArmor(cursor.getTypeId()) && NoItem.getPermsManager().has(p, Perm.WEAR, cursor)) {
 					event.setCancelled(true);
 					Messenger.sendMessage(p, AlertType.WEAR, cursor);
 					Messenger.alertAdmins(p, AlertType.WEAR, cursor);
 					return;
 				}
 			// Handle shift clicking
 			} else if (st != SlotType.ARMOR && current.getTypeId() != 0 && event.isShiftClick()) {
 				if(NoItem.getLists().isArmor(current.getTypeId()) && NoItem.getPermsManager().has(p, Perm.WEAR, current)) {
 					event.setCancelled(true);
 					Messenger.sendMessage(p, AlertType.WEAR, current);
 					Messenger.alertAdmins(p, AlertType.WEAR, current);
 					return;
 				}
 			}
 		}
 	}
 	
 	protected static void handleNoCookInvClick(InventoryClickEvent event) {
 		InventoryView view = event.getView();
 		int slot = event.getRawSlot();
 		ItemStack fuel = view.getItem(1);
 		ItemStack cookable = view.getItem(0);
 		if(view.getType() == InventoryType.FURNACE) {
 			SlotType st = event.getSlotType();
 			ItemStack cursor = event.getCursor();
 			ItemStack current = event.getCurrentItem();
 			Player p = getPlayerFromEntity(event.getWhoClicked());
 			if(slot == 1 && cursor.getTypeId() != 0 && cookable.getTypeId() != 0 && isFuel(cursor)) {
 				if(NoItem.getPermsManager().has(p, Perm.COOK, cookable)) {
 					event.setCancelled(true);
 					Messenger.sendMessage(p, AlertType.COOK, cookable);
 					Messenger.alertAdmins(p, AlertType.COOK, cookable);
 					// Check if the current item is also a fuel, just because.
 					if(current.getTypeId() != 0 && isFuel(current)) {
 						// If the inventory is full
 						if(p.getInventory().firstEmpty() == -1) {
 							// Drop a copy of the item by the player
 							p.getWorld().dropItem(p.getLocation(), new ItemStack(current));
 						} else {
 							// Give a copy of the item to the player
 							p.getInventory().addItem(new ItemStack(current));
 						}
 						// Remove the original item
 						view.setItem(slot, null);
 						p.sendMessage(ChatColor.BLUE + "Well darn, the old item is fuel too! Let me just fix that..");
 					}
 				}
 			// Uncooked Item slot
 			} else if (slot == 0) {
 				if(fuel.getTypeId() != 0 && cursor.getTypeId() != 0 && isCookable(cursor)) {
 					if(NoItem.getPermsManager().has(p, Perm.COOK, cursor)) {
 						event.setCancelled(true);
 						Messenger.sendMessage(p, AlertType.COOK, cursor);
 						Messenger.alertAdmins(p, AlertType.COOK, cursor);
 					}
 				}
 			// Shift clicking anywhere else in the inventory
 			} else if(slot > 3 && st != SlotType.OUTSIDE && event.isShiftClick()) {
 				if(current.getTypeId() != 0) {
 					if(fuel.getTypeId() != 0 && isCookable(current)) {
 						if(NoItem.getPermsManager().has(p, Perm.COOK, current)) {
 							event.setCancelled(true);
 							Messenger.sendMessage(p, AlertType.COOK, current);
 							Messenger.alertAdmins(p, AlertType.COOK, current);
 						}
 					} else if (cookable.getTypeId() != 0 && isFuel(current)) {
 						if(NoItem.getPermsManager().has(p, Perm.COOK, cookable)) {
 							event.setCancelled(true);
 							Messenger.sendMessage(p, AlertType.COOK, cookable);
 							Messenger.alertAdmins(p, AlertType.COOK, cookable);
 						}
 					}
 				}
 			}
 		}
 	}
 	// End - InventoryClickEvent //
 	
 	// Start - CraftItemEvent //
 	protected static void handleItemCraft(CraftItemEvent event) {
 		ItemStack result = event.getCurrentItem();
 		Player p = getPlayerFromEntity(event.getWhoClicked());
 		if(result.getTypeId() != 0 && NoItem.getPermsManager().has(p, Perm.CRAFT, result)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.CRAFT, result);
 			Messenger.alertAdmins(p, AlertType.CRAFT, result);
 		}
 	}
 	// End - CraftItemEvent //
 	
 	// Start - Helper Methods //
 	private static Player getPlayerFromEntity(HumanEntity ent) {
 		return Bukkit.getPlayer(ent.getName());
 	}
 	
 	private static String getRecipe(short dataValue, ItemStack ingredient) {
 		return dataValue + ":" + ingredient.getTypeId();
 	}
 	
 	private static boolean isFuel(ItemStack item) {
 		// Create an NMS item stack
 		net.minecraft.server.v1_4_6.ItemStack nmss = CraftItemStack.asNMSCopy(item);
 		// Use the NMS TileEntityFurnace to check if the item being clicked is a fuel
 		return TileEntityFurnace.isFuel(nmss);
 	}
 	
 	private static boolean isCookable(ItemStack item) {
 		net.minecraft.server.v1_4_6.ItemStack nmss = CraftItemStack.asNMSCopy(item);
 		// If the result of that item being cooked is null, it is not cookable
 		return RecipesFurnace.getInstance().getResult(nmss.getItem().id) != null;
 	}
 	// End - Helper Methods //
 }
