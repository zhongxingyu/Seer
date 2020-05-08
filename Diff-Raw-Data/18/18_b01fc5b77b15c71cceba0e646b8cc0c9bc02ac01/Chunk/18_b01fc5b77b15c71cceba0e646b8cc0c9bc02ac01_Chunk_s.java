 package com.colonycraft.world;
 
 import com.bulletphysics.dynamics.RigidBody;
 import com.colonycraft.Side;
 import com.colonycraft.math.AABB;
 import com.colonycraft.math.MathHelper;
 import com.colonycraft.math.Vec3f;
 import com.colonycraft.math.Vec3i;
 import com.colonycraft.rendering.world.ChunkMesh;
 import com.colonycraft.rendering.world.ChunkMeshBuilder;
 import com.colonycraft.utilities.IntList;
 
 public class Chunk
 {
 
 	/**
 	 * Blocks per Chunk in one dimension
 	 */
 	public static final int BPC_1D = 16;
 	public static final int BPC_2D = BPC_1D * BPC_1D;
 	public static final int BPC_3D = BPC_1D * BPC_1D * BPC_1D;
 
 	private static final Vec3f HALF_CHUNK_SIZE = new Vec3f(BPC_1D, BPC_1D, BPC_1D).scale(0.5f);
 
 	private World world;
 
 	private ChunkData data;
 	private IntList visibleBlocks;
 
 	private ChunkMesh mesh;
 	private boolean meshDirty;
 	private boolean lightDirty;
 
 	private RigidBody body;
 	private boolean bodyDirty;
 
 	private Vec3i pos;
 	private Vec3i worldPos;
 	private int positionIdentifier;
 	private AABB aabb;
 	private AABB visibleContentAABB;
 	private AABB contentAABBsPerMesh[];
 
 	private boolean loading;
 
 	public Chunk(World world, int x, int y, int z)
 	{
 		this.world = world;
 
 		data = new ChunkData();
 		visibleBlocks = new IntList(BPC_2D);
 
 		pos = new Vec3i(x, y, z);
 		worldPos = new Vec3i(x * BPC_1D, y * BPC_1D, z * BPC_1D);
 
 		aabb = new AABB(new Vec3f(worldPos).add(HALF_CHUNK_SIZE), HALF_CHUNK_SIZE);
 		visibleContentAABB = aabb;
 		contentAABBsPerMesh = new AABB[ChunkMesh.MESHES];
 
 		positionIdentifier = MathHelper.mapToPositiveAndCantorize3(worldPos.x, worldPos.y, worldPos.z);
 
 		mesh = null;
 		meshDirty = true;
 		lightDirty = true;
 		bodyDirty = true;
 
 		loading = true;
 	}
 
 	/* Block */
 
 	public int setBlockAbs(int x, int y, int z, int bdata)
 	{
 		return setBlockRel(x - worldPos.x, y - worldPos.y, z - worldPos.z, bdata);
 	}
 
 	public int setBlockRel(int x, int y, int z, int bdata)
 	{
 		return data.setBlockData(x, y, z, bdata);
 	}
 
 	public int getBlockAbs(int x, int y, int z)
 	{
 		return getBlockRel(x - worldPos.x, y - worldPos.y, z - worldPos.z);
 	}
 
 	public int getBlockRel(int x, int y, int z)
 	{
 		return data.getBlockData(x, y, z);
 	}
 
 	/* Light */
 
 	public byte getLightAbs(int x, int y, int z)
 	{
 		return getLightRel(x - worldPos.x, y - worldPos.y, z - worldPos.z);
 	}
 
 	public byte getLightRel(int x, int y, int z)
 	{
 		return data.getLight(x, y, z);
 	}
 
 	public void setLightAbs(int x, int y, int z, int l)
 	{
 		setLightRel(x - worldPos.x, y - worldPos.y, z - worldPos.z, l);
 	}
 
 	public void setLightRel(int x, int y, int z, int l)
 	{
 		data.setLight(x, y, z, l);
 	}
 
 	public ChunkData getData()
 	{
 		return data;
 	}
 
 	public ChunkMesh getMesh()
 	{
 		return mesh;
 	}
 
 	public boolean isMeshDirty()
 	{
 		return meshDirty;
 	}
 
 	public void setMeshClean()
 	{
 		meshDirty = false;
 	}
 
 	public void setMeshDirty()
 	{
 		meshDirty = true;
 		/*
		 * If the chunk got empty, make sure to set it's aabb back to the full chunk,
		 * so that when the first change occurs that makes the chunk visible again,
		 * the world renderer will catch the empty chunk and allow it to rebuild it's
		 * mesh
 		 */
		
		if (visibleBlocks.size() == 0)
		{
			restoreFullAABB();
		}
 	}
 
 	public boolean isLightDirty()
 	{
 		return lightDirty;
 	}
 
 	public void setLightDirty()
 	{
 		lightDirty = true;
 		setMeshDirty();
 	}
 
 	public void setLightClean()
 	{
 		lightDirty = false;
 	}
 
 	public boolean isBodyDirty()
 	{
 		return bodyDirty;
 	}
 
 	public void setBodyDirty()
 	{
 		this.bodyDirty = true;
 	}
 
 	public void setBodyClean()
 	{
 		this.bodyDirty = false;
 	}
 
 	public void setNeighborsLightDirty()
 	{
 		Side[] s = Side.getSides();
 		for (int i = 0; i < 6; ++i)
 		{
 			Vec3i normal = s[i].getNormal();
 			Chunk c = world.getChunk(pos.x + normal.x, pos.y + normal.y, pos.z + normal.z, false);
 			if (c == null)
 				continue;
 			c.setLightDirty();
 		}
 	}
 
 	public IntList getVisibleBlocks()
 	{
 		return visibleBlocks;
 	}
 
 	public Vec3i getPos()
 	{
 		return pos;
 	}
 
 	public Vec3i getWorldPos()
 	{
 		return worldPos;
 	}
 
 	public int getPositionIdentifier()
 	{
 		return positionIdentifier;
 	}
 
 	public AABB getAABB()
 	{
 		return aabb;
 	}
 
 	public void setContentAABBforMesh(AABB aabb, int mesh)
 	{
 		contentAABBsPerMesh[mesh] = aabb;
 		rebuildVisibleContentAABB();
 	}
 
 	public void rebuildVisibleContentAABB()
 	{
 		visibleContentAABB = null;
 		for (int i = 1; i < ChunkMesh.MESHES; ++i)
 		{
 			AABB aabb = contentAABBsPerMesh[i];
 			if (aabb != null)
 			{
 				if (visibleContentAABB == null)
 				{
 					visibleContentAABB = new AABB(aabb);
 				} else
 				{
 					visibleContentAABB.include(aabb);
 				}
 			}
 		}
 	}
 
 	public void setVisibleContantAABB(AABB aabb)
 	{
 		visibleContentAABB = aabb;
 	}
 
 	public AABB getVisibleContentAABB()
 	{
 		return visibleContentAABB;
 	}
 
 	public void restoreFullAABB()
 	{
 		visibleContentAABB = aabb;
 	}
	
 	public void rebuildMesh()
 	{
 		releaseMesh();
 		mesh = new ChunkMesh();
 		ChunkMeshBuilder cmb = new ChunkMeshBuilder(world);
 		cmb.buildMesh(mesh, this, ChunkMesh.MESH_OPAQUE);
 		cmb.buildMesh(mesh, this, ChunkMesh.MESH_GRASS);
 		setMeshClean();
 	}
 
 	public void releaseMesh()
 	{
 		if (mesh != null)
 			mesh.release();
 		mesh = null;
 		setMeshDirty();
 	}
 
 	public void setBody(RigidBody body)
 	{
 		this.body = body;
 	}
 
 	public RigidBody getBody()
 	{
 		return body;
 	}
 
 	public boolean isLoading()
 	{
 		return loading;
 	}
 
 	public void setLoading(boolean loading)
 	{
 		this.loading = loading;
 	}
 
 }
