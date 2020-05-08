 package com.Ichif1205.anrakutei;
 
 import java.util.ArrayList;
 
 import com.Ichif1205.anrakutei.Invader.InvarderListener;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class FieldSurfaceView extends SurfaceView implements
 		SurfaceHolder.Callback, Runnable, InvarderListener {
 	private final String TAG = FieldSurfaceView.class.getSimpleName();
 	private final int MAX_INVADER_NUM = 9;
 
 	private SurfaceHolder mHolder;
 	private Canvas mCanvas;
 	private Paint mPaint;
 	private Player mPlayer;
	private final Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
			R.drawable.invader);;
 	private Thread mThread;
 	private ArrayList<Shot> mShotList;
 	private ArrayList<InvaderBeam> mInvBeamList;
 	private ArrayList<Invader> mInvaderList;
 
 	public FieldSurfaceView(Context context) {
 		super(context);
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 		mHolder.setFixedSize(getWidth(), getHeight());
 	}
 
 	public FieldSurfaceView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 		mHolder.setFixedSize(getWidth(), getHeight());
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO 自動生成されたメソッド・スタブ
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		mThread = new Thread(this);
 		mThread.start();
 
 		mPaint = new Paint();
 		mPaint.setColor(Color.GREEN);
 		mPaint.setAntiAlias(true);
 
 		mInvBeamList = new ArrayList<InvaderBeam>();
 		mShotList = new ArrayList<Shot>();
 		mInvaderList = new ArrayList<Invader>();
 
 		mPlayer = new Player(getWidth() / 2, getHeight() * 7 / 8);
 		
 		// 複数の敵を表示
 		for (int i = 2; i < MAX_INVADER_NUM; i++) {
 			Invader invader = new Invader((getWidth() / i), (getHeight() / i),
 					this);
 			mInvaderList.add(invader);
 		}
 		
		
 		mShotList = new ArrayList<Shot>();
 
 		//onDraw();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO 自動生成されたメソッド・スタブ
 		mThread = null;
 	}
 
 	protected void onDraw() {
 		mCanvas = getHolder().lockCanvas();
		mCanvas.save();
 		mCanvas.drawColor(Color.BLACK);
 		// 自機の描画
 		if (mPlayer.getPlayerExistFlag()) {
 			drawPlayer();
 		}
 		for (int i = 0; i < mShotList.size(); i++) {
 			Shot shot = mShotList.get(i);
 			float shotPosX = shot.getShotPosX();
 			float shotPosY = shot.getShotPosY();
 			
 			for (Invader invader : mInvaderList) {
 				boolean isShooted = invader.isShooted(shotPosX, shotPosY);
 				// 弾が敵に当たったら消える
 				if (isShooted) {
 					shot.remove();
 					invader.remove();
 				}
 			}
 			
 			// 弾が画面上からはみ出るまで表示させ続ける
 			if (shotPosY > 0) {
 				shot.updatePosition();
 				drawShot(shot);
 			}
 		}
 		
 		for (Invader invader : mInvaderList) {
 			// 敵の描画
 			if (invader.getInvaderExistFlag()) {
 				drawInvader(invader);
 				for (int i = 0; i < mInvBeamList.size(); i++) {
 					InvaderBeam invBeam = mInvBeamList.get(i);
 					float invBeamPosX = invBeam.getInvBeamPosX();
 					float invBeamPosY = invBeam.getInvBeamPosY();
 					boolean isShooted = mPlayer.isShooted(invBeamPosX, invBeamPosY);
 					// ビームが自機に当たったら消える
 					if (isShooted) {
 						invBeam.remove();
 						mPlayer.remove();
 					}
 					// ビームが画面上からはみ出るまで表示させ続ける
 					if (invBeamPosY < getHeight()) {
 						invBeam.updatePosition();
 						drawInvBeam(invBeam);
 					}
 				}
 			}
 		}
 		
		mCanvas.restore();
 		getHolder().unlockCanvasAndPost(mCanvas);
 	}
 
 	// 自機描画
 	protected void drawPlayer() {
 		Path path = new Path();
 		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		mCanvas.drawPath(mPlayer.draw(path), mPaint);
 	}
 
 	// 敵描画
 	protected void drawInvader(Invader invader) {
 		invader.updatePosition();
 		if (invader.isOverBoundary(getWidth())) {
 			invader.reverseSpeedDirection();
 		}
 		Log.d(TAG, "[mBitmap]"+String.valueOf(mBitmap));
 		mCanvas.drawBitmap(mBitmap, invader.getInvaderPosX(),
 				invader.getInvaderPosY(), mPaint);
 	}
 
 	// 自機の弾描画
 	protected void drawShot(Shot shot) {
 		RectF rectf = shot.createRectangle();
 		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		mCanvas.drawRect(rectf, mPaint);
 	}
 
 	// 敵のビーム描画
 	protected void drawInvBeam(InvaderBeam invBeam) {
 		RectF rectf = invBeam.createRectangle();
 		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		mCanvas.drawRect(rectf, mPaint);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			// 弾発射
 			Shot shot = new Shot(mPlayer.getPlayerPosX(),
 					mPlayer.getPlayerPosY());
 			mShotList.add(shot);
 		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			mPlayer.setPlayerPosX(event.getX());
 		} else if (event.getAction() == MotionEvent.ACTION_UP) {
 			// mPlayer.setPlayerPosX(event.getX());
 		}
 
 		return true;
 	}
 
 	@Override
 	public void run() {
 		while (mThread != null) {
 			onDraw();
 		}
 	}
 
 	@Override
 	public void shootBeamEvent(float shotX, float shotY) {
 		InvaderBeam invBeam = new InvaderBeam(shotX, shotY);
 		mInvBeamList.add(invBeam);
 	}
 
 }
