 package steamcraft.blocks;
 
 import java.util.Random;
 
import steamcraft.Steamcraft;

 import net.minecraft.block.BlockRedstoneWire;
 import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockSCCopperWire extends BlockRedstoneWire
 {
     public static int modelID;
 
 	public BlockSCCopperWire(int i)
     {
         super(i);
         disableStats();
     }
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister)
     {
     	super.registerIcons(par1IconRegister);
         this.blockIcon = par1IconRegister.registerIcon("steamcraft:copperwire");
     }
     @Override
     public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
     {
         return side != -1;
     }
     @Override
     public int getRenderType()
     {
         return modelID;
     }
     @Override
     @SideOnly(Side.CLIENT)
     public int colorMultiplier(IBlockAccess iblockaccess, int i, int j, int k)
     {
         return 0x800000;
     }
    @Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return Steamcraft.redstoneWire.blockID;
    }
    @Override
    @SideOnly(Side.CLIENT)
    public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return Steamcraft.redstoneWire.blockID;
    }
     @Override
     public void randomDisplayTick(World world, int i, int j, int k, Random random)
     {
         int l = world.getBlockMetadata(i, j, k);
         if(l > 0)
         {
 			double d = (double)i + 0.5D + ((double)random.nextFloat() - 0.5D) * 0.20000000000000001D;
             double d1 = (float)j + 0.0625F;
             double d2 = (double)k + 0.5D + ((double)random.nextFloat() - 0.5D) * 0.20000000000000001D;
             float f = 1 / 15F;
             float f1 = -1;
             float f2 = f * f * 0.7F - 0.5F;
             float f3 = f * f * 0.6F - 0.7F;
             if(f2 < 0.0F)
             {
                 f2 = 0.7F;
             }
             if(f3 < 0.0F)
             {
                 f3 = 1.0F;
             }
             world.spawnParticle("reddust", d, d1, d2, f1, f2, f3);
         }
     }
 }
