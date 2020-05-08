 /**
  * This code is created by:
  * @author Esa Varemo (2012-2013)
  * It is released with license: 
  * @license This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  *          View it at: http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 
 package fi.dy.esav.GameEngine;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.EnumSet;
 
 import fi.dy.esav.GameEngine.enums.ENTITY;
 
 public class Entity implements Comparable<Entity>{
 	
 	protected GameEngine engine;
 	
 	private EnumSet<ENTITY> properties;
 	protected double x = 0, y = 0, z = 0;
 	
 	/**
 	 * Default constructor
 	 * @param engine Reference to the GameEngine
 	 */
 	public Entity(GameEngine engine) {
 		this.engine = engine;
 		init();
 	}
 	
 	/**
 	 * Additional constructor
 	 * @param engine Reference to the GameEngine
 	 * @param position point to position the entity at
 	 */
 	public Entity(Point position, GameEngine engine) {
 		init();
 		this.engine = engine;
 		this.setPos(position);
 	}
 	
 	/**
 	 * Additional constructor
 	 * @param engine Reference to the GameEngine
 	 * @param x X-Coordinate to position the entity at
 	 * @param y Y-Coordinate to position the entity at
 	 */
 	public Entity(double x, double y, GameEngine engine) {
 		init();
 		this.setPos(x, y);
 	}
 	
 	/**
 	 * A initialization method that is common to all constructors
 	 */
 	private void init() {
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
 	 * Method to return the width of an (drawable) object
 	 * To be implemented by a subclass.
 	 */
 	public int getWidth() {	return -1; }
 	
 	/**
 	 * Method to return the height of an (drawable) object
 	 * To be implemented by a subclass.
 	 */
 	public int getHeight() { return -1; }
 
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
 	public double getX() {
 		return x;
 	}
 
 	/**
 	 * @param x the x to set
 	 */
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	/**
 	 * @return the y
 	 */
 	public double getY() {
 		return y;
 	}
 
 	/**
 	 * @param y the y to set
 	 */
 	public void setY(double y) {
 		this.y = y;
 	}
 	
 	/**
 	 * @return the Point where the Entity is
 	 */
 	public Point getPos() {
 		return new Point((int)this.x, (int)this.y);
 	}
 	
 	/**
 	 * @param point  The Point where to set the new position
 	 */
 	public void setPos(Point newPos) {
 		this.x = newPos.x;
 		this.y = newPos.y;
 	}
 	
 	/**
 	 * @param x X-coordinate for new location
 	 * @param y Y-coordinate for new location
 	 */
 	public void setPos(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	/**
 	 * @return the z
 	 */
 	public double getZ() {
 		return z;
 	}
 
 	/**
 	 * @param z the z to set
 	 */
 	public void setZ(int z) {
 		this.z = z;
 	}
 	
 	/**
 	 * Put the selected object to front
 	 */
 	public void toFront() {
 		this.z = engine.getStage().getMaxZ() + 10;
 	}
 	
 	/**
 	 * Put the selected object to back
 	 */
 	public void toBack() {
 		this.z = engine.getStage().getMinZ() - 10;
 	}
 	
 	/**
 	 * Method to compare the "depths" of different entities,
 	 * for arraylist.sort() to work
 	 * @param another entity to compare to
 	 * @return difference between the z coordinates of this and other entity
 	 */
 	public int compareTo(Entity otherEnt) {
 		return (int) (this.getZ() - ((Entity)otherEnt).getZ());
 	}
 }
