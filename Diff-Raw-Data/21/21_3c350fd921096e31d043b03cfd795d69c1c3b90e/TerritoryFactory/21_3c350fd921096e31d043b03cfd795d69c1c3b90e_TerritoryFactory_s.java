 package warborn.main;
 
import java.awt.Color;
 import java.io.*;
import java.util.StringTokenizer;

 import warborn.model.*;
import warborn.map.*;
 
 public class TerritoryFactory {
 
 	
 	public static Territory[] getTerritories(String mapName) throws IOException{
 		File file = new File("maps/" + mapName + ".txt");
 		LineNumberReader lnr = new LineNumberReader(new FileReader(file));
 		lnr.skip(file.length()-1);
 		int lines = lnr.getLineNumber();
 		
 		BufferedReader in = new BufferedReader(new FileReader(file));
 		Territory[] territories = new Territory[lines];
 		String[][] connectionsNameList = new String[lines][lines];
 		String lineToRead = in.readLine();
 		
 		for(int i = 0; lineToRead != null; i++){
 			String[] territoryNames = lineToRead.split(":"); 
 			territories[i] = new Territory(territoryNames[0], i);
 			
 			String[] connectionsName = new String[territoryNames[1].split(";").length+1];
 			String[] namesToInsert = territoryNames[1].split(";");
 			
 			connectionsName[0] = territoryNames[0];
 			for(int j = 1; j < connectionsName.length; j++){
 				connectionsName[j] = namesToInsert[j-1];
 			}
 			connectionsNameList[i] = connectionsName;
 			
 			lineToRead = in.readLine();
 		}
 		
 		for(int x = 0; x < connectionsNameList.length; x++){
 			for(int y = 1; y < connectionsNameList[x].length; y++){
 				if(connectionsNameList[x][y] == null){
 					break;
 				}
 				for(int z = 0; z < territories.length; z++){
 					if(connectionsNameList[x][y].equalsIgnoreCase(territories[z].getName())){
 						territories[x].addConnection(territories[z]);
 					}
 				}
 			}
 		}
 		
 		return territories;
 	}
 	
 }
