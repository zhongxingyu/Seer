 package com.github.joukojo.testgame.world.core;
 
 public interface Moveable extends Drawable {
 
	void move();
 
	boolean isOutside(int xCoord, int yCoord); 
 	
	boolean isDestroyed();
 
	void setDestroyed(boolean isDestroyed); 
 }
