 package se.chalmers.tda367.std.utilities;
 
 import java.io.*;
 import java.util.*;
 
 import se.chalmers.tda367.std.core.Properties;
 import se.chalmers.tda367.std.core.tiles.*;
 
 /**
  * A class for loading maps
  * @author Emil Johansson
  * @date Apr 22, 2012
  */
 public class MapLoader {
 	private static IBoardTile[][] map;
 	private static File file;
 	private static ArrayList<Position> wayPointList = new ArrayList<Position>();
 	
 	public static IBoardTile[][] getMap(){
 		return map;
 	}
 	public static ArrayList<Position> getWayPointList(){
 		return wayPointList;
 	}
 	public static void setLevel(int level){
		wayPointList = new ArrayList<Position>();
 		file = new File("maps/level" + level + ".txt");
 		Scanner scanner;
 		try {
 			scanner = new Scanner(new FileReader(file));
 			while ( scanner.hasNextLine() ){
 		        processLine( scanner.nextLine() );
 			}
 			scanner.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	  }
 	
 	
 	private static void processLine(String line){ 
 	    Scanner scanner = new Scanner(line);
 	    scanner.useDelimiter(":");
 	    while ( scanner.hasNext() ){
 	    	String tileData[] = scanner.next().split(",");
 	    	String tileType = tileData[0];
 	    	int xCord = Integer.parseInt(tileData[1]);
 	    	int yCord = Integer.parseInt(tileData[2]); 
 	    	if(tileType.equals("S")){
 	    		map = new IBoardTile[xCord][yCord];
 	    		IBoardTile buildableTile = new BuildableTile();
 	    		for(int i = 0; i < map.length;i++){
 	    			for(int j = 0; j < map[i].length;j++){
 	    				map[i][j] = buildableTile;
 	    			}
 	    		}
 	    	} else if(tileType.equals("P")){
 	    		map[xCord][yCord] = new PathTile();
 	    	} else if(tileType.equals("W")){
 	    		map[xCord][yCord] = new PathTile();
 	    		wayPointList.add(new Position(xCord*Properties.INSTANCE.getTileScale() 
 	    				,yCord*Properties.INSTANCE.getTileScale()));
 	    	}
 	    } 
 	}
 	
 }
