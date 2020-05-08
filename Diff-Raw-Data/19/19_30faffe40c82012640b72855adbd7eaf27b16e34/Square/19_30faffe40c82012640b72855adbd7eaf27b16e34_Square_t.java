 package com.marakana.shapes;
 
 public class Square extends Rectangle {
 
 	public Square(int side) {
 		super(side, side);
 	}
 
	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		super.setHeight(width);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		super.setWidth(height);
	}
	
 }
