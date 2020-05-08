 package drexel.dragonmap;
 
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.res.AssetManager;
 import android.util.Log;
 
 
 public class POIList
 {
 
 	ArrayList<POI> list_;
 	String categories[] = null;
 	String children[][] = null;
 	
 	public POIList()
 	{
 		list_ = new ArrayList<POI>();
 	}
 	
 	public POIList(String fname, AssetManager assets)
 	{
 		this();
 		load(fname, assets);
 		genFloorPlans(assets);
 		
 		parseData();
 	}
 	
 	public ArrayList<POI> getList()
 	{
 		return list_;
 	}
 	
 	//TODO: handle error in a better way!
 	public void load(String fname, AssetManager assets)
 	{
 		try
 		{
 			InputStream is = assets.open(fname);
 			int size = is.available(); 
 			byte[] buffer = new byte[size]; 
 			is.read(buffer); 
 			is.close(); 
 			String text = new String(buffer); 
 			String[] lines = text.split("\n");
 			  
 			for (String line: lines)
 			{
 				POI newObj = new POI();
 				newObj.fromJSON(line);				
 				list_.add(newObj);
 			}
 		}
 		catch (IOException e)
 		{
 			Log.e("poilist", e.toString());
 		}
 	}
 	
 	public void genFloorPlans(AssetManager assets)
 	{
 		try
 		{
 			String[] buildings = assets.list("floor_plans");
 			
 			for (String building: buildings)
 			{
 				String dir = "floor_plans/" + building; 
 				String[] floorImages = assets.list(dir);
 				String POIName = null;
 				InputStream is = assets.open(dir + "/ref.txt");
 				try
 				{
 					int size = is.available(); 
 					byte[] buffer = new byte[size]; 
 					is.read(buffer); 
 					POIName = new String(buffer); 
 				}
 				catch (IOException e)
 				{
 					//couldn't open reference file, just ignore it
 					//(not a floor plan directory)
 					continue;
 				}
 				finally
 				{
 					is.close(); 
 				}
 				//POIName is our POI's ID
 				//floorImages is a list of image files
 				POI myPOI = this.getPOIByName(POIName);
 				
 				FloorList myFloorList = new FloorList();
 				for (String img: floorImages)
 				{
 					try
 					{
 						Floor newFloor = new Floor();
 						newFloor.setFloorNum( Integer.parseInt( img.split("\\.")[0] ) );
 						newFloor.setImageSrc(dir + "/" + img);
 						myFloorList.addFloor( newFloor );
 					}
 					catch (NumberFormatException e)
 					{
 						// file is not [num].ext, so we'll ignore it
 						continue;
 					}
 				}
 				myPOI.setFloorList(myFloorList);
 			}
 		}
 		catch (IOException e)
 		{
 			//
 			Log.e("floorplans", e.toString());
 		}
 	}
 	
 	public ArrayList<POI> find(String match)
 	{
 		ArrayList<POI> matched = new ArrayList<POI>();
 		for (POI item: list_)
 		{
 			if (item.isMatch(match))
 			{
 				matched.add(item);
 			}
 		}
 		return matched;
 	}
 	
 	/* I don't know what the word efficiency means. You can tell, right?
 	 * 
 	 */
 	private void parseData()
 	{
 		//could be a hashmap, but we're converting to arrays for android anyway, so why bother
 		ArrayList<String> cats = new ArrayList<String>();
 		ArrayList<ArrayList<String>> items = new ArrayList<ArrayList<String>>();
 		for (POI poi: list_)
 		{
 			String category = poi.getCategory();
 			if (!cats.contains(category))
 			{
 				cats.add(category);
 				if (cats.size() > items.size())
 					items.add(new ArrayList<String>());
 			}
 			items.get(cats.indexOf(category)).add(poi.getName());
 		}
		Collections.sort(cats);
 		this.categories = cats.toArray(new String[cats.size()]);
 		this.children = new String[categories.length][];
 		for (int i=0; i<categories.length; i++)
 		{
 			this.children[i] = items.get(i).toArray(new String[items.get(i).size()]);
 		}
 	}
 	
 	public String[] getCategories()
 	{
 		return this.categories;
 	}
 	
 	public String[][] getChildren()
 	{
 		return this.children;
 	}
 	
 	public POI getPOIByName(String name)
 	{
 		for (POI item: list_)
 		{
 			if (item.getName().equals(name))
 			{
 				return item;
 			}
 		}
 		return null;
 	}
 	
 	public POI getFirstContained(float x, float y)
 	{
 		for (POI p: list_)
 		{
 			if (p.isContained(x, y))
 				return p;
 		}
 		return null;
 	}
 
 	
 }
 
 
