 package tpw_rules.connectedmachines.block;
 
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import tpw_rules.connectedmachines.common.ConnectedMachines;
 import tpw_rules.connectedmachines.render.Texture;
 import tpw_rules.connectedmachines.tile.TileMachineLink;
 
 import java.util.List;
 
 public class BlockMachineLink extends Block implements ITileEntityProvider {
     public int renderID;
 
     public BlockMachineLink(int id) {
         super(id, Material.rock);
         setCreativeTab(ConnectedMachines.tabMachines);
         setHardness(1.0F);
         setResistance(10.0F);
         setStepSound(Block.soundClothFootstep);
 
         GameRegistry.registerBlock(this, "CMMachineLink");
         GameRegistry.registerTileEntity(TileMachineLink.class, "CMTEMachineLink");
         LanguageRegistry.addName(this, "Machine Link");
     }
 
     @Override
     public void onPostBlockPlaced(World world, int x, int y, int z, int meta) {
         // mark that neighbors need to be checked
         ((TileMachineLink)world.getBlockTileEntity(x, y, z)).checkNeighbors = true;
     }
 
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int neighborID) {
         // mark that neighbors need to be checked
         ((TileMachineLink)world.getBlockTileEntity(x, y, z)).checkNeighbors = true;
     }
 
     @Override
     public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
         TileMachineLink tile = (TileMachineLink)world.getBlockTileEntity(x, y, z);
         if (tile.link != null) {
             return Texture.blockLinked;
         }
         return Texture.blockSide;
     }
 
     @Override
     public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
         // get this block's connections
         boolean[] connections = ((TileMachineLink)world.getBlockTileEntity(x, y, z)).connectedNeighbors;
         setBlockBounds(
                 connections[ForgeDirection.WEST.ordinal()] ? 0f : .4f,
                 connections[ForgeDirection.DOWN.ordinal()] ? 0f : .4f,
                 connections[ForgeDirection.NORTH.ordinal()] ? 0f : .4f,
                 connections[ForgeDirection.EAST.ordinal()] ? 1f : .6f,
                 connections[ForgeDirection.UP.ordinal()] ? 1f : .6f,
                 connections[ForgeDirection.SOUTH.ordinal()] ? 1f : .6f
         );
     }
 
     @Override
     public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB box, List results, Entity entity) {
         setBlockBoundsBasedOnState(world, x, y, z);
         super.addCollisionBoxesToList(world, x, y, z, box, results, entity);
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileMachineLink();
     }
 
     @Override
     public int getRenderType() {
         return renderID;
     }
 
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @Override
     public boolean isBlockSolid(IBlockAccess world, int x, int y, int z, int side) {
         return false;
     }
 
     @Override
     public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
         return true;
     }
 }
