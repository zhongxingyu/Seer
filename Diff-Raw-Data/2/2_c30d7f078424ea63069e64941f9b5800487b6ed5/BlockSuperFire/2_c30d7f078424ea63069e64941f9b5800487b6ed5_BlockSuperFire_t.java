 package planetguy.Gizmos.tool;
 
 import static net.minecraftforge.common.ForgeDirection.DOWN;
 import static net.minecraftforge.common.ForgeDirection.EAST;
 import static net.minecraftforge.common.ForgeDirection.NORTH;
 import static net.minecraftforge.common.ForgeDirection.SOUTH;
 import static net.minecraftforge.common.ForgeDirection.UP;
 import static net.minecraftforge.common.ForgeDirection.WEST;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockFire;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import java.util.Random;
 
 import com.google.common.collect.ImmutableList;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldProviderEnd;
 
 import net.minecraftforge.common.ForgeDirection;
 import static net.minecraftforge.common.ForgeDirection.*;
 
 public class BlockSuperFire extends BlockFire{
 
 	Random randomizer = new Random();
 	
	public static int globalMode=0; //very, VERY bad way to do this
 	private ImmutableList<Integer> treeBlocks=ImmutableList.of(17, 18, 31, 106);
 	private ImmutableList<Integer> earthBlocks=ImmutableList.of(10, 11, 13);
 	
 	public BlockSuperFire(int id, int texture) {
 		super(id);
 		//setCreativeTab(CreativeTabs.tabRedstone);
 	}
 	
 	
 	public int tickRate(){
 		return 3;	
 	}
 	
 	public boolean canBurnBlock(int id){
 		switch(this.globalMode){
 		case 0:
 			return treeBlocks.contains(id);
 		case 1:
 			return earthBlocks.contains(id);
 		default:
 			return true;
 		}
 	}
     
     /*
     public String getTextureFile(){
 		  return "/planetguy/EvilToys/tex.png";
     }
     */
 
     /**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random par1Random)
     {
         return 0;
     }
 
 
     /**
      * Ticks the block if it's been scheduled
      */
     @Override
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (par1World.getGameRules().getGameRuleBooleanValue("doFireTick"))
         {
             Block base = Block.blocksList[par1World.getBlockId(par2, par3 - 1, par4)];
             boolean flag = (base != null && base.isFireSource(par1World, par2, par3 - 1, par4, par1World.getBlockMetadata(par2, par3 - 1, par4), UP));
 
             if (!this.canPlaceBlockAt(par1World, par2, par3, par4))
             {
                 par1World.setBlockToAir(par2, par3, par4);
             }
 
             if (!flag && par1World.isRaining() && (par1World.canLightningStrikeAt(par2, par3, par4) || par1World.canLightningStrikeAt(par2 - 1, par3, par4) || par1World.canLightningStrikeAt(par2 + 1, par3, par4) || par1World.canLightningStrikeAt(par2, par3, par4 - 1) || par1World.canLightningStrikeAt(par2, par3, par4 + 1)))
             {
                 par1World.setBlockToAir(par2, par3, par4);
             }
             else
             {
                 int l = par1World.getBlockMetadata(par2, par3, par4);
 
                 if (l < 15)
                 {
                     par1World.setBlockMetadataWithNotify(par2, par3, par4, l + par5Random.nextInt(3) / 2, 4);
                 }
 
                 par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate(par1World) + par5Random.nextInt(10));
 
                 if (!flag && !this.canNeighborBurn(par1World, par2, par3, par4))
                 {
                     if (!par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4) || l > 3)
                     {
                         par1World.setBlockToAir(par2, par3, par4);
                     }
                 }
                 else if (!flag && !this.canBlockCatchFire(par1World, par2, par3 - 1, par4, UP) && l == 15 && par5Random.nextInt(4) == 0)
                 {
                     par1World.setBlockToAir(par2, par3, par4);
                 }
                 else
                 {
                     boolean flag1 = par1World.isBlockHighHumidity(par2, par3, par4);
                     byte b0 = 0;
 
                     if (flag1)
                     {
                         b0 = -50;
                     }
 
                     this.tryToCatchBlockOnFire(par1World, par2 + 1, par3, par4, 300 + b0, par5Random, l, WEST );
                     this.tryToCatchBlockOnFire(par1World, par2 - 1, par3, par4, 300 + b0, par5Random, l, EAST );
                     this.tryToCatchBlockOnFire(par1World, par2, par3 - 1, par4, 250 + b0, par5Random, l, UP   );
                     this.tryToCatchBlockOnFire(par1World, par2, par3 + 1, par4, 250 + b0, par5Random, l, DOWN );
                     this.tryToCatchBlockOnFire(par1World, par2, par3, par4 - 1, 300 + b0, par5Random, l, SOUTH);
                     this.tryToCatchBlockOnFire(par1World, par2, par3, par4 + 1, 300 + b0, par5Random, l, NORTH);
 
                     for (int i1 = par2 - 1; i1 <= par2 + 1; ++i1)
                     {
                         for (int j1 = par4 - 1; j1 <= par4 + 1; ++j1)
                         {
                             for (int k1 = par3 - 1; k1 <= par3 + 4; ++k1)
                             {
                                 if (i1 != par2 || k1 != par3 || j1 != par4)
                                 {
                                     int l1 = 100;
 
                                     if (k1 > par3 + 1)
                                     {
                                         l1 += (k1 - (par3 + 1)) * 100;
                                     }
 
                                     int i2 = this.getChanceOfNeighborsEncouragingFire(par1World, i1, k1, j1);
 
                                     if (i2 > 0)
                                     {
                                         int j2 = (i2 + 40 + par1World.difficultySetting * 7) / (l + 30);
 
                                         if (flag1)
                                         {
                                             j2 /= 2;
                                         }
 
                                         if (j2 > 0 && par5Random.nextInt(l1) <= j2 && (!par1World.isRaining() || !par1World.canLightningStrikeAt(i1, k1, j1)) && !par1World.canLightningStrikeAt(i1 - 1, k1, par4) && !par1World.canLightningStrikeAt(i1 + 1, k1, j1) && !par1World.canLightningStrikeAt(i1, k1, j1 - 1) && !par1World.canLightningStrikeAt(i1, k1, j1 + 1))
                                         {
                                             int k2 = l + par5Random.nextInt(5) / 4;
 
                                             if (k2 > 15)
                                             {
                                                 k2 = 15;
                                             }
 
                                             par1World.setBlock(i1, k1, j1, this.blockID, k2, 3);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
     
     private void tryToCatchBlockOnFire(World par1World, int par2, int par3, int par4, int par5, Random par6Random, int par7, ForgeDirection face)
     {
         int var8 = 0;
         int block = par1World.getBlockId(par2, par3, par4);
         if (canBurnBlock(block))
         {
             int k1 = par7 + par6Random.nextInt(5) / 4;
 
             if (k1 > 15)
             {
                 k1 = 15;
             }
             par1World.setBlock(par2, par3, par4, this.blockID, k1, 3);
         } 
     }
     
     
 
     /**
      * Returns true if at least one block next to this one can burn.
      */
     private boolean canNeighborBurn(World par1World, int par2, int par3, int par4)
     {
         return canBlockCatchFire(par1World, par2 + 1, par3, par4, WEST ) ||
                canBlockCatchFire(par1World, par2 - 1, par3, par4, EAST ) ||
                canBlockCatchFire(par1World, par2, par3 - 1, par4, UP   ) ||
                canBlockCatchFire(par1World, par2, par3 + 1, par4, DOWN ) ||
                canBlockCatchFire(par1World, par2, par3, par4 - 1, SOUTH) ||
                canBlockCatchFire(par1World, par2, par3, par4 + 1, NORTH);
     }
 
     /**
      * Gets the highest chance of a neighbor block encouraging this block to catch fire
      */
     private int getChanceOfNeighborsEncouragingFire(World par1World, int par2, int par3, int par4)
     {
         byte var5 = 0;
 
         if (!par1World.isAirBlock(par2, par3, par4))
         {
             return 0;
         }
         else
         {
             int var6 = this.getChanceToEncourageFire(par1World, par2 + 1, par3, par4, var5, WEST);
             var6 = this.getChanceToEncourageFire(par1World, par2 - 1, par3, par4, var6, EAST);
             var6 = this.getChanceToEncourageFire(par1World, par2, par3 - 1, par4, var6, UP);
             var6 = this.getChanceToEncourageFire(par1World, par2, par3 + 1, par4, var6, DOWN);
             var6 = this.getChanceToEncourageFire(par1World, par2, par3, par4 - 1, var6, SOUTH);
             var6 = this.getChanceToEncourageFire(par1World, par2, par3, par4 + 1, var6, NORTH);
             return var6;
         }
     }
 
 
     /**
      * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
      */
     
     @Override
     public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
     {
         return par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4) || this.canNeighborBurn(par1World, par2, par3, par4);
     }
 
     /**
      * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
      * their own) Args: x, y, z, neighbor blockID
      */
     
     @Override
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
     {
         if (!par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4) && !this.canNeighborBurn(par1World, par2, par3, par4))
         {
             par1World.setBlock(par2, par3, par4, 0);
         }
     }
 
     /**
      * Called whenever the block is added into the world. Args: world, x, y, z
      */
     
     @Override
     public void onBlockAdded(World par1World, int par2, int par3, int par4)
     {
     	//this.meta=par1World.getBlockMetadata(par2, par3, par4);
         if (par1World.provider.dimensionId > 0 || par1World.getBlockId(par2, par3 - 1, par4) != Block.obsidian.blockID || !Block.portal.tryToCreatePortal(par1World, par2, par3, par4))
         {
             if (!par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4) && !this.canNeighborBurn(par1World, par2, par3, par4))
             {
                 par1World.setBlock(par2, par3, par4, 0);
             }
             else
             {
                 par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate() + par1World.rand.nextInt(10));
             }
         }
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (par5Random.nextInt(24) == 0)
         {
             par1World.playSound((double)((float)par2 + 0.5F), (double)((float)par3 + 0.5F), (double)((float)par4 + 0.5F), "fire.fire", 1.0F + par5Random.nextFloat(), par5Random.nextFloat() * 0.7F + 0.3F, false);
         }
 
         int var6;
         float var7;
         float var8;
         float var9;
 
         if (!par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4) && !canBlockCatchFire(par1World, par2, par3 - 1, par4, UP))
         {
             if (canBlockCatchFire(par1World, par2 - 1, par3, par4, EAST))
             {
                 for (var6 = 0; var6 < 2; ++var6)
                 {
                     var7 = (float)par2 + par5Random.nextFloat() * 0.1F;
                     var8 = (float)par3 + par5Random.nextFloat();
                     var9 = (float)par4 + par5Random.nextFloat();
                     par1World.spawnParticle("reddust", (double)var7, (double)var8, (double)var9, 0.0D, 0.0D, 0.0D);
                 }
             }
 
             if (canBlockCatchFire(par1World, par2 + 1, par3, par4, WEST))
             {
                 for (var6 = 0; var6 < 2; ++var6)
                 {
                     var7 = (float)(par2 + 1) - par5Random.nextFloat() * 0.1F;
                     var8 = (float)par3 + par5Random.nextFloat();
                     var9 = (float)par4 + par5Random.nextFloat();
                     par1World.spawnParticle("reddust", (double)var7, (double)var8, (double)var9, 0.0D, 0.0D, 0.0D);
                 }
             }
 
             if (canBlockCatchFire(par1World, par2, par3, par4 - 1, SOUTH))
             {
                 for (var6 = 0; var6 < 2; ++var6)
                 {
                     var7 = (float)par2 + par5Random.nextFloat();
                     var8 = (float)par3 + par5Random.nextFloat();
                     var9 = (float)par4 + par5Random.nextFloat() * 0.1F;
                     par1World.spawnParticle("reddust", (double)var7, (double)var8, (double)var9, 0.0D, 0.0D, 0.0D);
                 }
             }
 
             if (canBlockCatchFire(par1World, par2, par3, par4 + 1, NORTH))
             {
                 for (var6 = 0; var6 < 2; ++var6)
                 {
                     var7 = (float)par2 + par5Random.nextFloat();
                     var8 = (float)par3 + par5Random.nextFloat();
                     var9 = (float)(par4 + 1) - par5Random.nextFloat() * 0.1F;
                     par1World.spawnParticle("reddust", (double)var7, (double)var8, (double)var9, 0.0D, 0.0D, 0.0D);
                 }
             }
 
             if (canBlockCatchFire(par1World, par2, par3 + 1, par4, DOWN))
             {
                 for (var6 = 0; var6 < 2; ++var6)
                 {
                     var7 = (float)par2 + par5Random.nextFloat();
                     var8 = (float)(par3 + 1) - par5Random.nextFloat() * 0.1F;
                     var9 = (float)par4 + par5Random.nextFloat();
                     par1World.spawnParticle("reddust", (double)var7, (double)var8, (double)var9, 0.0D, 0.0D, 0.0D);
                 }
             }
         }
         else
         {
             for (var6 = 0; var6 < 3; ++var6)
             {
                 var7 = (float)par2 + par5Random.nextFloat();
                 var8 = (float)par3 + par5Random.nextFloat() * 0.5F + 0.5F;
                 var9 = (float)par4 + par5Random.nextFloat();
                 par1World.spawnParticle("reddust", (double)var7, (double)var8, (double)var9, 0.0D, 0.0D, 0.0D);
             }
         }
     }
     
     /**
      * Side sensitive version that calls the block function.
      * 
      * @param world The current world
      * @param x X Position
      * @param y Y Position
      * @param z Z Position
      * @param face The side the fire is coming from
      * @return True if the face can catch fire.
      */
     
     @Override
     public boolean canBlockCatchFire(IBlockAccess world, int x, int y, int z, ForgeDirection face)
     {
         int block = world.getBlockId(x, y, z);
         return canBurnBlock(block);
   
     }
 
     /**
      * Side sensitive version that calls the block function.
      * 
      * @param world The current world
      * @param x X Position
      * @param y Y Position
      * @param z Z Position
      * @param oldChance The previous maximum chance.
      * @param face The side the fire is coming from
      * @return The chance of the block catching fire, or oldChance if it is higher
      */
     
     @Override
     public int getChanceToEncourageFire(World world, int x, int y, int z, int oldChance, ForgeDirection face)
     {
         int newChance = 0;
         int block = world.getBlockId(x, y, z);
         if(canBurnBlock(block)){
         	return 100;
         }else{
         	return 0;
         }
   
         //return (newChance > oldChance ? newChance : oldChance);
     }
 }
