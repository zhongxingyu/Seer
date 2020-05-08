 package com.thorgaming.throwme;
 
 import android.view.MotionEvent;
 
 /**
 * Used to store the useful parameters from a motion event so they don't change before being processed later
  * @author Thomas Cheyney
  * @version 1.0
  */
 public class MotionEventStore {
 
 	/**
 	 * X position on the screen that the user touched
 	 */
 	private float x;
 	/**
 	 * Y position on the screen that the user touched
 	 */
 	private float y;
 	/**
 	 * The action being performed e.g. up, down, move
 	 */
 	private int action;
 
 	/**
 	 * Constructs an event store with values
 	 * @param x X position on the screen that the user touched
 	 * @param y Y position on the screen that the user touched
 	 * @param action The action being performed e.g. up, down, move
 	 */
 	public MotionEventStore(float x, float y, int action) {
 		this.x = x;
 		this.y = y;
 		this.action = action;
 	}
 
 	/**
 	 * Constructs an event store with an event
 	 * @param event A {@link android.view.MotionEvent}
 	 */
 	public MotionEventStore(MotionEvent event) {
 		x = event.getX();
 		y = event.getY();
 		action = event.getAction();
 	}
 
 	/**
 	 * Get the x value stored
 	 * @return X value of this store
 	 */
 	public float getX() {
 		return x;
 	}
 
 	/**
 	 * Get the y value stored
 	 * @return Y value of this store
 	 */
 	public float getY() {
 		return y;
 	}
 
 	/**
 	 * Get the action value stored
 	 * @return Action value of this store
 	 */
 	public int getAction() {
 		return action;
 	}
 
 }
