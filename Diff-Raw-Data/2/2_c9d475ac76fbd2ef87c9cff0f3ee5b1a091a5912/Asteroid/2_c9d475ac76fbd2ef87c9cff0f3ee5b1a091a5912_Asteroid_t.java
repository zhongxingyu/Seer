 package com.slauson.dasher.objects;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.slauson.dasher.game.MyGameView;
 import com.slauson.dasher.status.LocalStatistics;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
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
 	
 	private int leftPoints, rightPoints;
 	
 	private int radius;
 	
 	/**
 	 * Private constants
 	 */
 	private static final int NUM_POINTS_MIN = 6;
 	private static final int NUM_POINTS_MAX = 12;
 	
 	private static final float RADIUS_OFFSET = 0.25f;
 	private static final float HORIZONTAL_MOVEMENT_OFFSET = 0.25f;
 	private static final float SPEED_INVISIBLE = 100;
 	
 	// durations
 	private static final int HELD_IN_PLACE_DURATION = 10000;
 	private static final int DISAPPEAR_DURATION = 10000;
 	private static final int INVISIBLE_DURATION = 2000;
 
 	private static final float SPEED_HELD_IN_PLACE_FACTOR = 0.5f;
 
 	/**
 	 * Public constants
 	 */
 	public static final int FADE_OUT_FROM_BOMB = 0;
 	public static final int FADE_OUT_FROM_QUASAR = 1;
 	public static final int FADE_OUT_FROM_MAGNET = 2;
 		
 		
 	
 	public Asteroid(float sizeFactor, float speedFactor, boolean horizontalMovement) {
 		// do width/height later
 		super(0, 0, 0, 0);
 		
 		this.random = new Random();
 		
 		int numPoints = NUM_POINTS_MIN + random.nextInt(NUM_POINTS_MAX - NUM_POINTS_MIN - 1);
 		
 		points = new float[numPoints*4];
 		altPoints = new float[numPoints*4];
 		angles = new double[numPoints];
 		lineSegments = new ArrayList<LineSegment>();
 		
 		radius = 0;
 		
 		for (int i = 0; i < numPoints; i++) {
 			lineSegments.add(new LineSegment(0, 0, 0, 0));
 		}
 
 		reset(sizeFactor, speedFactor, horizontalMovement);
 	}
 	
 	/**
 	 * Resets asteroid
 	 * @param radiusFactor radius factor of asteroid relative to screen width
 	 * @param speedFactor speed factor of asteroid relative to screen height
 	 * @param horizontalMovement whether or not the asteroid has horizontal movement
 	 */
 	public void reset(float sizeFactor, float speedFactor, boolean horizontalMovement) {
 		
 		// calculate speed
 		speed = getRelativeHeightSize(speedFactor);
 		
 		// calculate radius
 		radius = getRelativeWidthSize(sizeFactor);
 		width = radius*2;
 		height = radius*2;
 
 		if (horizontalMovement) {
 			dirX = -HORIZONTAL_MOVEMENT_OFFSET + (2*HORIZONTAL_MOVEMENT_OFFSET*random.nextFloat());
 		}
 		
 		// clear out arrays of points
 		for (int i = 0; i < points.length; i++) {
 			points[i] = 0;
 			altPoints[i] = 0;
 		}
 		
 		resetRandomPoints();
 		
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
 	 * Resets random points for asteroid
 	 */
 	private void resetRandomPoints() {
 		
 		int numPoints = points.length/4;
 		double pointAngle = 2*Math.PI/numPoints;
 		
 		rightPoints = 0;
 		leftPoints = 0;
 		
 		// for each point
 		for(int i = 0; i < numPoints; i++) {
 			
 			// calculate angle based on point number and random offset
 			double angle = 2*Math.PI*i/numPoints - pointAngle/2 + random.nextFloat()*pointAngle;
 			
 			if (angle < Math.PI/2 || angle > 3*Math.PI/2) {
 				rightPoints++;
 			} else {
 				leftPoints++;
 			}
 			
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
 			
 			speed = 0;
 			status = STATUS_BREAKING_UP;
 			timeCounter = BREAKING_UP_DURATION;
 			
 			LocalStatistics.getInstance().asteroidsDestroyedByDash++;
 		}
 	}
 	
 	/**
 	 * Make asteroid disappear, caused by black hole
 	 */
 	public void disappear() {
 		if (status == STATUS_NORMAL) {
 			status = STATUS_DISAPPEARING;
 			timeCounter = DISAPPEAR_DURATION;
 			
 			LocalStatistics.getInstance().asteroidsDestroyedByBlackHole++;
 			
 			// create temporary array of points so we can shrink the asteroid
 			System.arraycopy(points, 0, altPoints, 0, points.length);
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
 			
 			int indexRight = 2, indexLeft = 0;
 			boolean leftSwitch = false, rightSwitch = false;
 			
 			// start at 2 so we get each pair of points (same values) for each corresponding angle
 			for (int i = 2; i < points.length-2; i+=4) {
 				
 				// right points
 				if (angles[(i+2)/4] < Math.PI/2.0 || angles[(i+2)/4] > 3.0*Math.PI/2.0) {
 
 					// add one right point to left side
 					if (leftSwitch && !rightSwitch) {
 						// perfect split
 						points[i] = 0;
 						points[i+2] = 0;
 						
 						altPoints[indexLeft] = points[i];
 						altPoints[indexLeft+1] = points[i+1];
 						altPoints[indexLeft+2] = points[i+2];
 						altPoints[indexLeft+3] = points[i+3];
 
 						indexLeft += 4;
 						rightSwitch = true;
 					}
 					
 					points[indexRight] = points[i];
 					points[indexRight+1] = points[i+1];
 					points[indexRight+2] = points[i+2];
 					points[indexRight+3] = points[i+3];
 					
 					indexRight += 4;
 				}
 				// left points
 				else {
 					
 					// add one left point to right side
 					if (!leftSwitch) {
 						// perfect split
 						points[i] = 0;
 						points[i+2] = 0;
 						
 						points[indexRight] = points[i];
 						points[indexRight+1] = points[i+1];
 						points[indexRight+2] = points[i+2];
 						points[indexRight+3] = points[i+3];
 
 						indexRight += 4;
 						leftSwitch = true;
 					}
 					
 					altPoints[indexLeft] = points[i];
 					altPoints[indexLeft+1] = points[i+1];
 					
 					// only add first point once on left side
 					if (indexLeft == 0) {
 						indexLeft += 2;
 					} else { 
 						altPoints[indexLeft+2] = points[i+2];
 						altPoints[indexLeft+3] = points[i+3];
 						indexLeft += 4;
 					}
 				}
 			}
 			
 			// close right points
 			points[indexRight] = points[points.length-2];
 			points[indexRight+1] = points[points.length-1];
 			
 			// close left points
 			altPoints[indexLeft] = altPoints[0];
 			altPoints[indexLeft+1] = altPoints[1];
 			
			// NOTE: don't reset speed here so that the split up animation looks smoother
 			status = STATUS_SPLITTING_UP;
 			timeCounter = SPLITTING_UP_DURATION;
 			
 			LocalStatistics.getInstance().asteroidsDestroyedByDrill++;
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
 				canvas.save();
 				canvas.translate(x, y);
 				
 				canvas.drawLines(points, paint);
 				canvas.restore();
 			}
 			// broken up asteroid
 			else if (status == STATUS_BREAKING_UP){
 				paint.setAlpha((int)(255 * (1.f*timeCounter/BREAKING_UP_DURATION)));
 				
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
 				canvas.save();
 				canvas.translate(x, y);
 				
 				canvas.drawLines(altPoints, paint);
 				canvas.restore();
 				paint.setAlpha(255);
 			}
 			// fading out asteroid
 			else if (status == STATUS_FADING_OUT) {
 				paint.setAlpha((int)(255 * (1.0*timeCounter/FADING_OUT_DURATION)));
 				canvas.save();
 				canvas.translate(x,  y);
 
 				canvas.drawLines(points, paint);
 				
 				canvas.restore();
 				paint.setAlpha(255);	
 			}
 			// splitting up
 			else if (status == STATUS_SPLITTING_UP) {
 				paint.setAlpha((int)(255 * (1.0*timeCounter/SPLITTING_UP_DURATION)));
 				
 				canvas.save();
 				canvas.translate(x + ((1.f*SPLITTING_UP_DURATION - timeCounter)/SPLITTING_UP_DURATION)*SPLITTING_UP_OFFSET,  y);
 
 				canvas.drawLines(points, 0, rightPoints*4+4, paint);
 				
 				canvas.restore();
 				
 				canvas.save();
 				canvas.translate(x - ((1.f*SPLITTING_UP_DURATION - timeCounter)/SPLITTING_UP_DURATION)*SPLITTING_UP_OFFSET,  y);
 
 				canvas.drawLines(altPoints, 0, leftPoints*4+4, paint);
 				
 				canvas.restore();
 				paint.setAlpha(255);
 			}
 			// held in place
 			else if (status == STATUS_HELD_IN_PLACE) {
 				
 				canvas.save();
 				canvas.translate(x, y);
 				
 				canvas.drawLines(points, paint);
 				canvas.restore();
 			}
 		}
 	}
 	
 	@Override
 	public void update() {
 		update(1.0f);
 	}
 	
 	public void update(float speedModifier) {
 		
 		long timeElapsed = System.currentTimeMillis() - lastUpdateTime;
 		lastUpdateTime = System.currentTimeMillis();
 		
 		float timeModifier = 1.f*timeElapsed/1000;
 		
 		x = x + (dirX*speed*timeModifier*speedModifier);
 		y = y + (MyGameView.gravity*dirY*speed*timeModifier*speedModifier);
 		
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
 			
 			// update all points
 			for (int i =  0; i < points.length; i++) {
 				altPoints[i] = factor*points[i];
 			}
 			
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
 		this.factor = factor;
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
