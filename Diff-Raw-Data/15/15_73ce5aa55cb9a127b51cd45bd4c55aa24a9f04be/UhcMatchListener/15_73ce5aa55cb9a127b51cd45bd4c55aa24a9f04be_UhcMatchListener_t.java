 package com.martinbrook.tesseractuhc;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import org.bukkit.ChatColor;
 
 public class UhcMatchListener implements Listener {
 	private UhcMatch m;
 
 	public UhcMatchListener(UhcMatch m) {
 		this.m = m;
 	}
 	
 
 	@EventHandler
 	public void onJoin(PlayerJoinEvent e) {
 		m.setVanish(e.getPlayer());
 	}
 
 
 	@EventHandler
 	public void onQuit(PlayerQuitEvent e) {
 		m.setLastLogoutLocation(e.getPlayer().getLocation());
 	}
 	
 	@EventHandler
 	public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
 		if (m.isChatMuted() && !e.getPlayer().isOp())
 			e.setCancelled(true);
 		
 		// ops get gold text automatically
 		if (e.getPlayer().isOp()) {
 			e.setMessage(ChatColor.GOLD + e.getMessage());
 		}
 		
 	}
 	
 	@EventHandler
 	public void onPlayerComandPreprocessEvent(PlayerCommandPreprocessEvent e) {
 		if (m.isChatMuted() && !e.getPlayer().isOp() && e.getMessage().toLowerCase().startsWith("/me"))
 			e.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onLogin(PlayerLoginEvent e) {
 		// If player not allowed to log in, do nothing
 		if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
 		
 		// If we are in the pre-launch period, do nothing
 		if (m.getMatchPhase() == MatchPhase.PRE_MATCH) return;
 		
 		// If match is over, put player in creative, do nothing else
 		if (m.getMatchPhase() == MatchPhase.POST_MATCH) {
 			e.getPlayer().setGameMode(GameMode.CREATIVE);
 			return;
 		}
 
 		// If player is op, do nothing
 		if (e.getPlayer().isOp()) return;
 		
 		// Get a uhcplayer if possible
 		UhcPlayer up = m.getUhcPlayer(e.getPlayer());
 		
 		// If the match has not yet started, try to launch the player if necessary
 		if (m.getMatchPhase() == MatchPhase.LAUNCHING) {
 			if (up != null) {
 				// Player is in the match, make sure they are launched
 				m.launch(up);
 			} else {
 				// Player isn't in the match, so make sure they log in at spawn
 				e.getPlayer().teleport(m.getStartingWorld().getSpawnLocation());
 			}
 			return;
 		}
 
 		
 		// Match is in progress.
 
 		// If player was not launched, don't allow them in.
 		if (up == null || !up.isLaunched()) {
 			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The match has already started");
 			return;
 		}
 		
 		// If player has died, don't allow them in, if deathban is in effect.
 		if (m.getDeathban() && up.isDead()) {
 			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Dead players cannot rejoin!");
 			return;
 		}
 	}
 	
 	
 		
 	/**
 	 * Handle death events; add bonus items, if any.
 	 * 
 	 * @param pDeath
 	 */
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent e){
 		Player p = e.getEntity();
 		
 		// If it's a pvp kill, drop bonus items
 		if (p.getKiller() != null) {
 			ItemStack bonus = m.getKillerBonus();
 			if (bonus != null)
 				e.getDrops().add(bonus);
 		}
 		
 		// Make death message red
 		String msg = e.getDeathMessage();
 		e.setDeathMessage(ChatColor.RED + msg);
 		
 		// Save death point
 		if (msg.indexOf("fell out of the world") == -1)
 			m.setLastDeathLocation(p.getLocation());
 		
 		// Handle the death
 		UhcPlayer up = m.getUhcPlayer(p);
 		if (up != null && up.isLaunched() && !up.isDead() && m.getMatchPhase() == MatchPhase.MATCH)
 			m.handlePlayerDeath(up);
 
 	}
 
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent e) {
 		// Only do anything if match is in progress
 		if (m.getMatchPhase() != MatchPhase.MATCH) return;
 		
 		Player p = e.getPlayer();
 		
 		// If they're a dead UHC player, put them into adventure mode and make sure they respawn at overworld spawn
 		UhcPlayer up = m.getUhcPlayer(p);
 		
 		if (up != null) {
 			if (up.isDead()) {
 				p.setGameMode(GameMode.ADVENTURE);
 				e.setRespawnLocation(m.getStartingWorld().getSpawnLocation());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent e) {
 		if (e.getBlock().getType() == Material.STONE) {
 			m.doMiningFatigue(e.getPlayer(), e.getBlock().getLocation().getBlockY());
 		}
 	}
 	
 	@EventHandler
 	public void onInteractEntityEvent(PlayerInteractEntityEvent e) {
 		Player p = e.getPlayer();
 		if (!p.isOp()) return;
 
 		Entity clicked = e.getRightClicked();
 
 		if (clicked.getType() == EntityType.PLAYER) {
 			m.showInventory(p, (Player) clicked);
 		}
 	}
 	
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
 		if (e.getPlayer().isOp() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerDropItem(PlayerDropItemEvent e) {
 		if (e.getPlayer().isOp() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityTarget(EntityTargetEvent e) {
 		Entity target = e.getTarget();
 		if (target != null && target.getType() == EntityType.PLAYER) {
 			if (((Player) target).isOp() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityDamage(EntityDamageEvent e) {
 		// Only interested in players taking damage
 		if (e.getEntityType() != EntityType.PLAYER) return;
 		
		// Only interested if match is in progress. Cancel damage if not.
		if (m.getMatchPhase() != MatchPhase.MATCH) {
			e.setCancelled(true);
			return;
		}
 		
 		// Only interested in registered players
 		UhcPlayer up = m.getUhcPlayer((Player) e.getEntity());
 		if (up == null) return;
 		
 		m.sendNotification(new DamageNotification(up, e.getDamage(), e.getCause()));
 		
 	}
 	
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
 		// Only interested in players taking damage
 		if (e.getEntityType() != EntityType.PLAYER) return;
 		
		// Only interested if match is in progress. Cancel damage if not.
		if (m.getMatchPhase() != MatchPhase.MATCH) {
			e.setCancelled(true);
			return;
		}
 		
 		// Only interested in registered players
 		UhcPlayer up = m.getUhcPlayer((Player) e.getEntity());
 		if (up == null) return;
 		
 		m.sendNotification(new DamageNotification(up, e.getDamage(), e.getCause(), e.getDamager()));
 		
 	}
 }
