 package com.medialab.humanbytes.model.gameObjects;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 
 import com.medialab.humanbytes.R;
 import com.medialab.humanbytes.model.managers.ActivityManager;
 import com.medialab.humanbytes.model.managers.DisplayManager;
 import com.medialab.humanbytes.model.managers.VibrationHelper;
 
 public class GameController {
 	
 	// ****************************************************************
 	// Attributes
 	// ****************************************************************
 	
 	private final static int DELAY_THREAD_UPDATE = 50;
 	private final static int DELAY_THREAD_NEW_FALLING_OBJECT = 1700;
	private final static int DELAY_THREAD_NEW_FALLING_OBJECT_MIN = 500;
 	
 	// ****************************************************************
 	// Attributes
 	// ****************************************************************
 	
 	private Vector<FallingObject> fallingObjects;
 	private Player player;
 	private int score;
 	private int lifes;
 	
 	private boolean threadsCanRun = true;
 	
 	// ****************************************************************
 	// Constructor
 	// ****************************************************************
 	
 	public GameController() {
 		
 		fallingObjects = new Vector<FallingObject>();
 		
 		score = 0;
 		lifes = 2;
 		
 	}
 	
 	// ****************************************************************
 	// Init methods
 	// ****************************************************************
 	
 	public void initThreadNewFallingObjects() {
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				
 				Activity activity = ActivityManager.getInstance().getActivity();
 				
 				int width = DisplayManager.getInstance().getWidth();
 				
 				Bitmap imageGood = BitmapFactory.decodeResource(activity.getResources(), R.drawable.tuercabuena);
 				Bitmap imageBad = BitmapFactory.decodeResource(activity.getResources(), R.drawable.tuercamala);
 				int imageWidth = imageGood.getWidth();
 				Random random = new Random();
				int delayTemp = DELAY_THREAD_NEW_FALLING_OBJECT;
 				
 				while (threadsCanRun) {
 					
 					// Set the limits, left limit right limit
 					float x = imageWidth / 2 + (float) (Math.random() * (width - imageWidth));
 					
 					if (random.nextInt(4) == 1) {
 						addFallingObject(new FallingObject(x, 0, imageBad, false));
 					}
 					else {
 						addFallingObject(new FallingObject(x, 0, imageGood, true));
 					}
 					
 					try {
 						// Poner este tiempo como variable
						if (delayTemp > DELAY_THREAD_NEW_FALLING_OBJECT_MIN) {
							delayTemp = DELAY_THREAD_NEW_FALLING_OBJECT - score * 20;
						}
						
						Thread.sleep(delayTemp);
 					}
 					catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				
 			}
 			
 		}).start();
 	}
 	
 	public void initThreadUpdateObjects() {
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				
 				int height = DisplayManager.getInstance().getHeight();
 				
 				ArrayList<FallingObject> objectsToRemove = new ArrayList<FallingObject>();
 				
 				while (threadsCanRun) {
 					
 					synchronized (fallingObjects) {
 						
 						Rect playerRect = player.getRectangle();
 						
 						for (FallingObject fallingObject : fallingObjects) {
 							fallingObject.move();
 							
 							// Check if is in the limit
 							if (fallingObject.getPositionY() > height) {
 								objectsToRemove.add(fallingObject);
 							}
 							// Check collitions
 							else if (Rect.intersects(playerRect, fallingObject.getRectangle())) {
 								// TODO: contar
 								objectsToRemove.add(fallingObject);
 								
 								if (fallingObject.isGood()) {
 									
 									score++;
 									
 								}
 								else {
 									
 									lifes--;
 									
 									VibrationHelper.vibrate(300);
 									
 								}
 								
 								if (lifes == 0) {
 									manageDead();
 								}
 							}
 							
 						}
 						
 						// Remove objects to remove
 						for (FallingObject fallingObject2 : objectsToRemove) {
 							fallingObjects.remove(fallingObject2);
 						}
 						
 					}
 					
 					// Cleaner the array of objects to remove
 					objectsToRemove.clear();
 					
 					try {
 						Thread.sleep(DELAY_THREAD_UPDATE);
 					}
 					catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				
 			}
 			
 		}).start();
 	}
 	
 	// ****************************************************************
 	// Player methods
 	// ****************************************************************
 	
 	public void createPlayer(Player player) {
 		this.player = player;
 	}
 	
 	public void movePlayer(float offsetX) {
 		player.moveX(offsetX);
 	}
 	
 	private void manageDead() {
 		
 		threadsCanRun = false;
 		
 		ActivityManager.getInstance().finish();
 		
 	}
 	
 	// ****************************************************************
 	// Falling objects methods
 	// ****************************************************************
 	
 	public void addFallingObject(FallingObject obj) {
 		synchronized (fallingObjects) {
 			fallingObjects.add(obj);
 		}
 	}
 	
 	public void removeFallingObject(FallingObject obj) {
 		synchronized (fallingObjects) {
 			fallingObjects.remove(obj);
 		}
 	}
 	
 	// ****************************************************************
 	// Accesor methods
 	// ****************************************************************
 	
 	public int getScore() {
 		return score;
 	}
 	
 	public int getLifes() {
 		return lifes;
 	}
 	
 	// ****************************************************************
 	// Draw methods
 	// ****************************************************************
 	public void drawObjects(Canvas canvas) {
 		
 		synchronized (fallingObjects) {
 			
 			for (FallingObject object : fallingObjects) {
 				object.draw(canvas);
 			}
 		}
 		
 		if (player != null) {
 			player.draw(canvas);
 		}
 		
 	}
 	
 }
