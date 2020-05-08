 package modJam;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.world.IBlockAccess;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class RenderLightGen implements ISimpleBlockRenderingHandler {
 
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID,
 			RenderBlocks renderer) {
 		Tessellator tessellator = Tessellator.instance;
 		GL11.glTranslatef(-0.5F, -0.5F, 0F);
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(0F, -1F, 0F);
 		renderer.setRenderBounds(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
 		renderer.renderBottomFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 0));
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(0F, +1F, 0F);
 		renderer.setRenderBounds(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
 		renderer.renderTopFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 1));
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(-1F, 0F, 0F);
 		renderer.setRenderBounds(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
 		renderer.renderWestFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 2));
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(+1F, 0F, 0F);
 		renderer.setRenderBounds(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
 		renderer.renderEastFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 3));
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(0F, 0F, -1F);
 		renderer.setRenderBounds(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
 		renderer.renderSouthFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 4));
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		tessellator.setNormal(0F, 0F, +1F);
 		renderer.setRenderBounds(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
 		renderer.renderNorthFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 5));
 		tessellator.draw();
 		GL11.glTranslatef(+0.5F, +0.5F, 0F);
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
 			Block block, int modelId, RenderBlocks renderer) {
 		renderer.setRenderBounds(0.35, 0.35, 0.35, 0.65, 0.65, 0.65);
 		renderer.renderStandardBlock(block, x, y, z);
 		renderAttachedHandles(world, x, y, z, block, modelId, renderer);
 		return true;
 	}
 
 	public void renderAttachedHandles(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
 		boolean[] map = getBlockSideMap(world, x, y, z);
 		boolean[] thisMap = getBlockSideMap(world, x, y, z, block.blockID);
		double topHandleHeight = 1.5;
		if(world.getBlockId(x, y + 1, z) == ModJam.woodTable.blockID || world.getBlockId(x, y + 1, z) == ModJam.stoneTable.blockID){
			topHandleHeight = 1.9;
		}else if(world.getBlockId(x, y + 1, z) == ModJam.woodChairIDs[0] || world.getBlockId(x, y + 1, z) == ModJam.woodChairIDs[1] || world.getBlockId(x, y + 1, z) == ModJam.woodChairIDs[2] || world.getBlockId(x, y + 1, z) == ModJam.woodChairIDs[3]
				 || world.getBlockId(x, y + 1, z) == ModJam.stoneChairIDs[0] || world.getBlockId(x, y + 1, z) == ModJam.stoneChairIDs[1] || world.getBlockId(x, y + 1, z) == ModJam.stoneChairIDs[2] || world.getBlockId(x, y + 1, z) == ModJam.stoneChairIDs[3]){
			topHandleHeight = 1.6;
		}
		
 		if(map[0] && !thisMap[0]){
 			renderer.setRenderBounds(0.45, -0.5, 0.45, 0.55, 0.35, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(map[1] && !thisMap[1]){
			renderer.setRenderBounds(0.45, 0.65, 0.45, 0.55, topHandleHeight, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(map[2] && !thisMap[2]){
 			renderer.setRenderBounds(-0.5, 0.45, 0.45, 0.35, 0.55, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(map[3] && !thisMap[3]){
 			renderer.setRenderBounds(0.45, 0.45, 0.45, 1.5, 0.55, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(map[4] && !thisMap[4]){
 			renderer.setRenderBounds(0.45, 0.45, -0.5, 0.55, 0.55, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(map[5] && !thisMap[5]){
 			renderer.setRenderBounds(0.45, 0.45, 0.45, 0.55, 0.55, 1.5);
 			renderer.renderStandardBlock(block, x, y, z);
 		}
 		if(thisMap[0]){
 			renderer.setRenderBounds(0.45, 0, 0.45, 0.55, 0.35, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(thisMap[1]){
 			renderer.setRenderBounds(0.45, 0.65, 0.45, 0.55, 1, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(thisMap[2]){
 			renderer.setRenderBounds(0, 0.45, 0.45, 0.35, 0.55, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(thisMap[3]){
 			renderer.setRenderBounds(0.45, 0.45, 0.45, 1, 0.55, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(thisMap[4]){
 			renderer.setRenderBounds(0.45, 0.45, 0, 0.55, 0.55, 0.55);
 			renderer.renderStandardBlock(block, x, y, z);
 		}if(thisMap[5]){
 			renderer.setRenderBounds(0.45, 0.45, 0.45, 0.55, 0.55, 1);
 			renderer.renderStandardBlock(block, x, y, z);
 		}
 	}
 	
 	public boolean isBlockAir(IBlockAccess par1World, int par2, int par3, int par4){
 		return par1World.getBlockId(par2, par3, par4) == 0;
 	}
 	
 	public boolean[] getBlockSideMap(IBlockAccess par1World, int par2, int par3, int par4){
 		boolean[] returnValue = new boolean[6];
 		returnValue[0] = !isBlockAir(par1World, par2, par3 - 1, par4);
 		returnValue[1] = !isBlockAir(par1World, par2, par3 + 1, par4);
 		returnValue[2] = !isBlockAir(par1World, par2 - 1, par3, par4);
 		returnValue[3] = !isBlockAir(par1World, par2 + 1, par3, par4);
 		returnValue[4] = !isBlockAir(par1World, par2, par3, par4 - 1);
 		returnValue[5] = !isBlockAir(par1World, par2, par3, par4 + 1);
 		return returnValue;
 	}
 	
 	public boolean[] getBlockSideMap(IBlockAccess par1World, int par2, int par3, int par4, int block){
 		boolean[] returnValue = new boolean[6];
 		returnValue[0] = !isBlockAir(par1World, par2, par3 - 1, par4) && par1World.getBlockId(par2, par3 - 1, par4) == block;
 		returnValue[1] = !isBlockAir(par1World, par2, par3 + 1, par4) && par1World.getBlockId(par2, par3 + 1, par4) == block;
 		returnValue[2] = !isBlockAir(par1World, par2 - 1, par3, par4) && par1World.getBlockId(par2 - 1, par3, par4) == block;
 		returnValue[3] = !isBlockAir(par1World, par2 + 1, par3, par4) && par1World.getBlockId(par2 + 1, par3, par4) == block;
 		returnValue[4] = !isBlockAir(par1World, par2, par3, par4 - 1) && par1World.getBlockId(par2, par3, par4 - 1) == block;
 		returnValue[5] = !isBlockAir(par1World, par2, par3, par4 + 1) && par1World.getBlockId(par2, par3, par4 + 1) == block;
 		return returnValue;
 	}
 	
 	@Override
 	public boolean shouldRender3DInInventory() {
 		return true;
 	}
 
 	@Override
 	public int getRenderId() {
 		return ClientProxyModJam.lightGenRenderType;
 	}
 
 }
