 package jboxGlue;
 
 import jgame.JGObject;
 
 import org.jbox2d.common.Vec2;
 
 import walls.HorizontalWall;
 import walls.VerticalWall;
 
 public class MovableMass extends Mass {
 	public MovableMass(float x, float y, float mass) {
 		this(x, y, 0, 0, mass);
 	}
 	
 	public MovableMass(float x, float y, double xForce, double yForce, float mass) {
 		super(x, y, mass);
 		myX = x;
 		myBody.m_type = 1;
 		this.setForce(xForce, yForce);
 	}
 	
 	@Override
 	public void applyForce(Vec2 force){
 		myBody.applyForce(force, myBody.getLocalCenter());
 	}
 	
 	public void applyForce(Vec2 force, Vec2 force2){
 		myBody.applyForce(force, force2);
 	}
 
 	@Override
 	public void move() {
 		super.move();
 		
 		Vec2 position = myBody.getPosition();
 		y = position.y;
 		x = position.x;
 		myX = position.x;
 		myRotation = -myBody.getAngle();
 		//Applying the spring force
 		for (Spring s : mySprings) {
 			Vec2 force = s.getForce(x, y);
 			this.setForce(force.x, force.y);
 		}
 	}
 
 //	@Override
 	public void hit(JGObject other) {	
 		//if hits top or bottom
 		if (other instanceof HorizontalWall) {
 			if(myBody.m_linearVelocity.length() <= 1) {
 				if(other.x < pfheight/2){
					myBody.m_linearVelocity = new Vec2(0, 2f);
 				}
 				else
					myBody.m_linearVelocity = new Vec2(0, -2f);
 			}
 			myBody.m_linearVelocity = (new Vec2(myBody.m_linearVelocity.x, -myBody.m_linearVelocity.y));
 		}
 		//if hits left or right
 		if (other instanceof VerticalWall) {
 			if(myBody.m_linearVelocity.length() <= 1) {
 				if(other.x < pfwidth/2){
					myBody.m_linearVelocity = new Vec2(-2f,0);
 				}
 				else
					myBody.m_linearVelocity = new Vec2(2f,0);
 			}
 			myBody.m_linearVelocity = (new Vec2(-myBody.m_linearVelocity.x, myBody.m_linearVelocity.y));
 		}
 	}
 }
