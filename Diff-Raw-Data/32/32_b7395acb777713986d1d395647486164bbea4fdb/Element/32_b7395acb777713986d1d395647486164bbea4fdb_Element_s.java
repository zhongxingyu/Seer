 package com.example.spaceshipgame.model;
 
 import java.util.Random;
 
 public class Element {
 	private Vector position;
 	private Vector velocity;
 	
 	Random r = new Random();
 	
 	public Element () {
		position = new Vector(r.nextInt(300)+10, r.nextInt(300)+10);
 	}
 	
 	public Element(Vector position) {
 		this.position = new Vector(position);  
 	}
 	
 	public Vector getPosition() {
 		return position;
 	}
 	
 	public void move(Vector vector) {
 		position.add(vector);
 	}
 }
 
