 package com.martinbrook.tesseractuhc;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 import com.martinbrook.tesseractuhc.util.TeleportUtils;
 
 public class UhcSpectator {
 
 	private boolean interacting = false;
 	private Integer cyclePoint = null;
 	private Location tpBackLocation = null;
 	private UhcPlayer player;
 
 	public UhcSpectator(UhcPlayer pl) { 
 		this.player = pl;
 		this.tpBackLocation = player.getMatch().getStartingWorld().getSpawnLocation();
 	}
 	
 	public void setInteracting(boolean interacting) { 
 		this.interacting=interacting;
 		MatchUtils.setCollidesWithEntities(player.getPlayer(), interacting);
 		
 	}
 	public boolean isInteracting() { return this.interacting; }
 
 	public String getName() { return player.getName(); }
 
 	public int nextCyclePoint(int numberOfPlayers) {
 		if (cyclePoint == null) {
 			cyclePoint = 0;
 		} else {
 			if (++cyclePoint >= numberOfPlayers) cyclePoint = 0;
 		}
 		return cyclePoint;
 	}
 	
 	public boolean teleport(Double x, Double y, Double z) {
 		Location l = player.getLocation();
 		if (l != null) {
 			return teleport(new Location(l.getWorld(),x,y,z));
 		} else {
 			return false;
 		}
 		
 		
 	}
 
 	public boolean teleport(Player p) { return this.teleport(p, "You have been teleported!"); }
 	public boolean teleport(Location l) { return this.teleport(l, "You have been teleported!"); }
 	public boolean teleport(Player p, String message) {	return this.teleport(TeleportUtils.getSpectatorTeleportLocation(p.getLocation()), message); }
 	public boolean teleport(Location l, String message) {
 		
 		Location oldLocation = player.getLocation();
 		if (player.teleport(l, message)) {
 			tpBackLocation = oldLocation;
 			return true;
 		}
 		return false;
 
 	}
 	
 	public boolean tpBack() {
 		return teleport(tpBackLocation, "You have been teleported back!");
 	}
 
 	public void sendMessage(String message) { player.sendMessage(message); }
 	public void toggleGameMode() {
 		player.setGameMode((player.getGameMode() == GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE);
 	}
 
 	/**
 	 * Show a spectator the contents of a player's inventory.
 	 * 
 	 * @param viewed The player being observed
 	 */
 	public boolean showInventory(Player viewed) {
 
 		Inventory i = MatchUtils.getInventoryView(viewed);
 		if (i == null) return false;
 		
 		player.getPlayer().openInventory(i);
 		return true;
 	}
 
 	public Location getLocation() {
 		return player.getLocation();
 	}
 
 	public UhcPlayer getPlayer() {
 		return player;
 	}
 	
	public void setNightVision(boolean b){
		if(b){
			this.getPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0,false));
		}else{
			this.getPlayer().getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
		}
 	}
 	
 }
