 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 
 public class Square {
 	
 	float x, y, vx, vy;
 	
 	public Square() {
 		x = 640 / 2;
 		y = 480 / 2;
 		vx = vy = 0;
 	}
 	
 	public void update(Graphics2D g, ArrayList<Rectangle> rects) {
 		g.setColor(Color.BLUE);
 		int xapprox = Math.round(x);
 		int yapprox = Math.round(y);
 		g.fillRect(xapprox - 5, yapprox - 5, 10, 10);
 		
 		y += vy;
 		x += vx;
 		
 		if (/*y + 5 >= 480 ||*/ y - 5 <= 0) {
 			vy = -vy;
			y = 5;
 		}
 		else {
 			vy += 0.1;
 		}
 		
		if(x + 5 >= 640) {
 			vx = -vx;
			x = 635;
		}
		else if(x - 5 <= 0) {
			vx = -vx;
			x = 5;
 		}
 		
 		for(Rectangle rect : rects) {
 			//x + 5 >= rect.x && rect.x - 5 <= rect.x + rect.width && y + 5 <= rect.y + rect.height && y - 5 >= rect.y - rect.height 
 			if(rect.contains(getCoords())) {
 				vy = -vy;
 			}
 		}
 	}
 	
 	public void accelerate(int ax, int ay) {
 		vx += ax;
 		vy += ay;
 	}
 	
 	public Point getCoords() {
 		return new Point(Math.round(x),Math.round(y));
 	}
 	
 	public void reset() {
 		x = 640 /2;
 		y = 480/2;
 		vx = vy = 0;
 	}
 }
