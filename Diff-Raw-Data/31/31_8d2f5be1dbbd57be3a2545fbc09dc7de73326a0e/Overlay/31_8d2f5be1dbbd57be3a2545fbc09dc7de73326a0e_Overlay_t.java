 /*
  * Overlay draws the laughing man logos
  */
 package com.slsw.fiarworks;
 
 import java.io.FileOutputStream;
 import java.lang.ref.SoftReference;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 import android.os.Environment;
 import android.view.Display;
 import android.view.View;
 
 import com.slsw.fiarworks.firework.*;
 
 
 public class Overlay implements GLSurfaceView.Renderer{
 	public static SynchBitmap preview;
 	private CameraActivity cont;
 	private int frame;
 	private final int FRAMERATE = 60;
 	private Firework mFirework;
     private GLCamera mGLCamera;
     private MatrixStack mStackMV;
     private float[] mProjectionMatrix = new float[16];
 
     private Thread mThread = null; 
     
 	public Overlay(CameraActivity context) {
 		preview=new SynchBitmap(null);
 		frame=0;
 		cont = context;
 	}
 	public void launchFirework(double x, double y, double raidus){
 		mFirework = new Firework();
 	}
 	@Override
     public void onSurfaceCreated(GL10 unused, EGLConfig config) {
         // Set the background frame color
         GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 //        System.out.println("Init glClearColor");
         mStackMV = new MatrixStack();
 //        System.out.println("Init MatrixStack");
         mGLCamera = new GLCamera(8.0f, 2.0f);
 //        System.out.println("Init GLCamera");
         mFirework = new Firework();
 
 
         	
         
     }
 
     @Override
     public void onDrawFrame(GL10 unused) {
         // Draw background color
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
         //Set Projection Matrix
         Matrix.setIdentityM(mProjectionMatrix, 0);
         Matrix.perspectiveM(mProjectionMatrix, 0, 45.0f, 1.0f, 0.01f, 1000.0f);
 
         mFirework.update();
 
         // Draw triangle
         mGLCamera.view(mStackMV);
         mFirework.draw(mStackMV, mProjectionMatrix);
 
     }
 
     @Override
     public void onSurfaceChanged(GL10 unused, int width, int height) {
         // Adjust the viewport based on geometry changes,
         // such as screen rotation
         GLES20.glViewport(0, 0, width, height);
     }
 
 }
