 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share  to copy, distribute and transmit the work
     to Remix  to adapt the work
 
  Under the following conditions:
     Attribution  You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial  You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver  Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain  Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights  In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice  For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
 */
 
 package alshain01.FlagsPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.Villager;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import alshain01.Flags.Flags;
 import alshain01.Flags.Flag;
 import alshain01.Flags.ModuleYML;
 import alshain01.Flags.Registrar;
 import alshain01.Flags.Director;
 import alshain01.Flags.area.Area;
 
 /**
  * Flags - Damage
  * Module that adds damage flags to the plug-in Flags.
  * 
  * @author Alshain01
  */
 public class FlagsPlayer extends JavaPlugin {
 	/**
 	 * Called when this module is enabled
 	 */
 	@Override
 	public void onEnable(){
 		PluginManager pm =  Bukkit.getServer().getPluginManager();
 
 		if(!pm.isPluginEnabled("Flags")) {
 		    this.getLogger().severe("Flags was not found. Shutting down.");
 		    pm.disablePlugin(this);
 		}
 		
 		// Connect to the data file
 		ModuleYML dataFile = new ModuleYML(this, "flags.yml");
 		
 		// Register with Flags
 		Registrar flags = Flags.instance.getRegistrar();
 		for(String f : dataFile.getModuleData().getConfigurationSection("Flag").getKeys(false)) {
 			ConfigurationSection data = dataFile.getModuleData().getConfigurationSection("Flag." + f);
 			
 			// We don't want to register flags that aren't supported.
 			// It would just muck up the help menu.
 			// Null value is assumed to support all versions.
 			String api = data.getString("MinimumAPI");  
 			if(api != null && !Flags.instance.checkAPI(api)) { continue; }
 			
 			// The description that appears when using help commands.
 			String desc = data.getString("Description");
 			
 			// The default message players get while in the area.
 			String area = data.getString("AreaMessage");
 			
 			// The default message players get while in an world.
 			String world = data.getString("WorldMessage");
 			
 			// Register it!  (All flags are defaulting to true in this module)
 			// Be sure to send a plug-in name or group description for the help command!
 			// It can be this.getName() or another string.
 			flags.register(f, desc, true, "Player", area, world);
 		}
 		
 		// Load plug-in events and data
 		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
 		if(Flags.instance.checkAPI("1.5.2")) {
 			Bukkit.getServer().getPluginManager().registerEvents(new PlayerConsumeListener(), this);
 		}
 	}
 	
 	/*
 	 * The event handlers for the flags we created earlier
 	 */
 	public class PlayerListener implements Listener{
 		private void sendMessage(Player player, Flag flag, Area area) {
 			player.sendMessage(area.getMessage(flag)
 					.replaceAll("\\{Player\\}", player.getName()));
 		}
 
 		private boolean isDenied(Player player, Flag flag, Area area) {
 			if(flag.hasBypassPermission(player) 
 					|| area.getTrustList(flag).contains(player.getName())) { return false; }
 					
 			if (!area.getValue(flag, false)) {
 				sendMessage(player, flag, area);
 				return true;
 			}
 			return false;
 		}
 		
 		/*
 		 * Handler for Teleportation
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerTeleport(PlayerTeleportEvent e) {
 			// We don't want to cancel plugin teleporting
 			// Because that would mess with flight, allowleave, and allowexit.
 			// Portals are handled in another event.
 			if(e.getCause() == TeleportCause.PLUGIN
 					|| e.getCause() == TeleportCause.END_PORTAL
 					|| e.getCause() == TeleportCause.NETHER_PORTAL) { return; }
 			
 			Player player = e.getPlayer();
 			Area tpFrom = Director.getAreaAt(e.getFrom());
 			Area tpTo = Director.getAreaAt(e.getTo());
 			
 			Registrar flags = Flags.instance.getRegistrar();
 		
 			// Teleport out of area
 			Flag flag = flags.getFlag("AllowTpOut");
 			if (!flag.hasBypassPermission(player) 
 					&& !tpFrom.getTrustList(flag).contains(player.getName())) {
 				if (!tpFrom.getValue(flag, false)) {
 					e.setCancelled(true);
 					sendMessage(player, flag, tpFrom);
 					return;
 				}
 			}
 
 			// Teleport into area
 			flag = flags.getFlag("AllowTpIn");
 			if (!flag.hasBypassPermission(player) 
 					&& !tpTo.getTrustList(flag).contains(player.getName())) {
 				if (!tpTo.getValue(flag, false)) {
 					e.setCancelled(true);
 					sendMessage(player, flag, tpTo);
 					return;
 				}
 			}
 		}
 		
 		/*
 		 * Handler for Right Clicking Entities, (Trading, Breeding)
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
 			Player player = e.getPlayer();
 			Entity entity = e.getRightClicked();
 			Area area = Director.getAreaAt(player.getLocation());
 			Registrar flags = Flags.instance.getRegistrar();
 			Flag flag = null;
 			
 			if (entity instanceof Villager) {
 				flag = flags.getFlag("Trading");
 				// Villager trading
 				if (flag.hasBypassPermission(player) 
 						|| area.getTrustList(flag).contains(player.getName())) { return; }
 				
 				if(!area.getValue(flag, false)) {
 					e.setCancelled(true);
 					sendMessage(player, flag, area);
 				}
 			} else if (entity instanceof Animals) {
 				// 1. This is not a "taming" flag, so let it be tamed.
 				// 2. This is not a "feeding" flag, so let it be fed.
 				if ((entity instanceof Tameable) 
 						&& (!((Tameable)entity).isTamed()
 						|| ((LivingEntity)entity).getHealth() != ((LivingEntity)entity).getMaxHealth())) { return; }
 				
 				flag = flags.getFlag("Breeding");
 				if (flag.hasBypassPermission(player) 
 						|| area.getTrustList(flag).contains(player.getName())) { return; }
 				
 				if (!area.getValue(flag, false)) {
 					e.setCancelled(true);
 					sendMessage(player, flag, area);
 				}
 			}
 		}
 		
 		/*
 		 * Handler for Leveling Up
 		 */
 		@EventHandler(priority = EventPriority.MONITOR)
 		private void onPlayerLevelChange(PlayerLevelChangeEvent e) {
 			Player player = e.getPlayer();
 			Area area = Director.getAreaAt(player.getLocation());
 			Flag flag = Flags.instance.getRegistrar().getFlag("Level");
 			
 			if(flag.hasBypassPermission(player) 
 					|| area.getTrustList(flag).contains(player.getName())) { return; }
 
 			if (!area.getValue(flag, false)) {
 				if(e.getNewLevel() > e.getOldLevel()) {
 					// You can't actually stop this event
 					// but you can make it ineffective by reducing a players level before they gain it back
 					player.setLevel(player.getLevel() - (e.getNewLevel() - e.getOldLevel()));
 					player.setExp(0.9999f);
 				} 
 			}
 		}
 		
 		/*
 		 * Handler for gaining Experience
 		 */
 		@EventHandler(priority = EventPriority.MONITOR)
 		private void onPlayerExpChange(PlayerExpChangeEvent e) {
 			Player player = e.getPlayer();
 			Area area = Director.getAreaAt(player.getLocation());
 			Flag flag = Flags.instance.getRegistrar().getFlag("Experience");
 			
 			if(flag.hasBypassPermission(player) 
 					|| area.getTrustList(flag).contains(player.getName())) { return; }
 					
 			if (!area.getValue(flag, false)) {
 				if (e.getAmount() > 0) {
 					e.setAmount(0);
 				}
 			}
 		}
 		
 		/*
 		 * Handler for picking up items
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerPickupItem(PlayerPickupItemEvent e){
 			Player player = e.getPlayer();
 			Area area = Director.getAreaAt(player.getLocation());
 			Flag flag = Flags.instance.getRegistrar().getFlag("ItemPickup");
 					
 			if(flag.hasBypassPermission(player) 
 					|| area.getTrustList(flag).contains(player.getName())) { return; }
 
 			if (!area.getValue(flag, false)) {
 				e.setCancelled(true);
 			}
 		}
 		
 		/*
 		 * Handler for Right Clicking Objects
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerInteract(PlayerInteractEvent e) {
 			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial() == Material.TRAP_DOOR) {
 				e.setCancelled(isDenied(e.getPlayer(),
 						Flags.instance.getRegistrar().getFlag("TrapDoor"),
 						Director.getAreaAt(e.getPlayer().getLocation())));
 			}
 		}
 		
 		/*
 		 * Handler for Fishing
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerFish(PlayerFishEvent e) {
 			e.setCancelled(isDenied(e.getPlayer(),
 					Flags.instance.getRegistrar().getFlag("Fishing"),
 					Director.getAreaAt(e.getPlayer().getLocation())));
 		}
 		
 		/*
 		 * Handler for entering a portal
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerPortal(PlayerPortalEvent e) {
 			e.setCancelled(isDenied(e.getPlayer(),
 					Flags.instance.getRegistrar().getFlag("Portal"),
 					Director.getAreaAt(e.getPlayer().getLocation())));
 		}
 		
 		/*
 		 * Handler for Commands
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
 			e.setCancelled(isDenied(e.getPlayer(),
 					Flags.instance.getRegistrar().getFlag("Commands"),
 					Director.getAreaAt(e.getPlayer().getLocation())));
 		}
 
 		/*
 		 * Handler for Dropping Items
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerDropItem(PlayerDropItemEvent e){
 			e.setCancelled(isDenied(e.getPlayer(),
 					Flags.instance.getRegistrar().getFlag("ItemDrop"),
 					Director.getAreaAt(e.getPlayer().getLocation())));
 		}
 	}
 	
 	/*
 	 * Handler for Eating
 	 * Kept in isolated class due to version support.
 	 */
 	private class PlayerConsumeListener implements Listener {
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerItemConsume(PlayerItemConsumeEvent e) {
 			Player player = e.getPlayer();
 			Flag flag = Flags.instance.getRegistrar().getFlag("Eat");
 			Area area = Director.getAreaAt(e.getPlayer().getLocation());
 			
 			if(flag.hasBypassPermission(player) 
 					|| area.getTrustList(flag).contains(player.getName())) { return; }
 					
 			if (!area.getValue(flag, false)) {
 				player.sendMessage(area.getMessage(flag)
						.replaceAll("\\{Player\\}", player.getDisplayName()));
 				e.setCancelled(true);
 			}
 		}
 	}
 }
