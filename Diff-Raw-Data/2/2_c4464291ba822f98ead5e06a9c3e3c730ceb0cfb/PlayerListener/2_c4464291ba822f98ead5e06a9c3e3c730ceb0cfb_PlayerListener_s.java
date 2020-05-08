 package org.fatecrafters.plugins.listeners;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.fatecrafters.plugins.RealisticBackpacks;
 import org.fatecrafters.plugins.util.MysqlFunctions;
 import org.fatecrafters.plugins.util.RBUtil;
 
 public class PlayerListener implements Listener {
 
 	private final RealisticBackpacks plugin;
 
 	private final HashMap<String, String> deadPlayers = new HashMap<String, String>();
 	private float walkSpeedMultiplier = 0.0F;
 
 	public PlayerListener(final RealisticBackpacks plugin) {
 		this.plugin = plugin;
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.NORMAL)
	public void onRightClick(final PlayerInteractEvent e) {
 		final Action act = e.getAction();
 		final Player p = e.getPlayer();
 		final ItemStack item = p.getItemInHand();
 		final String name = p.getName();
 		if (item.hasItemMeta()) {
 			for (final String backpack : plugin.backpacks) {
 				final List<String> key = plugin.backpackData.get(backpack);
 				if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', key.get(3)))) {
 					if (plugin.isUsingPerms() && !p.hasPermission("rb." + backpack + ".use")) {
 						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("openBackpackPermError")));
 						continue;
 					}
 					final String openWith = key.get(15);
 					if (openWith != null) {
 						if (openWith.equalsIgnoreCase("left_click")) {
 							if (act.equals(Action.RIGHT_CLICK_AIR)) {
 								continue;
 							}
 							if (act.equals(Action.RIGHT_CLICK_BLOCK)) {
 								continue;
 							}
 						} else if (openWith.equalsIgnoreCase("right_click")) {
 							if (act.equals(Action.LEFT_CLICK_AIR)) {
 								continue;
 							}
 							if (act.equals(Action.LEFT_CLICK_BLOCK)) {
 								continue;
 							}
 						}
 					} else {
 						if (act.equals(Action.LEFT_CLICK_AIR)) {
 							continue;
 						}
 						if (act.equals(Action.LEFT_CLICK_BLOCK)) {
 							continue;
 						}
 					}
 					if (act.equals(Action.RIGHT_CLICK_BLOCK)) {
 						e.setCancelled(true);
 						p.updateInventory();
 					}
 					Inventory inv = null;
 					if (plugin.isUsingMysql()) {
 						try {
 							inv = MysqlFunctions.getBackpackInv(name, backpack);
 						} catch (final SQLException e1) {
 							e1.printStackTrace();
 						}
 						if (inv == null) {
 							inv = plugin.getServer().createInventory(p, Integer.parseInt(key.get(0)), ChatColor.translateAlternateColorCodes('&', key.get(3)));
 						}
 					} else {
 						final File file = new File(plugin.getDataFolder() + File.separator + "userdata" + File.separator + name + ".yml");
 						if (!file.exists()) {
 							try {
 								file.createNewFile();
 							} catch (final IOException e1) {
 								e1.printStackTrace();
 							}
 						}
 						final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 						if (config.getString(backpack + ".Inventory") == null) {
 							inv = plugin.getServer().createInventory(p, Integer.parseInt(key.get(0)), ChatColor.translateAlternateColorCodes('&', key.get(3)));
 						} else {
 							inv = RealisticBackpacks.NMS.stringToInventory(config.getString(backpack + ".Inventory"), key.get(3));
 						}
 					}
 					plugin.playerData.put(name, backpack);
 					p.openInventory(inv);
 					break;
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onDrop(final PlayerDropItemEvent e) {
 		final Player p = e.getPlayer();
 		final String name = p.getName();
 		final ItemStack item = e.getItemDrop().getItemStack();
 		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 			@Override
 			public void run() {
 				if (plugin.slowedPlayers.contains(name)) {
 					for (final String backpack : plugin.backpacks) {
 						if (plugin.backpackItems.get(backpack).equals(item)) {
 							plugin.slowedPlayers.remove(name);
 							plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
 								@Override
 								public void run() {
 									p.setWalkSpeed(0.2F);
 								}
 							});
 							break;
 						}
 					}
 				}
 			}
 		});
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPickup(final PlayerPickupItemEvent e) {
 		final ItemStack item = e.getItem().getItemStack();
 		final Player p = e.getPlayer();
 		final String name = p.getName();
 		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 			@Override
 			public void run() {
 				for (final String backpack : plugin.backpacks) {
 					if (!item.equals(plugin.backpackItems.get(backpack))) {
 						continue;
 					}
 					final List<String> key = plugin.backpackData.get(backpack);
 					if (!plugin.slowedPlayers.contains(name)) {
 						plugin.slowedPlayers.add(name);
 					}
 					plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
 						@Override
 						public void run() {
 							p.setWalkSpeed(Float.parseFloat(key.get(9)));
 						}
 					});
 					break;
 				}
 			}
 		});
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onMove(final PlayerMoveEvent e) {
 		final Player p = e.getPlayer();
 		final String name = p.getName();
 		if (plugin.slowedPlayers.contains(name)) {
 			return;
 		}
 
 		final Inventory inv = p.getInventory();
 		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 			@Override
 			public void run() {
 				final List<String> backpackList = new ArrayList<String>();
 				for (final String backpack : plugin.backpacks) {
 					final List<String> key = plugin.backpackData.get(backpack);
 					if (key.get(8).equals("true") && inv.contains(plugin.backpackItems.get(backpack))) {
 						backpackList.add(backpack);
 					}
 				}
 				final int listsize = backpackList.size();
 				if (listsize > 0) {
 					if (listsize > 1) {
 						if (plugin.isAveraging()) {
 							float average = 0;
 							for (final String backpack : backpackList) {
 								average += Float.parseFloat(plugin.backpackData.get(backpack).get(9));
 							}
 							walkSpeedMultiplier = average / listsize;
 						} else if (plugin.isAdding()) {
 							float sum = 0;
 							for (final String backpack : backpackList) {
 								sum += 0.2F - Float.parseFloat(plugin.backpackData.get(backpack).get(9));
 							}
 							walkSpeedMultiplier = 0.2F - sum;
 						} else {
 							final List<Float> floatList = new ArrayList<Float>();
 							for (final String backpack : backpackList) {
 								floatList.add(Float.parseFloat(plugin.backpackData.get(backpack).get(9)));
 							}
 							walkSpeedMultiplier = Collections.max(floatList);
 						}
 					} else if (listsize == 1) {
 						for (final String backpack : backpackList) {
 							walkSpeedMultiplier = Float.parseFloat(plugin.backpackData.get(backpack).get(9));
 						}
 					}
 					plugin.slowedPlayers.add(name);
 					plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
 						@Override
 						public void run() {
 							p.setWalkSpeed(walkSpeedMultiplier);
 						}
 					});
 				}
 			}
 		});
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onDeath(final PlayerDeathEvent e) {
 		final Player p = e.getEntity();
 		final String name = p.getName();
 		for (final String backpack : plugin.backpacks) {
 			if (!p.getInventory().contains(plugin.backpackItems.get(backpack))) {
 				continue;
 			}
 			p.setWalkSpeed(0.2F);
 			final List<String> key = plugin.backpackData.get(backpack);
 			if (key.get(5) != null && key.get(5).equals("true")) {
 				//Drop contents
 				Inventory binv = null;
 				if (plugin.isUsingMysql()) {
 					try {
 						binv = MysqlFunctions.getBackpackInv(name, backpack);
 					} catch (final SQLException e1) {
 						e1.printStackTrace();
 					}
 				} else {
 					final File file = new File(plugin.getDataFolder() + File.separator + "userdata" + File.separator + name + ".yml");
 					if (!file.exists()) {
 						continue;
 					}
 					final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 					if (config.getString(backpack + ".Inventory") == null) {
 						continue;
 					}
 					binv = RealisticBackpacks.NMS.stringToInventory(config.getString(backpack + ".Inventory"), key.get(3));
 				}
 				if (binv != null) {
 					for (final ItemStack item : binv.getContents()) {
 						if (item != null) {
 							p.getWorld().dropItemNaturally(p.getLocation(), item);
 						}
 					}
 				}
 				RBUtil.destroyContents(name, backpack);
 			}
 			if (key.get(4) != null && key.get(4).equals("true")) {
 				//Destroy contents
 				RBUtil.destroyContents(name, backpack);
 				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("contentsDestroyed")));
 			}
 			if (key.get(6) != null && key.get(6).equals("false")) {
 				//Drop backpack
 				e.getDrops().remove(plugin.backpackItems.get(backpack));
 			}
 			if (key.get(7) != null && key.get(7).equals("true")) {
 				deadPlayers.put(name, backpack);
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onRespawn(final PlayerRespawnEvent e) {
 		final Player p = e.getPlayer();
 		final String name = p.getName();
 		for (final String backpack : plugin.backpacks) {
 			final List<String> key = plugin.backpackData.get(backpack);
 			if (key.get(7) != null && key.get(7).equals("true") && deadPlayers.get(name) != null && deadPlayers.get(name).equals(backpack)) {
 				//Keep backpack
 				p.getInventory().addItem(plugin.backpackItems.get(backpack));
 				p.updateInventory();
 				deadPlayers.remove(name);
 			}
 		}
 	}
 
 }
