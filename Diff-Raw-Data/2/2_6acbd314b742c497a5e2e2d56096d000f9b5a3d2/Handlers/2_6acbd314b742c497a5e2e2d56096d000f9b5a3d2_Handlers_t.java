 package net.worldoftomorrow.noitem.events;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.server.v1_4_R1.Item;
 import net.minecraft.server.v1_4_R1.RecipesFurnace;
 import net.minecraft.server.v1_4_R1.TileEntityFurnace;
 import net.worldoftomorrow.noitem.NoItem;
 import net.worldoftomorrow.noitem.permissions.Perm;
 import net.worldoftomorrow.noitem.util.Util;
 import net.worldoftomorrow.noitem.util.Messenger;
 import net.worldoftomorrow.noitem.util.Messenger.AlertType;
 import net.worldoftomorrow.noitem.util.NMSMethods;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public final class Handlers {
 	
 	private static final Map<String, ArrayList<ItemStack>> playerItems = new HashMap<String, ArrayList<ItemStack>>();
 
 	// Begin - PlayerPickupItemEvent //
 	protected static void handleItemPickup(PlayerPickupItemEvent event) {
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
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
 			Util.switchItems(event.getNewSlot(), event.getPreviousSlot(), inv);
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
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
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
 	
 	protected static void handleLRUseInteract(PlayerInteractEvent event) {
 		if (event.isCancelled()) return;
 		Player p = event.getPlayer();
 		ItemStack inHand = p.getItemInHand();
 		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			if(NoItem.getLists().isTool(inHand) && NoItem.getPermsManager().has(p, Perm.USE_R, inHand)) {
 				event.setCancelled(true);
 				Messenger.sendMessage(p, AlertType.USE, inHand);
 				Messenger.alertAdmins(p, AlertType.USE, inHand);
 			}
 		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
 			if(NoItem.getLists().isTool(inHand) && NoItem.getPermsManager().has(p, Perm.USE_L, inHand)) {
 				event.setCancelled(true);
 				Messenger.sendMessage(p, AlertType.USE, inHand);
 				Messenger.alertAdmins(p, AlertType.USE, inHand);
 			}
 		}
 	}
 	
 
 	protected static void handleUseInteract(PlayerInteractEvent event) {
 		if (event.isCancelled()) return;
 		Player p = event.getPlayer();
 		ItemStack inHand = p.getItemInHand();
 		// If it is an interaction with air, skip any checks for efficiency
 		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR)
 			return;
 		if(NoItem.getLists().isTool(inHand) && NoItem.getPermsManager().has(p, Perm.USE, inHand)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.USE, inHand);
 			Messenger.alertAdmins(p, AlertType.USE, inHand);
 		}
 	}
 
 	protected static void handleInteractEntity(PlayerInteractEntityEvent event) {
 		if (event.isCancelled()) return;
 		Player p = event.getPlayer();
 		Entity e = event.getRightClicked();
 		if (NoItem.getPermsManager().has(p, Perm.INTERACT, e) || NoItem.getPermsManager().has(p, Perm.INTERACT_R, e)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.INTERACT, e);
 			Messenger.alertAdmins(p, AlertType.INTERACT, e);
 		}
 	}
 	
 	protected static void handleUseInteractEntity(PlayerInteractEntityEvent event) {
 		if (event.isCancelled()) return;
 		Player p = event.getPlayer();
 		ItemStack inHand = p.getItemInHand();
 		// Check right click and normal nodes.
 		// Shears are handled separately 
 		if(NoItem.getLists().isTool(inHand)
 				&& !NoItem.getLists().getTools().isShear(inHand)
 				&& (NoItem.getPermsManager().has(p, Perm.USE, inHand))) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.USE, inHand);
 			Messenger.alertAdmins(p, AlertType.USE, inHand);
 		}
 	}
 	// End - Player'Interact/InteractEntity'Event //
 	
 	// Begin - BlockBreakEvent //
 	protected static void handleBlockBreak(BlockBreakEvent event) {
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
 		
 		InventoryView view = event.getView();
 		if(view.getType() == InventoryType.BREWING) {
 			ItemStack cursor = event.getCursor();
 			Player p = getPlayerFromEntity(event.getWhoClicked());
 			int slot = event.getRawSlot();
 			ItemStack item;
 			// Ing. Slot
 			if(slot == 3 && cursor.getTypeId() != 0) {
 				int result;
 				for(int i = 0; i < 3; i++) {
 					item = view.getItem(i);
 					result = NMSMethods.getPotionResult(item.getDurability(), cursor);
 					// If the item is air, or the items durability is the same as the results, continue
 					if(item.getTypeId() == 0 || item.getDurability() == result) continue;
 					
 					if(NoItem.getPermsManager().has(p, result)) {
 						event.setCancelled(true);
 						Messenger.sendMessage(p, AlertType.BREW, result);
 						Messenger.alertAdmins(p, AlertType.BREW, result);
 						return; // Be sure to break the loop to avoid sending multiple messages
 					}
 				}
 				// Potion slot
 				// If it is not the ing. slot, the cursor is a potion, and the ingredient slot is not empty
 			} else if (slot < 3 && slot >= 0 && cursor.getTypeId() == Item.POTION.id && view.getItem(3).getTypeId() != 0) {
 				item = view.getItem(3); // ingredient
 				int result = NMSMethods.getPotionResult(cursor.getDurability(), item);
 				if(result == cursor.getDurability()) return;
 				if(item.getTypeId() != 0 && NoItem.getPermsManager().has(p, result)) {
 					event.setCancelled(true);
 					Messenger.sendMessage(p, AlertType.BREW, result);
 					Messenger.alertAdmins(p, AlertType.BREW, result);
 				}
 				// Shift click
 			} else if (slot > 3 && event.isShiftClick()) {
 				item = view.getItem(slot); // Clicked
 				// If the item clicked is a ptoion
 				if(item.getTypeId() == Item.POTION.id) {
 					ItemStack ingredient = view.getItem(3);
 					// If the ingredient is empty, return
 					if(ingredient.getTypeId() == 0) return;
 					
 					int result = NMSMethods.getPotionResult(item.getDurability(), ingredient);
 					if(NoItem.getPermsManager().has(p, result)) {
 						event.setCancelled(true);
 						Messenger.sendMessage(p, AlertType.BREW, result);
 						Messenger.alertAdmins(p, AlertType.BREW, result);
 					}
 					// Else, treat it as an ingredient
 				} else {
 					ItemStack potion;
 					int result;
 					for(int i = 0; i < 3; i++) {
 						potion = view.getItem(i);
 						result = NMSMethods.getPotionResult(potion.getDurability(), item);
 						if(NoItem.getPermsManager().has(p, result)) {
 							event.setCancelled(true);
 							Messenger.sendMessage(p, AlertType.BREW, result);
 							Messenger.alertAdmins(p, AlertType.BREW, result);
 							return;
 						}
 					}
 				}
 			}
 			/*
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
 			*/
 		}
 	}
 	
 	protected static void handleNoWearInvClick(InventoryClickEvent event) {
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
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
 		if (event.isCancelled()) return;
 		ItemStack result = event.getCurrentItem();
 		Player p = getPlayerFromEntity(event.getWhoClicked());
 		if(result.getTypeId() != 0 && NoItem.getPermsManager().has(p, Perm.CRAFT, result)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.CRAFT, result);
 			Messenger.alertAdmins(p, AlertType.CRAFT, result);
 		}
 	}
 	// End - CraftItemEvent //
 	
 	// Start - PlayerDeathEvent //
 	protected static void handlePlayerDeath(PlayerDeathEvent event) {
 		Player p = event.getEntity();
 		if(NoItem.getPermsManager().has(p, Perm.ONDEATH)) {
 			// Save a copy of the drops and map it to the players name
 			// TODO: Improve this to preserve item order and location.
 			Handlers.playerItems.put(p.getName(), new ArrayList<ItemStack>(event.getDrops()));
 			event.getDrops().clear(); // Clear the drops;
 		}
 	}
 	// End - PlayerDeathEvent //
 	
 	// Start - PlayerRespawnEvent //
 	protected static void handlePlayerSpawn(PlayerRespawnEvent event) {
 		Player p = event.getPlayer();
 		if(Handlers.playerItems.containsKey(p.getName())) {
 			for(ItemStack stack : playerItems.get(p.getName())) {
 				p.getInventory().addItem(stack);
 			}
 			Handlers.playerItems.remove(p.getName());
 		}
 	}
 	// End - PlayerRespawnEvent //
 	
 	// Start - EnchantItemEvent //
 	protected static void handleEnchantItem(EnchantItemEvent event) {
 		if (event.isCancelled()) return;
 		Player p = event.getEnchanter();
 		ItemStack item = event.getItem();
 		if(NoItem.getPermsManager().has(p, Perm.ENCHANT, item)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.ENCHANT, item);
 			Messenger.alertAdmins(p, AlertType.ENCHANT, item);
 		}
 	}
 	// End - EnchantItemEvent //
 	
 	// Start - PlayerDamageEntityEvent //
 	protected static void handlePlayerDamageEntity(EntityDamageByEntityEvent event) {
 		if (event.isCancelled()) return;
 		Entity e = event.getDamager();
 		if(e instanceof Player) {
 			Player p = (Player) e;
 			ItemStack inHand = p.getItemInHand();
 			// Check both for both left clicking, and general nodes.
 			if(NoItem.getLists().isTool(inHand)
 					&& (NoItem.getPermsManager().has(p, Perm.USE_L, inHand) 
 							|| NoItem.getPermsManager().has(p, Perm.USE, inHand))) {
 				event.setCancelled(true);
 				Messenger.sendMessage(p, AlertType.USE, inHand);
 				Messenger.alertAdmins(p, AlertType.USE, inHand);
 			}
 		}
 	}
 	// End - PlayerDamageEntityEvent //
 	
 	// Start PlayerShearEntityEvent //
 	protected static void handlePlayerShearEntity(PlayerShearEntityEvent event) {
 		if (event.isCancelled()) return;
 		Player p = event.getPlayer();
 		ItemStack inHand = p.getItemInHand();
 		// We can skip a tool check here, we already know they must be using shears
 		if(NoItem.getPermsManager().has(p, Perm.USE, inHand) || NoItem.getPermsManager().has(p, Perm.USE_R, inHand)) {
 			event.setCancelled(true);
 			Messenger.sendMessage(p, AlertType.USE, inHand);
 			Messenger.alertAdmins(p, AlertType.USE, inHand);
 		}
 	}
 	// End PlayerShearEntityEvent //
 	// Start PlayerJoinEvent //
 	protected static void handleArmorCheck(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		PlayerInventory inv = p.getInventory();
 		Boolean foundPerm = false;
 		
 		// used to update the player's armor
 		ItemStack[] playerArmor = inv.getArmorContents();
     	
		// used for items that won't fit in the player's inventory
 		ArrayList<ItemStack> armorToDrop = new ArrayList<ItemStack>();
     	
 		// go through and find out which armor needs to be removed
 		for(int i = 0; i < playerArmor.length; i++) {
 			ItemStack armorPiece = playerArmor[i];
 			if (armorPiece != null && NoItem.getPermsManager().has(p, Perm.WEAR, armorPiece)) {
 				foundPerm = true;
 				playerArmor[i] = null;
     			
 				// put the armor in the inventory or 
 				//   add it to our drop list if inventory is full
 				armorToDrop.addAll((inv.addItem(armorPiece).values()));
     			
     			
 				Messenger.sendMessage(p, AlertType.WEAR, armorPiece);
 				Messenger.alertAdmins(p, AlertType.WEAR, armorPiece);
 			}
 		}
 
 		// don't bother doing this if everything was all good
 		if (foundPerm) {
 			// update player's armor
 			inv.setArmorContents(playerArmor);
 	    	
 			// drop the items that were not able to be added to the inventory
 			if (armorToDrop.size() > 0) {
 				// get location for drops
 				Location loc = p.getLocation();
 				World world = loc.getWorld();
 				
 				for(ItemStack item : armorToDrop) {
 					world.dropItemNaturally(loc, item);
 				}
 			}
 		}
 	}
 	
 	// End PlayerJoinEvent //
 	
 	// Start - Helper Methods //
 	private static Player getPlayerFromEntity(HumanEntity ent) {
 		return Bukkit.getPlayer(ent.getName());
 	}
 	
 	//private static String getRecipe(short dataValue, ItemStack ingredient) {
 	//	return dataValue + ":" + ingredient.getTypeId();
 	//}
 	
 	private static boolean isFuel(ItemStack item) {
 		// Create an NMS item stack
 		net.minecraft.server.v1_4_R1.ItemStack nmss = CraftItemStack.asNMSCopy(item);
 		// Use the NMS TileEntityFurnace to check if the item being clicked is a fuel
 		return TileEntityFurnace.isFuel(nmss);
 	}
 	
 	private static boolean isCookable(ItemStack item) {
 		net.minecraft.server.v1_4_R1.ItemStack nmss = CraftItemStack.asNMSCopy(item);
 		// If the result of that item being cooked is null, it is not cookable
 		return RecipesFurnace.getInstance().getResult(nmss.getItem().id) != null;
 	}
 	// End - Helper Methods //
 }
