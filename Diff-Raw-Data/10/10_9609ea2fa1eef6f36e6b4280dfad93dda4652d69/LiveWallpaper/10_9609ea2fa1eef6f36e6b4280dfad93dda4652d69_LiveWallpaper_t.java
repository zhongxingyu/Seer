 package com.luzi82.randomwallpaper;
 
 import java.nio.ByteBuffer;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.service.wallpaper.WallpaperService;
 import android.view.SurfaceHolder;
 
 public class LiveWallpaper extends WallpaperService {
 
 	public static final String LOG_TAG = "LiveWallpaper";
 	public static final String PREFERENCE_NAME = "PREF";
 
 	@Override
 	public void onDestroy() {
 		cleanBitmap();
 		super.onDestroy();
 	}
 
 	@Override
 	public Engine onCreateEngine() {
 		// Create the live wallpaper engine
 		return new LiveWallpaperEngine();
 	}
 
 	static final Matrix mi = new Matrix();
 	static final Paint paint = new Paint();
 	static final Random random = new Random();
 
 	static int oldWidth = -1;
 	static int oldHeight = -1;
 
 	static byte[] byteAry;
 	static ByteBuffer byteBuffer;
 	static Bitmap bitmap;
 
 	static boolean colorRandom = false;
 	static int color = 0;
 	static boolean doRandom = false;
 	static boolean doColor = false;
 
 	static void cleanBitmap() {
 		// Log.d(LOG_TAG, "static synchronized void clean()");
 		synchronized (mi) {
 			// size = -1;
 			oldWidth = -1;
 			oldHeight = -1;
 			byteAry = null;
 			byteBuffer = null;
 			if (bitmap != null)
 				bitmap.recycle();
 			bitmap = null;
 			// cleanBuf();
 		}
 	}
 
 	// static void drawCanvas(Canvas c) {
 	// }
 
 	class LiveWallpaperEngine extends Engine implements
 			SharedPreferences.OnSharedPreferenceChangeListener {
 
 		SharedPreferences sp;
 		Timer timer;
 
 		@Override
 		public void onCreate(SurfaceHolder surfaceHolder) {
 			super.onCreate(surfaceHolder);
 			sp = getSharedPreferences(PREFERENCE_NAME, 0);
 		}
 
 		@Override
 		public void onDestroy() {
 			synchronized (mi) {
 				cleanEngine();
 			}
 			super.onDestroy();
 		}
 
 		// Become false when switching to an app or put phone to sleep
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			super.onVisibilityChanged(visible);
 			synchronized (mi) {
 				// Log.d(LOG_TAG, "onVisibilityChanged=" + visible);
 				if (visible) {
 					startEngine();
 				} else {
 					cleanEngine();
 				}
 			}
 
 		}
 
 		private void updateCanvas() {
 			// Log.d(LOG_TAG, "updateCanvas start");
 			// long now = System.currentTimeMillis();
 			// int diff = (int) (now - time);
 			// time = now;
 			synchronized (mi) {
 				if (isVisible()) {
 					SurfaceHolder holder = getSurfaceHolder();
 					if (holder != null) {
 						// unable to get the size by SurfaceHolder only
 						Canvas c = null;
 						try {
 							c = holder.lockCanvas();
 							int nowWidth = c.getWidth();
 							int nowHeight = c.getHeight();
 							int s = (nowWidth * nowHeight) << 2;
 							// Log.d(LOG_TAG, "size "+nowWidth+" "+nowHeight);
 							if ((oldHeight != nowHeight)
 									|| (oldWidth != nowWidth)) {
 								cleanBitmap();
 								bitmap = Bitmap.createBitmap(nowWidth,
 										nowHeight, Bitmap.Config.ARGB_8888);
 								oldHeight = nowHeight;
 								oldWidth = nowWidth;
 							}
 							if (byteAry == null || byteAry.length != s) {
 								byteAry = new byte[s];
 								byteBuffer = ByteBuffer.wrap(byteAry);
 								// setSize(s);
 								// size = s;
 							}
 							genRandom(byteAry);
 							bitmap.copyPixelsFromBuffer(byteBuffer);
 							if (c != null) {
 								if (doRandom) {
 									c.drawBitmap(bitmap, mi, paint);
 								}
 								if (doColor) {
 									if (colorRandom) {
 										color = random.nextInt();
 									}
 									c.drawColor(color);
 								}
 							}
 						} finally {
 							if (c != null)
 								holder.unlockCanvasAndPost(c);
 						}
 					}
 				}
 			}
 			// Log.d(LOG_TAG, "updateCanvas end");
 		}
 
 		void createTimer() {
 			// Log
 			// .d(LOG_TAG,
 			// "static void createTimer(final LiveWallpaperEngine engine) start");
 			synchronized (mi) {
 				if (sp == null)
 					return;
 				int rateInt = Settings.getRefreshPeroid(sp);
 				if (timer == null) {
 					// Log.d(LOG_TAG, "create timer");
 					timer = new Timer();
 					timer.scheduleAtFixedRate(new TimerTask() {
 						@Override
 						public void run() {
 							if(System.currentTimeMillis()-scheduledExecutionTime()>10)
 								return;
 							updateCanvas();
 						}
 					}, 100, rateInt);
 				}
 			}
 			// Log
 			// .d(LOG_TAG,
 			// "static void createTimer(final LiveWallpaperEngine engine) end");
 		}
 
 		void clearTimer() {
 			synchronized (mi) {
 				if (timer != null) {
 					timer.cancel();
 					timer = null;
 				}
 			}
 		}
 
 		void updateColor() {
 			synchronized (mi) {
 				if (sp == null)
 					return;
 				colorRandom = Settings.getColorCheat(sp);
 				if (colorRandom) {
 					doRandom = true;
 					doColor = true;
 				} else {
 					color = Settings.getColor(sp);
 					int alpha = Color.alpha(color);
 					doRandom = alpha != 0xff;
 					doColor = alpha != 0;
 				}
 				if (!doRandom) {
 					cleanBitmap();
 				}
 			}
 		}
 
 		@Override
 		public void onSharedPreferenceChanged(
 				SharedPreferences sharedPreferences, String key) {
 			if (!isVisible())
 				return;
 			if (key.equals(Settings.REFRESH_PEROID_KEY)) {
 				synchronized (mi) {
 					clearTimer();
 					createTimer();
 				}
 			} else if (key.equals(Settings.COLOR_KEY)) {
 				updateColor();
 				updateCanvas();
 			}
 		}
 
 		public void startEngine() {
 			if (sp == null)
 				return;
 			sp.registerOnSharedPreferenceChangeListener(this);
 			updateColor();
 			createTimer();
 		}
 
 		public void cleanEngine() {
 			if (sp != null) {
 				sp.unregisterOnSharedPreferenceChangeListener(this);
 			}
 			clearTimer();
 			cleanBitmap();
 		}
 	}
 
 	static {
 		System.loadLibrary("randompixel");
 		setSeed(System.currentTimeMillis());
 	}
 
 	public static native void getVersion(byte[] out);
 
 	public static native void setSeed(long seed);
 
 	public static native void genRandom(byte[] out);
 
 }
