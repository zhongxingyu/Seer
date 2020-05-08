 package com.example.spaceshipgame.model;
 
 public class Spaceship extends Element {
 	private int width, height;
 	
 	public Spaceship () {
		width = r.nextInt(50) + 10;
		height = r.nextInt(50) + 10;
 	}
 
 	public int getHeigt() {
 		return height;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 }
