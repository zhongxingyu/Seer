 package Staartvin.InventoryDropChance;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import Staartvin.InventoryDropChance.updater.Updater;
 
 public class IDCEvents implements Listener {
 
 	InventoryDropChance plugin;
 
 	// Amount of items a player had
 	protected HashMap<String, Integer> count = new HashMap<String, Integer>();
 
 	// Items that will be given back on respawn
 	protected HashMap<String, List<ItemStack>> items = new HashMap<String, List<ItemStack>>();
 
 	// Is the player dead or not?
 	protected HashMap<String, Boolean> dead = new HashMap<String, Boolean>();
 
 	// The amount of EXP to give back on respawn
 	protected HashMap<String, Integer> ExpToKeep = new HashMap<String, Integer>();
 
 	public IDCEvents(InventoryDropChance plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	protected void onPlayerDeath(PlayerDeathEvent event) {
 
 		Player player = event.getEntity().getPlayer();
 
 		dead.put(player.getName(), true);
 
 		if (!plugin.wHandlers.worldIsEnabled(player.getWorld().getName()))
 			return;
 
 		count.put(player.getName(), player.getInventory().getContents().length);
 
 		// Run EXP check
 		plugin.methods.doEXPCheck(player, event);
 
 		if (player.hasPermission("idc.keepallitems")) {
 			List<ItemStack> itemStackArray = new ArrayList<ItemStack>();
 			List<ItemStack> armourStackArray = new ArrayList<ItemStack>();
 
 			// Give full inventory back
 			for (int i = 0; i < player.getInventory().getContents().length; i++) {
 				ItemStack item = player.getInventory().getContents()[i];
 
 				itemStackArray.add(item);
 			}
 
 			// Give all armour back
 			for (ItemStack armour : player.getInventory().getArmorContents()) {
 				armourStackArray.add(armour);
 			}
 
 			// Clear drops
 			event.getDrops().clear();
 
 			// Save items and armour for other methods
 			items.put(player.getName(), itemStackArray);
 			plugin.methods.armour.put(player.getName(), armourStackArray);
 
 			return;
 		}
 
 		String checkFirst = plugin.getConfig().getString(
 				"Groups." + plugin.files.getGroup(player) + ".check first",
 				"save");
 
 		if (checkFirst.equalsIgnoreCase("save")) {
 
 			// Run save check first
 			items.put(player.getName(),
 					plugin.methods.doSaveCheck(player, event.getDrops()));
 
 			// Run delete check afterwards
 			// Remove deleted items from items so they are not given back
 			List<ItemStack> givenItems = items.get(player.getName());
 			List<ItemStack> deletedItems = plugin.methods.doDeleteCheck(player,
 					givenItems);
 
 			givenItems.removeAll(deletedItems);
 
 			// Update given items
 			items.put(player.getName(), givenItems);
 
 			// Remove the deleted items from the drops so they are not dropped as well
 			event.getDrops().removeAll(deletedItems);
 
 		} else if (checkFirst.equalsIgnoreCase("delete")) {
 
 			// Run delete check first
 			List<ItemStack> deletedItems = plugin.methods.doDeleteCheck(player,
 					event.getDrops());
 
 			// Remove deleted items from dropped items
 			event.getDrops().removeAll(deletedItems);
 
 			// Run save check afterwards
 			items.put(player.getName(),
 					plugin.methods.doSaveCheck(player, event.getDrops()));
 		}
 	}
 
 	@EventHandler
 	protected void onPlayerRespawn(PlayerRespawnEvent event) {
 
 		final Player player = event.getPlayer();
 		final String playerName = player.getName();
 
 		if (dead.get(playerName) == null) {
 			dead.put(playerName, false);
 		}
 		if (!dead.get(playerName))
 			return;
 
 		dead.put(playerName, false);
 
 		if (!plugin.wHandlers.worldIsEnabled(player.getWorld().getName()))
 			return;
 
 		// Give player saved EXP
 		if (plugin.files.getExpLossUsage(player)) {
 			if (!player.hasPermission("idc.keepxp")) {
 				if (ExpToKeep.get(playerName) == null)
 					return;
 
 		plugin.getServer().getScheduler()
 						.runTaskLater(plugin, new Runnable() {
 
 							public void run() {
								player.giveExp(ExpToKeep.get(playerName));
 								ExpToKeep.put(playerName, null);
 							}
 						}, 3L);
 			}
 		}
 
 		plugin.methods.returnItems(player, items.get(playerName));
 
 		// Set everything to null so GC can do his work
 		count.put(playerName, null);
 		items.put(playerName, null);
 		plugin.methods.armour.put(playerName, null);
 		plugin.methods.whitelistedItems.put(playerName, null);
 		plugin.methods.randomUsed.put(playerName, null);
 	}
 
 	@EventHandler
 	protected void onServerQuit(PlayerQuitEvent event) {
 
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 
 		if (dead.get(playerName) == null) {
 			dead.put(playerName, false);
 		}
 		if (!dead.get(playerName))
 			return;
 	}
 	
 	@EventHandler
 	protected void onServerJoin(PlayerJoinEvent event) {
 
 		final Player player = event.getPlayer();
 		
 		plugin.getUpdaterStatus();
 		
 		if (player.hasPermission("idc.noticeonupdate")) {
 			if (plugin.updater != null && plugin.updater.getResult().equals(Updater.UpdateResult.UPDATE_AVAILABLE)) {
 				plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
 
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						player.sendMessage(ChatColor.GREEN + plugin.updater.getLatestVersionString() + ChatColor.GOLD + " is now available for download!");
 					}
 					
 				}, 10L);
 			}
 		}
 	}
 }
