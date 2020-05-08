 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.Material;
 import net.minecraft.server.World;
 
 public class WorldGenSwampTree extends WorldGenerator {
     @Override
     public boolean a(World paramWorld, Random paramRandom, int paramInt1, int paramInt2, int paramInt3) {
         int i = paramRandom.nextInt(4) + 5;
         while (paramWorld.getMaterial(paramInt1, paramInt2 - 1, paramInt3) == Material.WATER) {
             paramInt2--;
         }
         int j = 1;
         if ((paramInt2 < 1) || (paramInt2 + i + 1 > paramWorld.height))
             return false;
         int n;
         int i1;
         int i2;
        for (int k = paramInt2; k <= paramInt2 + 1 + i; k++) {
             m = 1;
             if (k == paramInt2)
                 m = 0;
             if (k >= paramInt2 + 1 + i - 2)
                 m = 3;
             for (n = paramInt1 - m; (n <= paramInt1 + m) && (j != 0); n++) {
                 for (i1 = paramInt3 - m; (i1 <= paramInt3 + m) && (j != 0); i1++) {
                     if ((k >= 0) && (k < paramWorld.height)) {
                         i2 = paramWorld.getTypeId(n, k, i1);
                         if ((i2 != 0) && (i2 != Block.LEAVES.id))
                             if ((i2 == Block.STATIONARY_WATER.id) || (i2 == Block.WATER.id)) {
                                 if (k > paramInt2)
                                     j = 0;
                             } else
                                 j = 0;
                     } else {
                         j = 0;
                     }
                 }
             }
         }
 
         if (j == 0)
             return false;
 
         k = paramWorld.getTypeId(paramInt1, paramInt2 - 1, paramInt3);
         if (((k != Block.GRASS.id) && (k != Block.DIRT.id)) || (paramInt2 >= paramWorld.height - i - 1))
             return false;
 
         paramWorld.setRawTypeId(paramInt1, paramInt2 - 1, paramInt3, Block.DIRT.id);
         int i3;
        for (int m = paramInt2 - 3 + i; m <= paramInt2 + i; m++) {
             n = m - (paramInt2 + i);
             i1 = 2 - n / 2;
             for (i2 = paramInt1 - i1; i2 <= paramInt1 + i1; i2++) {
                 i3 = i2 - paramInt1;
                 for (int i4 = paramInt3 - i1; i4 <= paramInt3 + i1; i4++) {
                     int i5 = i4 - paramInt3;
                    if (((Math.abs(i3) != i1) || (Math.abs(i5) != i1) || ((paramRandom.nextInt(2) != 0) && (n != 0))) && (Block.o[paramWorld.getTypeId(i2, m, i4)] == 0))
                         paramWorld.setRawTypeId(i2, m, i4, Block.LEAVES.id);
                 }
             }
         }
 
         for (m = 0; m < i; m++) {
             n = paramWorld.getTypeId(paramInt1, paramInt2 + m, paramInt3);
             if ((n != 0) && (n != Block.LEAVES.id) && (n != Block.WATER.id) && (n != Block.STATIONARY_WATER.id))
                 continue;
             paramWorld.setRawTypeId(paramInt1, paramInt2 + m, paramInt3, Block.LOG.id);
         }
 
         for (m = paramInt2 - 3 + i; m <= paramInt2 + i; m++) {
             n = m - (paramInt2 + i);
             i1 = 2 - n / 2;
             for (i2 = paramInt1 - i1; i2 <= paramInt1 + i1; i2++) {
                 for (i3 = paramInt3 - i1; i3 <= paramInt3 + i1; i3++) {
                     if (paramWorld.getTypeId(i2, m, i3) == Block.LEAVES.id) {
                         if ((paramRandom.nextInt(4) == 0) && (paramWorld.getTypeId(i2 - 1, m, i3) == 0)) {
                             a(paramWorld, i2 - 1, m, i3, 8);
                         }
                         if ((paramRandom.nextInt(4) == 0) && (paramWorld.getTypeId(i2 + 1, m, i3) == 0)) {
                             a(paramWorld, i2 + 1, m, i3, 2);
                         }
                         if ((paramRandom.nextInt(4) == 0) && (paramWorld.getTypeId(i2, m, i3 - 1) == 0)) {
                             a(paramWorld, i2, m, i3 - 1, 1);
                         }
                         if ((paramRandom.nextInt(4) == 0) && (paramWorld.getTypeId(i2, m, i3 + 1) == 0)) {
                             a(paramWorld, i2, m, i3 + 1, 4);
                         }
                     }
                 }
             }
         }
         return true;
     }
 
     private void a(World paramWorld, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
         paramWorld.setTypeIdAndData(paramInt1, paramInt2, paramInt3, Block.VINE.id, paramInt4);
         int i = 4;
         while (true) {
             paramInt2--;
             if ((paramWorld.getTypeId(paramInt1, paramInt2, paramInt3) != 0) || (i <= 0))
                 break;
             paramWorld.setTypeIdAndData(paramInt1, paramInt2, paramInt3, Block.VINE.id, paramInt4);
             i--;
         }
     }
 }
