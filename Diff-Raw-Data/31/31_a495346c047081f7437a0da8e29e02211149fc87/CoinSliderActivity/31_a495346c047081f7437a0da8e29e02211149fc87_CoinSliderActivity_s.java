 package dev.poutyface.coinslider;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 import android.content.Context;
 import android.util.Log;
 import android.content.res.Resources;
 import android.media.MediaPlayer;
 
 
 public class CoinSliderActivity extends Activity {
 	private MediaPlayer mMediaPlayer = null;
 	private CoinSlider mCoinSlider = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mMediaPlayer = MediaPlayer.create(this, R.raw.music);
 		mMediaPlayer.setLooping(true);
 

 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		int index;
 		if(savedInstanceState != null){
 			index = savedInstanceState.getInt("mNextImageIndex", 0);
 			mMediaPlayer.seekTo(savedInstanceState.getInt("musicPosition", 0));
 		}
 		else{
 			index = 0;
 		}
		mMediaPlayer.start();
 		mCoinSlider = new CoinSlider(this, index);
 		setContentView(mCoinSlider);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 	}
 	
 	@Override  
 	protected void onSaveInstanceState(Bundle outState) {  
 	  super.onSaveInstanceState(outState);
 	  outState.putInt("mNextImageIndex", mCoinSlider.getNextImageIndex());
 	  outState.putInt("musicPosition", mMediaPlayer.getCurrentPosition());
 	}  
 	  
 	@Override
 	public void onStop(){
 		super.onStop();
 		if(mMediaPlayer != null)
			mMediaPlayer.stop();
 	}
 }
 
 class Field {
 	private Rect mRect;
 	public int mAlpha;
 	private int mOrder;
 	public Paint mPaint = new Paint();
 	private final int mAlphaStep = 11;
 	private int mPosX, mPosY;
 
 	public Field(int posx, int posy, Rect rect) {
 		mRect = rect;
 		mOrder = 0;
 		mAlpha = 0;
 		mPosX = posx;
 		mPosY = posy;
 		mPaint.setAlpha(mAlpha);
 	}
 
 	public void setAlpha(int alpha) {
 		mAlpha = alpha;
 		mPaint.setAlpha(mAlpha);
 	}
 
 	public int getAlpha() {
 		return mAlpha;
 	}
 
 	public void upAlpha() {
 		mAlpha += mAlphaStep;
 		if (mAlpha > 0xff)
 			mAlpha = 0xff;
 		setAlpha(mAlpha);
 	}
 
 	public void setOrder(int order) {
 		mOrder = order;
 	}
 
 	public int getOrder() {
 		return mOrder;
 	}
 
 	public int getPosX() {
 		return mPosX;
 	}
 
 	public int getPosY() {
 		return mPosY;
 	}
 
 	public void draw(Canvas canvas, Bitmap bitmap) {
 		canvas.drawBitmap(bitmap, mRect, mRect, mPaint);
 	}
 }
 
 class Slide {
 	private Bitmap mImage;
 	private View mView;
 	private int mSpW, mSpH;
 	private int mOrder;
 	private ArrayList<Field> mFields = new ArrayList<Field>();
 
 	public enum Effect {
 		NONE, STRAIGHT, RANDOM, RAIN
 	};
 
 	private Effect mEffect;
 
 	public Slide(View view, Bitmap image) {
 		mImage = image;
 		mView = view;
 		mOrder = 0;
 
 		Resources res = mView.getResources();
 		int screenWidth = res.getDisplayMetrics().widthPixels;
 		int screenHeight = res.getDisplayMetrics().heightPixels;
 		if (screenWidth >= screenHeight) {
 			mSpW = 8;
 			mSpH = 6;
 		} else {
 			mSpW = 6;
 			mSpH = 8;
 		}
 
 		makeFields();
 		setEffect(Effect.STRAIGHT);
 	}
 
 	private void makeFields() {
 		int twidth, swidth, theight, sheight;
 		int left, top;
 		int tgapx, gapx, tgapy, gapy;
 		twidth = swidth = mImage.getWidth() / mSpW;
 		theight = sheight = mImage.getHeight() / mSpH;
 		left = top = 0;
 		tgapx = gapx = mImage.getWidth() - mSpW * swidth;
 		tgapy = gapy = mImage.getHeight() - mSpH * sheight;
 
 		for (int i = 0; i < mSpH; ++i) {
 			gapx = tgapx;
 			if (gapy > 0) {
 				gapy--;
 				sheight = theight + 1;
 			} else {
 				sheight = theight;
 			}
 
 			for (int j = 0; j < mSpW; ++j) {
 				if (gapx > 0) {
 					gapx--;
 					swidth = twidth + 1;
 				} else {
 					swidth = twidth;
 				}
 
 				// Log.d("MYAPP", "width:" + swidth + " height:" + sheight
 				// + " left:" + left + " top:" + top + " right:"
 				// + (left + swidth) + " bottom:" + (top + sheight));
 				Field field = new Field(i, j, new Rect(left, top,
 						left + swidth, top + sheight));
 				mFields.add(field);
 				left += swidth;
 			}
 
 			top += sheight;
 			left = 0;
 		}
 	}
 
 	public void setEffect(Effect effect) {
 		mEffect = effect;
 		switch (effect) {
 		case NONE:
 			setNoneEffect();
 			break;
 		case STRAIGHT:
 			setStraightEffect();
 			break;
 		case RANDOM:
 			setRandomEffect();
 			break;
 		case RAIN:
 			setRainEffect();
 			break;
 		}
 		mOrder = 0;
 	}
 
 	public Effect getEffect() {
 		return mEffect;
 	}
 
 	private void setNoneEffect() {
 		for (Field f : mFields) {
 			f.setOrder(0);
 			f.setAlpha(0xFF);
 		}
 	}
 
 	private void setStraightEffect() {
 		int i = 0;
 		for (Field f : mFields) {
 			f.setOrder(i++);
 			f.setAlpha(0);
 		}
 	}
 
 	private void setRandomEffect() {
 		int size = mFields.size();
 		for (Field f : mFields) {
 			f.setOrder((int) (Math.random() * (size + 1)));
 			f.setAlpha(0);
 		}
 	}
 
 	private void setRainEffect() {
 		for (Field f : mFields) {
 			f.setOrder(f.getPosX() + f.getPosY());
 			f.setAlpha(0);
 		}
 	}
 
 	public boolean didFinishedDraw() {
 		boolean finished = true;
 		for (Field f : mFields) {
 			if (f.getAlpha() != 0xFF) {
 				finished = false;
 			}
 		}
 		return finished;
 	}
 
 	public void draw(Canvas canvas) {
 		if (mEffect == Effect.NONE) {
 			canvas.drawBitmap(mImage, 0, 0, null);
 			return;
 		}
 
 		for (Field f : mFields) {
 			if (f.getOrder() <= mOrder) {
 				f.draw(canvas, mImage);
 				f.upAlpha();
 			}
 		}
 		mOrder++;
 	}
 	
 	public void drawWithoutEffect(Canvas canvas){
 		if(mEffect == Effect.NONE){
 			canvas.drawBitmap(mImage, 0, 0, null);
 			return;
 		}
 		
 		for(Field f : mFields){
 			if(f.getOrder() <= mOrder){
 				f.draw(canvas, mImage);
 			}
 		}
 	}
 	
 
 }
 
 class CoinSlider extends View implements OnTouchListener{
 	private Handler mHandler = new Handler();
 	private Slide fourground, background;
 	private File[] mImageFiles;
 	private int mNextImageIndex = 0;
 	private boolean mPlay = true;
 
 	public CoinSlider(Context context, int index) {
 		super(context);
 		
 		setKeepScreenOn(true);
 		setOnTouchListener(this);
 		
 		mNextImageIndex = index;
 		
 		initImageFiles();
 		setBackgroundImage(mImageFiles[mNextImageIndex].getPath());
 		incNextImageIndex();
 		setFourgroundImage(mImageFiles[mNextImageIndex].getPath());
 		incNextImageIndex();
 		
 		Timer timer = new Timer(false);
 		timer.schedule(new TimerTask() {
 			public void run() {
 				mHandler.post(new Runnable() {
 					public void run() {
 						invalidate();
 					}
 				});
 			}
 		}, 0, 20);
 	}
 	
 	private void incNextImageIndex(){
 		mNextImageIndex++;
 		if(mNextImageIndex >= mImageFiles.length){
 			mNextImageIndex = 0;
 		}
 	}
 	
 	public boolean onTouch(View v, MotionEvent event){
 		int action = event.getAction();
 		switch(action){
 		case MotionEvent.ACTION_UP:
 			mPlay = (mPlay == true) ? false : true;
 		}
 		return true;
 	}
 	
 	private void initImageFiles(){
 		String path = Environment.getExternalStorageDirectory()
 				.getAbsolutePath() + "/DCIM/100ANDRO";
 		Log.d("MYAPP", "PATH: " + path);
 		File dir = new File(path);
 		//mImageFiles = dir.listFiles();
 		mImageFiles = dir.listFiles(new FilenameFilter(){
 			public boolean accept(File file, String name){
 				return name.endsWith(".jpg");
 			}
 		});
 	}
 	
 	private void setBackgroundImage(String path){
 		Bitmap bitmap = makeBitmap(path);
 		background = new Slide(this, bitmap);
 		background.setEffect(Slide.Effect.NONE);
 	}
 	
 	private void setFourgroundImage(String path){
 		Bitmap bitmap = makeBitmap(path);
 		fourground = new Slide(this, bitmap);
 	}
 	
 	private void switchImage(){
 		background = fourground;
 		background.setEffect(Slide.Effect.NONE);
 	}
 	
 	public int getNextImageIndex(){
 		return mNextImageIndex;
 	}
 	
 	public void setNextImageIndex(int index){
 		mNextImageIndex = index;
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 
 		background.draw(canvas);
 		if(mPlay)
 			fourground.draw(canvas);
 		else
 			fourground.drawWithoutEffect(canvas);
 		
 		if (fourground.didFinishedDraw()) {
 			Slide.Effect effect = fourground.getEffect();
 			switchImage();
 
 			setFourgroundImage(mImageFiles[mNextImageIndex++].getPath());
 			if (mNextImageIndex >= mImageFiles.length)
 				mNextImageIndex = 0;
 			
 			switch (effect) {
 			case NONE:
 				fourground.setEffect(Slide.Effect.STRAIGHT);
 				break;
 			case STRAIGHT:
 				fourground.setEffect(Slide.Effect.RANDOM);
 				break;
 			case RANDOM:
 				fourground.setEffect(Slide.Effect.RAIN);
 				break;
 			case RAIN:
 				fourground.setEffect(Slide.Effect.STRAIGHT);
 				break;
 			}
 		}
 	}
 
 	private Bitmap makeBitmap(String path) {
 		Resources resource = getResources();
 		int screenWidth = resource.getDisplayMetrics().widthPixels;
 		int screenHeight = resource.getDisplayMetrics().heightPixels;
 
 		Bitmap image = BitmapFactory.decodeFile(path);
 
 		float scaleWidth = ((float) screenWidth) / image.getWidth();
 		float scaleHeight = ((float) screenHeight) / image.getHeight();
 		float scale = Math.min(scaleWidth, scaleHeight);
 		Matrix matrix = new Matrix();
 		matrix.postScale(scale, scale);
 
 		return Bitmap.createBitmap(image, 0, 0, image.getWidth(),
 				image.getHeight(), matrix, true);
 
 	}
 
 }
