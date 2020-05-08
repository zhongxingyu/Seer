 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class TerritoryData {
 	public static List<Territory> init(List<Continent> continentList)
 	{
 		List<Territory> territoryList = new ArrayList<Territory>();
 		
 		BufferedReader reader;
 		try
 		{
 			reader = new BufferedReader(new FileReader("TerritoryData.txt"));
 		}
 		catch(FileNotFoundException e)
 		{
 			throw new RuntimeException("File Not Found");
 		}
 		String line=null;
 		try {
 			while((line= reader.readLine())!= null)
 			{
 				//DO FILE STUFF HERE
 				if(line.charAt(0)!='#')//#=comment symbol
 				{
 					territoryList.add(initTerritory(line, continentList));
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return territoryList;
 	}
 	public static Territory initTerritory(String territoryData, List<Continent> continentList)
 	{
 		char currentChar;
 		int currentDataIndex=0;
 		String name="";
 		String continent="";
 		int numberOfAjacentTerritories=0;
 		String tempAjacentName="";
 		int xPos=0;
 		int yPos=0;
 		List<String> ajacentTerritoryNames = new ArrayList<String>();
 		
 		
 		for(int i=0;i<territoryData.length();i++)
 		{
 			if((currentChar=territoryData.charAt(i))!=';')
 			{
 				switch(currentDataIndex)
 				{
 				case 0:
 					name+=currentChar;
 					break;
 				case 1:
 					continent+=currentChar;
 					break;
 				case 2:
 					numberOfAjacentTerritories+=(currentChar-'0');
 					break;
 				case 3:
 					if(currentChar!=',')
 					{
 						tempAjacentName+=currentChar;
 					}
 					else
 					{
 						ajacentTerritoryNames.add(tempAjacentName);
 						tempAjacentName=new String("");
 					}
 					break;
 				case 4:
 					xPos=xPos*10+(currentChar-'0');
 					break;
 				case 5:
 					yPos=yPos*10+(currentChar-'0');
 					break;
 				}
 			}
 			else
 			{
 				currentDataIndex=++currentDataIndex%6;//increases currentDataIndex by 1, then modulizes(?) it by 6
 			}
 			
 		}
 		return new Territory(name, ContinentData.findContinentByName(continent,continentList), ajacentTerritoryNames, xPos, yPos);
 	}
 	public static Territory findTerritoryByName(String territoryName, List<Territory> territoryList)
 	{
 		for (Territory i : territoryList)
 		{
			if(i.name==territoryName) return i;
 		}
 		return new Territory(100,100);
 	}
 	
 	public void linkTerritories(List<Territory> territoryList)
 	{
 		for (Territory territory : territoryList)
 		{
 			for (String name : territory.getAjacentTerritoryNameList())
 			{
 				territory.link(TerritoryData.findTerritoryByName(name,territoryList));
 			}			
 		}
 	}
 	
 
 }
