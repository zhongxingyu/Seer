 package modJam;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public abstract class BlockGlobalFurniturePlacementHandler extends Block{
 	
 	public static boolean[] globalPlacementWhitelist = new boolean[Block.blocksList.length];
 	
 	public BlockGlobalFurniturePlacementHandler(int par1, Material par2Material) {
 		super(par1, par2Material);
 	}
 	
 	@Override
     public int getLightValue(IBlockAccess world, int x, int y, int z)
     {
        if(ClientProxyModJam.awesomeOreRenderStage == 0){
             Block block = blocksList[world.getBlockId(x, y, z)];
             if (block != null && block != this)
             {
                 return block.getLightValue(world, x, y, z);
             }
             return lightValue[blockID];
         }else{
         	return 15;
         }
     }
 	
 	public static void initPlacementWhitelist(){
 		globalPlacementWhitelist[Block.glass.blockID] = true;
 		globalPlacementWhitelist[Block.ice.blockID] = true;
 		globalPlacementWhitelist[Block.mobSpawner.blockID] = true;
 		globalPlacementWhitelist[ModJam.awesomeOre.blockID] = true;
 		globalPlacementWhitelist[ModJam.woodTable.blockID] = true;
 		globalPlacementWhitelist[ModJam.stoneTable.blockID] = true;
 		globalPlacementWhitelist[ModJam.awesomeBlock.blockID] = true;
 		globalPlacementWhitelist[ModJam.awesomeBlockStandard.blockID] = true;
 		globalPlacementWhitelist[ModJam.awesomeBlockCreeper.blockID] = true;
 		globalPlacementWhitelist[ModJam.lightGen.blockID] = true;
 	}
 
 	public abstract boolean isBlockInLocalPlacementWhiteList(World par1World, int par2, int par3, int par4);
 	
 	public boolean isBlockInGlobalPlacementWhiteList(World par1World, int par2, int par3, int par4){
 		int var1 = par1World.getBlockId(par2, par3 - 1, par4);
 		if(isBlockInLocalPlacementWhiteList(par1World, par2, par3, par4)){
 			return true;
 		}
 		return globalPlacementWhitelist[var1];
 	}
 	
 	public boolean testPlacement(World par1World, int par2, int par3, int par4){
 		return par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4) || isBlockInGlobalPlacementWhiteList(par1World, par2, par3, par4);
 	}
 	
     /**
      * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
      */
 	@Override
     public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
     {
         return testPlacement(par1World, par2, par3, par4);
     }
 	
     /**
      * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
      */
 	@Override
     public boolean canBlockStay(World par1World, int par2, int par3, int par4)
     {
         return testPlacement(par1World, par2, par3, par4);
     }
 	
     /**
      * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
      * their own) Args: x, y, z, neighbor blockID
      */
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
     	if(!canBlockStay(par1World, par2, par3, par4)){
     		Block.blocksList[par1World.getBlockId(par2, par3, par4)].dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
     		par1World.setBlockToAir(par2, par3, par4);
     	}
     }
 	
     public static void setBlockInWhitelist(int id, boolean isAllowed){
     	globalPlacementWhitelist[id] = isAllowed;
     }
     
     /**
      * Returns the mobility information of the block, 0 = free, 1 = can't push but can move over, 2 = total immobility
      * and stop pistons
      */
     public int getMobilityFlag()
     {
         return 0;
     }
 }
