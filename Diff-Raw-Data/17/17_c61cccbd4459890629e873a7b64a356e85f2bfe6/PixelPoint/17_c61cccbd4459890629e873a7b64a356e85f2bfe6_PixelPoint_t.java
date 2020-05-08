 package Generator;
 
 /**
  * Represents a point in an image
  * @author yonatan
  *
  */
 public class PixelPoint{
 	private int x;
 	private int y;
 
 	public int getY() {
 		return this.y;
 	}
 	
 	public int getX() {
 		return this.x;
 	}
 	
 	public PixelPoint(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	public double distanceFrom(PixelPoint p) {
 		return Math.sqrt(Math.pow(this.x-p.getX(),2) + Math.pow(this.y-p.getY(),2));
 	}
 	
 	@Override
 	public boolean equals(Object p)
 	{
 		if (p == this)
 			return true;
 		if (!(p instanceof PixelPoint))
 				return false;
 		PixelPoint castedPixelPoint = (PixelPoint) p;
 		if ((this.x == castedPixelPoint.getX()) && (this.y == castedPixelPoint.getY()))
 			return true;
 		return false;
 	}
 
 }
