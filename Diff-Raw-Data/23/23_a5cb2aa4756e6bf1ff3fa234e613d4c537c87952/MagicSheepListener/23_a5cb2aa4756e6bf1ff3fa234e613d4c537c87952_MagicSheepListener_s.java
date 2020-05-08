 package com.magicsheep.Techie;
 
 import java.util.Random;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 
 
 import com.magicsheep.Techie.MagicSheep;
 
 public class MagicSheepListener implements Listener{
 	   private final MagicSheep plugin;
 	   Random random = new Random();
 	   int scolor = 0;
 	   DyeColor sco = DyeColor.WHITE;
 	   Block dablock;
 	   Location daloc;
 	   
 	    public MagicSheepListener(MagicSheep instance) {
 	        plugin = instance;
 	    }
 	    
 	    @EventHandler(priority = EventPriority.NORMAL)
 		public void onMobHurt(EntityDamageByEntityEvent event){
 			if (event.getEntity() instanceof Sheep) {	
 				//if (plugin.sheepa != null && event.getEntity() == plugin.sheepa) {
 				if ((plugin.dasheep != null)) {
 				if (event.getEntity().getUniqueId().toString().equals(plugin.dasheep.toString())) {
 					Player attacker = (Player) event.getDamager(); //Get the person that attacked?		
 					if(scolor == 0) {
 						sco = DyeColor.WHITE;
 					}
 					if(scolor == 1) {
 						sco = DyeColor.GREEN;
 					}
 					if(scolor == 2) {
 						sco = DyeColor.RED;
 					}
 					if(scolor == 3) {
 						sco = DyeColor.BLUE;
 					}
 					if(scolor == 4) {
 						sco = DyeColor.BLACK;
 					}	
 					if(scolor == 5) {
 						sco = DyeColor.CYAN;
 					}	
 					if(scolor == 6) {
 						sco = DyeColor.PINK;
 					}	
 					if(scolor == 7) {
 						sco = DyeColor.GRAY;
 					}
 					if(scolor == 8) {
 						sco = DyeColor.YELLOW;
 					}
 					if(scolor == 9) {
 						sco = DyeColor.BROWN;
 					}
 					if(scolor == 10) {
 						sco = DyeColor.ORANGE;
 					}
 					World world = event.getEntity().getWorld();
 					world.playEffect(event.getEntity().getLocation(), Effect.EXTINGUISH, 5); // MUAHHA
 					((Sheep) event.getEntity()).setColor(sco);
 					//attacker.sendMessage(event.getEntity().getUniqueId().toString() + " " + plugin.dasheep.toString());
 					//^ used for debuggin
 					if (attacker.getInventory().getItemInHand().getType() != null && attacker.getInventory().getItemInHand().getType() == Material.STICK)
 					{
 						int xx = event.getEntity().getLocation().getBlockX();
 		                int yy = event.getEntity().getLocation().getBlockY() +2;
 		                int zz = event.getEntity().getLocation().getBlockZ();
 		                World ww = event.getEntity().getWorld();
 		                Location lloc = new Location(ww, xx, yy, zz);
 						((Sheep) event.getEntity()).teleport(lloc); //tp up 2 so it doesn't place  block when we wipe them below!
 						 if (plugin.dablock.size() > 0) {
 							 attacker.sendMessage("Does not equal 0!");
 						        for(int i=0; i<plugin.dablock.size(); i++){
 						        	plugin.daloc.get(i).getBlock().setType(plugin.dablock.get(i)); //Doesn't seem to remove them all 100% of the time! Should I run it twice? :,(
 						        	plugin.dablock.remove(i);
 						        	plugin.daloc.remove(i); 
 						        }
 								}
 					((Sheep) event.getEntity()).setPassenger(attacker); // :O RIDE THE SHEEP!
 					}
 					scolor++;
 					if(scolor > 10) {
 						scolor = 0;
 					}
 					event.setCancelled(true);
 				}
 				}
 			}
 		}
 		@EventHandler(priority = EventPriority.NORMAL)
 		public void onShearSheep(PlayerShearEntityEvent event){	
 				if ((plugin.dasheep != null) && (event.getEntity().getUniqueId().toString() == plugin.dasheep.toString())) {	
 					event.setCancelled(true);  //You can't shear the magic sheep :,(
 				}
 		}
 		 @EventHandler (priority = EventPriority.NORMAL)
 		    public void onPlayerMove (PlayerMoveEvent event) {
 		        if (!event.isCancelled()) {
 		        	Player player = event.getPlayer();
 		        	//List nearbyentity = player.getNearbyEntities(5, 5, 5)      	
 		        	if ((plugin.dasheep != null) && player.isInsideVehicle() && player.getVehicle().getUniqueId().toString().equals(plugin.dasheep.toString())) {	
 		        		int x = player.getLocation().getBlockX();
 		                int y = player.getLocation().getBlockY() -1;
 		                int z = player.getLocation().getBlockZ();
 		                World w = player.getWorld();
 		                Location loc = new Location(w, x, y, z);
 		                if (!loc.getBlock().getType().equals(Material.WOOL) && !loc.getBlock().getType().equals(Material.AIR)) { //An attempt to save the block it was previously and change it back after the sheep moves 5 blocks.
 		                plugin.dablock.add(loc.getBlock().getType());
 		                plugin.daloc.add(loc);
 		                player.sendMessage("Not wool or air!");
 		                }
 		                if (plugin.dablock.size() > 5) {
 		                	plugin.daloc.get(0).getBlock().setType(plugin.dablock.get(0));
 		                	plugin.dablock.remove(0);
 		                	plugin.daloc.remove(0); 
 		                	player.sendMessage("THIS STRING IS GREATER THEN 5");
 		                	player.sendMessage(plugin.dablock.toString());
 		                } 
 		                if (!loc.getBlock().getType().equals(Material.AIR)) {
 		                loc.getBlock().setTypeIdAndData(35, sco.getData(), true);
 		                }
 		                
 		        	}
 		        }
 		 }
 
 	   
 				
 }
