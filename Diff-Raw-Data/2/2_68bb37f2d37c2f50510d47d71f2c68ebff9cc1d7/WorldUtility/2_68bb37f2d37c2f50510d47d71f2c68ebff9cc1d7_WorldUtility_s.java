 package edgruberman.bukkit.simpleborder;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 public class WorldUtility {
     
     /**
      *  Materials safe for a player to occupy.
      */
     private static Set<Material> safeContainers = new HashSet<Material>(Arrays.asList(new Material[] {
           Material.AIR
         , Material.WATER
         , Material.STATIONARY_WATER
         , Material.SAPLING
         , Material.YELLOW_FLOWER
         , Material.RED_ROSE
         , Material.BROWN_MUSHROOM
         , Material.RED_MUSHROOM
         , Material.TORCH
         , Material.REDSTONE_WIRE
         , Material.CROPS
         , Material.SIGN_POST
         , Material.WOODEN_DOOR
         , Material.LADDER
         , Material.RAILS
         , Material.WALL_SIGN
         , Material.LEVER
         , Material.STONE_PLATE
         , Material.IRON_DOOR_BLOCK
         , Material.WOOD_PLATE
         , Material.REDSTONE_TORCH_OFF
         , Material.REDSTONE_TORCH_ON
         , Material.STONE_BUTTON
         , Material.SNOW
         , Material.SUGAR_CANE_BLOCK
         , Material.DIODE_BLOCK_OFF
         , Material.DIODE_BLOCK_ON
         , Material.POWERED_RAIL
         , Material.DETECTOR_RAIL
     }));
     
     /**
      * Materials safe for a player to stand on and safe for a player to occupy.
      */
     private static Set<Material> safeMaterials = new HashSet<Material>(Arrays.asList(new Material[] {
           Material.WATER
         , Material.STATIONARY_WATER
         , Material.SNOW
       }));
 
     /**
      * Materials unsafe for a player to stand on.
      */
     private static Set<Material> unsafeSupports = new HashSet<Material>(Arrays.asList(new Material[] {
           Material.AIR
         , Material.LAVA
         , Material.STATIONARY_LAVA
         , Material.CACTUS
       }));
     
     private WorldUtility() {}
     
     /**
      * Find the closest safe block for a player to occupy along the Y axis.
      * @param location Initial location to start searching up and down from.
      * @return Location that is safe for a player to occupy.
      */
     public static Block getSafeY(Block block) {
         int bottom = 0, top = 127;
         
         Block below = block, above = block;
        while (below.getY() > bottom || above.getY() < top) {
             if (below != null) {
               if (isSafe(below)) return below;
             }
             
             if (above != null && above != below) {
                 if (isSafe(above)) return above;
             }
             
             if (below.getY() > bottom)
                 below = below.getRelative(BlockFace.DOWN);
             else
                 below = null;
             
             if (above.getY() < top)
                 above = above.getRelative(BlockFace.UP);
             else
                 above = null;
         }
         
         return null;
     }
     
     /**
      * Check if the block itself and the block above it are safe containers while the block below it is also safe support.
      * @param block Block to check.
      * @return True if block is a safe block to teleport player to; Otherwise false.
      */
     public static boolean isSafe(Block block)
     {
         return
                isSafeContainer(block.getRelative(BlockFace.UP))
             && isSafeContainer(block)
             && isSafeSupport(block.getRelative(BlockFace.DOWN))
         ;
     }
     
     /**
      * Determines if a block is safe for a player to occupy.
      * @param block Block to check.
      * @return True if block is safe for a player to occupy; Otherwise false.
      */
     public static boolean isSafeContainer(Block block) {
         return safeContainers.contains(block.getType());
     }
 
     /**
      * Block will support a player standing on it safely.
      * @param block Block to check.
      * @return True if block is safe.
      */
     public static boolean isSafeSupport(Block block) {
         return
             (
                 !isSafeContainer(block)                   // Block is solid
                 || safeMaterials.contains(block.getType()) //    or block is not solid but still safe to stand on.
             ) && !unsafeSupports.contains(block.getType()) // Block won't cause pain when standing on it.
         ;
     }
 }
