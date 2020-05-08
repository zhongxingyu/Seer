 package model.geometrical;
 
 import java.awt.geom.Line2D;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A line which will be drawn between its last position to its new position.
  * See <code>setPosition()</code> for more information about how this line
  * behaves.
  * 
  * @author 
  *
  */
 public class ScanLine implements CollisionBox {
 
 	private Line2D line;
 	private List<Line2D> lineList;
	private Position pos;
 	
 	/**
 	 * Creates a new ScanLine at the specified position.
 	 * @param x the X coordinate.
 	 * @param y the Y coordinate.
 	 */
 	public ScanLine(float x, float y) {
		this.pos = new Position(x, y);
 		this.line = new Line2D.Float(x, y, x, y);
 		this.lineList = new ArrayList<Line2D>();
 		lineList.add(line);
 	}
 	
 	@Override
 	public float getWidth() {
 		return (float)Math.max(line.getX1(), line.getX2()) - this.getPosition().getX();
 	}
 
 	@Override
 	public float getHeight() {
 		return (float)Math.max(line.getY1(), line.getY2()) - this.getPosition().getY();
 	}
 
 	@Override
 	public Position getPosition() {
		return pos;
 	}
 
 	/**
 	 * Sets the new position of the line. <br>
 	 * Example: <br>
 	 * If the line is between position <code>p1</code> and <code>p2</code> and then <code>setPosition(p3)</code> 
 	 * is called, the line will be drawn between <code>p2</code> and <code>p3</code>.
 	 */
 	@Override
 	public void setPosition(Position pos) {
 		this.line.setLine(line.getX2(), line.getY2(), pos.getX(), pos.getY());
		this.pos = pos;
 	}
 
 	/**
 	 * Moves the whole line the specified amount.
 	 * Both the start and end position will be moved this amount.
 	 */
 	@Override
 	public void move(float dx, float dy) {
 		this.line.setLine(line.getX1() + dx, line.getY1() + dy, line.getX2() + dx, line.getY2() + dy);
 	}
 
 	@Override
 	public boolean intersects(CollisionBox box) {
 		if(box != null) {
 			for(Line2D l : box.getLines()) {
 				if(l.intersectsLine(line)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public List<Line2D> getLines() {
 		return lineList;
 	}
 
 	/**
 	 * NOTE: A ScanLine can only scan forward.
 	 */
 	@Override
 	public boolean moveBack() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return this.getClass().getName() + "[x1=" + this.line.getX1() + ",y1=" + this.line.getY1() +
 				",x2=" + this.line.getX2() + ",y2=" + this.line.getY2() + "]";
 	}
 	
 }
