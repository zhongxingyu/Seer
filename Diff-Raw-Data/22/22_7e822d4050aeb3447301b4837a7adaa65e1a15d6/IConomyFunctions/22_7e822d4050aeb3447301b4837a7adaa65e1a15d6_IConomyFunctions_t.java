 package com.alta189.minemail.addons;
 
 import org.bukkit.entity.Player;
 import com.iConomy.system.Holdings;
 import com.alta189.minemail.addons.IConomyHandler;
 
 public class IConomyFunctions {
 	IConomyHandler instance;
 
 	IConomyFunctions(IConomyHandler plugin) {
 		instance = plugin;
 	}
 
 	public Boolean takeBalance(Player player, Double amount, Holdings balance) {
 
 		if (player.isOp() && instance.plugin.isFree(player)) {
 			if (instance.hasAccount(player) == true) {
 				if (instance.hasEnough(amount, balance) == true) {
 					balance.subtract(amount);
 					return true;
 				}
			}
		}else {
			if (!player.isOp() && instance.plugin.isFree(player)) {
				if (instance.hasAccount(player) == true) {
					if (instance.hasEnough(amount, balance) == true) {
						balance.subtract(amount);
						return true;
 					}
 				}
				
 			}
 		}
		//Default is to return false
 		return false;
 	}
 }
