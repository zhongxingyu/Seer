 package balle.world;
 
 public class Line {
 	
	private final Coord a, b;
 	
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
 
	public Line rotate(Orientation orientation) {
		return new Line(a.rotate(orientation), b.rotate(orientation));
 	}
 
	public Line add(Coord position) {
		return new Line(a.add(position), b.add(position));
 	}
 
 }
