 package com.finlay.geomonsters.battle;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.RectF;
 
 public class Animation {
 
 	public static final int HIDDEN = 0;				// No animation. Hidden.
 	public static final int VISIBLE = 1;			// No animation. Just draw.
 	public static final int ENTER_FRAME = 2;		// Creature enters frame
 	public static final int EXIT_FRAME = 3;			// Creature leaves frame
 	public static final int HURT = 4;				// Creature hurt (flashing)
 	public static final int KILL = 5;				// Creature killed (fast fall)
 	public static final int STRIKE = 6;				// Creature attacks (quick jab)
 
 	public static final int SCREEN_BUBBLE = 100;
 
 	private int _currentAnimation = HIDDEN;
 	private double _startTime = 0;
 	private Bitmap _image;
 	private RectF _destRect;
 
 	public Animation(Bitmap image, RectF destRect) {
 		_image = image;
 		_destRect = destRect;
 	}
 
 	public void start(int animation) {		
 		_currentAnimation = animation;
 		_startTime = System.currentTimeMillis();
 	}
 
 	public void renderCreatureFrame(Canvas c, Paint p) {
 
 		switch (_currentAnimation) {
 		case HIDDEN:
 			// draw nothing
 			break;
 		case VISIBLE:
 			c.drawBitmap(_image, null, _destRect, p);
 			break;
 		case ENTER_FRAME:
 			renderFrame_EnterScreen(c, p);
 			break;
 		case EXIT_FRAME:
 			renderFrame_ExitScreen(c, p);
 			break;
 		case HURT:
 			renderFrame_Hurt(c, p);
 			break;
 		case KILL:
 			renderFrame_Kill(c, p);
 			break;
 		case STRIKE:
 			renderFrame_Strike(c, p);
 			break;
 		}
 	}
 
 	private void renderFrame_EnterScreen(Canvas c, Paint p) {
 		// Creature is out of screen from x-direction. Move back in
 		double time = System.currentTimeMillis() - _startTime;
 		RectF drawRect = new RectF(_destRect);
 
 		float final_time = 1000f;
 
 		float x = (float) (c.getWidth() - ((time/final_time)*c.getWidth()));
		x = (x < 0) ? 0 : x;
 		
 		drawRect.offset(x, 0); // hide out of screen
 
 		c.drawBitmap(_image, null, drawRect, p);
 
 		if (time > final_time)
 			// out of screen, set animation to hidden
 			_currentAnimation = VISIBLE;	
 	}
 	private void renderFrame_ExitScreen(Canvas c, Paint p) {
 
 		double time = System.currentTimeMillis() - _startTime;
 		RectF drawRect = new RectF(_destRect);
 
 		float final_time = 1000f;
 
 		float x = (float) ((time/final_time)*c.getWidth());
 		x = (x > c.getWidth()) ? c.getWidth() : x; // don't move it too far..
 
 		drawRect.offset(x, 0); // hide out of screen
 
 		c.drawBitmap(_image, null, drawRect, p);
 
 		if (time > final_time)
 			// out of screen, set animation to hidden
 			_currentAnimation = HIDDEN;	
 
 
 	}
 	private void renderFrame_Strike(Canvas c, Paint p) {
 
 		float time = (float) (System.currentTimeMillis() - _startTime);
 		RectF drawRect = new RectF(_destRect);
 
 		float dx = .22f*drawRect.width();
 
 		if (time < 300)
 			drawRect.offset(-dx, 0);
 		else if (time < 700)
 			drawRect.offset(-dx + dx*(time-300)/(700-300), 0);
 		else
 			_currentAnimation = VISIBLE;
 
 		c.drawBitmap(_image, null, drawRect, p);
 
 	}
 
 	private void renderFrame_Hurt(Canvas c, Paint p) {
 
 		double time = System.currentTimeMillis() - _startTime;
 
 		if (time > 400) {
 			_currentAnimation = VISIBLE;
 			c.drawBitmap(_image, null, _destRect, p);
 		} else if (time > 300) 
 			; // draw nothing
 		else if (time > 200)
 			c.drawBitmap(_image, null, _destRect, p);
 		else if (time > 100)
 			; // draw nothing
 		else
 			c.drawBitmap(_image, null, _destRect, p);
 
 	}
 
 	private void renderFrame_Kill(Canvas c, Paint p) {
 
 		double time = System.currentTimeMillis() - _startTime;
 		float final_time = 400f;
 		RectF drawRect = new RectF(_destRect);
 
 		float y = (float) ((time/final_time)*c.getHeight());
 		y = (y > c.getHeight()) ? c.getHeight() : y; // don't move it too far..
 
 		drawRect.offset(0, y); // hide below screen
 
 		c.drawBitmap(_image, null, drawRect, p);
 		
 		if (time > final_time)
 			// out of screen, set animation to hidden
 			_currentAnimation = HIDDEN;	
 
 	}
 
 
 }
