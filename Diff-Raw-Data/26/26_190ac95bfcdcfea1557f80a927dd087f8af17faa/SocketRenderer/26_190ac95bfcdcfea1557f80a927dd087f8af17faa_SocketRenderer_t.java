 package emasher.sockets.client;
 
 import java.awt.image.BufferedImage;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import emasher.api.SocketModule;
 import emasher.sockets.BlockSocket;
 import emasher.sockets.SocketsMod;
 import emasher.sockets.TileSocket;
 import emasher.sockets.modules.*;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.RenderBlocks;
 //import net.minecraft.client.renderer.RenderEngine;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.texture.TextureMap;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.client.IItemRenderer.ItemRenderType;
 import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.FakePlayerFactory;
 import net.minecraftforge.common.ForgeDirection;
 //import net.minecraftforge.liquids.LiquidDictionary;
 //import net.minecraftforge.liquids.LiquidStack;
 import net.minecraftforge.fluids.FluidStack;
 
 @SideOnly(Side.CLIENT)
 public class SocketRenderer extends TileEntitySpecialRenderer
 {
 	public static final SocketRenderer instance = new SocketRenderer();
 	private final Tessellator tessellator = Tessellator.instance;
 	private final RenderBlocks blockRender = new RenderBlocks();
 	
 	@Override
 	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float counter)
 	{
 		TileSocket ts = (TileSocket)t;
 		BlockSocket b = (BlockSocket)SocketsMod.socket;
 		SocketModule m;
 		
 		FMLClientHandler.instance().getClient().entityRenderer.disableLightmap(1);
 		RenderHelper.disableStandardItemLighting();
 		
 		for(int side = 0; side < 6; side++)
 		{	
 			m = ts.getSide(ForgeDirection.getOrientation(side));
 			
 			GL11.glPushMatrix();
 			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
 			GL11.glEnable(GL11.GL_CULL_FACE);
 			GL11.glDisable(GL11.GL_LIGHTING);
 			GL11.glEnable(GL11.GL_BLEND);
 			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			
 			
 			GL11.glTranslatef((float)x, (float)y, (float)z);
 			
 			
 			switch(side)
 			{
 			case 0:
 				GL11.glRotatef(270, 1, 0, 0);
 				GL11.glRotatef(180, 0, 0, 1);
 				GL11.glTranslatef(-1.0F, 0.0F, -0.001F);
 				break;
 			case 1:
 				GL11.glRotatef(90, 1, 0, 0);
 				GL11.glTranslatef(0.0F, 0.0F, -1.001F);
 				break;
 			case 2:
 				GL11.glRotatef(180, 0, 0, 1);
 				GL11.glTranslatef(-1.0F, -1.0F, -0.001F);
 				break;
 			case 3:
 				GL11.glRotatef(180, 0, 0, 1);
 				GL11.glRotatef(180, 0, 1, 0);
 				GL11.glTranslatef(0.0F, -1.0F, -1.001F);
 				break;
 			case 4:
 				GL11.glRotatef(180, 0, 0, 1);
 				GL11.glRotatef(270, 0, 1, 0);
 				GL11.glTranslatef(0.0F, -1.0F, -0.001F);
 				break;
 			case 5:
 				GL11.glRotatef(180, 0, 0, 1);
 				GL11.glRotatef(90, 0, 1, 0);
 				GL11.glTranslatef(-1.0F, -1.0F, -1.001F);
 				break;
 			}
 			
 			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/atlas/blocks.png"));
 			//Minecraft.getMinecraft().renderEngine.bindTexture(par1ResourceLocation)
 			//bindTextureByName("/terrain.png");
 			
 			Icon[] icons = new Icon[4];
 			
 			if(m.hasIndicator(0))icons[0] = b.tankIndicator[ts.tankIndicatorIndex(side)];
 			if(m.hasIndicator(1))icons[1] = b.inventoryIndicator[ts.inventoryIndicatorIndex(side)];
 			if(m.hasIndicator(2))icons[2] = b.rsIndicator[ts.rsIndicatorIndex(side)];
 			if(m.hasIndicator(3))icons[3] = b.latchIndicator[ts.latchIndicatorIndex(side)];
 			
 			if(! ts.sideLocked[side]) for(int i = 0; i < 4; i++)
 			{
 				if(m.hasIndicator(i))
 				{
 					tessellator.startDrawingQuads();
 					
 					tessellator.addVertexWithUV(0, 1, 0, icons[i].getMinU(), icons[i].getMaxV());
 					tessellator.addVertexWithUV(1, 1, 0, icons[i].getMaxU(), icons[i].getMaxV());
 					tessellator.addVertexWithUV(1, 0, 0, icons[i].getMaxU(), icons[i].getMinV());
 					tessellator.addVertexWithUV(0, 0, 0, icons[i].getMinU(), icons[i].getMinV());
 					
 					tessellator.draw();
 				}
 			}
 			
 			int tankToRender = m.getTankToRender(ts, ts.configs[side], ForgeDirection.getOrientation(side));
 			
 			if(tankToRender != -1 && ts.tanks[tankToRender].getFluid() != null)
 			{
 				//FluidStack ls = FluidDictionary.getCanonicalLiquid(ts.tanks[tankToRender].getLiquid());
 				FluidStack ls = ts.tanks[tankToRender].getFluid();
 				
 				if(ls != null)
 				{
 						int cap = 8000;
 						
 						for(int i = 0; i < 6; i++)
 						{
 							SocketModule m2 = ts.getSide(ForgeDirection.getOrientation(i));
 							if(m2 instanceof ModPressurizer)
 							{
 								cap = 32000;
 								break;
 							}
 						}
 						float scale = ts.tanks[tankToRender].getFluid().amount / (float) cap;
 						Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/atlas/blocks.png"));
 						//bindTextureByName(ls.getTextureSheet());
 						if(ls.getFluid() != null && ls.getFluid().getIcon() != null)
 						{
 							Icon fluidIcon = ls.getFluid().getIcon();//ls.getRenderingIcon();
 							
 							GL11.glTranslatef(0.25F, 0.25F, 0.0005F);
 							GL11.glScalef(0.5F, 0.5F, 0.5F);
 							GL11.glTranslatef(0.0F, 1.0F - scale, 0.0F);
 							GL11.glScalef(1.0F, scale, 1.0F);
 							
 							tessellator.startDrawingQuads();
 							
 							tessellator.addVertexWithUV(0, 1, 0, fluidIcon.getMinU(), fluidIcon.getMaxV());
 							tessellator.addVertexWithUV(1, 1, 0, fluidIcon.getMaxU(), fluidIcon.getMaxV());
 							tessellator.addVertexWithUV(1, 0, 0, fluidIcon.getMaxU(), fluidIcon.getMinV());
 							tessellator.addVertexWithUV(0, 0, 0, fluidIcon.getMinU(), fluidIcon.getMinV());
 							
 							tessellator.draw();
 							
 							GL11.glScalef(1.0F, 1.0F/scale, 1.0F);
 							GL11.glTranslatef(0.0F, -(1.0F - scale), 0.0F);
 							GL11.glScalef(2.0F, 2.0F, 2.0F);
 							GL11.glTranslatef(-0.25F, -0.25F,  -0.0005F);
 						}
 				}
 				
 			}
 			
 			Icon[] additional = m.getAdditionalOverlays(ts, ts.configs[side], ForgeDirection.getOrientation(side));
 			//bindTextureByName("/terrain.png");
 			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/atlas/blocks.png"));
 			
 			for(int i = 0; i < additional.length; i++)
 			{
 				tessellator.startDrawingQuads();
 				
 				if(side != 0 || ! m.flipBottomOverlay())
 				{
 					tessellator.addVertexWithUV(0, 1, 0, additional[i].getMinU(), additional[i].getMaxV());
 					tessellator.addVertexWithUV(1, 1, 0, additional[i].getMaxU(), additional[i].getMaxV());
 					tessellator.addVertexWithUV(1, 0, 0, additional[i].getMaxU(), additional[i].getMinV());
 					tessellator.addVertexWithUV(0, 0, 0, additional[i].getMinU(), additional[i].getMinV());
 				}
 				else
 				{
 					tessellator.addVertexWithUV(0, 1, 0, additional[i].getMaxU(), additional[i].getMaxV());
 					tessellator.addVertexWithUV(1, 1, 0, additional[i].getMinU(), additional[i].getMaxV());
 					tessellator.addVertexWithUV(1, 0, 0, additional[i].getMinU(), additional[i].getMinV());
 					tessellator.addVertexWithUV(0, 0, 0, additional[i].getMaxU(), additional[i].getMinV());
 				}
 				
 				tessellator.draw();
 			}
 			
 			
 			String s = m.getTextToRender(ts, ts.configs[side], ForgeDirection.getOrientation(side));
 			
 			GL11.glTranslatef(0.25F, 0.25F, 0.0F);
 			
 			GL11.glScalef(0.01F, 0.01F, 1);
 			
 			if(s != null) this.getFontRenderer().drawString(s, -this.getFontRenderer().getStringWidth(s) / 2, 2, -1);
 			
 			GL11.glScalef(100, 100, 1);
 			
 			ItemStack theStack= m.getItemToRender(ts, ts.configs[side], ForgeDirection.getOrientation(side));
 			
 			if(theStack != null)
 			{
 				Item theItem = theStack.getItem();
 				
 				GL11.glScalef(0.5F, 0.5F, 0.5F);
 				
 				IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(theStack, ItemRenderType.INVENTORY);
 				
 				boolean isBlock = false;
 				
 				if(theStack.itemID < 4096 && Block.blocksList[theStack.itemID] != null && ! Block.blocksList[theStack.itemID].getUnlocalizedName().equals("tile.ForgeFiller")) isBlock = true;
 				
 				if(isBlock && RenderBlocks.renderItemIn3d(Block.blocksList[theItem.itemID].getRenderType()) || customRenderer != null)
 				{
 					//bindTextureByName("/terrain.png");
 					Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/atlas/blocks.png"));
 					
 					boolean usesHelper = customRenderer == null ? true : customRenderer.shouldUseRenderHelper(ItemRenderType.INVENTORY, theStack, ItemRendererHelper.INVENTORY_BLOCK);
 					
 					int color = theItem.getColorFromItemStack(theStack, 0);
 					float r = (float) (color >> 16 & 255) / 255.0F;
 					float g = (float) (color >> 8 & 255) / 255.0F;
 					float bl = (float) (color & 255) / 255.0F;
 					GL11.glColor4f(r, g, bl, 1);
 					
 					GL11.glTranslatef(0.25F, 0.25F, 0.0F);
 					GL11.glScalef(0.5F, 0.5F, 0.5F);
 					
 					if (usesHelper){
 						GL11.glScalef(1, 1, 0.0001F);
 						GL11.glTranslatef(0.5F, 0.5F, 0);
 						GL11.glRotatef(210, 1, 0, 0);
 						GL11.glRotatef(-45, 0, 1, 0);
 					}else{
 						GL11.glTranslatef(0, 0, -3);
 					}
 					
 					if (usesHelper) blockRender.useInventoryTint = true;
 					
 					if (customRenderer != null){
 						customRenderer.renderItem(ItemRenderType.INVENTORY, theStack, blockRender);
 					}else{
 						blockRender.renderBlockAsItem(Block.blocksList[theStack.itemID], theStack.getItemDamage(), 1);
 					}
 				}
 				else
 				{
 					//bindTextureByName("/gui/items.png");
 					/*Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/atlas/items.png"));
 					
 					if(theItem instanceof ItemBlock) inecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/atlas/blocks.png"));//bindTextureByName("/terrain.png");*/
 					
 					Minecraft.getMinecraft().renderEngine.bindTexture(theStack.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
 						
 					if(theItem.requiresMultipleRenderPasses())
 					{
 						int passes = theItem.getRenderPasses(theStack.getItemDamage());
 						
 						for(int i = 0; i < passes; i++)
 						{
 							Minecraft.getMinecraft().renderEngine.bindTexture(theStack.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
 							Icon itemIcon = theItem.getIconFromDamageForRenderPass(theStack.getItemDamage(), i);//, FakePlayerFactory.getMinecraft(ts.worldObj), theStack, 0);
							if (itemIcon != null) {
								
								tessellator.startDrawingQuads();
 							
								tessellator.addVertexWithUV(0, 1, 0, itemIcon.getMinU(), itemIcon.getMaxV());
								tessellator.addVertexWithUV(1, 1, 0, itemIcon.getMaxU(), itemIcon.getMaxV());
								tessellator.addVertexWithUV(1, 0, 0, itemIcon.getMaxU(), itemIcon.getMinV());
								tessellator.addVertexWithUV(0, 0, 0, itemIcon.getMinU(), itemIcon.getMinV());
 							
								tessellator.draw();
							
							}
							else
							{
								// Log an error, But don't crash the game... Hopefully...
							}
 						}
 					}
 					else
 					{
 					
 						Icon itemIcon = theItem.getIcon(theStack, 0);
 						
 						tessellator.startDrawingQuads();
 						
 						tessellator.addVertexWithUV(0, 1, 0, itemIcon.getMinU(), itemIcon.getMaxV());
 						tessellator.addVertexWithUV(1, 1, 0, itemIcon.getMaxU(), itemIcon.getMaxV());
 						tessellator.addVertexWithUV(1, 0, 0, itemIcon.getMaxU(), itemIcon.getMinV());
 						tessellator.addVertexWithUV(0, 0, 0, itemIcon.getMinU(), itemIcon.getMinV());
 						
 						tessellator.draw();
 					}
 				}
 			}
 
 			
 			
 			
 			
 			FMLClientHandler.instance().getClient().entityRenderer.enableLightmap(1);
 			RenderHelper.enableStandardItemLighting();
 			
 			GL11.glDisable(GL11.GL_CULL_FACE);
 			GL11.glEnable(GL11.GL_LIGHTING);
 			GL11.glPopAttrib();
 			GL11.glPopMatrix();
 		}
 		
 		FMLClientHandler.instance().getClient().entityRenderer.enableLightmap((double) counter);
 		RenderHelper.enableStandardItemLighting();
 		
 		//Do custom rendering for each module
 		
 		ForgeDirection d;
 		SocketModule module;
 		
 		for(int i = 0; i < 6; i++)
 		{
 			d = ForgeDirection.getOrientation(i);
 			module = ts.getSide(d);
 			module.doCustomRendering(ts, ts.getConfigForSide(d), d, tessellator, blockRender);
 		}
 	}
 
}
