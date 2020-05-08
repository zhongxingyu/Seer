 package shadowcraft;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.ModelBase;
 import net.minecraft.src.Render;
 import net.minecraft.src.RenderLiving;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.TileEntitySpecialRenderer;
 
 @SideOnly(Side.CLIENT)
 public class RenderShadowCube extends TileEntitySpecialRenderer{
	
 	private ModelShadowCube modelShadowCube;
 	
 	public RenderShadowCube(ModelShadowCube modelShadowCubeArg){
 		modelShadowCube = modelShadowCubeArg;
 	}
 	
 	public void renderShadowCubeAt(TileEntityShadowRefinery tileEntity, double x, double y, double z, float f){
 		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F, (float)z + 0.5F);
		this.bindTextureByName("/item/shadowcube.png");
		this.modelShadowCube.render();
		GL11.glPopMatrix();
 	}
 	
 	@Override
 	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
 		this.renderShadowCubeAt((TileEntityShadowRefinery) tileEntity, x, y, z, f);
 	}
	
	
 }
