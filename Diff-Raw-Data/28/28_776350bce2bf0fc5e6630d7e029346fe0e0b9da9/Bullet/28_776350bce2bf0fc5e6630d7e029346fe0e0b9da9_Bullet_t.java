 package com.example.pineapple;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class Bullet {
 
 	private final int SPREAD = 10;
 	private double xPos;
 	private double yPos;
 	private double xVel;
 	private double yVel;
 	private double angle;
 	private static int radius = 2;
 	private boolean colliding;
 	private int rotation;
 	private int type;
	private Protagonist protagonist;
 
 	//CONSTRUCTOR
	public Bullet(double x, double y, double angle, double bulletSpeed, Protagonist protagonist) {
 		angle += Math.random()*SPREAD-SPREAD/2;
 		this.xPos = x;
 		this.yPos = y;
		this.xVel = protagonist.getXVel() + Math.cos(angle/180*Math.PI)*bulletSpeed;
		this.yVel = protagonist.getYVel() - Math.sin(angle/180*Math.PI)*bulletSpeed;
 		this.angle = angle;
 		rotation = (int)(Math.random()*360);
 		type = (int)(Math.random()*3);
 	}
 
 	//GET and SETmethods
 	public double getXPos() {
 		return xPos;
 	}
 
 	public void setXPos(double xPos) {
 		this.xPos = xPos;
 	}
 
 	public double getYPos() {
 		return yPos;
 	}
 
 	public void setYPos(double yPos) {
 		this.yPos = yPos;
 	}
 
 	public double getXVel() {
 		return xVel;
 	}
 
 	public void setXVel(double xVel) {
 		this.xVel = xVel;
 	}
 
 	public double getYVel() {
 		return yVel;
 	}
 
 	public void setYVel(double yVel) {
 		this.yVel = yVel;
 	}
 
 	public double getAngle() {
 		return angle;
 	}
 
 	public void setAngle(double angle) {
 		this.angle = angle;
 	}
 
 	public static int getRadius() {
 		return radius;
 	}
 	
 	public int getRotation(){
 		return rotation;
 	}
 	
 	public int getType(){
 		return type;
 	}
 
 	/*public static void setRadius(int radius) {
 		this.radius = radius;
 	}*/
 
 	//Moving bullet
 	public void move() {
 		this.setXPos(this.getXPos() + this.getXVel());
 		this.setYPos(this.getYPos() + this.getYVel());
 		this.rotation += 20;
 	}
 	
 	//Gravity on bullets
 	public void gravity(double acc) {
 		this.setYVel(this.getYVel() + acc);
 	}
 	
 	//Check if bullet hits ground or platform (is in air)
 	public boolean checkObstacles(Ground g, ArrayList<Platform> al) {
 		if (this.getYPos() > g.getYFromX(this.getXPos())) {
 			colliding = true;
 		} else {
 			for (int i = 0; i<al.size(); i++) {
 				if(al.get(i).spans(this.getXPos())) {
 					if (this.getYPos() < al.get(i).getLowerYFromX(this.getXPos()) && this.getYPos() > al.get(i).getUpperYFromX(this.getXPos())) {
 						colliding = true;
 					}
 				}
 			}
 		}
 		return colliding;
 	}
 
 	//Check if the passed enemy gets hit by this bullet
 	public boolean collideEnemy(Enemy e){
 		//Add more precise hitcheck later!
 		if(getXPos() - getRadius() < e.getXPos() + e.getWidth()/2 && getXPos() + getRadius() > e.getXPos() - e.getWidth()/2 &&
 				getYPos() - getRadius() < e.getYPos() + e.getHeight()/2 && getYPos() + getRadius() > e.getYPos() - e.getHeight()/2)
 			return true;
 		else 
 			return false;
 	}
 }
