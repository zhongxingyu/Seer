 /*
  * Copyright (C) 2012 Slawomir Cygan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.scygan.emuTi;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 import java.lang.Thread;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.AssetManager;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.Window;
 
 public class EmuTi extends Activity
 {
 	EmuTiView emutiView;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
     	super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         emutiView = new EmuTiView(this);
         setContentView(emutiView);
     }
     
     @Override
     public void onPause() {
     	emutiView.stop();
     	super.onPause();
     }
 
     /* load our native library */
     static {
         System.loadLibrary("emuti");
     }
 }
 
 class EmuTiView extends SurfaceView implements SurfaceHolder.Callback {
     private Thread mNativeThread;
     private EmuTiNative mNative;
     
     public EmuTiView(Context context) {
     	super(context);
         getHolder().addCallback(this);
         mNative = new EmuTiNative(getContext().getAssets(), getHolder(), this);
         setFocusable(true);
     }
 
 	public void stop() {
 		 boolean retry = true;
 		 mNative.requestStop();
 		 while (retry) {
 	    	 try {
 	    		 mNativeThread.join();
 	             retry = false;
 	         } catch (InterruptedException e) {
 	                // we will try it again and again...
 	         }
 	     }
 	}
 	
 	public void start() {
		mNativeThread = new Thread(mNative);
		mNativeThread.start();		
 	}
 	
 	@Override protected void onDraw(Canvas canvas) {
     	mNative.onDraw(canvas);
     	super.onDraw(canvas);
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	if (mNativeThread != null  && mNativeThread.isAlive()) {
     		return mNative.onTouch(event);
     	}
     	return false;    	
     }
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		mNative.onSurfaceResize(width, height);
 	}
 
 	public void surfaceCreated(SurfaceHolder arg0) {
 		start();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		stop();
 	}
 
 }
 
 class TouchEvent {
 	public TouchEvent(int x, int y, boolean pressed){
 		this.x = x;
 		this.y = y;
 		this.pressed = pressed;
 	}
 	int x, y;
 	boolean pressed;
 }
 
 class EmuTiNative implements Runnable {
     final int W = 480;
     final int H = 800;
 	private SurfaceHolder mSurfaceHolder;
 	private Bitmap mBitmap;
 	private AssetManager mAssetManager;
 	EmuTiView mView;
 	private Semaphore mTouchLock = new Semaphore(1);
 	private Queue<TouchEvent> mTouchEventQueue = new LinkedList<TouchEvent>();
 	int mSurfaceWidth = W, mSurfaceHeight = H;
 	boolean mStopExpected = false;
 	
     /* Implemented by libemuti.so */
 	private static native void nativeEntry(Bitmap  bitmap, AssetManager assetManager, EmuTiNative emuTiNative);
 	
     /* Implemented by libemuti.so */
 	private static native void nativeStop();
 	
 	public EmuTiNative(AssetManager assetManager, SurfaceHolder surfaceHolder, EmuTiView view) {
 		mBitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
 		mBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
 		mAssetManager = assetManager;
 		mSurfaceHolder = surfaceHolder;
 		mView = view;
 	}
 	
 	public boolean onTouch(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_DOWN) {
 			try {
 				mTouchLock.acquire();
 				mTouchEventQueue.add(new TouchEvent((int)event.getX() * mBitmap.getWidth() / mSurfaceWidth,
 						(int)event.getY() * mBitmap.getHeight() / mSurfaceHeight, event.getAction() == MotionEvent.ACTION_DOWN));
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			mTouchLock.release();
 			return true;
 		}
 		return false;
 	}
 	
 	public void onSurfaceResize(int width, int height) {
 		mSurfaceWidth = width;
 		mSurfaceHeight = height;
 
 	}
 	
 	public void onDraw(Canvas c) {
		c.scale(((float)mSurfaceWidth)/mBitmap.getWidth(), ((float)mSurfaceHeight)/mBitmap.getHeight());
		c.drawBitmap(mBitmap, 0, 0, null);
 	}
 	
 	public void requestStop() {
 		mStopExpected = true;
 		nativeStop();
 	}
 	
 	public void run() {
 		mStopExpected = false;
 		nativeEntry(mBitmap, mAssetManager, this);
 		if (!mStopExpected) {
 			//calculator was powered off by user
 			mView.post(new Runnable() {
                 public void run() {
                 	Context ctx = mView.getContext();
                 	Resources res = ctx.getResources();
                 	AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
                 	alertDialog.setTitle(res.getString(R.string.calc_powered_off));
                 	alertDialog.setButton(res.getString(R.string.power_on), new DialogInterface.OnClickListener() {
                 	      public void onClick(DialogInterface dialog, int which) {
                  	      //here you can add functions
                 	      mView.stop();
                 	      mView.start();
                 	} }); 
                 	//alertDialog.setIcon(R.drawable.icon);
                 	alertDialog.show();
                 }
             });
 		}
 	}
 	
 	/* Called by libemuti.so */
 	private TouchEvent getTouch() { //called from native thread
 		TouchEvent ret = null;
 		try {
 			mTouchLock.acquire();
 			if (!mTouchEventQueue.isEmpty()) {
 				ret = mTouchEventQueue.remove();
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}				
 		mTouchLock.release();
 		return ret; 
 	}
 	
 	/* Called by libemuti.so */
 	private void flip() { //called from native thread
 		Canvas c = null;
         try {
             c = mSurfaceHolder.lockCanvas(null);
             synchronized(mSurfaceHolder) {
             	onDraw(c);
             }
         } finally {
             // do this in a finally so that if an exception is thrown
             // during the above, we don't leave the Surface in an
             // inconsistent state
             if (c != null) {
             	mSurfaceHolder.unlockCanvasAndPost(c);
             }
 	    }
 	}
 }
 
 
