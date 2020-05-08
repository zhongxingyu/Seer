 package zerothindex.clancraft.bukkit;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 
 public class EntityListener implements Listener {
 	
 	@EventHandler
 	public void onEntityPrime(ExplosionPrimeEvent e) {
 		e.setRadius(2.1f); //smaller explosion radius
 	}
 	
 	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event) {
 		/* The flow of block destruction:
 		 * 
 		 *  OBSIDIAN* > LAPIS BLOCK > SMOOTH BRICK > COBBLESTONE (and everything else) > AIR
 		 *  
 		 *  Obsidian will be transformed to lapis block only if directly adjacent to tnt
 		 */
 		if (event.isCancelled()) return;
 		// if explosion isnt from tnt, dont do block damage
 		if (!(event.getEntity() instanceof TNTPrimed)) {
 			event.blockList().clear();
 		}
 		//Get a list of all blocks to be destroyed
 		List<Block> blist = event.blockList();
 		Iterator<Block> iter = blist.iterator();
 		while (iter.hasNext()) {
 			Block b = iter.next();
 			boolean dontblow = false;
 			if (b.getType() == Material.SMOOTH_BRICK) {
 				//Set smooth brick or mossy smooth brick to cobble
 				b.setType(Material.COBBLESTONE);
 				dontblow = true;
 				
 			} else if (b.getType() == Material.LAPIS_BLOCK) {
 				dontblow = true;
 				if (b.getLocation().distance(event.getLocation().getBlock().getLocation()) <= 2.0) {
 					b.setType(Material.SMOOTH_BRICK);
 				}
 				
 			} else if (b.getType() == Material.IRON_DOOR_BLOCK) {
 				dontblow = true;
 				
 				//Check for obsidian beneath this door-block or the door-block beneath it
 				/*
 				if (b.getRelative(0, -1, 0).getType() == Material.LAPIS_BLOCK) {
 					b.getRelative(0, -1, 0).setType(Material.SMOOTH_BRICK);
 				} else if (b.getRelative(0, -1, 0).getType() == Material.OBSIDIAN) {
 					b.getRelative(0, -1, 0).setType(Material.LAPIS_BLOCK);
 				} else if (b.getRelative(0, -2, 0).getType() == Material.LAPIS_BLOCK) {
 					b.getRelative(0, -2, 0).setType(Material.SMOOTH_BRICK);
 				} else if (b.getRelative(0, -2, 0).getType() == Material.OBSIDIAN) {
 					b.getRelative(0, -2, 0).setType(Material.LAPIS_BLOCK);
 				}*/
 			}
 			if (dontblow) {
 				iter.remove();
 			}
 		}
 		//do obsidian directly adjacent blockssearch (not in blockList)
 		Block center = event.getLocation().getBlock();
 		for (int x = -1; x <= 1; x++) {
 			for (int y = -1; y <= 1; y++) {
 				for (int z = -1; z <= 1; z++) {
 					if ( (Math.abs(x)+Math.abs(y)+Math.abs(z) == 1)
 							&& center.getRelative(x,y,z).getType() == Material.OBSIDIAN) {
 						center.getRelative(x,y,z).setType(Material.LAPIS_BLOCK);
 					}
 				}
 			}
 		}
 	}
 }
