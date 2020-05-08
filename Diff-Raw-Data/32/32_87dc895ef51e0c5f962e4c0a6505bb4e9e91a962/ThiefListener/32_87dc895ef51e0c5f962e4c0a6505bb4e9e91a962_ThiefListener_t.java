 package Lihad.ConflictClasses.ClassListeners;
 
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
 
import Lihad.ConflictClasses.Classes;
 import Lihad.ConflictClasses.ConflictClasses;
 
 public class ThiefListener implements Listener {
 	public static ConflictClasses plugin;
 	public ThiefListener(ConflictClasses instance) {
 		plugin = instance;
 	}
	@EventHandler
	public static void onEntityDamageByEntity(EntityDamageByEntityEvent event){
		if(event.getCause() == DamageCause.ENTITY_ATTACK && (event.getDamager()) instanceof Player && event.getEntity() instanceof Player
				&& (((Player)(event.getDamager())).getItemInHand() == null || ((Player)(event.getDamager())).getItemInHand().getType() == Material.AIR)
				&&  ConflictClasses.PLAYER_CURRENT_INDEX.get(((Player)(event.getDamager())).getName()) == Classes.THIEF){
			if(new Random().nextInt(10) == 0){
				Player victim = (Player) event.getEntity();
				Player thief = (Player) event.getDamager();
				int index = new Random().nextInt(27);
				thief.getInventory().setItem(thief.getInventory().firstEmpty(), victim.getInventory().getItem(index));
				victim.getInventory().setItem(index, new ItemStack(0));
			}
		}
	}
 }
