 package nl.q42.hue2.views;
 
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class TempSlider extends View {
 	private Bitmap background;
 	private Paint paint;
 	
 	private Rect backgroundRect, viewRect, contentRect;
 	private int w, h;
 	
 	private float temp = 326.0f;
 	
 	private HueSlider hueSlider;
 	private SatBriSlider satBriSlider;
 	
 	private boolean userSet = false;
 	
 	public TempSlider(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		// Load color temperature foreground
 		background = BitmapFactory.decodeResource(context.getResources(), R.drawable.color_temperature_strip);
 	}
 	
 	public boolean hasUserSet() {
 		return userSet;
 	}
 	
 	public void setTemp(float temp) {
 		this.temp = temp;
 		
 		if (hueSlider != null && satBriSlider != null) {
 			int col = Util.temperatureToColor(1000000 / (long) temp);
 			float[] hsv = new float[3];
 			Color.colorToHSV(col, hsv);
 			
 			hueSlider.setHue(hsv[0]);
 			satBriSlider.setSaturation(hsv[1]);
 			satBriSlider.setBrightness(hsv[2]);
 		}
 	}
 	
 	public float getTemp() {
 		return temp;
 	}
 	
 	@Override
 	public Parcelable onSaveInstanceState() {
 		Bundle bundle = new Bundle();
 		bundle.putParcelable("savedInstanceState", super.onSaveInstanceState());
 		bundle.putFloat("temp", temp);
 		
 		return bundle;
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Parcelable savedInstanceState) {
 		Bundle bundle = (Bundle) savedInstanceState;
 		
 		super.onRestoreInstanceState(bundle.getParcelable("savedInstanceState"));
 		temp = bundle.getFloat("temp");
 	}
 	
 	public void setSliders(HueSlider slider, SatBriSlider slider2) {
 		hueSlider = slider;
 		satBriSlider = slider2;
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
 		int tempX = (int) ((float) w / 347.0f * (temp - 153.0f));
 		canvas.drawLine(tempX, 0, tempX, h, paint);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getActionMasked()) {
 			case MotionEvent.ACTION_DOWN:
 			case MotionEvent.ACTION_MOVE:
 				temp = Math.max(153.0f, Math.min(event.getX() / viewRect.right * 347.0f + 153.0f, 499.0f));
 				
 				if (hueSlider != null) {
 					int col = Util.temperatureToColor(1000000 / (long) temp);
 					float[] hsv = new float[3];
 					Color.colorToHSV(col, hsv);
 					
 					hueSlider.setHue(hsv[0]);
 					satBriSlider.setSaturation(hsv[1]);
 					satBriSlider.setBrightness(hsv[2]);
 				}
 				
 				userSet = true;
 				invalidate();
 				break;
 		}
 		
 		return true;
 	}
 }
