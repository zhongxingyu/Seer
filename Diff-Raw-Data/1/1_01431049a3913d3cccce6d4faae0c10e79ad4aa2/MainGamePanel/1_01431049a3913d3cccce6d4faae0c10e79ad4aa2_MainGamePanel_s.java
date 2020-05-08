 package com.atlan1.mctpo.mobile;
 
 import com.atlan1.mctpo.mobile.Inventory.Inventory;
 import com.atlan1.mctpo.mobile.Inventory.Slot;
 
 import android.content.Context; 
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.graphics.*;
 
 public class MainGamePanel extends SurfaceView implements SurfaceHolder.Callback {
 	
 	private GameThread gThread;
 	
 	int mode = 0;
 	int rmode = 0;
 	
 	Paint p = new Paint();
 	
 	int frames;
 	int fps;
 	long countTime = System.nanoTime();
 	
 	int pointerBuildId = -1;
 	int pointerFingerId = -1;
 	
 	public MainGamePanel(Context context) {
 		super(context);
 		// adding the callback (this) to the surface holder to intercept events
 		getHolder().addCallback(this);
 		
 		gThread = new GameThread(getHolder(), this);
 		
 		setFocusable(true);
 		
 		p.setARGB(255, 255, 255, 255);
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		
 	}
 	
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Canvas canvas = null;
 		
 		try {
 			
 			// try locking the canvas for exclusive pixel editing on the surface
 			canvas = getHolder().lockCanvas();
 			
 			canvas.drawARGB(255, 0, 0, 0);
 			
 			canvas.drawText("Loading...", 30, 60, p);
 			
 			synchronized (this) {
 				// update game state 
 				// draws the canvas on the panel 
 				onDraw(canvas);
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			// in case of an exception the surface is not left in 
 			// an inconsistent state
 			if (canvas != null) {
 				getHolder().unlockCanvasAndPost(canvas);
 			}
 		}
 		
 		
 		if (gThread.getState() == Thread.State.TERMINATED) {
             gThread = new GameThread(getHolder(), this);
             gThread.setRunning(true);
             gThread.start();
         }
         else {
             gThread.setRunning(true);
             gThread.start();
         }
 
 		
 		/*gThread.setRunning(true);
 		Log.d("alive", String.valueOf(gThread.isAlive()));
 		if (!gThread.isAlive()) {
 			gThread.start();
 		}*/
 		
 	}
 
 	@Override 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		gThread.setRunning(false);
 		boolean retry = true;
 		while (retry) {
 			try {
 				gThread.join(); 
 				retry = false;
 			} catch (InterruptedException e) {
 				// try again shutting down the thread
 			} 
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 			synchronized (this) {
 				switch(event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					float eventX = event.getX();
 					float eventY = event.getY();
 					Slot[] slots = MCTPO.character.inventory.slots;
 					if ((eventX >= MCTPO.size.width - Character.modeButtonSize && eventX <= MCTPO.size.width && eventY >= 0 && eventY <= Character.modeButtonSize)) {
 						MCTPO.character.buildMode = MCTPO.character.buildMode.getNext();
 						return true;
 					}
 					
 					if (MCTPO.character.inventory.inflated) {
 						for (int i = 0; i < slots.length; i++) {
 							if (slots[i].contains(eventX, eventY)) {
 								MCTPO.character.inventory.selected = i;
 								return true;
 							}
 						}
 					}
 					
 					if (Inventory.inflateButtonRect.contains((int) eventX, (int) eventY)) {
 						MCTPO.character.inventory.inflated = !MCTPO.character.inventory.inflated;
 					}
 					
 						
 					if (Math.sqrt(Math.pow((this.getWidth() / 2 - eventX) / MCTPO.pixelSize, 2) + Math.pow((this.getHeight() / 2 - eventY) / MCTPO.pixelSize, 2)) > 60) {
 						MCTPO.fingerDownP.x = event.getX();
 						MCTPO.fingerDownP.y = event.getY();
 						MCTPO.fingerP.x = MCTPO.fingerDownP.x;
 						MCTPO.fingerP.y = MCTPO.fingerDownP.y;
 						//MCTPO.lastFingerP = MCTPO.fingerP;
 						MCTPO.fingerDown = true;
 						return true;
 					} else {
 						MCTPO.fingerBuildDownP.x = event.getX();
 						MCTPO.fingerBuildDownP.y = event.getY();
 						MCTPO.fingerBuildP.x = MCTPO.fingerBuildDownP.x;
 						MCTPO.fingerBuildP.y = MCTPO.fingerBuildDownP.y;
 						MCTPO.fingerBuildDown = true;
 						MCTPO.fingerBuildMoved = false;
 						return true;
 					}
 				case MotionEvent.ACTION_UP:
 					if (MCTPO.fingerDown) {
 						MCTPO.fingerDown = false;
 						MCTPO.fingerDownP.x = -1;
 						MCTPO.fingerDownP.y = -1;
 						//MCTPO.lastFingerP.x = -1;
 						//MCTPO.lastFingerP.y = -1;
 						MCTPO.fingerP.x = -1;
 						MCTPO.fingerP.y = -1;
 						return true;
 					} else if (MCTPO.fingerBuildDown) {
 						MCTPO.fingerBuildDown = false;
 						MCTPO.fingerBuildP.x = -1;
 						MCTPO.fingerBuildP.y = -1;
 						return true;
 					}
 				case MotionEvent.ACTION_MOVE:
 					if (MCTPO.fingerDown) {
 						//MCTPO.lastFingerP = MCTPO.fingerP;
 						MCTPO.fingerP.x = event.getX();
 						MCTPO.fingerP.y = event.getY();
 						return true;
 					} else if (MCTPO.fingerBuildDown) {
 						MCTPO.fingerBuildP.x = event.getX();
 						MCTPO.fingerBuildP.y = event.getY();
 						MCTPO.fingerBuildMoved = true;
 						return true;
 					}
 
 				}
 				/*try {
 					//this.wait(1000L);
 					Thread.sleep(15);
 				} catch (InterruptedException e) {
 				}*/
 			}
 			return true;
 	}
 
 	@Override 
 	protected void onDraw(Canvas canvas) {
 		
 		canvas.drawText("fps: " + fps, 30, 30, p);
 		
 		frames ++;
 		if (System.nanoTime() - countTime > 1000000000) {
 			fps = frames;
 			frames = 0;
 			countTime = System.nanoTime();
 			Log.d("fps", "fps: " + fps);
 		}
 	} 
 
 }
