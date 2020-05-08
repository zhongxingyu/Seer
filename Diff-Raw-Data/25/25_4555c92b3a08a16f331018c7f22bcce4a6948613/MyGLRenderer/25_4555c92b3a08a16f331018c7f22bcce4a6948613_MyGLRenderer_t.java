 package com.awevation.spuzzle;
 
 import android.opengl.GLSurfaceView;
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.egl.EGLConfig;
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 import android.os.SystemClock;
 import java.lang.System;
 import android.util.Log;
 import android.content.Context;
 
 public class MyGLRenderer implements GLSurfaceView.Renderer {
     public static final String TAG = "MyGLRenderer";
 
     private MatrixStack matrixStack = new MatrixStack();
     private World world;
    private Context context;
     private float[] mVMatrix = new float[16];
     private float[] mMVPMatrix = new float[16];
     private float[] mProjMatrix = new float[16];
     private float[] mMMatrix= new float[16];
     private float[] mMVMatrix = new float[16];
     private float[] mRotationMatrix = new float[16];
     private float[] mvMatrix = new float[16];
 
     private volatile double dt;
     private volatile double endTime; //for working out the dt
 
 	public MyGLRenderer(Context context) {
 	this.context = context;
 	}
 
     public void sendWorld(World targetworld) {
 	//receive the target world
 	world = targetworld;
     }
     public void onSurfaceCreated(GL10 unused, EGLConfig config) {
         // Set the background frame color
        GLES20.glClearColor(0.5f, 0.1f, 0.4f, 1.0f);
 	GLES20.glDisable(GLES20.GL_DEPTH_TEST);
 	GLES20.glDepthMask(false);
 	
 	GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 	GLES20.glEnable(GLES20.GL_BLEND);
 
 	world.init();
     }
 
     public void onDrawFrame(GL10 unused) {
 	if(world != null) {
 	    dt = System.currentTimeMillis() - endTime;
 
 	    world.update( (float) dt);
 
 	    // Redraw background color
 	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
 	    Matrix.setIdentityM(mvMatrix, 0);
 	    Matrix.translateM(mvMatrix, 0, 0f, 0f, 0f);
 
     	    //Matrix.setIdentityM(mMMatrix, 0);
     	    //Matrix.translateM(mMMatrix, 0, world.player.xPos, world.player.yPos, -3.f);
 	    //matrixStack.translate(world.player.xPos, world.player.yPos, -3.f);
 	    //matrixStack.translate(0.f, 100.f, 0.f);
     	    //Matrix.multiplyMM(mMVMatrix, 0, mMMatrix, 0, mVMatrix, 0);
     	    //Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);
 	
     	    //Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1);
 	    //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
     	   
 	    world.draw(mvMatrix, mProjMatrix);
 
     	    endTime = System.currentTimeMillis();
 	} else {
 	    Log.d("Quad", "There is no world....");
 	}
     }
 
     public void onSurfaceChanged(GL10 unused, int width, int height) {
         GLES20.glViewport(0, 0, width, height);
 
 	float ratio = (float) width / height;
 
 	//Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 4);
 	
 	Matrix.orthoM(mProjMatrix, 0, 0, width / 2, 0, height / 2, -1.f, 1.f);
     }
 
     public static int loadShader(int type, String shaderCode){
 
 	int shader = GLES20.glCreateShader(type);
 
 	GLES20.glShaderSource(shader, shaderCode);
 	GLES20.glCompileShader(shader);
 	
 	return shader; 
     }
 }
