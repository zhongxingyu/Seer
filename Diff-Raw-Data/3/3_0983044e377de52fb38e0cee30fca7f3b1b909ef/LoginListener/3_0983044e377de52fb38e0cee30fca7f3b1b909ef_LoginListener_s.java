 package com.martinbrook.tesseractuhc.listeners;
 
 import org.bukkit.GameMode;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.martinbrook.tesseractuhc.MatchPhase;
 import com.martinbrook.tesseractuhc.UhcMatch;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 
 public class LoginListener implements Listener {
 	private UhcMatch m;
 	public LoginListener(UhcMatch m) { this.m = m; }
 
 
 	@EventHandler
 	public void onJoin(PlayerJoinEvent e) {
 		UhcPlayer pl = m.getPlayer(e.getPlayer());
 
 		// If player is op, set them as a spectator
 		if (e.getPlayer().isOp()) {
 			pl.makeSpectator();
 			return;
 		}
 
 		// Normal player. Set their vanish correctly
 		pl.setVanish();
 		
 				
 		// If the match has not yet started, try to launch the player if necessary
 		if (m.getMatchPhase() == MatchPhase.LAUNCHING) {
 			// If player is in the match, make sure they are launched. If not, put them at spawn.
 			if (pl.isParticipant()) m.launch(pl.getParticipant());
 			return;
 		}
 		
 
 		// If match is launching or underway, and autospectate is enabled, make the new player a spectator
 		if (m.getMatchPhase() == MatchPhase.LAUNCHING || m.getMatchPhase() == MatchPhase.MATCH) {
 			if (m.isAutoSpectate() && !pl.isParticipant()) {
 				pl.makeSpectator();
 				return;
 			}
			if (!pl.isSpectator() && !pl.isAdmin()) {
 				pl.teleport(m.getStartingWorld().getSpawnLocation(), null);
 				return;
 			}
 
 		}
 		
 		// If match is over, put player in creative, do nothing else
 		if (m.getMatchPhase() == MatchPhase.POST_MATCH) {
 			e.getPlayer().setGameMode(GameMode.CREATIVE);
 			return;
 		}
 
 	}
 
 
 	@EventHandler
 	public void onQuit(PlayerQuitEvent e) {
 		m.setLastLogoutLocation(e.getPlayer().getLocation());
 	}
 	
 
 	
 	@EventHandler
 	public void onLogin(PlayerLoginEvent e) {
 		// Get a uhcplayer object
 		UhcPlayer pl = m.getPlayer(e.getPlayer());
 		
 		// If a registered player would be prevented from logging in due to the server being full or them not being whitelisted,
 		// let them in anyway.
 		if (pl.isParticipant() && (e.getResult() == PlayerLoginEvent.Result.KICK_FULL || e.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST))
 			e.allow();
 		
 		// If player not allowed to login, do no more
 		if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
 			
 		// If match isn't in progress, do no more
 		if (m.getMatchPhase() != MatchPhase.MATCH) return;
 
 		// If player was not launched, don't allow them in.
 		if (m.isNoLatecomers() && pl.isParticipant() && !pl.getParticipant().isLaunched()) {
 			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The match has already started");
 			return;
 		}
 		
 		// If player has died, don't allow them in, if deathban is in effect.
 		if (m.getDeathban() && pl.isParticipant() && pl.getParticipant().isDead()) {
 			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Dead players cannot rejoin!");
 			return;
 		}
 	}
 	
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent e) {
 		// Get a uhcplayer object
 		UhcPlayer pl = m.getPlayer(e.getPlayer());
 
 		// If this is an autospectate game, make the player a spectator 
 		if (m.isAutoSpectate() && pl.isParticipant() && pl.getParticipant().isDead()
 				&& (m.getMatchPhase() == MatchPhase.MATCH || m.getMatchPhase() == MatchPhase.POST_MATCH)) {
 			pl.makeSpectator();
 		}
 	}
 	
 	
 	
 	
 	
 }
