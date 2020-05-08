 package com.garlicg.cutinlib;
 
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.View;
 import android.view.WindowManager;
 
 public abstract class CutinService extends Service {
 	private View mLayout;
 	private WindowManager mWindowManager;
 	private boolean mStarted = false;
 
 	/**
 	 * Create root view which is inflated to full screen window.
 	 */
 	protected abstract View create();
 
 	/**
 	 * It is called after create(). At this time, view size is possible to get.
 	 * You must call finishCutin() or stopSelf() after your animation ending.
 	 */
 	protected abstract void start();
 
 	/**
	 * Release resources, etc.
 	 */
 	protected abstract void destroy();
 
 	@Override
 	final public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	@Override
 	final public void onCreate() {
 		super.onCreate();
 
 		mLayout = create();
 
 		if (mLayout == null) {
 			throw new NullPointerException("CutinService#create need to return view.");
 		}
 
 		@SuppressWarnings("deprecation")
 		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
 				WindowManager.LayoutParams.FILL_PARENT,
 				WindowManager.LayoutParams.FILL_PARENT,
 				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				PixelFormat.TRANSLUCENT);
 
 		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
 		mWindowManager.addView(mLayout, params);
 	}
 
 	@Override
 	final public int onStartCommand(Intent intent, int flags, final int startId) {
 		if (!mStarted) {
 			mStarted = true;
 			
 			// must to be possible to get view size on start method. 
 			new Handler().post(new Runnable() {
 				@Override
 				public void run() {
 					start();
 				}
 			});
 		} else {
 			reStart();
 		}
 		return START_NOT_STICKY;
 	}
 
 	protected void reStart() {
 	}
 
 	protected void finishCutin() {
 		stopSelf();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		destroy();
 		if (mLayout != null) {
 			mWindowManager.removeView(mLayout);
 		}
 	}
 
 }
