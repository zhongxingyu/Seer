 package org.github.craftfortress2;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
public class CFClasses implements Listener{
 	@EventHandler
 	public void onFoodBarLower(FoodLevelChangeEvent event){
			Player player = ((Player) event).getPlayer();
 				player.setExhaustion(0);
 				player.setFoodLevel(17);
 			}
 	}
