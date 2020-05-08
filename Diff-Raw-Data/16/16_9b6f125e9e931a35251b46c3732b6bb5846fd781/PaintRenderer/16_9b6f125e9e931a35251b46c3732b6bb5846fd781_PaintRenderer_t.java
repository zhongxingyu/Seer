 package choy.yoon.chul;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
 
 import android.opengl.GLSurfaceView.Renderer;
 import choy.yoon.chul.Shape.DrawableShapeList;
 
 public class PaintRenderer implements Renderer {
 	
 	private int windowWidth_;
 	private int windowHeight_;
 	
 	public PaintRenderer(int windowWidth, int windowHeight) {
 		windowWidth_ = windowWidth;
 		windowHeight_ = windowHeight;
 	}
 	
 	@Override
 	public void onDrawFrame(GL10 gl) {
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);  
 	    DrawableShapeList.getInstance().DrawShapes(gl);
 	    gl.glLoadIdentity();
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		gl.glViewport(0, 0, windowWidth_, windowHeight_);
 		gl.glMatrixMode( GL10.GL_PROJECTION );
 	    gl.glLoadIdentity();
 	    gl.glOrthof( 0.0f, windowWidth_, windowHeight_, 0, -0.1f, 0.1f );
 	    gl.glMatrixMode( GL10.GL_MODELVIEW );
 	    gl.glLoadIdentity();
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		gl.glShadeModel(GL10.GL_SMOOTH);
 		gl.glClearDepthf(1.0f);
 		gl.glEnable(GL10.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
 	}
 
 }
