 package at.doebi;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class MCMEPVPListener implements Listener{
 	
 	public MCMEPVPListener(MCMEPVP instance) {
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerJoin(final PlayerLoginEvent event){
 		MCMEPVP.addTeam(event.getPlayer(),"spectator");
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerLeave(final PlayerQuitEvent event){
 		MCMEPVP.removeTeam(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerChat(final PlayerChatEvent event){
 		MCMEPVP.sendToTeam(event.getMessage(), event.getPlayer());
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerDeath(final PlayerDeathEvent event){
 		MCMEPVP.removeTeam(event.getEntity());
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
	void onPlayerDamage(final EntityDamageByEntityEvent  event){
		String Damager = event.getDamager().getType().getName().toLowerCase();
        String Damaged = event.getEntityType().getName().toLowerCase();
		if((event.getDamager().getType().equals(EntityType.PLAYER) && event.getEntityType().equals(EntityType.PLAYER))
				&& ((MCMEPVP.PlayerTeams.get(Damager) == MCMEPVP.PlayerTeams.get(Damaged))
						|| MCMEPVP.PlayerTeams.get(Damager) == "spectator")){
			event.setCancelled(true);
 		}
 	}
 }
