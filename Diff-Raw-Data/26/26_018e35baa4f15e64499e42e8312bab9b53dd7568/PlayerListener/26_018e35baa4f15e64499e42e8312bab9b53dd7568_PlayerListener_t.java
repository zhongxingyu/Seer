 package me.bluejelly.main.listeners;
 
 import java.io.File;
 import java.io.IOException;
 
 import me.bluejelly.main.GuildZ;
 import me.bluejelly.main.commands.CmdBank;
 import me.bluejelly.main.configs.ChunkConfig;
 import me.bluejelly.main.configs.GuildConfig;
 import me.bluejelly.main.configs.PlayerConfig;
import me.bluejelly.main.getters.Guild;
 import me.bluejelly.main.getters.GuildPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class PlayerListener implements Listener {
 	
 	static GuildZ main;
 	
 	
 	public PlayerListener(GuildZ instance)
 	{
 		main = instance;
 	}
 	
 	@EventHandler(ignoreCancelled=true)
 	public void onJoin(PlayerJoinEvent event)
 	{
 		Player player = event.getPlayer();
 		
 		GuildZ.playerChunk.put(player.getName(), player.getLocation().getChunk());
 		
 		if(!GuildPlayer.exists(player.getName()))
 		{
 			PlayerConfig.config.set(player.getName()+".isInGuild", false);
 			PlayerConfig.config.set(player.getName()+".guildName", "null");
 			PlayerConfig.config.set(player.getName()+".role", "MEMBER");
 			PlayerConfig.config.set(player.getName()+".title", "I am to sexy for you");
 			PlayerConfig.config.set(player.getName()+".chatmode", "PUBLIC");
 			PlayerConfig.config.set(player.getName()+".honor", 0.0);
 			PlayerConfig.config.set(player.getName()+".reputation", 0.0);
 			PlayerConfig.saveConfig();
 			GuildZ.console.sendMessage(ChatColor.GOLD+"Successfully created player information for " + event.getPlayer().getName() + ".");
 		}
 	
 	//ERROR
 	
 		String guildName = GuildPlayer.guildName(player.getName());
		player.sendMessage(ChatColor.RED+"["+ ChatColor.DARK_AQUA+guildName+ChatColor.RED+"] " + ChatColor.GOLD + Guild.getDescription(guildName));
 		
 	//ERROR
 	}
 	
 	@EventHandler(ignoreCancelled=true)
 	public void onRespawn(PlayerRespawnEvent event)
 	{
 		GuildZ.playerChunk.put(event.getPlayer().getName(), event.getPlayer().getLocation().getChunk());
 	}
 	
 	@EventHandler
 	public void onPvP(EntityDamageByEntityEvent event) {
 		
 		if(event.isCancelled()) return;
 		
 		if(event.getEntity() instanceof Player) {
 			Player player = (Player) event.getEntity();
 			if(PlayerConfig.config.contains(player.getName())) {
 				if(GuildConfig.config.getBoolean(PlayerConfig.config.getInt(player.getName()+".guildID") + ".peaceful")) {
 					player.sendMessage(ChatColor.AQUA + "This player is in a protected guild.");
 					event.setCancelled(true);
 				}
 			}
 		}
 		else if(event.getDamager() instanceof Player) {
 			Player player = (Player) event.getDamager();
 			if(PlayerConfig.config.contains(player.getName())) {
 				if(GuildConfig.config.getBoolean(PlayerConfig.config.getInt(player.getName()+".guildID") + ".peaceful")) {
 					player.sendMessage(ChatColor.AQUA + "You can't hurt other player while you're in a protected guild.");
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority=EventPriority.LOWEST)
 	public void OnInteract(PlayerInteractEvent event) {
 		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			if(
 			event.getClickedBlock().getType().equals(Material.CHEST) ||
 			event.getClickedBlock().getType().equals(Material.TRAPPED_CHEST) || 
 			event.getClickedBlock().getType().equals(Material.JUKEBOX) || 
 			event.getClickedBlock().getType().equals(Material.ANVIL) || 
 			event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE) || 
 			event.getClickedBlock().getType().equals(Material.ENDER_CHEST) || 
 			event.getClickedBlock().getType().equals(Material.FURNACE)) {
 			
 				Chunk loc = event.getClickedBlock().getLocation().getChunk();
 				FileConfiguration gConfig = GuildConfig.config;
 				FileConfiguration pConfig = PlayerConfig.config;
 				FileConfiguration cConfig = ChunkConfig.config;
 				
 				if(cConfig.contains(loc.toString())) {
 					int guildID = pConfig.getInt(event.getPlayer().getName()+".guildID");
 					
 					if(!pConfig.contains(event.getPlayer().getName())) {
 						event.setCancelled(true);
 						return;
 					}
 					
 					if(!gConfig.get(guildID+".name").equals(cConfig.get(loc.toString()+".owner"))) {
 						event.setCancelled(true);
 					}
 					
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOWEST)
 	public void OnBlockBreak(BlockBreakEvent event) {
 		Chunk loc = event.getBlock().getLocation().getChunk();
 		FileConfiguration gConfig = GuildConfig.config;
 		FileConfiguration pConfig = PlayerConfig.config;
 		FileConfiguration cConfig = ChunkConfig.config;
 		
 		if(cConfig.contains(loc.toString())) {
 			int guildID = pConfig.getInt(event.getPlayer().getName()+".guildID");
 			
 			if(!pConfig.contains(event.getPlayer().getName())) {
 				event.setCancelled(true);
 				return;
 			}
 			
 			if(!gConfig.get(guildID+".name").equals(cConfig.get(loc.toString()+".owner"))) {
 				event.setCancelled(true);
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority=EventPriority.LOWEST)
 	public void OnBlockPlaced(BlockPlaceEvent event) {
 		Chunk loc = event.getBlock().getLocation().getChunk();
 		FileConfiguration gConfig = GuildConfig.config;
 		FileConfiguration pConfig = PlayerConfig.config;
 		FileConfiguration cConfig = ChunkConfig.config;
 		
 		if(cConfig.contains(loc.toString())) {
 			int guildID = pConfig.getInt(event.getPlayer().getName()+".guildID");
 			
 			if(!pConfig.contains(event.getPlayer().getName())) {
 				event.setCancelled(true);
 				return;
 			}
 			
 			if(!gConfig.get(guildID+".name").equals(cConfig.get(loc.toString()+".owner"))) {
 				event.setCancelled(true);
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void OnInventoryClosed(InventoryCloseEvent event) {
 		if(event.getPlayer() instanceof Player) {
 			Player player = (Player) event.getPlayer();
 			
 			String guildName = null;
 			guildName = PlayerConfig.config.getString(player.getName() + ".guildName");
 			
 			File guildBank = new File("banks", guildName+".yml");
 			
 			try {
 				CmdBank.saveToYaml(event.getInventory(), guildBank);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	
 }
