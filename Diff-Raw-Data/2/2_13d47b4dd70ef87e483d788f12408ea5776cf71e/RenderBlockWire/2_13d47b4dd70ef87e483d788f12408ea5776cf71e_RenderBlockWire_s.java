 package com.dmillerw.brainFuckBlocks.client.render;
 
 import com.dmillerw.brainFuckBlocks.BrainFuckBlocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.world.IBlockAccess;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class RenderBlockWire implements ISimpleBlockRenderingHandler {
 
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
 		//TODO Finish.
		block.setBlockBounds(0.35F, 0.47F, 0.35F, 65F, 0.77F, 0.65F);
 		renderer.renderStandardBlock(block, x, y, z);
 		return false;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory() {
 		return false;
 	}
 
 	@Override
 	public int getRenderId() {
 		return BrainFuckBlocks.wireRenderID;
 	}
 
 }
