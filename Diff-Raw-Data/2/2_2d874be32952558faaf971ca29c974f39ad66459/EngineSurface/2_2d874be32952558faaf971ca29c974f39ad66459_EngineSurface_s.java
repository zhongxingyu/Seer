 package edu.ncsu.uhp.escape.engine;
 
 import java.util.Queue;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.opengles.GL11;
 
 import edu.ncsu.uhp.escape.engine.actor.Actor;
 import edu.ncsu.uhp.escape.engine.utilities.*;
 import edu.ncsu.uhp.escape.engine.utilities.math.Point;
 import android.opengl.*;
 
 /**
  * OpenGL goodness
  * 
  * @author Tyler Dodge
  * 
  */
 public class EngineSurface implements GLSurfaceView.Renderer {
 	private Engine engine;
 	private String version = "";
 
 	private interface IRenderTargetFramework {
 		public GL10 getGl();
 
 		public void render(IRenderable renderable);
 	}
 
 	private class TargetGL10 implements IRenderTargetFramework {
 		private GL10 gl;
 
 		public GL10 getGl() {
 			return gl;
 		}
 
 		public TargetGL10(GL10 gl) {
 			this.gl = gl;
 		}
 
 		public void render(IRenderable renderable) {
 			renderable.drawGL10(gl);
 		}
 	}
 
 	private class TargetGL11 implements IRenderTargetFramework {
 		private GL11 gl;
 
 		public GL11 getGl() {
 			return gl;
 		}
 
 		public TargetGL11(GL11 gl) {
 			this.gl = gl;
 		}
 
 		public void render(IRenderable renderable) {
 			renderable.drawGL11(gl);
 		}
 	}
 
 	public void setEngine(Engine engine) {
 		this.engine = engine;
 
 	}
 
 	public void onDrawFrame(GL10 gl) {
 		Profiler.getInstance().incrementFrame();
 		Profiler.getInstance().startSection("Draw frame");
 		if (version == "")
 			version = gl.glGetString(GL10.GL_VERSION);
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		if (engine != null) {
 			gl.glLoadIdentity();
 			Actor<?> followActor = engine.getFollowActor();
 			if (followActor != null) {
 				Point followPosition = followActor.getPosition();
 				float offsetX, offsetY, offsetZ;
 				offsetX = -followPosition.getX();
 				offsetY = -followPosition.getY();
 				offsetZ = -followPosition.getZ();
 				gl.glTranslatef(offsetX, offsetY, offsetZ);
 			}
 			gl.glTranslatef(0, 0, -50);
 			Queue<RenderableData> renderables = engine.getRenderables(gl);
 
 			if (version.equals("OpenGL ES-CM 1.1"))
 				renderList(new TargetGL11((GL11) gl), renderables);
 			else
 				renderList(new TargetGL10(gl), renderables);
 
 		}
 		Profiler.getInstance().endSection();
 
 	}
 
 	private void renderList(IRenderTargetFramework framework,
 			Queue<RenderableData> renderables) {
 		Profiler.getInstance().startSection("Render list");
 		while (!renderables.isEmpty()) {
 			RenderableData data = renderables.remove();
 			framework.getGl().glPushMatrix();
 			Point position = data.getPosition();
 			framework.getGl().glTranslatef(position.getX(), position.getY(),
 					position.getZ());
 			framework.getGl().glMultMatrixf(data.getRotation().toGlMatrix(), 0);
 			framework.render(data.getRenderable());
 			framework.getGl().glPopMatrix();
 		}
 		Profiler.getInstance().endSection();
 	}
 
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		gl.glViewport(0, 0, width, height);
 		gl.glMatrixMode(GL10.GL_PROJECTION);
 		GLU.gluPerspective(gl, 45.0f, (float) width / height, 0.1f, 100.0f);
		gl.glTranslatef(1, 0, 0);
 		gl.glMatrixMode(GL10.GL_MODELVIEW);
 	}
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		// TODO Auto-generated method stub
 		gl.glShadeModel(GL10.GL_SMOOTH);
 		gl.glClearColor(0, 1, 0, 1);
 		gl.glClearDepthf(1.0f);
 		gl.glMatrixMode(GL10.GL_MODELVIEW);
 		gl.glEnable(GL10.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL10.GL_LEQUAL);
 		gl.glFrontFace(GL10.GL_CW);
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
 	}
 
 }
