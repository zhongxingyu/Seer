 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Collection;
 import java.util.List;
 
import jay.util.Deque;

 /**
  * A class that represents Constellations, with a list of star pairs.
  * 
  * @see Star
  * @author Jay
  * @version 1.0 (1-20-14)
  */
 public class Constellation implements JayACMCanvas.Paintable {
 	private static Color generateRandomColor() {
 		Color c = Color.getHSBColor((float) (Math.random() * 360), 1, .5f);
 		return new Color(c.getRed(), c.getGreen(), c.getBlue(), 64);
 	}
 
 	/*
 	 * OLD IMPLEMENTATION:
 	 * double rFactor = Math.random(), gFactor = Math.random(), bFactor = Math.random();
 	 * double scaleFactor = 400 / (rFactor + gFactor + bFactor);
 	 * color = new Color(Math.min((int) (scaleFactor * rFactor), 255), Math.min((int) (scaleFactor * gFactor), 255),
 	 * Math.min((int) (scaleFactor * bFactor), 255), 128);
 	 */
 	private Color color;
	private Collection<StarLine> starLines = new Deque<StarLine>();
 
 	/**
 	 * Creates a Constellation with the passed lines
 	 * 
 	 * @param lines
 	 *            - the pairs of stars
 	 * @param sky
 	 *            - the sky the stars are in
 	 */
 	public Constellation(List<String[]> lines, Sky sky) {
 		for (String[] namePairs : lines)
 			starLines.add(new StarLine(sky.findStar(namePairs[0]), sky.findStar(namePairs[1])));
 		color = generateRandomColor();
 	}
 
 	/**
 	 * Paints the star onto the passed graphics
 	 */
 	@Override
 	public void paint(Graphics2D g) {
 		g.setColor(color);
 		for (StarLine starLine : starLines)
 			g.draw(starLine);
 	}
 }
 
 /**
  * Represents a line between two stars
  * 
  * @author Jay
  * 
  */
 class StarLine extends Line2D {
 	private Star star1, star2;
 
 	/**
 	 * Creates a StarLine with the two passed stars
 	 * 
 	 * @param star1
 	 *            - The first Star
 	 * @param star2
 	 *            - The Second Star
 	 */
 	public StarLine(Star star1, Star star2) {
 		this.star1 = star1;
 		this.star2 = star2;
 	}
 
 	@Override
 	public Rectangle2D getBounds2D() {
 		return null;
 	}
 
 	@Override
 	public Point2D getP1() {
 		return new Point2D.Double(getX1(), getY1());
 	}
 
 	@Override
 	public Point2D getP2() {
 		return new Point2D.Double(getX2(), getY2());
 	}
 
 	@Override
 	public double getX1() {
 		return star1.getScreenX();
 	}
 
 	@Override
 	public double getX2() {
 		return star2.getScreenX();
 	}
 
 	@Override
 	public double getY1() {
 		return star1.getScreenY();
 	}
 
 	@Override
 	public double getY2() {
 		return star2.getScreenY();
 	}
 
 	@Override
 	public void setLine(double x1, double y1, double x2, double y2) {
 		throw new UnsupportedOperationException();
 	}
 }
