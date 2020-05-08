 package ch.k42.metropolis.generator.populators;
 
 import ch.k42.metropolis.minions.Constants;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.generator.BlockPopulator;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import ch.k42.metropolis.minions.XYZ;
 
 /**
  * Adds caves within the stone. Based on the Nordic world plugin.
  *
  * @author Spaceribs
  */
 public class CavePopulator extends BlockPopulator {
 
     private static final int MAX_LEVEL = Constants.BUILD_HEIGHT - 20;
 
     private enum CaveType {
         AIR,
         SAND,
         DIRT,
         LAVA,
         WATER;
 
         public static CaveType getRandom(Random random) {
             return values()[random.nextInt(values().length)];
         }
     }
 
     @Override
     public void populate(final World world, final Random random, Chunk source) {
 
         if (random.nextInt(100) < 30) {
             final int x = 4 + random.nextInt(8) + source.getX() * 16;
             final int z = 4 + random.nextInt(8) + source.getZ() * 16;
             int maxY = world.getHighestBlockYAt(x, z);
             if (maxY < 16) {
                 maxY = 32;
             }
 
             final int y = random.nextInt(maxY);
             Set<XYZ> snake = selectBlocksForCave(world, random, x, y, z);
             CaveType caveType = CaveType.getRandom(random);
             buildCave(world, snake.toArray(new XYZ[0]), caveType);
             for (XYZ block : snake) {
                 world.unloadChunkRequest(block.x / 16, block.z / 16);
             }
         }
     }
 
     static Set<XYZ> selectBlocksForCave(World world, Random random, int blockX, int blockY, int blockZ) {
         Set<XYZ> snakeBlocks = new HashSet();
 
         int airHits = 0;
         XYZ block = new XYZ();
         while (true) {
             if (airHits > 1200 || blockY > MAX_LEVEL) {
                 break;
             }
 
             if (random.nextInt(20) == 0) {
                 blockY++;
             } else if (world.getBlockAt(blockX, blockY + 2, blockZ).getType() == Material.AIR) {
                 blockY += 2;
             } else if (world.getBlockAt(blockX + 2, blockY, blockZ).getType() == Material.AIR) {
                 blockX++;
             } else if (world.getBlockAt(blockX - 2, blockY, blockZ).getType() == Material.AIR) {
                 blockX--;
             } else if (world.getBlockAt(blockX, blockY, blockZ + 2).getType() == Material.AIR) {
                 blockZ++;
             } else if (world.getBlockAt(blockX, blockY, blockZ - 2).getType() == Material.AIR) {
                 blockZ--;
             } else if (world.getBlockAt(blockX + 1, blockY, blockZ).getType() == Material.AIR) {
                 blockX++;
             } else if (world.getBlockAt(blockX - 1, blockY, blockZ).getType() == Material.AIR) {
                 blockX--;
             } else if (world.getBlockAt(blockX, blockY, blockZ + 1).getType() == Material.AIR) {
                 blockZ++;
             } else if (world.getBlockAt(blockX, blockY, blockZ - 1).getType() == Material.AIR) {
                 blockZ--;
             } else if (random.nextBoolean()) {
                 if (random.nextBoolean()) {
                     blockX++;
                 } else {
                     blockZ++;
                 }
             } else {
                 if (random.nextBoolean()) {
                     blockX--;
                 } else {
                     blockZ--;
                 }
             }
 
             if (world.getBlockTypeIdAt(blockX, blockY, blockZ) != 0) {
                 int radius = 2 + random.nextInt(4);
                 int radius2 = radius * radius + 1;
                 for (int x = -radius; x <= radius; x++) {
                     for (int y = -radius; y <= radius; y++) {
                         for (int z = -radius; z <= radius; z++) {
                             if (x * x + y * y + z * z <= radius2 && y >= 0 && y < 128) {
                                 if (world.getBlockAt(blockX + x, blockY + y, blockZ + z).getType() == Material.AIR) {
                                     airHits++;
                                 } else {
                                     block.x = blockX + x;
                                     block.y = blockY + y;
                                     block.z = blockZ + z;
                                     if (snakeBlocks.add(block)) {
                                         block = new XYZ();
                                     }
                                 }
                             }
                         }
                     }
                 }
             } else {
                 airHits++;
             }
         }
 
         return snakeBlocks;
     }
 
     static void buildCave(World world, XYZ[] snakeBlocks, CaveType caveType) {
         if (snakeBlocks.length > 0) {
             int midpoint = Math.abs(snakeBlocks[0].y + snakeBlocks[snakeBlocks.length - 1].y) / 2;
             int lavapoint = Math.min(midpoint, 24);
             for (XYZ loc : snakeBlocks) {
                 Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                 if (!block.isEmpty() && block.getType() != Material.BEDROCK) {
                     switch (caveType) {
                         case AIR:
                             block.setType(Material.AIR);
                             break;
                         case SAND:
                             block.setType(Material.SAND);
                             break;
                         case DIRT:
                             block.setType(Material.DIRT);
                             break;
                         case LAVA:
                             if (loc.y < lavapoint) {
                                 block.setType(Material.STATIONARY_LAVA);
                             } else {
                                 block.setType(Material.AIR);
                             }
                             break;
                         case WATER:
                             if (loc.y < midpoint) {
                                 block.setType(Material.STATIONARY_WATER);
                             } else {
                                 block.setType(Material.AIR);
                             }
                             break;
                     }
                 }
             }
         }
     }
 }
