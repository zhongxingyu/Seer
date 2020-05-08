 package com.secondhand.model;
 
 import org.anddev.andengine.entity.shape.IShape;
 
 import com.badlogic.gdx.physics.box2d.Body;
 
 public interface IPhysicsEntity {
 	
	public float getCenterX();
 	
	public float getCenterY();
 	
	public void registerBody(final Entity entity, final Body body, final IShape shape);
 	
 	void deleteBody(boolean scheduledBody);
 
 	void applyImpulse(float posX, float posY, float maxSpeed);
 
 	Body createType(IShape shape, Entity entity);
 
 	Body getBody();
 
	public void setLinearDamping(float f);
 }
