 /**
  * Pony's Handy Dandy Rape Generator
  *
  * INSERT BSD HERE
  */
 package net.nexisonline.spade.chunkproviders;
 
 import java.util.logging.Logger;
 
 import libnoiseforjava.module.ModuleBase;
 import libnoiseforjava.module.Multiply;
 import libnoiseforjava.module.Perlin;
 import libnoiseforjava.module.RidgedMulti;
 import libnoiseforjava.module.Turbulence;
 
 import org.bukkit.ChunkProvider;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 
 /**
  * @author PrettyPonyyy
  *
  */
 public class ChunkProviderWat extends ChunkProvider
 {
 	private ModuleBase m_perlinGenerator1;
 	private ModuleBase m_perlinGenerator2;
 	private Multiply m_multiplier;
 	private Turbulence m_turbulence;
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.bukkit.ChunkProvider#onLoad(org.bukkit.World, long)
 	 */
 	@Override
 	public void onLoad(World world, long seed)
 	{
 		this.setHasCustomTerrain(true);
 
 		try
 		{
 			m_perlinGenerator1 = new RidgedMulti(); //new Perlin();
 			m_perlinGenerator2 = new RidgedMulti(); //new Perlin();
 			m_multiplier = new Multiply(m_perlinGenerator1, m_perlinGenerator2);
 			m_turbulence = new Turbulence(m_perlinGenerator1);
 
 			((RidgedMulti)m_perlinGenerator1).setSeed((int)(seed*1024));
 			((RidgedMulti)m_perlinGenerator1).setOctaveCount(1);
 			((RidgedMulti)m_perlinGenerator1).setFrequency(0.5f);//1.0f);
 			((RidgedMulti)m_perlinGenerator1).setLacunarity(0.25f);
 
 			((RidgedMulti)m_perlinGenerator2).setSeed((int)(seed));
 			((RidgedMulti)m_perlinGenerator2).setOctaveCount(1);
 
 			m_turbulence.setSeed(135);
 			m_turbulence.setPower(0.125);
 		}
 		catch (Exception e)
 		{
 		}
 	}
 
 	private static double lerp(double a, double b, double f)
 	{
 		return (a + (b - a) * f);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.bukkit.ChunkProvider#generateChunk(int, int, byte[],
 	 * org.bukkit.block.Biome[], double[])
 	 */
 	@Override
 	public void generateChunk(World world, int X, int Z, byte[] abyte, Biome[] biomes, double[] temperature)
 	{
 		double density[][][] = new double[16][128][16];
 
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y += 3)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					double posX = x + (X*16);
 					double posY = y - 64;
 					double posZ = z + (Z*16);
 
					final double warp = 0.04;
					double warpMod = m_perlinGenerator2.getValue(posX * warp, posY * warp, posZ * warp) * 5;
 					double warpPosX = posX * warpMod;
 					double warpPosY = posY * warpMod;
 					double warpPosZ = posZ * warpMod;
 
 					double mod = m_perlinGenerator1.getValue(warpPosX * 0.0005, warpPosY * 0.0005, warpPosZ * 0.0005);
 
 					density[x][y][z] = -(y - 64);
 					density[x][y][z] += mod * 100;
 				}
 			}
 		}
 
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y += 3)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					if (y != 126)
 					{
 						density[x][y+1][z] = lerp(density[x][y][z], density[x][y+3][z], 0.2);
 						density[x][y+2][z] = lerp(density[x][y][z], density[x][y+3][z], 0.8);
 					}
 				}
 			}
 		}
 
 		for (int x = 0; x < 16; x += 3)
 		{
 			for (int y = 0; y < 128; y++)
 			{
 				for (int z = 0; z < 16; z += 3)
 				{
 					if (x == 0 && z > 0)
 					{
 						density[x][y][z-1] = lerp(density[x][y][z], density[x][y][z-3], 0.25);
 						density[x][y][z-2] = lerp(density[x][y][z], density[x][y][z-3], 0.85);
 					}
 					else if (x > 0 && z > 0)
 					{
 						density[x-1][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.25);
 						density[x-2][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.85);
 
 						density[x][y][z-1] = lerp(density[x][y][z], density[x][y][z-3], 0.25);
 						density[x-1][y][z-1] = lerp(density[x][y][z], density[x-3][y][z-3], 0.25);
 						density[x-2][y][z-1] = lerp(density[x][y][z], density[x-3][y][z-3], 0.85);
 
 						density[x][y][z-2] = lerp(density[x][y][z], density[x][y][z-3], 0.25);
 						density[x-1][y][z-2] = lerp(density[x][y][z], density[x-3][y][z-3], 0.85);
 						density[x-2][y][z-2] = lerp(density[x][y][z], density[x-3][y][z-3], 0.85);
 					}
 					else if (x > 0 && z == 0)
 					{
 						density[x-1][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.25);
 						density[x-2][y][z] = lerp(density[x][y][z], density[x-3][y][z], 0.85);
 					}
 				}
 			}
 		}
 
 		for (int x = 0; x < 16; x++)
 		{
 			for (int y = 0; y < 128; y++)
 			{
 				for (int z = 0; z < 16; z++)
 				{
 					if (density[x][y][z] > 0)
 					{
 						abyte[getBlockIndex(x,y,z)] = 1;
 					}
 					else
 					{
 						abyte[getBlockIndex(x,y,z)] = 0;
 					}
 					// Origin point + sand to prevent 5000 years of loading.
 					if ((x == 0) && (z == 0) && (X == x) && (Z == z) && (y <= 63)) {
 						abyte[getBlockIndex(x,y,z)] = (byte) ((y == 125) ? 12 : 7);
 					}
 					if(y==1)
 						abyte[getBlockIndex(x,y,z)]=7;
 				}
 			}
 		}
 
 		Logger.getLogger("Minecraft").info(String.format("[wat] Chunk (%d,%d)",X,Z));
 	}
 }
