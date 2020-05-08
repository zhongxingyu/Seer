 package org.fatecrafters.plugins.listeners;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.fatecrafters.plugins.RealisticBackpacks;
 import org.fatecrafters.plugins.util.MysqlFunctions;
 import org.fatecrafters.plugins.util.RBUtil;
 
 public class InventoryListener implements Listener {
 
 	private final RealisticBackpacks plugin;
 
 	public InventoryListener(final RealisticBackpacks plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onInventoryClose(final InventoryCloseEvent e) {
 		final String name = e.getPlayer().getName();
 		final Inventory inv = e.getView().getTopInventory();
 		if (plugin.playerData.containsKey(name)) {
 			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 				@Override
 				public void run() {
 					if (plugin.isUsingMysql()) {
 						try {
 							MysqlFunctions.addBackpackData(name, plugin.playerData.get(name), inv);
 						} catch (final SQLException e) {
 							e.printStackTrace();
 						}
 					} else {
 						final String invString = RealisticBackpacks.NMS.inventoryToString(inv);
 						final File file = new File(plugin.getDataFolder() + File.separator + "userdata" + File.separator + name + ".yml");
 						final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 						config.set(plugin.playerData.get(name) + ".Inventory", invString);
 						try {
 							config.save(file);
 						} catch (final IOException e) {
 							e.printStackTrace();
 						}
 					}
 					plugin.playerData.remove(name);
 				}
 			});
 		} else if (plugin.adminFullView.containsKey(name)) {
 			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 				@Override
 				public void run() {
 					final String[] split = plugin.adminFullView.get(name).split(":");
 					if (plugin.isUsingMysql()) {
 						try {
 							MysqlFunctions.addBackpackData(split[0], split[1], inv);
 						} catch (final SQLException e) {
 							e.printStackTrace();
 						}
 					} else {
 						final String invString = RealisticBackpacks.NMS.inventoryToString(inv);
 						final File file = new File(plugin.getDataFolder() + File.separator + "userdata" + File.separator + split[1] + ".yml");
 						final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 						config.set(split[0] + ".Inventory", invString);
 						try {
 							config.save(file);
 						} catch (final IOException e) {
 							e.printStackTrace();
 						}
 					}
 					plugin.adminFullView.remove(name);
 				}
 			});
 		} else if (plugin.adminRestrictedView.contains(name)) {
 			plugin.adminRestrictedView.remove(name);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onInventoryClick(final InventoryClickEvent e) {
 		if (e.getWhoClicked() instanceof Player) {
 
 			final Player p = (Player) e.getWhoClicked();
 			final String name = p.getName();
 
 			if (plugin.adminRestrictedView.contains(name)) {
 				e.setCancelled(true);
 			}
 
 			final ItemStack curItem = e.getCurrentItem();
 			final Inventory otherInv = e.getView().getTopInventory();
 
 			if (curItem != null && otherInv != null) {
 
 				final boolean isInBackpack = plugin.playerData.containsKey(name);
 				final List<String> key = plugin.backpackData.get(plugin.playerData.get(name));
 
 				if (isInBackpack) {
 
 					if (key.get(16) != null && key.get(16).equalsIgnoreCase("true") && p.getItemOnCursor().getType() == Material.AIR) {
 						for (final String whitelist : plugin.backpackWhitelist.get(plugin.playerData.get(name))) {
 							if (whitelist == null) {
 								continue;
 							}
 							if (plugin.backpackItems.containsKey(whitelist)) {
								if (!curItem.isSimilar(plugin.backpackItems.get(whitelist))) {
 									e.setCancelled(true);
 									p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("cantPutItemInBackpack")));
 									return;
 								}
 							} else {
								if (!curItem.isSimilar(RBUtil.getItemstackFromString(whitelist))) {
 									e.setCancelled(true);
 									p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("cantPutItemInBackpack")));
 									return;
 								}
 							}
 						}
 					}
 
 					for (final String blacklist : plugin.backpackBlacklist.get(plugin.playerData.get(name))) {
 						if (blacklist == null) {
 							continue;
 						}
 						if (plugin.backpackItems.containsKey(blacklist)) {
							if (curItem.isSimilar(plugin.backpackItems.get(blacklist))) {
 								e.setCancelled(true);
 								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("cantPutItemInBackpack")));
 								return;
 							}
 						} else {
							if (curItem.isSimilar(RBUtil.getItemstackFromString(blacklist))) {
 								e.setCancelled(true);
 								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("cantPutItemInBackpack")));
 								return;
 							}
 						}
 					}
 				}
 
 				for (final String backpack : plugin.backpacks) {
 					if (curItem.isSimilar(plugin.backpackItems.get(backpack))) {
 						plugin.slowedPlayers.remove(name);
 						p.setWalkSpeed(0.2F);
 						e.setCurrentItem(RealisticBackpacks.NMS.addGlow(plugin.backpackItems.get(backpack)));
 						break;
 					}
 				}
 
 			}
 		}
 	}
 
 }
