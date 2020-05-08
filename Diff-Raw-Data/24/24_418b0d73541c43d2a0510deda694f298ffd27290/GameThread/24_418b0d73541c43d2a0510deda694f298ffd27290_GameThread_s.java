 package dk.aau.cs.giraf.cars.gamecode;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.lang.System;
 
 import android.app.FragmentManager;
 
 import dk.aau.cs.giraf.cars.gamecode.GameObjects.*;
 
 
 public class GameThread extends Thread {
 	final int millisecondsPerTick = 25;
 	List<IWorkable> workableObjects;
 	List<ICollidable> collidableObjects;
 	Car car;
 	Boolean running;
 	private boolean mSettingsView = false;
 	public int numberOfClosedGarages;
 	private FragmentManager manager;
 	
 	public GameThread(List<GameObject> gameObjects) {
 		workableObjects = new ArrayList<IWorkable>();
 		collidableObjects = new ArrayList<ICollidable>();
 		
 		for (GameObject object : gameObjects) {
 			if (object instanceof IWorkable) {
 				workableObjects.add((IWorkable) object);
 			}
 			else if (object instanceof ICollidable) {
 				collidableObjects.add((ICollidable) object);
 			}
 			else if (object instanceof Car) {
 				car = (Car) object;
 			}
 		}
 	}
 	
 	public GameThread(List<GameObject> gameObjects, FragmentManager manager) {
 		workableObjects = new ArrayList<IWorkable>();
 		collidableObjects = new ArrayList<ICollidable>();
 		this.manager = manager;
 		
 		for (GameObject object : gameObjects) {
 			if (object instanceof IWorkable) {
 				workableObjects.add((IWorkable) object);
 			}
 			else if (object instanceof ICollidable) {
 				collidableObjects.add((ICollidable) object);
 			}
 			else if (object instanceof Car) {
 				car = (Car) object;
 			}
 		}
 	}
 	
 	public GameThread(Car car) {
 		this.car = car;
 		
 		mSettingsView = true;
 	}
 	
 	public void run() {
 		running = true;
 		if (!mSettingsView) {
 			gameLogic();
 		} else {
 			settingsLogic();
 		}
 	}
 	public void SetObjects(List<GameObject> gameObjects) {
 		workableObjects.clear();
 		
 		for (GameObject object : gameObjects) {
 			if (object instanceof Car) {
 				car = (Car) object;
 			}
 			if (object instanceof IWorkable) {
 				workableObjects.add((IWorkable) object);
 			}
 			if (object instanceof ICollidable) {
 				collidableObjects.add((ICollidable) object);
 			}
 
 		}
 	}
 	
 	
 	private void gameLogic() {
 		while(running){
 			
 			long currentTime = System.nanoTime();
 			numberOfClosedGarages = 0;
 			
 			for (IWorkable object : workableObjects) {
 				object.PerformWork();
 				
 				if (object instanceof Garage &&
 						((Garage)object).closed == true){
 					numberOfClosedGarages++;
 					
 					if (numberOfClosedGarages == 3){
 						car.resetPosition();
 						GameInfo.win=true;
 					}
 				}
 			}
 			for (ICollidable object : collidableObjects) {
 				if (car != null &&
 					car.CalculateCollisions(object.calculateCollisionBox())) { //�NDRE TIL PASSENDE FORM
 					if (object instanceof Garage) {
 						Garage garage = (Garage)object;
 						
 						if (garage.color == car.getColor()) {
 							garage.startClosing();
 							car.closeColor();
 						}
 						else {
							// TODO DISPLAY COLLISION
 						}
 						car.resetPosition();
 					}
 					else {
						// TODO DISPLAY COLLISION
 						car.resetPosition();
						// TODO evt. Gen. ny map
 						GameInfo.pause = true;
 						showDialog();
 					}
 				}
 			}
 		
 			long newTime = System.nanoTime();
 			long difference = newTime - currentTime;
 			int sleepFor = (int) ((millisecondsPerTick * 1000000 - difference) / 1000000);
 
 			try {
 				if (sleepFor > 0)
 				Thread.sleep(sleepFor);
 			} catch (InterruptedException e) {}
 		}
 	}
 	
 	private void settingsLogic() {
 		while(running){
 			long currentTime = System.nanoTime();
 			
 			car.PerformWork();
 			
 			long newTime = System.nanoTime();
 			long difference = newTime - currentTime;
 			int sleepFor = (int) ((millisecondsPerTick * 1000000 - difference) / 1000000);
 
 			try {
 				if (sleepFor > 0)
 				Thread.sleep(sleepFor);
 			} catch (InterruptedException e) {}
 		}
 	}
 	
 	
 	public void showDialog() {
 		CarCrashDialogFragment carCrash = new CarCrashDialogFragment();
 		
 		carCrash.setCancelable(false);
 		carCrash.show(manager, "crashdialog");
 	}
 	
 	public void stopRunning() {
 		running = false;
 	}
 	
 }
