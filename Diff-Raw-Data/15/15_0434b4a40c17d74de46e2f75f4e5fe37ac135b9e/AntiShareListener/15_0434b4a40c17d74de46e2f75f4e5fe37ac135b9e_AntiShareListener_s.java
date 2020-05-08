 package me.turt2live;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class AntiShareListener implements Listener {
 
 	private Plugin	plugin;
 
 	public AntiShareListener(Plugin p) {
 		plugin = p;
 	}
 
 	public void init() {
 		Bukkit.getPluginManager().registerEvents(this, plugin);
 	}
 
 	@EventHandler(event = PlayerDropItemEvent.class, priority = EventPriority.LOWEST)
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		// System.out.println("onDrop | " + event.getPlayer() + " | " + event.getItemDrop().getItemStack().getTypeId());
 		Player player = event.getPlayer();
 		if (player != null && !event.isCancelled()) {
 			boolean itemIsBlocked = false;
 			ItemStack item = event.getItemDrop().getItemStack();
 			itemIsBlocked = AntiShare.isBlocked(plugin.getConfig().getString("events.drop_item"), item.getTypeId());
 			if (plugin.getConfig().getBoolean("other.only_if_creative") && itemIsBlocked) {
 				if (player.getGameMode() == GameMode.CREATIVE) if (player.hasPermission("AntiShare.drop") && !player.hasPermission("AntiShare.allow.drop")) {
 					event.setCancelled(true);
 					player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.drop_item")));
 				}
 			} else if (player.hasPermission("AntiShare.drop") && !player.hasPermission("AntiShare.allow.drop") && itemIsBlocked) {
 				event.setCancelled(true);
 				player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.drop_item")));
 			}
 		}
 	}
 
 	@EventHandler(event = PlayerInteractEvent.class, priority = EventPriority.LOWEST)
	//TODO: Check for eggs
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		// System.out.println("onInteract | " + event.getPlayer() + " | " + event.getClickedBlock().getTypeId());
 		Player player = event.getPlayer();
 		if (player != null && !event.isCancelled() && event.getClickedBlock() != null) {
 			boolean itemIsBlocked = false;
 			int item = event.getClickedBlock().getTypeId();
 			itemIsBlocked = AntiShare.isBlocked(plugin.getConfig().getString("events.interact"), item);
 			if (plugin.getConfig().getBoolean("other.only_if_creative") && itemIsBlocked) {
 				if (player.getGameMode() == GameMode.CREATIVE) if (player.hasPermission("AntiShare.interact") && !player.hasPermission("AntiShare.allow.interact")) {
 					event.setCancelled(true);
 					player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.interact")));
 				}
 			} else if (player.hasPermission("AntiShare.interact") && !player.hasPermission("AntiShare.allow.interact") && itemIsBlocked) {
 				event.setCancelled(true);
 				player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.interact")));
 			}
 			//Egg check
 			if (plugin.getConfig().getBoolean("other.allow_eggs") == false) {
 				boolean filter = false;
 				if (plugin.getConfig().getBoolean("other.only_if_creative") && player.getGameMode() == GameMode.CREATIVE) filter = true;
 				else if (!plugin.getConfig().getBoolean("other.only_if_creative")) filter = true;
				if (filter) {
 					ItemStack possibleEgg = event.getItem();
					if (possibleEgg.getTypeId() == 383) {
 						event.setCancelled(true);
 						player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.eggs")));
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(event = EntityDeathEvent.class, priority = EventPriority.LOWEST)
 	public void onEntityDeath(EntityDeathEvent event) {
 		// System.out.println("onDeath | " + event.getEntity());
 		if (event.getEntity() instanceof Player) {
 			Player player = (Player) event.getEntity();
 			if (player != null) for (ItemStack item : event.getDrops()) {
 				boolean itemIsBlocked = false;
 				itemIsBlocked = AntiShare.isBlocked(plugin.getConfig().getString("events.death"), item.getTypeId());
 				if (plugin.getConfig().getBoolean("other.only_if_creative") && itemIsBlocked) {
 					if (player.getGameMode() == GameMode.CREATIVE) if (player.hasPermission("AntiShare.death") && !player.hasPermission("AntiShare.allow.death")) {
 						player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.death")));
 						item.setAmount(0);
 					}
 				} else if (player.hasPermission("AntiShare.death") && !player.hasPermission("AntiShare.allow.death") && itemIsBlocked) {
 					player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.death")));
 					item.setAmount(0);
 				}
 			}
 		}
 	}
 
 	@EventHandler(event = BlockPlaceEvent.class, priority = EventPriority.LOWEST)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		// System.out.println("onPlace | " + event.getPlayer() + " | " + event.getBlockPlaced().getTypeId());
 		Player player = event.getPlayer();
 		if (player != null && !event.isCancelled()) {
 			boolean itemIsBlocked = false;
 			int item = event.getBlockPlaced().getTypeId();
 			itemIsBlocked = AntiShare.isBlocked(plugin.getConfig().getString("events.block_place"), item);
 			if (plugin.getConfig().getBoolean("other.only_if_creative") && itemIsBlocked) {
 				if (player.getGameMode() == GameMode.CREATIVE) if (player.hasPermission("AntiShare.place") && !player.hasPermission("AntiShare.allow.place")) {
 					event.setCancelled(true);
 					player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.block_place")));
 				}
 			} else if (player.hasPermission("AntiShare.place") && !player.hasPermission("AntiShare.allow.place") && itemIsBlocked) {
 				event.setCancelled(true);
 				player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.block_place")));
 			}
 		}
 	}
 
 	@EventHandler(event = BlockBreakEvent.class, priority = EventPriority.LOWEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		// System.out.println("onBreak | " + event.getPlayer() + " | " + event.getBlock().getTypeId());
 		Player player = event.getPlayer();
 		if (player != null && !event.isCancelled()) {
 			boolean itemIsBlocked = false;
 			int item = event.getBlock().getTypeId();
 			itemIsBlocked = AntiShare.isBlocked(plugin.getConfig().getString("events.block_break"), item);
 			if (plugin.getConfig().getBoolean("other.only_if_creative") && itemIsBlocked) {
 				if (player.getGameMode() == GameMode.CREATIVE) if (player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break")) {
 					event.setCancelled(true);
 					player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.block_break")));
 				}
 			} else if (player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break") && itemIsBlocked) {
 				event.setCancelled(true);
 				player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.block_break")));
 			}
 		}
 	}
 
 	@EventHandler(event = BlockDamageEvent.class, priority = EventPriority.LOWEST)
 	public void onBlockDamage(BlockDamageEvent event) {
 		// System.out.println("onBreak | " + event.getPlayer() + " | " + event.getBlock().getTypeId());
 		Player player = event.getPlayer();
 		if (player != null && !event.isCancelled()) {
 			boolean itemIsBlocked = false;
 			int item = event.getBlock().getTypeId();
 			itemIsBlocked = AntiShare.isBlocked(plugin.getConfig().getString("events.block_break"), item);
 			if (plugin.getConfig().getBoolean("other.only_if_creative") && itemIsBlocked) {
 				if (player.getGameMode() == GameMode.CREATIVE) if (player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break")) {
 					event.setCancelled(true);
 					player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.block_break")));
 				}
 			} else if (player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break") && itemIsBlocked) {
 				event.setCancelled(true);
 				player.sendMessage(AntiShare.addColor(plugin.getConfig().getString("messages.block_break")));
 			}
 		}
 	}
 }
