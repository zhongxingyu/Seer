 package com.relurori.engine.graphics.premade;
 
 import com.relurori.engine.graphics.shapes.Rectangle;
 import com.relurori.engine.graphics.shapes.meta.Collision;
 import com.relurori.engine.graphics.shapes.meta.Intersection;
 
 import android.graphics.Bitmap;
 import android.util.Log;
 
 public class Paddle extends Rectangle {
 
 	private static final String TAG = Paddle.class.getSimpleName();
 	
	private int movementPercentage = 100;
	
	public Paddle(Bitmap bitmap, int maxY, int mvmtPercentage) {
		super(bitmap, 0, maxY);
		movementPercentage = mvmtPercentage;
	}
	
 	public Paddle(Bitmap bitmap, int maxY) {
 		super(bitmap, 0, maxY);
 	}
 
 	public Paddle(Bitmap bitmap) {
 		super(bitmap);
 	}
 	
 	public int getWidth() {
 		// TODO Auto-generated method stub
 		return bitmap.getWidth();
 	}
 	
 	public int getHeight() {
 		return bitmap.getHeight();
 	}
 
 	public boolean ballHit(Ball ball) {
 		return Collision.between(ball,this).getIntersect();
 	}
 
	public int getTotalMovementRange() {
		return movementPercentage;
	}
	
	public int getHalfMovementRange() {
		return movementPercentage/2;
	}
 }
