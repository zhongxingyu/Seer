 package edu.harvard.seas.cs266.naptime;
 
 import sim.engine.SimState;
 import sim.engine.Steppable;
 import sim.field.continuous.Continuous2D;
 import sim.portrayal.Oriented2D;
 import sim.util.Double2D;
 
 @SuppressWarnings("serial")
 public class Robot implements Steppable, Oriented2D {
 	/**
 	 * The radius of the round robots.
 	 */
 	public static final double robotSize = 12.0;
 	
 	/**
 	 * The parent team of this robot.
 	 */
 	private Team parent = null;
 	
 	/**
 	 * The current facing of the robot. Its front is where the camera and kicker are.
 	 */
 	private double orientation = 0.0;
 	
 	/**
 	 * The robot's current range sensor readings, arranged in 16 radial sectors.
 	 */
 	private double ranges[] = new double[16];
 	
 	/**
 	 * The robot's current "camera" view, a single line of 30 pixels, storing
 	 * references to the "seen" object.
 	 */
 	private Object camera[] = new Object[30];
 	
 	/**
 	 * The depth buffer used to calculate occlusion in the camera. Kept
 	 * around so we don't have to recalculate distances.
 	 */
 	double[] depthBuffer = new double[30];
 
 	/**
 	 * The speed (in units per step) of the robot's left motor.
 	 */
 	private double leftSpeed = 0.0;
 	
 	/**
 	 * The speed (in units per step) of the robot's right motor.
 	 */
 	private double rightSpeed = 0.0;
 	
 	/**
 	 * The possible states of the robot. Kept small for easier evolution.
 	 */
 	public enum State {
 		SEARCH,
 		CARRY,
 	}
 	
 	/**
 	 * The robot's current state. Defaults to State.SEARCH.
 	 */
 	private State state = State.SEARCH;
 	
 	/**
 	 * The robot's current payload, if any.
 	 */
 	private Treat carrying = null;
 
 	/**
 	 * Sets up the robot with initial facing.
 	 * 
 	 * @param team The team containing this robot.
 	 * @param startAngle The robot's starting orientation
 	 */
 	public Robot(Team team, double startAngle) {
 		this.parent = team;
 		this.orientation = startAngle;
 	}
 	
 	/**
 	 * Increments the simulated state of this robot one time step.
 	 * 
 	 * @param state The current Tournament simulation.
 	 */
 	@Override
 	public void step(SimState state) {
 		// Get the current simulation
 		Tournament tourney = (Tournament) state;
 		
 		// Check if a carried object has dropped into the goal (based on proximity, kinda cheaty)
 		if (carrying != null) {
 			Double2D carriedPosition = tourney.field.getObjectLocation(carrying);
 			if ((carriedPosition.x < 4 || carriedPosition.x > tourney.field.getWidth() - 4) &&
 				carriedPosition.y > (tourney.field.getHeight() - Goal.goalSize)/2 &&
 				carriedPosition.y < (tourney.field.getHeight() + Goal.goalSize)/2) {
 				// Drop it
 				tourney.field.remove(carrying);
 				carrying = null;
 				this.state = State.SEARCH;
 				
 				// Update the score
 				tourney.score[parent.opposing ? 1 : 0]++;
 			}
 		}
 		
 		// If we just dropped off the last piece of food, end the simulation prematurely
 		Boolean done = true;
 		for (Object treat: tourney.field.getAllObjects()) {
 			if (treat.getClass() == Treat.class) {
 				done = false;
 				break;
 			}
 		}
 		if (done) {
 			state.kill();
 			return;
 		}
 		
 		// Update the sensor state
 		updateRanges(tourney.field);
 		updateCamera(tourney.field);
 		
 		// Execute this robot's loaded step program (currently team-wide)
 		try {
 			parent.strategy.eval(this);
 		} catch (InvalidSexpException e) {
 			// Bad step program, stop simulating
 			System.err.println(e.getMessage());
 			System.err.println(parent.strategy.toString());
 			state.kill();
 			return;
 		}
 		
 		// Run the "motors" at their current speed settings
 		move(tourney.field);
 	}
 	
 	/**
 	 * Update the robot's internal range sensor readings with
 	 * the closest obstacles on all sides.
 	 */
 	private void updateRanges(Continuous2D field) {
 		// Get the robot's current location
 		Double2D current = field.getObjectLocation(this);
 		
 		// Reset the sensors
 		for (int r = 0; r < 16; r++)
 			ranges[r] = Double.MAX_VALUE;
 		
 		// Find the closest object to each sensor
 		for (Object obstacle: field.getAllObjects()) {
 			if (obstacle.getClass() == Robot.class && obstacle != this) {
 				// Get the relative position vector for this obstacle
 				Double2D position = field.getObjectLocation(obstacle).subtract(current).rotate(-orientation);
 				
 				// Get the angle to the obstacle
 				double obstacleAngle = Math.atan2(position.y, position.x);
 			
 				// Determine which sensor by which it would be seen
 				int sensor = (((int) Math.round(obstacleAngle*8/Math.PI)) + 16) % 16;
 				
				// Update the closest obstacle
				double distance = position.length() - Robot.robotSize;
 				if (distance < ranges[sensor])
 					ranges[sensor] = distance;
 			}
 		}
 		
 		// Update distance to closest wall for each sensor, if there isn't a closer obstacle
		for (int r = 0; r < 16; r++)
 			if (ranges[r] == Double.MAX_VALUE) {
 				double sensorAngle = (orientation + r*Math.PI/8) % (2*Math.PI);
 				if (sensorAngle > Math.PI)
 					sensorAngle -= 2*Math.PI;
 				if (sensorAngle >= -Math.PI/4 && sensorAngle < Math.PI/4)
 					ranges[r] = (field.getWidth() - current.x)/Math.cos(sensorAngle);
 				else if (sensorAngle >= Math.PI/4 && sensorAngle < 3*Math.PI/4)
 					ranges[r] = (field.getHeight() - current.y)/Math.cos(sensorAngle - Math.PI/2);
 				else if (sensorAngle >= 3*Math.PI/4)
 					ranges[r] = current.x/Math.cos(sensorAngle - Math.PI);
 				else if (sensorAngle < -3*Math.PI/4)
 					ranges[r] = current.x/Math.cos(sensorAngle + Math.PI);
 				else if (sensorAngle >= -3*Math.PI/4 && sensorAngle < -Math.PI/4)
 					ranges[r] = current.y/Math.cos(sensorAngle + Math.PI/2);
 			}
 	}
 	
 	/**
 	 * Update the robot's internal camera buffer with the closest
 	 * object currently in view.
 	 */
 	private void updateCamera(Continuous2D field) {
 		// Calculate some camera constants
 		final double imageWidth = Math.tan(Math.PI/6)*robotSize;
 		
 		// Get the robot's current location
 		Double2D current = field.getObjectLocation(this);
 		
 		// Find the closest object of the specified type within the field of view
 		for (int pixel = 0; pixel < 30; pixel++) {
 			depthBuffer[pixel] = Double.MAX_VALUE;
 			camera[pixel] = null;
 		}
 		for (Object objective: field.getAllObjects()) {
 			if ((state == State.SEARCH && objective.getClass() == Treat.class) ||
 				(state == State.CARRY && objective == parent.goal) ||
 				objective.getClass() == Robot.class) {
 				// Make sure this treat isn't already being carried
 				if (objective.getClass() == Treat.class && ((Treat) objective).carried)
 					continue;
 				
 				// Get the relative position vector for this objective
 				Double2D position = field.getObjectLocation(objective).subtract(current).rotate(-orientation);
 				
 				// Make sure the objective is in front
 				double objectiveAngle = Math.atan2(position.y, position.x);
 				if (objectiveAngle < -Math.PI/2 || objectiveAngle > Math.PI/2)
 					continue;
 				
 				// Determine the span of the object in the image plane
 				double imagePlaneLeft = 0, imagePlaneRight = 0;
 				if (objective.getClass() == Treat.class) {
 					// Treats are round, so edges are based on radius
 					imagePlaneLeft = (position.y - Treat.treatSize/2)*(robotSize/2)/position.x;
 					imagePlaneRight = (position.y + Treat.treatSize/2)*(robotSize/2)/position.x;				
 				} else if (objective.getClass() == Robot.class) {
 					// Robots are round, so edges are based on radius
 					imagePlaneLeft = (position.y - robotSize/2)*(robotSize/2)/position.x;
 					imagePlaneRight = (position.y + robotSize/2)*(robotSize/2)/position.x;				
 				} else if (objective.getClass() == Goal.class) {
 					// Goal is tall, so reproject each end
 					Double2D halfGoal = new Double2D(0, Goal.goalSize/2);
 					Double2D leftPost, rightPost;
 					if (field.getObjectLocation(objective).x == 0.0) {
 						leftPost = field.getObjectLocation(objective).add(halfGoal).subtract(current).rotate(-orientation);
 						rightPost = field.getObjectLocation(objective).subtract(halfGoal).subtract(current).rotate(-orientation);
 					} else {
 						leftPost = field.getObjectLocation(objective).subtract(halfGoal).subtract(current).rotate(-orientation);
 						rightPost = field.getObjectLocation(objective).add(halfGoal).subtract(current).rotate(-orientation);						
 					}
 					imagePlaneLeft = leftPost.y*(robotSize/2)/leftPost.x;
 					imagePlaneRight = rightPost.y*(robotSize/2)/rightPost.x;
 				}
 				
 				// Convert into pixels
 				int pixelLeft = (int) Math.round(imagePlaneLeft*30/imageWidth) + 14;
 				int pixelRight = (int) Math.round(imagePlaneRight*30/imageWidth) + 14;
 				
 				// Keep in bounds
 				if (pixelLeft < 0)
 					pixelLeft = 0;
 				if (pixelRight > 29)
 					pixelRight = 29;
 				
 				// Update the depth buffer and camera where not obscured
 				double distance = position.length();
 				for (int pixel = pixelLeft; pixel <= pixelRight; pixel++) {
 					if (distance < depthBuffer[pixel]) {
 						depthBuffer[pixel] = distance;
 						camera[pixel] = objective.getClass() != Robot.class ? objective : null;
 					}
 				}
 			}
 		}
 		
 		// "segment" by removing all but the front-most object
 		double minDistance = Double.MAX_VALUE;
 		for (int pixel = 0; pixel < 30; pixel++) {
 			// Look for the closest unobscured non-robot
 			if (depthBuffer[pixel] < minDistance && camera[pixel] != null)
 				minDistance = depthBuffer[pixel];
 		}
 		for (int pixel = 0; pixel < 30; pixel++)
 			if (depthBuffer[pixel] != minDistance)
 				camera[pixel] = null;
 	}
 	
 	/**
 	 * Hacky method for finding the midpoint of the object in view.
 	 */
 	public int findMidpointOfObjectiveInView() {
 		// Loop through the camera buffer, checking for object boundaries
 		int pixelLeft = -1;
 		int pixelRight = -1;
 		for (int pixel = 0; pixel < 30; pixel++) {
 			if (pixelLeft == -1 && camera[pixel] != null)
 				pixelLeft = pixel;
 			if (pixelLeft != -1 && pixelRight == -1 && camera[pixel] == null)
 				pixelRight = pixel - 1;
 		}
 		
 		// Check for edge-crossing and calculate the midpoint
 		if (pixelLeft == -1)
 			return 0;
 		else if (pixelRight == -1)
 			return (pixelLeft + 29)/2;
 		else
 			return (pixelLeft + pixelRight)/2;
 	}
 
 	/**
 	 * Hacky method for finding the width of the object in view.
 	 */
 	public int findWidthOfObjectiveInView() {
 		// Loop through the camera buffer, checking for object boundaries
 		int pixelLeft = -1;
 		int pixelRight = -1;
 		for (int pixel = 0; pixel < 30; pixel++) {
 			if (pixelLeft == -1 && camera[pixel] != null)
 				pixelLeft = pixel;
 			if (pixelLeft != -1 && pixelRight == -1 && camera[pixel] == null)
 				pixelRight = pixel - 1;
 		}
 		
 		// Check for edge-crossing and calculate the width
 		if (pixelLeft == -1)
 			return 0;
 		else if (pixelRight == -1)
 			return 30 - pixelLeft;
 		else
 			return pixelRight - pixelLeft;
 	}
 
 	/**
 	 * Rudimentary accessor to expose the camera buffer to the inspector.
 	 * 
 	 * @return A string where '*' represents the object in view.
 	 */
 	public String getCamera() {
 		// Loop through the camera buffer, appending
 		String image = "";
 		for (Object pixel: camera)
 			if (pixel != null)
 				image += "*";
 			else
 				image += "-";
 		return image;
 	}
 	
 	public String getRanges() {
 		String rangeString = "";
 		for (double range: ranges)
 			if (range == Double.MAX_VALUE)
 				rangeString += "--";
 			else
 				rangeString += String.format("%.2f ", range);
 		return rangeString;
 	}
 	
 	/**
 	 * Uses the current speed to adjust the robot's position and orientation.
 	 */
 	private void move(Continuous2D field) {
 		final double minTreatDistance = (Robot.robotSize + Treat.treatSize)/2;
 		
 		// Update the orientation based on relative wheel velocity
 		orientation -= (rightSpeed - leftSpeed)/robotSize;
 		
 		// Keep orientation in [-pi,pi] range even though Oriented2D mods correctly
 		if (orientation > Math.PI)
 			orientation -= 2*Math.PI;
 		else if (orientation < -Math.PI)
 			orientation += 2*Math.PI;
 		
 		// Update the position based on midpoint speed and new orientation
 		double midpointSpeed = (rightSpeed + leftSpeed)/2;
 		Double2D direction = new Double2D(Math.cos(orientation), Math.sin(orientation));
 		field.setObjectLocation(this, field.getObjectLocation(this).add(direction.multiply(midpointSpeed)));
 		
 		// Check for collisions
 		for (Object obstacle: field.getObjectsWithinDistance(field.getObjectLocation(this), Robot.robotSize, false, true)) {
 			Double2D obstaclePosition = field.getObjectLocation(obstacle);
 			Double2D position = field.getObjectLocation(this);
 			if (obstacle.getClass() == Robot.class) {
 				if (position.distance(obstaclePosition) < Robot.robotSize) {
 					// "Bounce" off the obstacle
 					field.setObjectLocation(this, obstaclePosition.add(position.subtract(obstaclePosition).resize(Robot.robotSize)));
 				}
 			} else if (obstacle.getClass() == Treat.class && obstacle != carrying) {
 				if (position.distance(obstaclePosition) < minTreatDistance) {
 					// "Shove" the treat
 					field.setObjectLocation(obstacle, position.add(obstaclePosition.subtract(position).resize(minTreatDistance)));
 				}
 			}
 		}
 		
 		// If we're carrying something, update its position too
 		if (carrying != null)
 			field.setObjectLocation(carrying, field.getObjectLocation(this).add(direction.multiply(minTreatDistance)));
 	}
 	
 	/**
 	 * Mutator to update the current motor speeds of the robot.
 	 * 
 	 * @param left The desired speed of the left motor.
 	 * @param right The desired speed of the right motor.
 	 */
 	protected void setSpeed(double left, double right) {
 		final double maxSpeed = 0.6;
 		if (left < -maxSpeed)
 			leftSpeed = -maxSpeed;
 		else if (left > maxSpeed)
 			leftSpeed = maxSpeed;
 		else
 			leftSpeed = left;
 		if (right < -maxSpeed)
 			rightSpeed = -maxSpeed;
 		else if (right > maxSpeed)
 			rightSpeed = maxSpeed;
 		else
 			rightSpeed = right;
 	}
 	
 	/**
 	 * Updates what the robot is carrying.
 	 * 
 	 * @return True on success, false otherwise (already carrying something,
 	 * not a treat, treat out of range, etc.).
 	 */
 	public Boolean pickUpObjective() {
 		// Pick up food if possible
 		if (carrying == null) {
 			for (int pixel = 0; pixel < 30; pixel++)
 				if (camera[pixel] != null && camera[pixel].getClass() == Treat.class &&
 				    depthBuffer[pixel] < Robot.robotSize*0.75) {
 					carrying = (Treat) camera[pixel];
 					carrying.carried = true;
 					state = State.CARRY;
 					return true;
 				}
 		}
 		return false;
 	}
 
 	/**
 	 * Accessor for the robot's current facing. Implements the Oriented2D interface.
 	 */
 	@Override
 	public double orientation2D() {
 		return orientation;
 	}
 
 	/**
 	 * Accessor for the robot's range sensors.
 	 * 
 	 * @param sensor Index into the robot's range sensor array. Positive clockwise.
 	 * @return The current distance to an obstacle as seen by the specified sensor.
 	 */
 	public double getRange(int sensor) {
 		return ranges[sensor];
 	}
 	
 	/**
 	 * Accessor for the robot's state.
 	 * 
 	 * @return True if the robot's state matches the specified state.
 	 */
 	public Boolean inState(State state) {
 		return this.state == state;
 	}
 }
