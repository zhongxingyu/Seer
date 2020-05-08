 package com.hifreshday.android.test.sprite;
 
import android.util.Log;
 import android.view.MotionEvent;
 
 import com.hifreshday.android.pge.entity.shape.sprite.Sprite;
 import com.hifreshday.android.pge.view.res.IBitmapRes;
 
 public class TestSprite extends Sprite {
 
 	public TestSprite(IBitmapRes bitmapRes, int pX, int pY, int width,
 			int height) {
 		super(bitmapRes, pX, pY, width, height);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(!contains((int)event.getX(), (int)event.getY())){
 			return false;
 		}
 		
 		if(event.getAction() == MotionEvent.ACTION_DOWN) {
 			setPosition(getX() + 10, getY() + 10);
 		}
 		return true;
 	}
 	
 	private boolean contains(int px, int py) {
 		
 		return true;
 	}
 }
