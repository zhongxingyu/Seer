 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Vector;
 
 import tiled.core.Map;
 import tiled.core.MapLayer;
 import tiled.core.MapObject;
 import tiled.core.ObjectGroup;
 import tiled.core.Tile;
 import tiled.core.TileLayer;
 import tiled.core.TileSet;
 import tiled.io.TMXMapReader;
 
 
 public class Convert {
 
 	static TMXMapReader reader = new TMXMapReader();
 	/**
 	 * @param args
 	 * @throws FileNotFoundException 
 	 */
 	public static void main(String[] args) throws FileNotFoundException {
 		String directory = "/Users/meros/Documents/development/playn-greengrappler/greengrappler/core/src/main/java/com/meros/playn/resources/data/rooms/";
 		File dir = new File(directory);
 
 
 		for (String file : dir.list())
 		{
 			if (file.endsWith(".tmx"))
 				convertFile(directory + file);
 		}
 	}
 
 	public static void convertFile(String filename) throws FileNotFoundException
 	{
 		PrintStream ps = new PrintStream(new FileOutputStream(filename.replace(".tmx", ".txt")));
 		// TODO Auto-generated method stub
 		try {
 			Map map = reader.readMap(filename);
 			Vector<MapLayer> layers = map.getLayers();
 			HashMap<Integer, Integer> gidtoidMap = new HashMap<Integer, Integer>();
 			HashMap<Integer, Integer> gidtoEntIdMap = new HashMap<Integer, Integer>();
 
 			Vector<TileSet> sets = map.getTileSets();
 
 			int id = 0;
 
 			int tilecount = 0;
 			for (int i = 0; i < sets.size(); i++)
 			{
 				tilecount += sets.get(i).size();
 			}
 
 			ps.println(tilecount);
 
 			for (int i = 0; i < sets.size(); i++)
 			{
 				TileSet set = sets.get(i);
 
 				for (int lid = 0; lid <= set.getMaxTileId(); lid++)
 				{
 					String imageFilename = set.getTilebmpFile();
 					int dataPos = imageFilename.indexOf("/data/");
 					imageFilename = imageFilename.substring(dataPos+1);
 					ps.println(imageFilename);
 
 					int xi = (lid)%set.getTilesPerRow();
 					int yi = (lid)/set.getTilesPerRow();
 
 					int x = xi*(set.getTileSpacing()+set.getTileWidth());
 					int y = yi*(set.getTileSpacing()+set.getTileHeight());
 
 					ps.println(x);
 					ps.println(y);
 					ps.println(set.getTileWidth());
 					ps.println(set.getTileHeight());
 
 					Tile tile = set.getTile(lid);
 					Properties prop = tile.getProperties();
 					boolean collide = false;
 					boolean hook = false;
 
 					if (prop.containsKey("collide"))
 						collide = prop.getProperty("collide").compareTo("true") == 0;
 					if (prop.containsKey("hook"))
 						hook = prop.getProperty("hook").compareTo("true") == 0;
 
 					ps.println(collide?1:0);
 					ps.println(hook?1:0);
 
 					gidtoidMap.put(tile.getGid(), id++);
 
 					if (sets.get(i).getName().compareTo("entities") == 0)
 					{
 						gidtoEntIdMap.put(tile.getGid(), gidtoEntIdMap.size());
 					}
 				}
 			}
 
 			printLayer("background", layers, gidtoidMap, ps);
 			printLayer("middle", layers, gidtoidMap, ps);
 			printLayer("foreground", layers, gidtoidMap, ps);
 
 			printLayer("entities", layers, gidtoEntIdMap, ps);
 			
 			boolean found = false;
 			
 			for(MapLayer layer : layers)
 			{
 				if (layer.getName().compareTo("camera") != 0)
 					continue;
 				
 				ObjectGroup cameraRects = (ObjectGroup)layer;
 				Iterator<MapObject> it = cameraRects.getObjects();
 				
 				ps.println(cameraRects.getObjectCount());
 						
 				while (it.hasNext())
 				{
 					MapObject object = it.next();
 					
 					ps.println(object.getX());
 					ps.println(object.getY());
 					ps.println(object.getWidth());
 					ps.println(object.getHeight());
 				}
 				
 				found = true;
 			}
 			
 			if (!found)
 			{
 				ps.println(0);
 			}
 			
 		} catch (Exception e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 
 	private static void printLayer(String aLayerName, Vector<MapLayer> aLayers, HashMap<Integer, Integer> aGidToIdMap, PrintStream aPs) {
 		boolean found = false;
 		for(int i = 0; i < aLayers.size(); i++)
 		{
 			MapLayer layer = aLayers.get(i);
 			if (layer.getName().compareTo(aLayerName) == 0)
 			{
 				aPs.println(layer.getWidth());
 				aPs.println(layer.getHeight());
 				found = true;
 				if (layer instanceof TileLayer)
 				{
 					TileLayer tLayer = (TileLayer)layer;
 
 					for(int x = 0; x < layer.getWidth(); x++)
 					{
 						for (int y = 0; y < layer.getHeight(); y++)
 						{
 							Tile tile = tLayer.getTileAt(x, y);
 
 							if (tile != null)
 							{
 								int id = aGidToIdMap.get(tile.getGid());
 								aPs.println(id);
 							}
 							else
 								aPs.println(-1);
 						}
 					}
 				}
 
 			}
 		}
 
 		if (!found)
 		{
 			aPs.println(0);
 			aPs.println(0);
 		}
 
 	}
 }
