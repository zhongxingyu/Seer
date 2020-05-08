 package org.github.craftfortress2;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
public class CFClasses {
 	@EventHandler
 	public void onFoodBarLower(FoodLevelChangeEvent event){
			if(event.getEntity()instanceof Player){
				Player player = (Player)event.getEntity();
 				player.setExhaustion(0);
 				player.setFoodLevel(17);
 			}
		}
 	}
