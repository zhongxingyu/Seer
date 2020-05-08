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
		int realX = m.getXPosition() +10;
		int realY = 450 - m.getYPosition() ;
		if(x-radius < realX && x+radius > realX
				&& y+radius > realY && y-radius < realY)
 			return true;
 		return false;
 	}
 	
 }
