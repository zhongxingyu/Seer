 package com.liongrid.gameengine;
 
 
 public class LCollisionNonRotateSquare extends LCollisionObject implements LShape.NonRotateSquare {
 	
 	private float mWidth;
 	private float mHeight;
 
 	public LCollisionNonRotateSquare(int type, LGameObject owner, 
 			float width, float height){
 		super(type, owner);
 		setDimensions(width, height);
 	}
 	
 	public LCollisionNonRotateSquare(int type, LGameObject owner) {
 		super(type, owner);
 	}
 
 	@Override
 	public int getShape() {
		return LShape.NON_ROTATE_SQUARE;
 	}
 	
 	public float getWidth(){
 		return mWidth;
 	}
 	
 	public float getHeight(){
 		return mHeight;
 	}
 
 	public void setDimensions(float width, float height){
 		mWidth = width;
 		mHeight = height;
 	}
 }
