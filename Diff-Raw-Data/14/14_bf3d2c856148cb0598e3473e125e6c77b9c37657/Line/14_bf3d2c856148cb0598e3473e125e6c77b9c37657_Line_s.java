 package balle.world;
 
 public class Line {
 	
	Coord a, b;
 	
 	public Coord getA() {
 		return a;
 	}
 	
 	public Coord getB() {
 		return b;
 	}
 	
 	public Line(Coord a, Coord b) {
 		this.a = a;
 		this.b = b;
 	}
 
	public void rotate(Orientation orientation) {
		a.rotate(orientation);
		b.rotate(orientation);
 	}
 
	public void add(Coord position) {
		a.add(position);
		b.add(position);
 	}
 
 }
