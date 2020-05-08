 package fi.haju.haju3d.server.world;
 
 import fi.haju.haju3d.protocol.coordinate.ChunkPosition;
 import fi.haju.haju3d.protocol.coordinate.LocalTilePosition;
 import fi.haju.haju3d.protocol.coordinate.Vector3i;
 import fi.haju.haju3d.protocol.world.Chunk;
 import fi.haju.haju3d.protocol.world.Tile;
 import fi.haju.haju3d.server.world.utils.FloodFiller;
 import fi.haju.haju3d.server.world.utils.PerlinNoiseGenerator;
 import fi.haju.haju3d.server.world.utils.WorldGenerationUtils;
 import fi.haju.haju3d.util.Profiled;
 
 import java.util.Random;
 
 public class PerlinNoiseWorldGenerator implements WorldGenerator {
   
   private static final int TERRAIN_FEATURE_SIZE = 6;
   private static final int TERRAIN_SMOOTHNESS = 8;
   private static final int TERRAIN_THRESHOLD = 32;
   
   private int seed;
   private PerlinNoiseGenerator generator;
 
   @Override
   @Profiled
   public Chunk generateChunk(ChunkPosition position, int size) {
     int realseed = seed ^ (position.x + position.y * 123 + position.z * 12347);
     return makeChunk(size, realseed, position);
   }
 
   @Override
   public void setSeed(int seed) {
     this.seed = seed;
     //TODO : Proper initialization
     this.generator = new PerlinNoiseGenerator(TERRAIN_FEATURE_SIZE, TERRAIN_SMOOTHNESS, new Random(seed));
   }
 
   private static Chunk filterFloaters(Chunk chunk) {
     Chunk ground = new Chunk(chunk.getSize(), chunk.getSeed(), chunk.getPosition());
     new FloodFiller(ground, chunk).fill();
     return ground;
   }
 
   private Chunk makeChunk(int size, int seed, ChunkPosition position) {
     boolean onlyAir = true;
     Vector3i wp = position.mult(size);
     float minVal = generator.getMinValue(wp, size); 
     if(minVal + position.y*(size + 1) > TERRAIN_THRESHOLD) {
       return new Chunk(size, seed, position, Tile.AIR);
     }
     float maxVal = generator.getMaxValue(wp, size);
     if(maxVal + position.y*(size) < TERRAIN_THRESHOLD) {
       return new Chunk(size, seed, position, Tile.GROUND);
     }
     Chunk chunk = new Chunk(size, seed, position);
     for (int x = 0; x < size; x++) {
       for (int y = 0; y < size; y++) {
         for (int z = 0; z < size; z++) {
           int rx = x + position.x*size;
           int ry = y + position.y*size;
           int rz = z + position.z*size;
           float v = ry + generator.getValueAt(rx, ry, rz);
           // TODO Type from noise
           Tile tile = v < TERRAIN_THRESHOLD ? Tile.GROUND : Tile.AIR;
           if(tile != Tile.AIR) {
             onlyAir = false;
           }
           chunk.set(x, y, z, tile);
           // TODO Color from noise
           float col = 1.0f;
           chunk.setColor(x, y, z, col);
         }
       }
     }
     if(onlyAir) {
       return new Chunk(size, seed, position, Tile.AIR);
     }
     Random r = new Random(seed);
     // add trees
     generateTrees(chunk, r);
    return filterFloaters(chunk);
   }
 
   private void generateTrees(Chunk chunk, Random r) {
     int size = chunk.getSize();
     for (int i = 0; i < 4; ++i) {
       int x = r.nextInt(size - 3);
       int z = r.nextInt(size - 3);
       int y = WorldGenerationUtils.findGround(chunk, size, x, z);
       if (y >= 1 && y < size - 20 && chunk.get(x, y - 1, z) == Tile.GROUND) {
        WorldGenerationUtils.makeTreeAt(chunk, r, new LocalTilePosition(x, z, y));
       }
     }
   }
 
 }
