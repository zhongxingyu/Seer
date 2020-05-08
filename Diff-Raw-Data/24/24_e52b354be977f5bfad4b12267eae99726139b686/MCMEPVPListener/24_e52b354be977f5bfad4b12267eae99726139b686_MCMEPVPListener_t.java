 package at.doebi;
 
import org.bukkit.ChatColor;
 import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
	void onPlayerDamage(final EntityDamageByEntityEvent event){
		if(event.getDamager().getType().equals(EntityType.PLAYER) && event.getEntity().getType().equals(EntityType.PLAYER)){
			Player Attacker = (Player) event.getDamager();
			Player Victim = (Player) event.getEntity();
		    String AttackerTeam = MCMEPVP.PlayerTeams.get(Attacker.getName());
		    String VictimTeam = MCMEPVP.PlayerTeams.get(Victim.getName());
		    if(AttackerTeam != "spectator" && VictimTeam != "spectator" && AttackerTeam != VictimTeam){
		    	//Victim got attacked by Attacker and both are in rivaling Teams
		    }else{
		    	//Either friendly fire or Spectator involved in fight
		    	Attacker.sendMessage(ChatColor.DARK_RED + "You can't attack " + Victim.getName() + "!");
		    	event.setCancelled(true);
		    }
 		}
 	}
 }
