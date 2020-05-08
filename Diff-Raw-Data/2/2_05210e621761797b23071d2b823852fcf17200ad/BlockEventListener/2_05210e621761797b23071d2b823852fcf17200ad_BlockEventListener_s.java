 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.ScheduledTasks;
 import com.araeosia.ArcherGames.utils.Archer;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class BlockEventListener implements Listener {
 
 	public ArcherGames plugin;
 
 	public BlockEventListener(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onBlockBreak(final BlockBreakEvent event) {
 		if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.blockedit") || !Archer.getByName(event.getPlayer().getName()).isAlive()) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 		}
 	}
 
 	@EventHandler
 	public void onBlockBurn(final BlockBurnEvent event) {
 		if (ScheduledTasks.gameStatus == 1) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockPlace(final BlockPlaceEvent event) {
 		if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.blockedit") || !Archer.getByName(event.getPlayer().getName()).isAlive()) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 		}
 	}
 
 	@EventHandler
 	public void onBlockIgnite(final BlockIgniteEvent event) {
 		if (event.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)) {
 			if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.blockedit") || !Archer.getByName(event.getPlayer().getName()).isAlive()) { // ERROR!
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 			}
 		}
 	}
 
 	@EventHandler
 	public void onInventoryOpen(final InventoryOpenEvent event) {
 		if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.invedit") || !Archer.getByName(event.getPlayer().getName()).isAlive()) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockInteract(final PlayerInteractEvent event) {
 		// TODO: Add some checking to see if 
 		if (event.hasBlock()) {
 			if (plugin.debug) {
 				if (event.getClickedBlock().getState() instanceof Sign) {
 					Sign sign = (Sign) event.getClickedBlock().getState();
 					plugin.log.info(sign.getLine(0));
 				}
 			}
 			if (event.getClickedBlock() instanceof Chest || event.getClickedBlock() instanceof DoubleChest) {
 				if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.invedit")) {
 					event.setCancelled(true);
 				}
 			} else if (event.getClickedBlock().getState() instanceof Sign && false) {
 				/*
 				 * Sign sign = (Sign) event.getClickedBlock().getState(); if
 				 * (sign.getLine(0).equals("§1[Enchant]")) {
 				 * event.setCancelled(true); // Line 2: Any, Line 3:
 				 * Enchantment:Level Line 4: Price double price = new
 				 * Double(sign.getLine(3).substring(1)); if
 				 * (plugin.econ.hasBalance(event.getPlayer().getName(), price))
 				 * { String[] data = sign.getLine(2).split(":"); Enchantment
 				 * enchantment = Enchantment.getByName(data[0]); ItemStack
 				 * itemInHand =
 				 * event.getPlayer().getInventory().getItemInHand(); boolean
 				 * isOkay = true; for (Enchantment e :
 				 * itemInHand.getEnchantments().keySet()) { if
 				 * (e.conflictsWith(enchantment) || e.equals(enchantment)) {
 				 * isOkay = false;
 				 * event.getPlayer().sendMessage(plugin.strings.get("enchantmentconflict"));
 				 * } } if
 				 * (enchantment.canEnchantItem(event.getPlayer().getInventory().getItemInHand())
 				 * && isOkay) { ItemStack newItemStack = itemInHand;
 				 * newItemStack.addEnchantment(enchantment,
 				 * Integer.parseInt(data[1]));
 				 * event.getPlayer().getInventory().setItemInHand(newItemStack);
 				 * } //plugin.econ.takePlayer(event.getPlayer().getName(),
 				 * price); } else {
 				 * event.getPlayer().sendMessage(plugin.strings.get("notenoughmoney"));
 				 * } } else if (sign.getLine(0).equals("§1[Buy]")) {
 				 * event.setCancelled(true); // Line 2: Quantity, Line 3: Item
 				 * name, Line 4: Price double price = new
 				 * Double(sign.getLine(3).substring(1)); //if
 				 * (plugin.econ.hasBalance(event.getPlayer().getName(), price))
 				 * { ItemStack itemToGive; if (sign.getLine(2).contains(":")) {
 				 * String[] data = sign.getLine(2).split(":"); int damage = 0;
 				 * if (data.length == 1) { damage = 0; } else { damage =
 				 * Integer.parseInt(data[1]); } plugin.log.info(data[0]);
 				 * Material mat; mat =
 				 * Material.getMaterial(data[0].toUpperCase());
 				 * plugin.log.info(mat.toString()); itemToGive = new
 				 * ItemStack(mat, Integer.parseInt(data[1])); }else{ Material
 				 * mat; plugin.log.info(sign.getLine(2)); mat =
 				 * Material.getMaterial(sign.getLine(2).toUpperCase());
 				 * plugin.log.info(mat.toString()); itemToGive = new
 				 * ItemStack(mat); }
 				 *
 				 * event.getPlayer().getInventory().addItem(itemToGive);
 				 * //plugin.econ.takePlayer(event.getPlayer().getName(), price);
 				 * } else {
 				 * event.getPlayer().sendMessage(plugin.strings.get("notenoughmoney"));
 				 * } }
 			}
 				 */
 			}
 		}
 	}
 }
