 package com.nidefawl.Achievements;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.*;
 
 import com.nidefawl.Achievements.Messaging.AchMessaging;
 import com.nidefawl.Stats.StatsSettings;
 
 @SuppressWarnings("unused")
 public class AchievementPlayerListener extends PlayerListener {
 
 	public HashMap<String, Float> distWalked = new HashMap<String, Float>();
 	protected Achievements plugin;
 
 	public AchievementPlayerListener(Achievements plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Called when a player leaves a server
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerQuit(PlayerQuitEvent event) {
 
 	}
 
 	/**
 	 * Called when a player sends a chat message
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerChat(PlayerChatEvent event) {
 
 	}
 
 	/**
 	 * Called when a player attempts to use a command
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 
 	}
 
 	/**
 	 * Called when a player attempts to move location in a world
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerMove(PlayerMoveEvent event) {
 		
 		if (event.isCancelled())
 			return;
 		
 		if (plugin.permission.has(event.getPlayer(), "achievements.check") && plugin.permission.has(event.getPlayer(), "achievements.discover")){
 			plugin.playerWorldNew = event.getPlayer().getWorld();
 			plugin.playerDistNew = event.getPlayer().getLocation();
 			if (plugin.playerWorldOld == null) {
 				plugin.playerWorldOld = plugin.playerWorldNew;
 				return;
 			}
 			if (plugin.playerDistOld == null) {
 				plugin.playerDistOld = plugin.playerDistNew;
 				return;
 			}	
 			if(plugin.playerWorldNew != plugin.playerWorldOld) {
 				plugin.playerDistOld = event.getPlayer().getLocation();
 				plugin.playerDistNew = event.getPlayer().getLocation();
				plugin.playerWorldOld = plugin.playerWorldNew;
				return;
 			}
 			if(plugin.playerDistNew.distance(plugin.playerDistOld) > Achievements.RangeDiscoveryMode) {
 				//AchMessaging.send(event.getPlayer(), ChatColor.LIGHT_PURPLE + "Move move :D");
 				plugin.playerDistOld = plugin.playerDistNew;
				plugin.playerWorldOld = plugin.playerWorldNew;
 				plugin.checkDiscover(event.getPlayer());
 			}
 		}
 		else if (!plugin.permission.has(event.getPlayer(), "achievements.check")){
 			if (StatsSettings.debugOutput) {
					Achievements.LogInfo("When using achievements.discover, the player needs achievements.check , too!");		
 			}
 		}
 		else{
 			return;
 		}
 	}
 
 	/**
 	 * Called when a player attempts to teleport to a new location in a world
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 
 	}
 
 	/**
 	 * Called when a player uses an item
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent  event) {
 
 	}
 
 	/**
 	 * Called when a player attempts to log in to the server
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 
 	}
 
 	/**
 	 * Called when a player plays an animation, such as an arm swing
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerAnimation(PlayerAnimationEvent event) {
 
 	}
 
 	/**
 	 * Called when a player throws an egg and it might hatch
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
 
 	}
 
 	/**
 	 * Called when a player drops an item from their inventory
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 
 	}
 
 	/**
 	 * Called when a player gets kicked from the server
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerKick(PlayerKickEvent event) {
 
 	}
 
 	/**
 	 * Called when a player respawns
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 
 	}
 
 	/**
 	 * Called when a player attempts to log in to the server
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerLogin(PlayerLoginEvent event) {
 	}
 
 	/**
 	 * Called when a player picks an item up off the ground
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 
 	}
 
 	/**
 	 * Called when a player opens an inventory
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onInventoryOpen(PlayerInventoryEvent event) {
 	}
 
 	/**
 	 * Called when a player changes their held item
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onItemHeldChange(PlayerItemHeldEvent event) {
 	}
 
 	/**
 	 * Called when a player toggles sneak mode
 	 * 
 	 * @param event
 	 *            Relevant event details
 	 */
 	@Override
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
 	}
 }
