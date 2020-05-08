 package com.garlicg.sample.cutin;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.PixelFormat;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Rect;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 import com.garlicg.cutinlib.CutinService;
 import com.garlicg.sample.cutin.util.Logger;
 
 public class GarlinParade extends CutinService {
 	private ParadeView mView;
 	
 	@Override
 	protected View create() {
 		RelativeLayout layout = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.garlin_parade, null);
 		mView = new ParadeView(this);
 		@SuppressWarnings("deprecation")
 		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		layout.addView(mView, 0, p);
 		return layout;
 	}
 
 	@Override
 	protected void start() {
 		mView.setOnParadeEndListener(new OnAnimationEndListener() {
 			@Override
 			public void endAnimation() {
 				finishCutin();
 			}
 		});
 		mView.startParade();
 	}
 
 	@Override
 	protected void destroy() {
 		mView.onDestroy();
 	}
 	
 	
 	private interface OnAnimationEndListener{
 		void endAnimation();
 	}
 	
 	private class ParadeView extends SurfaceView implements SurfaceHolder.Callback{
 		private class Garlin{
 			private Rect src;
 			private Rect dest;
 			public Garlin(){
 			}
 		}
 		private Bitmap mBitmap;
 		private Timer mTimer;
 		private OnAnimationEndListener mListener;
 		private long RATE = 1000/25;
 		private float mScreenWidth;
 		private float mScreenDx;
 		private float mFinishDx;
 		private float mScreenHeight;
 		private int mGarlinWidth;
 		private int mGarlinRectX;
 		private int mGarlinRectY;
 		private Garlin[][] mGarlins;
 		private static final int MAX_TATE = 5;
 		private static final int MAX_YOKO = 15;
 		
 		public ParadeView(Context context) {
 			super(context);
 			Logger.v("PradaView");
 			mTimer = new Timer(true);
 			getHolder().addCallback(this);
 			getHolder().setFormat(PixelFormat.TRANSPARENT);
			mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.garlin);
 		}
 		
 		@Override
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 				int height) {
 			Logger.v("width:" + width + " , height:" + height);
 			
 			mScreenWidth = width;
 			mScreenHeight = height;
 			
 			mGarlinWidth = (int)(mScreenHeight / MAX_TATE);
 			mGarlinRectX = mGarlinWidth/2;
 			mGarlinRectY = mGarlinWidth;
 		}
 
 		@Override
 		public void surfaceCreated(SurfaceHolder holder) {
 			Logger.v("surface create");
 		}
 
 		@Override
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			Logger.v("surface destroy");
 			mBitmap.recycle();
 		}
 		
 		private void onDestroy(){
 			mTimer.purge();
 		}
 		
 		protected void draw() {
 			if(!mBitmap.isRecycled()){
 				Canvas canvas = getHolder().lockCanvas();
 				if(canvas != null){
 					canvas.translate(mScreenDx, 0);
 					canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
 					int size1 = mGarlins.length;
 					for(int i = 0 ; i < size1 ; i++){
 						int size2 = mGarlins[i].length;
 						for(int s = 0 ; s < size2 ; s++){
 							Garlin garin = mGarlins[i][s];
 							// 描画
 							canvas.save();
 							canvas.rotate((float)(Math.random()*20)-10 , garin.dest.left,garin.dest.top);
 							canvas.drawBitmap(mBitmap, garin.src, garin.dest, null);
 							canvas.restore();
 						}
 					}
 					getHolder().unlockCanvasAndPost(canvas);
 				}
 			}
 		}
 		
 		private void startParade(){
 			Logger.v("startParade");
 			mGarlins = new Garlin[MAX_TATE][MAX_YOKO];
 			for(int i = 0 ; i < MAX_TATE ; i++){
 				for(int s = 0 ; s< MAX_YOKO ; s++){
 					Garlin garlin = new Garlin();
 					garlin.src = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
 					garlin.dest = new Rect(
 							mGarlinRectX*s, 
 							mGarlinRectY*i, 
 							mGarlinRectX*s+(int)(mGarlinWidth*1.5), 
 							mGarlinRectY*i+(int)(mGarlinWidth*1.5));
 					mGarlins[i][s] = garlin;
 				}
 			}
 			
 			mScreenDx = -(mScreenWidth + (mGarlinRectX * MAX_YOKO));
 			mFinishDx = mScreenWidth + (mGarlinRectX * (MAX_YOKO-1)*2/3);
 			final int xdiff = dpToPx(getResources(), 30);
 			
 			// invalidater
 			mTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					if(mScreenDx > mFinishDx){
 						mTimer.cancel();
 						mListener.endAnimation();
 					}
 					else{
 						draw();
 					}
 					mScreenDx += xdiff;
 				}
 			}, 0,RATE);
 		}
 		
 		private void setOnParadeEndListener(OnAnimationEndListener listener){
 			mListener = listener;
 		}
 		
 		private int dpToPx(Resources res , int dp){
 	    	return (int)(res.getDisplayMetrics().density * dp + 0.5f);
 		}
 		
 		
 	}
 
 }
