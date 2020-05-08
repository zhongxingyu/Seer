 package slimevoid.tmf.client.renderers;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import slimevoid.tmf.client.renderers.models.ModelGrinder;
 import slimevoid.tmf.core.lib.ResourceLib;
 import slimevoid.tmf.machines.tileentities.TileEntityGrinder;
 
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.tileentity.TileEntity;
 
 public class TileEntitySpecialRendererGrinder extends TileEntitySpecialRenderer {	
 	@Override
 	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTickTime) {
 		ModelGrinder grinder = new ModelGrinder((TileEntityGrinder) tile);
 		
 		GL11.glPushMatrix();
 			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 			GL11.glTranslatef((float)x, (float)y, (float)z);
 			
 			this.bindTextureByName(ResourceLib.MACHINE_TEXTURE_PATH);
 			
 			int meta = 0;
 			if ( tile.worldObj != null )
 				meta = tile.getBlockMetadata();
 			
 			switch (meta) {
 				case 2:
 					GL11.glTranslatef(0.5f, 0.5f, 0.5f);
 					GL11.glRotatef(90, 0, 1, 0);
 					GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
 					break;
 				case 3:
 					GL11.glTranslatef(0.5f, 0.5f, 0.5f);
 					GL11.glRotatef(270, 0, 1, 0);
 					GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
 					break;
 				case 4:
 					GL11.glTranslatef(0.5f, 0.5f, 0.5f);
 					GL11.glRotatef(180, 0, 1, 0);
 					GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
 					break;
 			}
 
 			grinder.renderAll();
 			
 			if ( tile.getBlockType() != null ) {
 				grinder.updateBounds(tile.getBlockType());
 			}
 			
 			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	}
 }
