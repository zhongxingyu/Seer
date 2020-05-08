 package mods.themike.modjam.handler;
 
 import org.lwjgl.opengl.GL11;
 
 import mods.themike.modjam.blocks.BlockCarvingStone;
 import mods.themike.modjam.render.ModelCarvingStone;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.world.IBlockAccess;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class SimpleBlockRenderingHandler implements ISimpleBlockRenderingHandler {
 
 	ModelCarvingStone stone = new ModelCarvingStone();
 	
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {	
 		if(block instanceof BlockCarvingStone) {
 			Minecraft.getMinecraft().renderEngine.bindTexture("/mods/mikejam/textures/models/stone.png");
 			GL11.glPushMatrix();
 			GL11.glTranslatef((float) 0.0F, (float) 1F, (float) 0.0F);
 			GL11.glScalef(1.0F, -1F, -1F);
 			GL11.glRotatef(0F, 0F, 1F, 0F);
 			stone.render(0.0625F);
 			GL11.glPopMatrix();
 		}
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
 		return true;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory() {
 		return true;
 	}
 
 	@Override
 	public int getRenderId() {
		return this.getRenderId();
 	}
 
 }
