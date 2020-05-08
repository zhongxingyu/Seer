 package com.mcode.myworld;
 
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.mcode.mjl.andriod.gles20.matrix.MVPMatrix;
 import com.mcode.mjl.andriod.gles20.matrix.MatrixTransformer;
 
 class MyWorldGLSurfaceView extends GLSurfaceView {
 	MyWorldRenderer renderer;
 	MVPMatrix matrix;
     public MyWorldGLSurfaceView(Context context) {
         super(context);
        super.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
         setEGLContextClientVersion(2);
         renderer = new MyWorldRenderer(context);
         matrix = renderer.getMatrix();
         setRenderer(renderer);
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent e) {
     	Log.e("onTouchEvent", "X: " + e.getX());
     	MatrixTransformer.rotate(matrix.getMMatrix(), e.getX(), 0, 0);
     	//renderer.mAngleY = e.getY();
     	return true;
     }
 }
 
