 import processing.core.*;
 
 public class FuType extends PApplet {
 	BG bgx[] = new BG[100];
 
 	public void setup() {
 		frameRate(30);
 		size(800, 600);
 		Ship s1 = new Ship(100, 100);
 		Ship.p = this;
 		// Weapon w[] = new Weapon[200];
 
 		for (int i = 0; i < bgx.length; i++) {
 			bgx[i].p = this;
			//bgx[i] = new Bagr();
			bgx[i].x = random(0, width);
			bgx[i].y = random(0, height);
			bgx[i].s = 1;
 		}
 
 	}
 
 	public void draw() {
 		background(0);
 		Ship.draw();
 
 		for (int i = 0; i < bgx.length; i++) {
 			bgx[i].draw();
 			bgx[i].move();
 		}
 		if (keyPressed) {
 			if (key == 's') {
 				Ship.moveDown();
 			} else if (key == 'w') {
 				Ship.moveUp();
 			} else if (key == 'a') {
 				Ship.moveLeft();
 			} else if (key == 'd') {
 				Ship.moveRight();
 			}
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		PApplet.main(new String[] { "--bgcolor=#ECE9D8", "Fu-Type" });
 	}
 
 }
