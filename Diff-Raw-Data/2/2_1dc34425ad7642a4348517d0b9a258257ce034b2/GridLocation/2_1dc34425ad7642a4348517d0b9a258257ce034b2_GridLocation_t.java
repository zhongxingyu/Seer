 package com.evervoid.state.geometry;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.Dimension;
 
 public class GridLocation implements Jsonable
 {
 	public static GridLocation fromJson(final Json j)
 	{
 		return new GridLocation(Point.fromJson(j.getAttribute("origin")), Dimension.fromJson(j.getAttribute("dimension")));
 	}
 
 	public Dimension dimension;
 	public Point origin;
 
 	public GridLocation(final int x, final int y)
 	{
 		this(new Point(x, y), new Dimension());
 	}
 
 	public GridLocation(final int x, final int y, final Dimension dimension)
 	{
 		this(new Point(x, y), dimension);
 	}
 
 	public GridLocation(final int x, final int y, final int width, final int height)
 	{
 		this(new Point(x, y), new Dimension(width, height));
 	}
 
 	public GridLocation(final Point origin)
 	{
 		this(origin, new Dimension());
 	}
 
 	public GridLocation(final Point origin, final Dimension dimension)
 	{
 		this.origin = origin;
 		this.dimension = dimension;
 	}
 
 	public GridLocation(final Point point, final int width, final int height)
 	{
 		this(point, new Dimension(width, height));
 	}
 
 	public GridLocation add(final int x, final int y)
 	{
 		return add(new Point(x, y));
 	}
 
 	public GridLocation add(final Point point)
 	{
 		return new GridLocation(origin.add(point), dimension);
 	}
 
 	public boolean collides(final GridLocation other)
 	{
 		for (int x = origin.x; x < origin.x + dimension.width; x++) {
 			for (int y = origin.y; y < origin.y + dimension.height; y++) {
 				if (other.collides(x, y)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean collides(final int x, final int y)
 	{
		return origin.x <= x && x < origin.x + dimension.width && origin.y <= y && y < origin.y + dimension.height;
 	}
 
 	public boolean collides(final Point point)
 	{
 		return collides(point.x, point.y);
 	}
 
 	public GridLocation constrain(final Dimension boundary)
 	{
 		return constrain(boundary.width, boundary.height);
 	}
 
 	public GridLocation constrain(final int width, final int height)
 	{
 		return constrain(0, 0, width, height);
 	}
 
 	public GridLocation constrain(final int minX, final int minY, final int maxX, final int maxY)
 	{
 		return new GridLocation(origin.constrain(minX, minY, maxX - dimension.width, maxY - dimension.height), dimension);
 	}
 
 	public Point delta(final GridLocation other)
 	{
 		return getCenter().subtract(other.getCenter());
 	}
 
 	/**
 	 * @param other
 	 *            Other Object to compare to.
 	 * @return True if the other object is a GridLocation of the same size, false otherwise.
 	 */
 	@Override
 	public boolean equals(final Object other)
 	{
 		if (super.equals(other)) {
 			return true;
 		}
 		if (!other.getClass().equals(getClass())) {
 			return false;
 		}
 		final GridLocation l = (GridLocation) other;
 		return origin.equals(l.origin) && dimension.equals(l.dimension);
 	}
 
 	public boolean fitsIn(final Dimension dimension)
 	{
 		return getX() + getWidth() <= dimension.width && getY() + getHeight() <= dimension.height;
 	}
 
 	public Point getCenter()
 	{
 		return origin.add(dimension.width / 2, dimension.height / 2);
 	}
 
 	public int getHeight()
 	{
 		return dimension.height;
 	}
 
 	public int getWidth()
 	{
 		return dimension.width;
 	}
 
 	public int getX()
 	{
 		return origin.x;
 	}
 
 	public int getY()
 	{
 		return origin.y;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		return toString().hashCode();
 	}
 
 	@Override
 	public Json toJson()
 	{
 		return new Json().setAttribute("origin", origin).setAttribute("dimension", dimension);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Loc[" + origin + " @ " + dimension + "]";
 	}
 }
