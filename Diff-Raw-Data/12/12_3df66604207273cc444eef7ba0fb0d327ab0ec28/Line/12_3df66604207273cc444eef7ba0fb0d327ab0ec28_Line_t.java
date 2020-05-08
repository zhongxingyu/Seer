 /**
  * PAINt 
  * 
  * Created for the course intro Human-Computer Interaction at the
  * Radboud Universiteit Nijmegen
  * 
  * 2013
  */
 package nl.PAINt.shapes;
 
import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 
 import nl.PAINt.PanelMode;
 
 /**
  * @author Luuk Scholten & Thom Wiggers
  * 
  */
 public class Line implements Shape {
 
 	private Point p1;
 	private Point p2;
 	private boolean drawSelectionBox = false;
	private float lineWidth;
	private Color lineColor;
 
 	/**
 	 * 
 	 */
 	public Line(Point p1, Point p2) {
 		this.p1 = p1;
 		this.p2 = p2;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#draw(java.awt.Graphics2D)
 	 */
 	@Override
 	public void draw(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(lineWidth));
 		g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
 
 		if (drawSelectionBox) {
 			g2d.setColor(Color.BLUE);
 			g2d.drawRect(p1.x - 5, p1.y - 5, 10, 10);
 			g2d.drawRect(p2.x - 5, p2.y - 5, 10, 10);
 		}
 	}
 
 	public void setSecondPoint(Point p) {
 		p2 = p;
 
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#checkHit(java.awt.Point)
 	 */
 	@Override
 	public boolean checkHit(Point point) {
 
 		Polygon p = new Polygon();
 
 		int dx = 6;
 
 		double x = p2.x - p1.x;
 		double y = p2.y - p1.y;
 
 		double vx = 1, vy = 1;
 		if (Math.sqrt(x * x + y * y) > 0) {
 			vx = y / Math.sqrt(x * x + y * y);
 			vy = -x / Math.sqrt(x * x + y * y);
 		}
 
 		System.out.println("vx,vy = " + vx + ", " + vy);
 
 		p.addPoint((int) (p1.x + dx * vx), (int) (p1.y + dx * vy));
 		p.addPoint((int) (p1.x - dx * vx), (int) (p1.y - dx * vy));
 		p.addPoint((int) (p2.x - dx * vx), (int) (p2.y - dx * vy));
 		p.addPoint((int) (p2.x + dx * vx), (int) (p2.y + dx * vy));
 
 		return p.contains(point);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#setSelectionBox(boolean)
 	 */
 	@Override
 	public void setSelectionBox(boolean bool) {
 		this.drawSelectionBox = bool;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#move(double, double)
 	 */
 	@Override
 	public void move(double dx, double dy) {
 		p1.x += dx;
 		p2.x += dx;
 		p1.y += dy;
 		p2.y += dy;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#lockCorner(java.awt.Point)
 	 */
 	@Override
 	public boolean lockCorner(Point point) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#unlockCorner()
 	 */
 	@Override
 	public void unlockCorner() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#setColor(java.awt.Color)
 	 */
 	@Override
 	public void setColor(Color color) {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#setLineColor(java.awt.Color)
 	 */
 	@Override
 	public void setLineColor(Color color) {
		this.lineColor = color;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#setLineWidth(float)
 	 */
 	@Override
 	public void setLineWidth(float lineWidth) {
 
		this.lineWidth = lineWidth;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#setFilled(boolean)
 	 */
 	@Override
 	public void setFilled(boolean b) {
 		return;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#getContext()
 	 */
 	@Override
 	public PanelMode getContext() {
 		return PanelMode.LINE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#rotate(int)
 	 */
 	@Override
 	public void rotate(int i) {
 		return;
 
 	}
 
 	public boolean getFilled() {
 		return false;
 	}
 
 }
