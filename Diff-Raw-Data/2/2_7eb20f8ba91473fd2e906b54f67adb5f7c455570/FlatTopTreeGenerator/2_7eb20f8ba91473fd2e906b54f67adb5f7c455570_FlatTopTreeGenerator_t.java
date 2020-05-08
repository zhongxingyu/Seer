 package com.xpansive.bukkit.expansiveterrain.structure.tree;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 
 import com.xpansive.bukkit.expansiveterrain.structure.tree.TreeGenerator;
 
 public class FlatTopTreeGenerator implements TreeGenerator {
 
    private int minHeight, maxHeight, minRadius, maxRadius, vineChance;
 
     public FlatTopTreeGenerator(int minHeight, int maxHeight, int minRadius, int maxRadius, int vineChance) {
         this.minHeight = minHeight;
         this.maxHeight = maxHeight;
         this.minRadius = minRadius;
         this.maxRadius = maxRadius;
         this.vineChance = vineChance;
     }
 
     public boolean generate(World bukkitWorld, Random rand, int x, int y, int z) {
         // Use NMS code for speed
         net.minecraft.server.World world = ((CraftWorld) bukkitWorld).getHandle();
 
         if (world.getTypeId(x, y - 1, z) != Material.GRASS.getId()) {
             return false;
         }
 
         int height = rand.nextInt(maxHeight - minHeight + 1) + minHeight;
         int radius = rand.nextInt(maxRadius - minRadius + 1) + minRadius;
 
         // Place the leaves
         for (int cx = -radius; cx < radius; cx++) {
             for (int cz = -radius; cz < radius; cz++) {
                 for (int cy = 0; cy < radius / 3; cy++) {
                     int dist = (int) Math.sqrt(cx * cx + cy * cy * 16 + cz * cz);
                     if (dist < radius) {
                         world.setRawTypeId(x + cx, y + height + cy, z + cz, Material.LEAVES.getId());
                     }
                 }
             }
         }
         
         // Place the vines on the outside
         for (int cx = -radius - 1; cx < radius + 1; cx++) {
             for (int cz = -radius - 1; cz < radius + 1; cz++) {
                 int dist = (int) Math.sqrt(cx * cx + cz * cz);
                 if (dist == radius && rand.nextInt(100) < vineChance) {
                     placeVine(world, rand, x + cx, y + height, z + cz, rand.nextInt(height));
                 }
             }
         }
         
         // Make the branches
         for (int i = 0; i < radius; i++) { // Use the radius as the number of branches
             int cx = 0, cz = 0;
             // Pick a random direction
             int dirX = rand.nextInt(3) - 1;
             int dirZ = rand.nextInt(3) - 1;
 
             for (int j = 0; j < radius; j++) { //Also use the radius as the length of the branch
                 int dist = (int) Math.sqrt(cx * cx + cz * cz);
                 if (dist < radius - 4) { // Subtract 4 to make sure we don't go completely to the edge
                     if (rand.nextInt(100) < 15) { //15% chance of a direction change
                         dirX = rand.nextInt(3) - 1;
                         dirZ = rand.nextInt(3) - 1;
                     }
                     cx += dirX;
                     cz += dirZ;
                     int cy = y + Math.min(height - (radius - j) / 3 + 1, height);
                     
                     //Make the branches rise up as they get longer, also use dark logs
                     world.setRawTypeIdAndData(x + cx, cy, z + cz, Material.LOG.getId(), 1);
                     if (rand.nextInt(100) < vineChance) {
                         placeVine(world, rand, x + cx + (rand.nextInt(3) - 1), cy, z + cz  + (rand.nextInt(3) - 1), rand.nextInt(height));
                     }
                 }
             }
         }
         
         // Make the trunk
         for (int cy = 0; cy < height + 1; cy++) {
             world.setRawTypeIdAndData(x, y + cy, z, Material.LOG.getId(), 1); // Dark logs
         }
         
         return true;
     }
     
     private void placeVine(net.minecraft.server.World world, Random rand, int x, int y, int z, int height) {
         // We can't place vines if this block is filled
         if (world.getTypeId(x, y, z) != 0) return;
         
         // Don't rely on these direction names, they're probably off
         int data = 0;
         int west = world.getTypeId(x, y, z - 1);
         int east = world.getTypeId(x, y, z + 1);
         int south = world.getTypeId(x + 1, y, z);
         int north = world.getTypeId(x - 1, y, z);
         int vine = Material.VINE.getId();
         
         // Set the data based on the surrounding blocks
         if (west != 0 && west != vine) data |= 4;
         if (east != 0 && east != vine) data |= 1;
         if (south != 0 && south != vine) data |= 8;
         if (north != 0 && north != vine) data |= 2;
         
         if (data != 0) {
             for (int cy = 0; cy < height && world.getTypeId(x, y - cy, z) == 0; cy++)
                 world.setRawTypeIdAndData(x, y - cy, z, Material.VINE.getId(), data);
         }
     }
 }
