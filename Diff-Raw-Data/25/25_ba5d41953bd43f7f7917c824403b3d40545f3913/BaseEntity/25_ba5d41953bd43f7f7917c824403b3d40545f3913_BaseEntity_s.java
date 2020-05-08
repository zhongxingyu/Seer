 package com.race.entity;
 
 import org.newdawn.slick.Image;
 
 /**
  * @author elamre
  * 
  */
 public abstract class BaseEntity {
 	/** The X value */
 	protected float x;
 	/** The Y value */
 	protected float y;
 	/** health */
 	protected int health;
 	/** The image to be displayed */
 	protected Image image;
 	/** The height of the entity */
 	protected int heigth = 1;
 	/** The width of the entity */
 	protected int width = 1;
 	/** The rotation of the image */
	float rotation = 0;
 	/** The scale of the image */
	float scale = 1;
 
 	/**
 	 * @param x
 	 *            The initial x value.
 	 * @param y
 	 *            The initial y value.
 	 * @param health
 	 *            The starting health.
 	 */
 	protected BaseEntity(int x, int y, int health) {
 		this.health = health;
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * @return returns the x value.
 	 */
 	public float getX() {
 		return x;
 	}
 
 	/**
 	 * @return returns the y value.
 	 */
 	public float getY() {
 		return y;
 	}
 
 	/**
 	 * @return returns the health.
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * @param newX
 	 *            the new x value.
 	 */
 	public void setX(int newX) {
 		this.x = newX;
 	}
 
 	/**
 	 * @param newY
 	 *            the new y value.
 	 */
 	public void setY(int newY) {
 		this.y = newY;
 	}
 
 	/**
 	 * @param newHealth
 	 *            the new health.
 	 */
 	public void setHealth(int newHealth) {
 		this.health = newHealth;
 	}
 
 	/**
 	 * Sets the hitbox sizes.
 	 */
 	public void setRect(int heigth, int width) {
 		this.heigth = heigth;
 		this.width = width;
 	}
 
 	/**
 	 * This will return the image, NO USE.
 	 * 
 	 * @return the image
 	 */
 	public Image getImage() {
 		return image;
 	}
 }
