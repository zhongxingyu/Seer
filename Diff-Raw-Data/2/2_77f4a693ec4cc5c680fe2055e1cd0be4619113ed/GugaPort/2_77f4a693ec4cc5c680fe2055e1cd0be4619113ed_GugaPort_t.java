 package me.Guga.Guga_SERVER_MOD;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 
 import org.bukkit.Location;
 
 public abstract class GugaPort 
 {
 	public static void SetPlugin(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	public static GugaPlace GetPlaceByName(String name)
 	{
 		Iterator<GugaPlace> i = places.iterator();
 		while (i.hasNext())
 		{
 			GugaPlace e = i.next();
 			if (e.GetName().equalsIgnoreCase(name))
 			{
 				return e;
 			}
 		}
 		return null;
 	}
 	public static void AddPlace(String name, String owner, Location loc)
 	{
 		GugaPlace place = new GugaPlace(name, owner, loc);
 		places.add(place);
 		SavePlaces();
 	}
 	public static void AddPlace(GugaPlace place)
 	{
 		places.add(place);
 		SavePlaces();
 	}
 	public static void RemovePlace(GugaPlace place)
 	{
 		places.remove(place);
 		SavePlaces();
 	}
 	public static void SavePlaces()
 	{
 		plugin.log.info("Saving Places Data...");
 		File file = new File(placesFile);
 		if (!file.exists())
 		{
 			try 
 			{
 				file.createNewFile();
 				
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		try 
 		{
 			FileWriter fStream = new FileWriter(file, false);
 			BufferedWriter bWriter;
 			bWriter = new BufferedWriter(fStream);
 			String line;
 			Iterator<GugaPlace> i = places.iterator();
 			while (i.hasNext())
 			{
 				line = i.next().toString();
 				bWriter.write(line);
 				bWriter.newLine();
 			}
 			
 			bWriter.close();
 			fStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public static void LoadPlaces()
 	{
 		plugin.log.info("Loading Places Data...");
 		File file = new File(placesFile);
 		if (!file.exists())
 		{
 			try 
 			{
 				file.createNewFile();
 				return;
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 				return;
 			}
 		}
 		else
 		{
 			try 
 			{
 				FileInputStream fRead = new FileInputStream(file);
 				DataInputStream inStream = new DataInputStream(fRead);
 				BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));		
 				String line;
 				String []splittedLine;
 				String name;
 				int x;
 				int y;
 				int z;
 				String world;
 				String owner;
 				try {
 					while ((line = bReader.readLine()) != null)
 					{
 						splittedLine = line.split(";");
 						name = splittedLine[0];
 						owner = splittedLine[1];
 						x = Integer.parseInt(splittedLine[2]);
 						y = Integer.parseInt(splittedLine[3]);
 						z = Integer.parseInt(splittedLine[4]);
						world = splittedLine[5];
 						places.add(new GugaPlace(plugin.getServer().getWorld(world),name , owner, x, y, z));
 					}
 					bReader.close();
 					inStream.close();
 					fRead.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}			
 			} 
 			catch (FileNotFoundException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	public static ArrayList<GugaPlace> GetPlacesForPlayer(String pName)
 	{
 		@SuppressWarnings("unchecked")
 		ArrayList<GugaPlace> p = (ArrayList<GugaPlace>) places.clone();
 		
 		Collections.copy(p, places);
 		Iterator<GugaPlace> i = places.iterator();
 		while (i.hasNext())
 		{
 			GugaPlace e = i.next();
 			if (e.GetOwner().equalsIgnoreCase("all"))
 			{
 				// Do nothing
 			}
 			else if (!e.GetOwner().equalsIgnoreCase(pName))
 			{
 				p.remove(e);
 			}
 		}
 		return p;
 	}
 	public static ArrayList<GugaPlace> GetAllPlaces()
 	{
 		return places;
 	}
 	private static String placesFile = "plugins/Places.dat";
 	private static ArrayList<GugaPlace> places = new ArrayList<GugaPlace>();
 	private static Guga_SERVER_MOD plugin;
 }
