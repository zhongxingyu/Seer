 
 package net.specialattack.modjam.client.render;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.texture.TextureManager;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraftforge.client.IItemRenderer;
 
 import org.lwjgl.opengl.GL11;
 
 public class ItemRendererLens implements IItemRenderer {
 
     @Override
     public boolean handleRenderType(ItemStack item, ItemRenderType type) {
         return type != ItemRenderType.FIRST_PERSON_MAP;
     }
 
     @Override
     public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
         switch (helper) {
         case BLOCK_3D:
             return false;
         case ENTITY_BOBBING:
             return true;
         case ENTITY_ROTATION:
             return true;
         case EQUIPPED_BLOCK:
             return false;
         case INVENTORY_BLOCK:
             return false;
         default:
             return false;
         }
     }
 
     @Override
     public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
         GL11.glPushMatrix();
 
         GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
        //GL11.glBlendFunc(GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_DST_COLOR);
         int color = item.getItem().getColorFromItemStack(item, 0);
         double red = (float) ((color >> 16) & 0xFF) / 255.0F;
         double green = (float) ((color >> 8) & 0xFF) / 255.0F;
         double blue = (float) (color & 0xFF) / 255.0F;
         GL11.glColor3d(red, green, blue);
         this.renderIcon(item, item.getItem().getIcon(item, 0), type);
         GL11.glDisable(GL11.GL_BLEND);
 
         color = item.getItem().getColorFromItemStack(item, 1);
         red = (float) ((color >> 16) & 0xFF) / 255.0F;
         green = (float) ((color >> 8) & 0xFF) / 255.0F;
         blue = (float) (color & 0xFF) / 255.0F;
         GL11.glColor3d(red, green, blue);
         this.renderIcon(item, item.getItem().getIcon(item, 1), type);
 
         GL11.glPopMatrix();
     }
 
     private void renderIcon(ItemStack item, Icon icon, ItemRenderType type) {
         if (type == ItemRenderType.INVENTORY) {
             Tessellator tes = Tessellator.instance;
             tes.startDrawingQuads();
             tes.addVertexWithUV(0.0D, 16.0D, 0.0D, icon.getMinU(), icon.getMaxV());
             tes.addVertexWithUV(16.0D, 16.0D, 0.0D, icon.getMaxU(), icon.getMaxV());
             tes.addVertexWithUV(16.0D, 0.0D, 0.0D, icon.getMaxU(), icon.getMinV());
             tes.addVertexWithUV(0.0D, 0.0D, 0.0D, icon.getMinU(), icon.getMinV());
             tes.draw();
         }
         else {
             GL11.glPushMatrix();
             TextureManager textureManager = Minecraft.getMinecraft().renderEngine;
 
             textureManager.func_110577_a(textureManager.func_130087_a(item.getItemSpriteNumber()));
             Tessellator tess = Tessellator.instance;
             float minU = icon.getMinU();
             float maxU = icon.getMaxU();
             float minV = icon.getMinV();
             float maxV = icon.getMaxV();
             if (type == ItemRenderType.ENTITY) {
                 GL11.glTranslatef(-0.5F, -0.3F, 0.0F);
             }
 
             this.renderItemIn3D(tess, maxU, minV, minU, maxV, icon.getOriginX(), icon.getOriginY(), 0.0625F);
 
             GL11.glPopMatrix();
         }
     }
 
     private void renderItemIn3D(Tessellator tess, float maxU, float minV, float minU, float maxV, int originX, int originY, float scale) {
         tess.startDrawingQuads();
         tess.setNormal(0.0F, 0.0F, 1.0F);
         tess.addVertexWithUV(0.0D, 0.0D, 0.0D, (double) maxU, (double) maxV);
         tess.addVertexWithUV(1.0D, 0.0D, 0.0D, (double) minU, (double) maxV);
         tess.addVertexWithUV(1.0D, 1.0D, 0.0D, (double) minU, (double) minV);
         tess.addVertexWithUV(0.0D, 1.0D, 0.0D, (double) maxU, (double) minV);
         tess.draw();
         tess.startDrawingQuads();
         tess.setNormal(0.0F, 0.0F, -1.0F);
         tess.addVertexWithUV(0.0D, 1.0D, (double) (0.0F - scale), (double) maxU, (double) minV);
         tess.addVertexWithUV(1.0D, 1.0D, (double) (0.0F - scale), (double) minU, (double) minV);
         tess.addVertexWithUV(1.0D, 0.0D, (double) (0.0F - scale), (double) minU, (double) maxV);
         tess.addVertexWithUV(0.0D, 0.0D, (double) (0.0F - scale), (double) maxU, (double) maxV);
         tess.draw();
         float f5 = 0.5F * (maxU - minU) / (float) originX;
         float f6 = 0.5F * (maxV - minV) / (float) originY;
         tess.startDrawingQuads();
         tess.setNormal(-1.0F, 0.0F, 0.0F);
         int k;
         float f7;
         float f8;
 
         for (k = 0; k < originX; ++k) {
             f7 = (float) k / (float) originX;
             f8 = maxU + (minU - maxU) * f7 - f5;
             tess.addVertexWithUV((double) f7, 0.0D, (double) (0.0F - scale), (double) f8, (double) maxV);
             tess.addVertexWithUV((double) f7, 0.0D, 0.0D, (double) f8, (double) maxV);
             tess.addVertexWithUV((double) f7, 1.0D, 0.0D, (double) f8, (double) minV);
             tess.addVertexWithUV((double) f7, 1.0D, (double) (0.0F - scale), (double) f8, (double) minV);
         }
 
         tess.draw();
         tess.startDrawingQuads();
         tess.setNormal(1.0F, 0.0F, 0.0F);
         float f9;
 
         for (k = 0; k < originX; ++k) {
             f7 = (float) k / (float) originX;
             f8 = maxU + (minU - maxU) * f7 - f5;
             f9 = f7 + 1.0F / (float) originX;
             tess.addVertexWithUV((double) f9, 1.0D, (double) (0.0F - scale), (double) f8, (double) minV);
             tess.addVertexWithUV((double) f9, 1.0D, 0.0D, (double) f8, (double) minV);
             tess.addVertexWithUV((double) f9, 0.0D, 0.0D, (double) f8, (double) maxV);
             tess.addVertexWithUV((double) f9, 0.0D, (double) (0.0F - scale), (double) f8, (double) maxV);
         }
 
         tess.draw();
         tess.startDrawingQuads();
         tess.setNormal(0.0F, 1.0F, 0.0F);
 
         for (k = 0; k < originY; ++k) {
             f7 = (float) k / (float) originY;
             f8 = maxV + (minV - maxV) * f7 - f6;
             f9 = f7 + 1.0F / (float) originY;
             tess.addVertexWithUV(0.0D, (double) f9, 0.0D, (double) maxU, (double) f8);
             tess.addVertexWithUV(1.0D, (double) f9, 0.0D, (double) minU, (double) f8);
             tess.addVertexWithUV(1.0D, (double) f9, (double) (0.0F - scale), (double) minU, (double) f8);
             tess.addVertexWithUV(0.0D, (double) f9, (double) (0.0F - scale), (double) maxU, (double) f8);
         }
 
         tess.draw();
         tess.startDrawingQuads();
         tess.setNormal(0.0F, -1.0F, 0.0F);
 
         for (k = 0; k < originY; ++k) {
             f7 = (float) k / (float) originY;
             f8 = maxV + (minV - maxV) * f7 - f6;
             tess.addVertexWithUV(1.0D, (double) f7, 0.0D, (double) minU, (double) f8);
             tess.addVertexWithUV(0.0D, (double) f7, 0.0D, (double) maxU, (double) f8);
             tess.addVertexWithUV(0.0D, (double) f7, (double) (0.0F - scale), (double) maxU, (double) f8);
             tess.addVertexWithUV(1.0D, (double) f7, (double) (0.0F - scale), (double) minU, (double) f8);
         }
 
         tess.draw();
     }
 
 }
