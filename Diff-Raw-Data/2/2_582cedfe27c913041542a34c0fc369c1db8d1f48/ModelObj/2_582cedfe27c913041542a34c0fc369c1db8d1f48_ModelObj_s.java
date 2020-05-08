 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.client;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import monnef.core.utils.ColorHelper;
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.client.model.AdvancedModelLoader;
 import net.minecraftforge.client.model.IModelCustom;
 import net.minecraftforge.client.model.obj.WavefrontObject;
 import org.lwjgl.opengl.GL11;
 
 public class ModelObj extends ModelBase implements IModelObj {
     private final WavefrontObject model;
     private float rotationFix;
     private String texture;
     private ResourceLocation textureResource;
 
     public ModelObj(String resourceName, float rotationFix, String texture) {
         this.rotationFix = rotationFix;
         this.texture = texture;
         this.textureResource = new ResourceLocation(texture);
         IModelCustom tmp = AdvancedModelLoader.loadModel(resourceName);
         if (!(tmp instanceof WavefrontObject))
            throw new RuntimeException("Loaded model is not " + WavefrontObject.class.getSimpleName() + ", cannot continue (Forge probably changes implementation).");
         model = (WavefrontObject) tmp;
     }
 
     @Override
     public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
     }
 
     @Override
     public void bindTexture() {
         FMLClientHandler.instance().getClient().renderEngine.bindTexture(textureResource);
     }
 
     @Override
     public void render(float scale) {
         renderWithTint(scale, null);
     }
 
     @Override
     public void renderWithTint(float scale, ColorHelper.IntColor tint) {
         GL11.glPushMatrix();
         GL11.glScalef(scale, scale, scale);
         GL11.glRotatef(rotationFix, 0, 1, 0);
         if (tint != null) {
             GL11.glColor4f(tint.getFloatRed(), tint.getFloatGreen(), tint.getFloatBlue(), 1f);
         }
         model.renderAll();
         GL11.glPopMatrix();
     }
 
     @Override
     public void renderWithTexture(float scale) {
         renderWithTextureAndTint(scale, null);
     }
 
     @Override
     public void renderWithTextureAndTint(float scale, ColorHelper.IntColor tint) {
         bindTexture();
         renderWithTint(scale, tint);
     }
 
     @Override
     public void renderPossiblyWithTexture(float scale) {
         if (texture != null) renderWithTexture(scale);
         else render(scale);
     }
 
     @Override
     public WavefrontObject getModel() {
         return model;
     }
 }
