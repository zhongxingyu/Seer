 package ml.boxes.client.render.item;
 
 import ml.boxes.Boxes;
 import ml.boxes.client.render.tile.BoxTESR;
 import ml.boxes.data.ItemBoxContainer;
 import ml.boxes.inventory.ContainerBox;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.ItemRenderer;
 import net.minecraft.client.renderer.OpenGlHelper;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.client.renderer.entity.RenderPlayer;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.Timer;
 import net.minecraftforge.client.IItemRenderer;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class BoxItemRenderer implements IItemRenderer {
 
 	@Override
 	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
 		switch (type) {
 		case ENTITY:
 		case EQUIPPED:
 		case INVENTORY:
 		case EQUIPPED_FIRST_PERSON:
 			return true;
 		case FIRST_PERSON_MAP:
 		}
 		return false;
 	}
 
 	@Override
 	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
 			ItemRendererHelper helper) {
 		switch (helper) {
 		case ENTITY_ROTATION:
 		case ENTITY_BOBBING:
 		case EQUIPPED_BLOCK:
 		case BLOCK_3D:
 		case INVENTORY_BLOCK:
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
 
 		switch (type){
 		case ENTITY:
 			GL11.glPushMatrix();
 			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
 			BoxTESR.instance.setBoxFlaps(5, 2, 0, 0);
 			break;
 		case EQUIPPED_FIRST_PERSON:
 			EntityLiving holder = (EntityLiving)data[1];
 			Minecraft mc = FMLClientHandler.instance().getClient();
 			if (Boxes.config.enableMapStyleRendering &&
 					holder instanceof EntityPlayer &&
 					holder == mc.thePlayer &&
 					((EntityPlayer) holder).openContainer instanceof ContainerBox &&
 					((ContainerBox)((EntityPlayer)holder).openContainer).box instanceof ItemBoxContainer &&
 					mc.gameSettings.thirdPersonView == 0 &&
 					!mc.renderViewEntity.isPlayerSleeping() &&
 					!mc.gameSettings.hideGUI &&
 					!mc.playerController.enableEverythingIsScrewedUpMode()
 					){
 
 				//This section took a lot longer than it should have...
 				
 				GL11.glPopMatrix();
 				GL11.glPushMatrix();
 				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 				GL11.glScalef(2.5F, 2.5F, 2.5F);
 				
 				float tickTime = ((Timer)ReflectionHelper.getPrivateValue(Minecraft.class, mc, 10)).renderPartialTicks;
 				
 				if (holder.rotationPitch<45F && Boxes.config.mapRenderingView){
 					holder.rotationPitch += tickTime * 6F;
 				}
 				
 				float prevEuipProg = ReflectionHelper.getPrivateValue(ItemRenderer.class, RenderManager.instance.itemRenderer, 3);
 				float euipProg = ReflectionHelper.getPrivateValue(ItemRenderer.class, RenderManager.instance.itemRenderer, 2);
 				
 				float f1 = prevEuipProg + (euipProg - prevEuipProg) * tickTime;
 				float f4 = 0.8F;
 
 				float f2 = holder.prevRotationPitch + (holder.rotationPitch - holder.prevRotationPitch) * tickTime;
 
 				float f7 = holder.getSwingProgress(tickTime);
 				float f6 = MathHelper.sin(f7 * f7 * 3.141593F);
 				float f5 = MathHelper.sin(MathHelper.sqrt_float(f7) * 3.141593F);
 				GL11.glRotatef(f5 * 80.0F, 1.0F, 0.0F, 0.0F);
 				GL11.glRotatef(f5 * 20.0F, 0.0F, 0.0F, 1.0F);
 				GL11.glRotatef(f6 * 20.0F, 0.0F, 1.0F, 0.0F);
 				GL11.glRotatef(45.0F, 0.0F, -1.0F, 0.0F);
 				GL11.glTranslatef(-0.7F * f4, 0.6F - (-0.65F * f4 - (1.0F - f1) * 0.6F), 0.9F * f4);
 
 				GL11.glPushMatrix();
 
 				float f3 = mc.theWorld.getLightBrightness(MathHelper.floor_double(holder.posX), MathHelper.floor_double(holder.posY), MathHelper.floor_double(holder.posZ));
 				f3 = 1.0F;
 				int i = mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(holder.posX), MathHelper.floor_double(holder.posY), MathHelper.floor_double(holder.posZ), 0);
 				int j = i % 65536;
 				int k = i / 65536;
 				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
 				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 
 				i = Item.itemsList[item.itemID].getColorFromItemStack(item, 0);
 				f7 = (i >> 16 & 0xFF) / 255.0F;
 				f6 = (i >> 8 & 0xFF) / 255.0F;
 				f5 = (i & 0xFF) / 255.0F;
 				GL11.glColor4f(f3 * f7, f3 * f6, f3 * f5, 1.0F);
 
 				f6 = MathHelper.sin(f7 * 3.141593F);
 				f5 = MathHelper.sin(MathHelper.sqrt_float(f7) * 3.141593F);
 				GL11.glTranslatef(-f5 * 0.4F, MathHelper.sin(MathHelper.sqrt_float(f7) * 3.141593F * 2.0F) * 0.2F, -f6 * 0.2F);
 				f7 = 1.0F - f2 / 45.0F + 0.1F;
 
 				if (f7 < 0.0F)
 				{
 					f7 = 0.0F;
 				}
 
 				if (f7 > 1.0F)
 				{
 					f7 = 1.0F;
 				}
 
 				f7 = -MathHelper.cos(f7 * 3.141593F) * 0.5F + 0.5F;
 				GL11.glTranslatef(0.0F, 0.0F * f4 - (1.0F - f1) * 1.2F - f7 * 0.5F + 0.04F, -0.9F * f4);
 				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
 				GL11.glRotatef(f7 * -85.0F, 0.0F, 0.0F, 1.0F);
 				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 				GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTextureForDownloadableImage(holder.skinUrl, holder.getTexture()));
 
 				for (k = 0; k < 2; k++)
 				{
 					int l = k * 2 - 1;
 					GL11.glPushMatrix();
 					GL11.glTranslatef(-0.0F, -0.6F, 1.1F * l);
 					GL11.glRotatef(-45 * l, 1.0F, 0.0F, 0.0F);
 					GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
 					GL11.glRotatef(59.0F, 0.0F, 0.0F, 1.0F);
 					GL11.glRotatef(-65 * l, 0.0F, 1.0F, 0.0F);
 					Render render = RenderManager.instance.getEntityRenderObject(holder);
 					RenderPlayer renderplayer = (RenderPlayer)render;
 					float f10 = 1.0F;
 					GL11.glScalef(f10, f10, f10);
 					renderplayer.renderFirstPersonArm((EntityPlayer)holder);
 					GL11.glPopMatrix();
 				}
 
 				f6 = holder.getSwingProgress(tickTime);
 				f5 = MathHelper.sin(f6 * f6 * 3.141593F);
 				float f8 = MathHelper.sin(MathHelper.sqrt_float(f6) * (float)Math.PI);
 				GL11.glRotatef(-f5 * 20.0F, 0.0F, 1.0F, 0.0F);
 				GL11.glRotatef(-f8 * 20.0F, 0.0F, 0.0F, 1.0F);
 				GL11.glRotatef(-f8 * 80.0F, 1.0F, 0.0F, 0.0F);
 				float f10 = 0.38F;
 				GL11.glScalef(f10, f10, f10);
 				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
 				GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
 				GL11.glTranslatef(-1.0F, -1.0F, 0.0F);
 				
 				GL11.glRotatef(270F, 1F, 0, 0);
 				GL11.glRotatef(90F, 0, 1F, 0);
 				GL11.glScalef(2F, 2F, 2F);
 				GL11.glTranslatef(-1F, -1F, 0.0F);
 
 				BoxTESR.instance.setBoxFlaps(150, 150, 150, 150);
				break;
 			}
 		case EQUIPPED:
 				GL11.glPushMatrix();
 				BoxTESR.instance.setBoxFlaps(5, 2, 0, 0);
 			break;
 		case INVENTORY:
 			GL11.glPushMatrix();
 			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
 			BoxTESR.instance.setBoxFlaps(0, 0, 0, 0);
 			break;
 		}
 		BoxTESR.instance.renderBox(new ItemBoxContainer(item).getBox().boxColor);
 		GL11.glPopMatrix();
 	}
 }
