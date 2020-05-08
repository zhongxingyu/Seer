 package com.secondhand.model;
 
 import java.util.List;
 
 public interface IPhysicsEntity {
 	
 	float getCenterX();
 	
 	float getCenterY();
 	
 	float getRadius();
 	
 	void setRadius(final float radius);
 	
 	void deleteBody(boolean scheduledBody);
 
 	void applyImpulse(float posX, float posY, float maxSpeed);
 
 
 	void setLinearDamping(float f);
 	
 	// detach from andengine rendering. 
 	void detachSelf();
 
 	float computePolygonRadius(final List<Vector2> polygon);
 
 }
