 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.Material;
 import net.minecraft.server.World;
 
 public class WorldGenLakes extends WorldGenerator {
     private int a;
 
     public WorldGenLakes(int paramInt) {
         this.a = paramInt;
     }
 
     @Override
     public boolean a(World paramWorld, Random paramRandom, int paramInt1, int paramInt2, int paramInt3) {
         paramInt1 -= 8;
         paramInt3 -= 8;
         while ((paramInt2 > 0) && (paramWorld.isEmpty(paramInt1, paramInt2, paramInt3))) {
             paramInt2--;
         }
         paramInt2 -= 4;
 
         boolean[] arrayOfBoolean = new boolean[2048];
 
         int i = paramRandom.nextInt(4) + 4;
        for (int j = 0; j < i; j++) {
             double d1 = paramRandom.nextDouble() * 6.0D + 3.0D;
             double d2 = paramRandom.nextDouble() * 4.0D + 2.0D;
             double d3 = paramRandom.nextDouble() * 6.0D + 3.0D;
 
             double d4 = paramRandom.nextDouble() * (16.0D - d1 - 2.0D) + 1.0D + d1 / 2.0D;
             double d5 = paramRandom.nextDouble() * (8.0D - d2 - 4.0D) + 2.0D + d2 / 2.0D;
             double d6 = paramRandom.nextDouble() * (16.0D - d3 - 2.0D) + 1.0D + d3 / 2.0D;
 
             for (int k = 1; k < 15; k++)
                 for (int m = 1; m < 15; m++)
                     for (int n = 1; n < 7; n++) {
                         double d7 = (k - d4) / (d1 / 2.0D);
                         double d8 = (n - d5) / (d2 / 2.0D);
                         double d9 = (m - d6) / (d3 / 2.0D);
                         double d10 = d7 * d7 + d8 * d8 + d9 * d9;
                         if (d10 >= 1.0D)
                             continue;
                         arrayOfBoolean[((k * 16 + m) * 8 + n)] = true;
                     }
         }
         int i1;
         int i2;
         for (j = 0; j < 16; j++) {
             for (i1 = 0; i1 < 16; i1++) {
                 for (i2 = 0; i2 < 8; i2++) {
                    int i3 = (arrayOfBoolean[((j * 16 + i1) * 8 + i2)] == 0) && (((j < 15) && (arrayOfBoolean[(((j + 1) * 16 + i1) * 8 + i2)] != 0)) || ((j > 0) && (arrayOfBoolean[(((j - 1) * 16 + i1) * 8 + i2)] != 0)) || ((i1 < 15) && (arrayOfBoolean[((j * 16 + (i1 + 1)) * 8 + i2)] != 0)) || ((i1 > 0) && (arrayOfBoolean[((j * 16 + (i1 - 1)) * 8 + i2)] != 0)) || ((i2 < 7) && (arrayOfBoolean[((j * 16 + i1) * 8 + (i2 + 1))] != 0)) || ((i2 > 0) && (arrayOfBoolean[((j * 16 + i1) * 8 + (i2 - 1))] != 0))) ? 1 : 0;
 
                     if (i3 != 0) {
                         Material localMaterial = paramWorld.getMaterial(paramInt1 + j, paramInt2 + i2, paramInt3 + i1);
                         if ((i2 >= 4) && (localMaterial.isLiquid()))
                             return false;
                         if ((i2 < 4) && (!localMaterial.isBuildable()) && (paramWorld.getTypeId(paramInt1 + j, paramInt2 + i2, paramInt3 + i1) != this.a))
                             return false;
                     }
                 }
             }
 
         }
 
         for (j = 0; j < 16; j++) {
             for (i1 = 0; i1 < 16; i1++) {
                 for (i2 = 0; i2 < 8; i2++) {
                    if (arrayOfBoolean[((j * 16 + i1) * 8 + i2)] != 0) {
                         paramWorld.setRawTypeId(paramInt1 + j, paramInt2 + i2, paramInt3 + i1, i2 >= 4 ? 0 : this.a);
                     }
                 }
             }
         }
 
         for (j = 0; j < 16; j++) {
             for (i1 = 0; i1 < 16; i1++) {
                 for (i2 = 4; i2 < 8; i2++) {
                    if ((arrayOfBoolean[((j * 16 + i1) * 8 + i2)] == 0) || (paramWorld.getTypeId(paramInt1 + j, paramInt2 + i2 - 1, paramInt3 + i1) != Block.DIRT.id) || (paramWorld.a(EnumSkyBlock.SKY, paramInt1 + j, paramInt2 + i2, paramInt3 + i1) <= 0))
                         continue;
                     BiomeBase localBiomeBase = paramWorld.getWorldChunkManager().getBiome(paramInt1 + j, paramInt3 + i1);
                     if (localBiomeBase.t == Block.MYCEL.id)
                         paramWorld.setRawTypeId(paramInt1 + j, paramInt2 + i2 - 1, paramInt3 + i1, Block.MYCEL.id);
                     else {
                         paramWorld.setRawTypeId(paramInt1 + j, paramInt2 + i2 - 1, paramInt3 + i1, Block.GRASS.id);
                     }
                 }
             }
 
         }
 
         if (Block.byId[this.a].material == Material.LAVA) {
             for (j = 0; j < 16; j++) {
                 for (i1 = 0; i1 < 16; i1++) {
                     for (i2 = 0; i2 < 8; i2++) {
                        int i4 = (arrayOfBoolean[((j * 16 + i1) * 8 + i2)] == 0) && (((j < 15) && (arrayOfBoolean[(((j + 1) * 16 + i1) * 8 + i2)] != 0)) || ((j > 0) && (arrayOfBoolean[(((j - 1) * 16 + i1) * 8 + i2)] != 0)) || ((i1 < 15) && (arrayOfBoolean[((j * 16 + (i1 + 1)) * 8 + i2)] != 0)) || ((i1 > 0) && (arrayOfBoolean[((j * 16 + (i1 - 1)) * 8 + i2)] != 0)) || ((i2 < 7) && (arrayOfBoolean[((j * 16 + i1) * 8 + (i2 + 1))] != 0)) || ((i2 > 0) && (arrayOfBoolean[((j * 16 + i1) * 8 + (i2 - 1))] != 0))) ? 1 : 0;
 
                         if ((i4 == 0) || ((i2 >= 4) && (paramRandom.nextInt(2) == 0)) || (!paramWorld.getMaterial(paramInt1 + j, paramInt2 + i2, paramInt3 + i1).isBuildable()))
                             continue;
                         paramWorld.setRawTypeId(paramInt1 + j, paramInt2 + i2, paramInt3 + i1, Block.STONE.id);
                     }
                 }
 
             }
 
         }
 
         if (Block.byId[this.a].material == Material.WATER) {
             for (j = 0; j < 16; j++) {
                 for (i1 = 0; i1 < 16; i1++) {
                     i2 = 4;
                     if (!paramWorld.p(paramInt1 + j, paramInt2 + i2, paramInt3 + i1))
                         continue;
                     paramWorld.setRawTypeId(paramInt1 + j, paramInt2 + i2, paramInt3 + i1, Block.ICE.id);
                 }
             }
         }
 
         return true;
     }
 }
