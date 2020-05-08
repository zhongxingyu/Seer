 package biotech.client.model;
 
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.entity.Entity;
 import net.minecraftforge.client.model.AdvancedModelLoader;
 import net.minecraftforge.client.model.IModelCustom;
 import net.minecraftforge.client.model.obj.WavefrontObject;
 
 import org.lwjgl.opengl.GL11;
 
 import biotech.Biotech;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
public class ModelCheese extends ModelBase
 {
     /** The Cheese*/
     private IModelCustom cheeseOBJ;
     public float cheeseScale = 0.3F;
     
     
    public ModelCheese()
     {
     	cheeseOBJ = AdvancedModelLoader.loadModel(Biotech.ModelBioCheese);
     }
     
     public void render()
     {
     	cheeseOBJ.renderAll();
     }
     
     public void render(Entity Entity, float par2, float par3, float par4, float par5, float par6, float par7)
     {
     	GL11.glPushMatrix();
     	GL11.glDisable(GL11.GL_LIGHTING);
 		
 		FMLClientHandler.instance().getClient().renderEngine.bindTexture(Biotech.BioCheeseTexture);
 		
 		GL11.glScalef(cheeseScale, cheeseScale, cheeseScale);
 		GL11.glTranslatef(0.0F, 0.0F, 0.0F);
 		//GL11.glRotatef(90F, 0, 0, 1F);
 		
 		this.render();
 		
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glPopMatrix();
     }
 }
