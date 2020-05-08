 package me.limebyte.battlenight.core.Other;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.entity.Player;
 
 public class Util {
 	
	BattleNight plugin = new BattleNight();
 	
 	public boolean isInTeam(Player player, String team) {
 		if (plugin.BattleUsersTeam.containsKey(player.getName())) {
 			if ((plugin.BattleUsersTeam.get(player.getName()) == team)) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		else {
 			return false;
 		}
 	}
 
 }
