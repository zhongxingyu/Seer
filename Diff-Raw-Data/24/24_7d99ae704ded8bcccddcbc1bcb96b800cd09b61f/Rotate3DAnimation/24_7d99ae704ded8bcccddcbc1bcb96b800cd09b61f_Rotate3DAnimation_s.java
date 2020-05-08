 package com.yugy.qianban.asisClass;
 
 import android.graphics.Camera;
 import android.graphics.Matrix;
 import android.view.animation.Animation;
 import android.view.animation.Transformation;
 
 public class Rotate3DAnimation extends Animation{
 
 	private float fromDegree;
 	private float toDegree;
 	
 	private float centerX;
 	private float centerY;
 	private float depthZ;
 	
 	private boolean reverse;
 	
 	private Camera camera;
 	
 	/**
 	 * 
 	 * @param fromDegree
 	 * @param toDegree
 	 * @param centerX
 	 * @param centerY
 	 * @param depthZ
 	 * @param reverse
 	 */
	public Rotate3DAnimation(float from, float to, float x, float y, float z, boolean r){
 		fromDegree = from;
 		toDegree = to;
 		centerX = x;
 		centerY = y;
 		depthZ = z;
 		reverse = r;
 	}
 	
 	@Override
 	public void initialize(int width, int height, int parentWidth,
 			int parentHeight) {
 		camera = new Camera();
 		super.initialize(width, height, parentWidth, parentHeight);
 	}
 	
 	@Override
 	protected void applyTransformation(float interpolatedTime, Transformation t) {
 		float from = fromDegree;
 		float degree = from + (toDegree - from) * interpolatedTime;
 		float x = centerX;
 		float y = centerY;
 		Camera c = camera;
 		Matrix matrix = t.getMatrix();
 		c.save();
 		if(reverse){
 			c.translate(0f, 0f, depthZ * interpolatedTime);
 		}else{
 			c.translate(0f, 0f, depthZ * (1f - interpolatedTime));
 		}
 		c.rotateY(degree);
 		c.getMatrix(matrix);
 		c.restore();
 		matrix.preTranslate(-x, -y);
 		matrix.postTranslate(x, y);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
