 package net.medsouz.omg.render;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import net.medsouz.omg.model.ModelBullet;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.client.model.ModelRenderer;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.ResourceLocation;
 
 public class RenderAmmunition extends Render{
//TODO: fix horrible desync issue :(
 	private static final ResourceLocation arrowTextures = new ResourceLocation("textures/entity/arrow.png");
 	ModelBullet b = new ModelBullet();
 	
 	@Override
 	public void doRender(Entity entity, double d0, double d1, double d2, float f, float f1) {
 		GL11.glPushMatrix();
 			GL11.glDisable(GL11.GL_TEXTURE_2D);
 			GL11.glColor3f(192f, 192f, 192f);
 			GL11.glTranslatef((float)d0, (float)d1, (float)d2);
 			GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f1, 0.0F, 1.0F, 0.0F);
 	        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f1, 0.0F, 0.0F, 1.0F);
 			b.render(entity, 0.05f);
 			GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glPopMatrix();
 	}
 
 	@Override
 	protected ResourceLocation getEntityTexture(Entity entity) {
 		return arrowTextures;
 	}
 
 }
