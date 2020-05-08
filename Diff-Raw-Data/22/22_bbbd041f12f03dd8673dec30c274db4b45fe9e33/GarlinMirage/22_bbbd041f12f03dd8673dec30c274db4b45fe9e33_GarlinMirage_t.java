 package com.garlicg.sample.cutin;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 import com.garlicg.cutinlib.CutinService;
 
 public class GarlinMirage extends CutinService {
 	
 	private MirageView mView;
 	
 	@Override
 	protected View create() {
 		RelativeLayout layout = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.garlin_mirage, null);
 		mView = new MirageView(this);
 		mView.invalidate();
		@SuppressWarnings("deprecation")
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		layout.addView(mView, 0, p);
 
 		return layout;
 	}
 	
 	@Override
 	protected void start() {
 		mView.setOnMirageEndListener(new OnAnimationEndListener() {
 			@Override
 			public void endAnimation() {
 				stopSelf();
 			}
 		});
 		
 		mView.startMirage();
 	}
 	
 	@Override
 	protected void destroy() {
 		mView.onDestroy();
 	}
 	
 	
 	private interface OnAnimationEndListener{
 		void endAnimation();
 	}
 	
 	public static  class MirageView extends View{
 
 		private class Garlin{
 			private Paint paint;
 			private int alphaDiff = -0x06;
 			private Rect src;
 			private Rect dst;
 			
 			public Garlin(){
 				src = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
 				paint = new Paint();
 				paint.setAlpha(0x00);
 			}
 		}
 		
 		private Bitmap mBitmap;
 		private final static int ALPHA_START = 0xFF;
 		private final static int ALPHA_START2 = 0xAA;
 		private Handler mHandler;
 		private Timer mTimer;
 		private OnAnimationEndListener mListener;
 		private long RATE = 1000/25;
 		private Garlin[] mGarlins1;
 		private Garlin[] mGarlins2Top;
 		private Garlin[] mGarlins2Bottom;
 		
 		private float mScreenWidth;
 		private float mScreenHeight;
 		private int mGarlinBaseWidth;
 		
 		
 		public MirageView(Context context) {
 			super(context);
 			mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.garlin);
 			mTimer = new Timer(true);
 			mHandler = new Handler();
 		}
 		
 		@Override
 		protected void onLayout(boolean changed, int left, int top, int right,
 				int bottom) {
 			super.onLayout(changed, left, top, right, bottom);
 			mScreenWidth = right -left;
 			mScreenHeight = bottom - top;
 			mGarlinBaseWidth = (int)(mScreenHeight / 3);
 		}
 		
 		private void onDestroy(){
 			mTimer.purge();
 			mBitmap.recycle();
 		}
 		
 		@Override
 		protected void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 			if(!mBitmap.isRecycled()){
 				if(mGarlins1 != null){
 					for(int i = mGarlins1.length-1 ; i >=0 ;i-- ){
 						Garlin garlin = mGarlins1[i];
 						if(garlin.paint.getAlpha() > 0){
 							canvas.drawBitmap(mBitmap, garlin.src, garlin.dst,garlin.paint);
 							int nextAlpha = Math.max(0, garlin.paint.getAlpha() + garlin.alphaDiff);
 							garlin.paint.setAlpha(nextAlpha);
 						}
 					}
 				}
 				if(mGarlins2Top != null){
 					for(int i = 0; i<7 ; i++){
 						Garlin garlin = mGarlins2Top[i];
 						if(garlin.paint.getAlpha() > 0){
 							canvas.drawBitmap(mBitmap, garlin.src, garlin.dst,garlin.paint);
 							int nextAlpha = Math.max(0, garlin.paint.getAlpha() + garlin.alphaDiff);
 							garlin.paint.setAlpha(nextAlpha);
 						}
 					}
 				}
 				if(mGarlins2Bottom != null){
 					for(int i = 6; i >=0 ; i--){
 						Garlin garlin = mGarlins2Bottom[i];
 						if(garlin.paint.getAlpha() > 0){
 							canvas.drawBitmap(mBitmap, garlin.src, garlin.dst,garlin.paint);
 							int nextAlpha = Math.max(0, garlin.paint.getAlpha() + garlin.alphaDiff);
 							garlin.paint.setAlpha(nextAlpha);
 						}
 					}
 				}
 			}
 		}
 		
 		private void startMirage(){
 			
 			createGarlins();
 			
 			// 定期描画タスク
 			mTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					mHandler.post(new Runnable() {
 						@Override
 						public void run() {
 							invalidate();
 						}
 					});
 				}
 			}, 0,RATE);
 			
			// param updater : middle
 			mTimer.schedule(new TimerTask() {
 				int sCount1 = 0;
 				@Override
 				public void run() {
 					if(sCount1 == 0){
 						mGarlins1[0].paint.setAlpha(ALPHA_START);
 					}
 					else if(sCount1 == 1){
 						mGarlins1[1].paint.setAlpha(ALPHA_START);
 						mGarlins1[2].paint.setAlpha(ALPHA_START);
 					}
 					else if(sCount1 == 2){
 						mGarlins1[3].paint.setAlpha(ALPHA_START);
 						mGarlins1[4].paint.setAlpha(ALPHA_START);
 					}
 					sCount1++;
 				}
 			},0,100);
 			
			// param updater : Bottom
 			mTimer.schedule(new TimerTask() {
 				int sCount = 0;
 				@Override
 				public void run() {
 					if(sCount < 7){
 						mGarlins2Bottom[6-sCount].paint.setAlpha(ALPHA_START2);
 					}
 					sCount++;
 				}
 			}, 400 , 80);
 			
			// param updater : top
 			mTimer.schedule(new TimerTask() {
 				int sCount = 0;
 				@Override
 				public void run() {
 					if(sCount < 7){
 						mGarlins2Top[sCount].paint.setAlpha(ALPHA_START2);
 					}
 					sCount++;
 				}
 			}, 800 , 80);
 			
			// cutin canceler
 			mTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					mTimer.cancel();
 					mHandler.post(new Runnable() {
 						@Override
 						public void run() {
 							mListener.endAnimation();
 						}
 					});
 				}
			}, 2800);
 		}
 		
 		private void createGarlins(){
 			int tmp = mGarlinBaseWidth;
 			mGarlinBaseWidth = (int)(mGarlinBaseWidth*1.5f);
 			// -- 真ん中から左右に分身がでるやつ
 			mGarlins1 = new Garlin[5];
 			mGarlins1[0] = new Garlin(); // center
 			mGarlins1[0].dst = new Rect(
 					(int)(mScreenWidth/2 - mGarlinBaseWidth/2),
 					(int)(mScreenHeight/2 - mGarlinBaseWidth/2),
 					(int)(mScreenWidth/2 + mGarlinBaseWidth/2),
 					(int)(mScreenHeight/2 + mGarlinBaseWidth/2)
 					);
 			mGarlins1[1] = new Garlin(); // right1 半分右ズラし and ちょっと小さくなる
 			mGarlins1[1].dst = new Rect(
 					mGarlins1[0].dst.left + mGarlinBaseWidth/3, 
 					mGarlins1[0].dst.top + mGarlinBaseWidth/10,
 					mGarlins1[0].dst.right + mGarlinBaseWidth/3,
 					mGarlins1[0].dst.bottom - mGarlinBaseWidth/10
 					);
 			mGarlins1[3] = new Garlin();
 			mGarlins1[3].dst = new Rect( // さらにもうチョイ
 					mGarlins1[1].dst.left + mGarlinBaseWidth/3, 
 					mGarlins1[1].dst.top + mGarlinBaseWidth/10,
 					mGarlins1[1].dst.right + mGarlinBaseWidth/3,
 					mGarlins1[1].dst.bottom - mGarlinBaseWidth/10
 					);
 			mGarlins1[2] = new Garlin(); // left1 半分左ズラし and ちょっと小さくなる
 			mGarlins1[2].dst = new Rect(
 					mGarlins1[0].dst.left - mGarlinBaseWidth/3, 
 					mGarlins1[0].dst.top + mGarlinBaseWidth/10,
 					mGarlins1[0].dst.right - mGarlinBaseWidth/3,
 					mGarlins1[0].dst.bottom - mGarlinBaseWidth/10
 					);
 			mGarlins1[4] = new Garlin(); // left2 さらにもうチョイ
 			mGarlins1[4].dst = new Rect(
 					mGarlins1[2].dst.left - mGarlinBaseWidth/3, 
 					mGarlins1[2].dst.top + mGarlinBaseWidth/10,
 					mGarlins1[2].dst.right - mGarlinBaseWidth/3,
 					mGarlins1[2].dst.bottom - mGarlinBaseWidth/10
 					);
 			
			// 上の段 等間隔  下の段　等間隔
 			mGarlinBaseWidth = tmp;
 			mGarlins2Top = new Garlin[7];
 			mGarlins2Bottom = new Garlin[7];
 			int left = (int)(mScreenWidth/7);
 			for(int i = 0 ; i < 7;i++){
 				mGarlins2Top[i] = new Garlin();
 				mGarlins2Top[i].dst = new Rect(
 						i*left - mGarlinBaseWidth/4,
 						0,
 						i*left + mGarlinBaseWidth - mGarlinBaseWidth/4,
 						mGarlinBaseWidth);
 				mGarlins2Bottom[i] = new Garlin();
 				mGarlins2Bottom[i].dst = new Rect(
 						i*left - mGarlinBaseWidth/2,
 						mGarlinBaseWidth*2,
 						i*left + mGarlinBaseWidth/2,
 						mGarlinBaseWidth*3);
 			}
 			
 		}
 		
 		private void setOnMirageEndListener(OnAnimationEndListener listener){
 			mListener = listener;
 		}
 		
 	}
 	
 	
 	
 }
