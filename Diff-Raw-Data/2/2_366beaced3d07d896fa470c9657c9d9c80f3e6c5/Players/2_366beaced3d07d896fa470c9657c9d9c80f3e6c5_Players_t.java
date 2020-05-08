 package net.amoebaman.gamemasterv3;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.potion.PotionEffect;
 
 import net.amoebaman.gamemasterv3.api.AutoGame;
 import net.amoebaman.gamemasterv3.enums.GameState;
 import net.amoebaman.gamemasterv3.enums.PlayerState;
 import net.amoebaman.utils.maps.PlayerMap;
 
 /**
  * Component that manages the handling of events and command related to players.
  * 
  * @author AmoebaMan
  */
 public class Players implements Listener{
 	
 	private PlayerMap<String> lastDamager = new PlayerMap<String>("");
 	private PlayerMap<Long> lastDamageTime = new PlayerMap<Long>(0L);
 	private PlayerMap<Long> lastMovementTime = new PlayerMap<Long>(0L);
 	private Set<Player> respawning = new HashSet<Player>();
 	
 	private GameMaster master;
 	
 	protected Players(GameMaster master){
 		this.master = master;
 	}
 	
 	/**
 	 * Stamps a player damage, setting the record for last damager and last
 	 * damage time to the given culprit and the system time respectively. This
 	 * data is used to give credit for unconventional kills (e.g. falling,
 	 * burning, etc.)
 	 * 
 	 * @param victim a player
 	 * @param culprit their damager
 	 */
 	public void stampDamage(Player victim, Player culprit){
 		lastDamager.put(victim, culprit.getName());
 		lastDamageTime.put(victim, System.currentTimeMillis());
 	}
 	
 	/**
 	 * Removes the stamps on record for a player.
 	 * 
 	 * @param player a player
 	 */
 	public void destamp(Player player){
 		lastDamager.remove(player);
 		lastDamageTime.remove(player);
 		respawning.remove(player);
 	}
 	
 	/**
 	 * Gets the last culprit stamped for player damage. See
 	 * {@link #stampPlayerDamage(Player, Player)}.
 	 * 
 	 * @param player a player
 	 * @return thec culprit
 	 */
 	public Player getLastDamager(Player player){
 		return Bukkit.getPlayer(lastDamager.get(player));
 	}
 	
 	/**
 	 * Gets the last time stamped for player damage. See
 	 * {@link #stampPlayerDamage(Player, Player)}.
 	 * 
 	 * @param player a player
 	 * @return the time stamp
 	 */
 	public long getLastDamageTime(Player player){
 		return lastDamageTime.get(player);
 	}
 	
 	/**
 	 * Resets a player fully.
 	 * <p>
 	 * Clears inventory; restores health, hunger, and saturation to full;
 	 * removes formatting from display name and list name; removes GameMaster
 	 * records; ejects the player from their vehicle.
 	 * 
 	 * @param player a player
 	 */
 	public void resetPlayer(Player player){
 		/*
 		 * Clear inventory and effects
 		 */
 		player.closeInventory();
 		player.getInventory().clear();
 		player.getInventory().setArmorContents(null);
 		for(PotionEffect effect : player.getActivePotionEffects())
 			player.removePotionEffect(effect.getType());
 		/*
 		 * Reset physical properites
 		 */
 		player.setHealth(player.getMaxHealth());
 		player.setFoodLevel(20);
 		player.setSaturation(20);
 		player.eject();
 		if(player.isInsideVehicle())
 			player.getVehicle().eject();
 		/*
 		 * Reset display properties
 		 */
 		updateColors(player);
 		/*
 		 * Reset recorded player stats
 		 */
 		lastDamager.remove(player);
 		lastDamageTime.remove(player);
		lastMovementTime.remove(player);
		respawning.remove(player);
 	}
 	
 	/**
 	 * Updates a player's display name and list name as necessary with the
 	 * colors designated by the active game using
 	 * {@link AutoGame#getColor(Player)}.
 	 */
 	public void updateColors(Player player){
 		String colorName = player.getName();
 		if(master.getState() != GameState.INTERMISSION && master.getState(player) == PlayerState.PLAYING){
 			ChatColor c = master.getActiveGame().getColor(player);
 			if(c != null)
 				colorName = c + player.getName() + ChatColor.RESET;
 		}
 		player.setDisplayName(colorName);
 	}
 	
 	/**
 	 * Checks whether a player is currently respawning, if the current game uses
 	 * the master respawning system.
 	 * 
 	 * @param player a player
 	 * @return true if the player is respawning using the master system, false otherwise
 	 */
 	public boolean isRespawning(Player player){
 		return respawning.contains(player);
 	}
 	
 	protected void toggleRespawning(Player player){
 		if(!respawning.remove(player))
 			respawning.add(player);
 	}
 
 	protected void stampMovement(Player player){
 		lastMovementTime.put(player, System.currentTimeMillis());
 	}
 	
 	protected long getTimeSinceLastMovement(Player player){
 		long stamp = lastMovementTime.get(player);
 		if(stamp == 0)
 			return 0;
 		else
 			return System.currentTimeMillis() - stamp;
 	}
 	
 	protected void resetMovementStamp(Player player){
 		lastMovementTime.remove(player);
 	}
 	
 }
