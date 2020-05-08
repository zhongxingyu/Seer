 package com.relurori.engine.graphics;
 
 public class Scale {
 
 	private float xMultiplier;
 	private float yMultiplier;
	private float scaledHeight;
	private float scaledWidth;
 	
 	public Scale(float width, float height) {
 		xMultiplier = width / 1280;
 		yMultiplier = height / 720;
 		
		scaledHeight = height * yMultiplier;
		scaledWidth = width * xMultiplier;
 	}
 	
 	public float getScaledY(float y) {
 		return yMultiplier * y;
 	}
 	
 
 	public float getScaledX(float x) {
 		return xMultiplier * x;
 	}
	
	public float getHeight() {
		return scaledHeight;
	}
	
	public float getWidth() {
		return scaledWidth;
	}
 }
