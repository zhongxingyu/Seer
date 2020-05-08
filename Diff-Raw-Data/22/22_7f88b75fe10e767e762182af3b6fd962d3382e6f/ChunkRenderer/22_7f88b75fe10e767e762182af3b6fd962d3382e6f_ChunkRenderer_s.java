 package com.vloxlands.render;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.voxel.Voxel;
 import com.vloxlands.game.world.Island;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.util.Direction;
 
 public class ChunkRenderer
 {
 	public static void renderChunk(int listIndex, int index, Island island)
 	{
 		int cs = Island.SIZE / Island.CHUNKSIZE;
 		int cx = index / (cs * cs);
 		int cy = index / cs % cs;
 		int cz = index % cs;
 		
 		HashMap<ArrayList<Integer>, VoxelFace>[] faceLists = generateFaces(cx, cy, cz, island);
 		
 		// HashMap<ArrayList<Integer>, VoxelFace> greedy0 = genereateGreedyMesh(cx, cy, cz, faceLists[0]);
 		// HashMap<ArrayList<Integer>, VoxelFace> greedy1 = genereateGreedyMesh(cx, cy, cz, faceLists[1]);
 		// if (greedy0.size() > 0) CFG.p(greedy0.size());
 		
 		glPushMatrix();
 		glNewList(listIndex, GL_COMPILE);
 		// for (VoxelFace v : greedy0.values())
 		// v.render();
 		for (VoxelFace v : faceLists[0].values())
 			v.render();
 		glEndList();
 		glPopMatrix();
 		
 		glPushMatrix();
 		glNewList(listIndex + 1, GL_COMPILE);
 		// for (VoxelFace v : greedy1.values())
 		// v.render();
 		for (VoxelFace v : faceLists[1].values())
 			v.render();
 		glEndList();
 		glPopMatrix();
 	}
 	
 	public static void initChunks(Island island)
 	{
 		island.chunk0ID = glGenLists(island.chunks[0].capacity() * 2);
 		for (int i = 0; i < island.chunks[0].capacity(); i++)
 		{
 			renderChunk(island.chunk0ID + i * 2, i, island);
 			island.chunks[0].put(i);
 			island.chunks[1].put(i);
 		}
 		
 		island.chunks[0].flip();
 		island.chunks[1].flip();
 		CFG.p("[ChunkRenderer]: Initialized chunks on Island " + island);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static HashMap<ArrayList<Integer>, VoxelFace>[] generateFaces(int cx, int cy, int cz, Island i)
 	{
 		HashMap<ArrayList<Integer>, VoxelFace> faces = new HashMap<>();
 		HashMap<ArrayList<Integer>, VoxelFace> transparentFaces = new HashMap<>();
 		for (int x = 0; x < Island.CHUNKSIZE; x++)
 		{
 			for (int y = 0; y < Island.CHUNKSIZE; y++)
 			{
 				for (int z = 0; z < Island.CHUNKSIZE; z++)
 				{
 					int posX = cx * Island.CHUNKSIZE + x;
 					int posY = cy * Island.CHUNKSIZE + y;
 					int posZ = cz * Island.CHUNKSIZE + z;
 					
 					if (i.getVoxelId(posX, posY, posZ) == Voxel.get("AIR").getId()) continue;
 					Voxel v = Voxel.getVoxelForId(i.getVoxelId(posX, posY, posZ));
 					for (Direction d : Direction.values())
 					{
 						Voxel w = Voxel.getVoxelForId(i.getVoxelId(posX + (int) d.dir.x, posY + (int) d.dir.y, posZ + (int) d.dir.z));
 						if (!w.isOpaque() && !(w == v))
 						{
 							VoxelFace f = new VoxelFace(d, new Vector3f(posX, posY, posZ), v.getTextureIndex());
 							if (v.isOpaque()) faces.put(getVoxelFaceKey(x, y, z, d.ordinal()), f);
 							else transparentFaces.put(getVoxelFaceKey(x, y, z, d.ordinal()), f);
 						}
 					}
 				}
 			}
 		}
 		return new HashMap[] { faces, transparentFaces };
 	}
 	
 	private static HashMap<ArrayList<Integer>, VoxelFace> genereateGreedyMesh(int cx, int cy, int cz, HashMap<ArrayList<Integer>, VoxelFace> originalMap)
 	{
 		HashMap<ArrayList<Integer>, VoxelFace> strips = new HashMap<>();
 		long time = System.currentTimeMillis();
 		for (Direction d : Direction.values())
 		{
 			// if (d.dir.x != 0) continue;
 			
 			// greedy-mode along Z - axis
 			for (int y = 0; y < Island.CHUNKSIZE; y++)
 			{
 				for (int x = 0; x < Island.CHUNKSIZE; x++)
 				{
 					VoxelFace activeStrip = null;
 					for (int z = 0; z < Island.CHUNKSIZE; z++)
 					{
 						if (activeStrip != null)
 						{
 							if (!originalMap.containsKey(getVoxelFaceKey(x, y, z, d.ordinal())))
 							{
 								strips.put(getVoxelFaceKey(activeStrip), activeStrip);
 								activeStrip = null;
 							}
 							else if (originalMap.get(getVoxelFaceKey(x, y, z, d.ordinal())).textureIndex == activeStrip.textureIndex)
 							{
 								activeStrip.increaseSize(0, 0, 1);
 							}
 							else
 							{
 								strips.put(getVoxelFaceKey(activeStrip), activeStrip);
 								activeStrip = null;
 							}
 						}
 						else if (originalMap.containsKey(getVoxelFaceKey(x, y, z, d.ordinal())))
 						{
							activeStrip = new VoxelFace(d, new Vector3f(x, y, z), originalMap.get(getVoxelFaceKey(x, y, z, d.ordinal())).textureIndex);
 						}
 					}
 					
 					if (activeStrip != null) strips.put(getVoxelFaceKey(activeStrip), activeStrip);
 				}
 			}
 		}
 		
 		CFG.p("[ChunkRenderer]: Greedy meshing took " + (System.currentTimeMillis() - time) + "ms");
 		
 		return strips;
 	}
 	
 	private static ArrayList<Integer> getVoxelFaceKey(int x, int y, int z, int d)
 	{
 		ArrayList<Integer> l = new ArrayList<Integer>();
 		l.add(x);
 		l.add(y);
 		l.add(z);
 		l.add(d);
 		return l;
 	}
 	
 	private static ArrayList<Integer> getVoxelFaceKey(VoxelFace face)
 	{
 		ArrayList<Integer> l = new ArrayList<Integer>();
 		l.add((int) face.pos.x);
 		l.add((int) face.pos.y);
 		l.add((int) face.pos.x);
 		l.add(face.dir.ordinal());
 		return l;
 	}
 }
