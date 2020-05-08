 package net.nexisonline.spade.chunkproviders;
 
 import java.util.logging.Logger;
 
 import net.nexisonline.spade.SpadeChunkProvider;
 
 import org.bukkit.Material;
 import org.bukkit.block.Biome;
 import org.bukkit.util.config.ConfigurationNode;
 
 import toxi.math.noise.SimplexNoise;
 
 public class ChunkProviderSurrealIslands extends SpadeChunkProvider {
 	private SimplexNoise terrainNoiseA;
 	private SimplexNoise terrainNoiseB;
 	@Override
 	public void onLoad(Object world, long seed) {
 		this.setHasCustomTerrain(true);
 		try {
 			terrainNoiseA=new SimplexNoise(seed);
 			terrainNoiseB=new SimplexNoise(seed+51);
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
 		final double SCALE=0.01;
 		final double THRESHOLD=-0.3;
 		for (int x = 0; x < 16; x+=1) {
 			for (int z = 0; z < 16; z+=1) {
 				for (int y = 0; y < 128; y+=1) {
 					
 					double a = terrainNoiseA.noise((double)(x+(X*16))*SCALE, (double)y*SCALE , (double)(z+(Z*16))*SCALE);
 					double b = terrainNoiseB.noise((double)(x+(X*16))*SCALE, (double)y*SCALE , (double)(z+(Z*16))*SCALE);
					byte block = (byte) ((a*b<THRESHOLD) ? Material.STONE.getId() : Material.AIR.getId());
 					
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
