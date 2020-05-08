 package net.minecraft.server;
 
 public class ItemWorldMap extends ItemWorldMapBase
 {
     protected ItemWorldMap(int var1)
     {
         super(var1);
         this.a(true);
         this.e(64);
     }
 
     public WorldMap getSavedMap(ItemStack var1, World var2)
     {
         WorldMap var3 = (WorldMap)var2.a(WorldMap.class, "map_" + var1.getData());
 
         if (var3 == null)
         {
             var1.setData(var2.b("map"));
             String var4 = "map_" + var1.getData();
             var3 = new WorldMap(var4);
 
             if (mod_HangableMaps.shouldCenterMapsOnPlayer)
             {
                 var3.centerX = var2.getWorldData().c();
                 var3.centerZ = var2.getWorldData().e();
             }
             else
             {
                 var3.centerX = 0;
                 var3.centerZ = 0;
             }
 
             var3.scale = 3;
             var3.map = (byte)var2.worldProvider.dimension;
             var3.a();
             var2.a(var4, var3);
         }
 
         return var3;
     }
 
     public void a(World var1, Entity var2, WorldMap var3)
     {
         if (var1.worldProvider.dimension == var3.map)
         {
             short var4 = 128;
             short var5 = 128;
             int var6 = 1 << var3.scale;
             int var7 = var3.centerX;
             int var8 = var3.centerZ;
             int var9 = MathHelper.floor(var2.locX - (double)var7) / var6 + var4 / 2;
             int var10 = MathHelper.floor(var2.locZ - (double)var8) / var6 + var5 / 2;
             int var11 = 128 / var6;
 
             if (var1.worldProvider.e)
             {
                 var11 /= 2;
             }
 
            ++var3.field_28159_g;
 
             for (int var12 = var9 - var11 + 1; var12 < var9 + var11; ++var12)
             {
                if ((var12 & 15) == (var3.field_28159_g & 15))
                 {
                     int var13 = 255;
                     int var14 = 0;
                     double var15 = 0.0D;
 
                     for (int var17 = var10 - var11 - 1; var17 < var10 + var11; ++var17)
                     {
                         if (var12 >= 0 && var17 >= -1 && var12 < var4 && var17 < var5)
                         {
                             int var18 = var12 - var9;
                             int var19 = var17 - var10;
                             boolean var20 = var18 * var18 + var19 * var19 > (var11 - 2) * (var11 - 2);
                             int var21 = (var7 / var6 + var12 - var4 / 2) * var6;
                             int var22 = (var8 / var6 + var17 - var5 / 2) * var6;
                             byte var23 = 0;
                             byte var24 = 0;
                             byte var25 = 0;
                             int[] var26 = new int[256];
                             Chunk var27 = var1.getChunkAtWorldCoords(var21, var22);
                             int var28 = var21 & 15;
                             int var29 = var22 & 15;
                             int var30 = 0;
                             double var31 = 0.0D;
                             int var34;
                             int var35;
                             int var33;
                             int var36;
 
                             if (var1.worldProvider.e)
                             {
                                 var35 = var21 + var22 * 231871;
                                 var35 = var35 * var35 * 31287121 + var35 * 11;
 
                                 if ((var35 >> 20 & 1) == 0)
                                 {
                                     var26[Block.DIRT.id] += 10;
                                 }
                                 else
                                 {
                                     var26[Block.STONE.id] += 10;
                                 }
 
                                 var31 = 100.0D;
                             }
                             else
                             {
                                 for (var35 = 0; var35 < var6; ++var35)
                                 {
                                     for (var33 = 0; var33 < var6; ++var33)
                                     {
                                         var34 = var27.b(var35 + var28, var33 + var29) + 1;
                                         int var37 = 0;
 
                                         if (var34 > 1)
                                         {
                                             boolean var38 = false;
 
                                             do
                                             {
                                                 var38 = true;
                                                 var37 = var27.getTypeId(var35 + var28, var34 - 1, var33 + var29);
 
                                                 if (var37 == 0)
                                                 {
                                                     var38 = false;
                                                 }
                                                 else if (var34 > 0 && var37 > 0 && Block.byId[var37].material.F == MaterialMapColor.b)
                                                 {
                                                     var38 = false;
                                                 }
 
                                                 if (!var38)
                                                 {
                                                     --var34;
                                                     var37 = var27.getTypeId(var35 + var28, var34 - 1, var33 + var29);
                                                 }
                                             }
                                             while (var34 > 0 && !var38);
 
                                             if (var34 > 0 && var37 != 0 && Block.byId[var37].material.isLiquid())
                                             {
                                                 var36 = var34 - 1;
                                                 boolean var39 = false;
                                                 int var40;
 
                                                 do
                                                 {
                                                     var40 = var27.getTypeId(var35 + var28, var36--, var33 + var29);
                                                     ++var30;
                                                 }
                                                 while (var36 > 0 && var40 != 0 && Block.byId[var40].material.isLiquid());
                                             }
                                         }
 
                                         var31 += (double)var34 / (double)(var6 * var6);
                                         ++var26[var37];
                                     }
                                 }
                             }
 
                             var30 /= var6 * var6;
                             int var10000 = var23 / (var6 * var6);
                             var10000 = var24 / (var6 * var6);
                             var10000 = var25 / (var6 * var6);
                             var35 = 0;
                             var33 = 0;
 
                             for (var34 = 0; var34 < 256; ++var34)
                             {
                                 if (var26[var34] > var35)
                                 {
                                     var33 = var34;
                                     var35 = var26[var34];
                                 }
                             }
 
                             double var46 = (var31 - var15) * 4.0D / (double)(var6 + 4) + ((double)(var12 + var17 & 1) - 0.5D) * 0.4D;
                             byte var45 = 1;
 
                             if (var46 > 0.6D)
                             {
                                 var45 = 2;
                             }
 
                             if (var46 < -0.6D)
                             {
                                 var45 = 0;
                             }
 
                             var36 = 0;
 
                             if (var33 > 0)
                             {
                                 MaterialMapColor var41 = Block.byId[var33].material.F;
 
                                 if (var41 == MaterialMapColor.n)
                                 {
                                     double var42 = (double)var30 * 0.1D + (double)(var12 + var17 & 1) * 0.2D;
                                     var45 = 1;
 
                                     if (var42 < 0.5D)
                                     {
                                         var45 = 2;
                                     }
 
                                     if (var42 > 0.9D)
                                     {
                                         var45 = 0;
                                     }
                                 }
 
                                 var36 = var41.q;
                             }
 
                             var15 = var31;
 
                             if (var17 >= 0 && var18 * var18 + var19 * var19 < var11 * var11 && (!var20 || (var12 + var17 & 1) != 0))
                             {
                                 byte var44 = var3.colors[var12 + var17 * var4];
                                 byte var47 = (byte)(var36 * 4 + var45);
 
                                 if (var44 != var47)
                                 {
                                     if (var13 > var17)
                                     {
                                         var13 = var17;
                                     }
 
                                     if (var14 < var17)
                                     {
                                         var14 = var17;
                                     }
 
                                     var3.colors[var12 + var17 * var4] = var47;
                                 }
                             }
                         }
                     }
 
                     if (var13 <= var14)
                     {
                        var3.func_28153_a(var12, var13, var14);
                     }
                 }
             }
         }
     }
 
     /**
      * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
      * update it's contents.
      */
     public void a(ItemStack var1, World var2, Entity var3, int var4, boolean var5)
     {
         if (!var2.isStatic)
         {
             WorldMap var6 = this.getSavedMap(var1, var2);
 
             if (var3 instanceof EntityHuman)
             {
                 EntityHuman var7 = (EntityHuman)var3;
                var6.func_28155_a(var7, var1);
             }
 
             if (var5)
             {
                 this.a(var2, var3, var6);
             }
         }
     }
 
     /**
      * Called when item is crafted/smelted. Used only by maps so far.
      */
     public void d(ItemStack var1, World var2, EntityHuman var3)
     {
         WorldMap var4 = null;
         String var5 = "";
 
         if (var1.getData() == -1)
         {
             var1.setData(var2.b("map"));
             var5 = "map_" + var1.getData();
             var4 = new WorldMap(var5);
 
             if (mod_HangableMaps.shouldCenterMapsOnPlayer)
             {
                 var4.centerX = MathHelper.floor(var3.locX);
                 var4.centerZ = MathHelper.floor(var3.locZ);
             }
             else
             {
                 double var6 = var3.locX / 1024.0D;
                 double var8 = var3.locZ / 1024.0D;
                 int var10 = (int)Math.round(var6);
                 int var11 = (int)Math.round(var8);
                 var10 *= 1024;
                 var11 *= 1024;
                 var4.centerX = var10;
                 var4.centerZ = var11;
             }
 
             var4.scale = 3;
             var4.map = (byte)var2.worldProvider.dimension;
             var4.a();
         }
         else
         {
             var5 = "map_" + var1.getData();
             var4 = this.getSavedMap(var1, var2);
         }
 
         var2.a(var5, var4);
     }
 
     public Packet c(ItemStack var1, World var2, EntityHuman var3)
     {
        byte[] var4 = this.getSavedMap(var1, var2).func_28154_a(var1, var2, var3);
         return var4 == null ? null : new Packet131ItemData((short)Item.MAP.id, (short)var1.getData(), var4);
     }
 
     public Packet getUpdatePacket2(ItemStack var1, World var2)
     {
         WorldMap var3 = this.getSavedMap(var1, var2);
 
        if (var3.field_28158_h.size() >= 1)
         {
            WorldMapHumanTracker var4 = (WorldMapHumanTracker)var3.field_28158_h.get(0);
 
             if (var4 != null)
             {
                byte[] var5 = var4.func_28118_a(var1);
 
                 if (var5 == null)
                 {
                     return null;
                 }
 
                 return new Packet131ItemData((short)Item.MAP.id, (short)var1.getData(), var5);
             }
         }
 
         return null;
     }
 
     /**
      * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
      * True if something happen and false if it don't. This is for ITEMS, not BLOCKS !
      */
     public boolean interactWith(ItemStack var1, EntityHuman var2, World var3, int var4, int var5, int var6, int var7)
     {
         int var8 = 0;
         float var9 = (float)var4;
         float var10 = (float)var5;
         float var11 = (float)var6;
         float var12 = 0.59F;
         byte var13 = 0;
 
         if (var7 == 1)
         {
             var10 += var12;
             var8 = MathHelper.floor((double)(var2.yaw * 4.0F / 360.0F) + 0.5D) & 3;
             var13 = 1;
         }
 
         if (var7 == 0)
         {
             return false;
         }
         else
         {
             if (var7 == 2)
             {
                 var11 -= var12;
             }
 
             if (var7 == 3)
             {
                 var11 += var12;
                 var8 = 2;
             }
 
             if (var7 == 4)
             {
                 var9 -= var12;
                 var8 = 1;
             }
 
             if (var7 == 5)
             {
                 var9 += var12;
                 var8 = 3;
             }
 
             if (!var3.isStatic)
             {
                 EntityHangableMap var14 = new EntityHangableMap(var3, (double)var4, (double)var5, (double)var6);
 
                 if (var14.onValidSurface())
                 {
                     int[] var15 = new int[] {var4, var5, var6};
                     EntityHangableMap var16 = new EntityHangableMap(var3, var9 + 0.5F, var10 + 0.5F, var11 + 0.5F, var8, var1.getData(), var15, var13);
                     var16.setOwner(var2);
                     var3.addEntity(var16);
                     var2.V();
                     --var1.count;
 
                     if (var1.count > 0)
                     {
                         var2.inventory.pickup(var1);
                     }
 
                     return true;
                 }
             }
 
             return false;
         }
     }
 }
