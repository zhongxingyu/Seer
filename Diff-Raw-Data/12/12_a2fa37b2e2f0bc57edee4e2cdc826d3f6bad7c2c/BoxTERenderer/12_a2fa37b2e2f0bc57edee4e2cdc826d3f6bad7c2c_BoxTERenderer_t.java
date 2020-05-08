 package ml.boxes.client;
 
 import ml.boxes.TileEntityBox;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.item.ItemDye;
 import net.minecraft.tileentity.TileEntity;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 import org.lwjgl.opengl.GL14;
 
 public class BoxTERenderer extends TileEntitySpecialRenderer {
 
 	private ModelBox boxModel = new ModelBox();
 	
 	@Override
 	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
 			double var6, float tickTime) {
 				
 		TileEntityBox box = (TileEntityBox)var1;
 		//int meta = box.worldObj.getBlockMetadata(box.xCoord, box.yCoord, box.zCoord);
 		
 		GL11.glPushMatrix();
         GL11.glEnable(GL12.GL_RESCALE_NORMAL);
         GL11.glTranslatef((float)var2, (float)var4, (float)var6);
         GL11.glTranslatef(0.5F, 0.5F, 0.5F);
         GL11.glRotatef(box.facing*90F, 0F, 1F, 0F);
         GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
         
         float openAngleInner = box.prevAngleInner + (box.flapAngleInner-box.prevAngleInner)*tickTime;
         openAngleInner = (float)Math.sin(openAngleInner*3.14/2);
         int innerAngle = (int)(120 * openAngleInner);
         
         float openAngleOuter = box.prevAngleOuter + (box.flapAngleOuter-box.prevAngleOuter)*tickTime;
         openAngleOuter = (float)Math.sin(openAngleOuter*3.14/2);
         int outerAngle = (int)(120 * openAngleOuter); 
         
         setBoxFlaps(outerAngle + 3, outerAngle+1, innerAngle, innerAngle);
 
         renderBox(box.getBoxData().boxColor);
 
         GL11.glDisable(GL12.GL_RESCALE_NORMAL);
         GL11.glPopMatrix();
 
 	}
 
 	public void setBoxFlaps(int a, int b, int c, int d){
 		boxModel.flap1.rotateAngleX = (float)Math.toRadians(360-a);
 		boxModel.flap2.rotateAngleX = (float)Math.toRadians(b);
 		boxModel.flap3.rotateAngleZ = (float)Math.toRadians(c);
 		boxModel.flap4.rotateAngleZ = (float)Math.toRadians(360-d);
 	}
 
 	public void renderBox(int color){
 
 		float red = ((color >> 16) & 0xFF)/255F;
 		float green = ((color >> 8) & 0xFF)/255F;
 		float blue = (color & 0xFF)/255F;
 
 		GL11.glColor4f(red, green, blue, 1.0F);
 
		bindTextureByName("/ml/boxes/res/boxColor.png");
 		boxModel.renderAll();
 		
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
 		GL11.glColor4f(0.7F, 0.47F, 0.3F, 1F);
		bindTextureByName("/ml/boxes/res/box.png");
 		boxModel.renderAll();
 
 		GL11.glDisable(GL11.GL_BLEND);
 		GL11.glColor4f(1F, 1F, 1F, 1F);
 	}
 
 }
