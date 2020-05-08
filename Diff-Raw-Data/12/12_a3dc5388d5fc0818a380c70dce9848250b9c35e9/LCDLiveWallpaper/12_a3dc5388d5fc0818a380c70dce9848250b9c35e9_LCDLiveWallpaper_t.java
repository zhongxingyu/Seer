 package lwp;
 
 //import java.sql.Date;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.service.wallpaper.WallpaperService;
 
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.WindowManager;
 
 public class LCDLiveWallpaper extends WallpaperService {
 
 	private class MyWallpaperEngine extends Engine {
 		private Handler mHandler = new Handler();
 		private SurfaceHolder mSurfaceHolder;
 		private Canvas mCanvas = null;
 		private Paint onPixelPaint = null;
 		private int width = 0;
 		private int height = 0;
 		private int pixelWidth = 0;
 		private int pixelHeight = 0;
 		private int[][] displayMatrix;
 		private int refreshDelay = 1000 / framerate;
 		private WriteClass wC = new WriteClass();
 		float margin = 0.5f;
 
 		private Runnable mDraw = new Runnable() {
 
 			public void run() {
 				drawFrame();
 				refreshDelay = 1000 / framerate;
 			}
 
 		};
 
 		private void drawBg(Canvas c) {
 			Paint bg = new Paint();
 			bg.setARGB(0xFF, 0x88, 0xAA, 0x88);
 			try {
 				c.drawRect(0, 0, width, height, bg);
 			} catch (Exception e) {
 			}
 		}
 
 		private void drawFrame() {
 
 			mCanvas = mSurfaceHolder.lockCanvas();
 			drawBg(mCanvas);
 			update();
 			drawMatrix();
 
 			try {
 				mSurfaceHolder.unlockCanvasAndPost(mCanvas);
 			} catch (Exception e) {
 			}
 			mHandler.postDelayed(mDraw, refreshDelay);
 
 		}
 
 		private void drawMatrix() {
 			for (int i = 0; i < LCD_WIDTH; i++)
 				for (int j = 0; j < LCD_HEIGHT; j++) {
 					if (displayMatrix[i][j] != 0)
						try {
							drawPixel(i, j, displayMatrix[i][j]);
						} catch (Exception e) {
						}
 				}
 		}
 
 		private void drawPixel(int x, int y, int value) {
 
 			try {
 				if (value != 0) {
 					mCanvas.drawRect((x * pixelWidth) + margin,
 							(y * pixelHeight) + margin, (x * pixelWidth)
 									+ pixelWidth - margin, (y * pixelHeight)
 									+ pixelHeight - margin, onPixelPaint);
 				} else {
 					Paint offPixelPaint = new Paint();
 					offPixelPaint.setARGB(0x10, 0x33, 0x33, 0x33);
 					offPixelPaint.setStrokeWidth(0.5f);
 
 					mCanvas.drawRect((x * pixelWidth) + margin,
 							(y * pixelHeight) + margin, (x * pixelWidth)
 									+ pixelWidth - margin, (y * pixelHeight)
 									+ pixelHeight - margin, offPixelPaint);
 				}
 			} catch (Exception e) {
 			}
 		}
 
		public void init() {
 			width = mCanvas.getWidth();
 			height = mCanvas.getHeight();
 			pixelWidth = width / LCD_WIDTH;
 			pixelHeight = height / LCD_HEIGHT;
 			try {
 				mSurfaceHolder.unlockCanvasAndPost(mCanvas);
 			} catch (Exception e) {
 			}
 
 			SharedPreferences sharedPref = PreferenceManager
 					.getDefaultSharedPreferences(context);
 
 			String candySetting = sharedPref.getString("eye_candy", "None");
 			Boolean clockEnabled = sharedPref.getBoolean("show_clock", false);
 			Boolean dateEnabled = sharedPref.getBoolean("show_date", false);
 			setEyeCandy(candySetting);
 			WriteClass.setTime(clockEnabled);
 			WriteClass.setDate(dateEnabled);
 			setFramerate(sharedPref.getString("frame_rate", "10"));
 		}

 		@SuppressLint("NewApi")
 		public void initMatrix() {
 			final WindowManager w = (WindowManager) getApplicationContext()
 					.getSystemService(Context.WINDOW_SERVICE);
 			final Display d = w.getDefaultDisplay();
 			final DisplayMetrics m = new DisplayMetrics();
 			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
 
 			if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
 				d.getRealMetrics(m);
 			} else {
 				d.getMetrics(m);
 			}
 			if (m.heightPixels >= 500) {
 				LCD_WIDTH = m.widthPixels / 10;
 				LCD_HEIGHT = m.heightPixels / 10;
 			} else if (m.heightPixels > 400) {
 				LCD_WIDTH = m.widthPixels / 5;
 				LCD_HEIGHT = m.heightPixels / 5;
 			} else {
 				LCD_WIDTH = m.widthPixels / 4;
 				LCD_HEIGHT = m.heightPixels / 4;
 			}
 			displayMatrix = new int[LCD_WIDTH][LCD_HEIGHT];
 			for (int i = 0; i < LCD_WIDTH; i++)
 				for (int j = 0; j < LCD_HEIGHT; j++)
 					displayMatrix[i][j] = 0;
 		}
 
 		@Override
 		public void onCreate(SurfaceHolder surfaceHolder) {
 			super.onCreate(surfaceHolder);
 			mSurfaceHolder = surfaceHolder;
 			mSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
 			onPixelPaint = new Paint();
 			onPixelPaint.setARGB(0xFF, 0x33, 0x33, 0x33);
 			onPixelPaint.setStrokeWidth(0.5f);
 
 		}
 
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format,
 				int width, int height) {
 			super.onSurfaceChanged(holder, format, width, height);
 		}
 
 		@Override
 		public void onSurfaceCreated(SurfaceHolder holder) {
 			super.onSurfaceCreated(holder);
 			initMatrix();
 			mCanvas = mSurfaceHolder.lockCanvas();
 			init();
 
 		}
 
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			mHandler.removeCallbacks(mDraw);
 		}
 
 		@Override
 		public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
 			super.onSurfaceRedrawNeeded(holder);
 			// drawFrame();
 
 		}
 
 		@Override
 		public void onTouchEvent(MotionEvent event) {
 			super.onTouchEvent(event);
 
 			// int touchX = (int) event.getX() * LCD_WIDTH / width;
 			// int touchY = (int) event.getY() * LCD_HEIGHT / height;
 
 			if (event.getAction() == MotionEvent.ACTION_UP)
 				initMatrix();
 
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				wC.incTouch();
 		}
 
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			super.onVisibilityChanged(visible);
 			if (visible)
 				mHandler.postDelayed(mDraw, refreshDelay);
 			else
 				mHandler.removeCallbacks(mDraw);
 
 		}
 
 		@SuppressLint("SimpleDateFormat")
 		private void update() {
 			initMatrix(); // Called just to clean the matrix
 
 			if (eyeCandy != null) {
 				displayMatrix = eyeCandy.draw(displayMatrix);
 			}
 
 			displayMatrix = wC.drawDateTime(displayMatrix);
 
 		}
 	}
 
 	// Static stuff goes here
 	private static int LCD_WIDTH = 72;
 	private static int LCD_HEIGHT = 128;
 	private static EyeCandy eyeCandy = null;
 	private static Context context = null;
 	private static int framerate = 1;
 	private static MyWallpaperEngine engine;
 
 	public static int getLCD_WIDTH() {
 		return LCD_WIDTH;
 	}
 
 	public static int getLCD_HEIGHT() {
 		return LCD_HEIGHT;
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// TODO Auto-generated method stub
 		super.onConfigurationChanged(newConfig);
 		engine.init();
 		engine.initMatrix();
 	}
 
 	@Override
 	public Engine onCreateEngine() {
 		context = getApplicationContext();
 		engine = new MyWallpaperEngine();
 
 		return engine;
 	}
 
 	public static void setEyeCandy(String name) {
 		if (name.equalsIgnoreCase("None")) {
 			eyeCandy = null;
 		} else if (name.equalsIgnoreCase("Gradient")) {
 			eyeCandy = new EyeCandyGradient();
 		} else if (name.equalsIgnoreCase("Random")) {
 			eyeCandy = new EyeCandyRandom();
 		}
 
 	}
 
 	public static void setFramerate(String value) {
 		int f = Integer.parseInt(value);
 
 		if (f > 0)
 			framerate = f;
 		else
 			framerate = 10;
 	}
 
 }
