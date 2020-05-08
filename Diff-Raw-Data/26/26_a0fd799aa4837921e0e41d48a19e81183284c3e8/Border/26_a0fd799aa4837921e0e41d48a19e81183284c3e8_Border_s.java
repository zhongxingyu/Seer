 package edgruberman.bukkit.simpleborder;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 class Border {
     
     /**
      *  Materials safe for a player to occupy.
      */
     private static final Set<Material> SAFE_CONTAINERS = new HashSet<Material>(Arrays.asList(new Material[] {
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
         , Material.LONG_GRASS
         , Material.DEAD_BUSH
     }));
     
     /**
      * Materials safe for a player to stand on and safe for a player to occupy.
      */
     private static final Set<Material> SAFE_MATERIALS = new HashSet<Material>(Arrays.asList(new Material[] {
           Material.WATER
         , Material.STATIONARY_WATER
         , Material.SNOW
       }));
 
     /**
      * Materials unsafe for a player to stand on.
      */
     private static final Set<Material> UNSAFE_SUPPORTS = new HashSet<Material>(Arrays.asList(new Material[] {
           Material.AIR
         , Material.LAVA
         , Material.STATIONARY_LAVA
         , Material.CACTUS
         , Material.FIRE
       }));
     
     /**
      * Default extra distance inside a border to use when finding a location inside a border.
      */
     private static final int DEFAULT_PADDING = 3;
     
     // Factory/Manager
     static String message = "";
     static Map<World, Border> defined = new HashMap<World, Border>();
     
     // Instance Configuration
     private World world;
     private int minX;
     private int maxX;
     private int minZ;
     private int maxZ;
     private Block defaultSafe;
     
     Border(final World world, final int minX, final int maxX, final int minZ, final int maxZ, final int safeX, final int safeY, final int safeZ) {
         this.world = world;
         this.minX = minX;
         this.maxX = maxX;
         this.minZ = minZ;
         this.maxZ = maxZ;
         
         if (!this.isInside(safeX, safeZ))
             new IllegalArgumentException("Default safe coordinates are not within border.");
         
         this.defaultSafe = this.world.getBlockAt(safeX, safeY, safeZ);
         
         Border.defined.put(this.world, this);
     }
     
     /**
      * Generates text describing this border's configuration.
      * 
      * @return border configuration in human readable format
      */
     String description() {
         return "Border in [" + this.world.getName() + "]"
             + " is x:{" + this.minX + " to " + this.maxX + "}"
             + " and z:{" + this.minZ + " to " + this.maxZ + "}"
             + " with the default safe block at " + this.defaultSafe.toString();
     }
     
     /**
      * Load chunks containing a cylindrical grouping of blocks, if any part of
      * the chunk containing one of the blocks is within this border.
      * 
      * @param origin block to center cylinder around
      * @param radius number of blocks from origin to ensure are loaded in both
      *        x and z directions
      */
     void loadChunks(final Location origin, final int radius) {
         Chunk maxC = origin.clone().add(radius, 0, radius).getBlock().getChunk();
         Chunk minC = origin.clone().subtract(radius, 0, radius).getBlock().getChunk();
         Chunk c;
         for (int x = minC.getX(); x <= maxC.getX(); x++)
             for (int z = minC.getZ(); z <= maxC.getZ(); z++) {
                 c = origin.getWorld().getChunkAt(x, z);
                 if (!c.isLoaded() && this.isInside(c)) c.load();
             }
     }
     
     /**
     * Determine distance to closest border.
     * 
     * @param l location to determine distance to border from
     * @return distance to closest border
     */
    double distanceFrom(final Location l) {
        double distanceMinX = Math.abs(l.getX() - this.minX);
        double distanceMaxX = Math.abs(l.getX() - this.maxX);
        double distanceMinZ = Math.abs(l.getZ() - this.minZ);
        double distanceMaxZ = Math.abs(l.getZ() - this.maxZ);
        
        double distance = distanceMinX;
        if (distanceMaxX < distance) distance = distanceMaxX;
        if (distanceMinZ < distance) distance = distanceMinZ;
        if (distanceMaxZ < distance) distance = distanceMaxZ;
        
        return distance;
    }
    
    /**
      * Determines if chunk has any block that is found within or on the border.
      * 
      * @param c chunk to check
      * @return true if chunk has a block inside border; otherwise false
      */
     boolean isInside(final Chunk c) {
         // Check all four corners of chunk, ignoring depth.
         if (this.isInside(c.getBlock(0, 0, 0).getLocation())) return true;
         if (this.isInside(c.getBlock(15, 0, 0).getLocation())) return true;
         if (this.isInside(c.getBlock(0, 0, 15).getLocation())) return true;
         if (this.isInside(c.getBlock(15, 0, 15).getLocation())) return true;
         
         return false;
     }
     
     /**
      * Determines if location is found within or on the border.
      * 
      * @param l location to check
      * @return true if location is inside border; otherwise false
      */
     boolean isInside(final Location l) {
         return this.isInside(l.getBlockX(), l.getBlockZ());
     }
     
     /**
      * Determines if coordinates are found within or on the border.
      * 
      * @param x X axis value
      * @param z Z axis value
      * @return true if coordinates are inside border; otherwise false
      */
     boolean isInside(final int x, final int z) {
         return !(x <= this.minX || x >= this.maxX || z <= this.minZ || z >= this.maxZ);
     }
     
     /**
      * Return player to inside border.
      * 
      * @param player player to return inside
      * @param breached where player moved outside the border
      * @return location player was returned to
      */
     Location returnInside(final Player player, final Location breached) {
         Location inside = this.findSafe(breached);
         
         // Return player to the middle of the block and players in vehicles need to be shifted up.
         inside.add(0.5, (player.isInsideVehicle() ? 1 : 0), 0.5);
         
         // Send player back to inside the border.
         if (player.isInsideVehicle()) {
             player.getVehicle().setVelocity(new Vector(0, 0, 0));
             player.getVehicle().teleport(inside);
         } else {
             player.teleport(inside);
         }
         
         return inside;
     }
     
     /**
      * Returns the closest location to the target inside the border.
      * 
      * @param target location to try and get closest to
      * @param padding margin from border
      * @return new location inside border closest to target
      */ 
     Location findClosest(final Location target, final double padding) {
         Location closest = target.clone();
         
         if (closest.getX() <= (this.minX + padding))
             closest.setX(this.minX + padding);
         else if (closest.getX() >= (this.maxX - padding))
             closest.setX(this.maxX - padding);
         
         if (closest.getZ() <= (this.minZ + padding))
             closest.setZ(this.minZ + padding);
         else if (closest.getZ() >= (this.maxZ - padding))
             closest.setZ(this.maxZ - padding);
         
         return closest;
     }
     
     /**
      * Attempts to find a player safe location inside the border closest to the
      * given target. If a safe location can not be found, the default safe
      * location for this border is used.
      * 
      * @param target location to try and find the closest safe location to
      * @return safe location inside border closest to target
      */
     Location findSafe(final Location target) {
         Location closest = this.findClosest(target, Border.DEFAULT_PADDING);
         
         Block safe = Border.findSafeY(closest.getBlock());
         if (safe == null) safe = this.defaultSafe;
         
         closest.setX(safe.getX());
         closest.setY(safe.getY());
         closest.setZ(safe.getZ());
         
         return closest;
     }
     
     /**
      * Find the closest safe block for a player to occupy along the Y axis.
      * 
      * @param location Initial location to start searching up and down from.
      * @return Location that is safe for a player to occupy.
      */
     private static Block findSafeY(final Block block) {
         int bottom = 0, top = block.getWorld().getMaxHeight() - 1;
         
         Block below = block, above = block;
         while ((below != null && below.getY() > bottom) || (above != null && above.getY() < top)) {
             
             if (below != null && isSafe(below)) return below;
             
             if (above != null && above != below && isSafe(above)) return above;
             
             // Get next block down to check, unless we've run out of blocks below already.
             if (below != null) {
                 if (below.getY() > bottom)
                     below = below.getRelative(BlockFace.DOWN);
                 else
                     // No more blocks below.
                     below = null;
             }
             
             // Get next block above to check, unless we've run out of blocks above already.
             if (above != null) {
                 if (above.getY() < top)
                     above = above.getRelative(BlockFace.UP);
                 else
                     // No more blocks above.
                     above = null;
             }
         }
         
         return null;
     }
     
     /**
      * Check if the block itself and the block above it are safe containers while the block below it is also safe support.
      * 
      * @param block Block to check.
      * @return True if block is a safe block to teleport player to; Otherwise false.
      */
     private static boolean isSafe(final Block block) {
         return isSafeContainer(block.getRelative(BlockFace.UP))
             && isSafeContainer(block)
             && isSafeSupport(block.getRelative(BlockFace.DOWN))
         ;
     }
     
     /**
      * Determines if a block is safe for a player to occupy.
      * 
      * @param block Block to check.
      * @return True if block is safe for a player to occupy; Otherwise false.
      */
     private static boolean isSafeContainer(final Block block) {
         return SAFE_CONTAINERS.contains(block.getType());
     }
 
     /**
      * Block will support a player standing on it safely.
      * 
      * @param block Block to check.
      * @return True if block is safe.
      */
     private static boolean isSafeSupport(final Block block) {
         return (
                 !isSafeContainer(block)                     // Block is solid
                 || SAFE_MATERIALS.contains(block.getType()) //    or block is not solid but still safe to stand on.
             ) && !UNSAFE_SUPPORTS.contains(block.getType()) // Block won't cause pain when standing on it.
         ;
     }
 }
