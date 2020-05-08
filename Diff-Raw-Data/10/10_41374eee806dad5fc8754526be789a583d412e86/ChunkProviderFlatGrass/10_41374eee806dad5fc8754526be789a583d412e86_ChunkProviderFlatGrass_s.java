 package net.nexisonline.spade.chunkproviders;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChunkProvider;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 
 public class ChunkProviderFlatGrass extends ChunkProvider {
 	private byte[] template;
 	public void onLoad(World world, long seed) {
 		this.setHasCustomTerrain(true);
 		
 		template=new byte[16*128*16];
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
 				double h = 64;
 				for (int y = 0; y < h; y++) {
 					template[getBlockIndex(x, y, z)] = (byte) ((y < 2) ? 7 : 1);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void generateChunk(World world, int X, int Z, byte[] abyte,
 			Biome[] biomes, double[] temperature) {
 		abyte=template;
 		Logger.getLogger("Minecraft")
 				.info(String.format("[Flatgrass] Chunk (%d,%d)", X, Z));
 	}
 
 }
