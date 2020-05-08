 package com.race.entity;
 
 import org.newdawn.slick.Graphics;
 
 public abstract class Car extends BaseEntity {
 	/** The current speed */
 	float speed;
 	/** If the car is moving or not */
 	boolean moving = false;
 	/** The acceleration of a car */
 	float acceleration = 0.1f;
 	/** The maximum speed of a car */
 	int maxSpeed = 10;
 	/** The friction of a car */
 	float friction = 0;
 
 	/**
 	 * @param x
 	 *            Starting x position
 	 * 
 	 * @param y
 	 *            Starting y position
 	 * 
 	 * @param health
 	 *            The health
 	 * 
 	 * @param acceleration
 	 *            the speed up
 	 * 
 	 * @param maxSpeed
 	 *            the maximum speed
 	 * 
 	 * @param friction
 	 *            the friction
 	 */
 	protected Car(int x, int y, int health, float acceleration, int maxSpeed,
 			float friction) {
 		super(x, y, health);
 		this.acceleration = acceleration;
 		this.maxSpeed = maxSpeed;
 		this.friction = friction;
 	}
 
 	/**
 	 * This changes the direction accordingly to the speed and time. For use
 	 * with the left and right button.
 	 * 
 	 * @param rotation
 	 *            The amount it should rotate.
 	 * @param deltaT
 	 *            The time that has passed.
 	 */
 	public void changeDirection(float rotation, float deltaT) {
		this.rotation += deltaT * rotation * speed * 0.1 + rotation / 10;
 	}
 
 	/**
 	 * If the car is moving or not.
 	 * 
 	 * @param moving
 	 *            The new moving.
 	 */
 	public void setMoving(boolean moving) {
 		this.moving = moving;
 	}
 
 	/**
 	 * The speedup function, should be called if the forward button is pressed.
 	 * 
 	 * @param deltaT
 	 *            The time that has passed.
 	 */
 	public void speedUp(float deltaT) {
 		if (speed < maxSpeed)
			speed += deltaT * acceleration;
 	}
 
 	/**
 	 * Sets the speed, because of a boost for example.
 	 * 
 	 * @param speed
 	 *            Will become the new speed.
 	 */
 	public void setSpeed(float speed) {
 		this.speed = speed;
 	}
 
 	/**
 	 * The update function for an individual car.
 	 * 
 	 * @param deltaT
 	 *            The time that has passed.
 	 */
 	public abstract void update(float deltaT);
 
 	/**
 	 * The update function which should be called in the game class.
 	 * 
 	 * @param deltaT
 	 *            The time that has passed.
 	 */
 	public void updateAll(float deltaT) {
 		if (moving) {
 			speedUp(deltaT);
 		}
 		x += speed * deltaT
 				* java.lang.Math.sin(java.lang.Math.toRadians(rotation)) * 0.1;
 		y -= speed * deltaT
 				* java.lang.Math.cos(java.lang.Math.toRadians(rotation)) * 0.1;
 		frictionUpdate(deltaT);
 		update(deltaT);
 	}
 
 	public void frictionUpdate(float deltaT) {
 		if (!moving) {
 			if (speed > 0)
 				speed -= deltaT * friction;
 			if (speed < 0.1) {
 				speed = 0;
 			}
 		}
 	}
 
 	/**
 	 * The draw function, should be implemented in an induvidual car.
 	 * 
 	 * @param g
 	 *            Just pass g here.
 	 */
 	public abstract void draw(Graphics g);
 }
