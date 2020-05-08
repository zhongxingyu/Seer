 package com.github.joukojo.testgame.world.core;
 
 public interface Moveable extends Drawable {
 
	public void move();
 
	public boolean isOutside(int x, int y); 
 	
	public boolean isDestroyed();
 
	public void setDestroyed(boolean b); 
 }
