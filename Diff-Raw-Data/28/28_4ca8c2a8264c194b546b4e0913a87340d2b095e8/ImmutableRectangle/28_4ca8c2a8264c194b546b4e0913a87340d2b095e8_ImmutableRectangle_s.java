 package pleocmd;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Line2D;
 
 public final class ImmutableRectangle {
 
 	private final Rectangle r;
 
 	public ImmutableRectangle(final Rectangle r) {
 		this.r = r;
 	}
 
 	public int getX() {
 		return r.x;
 	}
 
 	public int getY() {
 		return r.y;
 	}
 
 	public int getWidth() {
 		return r.width;
 	}
 
 	public int getHeight() {
 		return r.height;
 	}
 
 	public Rectangle createCopy() {
 		return new Rectangle(r);
 	}
 
	public boolean equalsRect(final Rectangle r) {
		return this.r.equals(r);
 	}
 
 	public boolean contains(final Point p) {
 		return r.contains(p);
 	}
 
 	public boolean intersects(final Rectangle other) {
 		return r.intersects(other);
 	}
 
 	public boolean intersectsLine(final Line2D line) {
 		return r.intersectsLine(line);
 	}
 
 	public Rectangle intersection(final Rectangle other) {
 		return r.intersection(other);
 	}
 
 	public Rectangle union(final Rectangle other) {
 		return r.union(other);
 	}
 
 	public void asClip(final Graphics g) {
 		g.clipRect(r.x, r.y, r.width, r.height);
 	}
 
 	public void draw(final Graphics g) {
 		g.drawRect(r.x, r.y, r.width, r.height);
 	}
 
 	public void fill(final Graphics g) {
 		g.fillRect(r.x, r.y, r.width, r.height);
 	}
 
 }
