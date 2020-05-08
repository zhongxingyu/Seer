 package org.dawb.workbench.plotting.system.swtxy.util;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 
 /**
  * A Draw2D annular sector. Its location is the centre (not the top-left corner of its bounding box)
  */
 public class Sector extends Shape {
 	private Rectangle box; // bounding box of sector
 	private PrecisionPoint centre;
 	private double[] radius;
 	private double[] angle; // degrees
 
 	public Sector() {
 		this(0, 0, 100, 200, 0, 360);
 	}
 
 	/**
 	 * 
 	 * @param cx centre x
 	 * @param cy centre y
 	 * @param inner radius
 	 * @param outer radius
 	 * @param start angle in degrees (0 is horizontal and +ve is anti-clockwise)
 	 * @param end angle in degrees
 	 */
 	public Sector(double cx, double cy, double inner, double outer, double start, double end) {
 		centre = new PrecisionPoint(cx, cy);
 		radius = new double[] { inner, outer };
 		angle = new double[] { start, end };
 		calcBox();
 	}
 
 	@Override
 	public void setLocation(Point p) {
 		centre.setPreciseX(p.preciseX() - radius[1]);
 		centre.setPreciseY(p.preciseY() - radius[1]);
 		calcBox();
 	}
 
 	/**
 	 * Set centre
 	 * @param cx
 	 * @param cy
 	 */
 	public void setCentre(double cx, double cy) {
 		centre.setPreciseX(cx);
 		centre.setPreciseY(cy);
 		calcBox();
 	}
 
 	/**
 	 * Set radii
 	 * @param inner
 	 * @param outer
 	 */
 	public void setRadii(double inner, double outer) {
 		radius[0] = inner;
 		radius[1] = outer;
 		calcBox();
 	}
 
 	/**
 	 * Set angles
 	 * @param start
 	 * @param end
 	 */
 	public void setAnglesDegrees(double start, double end) {
 		angle[0] = start;
 		angle[1] = end;
 		calcBox();
 	}
 
 	public PrecisionPoint getCentre() {
 		return centre;
 	}
 
 	public double[] getRadii() {
 		return radius;
 	}
 
 	public double[] getAnglesDegrees() {
 		return angle;
 	}
 
 	private void calcBox() {
 		int l = (int) (2 * radius[1] +1);
 		box = new Rectangle((int) (centre.preciseX() - radius[1]), (int) (centre.preciseY() - radius[1]), l, l);
 		setBounds(box);
 	}
 
 	@Override
 	public boolean containsPoint(int x, int y) {
 		if (!super.containsPoint(x, y))
 			return false;
 		double px = x - centre.preciseX();
 		double py = centre.preciseY() - y;
 		double r = Math.hypot(px, py);
 		if (r < radius[0] || r > radius[1])
 			return false;
 		double a = Math.toDegrees(Math.atan2(py, px));
		if (a < 0)
			a += 360;
 
 		return a >= angle[0] && a <= angle[1];
 	}
 
 	@Override
 	protected void fillShape(Graphics graphics) {
 //		if (box != null)
 //			graphics.fillOval(box);
 	}
 
 	@Override
 	protected void outlineShape(Graphics graphics) {
 		final double ri = radius[0], ro = radius[1];
 		final int din = (int) (2*ri), dout = (int) (2*ro);
 		final double as = Math.toRadians(angle[0]), ae = Math.toRadians(angle[1]);
 		final double cx = centre.preciseX(), cy = centre.preciseY();
 
		graphics.drawArc((int) (cx - ri), (int) (cy - ri), din, din, (int) angle[0], (int) (angle[1]-angle[0]));
		graphics.drawArc((int) (cx - ro), (int) (cy - ro), dout, dout, (int) Math.round(angle[0]), (int) Math.floor(angle[1]-angle[0]));
 		graphics.drawLine((int) (cx + ri*Math.cos(as)), (int) (cy - ri*Math.sin(as)),
 				(int) (cx + ro*Math.cos(as)), (int) (cy - ro*Math.sin(as)));
 		graphics.drawLine((int) (cx + ri*Math.cos(ae)), (int) (cy - ri*Math.sin(ae)),
 				(int) (cx + ro*Math.cos(ae)), (int) (cy - ro*Math.sin(ae)));
 	}
 }
