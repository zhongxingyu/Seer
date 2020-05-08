 package com.dat255_group3.model;
 
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * A class which represents a Character. 
  * It inherits properties involving the position from its superclass GameObject.
  * @author The Hans-Gunnar Crew
  *
  */
 public class Character extends GameObject {
 	private int weight;
 	private double friction;
 	private int jumpSteps;
 	private float width, height; //size of character in pixels
 
 	/**
 	 * Constructs a Character with its properties.
 	 * @param position
 	 * 			The position of the Character in the world
 	 * @param friction
 	 * 			The friction of the Character in the world
 	 * @param weight
 	 * 			The weight of the Character
 	 */
 	public Character(Vector2 position, double friction, int weight){
 		super(position);
 		
 		this.friction = friction;
 		this.weight = weight;
 		this.jumpSteps = 0;
 		this.width = 50;
 		this.height = 70;
 	}
 	
 	public void jumpCountdown(){
 		this.jumpSteps--;
 	}
 	
 	public int getJumpSteps() {
 		return jumpSteps;
 	}
 
 	public void setJumpSteps(int jumpSteps) {
 		this.jumpSteps = jumpSteps;
 	}
 
 	/**
 	 * A method which gives the weight of the Character.
 	 * @return
 	 * 		The weight of the Character
 	 */
 	public int getWeight() {
 		return weight;
 	}
 
 	/**
 	 * Sets the weight of the Character
 	 * @param weight
 	 * 			The weight which the Character is to be set to.
 	 */
 	public void setWeight(int weight) {
 		this.weight = weight;
 	}
 
 	public float getWidth() {
 		return width;
 	}
 
	public void setWith(float with) {
 		this.width = with;
 	}
 
 	public float getHeight() {
 		return height;
 	}
 
 	public void setHeight(float height) {
 		this.height = height;
 	}
 
 	/**
 	 * A method which gives the friction of the Character.
 	 * @return
 	 * 		The friction of the Character
 	 */		
 	public double getFriction() {
 		return friction;
 	}
 
 	/**
 	 * Sets the friction of the Character
 	 * @param friction
 	 * 		The friction which the Character is to be set to.
 	 */
 	public void setFriction(int friction) {
 		this.friction = friction;
 	}
 	
 	
 
 }
