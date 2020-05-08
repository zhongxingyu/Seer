 package com.jand.bombercommander;
 
 import com.jand.bombercommander.GameObject;
 import com.jand.bombercommander.GameObject.GameObjectType;
 import java.util.ArrayList;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PixelFormat;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class PlayingFieldView extends SurfaceView implements
 		SurfaceHolder.Callback {
 	private static final String TAG = PlayingFieldView.class.getSimpleName();
 	private GameThread thread;
 
 	private ArrayList<GameObject> gameObjects;
 
 	public PlayingFieldView(Context context, AttributeSet attributeSet) {
 		super(context, attributeSet);
 
 		if (!isInEditMode()) {
 			this.setZOrderOnTop(true);
 			getHolder().setFormat(PixelFormat.TRANSPARENT);
 		}
 
 		getHolder().addCallback(this);
 
 		// Game object tests
 		gameObjects = new ArrayList<GameObject>();
 		gameObjects.add(new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_aa), GameObjectType.ANTIAIR, 50,
 				640));
 		gameObjects.add(new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_bomber), GameObjectType.BOMBER,
 				200, 640));
 		gameObjects.add(new GameObject(BitmapFactory.decodeResource(
 				getResources(), R.drawable.bc_fighter), GameObjectType.FIGHTER,
 				350, 640));
 
 		thread = new GameThread(getHolder(), this);
 		setFocusable(true);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Matrix reverse = new Matrix();
 		reverse.postRotate(180);
 
 		thread.setRunning(true);
 		thread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;
 
 		Log.d(TAG, "Surface is being destroyed");
 		while (retry) {
 			try {
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				Log.d(VIEW_LOG_TAG, e.getMessage());
 			}
 		}
 		Log.d(TAG, "Thread was shut down cleanly");
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			boolean objectFound = false;
 			for (GameObject obj : gameObjects) {
 				// only tries to set A SINGLE object to a motion event
 				if (!objectFound) {
 					objectFound = obj.handleActionDown((int) event.getX(),
 							(int) event.getY());
 				}
 
 			}
 
 			if (event.getY() > getHeight() - 100) {
 				thread.setRunning(false);
 				((Activity) getContext()).finish();
 			} else
 				Log.d(VIEW_LOG_TAG, "Coords: x = " + event.getX() + ", y = "
 						+ event.getY());
 		}
 
 		if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			for (GameObject obj : gameObjects) {
 				if (obj.getIsTouched()) {
 					obj.setX((int) event.getX() - obj.getBitmap().getWidth()
 							/ 2);
 					obj.setY((int) event.getY() - obj.getBitmap().getHeight()
 							/ 2);
 				}
 			}
 		}
 
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			// Loops through game objects and places them on the top of the
 			// screen in a box
 			// first it sets the lane for the object, and prevents the object
 			// from stacking on an occupied lane
 			// then it sets the object's x,y coordinates onto the screen
 			for (GameObject obj : gameObjects) {
 				if (obj.getIsTouched())
 					obj.setIsTouched(false);
 
				if (obj.getY() <= 100) {
					if (obj.getX() >= 144 * 4) {
 						// obj.setX(144 * 4);
 						obj.setLane(4);
 
					} else if (obj.getX() >= 144 * 3 && obj.getX() <= 144 * 4) {
 						// obj.setX(144*3);
 						// obj.setY(0);
 						obj.setLane(3);
					} else if (obj.getX() >= 144 * 2 && obj.getX() <= 144 * 3) {
 						// obj.setX(144 * 2);
 						// obj.setY(0);
 						obj.setLane(2);
					} else if (obj.getX() >= 144 && obj.getX() <= 144 * 2) {
 						// obj.setX(144);
 						// obj.setY(0);
 						obj.setLane(1);
 					} else {
 						// obj.setX(0);
 						// obj.setY(0);
 						obj.setLane(0);
 					}
 				} else {
 					obj.setLane(-1);
 				}
 				for (GameObject otherObject : gameObjects) {
 					if (obj.getLane() == otherObject.getLane()
 							&& !obj.equals(otherObject)) {
 						obj.setLane(-1);
 					}
 				}
 				if (obj.getLane() == -1) {
 					int xOffset = 0;
 					switch (obj.getType()) {
 					case BOMBER:
 						xOffset = -100;
 						break;
 					case FIGHTER:
 						xOffset = 0;
 						break;
 					case ANTIAIR:
 						xOffset = 100;
 						break;
 					}
 					obj.setX(360 + xOffset);
 					obj.setY(640);
 				} else {
 					obj.setY(0);
 					obj.setX(obj.getLane() * 144);
 				}
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onDraw(Canvas c) {
 		if (!isInEditMode()) {
 			c.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
 
 			for (GameObject obj : gameObjects) {
 				obj.draw(c);
 			}
 		}
 	}
 }
