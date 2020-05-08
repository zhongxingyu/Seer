 package uk.co.jacekk.bukkit.almostflatlands.generator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.noise.SimplexOctaveGenerator;
 
 import uk.co.jacekk.bukkit.almostflatlands.AlmostFlatLands;
 import uk.co.jacekk.bukkit.almostflatlands.Config;
 
 public class ChunkProvider extends ChunkGenerator {
 	
 	private AlmostFlatLands plugin;
 	
 	public ChunkProvider(AlmostFlatLands plugin){
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public List<BlockPopulator> getDefaultPopulators(World world){
 		ArrayList<BlockPopulator> populators = new ArrayList<BlockPopulator>();
 		
 		populators.add(new FlowerPopulator(plugin));
 		populators.add(new TreePopulator(plugin));
 		
 		return populators;
 	}
 	
 	private void setBlockAt(byte[][] chunk, int x, int y, int z, Material type){
 		int section = y >> 4;
 		int position = ((y & 0xF) << 8) | (z << 4) | x;
 		
 		if (chunk[section] == null){
 			chunk[section] = new byte[4096];
 		}
 		
 		chunk[section][position] = (byte) type.getId();
 	}
 	
 	@Override
 	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes){
 	    byte[][] chunk = new byte[world.getMaxHeight() / 16][];
 	    
 	    SimplexOctaveGenerator gen =  new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
 		
 		gen.setScale(1 / 64.0);
 		
 		for (int x = 0; x < 16; ++x){
 			for (int z = 0; z < 16; ++z){
 				this.setBlockAt(chunk, x, 0, z, Material.BEDROCK);
 				
 				int height = (int) ((gen.noise(x + chunkX * 16, z + chunkZ * 16, 0.5, 0.5) / 0.75) + plugin.config.getInt(Config.WORLD_HEIGHT));
 				
 				for (int y = 1; y < height; ++y){
 					this.setBlockAt(chunk, x, y, z, Material.DIRT);
 				}
 				
 				this.setBlockAt(chunk, x, height, z, Material.GRASS);
 				
				biomes.setBiome(x, z, Biome.valueOf(plugin.config.getString(Config.WORLD_BIOME)));
 			}
 		}
 	    
 	    return chunk;
 	}
 	
 }
