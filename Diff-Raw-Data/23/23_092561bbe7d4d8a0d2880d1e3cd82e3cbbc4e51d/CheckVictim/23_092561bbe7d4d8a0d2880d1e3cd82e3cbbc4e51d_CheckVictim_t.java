 package me.firebreath15.icontrolu;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class CheckVictim extends BukkitRunnable{
 	
 	Player v;
 	Player c;
 	CheckVictim(Player victim, Player controller){
 		v=victim;
 		c=controller;
 	}
 	
 	public void run(){
 		if(v != null && c != null){
			if(v.hasMetadata("iCU_P")){
				if(c.hasMetadata("iCU_H")){
					if(v.getWorld().getName().equalsIgnoreCase(c.getWorld().getName())){
						if(v.getLocation().distance(c.getLocation()) > 25){
							v.teleport(c);
							TitleAPI.sendTitle(v, 20, 20, 60, "4TOO FAR AWAY", "");
						}
					}else{
						v.teleport(c);
						v.setGameMode(GameMode.SPECTATOR);
						TitleAPI.sendTitle(v, 20, 20, 60, "4SWITCHED WORLDS", "");
					}
				}else{
					this.cancel();
 				}
 			}else{
				this.cancel();
 			}
 		}else{
 			this.cancel();
 		}
 	}
 }
