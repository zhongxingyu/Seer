 package dk.slashwin.openframe;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 /**
  * User: DelusionalLogic
  * Date: 21-04-13
  * Time: 00:32
  */
 public class BlockMovingBlock extends BlockContainer
 {
     protected BlockMovingBlock(int id)
     {
         super(id, Material.piston);
     }
 
     @Override
     public void onBlockAdded(World par1World, int par2, int par3, int par4) {}
 
     @Override
     public TileEntity createNewTileEntity(World world)
     {
         return new TileMovingBlock();
     }
 
     @Override
     public int getRenderType()
     {
         return -1;
     }
 
     @Override
     public boolean isOpaqueCube()
     {
        return true;
     }
 
     @Override
     public boolean renderAsNormalBlock()
     {
         return false;
     }
 
     @Override
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
     {
         if (!par1World.isRemote && par1World.getBlockTileEntity(par2, par3, par4) == null)
         {
             ;
         }
     }
 
     public void registerIcons(IconRegister par1IconRegister)
     {
         this.blockIcon = par1IconRegister.registerIcon("openFrame:frame");
     }
 }
