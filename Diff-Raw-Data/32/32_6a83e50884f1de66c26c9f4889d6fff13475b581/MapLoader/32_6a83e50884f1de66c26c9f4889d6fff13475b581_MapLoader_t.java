 package jig.ironLegends;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Random;
 import java.util.Vector;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import jig.engine.ResourceFactory;
 import jig.ironLegends.core.ResourceIO;
 
 public class MapLoader 
 {
 	
 	// return a randomized range either "left" border, "center", or "right" border
 	static public int getRandBorderRange(Random random, int start, int max, int range)
 	{
 		float rand = random.nextFloat();
 		int t = (int) (rand*3000);
 		if (t < 1000)
 		{
 			return start;
 		}
 		else if (t < 2000)
 		{
 			return start + max - range;
 		}
 		else
 		{
 			return start + (max-range-1)/2;
 		}		
 	}
 	static public int getRandRange(Random random, int start, int range)
 	{
 		float rand = random.nextFloat();
 		int part = (int)(rand * 20);
 		return start+ part;
 	}
 	
 	public interface IMapLoadSink
 	{
 		void mapName(String sMapName);
 		void mapDim(int width, int height);
 		void onLine(String line);
 	}
 	
 	public interface IMapSave
 	{
 		String mapName();
 		int cols();
 		int rows();
 		String nextLine();
 	}
 	
 	public static boolean saveLayer(IMapSave source, String sFile, ResourceIO rw)
 	{
 		boolean bSuccess = true;
 		
 		rw.openWrite(sFile);
 		
 		BufferedWriter ps = rw.getBufferedWriter();
 		
 		try {
 			/*
 			 name of map
 			 cols rows
 			 map data
 			 */
 			ps.write(source.mapName() + "\n");
 			int rows = source.rows();
 			int cols = source.cols();
 			ps.write(Integer.toString(cols) + " " + Integer.toString(rows) + "\n");
 			
 			String line = source.nextLine();
 			while (line != null)
 			{
 				ps.write(line + "\n");
 				line = source.nextLine();
 			}
 			
 			ps.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			bSuccess = false;
 		}
 		
 		rw.close();
 		
 		return bSuccess;
 	}
 
 	protected static void addMaps(Vector<String> maps, String sDir)
 	{
 		File dir = new File(sDir);
 
 		String[] list = dir.list();
 		if (list == null)
 			return;
 		
 		String output = "";
 
 		for (int i = 0; i < list.length; i++)  {
 			if (list[i].endsWith(".txt"))
 			{
 				maps.add(sDir + "/" + list[i]);
 				output += list[i]+"\n";
 			}
 		}		
 	}
 
 	public static Vector<String> listMaps(String sInstallDir, String jarName)
 	{
 		Vector<String> maps = new Vector<String>();
 		String sMapJarLocation = sInstallDir + jarName;
 		
 		// read jar to get listing of maps
 		{
 			File file = new File(sMapJarLocation);
 			JarFile jarFile = null;
 			try {
 				jarFile = new JarFile(file);
 			} catch (IOException e1) {
 				jarFile = null;
 			}
 	
 			if (jarFile != null)
 			{
 			    Enumeration<JarEntry> e = jarFile.entries();
 			    while (e.hasMoreElements()) {
 			    	JarEntry j = e.nextElement();
 			    	if (j.getName().startsWith("jig/ironLegends/maps/") && 
 			    		j.getName().length() > "jig/ironLegends/maps/".length())
 			    	{
 			    		maps.add(j.getName());
 			    	}
 			    }	    
 			}
 		}
 		
		String sMapDevJarLocation = sInstallDir + "bin/jig/ironLegends/maps";
		String sMapExtLocation = sInstallDir + "maps";
 		
 		System.out.println("Loading maps from jar: " + sMapJarLocation);
 		addMaps(maps, sMapJarLocation);
 		if (maps.size() == 0)
 			addMaps(maps, sMapDevJarLocation);
 		
 		System.out.println("Loading maps from external: " + sMapExtLocation);
 		addMaps(maps, sMapExtLocation);
 		
 		return maps;
 	}
 	
 	public static boolean loadLayer(IMapLoadSink sink, String sFile, ResourceIO rr)
 	{
 		//ResourceFactory resourceFactory = ResourceFactory.getFactory();
 		URL url = ResourceFactory.findResource(IronLegends.GAME_ROOT + sFile);
 		if (url != null)
 		{
 			//System.out.println("Located level " + level + " at\n\t" + url.toString());
 			rr.open(url);
 		}
 		else
 		{
 			rr.open(sFile);
 		}
 
 		BufferedReader br = null;
 
 		br = rr.getBufferedReader();
 		if (br == null)
 		{			
 			rr.close();
 			return false;
 		}
 		
 		boolean bSuccess = true;
 
 		while (bSuccess)
 		{
 			try
 			{
 				String line = br.readLine();
 				String mapName = line;
 				sink.mapName(mapName);
 				
 				line = br.readLine();
 				String tokens[] = line.split(" ");
 				
 				if (tokens.length != 2)
 				{
 					break;
 				}
 				// extract cols,rows
 				int cols = 0;
 				int rows = 0;
 				cols = Integer.parseInt(tokens[0]);
 				rows = Integer.parseInt(tokens[1]);
 				
 				sink.mapDim(cols, rows);
 				
 				line = br.readLine();
 				while (line != null)
 				{
 					sink.onLine(line);
 					line = br.readLine();
 				}
 				tokens = null;
 								
 				break;
 				
 			} catch (FileNotFoundException ex) 
 			{
 				bSuccess = false;
 				System.out.println(ex);
 			} catch (IOException ex) 
 			{
 				bSuccess = false;
 				System.out.println(ex);
 			}
 		}
 
 		rr.close();
 		
 		return bSuccess;
 	}
 }
