 package com.example.spaceshipgame.model;
 
 public class Missile extends Element {
 	private int radius;
 	
 	public Missile () {
		radius = r.nextInt(20) + 10;
 	}
 	
 	public int getRadius() {
 		return radius;
 	}
 }
