 package com.em.chesttrap;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 import com.em.chesttrap.ChestTrapContent.SortType;
 
 public class ChestTrapListener implements Listener {
 
 	ChestTrap thePlugin;
 
 	/**
 	 * Constructor
 	 **/
 	public ChestTrapListener(ChestTrap plugin) {
 		this.thePlugin = plugin;
 	}
 
 	/**
 	 * On destruction of Block
 	 **/
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		// if it's a ChestTrap
 		if (this.thePlugin.chestMap.containsKey(event.getBlock().getLocation())) {
 			// Remove it from the list
 			this.thePlugin.chestMap.remove(event.getBlock().getLocation());
 			event.getPlayer().sendMessage(ChatColor.GREEN + "Chest removed!");
 		}
 	}
 
 	/**
 	 * On furnace has smelt (it changes the inventory)
 	 **/
 	@EventHandler
 	public void onInventoryEvent(FurnaceSmeltEvent event) {
 		// System.out.println(event.getEventName() + " " + event.getBlock());
 
 		BlockState blockState = event.getBlock().getState();
 		if (blockState instanceof InventoryHolder) {
 
 			InventoryHolder ih = (InventoryHolder) blockState;
 			onInventoryEvent(new MyInventoryModifiedEvent(ih.getInventory()));
 
 		}
 	}
 
 	/**
 	 * On an item is dispensed from a block. (it changes the inventory)
 	 **/
 	@EventHandler
 	public void onInventoryEvent(BlockDispenseEvent event) {
 		// System.out.println(event.getEventName() + " " + event.getBlock());
 
 		BlockState blockState = event.getBlock().getState();
 		if (blockState instanceof InventoryHolder) {
 
 			InventoryHolder ih = (InventoryHolder) blockState;
 			final MyInventoryModifiedEvent aEvent = new MyInventoryModifiedEvent(ih.getInventory());
 			this.thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(this.thePlugin, new Runnable() {
 				public void run() {
 					onInventoryEvent(aEvent);
 				}
 			}, 0L);
 
 		}
 	}
 
 	/**
 	 * On Inventory close
 	 **/
 	@EventHandler
 	public void onInventoryEvent(InventoryCloseEvent event) {
 		// System.out.println("================== InventoryCloseEvent");
 		onInventoryEvent(new MyInventoryModifiedEvent(event.getInventory()));
 	}
 
 	/**
 	 * On Inventory close
 	 **/
 	@EventHandler
 	public void onInventoryEvent(MyInventoryModifiedEvent event) {
 		// System.out.println("================== MyInventoryModifiedEvent 1");
 
 		if (event.getInventory() == null) {
 			return;
 		}
 
 		// System.out.println("================== MyInventoryModifiedEvent 2");
 		InventoryHolder ih = event.getInventory().getHolder();
 		// System.out.println("================== MyInventoryModifiedEvent 2 " +
 		// ih);
 
 		if (ih instanceof DoubleChest) {
 			DoubleChest doubleChest = (DoubleChest) ih;
 			if (Math.random() < 0.5) {
 				eventReceivedOnChest(event, doubleChest.getRightSide());
 				eventReceivedOnChest(event, doubleChest.getLeftSide());
 			} else {
 				eventReceivedOnChest(event, doubleChest.getLeftSide());
 				eventReceivedOnChest(event, doubleChest.getRightSide());
 			}
 		} else {
 			eventReceivedOnChest(event, ih);
 		}
 	}
 
 	/**
 	 * We just managed the event on a Chest (twice if it's a double chest
 	 * 
 	 * @param event
 	 *          the event
 	 * @param ih
 	 *          the single chast
 	 */
 	private void eventReceivedOnChest(MyInventoryModifiedEvent event, InventoryHolder ih) {
 		BlockState blockState = null;
 		if (ih instanceof BlockState) {
 			blockState = (BlockState) ih;
 		}
 		if (blockState != null) {
 			Block b = blockState.getBlock();
 
 			boolean changed = this.thePlugin.chestMap.containsKey(b.getLocation()) && this.thePlugin.chestMap.get(b.getLocation()).changeInventory(event.getInventory());
 			// System.out.println("================== MyInventoryModifiedEvent 4");
 			if (changed) {
 				// System.out.println("================== MyInventoryModifiedEvent 5");
 
 				ChestTrapContent chestTrapContent = this.thePlugin.chestMap.get(b.getLocation());
 
 				// Inventory change, set on power !!!!
 				List<Block> arrayTmp = new ArrayList<Block>();
 				arrayTmp.add(b.getRelative(BlockFace.NORTH));
 				arrayTmp.add(b.getRelative(BlockFace.SOUTH));
 				arrayTmp.add(b.getRelative(BlockFace.EAST));
 				arrayTmp.add(b.getRelative(BlockFace.WEST));
 				arrayTmp.add(b.getRelative(BlockFace.UP));
 				arrayTmp.add(b.getRelative(BlockFace.DOWN));
 
 				// Just sort it randomly
 				List<Block> array = new ArrayList<Block>();
 				while (!arrayTmp.isEmpty()) {
 					int r = (int) Math.floor(Math.random() * arrayTmp.size());
 					array.add(arrayTmp.get(r));
 					arrayTmp.remove(r);
 				}
 
 				for (Block block : array) {
 					final Block blockf = block;
 
 					// Send an event (for others mod)
 					// BlockRedstoneEvent newEvent = new BlockRedstoneEvent(block, 0, 15);
 					// Bukkit.getServer().getPluginManager().callEvent(newEvent);
 					this.thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(this.thePlugin, new Runnable() {
 						public void run() {
 							BlockRedstoneEvent newEvent = new BlockRedstoneEvent(blockf, 0, 15);
 							Bukkit.getServer().getPluginManager().callEvent(newEvent);
 						}
 					}, 5L);
 					this.thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(this.thePlugin, new Runnable() {
 						public void run() {
 							BlockRedstoneEvent newEvent = new BlockRedstoneEvent(blockf, 15, 0);
 							Bukkit.getServer().getPluginManager().callEvent(newEvent);
 						}
 					}, 8L);
 
 					// if it's a REDSTONE_WIRE, set power
 					if (block.getType() == Material.REDSTONE_WIRE) {
 
 						// I find no other way... change it to torch
 						block.setTypeId(0);
 						block.setType(Material.REDSTONE_TORCH_ON);
 						this.thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(this.thePlugin, new Runnable() {
 							public void run() {
 								blockf.setTypeId(0);
 								blockf.setType(Material.REDSTONE_WIRE);
 							}
 						}, 6L);
 
 					}
 				}
 
 				// Sort it
 				if (chestTrapContent.getSort() != SortType.NONE) {
 					List<ItemStack> lst = getSortedList(ih);
 
 					// for (ItemStack itemStack : lst) {
 					// Bukkit.getLogger().info("--" + itemStack.getType() + " " + itemStack.getAmount() + " " + itemStack.getData());
 					// }
 
 					// Put it back into the inventory
 					if (chestTrapContent.getSort() == SortType.SIMPLE) {
 						int cpt = 0;
 						for (ItemStack itemStack : lst) {
 							ih.getInventory().setItem(cpt, itemStack);
 							cpt++;
 						}
 						while (cpt < ih.getInventory().getSize()) {
 							ih.getInventory().clear(cpt);
 							cpt++;
 						}
 					}
 					// TODO : not yet implemented
 					// if ((chestTrapContent.getSort() == SortType.LINE) && (ih.getInventory().getType() == InventoryType.CHEST)) {
 					// int col = 9;
 					// int line = ih.getInventory().getSize() / col;
 					// Bukkit.getLogger().info("" + col+", "+line);
 					// }
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get the sorted list of stack
 	 * 
 	 * @param ih
 	 * @return
 	 */
 	private List<ItemStack> getSortedList(InventoryHolder ih) {
 		Inventory inventory = ih.getInventory();
 		List<ItemStack> lst = new ArrayList<ItemStack>();
 		ItemStackComparator comparator = new ItemStackComparator();
 
 		// Create List to sort
 		for (Iterator<ItemStack> iterator = inventory.iterator(); iterator.hasNext();) {
 			ItemStack ihStack = iterator.next();
 			if (ihStack == null) {
 				continue;
 			}
 			ihStack = ihStack.clone();
 
 			// Complete stack
 			for (ItemStack lstStack : lst) {
 				if ((ihStack.getAmount() != ihStack.getMaxStackSize()) && (lstStack != null) && (lstStack.getAmount() != lstStack.getMaxStackSize())) {
 					if (comparator.compareType(ihStack, lstStack) == 0) {
 						int complete = Math.min(ihStack.getAmount(), lstStack.getMaxStackSize() - lstStack.getAmount());
 						lstStack.setAmount(lstStack.getAmount() + complete);
 						ihStack.setAmount(ihStack.getAmount() - complete);
 					}
 				}
 			}
 			// add to List
 			if (ihStack.getAmount() != 0) {
 				lst.add(ihStack);
 			}
 		}
 
 		// Sort It
 		Collections.sort(lst, comparator);
 		return lst;
 	}
 
 	/**
 	 * On inventory open
 	 **/
 	// @EventHandler
 	// public void onInventoryEvent(InventoryOpenEvent event) {
 	//
 	// System.out.println(event.getEventName() + " " + event.getInventory());
 	// }
 	/**
 	 * On redstone change of a block
 	 **/
 	// @EventHandler
 	// public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
 	//
 	// System.out.println(event.getEventName()+" "+event.getBlock());
 	// }
 
 	private class ItemStackComparator implements Comparator<ItemStack> {
 
 		/**
 		 * Just compare type object (without comparing amount)
 		 * 
 		 * @param o1
 		 * @param o2
 		 * @return
 		 */
 		public int compareType(ItemStack o1, ItemStack o2) {
 			if ((o1 == null) && (o2 == null)) {
 				return 0;
 			}
 			if (o1 == null) {
 				return -1;
 			}
 			if (o2 == null) {
 				return 1;
 			}
 
 			if (o1.getTypeId() < o2.getTypeId()) {
 				return 1;
 			}
 			if (o1.getTypeId() > o2.getTypeId()) {
 				return -1;
 			}
 
 			if (o1.getData().getData() < o2.getData().getData()) {
 				return -1;
 			}
 			if (o1.getData().getData() > o2.getData().getData()) {
 				return 1;
 			}
 
 			Map<Enchantment, Integer> o1Enchantments = o1.getEnchantments();
 			Map<Enchantment, Integer> o2Enchantments = o2.getEnchantments();
 
 			for (Enchantment o1Ench : o1Enchantments.keySet()) {
 				if (o1.getEnchantmentLevel(o1Ench) > o2.getEnchantmentLevel(o1Ench)) {
 					return 1;
 				}
 			}
 			for (Enchantment o2Ench : o2Enchantments.keySet()) {
 				if (o2.getEnchantmentLevel(o2Ench) > o1.getEnchantmentLevel(o2Ench)) {
 					return -1;
 				}
 			}
 
 			// if equal compare amount
 			if (o1.getDurability() < o2.getDurability()) {
 				return 1;
 			}
 			if (o1.getDurability() > o2.getDurability()) {
 				return -1;
 			}
 
 			return 0;
 		}
 
 		public int compare(ItemStack o1, ItemStack o2) {
 
 			// compare type
 			int ret = compareType(o1, o2);
 			if (ret != 0) {
 				return ret;
 			}
 
 			// if equal compare amount
 			if (o1.getAmount() < o2.getAmount()) {
 				return 1;
 			}
 			if (o1.getAmount() > o2.getAmount()) {
 				return -1;
 			}
 
 			return 0;
 		}
 
 	}
 
 }
