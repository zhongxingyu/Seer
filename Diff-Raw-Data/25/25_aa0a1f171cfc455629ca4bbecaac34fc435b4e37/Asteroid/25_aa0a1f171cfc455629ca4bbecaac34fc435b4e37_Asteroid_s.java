 package com.slauson.dasher.objects;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.slauson.dasher.game.MyGameView;
 import com.slauson.dasher.status.Configuration;
 import com.slauson.dasher.status.LocalStatistics;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 
 /**
  * Asteroid that player ship has to avoid
  * @author Josh Slauson
  *
  */
 public class Asteroid extends DrawObject {
 
 	/**
 	 * Private fields
 	 */
 
 	private Random random;
 
 	private float factor;
 	
 	private double[] angles;
 	
 	private int radius;
 	
 	/**
 	 * Private constants
 	 */
 	private static final int NUM_POINTS_MIN = 6;
 	private static final int NUM_POINTS_MAX = 12;
 	
 	private static final float RADIUS_OFFSET = 0.25f;
 	private static final float SPEED_INVISIBLE = 100;
 	
 	// durations
 	private static final int HELD_IN_PLACE_DURATION = 10000;
 	private static final int DISAPPEAR_DURATION = 10000;
 	private static final int INVISIBLE_DURATION = 2000;
 
 	private static final float SPEED_HELD_IN_PLACE_FACTOR = 0.5f;
	
 	/**
 	 * Public constants
 	 */
 	public static final int FADE_OUT_GRAPHICS_LOW = -1;
 	public static final int FADE_OUT_FROM_BOMB = 0;
 	public static final int FADE_OUT_FROM_QUASAR = 1;
 	public static final int FADE_OUT_FROM_MAGNET = 2;
 
 	
 	public Asteroid(float sizeFactor, float speedFactor, float sizeFactorMax, float horizontalMovementMax) {
 		// do width/height later
 		super(0, 0, 0, 0);
 		
 		this.random = new Random();
 		
 		int numPoints = NUM_POINTS_MIN + random.nextInt(NUM_POINTS_MAX - NUM_POINTS_MIN - 1);
 		
 		points = new float[numPoints*4];
 		angles = new double[numPoints];
 		lineSegments = new ArrayList<LineSegment>();
 		
 		rectDest = new RectF();
 		rectSrc = new Rect();
 		
 		int bitmapRadius = getRelativeWidthSize(sizeFactorMax);
 		int bitmapSize = (int)(2.5 * bitmapRadius);
 		
 		bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
 		
 		for (int i = 0; i < numPoints; i++) {
 			lineSegments.add(new LineSegment(0, 0, 0, 0));
 		}
 
 		reset(sizeFactor, speedFactor, horizontalMovementMax);
 	}
 	
 	/**
 	 * Resets asteroid
 	 * @param radiusFactor radius factor of asteroid relative to screen width
 	 * @param speedFactor speed factor of asteroid relative to screen height
 	 * @param horizontalMovementMax maximum amount of horizontal movement
 	 */
 	public void reset(float sizeFactor, float speedFactor, float horizontalMovementMax) {
 		
 		// calculate speed
 		speed = getRelativeHeightSize(speedFactor);
 		
 		// calculate radius
 		radius = getRelativeWidthSize(sizeFactor);
 		width = radius*2;
 		height = radius*2;
 		
 		// draw points to bitmap
 		createRandomPoints();
 		drawPointsToBitmap();
 		
 		// horizontal movement
 		if (horizontalMovementMax > 0.01) {
 			dirX = -horizontalMovementMax + (2*horizontalMovementMax*random.nextFloat());
 		}
 		
 		reset();
 	}
 	
 	/**
 	 * Resets asteroid
 	 */
 	public void reset() {
 		
 		x = random.nextInt(MyGameView.canvasWidth);
 		
 		if (MyGameView.direction == MyGameView.DIRECTION_NORMAL) {
 			y = -random.nextInt(MyGameView.canvasHeight);
 		} else {
 			y = MyGameView.canvasHeight + random.nextInt(MyGameView.canvasHeight);
 		}
 		
 		dirY = 1 - dirX;
 		timeCounter = 0;
 		factor = 1;
 		
 		status = STATUS_NORMAL;
 	}
 	
 	/**
 	 * Creates random points 
 	 */
 	private void createRandomPoints() {
 		int numPoints = points.length/4;
 		double pointAngle = 2*Math.PI/numPoints;
 		
 		// for each point
 		for(int i = 0; i < numPoints; i++) {
 			
 			// calculate angle based on point number and random offset
 			double angle = 2*Math.PI*i/numPoints - pointAngle/2 + random.nextFloat()*pointAngle;
 			
 			// calculate radius
 			float pointRadius = radius - (radius*RADIUS_OFFSET) + (2*RADIUS_OFFSET*random.nextFloat()); 
 			
 			// get cos/sin values
 			double cos = Math.cos(angle);
 			double sin = Math.sin(angle);
 			
 			// calculate x/y coordinates
 			float x = (float)(pointRadius*cos);
 			float y = (float)(pointRadius*sin);
 			
 			angles[i] = angle;
 			
 			if (i != 0) {
 				points[4*i-2] = x;
 				points[4*i-1] = y;
 			}
 			points[4*i] = x;
 			points[4*i+1] = y;
 		}
 		
 		points[4*numPoints-2] = points[0];
 		points[4*numPoints-1] = points[1];
 	}
 
 	/**
 	 * Make asteroid break up into line segments, caused by dash
 	 */
 	public void breakup() {
 		if (status == STATUS_NORMAL || status == STATUS_HELD_IN_PLACE) {
 			
 			LocalStatistics.getInstance().asteroidsDestroyedByDash++;
 			
 			// if low graphics, use fade out instead
 			if (Configuration.graphicsType == Configuration.GRAPHICS_LOW) {
 				fadeOut(FADE_OUT_GRAPHICS_LOW);
 				return;
 			}
 			
 			LineSegment lineSegment;
 
 			for (int i = 0; i < points.length; i+=4) {
 
 				lineSegment = lineSegments.get(i/4);
 
 				lineSegment.x1 = x + points[i];
 				lineSegment.y1 = y + points[i+1];
 				lineSegment.x2 = x + points[i+2];
 				lineSegment.y2 = y + points[i+3];
 
 				// need to get perpendicular (these are opposite on purpose)
 				float yDiff = Math.abs(points[i] - points[i+2]);
 				float xDiff = Math.abs(points[i+1] - points[i+3]);
 
 				yDiff += Math.abs(dirY)*speed;
 				xDiff += Math.abs(dirX)*speed;
 
 				lineSegment.move = BREAKING_UP_MOVE;
 
 				// x direction is sometimes positive
 				if (points[i] < 0) {
 					lineSegment.dirX = (-xDiff/(xDiff + yDiff));
 				} else {
 					lineSegment.dirX = (xDiff/(xDiff + yDiff));
 				}
 
 				// y direction is always positive
 				lineSegment.dirY = (yDiff/(xDiff + yDiff)) + 1;
 			}
 
 			status = STATUS_BREAKING_UP;
 			timeCounter = BREAKING_UP_DURATION;
 		}
 	}
 	
 	/**
 	 * Make asteroid disappear, caused by black hole
 	 */
 	public void disappear() {
 		if (status == STATUS_NORMAL) {
 			LocalStatistics.getInstance().asteroidsDestroyedByBlackHole++;
 			
 			// if low graphics, use fade out instead
 			if (Configuration.graphicsType == Configuration.GRAPHICS_LOW) {
 				fadeOut(FADE_OUT_GRAPHICS_LOW);
 				return;
 			}
 
 			status = STATUS_DISAPPEARING;
 			timeCounter = DISAPPEAR_DURATION;			
 		}
 	}
 	
 	/**
 	 * Fade out asteroid
 	 * @param cause cause of the fadeout
 	 */
 	public void fadeOut(int cause) {
 		if (status == STATUS_NORMAL || status == STATUS_HELD_IN_PLACE) {
 			status = STATUS_FADING_OUT;
 			timeCounter = FADING_OUT_DURATION;
 			
 			if (cause == FADE_OUT_FROM_BOMB) {
 				LocalStatistics.getInstance().asteroidsDestroyedByBomb++;
 			} else if (cause == FADE_OUT_FROM_QUASAR) {
 				LocalStatistics.getInstance().asteroidsDestroyedByBlackHole++;
 			}
 		}
 	}
 	
 	/**
 	 * Split up asteroid, caused by drill
 	 */
 	public void splitUp() {
 		if (status == STATUS_NORMAL || status == STATUS_HELD_IN_PLACE) {
 			LocalStatistics.getInstance().asteroidsDestroyedByDrill++;
 			
 			// if low graphics, use fade out instead
 			if (Configuration.graphicsType == Configuration.GRAPHICS_LOW) {
 				fadeOut(FADE_OUT_GRAPHICS_LOW);
 				return;
 			}
 
 			// NOTE: don't reset speed here so that the split up animation looks smoother
 			status = STATUS_SPLITTING_UP;
 			timeCounter = SPLITTING_UP_DURATION;
 			
 		}
 	}
 
 	/**
 	 * Hold asteroid in place, caused by magnet
 	 */
 	public void holdInPlace() {
 		if (status == STATUS_NORMAL) {
 			status = STATUS_HELD_IN_PLACE;
 			timeCounter = HELD_IN_PLACE_DURATION;			
 		}
 	}
 	
 	@Override
 	public void draw(Canvas canvas, Paint paint) {
 		if (status != STATUS_INVISIBLE && onScreen()) {
 			
 			// intact asteroid
 			if (status == STATUS_NORMAL) {
 				canvas.drawBitmap(bitmap, x - bitmap.getWidth()/2, y - bitmap.getHeight()/2, paint);
 			}
 			// broken up asteroid
 			else if (status == STATUS_BREAKING_UP) {
 				
 				float factor = 1.f*timeCounter/BREAKING_UP_DURATION;
 				paint.setAlpha((int)(255 * factor));
 				
 				for (LineSegment lineSegment : lineSegments) {
 					lineSegment.draw(canvas, paint);
 				}
 				
 				paint.setAlpha(255);	
 			}
 			// disappearing asteroid
 			else if (status == STATUS_DISAPPEARING) {
 				int alpha = (int)(4*255*(factor*(1-DISAPPEARING_FACTOR)));
 				
 				if (alpha > 255) {
 					alpha = 255;
 				}
 				paint.setAlpha(alpha);
 				
 				rectDest.set(x - factor*bitmap.getWidth()/2, y - factor*bitmap.getHeight()/2, x + factor*bitmap.getWidth()/2, y + factor*bitmap.getHeight()/2);
 				canvas.drawBitmap(bitmap, null, rectDest, paint);
 				
 				paint.setAlpha(255);
 			}
 			// fading out asteroid
 			else if (status == STATUS_FADING_OUT) {
 				paint.setAlpha((int)(255 * (1.0*timeCounter/FADING_OUT_DURATION)));
 				canvas.drawBitmap(bitmap, x - bitmap.getWidth()/2, y - bitmap.getHeight()/2, paint);
 				paint.setAlpha(255);	
 			}
 			// splitting up
 			else if (status == STATUS_SPLITTING_UP) {
 				
 				float factor = 1.f*timeCounter/SPLITTING_UP_DURATION;
 				paint.setAlpha((int)(255*factor));
 				
 				factor = 1 - factor;
 				
 				float offset = factor*SPLITTING_UP_OFFSET;
 				
 				// draw left half
 				rectDest.set(x - offset - bitmap.getWidth()/2, y - bitmap.getHeight()/2, x - offset, y + bitmap.getHeight()/2);
 				rectSrc.set(0, 0, bitmap.getWidth()/2, bitmap.getHeight());
 				canvas.drawBitmap(bitmap, rectSrc, rectDest, paint);
 				
 				// draw left line
				canvas.drawLine(x - offset, y - radius, x - offset, y + radius, paint);
 				
 				// draw right half
 				rectDest.set(x + offset, y - bitmap.getHeight()/2, x + offset + bitmap.getWidth()/2, y + bitmap.getHeight()/2);
 				rectSrc.set(bitmap.getWidth()/2, 0, bitmap.getWidth(), bitmap.getHeight());
 				canvas.drawBitmap(bitmap, rectSrc, rectDest, paint);
 				
 				// draw right line
				canvas.drawLine(x + offset, y - radius, x + offset, y + radius, paint);
 
 				paint.setAlpha(255);
 			}
 			// held in place
 			else if (status == STATUS_HELD_IN_PLACE) {
 				canvas.drawBitmap(bitmap, x - bitmap.getWidth()/2, y - bitmap.getHeight()/2, paint);
 			}
 		}
 	}
 	
 	@Override
 	public void update() {
 		update(1.0f);
 	}
 	
 	public void update(float speedModifier) {
 		
 		long timeElapsed = getElapsedTime();
 		float timeModifier = 1.f*timeElapsed/1000;
 		
 		x = x + (dirX*speed*timeModifier*speedModifier);
 		y = getNextY(speedModifier, timeModifier);
 
 		// time counter
 		if (timeCounter > 0) {
 			timeCounter -= timeElapsed;
 		}
 		
 		// broken up item
 		if (status == STATUS_BREAKING_UP) {
 			
 			for (LineSegment lineSegment : lineSegments) {
 				lineSegment.update(timeModifier*speedModifier);
 			}
 			
 			if (timeCounter <= 0) {
 				setInvisible();
 			}
 		}
 		// disappearing asteroid
 		else if (status == STATUS_DISAPPEARING) {
 			
 			if (factor < DISAPPEARING_FACTOR || timeCounter <= 0) {
 				setInvisible();
 			}
 		}
 		// fading out asteroid
 		else if (status == STATUS_FADING_OUT) {
 			if (timeCounter <= 0) {
 				setInvisible();
 			}
 		}
 		// splitting up asteroid
 		else if (status == STATUS_SPLITTING_UP) {			
 			if (timeCounter <= 0) {
 				setInvisible();
 			}
 		} 
 		// held in place asteroid
 		else if (status == STATUS_HELD_IN_PLACE) {
 			
 			// update speed
 			speed *= SPEED_HELD_IN_PLACE_FACTOR;
 			
 			if (speed < 0.01) {
 				speed = 0;
 			}
 			
 			if (timeCounter <= 0) {
 				fadeOut(FADE_OUT_FROM_MAGNET);
 			}
 		}
 		// invisible asteroid
 		else if (status == STATUS_INVISIBLE) {
 			if (timeCounter <= 0) {
 				status = STATUS_NEEDS_RESET;
 			}
 		}
 	}
 	
 	/**
 	 * Returns next y position of asteroid
 	 * @param speedModifier
 	 * @param timeModifier
 	 * @return next y position
 	 */
 	public float getNextY(float speedModifier, float timeModifier) {
 
 		// only use gravity when direction is positive
 		if (dirY > 0) {
 			return y + (MyGameView.gravity*dirY*speed*timeModifier*speedModifier);
 		} else {
 			// otherwise use direction
 			if (MyGameView.direction == MyGameView.DIRECTION_NORMAL) {
 				return y + (1*dirY*speed*timeModifier*speedModifier);
 			} else {
 				return y + (-1*dirY*speed*timeModifier*speedModifier);
 			}
 		}
 	}
 	
 	/**
 	 * Makes asteroid invisible
 	 */
 	public void setInvisible() {
 		status = STATUS_INVISIBLE;
 		timeCounter = INVISIBLE_DURATION;
 		
 		speed = SPEED_INVISIBLE;
 		dirX = 0;
 		dirY = 1;
 	}
 	
 	/**
 	 * Sets factor, used for black hole
 	 * @param factor factor
 	 */
 	public void setFactor(float factor) {
 		if (factor < this.factor) {
 			this.factor = factor;
 		}
 	}
 	
 	/**
 	 * Returns index into points closest to the given angle
 	 * @param angle angle to check
 	 * @return index into points closest to the given angle
 	 */
 	public int getClosestPointsIndex(double angle) {
 		int i;
 		
 		for(i = 0; i < angles.length; i++) {
 			if (angles[i] >= angle) {
 				break;
 			}
 		}
 		
 		// handle special cases
 		if (i == 0 || i == angles.length) {
 			return points.length-2;
 		} else {
 			return 4*i-4;
 		}
 	}
 }
