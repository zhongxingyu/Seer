 package draw.primitives;
 
 import java.awt.*;
 import java.awt.geom.*;
 
 import java.math.*;
 
 import draw.DrawingPrimitive;
 
 public class Rectangle extends DrawingPrimitive {
 
 	public enum CornerStyle
 	{
 		Sharp,
 		Rounded
 	}
 	
 	private static final int ROUND_CORNER_RADIUS = 5;
 	
 	private Point topLeft;
 	private Dimension size;
 	private CornerStyle cornerStyle = CornerStyle.Sharp;
 	
 	public Rectangle(Point p1, Point p2)
 	{
 		super(p1, p2);
 		
 		if(p1 != null && p2 != null)
 		{
 			double minX = p1.getX() < p2.getX() ? p1.getX() : p2.getX();
 			double minY = p1.getY() < p2.getY() ? p1.getY() : p2.getY();
 			double maxX = p1.getX() > p2.getX() ? p1.getX() : p2.getX();
 			double maxY = p1.getY() > p2.getY() ? p1.getY() : p2.getY();
 			p1 = new Point((int)minX, (int)minY);
 			p2 = new Point((int)maxX, (int)maxY);
 			this.setPosition(p1);
 			this.setSize(new Dimension((int)Math.abs(p2.getX()-p1.getX()), (int)Math.abs(p2.getY()-p1.getY())));
 		}
 	}
 
 	@Override
 	public void draw(Graphics2D g) {
 		g.setColor(this.getColor());
 		if(isSelected())
 		{
 			BasicStroke stroke = new BasicStroke(3.0f);
 			g.setStroke(stroke);
			if(this.cornerStyle == CornerStyle.Sharp)
				g.drawRect((int)topLeft.getX(), (int)topLeft.getY(), (int)size.getWidth(), (int)size.getHeight());
			else if(this.cornerStyle == CornerStyle.Rounded)
				g.drawRoundRect((int)topLeft.getX(), (int)topLeft.getY(), (int)size.getWidth(), (int)size.getHeight(), ROUND_CORNER_RADIUS, ROUND_CORNER_RADIUS);
 		}
 		if(this.cornerStyle == CornerStyle.Sharp)
 			g.draw(new Rectangle2D.Double(topLeft.getX(), topLeft.getY(), size.getWidth(), size.getHeight()));
 		else if(this.cornerStyle == CornerStyle.Rounded)
			g.draw(new RoundRectangle2D.Double(ROUND_CORNER_RADIUS, ROUND_CORNER_RADIUS, topLeft.getX(), topLeft.getY(), size.getWidth(), size.getHeight()));
 	}
 
 	@Override
 	public Point getPosition() {
 		return topLeft;
 	}
 
 	@Override
 	public Dimension getSize() {
 		return size;
 	}
 
 	@Override
 	public void setPosition(Point p) {
 		topLeft = p;
 	}
 
 	@Override
 	public void setSize(Dimension d) {
 		size = d;
 	}
 
 	@Override
 	public Boolean contains(Point p) {
 		int x = (int)p.getX();
 		int y = (int)p.getY();
 		int x1 = (int)topLeft.getX();
 		int y1 = (int)topLeft.getY();
 		int w = (int)size.getWidth();
 		int h = (int)size.getHeight();
 		if(x > x1 && y > y1 && x < x1 + w && y < y1 + h)
 			return true;
 		else
 			return false;
 	}
 	
 	public void setCornerStyle(CornerStyle cornerStyle)
 	{
 		System.out.println(cornerStyle);
 		this.cornerStyle = cornerStyle;
 	}
 	
 	public CornerStyle getCornerStyle()
 	{
 		return this.cornerStyle;
 	}
 
 	@Override
 	public DrawingPrimitive clone() {
 		return new Rectangle(this.topLeft, new Point((int)(topLeft.getX() + size.getWidth()), (int)(topLeft.getY() + size.getHeight())));
 	}
 
 }
