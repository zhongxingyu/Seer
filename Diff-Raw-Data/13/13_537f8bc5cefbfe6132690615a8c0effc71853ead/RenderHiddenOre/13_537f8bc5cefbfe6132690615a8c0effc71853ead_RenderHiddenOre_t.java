 package loecraftpack.common.blocks.render;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.common.blocks.BlockHiddenOre;
 import loecraftpack.ponies.abilities.mechanics.MechanicHiddenOres;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraftforge.client.event.RenderWorldLastEvent;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class RenderHiddenOre implements ISimpleBlockRenderingHandler
 {
 	public int renderID;
 	public static boolean phantomPass = false;
 	
 	public List<int[]> phantomBlocks = new ArrayList<int[]>();
 	
 	public void addPhantomBlock(int x, int y, int z)
 	{
 		for(int[] coords : phantomBlocks)
 		{
 			if (coords[0] == x && coords[1] == y && coords[2] == z)
 			{
 				return;
 			}
 		}
 		phantomBlocks.add(new int[]{x, y, z});
 	}
 	
 	@Override
 	public void renderInventoryBlock(Block blockBase, int metadata, int modelID,
 			RenderBlocks renderer) {
 		BlockHiddenOre block = (BlockHiddenOre)blockBase;
 		
 		Tessellator tessellator = Tessellator.instance;
 		boolean flag = block.blockID == Block.grass.blockID;
 		float par3 = 1.0f;//why isn't this passed to us?
 		
 		int j;
         float f1;
         float f2;
         float f3;
 
         if (renderer.useInventoryTint)
         {
             j = block.getRenderColor(metadata);
 
             if (flag)
             {
                 j = 16777215;
             }
 
             f1 = (float)(j >> 16 & 255) / 255.0F;
             f2 = (float)(j >> 8 & 255) / 255.0F;
             f3 = (float)(j & 255) / 255.0F;
             GL11.glColor4f(f1 * par3, f2 * par3, f3 * par3, 1.0F);
         }
         
         renderer.setRenderBoundsFromBlock(block);
         int k;
         
 		block.setBlockBoundsForItemRender();
         renderer.setRenderBoundsFromBlock(block);
         GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
         GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, -1.0F, 0.0F);
         renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getHiddenBlockTextureFromSideAndMetadata(0, metadata));
         tessellator.draw();
 
         if (flag && renderer.useInventoryTint)
         {
             k = block.getRenderColor(metadata);
             f2 = (float)(k >> 16 & 255) / 255.0F;
             f3 = (float)(k >> 8 & 255) / 255.0F;
             float f7 = (float)(k & 255) / 255.0F;
             GL11.glColor4f(f2 * par3, f3 * par3, f7 * par3, 1.0F);
         }
 
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, 1.0F, 0.0F);
         renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getHiddenBlockTextureFromSideAndMetadata(1, metadata));
         tessellator.draw();
 
         if (flag && renderer.useInventoryTint)
         {
             GL11.glColor4f(par3, par3, par3, 1.0F);
         }
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getHiddenBlockTextureFromSideAndMetadata(2, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getHiddenBlockTextureFromSideAndMetadata(3, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getHiddenBlockTextureFromSideAndMetadata(4, metadata));
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getHiddenBlockTextureFromSideAndMetadata(5, metadata));
         tessellator.draw();
         GL11.glTranslatef(0.5F, 0.5F, 0.5F);
 		
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
 			Block block, int modelId, RenderBlocks renderer) {
 		if(phantomPass)
 		{
 			//phantom render
 			renderer.renderStandardBlock(block, x, y, z);
 		}
 		else
 		{
 			Icon iconPre = renderer.overrideBlockTexture;
 			BlockHiddenOre oreBlock = (BlockHiddenOre)block;
 			int meta = world.getBlockMetadata(x, y, z);
 			
 			if (iconPre == null)
 			{
 				boolean flag1 = !oreBlock.hidden(meta);
 				boolean flag2 = MechanicHiddenOres.revealHiddenGems && MechanicHiddenOres.inRangeofClientPlayer(x, y, z);
 				
 				//apply normal render override
 				if(flag1 || flag2)
 				{
 					Icon icon;
 					icon = oreBlock.getHiddenBlockTextureFromSideAndMetadata(0, meta);
 					renderer.setOverrideBlockTexture(icon);
 				}
 				
 				//append to phantom list
 				if(flag2)
 					addPhantomBlock(x, y, z);
 			}
 			
 			//normal render, and breaking render
 			renderer.renderStandardBlock(block, x, y, z);
 			
 			renderer.overrideBlockTexture = iconPre;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory()
 	{
 		return true;
 	}
 
 	@Override
 	public int getRenderId()
 	{
 		return renderID;
 	}
 	
 	public void drawBlockPhantomTexture(RenderWorldLastEvent event)
     {
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glEnable(GL11.GL_BLEND);
         
 		phantomPass = true;
 		
 		boolean hold = event.context.globalRenderBlocks.renderAllFaces;
 		event.context.globalRenderBlocks.renderAllFaces = true;
 		
 		Tessellator par1Tessellator = Tessellator.instance;
 		EntityLiving par2EntityPlayer = event.context.mc.renderViewEntity;
 		float partialTicks = event.partialTicks;
 		
 		
         double d0 = par2EntityPlayer.lastTickPosX + (par2EntityPlayer.posX - par2EntityPlayer.lastTickPosX) * (double)partialTicks;
         double d1 = par2EntityPlayer.lastTickPosY + (par2EntityPlayer.posY - par2EntityPlayer.lastTickPosY) * (double)partialTicks;
         double d2 = par2EntityPlayer.lastTickPosZ + (par2EntityPlayer.posZ - par2EntityPlayer.lastTickPosZ) * (double)partialTicks;
         
         if (!this.phantomBlocks.isEmpty())
         {
         	GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_DST_COLOR);
             event.context.renderEngine.bindTexture("/terrain.png");
             GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
             GL11.glPushMatrix();
             GL11.glDisable(GL11.GL_ALPHA_TEST);
             GL11.glPolygonOffset(-3.0F, -3.0F);
             GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
             GL11.glEnable(GL11.GL_ALPHA_TEST);
             par1Tessellator.startDrawingQuads();
             par1Tessellator.setTranslation(-d0, -d1, -d2);
             par1Tessellator.disableColor();
             
             int[][] locations = this.phantomBlocks.toArray(new int[phantomBlocks.size()][]);
 
             for (int i=0; i<locations.length; i++)
             {
                 int[] location = locations[i];
                 
                 int id = event.context.theWorld.getBlockId(location[0], location[1], location[2]);
                 Block block = Block.blocksList[id];
 
                 if (block instanceof BlockHiddenOre)
                 {
                 	event.context.globalRenderBlocks.renderBlockUsingTexture(block, location[0], location[1], location[2],
 							 ((BlockHiddenOre)block).getHiddenBlockTextureFromSideAndMetadata(0, event.context.theWorld.getBlockMetadata(location[0], location[1], location[2])));
                 }
             }
 
             par1Tessellator.draw();
             par1Tessellator.setTranslation(0.0D, 0.0D, 0.0D);
             GL11.glDisable(GL11.GL_ALPHA_TEST);
             GL11.glPolygonOffset(0.0F, 0.0F);
             GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
             GL11.glEnable(GL11.GL_ALPHA_TEST);
             GL11.glDepthMask(true);
             GL11.glPopMatrix();
         }
         
         event.context.globalRenderBlocks.renderAllFaces = hold;
         
         phantomPass = false;
         
         GL11.glDisable(GL11.GL_BLEND);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
     }
 
 }
