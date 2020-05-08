 package rsmith;
 
 import java.awt.geom.Point2D;
 
 public class CircleEvent implements SweepEvent {
 
 	private Point2D eventPoint;
 	private double eventY;
 	private Point2D center;
 	private double radius;
 	private Breakpoint leftBP;
 	private Breakpoint rightBP;
 	private Site pi;
 	private Site pj;
 	private Site pk;
 	
	public CircleEvent(Point2D center, double radius, Site pi, Site pj, Site pk) {
 		this.center = center;
 		this.pi = pi;
 		this.pj = pj;
 		this.pk = pk;
		this.radius = radius;
 		this.eventY = center.getY() - radius;
 		this.eventPoint = new Point2D.Double(center.getX(), eventY);
 	}
 	
 	@Override
 	public double getY() {
 		return eventY;
 	}
 
 	public static Point2D computeCircle(Point2D pi, Point2D pj, Point2D pk) {
 		Point2D result = null;
 		if(pi.getX() <= pj.getX() && pj.getX() <= pk.getX()) {
 			Line l1 = Line.bisector(pi, pj);
 			Line l2 = Line.bisector(pj, pk);
 			result = l1.intersect(l2);
 		}
 		return result;
 	}
 	
 	public static CircleEvent getCircleEvent(double sweep, Site pi, Site pj, Site pk) {
 		CircleEvent result = null;
 		Point2D center = computeCircle(pi.getPosition(),pj.getPosition(),pk.getPosition());
 		if(center != null) {
			double radius = center.distance(pi.getPosition());
			if(center.getY()-radius <= sweep) {
				result = new CircleEvent(center, radius, pi,pj,pk);
 			}
 		}
 		return result;		
 	}
 	
 	/**
 	 * @return the pi
 	 */
 	public Site getPi() {
 		return pi;
 	}
 
 	/**
 	 * @param pi the pi to set
 	 */
 	public void setPi(Site pi) {
 		this.pi = pi;
 	}
 
 	/**
 	 * @return the pj
 	 */
 	public Site getPj() {
 		return pj;
 	}
 
 	/**
 	 * @param pj the pj to set
 	 */
 	public void setPj(Site pj) {
 		this.pj = pj;
 	}
 
 	/**
 	 * @return the pk
 	 */
 	public Site getPk() {
 		return pk;
 	}
 
 	/**
 	 * @param pk the pk to set
 	 */
 	public void setPk(Site pk) {
 		this.pk = pk;
 	}
 
 	/**
 	 * @return the leftBP
 	 */
 	public Breakpoint getLeftBP() {
 		return leftBP;
 	}
 
 	/**
 	 * @param leftBP the leftBP to set
 	 */
 	public void setLeftBP(Breakpoint leftBP) {
 		this.leftBP = leftBP;
 	}
 
 	/**
 	 * @return the rightBP
 	 */
 	public Breakpoint getRightBP() {
 		return rightBP;
 	}
 
 	/**
 	 * @param rightBP the rightBP to set
 	 */
 	public void setRightBP(Breakpoint rightBP) {
 		this.rightBP = rightBP;
 	}
 
 	/**
 	 * @return the eventPoint
 	 */
 	public Point2D getEventPoint() {
 		return eventPoint;
 	}
 
 	/**
 	 * @param eventPoint the eventPoint to set
 	 */
 	public void setEventPoint(Point2D eventPoint) {
 		this.eventPoint = eventPoint;
 	}
 
 	/**
 	 * @return the center
 	 */
 	public Point2D getCenter() {
 		return center;
 	}
 
 	/**
 	 * @param center the center to set
 	 */
 	public void setCenter(Point2D center) {
 		this.center = center;
 	}
 
	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

 }
