 package solutionEvaluator;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 
 public class MapReader {
 	
	private String filePath = "./sokoban/res/mapTestDeadlocksV2.slc";
 	private int mapNo;
 	private int mapCount = 0;
 	private boolean thisMap = false;
     private String map;
 	
 	public MapReader(int mapNo) throws IOException{
 		this.mapNo = mapNo;
 		
 		BufferedReader test = new BufferedReader(new FileReader(filePath));
         StringBuilder lStringBuilder = new StringBuilder();
 		while(true){
 			String line = test.readLine();
 			if(line.charAt(0) == ';') mapCount = mapCount + 1;
 			else if(mapCount == mapNo) lStringBuilder.append(line + "\n");
 			else if(mapCount > mapNo) break;
 		}
         // set map
         map = lStringBuilder.toString();
 	}
 
     public String getMap() {
         return map;
     }
 	
 	public static void main(String[] args) throws IOException {
 		MapReader MAP = new MapReader(1);
         System.out.println(MAP.getMap());
     } // main
 }
