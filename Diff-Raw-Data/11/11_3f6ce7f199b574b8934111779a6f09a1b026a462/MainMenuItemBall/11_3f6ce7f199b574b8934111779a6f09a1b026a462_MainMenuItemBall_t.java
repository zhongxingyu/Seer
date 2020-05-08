 package com.karolmajta.stp.models;
 
 import android.util.Log;
 
 public class MainMenuItemBall extends Tickable {
 	// string label of this menu item
 	private String label; 
 	// coordinates of point where this ball is constrained
 	private float x0; // pixels
 	private float y0; // pixels
 	// radius of the ball
 	private float radius; // pixels
 	// current coordinates of ball 
 	private float currentX; // pixels
 	private float currentY; // pixels
 	// current velocities of ball
 	private float currentVX;
 	private float currentVY;
 	// spring-dumping coefficients
 	private float spring;
 	private float damping;
 	// mass
 	private float mass;
 	/**
 	 * 
 	 * @param label String label describing this menu item
 	 * @param x0 Constrain point (percentage of screen width)
 	 * @param y0 Constrain point (percentage of screen height)
 	 * @param radius Radius of ball (percentage of screen width)
 	 * @param currentX Current x coordinate of the center
 	 * @param currentY Current y coordinate of the center
 	 * @param currentVX Velocity x coordinate of the center
 	 * @param currentVY Velocity y coordinate of the center
 	 * @param spring Spring constant in the joint between ball and constraint
 	 * @param damping Damping factor between the joint and constraint
 	 * @param mass Mass of the ball
 	 */
 	public MainMenuItemBall(
 			String label,
 			float x0,
 			float y0,
 			float radius,
 			float currentX,
 			float currentY,
 			float currentVX,
 			float currentVY,
 			float spring,
 			float damping,
 			float mass
 	) {
 		this.x0 = x0;
 		this.y0 = y0;
 		this.radius = radius;
 		this.currentX = currentX;
 		this.currentY = currentY;
 		this.currentVX = currentVX;
 		this.currentVY = currentVY;
 		this.spring = spring;
 		this.damping = damping;
 		this.mass = mass;
 	}
 	
 	/**
 	 * Creates vanilla ball with empty string as label zero initial velocity
 	 * and current coordinates matching base coordinates.
 	 * 
 	 * @param x0
 	 * @param y0
 	 * @param radius
 	 * @param spring
 	 * @param damping
 	 * @param mass
 	 */
 	public MainMenuItemBall(
 			float x0,
 			float y0,
 			float radius,
 			float spring,
 			float damping,
 			float mass
 	) {
 		this("", x0, y0, radius, x0, y0, 0, 0, spring, damping, mass);
 	}
 	
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public float getX0() {
 		return x0;
 	}
 
 	public void setX0(float x0) {
 		this.x0 = x0;
 	}
 
 	public float getY0() {
 		return y0;
 	}
 
 	public void setY0(float y0) {
 		this.y0 = y0;
 	}
 
 	public float getRadius() {
 		return radius;
 	}
 
 	public void setRadius(float radius) {
 		this.radius = radius;
 	}
 
 	public float getCurrentX() {
 		return currentX;
 	}
 
 	public void setCurrentX(float currentX) {
 		this.currentX = currentX;
 	}
 
 	public float getCurrentY() {
 		return currentY;
 	}
 
 	public void setCurrentY(float currentY) {
 		this.currentY = currentY;
 	}
 
 	public float getCurrentVX() {
 		return currentVX;
 	}
 
 	public void setCurrentVX(float currentVX) {
 		this.currentVX = currentVX;
 	}
 
 	public float getCurrentVY() {
 		return currentVY;
 	}
 
 	public void setCurrentVY(float currentVY) {
 		this.currentVY = currentVY;
 	}
 
 	public float getSpring() {
 		return spring;
 	}
 
 	public void setSpring(float spring) {
 		this.spring = spring;
 	}
 
 	public float getDamping() {
 		return damping;
 	}
 
 	public void setDamping(float damping) {
 		this.damping = damping;
 	}
 
 	public float getMass() {
 		return mass;
 	}
 
 	public void setMass(float mass) {
 		this.mass = mass;
 	}
 	
 	/**
 	 * 
 	 * @return Distance (difference between coordinates, so it can be less
 	 * 		than zero!) between base x and current x.
 	 */
 	public float getBaseDx() {
		return currentX-x0;
 	}
 	
 	/**
 	 * 
 	 * @return Just like {@link MainMenuItemBall#getBaseDx()} but for the
 	 * 		y axis.
 	 */
 	public float getBaseDy() {
		return currentY-y0;
 	}
 	
 	/**
 	 * 
 	 * @return Value of currentAX (acceleration x) that would be calculated
 	 * 		in the next step of integration. 
 	 */
 	public float getNextAX() {
 		float dx = getBaseDx();
		float newAX = -(spring/mass)*dx -(damping/mass)*currentVX;
 		return newAX;
 	}
 	
 	/**
 	 *  
 	 * @return Value of currentAY (acceleration y) that would be calculated
 	 * 		in the next step of integration
 	 */
 	public float getNextAY() {
 		float dy = getBaseDy();
		float newAY = -(spring/mass)*dy -(damping/mass)*currentVY;
 		return newAY;
 	}
 	
 	/**
 	 * 
 	 * @param dt
 	 * @return Value of currentVX (velocity x) that would be calculated in
 	 * 		the next step of integration using time step dt.
 	 */
 	public float getNextVX(long dt) {
 		return currentVX+getNextAX()*dt;
 	}
 	
 	/**
 	 * 
 	 * @param dt
 	 * @return Value of currentVY (velocity y) that would be calculated in
 	 * 		the next step of integration using time step dt.
 	 */
 	public float getNextVY(long dt) {
 		return currentVY+getNextAY()*dt;
 	}
 	
 	/**
 	 * 
 	 * @param dt
 	 * @return Value of currentX (coord x) that would be calculated in
 	 * 		the next step of integration using time step dt.
 	 */
 	public float getNextCurrentX(long dt) {
 		float next = currentX+currentVX*dt;
 		return next;
 	}
 	
 	/**
 	 * 
 	 * @param dt
 	 * @return Value of currentY (coord y) that would be calculated in
 	 * 		the next step of integration using time step dt.
 	 */
 	public float getNextCurrentY(long dt) {
 		float next = currentY+currentVY*dt;
 		return next;
 	}
 	
 	/**
 	 * 
 	 * @return Distance between center of ball and base point (constrains)
 	 */
 	public float getBasePointDist() {
 		float dxSquared = (x0-currentX)*(x0-currentX);
 		float dySquared = (y0-currentY)*(y0-currentY);
 		return (float)Math.sqrt(dxSquared+dySquared);
 	}
 	
 	/**
 	 * Integrates new values of velocity and position and assigns them changing
 	 * state of the object
 	 * @param dt Integration step (milliseconds)
 	 */
 	public void integrate(long dt) {
 		float accX = getNextAX();
 		float accY = getNextAY();
 		float nextVX = currentVX+accX*dt;
 		float nextVY = currentVY+accY*dt;
 		float nextX = currentX+currentVX*dt;
 		float nextY = currentY+currentVY*dt;
 		
 		currentVX = nextVX;
 		currentVY = nextVY;
 		currentX = nextX;
 		currentY = nextY;
 	}
 	
 	/**
 	 * Calls {@ling MainMenuItemBall#integrate(long)} with dt.
 	 */
 	@Override
 	public void onTick(long dt) {
 		integrate(dt);
 	}
 }
