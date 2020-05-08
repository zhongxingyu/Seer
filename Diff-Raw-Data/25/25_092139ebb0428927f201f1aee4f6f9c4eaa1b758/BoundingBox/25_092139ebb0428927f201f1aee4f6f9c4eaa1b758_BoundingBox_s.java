 /**
  * 
  */
 package fr.iutvalence.java.projet.spaceinvaders;
 
 // FIXME what about defining BB as immutable ?

// FIXME (SEEN) fix comment : comment the class for itself, not for the class that use it
 /**
  * The BoundingBox class define area.<br/>
  * An area is defined by position (Coordinates) and size (Coordinates) on the grid.
  * The 0 point is bottom left. 
  * @author Gallet Guyon
  */
 public class BoundingBox
 {
 	// FIXME fix comment (BB is not related to element objects)
 	/**
 	 * The position of the Element object on the 2D grid.
 	 */
 	private Coordinates position;
 	
 	/**
 	 * The size of the movable on the grid.<br/>
 	 * The size is defined by two values as a Coordinate object whose x means width and y means height.
 	 */
 	private final Coordinates size;
 
 	// FIXME fix and finish writing comment (to discuss)
 	/**
 	 * Sets the boundingBox of the elements.
 	 * @param position Set the position of the element in 2D grid
 	 * @param size Can't be change once it's allocated
 	 */
 	public BoundingBox(Coordinates position, Coordinates size)
 	{
 		super();
 		this.position = position;
 		this.size = size;
 	}
 	
 	// FIXME fix and finish writing comment (to discuss)
 	/**
 	 * Sets the boundingBox of the elements.
 	 * @param positionX Set the X position of the element on 2D grid.
 	 * @param positionY Set the Y position of the element on 2D grid.
 	 * @param sizeX Can't be change once it's allocated. Set the width of the element on 2D grid.
 	 * @param sizeY Can't be change once it's allocated. Set the height of the element on 2D grid.
 	 */
 	public BoundingBox(int positionX, int positionY, int sizeX, int sizeY)
 	{
 		super();
 		this.position = new Coordinates(positionX,positionY);
 		this.size = new Coordinates(sizeX,sizeY);
 	}
 
 	/**
 	 * Getter to return position of the element on 2D grid.
 	 * @return the position of the element.
 	 */
 	public Coordinates getPosition()
 	{
 		return this.position;
 	}
 
 	/**
 	 * Setter to change position of element on the 2D grid.
 	 * @param position the new position to set
 	 */
 	public void setPosition(Coordinates position)
 	{
 		this.position = position;
 	}
 
 	/**
 	 * Return the size of the element by the couple (width,height).
 	 * @return the size of the element by the couple (width,height).
 	 */
 	public Coordinates getSize()
 	{
 		return this.size;
 	}
 	
 	// FIXME add a method to compute the intersection with another BB (the result is a BB)
 	
 	@Override
 	public String toString()
 	{
 		return "BoundingBox [position=" + position + ", size=" + size + "]";
 	}
 }
