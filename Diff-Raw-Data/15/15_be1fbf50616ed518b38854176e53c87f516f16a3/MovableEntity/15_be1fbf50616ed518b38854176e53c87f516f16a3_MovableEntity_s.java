 package chalmers.TDA367.B17.model;
 
 import org.newdawn.slick.geom.*;
 
 public abstract class MovableEntity extends Entity {
 
 	private Vector2f direction;
 	private double maxSpeed;
 	private double minSpeed;
 	private double reverseSpeed;
 	private double acceleration;
 	
 	public MovableEntity(int id, Vector2f direction, double maxSpeed, double minSpeed, double reverseSpeed) {
 		super(id);
 		this.direction = direction;
 		this.maxSpeed = maxSpeed;
 		this.minSpeed = minSpeed;
 		this.reverseSpeed = reverseSpeed;
 	}
 	
 	public Vector2f getDirection(){
 		return direction;
 	}
 
 	public double getSpeed(){
 		return direction.length();
 	}
 	
 	public double getMaxSpeed(){
 		return maxSpeed;
 	}
 	
 	public double getMinSpeed(){
 		return minSpeed;
 	}
 	
 	public void setVelocity(Vector2f v){
 		direction = v;
 	}
 	
 	public void setSpeed(double speed){
 		getDirection().normalise();
 		getDirection().scale((float)speed);
 	}
 	
 	public void setMaxSpeed(double maxSpeed){
 		this.maxSpeed = maxSpeed;
 	}
 	
 	public void setMinSpeed(double minSpeed){
 		this.minSpeed = minSpeed;
 	}
 
 	public double getReverseSpeed() {
 		return reverseSpeed;
 	}
 
 	public void setReverseSpeed(double reverseSpeed) {
 		this.reverseSpeed = reverseSpeed;
 	}
 
 	public double getAcceleration() {
 		return acceleration;
 	}
 
 	public void setAcceleration(double acceleration) {
 		this.acceleration = acceleration;
 	}
 
 	public void move(){
 		// compute velocity
 		Vector2f velocity = getDirection().scale((float) getSpeed());
 
 		// add velocity
 		setPosition(getPosition().add(velocity));
 	}
 
 	public void accelerate(){ // TODO change name
		setVelocity(getDirection().normalise());
		System.out.println(getDirection());
		//getVelocity().scale((float)getAcceleration());

		System.out.println(getDirection());
 	}
 
 	public void update(){
 		accelerate();
 		move();
 	}
 }
