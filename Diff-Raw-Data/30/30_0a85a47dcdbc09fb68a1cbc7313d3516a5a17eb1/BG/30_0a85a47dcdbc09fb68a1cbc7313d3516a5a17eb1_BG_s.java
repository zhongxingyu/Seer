 import processing.core.*;
 
 public class BG {
 
	static float x, y;
	static int s;
 
 	static PApplet p;
 
 	BG() {
 		x = p.random(0, p.width);
 		y = p.random(0, p.height);
		s = (int) p.random(1, 4);
 	}
 
	public static void move() {
 		x -= (float) s;
 		if (x < 0) {
 			x += p.width;
 			y = (int) p.random(0, p.height);
			s = (int) p.random(1, 4);
 		}
 	}
 
	public static void draw() {
 		p.ellipse(x, y, s, s);
 	}
 
 }
