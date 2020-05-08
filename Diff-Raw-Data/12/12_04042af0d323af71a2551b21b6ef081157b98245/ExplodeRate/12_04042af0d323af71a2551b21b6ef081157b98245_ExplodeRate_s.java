 package me.GudfareN.ExplodingEggs;
 
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class ExplodeRate extends PlayerListener {
     
 	public static Main plugin;
 
     public ExplodeRate(Main instance) {
         plugin = instance;
     }
     public void onPlayerEggThrow(PlayerEggThrowEvent event){
    	event.setHatching(true); //Always explode
    	event.getEgg().setBounce(true);
     }
 }
