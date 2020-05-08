 package edu.ncsu.uhp.escape.engine.utilities;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.opengles.GL11;
 
 import edu.ncsu.uhp.escape.engine.raster.IRenderParameters;
 import edu.ncsu.uhp.escape.engine.utilities.math.Point;
 
 public class ColorBox implements IRenderable {
 	private Point dimensions, offsets;
 	private FloatBuffer buffer;
 	private int verticesCount;
 	private float offsetX, offsetY, offsetZ;
 	private float rf, gf, bf, af;
 
 	public ColorBox(int r, int g, int b, int a, Point dimensions, Point offsets) {
 		this.rf = (float) (r / 255.0);
 		this.gf = (float) (g / 255.0);
 		this.bf = (float) (b / 255.0);
 		this.af = (float) (a / 255.0);
 		this.dimensions = dimensions;
		setOffsets(offsets);
 		float dimenX = dimensions.getX();
 		float dimenY = dimensions.getY();
 		float[] quadPoints = { 0.0f, dimenY, 0.0f, 
 							   dimenX, dimenY, 0.0f,
 				               dimenX, 0.0f, 0.0f, 
 				               0.0f, 0.0f, 0.0f ,
 				               0.0f, dimenY, 0.0f};
 		buffer = ByteBuffer.allocateDirect(quadPoints.length * 4)
 				.order(ByteOrder.nativeOrder()).asFloatBuffer();
 		verticesCount = quadPoints.length / 3;
 		buffer.put(quadPoints);
 	}
 
 	public Point getDimensions() {
 		return dimensions;
 	}
 
 	public Point getOffsets() {
 		return offsets;
 	}
 
 	public void drawGL10(GL10 gl) {
 		Profiler.getInstance().startSection("Drawing Color Quad");
 		gl.glPushMatrix();
 		gl.glTranslatef(offsetX, offsetY, offsetZ);
 
 		buffer.position(0);
 		gl.glColor4f(rf, gf, bf, af);
 		gl.glDisable(GL10.GL_BLEND);
 		gl.glDisable(GL10.GL_ALPHA);
 		gl.glDisable(GL10.GL_TEXTURE_2D);
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glLineWidth(3);
 		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, verticesCount);
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glPopMatrix();
 		Profiler.getInstance().endSection();
 	}
 
 	public void drawGL11(GL11 gl) {
 		drawGL10(gl);
 	}
 
 	public void setOffsets(Point newOffsets) {
 		this.offsets = newOffsets;
 		offsetX = newOffsets.getX();
 		offsetY = newOffsets.getY();
 		offsetZ = newOffsets.getZ();
 	}
 
 	public IRenderParameters getRenderParameters() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
