 package com.slauson.asteroid_dasher.powerups;
 
 import com.slauson.asteroid_dasher.game.Game;
 import com.slauson.asteroid_dasher.objects.Asteroid;
 import com.slauson.asteroid_dasher.status.Achievements;
 import com.slauson.asteroid_dasher.status.LocalStatistics;
 import com.slauson.asteroid_dasher.status.Upgrades;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.FloatMath;
 
 /**
  * Magnet powerup that attracts powerups
  * @author Josh Slauson
  *
  */
 public class PowerupMagnet extends ActivePowerup {
 	
 	// constants
 	private static final int DURATION_0 = 10000;
 	private static final int DURATION_1 = 15000;
 	private static final int DURATION_2 = 20000;
 	private static final int DURATION_3 = 30000;
 	
 	private static final float RANGE_PULL_FACTOR = 0.25f;
 	private static final float HOLD_RANGE_FACTOR = 1.5f;
 	
 	// "constants" (want to make sure Game.canvasWidth is initialized)
 	private static float rangePull = -1;
 	
 	private int direction;
 	private boolean hasSpin;
 	
 	public PowerupMagnet(Bitmap bitmap, float x, float y, int direction, int level) {
 		super(bitmap, x, y);
 		
 		this.direction = direction;
 		
 		if (rangePull < 0) {
 			rangePull = Game.canvasWidth*RANGE_PULL_FACTOR;
 		}
 		
 		// get duration
 		switch(level) {
 		case Upgrades.MAGNET_UPGRADE_INCREASED_DURATION_1:
 			activate(DURATION_1);
 			break;
 		case Upgrades.MAGNET_UPGRADE_INCREASED_DURATION_2:
 			activate(DURATION_2);
 			break;
 		case Upgrades.MAGNET_UPGRADE_INCREASED_DURATION_3:
 		case Upgrades.MAGNET_UPGRADE_SPIN:
 			activate(DURATION_3);
 			break;
 		default:
 			activate(DURATION_0);
 			break;
 		}
 		
 		// get spin
 		hasSpin = level >= Upgrades.MAGNET_UPGRADE_SPIN;
 	}
 	
 	/**
 	 * Alters asteroids direction based on distance from magnet
 	 * Also breaks up asteroid if too close
 	 * @param asteroid asteroid to modify
 	 */
 	public void alterAsteroid(Asteroid asteroid) {
 		
 		if (asteroid.getStatus() != Asteroid.STATUS_NORMAL) {
 			return;
 		}
 		
 		// get distance from asteroid
 		float distanceX = x - asteroid.getX();
 		float distanceY;
 
 		if (hasSpin) {
 			distanceY = y - asteroid.getY();
 		} else {
 			if (direction == Game.DIRECTION_NORMAL) {
 				distanceY = (y - height/2) - (asteroid.getY() + asteroid.getHeight()/2);
 			} else {
 				distanceY = (y + height/2) - (asteroid.getY() - asteroid.getHeight()/2);
 			}
 		}
 		
 		float absDistanceX = Math.abs(distanceX);
 		float absDistanceY = Math.abs(distanceY);
 
 		// don't pull asteroids past magnet if not spinning
 		if (!hasSpin && (direction == Game.DIRECTION_NORMAL && asteroid.getY() + asteroid.getHeight()/2 > y - height/2 ||
 				direction == Game.DIRECTION_REVERSE && asteroid.getY() - asteroid.getHeight()/2 < y + height/2)) {
 			return;
 		}
 				
 		float distance = FloatMath.sqrt((float)Math.pow(distanceX, 2) + (float)Math.pow(distanceY, 2));
 
 		// pull in asteroid
 		if (distance < rangePull) {
 			
 			float holdRange = HOLD_RANGE_FACTOR*height/2 + asteroid.getHeight()/2;
 			
 			// direction directly to asteroid
 			float dirX = distanceX/(absDistanceX + absDistanceY);
			float dirY = distanceY/(absDistanceX + absDistanceY);
			
			// take into account opposite direction
			if (Game.direction == Game.DIRECTION_REVERSE) {
				dirY *= -1;
			}
 			
 			// hold in place
 			if (distance < holdRange && asteroid.getStatus() != Asteroid.STATUS_HELD_IN_PLACE) {
 				asteroid.holdInPlace();
 				LocalStatistics.getInstance().asteroidsDestroyedByMagnet++;
 				numAffectedAsteroids++;
 			}
 			// otherwise pull in
 			else {
 			
 				float pullFactor = 1.f - (1.f*distance/rangePull);
 				
 				float asteroidDirX = (1 - pullFactor)*asteroid.getDirX() + (pullFactor)*dirX;
 				float asteroidDirY = (1 - pullFactor)*asteroid.getDirY() + (pullFactor)*dirY;
				
 				asteroid.setDirX(asteroidDirX);
 				asteroid.setDirY(asteroidDirY);
 			}
 		}
 	}
 	
 	@Override
 	public void draw(Canvas canvas, Paint paint) {
 		
 		// fade out
 		if (remainingDuration() < FADE_OUT_DURATION && Game.gameStatus == Game.STATUS_RUNNING) {
 			int alpha = (int)(255*(1.f*remainingDuration()/FADE_OUT_DURATION));
 			
 			if (alpha < 0) {
 				alpha = 0;
 			}
 			paint.setAlpha(alpha);
 		}
 
 		// rotate if needed
 		if (direction == Game.DIRECTION_REVERSE) {
 			canvas.save();
 			canvas.rotate(180, x, y);
 		}
 		
 		// rotate if spinning
 		if (hasSpin) {
 			canvas.save();
 			canvas.rotate(1.f*remainingDuration()/1000*360, x, y);
 		}
 		
 		canvas.drawBitmap(bitmap, x - width/2, y - height/2, paint);
 		
 		// unrotate
 		if (direction == Game.DIRECTION_REVERSE) {
 			canvas.restore();
 		}
 
 		if (hasSpin) {
 			canvas.restore();
 		}
 		
 		// restore alpha
 		if (remainingDuration() < FADE_OUT_DURATION && Game.gameStatus == Game.STATUS_RUNNING) {
 			paint.setAlpha(255);
 		}
 	}
 	
 	public void checkAchievements() {
 		if (numAffectedAsteroids >= Achievements.LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_1) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithMagnet1);
 		}
 		
 		if (numAffectedAsteroids >= Achievements.LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_2) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithMagnet2);
 		}
 		
 		if (numAffectedAsteroids >= Achievements.LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_3) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithMagnet3);
 		}
 	}
 }
