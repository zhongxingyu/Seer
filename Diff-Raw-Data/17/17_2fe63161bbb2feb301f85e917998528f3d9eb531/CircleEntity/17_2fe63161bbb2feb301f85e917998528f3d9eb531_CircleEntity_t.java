 package com.secondhand.model;
 
 public abstract class CircleEntity extends Entity {	
 	protected final float radius;
 	
 	private final Vector2 position;
 
 	public CircleEntity(final Vector2 position, final float radius, final boolean isEdible, final GameWorld level) {  
 		super(isEdible,level);
 		this.position = position;
 		this.radius = radius;
 	}
 	
 	@Override
 	public boolean isCircle(){
 		return true;
 	}
 	
 	@Override
 	public void createType(){
 	}
 	
 	
 	@Override 
 	public float getRadius() {
 		if(this.physics != null)
 			// return for body
 			return this.physics.getRadius();
 		else
 			return this.radius;
 	}
 	
	@Override
 	 public float getCenterX() {
 		 if(this.physics != null)
 			 return this.getCenterX();
 		 else
 			 return this.position.x;
 	}
 	 
	@Override
 	 public float getCenterY() {
 		 if(this.physics != null)
 			 return this.getCenterY();
 		 else
 			 return this.position.y;
 	}
 
 }
