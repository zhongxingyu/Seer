 package Lihad.ConflictClasses.ClassListeners;
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import Lihad.ConflictClasses.Classes;
 import Lihad.ConflictClasses.ConflictClasses;
 
 public class MysticListener implements Listener {
 	public static ConflictClasses plugin;
 	public MysticListener(ConflictClasses instance) {
 		plugin = instance;
 	}
 	@EventHandler
 	public static void onPlayerInteract(PlayerInteractEvent event){
 		if(ConflictClasses.PLAYER_CURRENT_INDEX.get(event.getPlayer().getName()) == Classes.MYSTIC && event.getPlayer().getItemInHand() != null
 				&& event.getPlayer().getItemInHand().getType() == Material.BLAZE_ROD && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)){
 			List<Entity> entities = event.getPlayer().getNearbyEntities(8, 5, 8);
 			for(int i = 0; i<entities.size();i++){
 				if(entities.get(i) instanceof Player){
 					((Player)entities.get(i)).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,20,5));
 				}
 			}
 		}
 		if(ConflictClasses.PLAYER_CURRENT_INDEX.get(event.getPlayer().getName()) == Classes.MYSTIC && event.getPlayer().getItemInHand() != null
 				&& event.getPlayer().getItemInHand().getType() == Material.BOOK && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)){
 			List<Entity> entities = event.getPlayer().getNearbyEntities(8, 5, 8);
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,20,5));
 			for(int i = 0; i<entities.size();i++){
 				if(entities.get(i) instanceof Player){
 					((Player)entities.get(i)).addPotionEffect(new PotionEffect(PotionEffectType.SPEED,20,5));
 				}
 			}
 		}
 	}
 }
 
