 package de.fhh.pr2.common.u.a2;
 
 import java.awt.Graphics;

 
 import de.fhh.pr2.common.u.a1.Point;
 
 public class Line {
 	private Point p1;
 	private Point p2;
 	private int thickness;
 	
 	public Line(Point p1, Point p2) {
 		this.setP1(p1);
 		this.setP2(p2);
 		this.setThickness(1);
 	}
 	
 	public Line(Point p1, Point p2, int thickness) {
 		this.setP1(p1);
 		this.setP2(p2);
 		this.setThickness(thickness);
 	}
 	
 	public Point getP1() {
 		return new Point(this.p1);
 	}
 	
 	public Point getP2() {
 		return new Point(this.p2);
 	}
 	
 	public int getThickness() {
 		return this.thickness;
 	}
 	
 	protected void setP1(Point p) {
 		this.p1 = new Point(p);
 	}
 	
 	protected void setP2(Point p) {
 		this.p2 = new Point(p);
 	}
 	
 	protected void setThickness(int thickness) {
 		this.thickness = thickness;
 	}
 	
 	public double length() {
 		return this.p1.distance(this.p2);
 	}
 	
 	public void draw(Graphics pen) {
 		if (this.thickness == 1) {
 			pen.drawLine((int) this.p1.getX(), (int) this.p1.getY(),
 					(int) this.p2.getX(), (int) this.p2.getY());
 		} else {
 			this.drawWithThickness(pen);
 		}
 	}
 	
 	public void draw(Graphics pen, int thickness) {
 		this.setThickness(thickness);
 		this.draw(pen);
 	}
 	
 	private void drawWithThickness(Graphics pen) {
 		// compute normal vector
 		Point direction = new Point(this.getP2().getX()-this.getP1().getX(),
 									this.getP2().getY()-this.getP1().getY());
 		Point normal = new Point(-direction.getY(), direction.getX());
 		double length = normal.length();
 		normal.setX((normal.getX()/length)*(thickness/2.0));
 		normal.setY((normal.getY()/length)*(thickness/2.0));
 
		pen.fillOval((int) Math.round(this.getP1().getX()-thickness/2), (int) Math.round(this.getP1().getY()-thickness/2), 
 					 thickness, thickness);
		pen.fillOval((int) Math.round(this.getP2().getX()-thickness/2), (int) Math.round(this.getP2().getY()-thickness/2), 
 				     thickness, thickness);
 
 		Point a = new Point(this.getP1());
 		a.add(normal);
 		Point b = new Point(this.getP2());
 		b.add(normal);
 		Point c = new Point(this.getP2());
 		c.sub(normal);
 		Point d = new Point(this.getP1());
 		d.sub(normal);
 		int[] xes = {(int) Math.round(a.getX()), (int) Math.round(b.getX()), (int) Math.round(c.getX()), (int) Math.round(d.getX())};
 		int[] yes = {(int) Math.round(a.getY()), (int) Math.round(b.getY()), (int) Math.round(c.getY()), (int) Math.round(d.getY())};
 		pen.fillPolygon(xes, yes, 4);
 	}
 	
 	public String toString() {
 		return "["+this.p1+", "+this.p2+", thickness="+this.thickness+"]";
 	}
 }
