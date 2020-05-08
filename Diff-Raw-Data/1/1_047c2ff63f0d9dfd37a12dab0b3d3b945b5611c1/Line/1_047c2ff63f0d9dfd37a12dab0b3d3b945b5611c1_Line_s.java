 package draw.primitives;
 
 import java.awt.*;
 import java.awt.geom.*;

 import draw.DrawingPrimitive;
 
 public class Line extends DrawingPrimitive {
 	
 	private enum Angle
 	{
 		Horizontal,
 		Vertical,
 		Positive,
 		Negative
 	}
 
 	static final int BOUNDING_MARGIN = 5;
 	
 	Point start;
 	Point end;
 	
 	public Line(Point start, Point end) {
 		super(start, end);
 		this.start = start;
 		this.end = end;
 	}
 
 	@Override
 	public void draw(Graphics2D g) {
 		g.setColor(this.getColor());
 		g.draw(new Line2D.Double(start, end));
 	}
 
 	@Override
 	public Point getPosition() {
 		return start;
 	}
 
 	@Override
 	public Dimension getSize() {
 		return new Dimension(abs(start.getX()-end.getX()), abs(start.getY() - end.getY()));
 	}
 
 	private static int abs(double d) {
 		return (int)(d < 0 ? -d : d);
 	}
 
 	@Override
 	public void setPosition(Point p) {
 		end.translate((int)(p.getX()-start.getX()), (int)(p.getY()-start.getY()));
 		start = p;	
 	}
 
 	@Override
 	//TODO: discuss options for setting line "size"
 	public void setSize(Dimension d) {
 		if(start.getX() < end.getX())
 			end.setLocation(start.getX() + d.getWidth(), end.getY());
 		else
 			start.setLocation(end.getX() + d.getWidth(), start.getY());
 		
 		if(start.getY() < end.getY())
 			end.setLocation(end.getX(), start.getY() + d.getHeight());
 		else
 			start.setLocation(start.getX(), end.getY() + d.getHeight());
 	}
 
 	@Override
 	public Boolean contains(Point p) {
 		Polygon poly = new Polygon();
 		int x0 = (int)start.getX();
 		int y0 = (int)start.getY();
 		int x1 = (int)end.getX();
 		int y1 = (int)end.getY();
 		Angle a = null;
 		if(x0 == x1)
 			a = Angle.Vertical;
 		else if(y0 == y1)
 			a = Angle.Horizontal;
 		else if((x0 > x1 && y0 > y1) || (x1 > x0 && y1 > y0))
 			a = Angle.Negative;
 		else if((x0 > x1 && y0 < y1) || (x1 > x0 && y1 < y0))
 			a = Angle.Positive;
 		
 		java.awt.Rectangle r0 = new java.awt.Rectangle(x0 - BOUNDING_MARGIN, y0 - BOUNDING_MARGIN, 2 * BOUNDING_MARGIN, 2 * BOUNDING_MARGIN);
 		java.awt.Rectangle r1 = new java.awt.Rectangle(x1 - BOUNDING_MARGIN, y1 - BOUNDING_MARGIN, 2 * BOUNDING_MARGIN, 2 * BOUNDING_MARGIN);
 		
 		switch(a)
 		{
 		case Vertical:
 			poly = getVerticalLineBoundingPoly(r0, r1);
 			return poly.contains(p);
 		case Horizontal:
 			poly = getHorizontalLineBoundingPoly(r0, r1);
 			return poly.contains(p);
 		case Negative:
 			poly = getNegativeLineBoundingPoly(r0, r1);
 			return poly.contains(p);
 		case Positive:
 			poly = getPositiveLineBoundingPoly(r0, r1);
 			return poly.contains(p);
 		}
 		
 		return false;
 	}
 	
 	private Polygon getVerticalLineBoundingPoly(java.awt.Rectangle r0, java.awt.Rectangle r1)
 	{
 		Polygon p = new Polygon();
 		java.awt.Rectangle top, bottom;
 		if(r0.getMinY() < r1.getMinY())
 		{
 			top = r0;
 			bottom = r1;
 		}
 		else
 		{
 			top = r1;
 			bottom = r0;
 		}
 		p.addPoint((int)top.getMinX(), (int)top.getMinY());
 		p.addPoint((int)top.getMaxX(), (int)top.getMinY());
 		p.addPoint((int)bottom.getMinX(), (int)bottom.getMaxY());
 		p.addPoint((int)bottom.getMaxX(), (int)bottom.getMaxY());
 		return p;
 	}
 	
 	private Polygon getHorizontalLineBoundingPoly(java.awt.Rectangle r0, java.awt.Rectangle r1)
 	{
 		Polygon p = new Polygon();
 		java.awt.Rectangle left, right;
 		if(r0.getMinX() < r1.getMinX())
 		{
 			left = r0;
 			right = r1;
 		}
 		else
 		{
 			left = r1;
 			right = r0;
 		}
 		p.addPoint((int)left.getMinX(), (int)left.getMaxY());
 		p.addPoint((int)left.getMinX(), (int)left.getMinY());
 		p.addPoint((int)right.getMaxX(), (int)right.getMinY());
 		p.addPoint((int)right.getMaxX(), (int)right.getMaxY());
 		return p;
 	}
 	
 	private Polygon getNegativeLineBoundingPoly(java.awt.Rectangle r0, java.awt.Rectangle r1)
 	{
 		Polygon p = new Polygon();
 		java.awt.Rectangle top, bottom;
 		if(r0.getMinY() < r1.getMinY())
 		{
 			top = r0;
 			bottom = r1;
 		}
 		else
 		{
 			top = r1;
 			bottom = r0;
 		}
 		p.addPoint((int)top.getMinX(), (int)top.getMaxY());
 		p.addPoint((int)top.getMinX(), (int)top.getMinY());
 		p.addPoint((int)top.getMaxX(), (int)top.getMinY());
 		
 		p.addPoint((int)bottom.getMaxX(), (int)bottom.getMinY());
 		p.addPoint((int)bottom.getMaxX(), (int)bottom.getMaxY());
 		p.addPoint((int)bottom.getMinX(), (int)bottom.getMaxY());
 		
 		return p;
 	}
 	
 	private Polygon getPositiveLineBoundingPoly(java.awt.Rectangle r0, java.awt.Rectangle r1)
 	{
 		Polygon p = new Polygon();
 		java.awt.Rectangle top, bottom;
 		if(r0.getMinY() < r1.getMinY())
 		{
 			top = r0;
 			bottom = r1;
 		}
 		else
 		{
 			top = r1;
 			bottom = r0;
 		}
 		p.addPoint((int)top.getMinX(), (int)top.getMinY());
 		p.addPoint((int)top.getMaxX(), (int)top.getMinY());
 		p.addPoint((int)top.getMaxX(), (int)top.getMaxY());
 		
 		p.addPoint((int)bottom.getMaxX(), (int)bottom.getMaxY());
 		p.addPoint((int)bottom.getMinX(), (int)bottom.getMaxY());
 		p.addPoint((int)bottom.getMinX(), (int)bottom.getMinY());
 		
 		return p;
 	}
 
 	@Override
 	public DrawingPrimitive clone() {
 		return new Line(start, end);
 	}
 
 }
