 package model;
 
 import java.awt.Dimension;
 
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
  * The basic object in the Game, from which all other inherite. It provides
  * basic interaction and movement functionallity and it is provides a printable
  * version/image of itself.
  * 
  * @author danielschmidt
  * 
  */
 @SuppressWarnings("serial")
 public abstract class SpaceObject implements Serializable {
 	Point2D position;
 	float size;
 	Point2D direction;
 	protected boolean shouldBounceWithWall = true;
 
 	/**
 	 * Constructor for Spaceobject
 	 * 
 	 * @param position
 	 *            The position of the SpaceObject
 	 * @param size
 	 *            The size of the SpaceObject
 	 */
 	protected SpaceObject(Point2D position, Point2D direction, float size) {
 		super();
 		this.position = position;
 		this.size = size;
 		this.direction = direction;
 	}
 
 	/**
 	 * Moves the SpaceObject
 	 * 
 	 * @param resolution
 	 *            Resolution of the enviroment it is in
 	 * @param objects
 	 *            All objects of the enviroment it is in
 	 */
 	public abstract void move(Dimension resolution, ArrayList<SpaceObject> objects);
 
 	/**
 	 * Moves the SpaceObject and handles bounces with walls and asteroids
 	 * 
 	 * @param resolution
 	 *            Resolution of the enviroment it is in
 	 * @param objects
 	 *            All objects of the enviroment it is in
 	 */
 	protected void movePhysicallyCorrect(Dimension resolution, ArrayList<SpaceObject> objects) {
 		ArrayList<Boolean> borders = getBorders(resolution);
 		ArrayList<SpaceObject> overlapingObjects = this.overlapingObjects(objects);
 
 		if (borders.contains(true)) {
 			this.bounceWithWall(resolution);
 		} else if (!overlapingObjects.isEmpty()) {
 			bounceWithAsteroids(overlapingObjects);
 		} else {
 			this.position.setLocation(this.position.getX() + this.direction.getX(), this.position.getY()
 			        + this.direction.getY());
 		}
 	}
 
 	/**
 	 * Bounces with all SpaceObjects
 	 * 
	 * @param objects
 	 *            Should be all overlapping objects
 	 */
 	void bounceWithAsteroids(ArrayList<SpaceObject> overlapingObjects) {
 		for (SpaceObject overlaper : overlapingObjects) {
 			if (overlaper.getClass() == Asteroid.class) {
 				this.bounce(overlaper);
 			}
 		}
 		this.position.setLocation(this.position.getX() + this.direction.getX(),
 		        this.position.getY() + this.direction.getY());
 	}
 
 	/**
 	 * Gets all borders the Spaceobject touches
 	 * 
 	 * @param resolution
 	 *            Resolution of the enviroment it is in
 	 * @return An boolean ArrayList with length 4. (Top, Right, Bottom, Left) as
 	 *         e.g. margin in CSS
 	 */
 	ArrayList<Boolean> getBorders(Dimension resolution) {
 		ArrayList<Boolean> borders = new ArrayList<Boolean>();
 		Point2D nextPosition = new Point2D.Double(this.position.getX() + this.direction.getX(), this.position.getY()
 		        + this.direction.getY());
 		borders.add((nextPosition.getY() - this.size) < 0); // oben
 		borders.add((nextPosition.getX() + this.size) > resolution.width); // rechts
 		borders.add((nextPosition.getY() + this.size) > resolution.height); // unten
 		borders.add((nextPosition.getX() - this.size) < 0); // links
 		return borders;
 	}
 
 	/**
 	 * Tests if this SpaceObject is overlapped by the other
 	 * 
 	 * @param object
 	 *            The object which should be tested
 	 * @return <CODE> True </CODE> if this SpaceObjects overlaps the
 	 *         inputelement
 	 */
 	public boolean overlap(SpaceObject object) {
 		int distance = (int) object.position.distance(this.position);
 		return (distance <= (object.size + this.size));
 	}
 
 	/**
 	 * Returns all the overlapping SpaceObjects out of objects
 	 * 
 	 * @param objects
 	 *            an ArrayList of SpaceObjects, e.g. game.objects
 	 * @return the overlapping SpaceObjects out of objects
 	 */
 	public ArrayList<SpaceObject> overlapingObjects(ArrayList<SpaceObject> objects) {
 		ArrayList<SpaceObject> overlappingObjects = new ArrayList<SpaceObject>();
 		for (SpaceObject object : objects) {
 			if (object != this) {
 				if (this.overlap(object)) {
 					overlappingObjects.add(object);
 				}
 			}
 		}
 		return overlappingObjects;
 	}
 
 	/**
 	 * Exchanges the direction with another SpaceObject
 	 * 
 	 * @param object
 	 *            another SpaceObject
 	 * @return this Spaceobject (for chaining)
 	 */
 	public SpaceObject bounce(SpaceObject object) {
 		Point2D a = (Point2D) this.getDirection().clone();
 		this.setDirection((Point2D) object.getDirection().clone());
 		object.setDirection(a);
 
 		return this;
 	}
 
 	/**
 	 * Bounces this SpaceObject with a wall, by changing position and direction
 	 * 
 	 * @param resolution
 	 *            The resolution of the enviroment the SpaceObject is in
 	 */
 	public void bounceWithWall(Dimension resolution) {
 		if (shouldBounceWithWall) {
 			ArrayList<Boolean> borders = getBorders(resolution);
 			for (int i = 0; i < borders.size(); i++) {
 				if (borders.get(i)) {
 					switch (i) {
 						case 0:
 							this.direction.setLocation(this.direction.getX(), Math.abs(this.direction.getY()));
 						break;
 						case 1:
 							this.direction.setLocation(-Math.abs(this.direction.getX()), this.direction.getY());
 						break;
 						case 2:
 							this.direction.setLocation(this.direction.getX(), -Math.abs(this.direction.getY()));
 						break;
 						case 3:
 							this.direction.setLocation(Math.abs(this.direction.getX()), this.direction.getY());
 						default:
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Helper, which rotates a Vektor
 	 * 
 	 * @param direction
 	 *            the Vektor which should be rotated
 	 * @param angle
 	 *            given angle of Rotation
 	 * @return the rotated Vektor
 	 */
 	public static Point2D transformVektorViaAngle(Point2D direction, int angle) {
 		double theta = Math.toRadians(angle);
 		double x = (double) direction.getX() * Math.cos(theta) - (double) direction.getY() * Math.sin(theta);
 		double y = (double) direction.getX() * Math.sin(theta) + (double) direction.getY() * Math.cos(theta);
 
 		Point2D point = new Point2D.Double(x, y);
 		return point;
 	}
 
 	/**
 	 * Checks if an element of the class is in a list
 	 * 
 	 * @param className
 	 *            Name of the wanted class
 	 * @param list
 	 *            List of SpaceObjects which should be checked
 	 * @return <CODE> True </CODE> if the classname is same to classname of an
 	 *         object in the list, else <CODE> False </CODE>
 	 */
 	protected static boolean classIsInList(String className, ArrayList<SpaceObject> list) {
 		for (SpaceObject object : list) {
 			if (object.getClass().getName() == className) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks which elements should also be deleted
 	 * 
 	 * @param overlappingObjects
 	 *            An ArrayList of overlapping SpaceObjects
 	 * @return elements which should be deleted
 	 */
 	public ArrayList<SpaceObject> shouldBeBothDeletedIfOverlaps(ArrayList<SpaceObject> overlappingObjects) {
 		ArrayList<SpaceObject> newArrayList = new ArrayList<SpaceObject>();
 		for (SpaceObject object : overlappingObjects) {
 			if (this.getClass() != object.getClass()) newArrayList.add(object);
 		}
 		return newArrayList;
 	}
 
 	/**
 	 * Returns true if the overlapping of an Object in the list should cause
 	 * this element to be deleted
 	 * 
 	 * @param overlappingObjects
 	 *            list of SpaceObjects which overlap
 	 * @return <CODE> True </CODE> if this sort of SpaceObject should be deleted
 	 */
 	public abstract boolean shouldBeDeletedIfOverlaps(ArrayList<SpaceObject> overlappingObjects);
 
 	/**
 	 * Returns true if the crash with a wall is deadly
 	 * 
 	 * @param resolution
 	 *            Resolution of the enviroment the SpaceObject is in
 	 * @return <CODE>true</CODE> if the crash with a wall is deadly
 	 */
 	public abstract boolean shouldBeDeletedAsItCrashsWithWall(Dimension resolution);
 
 	/**
 	 * Transforms itself into a Printable
 	 * 
 	 * @return a Printable image of itself
 	 */
 	public abstract Printable getPrintable();
 
 	/**
 	 * Calculates the value of this SpaceObject
 	 * 
 	 * @return the value of this SpaceObject
 	 */
 	public abstract int getPoints();
 
 	/**
 	 * getter for the direction of the object
 	 * 
 	 * @return the direction of the object
 	 */
 	public Point2D getDirection() {
 		return direction;
 	}
 
 	/**
 	 * setter for the direction of the object
 	 * 
 	 * @param direction
 	 *            the direction of the object
 	 * @return the SpaceObject for chaining
 	 */
 	public SpaceObject setDirection(Point2D direction) {
 		this.direction = direction;
 		return this;
 	}
 
 	/**
 	 * getter for the position of the object
 	 * 
 	 * @return the position of the object
 	 */
 	public Point2D getPosition() {
 		return position;
 	}
 
 	/**
 	 * getter for the size of the object
 	 * 
 	 * @return the size of the object
 	 */
 	public float getSize() {
 		return size;
 	}
 
 }
