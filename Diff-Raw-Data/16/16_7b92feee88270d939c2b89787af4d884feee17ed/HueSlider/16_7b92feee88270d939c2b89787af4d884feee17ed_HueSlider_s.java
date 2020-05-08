 package nl.q42.hue2.views;
 
 import nl.q42.hue2.R;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class HueSlider extends View {
 	private Bitmap background;
 	private Paint paint;
 	
 	private Rect backgroundRect, viewRect, contentRect;
 	private int w, h;
 	
 	private float hue = 180.0f;
 	
 	private SatBriSlider satBri;
 	
 	private boolean userSet = false;
 	
 	public HueSlider(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		// Load hue background
 		background = BitmapFactory.decodeResource(context.getResources(), R.drawable.hue_slider);
 	}
 	
 	public boolean hasUserSet() {
 		return userSet;
 	}
 	
 	@Override
 	public Parcelable onSaveInstanceState() {
 		Bundle bundle = new Bundle();
 		bundle.putParcelable("savedInstanceState", super.onSaveInstanceState());
 		bundle.putFloat("hue", hue);
 		
 		return bundle;
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Parcelable savedInstanceState) {
 		Bundle bundle = (Bundle) savedInstanceState;
 		
 		super.onRestoreInstanceState(bundle.getParcelable("savedInstanceState"));
 		setHue(bundle.getFloat("hue"));
 	}
 	
 	public void setSatBriSlider(SatBriSlider slider) {
 		satBri = slider;
 		satBri.setHue(hue);
 	}
 	
 	public void setHue(float hue) {
 		this.hue = hue;
 		if (satBri != null) {
 			satBri.setHue(hue);
 		}
 	}
 	
 	@Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
         this.w = w;
         this.h = h;
     }
 	
 	@Override
 	public void onDraw(Canvas canvas) {
 		if (paint == null) {
 			paint = new Paint();
 			paint.setARGB(255, 0, 0, 0);
 			paint.setStrokeWidth(4);
 			
 			backgroundRect = new Rect(0, 0, background.getWidth(), background.getHeight());
 			viewRect = new Rect(0, 0, w, h);
 			contentRect = new Rect(2, 2, w - 2, h - 2);
 		}
 		
 		// Draw border
 		canvas.drawRect(viewRect, paint);
 		
 		// Draw colors background
 		canvas.drawBitmap(background, backgroundRect, contentRect, paint);
 		
 		// Draw selector
 		int hueY = h - (int) ((float) h / 360.0f * hue);
 		canvas.drawLine(0, hueY, w, hueY, paint);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getActionMasked()) {
 			case MotionEvent.ACTION_DOWN:
 			case MotionEvent.ACTION_MOVE:
 				hue = 360.0f - Math.max(0.0f, Math.min(event.getY() / viewRect.bottom * 360.0f, 359.0f));
 				userSet = true;
 				if (satBri != null) satBri.setHue(hue);
 				invalidate();
 				break;
 		}
 		
 		return true;
 	}
 }
