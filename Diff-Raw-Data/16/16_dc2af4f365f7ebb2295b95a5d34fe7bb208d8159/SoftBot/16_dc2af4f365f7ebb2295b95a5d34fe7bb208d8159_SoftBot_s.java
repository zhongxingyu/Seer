 package balle.simulator;
 
 import balle.brick.Controller;
 
 public class SoftBot implements Controller {
 	
 	private float leftWheelSpeed = 0;
 	private float rightWheelSpeed = 0;
 	
 	public float getLeftWheelSpeed() {
 		return leftWheelSpeed;
 	}
 	
 	public float getRightWheelSpeed() {
 		return rightWheelSpeed;
 	}
 
 	@Override
 	public void backward(int speed) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void forward(int speed) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void floatWheels() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void rotate(int deg, int speed) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void travel(int dist) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setWheelSpeeds(int leftWheelSpeed, int rightWheelSpeed) {
 		this.leftWheelSpeed = leftWheelSpeed;
 		this.rightWheelSpeed = rightWheelSpeed;
 		
 	}
 
 	@Override
 	public int getMaximumWheelSpeed() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void kick() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
