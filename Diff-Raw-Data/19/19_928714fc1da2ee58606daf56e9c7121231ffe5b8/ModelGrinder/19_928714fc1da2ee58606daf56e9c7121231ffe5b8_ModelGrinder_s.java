 package slimevoid.tmf.client.renderers.models;
 
 import java.io.File;
 
 import org.lwjgl.opengl.GL11;
 
 import slimevoid.lib.render.ModelSlimevoidObject;
 import slimevoid.lib.render.WavefrontOBJModelLoader;
 import slimevoid.lib.render.WavefrontOBJModelLoader.FaceMissingTextureException;
 import slimevoid.tmf.core.TMFCore;
 import slimevoid.tmf.core.lib.ResourceLib;
 import slimevoid.tmf.machines.blocks.BlockMachine;
 import slimevoid.tmf.machines.tileentities.TileEntityGrinder;
 import net.minecraft.block.Block;
 import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.tileentity.TileEntity;
 
 public class ModelGrinder extends ModelBase {
 	public ModelSlimevoidObject base;
 	public ModelSlimevoidObject spinner;
 	public TileEntityGrinder tile;
 	
 	public int rotationSpeedDivider = 2;
 	
 	public ModelGrinder(TileEntityGrinder tile) {
 		this.tile = tile;
 		try {
 			base = (new WavefrontOBJModelLoader()).loadObjFile(
 					this, 
 					256, 256, 
 					new File(TMFCore.class.getResource(ResourceLib.MODEL_GRINDER).getFile())
 			);
 			spinner = (new WavefrontOBJModelLoader()).loadObjFile(
 					this, 
 					256, 256, 
 					new File(TMFCore.class.getResource(ResourceLib.MACHINE_PREFIX+"grinderSpinner.obj").getFile())
 			);
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (ArithmeticException e) {
 			e.printStackTrace();
 		} catch (FaceMissingTextureException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateBounds(Block block) {
 		ModelSlimevoidObject.ModelSlimevoidObjectBounds baseBound = base.getBounds();
 		ModelSlimevoidObject.ModelSlimevoidObjectBounds spinnerBound = spinner.getBounds();
 
 		int minX = 0;
 		int minY = 0;
 		int minZ = 0;
 		int maxX = 0;
 		int maxY = 0;
 		int maxZ = 0;
 		
 		minX = baseBound.minX;
 		
 		if ( baseBound.minY > spinnerBound.minY )
 			minY = spinnerBound.minY;
 		else
 			minY = baseBound.minY;
 		
 		minZ = baseBound.minZ;
 		
 		maxX = baseBound.maxX;
 		
 		if ( baseBound.maxY < spinnerBound.maxY )
 			maxY = spinnerBound.maxY;
 		else
 			maxY = baseBound.maxY;
 		
 		maxZ = baseBound.maxZ;
 				
 		block.setBlockBounds((float)minX/16f, (float)minY/16f, (float)minZ/16f, (float)maxX/16f, (float)maxY/16f, (float)maxZ/16f);
 	}
 	
 	public void renderAll() {
 		base.render(0.0625F);
 		GL11.glPushMatrix();
 		if ( tile != null && tile.getBlockType() != null && ((BlockMachine)(tile.getBlockType())).isActive ) {
 			GL11.glTranslatef(0.5f, 0, 0.5f);	
 			GL11.glRotatef((System.currentTimeMillis()/rotationSpeedDivider)%360, 0, 1, 0);
 			GL11.glTranslatef(-0.5f, 0, -0.5f);
 		}
 		spinner.render(0.0625F);
 		GL11.glPopMatrix();
 	}
 }
