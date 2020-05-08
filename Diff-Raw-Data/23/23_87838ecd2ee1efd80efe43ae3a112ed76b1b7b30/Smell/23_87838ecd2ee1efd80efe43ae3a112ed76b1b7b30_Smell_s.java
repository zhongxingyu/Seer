 package com.unexpected.ant.model.entity;
 
 import com.unexpected.ant.model.AbstractEntity;
 
 /**
  * Abstract base class for the smells in the game
  */
 public abstract class Smell extends AbstractEntity {
 	public int intensity;
 
 	@Override
 	public void action(long tickCount) {
		super.action(tickCount);
 		if (tickCount % 12 == 0)
 			decay();
 	}
 
 	/**
 	 * Decreases the intensity of the smell
 	 */
 	public void decay() {
 		intensity--;
 		if (intensity == 0) {
 			//removeSmell from Cells
 			remove();
 		}
 	}
 
 	/**
 	 * Gets the intensity of the smell
 	 *
 	 * @return intensity of the smell
 	 */
 	public int getIntensity() {
 		return intensity;
 	}
 
 	/**
 	 * Sets the intensity of the smell
 	 *
 	 * @param intensity intensity of the smell
 	 */
 	public void setIntensity(int intensity) {
 		this.intensity = intensity;
 	}
 }
