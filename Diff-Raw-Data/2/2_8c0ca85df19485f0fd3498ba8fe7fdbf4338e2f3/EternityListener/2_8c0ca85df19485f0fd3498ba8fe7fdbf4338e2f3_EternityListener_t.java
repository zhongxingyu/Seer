 package republicaEternityEventIII.republica.devteam;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class EternityListener implements Listener{
 	
 	private EternityMain em;
 	public EternityListener(EternityMain em) {
 		super();
 		this.em = em;
 	}
 	
 	@EventHandler
 	public void onPlayerDamageMethod(EntityDamageEvent e){
 		
 		if(e.getEntity() instanceof Player){
 			if(em.getZiminiarPlayer() == e.getEntity()){
				Player pl = (Player) (e.getEntity());
 				em.boss.ZiminiarHit((Player) pl.getLastDamageCause());
 				pl.setHealth(20);
 			}
 		}
 		
 	}
 	
 	@EventHandler
 	public void onPlayerPunchThing(PlayerInteractEvent e) {
 		SignPunchingOMatic.checkFor(e);
 		if (em.boss != null) {
 			em.boss.checkForSpells(e);
 		}
 		if (SignPunchingOMatic.changed()) {
 			em.saveResultsSignLocation();
 		}
 	}
 	
 }
