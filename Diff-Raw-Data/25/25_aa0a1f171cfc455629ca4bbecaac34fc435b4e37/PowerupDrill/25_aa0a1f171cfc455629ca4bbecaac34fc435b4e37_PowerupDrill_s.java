 package com.slauson.dasher.powerups;
 
 import com.slauson.dasher.game.MyGameView;
 import com.slauson.dasher.objects.Asteroid;
 import com.slauson.dasher.status.Achievements;
 import com.slauson.dasher.status.Upgrades;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.FloatMath;
 
 /**
  * Drill powerup that splits asteroids in half
  * @author Josh Slauson
  *
  */
 public class PowerupDrill extends ActivePowerup {
 
 	// constants
 	private static final float SPEED_FACTOR = 0.25f;
 	private static final float MAX_DIR_CHANGE = 0.05f;
 	private static final int TELEPORT_DURATION = 500;
 	private static final int NUM_FRAMES_LOOK_AHEAD = 10;
 	
 	private static final float MAX_X_DIR_1 = 0.2f;
 	private static final float MAX_X_DIR_2 = 0.3f;
 	private static final float MAX_X_DIR_3 = 0.5f;
 	
 	private static final int DURATION = 10000;
 	
 	private int direction;
 	private RectF rectDest;
 	
 	private Asteroid nextAsteroid;
 	private float nextDistance;
 	
 	private int teleportDuration;
 
 	// upgrades
 	private boolean hasSeek;
 	private float maxXDir;
 	private boolean hasTeleport;
 		
 	public PowerupDrill(Bitmap bitmap, float x, float y, int direction, int level) {
 		super(bitmap, x, y);
 		
 		this.direction = direction;
 		
 		nextAsteroid = null;
 		nextDistance = 0;
 
 		dirY = 1;
 		dirX = 0;
 
 		speed = MyGameView.canvasHeight*SPEED_FACTOR;
 
 		rectDest = new RectF(-width/2, -height/2, width/2, height/2);
 		
 		// calculate maximum direction change
 		switch(level) {
 		case Upgrades.DRILL_UPGRADE_SEEK_1:
 			maxXDir = MAX_X_DIR_1;
 			break;
 		case Upgrades.DRILL_UPGRADE_SEEK_2:
 			maxXDir = MAX_X_DIR_2;
 			break;
 		case Upgrades.DRILL_UPGRADE_SEEK_3:
 		case Upgrades.DRILL_UPGRADE_TELEPORT:
 			maxXDir = MAX_X_DIR_3;
 			break;
 		default:
 			maxXDir = 0;
 			break;
 		}
 		
 		hasSeek = level >= Upgrades.DRILL_UPGRADE_SEEK_1;
 		hasTeleport = level >= Upgrades.DRILL_UPGRADE_TELEPORT;
 		teleportDuration = 0;
 		
 		activate(DURATION);
 	}
 
 	@Override
 	public void alterAsteroid(Asteroid asteroid) {
 
		// check if drill can seek
		if (!hasSeek) {
			return;
		}
		
 		// ignore asteroids past drill, off screen, or non normal/held in place
 		if ((asteroid.getStatus() != Asteroid.STATUS_NORMAL && asteroid.getStatus() != Asteroid.STATUS_HELD_IN_PLACE) ||
 				!asteroid.onScreen() ||
 				direction == MyGameView.DIRECTION_NORMAL && asteroid.getY() - asteroid.getHeight()/2 >= y + height/2 ||
 				direction == MyGameView.DIRECTION_REVERSE && asteroid.getY() + asteroid.getHeight()/2 <= y - height/2)
 		{
 			// reset next asteroid
 			if (asteroid.equals(nextAsteroid)) {
 				nextAsteroid = null;
 			}
 
 			return;
 		}
 		
 		// check if drill split up asteroid
 		if (checkBoxCollision(asteroid)) {
 			asteroid.splitUp();
 			nextAsteroid = null;
 			
 			if (asteroid.getStatus() == Asteroid.STATUS_NORMAL) {
 				numAffectedAsteroids++;
 			}
 		}
 		
 		// get weighted distance
 		float distanceX = Math.abs(x - asteroid.getX());
 		float distanceY = Math.abs(y - asteroid.getY());
 		
 		// cone check (we only want to look at asteroids in narrow cone above asteroid based on maximum x direction)
 		if ((1.5f/maxXDir)*distanceX > distanceY) {
 			return;
 		}
 		
 		float distance = FloatMath.sqrt((float)Math.pow(distanceX, 2) + (float)Math.pow(distanceY, 2));
 		
 		// update distance for next asteroid
 		if (asteroid.equals(nextAsteroid)) {
 			nextDistance = distance;
 		}
 		// otherwise check if its closer
 		else if (distance < nextDistance || nextAsteroid == null) {
 			nextDistance = distance;
 			nextAsteroid = asteroid;
 		}
 	}
 	
 	@Override
 	public void update(float speedModifier) {
 		
 		long timeElapsed = getElapsedTime();
 		float timeModifier = 1.f*timeElapsed/1000;
 
 		if (teleportDuration > 0) {
 			teleportDuration -= timeElapsed;
 			
 			// reset drill position
 			if (hasTeleport && teleportDuration <= TELEPORT_DURATION/2) {
 
 				// pick random x coordinate
 				x = width/2 + (MyGameView.canvasWidth - width)*MyGameView.random.nextFloat();
 				
 				// position drill back on opposite side
 				if (direction == MyGameView.DIRECTION_NORMAL) {
 					y = MyGameView.canvasHeight - height/2; 
 				} else {
 					y = height/2;
 				}
 				
 				hasTeleport = false;
 			}
 			return;
 		}
 		
 		// update direction based on next asteroid
 		if (nextAsteroid != null) {
 			float distanceX = nextAsteroid.getX() - x;
 			float distanceY = nextAsteroid.getNextY(speedModifier, timeModifier*NUM_FRAMES_LOOK_AHEAD) - y;
 			
 			float distanceAbsoluteValue = Math.abs(distanceX) + Math.abs(distanceY);
 			float actualDirX = distanceX/(distanceAbsoluteValue)/2;
 			
 			// slowly change direction
 			float dirChangeX = actualDirX - dirX;
 			if (Math.abs(dirChangeX) < MAX_DIR_CHANGE) {
 				dirX = actualDirX;
 			} else if (dirChangeX > 0) {
 				dirX += MAX_DIR_CHANGE;
 			} else {
 				dirX -= MAX_DIR_CHANGE;
 			}
 			
 			// check bounds
 			if (dirX > maxXDir) {
 				dirX = maxXDir;
 			}
 			if (dirX < -maxXDir) {
 				dirX = -maxXDir;
 			}
 			
 			dirY = 1.0f - Math.abs(dirX);
 		}
 		
 		if (direction == MyGameView.DIRECTION_NORMAL) {
 			y -= dirY*speed*timeModifier*speedModifier;
 		} else {
 			y += dirY*speed*timeModifier*speedModifier;
 		}
 		
 		x += dirX*speed*timeModifier*speedModifier;
 	}
 	
 	@Override
 	public void draw(Canvas canvas, Paint paint) {
 		
 		// fade out
 		if (remainingDuration() < FADE_OUT_DURATION && MyGameView.gameMode == MyGameView.MODE_RUNNING) {
 			int alpha = (int)(255*(1.f*remainingDuration()/FADE_OUT_DURATION));
 			
 			if (alpha < 0) {
 				alpha = 0;
 			}
 			paint.setAlpha(alpha);
 		}
 
 		// translate to drill's position
 		canvas.save();
 		canvas.translate(x, y);
 
 		// rotate if needed
 		if (direction == MyGameView.DIRECTION_REVERSE) {
 			canvas.save();
 			canvas.rotate(180, 0, 0);
 		}
 		
 		// rotate based on direction
 		if (direction == MyGameView.DIRECTION_NORMAL) {
 			canvas.rotate(90*dirX, 0, 0);
 		} else {
 			canvas.rotate(-90*dirX, 0, 0);
 		}
 
 		// scale if teleporting
 		if (teleportDuration > 0) {
 			
 			float factor = Math.abs(TELEPORT_DURATION/2 - teleportDuration)/(1.f*TELEPORT_DURATION/2);
 			
 			rectDest.set(-width/2*factor, -height/2*factor, width/2*factor, height/2*factor);
 			
 			canvas.drawBitmap(bitmap, null, rectDest, paint);
 		} else {
 			canvas.drawBitmap(bitmap, -width/2, - height/2, paint);
 		}
 		
 		// unrotate
 		if (direction == MyGameView.DIRECTION_REVERSE) {
 			canvas.restore();
 		}
 		
 		canvas.restore();
 		
 		// restore alpha
 		if (remainingDuration() < FADE_OUT_DURATION && MyGameView.gameMode == MyGameView.MODE_RUNNING) {
 			paint.setAlpha(255);
 		}
 	}
 	
 	public void switchDirection() {
 		if (direction == MyGameView.DIRECTION_NORMAL) {
 			direction = MyGameView.DIRECTION_REVERSE;
 		} else {
 			direction = MyGameView.DIRECTION_NORMAL;
 		}
 	}
 	
 	@Override
 	public boolean isActive() {
 		
 		// check time component
 		if (!super.isActive()) {
 			
 			// drill use max time achievement
 			Achievements.unlockLocalAchievement(Achievements.localDrillUseMaximumTime);
 			
 			return false;
 		}
 		
 		if (teleportDuration > 0) {
 			return true;
 		}
 		
 		if (direction == MyGameView.DIRECTION_NORMAL) {
 			if (hasTeleport) {
 				return y - height/2 > 0;
 			} else {
 				return y + height/2 > 0;
 			}
 		} else {
 			if (hasTeleport) {
 				return y + height/2 < MyGameView.canvasHeight;
 			} else {
 				return y - height/2 < MyGameView.canvasHeight;
 			}
 		}
 	}
 	
 	/**
 	 * Teleports drill to other side of screen at random x position
 	 */
 	public void teleport() {
 		
 		nextAsteroid = null;
 		nextDistance = 0;
 
 		teleportDuration = TELEPORT_DURATION;
 	}
 	
 	/**
 	 * Checks local drill-related achievements
 	 */
 	@Override
 	public void checkAchievements() {
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_1) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithDrill1);
 		}
 		
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_2) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithDrill2);
 		}
 		
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_3) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithDrill3);
 		}
 	}
 	
 	/**
 	 * Returns true if drill has teleport ability
 	 * @return true if drill has teleport ability
 	 */
 	public boolean hasTeleport() {
 		return hasTeleport;
 	}
 
 }
