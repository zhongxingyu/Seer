 package com.evervoid.state;
 
 public class GridLocation
 {
 	public Dimension dimension;
 	public Point origin;
 
 	public GridLocation(final int x, final int y)
 	{
 		this(new Point(x, y), new Dimension());
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
 		// TODO
 		return false;
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
 
 	public boolean sameAs(final GridLocation other)
 	{
 		return origin.sameAs(other.origin) && dimension.sameAs(other.dimension);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Loc[" + origin + " @ " + dimension + "]";
 	}
 }
