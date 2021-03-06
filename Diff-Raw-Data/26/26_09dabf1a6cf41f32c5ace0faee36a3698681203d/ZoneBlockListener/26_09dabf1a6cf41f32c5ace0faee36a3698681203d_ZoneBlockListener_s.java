 package info.tregmine.listeners;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import info.tregmine.Tregmine;
 import info.tregmine.api.TregminePlayer;
 import info.tregmine.api.Zone;
 import info.tregmine.currency.Wallet;
 import info.tregmine.database.ConnectionPool;
 import info.tregmine.quadtree.Point;
 import info.tregmine.zones.Lot;
 import info.tregmine.zones.ZoneWorld;
 import info.tregmine.zones.ZonesPlugin;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class ZoneBlockListener implements Listener 
 {
 	private final ZonesPlugin plugin;
 	private final Tregmine tregmine;
 
 	public ZoneBlockListener(ZonesPlugin instance) 
 	{
 		this.plugin = instance;
 		this.tregmine = instance.tregmine;
 	}
 
 	public void mineForTreg(BlockBreakEvent event) {
 
 		if(event.isCancelled()) {
 			return;
 		}
 
 		TregminePlayer player = tregmine.getPlayer(event.getPlayer());
 
 		for (ItemStack item : event.getBlock().getDrops() ) {
 
 			Connection conn = null;
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
 			try {
 				conn = ConnectionPool.getConnection();
 
 				String sql = "SELECT value FROM items_destroyvalue WHERE itemid = ?";
 
 				stmt = conn.prepareStatement(sql);
 				stmt.setLong(1, item.getTypeId());
 				stmt.execute();
 
 				rs = stmt.getResultSet();
 
 				if (rs.first() ) {
 					event.setCancelled(true);
 					event.getBlock().setType(Material.AIR);
 
 					ItemStack drop = new ItemStack(item.getType(), item.getAmount(), item.getData().getData());
 
 					ItemMeta meta = drop.getItemMeta();
 					item.setType(Material.AIR);
 
 					if (this.tregmine.blockStats.isPlaced(event.getBlock())) {
 						List<String> lore = new ArrayList<String>();
 						lore.add(ChatColor.GREEN + "MINED");
 						lore.add(ChatColor.WHITE + "by: " + player.getChatName() );
 						lore.add(ChatColor.WHITE + "Value: "+ ChatColor.GOLD + 0 + ChatColor.WHITE + " Treg" );
 						meta.setLore(lore);					
 						drop.setItemMeta(meta);
 						event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), drop);
//						player.sendMessage("Placed");
 					} else {
 						List<String> lore = new ArrayList<String>();
 						lore.add(ChatColor.GREEN + "MINED");
						lore.add(ChatColor.WHITE + "by: " + player.getChatName() );
 						lore.add(ChatColor.WHITE + "Value: "+ ChatColor.GOLD + rs.getInt("value") + ChatColor.WHITE + " Treg" );
 						meta.setLore(lore);					
 						drop.setItemMeta(meta);
 						event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), drop);
//						player.sendMessage("not placed");
 						Wallet wallet = new Wallet (player.getName());
 						wallet.add(rs.getInt("value"));
 					}
 				}
 
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			} finally {
 				if (rs != null) {
 					try { rs.close(); } catch (SQLException e) {} 
 				}
 				if (stmt != null) {
 					try { stmt.close(); } catch (SQLException e) {}
 				}
 				if (conn != null) {
 					try { conn.close(); } catch (SQLException e) {}
 				}
 			}
 
 
 		}
 
 
		
 	}
	
 	@EventHandler
 	public void onBlockBreak (BlockBreakEvent event) 
 	{
 		TregminePlayer player = tregmine.getPlayer(event.getPlayer());
 		if (player.isAdmin()) {
 			return;
 		}
 
 		ZoneWorld world = plugin.getWorld(player.getWorld());
 
 		Block block = event.getBlock();
 		Location location = block.getLocation();
 		Point pos = new Point(location.getBlockX(), location.getBlockZ());
 
 		Zone currentZone = player.getCurrentZone();
 		if (currentZone == null || !currentZone.contains(pos)) {
 			currentZone = world.findZone(pos);
 			player.setCurrentZone(currentZone);
 		}
 
 		if (currentZone != null) {
 			Zone.Permission perm = currentZone.getUser(player.getName());
 
 			Lot lot = world.findLot(pos);
 			if (lot != null) {
 				if (perm != Zone.Permission.Owner && !lot.isOwner(player.getName())) {
 					player.sendMessage(ChatColor.RED + "[" + currentZone.getName() + "] " + 
 							"You are not allowed to break blocks in lot " + lot.getName() + ".");
 					event.setCancelled(true);
 					return;
 				}
 				mineForTreg(event);
 				return;
 			}
 
 			// if everyone is allowed to build in this zone...
 			if (currentZone.getDestroyDefault()) {
 				// ...the only people that can't build are those that are banned
 				if (perm != null && perm == Zone.Permission.Banned) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "[" + currentZone.getName() + "] " + 
 							"You are banned from " + currentZone.getName() + ".");	    			
 				}
 			}
 
 			// if this zone has limited building privileges...
 			else {
 				// ...we only allow builders and owners to make changes.
 				if (perm == null || (perm != Zone.Permission.Maker && perm != Zone.Permission.Owner)) {
 					player.setFireTicks(50);
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "[" + currentZone.getName() + "] " + 
 							"You are not allowed to break blocks in " + currentZone.getName() + ".");
 				}
 			}
 		}
 		mineForTreg(event);
 
 	}
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		TregminePlayer player = tregmine.getPlayer(event.getPlayer());
 		if (player.isAdmin()) {
 			return;
 		}
 
 		ZoneWorld world = plugin.getWorld(player.getWorld());
 
 		Block block = event.getBlock();
 		Location location = block.getLocation();
 		Point pos = new Point(location.getBlockX(), location.getBlockZ());
 
 		Zone currentZone = player.getCurrentZone();
 		if (currentZone == null || !currentZone.contains(pos)) {
 			currentZone = world.findZone(pos);
 			player.setCurrentZone(currentZone);
 		}
 
 		if (currentZone != null) {
 			Zone.Permission perm = currentZone.getUser(player.getName());
 
 			Lot lot = world.findLot(pos);
 			if (lot != null) {
 				if (perm != Zone.Permission.Owner && !lot.isOwner(player.getName())) {
 					player.sendMessage(ChatColor.RED + "[" + currentZone.getName() + "] " + 
 							"You are not allowed to break blocks in lot " + lot.getName() + ".");
 					event.setCancelled(true);
 					return;
 				}
 
 				// we should only get here if the event is allowed, in which case we don't need
 				// any more checks.
 				return;
 			}
 
 			// if everyone is allowed to build in this zone...
 			if (currentZone.getPlaceDefault()) {
 				// ...the only people that can't build are those that are banned
 				if (perm != null && perm == Zone.Permission.Banned) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "[" + currentZone.getName() + "] " + 
 							"You are banned from " + currentZone.getName() + ".");	    			
 				}
 			} 
 			// if this zone has limited building privileges...
 			else {
 				// ...we only allow builders and owners to make changes.
 				if (perm == null || (perm != Zone.Permission.Maker && perm != Zone.Permission.Owner)) {
 					player.setFireTicks(50);
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "[" + currentZone.getName() + "] " +
 							"You are not allowed to place blocks in " + currentZone.getName() + ".");
 				}
 			}
 		}
 	}
 }
