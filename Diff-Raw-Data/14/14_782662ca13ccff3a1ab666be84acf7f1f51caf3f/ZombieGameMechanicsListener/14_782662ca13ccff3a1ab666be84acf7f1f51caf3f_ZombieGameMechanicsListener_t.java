 package net.endercraftbuild.cod.zombies.listeners;
 
 import net.endercraftbuild.cod.CoDMain;
 import net.endercraftbuild.cod.utils.Utils;
 import net.endercraftbuild.cod.zombies.ZombieGame;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 public class ZombieGameMechanicsListener implements Listener {
 
 	private CoDMain plugin;
 
 	public ZombieGameMechanicsListener(CoDMain plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerHitPlayer(EntityDamageByEntityEvent event) { // don't let zombie players kill each other
 		if (!(event.getEntity() instanceof Player))
 			return;
 		if (!(Utils.getDamager(event) instanceof Player))
 			return;
 		
 		Player player = (Player) event.getEntity();
 		ZombieGame game = (ZombieGame) plugin.getGameManager().get(player);
 		
 		if (game == null)
 			return;
 		
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerHitByExplosion(EntityDamageEvent event) { // don't let zombie game players be injured by explosions
 		if (!(event.getEntity() instanceof Player))
 			return;
 		if (!event.getCause().equals(DamageCause.ENTITY_EXPLOSION) && !event.getCause().equals(DamageCause.BLOCK_EXPLOSION))
 			return;
 			
 		Player player = (Player) event.getEntity();
 		ZombieGame game = (ZombieGame) plugin.getGameManager().get(player);
 		
 		if (game == null)
 			return;
 		
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onWolfBurning(EntityDamageEvent event) { // make sure they're immune to fire
 		if (!(event.getEntity() instanceof Wolf))
 			return;
 		if (!event.getCause().equals(DamageCause.FIRE) && !event.getCause().equals(DamageCause.FIRE_TICK))
 			return;
 		
 		event.setCancelled(true);
 	}
 		
 	@EventHandler(ignoreCancelled = true)
 	public void onAKShare(PlayerPickupItemEvent event) { //Prevent AK sharing
 		Player player = event.getPlayer();
 		if(!player.hasPermission("cod.kits.zombies.donor"))
		{
 			
		if(event.getItem().getItemStack().getType() == Material.IRON_HOE) 
		{			
 		event.setCancelled(true);
		}
	}
 	}
 }
