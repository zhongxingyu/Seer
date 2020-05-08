 package com.secondhand.model;
 
 
 import com.badlogic.gdx.physics.box2d.Contact;
 
 public interface IPhysicsWorld {
 
 	// Make your Entities stay on the screen
 	void setWorldBounds(final int levelWidth, final int levelHeight);
 
 	// Remove your WorldBounds.
 	void removeWorldBounds();
 
 	// checks if enemy has staight line to the entity
 	boolean isStraightLine(final Entity entity, final Enemy enemy);
 
 	// check the collision between entities
 	void checkCollision(final Contact contact);
 
 	// checks if the rectangular area i UnOccupied
 	boolean isAreaUnOccupied(final float x, final float y, final float r);
 
 	// making this a listener of PhysicsWorld
 	void setContactListener();
 
 	// connect this to gameWorld
 	void setGameWorld(final GameWorld gameWorld);
 }
