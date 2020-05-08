 package com.martinbrook.tesseractuhc;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 
 /**
  * Represents a player who is, or has ever been, on the server.
  *
  */
 public class UhcPlayer {
 	private OfflinePlayer player;
 	private UhcParticipant participant;
 	private UhcSpectator spectator;
 	private UhcMatch m;
 	
 
 	public UhcPlayer(OfflinePlayer player, UhcMatch match) {
 		this.player = player;
 		this.m = match;
 	}
 
 	public String getName() { return player.getName(); }
 	public UhcMatch getMatch() { return m; }
 	public boolean isOnline() { return player.isOnline(); }
 	private boolean isOp() { return player.isOp(); }
 	public Player getPlayer() { return player.getPlayer(); }
 	
 	public void setParticipant(UhcParticipant participant) { this.participant = participant; }
 	
 	
 	public boolean isParticipant() {
 		return (participant != null);
 	}
 	
 	public boolean isActiveParticipant() {
 		if (participant == null)
 			return false;
 		else
 			return (participant.isLaunched() && !participant.isDead());
 	}
 	
 	public boolean isSpectator() {
 		return (spectator != null);
 	}
 	
 	public boolean isInteractingSpectator() {
 		return spectator != null && spectator.isInteracting();
 	}
 	
 	public boolean isNonInteractingSpectator() {
 		return spectator != null && !spectator.isInteracting();
 	}
 	
 	public UhcParticipant getParticipant() {
 		return participant;
 	}
 	
 	public UhcSpectator getSpectator() {
 		return spectator;
 	}
 	
 	public boolean isAdmin() {
 		return this.isSpectator() && this.isOp();
 	}
 	
 	public boolean makeSpectator() {
 		if (isActiveParticipant()) return false;
 		if (spectator == null) spectator = new UhcSpectator(this);
 		setVanish();
 		setGameMode(GameMode.CREATIVE);
 		return true;
 	}
 
 	public void makeNotSpectator() {
 		spectator=null;
 		setGameMode(GameMode.SURVIVAL);
 		setVanish();
 		
 	}
 
 	public boolean renew() {
 		return (heal() && feed() && clearXP() && clearPotionEffects() && clearInventory());
 	}
 	
 	/**
 	 * Heal the player
 	 */
 	public boolean heal() {
 		Player p = getPlayer();
 		if (p==null) return false;
 		p.setHealth(20);
 		return true;
 	}
 
 	/**
 	 * Feed the player
 	 */
 	public boolean feed() {
 		Player p = getPlayer();
 		if (p==null) return false;
 		p.setFoodLevel(20);
 		p.setExhaustion(0.0F);
 		p.setSaturation(5.0F);
 		return true;
 	}
 
 	/**
 	 * Reset XP of the given player
 	 */
 	public boolean clearXP() {
 		Player p = getPlayer();
 		if (p==null) return false;
 		p.setTotalExperience(0);
 		p.setExp(0);
 		p.setLevel(0);
 		return true;
 	}
 
 	/**
 	 * Clear potion effects of the given player
 	 */
 	public boolean clearPotionEffects() {
 		Player p = getPlayer();
 		if (p==null || !p.isOnline()) return false;
 		for (PotionEffect pe : p.getActivePotionEffects()) {
 			p.removePotionEffect(pe.getType());
 		}
 		return true;
 	}
 
 	/**
 	 * Clear inventory and ender chest of the given player
 	 */
 	public boolean clearInventory() {
 		Player p = getPlayer();
 		if (p==null) return false;
 		PlayerInventory i = p.getInventory();
 		i.clear();
 		i.setHelmet(null);
 		i.setChestplate(null);
 		i.setLeggings(null);
 		i.setBoots(null);
 		
 		p.getEnderChest().clear();
 		return true;
 		
 	}
 	
 	public boolean setGameMode(GameMode g) {
 		Player p = getPlayer();
 		if (p==null) return false;
 		p.setGameMode(g);
 		return true;
 	}
 
 	public GameMode getGameMode() {
 		Player p = getPlayer();
 		if (p==null) return null;
 		return p.getGameMode();
 	}
 
 	public boolean sendMessage(String message) {
 		Player p = getPlayer();
 		if (p==null) return false;
 		
 		p.sendMessage(message);
 		return true;
 	}
 	
 	/**
 	 * Set the correct vanish status for this player in relation to all other players
 	 */
 	public void setVanish() {
 		if (!isOnline()) return;
 
 		for (Player p : m.getServer().getOnlinePlayers()) {
 			UhcPlayer pl = m.getPlayer(p);
 			this.setVisibilityTo(pl);
 			pl.setVisibilityTo(this);
 		}
 	}
 	
 	/**
 	 * Set the correct visibility for another player from this player's perspective
 	 * 
 	 * @param viewed Player being viewed
 	 */
 	public void setVisibilityTo(UhcPlayer viewer) {
 		if (viewer == this) return;
 		
 		Player p = getPlayer();
 		
 		Player pv = viewer.getPlayer();
 		if (p == null || pv == null) return;
 		
 		
 		// A spec should be invisible to a non-spec if the match is launching and not ended
 		if (!viewer.isSpectator() && this.isSpectator() && (m.getMatchPhase() == MatchPhase.LAUNCHING || m.getMatchPhase() == MatchPhase.MATCH)) {
 			pv.hidePlayer(p);
 		} else {
 			pv.showPlayer(p);
 		}
 	}
 
 	public String getDisplayName() {
 		Player p = player.getPlayer();
 		if (p != null) return p.getDisplayName();
 		else return player.getName();
 	}
 
 	public Location getLocation() {
 		Player p = player.getPlayer();
 		if (p != null) return p.getLocation();
 		else return null;
 	}
 	
 	public boolean teleport(Location l, String message) {
 		Player p = getPlayer();
 		if (p == null) return false;
 		
 		// Ensure the chunk is loaded before teleporting
 		World w = l.getWorld();
 		Chunk chunk = w.getChunkAt(l);
 		if (!w.isChunkLoaded(chunk))
 			w.loadChunk(chunk);
 		
 		if (p.teleport(l)) {
			if (isSpectator() && p.getAllowFlight())
 				p.setFlying(true);
 			if (message != null && !message.isEmpty())
 				p.sendMessage(TesseractUHC.OK_COLOR + message);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void updatePlayerListName() {
 		Player p=getPlayer();
 		if (p == null) return;
 		
 		String name = getName();
 		
 		if (isActiveParticipant()) {
 			double health = p.getHealth() / 2.0;
 			ChatColor color = ChatColor.GREEN;
 			if (name.length() > 8) name = name.substring(0, 8);
 			if (health <= 5) color = ChatColor.YELLOW;
 			if (health <= 2) color = ChatColor.RED; 
 			boolean isOdd = (health - Math.floor(health) == 0.5);
 			String outputName = (color + name + " - " + (int) health + (isOdd ? ".5" : ""));
 			p.setPlayerListName(outputName);
 		} else {
 			if (name.length() > 14) name = name.substring(0, 14);
 			p.setPlayerListName(ChatColor.DARK_GRAY + name);
 		}
 
 	}
 
 	
 }
