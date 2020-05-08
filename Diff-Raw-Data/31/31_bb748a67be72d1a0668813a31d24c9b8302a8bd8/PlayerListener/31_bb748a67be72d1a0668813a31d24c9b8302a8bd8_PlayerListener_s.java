 /**
  *  Name: PlayerListener.java
  *  Date: 14:36:09 - 9 okt 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Description:
  *  
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.crownconquest.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import me.lucasemanuel.crownconquest.Main;
 import me.lucasemanuel.crownconquest.utils.ConsoleLogger;
 import me.lucasemanuel.crownconquest.utils.WorldGuardHook;
 
 public class PlayerListener implements Listener {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	public PlayerListener(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger(instance, "PlayerListener");
 		
 		logger.debug("Initiated!");
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		
 		Player player = event.getEntity();
 		
 		plugin.getTeamManager().removePlayer(player);
 		
 		if(player.getInventory().getHelmet() != null
 				&& player.getInventory().getHelmet().getType().equals(Material.GOLD_HELMET)) {
 			
 			plugin.getServer().broadcastMessage(
 					"Lag "
 					+ ChatColor.LIGHT_PURPLE + plugin.getTeamManager().getTeamNameFromPlayer(player)
 					+ ChatColor.WHITE + " har tappat kronan!"
 			);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		
 		event.setRespawnLocation(plugin.getLocationManager().getSpectatorSpawn());
 		
 		plugin.getTeamManager().removePlayer(event.getPlayer());
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if(!plugin.getStatus()) {
 			plugin.getTeamManager().addToConfiguredTeam(event.getPlayer());
 			plugin.getTeamManager().teleportToTeamSpawn(event.getPlayer());
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerDisconnect(PlayerQuitEvent event) {
 		
 		plugin.getTeamManager().removePlayer(event.getPlayer());
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onEntityDamage(EntityDamageEvent event) {
 		
 		if(event.getEntity() instanceof Player 
 				&& !plugin.getStatus()) {
 			
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		
 		Player player = event.getPlayer();
 		Block  block  = event.getClickedBlock();
 		
 		if(block.getType().equals(Material.WALL_SIGN) 
 				|| block.getType().equals(Material.SIGN_POST) 
 				&& !plugin.getStatus()) {
 			
 			String teamname = plugin.getTeamManager().getTeamNameFromSign(block.getLocation());
 			
 			if(teamname != null) {
 				if(plugin.getTeamManager().addToTeam(player, teamname)) {
 					plugin.getTeamManager().teleportToTeamSpawn(player);
 				}
 			}
 			else if(player.hasPermission("crownconquest.signs.registerteamsign")){
 				
 				Sign sign = (Sign) block.getState();
 				
 				if(sign.getLine(0).equalsIgnoreCase("[lag]") 
 						&& sign.getLine(1) != "" 
 						&& sign.getLine(1) != null) {
 					
 					teamname = sign.getLine(1);
 					plugin.getTeamManager().registerTeamSign(sign, teamname);
 				}
 			}
 		}
 		else if(block.getType().equals(Material.CHEST)) {
 			plugin.getChestManager().randomizeChest(((Chest) block.getState()));
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if(plugin.getStatus()) {
 			
 			Player player = event.getPlayer();
 			
 			if(WorldGuardHook.isInRegion(player.getLocation(), "goal")
 					&& player.getInventory().getHelmet() != null 
 					&& player.getInventory().getHelmet().getType().equals(Material.GOLD_HELMET)) {
 				
 				plugin.getServer().broadcastMessage(
 						ChatColor.LIGHT_PURPLE + plugin.getTeamManager().getTeamNameFromPlayer(player) 
 						+ ChatColor.WHITE + " har kommit i ml!"
 				);
 				
 				player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
 				player.updateInventory();
 				
 				for(PotionEffect potion : player.getActivePotionEffects()) {
 					player.removePotionEffect(potion.getType());
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerPickup(PlayerPickupItemEvent event) {
 		
 		final Player player = event.getPlayer();
 		final Item   item   = event.getItem();
 		
 		if(item.getItemStack().getType().equals(Material.GOLD_HELMET)) {
 			
 			if(player.getInventory().getHelmet() != null
 					&& player.getInventory().getHelmet().getType().equals(Material.GOLD_HELMET)) {
 				event.setCancelled(true);
 				return;
 			}
 			
 			player.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
 			
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 72000000, 3));
 			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 72000000, 3));
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 72000000, 1));
 			
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				public void run() {
 					player.getInventory().remove(Material.GOLD_HELMET);
 					player.updateInventory();
 				}
 			}, 1L);
 			
 			plugin.getServer().broadcastMessage(
					plugin.getTeamManager().getTeamNameFromPlayer(player) 
 					+ ChatColor.WHITE + " har plockat upp kronan!"
 			);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerInventoryClick(InventoryClickEvent event) {
 		
 		if(event.getSlotType().equals(SlotType.ARMOR)) {
 			
 			if(event.getCurrentItem().getType().equals(Material.GOLD_HELMET) 
 					|| event.getCurrentItem().getType().equals(Material.PUMPKIN)) {
 				
 				event.setCancelled(true);
 			}
 		}
 	}
 }
