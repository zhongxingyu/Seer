 package mods.nordwest.generators;
 
 import java.util.Random;
 
 import mods.nordwest.common.NordWest;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenerator;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class CustomTreeWorldGenerator implements IWorldGenerator {
 	private final WorldGenerator worldGenCustomTree2;
 	private final WorldGenerator worldGenSakuraForest;
 	private final WorldGenerator worldGenHugeTree;
 	private final WorldGenerator worldGenCustomTree;
 	private final WorldGenerator worldGenCustomTree3;
 	private final WorldGenerator worldGenBigCustomTrees;
 
 	public CustomTreeWorldGenerator() {
 		worldGenCustomTree2 = new WorldGenCustomTree2(true);
 		worldGenSakuraForest = new WorldGenSakuraForest(true);
 		worldGenHugeTree = new WorldGenHugeTree(true, 7 + NordWest.rand.nextInt(10), 3, 3);
 		worldGenCustomTree = new WorldGenCustomTree(true, 4 + NordWest.rand.nextInt(7), 3, 3, false);
 		worldGenCustomTree3 = new WorldGenCustomTree(true);
 		worldGenBigCustomTrees = new WorldGenBigCustomTrees(true);
 	}
 
 	@Override
 	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 		chunkX = chunkX << 4;
 		chunkZ = chunkZ << 4;
 		final BiomeGenBase biome = world.getBiomeGenForCoords(chunkX, chunkZ);
 		/** Yellow tree **/
 		if (biome == BiomeGenBase.plains || biome == BiomeGenBase.extremeHills)
			for (int i = 0; i < 13; i++) {
 				final int x = chunkX + rand.nextInt(16) + 8;
 				final int z = chunkZ + rand.nextInt(16) + 8;
 				final int y = world.getTopSolidOrLiquidBlock(x, z);
 				worldGenCustomTree2.generate(world, rand, x, y, z);
 			}
 		/** Pink tree **/
 		if (biome == BiomeGenBase.river || biome == BiomeGenBase.frozenRiver)
			for (int i = 0; i < 13; i++) {
 				final int x = chunkX + rand.nextInt(16) + 8;
 				final int z = chunkZ + rand.nextInt(16) + 8;
 				final int y = world.getTopSolidOrLiquidBlock(x, z);
 				worldGenSakuraForest.generate(world, rand, x, y, z);
 			}
 		/** Big tree **/
 		if (biome == BiomeGenBase.river || biome == BiomeGenBase.frozenRiver || biome == BiomeGenBase.forest || biome == BiomeGenBase.forestHills)
			for (int i = 0; i < 5; i++) {
 				final int x = chunkX + rand.nextInt(16) + 8;
 				final int z = chunkZ + rand.nextInt(16) + 8;
 				final int y = world.getTopSolidOrLiquidBlock(x, z);
 				worldGenHugeTree.generate(world, rand, x, y, z);
 			}
 		if (biome == BiomeGenBase.forest || biome == BiomeGenBase.forestHills || biome == BiomeGenBase.jungle)
			for (int i = 0; i < 2; i++) {
 				final int x = chunkX + rand.nextInt(16) + 8;
 				final int z = chunkZ + rand.nextInt(16) + 8;
 				final int y = world.getTopSolidOrLiquidBlock(x, z);
 				worldGenCustomTree.generate(world, rand, x, y, z);
 			}
 		if (biome == BiomeGenBase.forest || biome == BiomeGenBase.forestHills || biome == BiomeGenBase.jungle)
			for (int i = 0; i < 15; i++) {
 				final int x = chunkX + rand.nextInt(16) + 8;
 				final int z = chunkZ + rand.nextInt(16) + 8;
 				final int y = world.getTopSolidOrLiquidBlock(x, z);
 				worldGenCustomTree3.generate(world, rand, x, y, z);
 			}
 		if (biome == BiomeGenBase.forest || biome == BiomeGenBase.forestHills || biome == BiomeGenBase.taiga)
			for (int i = 0; i < 15; i++) {
 				final int x = chunkX + rand.nextInt(16) + 8;
 				final int z = chunkZ + rand.nextInt(16) + 8;
 				final int y = world.getTopSolidOrLiquidBlock(x, z);
 				worldGenBigCustomTrees.generate(world, rand, x, y, z);
 			}
 	}
 
 }
