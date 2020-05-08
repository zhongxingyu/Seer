 package me.heldplayer.HeldGeneration.generator.Populators;
 
 import java.util.Random;
 
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenBigMushroom;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenCactus;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenClay;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenDeadBush;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenFlowers;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenLiquids;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenMinable;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenPumpkin;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenReed;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenSand;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenTallGrass;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenVines;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenWaterlily;
 import me.heldplayer.HeldGeneration.generator.WorldGenerators.WorldGenerator;
 import me.heldplayer.HeldGeneration.helpers.BlockHelper;
 import me.heldplayer.HeldGeneration.helpers.Mat;
 import me.heldplayer.HeldGeneration.helpers.PopulatorAssist;
 
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.block.Block;
 
 public class BiomeDecorator {
 	/** The world the BiomeDecorator is currently decorating */
 	protected World currentWorld;
 
 	/** The Biome Decorator's random number generator. */
 	protected Random randomGenerator;
 
 	/** The X-coordinate of the chunk currently being decorated */
 	protected int chunk_X;
 
 	/** The Z-coordinate of the chunk currently being decorated */
 	protected int chunk_Z;
 
 	/** The biome generator object. */
 	protected Biome biome;
 
 	/** The clay generator. */
 	protected WorldGenerator clayGen;
 
 	/** The sand generator. */
 	protected WorldGenerator sandGen;
 
 	/** The gravel generator for the top of the world. */
 	protected WorldGenerator gravelAsSandGen;
 
 	/** The dirt generator. */
 	protected WorldGenerator dirtGen;
 
 	/** The gravel generator. */
 	protected WorldGenerator gravelGen;
 
 	/** Field that holds coal WorldGenMinable */
 	protected WorldGenerator coalGen;
 
 	/** Field that holds iron WorldGenMinable */
 	protected WorldGenerator ironGen;
 
 	/** Field that holds gold WorldGenMinable */
 	protected WorldGenerator goldGen;
 
 	/** Field that holds redstone WorldGenMinable */
 	protected WorldGenerator redstoneGen;
 
 	/** Field that holds diamond WorldGenMinable */
 	protected WorldGenerator diamondGen;
 
 	/** Field that holds Lapis WorldGenMinable */
 	protected WorldGenerator lapisGen;
 
 	/** Field that holds one of the plantYellow WorldGenFlowers */
 	protected WorldGenerator plantYellowGen;
 
 	/** Field that holds one of the plantRed WorldGenFlowers */
 	protected WorldGenerator plantRedGen;
 
 	/** Field that holds mushroomBrown WorldGenFlowers */
 	protected WorldGenerator mushroomBrownGen;
 
 	/** Field that holds mushroomRed WorldGenFlowers */
 	protected WorldGenerator mushroomRedGen;
 
 	/** Field that holds big mushroom generator */
 	protected WorldGenerator bigMushroomGen;
 
 	/** Field that holds WorldGenReed */
 	protected WorldGenerator reedGen;
 
 	/** Field that holds WorldGenCactus */
 	protected WorldGenerator cactusGen;
 
 	/** The water lily generation! */
 	protected WorldGenerator waterlilyGen;
 
 	public BiomeDecorator() {
 		this.sandGen = new WorldGenSand(7, Mat.Sand.id);
 		this.gravelAsSandGen = new WorldGenSand(6, Mat.Gravel.id);
 		this.dirtGen = new WorldGenMinable(Mat.Dirt.id, 32);
 		this.gravelGen = new WorldGenMinable(Mat.Gravel.id, 32);
 		this.coalGen = new WorldGenMinable(Mat.CoalOre.id, 16);
 		this.ironGen = new WorldGenMinable(Mat.IronOre.id, 8);
 		this.goldGen = new WorldGenMinable(Mat.GoldOre.id, 8);
 		this.redstoneGen = new WorldGenMinable(Mat.RedstoneOre.id, 7);
 		this.diamondGen = new WorldGenMinable(Mat.DiamondOre.id, 7);
 		this.lapisGen = new WorldGenMinable(Mat.LapisOre.id, 6);
 		this.plantYellowGen = new WorldGenFlowers(Mat.YellowFlower.id);
 		this.plantRedGen = new WorldGenFlowers(Mat.RedRose.id);
 		this.mushroomBrownGen = new WorldGenFlowers(Mat.BrownMushroom.id);
 		this.mushroomRedGen = new WorldGenFlowers(Mat.RedMushroom.id);
 		this.bigMushroomGen = new WorldGenBigMushroom();
 		this.reedGen = new WorldGenReed();
 		this.cactusGen = new WorldGenCactus();
 		this.waterlilyGen = new WorldGenWaterlily();
 		this.clayGen = new WorldGenClay(4);
 	}
 
 	/**
 	 * Decorates the world. Calls code that was formerly (pre-1.8) in
 	 * ChunkProviderGenerate.populate
 	 */
 	public void decorate(World world, Random rand, int cx, int cz, PopulatorAssist assist) {
 		if (this.currentWorld != null) {
 			throw new RuntimeException("Already decorating!!");
 		} else {
 			this.currentWorld = world;
 			this.randomGenerator = rand;
 			this.chunk_X = cx;
 			this.chunk_Z = cz;
 			this.decorate(assist);
 			this.currentWorld = null;
 			this.randomGenerator = null;
 		}
 	}
 
 	private int getHighestNonBlockingY(World world, int x, int z) {
 		Block block = null;
 		if ((block = world.getHighestBlockAt(x, z)).getTypeId() != Mat.Leaves.id) {
 			return block.getY();
 		}
 
 		for (int y = block.getY(); y >= 0; y++) {
 			if (world.getBlockTypeIdAt(x, y, z) != Mat.Leaves.id) {
 				return y;
 			}
 		}
 
 		return 0;
 	}
 
 	private int getHeightValue(World world, int x, int z) {
 		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
 			Block block = null;
 			if ((block = world.getHighestBlockAt(x, z)).getTypeId() != Mat.Leaves.id && BlockHelper.isSolid(block.getTypeId())) {
 				return block.getY();
 			}
 
			for (int y = block.getY(); y >= 0; y++) {
				if ((block = world.getBlockAt(x, y, z)).getTypeId() != Mat.Leaves.id && BlockHelper.isSolid(block.getTypeId())) {
					return y;
				}
 			}
 
			return 0;
 		}
 		return 0;
 	}
 
 	protected void decorate(PopulatorAssist assist) {
 		this.generateOres();
 		int var1;
 		int var2;
 		int var3;
 
 		for (var1 = 0; var1 < assist.sandPerChunk2; ++var1) {
 			var2 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var3 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.sandGen.generate(this.currentWorld, this.randomGenerator, var2, getHighestNonBlockingY(currentWorld, var2, var3), var3);
 		}
 
 		for (var1 = 0; var1 < assist.clayPerChunk; ++var1) {
 			var2 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var3 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.clayGen.generate(this.currentWorld, this.randomGenerator, var2, getHighestNonBlockingY(currentWorld, var2, var3), var3);
 		}
 
 		for (var1 = 0; var1 < assist.sandPerChunk; ++var1) {
 			var2 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var3 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.sandGen.generate(this.currentWorld, this.randomGenerator, var2, getHighestNonBlockingY(currentWorld, var2, var3), var3);
 		}
 
 		var1 = assist.treesPerChunk;
 
 		if (this.randomGenerator.nextInt(10) == 0) {
 			++var1;
 		}
 
 		int var4;
 
 		for (var2 = 0; var2 < var1; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			WorldGenerator var5 = assist.getRandomTreeGen(this.randomGenerator);
 			var5.setScale(1.0D, 1.0D, 1.0D);
 			var5.generate(this.currentWorld, this.randomGenerator, var3, getHeightValue(currentWorld, var3, var4), var4);
 		}
 
 		for (var2 = 0; var2 < assist.bigMushroomsPerChunk; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.bigMushroomGen.generate(this.currentWorld, this.randomGenerator, var3, getHeightValue(currentWorld, var3, var4), var4);
 		}
 
 		int var7;
 
 		for (var2 = 0; var2 < assist.flowersPerChunk; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.randomGenerator.nextInt(128);
 			var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.plantYellowGen.generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 
 			if (this.randomGenerator.nextInt(4) == 0) {
 				var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 				var4 = this.randomGenerator.nextInt(128);
 				var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 				this.plantRedGen.generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 			}
 		}
 
 		for (var2 = 0; var2 < assist.grassPerChunk; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.randomGenerator.nextInt(128);
 			var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			short grassData = 1;
 			if (this.biome == Biome.JUNGLE || this.biome == Biome.JUNGLE_HILLS) {
 				if (this.randomGenerator.nextInt(4) == 0) {
 					grassData = 2;
 				}
 			}
 
 			WorldGenerator var6 = new WorldGenTallGrass(Mat.TallGrass.id, grassData);
 			var6.generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 		}
 
 		for (var2 = 0; var2 < assist.deadBushPerChunk; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.randomGenerator.nextInt(128);
 			var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			(new WorldGenDeadBush(Mat.DeadShrub.id)).generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 		}
 
 		for (var2 = 0; var2 < assist.waterlilyPerChunk; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 
 			for (var7 = this.randomGenerator.nextInt(128); var7 > 0 && this.currentWorld.getBlockTypeIdAt(var3, var7 - 1, var4) == 0; --var7) {
 				;
 			}
 
 			this.waterlilyGen.generate(this.currentWorld, this.randomGenerator, var3, var7, var4);
 		}
 
 		for (var2 = 0; var2 < assist.mushroomsPerChunk; ++var2) {
 			if (this.randomGenerator.nextInt(4) == 0) {
 				var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 				var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 				var7 = getHeightValue(currentWorld, var3, var4);
 				this.mushroomBrownGen.generate(this.currentWorld, this.randomGenerator, var3, var7, var4);
 			}
 
 			if (this.randomGenerator.nextInt(8) == 0) {
 				var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 				var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 				var7 = this.randomGenerator.nextInt(128);
 				this.mushroomRedGen.generate(this.currentWorld, this.randomGenerator, var3, var7, var4);
 			}
 		}
 
 		if (this.randomGenerator.nextInt(4) == 0) {
 			var2 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var3 = this.randomGenerator.nextInt(128);
 			var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.mushroomBrownGen.generate(this.currentWorld, this.randomGenerator, var2, var3, var4);
 		}
 
 		if (this.randomGenerator.nextInt(8) == 0) {
 			var2 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var3 = this.randomGenerator.nextInt(128);
 			var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.mushroomRedGen.generate(this.currentWorld, this.randomGenerator, var2, var3, var4);
 		}
 
 		for (var2 = 0; var2 < assist.reedsPerChunk; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			var7 = this.randomGenerator.nextInt(128);
 			this.reedGen.generate(this.currentWorld, this.randomGenerator, var3, var7, var4);
 		}
 
 		for (var2 = 0; var2 < 10; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.randomGenerator.nextInt(128);
 			var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.reedGen.generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 		}
 
 		if (this.randomGenerator.nextInt(32) == 0) {
 			var2 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var3 = this.randomGenerator.nextInt(128);
 			var4 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			(new WorldGenPumpkin()).generate(this.currentWorld, this.randomGenerator, var2, var3, var4);
 		}
 
 		for (var2 = 0; var2 < assist.cactiPerChunk; ++var2) {
 			var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 			var4 = this.randomGenerator.nextInt(128);
 			var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 			this.cactusGen.generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 		}
 
 		if (assist.generateLakes) {
 			for (var2 = 0; var2 < 50; ++var2) {
 				var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 				var4 = this.randomGenerator.nextInt(this.randomGenerator.nextInt(120) + 8);
 				var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 				(new WorldGenLiquids(Mat.WaterMoving.id)).generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 			}
 
 			for (var2 = 0; var2 < 20; ++var2) {
 				var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 				var4 = this.randomGenerator.nextInt(this.randomGenerator.nextInt(this.randomGenerator.nextInt(112) + 8) + 8);
 				var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 				(new WorldGenLiquids(Mat.LavaMoving.id)).generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 			}
 		}
 
 		if (assist.biome == Biome.JUNGLE || assist.biome == Biome.JUNGLE_HILLS) {
 			WorldGenVines worldGenVines = new WorldGenVines();
 
 			for (var2 = 0; var2 < 50; ++var2) {
 				var3 = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
 				var4 = 64;
 				var7 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
 				worldGenVines.generate(this.currentWorld, this.randomGenerator, var3, var4, var7);
 			}
 		}
 	}
 
 	/**
 	 * Standard ore generation helper. Generates most ores.
 	 */
 	protected void genStandardOre1(int par1, WorldGenerator par2WorldGenerator, int par3, int par4) {
 		for (int var5 = 0; var5 < par1; ++var5) {
 			int var6 = this.chunk_X + this.randomGenerator.nextInt(16);
 			int var7 = this.randomGenerator.nextInt(par4 - par3) + par3;
 			int var8 = this.chunk_Z + this.randomGenerator.nextInt(16);
 			par2WorldGenerator.generate(this.currentWorld, this.randomGenerator, var6, var7, var8);
 		}
 	}
 
 	/**
 	 * Standard ore generation helper. Generates Lapis Lazuli.
 	 */
 	protected void genStandardOre2(int par1, WorldGenerator par2WorldGenerator, int par3, int par4) {
 		for (int var5 = 0; var5 < par1; ++var5) {
 			int var6 = this.chunk_X + this.randomGenerator.nextInt(16);
 			int var7 = this.randomGenerator.nextInt(par4) + this.randomGenerator.nextInt(par4) + (par3 - par4);
 			int var8 = this.chunk_Z + this.randomGenerator.nextInt(16);
 			par2WorldGenerator.generate(this.currentWorld, this.randomGenerator, var6, var7, var8);
 		}
 	}
 
 	/**
 	 * Generates ores in the current chunk
 	 */
 	protected void generateOres() {
 		this.genStandardOre1(20, this.dirtGen, 0, 128);
 		this.genStandardOre1(10, this.gravelGen, 0, 128);
 		this.genStandardOre1(20, this.coalGen, 0, 128);
 		this.genStandardOre1(20, this.ironGen, 0, 64);
 		this.genStandardOre1(2, this.goldGen, 0, 32);
 		this.genStandardOre1(8, this.redstoneGen, 0, 16);
 		this.genStandardOre1(1, this.diamondGen, 0, 16);
 		this.genStandardOre2(1, this.lapisGen, 16, 16);
 	}
 }
