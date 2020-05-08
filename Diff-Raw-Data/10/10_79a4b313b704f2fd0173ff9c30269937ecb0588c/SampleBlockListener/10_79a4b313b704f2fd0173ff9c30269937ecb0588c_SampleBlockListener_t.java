 
 package agaricus.plugins.IncompatiblePlugin;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockCanBuildEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
 
 /**
  * Sample block listener
  * @author Dinnerbone
  */
 public class SampleBlockListener implements Listener {
     @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        System.out.println("CraftItemEvent "+event+", inventory="+event.getInventory()+", recipe="+event.getRecipe());
    }

    @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
        System.out.println("BlockPlaceEvent "+event+", block="+event.getBlock());
     }
 
     @EventHandler
     public void onBlockPhysics(BlockPhysicsEvent event) {
         Block block = event.getBlock();
 
         if ((block.getType() == Material.SAND) || (block.getType() == Material.GRAVEL)) {
             Block above = block.getRelative(BlockFace.UP);
             if (above.getType() == Material.IRON_BLOCK) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler
     public void onBlockCanBuild(BlockCanBuildEvent event) {
         Material mat = event.getMaterial();
 
         if (mat.equals(Material.CACTUS)) {
             event.setBuildable(true);
         }
     }
 }
