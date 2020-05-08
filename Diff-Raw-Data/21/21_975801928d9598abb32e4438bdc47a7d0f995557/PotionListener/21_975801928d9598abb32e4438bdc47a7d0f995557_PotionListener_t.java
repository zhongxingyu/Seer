 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class PotionListener implements Listener {
 
 	// Get Main Class
 	public static BattleNight plugin;
 	public PotionListener(BattleNight instance) {
 		plugin = instance;
 	}
 	
	@EventHandler(priority = EventPriority.HIGH)
 	public void onPotionSplash(PotionSplashEvent event) {
			ThrownPotion potion = event.getEntity();
			if(potion.getShooter() instanceof Player) {
				Player thrower = (Player) potion.getShooter();
				if (plugin.BattleUsersTeam.containsKey(thrower.getName()) && plugin.playersInLounge) {
					potion.remove();
 					thrower.sendMessage(ChatColor.GRAY + "[BattleNight] " + ChatColor.WHITE + "Not so fast! No Cheating!");
 				}
 			}
 	}
 	
	@EventHandler(priority = EventPriority.HIGH)
 	public void onPotionDrink(PlayerInteractEvent event) {
 		if(event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
 			Player drinker = event.getPlayer();
 			String playerName = drinker.getName();
 			if(drinker.getItemInHand().equals(Material.POTION)) {
 				if (plugin.BattleUsersTeam.containsKey(playerName) && plugin.playersInLounge) {
 						event.setCancelled(true);
 						drinker.sendMessage(ChatColor.GRAY + "[BattleNight] " + ChatColor.WHITE + "Not so fast! No Cheating!");
 				}
 			}
 		}
 	}
 	
 }
