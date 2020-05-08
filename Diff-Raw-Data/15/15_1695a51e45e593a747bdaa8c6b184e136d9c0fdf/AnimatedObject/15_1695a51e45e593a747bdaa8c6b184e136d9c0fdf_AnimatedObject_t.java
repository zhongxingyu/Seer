 package com.jpapps.pandroidGL;
 
 import android.util.Log;
 
 public class AnimatedObject {
 	private NumberMill framesMill;
 	private NumberMill pathX;
 	private NumberMill pathY;
 	private int width;
 	private int height;
 	private int x;
 	private int y;
 	private AnimatedObject init;
 		
 	public AnimatedObject(NumberMill framesMill, int width, int height) {
 		this(framesMill, width, height, 0,0);
 	}
 	
 	public AnimatedObject(NumberMill framesMill, int width, int height, int x, int y) {
 		this(framesMill, width, height, x, y, null, null);
 	}
 	
 	public AnimatedObject(NumberMill framesMill, int width, int height, int x, int y, NumberMill pathX, NumberMill pathY) {
 		this.setFramesMill(framesMill);
 		this.setPath(pathX, pathY);
 		this.setWidth(width);
 		this.setHeight(height);
 		this.setPosition(x,y);
		this.init = new AnimatedObject(this);
	}
	
	public AnimatedObject(AnimatedObject ao) {
		this.setFramesMill(ao.getFramesMill());
		this.setPath(ao.getPathX(),ao.getPathY());
		this.setWidth(ao.getWidth());
		this.setHeight(ao.getHeight());
		this.setPosition(ao.getX(),ao.getY());
 	}
 	
 	public void reset() {
 		this.setFramesMill(init.getFramesMill());
 		this.setHeight(init.getHeight());
 		this.setWidth(init.getWidth());
 		this.setPathX(init.getPathX());
 		this.setPathY(init.getPathY());
 		this.setPosition(init.getX(),init.getY());
 	}
 
 	public NumberMill getFramesMill() {
 		return framesMill;
 	}
 
 	public void setFramesMill(NumberMill frames) {
 		this.framesMill = frames;
 	}
 
 	public NumberMill getPathX() {
 		return pathX;
 	}
 	
 	public NumberMill getPathY() {
 		return pathY;
 	}
 
 	public void setPath(NumberMill pathX, NumberMill pathY) {
 		this.setPathX(pathX);
 		this.setPathY(pathY);
 	}
 	
 	public void setPathX(NumberMill pathX) {
 		if(pathX==null) {
 			int[] n = {0};
 			pathX = new NumberMill(n,NumberMill.ONCE);
 		}
 		this.pathX = pathX;
 	}
 	
 	public void setPathY(NumberMill pathY) {
 		if(pathY==null) {
 			int[] n = {0};
 			pathY = new NumberMill(n,NumberMill.ONCE);
 		}
 		this.pathY = pathY;
 	}
 	
 	public void advance() {
 		framesMill.advance();
 		pathX.advance();
 		pathY.advance();
 		this.x += pathX.getCurrent();
 		this.y += pathY.getCurrent();
 	}
 	
 	public int getCurrentFrame() {
 		return framesMill.getCurrent();
 	}
 	
 	public int[] getCurrentPath() {
 		int[] path = {pathX.getCurrent(), pathY.getCurrent()}; 
 		return path;
 	}
 	
 	public void noPath() {
 		this.setPathX(null);
 		this.setPathY(null);
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public int getX() {
 		return this.x;
 	}
 	
 	public int getY() {
 		return this.y;
 	}
 
 	public void setPosition(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 }
