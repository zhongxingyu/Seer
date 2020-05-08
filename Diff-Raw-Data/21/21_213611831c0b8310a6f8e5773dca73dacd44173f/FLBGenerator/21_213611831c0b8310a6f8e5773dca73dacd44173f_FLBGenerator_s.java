 package net.dkebnh.bukkit.FlatlandsBuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World; 
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 public class FLBGenerator extends ChunkGenerator{
 	
 	private Logger log = Logger.getLogger("Minecraft");
 	private int height;
 	private String fillblock;
 	private String borderAblock;
 	private String borderBblock;
 	
 	public FLBGenerator(){
		this("64,wool:15,wool:8");
 	}
 	
 	public FLBGenerator(String id){
 		if (id != null){
 			try {
 				if (id.length() > 0){
 					String tokens[] = id.split("[,]");
 					
 					if (tokens.length == 3){
 						height = Integer.parseInt(tokens[0]);
 						if (height <= 0 || height >= 128) // Will change max height later on, once sure 256 is the maximum value and not lower.
                         {
                             log.warning("[FlatlandsBuilder] Invalid height '" + tokens[0] + "'. Using 64 instead.");
                             height = 64;
                         } 
 						
 						fillblock = tokens[1];
 						borderAblock = tokens[2];
 						borderBblock = null;
 					}
 				}
 			} catch (Exception e){
				log.severe("[FlatlandsBuilder] Error parsing FlatlandsBuilder Settings '" + id + "'. using defaults '64,wool:15,wool:8': " + e.toString());
                 e.printStackTrace();
                 height = 64;
                 fillblock = "wool:15";
				borderAblock = "wool:8";
 				borderBblock = null;
 			}
 		} else {
			log.info("[FlatlandsBuilder] No Settings provided, using defaults 64,wool:15,wool:8");
             height = 64;
             fillblock = "wool:15";
			borderAblock = "wool:8";
 			borderBblock = null;
 		}
 	}
 	
 	public List<BlockPopulator> getDefaultPopulators(World world){
 		ArrayList<BlockPopulator> populators = new ArrayList<BlockPopulator>();
 		
 		populators.add(new FLBPopulator(height, fillblock, borderAblock, borderBblock));
 		
 		return populators;
 	}
 	
 	public Location getFixedSpawnLocation(World world, Random random){
 		return new Location(world, 0, height + 1, 0);
 	}
 	
 	private int coordsToInt(int x, int y, int z){
 			return (x * 16 + z) * 256 + y;
 	}
 	
 	public byte[] generate(World world, Random rand, int chunkx, int chunkz){
 		byte[] blocks = new byte[65536];
 		int x, y, z;
 		
 		for (x = 0; x < 16; ++x){
 			for (z = 0; z < 16; ++z){
 				blocks[this.coordsToInt(x, 0, z)] = (byte) Material.BEDROCK.getId();
 				
 				for (y = 1; y < height; ++y){
 					blocks[this.coordsToInt(x, y, z)] = (byte) Material.STONE.getId();
 				}
 
 				blocks[this.coordsToInt(x, height, z)] = (byte) Material.WOOL.getId();
 			}
 		}
 		
 	return blocks;
 	}
 }
