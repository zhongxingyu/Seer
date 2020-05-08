 package com.titankingdoms.nodinchan.overworld.test.simplex;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.BlockVector;
 import org.bukkit.util.Vector;
 
 public class SimplexGen extends ChunkGenerator {
 	
 	private final Map<Biome, SimplexBiome> biomes;
 	
 	public SimplexGen() {
 		this.biomes = new HashMap<Biome, SimplexBiome>();
 	}
 	
 	@Override
 	public boolean canSpawn(World world, int x, int z) {
 		if (world.getHighestBlockAt(x, z).getRelative(BlockFace.DOWN).isEmpty())
 			return false;
 		
 		if (world.getHighestBlockAt(x, z).getRelative(BlockFace.DOWN).isLiquid())
 			return false;
 		
 		return true;
 	}
 	
 	public int[][][] generateBlockMap(World world, int cx, int cz) {
 		int[][][] blockMap = new int[16][world.getMaxHeight()][16];
 		
 		int radius = 8;
 		double scale = 1 / 289.0;
 		
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
 				SimplexBiome biome = new SimplexBiome(
 						new SimplexOctave(world, 0.0, 0, 0.0, 0.0),
 						new SimplexOctave(world, 0.0, 0, 0.0, 0.0), 0);
 				
 				for (int offsetX = -radius; offsetX <= radius; offsetX++) {
 					for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
 						biome.add(biomes.get(world.getBiome(x + cx * 16 + offsetX, z + cz * 16 + offsetZ)), scale);
 					}
 				}
 				
 				double base = (biome.baseHeight() == 0) ? 64 : biome.baseHeight();
 				
 				double height = biome.height().calc((x + cx * 16) / 16.0, (z + cz * 16) / 16.0, true) + base;
 				
 				for (int y = 0; y <= height; y++) {
 					double density = 0.0;
 					
 					if (y + 1 > height / 2)
 						density = (y + 1.0 - height / 2.0) * (4.0 / (3.0 * height));
 					else if (y + 1 < height / 2)
 						density = (height / 2.0 - y + 1.0) * (4.0 / (3.0 * height));
 					else
 						density = (y + 1.0 - height / 2.0);
 					
 					if (biome.density().calc((x + cx * 16) / 16.0, y / 16.0, (z + cz * 16) / 16.0, true) > density)
 						blockMap[x][y][z] = 1;
 					else
 						blockMap[x][y][z] = -1;
 				}
 			}
 		}
 		
 		return blockMap;
 	}
 	
 	@Override
 	public byte[][] generateBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes) {
 		byte[][] result = new byte[world.getMaxHeight() / 16][];
 		
 		int[][][] blockMap = generateBlockMap(world, cx, cz);
 		
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
 				int surface = -1;
 				
 				for (int y = world.getMaxHeight(); y >= 0; y--) {
 					if (y == 0) {
 						setBlock(result, x, y, z, Material.BEDROCK.getId());
 						break;
 					}
 					
 					if (y <= 64 && y > 0) {
 						setBlock(result, x, y, z, Material.STATIONARY_WATER.getId());
 						
 						if (y == 64) {
 							if (biomes.getBiome(x, z).equals(Biome.FROZEN_OCEAN) || biomes.getBiome(x, z).equals(Biome.FROZEN_RIVER))
 								setBlock(result, x, y, z, Material.ICE.getId());
 						}
 					}
 					
					if (blockMap[x][y][z] > 0) {
 						if (surface < 0)
 							surface = y;
 						
 						generateWorld(result, x, surface - y, surface, z, biomes.getBiome(x, z));
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	public void generateWorld(byte[][] result, int x, int depth, int height, int z, Biome biome) {
 		if (depth == 0) {
 			switch (biome) {
 			
 			case ICE_DESERT:
 				setBlock(result, x, height - depth + 1, z, Material.SNOW.getId());
 			
 			case BEACH:
 			case DESERT:
 			case DESERT_HILLS:
 				setBlock(result, x, height - depth, z, Material.SAND.getId());
 				break;
 				
 			case FROZEN_OCEAN:
 			case FROZEN_RIVER:
 			case OCEAN:
 			case RIVER:
 				setBlock(result, x, height - depth, z, Material.SAND.getId());
 				break;
 				
 			case ICE_MOUNTAINS:
 			case ICE_PLAINS:
 				setBlock(result, x, height - depth + 1, z, Material.SNOW.getId());
 				
 				default:
 					setBlock(result, x, height - depth, z, Material.GRASS.getId());
 					break;
 			}
 			return;
 		}
 		
 		switch (biome) {
 		
 		case BEACH:
 		case DESERT:
 		case DESERT_HILLS:
 		case ICE_DESERT:
 			if (depth < 6)
 				setBlock(result, x, height - depth, z, Material.SAND.getId());
 			
 			else if (depth < 14)
 				setBlock(result, x, height - depth, z, Material.SANDSTONE.getId());
 			
 			else
 				setBlock(result, x, height - depth, z, Material.STONE.getId());
 			
 			break;
 			
 		case FROZEN_OCEAN:
 		case FROZEN_RIVER:
 		case OCEAN:
 		case RIVER:
 			if (depth < 4)
 				setBlock(result, x, height - depth, z, Material.SAND.getId());
 			
 			else if (depth < 12)
 				setBlock(result, x, height - depth, z, Material.DIRT.getId());
 			
 			else
 				setBlock(result, x, height - depth, z, Material.STONE.getId());
 			break;
 			
 			default:
 				if (depth < 6)
 					setBlock(result, x, height - depth, z, Material.DIRT.getId());
 				
 				else
 					setBlock(result, x, height - depth, z, Material.STONE.getId());
 				
 				break;
 		}
 	}
 	
 	public int getBlock(byte[][] result, int x, int y, int z) {
 		if (result[y >> 4] == null)
 			return 0;
 		
 		return result[y >> 4][((y & 0xF) << 8) | ((z & 0xF) << 4) | (x & 0xF)];
 	}
 	
 	@Override
 	public Location getFixedSpawnLocation(World world, Random random) {
 		Vector centre = new BlockVector(0, world.getHighestBlockYAt(0, 0) - 1, 0);
 		
 		for (int x = -5; x <= 5; x++) {
 			for (int z = -5; z <= 5; z++) {
 				Vector position = centre.clone().add(new Vector(x, 0, z));
 				Block block = position.toLocation(world).getBlock();
 				double distance = centre.distance(position);
 				
 				if (distance <= 0.5) {
 					block.setType(Material.GLOWSTONE);
 				} else if (distance <= 1.5) {
 					block.setType(Material.SMOOTH_BRICK);
 				} else if (distance <= 2.5) {
 					block.setType(Material.GLOWSTONE);
 				} else if (distance <= 3.5) {
 					block.setType(Material.SMOOTH_BRICK);
 				} else if (distance <= 4.5) {
 					block.setType(Material.GLOWSTONE);
 				} else if (distance <= 5.5) {
 					block.setType(Material.SMOOTH_BRICK);
 				}
 			}
 		}
 		
 		return centre.toLocation(world).add(0, 1, 0);
 	}
 	
 	public void setBlock(byte[][] result, int x, int y, int z, int blockId) {
 		if (result[y >> 4] == null)
 			result[y >> 4] = new byte[4096];
 		
 		result[y >> 4][((y & 0xF) << 8) | ((z & 0xF) << 4) | (x & 0xF)] = (byte) blockId;
 	}
 }
