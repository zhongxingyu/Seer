 package net.nexisonline.spade.chunkproviders;
 
 import java.util.Random;
 import java.util.logging.Logger;
 
import net.minecraft.server.NoiseGeneratorOctaves;
 import net.minecraft.server.NoiseGeneratorPerlin;
 import net.nexisonline.spade.SpadeChunkProvider;
 
 import org.bukkit.Material;
 import org.bukkit.block.Biome;
 import org.bukkit.util.config.ConfigurationNode;
 
 public class ChunkProviderSurrealIslands extends SpadeChunkProvider {
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#onLoad(org.bukkit.World, long)
 	 */
 	private Random randA;
 	private Random randB;
	private NoiseGeneratorPerlin terrainNoiseA;
 	private NoiseGeneratorPerlin terrainNoiseB;
 	@Override
 	public void onLoad(Object world, long seed) {
 		this.setHasCustomTerrain(true);
 
 		this.randA=new Random(seed);
 		this.randB=new Random(seed+51);
 		try {
 			terrainNoiseA=new NoiseGeneratorPerlin(randA);
 			terrainNoiseB=new NoiseGeneratorPerlin(randB);
 		} catch (Exception e) {
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#generateChunk(int, int, byte[],
 	 * org.bukkit.block.Biome[], double[])
 	 */
 	@Override
 	public void generateChunk(Object world, int X, int Z, byte[] abyte, Biome[] biomes,
 			double[] temperature) {
 
 		for (int x = 0; x < 16; x+=1) {
 			for (int z = 0; z < 16; z+=1) {
 				for (int y = 0; y < 128; y+=1) {
					double a = terrainNoiseA.a(x+(X*16)*0.0001, y*0.0001 , z+(Z*16)*0.0001);
					double b = terrainNoiseB.a(x+(X*16)*0.0001, y*0.0001 , z+(Z*16)*0.0001);
 					byte block = (byte) ((a*b>0d) ? Material.STONE.getId() : Material.AIR.getId());
 					
 					// If below height, set rock. Otherwise, set air.
 					block = (y <= 63 && block == 0) ? (byte) 9 : block; // Water
 					block = (y <= 5 && block==9) ? (byte)Material.SAND.getId() : block;
 					block = (y <= 1) ? (byte)Material.BEDROCK.getId() : block;
 					
 					// Origin point + sand to prevent 5000 years of loading.
 					if(x==0&&z==0&&X==x&&Z==z&&y<=63)
 						block=(byte) ((y==63)?12:7);
 					
 					abyte[getBlockIndex(x,y,z)]=block;//(byte) ((y<2) ? Material.BEDROCK.getId() : block);
 				}
 			}
 		}
 		Logger.getLogger("Minecraft").info(String.format("[Islands] Chunk (%d,%d)",X,Z));
 
 	}
 
 	@Override
 	public ConfigurationNode configure(ConfigurationNode node) {
 		return node;
 		// TODO Auto-generated method stub
 		
 	}
 }
