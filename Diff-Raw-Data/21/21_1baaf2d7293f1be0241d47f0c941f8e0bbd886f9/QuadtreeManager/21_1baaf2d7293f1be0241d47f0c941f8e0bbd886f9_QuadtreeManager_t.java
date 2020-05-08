 package com.github.catageek.BCProtect.Persistence;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.bukkit.World;
 
 import com.github.catageek.BCProtect.BCProtect;
 
 public final class QuadtreeManager {
 
 	private static Map<String,PersistentQuadtree> quadtreeList = new HashMap<String,PersistentQuadtree>();
 
 	public QuadtreeManager() {
 		Iterator<World> it = BCProtect.myPlugin.getServer().getWorlds().iterator();
 		while (it.hasNext()) {
 			String s = it.next().getName();
			addWorld(s);
 		}
 
 	}

	public void addWorld(String s) {
		quadtreeList.put(s, new PersistentQuadtree(s));
	}
 	
 	public PersistentQuadtree getQuadtree(String world) {
 		return quadtreeList.get(world);
 		
 	}
 
 	public void closeAll() {
 		Iterator<PersistentQuadtree> it = quadtreeList.values().iterator();
 		while (it.hasNext())
 			it.next().close();
 	}
 
 }
