 import java.awt.Color;
 
 /**
  * Class for storing a single point that is part of a line.
  * All values are final.
  * @author Lorenz Diener
  */
 public final class Point {
 	// Points color and size.
 	private final Color brushColor;
 	private final int brushSize;
 
 	// Point position
 	private final double posX;
 	private final double posY;
 
 	// Is this the last point of some line?
 	private final boolean endPoint;
 
 	/**
 	 * Constructor filling a point with given values.
 	 * @param x The points x position.
 	 * @param y The points y position.
 	 * @param brushColor The points color.
 	 * @param size The points size.
 	 * @param endPoint Is the point the last in some line?
 	 */
 	public Point( double x, double y, Color brushColor, int size,
 		boolean endPoint ) {
 		this.posX = x;
 		this.posY = y;
 		this.brushColor = brushColor;
 		this.brushSize = size;
 		this.endPoint = endPoint;
 	}
 
 	/**
 	 * Getter for the point color.
 	 * @return This points brush color.
 	 */
 	public Color getColor() {
 		return( this.brushColor );
 	}
 
 	/**
 	 * Getter for the point size.
 	 * @return This points brush size.
 	 */
 	public int getBrushSize() {
 		return( this.brushSize );
 	}
 
 	/**
 	 * Getter for the points x position.
 	 * @return This points brush color.
 	 */
 	public double getX() {
 		return( this.posX );
 	}
 
 	/**
 	 * Getter for the points y position.
 	 * @return This points brush color.
 	 */
 	public double getY() {
 		return( this.posY );
 	}
 
 	/**
 	 * Is this point last in some line?.
	 * @return True if last, false if not.
 	 */
 	public boolean isEndPoint() {
 		return( this.endPoint );
 	}
 }
