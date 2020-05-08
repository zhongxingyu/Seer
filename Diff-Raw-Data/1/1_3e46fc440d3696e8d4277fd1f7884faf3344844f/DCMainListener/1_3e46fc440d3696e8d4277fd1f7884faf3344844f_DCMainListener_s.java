 package pgDev.bukkit.DisguiseCraft.listeners;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.NetServerHandler;
 import net.minecraft.server.Packet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
 import org.bukkit.event.player.*;
 
 import pgDev.bukkit.DisguiseCraft.Disguise;
 import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
 import pgDev.bukkit.DisguiseCraft.api.PlayerUndisguiseEvent;
 import pgDev.bukkit.DisguiseCraft.injection.DCNetServerHandler;
 import pgDev.bukkit.DisguiseCraft.injection.SpoutHandleProducer;
 
 public class DCMainListener implements Listener {
 	final DisguiseCraft plugin;
 	
 	public DCMainListener(final DisguiseCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		// Track player movements in order to synchronize their disguise
 		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
 			plugin.sendMovement(event.getPlayer(), null, event.getPlayer().getVelocity(), event.getTo());
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		
 		// Injection
 		if (plugin.pluginSettings.disguisePVP && player instanceof CraftPlayer) {
 			EntityPlayer entity = ((CraftPlayer)player).getHandle();
 			entity.netServerHandler.disconnected = true;
 			NetServerHandler handler;
 			if (plugin.spoutEnabled()) {
 				handler = SpoutHandleProducer.getHandle(entity.server, entity.netServerHandler.networkManager, entity);
 			} else {
 				handler = new DCNetServerHandler(entity.server, entity.netServerHandler.networkManager, entity);
 			}
 			entity.server.networkListenThread.a(handler);
 		}
 		
 		// Show disguises to newly joined players
 		plugin.showWorldDisguises(player);
 		
 		// If he was a disguise-quitter, tell him
 		if (plugin.disguiseQuitters.contains(player.getName())) {
 			event.getPlayer().sendMessage(ChatColor.RED + "You were undisguised because you left the server.");
 			plugin.disguiseQuitters.remove(player.getName());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		
 		// Undisguise them because they left
 		if (plugin.disguiseDB.containsKey(player.getName())) {
 			plugin.unDisguisePlayer(player);
 			plugin.disguiseQuitters.add(player.getName());
 		}
 		
 		// Undisguise others
 		plugin.halfUndisguiseAllToPlayer(player);
 	}
 	
 	@EventHandler
 	public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
 		// World Change is like a join
 		plugin.showWorldDisguises(event.getPlayer());
 		
 		// Handle disguise wearer going through a portal
 		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
 			Player disguisee = event.getPlayer();
 			Disguise disguise = plugin.disguiseDB.get(disguisee.getName());
 			
 			// Packets
 			Packet killPacket = disguise.getEntityDestroyPacket();
     		Packet killListPacket = disguise.getPlayerInfoPacket(disguisee, false);
     		Packet revivePacket = disguise.getMobSpawnPacket(disguisee.getLocation());
     		Packet revivePlayerPacket = disguise.getPlayerSpawnPacket(disguisee.getLocation(), (short) disguisee.getItemInHand().getTypeId());
 			Packet reviveListPacket = disguise.getPlayerInfoPacket(disguisee, true);
     		
 			// Remove his disguise from the old world
 			if (killListPacket == null) {
 				plugin.undisguiseToWorld(event.getFrom(), disguisee, killPacket);
 			} else {
 				plugin.undisguiseToWorld(event.getFrom(), disguisee, killPacket, killListPacket);
 			}
 			
 			if (!disguise.hasPermission(disguisee)) {
 				// Pass the event
 				PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(disguisee);
 				plugin.getServer().getPluginManager().callEvent(ev);
 				if (!ev.isCancelled()) {
 					plugin.unDisguisePlayer(disguisee);
 					disguisee.sendMessage(ChatColor.RED + "You've been undisguised because you do not have permissions to wear that disguise in this world.");
 					return;
 				}
 			}
 			
 			// Show the disguise to the people in the new world
 			if (disguise.isPlayer()) {
 				plugin.disguiseToWorld(disguisee.getWorld(), disguisee, revivePlayerPacket, reviveListPacket);
 			} else {
 				plugin.disguiseToWorld(disguisee.getWorld(), disguisee, revivePacket);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onTarget(EntityTargetEvent event) {
 		if (!event.isCancelled()) {
 			if (event.getTarget() instanceof Player) {
 				Player player = (Player) event.getTarget();
 				if (plugin.disguiseDB.containsKey(player.getName())) {
 					if (plugin.hasPermissions(player, "disguisecraft.notarget")) {
 						if (plugin.hasPermissions(player, "disguisecraft.notarget.strict")) {
 							event.setCancelled(true);
 						} else {
 							if (!plugin.disguiseDB.get(player.getName()).isPlayer() && (event.getReason() == TargetReason.CLOSEST_PLAYER || event.getReason() == TargetReason.RANDOM_TARGET)) {
 								event.setCancelled(true);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPickup(PlayerPickupItemEvent event) {
 		if (!event.isCancelled()) {
 			if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
 				Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
 				if (disguise.data != null && disguise.data.contains("nopickup")) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 }
