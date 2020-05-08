 package com.nexized.cross.world.biome;
 
 import net.minecraft.block.Block;
 import net.minecraft.world.biome.BiomeGenBase;
 
 public class BiomeRedwoodForest extends BiomeGenBase{
 
 	public BiomeRedwoodForest(int par1) {
 		super(par1);
 		this.topBlock = (byte)Block.grass.blockID;
 		this.fillerBlock = (byte)Block.dirt.blockID;
		theBiomeDecorator.treesPerChunk = 10;
		
 	}
 
 
 }
