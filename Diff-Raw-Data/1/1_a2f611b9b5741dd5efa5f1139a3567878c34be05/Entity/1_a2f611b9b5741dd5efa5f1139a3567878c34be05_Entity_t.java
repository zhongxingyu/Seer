 /**
  * This code is created by:
  * @author Esa Varemo (2012-2013)
  * It is released with license: 
  * @license This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  *          View it at: http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 
 package fi.dy.esav.GameEngine;
 
 import java.awt.Graphics;
 import java.util.EnumSet;
 
 import fi.dy.esav.GameEngine.enums.ENTITY;
 
 public class Entity implements Comparable<Entity>{
 	
 	protected GameEngine engine;
 	
 	private EnumSet<ENTITY> properties;
 	private int x, y, z;
 	
 	/**
 	 * Default constructor
 	 */
 	public Entity(GameEngine engine) {
 		this.engine = engine;
		properties = EnumSet.noneOf(ENTITY.class);
 	}
 	
 	/**
 	 * Method that is used to draw the entity on the screen (if applicable).
 	 * To be implemented by a subclass.
 	 * @param g The instance of graphics to draw on.
 	 */
 	public void draw(Graphics g) {	}
 	
 	/**
 	 * Method to process input, calculate actions & etc. (if applicable)
 	 * To be implemented by a subclass.
 	 */
 	public void act() {	}
 
 	/**
 	 * Set entity special properties (overwrite)
 	 * @param enumSet properties
 	 */
 	public void setProperties(EnumSet<ENTITY> enumSet) {
 		this.properties = enumSet;
 	}
 	
 	/**
 	 * Get entity special properties
 	 * @return enumSet of entity properties
 	 */
 	public EnumSet<ENTITY> getProperties() {
 		return this.properties;
 	}
 	
 	/**
 	 * Add special properties to entity
 	 * @param property Property to be set
 	 */
 	public void setProperty(ENTITY property) {
 		this.properties.add(property);
 	}
 	
 	/**
 	 * Remove special property from entity
 	 * @param property Property to be removed
 	 * @return If the operation succeeded
 	 */
 	public boolean removeProperty(ENTITY property) {
 		if(this.properties.contains(property)) {
 			this.properties.remove(property);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return the x
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * @param x the x to set
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	/**
 	 * @return the y
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * @param y the y to set
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	/**
 	 * @return the z
 	 */
 	public int getZ() {
 		return z;
 	}
 
 	/**
 	 * @param z the z to set
 	 */
 	public void setZ(int z) {
 		this.z = z;
 	}
 
 	@Override
 	public int compareTo(Entity otherEnt) {
 		return this.getZ() - ((Entity)otherEnt).getZ();
 	}
 }
