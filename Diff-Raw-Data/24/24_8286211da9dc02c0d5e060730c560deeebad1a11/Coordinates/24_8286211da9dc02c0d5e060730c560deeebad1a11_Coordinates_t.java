 package fr.iutvalence.java.projet.spaceinvaders;
 
 /**
  * This class allow to use couple of integers as coordinates.
  * Once the created, a Coordinate object is immutable (i.e. its state can not be changed)
  * 
  * @author Gallet Guyon
  */
 public class Coordinates
 {
	/*
	 * The X coordinate
	 */
 	private final int x;
 
	/**
	 * The Y coordinate
	 */
 	private final int y;
 
 	/**
	 * This constructor creates a new Coordinate <tt>(x,y)</tt> taking x and y as parameters. 
 	 * 
 	 * @param x the X coordinate of the object you create  
 	 * @param y the Y coordinate of the object you create
 	 */
 	public Coordinates(final int x, final int y)
 	{
 		super();
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * This method returns the value of X from of object's coordinates  
 	 * 
 	 * @return the value of X
 	 */
 	public int getX()
 	{
 		return this.x;
 	}
 
 	/**
 	 * This method returns the value of Y from of object's coordinates  
 	 * 
 	 * @return the value of Y
 	 */
 	public int getY()
 	{
 		return this.y;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return "x=" + this.x + ", y=" + this.y + " "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + this.x;
 		result = prime * result + this.y;
 		return result;
 	}
 
 	/**
 	 * Indicates whether some other object is "equal to" this one.<br/>
 	 * That is to say, the coordinates have to be the same :<br/>
 	 * x1 = x2
 	 * y1 = y2
 	 * 
 	 * @param obj the reference object with which to compare (supposed to be instance of Coordinate).
 	 * @return <tt>true</tt> if this object is the same as the <tt>obj</tt> argument; <tt>false</tt> otherwise.
 	 */
 	@Override
 	// FIXME explain me this
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
 		if (!(obj instanceof Coordinates))
 		{ // if they two haven't the same instance
 			return false;
 		}
 		Coordinates other = (Coordinates) obj; // Cast obj
 		if (this.x != other.x)
 		{ // if they have differences return false
 			return false;
 		}
 		if (this.y != other.y)
 		{
 			return false;
 		}
 		return true; // else return true
 	}
 }
