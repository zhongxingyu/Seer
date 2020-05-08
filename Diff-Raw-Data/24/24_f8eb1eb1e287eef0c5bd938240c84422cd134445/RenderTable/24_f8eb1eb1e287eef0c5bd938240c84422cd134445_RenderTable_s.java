 package modJam;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.world.IBlockAccess;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class RenderTable implements ISimpleBlockRenderingHandler {
 
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID,
 			RenderBlocks renderer) {
 		BlockTable handlerBlock = (BlockTable)block;
 		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
 		Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(-1F, 0F, 0F);
 		renderer.setRenderBounds(0.001, 0.001, 0.001, 0.099, 0.899, 0.099);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderEastFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderWestFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderTopFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.setRenderBounds(0, 0, 0, 0.1, 0.9, 0.1);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderEastFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderWestFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderTopFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.setRenderBounds(0.901, 0.001, 0.001, 0.999, 0.899, 0.099);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderEastFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderWestFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderTopFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.setRenderBounds(0.9, 0, 0, 1, 0.9, 0.1);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderEastFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderWestFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderTopFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(+1F, 0F, 0F);
 		renderer.setRenderBounds(0.001, 0.001, 0.901, 0.099, 0.899, 0.999);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderEastFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderWestFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderTopFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.setRenderBounds(0.0, 0.0, 0.9, 0.1, 0.9, 1.0);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderEastFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderWestFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderTopFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.setRenderBounds(0.901, 0.001, 0.901, 0.999, 0.899, 0.999);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderEastFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderWestFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderTopFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.setRenderBounds(0.9, 0.0, 0.9, 1.0, 0.9, 1.0);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderEastFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderWestFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderTopFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(0F, +1F, 0F);
 		renderer.setRenderBounds(0.001, 0.899, 0.001, 0.999, 0.999, 0.999);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderEastFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderWestFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderTopFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.setRenderBounds(0F, 0.9F, 0F, 1F, 1F, 1F);
 		renderer.renderNorthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderEastFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderSouthFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderWestFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderTopFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		renderer.renderBottomFace(block, 0D, 0D, 0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
 		tessellator.draw();
 		GL11.glTranslatef(+0.5F, +0.5F, +0.5F);
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
 			Block block, int modelId, RenderBlocks renderer) {
 		BlockTable handlerBlock = (BlockTable)block;
 		ClientProxyModJam.furnitureRenderStage = 0;
 		renderer.setRenderBounds(0.001, 0.001, 0.001, 0.099, 0.899, 0.099);
 		renderer.setOverrideBlockTexture(handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 1;
 		renderer.setRenderBounds(0.0, 0.0, 0.0, 0.1, 0.9, 0.1);
 		renderer.clearOverrideBlockTexture();
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 0;
 		renderer.setRenderBounds(0.901, 0.001, 0.001, 0.999, 0.899, 0.099);
 		renderer.setOverrideBlockTexture(handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 1;
 		renderer.setRenderBounds(0.9, 0.0, 0.0, 1.0, 0.9, 0.1);
 		renderer.clearOverrideBlockTexture();
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 0;
 		renderer.setRenderBounds(0.001, 0.001, 0.901, 0.099, 0.899, 0.999);
 		renderer.setOverrideBlockTexture(handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 1;
 		renderer.setRenderBounds(0.0, 0.0, 0.9, 0.1, 0.9, 1.0);
 		renderer.clearOverrideBlockTexture();
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 0;
 		renderer.setRenderBounds(0.901, 0.001, 0.901, 0.999, 0.899, 0.999);
 		renderer.setOverrideBlockTexture(handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 1;
 		renderer.setRenderBounds(0.9, 0.0, 0.9, 1.0, 0.9, 1.0);
 		renderer.clearOverrideBlockTexture();
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 0;
		renderer.setRenderBounds(0.001, 0.899, 0.001, 0.999, 0.999, 0.999);
 		renderer.setOverrideBlockTexture(handlerBlock.belowBlock.getBlockTextureFromSide(0));
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 1;
 		renderer.setRenderBounds(0.0, 0.9, 0.0, 1.0, 1.0, 1.0);
 		renderer.clearOverrideBlockTexture();
 		renderer.renderStandardBlock(block, x, y, z);
 		ClientProxyModJam.furnitureRenderStage = 0;
 		renderer.clearOverrideBlockTexture();
 		return false;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory() {
 		return true;
 	}
 
 	@Override
 	public int getRenderId() {
 		return ClientProxyModJam.tableRenderType;
 	}
 
 }
