 package muCkk.DeathAndRebirth.listener;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import muCkk.DeathAndRebirth.DAR;
 import muCkk.DeathAndRebirth.ghost.Ghosts;
 import muCkk.DeathAndRebirth.ghost.Shrines;
 import muCkk.DeathAndRebirth.messages.Errors;
 import muCkk.DeathAndRebirth.messages.Messages;
 import net.minecraft.server.Packet29DestroyEntity;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PListener implements Listener {
 
 	private Ghosts ghosts;
 	private Shrines shrines;
 	private DAR plugin;
 	private double flySpeed;
 	
 	private ArrayList<String> checkList;
 	
 	public PListener(DAR plugin, Ghosts ghosts, Shrines shrines) {
 		this.plugin = plugin;
 		this.flySpeed = plugin.getConfig().getDouble("FLY_SPEED");
 		this.ghosts = ghosts;
 		this.shrines = shrines;
 		checkList = new ArrayList<String>();
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		if(ghosts.isGhost(event.getPlayer())) event.setCancelled(true);
 	}
 	
 	/**
 	 * Looks for new Players and adds them to the list
 	 */
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerJoin(final PlayerJoinEvent event) {
 		final Player player = event.getPlayer();
 		if(!ghosts.existsPlayer(player)) {
 			ghosts.newPlayer(player);
 		}
 	// if dead players join
 		if(ghosts.isGhost(player)) {
 			ghosts.setDisplayName(player, true);
 			if(plugin.getConfig().getBoolean("INVISIBILITY")) ghosts.vanish(player);
 			if (plugin.getConfig().getBoolean("SPOUT_ENABLED")) {
 				plugin.darSpout.setDeathOptions(player, plugin.getConfig().getString("GHOST_SKIN"));
 			}
 			plugin.message.send(player, Messages.playerDied);
 		}
 		else if(plugin.getConfig().getBoolean("INVISIBILITY")) hideGhosts(player); 
 		
 	// version checking
 	// in it's own thread because it takes some time and would stop the rest of the world to load
 		if(player.isOp()) {
 			new Thread() {
 				public void run() {
 					try {
 						URL versionURL = new URL("http://dl.dropbox.com/u/12769915/minecraft/plugins/DAR/version.txt");
 						BufferedReader reader = new BufferedReader(new InputStreamReader(versionURL.openStream()));
 						
 						String line = reader.readLine();
 						if (!plugin.getDescription().getVersion().equalsIgnoreCase(line)) {
 							plugin.message.sendChat(player, Messages.newVersion, ": " + line);
 						}
 						reader.close();
 					} catch (MalformedURLException e) {
 						// versionURL
 						Errors.readingURL();
 						e.printStackTrace();
 					} catch (IOException e) {
 						// versionURL.openstream()
 						Errors.openingURL();
 						e.printStackTrace();
 					}
 				}
 			}.start();
 		}
 	}
 	
 	// if otherplayer is a ghost make him invisible
 	private void hideGhosts(Player player) {
 		Player[] onlinePlayers = player.getServer().getOnlinePlayers();
 		for (Player otherPlayer : onlinePlayers) {
 			if (otherPlayer == player || !ghosts.isGhost(otherPlayer)) continue;
 			((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(((CraftPlayer) otherPlayer).getEntityId()));
 		}
 	}
 	
 	/**
 	 * Checks if ghosts are allowed to chat
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerChat(PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		if(ghosts.isGhost(player) && !plugin.getConfig().getBoolean("GHOST_CHAT")) {
 			plugin.message.send(player, Messages.ghostNoChat);
 			event.setCancelled(true);
 		}
 	}
 	
 	/**
 	 * Sets the players respawn location to their location of death
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 	// check if the world is enabled
 		if(!plugin.getConfig().getBoolean(player.getWorld().getName())) {
 			return;
 		}
 		if(ghosts.isGhost(player)) {
 			// reverse spawning
 			Location nearestShrine = shrines.getNearestShrineSpawn(player.getLocation());
 			Location corpse = ghosts.getLocation(player);
 			if (!plugin.getConfig().getBoolean("CORPSE_SPAWNING")) {
 				Location loc = ghosts.getBoundShrine(player);
 				
 				if (loc != null) event.setRespawnLocation(loc);
 				else if(nearestShrine != null) event.setRespawnLocation(nearestShrine);
 				giveGhostCompass(player, corpse);
 			}
 			// corpse spawning
 			else {
 				event.setRespawnLocation(corpse);
 				if (nearestShrine != null) giveGhostCompass(player, nearestShrine);
 			}
 			plugin.message.send(player, Messages.playerDied);
 	//  spout related
 			if (plugin.getConfig().getBoolean("SPOUT_ENABLED")) {
 				plugin.darSpout.setDeathOptions(player, plugin.getConfig().getString("GHOST_SKIN"));
 			}
 			ghosts.setDisplayName(player, true);
 		// invisibility
 			if (plugin.getConfig().getBoolean("INVISIBILITY")) ghosts.vanish(player);
 		}
 	}
 	
 	private void giveGhostCompass(final Player player, final Location loc) {
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				
 				ItemStack compass = new ItemStack(345);
 				compass.setAmount(1);
 				player.getInventory().addItem(compass);
 				player.setCompassTarget(loc);
 			}
 		}.start();
 	}
 	
 	/**
 	 * Prevents dead players from picking up items
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		// check if the world is enabled
 		if(!plugin.getConfig().getBoolean(event.getPlayer().getWorld().getName())) {
 			return;
 		}
 		if(ghosts.isGhost(event.getPlayer())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	/**
 	 * Flying for ghosts
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent event) {		
 		final Player player = event.getPlayer();			
 		
 		if(ghosts.isGhost(player)) {
 		// inform ghost if he is on a shrine
 			if(!checkList.contains(player.getName())) {
 				if ((event.getFrom().getBlockX() != event.getTo().getBlockX()) || (event.getFrom().getBlockY() != event.getTo().getBlockY()) || (event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
 					if(!checkList.contains(player.getName())) {
 						if(shrines.isOnShrine(player)) { 
 							plugin.message.send(player, Messages.shrineArea);
 							checkList.add(player.getName());
 							// 5 second delay before that player gets checked again
 							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 								@Override
 								public void run() {
 									checkList.remove(player.getName());
 								}
 							}, 100L);
 						}
 						
 					}
 				}
 			}
 		// flying for ghosts
 			if(player.isSneaking() &&  plugin.getConfig().getBoolean("FLY")) {		
 				player.setVelocity(player.getLocation().getDirection().multiply(flySpeed));
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Players try to bind their soul to a shrine
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		
 		
 		// check if the world is enabled
 		if(!plugin.getConfig().getBoolean(player.getWorld().getName())) {
 			return;
 		}
 		
 
 	// *** ghost interactions ***
 		if (ghosts.isGhost(player)) {
 			Block block = event.getClickedBlock();
 		// chest, furnace
 			try {
 				Material type = block.getType(); 			
 				if(			type.equals(Material.FURNACE)
 						||	type.equals(Material.CHEST)) {
 					plugin.message.send(player, Messages.cantDoThat);
 					event.setCancelled(true);
 					return;
 				}
 			}catch (NullPointerException e) {
 				// Material = null
 			}
 		// resurrection
 			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				Location locDeath = ghosts.getLocation(player);
 			// reverse spawning
 				if(!plugin.getConfig().getBoolean("CORPSE_SPAWNING")) {
 					if (event.getClickedBlock().getLocation().distance(locDeath) < 3) ghosts.resurrect(player);
 					else {
 						String shrine = shrines.getClose(player.getLocation());
 						if (shrine != null) {
 							ghosts.resurrect(player);
 							ghosts.removeItems(player);
 							player.setHealth(plugin.getConfig().getInt("HEALTH"));
 						}
 					}
 				}
 			// corpse spawning
 				else {
 					String shrine = shrines.getClose(player.getLocation());
 					if (shrine != null) ghosts.resurrect(player);
 					else {
 						if (event.getClickedBlock().getLocation().distance(locDeath) < 3) {
 							ghosts.resurrect(player);
 							ghosts.removeItems(player);
 							player.setHealth(plugin.getConfig().getInt("HEALTH"));
 						}
 					}
 				}
 			return;
 			}
 			
 			// normal interactions		
 			
 			if (plugin.getConfig().getBoolean("BLOCK_GHOST_INTERACTION")) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 	// *** selection mode for creating shrines ****************
 		if(player.getName().equalsIgnoreCase(shrines.getSelPlayer()) && shrines.isSelModeEnable() && player.getItemInHand().getTypeId() == 280) {
 			if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				shrines.setSel1(event.getClickedBlock().getLocation());
 				player.sendMessage("Position 1 set");
 			}
 			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				shrines.setSel2(event.getClickedBlock().getLocation());
 				player.sendMessage("Position 2 set");
 			}
 		}
 		
 	// *** shrine is clicked ***
 		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			String shrine = shrines.getClose(player.getLocation());
 			if (shrine != null) {
 			// check if soul can be bound at this shrine
 				if (!shrines.checkBinding(shrine, player.getWorld().getName())) {
 					plugin.message.send(player, Messages.cantBindSoul);
 					return;
 				}
 				Block clickedBlock = event.getClickedBlock();
 				Location boundShrine = ghosts.getBoundShrine(player);
 			// soul is bound
 				if (boundShrine != null) {
 					Location bshrine = shrines.getNearestShrine(ghosts.getBoundShrine(player));
 			// unbinding
 					if((shrines.getNearestShrine(player.getLocation() ) ).distance(bshrine) < 2) {
 						ghosts.unbind(player);
 						plugin.message.send(player, Messages.unbindSoul);
 						return;
 					}
 				}
 			// binding
 				if(shrines.isShrineArea(shrine, clickedBlock)) {
 					ghosts.bindSoul(player);
 					plugin.message.send(player, Messages.soulNowBound);
 				}
 			}
 			return;
 		}
 		
 	// *** getting the name of a shrine ******************************
 		if (player.isOp() && event.getAction() == Action.LEFT_CLICK_BLOCK && player.getItemInHand().getTypeId() == 280) {
 			String shrine = shrines.getClose(player.getLocation());
 			if (shrine != null) {
 				player.sendMessage("Shrine: "+shrine);
 			}
 			return;
 		}
 	}
 	
 	/**
 	 * checks if the player enters a new world
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		Player player = event.getPlayer();
 		ghosts.worldChange(player);
 	}
 	
 	/**
 	 * checks if the player enters a new world
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerPortal(PlayerPortalEvent event) {
 		Player player = event.getPlayer();
 		ghosts.worldChange(player);
 	}
 }
