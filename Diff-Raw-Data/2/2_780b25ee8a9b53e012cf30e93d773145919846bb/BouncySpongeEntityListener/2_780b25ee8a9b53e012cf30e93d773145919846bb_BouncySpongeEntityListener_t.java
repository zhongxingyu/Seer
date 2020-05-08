 package com.adencraft2000.bouncysponge;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.util.Vector;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class BouncySpongeEntityListener implements Listener{
 	 BouncySponge plugin;
 	 
 	 /**
 	  * Constructor for EntityListener
 	  * @param instance grabs an instance of BouncySpunge
 	  */
 	 public BouncySpongeEntityListener(BouncySponge instance){
 		 plugin = instance;
 	 }
 	 
 	 /**
 	  * Called when an entity is damaged
 	  * Checks if player and if true, will not deal fall damage on Sponge or lapis
 	  * @param ev A EntityDamageEvent object
 	  */
 	 @EventHandler
 	 public void onEntityDamage(EntityDamageEvent ev){
 		 if(ev.getEntity() instanceof Player) {
 			 Player player = (Player) ev.getEntity();
 			 if(ev.getCause().equals((DamageCause.FALL)) && player.hasPermission("bouncysponge.jump")){
 				 Block b = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
 				 if(b.getType() == Material.LAPIS_BLOCK || b.getType() == Material.SPONGE){
 					 ev.setCancelled(true);
 					 if (b.getType() == Material.SPONGE) {
 							if (player.isSneaking()){
 							}
 							else{
 							Vector dir = player.getLocation().getDirection().multiply(1.75);
 							Vector vec = new Vector(dir.getX(), plugin.getConfig().getDouble("launch"), dir.getZ());
 							player.setVelocity(vec);
							player.setNoDamageTicks(400);
 							
 					 
 					 }
 				 }
 			 }
 		 }
 	 
 	 
 		 }
 	 }
 }
 
