 package me.arno.blocklog.listeners;
 
 import java.util.List;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import me.arno.blocklog.OrderByDate;
 import me.arno.blocklog.WandSettings;
 import me.arno.blocklog.WandSettings.ResultType;
 import me.arno.blocklog.logs.BlockEntry;
 import me.arno.blocklog.logs.ChestEntry;
 import me.arno.blocklog.logs.DataEntry;
 import me.arno.blocklog.logs.InteractionEntry;
 import me.arno.blocklog.search.BlockSearch;
 import me.arno.blocklog.search.ChestSearch;
 import me.arno.blocklog.search.InteractionSearch;
 import me.arno.blocklog.util.Util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class WandListener extends BlockLogListener {
 
 	public void getBlockEdits(Player player, Location location) {
 		try {
 			player.sendMessage(ChatColor.YELLOW + "Block History " + ChatColor.BLUE + "[ID: " + location.getBlock().getTypeId() + " Name: " + location.getBlock().getType().name() + "] [" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]" + ChatColor.DARK_GRAY + " ------------------------");
			player.sendMessage(ChatColor.GRAY + Util.addSpaces("Name", 80) + Util.addSpaces("Action", 90) + "Details");
 
 			WandSettings wandSettings = plugin.wandSettings.get(player.getName());
 			int maxResults = wandSettings.getMaxResults();
 			int since = wandSettings.getSince();
 			int until = wandSettings.getUntil();
 
 			Connection conn = getDatabaseManager().getConnection();
 
 			ArrayList<DataEntry> logs = new ArrayList<DataEntry>();
 			
 			if (wandSettings.getResultType() == ResultType.BLOCKS || wandSettings.getResultType() == ResultType.ALL) {
 				BlockSearch blockSearch = new BlockSearch(conn);
 				blockSearch.setLocation(location);
 				blockSearch.setLimit(maxResults);
 				blockSearch.setDate(since, until);
 				blockSearch.setUseLocation(true);
 
 				logs.addAll(blockSearch.getResults());
 			}
 
 			if (wandSettings.getResultType() == ResultType.INTERACTIONS || wandSettings.getResultType() == ResultType.ALL) {
 				InteractionSearch interactionSearch = new InteractionSearch(conn);
 				interactionSearch.setLocation(location);
 				interactionSearch.setLimit(maxResults);
 				interactionSearch.setDate(since, until);
 
 				logs.addAll(interactionSearch.getResults());
 			}
 
 			if (wandSettings.getResultType() == ResultType.CHESTS || wandSettings.getResultType() == ResultType.ALL) {
 				ChestSearch chestSearch = new ChestSearch(conn);
 				chestSearch.setLocation(location);
 				chestSearch.setLimit(maxResults);
 				chestSearch.setDate(since, until);
 
 				logs.addAll(chestSearch.getResults());
 			}
 			
 			conn.close();
 			
 			Collections.sort(logs, new OrderByDate());
 			
 			List<DataEntry> dataEntries = logs;
 			
 			if(logs.size() > maxResults)
 				dataEntries = logs.subList(0, maxResults - 1);
 			
 			for(DataEntry data : dataEntries) {
 				String playerName = null;
 				String name = null;
 				String action = null;
 				String date = null;
 				
 				if(data instanceof BlockEntry) {
 					BlockEntry block = (BlockEntry) data;
 
 					playerName = (block.getPlayer() == null || block.getPlayer() == "Environment") ? block.getEntity() : block.getPlayer();
 					name = Material.getMaterial((block.getType().isCreateLog() ? block.getBlock() : block.getOriginalBlock())).name();
					action = block.getType().name();
 					date = " [" + Util.getDate(block.getDate()) + "]";
 				} else if(data instanceof InteractionEntry) {
 					InteractionEntry interaction = (InteractionEntry) data;
 
 					playerName = interaction.getPlayer();
 					name = Material.getMaterial(interaction.getBlock()).name();
					action = interaction.getType().name();
 					date = Util.getDate(interaction.getDate());
 				} else if(data instanceof ChestEntry) {
 					ChestEntry chest = (ChestEntry) data;
 					
 					playerName = chest.getPlayer();
					name = chest.getItem().getType().name() + " (" + chest.getItem().getAmount() + ")";
 					action = chest.getType().name();
 					date = Util.getDate(chest.getDate());
 				}
 				
				player.sendMessage(Util.addSpaces(ChatColor.GOLD + playerName, 85) + Util.addSpaces(ChatColor.DARK_RED + action, 105) + ChatColor.AQUA + name + " " + date);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			player.sendMessage("An unexpected NPE occured while using the wand");
 			e.printStackTrace();
 		}
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
		if (plugin.wandSettings.containsKey(player.getName())
				&& player.getItemInHand().getType() == getSettingsManager()
						.getWand()) {
 			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				getBlockEdits(player, event.getClickedBlock().getLocation());
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		if (!getSettingsManager().getWand().isBlock())
 			return;
 
		if (plugin.wandSettings.containsKey(player.getName())
				&& player.getItemInHand().getType() == getSettingsManager()
						.getWand()) {
 			getBlockEdits(player, event.getBlock().getLocation());
 			event.setCancelled(true);
 		}
 	}
 }
