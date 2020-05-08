 package net.runfast.frangiblebuttons.block;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.world.IBlockAccess;
 import net.runfast.Util;
 import net.runfast.frangiblebuttons.ClientProxy;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class RenderSafetyButton implements ISimpleBlockRenderingHandler {
 
     @Override
     public void renderInventoryBlock(Block block, int metadata, int modelID,
             RenderBlocks renderer) {
         // We use an icon instead.
     }
 
     @Override
     public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
             Block block, int modelId, RenderBlocks renderer) {
         if (ClientProxy.renderPass == 0) {
             // solid pass: draw the button
             Util.applyBB(renderer, BlockSafetyButton.instance
                     .getButtonBounds(world.getBlockMetadata(x, y, z)));
             renderer.renderStandardBlock(Block.stoneButton, x, y, z);
         } else if (ClientProxy.renderPass == 1) {
             // alpha pass: draw the glass cover
             Util.applyBB(renderer, BlockSafetyButton.instance
                     .getGlassBounds(world.getBlockMetadata(x, y, z)));
            renderer.overrideBlockTexture = Block.glass
                    .getBlockTextureFromSide(1);
            renderer.renderStandardBlock(BlockSafetyButton.instance, x, y, z);
         }
         return true;
     }
 
     @Override
     public boolean shouldRender3DInInventory() {
         return false;
     }
 
     @Override
     public int getRenderId() {
         return BlockSafetyButton.instance.getRenderType();
     }
 
 }
