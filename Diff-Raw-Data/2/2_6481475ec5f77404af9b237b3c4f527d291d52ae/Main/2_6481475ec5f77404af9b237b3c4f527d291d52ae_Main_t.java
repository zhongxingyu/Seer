 package se.chalmers.tda367.std;
 
 import java.util.Random;
 
 import se.chalmers.tda367.std.core.GameBoard;
 import se.chalmers.tda367.std.core.tiles.BuildableTile;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.IBuildableTile;
 import se.chalmers.tda367.std.core.tiles.PathTile;
 import se.chalmers.tda367.std.utilities.Position;
 import se.chalmers.tda367.std.utilities.Sprite;
 
 /**
  * Contains the main method. The entrance to the game.
  * @author Unchanged
  * @date Mar 22, 2012
  */
 public final class Main {
 
 	/**
 	 * The main method. Used to start the game.
 	 * @param args the command line arguments.
 	 */
 	public static void main(String[] args) {
 		GameBoard board = new GameBoard(20,20);
 		randomPlaceTile(board);
 		placePath(board);
 		System.out.println(board);
 
 	}
 
 	/**
 	 * @param board
 	 */
 	private static void placePath(GameBoard board) {
 		IBoardTile pathTile = new PathTile(new Sprite());
		int y = (board.getHeigth()/2)-1;
 		for (int i = 0; i < board.getWidth()-1; i++) {
 			board.placeTile(pathTile, new Position(i,y));
 			board.placeTile(pathTile, new Position(i,y+1));
 		}
 	}
 
 	/**
 	 * @param board
 	 */
 	private static void randomPlaceTile(GameBoard board) {
 		Random rnd = new Random();
 		IBoardTile buildTile = new BuildableTile(new Sprite());
 		for (int i = 0; i < 200; i++) {
 			int x = rnd.nextInt(board.getWidth()-1);
 			int y = rnd.nextInt(board.getHeight()-1);
 			board.placeTile(buildTile, new Position(x,y));
 		}
 	}
 	
 
 }
