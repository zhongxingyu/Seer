 package shapes;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Container;
 
 public abstract class Shape {
 	private int x1 = 10, y1 = 10, x2 = 20, y2 = 20;
 	private Color lineColor = Color.BLACK;
 	private boolean markedSelected;
 
 	/**
 	 * The empty constructor so a shape can be created without the user defining the size and line color
 	 * Uses the default values of the variables.
 	 */
 	public Shape() {
 
 	}
 
 	/**
 	 * The Shape constructor
 	 * @param x1 X coordinate of first point declared.
 	 * @param y1 Y coordinate of first point declared.
 	 * @param x2 X coordinate of second point declared.
 	 * @param y2 Y coordinate of second point declared.
 	 * @param lineColor The color of the shape's line.
 	 */
 	public Shape(int x1, int y1, int x2, int y2, Color lineColor) {
 		this.x1 = x1;
 		this.y1 = y1;
 		this.lineColor = lineColor;
 	}
 
 	/*
 	 * Requires all shapes to have a draw method.
 	 */
 	public abstract void draw(Graphics g);
 
 	/*
 	 * Determines whether the given x and y are within the rectangle created by the X1, Y1 and X2, Y2 coordinates.
 	 */
 	public boolean containsLocation(int x, int y) {
 		if (Math.min(getX1(), getX2()) <= x && Math.min(getY1(), getY2()) <= y 
 				&& Math.max(getX1(), getX2()) >= x && Math.max(getY1(), getY2()) >=y  ) {
 			return true;
 		}
 		return false;
 	}
 
 	public int getX1() {
 		return x1;
 	}
 
 	public int getY2() {
 		return y2;
 	}
 
 	public int getX2() {
 		return x2;
 	}
 
 	public int getY1() {
 		return y1;
 	}
 
 	public Color getLineColor() {
 		return lineColor;
 	}
 	
 	public boolean isMarkedSelected() {
 		return markedSelected;
 	}
 
 	public void setX1(int x1) {
 		this.x1 = x1;
 	}
 
 	public void setY1(int y1) {
 		this.y1 = y1;
 	}
 
 	public void setX2(int x2) {
 		this.x2 = x2;
 	}
 
 	public void setY2(int y2) {
 		this.y2 = y2;
 	}
 
 	public void setLineColor(Color lineColor) {
 		this.lineColor = lineColor;
 	}
 	
 	public void setMarkedSelected(boolean markedSelected) {
 		this.markedSelected = markedSelected;
 	}
 
 	public String toString() {
 		return "x1 = " + x1 + "y1 = " + y1;
 	}
 
 }
