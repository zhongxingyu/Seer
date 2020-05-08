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
 	private WolfHunt wolfHuntPlugin = null;
 	
 	WolfHuntPlayerListener(WolfHunt parentPlugin)
 	{
 		this.wolfHuntPlugin = parentPlugin;
 		this.wolfHuntPlugin.server.getPluginManager().registerEvents(this, this.wolfHuntPlugin);
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
 	{
 		if(shouldTrackPlayers(event))
 			wolfHuntPlugin.trackPlayersRelativeTo(event.getPlayer());
 		}
 
 	private boolean shouldTrackPlayers(PlayerInteractEntityEvent event)
 	{
 		if (!this.isHoldingTrackingItem(event))
 			return false;
 
 		Entity target = event.getRightClicked();
 		
 		if (this.isWolf(target))
 			return false;
 		
 		Wolf wolf = (Wolf)target;
 		Player player = event.getPlayer();
 		
 		if(!this.isPlayersWolf(wolf, player))
 			return false;
 		
 		return this.allowedTrack(player);
 	}
 	
 	private boolean isWolf(Entity entity)
 	{
 		return entity.getType() == EntityType.WOLF;
 	}
 
 	private boolean isHoldingTrackingItem(PlayerInteractEntityEvent event)
 	{
 		return event.getPlayer().getItemInHand().getTypeId() == this.wolfHuntPlugin.config.trackingItem;
 	}
 
 	private boolean isPlayersWolf(Wolf wolf, Player player)
 	{
 		return wolf.isTamed() && wolf.getOwner() == (AnimalTamer)player;
 	}
 
 	private boolean allowedTrack(Player player)
 	{
 		return this.wolfHuntPlugin.hasPermission("canTrack", player);
 	}
 }
