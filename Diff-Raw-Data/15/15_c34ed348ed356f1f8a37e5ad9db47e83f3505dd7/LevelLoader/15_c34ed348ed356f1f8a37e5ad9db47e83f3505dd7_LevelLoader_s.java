 package net.infinitycoding.carsim.modules.io;
 
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Scanner;
 
 import net.infinitycoding.carsim.CarSim;
 import net.infinitycoding.carsim.exceptions.LevelFormatException;
 import net.infinitycoding.carsim.modules.Level;
 
 public class LevelLoader
 {	
 	static String lvlPic;
 	static int maxCars;
 	static long carRatio;
 	static int streetCount;
	static HashMap<Integer, Integer> stopLineCoords;
 	static ArrayList<Integer> carSpawns;
	static HashMap<Integer, Integer> trafficLightCoords;
 	
 	public static Level loadLevel(String lvlFileName) throws LevelFormatException
 	{
 		HashMap<String, String> contents = new HashMap<String, String>();
 		Scanner scr = new Scanner(CarSim.class.getResourceAsStream(lvlFileName));
 			
 		while (scr.hasNext())
 		{
 			String line = scr.nextLine();
			System.out.println("Line: " + line);
 			String key = line.split("=")[0];
 			String val = line.split("=")[1];
 			contents.put(key, val);
 				
 			// DEBUGPART //
 			//System.out.println("Key: " + key);
 			//System.out.println("Value: " + val);
 			//System.out.println("------------------");
 		}
 		
		HashSet<String> keys = (HashSet<String>) contents.keySet();
 		
 		for (String elem : keys)
 		{
 			if (elem.equalsIgnoreCase("lvlpic"))
 			{
 				lvlPic = contents.get(elem);
 			}
 			else if (elem.equalsIgnoreCase("maxcars"))
 			{
 				try
 				{
 					maxCars = Integer.parseInt(contents.get(elem));
 				}
 				catch (NumberFormatException ex)
 				{
 					throw new LevelFormatException();
 				}
 			}
 			else if (elem.equalsIgnoreCase("spawnrate"))
 			{
 				try
 				{
 					carRatio = Long.parseLong(contents.get(elem));
 				}
 				catch (NumberFormatException ex)
 				{
 					throw new LevelFormatException();
 				}
 			}
 			else if (elem.equalsIgnoreCase("streetcount"))
 			{
 				try
 				{
 					streetCount = Integer.parseInt(contents.get(elem));
 				}
 				catch (NumberFormatException ex)
 				{
 					throw new LevelFormatException();
 				}
 			}
 			else if (elem.startsWith("stopline"))
 			{
 				int x = Integer.parseInt(contents.get(elem).split(",")[0]);
 				int y = Integer.parseInt(contents.get(elem).split(",")[1]);
 				stopLineCoords.put(x, y);
 			}
 			else if (elem.equalsIgnoreCase(""))
 			{
 				
 			}
 			else if (elem.equalsIgnoreCase(""))
 			{
 				
 			}
 			else
 			{
 				throw new LevelFormatException();
 			}
 		}
 		
 		return null;
 	}
 }
