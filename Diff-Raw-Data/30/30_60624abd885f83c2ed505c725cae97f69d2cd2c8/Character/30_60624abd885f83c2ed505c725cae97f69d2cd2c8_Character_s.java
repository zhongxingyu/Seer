 package com.dat255_group3.model;
 
 import com.badlogic.gdx.math.Vector2;
 import com.dat255_group3.utils.WorldUtil;
 
 /**
  * A class which represents a Character.
  * The character is represented with a circle. 
  * @author The Hans-Gunnar Crew
  *
  */
 public class Character {
 	private double friction;
 	private static float radius; //size of character in pixels
 	private float deathLimit;
 	private Vector2 position; 
	
 
 	/**
 	 * Constructs a Character with its properties.
 	 * @param friction
 	 * 			The friction of the Character in the world (a value between 0-1)
 	 */
 	public Character(double friction){
 		this.friction = friction;
 		Character.radius = 25;
 		this.deathLimit = 20f; 
 		this.position = WorldUtil.getStartPos();
 	}
 	
 	public boolean isDead(){
 		return getPosition().x < deathLimit;
 	}
 
 	public static float getRadius() {
 		return radius;
 	}
 
 	public void setRadius(float radius) {
 		Character.radius = radius;
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
 	
 	/**
 	 *  A method which gets the position of the character.
 	 * @return
 	 * 		The position of the character
 	 */
 	public Vector2 getPosition() {
 		return position;
 	}
 
 	/**
 	 * Sets the position of the character
 	 * @param position
 	 * 		The position which the character is to be set to.
 	 */
 	public void setPosition(Vector2 position) {
 		this.position = position;
 	}
 
 	public float getDeahLimit() {
 		return deathLimit;
 	}
 	
 	public void moveDeathLimit(float speedP) {
 		this.deathLimit = deathLimit + speedP;
 	}
 
 }
