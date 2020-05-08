 package techguy.mods.undead.common;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 
 public class ModUndeadBlockPyrose extends Block
 {
 	public ModUndeadBlockPyrose (int i, int j)
 	{
 		super(i, j, Material.plants);
 		float var4 = 0.2F;
 		setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.5F, 0.8F);
 	}
 
 	public String getTextureFile()
 	{
 		return "/undeadPlus/terrain.png";
 	}
 
 	public int getRenderType()
 	{
 		return 1;
 	}
 
 	public boolean isOpaqueCube()
 	{
 		return false;
 	}
 
 	public boolean renderAsNormalBlock()
 	{
 		return false;
 	}
 
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
 	{
 		return null;
 	}
 
 	public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
 	{
 		double var7 = (double)((float)par2 + 0.5F);
 		double var9 = (double)((float)par3 + 0.7F);
 		double var11 = (double)((float)par4 + 0.5F);
 
 		par1World.spawnParticle("smoke", var7, var9, var11, 0.0D, 0.0D, 0.0D);
 		par1World.spawnParticle("flame", var7, var9, var11, 0.0D, 0.0D, 0.0D);
 	}
 	
     public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
     {
         return super.canPlaceBlockAt(par1World, par2, par3, par4) && this.canThisPlantGrowOnThisBlockID(par1World.getBlockId(par2, par3 - 1, par4));
     }
 
     protected boolean canThisPlantGrowOnThisBlockID(int par1)
     {
         return par1 == ModUndeadMainRegistry.dimGrass.blockID || par1 == ModUndeadMainRegistry.dimDirt.blockID;
     }
     
     public boolean canBlockStay(World par1World, int par2, int par3, int par4)
     {
         return this.canPlaceBlockAt(par1World, par2, par3, par4);
     }
     
     protected final void checkBlockCoordValid(World par1World, int par2, int par3, int par4)
     {
         if (!this.canBlockStay(par1World, par2, par3, par4))
         {
             this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
             par1World.setBlockWithNotify(par2, par3, par4, 0);
         }
     }
 }
