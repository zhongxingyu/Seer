 package ee.lutsu.alpha.mc.mytown.event.tick;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import com.google.common.collect.Lists;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.ChunkProviderServer;
 import net.minecraftforge.common.ConfigCategory;
 import net.minecraftforge.common.Property;
 import ee.lutsu.alpha.mc.mytown.Log;
 import ee.lutsu.alpha.mc.mytown.MyTown;
 import ee.lutsu.alpha.mc.mytown.event.TickBase;
 
 public class WorldBorder extends TickBase
 {
 	public static WorldBorder instance = new WorldBorder();
 	
 	HashMap<Integer, DimConfig> limits = new HashMap<Integer, DimConfig>();
 	List<ChunkGen> generators = Lists.newArrayList();
 	
 	public boolean enabled = true, genenabled = false;
 	private long nextGenConfigSave = 0;
 	public boolean multithreaded = false;
 	
 	public void loadConfig()
 	{
 		enabled = false;
 		limits.clear();
 		
 		boolean willBeEnabled = MyTown.instance.config.get("worldborder", "enabled", true, "Module enabled?").getBoolean(true);
 		
 		ConfigCategory cat = MyTown.instance.config.getCategory("worldborder.limits");
 		cat.setComment("Dimensional limits. S:Limit<n>=<dim>, <type>, <radius>. Type is either circle or square\nExample: S:Limit1=0, circle, 50000");
 		
 		for (Property p : cat.values())
 		{
 			DimConfig c = DimConfig.deserialize(p.value);
 			limits.put(c.dimension, c);
 		}
 
 		enabled = willBeEnabled;
 	}
 	
 	public boolean isWithinArea(Entity e)
 	{
 		if (!enabled)
 			return true;
 		
 		DimConfig limit = limits.get(e.dimension);
 		if (limit != null)
 			return limit.isWithinArea(e);
 		
 		return true;
 	}
 	
 	public void continueGeneratingChunks()
 	{
 		genenabled = MyTown.instance.config.get("worldborder", "chunk-generator-enabled", false, "Generate blocks?").getBoolean(false);
 		multithreaded = MyTown.instance.config.get("worldborder", "chunk-generator-multithreaded", false, "Multithreaded chunk generation. Usable only by threadsafe servers. Expect crashes from modded worlds.").getBoolean(false);
 		
 		if (!genenabled)
 			return;
 		
 		for (Entry<Integer, DimConfig> kv : limits.entrySet())
 		{
 			World w = MinecraftServer.getServer().worldServerForDimension(kv.getKey());
 			if (w == null)
 				continue;
 
 			int radiusTo = (kv.getValue().radius / 16) + 6;
 			boolean circle = kv.getValue().typeCircle;
 
 			generators.add(ChunkGen.start(w, MyTown.instance.config.get("worldborder.generator", "dim_" + kv.getKey() + "_0_radius", 0).getInt(), radiusTo, circle, 0));
 			generators.add(ChunkGen.start(w, MyTown.instance.config.get("worldborder.generator", "dim_" + kv.getKey() + "_1_radius", 0).getInt(), radiusTo, circle, 1));
 			generators.add(ChunkGen.start(w, MyTown.instance.config.get("worldborder.generator", "dim_" + kv.getKey() + "_2_radius", 0).getInt(), radiusTo, circle, 2));
 			generators.add(ChunkGen.start(w, MyTown.instance.config.get("worldborder.generator", "dim_" + kv.getKey() + "_3_radius", 0).getInt(), radiusTo, circle, 3));
 		}
 	}
 	
 	public void generatorReporting(ChunkGen gen, int radiusDone)
 	{
 		MyTown.instance.config.get("worldborder.generator", "dim_" + gen.w.provider.dimensionId + "_" + gen.sector + "_radius", 0).value = String.valueOf(radiusDone + 1);
 
 		if (System.currentTimeMillis() >= nextGenConfigSave)
 		{
 			nextGenConfigSave = System.currentTimeMillis() + 30 * 1000;
 			
 			long done = 0, total = 0;
 			for (ChunkGen g : generators)
 			{
 				done += g.blocksDone;
 				total += g.totalBlocks;
 			}
 			double prc = done * 100 / total;
 			
			Log.info(String.format("[WorldBorder] %s of %s chunks done - %s%%", done, total, (int)prc));
 
 			MyTown.instance.config.save();
 		}
 		
 		if (gen.done())
 		{
 			boolean done = true;
 			for (ChunkGen g : generators)
 				if (!g.done())
 					done = false;
 			
 			if (done)
 			{
 				Log.info("[WorldBorder] All done. Disabled in config.");
 				
 				ConfigCategory cat = MyTown.instance.config.getCategory("worldborder.generator");
 				cat.clear();
 				
 				genenabled = false;
 				MyTown.instance.config.get("worldborder", "chunk-generator-enabled", false, "Generate blocks?").value = String.valueOf(false);
 				MyTown.instance.config.save();
 			}
 		}
 	}
 	
 	public void stopGenerators() throws InterruptedException
 	{
 		if (generators.size() < 1 || !genenabled)
 			return;
 		
 		for (ChunkGen g : generators)
 			g.enabled = false;
 		
 		try
 		{
 			boolean running = true;
 			while (running)
 			{
 				running = false;
 				for (ChunkGen g : generators)
 					if (g.runThread != null && g.runThread.isAlive())
 						running = true;
 				
 				Thread.sleep(50);
 			}
 		}
 		catch (InterruptedException ex)
 		{
 			for (ChunkGen g : generators)
 				if (g.runThread.isAlive())
 					g.runThread.interrupt();
 			
 			throw ex;
 		}
 		finally
 		{
 			for (ChunkGen g : generators)
 				MyTown.instance.config.get("worldborder.generator", "dim_" + g.w.provider.dimensionId + "_" + g.sector + "_radius", 0).value = String.valueOf(g.lastSuccessfulRadius + 1);
 
 			MyTown.instance.config.save();
 			Log.info("[WorldBorder] Stopped chunk gen.");
 		}
 	}
 	
 	public static class ChunkGen implements Runnable
 	{
 		public boolean enabled = true;
 		public final int radiusFrom, radiusTo, sector;
 		public final boolean circle; // unused
 		public final World w;
 		
 		public int lastSuccessfulRadius;
 		public int blocksDone;
 		public final int totalBlocks;
 		
 		public Thread runThread;
 		
 		protected ChunkGen(World w, int radiusFrom, int radiusTo, boolean circle, int sector)
 		{
 			this.w = w;
 			this.radiusFrom = radiusFrom;
 			this.radiusTo = radiusTo;
 			this.circle = circle;
 			this.sector = sector;
 			
 			int smallRound = radiusFrom <= 0 ? 0 : (radiusFrom * 2 + 1) * (radiusFrom * 2 + 1);
 			int bigRound = radiusTo <= 0 ? 1 : (radiusTo * 2 + 1) * (radiusTo * 2 + 1);
 			
 			this.lastSuccessfulRadius = radiusFrom - 1;
 			this.blocksDone = smallRound / 4;
 			this.totalBlocks = bigRound / 4;
 		}
 		
 		public static ChunkGen start(World w, int radiusFrom, int radiusTo, boolean circle, int sector)
 		{
 			ChunkGen gen = new ChunkGen(w, radiusFrom, radiusTo, circle, sector);
 			
 			if (WorldBorder.instance.multithreaded)
 			{
 				gen.runThread = new Thread(gen, String.format("MyTown Chunk generator dim %s sector %s", w.provider.dimensionId, sector));
 				
 				try
 				{
 					gen.runThread.setPriority(Thread.MIN_PRIORITY);
 				}
 				catch (Exception e) { }
 				
 				gen.runThread.start();
 			}
 			
 			return gen;
 		}
 
 		@Override
 		public void run()
 		{
 			for (int round = radiusFrom; round <= radiusTo && enabled; round++)
 			{
 				runTick();
 			}
 		}
 		
 		public void runTick()
 		{
 			if (done())
 				return;
 			
 			int round = lastSuccessfulRadius + 1;
 
 			if (round == 0)
 			{
 				if (sector == 0)
 				{
 					genChunk(w, 0, 0);
 					reportRoundDone(round);
 				}
 				else
 					lastSuccessfulRadius++;
 			}
 			else
 			{
 				if (sector == 0)
 				{
 					int x = -round, z = -round;
 					// upper line
 					for (x++; x <= round && enabled; x++)
 						genChunk(w, x, z);
 				}
 				else if (sector == 1)
 				{
 					int x = round, z = -round;
 					// right line
 					for (z++; z <= round && enabled; z++)
 						genChunk(w, x, z);
 				}
 				else if (sector == 2)
 				{
 					int x = round, z = round;
 					// bottom line
 					for (x--; x >= -round && enabled; x--)
 						genChunk(w, x, z);
 				}
 				else
 				{
 					int x = -round, z = round;
 					// left line
 					for (z--; z >= -round && enabled; z--)
 						genChunk(w, x, z);
 				}
 				
 				if (enabled)
 					reportRoundDone(round);
 				/*
 				 * 4 1 1 1
 				 * 4     2
 				 * 4     2
 				 * 3 3 3 2
 				 * 
 				 *    -z
 				 *  -x  +x
 				 *    +z
 				 */
 			}
 		}
 		
 		public void genChunk(World w, int x, int z)
 		{
 			IChunkProvider provider = w.getChunkProvider();
 			if (!provider.chunkExists(x, z))
 			{
 				provider.loadChunk(x, z);
 				
 				if (provider instanceof ChunkProviderServer)
 					((ChunkProviderServer)provider).unloadChunksIfNotNearSpawn(x, z);
 			}
 		}
 		
 		public void reportRoundDone(int radiusDone)
 		{
 			//int smallRound = radiusFrom <= 0 ? 0 : (radiusFrom * 2 + 1) * (radiusFrom * 2 + 1);
 			int bigRound = radiusDone <= 0 ? 1 : (radiusDone * 2 + 1) * (radiusDone * 2 + 1);
 
 			this.blocksDone = bigRound / 4;
 			this.lastSuccessfulRadius = radiusDone;
 
 			WorldBorder.instance.generatorReporting(this, radiusDone);
 		}
 	
 		public boolean done() { return lastSuccessfulRadius >= radiusTo; }
 	}
 
 	public static class DimConfig
 	{
 		public int dimension;
 		
 		public boolean typeCircle;
 		public int radius;
 		public int sqrDist;
 		
 		public DimConfig(int dimension, boolean circle, int radius)
 		{
 			this.dimension = dimension;
 			this.typeCircle = circle;
 			this.radius = radius;
 			this.sqrDist = radius * radius;
 		}
 		
 		public boolean isWithinArea(Entity e)
 		{
 			if (typeCircle)
 				return (int)e.posX * (int)e.posX + (int)e.posZ * (int)e.posZ <= sqrDist;
 			else
 				return e.posX <= radius && e.posX >= -radius && e.posZ <= radius && e.posZ >= -radius;
 		}
 		
 		public static DimConfig deserialize(String line)
 		{
 			String[] prts = line.split(",");
 			
 			int dim = Integer.parseInt(prts[0].trim());
 			boolean circle = prts[1].trim().equalsIgnoreCase("circle");
 			int radius = Integer.parseInt(prts[2].trim());
 			
 			DimConfig c = new DimConfig(dim, circle, radius);
 			return c;
 		}
 	}
 
 	@Override
 	public void run() throws Exception
 	{
 		if (!genenabled || multithreaded)
 			return;
 		
 		for (ChunkGen g : generators)
 			g.runTick();
 	}
 
 	@Override
 	public String name() { return "WorldBorder Chunk Gen"; }
 }
