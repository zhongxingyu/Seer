 package interfaces.lib;
 
 /**
  * A concrete 1 dimensional point that acts like a facade for the Point
  * abstract class. Gives a constructor to set the one coordinate, and a
  * method to retrieve the value. The origin of a Point1D has value 0. 
  * 
  * @author Kunal Desai, James Grugett, Prasanth Somasundar
  *
  */
 public final class Point1D extends Point
 {
 
 	/**
 	 * Constructs a one dimensional Point with coordinate given by {@code val}
 	 * 
 	 * @param coord The coordinate of the point being constructed.
 	 */
 	public Point1D(int coord)
 	{
 		super(1);
		setCoord(0, coord);
 	}
 
 	/**
 	 * This function returns the coordinate of the point specified on construction. 
 	 * @return the coordinate of the point
 	 */
 	public int getCoord()
 	{
 		return getCoord(0);
 	}
 
 	@Override
 	public Point copy()
 	{
 		return new Point1D(getCoord());
 	}
 	
 }
