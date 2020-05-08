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
 
		if (instance.plugin.isFree(player)) {
			//If player isFree then return true
			return true;
 		}else {
			if (!instance.plugin.isFree(player)) {
 				if (instance.hasAccount(player) == true) {
 					if (instance.hasEnough(amount, balance) == true) {
						//if player is not Free and has enough money return true
 						balance.subtract(amount);
 						return true;
 					}
 				}
 				
 			}
 		}
		//if player is not free and does not have enough money return false
 		return false;
 	}
 }
