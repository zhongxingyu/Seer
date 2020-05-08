 package com.punchline.javalib.entities.components;
 
 import com.punchline.javalib.entities.Component;
 
 /**
  * Base class for Components such as a health bar, magic meter, or any other stat with a max value.
  * @author Nathaniel
  *
  */
public abstract class StatBar implements Component {
 	
 	protected double current;
 	protected double max;
 	
 	/**
 	 * Constructs a StatBar.
 	 * @param max The StatBar's maximum value.
 	 */
 	public StatBar(double max) {
 		this.max = max;
 		this.current = max;
 	}
 	
 	/**
 	 * @return The StatBar's current value.
 	 */
 	public double getCurrentValue() {
 		return current;
 	}
 	
 	/**
 	 * @return The StatBar's max value.
 	 */
 	public double getMaxValue() {
 		return max;
 	}
 	
 	/**
 	 * @return Whether the StatBar is empty.
 	 */
 	public boolean isEmpty() {
 		return current <= 0;
 	}
 	
 	/**
 	 * @return How full the StatBar is, from 0 to 1.
 	 */
 	public float fraction() {
 		return (float)(current/max);
 	}
 	
 	/**
 	 * Sets the current value of the StatBar.
 	 * @param current The desired value.
 	 */
 	public void setCurrentValue(double current) {
 		
 		if (current < this.current) onDrain(this.current - current); //Call drain event
 		if (current > this.current) onFill(current - this.current); //Call fill event
 		
 		this.current = current;
 		
 		if (current > max) current = max;
 		if (current < 0) { current = 0; onEmpty(); } //Call empty event
 	}
 	
 	/**
 	 * Sets the max value of the StatBar.
 	 * @param max The desired value. If negative, nothing will happen.
 	 */
 	public void setMaxValue(double max) {
 		if (max < 0) return;
 		
 		this.max = max;
 		
 		if (current > max) current = max;
 	}
 	
 	/**
 	 * Drains the StatBar by the given amount.
 	 * @param amount The amount to drain.
 	 */
 	public void drain(double amount) {
 		setCurrentValue(current - amount);
 	}
 	
 	/**
 	 * Sets the StatBar's current value to 0.
 	 */
 	public void drainEmpty() {
 		setCurrentValue(0);
 	}
 	
 	/**
 	 * Fills the StatBar by the given amount.
 	 * @param amount The amount to fill.
 	 */
 	public void fill(double amount) {
 		setCurrentValue(current + amount);
 	}
 	
 	/**
 	 * Sets the StatBar's current value to its maximum value.
 	 */
 	public void fillMax() {
 		setCurrentValue(max);
 	}
 	
 	/**
 	 * Called when the StatBar is drained by any amount.
 	 * @param amount The amount drained.
 	 */
 	protected void onDrain(double amount) { }
 	
 	/**
 	 * Called when the StatBar is filled by any amount.
 	 * @param amount The amount filled.
 	 */
 	protected void onFill(double amount) { }
 	
 	/**
 	 * Called when the StatBar becomes empty.
 	 */
 	protected void onEmpty() { }
 	
 }
