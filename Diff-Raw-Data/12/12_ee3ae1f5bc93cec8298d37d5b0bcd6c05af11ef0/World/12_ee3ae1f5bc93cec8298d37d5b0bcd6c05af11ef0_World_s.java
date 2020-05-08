 package com.colonycraft.world;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.colonycraft.ColonyCraft;
 import com.colonycraft.Side;
 import com.colonycraft.math.MathHelper;
 import com.colonycraft.math.Vec3f;
 import com.colonycraft.math.Vec3i;
 import com.colonycraft.rendering.world.WorldRenderer;
 import com.colonycraft.world.blocks.BlockManager;
 import com.colonycraft.world.entities.Player;
 import com.colonycraft.world.generators.WorldGenerator;
 import com.colonycraft.world.physics.PhysicsManager;
 
 public class World implements EnvironmentManagerCallbacks
 {
 
 	public void loadTestEnvironment()
 	{
 //		Block dirt = BlockManager.getBlock("Dirt");
 //		Block grass = BlockManager.getBlock("Grass");
 //
 //		PerlinNoise n = new PerlinNoise(System.nanoTime());
 //		for (int x = -200; x < 200; ++x)
 //		{
 //			for (int z = -200; z < 200; ++z)
 //			{
 //				float no = n.noise(0.001f * z, 0.001f * x);
 //				int r = (int) (n.noise(0.02f * x, 0.02f * z) * 20.0f + Math.max(Math.min(Math.pow(0.5 + no, 4.0), 4 * no), 0.0) * 0.4f * 120.0f);
 //				for (int y = 0; y < r; ++y)
 //				{
 //					setBlockAt(x, y, z, dirt);
 //				}
 //				setBlockAt(x, r, z, grass);
 //			}
 //		}
 //		lightProcessor.spreadLightB(0, 4, 0, 15);
 //		lightProcessor.spreadLightB(30, 4, 30, 15);
 //		// lightProcessor.unspreadLightB(0, 4, 0, 15);
 //		
 		
 		WorldGenerator wg = new WorldGenerator(this, System.nanoTime());
 		for (int x = -5; x < 5; ++x)
 		{
 			for (int z = -5; z < 5; ++z)
 			{
 				wg.generateChunk(x, -3, z);
 				wg.generateChunk(x, -2, z);
 				wg.generateChunk(x, -1, z);
 				wg.generateChunk(x, 0, z);
 				wg.generateChunk(x, 1, z);
 				wg.generateChunk(x, 2, z);
 				wg.generateChunk(x, 3, z);
 			}
 		}
 		
 		/* Fill light! */
 		for (int x = -5 * 16; x < 5 * 16; ++x)
 		{
 			for (int z = -5 * 16; z < 5 * 16; ++z)
 			{
 				lightProcessor.fillSun(x, 47, z, 0);
 //				lightProcessor.fillSun(x, 31, z, 0);
 //				lightProcessor.fillSun(x, 15, z, 0);
 			}
 		}
 	}
 	
 	private float DAY_LENGTH = 300.0f;
 	private float DAY_LENGTH_INV = 1.0f / DAY_LENGTH;
 
 	private WorldRenderer renderer;
 	private LightProcessor lightProcessor;
 	private EnvironmentManager localEnvironmentManager;
 	private PhysicsManager physicsManager;
 	private BlockRayCastCalculator blockRayCastCalculator;
 	private Map<Integer, SuperChunk> superChunks;
 
 	private Player activePlayer;
 
 	private float time;
 	private float sunligth;
 	private float flicker;
 	private float viewingDistance;
 
 	public World()
 	{
 		viewingDistance = 256.0f;
 
 		
 		superChunks = new HashMap<Integer, SuperChunk>();
 		localEnvironmentManager = new EnvironmentManager(this);
 		localEnvironmentManager.setCallbacks(this);
 		physicsManager = new PhysicsManager(this);
 		activePlayer = new Player(this);
 		lightProcessor = new LightProcessor(this);
 		blockRayCastCalculator = new BlockRayCastCalculator();
 		renderer = new WorldRenderer(this);
 		
 
 		loadTestEnvironment();
 	}
 
 	public SuperChunk getSuperChunk(int x, int y, int z, boolean cin)
 	{
 		int index = MathHelper.mapToPositiveAndCantorize3(x, y, z);
 		SuperChunk schunk = superChunks.get(index);
 		if (cin && schunk == null)
 		{
 			schunk = new SuperChunk(this, x, y, z);
 			superChunks.put(index, schunk);
 		}
 		return schunk;
 	}
 
 	public Chunk getChunk(int x, int y, int z, boolean cin)
 	{
 		int superX = MathHelper.floorDivision(x, SuperChunk.CPSC_1D);
 		int superY = MathHelper.floorDivision(y, SuperChunk.CPSC_1D);
 		int superZ = MathHelper.floorDivision(z, SuperChunk.CPSC_1D);
 
 		int subX = x - superX * SuperChunk.CPSC_1D;
 		int subY = y - superY * SuperChunk.CPSC_1D;
 		int subZ = z - superZ * SuperChunk.CPSC_1D;
 
 		SuperChunk schunk = getSuperChunk(superX, superY, superZ, cin);
 		if (schunk == null)
 			return null;
 		return schunk.getChunk(subX, subY, subZ, cin);
 	}
 
 	public void render()
 	{
 		renderer.render();
 	}
 
 	public void update()
 	{
 		activePlayer.update();
 		localEnvironmentManager.manage(new Vec3f(activePlayer.getTransform().origin), viewingDistance, activePlayer.getCamera().getFrustum());
 		physicsManager.update();
 		sunligth += ColonyCraft.getIntance().getStep() * MathHelper.f_2PI * DAY_LENGTH_INV;
		flicker += 0.02f;
 		time += ColonyCraft.getIntance().getStep();
 	}
 
 	/* Chunk selections */
 
 	public Chunk getChunkContaining(int x, int y, int z, boolean cin)
 	{
 		int cx = MathHelper.floorDivision(x, Chunk.BPC_1D);
 		int cy = MathHelper.floorDivision(y, Chunk.BPC_1D);
 		int cz = MathHelper.floorDivision(z, Chunk.BPC_1D);
 		return getChunk(cx, cy, cz, cin);
 	}
 
 	/* World operations */
 
 	public void setBlockAt(int x, int y, int z, Block b)
 	{
 		Chunk ch = getChunkContaining(x, y, z, true);
 		int newData = b.getType();
 		int oldData = ch.setBlockAbs(x, y, z, newData);
 		Vec3i worldPosChunk = ch.getWorldPos();
 		ch.getVisibleBlocks().removeValue(ChunkData.offsetForPos(x - worldPosChunk.x, y - worldPosChunk.y, z - worldPosChunk.z));
 
 		// Notify neighbors
 		Side[] sides = Side.getSides();
 		for (int i = 0; i < 6; ++i)
 		{
 			Vec3i n = sides[i].getNormal();
 			neighborChanged(x + n.x, y + n.y, z + n.z);
 		}
 
 		/* Update the modified block */
 		changed(x, y, z, oldData, ch.getBlockAbs(x, y, z));
 
 	}
 
 	public int getBlockAt(int x, int y, int z, boolean cin)
 	{
 		Chunk ch = getChunkContaining(x, y, z, cin);
 		if (ch == null)
 			return -1;
 		return ch.getBlockAbs(x, y, z);
 	}
 
 	public void setLightAt(int x, int y, int z, int l)
 	{
 		Chunk ch = getChunkContaining(x, y, z, false);
 		if (ch == null)
 			return;
 		ch.setLightAbs(x, y, z, l);
 
 		ch.setLightDirty();
 		ch.setNeighborsLightDirty();
 	}
 
 	public byte getLightAt(int x, int y, int z, boolean cin)
 	{
 		Chunk ch = getChunkContaining(x, y, z, cin);
 		if (ch == null)
 			return (byte) 0xF0;// -1;
 		return ch.getLightAbs(x, y, z);
 	}
 
 	public void changed(int x, int y, int z, int olddata, int newdata)
 	{
 		updateVisibilityAndPhysics(x, y, z);
 
 		Chunk ch = getChunkContaining(x, y, z, false);
 
 		/* If the chunk isn't loading, then update the light */
 		if (!ch.isLoading())
 		{
 			Block oldblock = BlockManager.getBlock(ChunkData.getType(olddata));
 			Block newblock = BlockManager.getBlock(ChunkData.getType(newdata));
 			if (oldblock.lightPasses() ^ newblock.lightPasses())
 			{
 				Side[] sides = Side.getSides();
 				if (newblock.lightPasses())
 				{
 					for (int i = 0; i < 6; ++i)
 					{
 						Vec3i n = sides[i].getNormal();
 						lightProcessor.respreadLightB(x + n.x, y + n.y, z + n.z);
 						lightProcessor.respreadLightS(x + n.x, y + n.y, z + n.z);
 					}
 				} else
 				{
 					lightProcessor.unfillSun(x, y + 1, z, false);
 					lightProcessor.unspreadLightB(x, y, z);
 					lightProcessor.unspreadLightS(x, y, z);
 				}
 			}
 		}
 
 	}
 
 	public void neighborChanged(int x, int y, int z)
 	{
 		updateVisibilityAndPhysics(x, y, z);
 	}
 
 	public void updateVisibilityAndPhysics(int x, int y, int z)
 	{
 		/* Check visibility */
 		Side[] sides = Side.getSides();
 		Chunk thisChunk = getChunkContaining(x, y, z, false);
 		if (thisChunk == null)
 		{
 			return;
 		}
 		int thisBlockData = thisChunk.getBlockAbs(x, y, z);
 		Block thisBlock = BlockManager.getBlock(ChunkData.getType(thisBlockData));
 		if (!thisBlock.renders())
 			return;
 		int faces = thisBlock.getAlwaysVisibleFaces();
 		for (int i = 0; i < 6; ++i)
 		{
 			if (((faces >>> i) & 1) == 0)
 			{
 				Vec3i n = sides[i].getNormal();
 				int blockdata = getBlockAt(x + n.x, y + n.y, z + n.z, false);
 				if (blockdata == -1)
 				{
 					{
 						faces |= 1 << i;
 					}
 					continue;
 				}
 				Block b = BlockManager.getBlock(ChunkData.getType(blockdata));
 				int bf = b.getAlwaysVisibleFaces();
 				if (!b.renders() || b.hasAlpha() || (((bf >>> (i ^ 1)) & 1) == 1))
 				{
 					faces |= 1 << i;
 				}
 			}
 		}
 
 		int oldFaces = ChunkData.getFaceMask(thisBlockData);
 		if (oldFaces != faces)
 		{
 			thisBlockData = ChunkData.setFaceMask(thisBlockData, faces);
 			Vec3i worldPosChunk = thisChunk.getWorldPos();
 			if (oldFaces == 0)
 			{
 				thisChunk.getVisibleBlocks().add(ChunkData.offsetForPos(x - worldPosChunk.x, y - worldPosChunk.y, z - worldPosChunk.z));
 			} else if (faces == 0)
 			{
 				thisChunk.getVisibleBlocks().removeValue(ChunkData.offsetForPos(x - worldPosChunk.x, y - worldPosChunk.y, z - worldPosChunk.z));
 			}
 			thisChunk.setBlockAbs(x, y, z, thisBlockData);
 
 			thisChunk.setMeshDirty();
 			thisChunk.setBodyDirty();
 		} else if (faces != 0)
 		{
 			thisChunk.setMeshDirty();
 			thisChunk.setBodyDirty();
 		}
 
 	}
 
 	public void updateVisiblityForAllBlocksOnChunkSide(Chunk chunk, Side side)
 	{
 		boolean varX;
 		int j;
 
 		Vec3i normal = side.getNormal();
 
 		if (normal.x == 1)
 		{
 			j = Chunk.BPC_1D - 1;
 			varX = false;
 		} else if (normal.x == -1)
 		{
 			j = 0;
 			varX = false;
 		} else if (normal.z == 1)
 		{
 			j = Chunk.BPC_1D - 1;
 			varX = true;
 		} else if (normal.z == -1)
 		{
 			j = 0;
 			varX = true;
 		} else
 		{
 			return;
 		}
 
 		Vec3i worldPos = chunk.getWorldPos();
 		for (int i = 0; i < Chunk.BPC_1D; ++i)
 		{
 			for (int y = 0; y < Chunk.BPC_1D; ++y)
 			{
 				if (varX)
 				{
 					updateVisibilityAndPhysics(worldPos.x + i, worldPos.y + y, worldPos.z + j);
 				} else
 				{
 					updateVisibilityAndPhysics(worldPos.x + j, worldPos.y + y, worldPos.z + i);
 				}
 			}
 		}
 	}
 
 	public Player getActivePlayer()
 	{
 		return activePlayer;
 	}
 
 	public PhysicsManager getPhysicsManager()
 	{
 		return physicsManager;
 	}
 
 	@Override
 	public void unloadChunk(Chunk oldChunk)
 	{
 		System.out.println("Unload chunk + " + oldChunk.getPos());
 		physicsManager.removeChunk(oldChunk);
 		oldChunk.releaseMesh();
 	}
 
 	@Override
 	public void loadChunk(Chunk chunk)
 	{
 		System.out.println("Load chunk + " + chunk.getPos());
 		physicsManager.addChunk(chunk);
 		chunk.setLoading(false);
 	}
 
 	public float getSunligth()
 	{
 		return 0.05f + 0.95f * (0.5f + 0.5f * MathHelper.sin(sunligth));
 	}
 
 	public float getFlicker()
 	{
 		return (1.0f + MathHelper.sin(flicker + MathHelper.sin(5.0f * flicker))) * 0.04f;
 	}
 
 	public BlockRayCastCalculator getBlockRayCastCalculator()
 	{
 		return blockRayCastCalculator;
 	}
 
 	public EnvironmentManager getLocalEnvironmentManager()
 	{
 		return localEnvironmentManager;
 	}
 
 	public float getViewingDistance()
 	{
 		return viewingDistance;
 	}
 
 	public LightProcessor getLightProcessor()
 	{
 		return lightProcessor;
 	}
 
 	public float getTime()
 	{
 		return time;
 	}
 }
