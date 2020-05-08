 package me.leonblade.neatpics.client;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.sql.Time;
 import java.util.Calendar;
 import java.util.logging.Logger;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 import org.lwjgl.util.glu.GLU;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.FMLTextureFX;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.discovery.DirectoryDiscoverer;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import me.leonblade.neatpics.ItemNeatPic;
 import me.leonblade.neatpics.EntityNeatPic;
 import me.leonblade.neatpics.ModNeatPic;
 import me.leonblade.neatpics.CommonProxy;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.OpenGlHelper;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.RenderEngine;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.client.renderer.texturefx.TextureCompassFX;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Direction;
 import net.minecraft.util.EnumArt;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.storage.MapData;
 import net.minecraftforge.client.MinecraftForgeClient;
 
 @SideOnly(Side.CLIENT)
 public class RenderNeatPic extends Render 
 {	
 	private float rotation = 0.0f;
 	
 	public void renderPic(EntityNeatPic entity, double x, double y, double z, float rotation, float k) 
 	{	
 		if (entity.hangingDirection == 2)
         {
             x -= 0.5 * entity.imageWidth;
             z += 0.025;
         }
 
         if (entity.hangingDirection == 1)
         {
             z += 0.5 * entity.imageWidth;
             x += 0.025;
         }
 
         if (entity.hangingDirection == 0)
         {
         	x += 0.5 * entity.imageWidth;
         	z -= 0.025;
         }
 
         if (entity.hangingDirection == 3)
         {
         	z -= 0.5 * entity.imageWidth;
         	x -= 0.025;
         }
         
         y -= 0.5 * entity.imageHeight;
         
         Tessellator tessellator = Tessellator.instance;
 		
 		if (!this.loadDownloadableImageTexture(entity.textureUrl, null))
 		{
 			GL11.glPushMatrix();
 	        GL11.glTranslated(x, y, z);
 	        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
 	
 	        float var11 = 0.0625F;
 	        GL11.glScalef(var11, var11, var11);
 	        
 	        GL11.glDisable(GL11.GL_TEXTURE_2D);
 	        GL11.glDisable(GL11.GL_LIGHTING);
 	        
 	        tessellator.startDrawing(GL11.GL_QUADS);
 	        
 	        GL11.glColor3f(0.7734375f, 0.7734375f, 0.7734375f);
 	        tessellator.addVertex(0, 0, 0);
 	        tessellator.addVertex(0, entity.imageHeight * 16, 0);
 	        tessellator.addVertex(entity.imageWidth * 16, entity.imageHeight * 16, 0);
 	        tessellator.addVertex(entity.imageWidth * 16, 0, 0);
 	        
 	        tessellator.draw();
 	        
 	        GL11.glColor3f(1.0f, 1.0f, 1.0f);
 	        
 	        GL11.glEnable(GL11.GL_TEXTURE_2D);
 	        
 	        GL11.glPushMatrix();
 
 	        GL11.glTranslated(((entity.imageWidth * 16 / 2) - 8), (entity.imageHeight * 16 / 2) - 8, -0.01);
 	        
 	        GL11.glTranslated(8, 8, 0);
 	        GL11.glRotatef(this.rotation+=3.0f, 0.0f, 0.0f, 1.0f);
 	        GL11.glScaled(0.5, 0.5, 0.5);
 	        
 	        FMLClientHandler.instance().getClient().renderEngine.bindTexture(FMLClientHandler.instance().getClient().renderEngine.getTexture(CommonProxy.SPIN_PNG));
 	        
 	        tessellator.startDrawing(GL11.GL_QUADS);
 	        
 	        tessellator.addVertexWithUV(-8, -8, 0, 1, 1);
 	        tessellator.addVertexWithUV(-8, 8, 0, 1, 0);
 	        tessellator.addVertexWithUV(8, 8, 0, 0, 0);
 	        tessellator.addVertexWithUV(8, -8, 0, 0, 1);
 	        
 	        tessellator.draw();
 	        
 	        GL11.glEnable(GL11.GL_LIGHTING);
 	        
 	        GL11.glPopMatrix();
 	        
 	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 	        GL11.glPopMatrix();
 		}
 		else
		{			
 			GL11.glPushMatrix();
 	        GL11.glTranslated(x, y, z);
 	        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
 	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 	        GL11.glEnable(GL11.GL_LIGHTING);
 	
 	        float var11 = 0.0625F;
 	        GL11.glScalef(var11, var11, var11);
 	        
 	        tessellator.startDrawing(GL11.GL_QUADS);
 	        
 	        tessellator.addVertexWithUV(0, 0, 0, 1, 1);
 	        tessellator.addVertexWithUV(0, entity.imageHeight * 16, 0, 1, 0);
 	        tessellator.addVertexWithUV(entity.imageWidth * 16, entity.imageHeight * 16, 0, 0, 0);
 	        tessellator.addVertexWithUV(entity.imageWidth * 16, 0, 0, 0, 1);
 	        
 	        tessellator.draw();
 	        
 	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 	        GL11.glPopMatrix();
 		}
 	}
 
 	public void doRender(Entity entity, double x, double y, double z, float j, float k) 
 	{
 		this.renderPic((EntityNeatPic)entity, x, y, z, j, k);
 	}
 
 }
