 package com.lab3129.gltest;
 
 import java.io.IOException;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLSurfaceView.Renderer;
 import android.opengl.GLU;
 import android.opengl.Matrix;
 import android.util.Log;
 
 public class GLRenderer implements Renderer {
 	private Plane plane;
 	private long startTime;
 	private int fps_count;
 	private SListener sl;
 	private static final float centerVec[] = {0.0f, 0.0f, -1.0f, 1.0f};
 	private static final float upperVec[] = {0.0f, 1.0f, 0.0f, 1.0f};
 	public GLRenderer(Context context, SListener slistener) {
 		fps_count = 0;
 		sl = slistener;
 		plane = new Plane(4.0f);
		//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass);
 		try {
 			Bitmap b = BitmapFactory.decodeStream(context.getResources().getAssets().open("compass.png"));
 			Log.i("GLTest","Load Bitmap" + b.getWidth() + " " + b.getHeight());
 			plane.loadBitmap(b);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 	}
 
 	@Override
 	public void onDrawFrame(GL10 gl) {
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();// OpenGL docs.
 		
 		float [] rotationMatrix = sl.computeRotationMatrix();
 		float r[] = new float[16];
 		Matrix.transposeM(r, 0, rotationMatrix, 0);
 		float center[] = new float[4];
 		float upper[] = new float[4];
 		Matrix.multiplyMV(center, 0, r, 0, centerVec, 0);
 		Matrix.multiplyMV(upper, 0, r, 0, upperVec, 0);
 		GLU.gluLookAt(gl, -center[0] * 10, -center[1] * 10, -center[2] * 10, 
 				0, 0, 0, 
 				upper[0], upper[1], upper[2] );
 		
 		plane.draw(gl);
 		fps_count ++;
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		gl.glViewport(0, 0, width, height);// OpenGL docs.
 		// Select the projection matrix
 		gl.glMatrixMode(GL10.GL_PROJECTION);// OpenGL docs.
 		// Reset the projection matrix
 		gl.glLoadIdentity();// OpenGL docs.
 		// Calculate the aspect ratio of the window
 		GLU.gluPerspective(gl, 45.0f,
 		(float) width / (float) height,
 		0.1f, 100.0f);
 		// Select the modelview matrix
 		gl.glMatrixMode(GL10.GL_MODELVIEW);// OpenGL docs.
 		// Reset the modelview matrix
 		gl.glLoadIdentity();// OpenGL docs.
 		GLU.gluLookAt(gl, 0, 0, 6, 0, 0, -1, 0, 1, 0 );
 		this.startTime = System.currentTimeMillis();
 		this.fps_count = 0;
 		
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		// TODO Auto-generated method stub
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);  // OpenGL docs.
 		// Enable Smooth Shading, default not really needed.
 		gl.glShadeModel(GL10.GL_SMOOTH);// OpenGL docs.
 		// Depth buffer setup.
 		gl.glClearDepthf(1.0f);// OpenGL docs.
 		// Enables depth testing.
 		gl.glEnable(GL10.GL_DEPTH_TEST);// OpenGL docs.
 		// The type of depth testing to do.
 		gl.glDepthFunc(GL10.GL_LEQUAL);// OpenGL docs.
 		// Really nice perspective calculations.
 		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, // OpenGL docs.
 		GL10.GL_NICEST);
 		
 
 	}
 	
 	public float getFPS()
 	{
 		long now = System.currentTimeMillis();
 		float diff = (float)((now - this.startTime)/1000.0f);
 		float ret = this.fps_count / diff;
 		this.startTime = now;
 		this.fps_count = 0;
 		return ret;
 		
 	}
 
 }
