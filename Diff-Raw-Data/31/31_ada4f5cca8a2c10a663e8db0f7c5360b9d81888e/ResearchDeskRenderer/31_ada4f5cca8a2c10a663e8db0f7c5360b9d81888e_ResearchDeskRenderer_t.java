 package industrialscience.modules.research.frontend.renderer;
 
 import industrialscience.Modinfo;
 import industrialscience.modules.research.frontend.ResearchBlockType;
 import industrialscience.modules.research.frontend.models.ResearchDeskModel;
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 public class ResearchDeskRenderer extends TileEntitySpecialRenderer {
 	ResearchDeskModel mb=new ResearchDeskModel();
 	private static final ResourceLocation texture= new ResourceLocation(Modinfo.ModID, "textures/models/Researchtable.png");
     @Override
     public void renderTileEntityAt(TileEntity tileentity, double d0, double d1,
             double d2, float f) {
    	bindTexture(texture);
         GL11.glPushMatrix();
         GL11.glTranslatef((float) d0 + 0.5F, (float) d1 + 1.5F,
                 (float) d2 + 0.5F);
         GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);
         GL11.glScalef(1.0F, -1F, -1F);
             mb.render(tileentity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
         GL11.glPopMatrix();
 
     }
 
 }
