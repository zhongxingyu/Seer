 package com.djdch.bukkit.onehundredgenerator.mc100;
 
import java.util.List;
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.World;
 
 import org.bukkit.BlockChangeDelegate;
 import org.bukkit.Bukkit;
 import org.bukkit.block.BlockState;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.event.world.StructureGrowEvent;
 import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginManager;
 
 public class WorldGenTaiga2 extends WorldGenerator {
     public WorldGenTaiga2(boolean flag) {
         super(flag);
     }
 
     @Override
     public boolean a(World world, Random random, int i, int j, int k) {
         return generate((BlockChangeDelegate) world, random, i, j, k, null, null, world.getWorld());
     }
 
     public boolean generate(BlockChangeDelegate world, Random random, int i, int j, int k, StructureGrowEvent event, ItemStack itemstack, CraftWorld bukkitWorld) {
         int l = random.nextInt(4) + 6;
         int i1 = 1 + random.nextInt(2);
         int j1 = l - i1;
         int k1 = 2 + random.nextInt(2);
         boolean flag = true;
 
         if ((j >= 1) && (j + l + 1 <= world.getHeight())) {
            for (int l1 = j; (l1 <= j + 1 + l) && (flag); l1++) {
                boolean flag1 = true;
                int k2;
                 int k2;
                 if (l1 - j < i1)
                     k2 = 0;
                 else {
                     k2 = k1;
                 }
 
                 for (int i2 = i - k2; (i2 <= i + k2) && (flag); i2++) {
                     for (int l2 = k - k2; (l2 <= k + k2) && (flag); l2++) {
                         if ((l1 >= 0) && (l1 < world.getHeight())) {
                             int j2 = world.getTypeId(i2, l1, l2);
                             if ((j2 != 0) && (j2 != Block.LEAVES.id))
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
             l1 = world.getTypeId(i, j - 1, k);
             if (((l1 == Block.GRASS.id) || (l1 == Block.DIRT.id)) && (j < world.getHeight() - l - 1)) {
                 if (event == null) {
                     world.setRawTypeId(i, j - 1, k, Block.DIRT.id);
                 } else {
                     BlockState dirtState = bukkitWorld.getBlockAt(i, j - 1, k).getState();
                     dirtState.setTypeId(Block.DIRT.id);
                     event.getBlocks().add(dirtState);
                 }
 
                 int k2 = random.nextInt(2);
                 int i2 = 1;
                 byte b0 = 0;
 
                for (int j2 = 0; j2 <= j1; j2++) {
                     int j3 = j + l - j2;
 
                     for (int i3 = i - k2; i3 <= i + k2; i3++) {
                         int k3 = i3 - i;
 
                         for (int l3 = k - k2; l3 <= k + k2; l3++) {
                             int i4 = l3 - k;
 
                            if (((Math.abs(k3) == k2) && (Math.abs(i4) == k2) && (k2 > 0)) || (Block.o[world.getTypeId(i3, j3, l3)] != 0))
                                 continue;
                             if (event == null) {
                                 a(world, i3, j3, l3, Block.LEAVES.id, 1);
                             } else {
                                 BlockState leavesState = bukkitWorld.getBlockAt(i3, j3, l3).getState();
                                 leavesState.setTypeId(Block.LEAVES.id);
                                leavesState.setData(new MaterialData(Block.LEAVES.id, 1));
                                 event.getBlocks().add(leavesState);
                             }
 
                         }
 
                     }
 
                     if (k2 >= i2) {
                         k2 = b0;
                         b0 = 1;
                         i2++;
                         if (i2 > k1)
                             i2 = k1;
                     } else {
                         k2++;
                     }
                 }
 
                 j2 = random.nextInt(3);
 
                 for (int j3 = 0; j3 < l - j2; j3++) {
                     int i3 = world.getTypeId(i, j + j3, k);
                     if ((i3 != 0) && (i3 != Block.LEAVES.id))
                         continue;
                     if (event == null) {
                         a(world, i, j + j3, k, Block.LOG.id, 1);
                     } else {
                         BlockState logState = bukkitWorld.getBlockAt(i, j + j3, k).getState();
                         logState.setTypeId(Block.LOG.id);
                        logState.setData(new MaterialData(Block.LOG.id, 1));
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
