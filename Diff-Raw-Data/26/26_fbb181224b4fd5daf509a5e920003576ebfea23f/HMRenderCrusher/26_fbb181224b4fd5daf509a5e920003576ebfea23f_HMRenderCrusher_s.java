 
 package hawksmachinery;
 
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.TileEntitySpecialRenderer;
 import org.lwjgl.opengl.GL11;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMRenderCrusher extends TileEntitySpecialRenderer
 {
 	public static HawksMachinery BASEMOD;
 	private HMModelCrusher model;
 	
 	public HMRenderCrusher()
 	{
 		this.model = new HMModelCrusher();
 		
 	}
 	
 	@Override
 	public void renderTileEntityAt(TileEntity tileEntity, double par2, double par3, double par4, float par5)
 	{
 		bindTextureByName(BASEMOD.TEXTURE_PATH + "/Crusher.png");
 		GL11.glPushMatrix();
 		GL11.glTranslatef((float)par2 + 0.5F, (float)par3 + 1.5F, (float)par4 + 0.5F);
 		switch (tileEntity.worldObj.getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord))
 		{
 			case 2: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
 			case 3: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
 			case 4: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
 			case 5: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
 		}
 		
 		GL11.glScalef(1.0F, -1F, -1F);
 		this.model.render(null, 0, 0, 0, 0, 0, 0.0625F);
 		GL11.glPopMatrix();
 		
 	}
 	
 }
