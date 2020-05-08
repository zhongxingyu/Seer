 package agent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import board.Board;
 import board.Cell;
 
 public class Neighborhood {
 
 	public enum Direction {
 		TOP, TOPRIGHT, RIGHT, BOTTOMRIGHT, BOTTOM, BOTTOMLEFT, LEFT, TOPLEFT;
 
 		/**
 		 * Potrzebne miedzy innymi do badania zagrozenia.
 		 * 
 		 * @param dir
 		 *            zadany kierunek
 		 * @return kierunek przeciwny do podanego
 		 */
 		public static Direction getOppositeDir(Direction dir) {
 			Direction opposite = null;
 			switch (dir) {
 			case TOP:
 				opposite = BOTTOM;
 				break;
 			case TOPRIGHT:
 				opposite = BOTTOMLEFT;
 				break;
 			case RIGHT:
 				opposite = LEFT;
 				break;
 			case BOTTOMRIGHT:
 				opposite = TOPLEFT;
 				break;
 			case BOTTOM:
 				opposite = TOP;
 				break;
 			case BOTTOMLEFT:
 				opposite = TOPRIGHT;
 				break;
 			case LEFT:
 				opposite = RIGHT;
 				break;
 			case TOPLEFT:
 				opposite = BOTTOMRIGHT;
 				break;
 			}
 			return opposite;
 		}
 	}
 
 	/**
 	 * 9 to Agent, 0 to komrka nie brana pod uwag
 	 * 
 	 * Tylko na razie tak "na pa"... ;]
 	 */
 	private static final Direction[] translateMask = { null, Direction.TOP,
 			Direction.TOPRIGHT, Direction.RIGHT, Direction.BOTTOMRIGHT,
 			Direction.BOTTOM, Direction.BOTTOMLEFT, Direction.LEFT,
 			Direction.TOPLEFT, null };
 	private static final int[][] mask = {
 			{ 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
 			{ 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0 },
 			{ 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
 			{ 0, 0, 8, 8, 1, 1, 1, 1, 1, 2, 2, 0, 0 },
 			{ 0, 8, 8, 8, 8, 1, 1, 1, 2, 2, 2, 2, 0 },
 			{ 0, 7, 8, 8, 8, 1, 1, 1, 2, 2, 2, 3, 0 },
 			{ 0, 7, 7, 7, 8, 8, 1, 2, 2, 3, 3, 3, 0 },
 			{ 7, 7, 7, 7, 7, 7, 9, 3, 3, 3, 3, 3, 3 },
 			{ 0, 0, 7, 7, 6, 6, 5, 4, 4, 3, 3, 0, 0 },
 			{ 0, 0, 0, 6, 6, 5, 5, 5, 4, 4, 0, 0, 0 },
 			{ 0, 0, 0, 0, 6, 5, 5, 5, 4, 0, 0, 0, 0 } };
 
 	private static int cell9X = -1, cell9Y = -1;
 
 	private Agent.Orientation orientation;
 
 	private Cell firstCell;
 	private List<Cell> cells;
 	private int x, y;
 
 	private float temperature;
 
 	public Neighborhood(Board board, Agent agent, Direction direction) {
 		findCell9();
 
 		orientation = agent.getOrientation();
 		x = agent.getPosition().getX();
 		y = agent.getPosition().getY();
 
 		findAllCellsOfDirection(board, direction);
 
 		computeAverages();
 	}
 
 	private void computeAverages() {
 		float temperature = 0.0f;
 		float minDist = Float.POSITIVE_INFINITY;
 
 		for (Cell cell : cells) {
 			// temp
 			temperature += cell.getTemperature();
 
 			// which cell is first?
 			float dist = (float) Math.sqrt(Math.pow(cell.getX() - x, 2)
 					+ Math.pow(cell.getY() - y, 2));
 			if (dist < minDist) {
 				minDist = dist;
 				firstCell = cell;
 			}
 		}
 
 		this.temperature = temperature / cells.size();
 	}
 
 	private void findAllCellsOfDirection(Board board, Direction direction) {
 		cells = new ArrayList<Cell>();
 		for (int mx = 0; mx < mask.length; mx++)
 			for (int my = 0; my < mask[mx].length; my++)
 				if (translateMask[mask[mx][my]] == direction) {
 					Cell tmp = board.getCellAt(
 							x + tx(mx - cell9X, my - cell9Y),
 							y + ty(mx - cell9X, my - cell9Y));
 					if (tmp != null)
 						cells.add(tmp);
 				}
 	}
 
 	/**
 	 * Obracanie wzgldnych wsprzdnych...
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	private int tx(int x, int y) {
 		switch (orientation) {
		default:
			return x;
 		case SOUTH:
 			return -x;
 		case EAST:
 			return -y;
 		case WEST:
 			return y;
 		}
 	}
 
 	private int ty(int x, int y) {
 		switch (orientation) {
		default:
			return y;
 		case SOUTH:
 			return -y;
 		case EAST:
 			return x;
 		case WEST:
 			return -x;
 		}
 	}
 
 	private void findCell9() {
 		if (cell9X != -1 && cell9Y != -1)
 			return;
 		for (int x = 0; x < mask.length; x++)
 			for (int y = 0; y < mask[x].length; y++)
 				if (mask[x][y] == 9) {
 					cell9X = x;
 					cell9Y = y;
 					break;
 				}
 	}
 
 	public float getTemperature() {
 		return temperature;
 	}
 
 	public Cell getFirstCell() {
 		return firstCell;
 	}
 }
