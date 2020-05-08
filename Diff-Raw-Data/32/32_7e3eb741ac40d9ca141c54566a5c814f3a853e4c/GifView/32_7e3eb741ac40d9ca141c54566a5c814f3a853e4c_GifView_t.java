 package com.example.shooter.antistress;
 
 import java.io.InputStream;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.PorterDuff;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.LinearLayout.LayoutParams;
 
 public class GifView extends SurfaceView implements SurfaceHolder.Callback {
 
 	public static final int IMAGE_TYPE_UNKNOWN = 0;
 	public static final int IMAGE_TYPE_STATIC = 1;
 	public static final int IMAGE_TYPE_DYNAMIC = 2;
 
 	public static final int DECODE_STATUS_UNDECODE = 0;
 	public static final int DECODE_STATUS_DECODING = 1;
 	public static final int DECODE_STATUS_DECODED = 2;
 	
 	public static final int FPS = 24;
 	public static final int LOOPS = 1;
 	private int loopedTimes = 0;
 
 	private GifDecoder decoder;
 	private Bitmap bitmap;
 
 	public int imageType = IMAGE_TYPE_UNKNOWN;
 	public int decodeStatus = DECODE_STATUS_UNDECODE;
 
 	private int width;
 	private int height;
 	private float x,y,minX,stepX,totalX, totalTimeGif;
 	long lastStepTimeX, timeStepX, lastStepTimeGif;
 	
 	private int currFrame;
 
 	private int resId;
 
 	public boolean playFlag = false;
 	
 	public GifView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	/**
 	 * Constructor
 	 */
 	public GifView(Context context) {
 		super(context);
 		
 	}
 
 	private InputStream getInputStream() {
 		if (resId > 0)
 			return getContext().getResources().openRawResource(resId);
 		return null;
 	}
 
 	/**
 	 * set gif resource id
 	 * 
 	 * @param resId
 	 */
 	public void setGif(int resId) {
 		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
 		setGif(resId, bitmap);
 	}
 
 	/**
 	 * set gif resource id and cache image
 	 * 
 	 * @param resId
 	 * @param cacheImage
 	 */
 	public void setGif(int resId, Bitmap cacheImage) {
 		this.resId = resId;
 		imageType = IMAGE_TYPE_UNKNOWN;
 		decodeStatus = DECODE_STATUS_UNDECODE;
 		playFlag = false;
 		bitmap = cacheImage;
		width = bitmap.getScaledWidth(bitmap.getDensity());
		height = bitmap.getScaledHeight(bitmap.getDensity());
 		x = this.getWidth();
 		y = this.getHeight();
		minX = (x-width)/2;
 		totalX = x - minX;
 		
 		Log.d("Coordinates obtained", "("+Float.toString(x)+"; "+Float.toString(y)+")");
 		
 //		setLayoutParams(new LayoutParams(width, height));
 	}
 
 	private void decode() {
 		release();
 		currFrame = 0;
 
 		decodeStatus = DECODE_STATUS_DECODING;
 
 		new Thread() {
 			@Override
 			public void run() {
 				decoder = new GifDecoder();
 				decoder.read(getInputStream());
 				if (decoder.width == 0 || decoder.height == 0) {
 					imageType = IMAGE_TYPE_STATIC;
 				} else {
 					imageType = IMAGE_TYPE_DYNAMIC;
 				}
 				totalTimeGif = decoder.getDuration();
				stepX = totalX/((totalTimeGif*LOOPS)/1000);
 				Log.d("Dimensions","totalTimeGif="+Float.toString(totalTimeGif)+"; stepX="+Float.toString(stepX)+"; totalX="+Float.toString(totalX));
 				decodeStatus = DECODE_STATUS_DECODED;
 			}
 		}.start();
 	}
 
 	public void release() {
 		decoder = null;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		if (decodeStatus == DECODE_STATUS_UNDECODE) { 
 			if (playFlag) {
 				decode();
 				lastStepTimeX = lastStepTimeGif = System.currentTimeMillis();
 			}
 		} else if (decodeStatus == DECODE_STATUS_DECODING) {
 			
 		} else if (decodeStatus == DECODE_STATUS_DECODED) {
 			if (imageType == IMAGE_TYPE_STATIC) {
 				canvas.drawBitmap(bitmap, x, y, null);
 			} else if (imageType == IMAGE_TYPE_DYNAMIC) {
 				if (playFlag) {
 					long now = System.currentTimeMillis();
 					
 					if (now - lastStepTimeX >= 1000 / FPS) {
 						updateCoordinates();
 					}
 					
 					if(now-lastStepTimeGif>=decoder.getDelay(currFrame)){
 						lastStepTimeGif = now;
 						incrementFrameIndex();
 						bitmap = decoder.getFrame(currFrame);
 						Log.d("next frame", Integer.toString(currFrame));
 					}
 					
 					if (bitmap != null) {
 						canvas.drawBitmap(bitmap, x, y, null);
 					}
 					
 					if(x<=minX){
 						stop();
 						Log.d("Stopped", "because x("+Float.toString(x)+")<=minX("+Float.toString(minX)+")");
 					}
 				}
 			} else {
 				canvas.drawBitmap(bitmap, x, y, null);
 			}
 		}
 	}
 
 	private void incrementFrameIndex() {
 		currFrame++;
 		if (currFrame >= decoder.getFrameCount()) {
 			loopedTimes++;
 			currFrame = 0;
 			if(loopedTimes>=LOOPS){
 				stop();
 			}
 		}
 	}
 
 	private void decrementFrameIndex() {
 		currFrame--;
 		if (currFrame < 0) {
 			currFrame = decoder.getFrameCount() - 1;
 		}
 	}
 
 	public void play() {
 		playFlag = true;
 		while(playFlag){
 			Canvas c = getHolder().lockCanvas();
 			c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
 			onDraw(c);
 			getHolder().unlockCanvasAndPost(c);
 		}
 	}
 
 	public void pause() {
 		playFlag = false;
 		invalidate();
 	}
 
 	public void stop() {
 		playFlag = false;
 		currFrame = 0;
 		invalidate();
 	}
 
 	public void nextFrame() {
 		if (decodeStatus == DECODE_STATUS_DECODED) {
 			incrementFrameIndex();
 			invalidate();
 		}
 	}
 
 	public void prevFrame() {
 		if (decodeStatus == DECODE_STATUS_DECODED) {
 			decrementFrameIndex();
 			invalidate();
 		}
 	}
 	
 	private void updateCoordinates(){
 		long timeDiff = System.currentTimeMillis()-lastStepTimeX;
 		Log.d("timeDiff", Long.toString(timeDiff));
 		if(timeDiff >= 1000){
 			x -= stepX*(int)(timeDiff/1000);
			Log.d("x dec by sec", Integer.toString((int)(timeDiff/1000)));
 		}
 		if ((timeDiff % 1000)/(1000/FPS) > 0){
			x -= stepX/FPS*((timeDiff % 1000)/(1000/FPS));
			Log.d("x dec by ms", Float.toString((stepX/FPS)*((timeDiff % 1000)/(1000/FPS))));
 		}
 		y = this.getHeight()-2*(-x+this.getWidth());
 		lastStepTimeX = System.currentTimeMillis();
 		Log.d("Coordinates", "("+Float.toString(x)+"; "+Float.toString(y)+")");
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		setWillNotDraw(false);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format,
 			int width, int height) {
 	}
 }
