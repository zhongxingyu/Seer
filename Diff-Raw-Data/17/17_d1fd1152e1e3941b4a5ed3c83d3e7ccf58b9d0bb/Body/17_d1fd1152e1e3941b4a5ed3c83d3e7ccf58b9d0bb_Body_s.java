 package structures;
 
 import game.DataReader;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.RadialGradientPaint;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 /**
  * A Body is the default class for an object that is representing a Planet.
  * @author Sean Lewis
  */
 public class Body extends CircularShape {
 
 	protected int mass;
 	private ArrayList<Moon> moons;
 	private boolean reflector = false;
 
 	protected int extraRadius;
 	protected Color[] colors;
 	protected float[] dist;
 
 	/**
 	 * Initializes a new empty Body.
 	 */
 	public Body() {
 	}
 
 	/**
 	 * Initializes a new Body at a center point and radius with a color. Since
 	 * no mass is specified, the radius is used as the mass.
 	 * @param centerX the center x coordinate
 	 * @param centerY the center y coordinate
 	 * @param radius the radius
 	 * @param color the color
 	 */
 	public Body(int centerX, int centerY, int radius, Color color) {
 		this(centerX, centerY, radius, color, radius);
 		// if no mass specified, the mass is set to the radius
 	}
 
 	/**
 	 * Initializes a new Body at a center point and radius with a color.
 	 * @param centerX the center x coordinate
 	 * @param centerY the center y coordinate
 	 * @param radius the radius
 	 * @param color the color
 	 * @param mass the mass
 	 */
 	public Body(int centerX, int centerY, int radius, Color color, int mass) {
 		setCenter(new Point2d(centerX, centerY));
 		setRadius(radius);
 		setColor(color);
 		this.mass = mass;
 		moons = new ArrayList<Moon>();
 
 	}
 
 	/**
 	 * 
 	 */
 	protected void computeColoring() {
 		dist = new float[] { 0.93f, 0.97f, 1.0f };
 		if (radius < 60) {
 			dist[0] = 0.88f;
 			dist[1] = 0.95f;
 			extraRadius = (int) (radius / 8.0 - 3.0);
 		} else if (radius > 150) {
 			dist[0] = 0.95f;
 			dist[1] = 0.98f;
 			extraRadius = (int) ((dist[2] - dist[1]) * (radius / 2.0));
 		}
 		colors = new Color[3];
 		colors[0] = color;
 		colors[1] = Color.WHITE;
 		colors[2] = Color.BLACK;
 	}
 
	/**
	 * @param dx
	 * @param dy
	 * @param g
	 */
	public void advancedDraw(double dx, double dy, Graphics2D g) {
 		if (colors == null || dist == null)
 			computeColoring();
 		if (radius < 10) {
 			draw((int) Math.round(dx), (int) Math.round(dy), g);
 		} else {
			g.setPaint(new RadialGradientPaint(new Point2D.Double(
 					center.x + dx, center.y + dy), radius + extraRadius, dist,
 					colors));
			g.fillOval((int) Math.round(center.x - radius + dx - extraRadius),
 					(int) Math.round(center.y - radius + dy - extraRadius),
 					diameter + 2 * extraRadius, diameter + 2 * extraRadius);
 		}
 
 	}
 
 	/**
 	 * Adds a moon to orbit this Body.
 	 * @param m a parameter
 	 */
 	public void addMoon(Moon m) {
 		moons.add(m);
 	}
 
 	/**
 	 * Sets the reflector property for this Body.
 	 * @param isReflector a parameter
 	 */
 	public void setReflector(boolean isReflector) {
 		reflector = isReflector;
 	}
 
 	/**
 	 * Return if this Body has the reflector property.
 	 * @return if this Body has the reflector property
 	 */
 	public boolean isReflector() {
 		return reflector;
 	}
 
 	/**
 	 * Returns the mass of the Body.
 	 * @return the mass
 	 */
 	public int getMass() {
 		return mass;
 	}
 
 	/**
 	 * Returns the List of Moons attached to this Body.
 	 * @return the List of Moons
 	 */
 	public ArrayList<Moon> getMoons() {
 		return moons;
 	}
 
 	/**
 	 * 
 	 */
 	public String toString() {
 		String str = "body(";
 		str += Math.round(center.x) + ", " + Math.round(center.y) + ", "
 				+ radius + ", " + DataReader.getColorDisplay(color) + ")";
 		for (Moon m : moons) {
 			str += "\n" + m.toString();
 		}
 		return str;
 	}
 
 }
