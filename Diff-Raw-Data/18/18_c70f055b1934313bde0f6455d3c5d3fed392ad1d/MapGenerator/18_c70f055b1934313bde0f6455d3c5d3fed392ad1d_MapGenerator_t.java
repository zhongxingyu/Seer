 //package com.vloxlands.gen;
 //
 //import java.io.IOException;
 //
 //import org.lwjgl.util.vector.Vector3f;
 //
 //import com.vloxlands.game.Game;
 //import com.vloxlands.game.world.Island;
 //import com.vloxlands.game.world.Map;
 //import com.vloxlands.net.packet.Packet8Loading;
 //import com.vloxlands.net.packet.Packet9Island;
 //
 //public class MapGenerator extends Thread
 //{
 //	public static enum MapSize
 //	{
 //		TINY(3),
 //		SMALL(5),
 //		MEDIUM(7),
 //		BIG(9),
 //		HUGE(12);
 //		
 //		private int size;
 //		
 //		private MapSize(int size)
 //		{
 //			this.size = size;
 //		}
 //		
 //		public int getSize()
 //		{
 //			return size;
 //		}
 //	}
 //	
 //	public Map map;
 //	public float progress, progressBefore, lastProgress;
 //	
 //	IslandGenerator gen;
 //	int z, x, spread, size, index;
 //	float cacheY;
 //	
 //	public MapGenerator(int x, int z, int spread, int size)
 //	{
 //		this.x = x;
 //		this.z = z;
 //		this.spread = spread;
 //		this.size = size;
 //		progress = 0;
 //		progressBefore = 0;
 //		setDaemon(true);
 //		setName("MapGenerator-Thread");
 //	}
 //	
 //	@Override
 //	public void run()
 //	{
 //		map = new Map();
 //		while (!isDone())
 //		{
 //			if (gen == null)
 //			{
 //				progressBefore = progress;
 //				cacheY = (float) ((Math.random() * spread * 2) - spread);
 //				gen = new IslandGenerator((int) (size * 0.75), (int) (size * 1.25), cacheY);
 //				gen.start();
 //				index++;
 //			}
 //			lastProgress = new Float(progress); // stupid reference system :(
 //			progress = gen.progress / (x * z) + progressBefore;
 //			
 //			if (progress != lastProgress)
 //			{
 //				try
 //				{
 //					Game.server.sendPacketToAllClients(new Packet8Loading("generatemap", progress));
 //				}
 //				catch (IOException e)
 //				{
 //					e.printStackTrace();
 //				}
 //			}
 //			
 //			
 //			if (gen.finishedIsland != null)
 //			{
 //				Island i = gen.finishedIsland;
 //				i.setPos(new Vector3f((int) (((index - 1) / z) * size * 2 + (Math.random() * spread * 2) - spread), cacheY, (int) (((index - 1) % z) * size * 2 + (Math.random() * spread * 2) - spread)));
 //				try
 //				{
 //					Game.server.sendPacketToAllClients(new Packet9Island(i));
 //				}
 //				catch (IOException e)
 //				{
 //					e.printStackTrace();
 //				}
 //				map.addIsland(i);
 //				gen = null;
 //			}
 //			
 //		}
 //	}
 //	
 //	public boolean isDone()
 //	{
 //		if (map == null) return false;
 //		return map.islands.size() == (x * z);
 //	}
 //}
 //
 
 // TODO: Will be finished soon
 package com.vloxlands.gen;
 
 import java.awt.Point;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.game.world.Island;
 import com.vloxlands.game.world.Map;
 import com.vloxlands.net.packet.Packet8Attribute;
 import com.vloxlands.net.packet.Packet9Island;
 
 public class MapGenerator extends Thread
 {
 	public static enum MapSize
 	{
 		TINY(3),
 		SMALL(5),
 		MEDIUM(7),
 		BIG(9),
 		HUGE(12);
 		
 		private int size;
 		
 		private MapSize(int size)
 		{
 			this.size = size;
 		}
 		
		public int getSizeSQ()
		{
			return size * size;
		}
		
 		public int getSize()
 		{
 			return size;
 		}
 	}
 	
 	public Map map;
 	public float progress, progressBefore, lastProgress;
 	
 	IslandGenerator gen;
 	int index;
 	int playerCount;
 	int playableIslands;
 	float factor;
 	MapSize size;
 	ArrayList<Point> takenSpots;
 	
 	public MapGenerator(int playerCount, MapSize size)
 	{
 		this.size = size;
 		this.playerCount = playerCount;
 		playableIslands = (int) (Math.random() * (playerCount * 2) + playerCount);
 		progress = 0;
 		progressBefore = 0;
 		setDaemon(true);
 		setName("MapGenerator-Thread");
 		takenSpots = new ArrayList<>();
		factor = size.getSizeSQ();
 	}
 	
 	@Override
 	public void run()
 	{
 		map = new Map();
 		try
 		{
 			Game.server.sendPacketToAllClients(new Packet8Attribute("mapeditor_progress_string", "generatemap"));
 		}
 		catch (IOException e1)
 		{
 			e1.printStackTrace();
 		}
 		for (int i = 0; i < playableIslands; i++)
 		{
 			float y = (float) (Math.random() * 256);
 			Point spot = pickRandomSpot();
 			generateIsland(y, 32, 48);
 			gen.finishedIsland.setPos(new Vector3f(spot.x * Island.SIZE, y, spot.y * Island.SIZE));
 			try
 			{
 				Game.server.sendPacketToAllClients(new Packet9Island(gen.finishedIsland));
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 			map.addIsland(gen.finishedIsland);
 		}
 		
 		for (int i = 0; i < size.getSize(); i++)
 		{
 			for (int j = 0; j < size.getSize(); j++)
 			{
 				if (takenSpots.contains(new Point(i, j))) continue;
 				float y = (float) (Math.random() * 256);
 				generateIsland(y, 12, 20);
 				gen.finishedIsland.setPos(new Vector3f(i * Island.SIZE, y, j * Island.SIZE));
 				try
 				{
 					Game.server.sendPacketToAllClients(new Packet9Island(gen.finishedIsland));
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 				map.addIsland(gen.finishedIsland);
 			}
 		}
 	}
 	
 	private void generateIsland(float y, int minSize, int maxSize)
 	{
 		gen = new IslandGenerator(minSize, maxSize, y);
 		gen.start();
 		while (gen.finishedIsland == null)
 		{
 			lastProgress = new Float(progress);
 			
 			progress = (gen.progress / factor) + progressBefore;
 			
 			if (progress != lastProgress)
 			{
 				try
 				{
 					Game.server.sendPacketToAllClients(new Packet8Attribute("mapeditor_progress_float", progress));
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 		progressBefore = new Float(progress);
 	}
 	
 	private Point pickRandomSpot()
 	{
 		Point p = new Point();
 		
 		do
 		{
 			p.x = (int) (Math.random() * size.getSize());
 			p.y = (int) (Math.random() * size.getSize());
 		}
 		while (takenSpots.contains(p));
 		takenSpots.add(p);
 		
 		return p;
 	}
 }
