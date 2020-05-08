 package com.portman.panel;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public final class Variometer extends View {
 
 	private static final String TAG = Variometer.class.getSimpleName();
 
 	// drawing tools
 	private RectF rimRect;
 	private Paint rimPaint;
 	private Paint rimCirclePaint;
 	
 	private RectF faceRect;
 	private Paint facePaint;
 	
 	private Paint scalePaint;	
 	private RectF scaleRect, scaleRect2;
 	
 	private Paint titlePaint;	
 	
 	private Paint handPaint;
 	
 	private Paint backgroundPaint; 
 	// end drawing tools
 	
 	private Bitmap background; // holds the cached static part
 	
 	// scale configuration
 	private static final int totalNicks1 = 10;
 	private static final float degreesPerNick1 = 125.0f / totalNicks1;	
 	private static final float minValue1 = -6.0f;
 	private static final float maxValue1 = -1.0f;
 	
 	private static final int totalNicks2 = 20;
 	private static final float degreesPerNick2 = 90.0f / totalNicks2;	
 	private static final float minValue2 = -1.0f;
 	private static final float maxValue2 = 1.0f;
 	
 	private static final int totalNicks3 = 10;
 	private static final float degreesPerNick3 = 125.0f / totalNicks3;	
 	private static final float minValue3 = 1.0f;
 	private static final float maxValue3 = 6.0f;
 	
 	// hand dynamics
 	private float handPosition = 0f;
 	
 	public Variometer(Context context) {
 		super(context);
 		init();
 	}
 
 	public Variometer(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public Variometer(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Parcelable state) {
 		Bundle bundle = (Bundle) state;
 		Parcelable superState = bundle.getParcelable("superState");
 		super.onRestoreInstanceState(superState);
 		
 		handPosition = bundle.getFloat("handPosition");
 	}
 
 	@Override
 	protected Parcelable onSaveInstanceState() {
 		Parcelable superState = super.onSaveInstanceState();
 		
 		Bundle state = new Bundle();
 		state.putParcelable("superState", superState);
 		state.putFloat("handPosition", handPosition);
 		return state;
 	}
 
 	private void init() {
 		initDrawingTools();
 	}
 
 	private String getTitle() {
 		return "CLIMB";
 	}
 
 	private void initDrawingTools() {
 		rimRect = new RectF(1f, 1f, 99f, 99f);
 
 		rimPaint = new Paint();
 		rimPaint.setAntiAlias(true);
 		rimPaint.setColor(Color.BLACK);
 
 		rimCirclePaint = new Paint();
 		rimCirclePaint.setAntiAlias(true);
 		rimCirclePaint.setStyle(Paint.Style.STROKE);
 		rimCirclePaint.setColor(Color.GRAY);
 		rimCirclePaint.setStrokeWidth(0.5f);
 
 		float rimSize = 2f;
 		faceRect = new RectF();
 		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
 			     rimRect.right - rimSize, rimRect.bottom - rimSize);
 		
 		facePaint = new Paint();
 		facePaint.setStyle(Paint.Style.FILL);
 		facePaint.setColor(Color.BLACK);
 
 		scalePaint = new Paint();
 		scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		scalePaint.setColor(Color.WHITE);
 		scalePaint.setStrokeWidth(0.5f);
 		scalePaint.setAntiAlias(true);
 		
 		scalePaint.setTextSize(10f);
 		scalePaint.setTypeface(Typeface.SANS_SERIF);
 		scalePaint.setTextAlign(Paint.Align.CENTER);
 		
 		float scalePosition = 3f;
 		scaleRect = new RectF();
 		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
 					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);
 		float scalePosition2 = 6f;
 		scaleRect2 = new RectF();
 		scaleRect2.set(faceRect.left + scalePosition2, faceRect.top + scalePosition2,
 				  faceRect.right - scalePosition2, faceRect.bottom - scalePosition2);
 
 		titlePaint = new Paint();
 		titlePaint.setColor(Color.WHITE);
 		titlePaint.setAntiAlias(true);
 		titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
 		titlePaint.setTextAlign(Paint.Align.CENTER);
 		titlePaint.setTextSize(8f);
 
 		handPaint = new Paint();
 		handPaint.setAntiAlias(true);
 		handPaint.setColor(Color.WHITE);
 		handPaint.setStrokeWidth(2f);
 		handPaint.setStyle(Paint.Style.FILL_AND_STROKE);	
 		
 		backgroundPaint = new Paint();
 		backgroundPaint.setFilterBitmap(true);
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
 		Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
 		
 		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
 		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
 		
 		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
 		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
 		
 		int chosenWidth = chooseDimension(widthMode, widthSize);
 		int chosenHeight = chooseDimension(heightMode, heightSize);
 		
 		int chosenDimension = Math.min(chosenWidth, chosenHeight);
 		
 		setMeasuredDimension(chosenDimension, chosenDimension);
 	}
 	
 	private int chooseDimension(int mode, int size) {
 		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
 			return size;
 		} else { // (mode == MeasureSpec.UNSPECIFIED)
 			return getPreferredSize();
 		} 
 	}
 	
 	// in case there is no size specified
 	private int getPreferredSize() {
 		return 300;
 	}
 
 	private void drawRim(Canvas canvas) {
 		// first, draw the metallic body
 		canvas.drawOval(rimRect, rimPaint);
 		// now the outer rim circle
 		canvas.drawOval(rimRect, rimCirclePaint);
 	}
 	
 	private void drawFace(Canvas canvas) {		
 		canvas.drawOval(faceRect, facePaint);
 		// draw the inner rim circle
 		canvas.drawOval(faceRect, rimCirclePaint);
 	}
 
 	private void drawScale(Canvas canvas) {
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		scalePaint.setTextSize(6f);
 		canvas.drawText("UP", 30f, 45f, scalePaint);
 		canvas.drawText("DOWN", 35f, 60f, scalePaint);
 		scalePaint.setTextSize(10f);
 		
 		canvas.drawArc(scaleRect2, -10f, 20f, false, scalePaint);
 		
 		canvas.rotate(100f, 50f, 50f);
 
 		for (int i = 0; i < totalNicks1; ++i) {
 			float y1 = scaleRect.top;
 			float y2 = y1 + 3f;
 			
 			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
 			
 			if (i % 2 == 0) { // every 2
 				canvas.drawLine(50f, y1, 50f, y2 + 2f, scalePaint);
 				
 				float value = nickToValue1(i);
 				String valueString = Integer.toString(Math.abs((int)value));
 				
 				// draw vertical text
 				canvas.save(Canvas.MATRIX_SAVE_FLAG);
 				canvas.rotate(- degreesPerNick1 * i - 100, 50f, y2 + 8f);
 				if (!valueString.contentEquals("6"))
 					canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
 				else
 					canvas.drawText(valueString, 50f, y2 + 5f, scalePaint);
 				canvas.restore();
 			}
 			
 			canvas.rotate(degreesPerNick1, 50f, 50f);
 		}
 		
 		for (int i = 0; i < totalNicks2; ++i) {
 			float y1 = scaleRect.top;
 			float y2 = y1 + 3f;
 			
 			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
 			
 			if (i % 5 == 0) { // every 5
 				canvas.drawLine(50f, y1, 50f, y2 + 2f, scalePaint);
 				
 				float value = nickToValue2(i);
 				String valueString = Integer.toString(Math.abs((int)value));
 				if (valueString.contentEquals("5"))
 					scalePaint.setTextSize(6f);
 				
 				// draw vertical text
 				canvas.save(Canvas.MATRIX_SAVE_FLAG);
 				canvas.rotate(- degreesPerNick2 * i - 225, 50f, y2 + 8f);
 				canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
 				scalePaint.setTextSize(10f);
 				canvas.restore();
 			}
 			
 			canvas.rotate(degreesPerNick2, 50f, 50f);
 		}
 		
 		for (int i = 0; i <= totalNicks3; ++i) {
 			float y1 = scaleRect.top;
 			float y2 = y1 + 3f;
 			
 			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
 			
 			if (i % 2 == 0) { // every 2
 				canvas.drawLine(50f, y1, 50f, y2 + 2f, scalePaint);
 				
 				float value = nickToValue3(i);
 				String valueString = Integer.toString(Math.abs((int)value));
 				
 				// draw vertical text
 				canvas.save(Canvas.MATRIX_SAVE_FLAG);
 				canvas.rotate(- degreesPerNick3 * i - 315, 50f, y2 + 8f);
 				if (!valueString.contentEquals("6"))
 					canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
 				canvas.restore();
 			}
 			
 			canvas.rotate(degreesPerNick3, 50f, 50f);
 		}
 		
 		canvas.restore();		
 	}
 	
 	private float nickToValue1(int nick) {
 		float shiftedValue =  minValue1 + nick * (maxValue1 - minValue1) / totalNicks1;
 		return shiftedValue;
 	}
 	
 	private float nickToValue2(int nick) {
 		float shiftedValue =  minValue2 + nick * (maxValue2 - minValue2) / totalNicks2;
 		if (Math.abs(shiftedValue) < 1f)
 			shiftedValue = shiftedValue * 10f;
 		return shiftedValue;
 	}
 	
 	private float nickToValue3(int nick) {
 		float shiftedValue =  minValue3 + nick * (maxValue3 - minValue3) / totalNicks3;
 		return shiftedValue;
 	}
 	
 	private float valueToAngle(float value) {
 		float angle = 0f;
 		if (value < maxValue1) {
 			float valuePerNick = (float)(maxValue1 - minValue1) / totalNicks1;
 			angle =  degreesPerNick1 * (value - minValue1) / valuePerNick + 100f;
 		} else if (value < maxValue2) {
 			float valuePerNick = (float)(maxValue2 - minValue2) / totalNicks2;
 			angle =  degreesPerNick2 * (value - minValue2) / valuePerNick + 125f + 100f;
 		} else {
 			float valuePerNick = (float)(maxValue3 - minValue3) / totalNicks3;
 			angle =  degreesPerNick3 * (value - minValue3) / valuePerNick + 215f + 100f;
 		}
 		
 		return angle;
 	}
 	
 	private void drawTitle(Canvas canvas) {
 		String title = getTitle();
 		canvas.drawText(title, 50f, 40f, titlePaint);
 	}
 	
 
 	private void drawHand(Canvas canvas) {
 		float handAngle = valueToAngle(handPosition);
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		canvas.rotate(handAngle, 50f, 50f);
 		canvas.drawLine(50f, 50f, 50f, 10f, handPaint);
 		canvas.restore();
 	}
 
 	private void drawBackground(Canvas canvas) {
 		if (background == null) {
 			Log.w(TAG, "Background not created");
 		} else {
 			canvas.drawBitmap(background, 0, 0, backgroundPaint);
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		drawBackground(canvas);
 
 		float scale = (float) getWidth();		
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		canvas.scale(scale / 100f, scale / 100f);
 
 		drawHand(canvas);
 		
 		canvas.restore();
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		Log.d(TAG, "Size changed to " + w + "x" + h);
 		
 		regenerateBackground();
 	}
 	
 	private void regenerateBackground() {
 		// free the old bitmap
 		if (background != null) {
 			background.recycle();
 		}
 		
 		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
 		Canvas backgroundCanvas = new Canvas(background);
 		float scale = (float) getWidth();		
 		backgroundCanvas.scale(scale / 100f, scale / 100f);
 		
 		drawRim(backgroundCanvas);
 		drawFace(backgroundCanvas);
 		drawScale(backgroundCanvas);
 		drawTitle(backgroundCanvas);		
 	}
 
		
 	public void setVariometer(float value) {
		if (value < minValue2) {
			value = minValue2;
		} else if (value > maxValue2) {
			value = maxValue2;
 		}
 		handPosition = value;
 		invalidate();
 	}
 }
