 import java.util.Random;
 
 abstract class Actor {
 	static private final double MAX_VELOCITY = 0.1;
 	static private final float MAX_OMEGA = 0.5f;
 	
 	/**
 	 * Common random number generator object
 	 */
 	static protected Random gen = new Random();
 
 	/**
 	 *  All the actors currently in play
 	 *  We use the fully qualified named space for the Vector container so 
 	 *  it doesn't clash with our name space. Vectors work like ArrayLists,
 	 *  but are synchronized.
 	 */
 	static public java.util.Vector<Actor> actors = new java.util.Vector<Actor>();
 	
 	// Used by generateId();
 	public static int lastId;
 	
 	// These fields are protected so that our descendants can modify them
 	protected Vector position;
 	protected Vector velocity;
 	protected float theta; // Angular position
 	protected float omega; // Angular velocity
 	protected Sprite sprite; // This is the texture of this object
 	protected float size; // the radius of the object
 	protected int id; // unique ID for each Actor 
 	protected int parentId;
 	protected int age; // Actor age in frames
 
 	/**
 	 * Call back before render loop for update to update it's position and do any housekeeping
 	 */
 	 public void update() {
 		 /* Limit maximum speed */
 		 if (omega > MAX_OMEGA)
 			 omega = MAX_OMEGA;
 		 if (velocity.magnitude() > MAX_VELOCITY)
 			 velocity.normalizeTo(MAX_VELOCITY);
 		 
 		 /* Update position and angle of rotation */
 		 theta += omega;
 		 position.incrementBy(velocity);
 		 
 		 age ++;
 		 
 		 checkBounds();
 	 }
 	 
 	 public Vector getTailPosition() {
 		 Vector tail = new Vector(position);
 		 tail.incrementXBy(-Math.cos(theta) * getRadius());
 		 tail.incrementYBy(-Math.sin(theta) * getRadius());
 		 
 		 return tail;
 	 }
 	 
 	 public Vector getNosePosition() {
 		 Vector nose = new Vector(position);
 		 nose.incrementXBy(Math.cos(theta) * getRadius());
 		 nose.incrementYBy(Math.sin(theta) * getRadius());
 		 
 		 return nose;
 	 }
 	 
 	 /**
 	  * CL - We need to synchronize removing actors so we don't have threads
 	  *      stepping on eachother's toes.
 	  *      
 	  *      NOTE: thread concurrency is an advanced topic. This is a base
 	  *      implementation to handle the problem.
 	  */
 	 protected void delete() {
 		 // NOTE: This needs to be thread safe.
 		 Actor.actors.remove(this);
 	 }
 
 	/**
 	 * Call back upon collision detection for object to handle collision
 	 * It could...
 	 *   Bounce off
 	 *   Explode into many smaller objects
 	 *   Just explode
 	 * @param other the object this actor collided with
 	 */
 	abstract public void handleCollision(Actor other);
 	
 	/**
 	 * 
 	 * @return the actors current position
 	 */
 	public Vector getPosition() {
 		return position;
 	}
 	
 	/**
 	 * 
 	 * @return the actors current velocity
 	 */
 	public Vector getVelocity(){
 		return velocity;
 	}
 	
 	/**
 	 * 
 	 * @return the actors current rotational position
 	 */
 	public float getTheta(){
 		return theta;
 	}
 	
 	/**
 	 * 
 	 * @return the actors current rotational position in degrees
 	 */
 	public float getThetaDegrees() {
 		return theta * 180 / (float)Math.PI;
 	}
 	
 	/**
 	 * 
 	 * @return the actors current angular velocity
 	 */
 	public float getOmega() {
 		return omega;
 	}
 	
 	/**
 	 * 
 	 * @return the actors Sprite/Texture
 	 */
 	public Sprite getSprite(){
 		return sprite;
 	}
 	
 	/**
 	 * 
 	 * @return the actors size (for texture scaling and collision detection)
 	 */
 	public float getSize(){
 		return size;
 	}
 	
 	public float getRadius() {
 		return size / 2;
 	}
 	
 	// Lets you reference chain
 	public Actor setSize(float newSize){
 		size = newSize;
 		return this;
 	}
 	
 	/**
 	 * This checks that the position vector is in bounds (the on screen region)
 	 * and if it passes one side it moves it to the opposite edge.
 	 */
 	private void checkBounds() {
 		if (position.x() > 1)
 			position.incrementXBy(-2);
 		else if (position.x() < -1)
 			position.incrementXBy(2);
 		
 		if (position.y() > 1)
 			position.incrementYBy(-2);
 		else if (position.y() < -1)
 			position.incrementYBy(2);		
 	}
 	
 	protected int generateId() {
 		return (lastId =+ gen.nextInt(1000) + 1); // Pseudo random increments
 	}
 	
 	public static void updateActors() {
 		// Update each actor
 		for(int i = 0; i < actors.size(); i++) {
 			// We get the actor only once in case we the actor is removed
 			// during the update phase. E.G. Bullets FramesToLive reaches 0
 			Actor a = actors.get(i);
 
 			// Track down actors without ids.
 			if (a.id == 0)
 				System.err.println("DEBUG: " + a + " actor without ID set");
 
 			a.update();
 		}
 	}
 	
 	public static void collisionDetection() {
 		/*
 		 * Collision detection
 		 * For each actor, check for collisions with the remaining actors
		 * For collision purposes we are modeling each actor as a circle with radius getSize()
 		 * This algorithm is 1/2 n^2 compares, but it should be sufficient for our purposes
 		 */
 		for(int i = 0; i < actors.size(); i++) {
 			Actor a = actors.get(i);
 
 			for (int j = i + 1; j < actors.size(); j++) {
 				Actor b = actors.get(j);
 
 				if (a.checkCollision(b)) {
 					//System.err.println("DEBUG: detected collision between " + a + " and " + b);
 					a.handleCollision(b);
 					b.handleCollision(a);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Check for a collision between this actor and another in the next frame
 	 * @param other - another actor
 	 * @return truth if a collision will occur in the next frame
 	 */
 	private boolean checkCollision(Actor other) {
 		/*
 		 * To check for a collision in the next frame we use
 		 * parametric equations for the position of each object
 		 * and find where there paths will intersect, and
 		 * check if it's in the next frame.
 		 * 
 		 * We model each objects motion with the vector equation
 		 * 		P + t * V
 		 * 
 		 * So for two objects we have
 		 * 		P(1) + t * V(1)
 		 * and
 		 * 		P(2) + t * V(2)
 		 * 
 		 * To find the intersection we set the two equations
 		 * equal to each other
 		 * 		P(1) + t * V(1) = P(2) + t * V(2)
 		 * 
 		 * Then solve for t
 		 * 		t * V(1) - t * V(2) = P(2) - P(1)
 		 * 
 		 * 		t * (V(1) - V(2)) = P(2) - P(1)
 		 * 
 		 *		    P(2) - P(1)
 		 *		t = -----------
 		 *		    V(1) - V(2)
 		 *  
 		 * Since we simply increment the position by the velocity
 		 * each frame, we just just need to check if there is
 		 * an intersection in t = 0..1.
 		 */
 		
 		float deltaVX = other.velocity.x() - velocity.x();
 		float deltaVY = other.velocity.y() - velocity.y();
 		float deltaPX = position.x() - other.position.x();
 		float deltaPY = position.y() - other.position.y();
 
 		/* Our sizes are the diameter of each object and we want the distance between their centers */                          
 		float minDistance = getRadius() + other.getRadius();
 		
 		/*
 		 * Since we are looking for an intersection in two dimensions
 		 * we check for a collision in each dimension and return
 		 * true only if both are true.
 		 */
 		boolean collideX = isCollision1D(deltaPX, deltaVX, minDistance);
 		boolean collideY = isCollision1D(deltaPY, deltaVY, minDistance);
 
 		return collideX && collideY;
 	}
 
 	/**
 	 * Check for a collision on one dimension
 	 * @param deltaP - delta position
 	 * @param deltaV - delta velocity
 	 * @param minDist - minimum distance between particles for a collision to occur, usually the sum of their radii
 	 * @return truth if a collision will occur in the next frame
 	 */
 	private static boolean isCollision1D(float deltaP, float deltaV, float minDist) {
 		/* Since we want to detect collision of objects, rather than just
 		 * point like particles, we check for collisions our minimum
 		 * collision distance each side of the point of collision if our
 		 * our objects where both just points.
 		 * 
 		 * The code for point collisions is:
 		 * 		float t = deltaP / deltaV;
 		 * 		return t >= 0 && t < 1;
 		 * 
 		 * Note: this doesn't protect against dividing by zero
 		 */
 		
 		if (deltaV != 0) { // Don't divide by zero
 			// Calculate the extremes of our collision range
 			float a = (deltaP - minDist) / deltaV;
 			float b = (deltaP + minDist) / deltaV;
 
 			/*
 			 * There are six cases, excluding the cases
 			 * where a and b are swapped by a negative
 			 * deltaV:
 			 * 
 			 * a--b    a--b    a--b
 			 *     a--b    a--b
 			 *      a--------b
 			 * <-----0------1-----> 
 			 * We only check for the two non collision
 			 * cases, else assume the collision case 
 			 * takes place. 
 			 */		
 			if(a > 1 && b > 1)
 				return false;    
 			if(a <= 0 && b <= 0)
 				return false;
 		} else {
 			/*
 			 * The zero velocity case is actually much simpler
 			 */
 			if (deltaP >= minDist)
 				return false;
 			if (-deltaP >= minDist)
 				return false;
 		}
 		return true;
 	}
 	/* End Collision Detection */
 }
