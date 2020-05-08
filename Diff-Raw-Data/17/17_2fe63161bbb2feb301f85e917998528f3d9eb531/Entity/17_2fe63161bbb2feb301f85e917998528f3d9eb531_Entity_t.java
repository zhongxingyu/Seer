 package com.secondhand.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 public abstract class Entity {
 
 	private boolean isEdible;
 	protected IPhysicsEntity physics;
 	
 	// only the initial postion. the updated position is later fetched by physics. 
 	
 	protected final GameWorld gameWorld;
 	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	public Entity(final boolean isEdible,
 			final GameWorld level) {
 		//this.shape = shape;
 		this.isEdible = isEdible;
 		this.gameWorld = level;
 		bodyScheduledForDeletion = false;
 	}
 
 	public void setPhysics(final IPhysicsEntity physics){
 		this.physics = physics;
 		createType();
 	}
 	
 	public abstract void createType();
 
 	public abstract boolean isCircle();
 
 
 	public boolean isEdible() {
 		return this.isEdible;
 	}
 	
 	public IPhysicsEntity getPhysics(){
 		return this.physics;
 	}
 
 	public void setIsEdible(final boolean isEdible) {
 		this.isEdible = isEdible;
 	}
 
 	public abstract float getRadius();
 	
 	// how much every unit(pixel) of radius is worth in points.
 	public abstract float getScoreWorth();
 
 	public int getScoreValue() {
 		return (int) (this.getRadius() * this.getScoreWorth());
 	}
 
 	// remove this entity from andengine rendering and the physics world.
 	private void removeEntity() {
 		this.gameWorld.getEntityManager().removeEntityFromList(this);
 
 		destroyEntity();
 
 	}
 
 	// TODO detaching should be done by view or physics, wherever 
 	// we save the shape
 	public void destroyEntity() {
 
 		// we can't remove the body within a contact listener
 		scheduleBodyForDeletion();
 
 		// Detach the shape from AndEngine-rendering
 		// do this from IPhysicsEntity instead.
 		this.physics.detachSelf();
 	}
 
 	private boolean bodyScheduledForDeletion;
 
 	private boolean isBodyScheduledForDeletion() {
 		return this.bodyScheduledForDeletion;
 	}
 
 	private void scheduleBodyForDeletion() {
 		//pcs.firePropertyChange(propertyName, oldValue, newValue)
 		
 		this.gameWorld.getEntityManager().scheduleEntityForDeletion(this);
 
 		this.bodyScheduledForDeletion = true;
 	}
 
 	// only valid when the body has been scheduled for deletion.
 	public void deleteBody() {
 		physics.deleteBody(isBodyScheduledForDeletion());
 	}
 
 	// called when this entity is eaten up.
 	protected void wasEaten() {
 		removeEntity();
 	}
 
 	public boolean getRotation() {
 		return this instanceof Player ? false : true;
 	}
 	
 	public void addPropertyChangeListener(final PropertyChangeListener listener){
 		pcs.addPropertyChangeListener(listener);
 	}

	 public float getCenterX() {
			 return this.getCenterX();
	}
	 
	 public float getCenterY() {
		 return this.getCenterY();
	}

 }
