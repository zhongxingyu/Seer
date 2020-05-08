 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.World;
 
 import org.bukkit.BlockChangeDelegate;
 import org.bukkit.Bukkit;
 import org.bukkit.block.BlockState;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.event.world.StructureGrowEvent;
 
 public class WorldGenTrees extends WorldGenerator {
     public WorldGenTrees(boolean flag) {
         super(flag);
     }
 
     public boolean a(World world, Random random, int i, int j, int k) {
         return generate((BlockChangeDelegate) world, random, i, j, k, null, null, world.getWorld());
     }
 
     public boolean generate(BlockChangeDelegate world, Random random, int i, int j, int k, StructureGrowEvent event, ItemStack itemstack, CraftWorld bukkitWorld) {
         int l = random.nextInt(3) + 4;
         boolean flag = true;
 
         if ((j >= 1) && (j + l + 1 <= world.getHeight())) {
             int i1;
             for (i1 = j; i1 <= j + 1 + l; i1++) {
                 byte b0 = 1;
 
                 if (i1 == j) {
                     b0 = 0;
                 }
 
                 if (i1 >= j + 1 + l - 2) {
                     b0 = 2;
                 }
 
                 for (int j1 = i - b0; (j1 <= i + b0) && (flag); j1++) {
                     for (int k1 = k - b0; (k1 <= k + b0) && (flag); k1++) {
                         if ((i1 >= 0) && (i1 < world.getHeight())) {
                             int l1 = world.getTypeId(j1, i1, k1);
                             if ((l1 != 0) && (l1 != Block.LEAVES.id))
                                 flag = false;
                         } else {
                             flag = false;
                         }
                     }
                 }
             }
 
             if (!flag) {
                 return false;
             }
             i1 = world.getTypeId(i, j - 1, k);
             if (((i1 == Block.GRASS.id) || (i1 == Block.DIRT.id)) && (j < world.getHeight() - l - 1)) {
                 if (event == null) {
                     world.setRawTypeId(i, j - 1, k, Block.DIRT.id);
                 } else {
                     BlockState dirtState = bukkitWorld.getBlockAt(i, j - 1, k).getState();
                     dirtState.setTypeId(Block.DIRT.id);
                     event.getBlocks().add(dirtState);
                 }
 
                 int i2;
                 for (i2 = j - 3 + l; i2 <= j + l; i2++) {
                     int j1 = i2 - (j + l);
                     int k1 = 1 - j1 / 2;
 
                     for (int l1 = i - k1; l1 <= i + k1; l1++) {
                         int j2 = l1 - i;
 
                         for (int k2 = k - k1; k2 <= k + k1; k2++) {
                             int l2 = k2 - k;
 
                             if (((Math.abs(j2) == k1) && (Math.abs(l2) == k1) && ((random.nextInt(2) == 0) || (j1 == 0))) || (Block.o[world.getTypeId(l1, i2, k2)] != false))
                                 continue;
                             if (event == null) {
                                 a(world, l1, i2, k2, Block.LEAVES.id, 0);
                             } else {
                                 BlockState leavesState = bukkitWorld.getBlockAt(l1, i2, k2).getState();
                                 leavesState.setTypeId(Block.LEAVES.id);
                                 event.getBlocks().add(leavesState);
                             }
                         }
 
                     }
 
                 }
 
                 for (i2 = 0; i2 < l; i2++) {
                     int j1 = world.getTypeId(i, j + i2, k);
                     if ((j1 != 0) && (j1 != Block.LEAVES.id))
                         continue;
                     if (event == null) {
                         a(world, i, j + i2, k, Block.LOG.id, 0);
                     } else {
                         BlockState logState = bukkitWorld.getBlockAt(i, j + i2, k).getState();
                         logState.setTypeId(Block.LOG.id);
                         event.getBlocks().add(logState);
                     }
 
                 }
 
                 if (event != null) {
                     Bukkit.getPluginManager().callEvent(event);
                     if (!event.isCancelled()) {
                         for (BlockState state : event.getBlocks()) {
                             state.update(true);
                         }
                         if ((event.isFromBonemeal()) && (itemstack != null)) {
                             itemstack.count -= 1;
                         }
                     }
                 }
 
                 return true;
             }
             return false;
         }
 
         return false;
     }
 }
