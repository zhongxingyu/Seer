 package tpw_rules.connectedmachines.render;
 
 
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.world.IBlockAccess;
 import net.minecraftforge.common.ForgeDirection;
 import tpw_rules.connectedmachines.tile.TileMachineLink;
 
 
 public class RenderMachineLink implements ISimpleBlockRenderingHandler {
     public int renderID;
 
     public RenderMachineLink(int renderID) {
         this.renderID = renderID; // store our render ID
     }
 
     @Override
     public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
     }
 
     @Override
     public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
         // get tile for this machine link
         TileMachineLink tile = (TileMachineLink)world.getBlockTileEntity(x, y, z);
 
         // render center piece
         renderer.setRenderBounds(.4, .4, .4, .6, .6, .6);
         renderer.renderStandardBlock(block, x, y, z);
 
         // render connecting pieces
         if (tile.connectedNeighbors[ForgeDirection.EAST.ordinal()]) {
             renderer.setRenderBounds(.6, .4, .4, 1, .6, .6);
             renderer.renderStandardBlock(block, x, y, z);
         }
         if (tile.connectedNeighbors[ForgeDirection.WEST.ordinal()]) {
             renderer.setRenderBounds(0, .4, .4, .4, .6, .6);
             renderer.renderStandardBlock(block, x, y, z);
         }
         if (tile.connectedNeighbors[ForgeDirection.UP.ordinal()]) {
             renderer.setRenderBounds(.4, .6, .4, .6, 1, .6);
             renderer.renderStandardBlock(block, x, y, z);
         }
         if (tile.connectedNeighbors[ForgeDirection.DOWN.ordinal()]) {
             renderer.setRenderBounds(.4, 0, .4, .6, .4, .6);
             renderer.renderStandardBlock(block, x, y, z);
         }
         if (tile.connectedNeighbors[ForgeDirection.SOUTH.ordinal()]) {
             renderer.setRenderBounds(.4, .4, .6, .6, .6, 1);
             renderer.renderStandardBlock(block, x, y, z);
         }
         if (tile.connectedNeighbors[ForgeDirection.NORTH.ordinal()]) {
             renderer.setRenderBounds(.4, .4, 0, .6, .6, .4);
             renderer.renderStandardBlock(block, x, y, z);
         }
         return true;
     }
 
     @Override
     public boolean shouldRender3DInInventory() {
         return false;
     }
 
     @Override
     public int getRenderId() {
         return renderID;
     }
 }
