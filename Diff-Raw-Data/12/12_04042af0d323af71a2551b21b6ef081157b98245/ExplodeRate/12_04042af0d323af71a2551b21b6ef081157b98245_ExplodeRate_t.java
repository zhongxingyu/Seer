 package me.GudfareN.ExplodingEggs;
 
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class ExplodeRate extends PlayerListener {
     
 	public static Main plugin;
 
     public ExplodeRate(Main instance) {
         plugin = instance;
     }
    
     public void onPlayerEggThrow(PlayerEggThrowEvent event){
    	//event.getEntity().getWorld().createExplosion(event.getLocation(), 2); //Where to explode and radius
    	event.getEgg().remove();
     }
 }
