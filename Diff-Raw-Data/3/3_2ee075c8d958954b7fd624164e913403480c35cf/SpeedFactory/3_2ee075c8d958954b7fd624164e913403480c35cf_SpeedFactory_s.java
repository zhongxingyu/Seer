 package com.glowman.spaceunit.game;
 
 import com.glowman.spaceunit.game.mapObject.MovingSpaceObject;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.mapObject.SpaceObject;
 import com.glowman.spaceunit.game.mapObject.enemy.*;
 
 /**
  *
  */
 public class SpeedFactory {
 
 	public static float getSpeed(SpaceObject object, int gameType) {
 		float result = 0f;
 		if (object instanceof MovingSpaceObject) {
 			// ship speed
 			if (object instanceof Ship) {
 				result = 2.01f;
 			}
 			else if (object instanceof Enemy) {
 				Enemy enemy = (Enemy) object;
 				//basic space object (asteroids)
 				if (enemy.getEnemyType() == EnemyTypeENUM.ASTEROID) {
 					result = (float)Math.random() * 2f;
 				}
 				//mine
 				else if (enemy.getEnemyType() == EnemyTypeENUM.MINE) {
 					return 1f;
 				}
 				//alien
 				else if (enemy.getEnemyType() == EnemyTypeENUM.ALIEN) {
 					return 1.6f;
 				}
 			}
 		}
 
 		return result;
 	}
 }
