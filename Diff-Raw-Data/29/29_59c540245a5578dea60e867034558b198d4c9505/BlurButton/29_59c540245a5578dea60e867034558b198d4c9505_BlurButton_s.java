 package net.fhtagn.zoobgame.menus;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BlurMaskFilter;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.graphics.Paint.Align;
 import android.text.TextPaint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.Button;
 
 /**
  * A button whose visual simply consist of text. The text is blurred when the button is pressed
  */
 public class BlurButton extends Button {
 	static final String TAG = "BlurButton";
 	
 	static private Typeface oogie = null;
 	
 	//FIXME:Why the hell do we have to manage that ourselves ? Looks like isPressed() and isFocused() don't work as expected
 	private boolean pressed = false;
 	private boolean focused = false;
 	
 	public BlurButton(Context context, AttributeSet attrs) {
 	  super(context, attrs);
 	  if (oogie == null) {
 	  	oogie = Typeface.createFromAsset(context.getAssets(), "fonts/OogieBoogie.ttf");
 	  }
 	  this.setTypeface(oogie);
 	  //Have to do that here so the shadow size is taken into account in onMeasure()
 	  this.setShadowLayer(Common.SHADOW_RADIUS, Common.SHADOW_DX, Common.SHADOW_DY, Common.SHADOW_COLOR);
 	  this.setPadding((int)Common.SHADOW_RADIUS, (int)Common.SHADOW_RADIUS, (int)Common.SHADOW_RADIUS, (int)Common.SHADOW_RADIUS);
   }
 	
 	@Override
 	protected void onDraw (Canvas canvas) {
 		if (pressed) {
 			this.getPaint().setShadowLayer(0, 1, 1, Common.SHADOW_COLOR); //remove shadow for blur
 			this.getPaint().setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
 		} else if (focused) {
 			this.getPaint().setShadowLayer(Common.SHADOW_RADIUS, Common.SHADOW_DX, Common.SHADOW_DY, Common.SHADOW_FOCUS_COLOR);
 			this.getPaint().setMaskFilter(null);
 		} else {
 			this.getPaint().setShadowLayer(Common.SHADOW_RADIUS, Common.SHADOW_DX, Common.SHADOW_DY, Common.SHADOW_COLOR);
 			this.getPaint().setMaskFilter(null);
 		}
 		super.onDraw(canvas);
 	}
 	
 	//FIXME: handle selected ?
 	/*@Override
 	public void onFocusChanged (boolean focused, int direction, Rect previousFocus) {
 		pressed = focused;
 	}*/
 	
 	@Override
 	public boolean onTouchEvent (MotionEvent event) {
 		switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 			case MotionEvent.ACTION_MOVE:
 				pressed = true;
 				break;
 			default:
 				pressed = false;
 		}
 		postInvalidate();
 		return super.onTouchEvent(event);
 	}
 	
 	@Override
 	public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
 		this.focused = focused;
 		postInvalidate();
 		super.onFocusChanged(focused, direction, previouslyFocusedRect);
 	}
 	
 	@Override
 	public boolean onTrackballEvent (MotionEvent event) {
 		super.onTrackballEvent(event);
 		postInvalidate();
 		return false;
 	}
 	
 }
