 package com.basspro.scm.block;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockPortal;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ModLoader;
 import net.minecraft.world.World;
 
 import com.basspro.scm.SixCoreMod;
 import com.basspro.scm.lib.Textures;
import com.basspro.scm.world.DimensionSCM;
 import com.basspro.scm.world.TeleporterSCM;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockPandoraPortal extends BlockPortal {
     public BlockPandoraPortal(int id) {
         super(id);
         setCreativeTab(SixCoreMod.tabSixCoreModBlock);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister) {
 
         blockIcon = iconRegister.registerIcon(Textures.TEXTURE_PATH
                 + this.getUnlocalizedName().substring(5));
     }
 
     public void updateTick(World par1World, int par2, int par3, int par4,
             Random par5Random) {
 
     }
 
     public void onEntityCollidedWithBlock(World par1World, int par2, int par3,
             int par4, Entity par5Entity) {
         if ((par5Entity.ridingEntity == null)
                 && (par5Entity.riddenByEntity == null)
                 && ((par5Entity instanceof EntityPlayerMP))) {
             EntityPlayerMP player = (EntityPlayerMP) par5Entity;
             ModLoader.getMinecraftServerInstance();
             MinecraftServer mServer = MinecraftServer.getServer();
 
             if (player.timeUntilPortal > 0) {
                 player.timeUntilPortal = 10;
            } else if (player.dimension != DimensionSCM.dimension) {
                 player.timeUntilPortal = 10;
 
                 player.mcServer
                         .getConfigurationManager()
                         .transferPlayerToDimension(
                                 player,
                                DimensionSCM.dimension,
                                 new TeleporterSCM(
                                        mServer.worldServerForDimension(DimensionSCM.dimension)));
             } else {
                 player.timeUntilPortal = 10;
                 player.mcServer.getConfigurationManager()
                         .transferPlayerToDimension(
                                 player,
                                 0,
                                 new TeleporterSCM(mServer
                                         .worldServerForDimension(1)));
             }
         }
     }
 
     public boolean tryToCreatePortal(World par1World, int par2, int par3,
             int par4) {
         byte var5 = 0;
         byte var6 = 0;
 
         if (par1World.getBlockId(par2 - 1, par3, par4) == Block.sandStone.blockID
                 || par1World.getBlockId(par2 + 1, par3, par4) == Block.sandStone.blockID) {
             var5 = 1;
         }
 
         if (par1World.getBlockId(par2, par3, par4 - 1) == Block.sandStone.blockID
                 || par1World.getBlockId(par2, par3, par4 + 1) == Block.sandStone.blockID) {
             var6 = 1;
         }
 
         if (var5 == var6) {
             return false;
         } else {
             if (par1World.getBlockId(par2 - var5, par3, par4 - var6) == 0) {
                 par2 -= var5;
                 par4 -= var6;
             }
 
             int var7;
             int var8;
 
             for (var7 = -1; var7 <= 2; ++var7) {
                 for (var8 = -1; var8 <= 3; ++var8) {
                     boolean var9 = var7 == -1 || var7 == 2 || var8 == -1
                             || var8 == 3;
 
                     if (var7 != -1 && var7 != 2 || var8 != -1 && var8 != 3) {
                         int var10 = par1World.getBlockId(par2 + var5 * var7,
                                 par3 + var8, par4 + var6 * var7);
 
                         if (var9) {
                             if (var10 != Block.sandStone.blockID) {
                                 return false;
                             }
                         } else if (var10 != 0 && var10 != Block.waterStill.blockID) {
                             return false;
                         }
                     }
                 }
             }
 
             for (var7 = 0; var7 < 2; ++var7) {
                 for (var8 = 0; var8 < 3; ++var8) {
                     par1World.setBlock(par2 + var5 * var7, par3 + var8, par4
                             + var6 * var7, this.blockID, 0, 2);
                 }
             }
 
             return true;
         }
     }
 
     public void onNeighborBlockChange(World par1World, int par2, int par3,
             int par4, int par5) {
 
     }
 
     public int quantityDropped(Random par1Random) {
         return 1;
     }
 
 }
