 
 /**
  * 
  */
 package fr.iutvalence.java.projet.spaceinvaders;
 
 /**
  * The BoundingBox class define area.<br/>
  * An area is defined by position (Coordinates) and size (Coordinates) on the grid. The coordinates have to be positive
  * or equals to 0 otherwise it returns NegativeCoordinates exception The 0 point is bottom left, the grid is represented
  * like this : <br/>
  * <br/>
  * Y<br/>
  * ^<br/>
  * |<br/>
  * |<br/>
  * |<br/>
  * | (x1,y2)__________(x2,y2)<br/>
  * | | |<br/>
  * | | |<br/>
  * | | |<br/>
  * | | |<br/>
  * | | |<br/>
  * | | |<br/>
  * | | |<br/>
  * | (x1,y1)__________(x2,y1)<br/>
  * |<br/>
  * |<br/>
  * 0---------------------------------> X<br/>
  * 
  * @author Gallet Guyon
  */
 public class BoundingBox
 {
 	/**
 	 * Coordinates of the bottom-left corner of the bounding box
 	 */
 	private final Coordinates position;
 
 	/**
 	 * The size (width, height) of the bounding box, defined by a <tt>Coordinate</tt> object whose x means width and y means height.
 	 */
 	private final Coordinates size;
 
 	// ********************* Constructor ************************
 	/**
 	 * Creates a new bounding box, whose position and size are given as parameters
 	 * 
 	 * @param position
 	 *            Set the position of the element in 2D grid
 	 * @param size
 	 *            Can't be change once it's allocated
 	 * @throws NegativeSizeException
 	 *             If size has at least one negative coordinate.
 	 */
 	public BoundingBox(Coordinates position, Coordinates size) throws NegativeSizeException
 	{
 		super();
 		if (size.getX() < 0 || size.getY() < 0)
 			throw new NegativeSizeException(size);
 		this.position = position;
 		this.size = size;
 	}
 
 	// **************** Method ************************
 
 	/**
 	 * Method to change position of the bottom-left corner of the bounding box
 	 * 
 	 * @param newPosition
 	 *            (Coordinates) the new position to set
 	 * @return New BoundingBox with new coordinates. 
 	 */
 	public BoundingBox moveTo(Coordinates newPosition)
 	{
 		try
 		{
 			return new BoundingBox(newPosition, this.size);
 		}
 		catch (NegativeSizeException e)
 		{
 			//  here, the exception can not be raised since (as the bounding box exists) the size can not be negative
 			// so, we can safely ignore it
 			// FIXME (SEEN) remove the debug message
 			return null;
 		}
 	}
 
 	/**
 	 * Method to translate position of the bottom-left corner of the bounding box
 	 * 
 	 * @param delta
 	 *            (Coordinates) take the old coordinates and add delta to it.
 	 * @return New BoundingBox with new coordinates.
 	 * @throws NegativeSizeException
 	 *             If position is negative.
 	 */
 	public BoundingBox translate(Coordinates delta) throws NegativeSizeException
 	{
 		return this.moveTo(this.position.translate(delta));
 	}
 
 	/**
 	 * Method to change the size of the bounding box
 	 * 
 	 * @param newSize
 	 *            (Coordinates) the new size to set
 	 * @return New BoundingBox with new size.
 	 * @throws NegativeSizeException
 	 *             If the size is negative.
 	 */
 	public BoundingBox reSize(Coordinates newSize) throws NegativeSizeException
 	{
 		return new BoundingBox(this.position, newSize);
 	}
 
 	/**
 	 * Method to check if a point is contained in this bounding box.
 	 * 
 	 * @param point
 	 *            Coordinates of the point to check.
 	 * @return  <tt>true</tt> if the point is in the area, <tt>false</tt> otherwise.
 	 */
 	public boolean pointIn(Coordinates point)
 	{
 		int x = point.getX();
 		int y = point.getY();
 		int bx1 = this.position.getX();
 		int bx2 = this.position.getX() + this.size.getX();
 		int by1 = this.position.getY();
 		int by2 = this.position.getY() + this.size.getY();
 
		return ((x > bx1) && (x < bx2) && (y > by1) && (y < by2));
 	}
 
 	/**
 	 * Intersection of the current bounding box and another
 	 * 
 	 * @param bb
 	 *            Bounding box to calculate intersection with.
 	 * @return Return the bounding box resulting of this one and bb intersection if it exists, null otherwise.
 	 */
 	public BoundingBox intersection(BoundingBox bb)
 	{
 		int x1, x2, y1, y2, hx1, hx2, hy1, hy2;
 		int posResX, posResY, sizeResX, sizeResY;
 
 		hx1 = this.position.getX();
 		hx2 = this.position.getX() + this.size.getX();
 		hy1 = this.position.getY();
 		hy2 = this.position.getY() + this.size.getY();
 
 		x1 = bb.position.getX();
 		x2 = bb.position.getX() + bb.size.getX();
 		y1 = bb.position.getY();
 		y2 = bb.position.getY() + bb.size.getY();
 
 		if ((bb.pointIn(new Coordinates(hx1, hy1))) || (bb.pointIn(new Coordinates(hx2, hy1)))
 				|| (bb.pointIn(new Coordinates(hx2, hy2))) || (bb.pointIn(new Coordinates(hx1, hy2)))
 				|| (this.pointIn(new Coordinates(x1, y1))) || (this.pointIn(new Coordinates(x2, y1)))
 				|| (this.pointIn(new Coordinates(x2, y2))) || (this.pointIn(new Coordinates(x1, y2))))
 		{
 			
 			posResX = Math.max(x1, hx1);
 			sizeResX = Math.min(x2, hx2);
 			posResY = Math.max(y1, hy1);
 			sizeResY = Math.min(y2, hy2);
 			
 			if((sizeResX - posResX) < 0)
 				sizeResX = posResX - sizeResX;
 			else
 				sizeResX = sizeResX - posResX;
 			if((sizeResY - posResY) < 0)
 				sizeResY = posResY - sizeResY;
 			else
 				sizeResY = sizeResY - posResY;
 				
 			
 			try
 			{
 				return new BoundingBox(new Coordinates(posResX, posResY), new Coordinates(sizeResX, sizeResY));
 			}
 			catch (NegativeSizeException e)
 			{
 				System.out.println(e.getNegativeCoordinatesException());
 				return null;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	// ***************** Getters and Setters ************************
 
 	/**
 	 * Return the size of the bottom-left corner of the bounding box as a couple (width,height).
 	 * 
 	 * @return the size of the bottom-left corner of the bounding box as a couple (width,height).
 	 */
 	public Coordinates getSize()
 	{
 		return this.size;
 	}
 
 	/**
 	 * Getter to return position of the bottom-left corner of the bounding box.
 	 * 
 	 * @return the position of the bottom-left corner of the bounding box.
 	 */
 	public Coordinates getPosition()
 	{
 		return this.position;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return "BoundingBox [position=" + this.position + ", size=" + this.size + "]";
 	}
 	
 
 	/**
 	 * Indicates whether some other object is "equal to" this one.<br/>
 	 * That is to say, the BoundingBox have to be the same (equals size and positions)<br/> 
 	 */
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (this == obj)
 		{ // if they two have the same references
 			return true;
 		}
 		if (obj == null)
 		{ // if one is null
 			return false;
 		}
 		if (!(obj instanceof BoundingBox))
 		{ // if they two haven't the same instance
 			return false;
 		}
 		BoundingBox other = (BoundingBox) obj; // Cast obj
 		return ((this.position.equals(other.position)) && (this.size.equals(other.size)));
 	}
 }
