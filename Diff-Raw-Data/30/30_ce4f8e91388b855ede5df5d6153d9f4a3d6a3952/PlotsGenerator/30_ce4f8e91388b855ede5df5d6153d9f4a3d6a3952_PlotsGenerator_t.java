 package uk.co.jacekk.bukkit.infiniteplots;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.noise.SimplexOctaveGenerator;
 
 public class PlotsGenerator extends ChunkGenerator {
 	
 	private int plotSize;
 	private int plotSizeBy2;
 	
 	private int plotHeight;
 	
 	private byte bedId;
 	private byte baseId;
 	private byte surfaceId;
 	private byte hiddenId;
 	private byte pathId;
 	private byte wallLowerId;
 	private byte wallUpperId;
 	
 	private Biome plotBiome;
 	private Biome pathBiome;
 	
 	public PlotsGenerator(int size, int height, byte baseId, byte surfaceId, byte pathId, byte wallLowerId, byte wallUpperId, Biome plotBiome, Biome pathBiome){
 		this.plotSize = size + 7;
 		this.plotSizeBy2 = this.plotSize / 2;
 		
 		this.plotHeight = height;
 		
 		this.bedId = (byte) Material.BEDROCK.getId();
 		this.baseId = (baseId == (byte) Material.GRASS.getId()) ? (byte) Material.DIRT.getId() : baseId;
 		this.surfaceId = surfaceId;
 		this.hiddenId = (this.surfaceId == (byte) Material.GRASS.getId()) ? (byte) Material.DIRT.getId() : this.surfaceId;
 		this.pathId = pathId;
 		this.wallLowerId = wallLowerId;
 		this.wallUpperId = wallUpperId;
 		
 		this.plotBiome = plotBiome;
 		this.pathBiome = pathBiome;
 	}
 	
 	public int getPlotSize(){
 		return this.plotSize - 7;
 	}
 	
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		return new ArrayList<BlockPopulator>();
 	}
 	
 	public Location getFixedSpawnLocation(World world, Random rand){
 		return new Location(world, 0, 18, 0);
 	}
 	
 	public int coordsToByte(int x, int y, int z){
 		return (x * 16 + z) * 128 + y;
 	}
 	
 	private boolean isPathBlock(int x, int z){
 		if ((x % this.plotSize == 0) || (z % this.plotSize == 0)) return true;
 		
 		if (((x + 1) % this.plotSize == 0) || ((z + 1) % this.plotSize == 0)) return true;
 		
 		if (((x - 1) % this.plotSize == 0) || ((z - 1) % this.plotSize == 0)) return true;
 		
 		if (((x + 2) % this.plotSize == 0) || ((z + 2) % this.plotSize == 0)) return true;
 		
 		if (((x - 2) % this.plotSize == 0) || ((z - 2) % this.plotSize == 0)) return true;
 		
 		return false;
 	}
 	
 	private boolean isWallBlock(int x, int z){
 		if (((x + 3) % this.plotSize == 0) || ((z + 3) % this.plotSize == 0)) return true;
 		
 		if (((x - 3) % this.plotSize == 0) || ((z - 3) % this.plotSize == 0)) return true;
 		
 		return false;
 	}
 	
 	private boolean isGateBlock(int x, int z){
 		double pos = this.plotSizeBy2 + 0.5;
 		x += this.plotSizeBy2;
 		
 		if ((x % pos == 0) && ((z + 3) % this.plotSize == 0)) return true;
 		
 		if (((x + 1) % pos == 0) && ((z + 3) % this.plotSize == 0)) return true;
 		
 		if (((x - 1) % pos == 0) && ((z + 3) % this.plotSize == 0)) return true;
 		
 		return false;
 	}
 	
 	private void setBlockAt(byte[][] chunk, int x, int y, int z, byte typeId){
 		chunk[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = typeId;
 	}
 	
 	@Override
 	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes){
 		byte[][] chunk = new byte[(int) Math.ceil((this.plotHeight + 2.0d) / 16)][4096];
		Random rand = new Random(world.getSeed());
		SimplexOctaveGenerator octave = new SimplexOctaveGenerator(rand, 12);
 		
 		int worldChunkX = chunkX * 16;
 		int worldChunkZ = chunkZ * 16;

	
	
 		octave.setScale(1 /34.0);
		world.setSpawnLocation(0, 0, 0);
		Location loc= world.getSpawnLocation();
		this.setBlockAt(chunk, 0, 0, 0, (byte) Material.GRASS.getId());
 		for (int x = 0; x < 16; ++x){
 			for (int z = 0; z < 16; ++z){
 				double noise = octave.noise(worldChunkX + x, worldChunkZ +z, 0.5, 0.5) * 12;
				loc.setX(x + noise );
				loc.setZ(z + noise);
				world.generateTree(loc, TreeType.JUNGLE);
				//this.setBlockAt(chunk, x, 0, z, this.bedId); 
				
 				for(int y = 0; y <(int)noise; ++y){
					loc.setY(y + noise);
					world.generateTree(loc, TreeType.JUNGLE);
 					this.setBlockAt(chunk, x, y, z, (byte) Material.GRASS.getId());
				this.setBlockAt(chunk, (int) (x/noise), (int) noise%8, z, (byte) Material.LONG_GRASS.getId());
					
 				}
 			}
 		}
 		
 		return chunk;
 	}
 	
 }
