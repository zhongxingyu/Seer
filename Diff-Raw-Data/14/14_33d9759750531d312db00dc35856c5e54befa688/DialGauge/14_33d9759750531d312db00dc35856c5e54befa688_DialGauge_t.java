 package com.wikispeed.dashboard;
 
 import java.util.StringTokenizer;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapShader;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.LinearGradient;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.RadialGradient;
 import android.graphics.RectF;
 import android.graphics.Shader;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public class DialGauge extends View {
 
 	private static final String TAG = "CanvasView";
 
 	private Handler handler;
 
 	private Bitmap background; // holds the cached static part
 
 	private RectF rimRect;
 
 	private Paint rimPaint;
 
 	private RectF faceRect;
 	private Bitmap faceTexture;
 	private Paint facePaint;
 	private Paint rimShadowPaint;
 	
 	private RectF scaleRect;
 
 	private Paint rimCirclePaint;
 
 	private Paint scalePaint;
 
 	private Paint scaleLabelPaint;
 
 	private Path scaleLabelPath;
 
 	private Paint notchPaint;
 
 	private Paint redlineNotchPaint;
 
 	private Paint redlineMajorNotchPaint;
 
 	private Paint majorNotchPaint;
 
 	private Paint titlePaint;
 
 	private Path titlePath;
 
 	private Paint handPaint;
 	
 	private Path handPath;
 	
 	private Paint handScrewPaint;
 	
 	private Paint backgroundPaint;
 
 	// configurable variables
 
 	private String title = "";
 	
 	private int numberOfNotches;
 
 	private int scaleDivider;
 	
 	private Float scaleEndAngle;
 
 	private Float scaleSweepAngle; // how much to rotate CCW from the
 									// end of the scale e.g. 270 = 6
 									// o'clock position
 
 	private Float angleBetweenNotch;
 
 	private Float notchLength;
 
 	private Float majorNotchLength;
 
 	private Float notchStrokeWidth;
 
 	private Float majorNotchStrokeWidth;
 
 	private int majorNotchInterval;
 
 	private String majorNotchLabels = "";
 	
 	private int redlineNotch;
 
 	private float scaleOffset;
 
 	private float scaleRadius;
 	
 	private float scaleStrokeWidth;
 	
 	private float scaleLabelTextSize;
 
 	private float hOffsetAdjustment;
 	
 	private float tailLength;
 	
	private float titleTextScaleX;
	
 	private boolean drawRim = true;
 	
 	private boolean drawFace = true;
 	
 	// draw the scale line? (notches are always drawn)
 	private boolean drawScale = true;
 	
 	private int redlineColor;
 
 	private int centerDegree = 50;
 	
 	// only handling positive ranges at the moment
 	private int minDegrees = 0;
 	
 	// hand dynamics
 	private boolean handInitialized = false;
 	private float handPosition = centerDegree;
 	private float handTarget = centerDegree;
 	private float handVelocity = 0.0f;
 	private float handAcceleration = 0.0f;
 	private long lastHandMoveTime = -1L;
 	
 	private boolean running;
 	
 	public DialGauge(Context context) {
 		super(context);
 		init();
 	}
 
 	public DialGauge(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initAttributes(context, attrs);
 		init();
 	}
 
 	public DialGauge(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		initAttributes(context, attrs);
 		init();
 	}
 
 	private void initAttributes(Context context, AttributeSet attrs) {
 		TypedArray a = context.obtainStyledAttributes(attrs,
 				R.styleable.DialGauge);
 		title = a.getString(R.styleable.DialGauge_title);
 		numberOfNotches = a.getInteger(R.styleable.DialGauge_numberOfNotches,
 				100);
 		scaleDivider = a.getInteger(R.styleable.DialGauge_scaleDivider,
 				1);
 		scaleRadius = a.getFloat(R.styleable.DialGauge_scaleRadius, 0.37f);
 		scaleStrokeWidth = a.getFloat(R.styleable.DialGauge_scaleStrokeWidth, 0.005f);
 		scaleLabelTextSize = a.getFloat(R.styleable.DialGauge_scaleLabelTextSize, 0.04f);
 		majorNotchInterval = a.getInteger(
 				R.styleable.DialGauge_majorNotchInterval, 10);
 		majorNotchLength = a.getFloat(R.styleable.DialGauge_majorNotchLength,
 				0.03f);
 		majorNotchStrokeWidth = a.getFloat(R.styleable.DialGauge_majorNotchStrokeWidth,
 				0.006f);
 		majorNotchLabels = a.getString(R.styleable.DialGauge_majorNotchLabels);
 		notchLength = a.getFloat(R.styleable.DialGauge_notchLength, 0.02f);
 		notchStrokeWidth = a.getFloat(R.styleable.DialGauge_notchStrokeWidth,
 				0.005f);
 		scaleEndAngle = a.getFloat(R.styleable.DialGauge_scaleEndAngle, 135.0f);
 		scaleOffset = a.getFloat(R.styleable.DialGauge_scaleOffset, 0.10f);
 		scaleSweepAngle = a.getFloat(R.styleable.DialGauge_scaleSweepAngle,
 				270.0f);
 		redlineNotch = a.getInteger(R.styleable.DialGauge_redlineNotch, 0);
 		redlineColor = a.getColor(R.styleable.DialGauge_redlineColor,
 				android.R.color.holo_red_light);
 
 		angleBetweenNotch = scaleSweepAngle / numberOfNotches;
 
 		hOffsetAdjustment = a.getFloat(R.styleable.DialGauge_hOffsetAdjustment, 1.695f);
 		drawRim = a.getBoolean(R.styleable.DialGauge_drawRim, true);
 		drawFace = a.getBoolean(R.styleable.DialGauge_drawFace, true);
 		drawScale = a.getBoolean(R.styleable.DialGauge_drawScale, true);
 		tailLength = a.getFloat(R.styleable.DialGauge_tailLength, 0.2f);
		titleTextScaleX = a.getFloat(R.styleable.DialGauge_titleTextScaleX, 1.0f);
 		a.recycle();
 	}
 
 	@Override
 	protected void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		attachToSensor();
 		// start off at zero
 		setHandTarget(0.0f);
 	}
 
 	@Override
 	protected void onDetachedFromWindow() {
 		detachFromSensor();
 		super.onDetachedFromWindow();
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Parcelable state) {
 		Bundle bundle = (Bundle) state;
 		Parcelable superState = bundle.getParcelable("superState");
 		super.onRestoreInstanceState(superState);
 
 		handInitialized = bundle.getBoolean("handInitialized");
 		handPosition = bundle.getFloat("handPosition");
 		handTarget = bundle.getFloat("handTarget");
 		handVelocity = bundle.getFloat("handVelocity");
 		handAcceleration = bundle.getFloat("handAcceleration");
 		lastHandMoveTime = bundle.getLong("lastHandMoveTime");
 	}
 
 	@Override
 	protected Parcelable onSaveInstanceState() {
 		Parcelable superState = super.onSaveInstanceState();
 
 		Bundle state = new Bundle();
 		state.putParcelable("superState", superState);
 		state.putBoolean("handInitialized", handInitialized);
 		state.putFloat("handPosition", handPosition);
 		state.putFloat("handTarget", handTarget);
 		state.putFloat("handVelocity", handVelocity);
 		state.putFloat("handAcceleration", handAcceleration);
 		state.putLong("lastHandMoveTime", lastHandMoveTime);
 		return state;
 	}
 	
 	private void attachToSensor() {
 		running = false;
 		// simulate a sensor
 		Runnable runnable = new Runnable() {
 		      @Override
 		      public void run() {
 		        while (running) {
 		          final int value = (int) Math.abs(Math.random() * numberOfNotches);
 		          Log.d(TAG, "random value " + value);
 		          try {
 		            Thread.sleep(2000);
 		          } catch (InterruptedException e) {
 		            e.printStackTrace();
 		          }
 		          handler.post(new Runnable() {
 		            @Override
 		            public void run() {
 		              setHandTarget(value);
 		            }
 		          });
 		        }
 		      }
 		    };
 		    new Thread(runnable).start();
 		
 		
 		/*SensorManager sensorManager = getSensorManager();
 
 		List<Sensor> sensors = sensorManager
 				.getSensorList(Sensor.TYPE_GRAVITY);
 		if (sensors.size() > 0) {
 			Sensor sensor = sensors.get(0);
 			sensorManager.registerListener(this, sensor,
 					SensorManager.SENSOR_DELAY_UI, handler);
 		} else {
 			Log.e(TAG, "No sensor found");
 		}*/
 	}
 
 	private void detachFromSensor() {
 		
 		running = false;
 		
 		/*SensorManager sensorManager = getSensorManager();
 		sensorManager.unregisterListener(this);*/
 	}
 	
 	private void init() {
 		handler = new Handler();
 
 		initDrawingTools();
 	}
 
 	private void initDrawingTools() {
 
 		Resources resources = getResources();
 
 		// rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);
 		rimRect = new RectF(0.01f, 0.01f, 0.99f, 0.99f);
 
 		// the linear gradient is a bit skewed for realism
 		rimPaint = new Paint();
 		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 		rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, Color
 				.rgb(0xf0, 0xf5, 0xf0), Color.rgb(0x30, 0x31, 0x30),
 				Shader.TileMode.CLAMP));
 
 		rimCirclePaint = new Paint();
 		rimCirclePaint.setAntiAlias(true);
 		rimCirclePaint.setStyle(Paint.Style.STROKE);
 		rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
 		rimCirclePaint.setStrokeWidth(0.005f);
 
 		float rimSize = 0.02f;
 		faceRect = new RectF();
 		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
 				rimRect.right - rimSize, rimRect.bottom - rimSize);
 
 		faceTexture = BitmapFactory.decodeResource(getContext().getResources(),
 				R.drawable.black_brushed_metal);
 		BitmapShader paperShader = new BitmapShader(faceTexture,
 				Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
 		Matrix paperMatrix = new Matrix();
 		facePaint = new Paint();
 		facePaint.setFilterBitmap(true);
 		paperMatrix.setScale(1.0f / faceTexture.getWidth(),
 				1.0f / faceTexture.getHeight());
 		paperShader.setLocalMatrix(paperMatrix);
 		facePaint.setStyle(Paint.Style.FILL);
 		facePaint.setShader(paperShader);
 
 		rimShadowPaint = new Paint();
 		rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect
 				.width() / 2.0f,
 				new int[] { 0x00000000, 0x00000500, 0x50000500 }, new float[] {
 						0.96f, 0.96f, 0.99f }, Shader.TileMode.MIRROR));
 		rimShadowPaint.setStyle(Paint.Style.FILL);
 
 		scaleRect = new RectF();
 		scaleRect.set(faceRect.left + scaleOffset, faceRect.top + scaleOffset,
 				faceRect.right - scaleOffset, faceRect.bottom - scaleOffset);
 
 		scalePaint = new Paint();
 		scalePaint.setStyle(Paint.Style.STROKE);
 		// TODO get color from preferences
 		scalePaint.setColor(resources
 				.getColor(android.R.color.holo_blue_bright));
 		scalePaint.setStrokeWidth(scaleStrokeWidth);
 		scalePaint.setAntiAlias(true);
 
 		scaleLabelPaint = new Paint();
 		scaleLabelPaint.setStyle(Paint.Style.FILL);
 		// TODO get color from preferences
 		scaleLabelPaint.setColor(resources
 				.getColor(android.R.color.holo_blue_bright));
 		scaleLabelPaint.setAntiAlias(true);
 		// TODO get text size from preferences
 		//scaleLabelPaint.setTextSize(0.040f);
 		scaleLabelPaint.setTextSize(scaleLabelTextSize);
 		// get text size for device density
 		Log.d(TAG, "device density: "
 				+ getContext().getResources().getDisplayMetrics().density);
 		scaleLabelPaint.setTextSize(scaleLabelPaint.getTextSize()
 				* getContext().getResources().getDisplayMetrics().density);
 		// TODO get typeface from preferences
 		scaleLabelPaint.setTypeface(Typeface.SANS_SERIF);
 		// TODO get text scale from preferences
 		scaleLabelPaint.setTextScaleX(0.8f);
 		scaleLabelPaint.setTextAlign(Paint.Align.LEFT);
 
 		scaleLabelPath = new Path();
 		scaleLabelPath.addCircle(0.5f, 0.5f, scaleRadius, Path.Direction.CW);
 		// scaleLabelPath.addArc(new RectF(faceRect.left + scaleOffset,
 		// faceRect.top
 		// + scaleOffset, faceRect.right - scaleOffset,
 		// faceRect.bottom - scaleOffset), 360.0f - scaleSweepAngle +
 		// scaleEndAngle, scaleSweepAngle);
 
 		notchPaint = new Paint();
 		notchPaint.setAntiAlias(true);
 		notchPaint.setStyle(Paint.Style.STROKE);
 		// TODO get notch color from preferences
 		notchPaint.setColor(resources
 				.getColor(android.R.color.holo_blue_bright));
 		// TODO get notch width from preferences
 		//notchPaint.setStrokeWidth(0.005f);
 		notchPaint.setStrokeWidth(notchStrokeWidth);
 
 		majorNotchPaint = new Paint();
 		majorNotchPaint.setAntiAlias(true);
 		majorNotchPaint.setStyle(Paint.Style.STROKE);
 		// TODO get notch color from preferences
 		majorNotchPaint.setColor(resources
 				.getColor(android.R.color.holo_orange_dark));
 		// TODO get notch width from preferences
 		//majorNotchPaint.setStrokeWidth(0.006f);
 		majorNotchPaint.setStrokeWidth(majorNotchStrokeWidth);
 
 		redlineNotchPaint = new Paint(notchPaint);
 		redlineNotchPaint.setColor(resources.getColor(redlineColor));
 		redlineMajorNotchPaint = new Paint(majorNotchPaint);
 		redlineMajorNotchPaint.setColor(resources.getColor(redlineColor));
 
 		titlePaint = new Paint();
 		titlePaint.setColor(resources
 				.getColor(android.R.color.holo_blue_bright));
 		titlePaint.setAntiAlias(true);
		titlePaint.setTypeface(Typeface.DEFAULT);
 		titlePaint.setTextAlign(Paint.Align.CENTER);
 		titlePaint.setTextSize(0.08f);
		//titlePaint.setTextScaleX(1.0f);
		titlePaint.setTextScaleX(titleTextScaleX);
 		// set linear text flag to true otherwise only one character
 		// is rendered on Jelly Bean
		titlePaint.setFlags( titlePaint.getFlags() | Paint.LINEAR_TEXT_FLAG | Paint.DEV_KERN_TEXT_FLAG);
 
 		titlePath = new Path();
 		// use -180.0f for title at bottom of dial
 		//titlePath.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), -180.0f,
 		//		-180.0f);
 		// title at top of dial
 		titlePath.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), 180.0f,
 				180.0f);
 
 		handPaint = new Paint();
 		handPaint.setAntiAlias(true);
 		handPaint.setColor(resources.getColor(R.color.neon_blue));
 		handPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);
 		handPaint.setStyle(Paint.Style.FILL);
 
 		handPath = new Path();
 		//float tailLength = 0.00f;
 		handPath.moveTo(0.5f, 0.5f + tailLength);
 		handPath.lineTo(0.5f - 0.010f, 0.5f + tailLength - 0.007f);
 		//float handLength = 0.32f;
 		float handLength = 0.36f;
 		handPath.lineTo(0.5f - 0.002f, 0.5f - handLength);
 		handPath.lineTo(0.5f + 0.002f, 0.5f - handLength);
 		handPath.lineTo(0.5f + 0.010f, 0.5f + tailLength - 0.007f);
 		handPath.lineTo(0.5f, 0.5f + tailLength);
 		handPath.addCircle(0.5f, 0.5f, 0.025f, Path.Direction.CW);
 
 		handScrewPaint = new Paint();
 		handScrewPaint.setAntiAlias(true);
 		handScrewPaint.setColor(0xff493f3c);
 		handScrewPaint.setStyle(Paint.Style.FILL);
 		
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
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		drawBackground(canvas);
 
 		float scale = (float) getWidth();
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		canvas.scale(scale, scale);
 
 		// drawLogo(canvas);
 		drawHand(canvas);
 
 		canvas.restore();
 
 		if (handNeedsToMove()) {
 			moveHand();
 		}
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
 
 		background = Bitmap.createBitmap(getWidth(), getHeight(),
 				Bitmap.Config.ARGB_8888);
 		Canvas backgroundCanvas = new Canvas(background);
 		float scale = (float) getWidth();
 		backgroundCanvas.scale(scale, scale);
 
 		if (drawRim) {
 			drawRim(backgroundCanvas);
 		}
 		if (drawFace) {
 			drawFace(backgroundCanvas);
 		}
 		drawScale(backgroundCanvas);
 		drawTitle(backgroundCanvas);
 	}
 
 	private void drawHand(Canvas canvas) {
 		if (handInitialized) {
 			float handAngle = degreeToAngle(handPosition);
 			Log.d(TAG, "handAngle = " + handAngle);
 			canvas.save(Canvas.MATRIX_SAVE_FLAG);
 			canvas.rotate(360f - scaleEndAngle + handAngle, 0.5f, 0.5f);
 			canvas.drawPath(handPath, handPaint);
 			canvas.restore();
 
 			canvas.drawCircle(0.5f, 0.5f, 0.01f, handScrewPaint);
 		}
 	}
 	
 	private float degreeToAngle(float degree) {
 		//return (degree - centerDegree) / 2.0f * degreesPerNick;
 		return degree * angleBetweenNotch;
 	}
 	
 	private void drawBackground(Canvas canvas) {
 		if (background == null) {
 			Log.w(TAG, "Background not created");
 		} else {
 			canvas.drawBitmap(background, 0, 0, backgroundPaint);
 		}
 	}
 
 	private void drawRim(Canvas canvas) {
 		// first, draw the metallic body
 		canvas.drawOval(rimRect, rimPaint);
 		// now the outer rim circle
 		canvas.drawOval(rimRect, rimCirclePaint);
 	}
 
 	private void drawScale(Canvas canvas) {
 		Resources resources = getResources();
 		Log.d(TAG,
 				"Canvas size w: " + canvas.getWidth() + ", h: "
 						+ canvas.getHeight());
 
 		// scale circle
 		if (drawScale) {
 			canvas.drawArc(scaleRect, scaleEndAngle, scaleSweepAngle, false,
 					scalePaint);
 			if (redlineNotch > 0) {
 				// overlay a red arc from the redline interval to the end
 				Paint redlineScalePaint = new Paint(scalePaint);
 				redlineScalePaint.setColor(resources.getColor(redlineColor));
 				canvas.drawArc(scaleRect, scaleEndAngle - 90,
 						-angleBetweenNotch * (numberOfNotches - redlineNotch),
 						false, redlineScalePaint);
 			}
 		}
 		String[] labelArray = null;
 		if (majorNotchLabels != null) {
 			StringTokenizer st = new StringTokenizer(majorNotchLabels, ",");
 			labelArray = new String[st.countTokens()];
 			int i = 0;
 			while (st.hasMoreTokens()) {
 				labelArray[i] = st.nextToken();
 				i++;
 			}
 		}
 		
 		float y1 = scaleRect.top;
 		float y2 = y1 - notchLength;
 		float y3 = y1 - majorNotchLength;
 
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		canvas.rotate(360.0f - scaleSweepAngle + scaleEndAngle, 0.5f, 0.5f);
 		Paint np = notchPaint;
 		Paint mp = majorNotchPaint;
 		for (int i = 0; i <= numberOfNotches; i++) {
 			if (i >= redlineNotch && redlineNotch > 0) {
 				np = redlineNotchPaint;
 				mp = redlineMajorNotchPaint;
 			}
 			if (majorNotchInterval > 0 && i % majorNotchInterval == 0) {
 				Log.d(TAG, "drawing major notch " + i);
 				// draw a major notch
 				canvas.drawLine(0.5f, y1, 0.5f, y3, mp);
 				// draw the major label
 				String label = "";
 				if (labelArray != null) {
 					label = labelArray[i / majorNotchInterval];
 				} else {
 					label = String.valueOf(i/scaleDivider);
 				}
 				float measureText = scaleLabelPaint.measureText(label);
 				// float x = measureText/2.0f;
 				float hOffset = (float) (measureText * scaleRadius
 						* (2 * Math.PI) / numberOfNotches);
 				Log.d(TAG, "angleBetweenNotches: " + angleBetweenNotch
 						+ ", hOffset for " + i + " = " + hOffset);
 				// -x makes text center on length to line up with notches
 				// TODO - why does 1.695f work for hOffset? (works for radius
 				// 0.37f)
 				//float hOffsetAdjustment = 1.695f;
 				canvas.drawTextOnPath(label, scaleLabelPath,
 						hOffsetAdjustment - hOffset / 2, -0.05f, scaleLabelPaint);
 			} else {
 				Log.d(TAG, "drawing notch " + i);
 				// draw a notch
 				canvas.drawLine(0.5f, y1, 0.5f, y2, np);
 			}
 			canvas.rotate(angleBetweenNotch, 0.5f, 0.5f);
 		}
 
 		canvas.restore();
 
 	}
 
 	private void drawTitle(Canvas canvas) {
 		if (title != null) {
 			//canvas.drawTextOnPath(title, titlePath, 0.0f, 0.0f, titlePaint);
 			canvas.drawText(title, 0.5f, 0.35f, titlePaint);
 		}
 		
 		// example of drawing horizontal labels in a circle
 		//int n = 10; // Anzahl Werte
 		//float r = 0.28f; // Radius Kreis
 		//for (int i = 0; i < (n); i++) {
 		//	double fi = -2 * Math.PI * i / n;
 		//	double x = r * Math.sin(fi + Math.PI) + 0.5f;
 		//	double y = r * Math.cos(fi + Math.PI) + 0.52f;
 		//	canvas.drawText(i + "", (float) x, (float) y, scaleLabelPaint);
 		//}
 
 	}
 	
 	private void drawFace(Canvas canvas) {
 		canvas.drawOval(faceRect, facePaint);
 		// draw the inner rim circle
 		canvas.drawOval(faceRect, rimCirclePaint);
 		// draw the rim shadow inside the face
 		canvas.drawOval(faceRect, rimShadowPaint);
 	}
 	
 	private boolean handNeedsToMove() {
 		return Math.abs(handPosition - handTarget) > 0.01f;
 	}
 
 	private void moveHand() {
 		if (!handNeedsToMove()) {
 			return;
 		}
 
 		if (lastHandMoveTime != -1L) {
 			long currentTime = System.currentTimeMillis();
 			float delta = (currentTime - lastHandMoveTime) / 1000.0f;
 
 			float direction = Math.signum(handVelocity);
 			if (Math.abs(handVelocity) < 90.0f) {
 				handAcceleration = 5.0f * (handTarget - handPosition);
 			} else {
 				handAcceleration = 0.0f;
 			}
 			handPosition += handVelocity * delta;
 			handVelocity += handAcceleration * delta;
 			if ((handTarget - handPosition) * direction < 0.01f * direction) {
 				handPosition = handTarget;
 				handVelocity = 0.0f;
 				handAcceleration = 0.0f;
 				lastHandMoveTime = -1L;
 			} else {
 				lastHandMoveTime = System.currentTimeMillis();
 			}
 			invalidate();
 		} else {
 			lastHandMoveTime = System.currentTimeMillis();
 			moveHand();
 		}
 	}
 	
 	private void setHandTarget(float temperature) {
 		if (temperature < minDegrees) {
 			temperature = minDegrees;
 		} else if (temperature > numberOfNotches) {
 			temperature = numberOfNotches;
 		}
 		handTarget = temperature;
 		handInitialized = true;
 		invalidate();
 	}
 }
