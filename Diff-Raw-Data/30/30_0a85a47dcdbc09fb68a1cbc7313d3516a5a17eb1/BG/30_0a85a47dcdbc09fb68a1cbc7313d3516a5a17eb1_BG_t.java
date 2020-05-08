 import processing.core.*;
import java.util.*;
 
 public class BG {
 
	float x, y;
	int s;
 
 	static PApplet p;
 
 	BG() {
 		x = p.random(0, p.width);
 		y = p.random(0, p.height);
		s = (int) p.random(1, 5);
 	}
 
	public void move() {
 		x -= (float) s;
 		if (x < 0) {
 			x += p.width;
 			y = (int) p.random(0, p.height);
			s = (int) p.random(1, 5);
 		}
 	}
 
	public void draw() {
		p.noStroke();
		p.fill(150);
 		p.ellipse(x, y, s, s);
 	}
 
 }
