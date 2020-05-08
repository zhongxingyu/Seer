 package com.liongrid.gameengine;
 
 
 public abstract class Component<TARGET_OBJECT extends GameObject> extends BaseObject{
	
 	public Component() {
 		super();
 	}
 	
 	public abstract void update(float dt, TARGET_OBJECT parent);
 	
 	//Made final to force subclasses to fill in the correct update method
 	@Override
 	public final void update(float dt, BaseObject parent){
 		throw new RuntimeException("You called an update method in component not " +
 				"meant to be called. Call the generic one instead!");
 	};
 	
 	@Override
 	public void reset(){
 		//TODO
 	}
 }
