 package com.hack.elements.model.impl;
 
 import org.newdawn.slick.Image;
 
 import com.hack.elements.model.FreeObject;
 
 public class Hero implements FreeObject {
 
 	private float x;
 	private float y;
 	private int width;
 	private int height;
 	
 	public boolean usesDirection;
 	private Direction facing;
 	
 	private Image image;
 
 	@Override
 	public float getX() {
 		return x;
 	}
 
 	@Override
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	@Override
 	public float getY() {
 		return y;
 	}
 
 	@Override
 	public void setY(float y) {
 		this.y = y;
 	}
 
 	@Override
 	public int getWidth() {
 		return width;
 	}
 
 	@Override
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	@Override
 	public int getHeight() {
 		return height;
 	}
 
 	@Override
 	public void setHeight(int height) {
 		this.height = height;
 	}
 	
 	@Override
 	public boolean usesDirection(){
 		return true;
 	}
 	
 	@Override
 	public Direction getFacing() {
 		return facing;
 	}
 	
 	@Override
 	public void setFacing(Direction facing) {
 		this.facing = facing;
 	}
 	
 	@Override
 	public Image getImage() {
 		return image;
 	}
 	
 	@Override
 	public void setImage(Image image) {
 		this.image = image;
 	}
}
