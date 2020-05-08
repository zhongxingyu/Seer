 package minebot.world;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 public final class World {
 	
 	public final class Chunk {
 		public int cx, cz;
 		public byte[] blocks;
 		
 		public Chunk(int cx, int cz, byte[] blocks) {
 			this.cx = cx;
 			this.cz = cz;
 			this.blocks = blocks;
 			if (blocks.length != 16*16*128) {
 				System.out.println("Some sort of bug with creation of chunks");
 			}
 		}
 		
 		public void setBlock(int x, int y, int z, int type) {
 			blocks[(x&15)*128*16 + (z&15)*128 + (y&127)] = (byte)type;
 		}
 		
 		public int getBlock(int x, int y, int z) {
 			return blocks[(x&15)*128*16 + (z&15)*128 + (y&127)]; 
 		}
 	}
 	
 	public static int GetKey(int cx, int cz) {
 		return cx + (cz << 16);
 	}
 	
 	public Map<Integer, Chunk> chunks;
 	public EntityManager entities;
 	
 	public World() {
 		chunks = new HashMap<Integer, Chunk>();
 		entities = new EntityManager();
 	}
 	
 	public void readChunkData(int x, int y, int z, int sx, int sy, int sz, byte[] data) throws IOException, DataFormatException {
 		boolean isEntireChunk = (sx == 16 && sy == 128 && sz == 16);
 		int cx = x >> 4;
 		int cz = z >> 4;
 		
 		Chunk chunk = getChunk(cx, cz);
 		
 		Inflater inf = new Inflater();
 		inf.setInput(data);
 		
 		byte[] blocks;
 		
 		if (isEntireChunk && chunk != null) {
 			blocks = chunk.blocks;
 		} else {
 			blocks = new byte[sx*sy*sz];
 		}
		inf.inflate(blocks, 0, blocks.length);
 		
 		if (chunk == null) {
 			if (isEntireChunk) {
 				createChunk(cx, cz, blocks);
 				return;
 			}
 			createEmptyChunk(cx, cz);
 		}
 		for (int bx = 0; bx < sx; bx++) {
 			for (int by = 0; by < sy; by++) {
 				for(int bz = 0; bz < sz; bz++) {
 					chunk.setBlock(x+bx, y+by, z+bz, blocks[bx*(sy*sz) + bz*(sy) + by]);
 				}
 			}
 		}
 	}
 	public void multiBlockChange(int cx, int cz, int len, byte[] coords, byte[] types, byte[] metadata) {
 		Chunk chunk = getChunk(cx, cz);
 		for (int i = 0; i < len; i++) {
 			chunk.setBlock((coords[i*2]>>4), (coords[i*2+1]), (coords[i*2]&15), types[i]);
 		}
 	}
 	
 	public void setBlock(int x, int y, int z, int type) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) {
 			c.setBlock(x, y, z, type);
 		}
 	}
 	
 	public int getBlock(int x, int y, int z) {
 		Chunk c = getChunk(x >> 4, z >> 4);
 		if (c != null) {
 			return c.getBlock(x, y, z);
 		}
 		return 0;
 	}
 	
 	public int getBlock(double x, double y, double z) {
 		return getBlock((int)Math.floor(x), (int)y, (int)Math.floor(z));
 	}
 	
 	public Chunk getChunk(int cx, int cz) {
 		return chunks.get(GetKey(cx, cz));
 	}
 	
 	public boolean containsChunk(int cx, int cz) {
 		return chunks.containsKey(GetKey(cx, cz));
 	}
 	
 	public void createEmptyChunk(int cx, int cz) {
 		chunks.put(GetKey(cx, cz), new Chunk(cx, cz, new byte[16*16*128]));
 	}
 	
 	public void createChunk(int cx, int cz, byte[]data) {
 		chunks.put(GetKey(cx, cz), new Chunk(cx, cz, data));
 	}
 	
 	public void deleteChunk(int cx, int cz) {
 		chunks.remove(GetKey(cx, cz));
 	}
 	
 	public boolean canStand(double x, double y, double z) {
 		return canStand((int)Math.floor(x), (int)y, (int)Math.floor(z));
 	}
 	
 	public boolean canStand(int x, int y, int z) {
 		int b1 = getBlock(x,y-1,z);
 		int b2 = getBlock(x,y,z);
 		int b3 = getBlock(x,y+1,z);
 
 		if (ItemID.solid[b1] && !ItemID.solid[b2] && !ItemID.solid[b3]) {
 			return true;
 		}
 		return false;
 	}
 }
 
 
 
 
 
 
 
 
