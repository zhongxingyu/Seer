 package fr.jules_cesar.Paintball;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class PaintballListener implements Listener{
 	
 	@EventHandler
 	public void toucher(EntityDamageByEntityEvent event){
 		Paintball.getArene().toucher(event);
 	}
 	
 	@EventHandler
 	public void deconnexion(PlayerQuitEvent event){
 		Paintball.getArene().deconnexion(event.getPlayer());
 	}
 	
 	@EventHandler
 	public void commande(PlayerCommandPreprocessEvent event){
 		if(Paintball.getPartie() != null){
 			if(Paintball.getPartie().estJoueur(event.getPlayer())){
 				if(!event.getMessage().startsWith("/paintball") && !event.getMessage().startsWith("/help paintball")){
 					event.setCancelled(true);
 					event.getPlayer().sendMessage(Paintball.messages.get("command.noallowed"));
 				}
 			}
 		}
 	}
 }
