 
 package main;
 
 import asteroids.IShip;
 import be.kuleuven.cs.som.annotate.*;
 import be.kuleuven.cs.som.taglet.*;
 
 /**
  * A class of ships that have a position, velocity, radius, and direction.
  * Ships can move, apply thrust and turn. The class also provides functionality for collision detection.
  * 
  * @invar A ship's speed limit can never exceed the speed of light (c=300000 km/s).
  * 		  | getSpeedLimit =< 300000
  * 
  * @invar A ship's speed can never exceed it's speed limit. 
  * 	      | getVelocity =< getSpeedLimit
  * 
  * @author Jasper, Tom
  * @version 0.1
  */
 
 public class Ship implements IShip {
 	
 	/**
 	 * Creates a new Ship with default parameters.
 	 */
 	//TODO Specificatie + Implementatie
 	public Ship(){
 		
 	}
 	
 	/**
 	 * Creates a new Ship with user defined parameters.
 	 * @param x	The initial x-coordinate of the ship, expressed in km.
 	 * @param y	The initial y-coordinate of the ship, expressed in km.
 	 * @param xVelocity The initial velocity in the x-direction the ship will have, expressed in km/s.
 	 * @param yVelocity The initial velocity in the y-direction the ship will have, expressed in km/s.
 	 * @param radius The ship's radius, expressed in m.
 	 * @param angle The direction the ship will face,expressed in radians (0 is to the right).
 	 */
 	/* TODO Afwerken Specificatie + Implementatie; 
 	 * TODO Test radius; 
 	 * TODO Test snelheid 
 	 */
 	
 	public Ship(double x, double y, double xVelocity,
 			double yVelocity, double radius, double angle) {
 		
 		this.xPos = x;
 		this.yPos = y;
 		this.xVelocity = xVelocity;				//Testen op geldoge snelheid
 		this.yVelocity = yVelocity;
 		this.radius = radius; 					//Hier moet wel nog getest worden of de radius correct is
 		this.angle = angle;
 				
 		}
 	
 	/**
 	 * Returns the current x-coordinate of the ship in km.
 	 * @return The ship's current x-coordinate in km.
 	 */
 	@Basic
 	public double getX() {
 		
 		return xPos;
 	}
 	
 	/**
 	 * Returns the current y-coordinate of the ship in km.
 	 * @return The ship's current x-coordinate in km.
 	 */
 	@Basic
 	public double getY() {
 		
 		return yPos;
 	}
 	
 	
 	/**
 	 * Moves the ship for a duration dt according to its current state.
 	 * The ship will be moved according to its current position and its current velocity during the specified duration <code>dt</code>.
 	 * The specified duration <code>dt</code> can never be less than zero.
 	 * @param dt Specifies the duration of the move command.
 	 * @post The new xPos (yPos) is equal to the old xPos (yPos) changed by the xVelocity (yVelocity) multiplied by the given duration. 
 	 * 		| new this.getX() = this.getX() + getXVelocity*dt 
 	 * 		| new this.getY() = this.getY() + getYVelocity*dt
 	 * @throws 		 
 	 */
 	//TODO spec + exception!
 	public void move(double dt) {
 		
 		xPos = xPos + this.getXVelocity()*dt;
 		yPos = yPos + this.getYVelocity()*dt;
 		
 	}
 	
 	/**
 	 * The ship's current position along the x-axis.
 	 */
 	private double xPos;
 	
 	/**
 	 * The ship's current position along the y-axis.
 	 */
 	private double yPos;
 	
 	
 	
 	/**
 	 * Returns the ship's current velocity in the x-direction, expressed in km/s.
 	 * @return The ship's current velocity in the x-direction(km/s).
 	 */
 	@Basic
 	public double getXVelocity() {
 		
 		return xVelocity;
 	}
 	/**
 	 * Returns the ship's current velocity in the y-direction, expressed in km/s.
 	 * @return The ship's current velocity in the y-direction(km/s).
 	 */
 	@Basic
 	public double getYVelocity() {
 		
 		return yVelocity;
 	}
 	
 	/**
 	 * Returns the ship's total velocity (in km/s).
 	 * @return The ship's total velocity.
 	 */
 	public double getVelocity() {
 		
 		return Math.sqrt(Math.pow(getXVelocity(),2)+Math.pow(getYVelocity(),2));
 	}
 	
 	/**
 	 * Returns the ship's maximum speed.
 	 * @return The ships maximum speed.
 	 */
 	@Basic
 	public double getSpeedLimit() {
 		
 		return speedLimit;
 		
 	}
 	
 	/**
 	 * Changes the velocity of the ship by an amount <code>da</code>.
 	 * This method changes the velocity of the ship according to its current velocity and the direction it's facing.
 	 * The change in total velocity will be equal to the specified amount <code>da</code>.
 	 * @param da The total velocity to be added to the ship's current velocity.
 	 * 
 	 * @post If the given amount of change in velocity is equal to or less than zero, no change to the ship's velocity is made.
 	 * 	     | if(da =< 0) then 
 	 *       |       new getXVelocity = getXVelocity
 	 *       |	     new getYVelocity = getYVelocity
 	 * @post The new velocity vector is equal to the old velocity vector changed by the given amount da in the ship's current direction.
 	 *       | new getXVelocity = getXVelocity + da*cos(angle)
 	 *       | new getYVelocity = getYVelocity + da*sin(angle)
 	 * @post If the new total velocity would exceed the ship's upper speed limit, 
 	 * 		 the new total velocity is changed to be equal to the speed of light without changing the directionof the velocity.
 	 */
 	public void thrust(double da) {
 		
		if (da <= 0) return;
		
		xVelocity = getXVelocity() + da*Math.cos(angle);
		yVelocity = getYVelocity() + da*Math.sin(angle);
		
 		
		if (this.getVelocity()>300000) 	//TODO Dit stuk code werkt niet voor negatieve waarden van xVelocity en yVelocity, ik zal het morgen aanpassen (Jasper)
 		{
			double proportion = (this.getXVelocity())/(this.getYVelocity());	//TODO Deze regel werkt niet als yVelocity 0 is.
 			yVelocity = (300000)/(Math.sqrt(1+Math.pow(proportion,2)));
 			xVelocity = Math.sqrt(Math.pow(300000,2)-Math.pow(this.getYVelocity(),2));
 			
 			}
 		
 	}
 	
 	/**
 	 * The ship's current velocity in the x-direction.
 	 */
 	private double xVelocity;
 	
 	/**
 	 * The ship's current velocity in the x-direction.
 	 */
 	private double yVelocity; 
 	
 	/**
 	 * The ship's maximum speed, cannot exceed the speed of light.
 	 */
 	private double speedLimit;
 	
 	
 	
 	/**
 	 * Returns the ship's radius, expressed in km.
 	 * @return The ship's radius (in km).
 	 */
 	@Basic @Immutable
 	public double getRadius() {
 		
 		return radius;
 	}
 	
 	/**
 	 * The ship's radius (in km)
 	 */
 	private final double radius;
 	
 	/**
 	 * Returns the direction the ship is currently facing.
 	 * @return The direction the ship is facing.
 	 */
 	//TODO spec+implement
 	@Basic
 	public double getDirection() {
 		
 		return angle;
 	}
 	
 	/**
 	 * Turns the ship over a given angle in radians. <code>angle</code> can be negative 
 	 * @param angle The amount of radians to be added to the ships direction. 
 	 */
 	//TODO spec+implement
 	public void turn(double Angle) {
 		
 		angle = angle + Angle;
 		
 	}
 		
 	/**
 	 * The direction the ship is currently facing
 	 */
 	public double angle;
 	
 	/**
 	 * Returns the distance between this ship and the given ship. This distance is the shortest distance the ships would have to move
 	 * to be adjacent to each other. When the given ship is the same as this ship, the distance is zero, when the ships overlap, the distance is negative.
 	 * @param Ship2 The ship to which the distance should be calculated.
 	 * @return The distance between the 2 ships. The distance is negative if and only if the ship's overlap.
 	 */
 	//TODO Defensieve specificatie + implementatie
 	public double getDistanceBetween(Ship ship2) {
 		
 		double distance = Math.sqrt(Math.pow(ship2.getX()-this.getX(),2)+Math.pow(ship2.getY()-this.getY(),2))-(ship2.getRadius()+this.getRadius());
 		return distance;
 	}
 	
 	/**
 	 * Returns true if and only if this ship overlaps with the given ship. A ship always overlaps with itself. Two adjacent ships are considered to overlap.
 	 * @param Ship2 The ship to check for overlap with.
 	 * @return True if this ship overlaps with the given ship, False if the ships do not overlap.
 	 */
 	//TODO defensieve specificatie + implementatie
 	public boolean overlap(Ship ship2) {
 		
 		double distance = this.getDistanceBetween(ship2);
 		double sumOfRadius = this.getRadius() + ship2.getRadius();
 		
 		if(distance <= sumOfRadius)	{
 			return true;
 		}
 		
 	}
 	
 	
 	/**
 	 * Returns the time left (in seconds) before this ship collides for the first time with the given ship or Double.POSITIVE_INTINITY if they never collide.
 	 * A ship can never collide with itself.
 	 * @param Ship2 The ship to which the collision time should be calculated.
 	 * @return The time, in seconds, to collision with the given ship.
 	 */
 	public double getTimeToCollision(Ship ship2) {
 		return 0;
 	}
 	
 	/**
 	 * 
 	 * @param ship2
 	 * @return
 	 */
 	public double[] getCollisionPosition(Ship ship2) {
 		return null;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 }
