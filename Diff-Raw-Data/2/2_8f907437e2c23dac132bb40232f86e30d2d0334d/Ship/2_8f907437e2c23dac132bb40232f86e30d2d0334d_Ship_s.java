 package asteroids;
 
 import be.kuleuven.cs.som.annotate.*;
 
 /**
 * A class for a ship describing it's current state.
  * 
  * @author Yannick Horvat (1ste bachelor Informatica) & Koen Jacobs (1ste bachelor Informatica)
  * Repository: https://github.com/KoenKolko/Project_X.git
  * @Version 1.0
  *
  */
 
 public class Ship implements IShip {
 
 	private double x;										// x-position of the ship (km)
 	private double y;										// y-position of the ship (km)
 	private double xVelocity;								// Velocity in x-direction (km/s)
 	private double yVelocity;								// Velocity in y-direction (km/s)
 	private double radius;									// Radius of the ship (km)
 	private double angle;									// The angle of the ship (radian)
 	private final double C = 300000;						// Speed of light (km/s)
 	private DoubleCalculator calc = new DoubleCalculator(); // A calculator to calc with Doubles.
 
 	/**
 	 * Creates a new ship with the given parameters.
 	 * 
 	 * @param x				x-position of the ship (km)
 	 * @param y				y-position of the ship (km)
 	 * @param xVelocity		Velocity in x-direction (km/s)
 	 * @param yVelocity		Velocity in y-direction (km/s)
 	 * @param radius		Radius of the ship (km)
 	 * @param angle			The angle of the ship (radian)
 	 * 
 	 * @post	The x-coordinate is set to the parameter x.
 	 * 			| (new this).getX() == x
 	 * @post 	The y-coordinate is set to the parameter y.
 	 * 			| (new this).getY() == y
 	 * @post 	The velocity on the x-axis is set to the parameter xVelocity.
 	 * 			| (new this).getXVelocity() == xVelocity
 	 * @post	The velocity on the y-axis is set to the parameter yVelocity.
 	 * 			| (new this).getYVelocity() == yVelocity
 	 * @post	The radius of the ship is set to the parameter radius.
 	 * 			| (new this).getRadius() == radius
 	 * @post 	The angle the ship is facing is set to the parameter angle.
 	 * 			| (new this).getAngle() == angle	
 	 * @post	The radius will be larger then 10
 	 * 			| (new this).getRadius > 10
 	 * 
 	 */
 	public Ship(double x, double y, double xVelocity, double yVelocity, double radius, double angle)
 	{		
 		this.setX(x);
 		this.setY(y);
 		this.setXVelocity(xVelocity);
 		this.setYVelocity(yVelocity);
 		this.setRadius(radius);
 		this.setAngle(angle);	
 
 	}
 
 	/**
 	 * Moves the ship during a given amount of time.
 	 * 
 	 * @param time		The duration of the current move. (seconds)
 	 * 
 	 * @post	The x-coordinate is set to the new position after move.
 	 * 			| (new this).getX() == getX() + (getXVelocity()*time)
 	 * 
 	 * @post 	The y-coordinate is set to the new position after move.
 	 * 			| (new this).getY() == getY() + (getYVelocity()*time)
 	 * 
 	 * @throws IllegalArgumentException
 	 * 			The time is not a legal parameter for this method.
 	 * 			| !isValidTime(time)
 	 */
 	public void move (double time) throws IllegalArgumentException {
 		if (!isValidTime(time))
 			throw new IllegalArgumentException();
 
 		setX(calc.addDoubles(getX(), calc.multiplyDoubles(getXVelocity(), time)));					// x = x + velocity*time
 		setY(calc.addDoubles(getY(), calc.multiplyDoubles(getYVelocity(), time)));					// y = y + velocity*time
 
 	}
 
 	/**
 	 * 
 	 * @param time	The time that has to be checked.
 	 * @return	
 	 * 		Returns if the time is valid.
 	 * 		| !(Double.isNaN(time) || time < 0)  
 	 */
 	public boolean isValidTime (double time) { 
 		return !(Double.isNaN(time) || time < 0);
 	}
 
 
 	/**
 	 * Turns the ship by a given angle.
 	 * 
 	 * @param angle		The angle the ship will turns. (radians)
 	 * 
 	 * @pre		The angle is a number.
 	 * 			| isValidAngle(angle)
 	 * @post 	The angle is set to the old angle increased by the parameter angle.
 	 * 			| (new this).getAngle() == (getAngle() + angle)%(2*Math.PI)
 	 */
 	public void turn (double angle) {
 		assert (isValidAngle(angle)) : "No valid argument!";
 		setAngle(calc.addDoubles(getAngle(), angle));
 	}
 
 	/**
 	 * 
 	 * @param angle		The angle that has to be checked.
 	 * @return
 	 * 		Returns if the angle is valid.
 	 * 		| !Double.isNaN(angle)
 	 */
 	public boolean isValidAngle (double angle) {
 		return !Double.isNaN(angle);
 	}
 
 
 	/**
 	 * 
 	 * @param amount	The amount by which the velocity is increased.
 	 * @post 	If the given amount is invalid (< 0 or NaN) or the amount is zero, then the velocity will remain unchanged.
 	 * 			Else if the new velocity is greater than the speed of light (C), then the velocity in the x and y-axis will be adjusted
 	 * 			so that the velocity equals the speed of light (C).
 	 * 			Else, the new velocity is smaller or equal to speed of light (C), then the new x and y velocities are calculated 
 	 * 			and adjusted.
 	 * 			| if (Double.isNaN(amount) || amount <= 0)
 	 * 			|	then return;
 	 * 			| if (calcVelocity(vXNew, vYNew) > C)
 	 * 			|	then (new this).calcVelocity(getXVelocity(), getYVelocity()) == C
 	 *			| else 
 	 *			| 	then (new this).getXVelocity() = vXNew && (new this).getYVelocity() = vYNew 
 	 *			|	(new this).calcVelocity(getXVelocity(), getYVelocity()) <= C
 	 * 			 		
 	 */
 	public void thrust (double amount) {
 		if(Double.isNaN(amount) || amount <= 0)
 			return;
 		double vXNew = calc.addDoubles(getXVelocity(), calc.multiplyDoubles(amount, Math.cos(getAngle())));		// the new x-velocity
 		double vYNew = calc.addDoubles(getYVelocity(), calc.multiplyDoubles(amount, Math.sin(getAngle())));		// the new y-velocity
 		if (calcVelocity(vXNew, vYNew) > C){ // if (speed > 300 000km/s)
 			double constant = Math.sqrt(  calc.multiplyDoubles(C, C)    /   calc.addDoubles(calc.multiplyDoubles(getXVelocity(), getXVelocity()), calc.multiplyDoubles(getYVelocity(), getYVelocity()))  ); // constant multiple, so the new speed will be C.
 			vXNew = calc.multiplyDoubles(getXVelocity(), constant);
 			vYNew = calc.multiplyDoubles(getYVelocity(), constant);
 		}
 		setXVelocity(vXNew); 
 		setYVelocity(vYNew);
 	}
 
 	/**
 	 * Checks the radius of the ship.
 	 * 
 	 * @param radius	The new radius of this ship.	
 	 * @return	
 	 * 		Returns if the radius is valid.
 	 * 		| !Double.isNaN(radius) || radius >= 10
 	 */
 	private boolean isValidRadius (double radius)
 	{
 		return (!Double.isNaN(radius) && radius >= 10);
 	}
 
 	/**
 	 * 
 	 * @return
 	 * 		Returns the x-coordinate of the ship.
 	 * 
 	 */
 	@Basic
 	public double getX() {
 		return x;
 	}
 
 	/**
 	 * 
 	 * @param x		The new x-coordinate
 	 * @post 		X-position has been set to x.
 	 * 				| (new this).getX() == x
 	 * @throws IllegalArgumentException
 	 * 		The entered x-parameter is invalid.
 	 * 		| Double.isNaN(x)
 	 */
 	public void setX(double x) throws IllegalArgumentException {
 		if (Double.isNaN(x))
 			throw new IllegalArgumentException("Invalid x-coordinate");
 		this.x = x;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * 		Returns the y-coordinate of the ship.
 	 */
 	@Basic
 	public double getY() {
 		return y;
 	}
 
 	/**
 	 * 
 	 * @param y		The new y-coordinate
 	 * @post 		Y-position has been set to y.
 	 * 				| (new this).getY() == y
 	 * @throws IllegalArgumentException
 	 * 				The entered y-parameter is invalid.
 	 * 				| Double.isNaN(y)
 	 */
 	public void setY(double y) throws IllegalArgumentException {
 		if (Double.isNaN(y))
 			throw new IllegalArgumentException("Invalid y-coordinate");
 		this.y = y;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * 		Returns the velocity of the ship in the x-axis.
 	 */
 	@Basic
 	public double getXVelocity() {
 		return xVelocity;
 	}
 
 	/**
 	 * 
 	 * @param xVelocity		The new x-velocity of the ship.
 	 * @post 	If xVelocity is not a number, the velocity is set to zero.
 	 * 			Else, the x-velocity is set to xVelocity.
 	 * 			| if (Double.isNaN(xVelocity))
 	 * 			| 	then (new this).getXVelocity == 0
 	 * 			| else (new this).getXVelocity == xVelocity
 	 */
 	public void setXVelocity(double xVelocity) {
 		if (Double.isNaN(xVelocity))
 			this.xVelocity = 0;
 		else this.xVelocity = xVelocity;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * 		Returns the velocity of the ship in the y-axis.
 	 */
 	@Basic
 	public double getYVelocity() {
 		return yVelocity;
 	}
 
 	/**
 	 * 
 	 * @param yVelocity		The new y-velocity of the ship.
 	 * @post 	If yVelocity is not a number, the velocity is set to zero.
 	 * 			Else, the y-velocity is set to yVelocity.
 	 * 			| if (Double.isNaN(yVelocity))
 	 * 			| 	then (new this).getYVelocity == 0
 	 * 			| else (new this).getYVelocity == yVelocity
 	 */
 	public void setYVelocity(double yVelocity) {
 		if (Double.isNaN(yVelocity))
 			this.yVelocity = 0;
 		else this.yVelocity = yVelocity;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * 		Returns the angle the ship is faced to.
 	 */
 	@Basic
 	public double getAngle() {
 		return angle;
 	}
 
 	/**
 	 * 
 	 * @param angle 	The new angle.
 	 * 
 	 * @pre				The angle has to be valid.
 	 * 					| isValidAngle(angle)
 	 * 
 	 * @post 			The new angle is angle.
 	 * 					| if (angle > 2*Math.PI)
 	 * 					| 	then (new this).getAngle() == angle%(2*Math.PI)
 	 * 					| else if angle < -2*Math.PI
 	 * 					| 	then (new this).getAngle() == angle%(-2*Math.PI)
 	 */
 	public void setAngle(double angle) {
 		assert isValidAngle(angle) : "Wrong angle";
 		if (angle > 2*Math.PI)
 			angle %= 2*Math.PI;
 		else if ( angle < -2*Math.PI)
 			angle %= -2*Math.PI;
 		this.angle = angle;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * 		Returns the radius of the ship.
 	 */
 	@Basic
 	public double getRadius() {
 		return radius;
 	}
 
 	/**
 	 * 
 	 * @param radius	The new radius.
 	 * @post 			The new radius is equal to radius.
 	 * 					|(new this).getRadius() == radius
 	 * @throws IllegalArgumentException
 	 * 					The parameter radius is invalid.
 	 * 					| !isValidRadius(radius)
 	 */
 	public void setRadius(double radius) throws IllegalArgumentException {
 		if (!isValidRadius(radius))
 			throw new IllegalArgumentException("Invalid radius!");
 		this.radius = radius;
 	}
 
 	/**
 	 * Calculates the velocity of a given x and y velocity.
 	 * 
 	 * @param x		The x-velocity.
 	 * @param y		The y-velocity.
 	 * @return		If one of the parameters is not a number, return 0.0. 
 	 * 				Else, return the velocity.
 	 *				| if ( Double.isNaN(x) || Double.isNaN(y) )
 	 *				| 	then return 0.0
 	 *				| else return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2))
 	 */
 	public double calcVelocity(double x, double y) {
 		if (Double.isNaN(x) || Double.isNaN(y))
 			return 0.0;
 		return Math.sqrt(calc.multiplyDoubles(x, x)+ calc.multiplyDoubles(y, y));
 	}
 
 
 	/**
 	 * 
 	 * @param otherShip		The ship to be compared to.
 	 * @return
 	 * 		The distance between this ship and the other ship.
 	 * 		| Math.sqrt(Math.pow(this.getXDistanceBetween(otherShip), 2) + Math.pow(this.getYDistanceBetween(otherShip), 2))
 	 * @throws IllegalArgumentException
 	 * 		The other ship doesn't exist.
 	 * 		| otherShip == null
 	 */
 	public double getDistanceBetween(Ship otherShip) throws IllegalArgumentException {
 		if (otherShip == null) // The other ship doesn't exist.
 			throw new IllegalArgumentException("Invalid ship!");
 		return Math.sqrt(calc.addDoubles(calc.multiplyDoubles(this.getXDistanceBetween(otherShip), this.getXDistanceBetween(otherShip)),
 				calc.multiplyDoubles(this.getYDistanceBetween(otherShip), this.getYDistanceBetween(otherShip)))); 								// The distance between the two centers.
 	}
 
 	/**
 	 * 
 	 * @param otherShip
 	 * @return
 	 * 		Calculates the x-distance between this ship and the other ship.
 	 */
 	private double getXDistanceBetween(Ship otherShip){
 		return calc.addDoubles(getX(), -otherShip.getX()); // The x-distance between the two ships.
 	}
 
 	/**
 	 * 
 	 * @param otherShip
 	 * @return
 	 * 		Calculates the y-distance between this ship and the other ship.
 	 */
 
 	private double getYDistanceBetween(Ship otherShip){
 		return calc.addDoubles(getY(), -otherShip.getY()); // The y-distance between the two ships.
 	}
 
 	/**
 	 * 
 	 * @param otherShip
 	 * @return	
 	 * 		Calculates delta R squared.
 	 */
 	private double calcDeltaRSquared(Ship otherShip){
 		double deltaX = getXDistanceBetween(otherShip);
 		double deltaY = getYDistanceBetween(otherShip);
 		return calc.addDoubles(calc.multiplyDoubles(deltaX, deltaX), calc.multiplyDoubles(deltaY, deltaY));
 	}
 	/**
 	 * 
 	 * @param otherShip
 	 * @return
 	 * 		Calculates delta V squared.
 	 */
 	private double calcDeltaVSquared(Ship otherShip){
 		double deltaVX = calc.addDoubles(getXVelocity(), -otherShip.getXVelocity());
 		double deltaVY = calc.addDoubles(getYVelocity(), -otherShip.getYVelocity());
 		return calc.addDoubles(calc.multiplyDoubles(deltaVX, deltaVX), calc.multiplyDoubles(deltaVY, deltaVY));
 	}
 
 	/**
 	 * 
 	 * @param otherShip
 	 * @return
 	 * 		Calculates delta V times delta R.
 	 */
 	private double calcDeltaVDeltaR(Ship otherShip){
 		double deltaX = getXDistanceBetween(otherShip);
 		double deltaY = getYDistanceBetween(otherShip);
 		double deltaVX = calc.addDoubles(getXVelocity(), -otherShip.getXVelocity());
 		double deltaVY = calc.addDoubles(getYVelocity(), -otherShip.getYVelocity());
 		return calc.addDoubles(calc.multiplyDoubles(deltaVX, deltaX), calc.multiplyDoubles(deltaVY, deltaY));
 	}
 
 	/**
 	 * 
 	 * @param otherShip		The other ship to be compared to.
 	 * @throws IllegalArgumentException
 	 * 						The other ship doesn't exist.
 	 * 						| otherShip == null
 	 * @return
 	 * 						Returns if the ships overlap each other.
 	 * 						this.getRadius() + otherShip.getRadius() > this.getDistanceBetween(otherShip)
 	 */
 	public boolean overlap(Ship otherShip){
 		if (otherShip == null) 		// The other ship doesn't exist.
 			throw new IllegalArgumentException("Invalid ship!");
 		return(calc.addDoubles(getRadius(), otherShip.getRadius()) > this.getDistanceBetween(otherShip)); // If the distance is smaller than the sum of the radii, the ships overlap.
 	}	
 
 	/**
 	 * 
 	 * @param otherShip 	The other ship.
 	 * @throws IllegalArgumentException
 	 * 						The other ship doesn't exist.
 	 * 						| otherShip == null
 	 * @return
 	 * 						Returns the time before the ships collide.
 	 * 						| if(this.overlap(otherShip))
 	 *						| 	then return Double.POSITIVE_INFINITY
 	 *						| else if(Double.compare(d,0) <= 0) 
 	 *						| 	then return Double.POSITIVE_INFINITY
 	 *						| else if(Double.compare(VR,0) >=0)
 	 *						| 	then return Double.POSITIVE_INFINITY
 	 *						| else
 	 *						| 	then return -( (VR+Math.sqrt(d)) / VV)
 	 */
 	public double getTimeToCollision(Ship otherShip){	
 		if (otherShip == null) // The other ship doesn't exist.
 			throw new IllegalArgumentException("Invalid ship!");
 		double sigma = calc.addDoubles(getRadius(), otherShip.getRadius());
 		double VR = calcDeltaVDeltaR(otherShip);
 		double RR = calcDeltaRSquared(otherShip);
 		double VV = calcDeltaVSquared(otherShip);
 		double d = calc.addDoubles(calc.multiplyDoubles(VR, VR), -calc.multiplyDoubles(VV, calc.addDoubles(RR, -calc.multiplyDoubles(sigma, sigma))));
 		if(this.overlap(otherShip))
 			return Double.POSITIVE_INFINITY; 	// The ships overlap.
 		else if(Double.compare(d,0) <= 0)
 			return Double.POSITIVE_INFINITY; 	// The ships will not collide.
 		else if(Double.compare(VR,0) >=0)
 			return Double.POSITIVE_INFINITY;		
 		else
 			return -(calc.addDoubles(VR, Math.sqrt(d)) / VV); 	// Calculate the time to collision.
 	}
 
 	/**
 	 * 
 	 * @param otherShip		The ship that could collide with the ship.
 	 * @throws IllegalArgumentException
 	 * 		The other ship doesn't exist.
 	 * 		| otherShip == null
 	 * @return
 	 * 		Returns the position where the 2 ships will collide. It returns null if they won't.
 	 * 		| if(timeToCollision != Double.POSITIVE_INFINITY)		
 	 *  	| 	double[] positions = new double[2]
 	 * 		| 	positions[0] = (thisX * otherShip.getRadius() + otherX * getRadius())/sumRadii
 	 *		|	positions[1] = (thisY * otherShip.getRadius() + otherY * getRadius())/sumRadii
 	 *		|	return positions
 	 *		| else return null
 	 */
 	public double[] getCollisionPosition(Ship otherShip)
 	{
 		if (otherShip == null) // The other ship doesn't exist.
 			throw new IllegalArgumentException("Invalid ship!");
 		double timeToCollision = this.getTimeToCollision(otherShip);
 		if(timeToCollision != Double.POSITIVE_INFINITY){			
 			double[] positions = new double[2];
 			double thisX = calc.addDoubles(getX(), calc.multiplyDoubles(timeToCollision, getXVelocity()));
 			double otherX = calc.addDoubles(otherShip.getX(), calc.multiplyDoubles(timeToCollision, otherShip.getXVelocity()));
 			double thisY = calc.addDoubles(getY(), calc.multiplyDoubles(timeToCollision, getYVelocity()));
 			double otherY = calc.addDoubles(otherShip.getY(), calc.multiplyDoubles(timeToCollision, otherShip.getYVelocity()));
 			double sumRadii = this.getRadius() + otherShip.getRadius();
 			positions[0] = (thisX * otherShip.getRadius() + otherX * getRadius())/sumRadii; 
 			positions[1] = (thisY * otherShip.getRadius() + otherY * getRadius())/sumRadii; 
 			return positions;
 		}
 		else return null;
 	}
 }
 
