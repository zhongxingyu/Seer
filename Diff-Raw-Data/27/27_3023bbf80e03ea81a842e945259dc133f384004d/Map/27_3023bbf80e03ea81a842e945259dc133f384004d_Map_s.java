 package mybot;
 
 import java.util.TreeMap;
 
 import core.Tile;
 
 public class Map {
 
 	private TreeMap<Integer, MapTile> tiles = new TreeMap<Integer, MapTile>();
 	public static int cols, rows;
 
 	public Map() {
 		Map.cols = GameState.getCore().getCols();
 		Map.rows = GameState.getCore().getRows();
 	}
 
 	public MapTile getTile(int row, int col) {
 		col = (col >= Map.cols) ? 0 : (col < 0) ? Map.cols - 1 : col; // it's the
 																																	// right
 																																	// modulo
 		row = (row >= Map.rows) ? 0 : (row < 0) ? Map.rows - 1 : row;
 		MapTile tile = tiles.get(MapTile.CalculateHash(row, col));
 		if (tile == null)
 			tile = createTile(row, col);
 		return tile;
 	}
 
 	public MapTile createTile(int row, int col) {
 		MapTile mapTile = new MapTile(row, col);
 		tiles.put(MapTile.CalculateHash(row, col), mapTile);
 		return mapTile;
 	}
 
 	public void updateWholeMap() {
 		for (MapTile tile : tiles.values()) {
 			tile.updateState();
 		}
 	}
 
 	public static int getManhattanDistance(Tile t1, Tile t2) {
 		int rowDelta = Math.abs(t1.getRow() - t2.getRow());
 		int colDelta = Math.abs(t1.getCol() - t2.getCol());
 		rowDelta = Math.min(rowDelta, Map.rows - rowDelta);
 		colDelta = Math.min(colDelta, Map.cols - colDelta);
 		return rowDelta + colDelta;
 	}
 
 	@Override
 	public String toString() {
 
 		String string = "  ";
 		for (int col = 0; col < cols; col++)
 			string += (col % 10) + "";
 		string += "\n";
 		for (int row = 0; row < rows; row++) {
 			string += (row % 10) + " ";
 			for (int col = 0; col < cols; col++)
				string += getTile(row, col).getValueCode();
 			string += "\n";
 		}
 		return string;
 	}
 
 }
