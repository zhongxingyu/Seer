 package battlechallenge.maps;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 import battlechallenge.Coordinate;
 import battlechallenge.structures.Base;
 import battlechallenge.structures.City;
 import battlechallenge.structures.Structure;
 
 public class MapImporter {
 
 	public static final String DEFAULT_MAP;
 	public static final String DEFAULT_MAP_ECLIPSE;
 	public static final String DEFAULT_MAP_WINDOWS;
 	public static final String DEFAULT_MAP_ECLIPSE_WINDOWS;
 	
 	static {
		DEFAULT_MAP = "../Maps/Map_01";
		DEFAULT_MAP_ECLIPSE = "Maps/Map_01";
		DEFAULT_MAP_WINDOWS = "..\\Maps\\Map_01";
		DEFAULT_MAP_ECLIPSE_WINDOWS = "Maps\\Map_01";
 	}
 	
 	public static BattleMap getMap() {
 		return getMap(DEFAULT_MAP);
 	}
 	
 	public static BattleMap getMap(String filename) {
 		return getMap(new File(filename));
 	}
 	
 	public static BattleMap getMap(File file) {
 		String name = file.getName();
 		int playerNum = 0; // Start at 0 increase every time map finds a base
 		try {
 			Scanner scanner = new Scanner(file);
 			String[] arr = scanner.nextLine().split(",");
 			int boardWidth = Integer.parseInt(arr[0]);
 			int boardHeight = Integer.parseInt(arr[1]);
 			if (arr.length >= 3)
 				name = arr[2];
 			List<Structure> structures = new LinkedList<Structure>();
 			List<Base> bases = new LinkedList<Base>();
 			for (int row=0;row<boardHeight;row++) {
 		      String line = scanner.nextLine();
 		      for (int col = 0; col < boardWidth; col++) { // Go through the column in the map
 					if (line.charAt(col) == ('C'))
 						structures.add(new City(new Coordinate(row, col)));
 					if (line.charAt(col) == ('B'))
 						bases.add(new Base(playerNum, new Coordinate(row, col))); 
 				}
 			}
 			return new BattleMap(name, boardWidth, boardHeight, structures, bases);
 		} catch (Exception e) {
 			System.err.println(e.getMessage()); 
 		}
 		// cannot import map
 		return null;
 	}
 	
 }
