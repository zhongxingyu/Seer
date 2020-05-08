 package jcj94.direWolfInABottle.client;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.Sphere;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.inventory.*;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.client.IItemRenderer;
 
 public class ItemRenderDWIAB implements IItemRenderer 
 {
 
 
 	public ItemRenderDWIAB()
 	{
 	}
 	@Override
 	public boolean handleRenderType(ItemStack item, ItemRenderType type) 
 	{
 		switch(type)
 		{
 		case EQUIPPED: return true;
 		default: return false;
 		}
 	}
 
 	@Override
 	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) 
 	{
 		return false;
 	}
 
 	@Override
 	public void renderItem(ItemRenderType type, ItemStack item, Object... data) 
 	{
 		switch(type)
 		{
 		case EQUIPPED:
 		{
			Minecraft.getMinecraft().renderEngine.bindTexture("/dwiab/DWIAB.png");
 			if(!((EntityPlayer)data[1] == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) && RenderManager.instance.playerViewY == 180.0F)))
 			{
 				GL11.glPushMatrix();
 
 				GL11.glRotatef(90, 1.00F, 0.00F, 0.00F);
 
 				GL11.glRotatef(130, 0.00F, 1.00F, 0.00F);
 
 				GL11.glRotatef(90, 0.00F, 0.00F, 1.00F);
 
 				float scale = 0.50F;
 
 				GL11.glScalef(scale, scale, scale);	
 
 				GL11.glTranslatef(-0.025F, 0.05F, 0.70F);
 
 				GL11.glPopMatrix();
 			}
 		}
 		default:
 			break;
 		}
 
 	}
 
 }
