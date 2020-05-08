 /*
 *copyright
 */
 package com.github.computerdude5000.derpingworldgen;
 
 import com.github.computerdude5000.derpingworldgen.populators.DirtPop;
 import com.github.computerdude5000.derpingworldgen.populators.GoldPop;
 import com.github.computerdude5000.derpingworldgen.populators.GrassPop;
 import com.github.computerdude5000.derpingworldgen.populators.SaplingsPop;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.noise.SimplexOctaveGenerator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 6/2/13
  * Time: 10:13 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class BasicDerpingGen  extends ChunkGenerator
 {
     /**
      *
      * @param x
      * X co-ordinate of the block to be set in the array
      * @param y
      * Y co-ordinate of the block to be set in the array
      * @param z
      * Z co-ordinate of the block to be set in the array
      * @param chunk
      * An array containing the Block id's of all the blocks in the chunk. The first offset
      * is the block section number. There are 16 block sections, stacked vertically, each of which
      * 16 by 16 by 16 blocks.
      * @param material
      * The material to set the block to.
      */
     void setBlock(int x, int y, int z, byte[][] chunk, Material material) {
         if (chunk[y >> 4] == null)
             chunk[y >> 4] = new byte[16 * 16 * 16];
         if (!(y <= 256 && y >= 0 && x <= 16 && x >= 0 && z <= 16 && z >= 0))
             return;
         try {
             chunk[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (byte) material.getId();
         } catch (Exception e) {
             // do nothing
         }
     }
     @Override
 /**
  * @param world
  * The world the chunk belongs to
  * @param rand
  * Don't use this, make a new random object using the world seed (world.getSeed())
  * @param biome
  * Use this to set/get the current biome
  * @param ChunkX and ChunkZ
  * The x and z co-ordinates of the current chunk.
  */
     public byte[][] generateBlockSections(World world, Random rand, int ChunkX, int ChunkZ, BiomeGrid biome) {
         byte[][] chunk = new byte[world.getMaxHeight() / 16][];
         SimplexOctaveGenerator gen1 = new SimplexOctaveGenerator(world,8);
         SimplexOctaveGenerator gen2 = new SimplexOctaveGenerator(world,8);
         SimplexOctaveGenerator gen3 = new SimplexOctaveGenerator(world,8);
         gen1.setScale(1/66.0);
         gen2.setScale(1/64.0);
         gen2.setScale(1/73.8);
 
         for (int x=0; x<16; x++) { //loop through all of the blocks in the chunk that are lower than maxHeight
             for (int z=0; z<16; z++) {
                 int maxHeight = 45; //how thick we want it to be.
                 for (int y=0;y<maxHeight;y++) {
                  if(x+y+z % 2 == 0 ){
                      setBlock(x,y,z,chunk,Material.STONE);
 
                  } else if (x+y+z % 3 == 0) {
                         setBlock(x,y,z,chunk,Material.SOUL_SAND);
                     } else if (x+y-z % 2 == 0 ){
                      setBlock(x,y,z,chunk,Material.SANDSTONE);
                  }  else if (x-y+z % 3 == 0 ){
                      setBlock(x,y,z,chunk,Material.SAND);
                  } else if (x*z*y % 7 == 0 ){
                      setBlock(x,y,z,chunk,Material.ENDER_STONE);
                  } else if (x+y*4*z /6 % 3 == 0 ){
                      setBlock(x,y,z,chunk,Material.REDSTONE_ORE);
                  } else {
                      setBlock(x,y,z,chunk,Material.DIRT);
                  }
                 }
             }
         }
         for (int x=0; x<16; x++) {
             for (int z=0; z<16; z++) {
 
                 int realX = x + ChunkX * 16; //used so that the noise function gives us
                 int realZ = z + ChunkZ * 16; //different values each chunk
                 double frequency = 0.5; // the reciprocal of the distance between points
                 double frequency1 = 0.7;
                 double amplitude = 0.5; // The distance between largest min and max values
                 double amplitude1 = 0.8;
                 int multitude = 64; //how much we multiply the value between -1 and 1. It will determine how "steep" the hills will be.
                 int multitude1 = 44;
                 int sea_level = 64;
 
                 double gen1MaxHeight = gen1.noise(realX, realZ, frequency, amplitude) * multitude + sea_level;
                 double gen2MaxHeight = gen2.noise(realX, realZ, frequency1, amplitude1)* multitude1 + sea_level;
                 double maxHeight = Math.max(gen1MaxHeight ,gen2MaxHeight);
                 for (int y=0;y<maxHeight;y++) {
                     setBlock(x,y,z,chunk,Material.STONE); //set the current block to stone
                    // setBlock(x,y+1,z,chunk,Material.GRASS);
                 }
                setBlock(x, (int) (maxHeight+1),z,chunk,Material.GRASS);
             }
         }
         return chunk;
     }
     /**
      * Returns a list of all of the block populators (that do "little" features)
      * to be called after the chunk generator
      */
     @Override
     public List<BlockPopulator> getDefaultPopulators(World world) {
         ArrayList<BlockPopulator> pops = new ArrayList<BlockPopulator>();
         pops.add(new DirtPop());
         pops.add(new GrassPop());
         pops.add(new GoldPop());
         pops.add(new SaplingsPop());
 
         return pops;
     }
 }
