 package com.secondhand.model;
 
 import org.anddev.andengine.entity.shape.RectangularShape;
 
 
 public abstract class RectangleEntity extends Entity {
 	
 	protected RectangularShape rectangle;
 	
 	public RectangleEntity(final RectangularShape rectangle, final boolean isEdible, final GameWorld level) {
 		super(rectangle, isEdible, level);
 		
		
		
 		this.rectangle = rectangle;
 	}
 	
 	@Override
 	public float getRadius() {
 		// because don't really use this method for rectangles entities at all.
 		return 0;
 	}
 	
 	@Override
 	public void createType(){
 		//physics.createType(rectangle, this);
 	}	
 	
 	@Override
 	public boolean isCircle(){
 		return false;
 	}
 	
 	public float getWidth() {
 		return this.rectangle.getWidth();
 	}
 	
 	public float getHeight() {
 		return this.rectangle.getHeight();
 	}
 	
 
 	public RectangularShape getRectangle() {
 		return this.rectangle;
 	}
 }
