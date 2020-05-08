 package com.jbromley.processing;
 
 import java.awt.geom.Line2D;
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PMatrix2D;
 import processing.core.PVector;
 
 /**
  * This class represents a "boid". It represents the location and position of a
  * boid and also contains the steering behaviors.
  * @author <a href="mailto:jbromley@gmail.com">J. Bromley</a>
  */
 public class Boid {
 	
 	private static final int THROB_PERIOD = 250;
	private static final float WANDER_RADIUS = 1.2f;
 	private static final float WANDER_DISTANCE = 2.0f;
	private static final float WANDER_JITTER = 80.0f;
 	
 	private static float alignment = 1.0f;
 	private static float cohesion = 0.5f;
 	private static float separation = 1.5f;
 	private static float neighborhoodSize = 32.0f;
 	private static float separationDistance = 24.0f;
 
 	private PVector position;
 	private PVector velocity;
 	private PVector accel;
 	private PMatrix2D feelerMatrix1;
 	private PMatrix2D feelerMatrix2;
 	private PMatrix2D coordinateMatrix;
 
 	private PVector wanderTarget;
 	
 	private PVector feelers[] = new PVector[3];
 	
 	private float startRadius;
 	private float radius;
 	private float maxForce;    // Maximum steering force
 	private float maxSpeed;    // Maximum speed
 	private int color;
 	private int throbOffset;
 	private Flocking parent;
 	
 	/**
 	 * This class is a value object for intersection test results.
 	 * @author <a href="mailto:jbromley@gmail.com">J. Bromley</a>
 	 */
 	private static class Intersection {
 		public Intersection(PVector point, float distance) {
 			this.point = point;
 			this.distance = distance;
 		}
 		
 		public PVector point;
 		public float distance;
 	}
 
 	/**
 	 * Create a single boid.
 	 * @param pos the initial position of the boid on the screen
 	 * @param ms the maximum speed for the boid
 	 * @param mf the maximum force that can be applied to a boid
 	 * @param owner the PApplet owner of the boid
 	 */
 	public Boid(PVector pos, float ms, float mf, Flocking owner) {
 		parent = owner;
 		position = pos.get();
 		velocity = new PVector(parent.random(-1, 1), parent.random(-1, 1));
 		accel = new PVector(0,0);
 		startRadius = 4.0f;
 		maxSpeed = ms;
 		maxForce = mf;
 		color = parent.color(parent.random(0, 256), parent.random(0, 256), 
 							 parent.random(0, 256));
 		throbOffset = (int) parent.random(0, THROB_PERIOD);
 
 		float theta = parent.random(1.0f) * PApplet.TWO_PI;
 		wanderTarget = new PVector(WANDER_RADIUS * PApplet.cos(theta),
 								   WANDER_RADIUS * PApplet.sin(theta));
 		feelerMatrix1 = new PMatrix2D();
 		feelerMatrix1.rotate(PApplet.HALF_PI * 3.5f);
 		feelerMatrix2 = new PMatrix2D();
 		feelerMatrix2.rotate(PApplet.HALF_PI * 0.5f);
 		
 		coordinateMatrix = new PMatrix2D();
 	}
 	
 	public PVector getPosition() {
 		return position;
 	}
 	
 	public void setPosition(PVector newPosition) {
 		position = newPosition;
 	}
 	
 	/**
 	 * Advances the state of the boid through a single step
 	 * @param boids a list of all Boids
 	 */
 	public void update(CellSpacePartition<Boid> boids) {
 		PVector oldPosition = position.get();
 		flock(boids);
 		updateMotion();
 		boids.updateEntity(this, oldPosition);
 		render();
 	}
 
 	/**
 	 * Calculates and weights the forces from all steering forces.
 	 * @param boids a list of all boids
 	 */
 	private void flock(CellSpacePartition<Boid> boids) {
 		ArrayList<Boid> neighbors = boids.getNeighborList(position, 
 				getNeighborhoodSize());
 		PVector separation = separate(boids);
 		PVector alignment = align(neighbors);
 		PVector cohesion = cohesion(neighbors);
 		PVector wander = wander();
 		PVector avoidWalls = avoidWalls();
 		
 		// Weight the steering forces.
 		alignment.mult(getAlignment());
 		cohesion.mult(getCohesion());
 		separation.mult(getSeparation());
 		avoidWalls.mult(1.5f);
 
 		// Add the force vectors to acceleration.
 		accel.add(separation);
 		accel.add(alignment);
 		accel.add(cohesion);
 		accel.add(wander);
 		if (parent.getUseWalls()) {
 			accel.add(avoidWalls);
 		}
 	}
 
 	/**
 	 * Updates the position of the boids based on applied steering forces.
 	 */
 	private void updateMotion() {
 		// Make boid "throb".
 		radius = startRadius * (1.0f + 0.5f * PApplet.sin(throbOffset + 
 				(float) parent.millis() / THROB_PERIOD));
 		
 		// Calculate motion for this step.
 		velocity.add(accel);
 		velocity.limit(maxSpeed);
 		
 		position.add(velocity);
 
 		if (!parent.getUseWalls()){
 			checkBoundaries();
 		}
 
 		// Reset the acceleration for the next step.
 		accel.mult(0);
 	}
 
 	/**
 	 * Applies a steering force towards the given target.
 	 * @param target the point towards which the boid should steer
 	 */
 	private void seek(PVector target) {
 		accel.add(steer(target, false));
 	}
 
 	/**
 	 * Applies a braking force when approaching the target.
 	 * @param target the point at which the boid should arrive
 	 */
 	private void arrive(PVector target) {
 		accel.add(steer(target,true));
 	}
 
 	/**
 	 * Calculates a steering vector towards a target. The boid may optionally
 	 * brake as it approaches the target.
 	 * @param target the point toward which the boid should steer
 	 * @param slowdown if true, apply the braking steering behavior on arriving
 	 * @return the steering vector needed to move towards the target 
 	 */
 	private PVector steer(PVector target, boolean slowdown) {
 		PVector steer;
 		PVector desired = PVector.sub(target, position);
 		float d = desired.mag();
 		
 		// If the distance is greater than 0, calculate the steering force
 		// (otherwise return zero vector).
 		if (d > 0) {
 			desired.normalize();
 
 			// Two options for desired vector magnitude (1 -- based on 
 			// distance, 2 -- maxSpeed).
 			if (slowdown && d < 100.0f) {
 				desired.mult(maxSpeed * (d / 100.0f)); 
 			} else {
 				desired.mult(maxSpeed);
 			}
 			// Steering = Desired minus Velocity
 			steer = PVector.sub(desired, velocity);
 			steer.limit(maxForce);
 		} else {
 			steer = new PVector(0,0);
 		}
 		
 		return steer;
 	}
 	
 	/**
 	 * Avoids walls in the world.
 	 */
 	private PVector avoidWalls() {
 		double distClosest = Double.MAX_VALUE;
 		Line2D.Float closestWall = null;
 		PVector touchingFeeler = null;
 		PVector closestPoint = new PVector();
 		PVector steer = new PVector();
 		
 		createFeelers();
 		
 		for (PVector feeler : feelers) {
 			for (Line2D.Float wall : parent.getWalls()) {
 				Intersection intersection = null;
 				if ((intersection = intersectsLine(wall, position, feeler)) != null) {
 					if (intersection.distance < distClosest) {
 						closestWall = wall;
 						touchingFeeler = feeler;
 						distClosest = intersection.distance;
 						closestPoint = intersection.point;
 					}
 				}
 			}
 		}
 		
 		if (closestWall != null) {
 			PVector overshoot = PVector.sub(touchingFeeler, closestPoint);
 			PVector temp = new PVector(-(closestWall.y2 - closestWall.y1), 
 					(closestWall.x2 - closestWall.x1));
 			temp.normalize();
 			steer = PVector.mult(temp, overshoot.mag());
 			//steer.limit(maxForce);
 		}
 		
 		return steer;
 	}
 	
 	private Intersection intersectsLine(Line2D.Float line, PVector point1, PVector point2) {
 		float rNumerator = (point1.y - line.y1) * (line.x2 - line.x1) -
 				(point1.x - line.x1) * (line.y2 - line.y1);
 		float sNumerator = (point1.y - line.y1) * (point2.x - point1.x) -
 				(point1.x - line.x1) * (point2.y - point1.y);
 		float det = (point2.x - point1.x) * (line.y2 - line.y1) -
 				(point2.y - point1.y) * (line.x2 - line.x1);
 		
 		if (det == 0.0f) {
 			return null;
 		}
 		
 		float r = rNumerator / det;
 		float s = sNumerator / det;
 		if (0.0f < r && r < 1.0f && 0.0f < s && s < 1.0f) {
 			PVector point = PVector.add(point1, 
 					PVector.mult(PVector.sub(point2, point1), (float) r));
 			float distance = PVector.dist(point1, point);
 			return new Intersection(point, distance);
 		}
 		
 		return null;
 	}
 	
 	private void createFeelers() {
 		final float FEELER_LENGTH = 8.0f;
 		
 		feelers[0] = PVector.add(position, PVector.mult(velocity, FEELER_LENGTH));
 		
 		PVector temp = new PVector();
 		feelerMatrix1.mult(velocity, temp);
 		feelers[1] = PVector.add(position, PVector.mult(temp, FEELER_LENGTH / 2.0f));
 		
 		feelerMatrix2.mult(velocity, temp);
 		feelers[2] = PVector.add(position, PVector.mult(temp, FEELER_LENGTH / 2.0f));
 	}
 	
 	/**
 	 * Adds a small amount of random wandering to a boid's path.
 	 */
 	private PVector wander() {
 		float jitter = WANDER_JITTER * (1.0f / parent.frameRate);
 		wanderTarget.add(new PVector(parent.random(-1.0f, 1.0f) * jitter,
 						 parent.random(-1.0f, 1.0f) * jitter));
 		wanderTarget.normalize();
 		wanderTarget.mult(WANDER_RADIUS);
		PVector target = PVector.add(wanderTarget, new PVector(WANDER_DISTANCE, 0));
 		PVector worldTarget = pointToWorldSpace(position, velocity, target);
 
 		return steer(worldTarget, false);
 	}
 	
 	/**
 	 * Converts a point from boid local coordinates to world coordinates. Boid 
 	 * local coordinates have the x-axis aligned with the velocity and the 
 	 * y-axis perpendicular to this.
 	 * @param target the point in boid-local coordinates
 	 * @param velocity the boid's velocity vector 
 	 * @param position the boid's position in world coordinates
 	 * @return the point translated to world coordinates
 	 */
 	PVector pointToWorldSpace(PVector position, PVector velocity, PVector localPos) {
 		coordinateMatrix.reset();
 		coordinateMatrix.translate(position.x, position.y);
 		coordinateMatrix.rotate(velocity.heading2D());
 		PVector worldPos = new PVector();
 		coordinateMatrix.mult(localPos, worldPos);
 		return worldPos;
 	}
 
 	/** 
 	 * Renders a single frame of the boid.
 	 */
 	private void render() {
 		// Draw a triangle rotated in the direction of velocity
 		float theta = velocity.heading2D() + PApplet.HALF_PI;
 		parent.fill(color, 128);
 		parent.stroke(color);
 		parent.pushMatrix();
 		parent.translate(position.x, position.y);
 		parent.rotate(theta);
//		parent.triangle(0, -radius * 2, -radius, radius * 2, radius, radius * 2);
 		parent.beginShape(PApplet.TRIANGLES);
 		parent.vertex(0, -radius * 2);
 		parent.vertex(-radius, radius * 2);
 		parent.vertex(radius, radius * 2);
 		parent.endShape();
 		parent.popMatrix();
 	}
 
 	/**
 	 * Handles boids moving off the edge of the screen. The screen is treated 
 	 * as the surface of a torus. Boids moving off the top are wrapped to the 
 	 * top of the screen and vice versa.
 	 */
 	private void checkBoundaries() {
 		if (position.x < -radius) { 
 			position.x = parent.width + radius;
 		} else if (position.x > parent.width + radius) {
 			position.x = -radius; 
 		}
 		if (position.y < -radius) {
 			position.y = parent.height + radius;
 		} else if (position.y > parent.height + radius) {
 			position.y = -radius;
 		}
 	}
 
 	/**
 	 * Calculates a steering force that moves a boid away from nearby boids.
 	 * @param boids a list of all boids
 	 * @return the steering force to keep this boid separated from the flock
 	 */
 	private PVector separate(CellSpacePartition<Boid> boids) {
 		PVector steer = new PVector(0, 0);
 		float separationRadius = getSeparationDistance();
 		ArrayList<Boid> neighbors = boids.getNeighborList(position,
 				separationRadius);
 		for (Boid other : neighbors) {
 			if (other != this) {
 				// Calculate vector pointing away from neighbor
				PVector diff = PVector.sub(position, other.position);
 				diff.normalize();
				float distance = PVector.dist(position, other.position); 
				if (distance > 0.0f) {
					diff.div(distance);
				}
 				steer.add(diff);
 			}
 		}
 
 		if (neighbors.size() > 0) {
 			steer.div(neighbors.size());
 		}
 
 		if (steer.mag() > 0) {
 			steer.normalize();
 			steer.mult(maxSpeed);
 			steer.sub(velocity);
 			steer.limit(maxForce);
 		}
 		return steer;
 	}
 
 	/**
 	 * Aligns the boid with the average velocity of nearby boids.
 	 * @param neighbors a list of all neighbors of this boid
 	 * @return a vector aligned with the flock's average velocity
 	 */
 	private PVector align(ArrayList<Boid> neighbors) {
 		PVector steer = new PVector(0, 0);
 		int count = 0;
 
 		for (Boid other : neighbors) {
 			if (other != this) {
 				steer.add(other.velocity);
 				count++;
 			}
 		}
 
 		if (count > 0) {
 			steer.div((float) count);
 		}
 
 		if (steer.mag() > 0) {
 			steer.normalize();
 			steer.mult(maxSpeed);
 			steer.sub(velocity);
 			steer.limit(maxForce);
 		}
 		return steer;
 	}
 
 	/**
 	 * Calculates steering vector towards average position of neighbors.
 	 * @param neighbors a list of all neighbors of this boid
 	 * @return steering force to go towards average position of neighbors
 	 */
 	private PVector cohesion(ArrayList<Boid> neighbors) {
 		PVector sum = new PVector(0, 0);
 		int count = 0;
 
 		for (Boid other : neighbors) {
 			if (other != this) {
 				sum.add(other.position);
 				count++;
 			}
 		}
 		
 		if (count > 0) {
 			sum.div((float) count);
 			return steer(sum, false);  // Steer towards the location
 		}
 		return sum;
 	}
 	
 	/**
 	 * Enforce the non-overlap condition.
 	 */
 	private void enforceNoOverlap(ArrayList<Boid> neighbors) {
 		for (Boid other : neighbors) {
 			if (this == other) {
 				continue;
 			}
 			
 			PVector separationVector = PVector.sub(position, other.position);
 			float distance = separationVector.mag();
 			float overlap = radius + other.radius - distance;
 			if (overlap >= 0.0f){
 				position.add(PVector.mult(PVector.div(separationVector, distance), overlap));
 			}
 		}
 	}
 
 	public static void setAlignment(float alignment) {
 		Boid.alignment = alignment;
 	}
 
 	public static float getAlignment() {
 		return alignment;
 	}
 
 	public static void setCohesion(float cohesion) {
 		Boid.cohesion = cohesion;
 	}
 
 	public static float getCohesion() {
 		return cohesion;
 	}
 
 	public static void setSeparation(float separation) {
 		Boid.separation = separation;
 	}
 
 	public static float getSeparation() {
 		return separation;
 	}
 
 	public static void setNeighborhoodSize(float neighborhoodSize) {
 		Boid.neighborhoodSize = neighborhoodSize;
 	}
 
 	public static float getNeighborhoodSize() {
 		return neighborhoodSize;
 	}
 
 	public static void setSeparationDistance(float separationDistance) {
 		Boid.separationDistance = separationDistance;
 	}
 
 	public static float getSeparationDistance() {
 		return separationDistance;
 	}
 }
