 package ch.expectusafterlun.androidtutorial;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class GFXSurface extends Activity implements OnTouchListener {
 
 	private AnimationViewSurface surfaceView;
 	/*
 	 * s means starting point
 	 * f means finishing point
 	 * d means change in
 	 * ani means animate
 	 */
 	private float x, y, sX, sY, fX, fY, dX, dY, aniX, aniY, scaledX, scaledY;
 	private Bitmap ball, plus;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		surfaceView = new AnimationViewSurface(this);
 		surfaceView.setOnTouchListener(this);
 		x = y = 0;
 		sX = sY = 0;
 		fX = fY = 0;
 		dX = dY = 0;
 		aniX = aniY = 0;
 
 		ball = BitmapFactory.decodeResource(getResources(),
 				R.drawable.greenball);
 		plus = BitmapFactory.decodeResource(getResources(),
 				R.drawable.greenplus);
 		setContentView(surfaceView);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		surfaceView.pause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		surfaceView.resume();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		x = event.getX();
 		y = event.getY();
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			sX = event.getX();
 			sY = event.getY();
 			// reset to 0
 			dX = dY = 0;
 			aniX = aniY = 0;
 			break;
 		case MotionEvent.ACTION_UP:
 			fX = event.getX();
 			fY = event.getY();
 			dX = fX - sX;
			dY = fY - sY;
 			scaledX = dX/30;
 			scaledY = dY/30;
 			// prevents the ball from being drawn on the plus
 			x = 0;
 			y = 0;
 			break;
 		}
 
 		return true;
 	}
 
 	public class AnimationViewSurface extends SurfaceView implements Runnable {
 
 		private SurfaceHolder holder;
 		private Thread thread;
 		private boolean isRunning;
 
 		public AnimationViewSurface(Context context) {
 			super(context);
 			holder = getHolder();
 			isRunning = false;
 		}
 
 		public void pause() {
 			isRunning = false;
 			try {
 				thread.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			thread = null;
 		}
 
 		public void resume() {
 			isRunning = true;
 			thread = new Thread(this);
 			thread.start();
 		}
 
 		@Override
 		public void run() {
 			while (isRunning) {
 				if (!holder.getSurface().isValid()) {
 					continue;
 				}
 
 				Canvas canvas = holder.lockCanvas();
 				canvas.drawRGB(2, 2, 150);
 				if (x != 0 && y != 0) {
 					canvas.drawBitmap(ball, x - (ball.getWidth() / 2), y
 							- (ball.getHeight() / 2), null);
 				}
 				if (sX != 0 && sY != 0) {
 					canvas.drawBitmap(plus, sX - (plus.getWidth() / 2), sY
 							- (plus.getHeight() / 2), null);
 				}
 				if (fX != 0 && fY != 0) {
 					canvas.drawBitmap(ball, fX - (ball.getWidth() / 2) - aniX, fY
 							- (ball.getHeight() / 2) - aniY, null);
 					canvas.drawBitmap(plus, fX - (plus.getWidth() / 2), fY
 							- (plus.getHeight() / 2), null);
 				}
 				aniX = aniX + scaledX;
 				aniY = aniY + scaledY;
 				
 				holder.unlockCanvasAndPost(canvas);
 			}
 		}
 	}
 }
