 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.World;
 
 import org.bukkit.BlockChangeDelegate;
 
 public class WorldGenTaiga1 extends WorldGenerator {
     @Override
     public boolean a(World world, Random random, int i, int j, int k) {
         return generate((BlockChangeDelegate) world, random, i, j, k);
     }
 
     public boolean generate(BlockChangeDelegate world, Random random, int i, int j, int k) {
         int l = random.nextInt(5) + 7;
         int i1 = l - random.nextInt(2) - 3;
         int j1 = l - i1;
         int k1 = 1 + random.nextInt(j1 + 1);
         boolean flag = true;
 
        int l1;
         if ((j >= 1) && (j + l + 1 <= world.getHeight())) {
            for (l1 = j; (l1 <= j + 1 + l) && (flag); l1++) {
                 int l2;
                 if (l1 - j < i1)
                     l2 = 0;
                 else {
                     l2 = k1;
                 }
 
                 for (int i2 = i - l2; (i2 <= i + l2) && (flag); i2++) {
                     for (int j2 = k - l2; (j2 <= k + l2) && (flag); j2++) {
                         if ((l1 >= 0) && (l1 < world.getHeight())) {
                             int k2 = world.getTypeId(i2, l1, j2);
                             if ((k2 != 0) && (k2 != Block.LEAVES.id))
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
                 world.setRawTypeId(i, j - 1, k, Block.DIRT.id);
                 int l2 = 0;
 
                int i2;
                for (i2 = j + l; i2 >= j + i1; i2--) {
                     for (int j2 = i - l2; j2 <= i + l2; j2++) {
                         int k2 = j2 - i;
 
                         for (int i3 = k - l2; i3 <= k + l2; i3++) {
                             int j3 = i3 - k;
 
                            if (((Math.abs(k2) != l2) || (Math.abs(j3) != l2) || (l2 <= 0)) && (Block.o[world.getTypeId(j2, i2, i3)] == false)) {
                                 world.setRawTypeIdAndData(j2, i2, i3, Block.LEAVES.id, 1);
                             }
                         }
                     }
 
                     if ((l2 >= 1) && (i2 == j + i1 + 1))
                         l2--;
                     else if (l2 < k1) {
                         l2++;
                     }
                 }
 
                 for (i2 = 0; i2 < l - 1; i2++) {
                     int j2 = world.getTypeId(i, j + i2, k);
                     if ((j2 == 0) || (j2 == Block.LEAVES.id)) {
                         world.setRawTypeIdAndData(i, j + i2, k, Block.LOG.id, 1);
                     }
                 }
 
                 return true;
             }
             return false;
         }
 
         return false;
     }
 }
