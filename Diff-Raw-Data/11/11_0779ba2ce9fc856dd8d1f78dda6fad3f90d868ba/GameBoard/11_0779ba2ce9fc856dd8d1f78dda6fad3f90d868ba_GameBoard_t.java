 package se.chalmers.dryleafsoftware.androidrally.model.gameBoard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Representing the map. Holds all tiles and lasers.
  * @author
  *
  */
 public class GameBoard {
 	private Tile[][] tiles = null;
 	private List<Laser> lasers;
 	private String[][] mapAsString;
 	/*
 	 * Max 8 players.
 	 */
 	private int[][] startingPosition = new int[8][2];
 	
 	public static final int EAST = 1;
 	public static final int WEST = 3;
 	public static final int SOUTH = 2;
 	public static final int NORTH = 0;
 
 	private static final int
 		TILE_HOLE = 1,
 		TILE_CHECKPOINT = 2,
 		TILE_CONVEYORBELT = 3,
 		TILE_GEARS = 4,
 		TILE_REPAIR = 5,
 		TILE_WALL = 6,
 		TILE_LASER = 7,
 		TILE_START = 8;
 	
 	/*
 	 * Hole = 1
 	 * CheckPoint = (nr)2
 	 * ConveyorBelt = (GameBoard.staticfinal)3
 	 * Gear = 4 -> left, 14 -> right
 	 * Repair = 5
 	 * Wall = (GameBoard.staticFinal)6
 	 * Laser = (GameBoard.staticFinal)7
 	 * 
 	 *  : between different elements on the same tile
 	 *  
 	 */
 	
 	/**
 	 * Creates a new gameBoard from a String[][].
 	 * 
 	 * Hole = 1
 	 * CheckPoint = (nr)2
 	 * ConveyorBelt = (nbrOfSteps)(GameBoard.staticfinal)3
 	 * Gear = 4 -> left, 14 -> right
 	 * Repair = 5
 	 * Wall = (GameBoard.staticFinal)6
 	 * Laser = (GameBoard.staticFinal)7
 	 * 
 	 *  : between different elements on the same tile
 	 *  
 	 *
 	 * @param map the String[][] which will be converted to a gameBoard.
 	 */
 	public GameBoard(String[][] map) {
 		createBoard(map);
 		mapAsString = map;
 		lasers = new ArrayList<Laser>();
 		
 	}
 	
 	/**
 	 * Returns a specific tile. (0, 0) being the tile in the topleft corner.
 	 * @param x the tile's position on the x-axis.
 	 * @param y the tile's position on the y-axis.
 	 * @return the specified tile.
 	 */
 	public Tile getTile(int x, int y){
 		return tiles[x][y];
 	}
 	
 	public int getWidth(){
 		return tiles.length;
 	}
 	
 	public int getHeight(){
 		return tiles[0].length;
 	}
 	
 	/**
 	 * Returns a matrix containing all startingPositions for the map.
 	 * [i][0] will provide the x value for starting position i and [i][1]
 	 * will provide the y value.
 	 * @return a matrix containing all startingPositions for the map.
 	 */
 	public int[][] getStartingPositions(){
 		return startingPosition;
 	}
 	
 	/**
 	 * Returns a list of all lasers on the gameBoard.
 	 * @return a list of all lasers on the gameBoard.
 	 */
 	public List<Laser> getLasers(){
 		return lasers;
 	}
 	
 	/**
 	 * Return a String[][] representation of the map.
 	 * 
 	 * Hole = 1
 	 * CheckPoint = (nr)2
 	 * ConveyorBelt = (GameBoard.staticfinal)3
 	 * Gear = 4 -> left, 14 -> right
 	 * Repair = 5
 	 * Wall = (GameBoard.staticFinal)6
 	 * Laser = (GameBoard.staticFinal)7
 	 * 
 	 *  : between different elements on the same tile
 	 *  
 	 * 
 	 * @return a String[][] representation of the map.
 	 */
 	public String[][] getMapAsString(){
 		return mapAsString;
 	}
 		
 	
 	/**
 	* Builds the board using the specified map data.
 	* @param map An array of integers mapping the map's layout.
 	*/
 	private void createBoard(String[][] map) {
 		tiles = new Tile[map.length][map[0].length];
		//Tiles need to created first or nullPointException will occur during wall creations
 		for(int x = 0; x < map.length; x++) {
			for(int y = 0; y < map[0].length; y++) {
 				tiles[x][y] = new Tile();
			}
		}
		
		for(int x = 0; x < map.length; x++) {
			for(int y = 0; y < map[0].length; y++) {	
 				// Add all the elements to the tile
 				if(!map[x][y].equals("")) {
 					for(String elementData : map[x][y].split(":")) {
 						int tileData = Integer.parseInt(elementData);
 						// Create the boardelement
 						int tile = tileData % 10;
 						if(tile == TILE_HOLE) {
 							tiles[x][y].addBoardElement(new Hole());
 						}else if(tile == TILE_GEARS) {
 							tiles[x][y].addBoardElement(new Gears(!(tileData / 10 == 0)));
 						}else if(tile == TILE_CONVEYORBELT) {
 							tiles[x][y].addBoardElement(new ConveyorBelt(tileData / 100, ((tileData / 10)%10)));
 						}else if(tile == TILE_CHECKPOINT) {
 							tiles[x][y].addBoardElement(new CheckPoint(tileData / 10));
 						}else if(tile == TILE_REPAIR) {
 							tiles[x][y].addBoardElement(new Wrench(1));
 						}else if(tile == TILE_WALL || tile == TILE_LASER) {
 							setWallOnTile(tileData / 10, x, y);
 						}else if(tile == TILE_LASER){
 							lasers.add(new Laser(x, y, tileData / 10));
 						}else if(tile == TILE_START){
 							startingPosition[tileData / 10 - 1][0] = x;
 							startingPosition[tileData / 10 - 1][1] = y;
 						}
 					} // loop - elements
 				} // if
 			} // loop - Y
 		} // loop - X
 	}
 	
 	private void setWallOnTile(int wall, int x, int y){
 		tiles[x][y].setWall(wall);
 		switch (wall){
 		case GameBoard.NORTH:
 			if(y>0){
 				tiles[x][y-1].setWall(GameBoard.SOUTH);
 			}
 			break;
 		case GameBoard.EAST:
 			if(x<getWidth()){
 				tiles[x+1][y].setWall(GameBoard.WEST);
 			}
 			break;
 		case GameBoard.SOUTH:
 			if(y<getHeight()){
 				tiles[x][y+1].setWall(GameBoard.NORTH);
 			}
 			break;
 		case GameBoard.WEST:
 			if(x>0){
 				tiles[x-1][y].setWall(GameBoard.EAST);	
 			}
 			break;
 		}
 	}
 }
