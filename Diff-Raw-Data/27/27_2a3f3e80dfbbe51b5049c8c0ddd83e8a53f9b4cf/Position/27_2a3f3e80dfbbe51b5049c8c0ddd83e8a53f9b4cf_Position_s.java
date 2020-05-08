 package rover;
 
 public class Position {
 
 	public final int x;
 	public final int y;
 
 	public Position(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public Position translate(Position p) {
 		return new Position(x + p.x, y + p.y);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (this == obj)
 			return true;
 		if (!obj.getClass().equals(getClass()))
 			return false;
 
 		Position other = (Position) obj;
 		return other.x == x && other.y == y;
 	}
 
	public Position negative() {
		return new Position(-x, -y);
	}

 }
