 /*
  * Copyright (C) 2011 Nicolas Simonds
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.submedia.android.uqmwallpaper;
 
 import android.content.Context;
 import android.content.res.*;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.os.Environment;
 import android.os.Handler;
 import android.service.wallpaper.WallpaperService;
 import android.util.Log;
 import android.view.Display;
 import android.view.SurfaceHolder;
 import android.view.WindowManager;
import java.lang.Integer;
import java.lang.Thread;
 
 public class UQMWallpaper extends WallpaperService {
 
	public static final String SHARED_PREFS_NAME = "uqmwallpaper";
 	public static final String TAG = "UQMWallpaper";
 
 	private final Handler mHandler = new Handler();
 	private static Context mContext;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mContext = this;
 	}
 
	// XXX: switching contexts might confuse the hell out of things
 	public static Context getContext() {
 		return mContext;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	@Override
 	public Engine onCreateEngine() {
 		return new CommsEngine();
 	}
 
 	class CommsEngine extends Engine
 			implements SharedPreferences.OnSharedPreferenceChangeListener {
 
 		private int mOffset;
 		private int totalWidth;
 		private int aspectHeight;
 		private int mWidth;
 		private int mHeight;
 		private SharedPreferences mPrefs;
 		private Animation mAnim;
 		private int mScaling = 2;
 
 		private final Runnable mDrawComms = new Runnable() {
 			public void run() {
 				drawFrame();
 			}
 		};
 		private boolean mVisible;
 
 		CommsEngine() {
 			mPrefs = UQMWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
 			mPrefs.registerOnSharedPreferenceChangeListener(this);
 			onSharedPreferenceChanged(mPrefs, null);
 			// block indefinitely on the external storage coming online.
 			// Needed in order to initialize properly after reboots
 			while (! Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					continue;
 				}
 			}
 			Log.v(TAG, "started");
 		}
 
 		private void setAspect(Bitmap b) {
 			int aspect = (((mScaling > 1) ? totalWidth : mWidth) * 10000) / b.getWidth();
 			aspectHeight = (b.getHeight() * aspect) / 10000;
 		}
 
 		@Override
 		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 			Log.d(TAG, String.format("%s => %s", key, prefs.toString()));
 			try {
 				if (key != null) {
 					if (key.equals("race")) {
 						mAnim = new Animation(prefs.getString("contentPack", null),
 								prefs.getString("race", null), mContext);
 						Log.d(TAG, mAnim.toString());
 					}
 					else if (key.equals("scaling")) {
 						mScaling = Integer.parseInt(prefs.getString("scaling", "2"));
 					}
 					setAspect(mAnim.getFrame());
 				}
 			} catch (Exception e) {
 				Log.w(TAG, e.toString());
 				for (StackTraceElement t : e.getStackTrace()) {
 					Log.d(TAG, t.toString());
 				}
 				mAnim = null;
 			}
 			return;
 		}
 
 		@Override
 		public void onCreate(SurfaceHolder surfaceHolder) {
 			super.onCreate(surfaceHolder);
 		}
 
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			mHandler.removeCallbacks(mDrawComms);
 		}
 
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			mVisible = visible;
 			if (visible) {
 				drawFrame();
 			} else {
 				mHandler.removeCallbacks(mDrawComms);
 			}
 		}
 
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			super.onSurfaceChanged(holder, format, width, height);
 			mWidth = width;
 			mHeight = height;
 
 			if (mAnim == null) {
 				try {
 					mScaling = Integer.parseInt(mPrefs.getString("scaling", "2"));
 					mAnim = new Animation(mPrefs.getString("contentPack", null),
 							mPrefs.getString("race", null), mContext);
 					setAspect(mAnim.getFrame());
 				} catch (Exception e) {
 					Log.w(TAG, e.toString());
 					for (StackTraceElement t : e.getStackTrace()) {
 						Log.d(TAG, t.toString());
 					}
 					mAnim = null;
 				}
 			} else {
 				setAspect(mAnim.getFrame());
 			}
 
 			drawFrame();
 		}
 
 		@Override
 		public void onSurfaceCreated(SurfaceHolder holder) {
 			super.onSurfaceCreated(holder);
 			// determine width of virtual display.  All signs point to it
 			// being Display.getWidth() * 2 when orientation is 0
 			// For the record, retrieving the default display is ridiculous
 			// and poorly-documented.
 			Display d = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
 			totalWidth = 2 * (((d.getRotation() & 1) == 1) ?
 					d.getHeight() : d.getWidth());
 			// Log.d(TAG, String.format("totalWidth(%03d)\n", totalWidth));
 		}
 
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			mVisible = false;
 			mHandler.removeCallbacks(mDrawComms);
 		}
 
 		@Override
 		public void onOffsetsChanged(float xOffset, float yOffset,
 				float xStep, float yStep, int xPixels, int yPixels) {
 			mOffset = xPixels;
 			// Log.d(TAG, String.format("xOffset(%f) yOffset(%f) xStep(%f) yStep(%f) xPixels(%d) yPixels(%d)",
 			//		xOffset, yOffset, xStep, yStep, xPixels, yPixels));
 			drawFrame();
 		}
 
 		/*
 		 * Draw one frame of the animation.  You can do any drawing you want in
 		 * here.
 		 */
 		void drawFrame() {
 			final SurfaceHolder holder = getSurfaceHolder();
 			// comm frame rate according to UQM sources
 			int delay = (1000 / 40);
 
 			Canvas c = null;
 			try {
 				c = holder.lockCanvas();
 				if (c != null) {
 					// reset canvas
 					c.drawColor(Color.BLACK);
 					// draw something
 					if (mAnim == null) {
 						int w = mWidth / 2;
 						int h = mHeight / 2;
 						Paint p = new Paint();
 
 						p.setAntiAlias(true);
 						p.setColor(Color.GRAY);
 						p.setTextAlign(Align.CENTER);
 						c.drawText(getContext().getString(R.string.no_content1), w - 10, h - 10, p);
 						c.drawText(getContext().getString(R.string.no_content2), w + 10, h + 10, p);
 					} else {
 						/* Handle the differences based on the scaling type.
 						 * ternary operator abuse is a capital offense in some
 						 * countries...
 						 */
 						int width = (mScaling > 1) ? totalWidth : mWidth;
 						Bitmap b = (mScaling == 0) ?
 								Bitmap.createBitmap(mAnim.getFrame()) :
 								Bitmap.createScaledBitmap(mAnim.getFrame(), width, aspectHeight, true);
 
 						/* Types 0 and 1 have no "parallax effect", and as such
 						 * require no special handling.
 						 * Type 2 needs to position itself according to the
 						 * current screen offset, unless it's in preview mode
 						 */
 						int x;
 						if (mScaling == 2) {
 							x = ((((this.isPreview()) ? mWidth : totalWidth) / 2) -
 									(b.getWidth() / 2)) + (this.isPreview() ? 0 : mOffset);
 						} else {
 							x = (mWidth / 2) - (b.getWidth() / 2);
 						}
 
 						int y = (mHeight / 2) - (b.getHeight() / 2);
 						if (b != null) {
 							c.drawBitmap(b, x, y, null);
 						}
 						delay = mAnim.next_frame_delay;
 					}
 				}
 			} finally {
 				if (c != null) holder.unlockCanvasAndPost(c);
 			}
 
 			// Reschedule the next redraw
 			mHandler.removeCallbacks(mDrawComms);
 			if (mVisible) {
 				mHandler.postDelayed(mDrawComms, delay);
 			}
 		}
 	}
 }
