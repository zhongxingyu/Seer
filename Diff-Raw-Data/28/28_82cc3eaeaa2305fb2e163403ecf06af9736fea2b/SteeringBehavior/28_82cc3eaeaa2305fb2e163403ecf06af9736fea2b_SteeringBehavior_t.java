 package jig.ironLegends;
 
 import java.awt.Rectangle;
 import java.util.Random;
 
 import jig.engine.physics.Body;
 import jig.engine.physics.BodyLayer;
 import jig.engine.util.Vector2D;
 
 /**
  * Steering Behaviors
  * 
  * @author Bhadresh
  */
 public class SteeringBehavior {
 	/**
 	 * Agent Behavior Types
 	 */
 	public static enum Behavior {
 		NONE, SEEK, ARIVE, FLEE, WANDER, PURSUIT
 	}
 
 	/**
 	 * The Agent to which behavior is applied to
 	 */
 	protected Body agent;
 	/**
 	 * Agent's Behavior
 	 */
 	protected Behavior behavior;
 	/**
 	 * Wandering Radius
 	 */
 	protected double wanderRadius;
 	/**
 	 * Distance from the Agent to the center of Wandering circle
 	 */
 	protected double wanderDistance;
 	/**
 	 * Wander change rate
 	 */
 	protected double wanderRate;
 	/**
 	 * Wander Theta/Angle
 	 */
 	protected double wanderTheta;
 	/**
 	 * Steering Force
 	 */
 	protected Vector2D steerForce;
 	/**
 	 * Maximum Steering Force
 	 */
 	protected double maxSteerForce;
 	/**
 	 * Slow Down distance
 	 */
 	protected double slowingDistance;
 	/**
 	 * Avoid Distance
 	 */
 	protected double avoidDistance;
 	/**
 	 * Target Margin/Bounds
 	 */
 	protected double targetBound;
 	/**
 	 * Maximum Speed
 	 */
 	protected double maxSpeed;
 	/**
 	 * Target location
 	 */
 	protected Vector2D target;
 	/**
 	 * Random
 	 */
 	protected Random rand;
 	/**
 	 * World Bounds
 	 */
 	protected Rectangle worldbounds;
 	/**
 	 * Obstacles
 	 */
 	protected BodyLayer<Body> obstacles;
 	/**
 	 * Avoid obstacles
 	 */
 	protected boolean obstacleAvoidance;
 	/**
 	 * Reset Velocity slowly
 	 */
 	protected boolean slowVelReset = true;
 	protected boolean resetingVelcotiy = false;
 	protected double resetVelStartAngle = 0.0;
 	protected double resetVelEndAngle = 0.0;
 	
 	/**
 	 * use Temp target
 	 */
 	protected boolean useTempTarget = false;	
 	protected Vector2D tempTarget = Vector2D.ZERO;
 	protected Vector2D tempLastPos = Vector2D.ZERO;
	protected Vector2D _lastpos = Vector2D.ZERO;
 	private int tempTargetCounter = 0;
 	
 	/**
 	 * Steering Behavior
 	 * 
 	 * @param agent
 	 * @param behavior
 	 */
 	public SteeringBehavior(Body agent, Behavior behavior) {
 		this.agent = agent;
 		this.behavior = behavior;
 		target = Vector2D.ZERO;
 		steerForce = Vector2D.ZERO;
 		targetBound = 0;
 		maxSpeed = 100.0;
 		maxSteerForce = 100.0;
 		slowingDistance = 10.0;
 		avoidDistance = 100.0;
 		obstacleAvoidance = true;
 		worldbounds = IronLegends.WORLD_BOUNDS;
 		obstacles = null;
 		rand = new Random();
 	}
 
 	/**
 	 * Steering Behavior - Wander
 	 * 
 	 * @param agent
 	 */
 	public SteeringBehavior(Body agent) {
 		this(agent, 15.0, 50.0, 0.05);
 	}
 
 	/**
 	 * Steering Behavior - Wander
 	 * 
 	 * @param agent
 	 * @param radius
 	 * @param distance
 	 * @param jitterRate
 	 */
 	public SteeringBehavior(Body agent, double radius, double distance,
 			double jitterRate) {
 		this(agent, Behavior.WANDER);
 		wanderRadius = radius;
 		wanderDistance = distance;
 		wanderRate = jitterRate;
 		wanderTheta = (Math.random() * Math.PI * 2.0);
 		// initial random target
 		target = Vector2D.getUnitLengthVector(wanderTheta).scale(wanderRadius);
 		targetBound = 0.0;
 	}
 
 	/**
 	 * Create Random Vector
 	 * 
 	 * @param sf
 	 * @return Vector2D
 	 */
 	public Vector2D getRandomVector(double sf) {
 		Vector2D v = getRandomUnitVector();
 		return v.scale(sf);
 	}
 
 	/**
 	 * Random Unit Vector
 	 * 
 	 * @return Vector2D
 	 */
 	public Vector2D getRandomUnitVector() {
 		int x = rand.nextInt();
 		int y = rand.nextInt();
 
 		if (rand.nextInt() > 0.5) {
 			x *= -1;
 		}
 		if (rand.nextInt() > 0.5) {
 			y *= -1;
 		}
 
 		Vector2D v = new Vector2D(x, y);
 		return v.unitVector();
 	}
 
 	/**
 	 * Calculate Wander Target
 	 * 
 	 * @param t
 	 * @return Vector2D
 	 */
 	public Vector2D getWanderTarget(Vector2D t) {
 		Vector2D circleLocation = t;
 		// Normalize to get heading
 		if (circleLocation.epsilonEquals(Vector2D.ZERO, 0.01)) {
 			circleLocation = Vector2D.getUnitLengthVector((Math.random()
 					* Math.PI * 2.0));
 		} else {
 			circleLocation = circleLocation.unitVector();
 		}
 
 		// Multiply by Wander Distance
 		circleLocation = circleLocation.scale(wanderDistance);
 		// Make it relative to agent's location
 		circleLocation = circleLocation.translate(agent.getCenterPosition());
 
 		Vector2D circleOffset = Vector2D.getUnitLengthVector(wanderTheta);
 		// Multiply by Wander Radius
 		circleOffset = circleOffset.scale(wanderRadius);
 
 		// New target location
 		circleLocation = circleLocation.translate(circleOffset);
 
 		return circleLocation;
 	}
 
 	/**
 	 * Wander: Random steering. Retain steering direction and make random
 	 * displacement in each frame.
 	 */
 	public void wander() {
 		targetBound = 0.0;
 
 		// Random change
 		wanderTheta += (Math.random() * 2 - 1) * wanderRate;
 
 		// Calculate the new location to steer towards on the wander circle
 		// Start with velocity
 		int tries = 4;
 		do {
 			target = getWanderTarget(agent.getVelocity());
 			if (!clampToWorld(target, worldbounds, false)) {
 				break;
 			}
 			tries--;
 		} while (tries > 0);
 
 		if (tries == 0) { // force fully change the direction
 			clampToWorld(target, worldbounds, true);
 		}
 		// Steer towards the target
 		steer(false);
 	}
 
 	/**
 	 * Steer: Steer the agent towards a specific target
 	 * 
 	 * Arrive: Set slowdown to true - Arrive near the target
 	 * 
 	 * @param slowdown
 	 *            if true, it slows down as it approaches the target
 	 */
 	public void steer(boolean slowdown) {
 		Vector2D steervector = Vector2D.ZERO;
 		Vector2D dv = target.difference(agent.getCenterPosition());
 		double d = Math.sqrt(dv.magnitude2()); // distance to travel
 		if (d > targetBound + 0.1) {
 			dv = new Vector2D(dv.getX() / d, dv.getY() / d); // Unit Vector
 			if (slowdown && d <= (slowingDistance + targetBound)) {
 				dv = dv.scale(getMaxSpeed()
 						* (d / (slowingDistance + targetBound)));
 			} else {
 				dv = dv.scale(getMaxSpeed());
 			}
 
 			// Steering Vector = "Desired Vector" minus "Velocity"
 			steervector = dv.difference(agent.getVelocity());
 			steervector = limitVector(steervector, maxSteerForce);
 		}
 
 		steerForce = steervector;
 	}
 
 	/**
 	 * Flee: Steer away from a location/target
 	 */
 	public void flee() {
 		Vector2D steervector = Vector2D.ZERO;
 		Vector2D dv = agent.getCenterPosition().difference(target);
 		double d = Math.sqrt(dv.magnitude2()); // distance to travel
 //		System.out.printf("current: %s, running away: %s d: %.2f\n", agent.getCenterPosition(), target, d);
 		if (d < targetBound + 0.1) {
 			dv = new Vector2D(dv.getX() / d, dv.getY() / d); // Unit Vector
 			dv = dv.scale(getMaxSpeed());
 
 			steervector = dv.difference(agent.getVelocity());
 		}
 
 		steerForce = steervector;
 	}
 
 	/**
 	 * Apply Behavior Only sets the velocity of the agent, caller need to set
 	 * the position
 	 * 
 	 * @param deltaMs
 	 */
 	public void apply(long deltaMs) {
 		if (resetingVelcotiy) { // gradually reset the velocity			
 			double angleDiff = Math.abs(resetVelEndAngle - resetVelStartAngle);
 			if (angleDiff <= 0.001) {
 				resetingVelcotiy = false;
 			} else {
 				double newa = ((resetVelEndAngle > resetVelStartAngle ? 1 : -1) * Math.toRadians(10)); // changing angle 10 degree at a time
 				newa = resetVelStartAngle + (Math.abs(newa) > angleDiff ? angleDiff * (newa > 0 ? 1 : -1) : newa);				
 				agent.setVelocity(Vector2D.getUnitLengthVector(newa).scale((getMaxSpeed()/2) * deltaMs / 1000.0));
 				wanderTheta = newa;
 				resetVelStartAngle = newa;				
 				return;
 			}
 		}
 
 		if (useTempTarget) { // was stuck so goto random target
 			target = tempTarget;			
 			tempTargetCounter++;
 			if (tempLastPos.distance2(agent.getCenterPosition()) > 100 * 100) {
 				useTempTarget = false;
 				tempTargetCounter = 0;				
 			} else if (tempTargetCounter > 500) {
 				useTempTarget = false;
 				tempTargetCounter = 0;
 			}
 			
 			flee();
 		} else {		
 			switch (behavior) {
 			case WANDER:
 				wander();
 				break;
 	
 			case SEEK:
 				steer(false);
 				break;
 	
 			case ARIVE:
 				steer(true);
 				break;
 	
 			case FLEE:
 				flee();
 				break;
 	
 			default:
 				return;
 			}
 		}
 
 		// Set Velocity
 		Vector2D vel = agent.getVelocity();
 		vel = vel.translate(steerForce);
 		vel = limitVector(vel, getMaxSpeed());
 		vel = vel.scale(deltaMs / 1000.0);		
 		agent.setVelocity(vel);		
 
 		steerForce = Vector2D.ZERO; // reset the force to zero
 		
 		if (!vel.epsilonEquals(Vector2D.ZERO, 0.001)) { // Only need to check if agent is moving
 			if (behavior != Behavior.WANDER) { // clamp future position to world
 				clampToWorld(agent.getCenterPosition().translate(vel), worldbounds, true);
 			}
 			
 			vel = agent.getVelocity();
 /*
 			Vector2D futurePos = agent.getCenterPosition().translate(vel);
 			if (behavior != Behavior.WANDER && !useTempTarget && futurePos.epsilonEquals(_lastpos, 0.1)) { // stuck
 				//vel = getRandomVector(getMaxSpeed() * deltaMs / 1000.0);
 //				vel = new Vector2D(700, 700).unitVector().scale((getMaxSpeed() * deltaMs / 1000.0));
 //				resetVelocity(vel, false);
 				useTempTarget = true;
 				tempLastPos = agent.getCenterPosition();
 				tempTarget = agent.getCenterPosition().translate(new Vector2D(0.2, 0.2));//vel.unitVector().scale(targetBound).translate(agent.getCenterPosition()); // random target
 				System.out.printf("Target: %s, tempTarget: %s\n", target, tempTarget);				
 			} 
 */
 			
 			if (obstacleAvoidance && !useTempTarget) { // check for potential obstacle collision				
 				Vector2D avoid = avoidObstacle();
 				if (!avoid.epsilonEquals(Vector2D.ZERO, 0.001)) {				
 					vel = vel.translate(avoid);
 					vel = limitVector(vel, getMaxSpeed());
 					vel = vel.scale(deltaMs / 1000.0);
 					resetVelocity(vel, false);
 				}
 			}
 		}
 		
 		_lastpos = agent.getCenterPosition().translate(vel);
 	}
 
 	/**
 	 * Avoid Obstacles
 	 * @return Vector2D
 	 */	
 	public Vector2D avoidObstacle() {
 		Vector2D avoid = Vector2D.ZERO;
 		if (obstacles == null) {
 			return avoid;
 		}
 		
 		Vector2D uv = agent.getVelocity().unitVector();
 		Vector2D futurePos = agent.getCenterPosition().translate(agent.getVelocity());
 		double vmag = Math.sqrt(agent.getVelocity().magnitude2());
 		double distBetweenObjects = 0.0;
 		double distToKeep = 0.0;
 		double closest = -1.0;
 		
 		//System.out.printf("Agent's Vel: %s uv: %s vmag: %.2f\n", agent.getVelocity(), uv, vmag);
 		for(Body b : obstacles) {
 			if (!b.equals(agent) && b.isActive()) {				
 				Vector2D dv = b.getCenterPosition().difference(futurePos);				
 				distBetweenObjects = dv.magnitude2();
 				if (closest == -1.0) {
 					closest = distBetweenObjects;
 				}
 				distToKeep = (avoidDistance * avoidDistance) + Math.pow(Math.max(b.getWidth(), b.getHeight()), 2);
 				if (closest > distBetweenObjects && distBetweenObjects <= distToKeep) {
 					closest = distBetweenObjects;
 					Vector2D projection = uv.scale(dv.dot(uv));					
 					Vector2D ortho = b.getCenterPosition().difference(futurePos.translate(projection));					
 					// to avoid, move perpendicularly away from the obstacle
 					avoid = ortho.unitVector().scale(vmag * -1.0);
 					//System.out.printf("dv: %s distBetweenObjects: %.2f, distToKeep: %.2f avoid: %s\n", dv, Math.sqrt(distBetweenObjects), Math.sqrt(distToKeep), avoid);
 				}
 			}
 		}
 		
 		return avoid;
 	}
 
 	/**
 	 * Reset Agent's Velocity
 	 * @param vel
 	 */
 	public void resetVelocity(Vector2D vel, boolean force) {
 		if (!force && slowVelReset) {
 			resetingVelcotiy = true;
 			resetVelStartAngle = getVectorAngle(agent.getVelocity());
 			resetVelEndAngle = getVectorAngle(vel);
 		} else {
 			agent.setVelocity(vel);
 			wanderTheta = getVectorAngle(vel);
 		}
 	}
 	
 	/**
 	 * Simple distance check to see if two Body are in collision
 	 * 
 	 * @param a
 	 * @param b
 	 * @return boolean
 	 */
 	public boolean intersects(Body a, Body b) {
 		Vector2D aCenter = a.getCenterPosition();
 		Vector2D bCenter = b.getCenterPosition();
 
 		double aRadius = Math.max(a.getWidth() / 2, a.getHeight() / 2);
 		double bRadius = Math.max(b.getWidth() / 2, b.getHeight() / 2);
 		return (aCenter.distance2(bCenter) <= ((aRadius + bRadius) * (aRadius + bRadius)));
 	}
 
 	/**
 	 * Limit Vector Magnitude
 	 * 
 	 * @param v
 	 * @param mag
 	 * @return
 	 */
 	public Vector2D limitVector(Vector2D v, double mag) {
 		if (v.magnitude2() > mag * mag) {
 			v = v.unitVector().scale(mag);
 		}
 
 		return v;
 	}
 
 	/**
 	 * Clamp Velocity to keep agent in the World bounds
 	 * 
 	 * @param pos
 	 * @param r
 	 * @return
 	 */
 	public boolean clampToWorld(Vector2D pos, final Rectangle r, boolean force) {
 		boolean changed = false;
 		Vector2D vel = agent.getVelocity();
 		double px = pos.getX();
 		double py = pos.getY();
 		double vx = vel.getX();
 		double vy = vel.getY();
 
 		if (px <= r.getMinX()) {
 			vx = (force ? 1.0 : Math.random());
 			changed = true;			
 		} else if (px > r.getMaxX()) {
 			vx = (force ? -1.0 : Math.random());
 			changed = true;			
 		}
 		
 		if (py < r.getMinY()) {
 			vy = (force ? 1.0 : Math.random());
 			changed = true;			
 		} else if (py >= r.getMaxY()) {
 			vy = (force ? -1.0 : Math.random());
 			changed = true;
 		}
 
 		if (changed) {
 			resetVelocity(new Vector2D(vx, vy), true);
 		}
 		return changed;
 	}
 
 	public double getVectorAngle(Vector2D v) {
 		return Math.atan2(v.getY(), v.getX());
 	}
 
 	public void setTarget(Body b) {
 		this.target = b.getCenterPosition();
 	}
 
 	public Body getAgent() {
 		return agent;
 	}
 
 	public void setAgent(Body agent) {
 		this.agent = agent;
 	}
 
 	public Behavior getBehavior() {
 		return behavior;
 	}
 
 	public void setBehavior(Behavior behavior) {
 		this.behavior = behavior;
 	}
 
 	public double getWanderRadius() {
 		return wanderRadius;
 	}
 
 	public void setWanderRadius(double wanderRadius) {
 		this.wanderRadius = wanderRadius;
 	}
 
 	public double getWanderDistance() {
 		return wanderDistance;
 	}
 
 	public void setWanderDistance(double wanderDistance) {
 		this.wanderDistance = wanderDistance;
 	}
 
 	public double getWanderRate() {
 		return wanderRate;
 	}
 
 	public void setWanderRate(double wanderRate) {
 		this.wanderRate = wanderRate;
 	}
 
 	public double getWanderTheta() {
 		return wanderTheta;
 	}
 
 	public void setWanderTheta(double wanderTheta) {
 		this.wanderTheta = wanderTheta;
 	}
 
 	public Vector2D getSteerForce() {
 		return steerForce;
 	}
 
 	public void setSteerForce(Vector2D steerForce) {
 		this.steerForce = steerForce;
 	}
 
 	public Vector2D getTarget() {
 		return target;
 	}
 
 	public void setTarget(Vector2D target) {
 		this.target = target;
 	}
 
 	public void setMaxSteerForce(double maxSteerForce) {
 		this.maxSteerForce = maxSteerForce;
 	}
 
 	public double getMaxSteerForce() {
 		return maxSteerForce;
 	}
 
 	public void setSlowingDistance(double slowingDistance) {
 		this.slowingDistance = slowingDistance;
 	}
 
 	public double getSlowingDistance() {
 		return slowingDistance;
 	}
 
 	public void setTargetBound(double targetBound) {
 		this.targetBound = targetBound;
 	}
 
 	public double getTargetBound() {
 		return targetBound;
 	}
 
 	public void setMaxSpeed(double maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 
 	public double getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	public void setWorldbounds(Rectangle worldbounds) {
 		this.worldbounds = worldbounds;
 	}
 
 	public Rectangle getWorldbounds() {
 		return worldbounds;
 	}
 
 	public void setObstacles(BodyLayer<Body> obstacles) {
 		this.obstacles = obstacles;
 	}
 
 	public BodyLayer<Body> getObstacles() {
 		return obstacles;
 	}
 
 	public void setObstacleAvoidance(boolean obstacleAvoidance) {
 		this.obstacleAvoidance = obstacleAvoidance;
 	}
 
 	public boolean isObstacleAvoidance() {
 		return obstacleAvoidance;
 	}
 
 	public void setAvoidDistance(double avoidDistance) {
 		this.avoidDistance = avoidDistance;
 	}
 
 	public double getAvoidDistance() {
 		return avoidDistance;
 	}
 }
