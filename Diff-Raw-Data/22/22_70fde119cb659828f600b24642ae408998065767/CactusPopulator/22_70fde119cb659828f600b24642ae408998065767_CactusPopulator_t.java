 package com.xpansive.bukkit.expansiveterrain.populator;
 
 import java.util.Random;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.generator.BlockPopulator;
 
 public class CactusPopulator extends BlockPopulator {
 
     private int minHeight, maxHeight, cactusPatchRadius, cactusPatchChance,
             newCactusChance;
 
     public CactusPopulator(int minHeight, int maxHeight, int cactusPatchRadius,
             int cactusPatchChance, int newCactusChance) {
         this.minHeight = minHeight;
         this.maxHeight = maxHeight;
         this.cactusPatchRadius = cactusPatchRadius;
         this.cactusPatchChance = cactusPatchChance;
         this.newCactusChance = newCactusChance;
     }
 
     @Override
     public void populate(World world, Random random, Chunk source) {
         // Determine if we should plant anything at all
         boolean plantNewCactus = random.nextInt(100) < cactusPatchChance;
 
         int x = (source.getX() << 4) + random.nextInt(16);
         int z = (source.getZ() << 4) + random.nextInt(16);
 
         while (plantNewCactus) {
             // Pick some random coordinates
             x += random.nextInt(cactusPatchRadius * 2) - cactusPatchRadius;
             z += random.nextInt(cactusPatchRadius * 2) - cactusPatchRadius;
 
             int y = world.getHighestBlockYAt(x, z);
             Block b = world.getBlockAt(x, y, z);
 
             // Check if we're planting it on sand
             if (b.getRelative(BlockFace.DOWN).getType() == Material.SAND) {
                 // Determine the height of the cactus
                 int height = random.nextInt(maxHeight - minHeight + 1)
                         + minHeight;
 
                 for (int i = 0; i < height; i++) {
                     // Cacti can only be planted if there are no blocks adjacent
                     // to them
                     if (b.getRelative(BlockFace.NORTH).getTypeId() == 0
                             && b.getRelative(BlockFace.EAST).getTypeId() == 0
                             && b.getRelative(BlockFace.SOUTH).getTypeId() == 0
                             && b.getRelative(BlockFace.WEST).getTypeId() == 0)
                         b.getRelative(0, i, 0).setType(Material.CACTUS);
                 }
             }
            // Determine if we should continue, planting more cacti in this patch
            plantNewCactus = random.nextInt(100) < newCactusChance;
         }
     }
 
 }
