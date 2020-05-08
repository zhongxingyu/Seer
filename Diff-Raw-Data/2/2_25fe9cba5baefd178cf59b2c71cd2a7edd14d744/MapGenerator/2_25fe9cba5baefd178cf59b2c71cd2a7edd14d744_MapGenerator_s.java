 package com.vloxlands.gen;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.world.Island;
 import com.vloxlands.game.world.Map;
 
 public class MapGenerator extends Thread
 {
 	public Map map;
 	public float progress, progressBefore;
 	
 	IslandGenerator gen;
 	int z, x, spread, size, index;
 	
 	public MapGenerator(int x, int z, int spread, int size)
 	{
 		this.x = x;
 		this.z = z;
 		this.spread = spread;
 		this.size = size;
 		progress = 0;
 		progressBefore = 0;
 		setDaemon(true);
 		setName("MapGenerator-Thread");
 	}
 	
 	@Override
 	public void run()
 	{
 		map = new Map();
 		while (!isDone())
 		{
 			if (gen == null)
 			{
 				progressBefore = progress;
 				gen = new IslandGenerator((int) (size * 0.75), (int) (size * 1.25));
 				gen.start();
 				index++;
 			}
 			
 			System.out.print(""); // don't why this is needed
 			
 			progress = gen.progress / (x * z) + progressBefore;
 			if (gen.finishedIsland != null)
 			{
 				Island i = gen.finishedIsland;
				i.setPos(new Vector3f(((index - 1) / x) * size * 2, 0, ((index - 1) % z) * size * 2));
 				
 				map.addIsland(i);
 				gen = null;
 			}
 		}
 	}
 	
 	public boolean isDone()
 	{
 		return map.islands.size() == (x * z);
 	}
 }
