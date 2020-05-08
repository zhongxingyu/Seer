 package com.vloxlands.game.world;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.Arrays;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.voxel.Voxel;
 import com.vloxlands.settings.CFG;
 
 public class Island
 {
 	public static final int SIZE = 128;
 	public static final int CHUNKSIZE = 8;
 	
 	public static final int SNOWLEVEL = 50;
 	public static final float SNOW_PER_TICK = 0.2f;
 	public static final float SNOW_INCREASE = 16;
 	
 	byte[][][] voxels = new byte[SIZE][SIZE][SIZE];
 	byte[][][] voxelMetadata = new byte[SIZE][SIZE][SIZE];
 	public Chunk[][][] chunks = new Chunk[SIZE / CHUNKSIZE][SIZE / CHUNKSIZE][SIZE / CHUNKSIZE];
 	
 	Vector3f smallestNonAirVoxel, biggestNonAirVoxel;
 	
 	/**
 	 * Keeps track of which resources can be found on this Island
 	 */
 	boolean[] resources = new boolean[Voxel.VOXELS];
 	
 	Vector3f pos;
 	
 	public float weight, uplift, initBalance = 0;
 	
 	public Island()
 	{
 		pos = new Vector3f(0, 0, 0);
 		for (int i = 0; i < SIZE; i++)
 		{
 			for (int j = 0; j < SIZE; j++)
 			{
 				for (int k = 0; k < SIZE; k++)
 				{
 					voxels[i][j][k] = Voxel.get("AIR").getId();
 					voxelMetadata[i][j][k] = -128;
 					if (i % CHUNKSIZE == 0 && j % CHUNKSIZE == 0 && k % CHUNKSIZE == 0)
 					{
 						chunks[i / CHUNKSIZE][j / CHUNKSIZE][k / CHUNKSIZE] = new Chunk(i / CHUNKSIZE, j / CHUNKSIZE, k / CHUNKSIZE);
 					}
 				}
 			}
 		}
 		for (int i = 0; i < resources.length; i++)
 			resources[i] = false;
 		
 		setResourceAvailable(Voxel.get("AIR"), true);
 	}
 	
 	public void setResourceAvailable(Voxel v, boolean avail)
 	{
 		resources[v.getId() + 128] = avail;
 	}
 	
 	public boolean isResourceAvailable(Voxel v)
 	{
 		return resources[v.getId() + 128];
 	}
 	
 	@Override
 	public Island clone()
 	{
 		Island island = new Island();
 		for (int i = 0; i < SIZE; i++)
 		{
 			for (int j = 0; j < SIZE; j++)
 			{
 				island.voxels[i][j] = Arrays.copyOf(voxels[i][j], SIZE);
 			}
 		}
 		for (int i = 0; i < SIZE; i++)
 		{
 			for (int j = 0; j < SIZE; j++)
 			{
 				island.voxelMetadata[i][j] = Arrays.copyOf(voxelMetadata[i][j], SIZE);
 			}
 		}
 		return island;
 	}
 	
 	public void onTick()
 	{
 		pos.translate(0, (int) (((uplift * Map.calculateUplift(pos.y) - weight) / 100000f - initBalance) * 100f) / 100f, 0);
 	}
 	
 	public void calculateWeight()
 	{
 		weight = 0;
 		for (int x = 0; x < 256; x++)
 		{
 			for (int y = 0; y < 256; y++)
 			{
 				for (int z = 0; z < 256; z++)
 				{
 					if (getVoxelId(x, y, z) == 0) continue;
 					weight += Voxel.getVoxelForId(getVoxelId(x, y, z)).getWeight();
 				}
 			}
 		}
 	}
 	
 	public void calculateUplift()
 	{
 		uplift = 0;
 		for (int x = 0; x < 256; x++)
 		{
 			for (int y = 0; y < 256; y++)
 			{
 				for (int z = 0; z < 256; z++)
 				{
 					if (getVoxelId(x, y, z) == 0) continue;
 					uplift += Voxel.getVoxelForId(getVoxelId(x, y, z)).getUplift();
 				}
 			}
 		}
 	}
 	
 	public void placeVoxel(int x, int y, int z, byte id)
 	{
 		placeVoxel(x, y, z, id, (byte) 0);
 	}
 	
 	public void placeVoxel(int x, int y, int z, byte id, byte metadata)
 	{
 		voxels[x][y][z] = id;
 		voxelMetadata[x][y][z] = metadata;
 		Voxel.getVoxelForId(id).onPlaced(x, y, z);
 		
 		chunks[(int) Math.floor(x / (float) chunks.length)][(int) Math.floor(y / (float) chunks.length)][(int) Math.floor(z / (float) chunks.length)].updateMesh(this);
 		
 		weight += Voxel.getVoxelForId(id).getWeight();
 		uplift += Voxel.getVoxelForId(id).getUplift();
 	}
 	
 	public void removeVoxel(int x, int y, int z)
 	{
 		Voxel v = Voxel.getVoxelForId(getVoxelId(x, y, z));
 		setVoxel(x, y, z, Voxel.get("AIR").getId());
 		weight -= v.getWeight();
 		uplift -= v.getUplift();
 	}
 	
 	public byte getVoxelId(int x, int y, int z)
 	{
 		if (x >= Island.SIZE || y >= Island.SIZE || z >= Island.SIZE || x < 0 || y < 0 || z < 0) return 0;
 		return voxels[x][y][z];
 	}
 	
 	public byte getMetadata(int x, int y, int z)
 	{
 		return voxelMetadata[x][y][z];
 	}
 	
 	public void setVoxel(int x, int y, int z, byte id)
 	{
 		if (x >= Island.SIZE || y >= Island.SIZE || z >= Island.SIZE || x < 0 || y < 0 || z < 0) return;
 		setResourceAvailable(Voxel.getVoxelForId(id), true);
 		
 		if (id != Voxel.get("AIR").getId())
 		{
 			chunks[(int) (x / (float) CHUNKSIZE)][(int) (y / (float) CHUNKSIZE)][(int) (z / (float) CHUNKSIZE)].addResource(Voxel.getVoxelForId(voxels[x][y][z]), -1);
 			chunks[(int) (x / (float) CHUNKSIZE)][(int) (y / (float) CHUNKSIZE)][(int) (z / (float) CHUNKSIZE)].addResource(Voxel.getVoxelForId(id), 1);
 		}
 		voxels[x][y][z] = id;
 		
 		if (id != Voxel.get("AIR").getId())
 		{
 			if (smallestNonAirVoxel == null || (x < smallestNonAirVoxel.x && y < smallestNonAirVoxel.y && z < smallestNonAirVoxel.z))
 			{
 				smallestNonAirVoxel = new Vector3f(x, y, z);
 			}
 			
 			if (biggestNonAirVoxel == null || (x > biggestNonAirVoxel.x && y > biggestNonAirVoxel.y && z > biggestNonAirVoxel.z))
 			{
 				biggestNonAirVoxel = new Vector3f(x, y, z);
 			}
 		}
 	}
 	
 	public void setVoxel(int x, int y, int z, byte id, byte metadata)
 	{
 		setVoxel(x, y, z, id);
 		setVoxelMetadata(x, y, z, metadata);
 	}
 	
 	public void setVoxelMetadata(int x, int y, int z, byte metadata)
 	{
 		voxelMetadata[x][y][z] = metadata;
 	}
 	
 	public byte[] getVoxels()
 	{
 		byte[] bytes = new byte[(int) Math.pow(SIZE, 3)];
 		for (int i = 0; i < SIZE; i++)
 		{
 			for (int j = 0; j < SIZE; j++)
 			{
 				for (int k = 0; k < SIZE; k++)
 				{
 					bytes[(i * SIZE + j) * SIZE + k] = voxels[i][j][k];
 				}
 			}
 		}
 		return bytes;
 	}
 	
 	public byte[] getVoxelMetadatas()
 	{
 		byte[] bytes = new byte[(int) Math.pow(SIZE, 3)];
 		for (int i = 0; i < SIZE; i++)
 		{
 			for (int j = 0; j < SIZE; j++)
 			{
 				for (int k = 0; k < SIZE; k++)
 				{
 					bytes[(i * SIZE + j) * SIZE + k] = voxelMetadata[i][j][k];
 				}
 			}
 		}
 		return bytes;
 	}
 	
 	public void render()
 	{
 		glTranslatef(pos.x, pos.y, pos.z);
 		
 		int s = SIZE / CHUNKSIZE;
 		for (int i = 0; i < s; i++)
 		{
 			for (int j = 0; j < s; j++)
 			{
 				for (int k = 0; k < s; k++)
 				{
 					chunks[i][j][k].render(this, true);
 				}
 			}
 		}
 		for (int i = 0; i < s; i++)
 		{
 			for (int j = 0; j < s; j++)
 			{
 				for (int k = 0; k < s; k++)
 				{
 					chunks[i][j][k].render(this, false);
 				}
 			}
 		}
 		
 		if (CFG.SHOW_CHUNK_BOUNDRIES)
 		{
 			for (int x = 0; x < Island.SIZE; x += Island.CHUNKSIZE)
 			{
 				for (int y = 0; y < Island.SIZE; y += Island.CHUNKSIZE)
 				{
 					for (int z = 0; z < Island.SIZE; z += Island.CHUNKSIZE)
 					{
 						glPushMatrix();
 						{
 							glLineWidth(1);
 							glColor3d(1, 0, 0);
 							glBegin(GL_LINES);
 							{
 								glVertex3d(x, y, z);
 								glVertex3d(x, y + Island.CHUNKSIZE, z);
 								glVertex3d(x, y, z);
 								glVertex3d(x, y, z + Island.CHUNKSIZE);
 								glVertex3d(x, y, z);
 								glVertex3d(x + Island.CHUNKSIZE, y, z);
 							}
 							glEnd();
 							glColor3d(1, 1, 1);
 							glLineWidth(1);
 						}
 						glPopMatrix();
 					}
 				}
 			}
 		}
 	}
 	
 	public Vector3f getPos()
 	{
 		return pos;
 	}
 	
 	public void setPos(Vector3f pos)
 	{
 		this.pos = pos;
 	}
 	
 	public int grassify()
 	{
 		int grassed = 0;
 		for (int i = 0; i < SIZE; i++)
 		{
 			for (int j = 0; j < SIZE; j++)
 			{
 				for (int k = 0; k < SIZE; k++)
 				{
 					if (getVoxelId(i, j, k) == Voxel.get("DIRT").getId())
 					{
 						if (getVoxelId(i, j + 1, k) == Voxel.get("AIR").getId())
 						{
 							grassed++;
 							setVoxel(i, j, k, Voxel.get("GRASS").getId());
 						}
 					}
 				}
 			}
 		}
 		
 		return grassed;
 	}
 	
 	public int getHighestVoxel(int x, int z)
 	{
 		for (int i = SIZE - 1; i > -1; i--)
 		{
 			if (voxels[x][i][z] != Voxel.get("AIR").getId()) return i;
 		}
 		
 		return -1;
 	}
 	
 	public Chunk getChunk(int x, int y, int z)
 	{
		try
		{
			
			return chunks[x][y][z];
		}
		catch (Exception e)
		{
			CFG.p(x, y, z);
			return null;
		}
 	}
 	
 	public Vector3f getSmallestNonAirVoxel()
 	{
 		return smallestNonAirVoxel;
 	}
 	
 	public Vector3f getBiggestNonAirVoxel()
 	{
 		return biggestNonAirVoxel;
 	}
 }
