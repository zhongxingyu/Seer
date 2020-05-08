 package com.spaceinvaders;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 import android.graphics.Rect;
 
 public class CollisionManager {
 	
 	public static final CollisionManager INSTANCE = new CollisionManager();
	private Ship ship;
 	
 	public CollisionManager() {
 		
 	}
 	
 	public boolean collision() {
 		CopyOnWriteArrayList<Entity> entityArray		= EntityManager.INSTANCE.getArrayListEntity();
 		CopyOnWriteArrayList<LaserBeam> shipLasers		= EntityManager.INSTANCE.getArrayListShipLasers();
 		CopyOnWriteArrayList<LaserBeam> monsterLasers	= EntityManager.INSTANCE.getArrayListMonsterLasers();
 		
 		int monstersLeft = 0;
 		for (Entity entity : entityArray) {
 			if (entity instanceof Monster || entity instanceof Boss) {
 				for (LaserBeam laser : shipLasers) {
 					if (Rect.intersects(entity.getDestRect(), laser.getDestRect())) {
 						entity.collision();
 						laser.collision();
 						return true;
 					}
 				}
				monstersLeft++;
 			}
 			if (entity instanceof Ship) {
 				for (LaserBeam laser : monsterLasers) {
 					if (Rect.intersects(entity.getDestRect(), laser.getDestRect())) {
 						entity.collision();
 						laser.collision();
 						return true;
 					}
 				}
				ship = (Ship) entity;
 			}
 		}
		if (entityArray.contains(ship) && monstersLeft == 0) {
 			Panel.makeMonsterRows(2);
 		}
		return false;
 	}
 	
 }
