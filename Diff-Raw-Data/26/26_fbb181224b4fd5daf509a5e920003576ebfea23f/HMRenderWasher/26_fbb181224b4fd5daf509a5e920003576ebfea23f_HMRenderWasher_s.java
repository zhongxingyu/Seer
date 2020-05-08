 
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
 public class HMRenderWasher extends TileEntitySpecialRenderer
 {
 	public static HawksMachinery BASEMOD;
 	private HMModelWasher model;
 	
 	public HMRenderWasher()
 	{
 		this.model = new HMModelWasher();
 		
 	}
 	
 	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var3, double var4, float var5)
 	{
 		bindTextureByName(BASEMOD.TEXTURE_PATH + "/Washer.png");
 		GL11.glPushMatrix();
 		GL11.glTranslatef((float) var2 + 0.5F, (float) var3 + 1.5F, (float) var4 + 0.5F);
		switch (var1.worldObj.getBlockMetadata(var1.xCoord, var1.yCoord, var1.zCoord))
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
