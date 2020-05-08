 package zensiert1997.realphysics.core.models;
 
 import java.util.HashMap;
 
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.client.IItemRenderer;
 
 import org.lwjgl.opengl.GL11;
 
 import zensiert1997.realphysics.core.RealPhysics;
 import zensiert1997.realphysics.core.tile.TileGear;
 import zensiert1997.realphysics.util.MathHelper;
 
 public class RenderGear extends BaseTileEntitySpecialRenderer
 implements IItemRenderer {
 	
 	private static final float spikeRatio = 0.5f/11;
 	private static final float gearThickness = 1.0f/4.0f;
 	private static final float spikeSize = 0.1f;
 	private static HashMap<Integer, Integer> loadedGears = new HashMap<Integer, Integer>();
 	
 	public RenderGear() {
 		super(RealPhysics.modelGear, new ModelGear());
 	}
 	
 	@Override
 	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {
 		int spikes;
 		if(tile == null) {
 			spikes = 13;
 		} else {
 			spikes = ((TileGear)tile).spikes;
 		}
 		Integer displayList = loadedGears.get(spikes);
 		if(displayList != null && displayList > 0) {
 			GL11.glPushMatrix();
 			doNeededMatrixManipulations(tile, x, y, z);
 			drawGearFromDisplayList(displayList);
 			GL11.glPopMatrix();
 		} else {
 			GL11.glPushMatrix();
 			doNeededMatrixManipulations(tile, x, y, z);
 			loadAndRenderGear(spikes);
 			GL11.glPopMatrix();
 		}
 	}
 	
 	private void doNeededMatrixManipulations(TileEntity tile, double x, double y, double z) {
 		GL11.glTranslated(x+0.5f, y+0.5f, z+0.5f);
 //		GL11.glRotatef(rotation, 0, 0, 1);
 	}
 	
 	private void loadAndRenderGear(int spikes) {
 		float radiusInnerCircle = spikes*spikeRatio;
 		float radiusOuterCircle = radiusInnerCircle+spikeSize;
 		
 		int displayList = GL11.glGenLists(1);
 		if(displayList > 0) {
 			GL11.glNewList(displayList, GL11.GL_COMPILE_AND_EXECUTE);
 		}
 		GL11.glDepthMask(true);
         GL11.glDisable(GL11.GL_LIGHTING);
         GL11.glDisable(GL11.GL_CULL_FACE);
         
         GL11.glPushMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		bindTextureByName(RealPhysics.modelGear);
 		GL11.glColor3f(0.82f, 0.41f, 0.12f);
 		GL11.glColor3f(1, 1, 1);
 		drawGear(radiusInnerCircle, radiusOuterCircle, spikes);
 		drawSides(radiusInnerCircle, radiusOuterCircle, spikes);
 		GL11.glTranslatef(0, 0, gearThickness);
 		drawGear(radiusInnerCircle, radiusOuterCircle, spikes);
 		GL11.glPopMatrix();
         
 		GL11.glEnable(GL11.GL_CULL_FACE);
         GL11.glEnable(GL11.GL_LIGHTING);
         GL11.glDepthMask(true);
         if(displayList > 0) {
         	GL11.glEndList();
        	loadedGears.put(spikes, displayList);
         }
 	}
 	
 	private void drawGearFromDisplayList(int displayList) {
 		GL11.glCallList(displayList);
 	}
 	
 	private void drawGear(float radiusInnerCircle, float radiusOuterCircle, int spikes) {
 		int points = spikes*2;
 		float angle = 360f/points;
 		Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawing(GL11.GL_TRIANGLES);
 		double[][] maths = new double[points/2][4];
 		for(int i = 0; i < points; i++) {
 			double sin1, cos1, sin2, cos2;
 			sin1 = MathHelper.sin(i*angle);
 			cos1 = MathHelper.cos(i*angle);
 			sin2 = MathHelper.sin((i+1)*angle);
 			cos2 = MathHelper.cos((i+1)*angle);
 			
 			tessellator.addVertexWithUV(sin1*radiusInnerCircle, cos1*radiusInnerCircle, 0, 0.5f-sin1, 0.5f-cos1);
 			tessellator.addVertexWithUV(sin2*radiusInnerCircle, cos2*radiusInnerCircle, 0, 0.5f-sin2, 0.5f-cos2);
 			tessellator.addVertexWithUV(0, 0, 0, 0, 0);
 			if(i%2 == 0) {
 				int num = i/2;
 				maths[num][0] = sin1;
 				maths[num][1] = cos1;
 				maths[num][2] = sin2;
 				maths[num][3] = cos2;
 			}
 		}
 		tessellator.draw();
 		tessellator.startDrawingQuads();
 		for(int i = 0; i < points; i+=2) {
 			double[] cMaths = maths[i/2];
 			tessellator.addVertexWithUV(cMaths[0]*radiusOuterCircle, cMaths[1]*radiusOuterCircle, 0, 0.5f-cMaths[0], 0.5f-cMaths[1]);
 			tessellator.addVertexWithUV(cMaths[2]*radiusOuterCircle, cMaths[3]*radiusOuterCircle, 0, 0.5f-cMaths[2], 0.5f-cMaths[3]);
 			tessellator.addVertexWithUV(cMaths[2]*radiusInnerCircle, cMaths[3]*radiusInnerCircle, 0, 0.5f-cMaths[2], 0.5f-cMaths[3]);
 			tessellator.addVertexWithUV(cMaths[0]*radiusInnerCircle, cMaths[1]*radiusInnerCircle, 0, 0.5f-cMaths[0], 0.5f-cMaths[1]);
 		}
 		tessellator.draw();
 	}
 	
 	private void drawSides(float radiusInnerCircle, float radiusOuterCircle, int spikes) {
 		int points = spikes*2;
 		float angle = 360f/points;
 		Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawingQuads();
 		for(int i = 0; i < points; i++) {
 			double sin1, cos1, sin2, cos2;
 			sin1 = MathHelper.sin(i*angle);
 			cos1 = MathHelper.cos(i*angle);
 			sin2 = MathHelper.sin((i+1)*angle);
 			cos2 = MathHelper.cos((i+1)*angle);
 			
 			float radius;
 			if(i%2 == 0) {
 				radius = radiusOuterCircle;
 			} else {
 				radius = radiusInnerCircle;
 			}
 			tessellator.addVertex(sin1*radius, cos1*radius, 0);
 			tessellator.addVertex(sin1*radius, cos1*radius, gearThickness);
 			tessellator.addVertex(sin2*radius, cos2*radius, gearThickness);
 			tessellator.addVertex(sin2*radius, cos2*radius, 0);
 			tessellator.addVertex(sin1*radiusInnerCircle, cos1*radiusInnerCircle, 0);
 			tessellator.addVertex(sin1*radiusInnerCircle, cos1*radiusInnerCircle, gearThickness);
 			tessellator.addVertex(sin1*radiusOuterCircle, cos1*radiusOuterCircle, gearThickness);
 			tessellator.addVertex(sin1*radiusOuterCircle, cos1*radiusOuterCircle, 0);
 		}
 		tessellator.draw();
 	}
 
 	@Override
 	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
 		return type == ItemRenderType.INVENTORY || type == ItemRenderType.EQUIPPED || type == ItemRenderType.ENTITY;
 	}
 
 	@Override
 	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
 			ItemRendererHelper helper) {
 		return helper == ItemRendererHelper.INVENTORY_BLOCK || helper == ItemRendererHelper.EQUIPPED_BLOCK || helper == ItemRendererHelper.ENTITY_BOBBING;
 	}
 
 	@Override
 	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
 		renderTileEntityAt(null, 0, 0, 0, 0);
 	}
 	
 }
