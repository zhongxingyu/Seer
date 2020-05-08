 package se.chalmers.tda367.std.core;
 
 import java.util.*;
 
 import se.chalmers.tda367.std.core.tiles.*;
 import se.chalmers.tda367.std.core.tiles.enemies.IEnemy;
 import se.chalmers.tda367.std.utilities.*;
 
 /**
  * Represents the whole game board in a grid system.
  * @author Johan Gustafsson
  * @date Mar 22, 2012
  */
 public class GameBoard {
 	
 	private IBoardTile[][] board;
 	private final int width;
 	private final int height;
 	
 	public GameBoard(){
 		Properties p = Properties.INSTANCE;
 		width = p.getDefaultBoardWidth();
 		height = p.getDefaultBoardHeight();
 		board = new IBoardTile[width][height];
 		IBoardTile tile = new TerrainTile(new Sprite());
 		initBoard(tile);
 		
 	}
 	
 	public GameBoard(int width, int height){
 		this.width = width;
 		this.height = height;
 		board =  new IBoardTile[this.width][this.height];
 		IBoardTile tile = new TerrainTile(new Sprite());
 		initBoard(tile);
 		
 	}
 	
 	/**
 	 * Returns a list of enemies that is inside the radius based on supplied position
 	 * @param p
 	 * @param radius
 	 * @return List of enemies.
 	 */
 	public List<IEnemy> getEnemiesInRadius(Position p, int radius){
 		List<IEnemy> enemies = new ArrayList<IEnemy>();
 		
 		for(int y = p.getY()-radius; y < p.getY()+radius; y++) {
 			for(int x = p.getX()-radius; x < p.getX()+radius; x++) {
 				if(posOnBoard(x, y) && getTileAt(x, y) instanceof IEnemy) {
 					enemies.add((IEnemy) getTileAt(x, y));
 				}
 			}
 		}
 		return enemies;
 	}
 
 	/**
 	 * Move the IBoardTile from the old position to the new position
 	 * @param oldP
 	 * @param newP
 	 */
 	public void moveTile(Position oldP, Position newP){
 		IBoardTile newPosTile = getTileAt(newP);
 		
 		placeTile(getTileAt(oldP), newP);
 		placeTile(newPosTile, oldP);
 	}
 	
 	/**
 	 * Place given tile on given position.
 	 * @param tile
 	 * @param p
 	 */
 	public void placeTile(IBoardTile tile, Position p){
 		if(posOnBoard(p)) {
 			board[p.getX()][p.getY()] = tile;
 		}
 	}
 	
 	/**
 	 * Returns the tile from given x and y values.
 	 * @param x
 	 * @param y
 	 * @return IBoardTile from given x and y values.
 	 */
 	public IBoardTile getTileAt(int x, int y) {
 		return board[x][y];
 	}
 	
 	/**
 	 * Returns the tile from given position.
 	 * @param p
 	 * @return IBoardTile from given position.
 	 */
 	public IBoardTile getTileAt(Position p){
 		return getTileAt(p.getX(), p.getY());
 	}
 	
 	/**
 	 * Check if a given x and y values is inside the boundaries of the board.
 	 * @param p
 	 * @return true if given x and y values are on the game board.
 	 */
 	private boolean posOnBoard(int x, int y){
 		if(x < 0 || y < 0) {
 			return false;
 		}
 		if(x >= board[0].length || y >= board.length) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Check if a given position is inside the boundaries of the board.
 	 * @param p
 	 * @return true if position is on the game board.
 	 */
 	private boolean posOnBoard(Position p){
 		return posOnBoard(p.getX(), p.getY());
 	}
 	
 	private void initBoard(IBoardTile tile){
 		for(int y = 0; y < height; y++){
 			for(int x = 0; x < width; x++) {
 				board[x][y] = tile;
 			}
 		}
 	}
 	
 	/**
 	 * Overrides toString
 	 * @return a string representation of the game board.
 	 */
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 		for (int y = 0; y < board.length; y++) {
 			for (int x = 0; x < board[y].length; x++) {
 				str.append('[');
 				str.append(board[x][y].toString());
 				str.append(']');
 			}
 			str.append("\n");
 		}
 		return str.toString();
 		
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 }
