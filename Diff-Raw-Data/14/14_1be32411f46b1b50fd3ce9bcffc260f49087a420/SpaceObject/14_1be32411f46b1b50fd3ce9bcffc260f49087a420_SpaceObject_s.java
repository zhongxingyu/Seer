 package asteroids.model;
 
 import asteroids.Vector;
 import be.kuleuven.cs.som.annotate.*;
 
 public abstract class SpaceObject {
 
 	private double radius;
 	private double mass; 									// The mass of the ship (kg)
 	private Vector location, velocity;
 	protected static final double C = 300000;						// Speed of light (km/s)
 	private World world;
 
 	protected SpaceObject(Vector coordinates, Vector velocity, double radius)
 	{
 		setLocation(coordinates);
 		setVelocity(velocity);
 		setRadius(radius);
 		
 	}
 	
 	protected SpaceObject() {}
 
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
 
 	public void setLocation(Vector newVector)
 	{
 		if (!newVector.isValidVector())
 			throw new IllegalArgumentException();
 		this.location = newVector;
 	}
 
 	public Vector getLocation()
 	{
 		return this.location;
 	}
 
 	public void setVelocity(Vector newVelocity)
 	{
 		if (!newVelocity.isValidVector())
 			this.velocity = new Vector(0,0);
 		else if (newVelocity.getNorm() > C) {
 			double constant = C / newVelocity.getNorm();
 			this.velocity = newVelocity.multiply(constant);
 		}
 		else this.velocity = newVelocity;
 	}
 
 	public Vector getVelocity () 
 	{
 		return this.velocity;
 	}
 
 	public double getDistanceBetween (SpaceObject other) {
 		if (other == null) 																// The other object doesn't exist.
 			throw new IllegalArgumentException();
 		return getLocation().getDistanceBetween(other.getLocation());
 	}
 
 	public boolean overlap(SpaceObject other){
 		if (other == null) 																	// The other object doesn't exist.
 			throw new IllegalArgumentException("Invalid ship!");
 		return( (getRadius() + other.getRadius()) > this.getDistanceBetween(other)); 		// If the distance is smaller than the sum of the radii, the objects overlap.
 	}	
 
 	public void move (double time) {
 		if (!isValidTime(time))
 			throw new IllegalArgumentException();
 		setLocation(getLocation().add(getVelocity().multiply(time)));
 	}
 
 	public boolean isValidTime (double time) { 
 		return !(Double.isNaN(time) || time < 0);
 	}
 
 	public double getTimeToCollision(SpaceObject other){	
 		if (other == null) 																// The other object doesn't exist.
 			throw new IllegalArgumentException("Invalid ship!");
 
 		Vector deltaR = other.getLocation().substract(getLocation());
 		Vector deltaV = other.getVelocity().substract(getVelocity());
 		double sigma = getRadius() + other.getRadius();
 		double VR = deltaV.multiply(deltaR);
 		double RR = deltaR.multiply(deltaR);
 		double VV = deltaV.multiply(deltaV);
 		double d = Math.pow(VR, 2) - VV*(RR - Math.pow(sigma, 2));
 
 		if(this.overlap(other))
 			return Double.POSITIVE_INFINITY; 					// The object overlap.
 		else if(Double.compare(d,0) <= 0)
 			return Double.POSITIVE_INFINITY; 					// The object will not collide.
 		else if(Double.compare(VR,0) >=0)
 			return Double.POSITIVE_INFINITY;		
 		else
 			return -((VR + Math.sqrt(d)) / VV); 				// Calculate the time to collision.
 	}
 
 	public double[] getCollisionPosition(SpaceObject other)
 	{
 		if (other == null) 									// The other object does not exist.
 			throw new IllegalArgumentException("Invalid ship!");
 
 		double timeToCollision = this.getTimeToCollision(other);
 
 		if(timeToCollision != Double.POSITIVE_INFINITY){			
 			Vector locationThis = getLocation().add(getVelocity().multiply(timeToCollision));
 			Vector locationOther = other.getLocation().add(other.getVelocity().multiply(timeToCollision));
 			double sumRadii = this.getRadius() + other.getRadius();
 			locationOther = locationOther.multiply(this.getRadius());
 			locationThis = locationThis.multiply(other.getRadius()).add(locationOther).multiply(1/sumRadii);
 			return new double[] {locationThis.getX(), locationThis.getY() };
 		}
 		else return null;
 	}
 
 	// !! Not sure: totally, nominally or defensively?
 	public void setMass (double mass) {
 		if (!isValidMass(mass))
 			throw new IllegalArgumentException();
 		this.mass = mass;
 	}
 	
 	public void setMassWithDensity (double density) {
 		double mass = (4/3) * Math.PI * Math.pow(getRadius(), 3) * density;
 		setMass(mass);
 	}
 
 	public double getMass () {
 		return this.mass;
 	}
 
 	// !! Not sure if private or public?
 	private boolean isValidMass (double mass) {
 		if (Double.isNaN(mass) || mass <= 0)
 			return false;
 		return true;
 	}
 
 
 	public World getWorld(){
 		return this.world;
 	}
 
 	public void setWorld(World world) throws IllegalArgumentException{
 		if(!this.isValidWorld(world))
 			throw new IllegalArgumentException();
 		if(!(getWorld()==null)){
 			removeWorld();
 		}
 		this.world = world;
 	}
 	
 	public boolean isValidWorld(World world){
 		if(world == null)
 			return false;
 		if(getLocation().getX() + getRadius() > world.getWidth())
 			return false;
 		if(getLocation().getY() + getRadius() > world.getHeigth())
 			return false;
 		return true;
 	}
 	
 	public void removeWorld(){
 		this.world = null;
 	}
 	
 	public double collisionTimeWithBoundaries() {
 		
 		if (this.world == null)
 			return Double.POSITIVE_INFINITY;
 		if (getVelocity().getNorm() == 0)
 			return Double.POSITIVE_INFINITY;
 		
 		double timeToY = collisionWithAxis(getLocation().getY(), getVelocity().getY(), getWorld().getHeigth());
 		double timeToX = collisionWithAxis(getLocation().getX(), getVelocity().getX(), getWorld().getWidth());
 		
 		if (timeToY < timeToX)
 			return timeToY;
 		else return timeToX;
 		
 	}
 	
 	private double collisionWithAxis(double coord, double velocity, double axis) {
 		
 		if (velocity == 0)
 			return Double.POSITIVE_INFINITY;
 		if (velocity > 0)
 			return (axis - coord - this.getRadius()) / velocity;
 		if (velocity < 0)
 			return (0 - coord + this.getRadius()) / velocity;
 		
 		return -1;
 		
 	}
 
 
 }
