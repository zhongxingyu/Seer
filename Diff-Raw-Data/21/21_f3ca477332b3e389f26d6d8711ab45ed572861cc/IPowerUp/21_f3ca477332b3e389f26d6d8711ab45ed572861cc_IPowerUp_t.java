 package com.secondhand.model.entity;
 
 import com.secondhand.model.resource.PowerUpType;
 
 public interface IPowerUp {
 
 	PowerUpType getPowerUpType();
 	
 	float getDuration();
 	
 	void activateEffect(final Player player);
 	
 	boolean hasText();
 	
 	String getText();
 	
	void resetPlayerColor();
	
 	float getR();
 	float getG();
 	float getB();
 
 	void eaten();
 
 	void activatePowerUp();
 
 	void deactivateEffect(boolean hasAnother);
 	
 	// frequency of this powerup. Given on a scale from 1-10.
 	// where 10 is dirt common and 1 is super rare. 
 	// static now, but must be specified for every powerup.
 	//int getFrequency();
 }
