 package fr.jules_cesar.Paintball;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
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
 	
 	@EventHandler
 	public void faim(FoodLevelChangeEvent event){
 		if(Paintball.getPartie() != null){
 			if(Paintball.getPartie().estJoueur((Player) event.getEntity())){
 				event.setCancelled(true);
 			}
 		}
 	}
 	
	@EventHandler(priority = EventPriority.HIGHEST)
 	public void mort(PlayerDeathEvent event){
 		if(Paintball.getPartie() != null && Paintball.getPartie().estJoueur(event.getEntity())){
 			event.setDeathMessage("");
 		}
 	}
 	
	@EventHandler(priority = EventPriority.HIGHEST)
 	public void reapparition(PlayerRespawnEvent event){
 		if(Paintball.getPartie() != null && Paintball.getPartie().estJoueur(event.getPlayer())){
 			event.setRespawnLocation(Paintball.getPartie().retour(event.getPlayer()));
 			event.getPlayer().getInventory().addItem(new ItemStack(332, 128));
 		}
 	}
 }
