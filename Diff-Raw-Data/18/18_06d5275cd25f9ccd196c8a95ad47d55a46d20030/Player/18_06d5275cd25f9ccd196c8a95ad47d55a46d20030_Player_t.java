 package com.slauson.dasher.objects;
 
 import java.util.ArrayList;
 
 import com.slauson.dasher.game.MyGameView;
 import com.slauson.dasher.status.Achievements;
 import com.slauson.dasher.status.Configuration;
 import com.slauson.dasher.status.LocalStatistics;
 import com.slauson.dasher.status.Upgrades;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Paint.Style;
 import android.graphics.RectF;
 
 /**
  * Player ship
  * @author Josh Slauson
  *
  */
 public class Player extends DrawObject {
 	
 	// player state
 	private long startTime;
 	
 	private float goX;
 	
 	private float speedX, speedY;
 	
 	private int move;
 	private boolean inPosition;
 	private int direction;
 	private float maxSpeed;
 	
 	private RectF dashPercentRect;
 	private RectF dashPercentRectSmall;
 	private int dashTimeout;
 	private int dashNumAffectedAsteroids;
 	private int dashNumAffectedAsteroidsHeldInPlace;
 	
 	private int invulnerabilityCounter;
 
 	// dash upgrades
 	private int dashRechargeDuration;
 	private boolean dashMultipleDrops;
 	
 	private float yBottom, yTop;
 	private int size;
 	private int rotationDegrees;
 	
 	private boolean moveByTouch;
 	
 	/**
 	 * Private constants
 	 */
 	
 	// button movement stuff
 	private static final int MOVE_NONE = 0;
 	private static final int MOVE_LEFT = 1;
 	private static final int MOVE_RIGHT = 2;
 	private static final int MOVE_ANY = 3;
 	private static final float BUTTON_SPEED_INCREASE_FACTOR = 0.05f;
 	private static final float BUTTON_MIN_SPEED_FACTOR = 0.25f;
 	private static final float MAX_SPEED_FACTOR = 0.5f;
 	
 	// size stuff
 	private static final float SIZE_FACTOR = 0.067f;
 	private static final float REAR_OFFSET_FACTOR = 0.25f;
 	private static final int INVULNERABILITY_DURATION = 5000;
 	
 	// dash recharge
 	private static final int DASH_RECHARGE_DURATION_0 = 20000;
 	private static final int DASH_RECHARGE_DURATION_1 = 17500;
 	private static final int DASH_RECHARGE_DURATION_2 = 15000;
 	private static final int DASH_RECHARGE_DURATION_3 = 10000;
 	
 	// breakup duration
 	private static final int BREAKING_UP_DURATION = 3000;
 	private static final float BREAKING_UP_MOVE = 20;
 
 	
 	public Player(boolean moveByTouch) {
 		// set y, width, height later 
 		super(MyGameView.canvasWidth/2, 0, 0, 0);
 
 		this.moveByTouch = moveByTouch;
 		
 		// set y offset
 		yBottom = MyGameView.canvasHeight - MyGameView.canvasHeight*Configuration.offsetHeight;
 		y = yBottom;
 		
 		// set height/width
 		size = getRelativeWidthSize(SIZE_FACTOR);
 		width = size;
 		height = size;
 		yTop = size;
 		
 		// set speed
 		maxSpeed = getRelativeWidthSize(MAX_SPEED_FACTOR);
 		
 		goX = x;
 		
 		speedX = 0;
 		speedY = 0;
 		
 		move = MOVE_NONE;
 		inPosition = true;
 		direction = MyGameView.DIRECTION_NORMAL;
 		
 		points = new float[] {
 				-width/2, height/2,
 				0, -height/2,
 				0, -height/2,
 				width/2, height/2,
 				width/2, height/2,
 				0, height/2-height*REAR_OFFSET_FACTOR,
 				0, height/2-height*REAR_OFFSET_FACTOR,
 				-width/2, height/2
 		};
 		
 		rectDest = new RectF(-size/4, -size/4, size/4, size/4);
 		rectSrc = new Rect(0, 0, size, size);
 		
 		dashTimeout = 0;
 		dashPercentRect = new RectF(-width/8, -height/16, width/8, 3*height/16);
 		dashPercentRectSmall = new RectF(-width/16, -height/32, width/16, 3*height/32);
 		dashNumAffectedAsteroids = 0;
 		dashNumAffectedAsteroidsHeldInPlace = 0;
 		
 		lineSegments = new ArrayList<LineSegment>();
 		
 		for(int i = 0; i < points.length; i+=4) {
 			lineSegments.add(new LineSegment(0, 0, 0, 0));
 		}
 		
 		// calculate rotation degrees (use 180 +- 360 of twice the canvas height) 
 		rotationDegrees = MyGameView.canvasHeight*2 - ((MyGameView.canvasHeight*2-180)%360);
 		
 		startTime = System.currentTimeMillis();
 		
 		invulnerabilityCounter = 0;
 		status = STATUS_INVULNERABILITY;
 		timeCounter = INVULNERABILITY_DURATION;
 		
 		// calculate dash recharge duration
 		switch (Upgrades.dashUpgrade.getLevel()) {
 		case Upgrades.DASH_UPGRADE_REDUCED_RECHARGE_1:
 			dashRechargeDuration = DASH_RECHARGE_DURATION_1;
 			break;
 		case Upgrades.DASH_UPGRADE_REDUCED_RECHARGE_2:
 			dashRechargeDuration = DASH_RECHARGE_DURATION_2;
 			break;
 		case Upgrades.DASH_UPGRADE_REDUCED_RECHARGE_3:
 		case Upgrades.DASH_UPGRADE_MULTIPLE_POWERUPS:
 			dashRechargeDuration = DASH_RECHARGE_DURATION_3;
 			break;
 		default:
 			dashRechargeDuration = DASH_RECHARGE_DURATION_0;
 			break;
 		}
 		
 		// determine if multiple drops are enabled for dash
 		dashMultipleDrops = Upgrades.dashUpgrade.getLevel() >= Upgrades.DASH_UPGRADE_MULTIPLE_POWERUPS;
 		
 		bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
 		drawPointsToBitmap();
 	}
 	
 	public void setMoveByTouch(boolean moveByTouch) {
 		this.moveByTouch = moveByTouch;
 	}
 	
 	/**
 	 * Resets start time
 	 */
 	public void reset() {
 		LocalStatistics.getInstance().timePlayed = (int) ((System.currentTimeMillis() - startTime)/1000);
 	}
 	
 	@Override
 	public void draw(Canvas canvas, Paint paint) {
 
 		if (status != STATUS_INVISIBLE) {
 			
 			canvas.save();
 			
 			// check if we need to rotate
 			if (speedY > 1 || direction == MyGameView.DIRECTION_REVERSE) {
 				float degrees = rotationDegrees * (yBottom - y) / (yBottom - yTop);			
 				canvas.rotate(degrees, x, y);
 			}
 
 			// normal or invulnerability blink
 			if ((status == STATUS_NORMAL && !MyGameView.powerupInvulnerability.isActive()) || (status == STATUS_INVULNERABILITY && invulnerabilityCounter % 4 < 2) || (MyGameView.powerupInvulnerability.isActive() && MyGameView.powerupInvulnerability.getCounter() % 4 < 2)) {
 				
				canvas.translate(x, y);
 				
 				// if small powerup is active, draw resized bitmap
 				if (MyGameView.powerupSmall.isActive() && !(!inPosition && MyGameView.powerupSmall.isBigDash())) {
 					canvas.drawBitmap(bitmap, rectSrc, rectDest, paint);
 				}
 				// draw normal bitmap
 				else {
					canvas.drawBitmap(bitmap, -width/2, -height/2, paint);
 				}
 				
 				// draw dash timeout percentage
 				paint.setStyle(Style.FILL_AND_STROKE);
 				
 				float dashPercentage = 1.f - 1.f*dashTimeout/dashRechargeDuration;
 				dashPercentage = (float)(dashPercentage - (dashPercentage % .25));
 				
				if (dashPercentage > 0) {
					if (MyGameView.powerupSmall.isActive() && !(!inPosition && MyGameView.powerupSmall.isBigDash())) {
						canvas.drawArc(dashPercentRectSmall, -90, 360*dashPercentage, true, paint);
					} else {
						canvas.drawArc(dashPercentRect, -90, 360*dashPercentage, true, paint);
					}
 				}
 			}
 			// breaking up
 			else if (status == STATUS_BREAKING_UP) {
 				paint.setAlpha((int)(255 * (1.0*timeCounter/BREAKING_UP_DURATION)));
 				canvas.translate(x, y);
 				
 				for (LineSegment lineSegment : lineSegments) {
 					lineSegment.draw(canvas, paint);
 				}
 				
 				paint.setAlpha(255);
 			}
 			
 			canvas.restore();
 		}
 	}
 
 	@Override
 	public void update() {
 		
 		long timeElapsed = getElapsedTime();
 		float timeModifier = 1.f*timeElapsed/1000;
 
 		if (timeCounter > 0) {
 			timeCounter -= timeElapsed;
 		}
 		
 		if (status == STATUS_NORMAL || status == STATUS_INVULNERABILITY) {
 		
 			// touch based controls
 			if (moveByTouch) {
 				// damn floating point arithmetic
 				if (Math.abs(goX - x) > 1) {
 					speedX = maxSpeed;
 	
 					// need to move right
 					if (goX > x) {
 						dirX = 1;
 					}
 					// need to move left
 					else {
 						dirX = -1;
 					}
 				} else {
 					speedX = 0;
 				}
 			}
 			// key/accelerometer based controls
 			else {
 				if (move != MOVE_NONE) {
 					if (speedX < maxSpeed) {
 						speedX += BUTTON_SPEED_INCREASE_FACTOR*maxSpeed;
 						
 						if (speedX > maxSpeed) {
 							speedX = maxSpeed;
 						}
 					}
 				}
 			}
 			
 			x = x + (dirX*speedX*timeModifier);
 			
 			// autocorrect position if we overshoot
 			if (moveByTouch && ((dirX > 0 && x > goX) || (dirX < 0 && x < goX))) {
 				x = goX;
 			}
 			
 			speedY = 0;
 			dirY = 0;
 			
 			// move from bottom to top
 			if (!inPosition) {
 				if (direction == MyGameView.DIRECTION_REVERSE && Math.abs(y - yTop) > 0) {
 					dirY = -1;
 					speedY = maxSpeed;
 				}
 				// move from top to bottom
 				else if (direction == MyGameView.DIRECTION_NORMAL && Math.abs(y - yBottom) > 0) {
 					dirY = 1;	
 					speedY = maxSpeed;
 				}
 				
 				y = y + (dirY*speedY*timeModifier);
 				
 				// autocorrect position if we overshoot
 				if (dirY > 0 && y >= yBottom) {
 					y = yBottom;
 					inPosition = true;
 					checkAchievements();
 				} else if (dirY < 0 && y <= yTop) {
 					y = yTop;
 					inPosition = true;
 					checkAchievements();
 				}
 			}
 			
 			// update dash timeout
 			if (dashTimeout > 0) {
 				dashTimeout-=timeElapsed;
 			}
 
 			// update invulnerability timer
 			if (status == STATUS_INVULNERABILITY) {
 				invulnerabilityCounter++;
 
 				// make ship invulnerability for short period
 				if (timeCounter <= 0) {
 					status = STATUS_NORMAL;
 				}
 			}
 
 		}
 		else if (status == STATUS_BREAKING_UP) {
 			
 			for (LineSegment lineSegment : lineSegments) {
 				lineSegment.update(timeModifier);
 			}
 			
 			// make ship invulnerability for short period
 			if (timeCounter <= 0) {
 				//System.out.println("INVULNERABILITY");
 				status = STATUS_INVULNERABILITY;
 				timeCounter = INVULNERABILITY_DURATION;
 			}
 		} 
 	}
 	
 	/**
 	 * Breaks up ship into line segments
 	 */
 	public void breakup() {
 		
 		float modifier = 1f;
 		
 		if (MyGameView.powerupSmall.isActive()) {
 			modifier = 0.5f;
 		}
 		
 		LineSegment lineSegment;
 		
 		for (int i = 0; i < points.length; i+=4) {
 			
 			lineSegment = lineSegments.get(i/4);
 			
 			lineSegment.x1 = modifier*points[i];
 			lineSegment.y1 = modifier*points[i+1];
 			lineSegment.x2 = modifier*points[i+2];
 			lineSegment.y2 = modifier*points[i+3];
 			
 			// need to get perpendicular (these are opposite on purpose)
 			float yDiff = Math.abs(points[i] - points[i+2]);
 			float xDiff = Math.abs(points[i+1] - points[i+3]);
 			
 			yDiff += Math.abs(dirY)*speed;
 			xDiff += Math.abs(dirX)*speed;
 			
 			lineSegment.move = BREAKING_UP_MOVE;
 			
 			// x direction is sometimes positive
 			if (points[i] < 0 || points[i+2] < 0) {
 				lineSegment.dirX = (-xDiff/(xDiff + yDiff));
 			} else {
 				lineSegment.dirX = (xDiff/(xDiff + yDiff));
 			}
 			
 			// y direction is always positive
 			lineSegment.dirY = (yDiff/(xDiff + yDiff)) + 1;
 			
 			// reverse direction
 			if (direction == MyGameView.DIRECTION_REVERSE) {
 				lineSegment.dirY *= -1;
 			}
 		}
 		
 		status = STATUS_BREAKING_UP;
 		timeCounter = BREAKING_UP_DURATION;
 		
 		// don't count this as a dash kill
 		LocalStatistics.getInstance().asteroidsDestroyedByDash--;
 		
 		reset();
 	}
 
 	/**
 	 * Returns start time of player
 	 * @return start time of player
 	 */
 	public long getStartTime() {
 		return startTime;
 	}
 	
 	/**
 	 * Adds the given amount of time to the start time (used for pause menu)
 	 * @param milliseconds number of milliseconds to add
 	 */
 	public void addToStartTime(long milliseconds) {
 		startTime += milliseconds;
 	}
 
 	/**
 	 * Sets player's move x coordinate
 	 * @param goX x coordinate player should move to
 	 */
 	public void setGoX(float goX) {
 		this.goX = goX;
 	}
 	
 	@Override
 	public boolean checkBoxCollision(Item other) {
 		if (MyGameView.powerupSmall.isActive() && !(!inPosition && MyGameView.powerupSmall.isBigDash())) {
 			return Math.abs(x - other.x) <= width/8 + other.width/4 && Math.abs(y - other.y) <= width/8 + other.height/4;
 		} else {
 			return Math.abs(x - other.x) <= width/2 + other.height/2 && Math.abs(y - other.y) <= height/2 + other.height/2;
 		}
 	}
 	
 	/**
 	 * Checks if given point is contained in a bounding box for the player
 	 * defined by the given width and height factors and y offset
 	 * 
 	 * @param widthFactor factor to base the bounding box's width compared to the ship's width/2 (0-1)
 	 * @param heightFactor factor to base the bounding box's height compared to the ship's height/2 (0-1)
 	 * @param yOffset offset for lower y of bounding box
 	 * @param checkX x coordinate of point to check
 	 * @param checkY y coordinate of point to check
 	 * @return true if point intersects with bounding box
 	 */
 	public boolean checkAsteroidCollisionHelper(float widthFactor, float heightFactor, float yOffset, float checkX, float checkY) {
 		
 		// small ship
 		if (MyGameView.powerupSmall.isActive()) {
 			
 			yOffset /= 2;
 			return checkX >= x - width*widthFactor/4 && checkX < x + width*widthFactor/4 &&
 					checkY >= y + yOffset && checkY <= y + yOffset + height*heightFactor/4;
 		}
 		// normal ship
 		else {
 			return checkX >= x - width*widthFactor/2 && checkX < x + width*widthFactor/2 &&
 					checkY >= y + yOffset && checkY <= y + yOffset + height*heightFactor/2;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param asteroid
 	 * @return
 	 */
 	public boolean checkAsteroidCollision(Asteroid asteroid) {
 		
 		// first single box-based collision
 		if (checkBoxCollision(asteroid)) {
 			
 			// if player is dashing, just return true (don't want to mess around with rotation stuff)
 			if (!inPosition) {
 				return true;
 			}
 			
 			// determine angle between player and asteroid
 			float xDiff = x - asteroid.x;
 			float yDiff = y - asteroid.y;
 			
 			//System.out.println("Player: " + x + ", " + y);
 			//System.out.println("Asteroid: " + asteroid.x + ", " + asteroid.y);
 			
 			//System.out.println("Diff: " + xDiff + ", " + yDiff);
 			
 
 			// angle with respect to the asteroid
 			double angle = Math.atan(yDiff/xDiff);
 			
 			// on left side
 			if (xDiff < 0) {
 				angle += Math.PI;
 			}
 			
 			if (angle < 0) {
 				angle += 2*Math.PI;
 			}
 			
 			//System.out.println("Asteroid Angle: " + angle);
 
 			// get two closest asteroid points to angle
 			int asteroidPointsIndex = asteroid.getClosestPointsIndex(angle);
 			
 			float x1 = asteroid.x + asteroid.points[asteroidPointsIndex];
 			float y1 = asteroid.y + asteroid.points[asteroidPointsIndex+1];
 			
 			float x2 = asteroid.x;
 			float y2 = asteroid.y;
 			
 			if (asteroidPointsIndex + 2 >= asteroid.points.length) {
 				x2 += asteroid.points[0];
 				y2 += asteroid.points[1];
 			} else {
 				x2 += asteroid.points[asteroidPointsIndex+2];
 				y2 += asteroid.points[asteroidPointsIndex+3];
 			}
 			
 			// variables for helper method
 			float widthFactor = 0.25f;
 			float heightFactor = 0.25f;
 			float yOffset = -16;
 			
 			if (direction == MyGameView.DIRECTION_REVERSE) {
 				yOffset = 16;
 			}
 			
 			// check top box
 			if (checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x1, y1) ||
 					checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x2, y2))
 			{
 				return true;
 			}
 			
 			widthFactor += 0.25f;
 			if (direction == MyGameView.DIRECTION_NORMAL) {
 				yOffset += 8;
 			} else {
 				yOffset -= 8;
 			}
 			
 			// check middle top box
 			if (checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x1, y1) ||
 					checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x2, y2))
 			{
 				return true;
 			}
 			
 			widthFactor += 0.25f;
 			if (direction == MyGameView.DIRECTION_NORMAL) {
 				yOffset += 8;
 			} else {
 				yOffset -= 8;
 			}
 			
 			// check middle bottom box
 			if (checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x1, y1) ||
 					checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x2, y2))
 			{
 				return true;
 			}
 			
 			widthFactor += 0.25f;
 			if (direction == MyGameView.DIRECTION_NORMAL) {
 				yOffset += 8;
 			} else {
 				yOffset -= 8;
 			}
 			
 			// check bottom box
 			if (checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x1, y1) ||
 					checkAsteroidCollisionHelper(widthFactor, heightFactor, yOffset, x2, y2))
 			{
 				return true;
 			}
 			
 			return false;
 			
 		}
 		
 		return false;
 			
 	}
 	
 	
 	/**
 	 * Set player's x value
 	 * @param x x value to set to
 	 */
 	public void setX(float x) {
 		this.x = x;
 	}
 	
 	public void moveLeft() {
 		
 		if (speedX == 0 || move != MOVE_LEFT) {
 			speedX = BUTTON_MIN_SPEED_FACTOR*maxSpeed;
 		}
 		
 		dirX = -1;
 		
 		move = MOVE_LEFT;
 	}
 	
 	public void moveRight() {
 		if (speedX == 0 || move != MOVE_RIGHT) {
 			speedX = BUTTON_MIN_SPEED_FACTOR*maxSpeed;
 		}
 		
 		dirX = 1;
 		
 		move = MOVE_RIGHT;
 	}
 	
 	public void moveStart() {
 		move = MOVE_ANY;
 	}
 	
 	public void moveStop() {
 		dirX = 0;
 		move = MOVE_NONE;
 	}
 	
 	public void dash() {
 		
 		inPosition = false;
 		dashNumAffectedAsteroids = 0;
 		dashNumAffectedAsteroidsHeldInPlace = 0;
 	
 		if (direction == MyGameView.DIRECTION_NORMAL) {
 			direction = MyGameView.DIRECTION_REVERSE;
 		} else {
 			direction = MyGameView.DIRECTION_NORMAL;
 		}
 		
 		dashTimeout = dashRechargeDuration;
 		
 		LocalStatistics.getInstance().usesDash++;
 	}
 	
 	public boolean inPosition() {
 		return inPosition;
 	}
 	
 	public int getDirection() {
 		return direction;
 	}
 	
 	/**
 	 * Returns float between [-1, 1] depending on ship's position
 	 * @return gravity
 	 */
 	public float getGravity() {
 		return 1 - 2 * (yBottom - y) / (yBottom - yTop);
 	}
 	
 	/**
 	 * Increments dash number of affected asteroids
 	 */
 	public void dashAffectedAsteroid(Asteroid asteroid) {
 		if (asteroid.getStatus() == STATUS_HELD_IN_PLACE) {
 			dashNumAffectedAsteroidsHeldInPlace++;
 		} else {
 			dashNumAffectedAsteroids++;
 		}
 	}
 	
 	/**
 	 * Checks local dash achievements
 	 */
 	private void checkAchievements() {
 		if (dashNumAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_1) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithDash1);
 		}
 		
 		if (dashNumAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_2) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithDash2);
 		}
 		
 		if (dashNumAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_3) {
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithDash3);
 		}
 		
 		if (dashNumAffectedAsteroidsHeldInPlace > Achievements.LOCAL_MAGNET_HOLD_IN_PLACE_NUM) {
 			Achievements.unlockLocalAchievement(Achievements.localMagnetHoldInPlace);
 		}
 	}
 	
 	/**
 	 * Returns number of affected asteroids with dash ability
 	 */
 	public int getDashNumAffectedAsteroids() {
 		return dashNumAffectedAsteroids + dashNumAffectedAsteroidsHeldInPlace;
 	}
 	
 	/**
 	 * Returns true if the dash ability causes multiple drops
 	 * @return true if the dash ability causes multiple drops
 	 */
 	public boolean getDashMultipleDrops() {
 		return dashMultipleDrops;
 	}
 
 	/**
 	 * Returns maximum speed of player
 	 * @return maximum speed of player
 	 */
 	public float getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	/**
 	 * Returns duration of breakup animation
 	 * @return duration of breakup animation
 	 */
 	public int getBreakupDuration() {
 		return BREAKING_UP_DURATION;
 	}
 	
 	/**
 	 * Returns true if player can dash
 	 * @return true if player can dash
 	 */
 	public boolean canDash() {
 		return dashTimeout <= 0;
 	}
 	
 }
