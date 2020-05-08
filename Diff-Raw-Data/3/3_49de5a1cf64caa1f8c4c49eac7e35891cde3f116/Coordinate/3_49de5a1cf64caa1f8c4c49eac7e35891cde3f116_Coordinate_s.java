 package ui;
 
 public class Coordinate {
 	public int x;
 	public int y;
 
 	public Coordinate(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	@Override
 	public boolean equals(Object a) {
 		boolean t = false;
		if ((((Coordinate) a).x == x) && (((Coordinate) a).y == y))
 			t = true;
 		return t;
 	}
 }
