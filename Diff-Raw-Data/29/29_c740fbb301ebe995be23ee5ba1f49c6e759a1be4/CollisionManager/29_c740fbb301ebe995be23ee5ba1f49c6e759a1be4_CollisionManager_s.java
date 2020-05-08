 package com.spaceinvaders;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 import android.graphics.Rect;
 
 public class CollisionManager {
 	
 	public static final CollisionManager INSTANCE = new CollisionManager();
	public boolean alive = true;
 	
 	public CollisionManager() {
 		
 	}
 	
 	public boolean collision() {
 		CopyOnWriteArrayList<Entity> entityArray		= EntityManager.INSTANCE.getArrayListEntity();
 		CopyOnWriteArrayList<LaserBeam> shipLasers		= EntityManager.INSTANCE.getArrayListShipLasers();
 		CopyOnWriteArrayList<LaserBeam> monsterLasers	= EntityManager.INSTANCE.getArrayListMonsterLasers();
 		
 		int monstersLeft = 0;
 		for (Entity entity : entityArray) {
			if (entity instanceof Monster) {
				monstersLeft++;
			}
 			if (entity instanceof Monster || entity instanceof Boss) {
 				for (LaserBeam laser : shipLasers) {
 					if (Rect.intersects(entity.getDestRect(), laser.getDestRect())) {
 						entity.collision();
 						laser.collision();
 						return true;
 					}
 				}
 			}
 			if (entity instanceof Ship) {
 				for (LaserBeam laser : monsterLasers) {
 					if (Rect.intersects(entity.getDestRect(), laser.getDestRect())) {
 						entity.collision();
 						laser.collision();
						alive = false;
 						return true;
 					}
 				}
 			}
 		}
		if (alive && monstersLeft == 0) {
 			Panel.makeMonsterRows(2);
 		}
		return false;	
 	}
 	
 }
