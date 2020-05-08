 package eu.MrSnowflake.android.gametemplate;
 
 public class Point {
 
 	private float x;
 	private float y;
 	
 	public Point(float x, float y)
 	{
 		this.x = x;
 		this.y=y;
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 	public String toString()
 	{
 		return "(" + this.x + "," + this.y + ")";
 	}
 	
	public void translate(Point p)
 	{
		this.x += p.getX();
		this.y += p.getY();
 	}
 }
