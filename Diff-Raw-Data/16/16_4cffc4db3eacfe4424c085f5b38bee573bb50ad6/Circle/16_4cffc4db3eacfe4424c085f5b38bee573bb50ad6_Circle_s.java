 package shapelauncher;
 
 import java.awt.Graphics;
 
 public class Circle extends Shape {
 	
 	public Circle(int x, int y, int radius) {
 		this.x = x;
 		this.y = y;
 		this.radius = radius;
 	}
 
 	public int radius;
 
 	@Override
 	public void draw(Graphics g) {
 		g.fillOval(this.x, this.y, radius, radius);
 		
 	}
 
 	@Override
 	public boolean contains(Missile m) {
		if(x-radius < m.getXPosition() && x+radius > m.getXPosition()
				&& y+radius > m.getYPosition() && y-radius < m.getYPosition())
 			return true;
 		return false;
 	}
 	
 }
