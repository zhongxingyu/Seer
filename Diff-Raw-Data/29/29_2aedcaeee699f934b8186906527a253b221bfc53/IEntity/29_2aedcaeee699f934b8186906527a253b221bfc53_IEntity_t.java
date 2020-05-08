 package spaceshooters.aurora.entity;
 
import pl.blackburn.gfx.Texture;
import pl.blackburn.math.AABB;
import pl.blackburn.math.Vector2f;
 import spaceshooters.aurora.level.ILevel;
 
 /**
  * 
  * Defines a basic entity.
  * 
  */
 public interface IEntity {
 	
 	/**
 	 * Sets the position of this entity.
 	 * 
 	 * @param pos
 	 *            New position.
 	 */
 	void setPosition(Vector2f pos);
 	
 	/**
 	 * Gets the current x position of this entity.
 	 * 
 	 * @return The x coordinate of this entity.
 	 */
 	float getX();
 	
 	/**
 	 * Sets the x position of this entity.
 	 * 
 	 * @param x
 	 *            New x coordinate.
 	 */
 	void setX(float x);
 	
 	/**
 	 * Gets the current y position of this entity.
 	 * 
 	 * @return The y coordinate of this entity.
 	 */
 	float getY();
 	
 	/**
 	 * Sets the y position of this entity.
 	 * 
 	 * @param y
 	 *            New y coordinate.
 	 */
 	void setY(float y);
 	
 	/**
 	 * Renders this entity.
 	 */
 	void render();
 	
 	/**
 	 * Updates this entity.
 	 * 
 	 * @param delta
 	 *            The delta.
 	 */
 	void update(int delta);
 	
 	int getHealth();
 	
 	void setHealth(int health);
 	
 	/**
 	 * Checks if this entity is still alive.
 	 * 
 	 * @return True if the entity is alive, false otherwise.
 	 */
 	boolean isAlive();
 	
 	/**
 	 * Sets the entity to be either alive or dead.
 	 * 
 	 * @param alive
 	 *            True if this entity should be alive, false otherwise.
 	 */
 	void setAlive(boolean alive);
 	
 	/**
 	 * Gets the texture of this entity.
 	 * 
 	 * @return The texture of this entity.
 	 */
 	Texture getTexture();
 	
 	/**
 	 * Gets the position of this entity.
 	 * 
 	 * @return The current position of this entity.
 	 */
 	Vector2f getPosition();
 	
 	/**
 	 * Gets the {@link EntityType} of this entity.
 	 * 
 	 * @return The type of this entity.
 	 */
 	IEntityType getEntityType();
 	
 	/**
 	 * Gets the level holding this entity.
 	 * 
 	 * @return The level.
 	 */
 	ILevel getLevel();
 	
 	/**
 	 * Gets the {@link AABB} of this collidable object.
 	 * 
 	 * @return The bounding box of this entity.
 	 */
 	AABB getBoundingBox();
 	
 	/**
 	 * Hook triggered on death of this entity. Can be overridden.
 	 */
 	void onDeath();
 	
 	/**
 	 * Hook triggered after the spawn of this entity. Can be overridden.
 	 */
 	void onSpawn();
 }
