 package edu.ncsu.uhp.escape.engine.utilities;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.opengles.GL11;
 
 import edu.ncsu.uhp.escape.engine.raster.IRenderParameters;
 import edu.ncsu.uhp.escape.engine.utilities.math.Point;
 
 public class MultiRenderable implements IRenderable {
 	private Point offsets;
 	private Point dimensions;
 	private float offsetX, offsetY, offsetZ;
 	private IRenderable[] renderables;
 
 	public MultiRenderable(IRenderable[] renderables, Point offsets) {
 		if (offsets == null)
 			throw new IllegalArgumentException("Offsets cannot be null");
 		setOffsets(offsets);
 	}
 
 	public void drawGL10(GL10 gl) {
 		Profiler.getInstance().startSection("Drawing Image");
 		gl.glPushMatrix();
 		gl.glTranslatef(offsetX, offsetY, offsetZ);
 		for (IRenderable renderable : renderables) {
 			renderable.drawGL10(gl);
 		}
 		gl.glPopMatrix();
 		Profiler.getInstance().endSection();
 	}
 
 	public void drawGL11(GL11 gl) {
 		gl.glPushMatrix();
 		gl.glTranslatef(offsetX, offsetY, offsetZ);
 		for (IRenderable renderable : renderables) {
 			renderable.drawGL11(gl);
 		}
 		gl.glPopMatrix();
 	}
 
 	public Point getDimensions() {
 		if (dimensions == null) {
 			float minX = 0.0f, minY = 0.0f, minZ = 0.0f, maxX = 0.0f, maxY = 0.0f, maxZ = 0.0f;
 			boolean firstTime = true;
 			for (IRenderable renderable : renderables) {
 				Point rendOffsets = renderable.getOffsets();
 				Point rendMax = renderable.getDimensions().add(rendOffsets);
 				if (firstTime) {
 					minX = rendOffsets.getX();
 					minY = rendOffsets.getY();
 					minZ = rendOffsets.getZ();
 					maxX = rendMax.getX();
 					maxY = rendMax.getY();
 					maxZ = rendMax.getZ();
 					firstTime = false;
 				} else {
 					if (rendOffsets.getX() < minX)
 						minX = rendOffsets.getX();
 					if (rendOffsets.getY() < minY)
 						minY = rendOffsets.getY();
 					if (rendOffsets.getZ() < minZ)
 						minZ = rendOffsets.getZ();
 					if (rendMax.getX() > maxX)
 						maxX = rendMax.getX();
 					if (rendMax.getY() > maxY)
 						maxY = rendMax.getY();
 					if (rendMax.getZ() > maxZ)
 						maxZ = rendMax.getZ();
 				}
 			}
 		}
 		return dimensions;
 	}
 
 	public Point getOffsets() {
 		return offsets;
 	}
 
 	public void setOffsets(Point newOffsets) {
 		this.offsets = newOffsets;
 		offsetX = offsets.getX();
 		offsetY = offsets.getY();
 		offsetZ = offsets.getZ();
 	}
 
 	public IRenderParameters getRenderParameters() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
