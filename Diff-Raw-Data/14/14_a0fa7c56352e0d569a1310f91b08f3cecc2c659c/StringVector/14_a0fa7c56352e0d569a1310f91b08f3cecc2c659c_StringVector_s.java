 package analyticgeometry;
 
 import model.Player;
 import processing.core.PApplet;
 import processing.core.PVector;
 
 public class StringVector extends PApplet {
 	private PVector COM = new PVector(320, 240);
 	private PVector handLeft;
 	private PVector point1 = new PVector(600, 100);
 
 	private int numStrings = 5;
 	private Player p;
 
 	public void setup() {
 		size(640, 480);
 		frameRate(60);
 		smooth();
 		// translate(0, 0);
 
 		stroke(255, 0, 255);
 		strokeWeight(2);
 		// p = new Player(0, handLeft, handRight, centerOfMass)
 	}
 
 	public void draw() {
 		background(0);
 		translate(0, 0);
 
 		int size = 10;
 
 		pushMatrix();
 
 		// COM
 		ellipse(COM.x, COM.y, size, size);
 
 		// point1
 		point1 = new PVector(mouseX, mouseY);
 		ellipse(point1.x, point1.y, size, size);
 
 		// Ausgangsvektoren
 		PVector v1 = point1;
 		PVector v2 = COM;
 
 		// Richtungsvektor zu punkt 1 aka Linke Hand
 		PVector rv = new PVector(v1.x - v2.x, v1.y - v2.y);
 
 		// Point 2
 		PVector v4 = new PVector(COM.x - rv.x, COM.y - rv.y);
 		ellipse(v4.x, v4.y, size, size);
 
 		// Saite 0
 		line(COM.x, COM.y, Math.abs(rv.x + COM.x), Math.abs(rv.y + COM.y));
 		line(COM.x, COM.y, Math.abs(rv.x - COM.x), Math.abs(rv.y - COM.y));
 
 		// Orthogonaler Vektor
 		PVector ov = new PVector(rv.x, rv.y);
		ov.normalize();
		// ov.mult(100);
 		ov = new PVector(ov.y, -ov.x);
 
 		ellipse(ov.x + COM.x, ov.y + COM.y, size, size);
 		line(COM.x, COM.y, Math.abs(ov.x + COM.x), Math.abs(ov.y + COM.y));
 
 		// Zweite Saite
 		createStrings(COM, ov, rv);
 
 		popMatrix();
 	}
 
 	private void createStrings(PVector COM, PVector ov, PVector rv) {
 		int padding = 10;
 
		for (int i = 0; i < numStrings; i++) {

			ov.add(padding, padding, 0);
 			PVector newCOM = new PVector(ov.x + COM.x, ov.y + COM.y);
 
 			// Vertikale Linie
			line(COM.x, COM.y, Math.abs(ov.x + COM.x), Math.abs(ov.y + COM.y));
 
 			// Saite
 			line(newCOM.x, newCOM.y, Math.abs(rv.x + newCOM.x), Math.abs(rv.y + newCOM.y));
 			line(newCOM.x, newCOM.y, Math.abs(rv.x - newCOM.x), Math.abs(rv.y - newCOM.y));
 
 		}
 
 	}
 }
