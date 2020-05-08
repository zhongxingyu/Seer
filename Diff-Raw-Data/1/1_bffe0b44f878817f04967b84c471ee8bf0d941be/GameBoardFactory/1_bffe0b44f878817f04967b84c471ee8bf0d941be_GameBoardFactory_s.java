 package se.chalmers.tda367.std.core.factories;
 
 import java.io.File;
 import java.io.IOException;
 
 import se.chalmers.tda367.std.core.GameBoard;
 import se.chalmers.tda367.std.mapeditor.LevelMap;
import se.chalmers.tda367.std.mapeditor.MapLoader;
 import se.chalmers.tda367.std.utilities.IO;
 
 /**
  * A {@code GameBoardFactory} used to create different levels/maps of the game.
  * @author Emil Edholm
  * @date May 13, 2012
  */
 public class GameBoardFactory implements IFactory<GameBoard, Integer> {
 
 	/**
 	 * Create a new game board. 
 	 * Effectively a level of the game.
 	 * @param level - the level to create. Controls what map are loaded etc.
 	 */
 	@Override
 	public GameBoard create(Integer level) {
 		LevelMap map = null;
 		File f = new File("maps/level" + level + ".map");
 		
 		try {
 			map = IO.loadObject(LevelMap.class, f);
 		} catch (ClassNotFoundException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		return new GameBoard(map);
 	}
 
 }
