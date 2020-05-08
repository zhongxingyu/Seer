 package com.vloxlands.game.world;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.game.entity.Entity;
 import com.vloxlands.game.voxel.Voxel;
 import com.vloxlands.game.world.Chunk.ChunkKey;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.util.RenderAssistant;
 import com.vloxlands.util.math.AABB;
 import com.vloxlands.util.math.MathHelper;
 
 public class Island
 {
 	public static final int SIZE = 128;
 	public static final int SNOWLEVEL = 50;
 	public static final float SNOW_PER_TICK = 0.2f;
 	public static final float SNOW_INCREASE = 16;
 	
 	HashMap<ChunkKey, Chunk> chunks = new HashMap<>();
 	ArrayList<Entity> entities = new ArrayList<>();
 	
 	
 	Vector3f pos;
 	
 	public float weight, uplift, initBalance = 0;
 	
 	int renderedChunks = 0;
 	
 	public Island()
 	{
 		pos = new Vector3f(0, 0, 0);
 	}
 	
 	/**
 	 * Call from client!
 	 */
 	public void calculateInitBalance()
 	{
 		calculateWeight();
 		calculateUplift();
 		initBalance = (uplift * Map.calculateUplift(pos.y) - weight) / 100000f;
 	}
 	
 	public boolean isResourceAvailable(Voxel v)
 	{
 		for (Chunk c : chunks.values())
 		{
 			if (c.getResource(v) > 0) return true;
 		}
 		
 		return false;
 	}
 	
 	public void onTick()
 	{
 		float deltaY = (int) (((uplift * Map.calculateUplift(pos.y) - weight) / 100000f - initBalance) * 100f) / 100f;
 		pos.translate(0, deltaY, 0);
 		
 		for (Entity e : entities)
 		{
 			e.onTick();
 		}
 	}
 	
 	public void calculateWeight()
 	{
 		weight = 0;
 		for (Chunk c : chunks.values())
 		{
 			c.calculateWeight();
 			weight += c.weight;
 		}
 	}
 	
 	public void calculateUplift()
 	{
 		uplift = 0;
 		for (Chunk c : chunks.values())
 		{
 			c.calculateUplift();
 			uplift += c.uplift;
 		}
 	}
 	
 	// public void placeVoxel(int x, int y, int z, byte id)
 	// {
 	// placeVoxel(x, y, z, id, (byte) 0);
 	// }
 	//
 	// public void placeVoxel(int x, int y, int z, byte id, byte metadata)
 	// {
 	// voxels[x][y][z] = id;
 	// voxelMetadata[x][y][z] = metadata;
 	// Voxel.getVoxelForId(id).onPlaced(x, y, z);
 	//
 	// chunks[(int) Math.floor(x / (float) chunks.length)][(int) Math.floor(y / (float) chunks.length)][(int) Math.floor(z / (float) chunks.length)].updateMesh(this);
 	//
 	// weight += Voxel.getVoxelForId(id).getWeight();
 	// uplift += Voxel.getVoxelForId(id).getUplift();
 	// }
 	//
 	// public void removeVoxel(int x, int y, int z)
 	// {
 	// Voxel v = Voxel.getVoxelForId(getVoxelId(x, y, z));
 	// setVoxel(x, y, z, Voxel.get("AIR").getId());
 	// weight -= v.getWeight();
 	// uplift -= v.getUplift();
 	// }
 	
 	public byte getVoxelId(int x, int y, int z)
 	{
 		if (x >= SIZE || y >= SIZE || z >= SIZE || x < 0 || y < 0 || z < 0) return 0;
 		
 		ChunkKey cp = getChunkPosForVoxel(x, y, z);
 		
 		if (!chunks.containsKey(cp)) return Voxel.get("AIR").getId();
 		
 		return chunks.get(cp).getVoxelId(x - cp.x * Chunk.SIZE, y - cp.y * Chunk.SIZE, z - cp.z * Chunk.SIZE);
 	}
 	
 	public byte getMetadata(int x, int y, int z)
 	{
 		if (x >= SIZE || y >= SIZE || z >= SIZE || x < 0 || y < 0 || z < 0) return 0;
 		
 		ChunkKey cp = getChunkPosForVoxel(x, y, z);
 		
 		if (!chunks.containsKey(cp)) return 0;
 		
 		return chunks.get(cp).getMetadata(x - cp.x * Chunk.SIZE, y - cp.y * Chunk.SIZE, z - cp.z * Chunk.SIZE);
 	}
 	
 	public void setVoxel(int x, int y, int z, byte id)
 	{
 		if (x >= SIZE || y >= SIZE || z >= SIZE || x < 0 || y < 0 || z < 0) return;
 		
 		ChunkKey cp = getChunkPosForVoxel(x, y, z);
 		
 		initChunkIfNeeded(cp);
 		
 		chunks.get(cp).setVoxel(x - cp.x * Chunk.SIZE, y - cp.y * Chunk.SIZE, z - cp.z * Chunk.SIZE, id);
 	}
 	
 	public void setVoxel(int x, int y, int z, byte id, byte metadata)
 	{
 		if (x >= SIZE || y >= SIZE || z >= SIZE || x < 0 || y < 0 || z < 0) return;
 		
 		ChunkKey cp = getChunkPosForVoxel(x, y, z);
 		
 		initChunkIfNeeded(cp);
 		
 		chunks.get(cp).setVoxel(x - cp.x * Chunk.SIZE, y - cp.y * Chunk.SIZE, z - cp.z * Chunk.SIZE, id);
 		chunks.get(cp).setMetadata(x - cp.x * Chunk.SIZE, y - cp.y * Chunk.SIZE, z - cp.z * Chunk.SIZE, metadata);
 	}
 	
 	public void render()
 	{
 		if (!Game.frustum.sphereInFrustum(pos.x, pos.y, pos.z, SIZE * (float) Math.sqrt(2))) return;
 		
 		glTranslatef(pos.x, pos.y, pos.z);
 		
 		renderedChunks = 0;
 		
 		for (Chunk c : chunks.values())
 		{
 			if (c.render(this, true)) renderedChunks++;
 		}
 		
 		for (Entity e : entities)
 		{
 			glPushMatrix();
 			e.render();
 			glPopMatrix();
 		}
 		
 		for (Chunk c : chunks.values())
 		{
 			c.render(this, false);
 		}
 		
 		glDisable(GL_LIGHTING);
 		if (CFG.SHOW_CHUNK_BOUNDARIES)
 		{
 			glPushMatrix();
 			{
 				glLineWidth(1);
 				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
 				for (Chunk c : chunks.values())
 				{
 					if (c.getResource(Voxel.get("AIR")) == Math.pow(Chunk.SIZE, 3)) continue;
 					
 					Vector3f pos = new Vector3f(c.getX() * Chunk.SIZE, c.getY() * Chunk.SIZE, c.getZ() * Chunk.SIZE);
 					
 					glColor3f(1, 0, 0);
 					
 					float intersection = MathHelper.intersects(Game.pickingRay, new AABB(Vector3f.add(pos, this.pos, null), Chunk.SIZE, Chunk.SIZE, Chunk.SIZE));
 					
 					if (intersection != -1)
 					{
 						// CFG.p(intersection);
 						glColor3f(0, 1, 1);
 					}
 					
 					RenderAssistant.renderCuboid(pos.x, pos.y, pos.z, Chunk.SIZE, Chunk.SIZE, Chunk.SIZE);
 				}
 				
 				glColor3f(0, 0, 0);
 				RenderAssistant.renderCuboid(0, 0, 0, SIZE, SIZE, SIZE);
 				glColor3f(1, 1, 1);
 				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
 			}
 			glPopMatrix();
 		}
 		glEnable(GL_LIGHTING);
 		
 		glDisable(GL_TEXTURE_2D);
 	}
 	
 	public Vector3f getPos()
 	{
 		return pos;
 	}
 	
 	public void setPos(Vector3f pos)
 	{
 		this.pos = pos;
 	}
 	
 	public void addEntity(Entity e)
 	{
 		e.island = this;
 		entities.add(e);
 	}
 	
 	public Entity[] getEntities()
 	{
 		return entities.toArray(new Entity[] {});
 	}
 	
 	public int grassify()
 	{
 		int grassed = 0;
 		for (Chunk c : chunks.values())
 		{
 			grassed += c.grassify(this);
 		}
 		
 		return grassed;
 	}
 	
 	public int getHighestVoxel(int x, int z)
 	{
 		ChunkKey key = getChunkPosForVoxel(x, 0, z);
 		for (int i = SIZE / Chunk.SIZE - 1; i > -1; i--)
 		{
 			if (chunks.containsKey(new ChunkKey(key.x, i, key.z)))
 			{
 				return i * Chunk.SIZE + chunks.get(new ChunkKey(key.x, i, key.z)).getHighestVoxel(x - key.x * Chunk.SIZE, z - key.z * Chunk.SIZE);
 			}
 		}
 		
 		return -1;
 	}
 	
 	public Chunk getChunk(int x, int y, int z)
 	{
 		return getChunk(new ChunkKey(x, y, z));
 	}
 	
 	public Chunk getChunk(int index)
 	{
 		return new ArrayList<>(chunks.values()).get(index);
 	}
 	
 	public void addChunk(ChunkKey key, Chunk chunk)
 	{
 		if (chunks.containsKey(key)) return;
 		
 		chunks.put(key, chunk);
 	}
 	
 	public Chunk getChunk(ChunkKey v)
 	{
 		return chunks.get(v);
 	}
 	
 	public Collection<Chunk> getChunks()
 	{
 		return chunks.values();
 	}
 	
 	public ChunkKey getChunkPosForVoxel(int x, int y, int z)
 	{
 		return new ChunkKey((int) Math.floor(x / (float) Chunk.SIZE), (int) Math.floor(y / (float) Chunk.SIZE), (int) Math.floor(z / (float) Chunk.SIZE));
 	}
 	
 	public void initChunkIfNeeded(ChunkKey pos)
 	{
 		if (chunks.containsKey(pos)) return;
 		
 		chunks.put(pos, new Chunk(pos));
 	}
 	
 	public void removeEmptyChunks()
 	{
 		ArrayList<ChunkKey> removalKeys = new ArrayList<>();
 		for (ChunkKey key : chunks.keySet())
 		{
 			if (chunks.get(key).getResource(Voxel.get("AIR")) == (int) Math.pow(Chunk.SIZE, 3))
 			{
 				removalKeys.add(key);
 			}
 		}
 		
 		for (ChunkKey key : removalKeys)
 		{
 			chunks.remove(key);
 		}
 	}
 }
