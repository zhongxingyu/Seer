 package com.relurori.engine.graphics.premade;
 
 import com.relurori.engine.graphics.shapes.Rectangle;
 import com.relurori.engine.graphics.shapes.meta.Collision;
 import com.relurori.engine.graphics.shapes.meta.Intersection;
 
 import android.graphics.Bitmap;
 import android.util.Log;
 
 public class Paddle extends Rectangle {
 
 	private static final String TAG = Paddle.class.getSimpleName();
 	
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
 
 }
