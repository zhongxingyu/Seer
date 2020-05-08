 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode 
 
 package MCP.mod_finiteliquids;
 
 import java.util.Random;
 
 import MCP.ApiController;
 import MCP.api.BlockBase;
 import net.minecraft.src.*;
 
 // Referenced classes of package net.minecraft.src:
 //            BlockNWater, mod_NWater, Entity, Block, 
 //            Material, World, IBlockAccess
 
 public class BlockNQSand extends BlockNWater
 {
 
     protected BlockNQSand(ApiController api, int j, Material material)
     {
        super(api, j, material);
         blockIndexInTexture = mod_NWater.texx[18];
         setHardness(100F);
         setLightOpacity(255);
         setBlockName("nqsand");
     }
 
     public void updateTick(World world, int i, int j, int k, Random random)
     {
         super.updateTick(world, i, j, k, random);
     }
 
     public int tickRate()
     {
         return 6;
     }
 
     public int getThreshold()
     {
         return 2;
     }
 
     public int getMetathresh()
     {
         return 0;
     }
 
     public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l)
     {
         return mod_NWater.texx[18];
     }
 
     public float getSurfaceMull()
     {
         return 1.0F;
     }
 
     public void setToStill(World world, int i, int j, int k)
     {
     }
 
     public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity)
     {
         super.onEntityCollidedWithBlock(world, i, j, k, entity);
         entity.motionX = entity.motionX * -0.69999999999999996D;
         entity.motionY = entity.motionY * -0.69999999999999996D;
         entity.motionZ = entity.motionZ * -0.69999999999999996D;
     }
 
     public int getStill()
     {
         return mod_NWater.nqsand_still.blockID;
     }
 }
