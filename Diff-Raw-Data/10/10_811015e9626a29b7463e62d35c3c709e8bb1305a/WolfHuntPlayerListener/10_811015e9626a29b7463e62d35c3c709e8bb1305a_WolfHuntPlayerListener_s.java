 package me.Kruithne.WolfHunt;
 
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 public class WolfHuntPlayerListener implements Listener
 {
 	private Tracking tracking = null;
 	private Output output = null;
 	private VanishHandler vanish = null;
 	private Permissions permission = null;
 	private Configuration config = null;
 	
 	WolfHuntPlayerListener(Tracking tracking, Output output, VanishHandler vanish, Permissions permission, Configuration config)
 	{
 		this.tracking = tracking;
 		this.output = output;
 		this.vanish = vanish;
 		this.permission = permission;
 		this.config = config;
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
 	{
 		if (shouldTrackPlayers(event))
 		{
			Player player = event.getPlayer()
 			this.output.toPlayer(this.tracking.trackPlayersRelativeTo(player), player);
 			event.setCancelled(true);
 		}
 	}
 
 	private boolean shouldTrackPlayers(PlayerInteractEntityEvent event)
 	{
 		Player eventPlayer = event.getPlayer();
 		Entity target = event.getRightClicked();
 		
 		if (!this.isHoldingTrackingItem(event))
 			return false;
 		
 		if (!this.isWolf(target))
 			return false;
 		
 		if (this.playerIsVanished(eventPlayer))
 			return false;
 		
 		Wolf wolf = (Wolf)target;
 		
 		if (this.isBaby(wolf))
 			return false;
 		
 		if(!this.isPlayersWolf(wolf, eventPlayer))
 			return false;
 		
 		return this.allowedTrack(eventPlayer);
 	}
 	
 	private boolean isWolf(Entity entity)
 	{
 		return entity.getType() == EntityType.WOLF;
 	}
 
 	private boolean playerIsVanished(Player player)
 	{
 		return this.vanish.playerIsVanished(player);
 	}
 
 	private boolean isBaby(Wolf wolf)
 	{
 		return !this.config.babyWolvesCanTrack && wolf.getAge() < 0;
 	}
 
 	private boolean isHoldingTrackingItem(PlayerInteractEntityEvent event)
 	{
 		return event.getPlayer().getItemInHand().getTypeId() == this.config.trackingItem;
 	}
 
 	private boolean isPlayersWolf(Wolf wolf, Player player)
 	{
 		return wolf.isTamed() && wolf.getOwner() == (AnimalTamer)player;
 	}
 
 	private boolean allowedTrack(Player player)
 	{
 		return this.permission.has(player, Permissions.canTrack);
 	}
 }
