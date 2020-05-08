 package com.avona.games.towerdefence.android;
 
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import javax.vecmath.Point2d;
 
 import android.opengl.GLU;
 import android.opengl.GLSurfaceView.Renderer;
 
 import com.avona.games.towerdefence.Game;
 import com.avona.games.towerdefence.PortableGraphicsEngine;
 
 /**
  * The GraphicsEngine object currently incorporates all drawing operations. It
  * will iterate over all in-game objects and call (possibly overloaded) class
  * methods to perform the GL calls. It will not touch any in-game state, though.
  */
 public class GraphicsEngine extends PortableGraphicsEngine implements Renderer {
 	private GL10 gl;
 
 	public GraphicsEngine(Game game) {
 		super(game);
 	}
 
 	@Override
 	public void drawCircle(final double x, final double y, final double colR,
 			final double colG, final double colB, final double colA,
 			final double radius, final int segments, final int mode) {
 	}
 
 	@Override
 	public void drawCircle(final double x, final double y, final double colR,
 			final double colG, final double colB, final double colA,
 			final double radius) {
 		drawCircle(x, y, colR, colG, colB, colA, radius, 100, GL10.GL_LINE_LOOP);
 	}
 
 	@Override
 	public void drawFilledCircle(final double x, final double y,
 			final double colR, final double colG, final double colB,
 			final double colA, final double radius) {
 	}
 
 	@Override
 	public void onDrawFrame(GL10 gl) {
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		size = new Point2d(width, height);
 
 		gl.glViewport(0, 0, (int) size.x, (int) size.y);
 		gl.glMatrixMode(GL10.GL_PROJECTION);
 		gl.glLoadIdentity();
 		GLU.gluOrtho2D(gl, 0, width, 0, height);
 		gl.glMatrixMode(GL10.GL_MODELVIEW);
 		gl.glLoadIdentity();
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		this.gl = gl;
 	}
 
 	protected void prepareScreen() {
 		// Paint background, clearing previous drawings.
 		gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 	}
 
 	protected void drawColorVertexArray(final int vertices,
 			final FloatBuffer vertexBuffer, final FloatBuffer colourBuffer) {
 		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
 		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colourBuffer);
 
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
 
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices);
 
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
 	}
 
 	public void drawText(final String text, final double x, final double y, final float colR,
 			final float colG, final float colB, final float colA) {
 	}
 
 	public Point2d getTextBounds(final String text) {
		return null;
 	}
 }
