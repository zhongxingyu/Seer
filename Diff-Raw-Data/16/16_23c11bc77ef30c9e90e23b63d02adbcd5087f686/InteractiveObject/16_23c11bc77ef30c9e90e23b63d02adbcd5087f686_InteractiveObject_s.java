 package rsmg.model;
 import rsmg.util.Vector2d;
 
 /**
  * Class which represents an object that can interact with the main character
  * and has the possibility of being moved around.  
  * @author Johan Grnvall
  *
  */
 public abstract class InteractiveObject {
 	protected double x;
 	protected double y;
 	private double height;
 	private double width;
 	private Vector2d velocity = new Vector2d();
 	
 	public InteractiveObject(double x, double y){
 		this.x=x;
 		this.y=y;
 	}
 	public InteractiveObject(double x, double y, double width, double height ){
 		this.x=x;
 		this.y=y;
 		this.height=height;
 		this.width=width;
 	}
 	
 	public Vector2d getVector(){
 		return velocity;
 	}
 	
 	public double getVelocity(){
 		return velocity.getlength();
 	}
 	
 	public void addVector(Vector2d vector){
 		velocity.add(vector);
 	}
 	
 	public double getX(){
 		return x;
 	}
 	
 	public double getY(){
 		return y;
 	}
 	
 	public double getHeight(){
 		return height;
 	}
 	
 	public double getWidth(){
 		return width;
 	}
 	
 	public void setX(double x){
 		this.x=x;
 	}
 	
 	public void setY(double y){
 		this.y=y;
 	}
 	
 	/**
 	 * Changes the position of the InteractiveObject based on its current Vector.
 	 * a vector with the strength of 1 is equivalent to a speed of 1pixel/s
 	 * @param delta, time since last update
 	 */
 	public void move(int delta){
 		this.x += this.getVector().getX()*delta;
 		this.y += this.getVector().getY()*delta;
 	}
 	
 	/**
 	 * applies a gravity vector to this InteractiveObject 
 	 * @param delta
 	 */
	public void applyGravity(int delta){
 		addVector(new Vector2d(0, -Constants.GRAVITYSTRENGTH*delta));
 	}
 	
 	/**
 	 * 
 	 * @param obj
 	 * @return true if the two InteractiveObjects have collided, otherwise it returns false
 	 */
 	public boolean hasCollidedWith(InteractiveObject obj){
 		if (obj == null){
 			return false;
 		}
 		else{
 			return (((this.getX() - obj.getX() < (obj.getWidth()) && (this.getX() > obj.getX()) &&
 					((this.getY() - obj.getY()) < obj.getHeight()) && (this.getY() > obj.getY()) ) ||
 					((obj.getX() - this.getX() < this.getWidth()) && (obj.getX() > this.getX()) &&
 						(obj.getY() - this.getY() < this.getHeight()) && (obj.getY() > this.getY()))));
 		}
 	}	
 	/**
 	 * class for specifying what happens when this objects collides with another InteractiveObject
 	 */
 	public abstract void collide(InteractiveObject obj);
 	
 }
