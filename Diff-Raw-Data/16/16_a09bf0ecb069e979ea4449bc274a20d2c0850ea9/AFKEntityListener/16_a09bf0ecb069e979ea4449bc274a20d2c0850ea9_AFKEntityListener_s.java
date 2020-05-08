 package me.sam.afk;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
 
 public class AFKEntityListener extends EntityListener
 {
 	 public static AFK plugin;
 	  public AFKEntityListener(AFK instance) {
 	        plugin = instance;
 	    }
 	  
 	  public void onEntityDamage(EntityDamageEvent event)
 	  {
 		  if(event.getClass() == Player.class)
 		  {
 			  Player thePlayer = (Player)event.getEntity();
 			  
 			  if(plugin.afkers.containsKey(thePlayer.getName()))
 			  {
 				  event.setCancelled(true);
 			  }
 		  }
 	  }
 	  
 	  public void onEntityTarget(EntityTargetEvent event)
	  {		  
		  if(event.getTarget().getClass() == CraftPlayer.class)
 		  {
			  CraftPlayer thePlayer = (CraftPlayer)event.getTarget();
			  System.out.println("Player name: " + thePlayer.getName());
			  if(plugin.afkers.containsKey(thePlayer.getName()))
 			  {
			  		event.setCancelled(true);
 			  }
 		  }
 	  }
 }
