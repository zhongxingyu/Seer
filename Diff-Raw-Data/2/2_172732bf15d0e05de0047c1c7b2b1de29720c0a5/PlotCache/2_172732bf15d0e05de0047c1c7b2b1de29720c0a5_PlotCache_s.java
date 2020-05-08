 package net.croxis.plugins.civilmineation;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.bukkit.Chunk;
 
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 
 public class PlotCache {
 	private static Civilmineation plugin;
 	private static HashMap<String, HashMap<Integer, HashMap<Integer, PlotComponent>>> db;
 
 	public PlotCache(Civilmineation plugin){
 		PlotCache.plugin = plugin;
 		flushDatabase();
 	}
 	
 	public static void flushDatabase(){
 		db = new HashMap<String, HashMap<Integer, HashMap<Integer, PlotComponent>>>();
 	}
 	
 	public static PlotComponent getPlot(Chunk chunk){
 		return getPlot(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
 	}
 	
 	public static PlotComponent getPlot(String world, int x, int z){
 		try{
 			db.get(world).get(x).get(z).setLastAccess(System.currentTimeMillis());
 			return db.get(world).get(x).get(z);
 		} catch (Exception e){
 			PlotComponent plot = plugin.getDatabase().find(PlotComponent.class).where().ieq("world", world).eq("x", x).eq("z", z).findUnique();
 			if (plot == null){
 				plot = new PlotComponent();
 				plot.setName("Wilderness");
 				plot.setWorld(world);
 				plot.setX(x);
 				plot.setZ(z);
 			}
 			if (!db.containsKey(world))
 				db.put(world, new HashMap<Integer, HashMap<Integer, PlotComponent>>());
 			if (!db.get(world).containsKey(x))
 				db.get(world).put(x, new  HashMap<Integer, PlotComponent>());
 			if (!db.get(world).get(x).containsKey(z))
 				db.get(world).get(x).put(z, plot);	
 			plot.setLastAccess(System.currentTimeMillis());
 			return plot;
 		}
 	}
 	
 	public static void dirtyPlot(PlotComponent plot){
 		dirtyPlot(plot.getWorld(), plot.getX(), plot.getZ());
 	}
 	
 	public static void dirtyPlot(String world, int x, int z){
 		try{
 			db.get(world).get(x).remove(z);
 		} catch (Exception e){
 			// Well if it doesn't exist why bother!
 		}
 	}
 	
 	public static void upkeep(long time){
 		// Pumped externally, may not be thread safe. Remove entries that are older than the given time
 		Civilmineation.logDebug("Initiating plot cache cleanup");
 		Iterator<HashMap<Integer, HashMap<Integer, PlotComponent>>> worldIterator = db.values().iterator();
 		while (worldIterator.hasNext()){
 			HashMap<Integer, HashMap<Integer, PlotComponent>> xMap = worldIterator.next();
 			Iterator<HashMap<Integer, PlotComponent>> xIterator = xMap.values().iterator();
 			while (xIterator.hasNext()){
 				HashMap<Integer, PlotComponent> zMap = xIterator.next();
 				Iterator<Entry<Integer, PlotComponent>> plotIterator = zMap.entrySet().iterator();
 				Civilmineation.logDebug("Pre size: " + Integer.toString(zMap.size()));
 				while (plotIterator.hasNext()){
					if (zMap.get(plotIterator.next().getValue()).getLastAccess() < System.currentTimeMillis() - time){
 						plotIterator.remove();
 						Civilmineation.logDebug("Removed a plot. I hope this works!");
 					}
 				}
 				Civilmineation.logDebug("Post size: " + Integer.toString(zMap.size()));
 			}
 		}
 		Civilmineation.logDebug("Finished plot cache cleanup");
 	}
 }
