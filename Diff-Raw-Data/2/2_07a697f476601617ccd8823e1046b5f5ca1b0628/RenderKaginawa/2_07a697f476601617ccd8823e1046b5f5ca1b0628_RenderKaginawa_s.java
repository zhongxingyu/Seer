 package ruby.bamboo.render;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.Vec3;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import ruby.bamboo.BambooCore;
 import ruby.bamboo.entity.EntityKaginawa;
 
 public class RenderKaginawa extends Render {
    private static final ResourceLocation RESOURCE = new ResourceLocation(BambooCore.resourceDomain + "textures/particle/particles.png");
 
     public void doRenderKaginawa(EntityKaginawa par1Entity, double par2, double par4, double par6, float par8, float par9) {
         GL11.glPushMatrix();
         GL11.glTranslatef((float) par2, (float) par4, (float) par6);
         GL11.glEnable(GL12.GL_RESCALE_NORMAL);
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         byte b0 = 1;
         byte b1 = 2;
         bindEntityTexture(par1Entity);
         Tessellator tessellator = Tessellator.instance;
         float f2 = (b0 * 8 + 0) / 128.0F;
         float f3 = (b0 * 8 + 8) / 128.0F;
         float f4 = (b1 * 8 + 0) / 128.0F;
         float f5 = (b1 * 8 + 8) / 128.0F;
         float f6 = 1.0F;
         float f7 = 0.5F;
         float f8 = 0.5F;
         GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         tessellator.startDrawingQuads();
         tessellator.setNormal(0.0F, 1.0F, 0.0F);
         tessellator.addVertexWithUV(0.0F - f7, 0.0F - f8, 0.0D, f2, f5);
         tessellator.addVertexWithUV(f6 - f7, 0.0F - f8, 0.0D, f3, f5);
         tessellator.addVertexWithUV(f6 - f7, 1.0F - f8, 0.0D, f3, f4);
         tessellator.addVertexWithUV(0.0F - f7, 1.0F - f8, 0.0D, f2, f4);
         tessellator.draw();
         GL11.glDisable(GL12.GL_RESCALE_NORMAL);
         GL11.glPopMatrix();
 
         if (par1Entity.getThrower() != null) {
             float f9 = par1Entity.getThrower().getSwingProgress(par9);
             float f10 = MathHelper.sin(MathHelper.sqrt_float(f9) * (float) Math.PI);
             Vec3 vec3 = par1Entity.worldObj.getWorldVec3Pool().getVecFromPool(-0.5D, 0.03D, 0.8D);
             vec3.rotateAroundX(-(par1Entity.getThrower().prevRotationPitch + (par1Entity.getThrower().rotationPitch - par1Entity.getThrower().prevRotationPitch) * par9) * (float) Math.PI / 180.0F);
             vec3.rotateAroundY(-(par1Entity.getThrower().prevRotationYaw + (par1Entity.getThrower().rotationYaw - par1Entity.getThrower().prevRotationYaw) * par9) * (float) Math.PI / 180.0F);
             vec3.rotateAroundY(f10 * 0.5F);
             vec3.rotateAroundX(-f10 * 0.7F);
             double d3 = par1Entity.getThrower().prevPosX + (par1Entity.getThrower().posX - par1Entity.getThrower().prevPosX) * par9 + vec3.xCoord;
             double d4 = par1Entity.getThrower().prevPosY + (par1Entity.getThrower().posY - par1Entity.getThrower().prevPosY) * par9 + vec3.yCoord;
             double d5 = par1Entity.getThrower().prevPosZ + (par1Entity.getThrower().posZ - par1Entity.getThrower().prevPosZ) * par9 + vec3.zCoord;
             double d6 = par1Entity.getThrower() != Minecraft.getMinecraft().thePlayer ? (double) par1Entity.getThrower().getEyeHeight() : 0.0D;
 
             if (this.renderManager.options.thirdPersonView > 0 || par1Entity.getThrower() != Minecraft.getMinecraft().thePlayer) {
                 float f11 = (par1Entity.getThrower().prevRenderYawOffset + (par1Entity.getThrower().renderYawOffset - par1Entity.getThrower().prevRenderYawOffset) * par9) * (float) Math.PI / 180.0F;
                 double d7 = MathHelper.sin(f11);
                 double d8 = MathHelper.cos(f11);
                 d3 = par1Entity.getThrower().prevPosX + (par1Entity.getThrower().posX - par1Entity.getThrower().prevPosX) * par9 - d8 * 0.25D - d7 * 0.75D;
                 d4 = par1Entity.getThrower().prevPosY + d6 + (par1Entity.getThrower().posY - par1Entity.getThrower().prevPosY) * par9 - 0.45D;
                 d5 = par1Entity.getThrower().prevPosZ + (par1Entity.getThrower().posZ - par1Entity.getThrower().prevPosZ) * par9 - d7 * 0.25D + d8 * 0.75D;
             }
 
             double d9 = par1Entity.prevPosX + (par1Entity.posX - par1Entity.prevPosX) * par9;
             double d10 = par1Entity.prevPosY + (par1Entity.posY - par1Entity.prevPosY) * par9 + 0.75D;
             double d11 = par1Entity.prevPosZ + (par1Entity.posZ - par1Entity.prevPosZ) * par9;
             double d12 = ((float) (d3 - d9));
             double d13 = ((float) (d4 - d10));
             double d14 = ((float) (d5 - d11));
             GL11.glDisable(GL11.GL_TEXTURE_2D);
             GL11.glDisable(GL11.GL_LIGHTING);
             tessellator.startDrawing(3);
             tessellator.setColorOpaque_I(0);
             byte b2 = 16;
 
             for (int i = 0; i <= b2; ++i) {
                 float f12 = (float) i / (float) b2;
                 tessellator.addVertex(par2 + d12 * f12, par4 + d13 * (f12 * f12 + f12) * 0.5D + 0.25D, par6 + d14 * f12);
             }
 
             tessellator.draw();
             GL11.glEnable(GL11.GL_LIGHTING);
             GL11.glEnable(GL11.GL_TEXTURE_2D);
         }
     }
 
     @Override
     public void doRender(Entity entity, double d0, double d1, double d2, float f, float f1) {
         this.doRenderKaginawa((EntityKaginawa) entity, d0, d1, d2, f, f1);
     }
 
     @Override
     protected ResourceLocation getEntityTexture(Entity entity) {
         // TODO 自動生成されたメソッド・スタブ
         return RESOURCE;
     }
 }
