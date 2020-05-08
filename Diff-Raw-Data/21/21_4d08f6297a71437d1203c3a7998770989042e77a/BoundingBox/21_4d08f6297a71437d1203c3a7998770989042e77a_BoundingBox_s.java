 /**
  * 
  */
 package fr.iutvalence.java.projet.spaceinvaders;
 
 /**
  * The BoundingBox class define area.<br/>
  * An area is defined by position (Coordinates) and size (Coordinates) on the grid.
  * The coordinates have to be positive or equals to 0 otherwise it returns NegativeCoordinates exception  
  * The 0 point is bottom left, the grid is represented like this : <br/>
  * <br/>
  *	Y<br/>
  *	^<br/>
  *	|<br/>
  *	|<br/>
  *	|<br/>
  *	|	 (x1,y2)__________(x2,y2)<br/>
  *	|		|				 |<br/>
  *	|		|				 |<br/>
  *	|		|				 |<br/>
  *	|		|				 |<br/>
  *	|		|				 |<br/>
  *	|		|				 |<br/>
  *	|		|				 |<br/>
  *	|	 (x1,y1)__________(x2,y1)<br/>
  *	|<br/>
  *	|<br/>		
  *	0---------------------------------> X<br/>
  *			
  * @author Gallet Guyon
  */
 public class BoundingBox
 {
 	// FIXME (SEEN) fix comment (BB is not related to element objects)
 	/**
 	 * The position of the object on the 2D grid.
 	 */
 	private final Coordinates position;
 	
 	/**
 	 * The size of the movable on the grid.<br/>
 	 * The size is defined by two values as a Coordinate object whose x means width and y means height.
 	 */
 	private final Coordinates size;
 
 	// ********************* Constructor ************************
 	/**
 	 * Creates a new bounding box, whose position and size are given as parameters 
 	 * @param position Set the position of the element in 2D grid
 	 * @param size Can't be change once it's allocated
 	 * @throws NegativeCoordinatesException If size has at least one negative coordinate.
 	 */
	// TODO Size can't be negative because of interBound method.
 	public BoundingBox(Coordinates position, Coordinates size) throws NegativeCoordinatesException
 	{
		// FIXME size cannot be negative but position can be
 		super();
		if(position.getX() < 0
				|| position.getY() < 0
				|| size.getX() < 0
				|| size.getY() < 0)
 			throw new NegativeCoordinatesException();
 		this.position = position;
 		this.size = size;
 	}
 
 	//**************** Method ************************
 
 	/**
 	 * Method to change position of element on the 2D grid.
 	 * @param newPosition (Coordinates) the new position to set 
 	 * @return New BoundingBox with new coordinates.
 	 * @throws NegativeCoordinatesException If position is negative.
 	 */
 	public BoundingBox moveTo(Coordinates newPosition) throws NegativeCoordinatesException
 	{
 		return new BoundingBox(newPosition, this.size);
 	}
 	
 	/**
 	 * Method to translate position of element on the 2D grid.
 	 * @param delta (Coordinates) take the old coordinates and add delta to it.
 	 * @return New BoundingBox with new coordinates.
 	 * @throws NegativeCoordinatesException If position is negative.
 	 */
 	public BoundingBox translate(Coordinates delta) throws NegativeCoordinatesException
 	{
 		return this.moveTo(this.position.translate(delta));
 	}
 	
 	/**
 	 * Method to change position of element on the 2D grid.
 	 * @param newPosition (Coordinates) the new position to set 
 	 * @return New BoundingBox with new coordinates.
 	 * @throws NegativeCoordinatesException If the size is negative.
 	 */
 	public BoundingBox reSize(Coordinates newSize) throws NegativeCoordinatesException
 	{
 		return new BoundingBox(this.position, newSize);
 	}
 
 	
 	/**
 	 * Method to check if a point is contained in this bounding box.
 	 * @param point Coordinates of the point to check. 
 	 * @param B BoundingBox area to check in
 	 * @return Return true if the point is in the area, false otherwise.
 	 */
 	// FIXME (FIXED) make this method public and using this as target BB
 	public boolean pointIn(Coordinates point)
 	{
 		int x = point.getX();
 		int y = point.getY();
 		int bx1 = this.position.getX();
 		int bx2 = this.position.getX() + this.size.getX();
 		int by1 = this.position.getY();
 		int by2 = this.position.getY() + this.size.getY();
 		
 		return ((x >= bx1) && (x <= bx2) && (y >= by1) && (y <= by2));
 	}
 	
 	/**
 	 * Intersection of the current bounding box and another
 	 * @param bb Bounding box to calculate intersection with.
 	 * @return Return the bounding box resulting of this one and bb intersection if it exists, null otherwise.
 	 */
 	// FIXME (FIXED) rename method
 	public BoundingBox intersection(BoundingBox bb)
 	{
 		int x1,x2,y1,y2,hx1,hx2,hy1,hy2;
 		int posResX, posResY, sizeResX, sizeResY;
 		
 		hx1 = this.position.getX();
 		hx2 = this.position.getX() + this.size.getX();
 		hy1 = this.position.getY();
 		hy2 = this.position.getY() + this.size.getY();
 		
 		x1 = bb.position.getX();
 		x2 = bb.position.getX() + bb.size.getX();
 		y1 = bb.position.getY();
 		y2 = bb.position.getY() + bb.size.getY();
 		
		// FIXME it does not work when the current bb is completely contained by bb
 		if(			(bb.pointIn(new Coordinates(hx1,hy1)))
 				|| 	(bb.pointIn(new Coordinates(hx2,hy1)))
 				|| 	(bb.pointIn(new Coordinates(hx2,hy2)))
				|| 	(bb.pointIn(new Coordinates(hx1,hy2))) )
 		{
 			if(x1 <= hx1)
 				posResX = hx1;
 			else
 				posResX = x1;
 			if(x2 >= hx2)
 				sizeResX = hx2;
 			else
 				sizeResX = x2;
 			if(y1 <= hy1)
 				posResY = hy1;
 			else
 				posResY = y2;
 			if(y2 >= hy2)
 				sizeResY = hy2;
 			else
 				sizeResY = y2;
 			
 			try
 			{
 				return new BoundingBox(new Coordinates(posResX, posResY), new Coordinates(sizeResX, sizeResY)) ;
 			}
 			catch (NegativeCoordinatesException e)
 			{
 				return null;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	//***************** Getters and Setters ************************
 	
 	/**
 	 * Return the size of the element by the couple (width,height).
 	 * @return the size of the element by the couple (width,height).
 	 */
 	public Coordinates getSize()
 	{
 		return this.size;
 	}
 	
 	/**
 	 * Getter to return position of the element on 2D grid.
 	 * @return the position of the element.
 	 */
 	public Coordinates getPosition()
 	{
 		return this.position;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "BoundingBox [position=" + this.position + ", size=" + this.size + "]";
 	}
 }
