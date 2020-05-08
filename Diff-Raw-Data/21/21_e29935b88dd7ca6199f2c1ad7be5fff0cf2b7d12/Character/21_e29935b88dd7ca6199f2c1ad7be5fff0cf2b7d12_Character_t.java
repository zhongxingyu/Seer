 package com.dat255_group3.model;
 
 import com.badlogic.gdx.math.Vector2;
 import com.dat255_group3.utils.WorldUtil;
 
 /**
  * A class which represents a Character. 
  * It inherits properties involving the position from its superclass GameObject.
  * @author The Hans-Gunnar Crew
  *
  */
 public class Character {
 	private int weight;
 	private double friction;
 	private static float width, height; //size of character in pixels
 	private float deathLimit;
	private static  Vector2 position; 
 
 	/**
 	 * Constructs a Character with its properties.
 	 * @param friction
 	 * 			The friction of the Character in the world
 	 * @param weight
 	 * 			The weight of the Character
 	 */
 	public Character(double friction, int weight){
 		//TODO no position in constructor! physics and render will handle it later!
 		this.friction = friction;
 		this.weight = weight;
 		Character.width = 50;
 		Character.height = 70;
 		this.deathLimit = 0f;
		this.position = WorldUtil.getStartPos();
 	}
 	
 	public boolean isDead(){
 		return getPosition().x < deathLimit;
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
 
 	public static float getWidth() {
 		return width;
 	}
 
 	public void setWidth(float with) {
 		Character.width = with;
 	}
 
 	public static float getHeight() {
 		return height;
 	}
 
 	public void setHeight(float height) {
 		Character.height = height;
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
 	 *  A method which gives the position of the GameObject.
 	 * @return
 	 * 		The position of the GameObject
 	 */
 	public static Vector2 getPosition() {
 		return position;
 	}
 
 	/**
 	 * Sets the position of the GameObject
 	 * @param position
 	 * 		The position which the GameObject is to be set to.
 	 */
 	public void setPosition(Vector2 position) {
 		this.position = position;
 	}
 
 	
 
 }
