 package pylos.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Represent an (immutable) position on the Board.
  * x and y are horizontal coordinates, z is the level in the pyramid.
  */
 public class Position {
 	public static final Position[][][] positions = new Position[Model.LEVELS][][];
 	public static final Position[] all = new Position[Model.BALLS];
 	public static Position top;
 	public final int x, y, z;
 
 	private Position(int x, int y, int z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 
 	@Override
 	public String toString() {
 		return "(" + x + ", " + y + ", " + z + ")";
 	}
 
 	public static void createPositions() {
 		int all_index = 0;
 		Position pos;
 		for (int level = 0; level < Model.LEVELS; level++) {
 			positions[level] = new Position[Model.LEVELS - level][Model.LEVELS - level];
 			for (int y = 0; y < Model.LEVELS - level; y++) {
 				for (int x = 0; x < Model.LEVELS - level; x++) {
 					pos = new Position(x, y, level);
 					positions[level][y][x] = pos;
 					all[all_index++] = pos;
 				}
 			}
 		}
 		top = Position.at(0, 0, Model.LEVELS - 1);
 	}
 
 	public static Position at(int x, int y, int z) {
 		return positions[z][y][x];
 	}
 
 	public static boolean isValid(int x, int y, int z) {
 		return x >= 0 && y >= 0 && z >= 0 && x < Model.LEVELS - z && y < Model.LEVELS - z && z < Model.LEVELS;
 	}
 
 	public List<Position> square() {
 		List<Position> square = new LinkedList<Position>();
 
 		for (int x = this.x; x <= this.x + 1; x++) {
 			for (int y = this.y; y <= this.y + 1; y++) {
 				if (!isValid(x, y, z))
 					return null;
 				square.add(Position.at(x, y, z));
 			}
 		}
 		return square;
 	}
 
 	public List<List<Position>> fourSquare() {
 		List<List<Position>> fourSquare = new LinkedList<List<Position>>();
 		List<Position> square;
 		for (int x = this.x - 1; x <= this.x; x++) {
 			for (int y = this.y - 1; y <= this.y; y++) {
 				if (isValid(x, y, z)) {
 					square = at(x, y, z).square();
 					if (square != null)
 						fourSquare.add(square);
 				}
 			}
 		}
 		return fourSquare;
 	}
 
 	public List<List<Position>> lines() {
 		List<List<Position>> lines = new LinkedList<List<Position>>();
 		List<Position> line;
 
 		line = new LinkedList<Position>();
 		for (int x = 0; x < Model.LEVELS - z; x++) {
 			line.add(at(x, y, z));
 		}
 		lines.add(line);
 
 		line = new LinkedList<Position>();
 		for (int y = 0; y < Model.LEVELS - z; y++) {
 			line.add(at(x, y, z));
 		}
 		lines.add(line);
 
 		if (onFirstDiagonal()) {
 			line = new LinkedList<Position>();
 			for (int xy = 0; xy < Model.LEVELS - z; xy++) {
 				if (isValid(xy, xy, z))
 					line.add(at(xy, xy, z));
 			}
 			lines.add(line);
 		}
 
 		if (onSecondDiagonal()) {
 			line = new LinkedList<Position>();
 			for (int xy = 0; xy < Model.LEVELS - 1 - z; xy++) {
 				if (isValid(xy, Model.LEVELS - 1 - z - xy, z))
 					line.add(at(xy, Model.LEVELS - 1 - z - xy, z));
 			}
 			lines.add(line);
 		}
 
 		return lines;
 	}
 
 	private boolean onSecondDiagonal() {
		return x + y == Model.LEVELS - 1;
 	}
 
 	private boolean onFirstDiagonal() {
 		return x == y;
 	}
 
 	// No equals(), Positions are unique and can be compared by identity
 }
