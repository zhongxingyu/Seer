 package shadowhax.crystalluscraft.block.renderer;
  
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 import shadowhax.crystalluscraft.block.model.ModelWarpPad;
 import shadowhax.crystalluscraft.block.tile.TileEntityWarpPad;
  
 public class RenderWarpPad extends TileEntitySpecialRenderer {
  
     public RenderWarpPad() {
         aModel = new ModelWarpPad();
     }
        
     public void renderAModelAt(TileEntityWarpPad tileentity1, double d, double d1, double d2, float f) {  
    	this.func_110628_a(new ResourceLocation("crystalluscraft:/textures/tileentity/warppad.png"));
         GL11.glPushMatrix();
         GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
         GL11.glScalef(1.0F, -1F, -1F);
         aModel.render(0.0625F);
         GL11.glPopMatrix();                    
     }
 
 
 	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f) {
         renderAModelAt((TileEntityWarpPad)tileentity, d, d1, d2, f);
     }
  
     private ModelWarpPad aModel;
 }
