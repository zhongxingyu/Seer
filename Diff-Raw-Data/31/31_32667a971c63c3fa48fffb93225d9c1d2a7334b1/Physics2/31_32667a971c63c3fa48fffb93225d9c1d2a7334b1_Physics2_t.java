 package atlantis.engine.graphics;
 
 import atlantis.framework.Vector2;
 //AtlantisEngine.java - Copyright (C) Yannick Comte.
 //This file is subject to the terms and conditions defined in
 //file 'LICENSE', which is part of this source code package.
 /**
  * A basic physics module for a 2D entity.
  * @author Yannick
  */
 public class Physics2 {
 	protected Vector2 acceleration;
 	protected Vector2 velocity;
 	protected float maxVelocity;
 	protected float speed;
 	protected float maxSpeed;
 	protected Vector2 gravity;
 	
 	/**
 	 * Create a default physics configuration without acceleration, velocity and gravity.
 	 */
 	public Physics2() {
 		this.acceleration = new Vector2(1.0f);
 		this.velocity = new Vector2();
 		this.maxVelocity = 1.0f;
 		this.gravity = new Vector2();
		this.speed = 0;
		this.maxSpeed = 0;
 	}
 
 	/**
 	 * Apply the velocity on a sprite.
 	 * @param sprite A sprite to use.
 	 */
	public void applyVelocity(Sprite sprite) { 
		float speed = (this.speed + this.velocity.x * this.acceleration.x >= this.maxSpeed) ? this.speed : this.maxSpeed;
		sprite.position.x = ((sprite.position.x + this.velocity.x) * this.acceleration.x) + speed;
        sprite.position.y = ((sprite.position.y + this.velocity.y) * this.acceleration.y) + speed;
         this.velocity.multiply(this.maxVelocity);
 	}
 	
 	/**
 	 * Apply gravity on a sprite.
 	 * @param sprite A sprite to use.
 	 */
 	public void applyGravity(Sprite sprite) {
 		sprite.position.add(this.gravity);
 		sprite.rectangle.setPosition(sprite.position.x, sprite.position.y);
 	}
 
 	// ---
 	// --- Getters and Setters declarations
 	// ---
 	
 	/**
 	 * @return the acceleration
 	 */
 	public final Vector2 getAcceleration() {
 		return acceleration;
 	}
 
 	/**
 	 * @return the velocity
 	 */
 	public final Vector2 getVelocity() {
 		return velocity;
 	}
 
 	/**
 	 * @return the maxVelocity
 	 */
 	public final float getMaxVelocity() {
 		return maxVelocity;
 	}
 
 	/**
 	 * @return the gravity
 	 */
 	public final Vector2 getGravity() {
 		return gravity;
 	}
 	
 	/**
 	 * @return the speed
 	 */
 	public final float getSpeed() {
 		return this.speed;
 	}
 	
 	/**
 	 * @return the max speed.
 	 */
 	public final float getMaxSpeed() {
 		return this.maxSpeed;
 	}
 
 	/**
 	 * @param speed the speed to set
 	 */
 	public final void setSpeed(float speed) {
 		this.speed = speed;
 	}
 
 	/**
 	 * @param maxSpeed the maxSpeed to set
 	 */
 	public final void setMaxSpeed(float maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 
 	/**
 	 * @param acceleration the acceleration to set
 	 */
 	public final void setAcceleration(Vector2 acceleration) {
 		this.acceleration = acceleration;
 	}
 
 	/**
 	 * @param velocity the velocity to set
 	 */
 	public final void setVelocity(Vector2 velocity) {
 		this.velocity = velocity;
 	}
 
 	/**
 	 * @param maxVelocity the maxVelocity to set
 	 */
 	public final void setMaxVelocity(float maxVelocity) {
 		this.maxVelocity = maxVelocity;
 	}
 
 	/**
 	 * @param gravity the gravity to set
 	 */
 	public final void setGravity(Vector2 gravity) {
 		this.gravity = gravity;
 	}
 }
