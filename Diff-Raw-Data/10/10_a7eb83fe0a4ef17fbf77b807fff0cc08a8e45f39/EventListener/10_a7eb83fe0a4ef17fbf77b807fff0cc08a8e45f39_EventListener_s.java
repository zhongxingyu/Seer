 package net.worldoftomorrow.nala.ni.listeners;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.server.Container;
 import net.minecraft.server.CraftingManager;
 import net.minecraft.server.InventoryCraftResult;
 import net.minecraft.server.InventoryCrafting;
 import net.worldoftomorrow.nala.ni.CustomBlocks;
 import net.worldoftomorrow.nala.ni.Enchant;
 import net.worldoftomorrow.nala.ni.EventTypes;
 import net.worldoftomorrow.nala.ni.Log;
 import net.worldoftomorrow.nala.ni.NoItem;
 import net.worldoftomorrow.nala.ni.Perms;
 import net.worldoftomorrow.nala.ni.StringHelper;
 import net.worldoftomorrow.nala.ni.CustomItems.CustomBlock;
 import net.worldoftomorrow.nala.ni.CustomItems.CustomFurnace;
 import net.worldoftomorrow.nala.ni.CustomItems.CustomWorkbench;
 import net.worldoftomorrow.nala.ni.tasks.LoginTask;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import forge.bukkit.ModInventoryView;
 
 public class EventListener implements Listener {
 
 	private final NoItem plugin;
 
 	public EventListener(NoItem plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Check for the following: <br />
 	 * NoHold
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		ItemStack inhand = p.getItemInHand();
 		BukkitScheduler scheduler = plugin.getServer().getScheduler();
 		if (Perms.NOHOLD.has(p, inhand)) {
 			scheduler.scheduleSyncDelayedTask(plugin, new LoginTask(p), 60L);
 		}
 	}
 
 	/**
 	 * Check for the following: <br />
 	 * NoDrop
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onItemDrop(PlayerDropItemEvent event) {
 		Player p = event.getPlayer();
 		ItemStack drop = event.getItemDrop().getItemStack();
 		if (Perms.NODROP.has(p, drop)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.DROP, drop);
 		}
 	}
 
 	/**
 	 * Check for the following: <br />
 	 * NoPickup
 	 * NoHave
 	 * NoHold
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onItemPickup(PlayerPickupItemEvent event) {
 		Player p = event.getPlayer();
 		ItemStack item = event.getItem().getItemStack();
 
 		if (Perms.NOPICKUP.has(p, item)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.PICKUP, item);
 		} else if (Perms.NOHAVE.has(p, item)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.HAVE, item);
 		} else if (Perms.NOHOLD.has(p, item)) {
 			PlayerInventory inv = p.getInventory();
 			if (p.getItemInHand().getType() == Material.AIR
 					&& inv.firstEmpty() == inv.getHeldItemSlot()) {
 				event.setCancelled(true);
 				event.getItem().setPickupDelay(200);
 				this.notify(p, EventTypes.HOLD, item);
 			}
 		}
 	}
 
 	/**
 	 * Check for the following: <br />
 	 * OnDeath
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player p = event.getEntity();
 		if (Perms.ONDEATH.has(p, "keep")) {
 			List<ItemStack> drops = new ArrayList<ItemStack>(event.getDrops());
 			event.getDrops().clear();
 			plugin.getItemList().put(p.getName(), drops);
 		} else if (Perms.ONDEATH.has(p, "remove")) {
 			event.getDrops().clear();
 		}
 	}
 	
 	/**
 	 * Check for the following: <br />
 	 * OnDeath
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player p = event.getPlayer();
 		Map<String, List<ItemStack>> itemList = plugin.getItemList();
 		if (itemList.containsKey(p.getName())) {
			Map<Integer, ItemStack> r = p.getInventory().addItem(
					(ItemStack[]) itemList.get(p.getName()).toArray());
 			if(!r.values().isEmpty()) {
 				itemList.put(p.getName(), new ArrayList<ItemStack>(r.values()));
 				p.sendMessage(ChatColor.BLUE + "You have " + r.size() + " unclaimed items!");
 				p.sendMessage(ChatColor.BLUE + "Make room in your inventory, then type \"/noitem claim\" to claim them!");
 			}
 		}
 	}
 	
 	/**
 	 * Check for the following: <br />
 	 * NoBreak
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player p = event.getPlayer();
 		Block b = event.getBlock();
 		if(Perms.NOBREAK.has(p, b)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.BREAK, b);
 		}
 	}
 
 	/**
 	 * Check for the following: <br />
 	 * NoPlace
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Player p = event.getPlayer();
 		Block b = event.getBlock();
 		if(Perms.NOPLACE.has(p, b)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.PLACE, b);
 		}
 	}
 
 	/**
 	 * Check for the following: <br />
 	 * NoHold
 	 * NoHave
 	 * NoWear
 	 * NoCook
 	 * NoCraft
 	 * NoBrew
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event) {
 		Player p = Bukkit.getPlayer(event.getWhoClicked().getName());
 		Inventory inv = event.getInventory();
 		ItemStack oncur = p.getItemOnCursor();
 		ItemStack current = event.getCurrentItem();
 		int rs = event.getRawSlot();
 		InventoryView view = event.getView();
 		
 		switch(inv.getType()) {
 		case CRAFTING:
 			if(event.getSlotType() == SlotType.ARMOR) {
 				if(oncur != null && Perms.NOWEAR.has(p, oncur)) {
 					event.setCancelled(true);
 					this.notify(p, EventTypes.WEAR, oncur);
 				}
 			}
 			break;
 		case BREWING:
 			if(rs == 3 && oncur != null) {
 				for(ItemStack item : inv.getContents()) {
 					if(item != null) {
 						int dv = item.getDurability();
 						int id = oncur.getTypeId();
 						if (Perms.NOBREW.has(p, dv + "." + id)) {
 							event.setCancelled(true);
 							this.notify(p, EventTypes.BREW, dv + ":" + id);
 						}
 					}
 				}
 			} else if (rs < 3 && rs >= 0) {
 				ItemStack ing = inv.getItem(3);
 				if(ing != null && oncur != null) {
 					int dv = oncur.getDurability();
 					int id = ing.getTypeId();
 					if (Perms.NOBREW.has(p, dv + "." + id)) {
 						event.setCancelled(true);
 						this.notify(p, EventTypes.BREW, dv + ":" + id);
 					}
 				}
 			}
 			break;
 		case FURNACE:
 			if(rs == 0 && oncur != null) {
 				if(Perms.NOCOOK.has(p, oncur)) {
 					event.setCancelled(true);
 					this.notify(p, EventTypes.COOK, oncur);
 				}
 			} else if (rs != 0 && event.isShiftClick()) {
 				if(Perms.NOCOOK.has(p, current)) {
 					event.setCancelled(true);
 					this.notify(p, EventTypes.COOK, current);
 				}
 			}
 		default:
 			Block b = p.getTargetBlock(null, 8);
 			if(!CustomBlocks.isCustomBlock(b))
 				return;
 			CustomBlock cb = CustomBlocks.getCustomBlock(b);
 			switch(cb.getType()) {
 			case FURNACE:
 				CustomFurnace cf = (CustomFurnace) cb;
 				if(cf.isFuelSlot((short) rs) && oncur != null) {
 					for(Short s : cf.getItemSlots()) {
 						ItemStack item = view.getItem(s);
 						if(item != null && Perms.NOCOOK.has(p, item)) {
 							event.setCancelled(true);
 							this.notify(p, EventTypes.COOK, item);
 							break;
 						}
 					}
 				} else if (cf.isItemSlot((short) rs) && oncur != null) {
 					if(!cf.usesFuel() && Perms.NOCOOK.has(p, oncur)) {
 						event.setCancelled(true);
 						this.notify(p, EventTypes.COOK, oncur);
 						break;
 					}
 					for(Short s : cf.getFuelSlots()) {
 						if(view.getItem(s) != null && Perms.NOCOOK.has(p, oncur)) {
 							event.setCancelled(true);
 							this.notify(p, EventTypes.COOK, oncur);
 						}
 					}
 				}
 				break;
 			case WORKBENCH:
 				CustomWorkbench cw = (CustomWorkbench) cb;
 				if(cw.isResultSlot((short) rs)) {
 					if(current != null) {
 						try {
 						ModInventoryView miv = (ModInventoryView) view;
 						Field fcontainer = view.getClass().getDeclaredField("container");
 						fcontainer.setAccessible(true);
 						Container container = (Container) fcontainer.get(miv);
 						InventoryCrafting craftingInv = new InventoryCrafting(container, 3, 3);
 						craftingInv.resultInventory = new InventoryCraftResult();
 						for(int i = 1; i < 10; i++) {
 							short slot = (Short) cw.getRecipeSlots().toArray()[i-1];
 							ItemStack item = slot == rs ? oncur : view.getItem(slot);
 							if(item == null) continue;
 							net.minecraft.server.ItemStack stack = new net.minecraft.server.ItemStack(item.getTypeId(), item.getAmount(), item.getDurability());
 							craftingInv.setItem(i, stack);
 						}
 						net.minecraft.server.ItemStack mcResult = CraftingManager.getInstance().craft(craftingInv);
 						if(mcResult == null) return;
 						ItemStack result = new ItemStack(mcResult.id, mcResult.getData());
 						if (Perms.NOCRAFT.has(p, result)) {
 							event.setCancelled(true);
 							this.notify(p, EventTypes.CRAFT, result);
 						}
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 				break;
 			default:
 				Log.severe("Undefined custom block.");
 				break;
 			}
			
 		}
 		//NoHold handling
 		if(event.getSlotType() == SlotType.QUICKBAR
 				&& oncur != null
 				&& !event.isCancelled()
 				&& Perms.NOHOLD.has(p, oncur)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.HOLD, oncur);
 		} else if (event.getSlotType() != SlotType.QUICKBAR
 				&& event.isShiftClick()
 				&& !event.isCancelled()
 				&& current != null
 				&& Perms.NOHOLD.has(p, current)) {
 			Inventory binv = view.getBottomInventory();
 			if(binv instanceof PlayerInventory) {
 				for(int i = 0; i < 9; i++) {
 					ItemStack stack = binv.getItem(i);
					if(stack == null) {
 						event.setCancelled(true);
 						this.notify(p, EventTypes.HOLD, stack);
 						continue;
 					}
 				}
 			}
 		}
 		//NoHave handling
 		if(current != null && Perms.NOHAVE.has(p, current)) {
 			current = null;
 			this.notify(p, EventTypes.HAVE, current);
 		}
 	}
 
 	@EventHandler
 	public void onInventoryOpen(InventoryOpenEvent event) {
 		Player p = Bukkit.getPlayer(event.getPlayer().getName());
 		List<Block> blocks = p.getLastTwoTargetBlocks(null, 8);
 		if(!blocks.isEmpty() && blocks.size() == 2) {
 			Block target = blocks.get(1);
 			if(Perms.NOOPEN.has(p, target)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.OPEN, target);
 				return;
 			}
 		}
 		ItemStack inHand = p.getItemInHand();
 		CustomWorkbench cw = CustomBlocks.getWorkbench(inHand);
 		if(cw != null && !cw.isBlock()) {
 			if(Perms.NOOPEN.has(p, inHand)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.OPEN, inHand);
 			}
 		}
 	}
 	
 	/**
 	 * Check for the following: <br />
 	 * NoHold
 	 * NoHave
 	 * @param event
 	 */
 	@EventHandler
 	public void onItemHeld(PlayerItemHeldEvent event) {
 		Player p = event.getPlayer();
 		PlayerInventory inv = p.getInventory();
 		ItemStack allowed = inv.getItem(event.getPreviousSlot());
 		ItemStack notAllowed = inv.getItem(event.getNewSlot());
 		if(notAllowed != null) {
 			if(Perms.NOHOLD.has(p, notAllowed)) {
 				inv.setItem(event.getPreviousSlot(), notAllowed);
 				inv.setItem(event.getNewSlot(), allowed);
 				this.notify(p, EventTypes.HOLD, notAllowed);
 			}
 			if(Perms.NOHAVE.has(p, notAllowed)) {
 				notAllowed = null;
 				this.notify(p, EventTypes.HAVE, notAllowed);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onItemCraft(CraftItemEvent event) {
 		Player p = Bukkit.getPlayer(event.getWhoClicked().getName());
 		ItemStack result = event.getCurrentItem();
 		if(Perms.NOCRAFT.has(p, result)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.CRAFT, result);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 		Block b = event.getClickedBlock();
 		ItemStack inHand = event.getItem();
 		switch(event.getAction()) {
 		case RIGHT_CLICK_BLOCK:
 		case LEFT_CLICK_BLOCK:
 			if(Perms.NOUSE.has(p, b)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.USE, b);
 			} else if (Perms.NOUSE.has(p, inHand)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.USE, inHand);
 			}
 			break;
 		case RIGHT_CLICK_AIR:
 			if(Perms.NOUSE.has(p, inHand)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.USE, inHand);
 			}
 			break;
 		case LEFT_CLICK_AIR:
 			break;
 		case PHYSICAL:
 			if(Perms.NOUSE.has(p, b)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.USE, b);
 			}
 			
 		}
 		if(Perms.NOHAVE.has(p, inHand)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.HAVE, inHand);
 			inHand = null; //Remove the item
 		}
 	}
 
 	@EventHandler
 	public void onBowShoot(EntityShootBowEvent event) {
 		Entity e = event.getEntity();
 		if (e instanceof Player) {
 			Player p = (Player) e;
 			ItemStack bow = event.getBow().clone();
 			bow.setDurability((short) 0);
 			if(Perms.NOUSE.has(p, bow)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.USE, bow);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
 		Player p = event.getPlayer();
 		ItemStack bucket = p.getItemInHand();
 		if(Perms.NOUSE.has(p, bucket)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.USE, bucket);
 		}
 	}
 
 	@EventHandler
 	public void onBucketFill(PlayerBucketFillEvent event) {
 		Player p = event.getPlayer();
 		ItemStack bucket = event.getItemStack();
 		if(Perms.NOUSE.has(p, bucket)) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.USE, bucket);
 		}
 	}
 
 	@EventHandler
 	public void onShear(PlayerShearEntityEvent event) {
 		Player p = event.getPlayer();
 		if(Perms.NOUSE.has(p, new ItemStack(Material.SHEARS))) {
 			event.setCancelled(true);
 			this.notify(p, EventTypes.USE, new ItemStack(Material.SHEARS));
 		}
 	}
 
 	@EventHandler
 	public void onPlayerAttack(EntityDamageByEntityEvent event) {
 		Entity damager = event.getDamager();
 		if (damager instanceof Player) {
 			Player p = (Player) damager;
 			ItemStack inhand = p.getItemInHand().clone();
 			if(Perms.NOUSE.has(p, inhand)) {
 				event.setCancelled(true);
 				this.notify(p, EventTypes.USE, inhand);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onItemEnchant(EnchantItemEvent event) {
 		Player p = event.getEnchanter();
 		ItemStack item = event.getItem();
 		Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
 		for(Enchantment enchantment : enchantments.keySet()) {
 			Enchant enchant = Enchant.getByID(enchantment.getId());
 			if(Perms.NOENCHANT.has(p, enchant.getName(), item)) {
 				//Silently remove the enchantment
 				enchantments.remove(enchantment);
 				//If there is not another enchantment, cancel the event.
 				if(enchantments.size() == 0) {
 					event.setCancelled(true);
 					this.notify(p, EventTypes.ENCHANT, enchant.getName());
 					break;
 				}
 			}
 		}
 	}
 
 	private void notify(Player p, EventTypes type, ItemStack stack) {
 		StringHelper.notifyPlayer(p, type, stack);
 		StringHelper.notifyAdmin(p, type, stack);
 	}
 	
 	private void notify(Player p, EventTypes type, Block b) {
 		this.notify(p, type, new ItemStack(b.getType(), b.getData()));
 	}
 	
 	private void notify(Player p, EventTypes type, String recipe) {
 		StringHelper.notifyAdmin(p, type, recipe);
 		StringHelper.notifyPlayer(p, type, recipe);
 	}
 }
