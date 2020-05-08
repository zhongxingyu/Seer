 package com.martinbrook.uhctools;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import org.bukkit.ChatColor;
 
 public class UhcToolsListener implements Listener {
 	private UhcTools t;
 
 	public UhcToolsListener(UhcTools t) {
 		this.t = t;
 	}
 	
 	
 	@EventHandler
 	public void onQuit(PlayerQuitEvent e) {
 		t.setLastLogoutLocation(e.getPlayer().getLocation());
 	}
 	
 	@EventHandler
 	public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
 		if (t.isChatMuted() && !e.getPlayer().isOp())
 			e.setCancelled(true);
 		
 		// ops get gold text automatically
 		if (e.getPlayer().isOp()) {
 			e.setMessage(ChatColor.GOLD + e.getMessage());
 		}
 		
 	}
 	
 	@EventHandler
 	public void onPlayerComandPreprocessEvent(PlayerCommandPreprocessEvent e) {
 		if (t.isChatMuted() && !e.getPlayer().isOp() && e.getMessage().toLowerCase().startsWith("/me"))
 			e.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onLogin(PlayerLoginEvent e) {
 		// If player not allowed to log in, do nothing
 		if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
 		
 		// If we are in the pre-launch period, do nothing
 		if (!t.getLaunchingPlayers()) return;
 		
 		// If match is over, do nothing
 		if (t.isMatchEnded()) return;
 
 		
 		// Get a uhcplayer if possible
 		UhcPlayer up = t.getUhcPlayer(e.getPlayer());
 		
 		// If the match has not yet started, try to launch the player if necessary
 		if (!t.isMatchStarted()) {
 			if (up != null) {
 				// Player is in the match, make sure they are launched
 				t.launch(up);
 			} else {
 				// Player isn't in the match, so make sure they log in at spawn
 				e.getPlayer().teleport(t.world.getSpawnLocation());
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
		if (t.getDeathban() && up.isDead()) {
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
 			ItemStack bonus = t.getKillerBonus();
 			if (bonus != null)
 				e.getDrops().add(bonus);
 		}
 		
 		// Make death message red
 		String msg = e.getDeathMessage();
 		e.setDeathMessage(ChatColor.RED + msg);
 		
 		// Save death point
 		if (msg.indexOf("fell out of the world") == -1)
 			t.setLastDeathLocation(p.getLocation());
 		
 		// Handle the death
 		UhcPlayer up = t.getUhcPlayer(p);
 		if (up != null)
 			t.handlePlayerDeath(up);
 
 	}
 
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent e) {
 		// Only do anything if match is in progress
 		if (!t.isMatchStarted() || t.isMatchEnded()) return;
 		
 		Player p = e.getPlayer();
 		
 		// If they're a dead UHC player, put them into adventure mode and make sure they respawn at overworld spawn
 		UhcPlayer up = t.getUhcPlayer(p);
 		
 		if (up != null) {
 			if (up.isDead()) {
 				p.setGameMode(GameMode.ADVENTURE);
 				e.setRespawnLocation(t.world.getSpawnLocation());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent e) {
 		if (e.getBlock().getType() == Material.STONE) {
 			t.doMiningFatigue(e.getPlayer(), e.getBlock().getLocation().getBlockY());
 		}
 	}
 	
 }
