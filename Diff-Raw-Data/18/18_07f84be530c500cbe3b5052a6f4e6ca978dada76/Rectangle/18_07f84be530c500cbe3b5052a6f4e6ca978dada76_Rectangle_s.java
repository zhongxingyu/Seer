 package caskman.polygonsim.model;
 
 
 public class Rectangle {
 	private int width;
 	private int height;
 	private int x;
 	private int y;
 	
 	public Rectangle(int x,int y, int width,int height) {
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 	}
 	
 	public Rectangle(float x,float y, float width,float height) {
 		this.x = (int)x;
 		this.y = (int)y;
 		this.width = (int)width;
 		this.height = (int)height;
 	}
 	
	public static boolean intersect(Rectangle a,Rectangle b) { // left is top, right is bottom
		boolean x = (a.left() <= b.right() && a.right() >= b.left()) || (a.left() >= b.left() && a.right() <= b.right()) || (a.left() <= b.right() && a.right() >= b.right());
		boolean y = (a.top() <= b.bottom() && a.bottom() >= b.top()) || (a.top() >= b.top() && a.bottom() <= b.bottom()) || (a.top() <= b.bottom() && a.bottom() >= b.bottom());
 		return x && y;
 	}
 	
 	public int top() {
 		return y;
 	}
 	
 	public int left() {
 		return x;
 	}
 	
 	public int bottom() {
 		return y + height;
 	}
 	
 	public int right() {
 		return x + width;
 	}
 	
 	public int centerX() {
 		return x + (width>>1);
 	}
 	
 	public int centerY() {
 		return y + (height>>1);
 	}
 	
 	public int width() {
 		return width;
 	}
 	
 	public int height() {
 		return height;
 	}
 	
 	public String toString() {
 		return "["+x+","+y+","+right()+","+bottom()+"]";
 	}
 }
