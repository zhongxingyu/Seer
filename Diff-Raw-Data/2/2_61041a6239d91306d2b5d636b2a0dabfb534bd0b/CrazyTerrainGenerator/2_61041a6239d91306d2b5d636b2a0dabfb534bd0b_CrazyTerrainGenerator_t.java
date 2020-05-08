 package edu.unca.atjones.CrazyTerrain;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 public class CrazyTerrainGenerator extends ChunkGenerator {
 	
 	private CrazyTerrain plugin;
 	private double xFactor = Math.PI/32;
 	private double zFactor = Math.PI/32;
 	
 	public CrazyTerrainGenerator(CrazyTerrain plugin){
 		this.plugin = plugin;
 	}
 	
 	private double densityMap(long x, long y, long z) {
 		return Math.sin(x*y*xFactor)*Math.sin(z*y*zFactor);
 	}
 	
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		return new ArrayList<BlockPopulator>();
 	}
 	
 	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0, 128, 0);
 	}
 	
 	void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
         if (result[y >> 4] == null) {
             result[y >> 4] = new byte[4096];
         }
         result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
 	}
 	
 	byte getBlock(byte[][] result, int x, int y, int z) {
 		if (result[y >> 4] == null) {
 	        return (byte)0;
 	    }
 	    return result[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
 	}
 	
 	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes){
 		
 		byte[][] blocks = new byte[8][4096];
 		
 		int worldChunkX = chunkX * 16;
 		int worldChunkZ = chunkZ * 16;
 		
 		for (int x = 0; x < 16; ++x){
 			for (int z = 0; z < 16; ++z){
 				for(int y = 0; y < 128; ++y){
 					long X = worldChunkX + x;
 					long Z = worldChunkZ + z;
 					if(Math.abs(densityMap(X,y,Z)) < 0.3) {
 						setBlock(blocks,x,y,z,(byte) Material.STONE.getId());
 					}
 				}
 			}
 		}
 		
 		return blocks;
 	}
 	
 }
