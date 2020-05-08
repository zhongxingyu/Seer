 /*
  * Copyright (c) 2013 monnef.
  */
 
 package monnef.core.client;
 
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.client.renderer.texture.Texture;
 import net.minecraft.client.renderer.texture.TextureStitched;
 import net.minecraft.entity.Entity;
 import net.minecraft.item.Item;
 import net.minecraft.util.Icon;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import java.lang.reflect.Field;
 
 public class RenderItemInAir extends Render {
     private Texture texture;
     private Icon icon;
 
     public RenderItemInAir(Item item) {
         super();
         icon = item.getIconFromDamage(0);
         Field ts = ReflectionHelper.findField(TextureStitched.class, "textureSheet", "field_94228_a");
         try {
             texture = (Texture) ts.get(icon);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
         GL11.glPushMatrix();
 
         GL11.glTranslatef((float) par2, (float) par4, (float) par6);
         GL11.glEnable(GL12.GL_RESCALE_NORMAL);
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         Tessellator var10 = Tessellator.instance;
         texture.bindTexture(0);
         this.doRender(var10);
         GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 
         GL11.glPopMatrix();
     }
 
     private void doRender(Tessellator par1Tessellator) {
         float f = icon.getMinU();
         float f1 = icon.getMaxU();
         float f2 = icon.getMinV();
         float f3 = icon.getMaxV();
         float f4 = 1.0F;
         float f5 = 0.5F;
         float f6 = 0.25F;
         GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         par1Tessellator.startDrawingQuads();
         par1Tessellator.setNormal(0.0F, 1.0F, 0.0F);
         par1Tessellator.addVertexWithUV((double) (0.0F - f5), (double) (0.0F - f6), 0.0D, (double) f, (double) f3);
         par1Tessellator.addVertexWithUV((double) (f4 - f5), (double) (0.0F - f6), 0.0D, (double) f1, (double) f3);
         par1Tessellator.addVertexWithUV((double) (f4 - f5), (double) (f4 - f6), 0.0D, (double) f1, (double) f2);
         par1Tessellator.addVertexWithUV((double) (0.0F - f5), (double) (f4 - f6), 0.0D, (double) f, (double) f2);
         par1Tessellator.draw();
     }
 }
