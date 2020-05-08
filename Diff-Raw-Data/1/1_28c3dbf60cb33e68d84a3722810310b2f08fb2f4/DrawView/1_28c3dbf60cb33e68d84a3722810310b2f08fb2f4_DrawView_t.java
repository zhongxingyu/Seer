 package com.innoraft.subhojitpaul.followme;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.Rect;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.WindowManager;
 
 public class DrawView extends SurfaceView implements SurfaceHolder.Callback {
 	GameThread thread;
 	private int xPosition;
 	private int yPosition;
 	private int screenWidth;
 	private int screenHeight;
 	private int statusBarHeight;
 	private boolean objectFingerMove;
 	
 	private long timeNow;
 	private long timePrevFrame = 0;
 	private long timeDelta;
 	
 	private Rect rectangle = new Rect();
 	private Paint paint = new Paint();
 	
 	public DrawView(Context context) {
 		super(context);
 		
 		paint.setAntiAlias(true);
 		paint.setColor(Color.RED);
 		
 		// Get screen dimensions.
 		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		screenWidth = display.getWidth();
 		screenHeight = display.getHeight();
 		
 		// Start thread.
 		getHolder().addCallback(this);
 		setFocusable(true);
 	}
 	
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		if (objectFingerMove) {
 			// Reset the canvas.
 			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
 			
 			// Draw image on canvas.
 			canvas.save();
 			// Image cannot go beyond top left corner.
 			if ((xPosition - 100 < 0) && (yPosition - 100 < 0)) {
 				rectangle.set((xPosition - 100 < 0) ? 0 : (xPosition - 100), (yPosition - 100 < 0) ? 0 : (yPosition - 100), (xPosition - 100 < 0) ? (xPosition + 100 + Math.abs(xPosition - 100)) : (xPosition + 100), (yPosition - 100 < 0) ? (yPosition + 100 + Math.abs(yPosition - 100)) : (yPosition + 100));
 			}
 			// Image cannot go beyond top right corner.
 			else if ((xPosition + 100 > screenWidth) && (yPosition - 100 < 0)) {
 				rectangle.set((xPosition + 100 > screenWidth) ? (screenWidth - 200) : (xPosition - 100), (yPosition - 100 < 0) ? 0 : (yPosition - 100), (xPosition + 100 > screenWidth) ? screenWidth : (xPosition + 100), (yPosition - 100 < 0) ? (yPosition + 100 + Math.abs(yPosition - 100)) : (yPosition + 100));
 			}
 			// Image cannot go beyond bottom left corner.
 			else if ((xPosition - 100 < 0) && (yPosition + 100 > (screenHeight - statusBarHeight))) {
 				rectangle.set((xPosition - 100 < 0) ? 0 : (xPosition - 100), (yPosition + 100 > (screenHeight - statusBarHeight)) ? (screenHeight - statusBarHeight - 200) : (yPosition - 100), (xPosition - 100 < 0) ? (xPosition + 100 + Math.abs(xPosition - 100)) : (xPosition + 100), (yPosition + 100 > (screenHeight - statusBarHeight)) ? (screenHeight - statusBarHeight) : (yPosition + 100));
 			}
 			// Image cannot go beyond bottom right corner.
 			else if ((xPosition + 100 > screenWidth) && (yPosition + 100 > (screenHeight - statusBarHeight))) {
 				rectangle.set((xPosition + 100 > screenWidth) ? (screenWidth - 200) : (xPosition - 100), (yPosition + 100 > (screenHeight - statusBarHeight)) ? (screenHeight - statusBarHeight - 200) : (yPosition - 100), (xPosition + 100 > screenWidth) ? screenWidth : (xPosition + 100), (yPosition + 100 > (screenHeight - statusBarHeight)) ? (screenHeight - statusBarHeight) : (yPosition + 100));
 			}
 			// Image cannot go further left.
 			else if (xPosition - 100 < 0) {
 				rectangle.set(0, yPosition - 100, xPosition + 100 + Math.abs(xPosition - 100), yPosition + 100);
 			}
 			// Image cannot go beyond further top.
 			else if (yPosition - 100 < 0) {
 				rectangle.set(xPosition - 100, 0, xPosition + 100, yPosition + 100 + Math.abs(yPosition - 100));
 			}
 			// Image cannot go beyond further right.
 			else if (xPosition + 100 > screenWidth) {
 				rectangle.set(screenWidth - 200, yPosition - 100, screenWidth, yPosition + 100);
 			}
 			// Image cannot go beyond further bottom.
 			else if (yPosition + 100 > (screenHeight - statusBarHeight)) {
 				rectangle.set(xPosition - 100, screenHeight - statusBarHeight - 200, xPosition + 100, screenHeight - statusBarHeight);
 			}
 			// User has touched the centre of screen.
 			else {
 				rectangle.set(xPosition - 100, yPosition - 100, xPosition + 100, yPosition + 100);
 			}
 			canvas.drawRect(rectangle, paint);
 			canvas.restore();
 		}
 	}
 	
 	@Override
 	public synchronized boolean onTouchEvent(MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			xPosition = (int) event.getX();
 			yPosition = (int) event.getY();
 			objectFingerMove = true;
 			break;
 		case MotionEvent.ACTION_MOVE:
 			xPosition = (int) event.getX();
 			yPosition = (int) event.getY();
 			break;
 		case MotionEvent.ACTION_UP:
 			break;
 		default:
 			return false;
 		}
 		
 		return true;
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		statusBarHeight = screenHeight - height;
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		thread = new GameThread(getHolder(), this);
         thread.setRunning(true);
         thread.start();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		boolean retry = true;
         thread.setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
             } catch (InterruptedException e) {
 
             }
         }
 	}
 	
 	class GameThread extends Thread {
 		private SurfaceHolder surfaceHolder;
 		private DrawView view;
 		private boolean run = false;
 		
 		public GameThread(SurfaceHolder surfaceHolder, DrawView view) {
 			this.surfaceHolder = surfaceHolder;
 			this.view = view;
 		}
 		
 		public void setRunning(boolean run) {
 			this.run = run;
 		}
 		
 		public SurfaceHolder getSurfaceHolder() {
 			return surfaceHolder;
 		}
 		
 		@Override
 		public void run() {
 			Canvas c;
 			while (run) {
 				c = null;
 				
 				// Limit frame rate to max 60fps.
 				timeNow = System.currentTimeMillis();
 				timeDelta = timeNow - timePrevFrame;
 				if ( timeDelta < 16) {
                     try {
                         Thread.sleep(16 - timeDelta);
                     } catch(InterruptedException e) {
 
                     }
                 }
 				timePrevFrame = System.currentTimeMillis();
 				
 				try {
                     c = surfaceHolder.lockCanvas(null);
                     synchronized (surfaceHolder) {
                        // Call methods to draw and process next fame
                         view.onDraw(c);
                     }
                 } finally {
                     if (c != null) {
                         surfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
 			}
 		}
 	}
 }
