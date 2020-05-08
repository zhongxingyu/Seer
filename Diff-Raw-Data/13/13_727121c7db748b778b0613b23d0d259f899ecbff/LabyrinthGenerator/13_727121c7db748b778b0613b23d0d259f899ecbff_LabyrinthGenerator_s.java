 package at.ac.foop.pacman.domain;
 
 import at.ac.foop.pacman.util.MapLoader;
 import at.ac.foop.pacman.util.PropertyLoader;
 import at.ac.foop.pacman.util.ServerSettings;
 
 /**
  *
  * @author Phil
  */
 public class LabyrinthGenerator {
 	
 	public static Labyrinth generateLabyrinth() {
 		return new Labyrinth(LabyrinthGenerator.generateSquares());
 	}
 	
 	public static Square[][] generateSquares() {
 		String[][] mapArrStr = MapLoader.getInstance().loadRandomMap();
 		if(mapArrStr == null) {
 			// TODO: ExceptionHandling
 			return null;
 		}
 		Square[][] map = new Square[mapArrStr.length][mapArrStr[0].length];
		
 		Integer fieldPoints = Integer.parseInt(PropertyLoader.getInstance().
																getProperty(ServerSettings.SERVER_CONFIG, 
																		ServerSettings.FIELD_POINTS, 
																		ServerSettings.DEFAULT_FIELD_POINTS)
												);
		
 		for (int i = 0; i < mapArrStr.length; i++) {
 			for(int j = 0; j < mapArrStr[i].length; j++) {
 				if(mapArrStr[i][j].equals("W")) {
 					// create wall field
 					map[i][j] = new Wall();
 				}
 				else if(mapArrStr[i][j].equals("E")) {
 					// create empty field
 					map[i][j] = new Field(fieldPoints);
 				}
 				else {
 					// default: empty field
 					map[i][j] = new Field(fieldPoints);
 				}
 			}
 		}
 		return map;
 	}
 }
