 package us.bliven.bukkit.earthcraft;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 import us.bliven.bukkit.earthcraft.gis.DataUnavailableException;
 import us.bliven.bukkit.earthcraft.gis.ElevationProvider;
 import us.bliven.bukkit.earthcraft.gis.MapProjection;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 /**
  * Generates real-world maps
  * @author Spencer Bliven
  */
 public class EarthGen extends ChunkGenerator {
 	Logger log;
 
 	private MapProjection projection;
 	private ElevationProvider elevation;
 	private Coordinate spawn;
 
 	private final int defaultBlockHeight = 1;
 
 	private double seaLevel; // 1st block above water
 
 	public EarthGen( MapProjection projection, ElevationProvider elevation, Coordinate spawn) {
 		super();
 		this.projection = projection;
 		this.elevation = elevation;
 		this.spawn = spawn;
 		this.log = Logger.getLogger(EarthGen.class.getName());
 
 		Location origin = projection.coordinateToLocation(null, new Coordinate(0,0,0));
 		this.seaLevel = origin.getY();
 
 		log.info("Sea level at "+seaLevel);
 	}
 
 
 	@Override
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		return Arrays.asList();//super.getDefaultPopulators(world);
 		//Arrays.asList((BlockPopulator) new BlankPopulator());
 	}
 
 	/**
 	 * Spawn everywhere
 	 */
 	@Override
 	public boolean canSpawn(World world, int x, int z) {
 		return true;
 	}
 
 	/**
 	 * This converts relative chunk locations to bytes that can be written to the chunk
 	 */
 	protected int xyzToByte(int x, int y, int z) {
 		return (x * 16 + z) * 256 + y;
 	}
 
 
 	@Override
 	public byte[] generate(World world, Random rand, int chunkx, int chunkz) {
 		int chunkHeight = world.getMaxHeight();
 		byte[] result = new byte[16*16*chunkHeight];
 
 
 		for(int x=0; x<16; x++){
 			for(int z=0; z<16; z++) {
 				int y = 0;
 				//This will set the floor of each chunk at bedrock level to bedrock
 				result[xyzToByte(x,y,z)] = (byte) Material.BEDROCK.getId();
 
 				int height = getBlockHeight(world, chunkx*16+x, chunkz*16+z);
 				y++;
				for(;y<height-1 && y<256;y++) {
 					result[xyzToByte(x,y,z)] = (byte) Material.DIRT.getId();
 				}
				if(y==height-1 && y<256) {
					if(y<seaLevel-1) {
 						result[xyzToByte(x,y,z)] = (byte) Material.SAND.getId();
 					} else {
 						result[xyzToByte(x,y,z)] = (byte) Material.GRASS.getId();
 					}
 					y++;
 				}
				for(;y<seaLevel-1 && y<256 ;y++) {
 					result[xyzToByte(x,y,z)] = (byte) Material.STATIONARY_WATER.getId();
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Calculate the elevation for a position
 	 * @param world
 	 * @param worldx
 	 * @param worldz
 	 * @return
 	 */
 	public int getBlockHeight(World world, int worldx, int worldz) {
 		int height = 1;
 		Location root = new Location(world,worldx,0,worldz);
 		// Translate x/z to lat/lon
 		Coordinate coord = projection.locationToCoordinate(root);
 		try {
 			// get elevation in m
 			Double h = elevation.fetchElevation(coord);
 
 			if(h != null) {
 				coord.z = h;
 			} else {
 				coord.z = Double.NaN;
 			}
 
 		} catch (DataUnavailableException e) {
 			log.log(Level.SEVERE,"Data unavailable at "+worldx+","+worldz,e);
 			coord.z = Double.NaN;
 		}
 		// translate elevation to blocks
 		root = projection.coordinateToLocation(world, coord);
 		if(root.getY() == Double.NaN ) {
 			height = defaultBlockHeight;
 		} else {
 			height = (int) Math.floor(root.getY()+1);
 		}
 
 		if(height>world.getMaxHeight()) {
 			height = world.getMaxHeight();
 		}
 
 		return height;
 	}
 
 
 	@Override
 	public Location getFixedSpawnLocation(World world, Random random) {
 		Double elev;
 		try {
 			elev = elevation.fetchElevation(spawn);
 		} catch (DataUnavailableException e) {
 			elev = Double.NaN;
 		}
 		Location spawnloc = projection.coordinateToLocation(world, new Coordinate(spawn.x,spawn.y,elev));
 		if(Double.isNaN(spawnloc.getY())) {
 			spawnloc.setY( defaultBlockHeight );
 		}
 
 		return spawnloc;
 	}
 
 	public MapProjection getMapProjection() {
 		return projection;
 	}
 
 
 	public ElevationProvider getElevationProvider() {
 		return elevation;
 	}
 
 
 	public Coordinate getSpawn() {
 		return spawn;
 	}
 
 }
