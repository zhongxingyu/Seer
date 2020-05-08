 package gameLogic;
 
import java.util.*;

 public class MapCreator {
 
 	public Map createMap(int width, int height, int difficulty) {
 		
 		// whatever the difficulty is
 		Field[][] fields = new Field[height][width];
 		
 		for (int r=0; r<height; ++r)
 			for (int c=0; c<width; ++ c)
 				fields[r][c] = new Field();
 		
 		Map map = new Map(width, height, fields[0][0]);
 		
 		// connect fields
 		for (int r=0; r<height; ++r) {
 			for (int c=0; c<width; ++c) {
 				
 				if (r-1 >= 0 && c-1 >= 0)
 					fields[r][c].addNeighbour(fields[r-1][c-1], 0); // (-1,-1)
 				
 				if (c-1 >= 0)
 					fields[r][c].addNeighbour(fields[r][c-1], 1); // (0,-1)
 					
 				if (r+1 < height)
 					fields[r][c].addNeighbour(fields[r+1][c], 2); // (1,0)
 				
 				if (c+1 < width)
 					fields[r][c].addNeighbour(fields[r][c+1], 3); // (0,1)
 				
 				if (r+1 < height && c-1 >= 0)
 					fields[r][c].addNeighbour(fields[r+1][c-1], 4); // (1,-1)
 				
 				if (r-1 >= 0)
 					fields[r][c].addNeighbour(fields[r-1][c], 5); // (-1,0)
 				
 				map.addField(fields[r][c]);
 			}	
 		}
 		
 		return map;
 	}
 
 }
