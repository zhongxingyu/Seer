 package com.ftwinston.KillerMinecraft.Modules.FlyingIslands;
 
 import java.util.Arrays;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.World;
 
 import com.ftwinston.KillerMinecraft.Option;
 import com.ftwinston.KillerMinecraft.WorldConfig;
 import com.ftwinston.KillerMinecraft.WorldGenerator;
 
 public class FlyingIslands extends WorldGenerator
 {
 	@Override
 	public Option[] setupOptions()
 	{
 		return new Option[] { };
 	}
 	
 	@Override
 	public void setupWorld(WorldConfig world, Runnable runWhenDone)
 	{
 		if ( world.getEnvironment() == Environment.NORMAL )
 			world.setGenerator(new IslandGenerator());
 			
 		createWorld(world, runWhenDone);
 	}
 	
 	class IslandGenerator extends org.bukkit.generator.ChunkGenerator
 	{
 		static final int solidChance1x1 = 16, solidChance2x2 = 11, solidChance3x3 = 30;
 
 		final byte d = (byte)Material.DIRT.getId();
 		final byte grass = (byte)Material.GRASS.getId();
 		
 		final int blockLayers = 12;
 		
 		Random r = new Random();
 		long seed = r.nextLong();
 		
 		public byte[][] generateBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
 		{
 			if ( !isSolid(cx, cz))
 				return new byte[1][];
 			
 			byte[][] chunk = new byte[16][];
 			
 			boolean north = isSolid(cx, cz-1), south = isSolid(cx, cz+1);
 			boolean east = isSolid(cx+1, cz), west = isSolid(cx-1, cz);
 			
 			byte[] outline = selectChunkOutline(north, south, east, west, random);
 			
 			for ( int i=0; i<16; i++ )
 			{
 				byte[] layer = new byte[4096];
 				chunk[i] = layer;
 				
 				if ( i == 6 )
 				{
 					for ( int times = 0; times<blockLayers; times++ )
 						for ( int j=0; j<outline.length; j++ )
 							layer[outline.length * times + j] = outline[j];
 				} 
 			}
 			
 			return chunk; 
 		}
 /*		
 		private void setMaterialAt(byte[][] chunk, int x, int y, int z, Material material)
 		{
 			int sec_id = (y >> 4);
 			int yy = y & 0xF;
 			if (chunk[sec_id] == null)
 				chunk[sec_id] = new byte[4096];
 
 			chunk[sec_id][(yy << 8) | (z << 4) | x] = (byte) material.getId();
 		}
 */
 		private boolean isSolid(int cx, int cz)
 		{
 			r.setSeed((long) cx * 341873128712L + (long) cz * 132897987541L + seed);
 			if ( r.nextInt(solidChance1x1) == 0 )
 				return true;
 			
 			r.setSeed((long) Math.floor(cx/2) * 341873128712L + (long) Math.floor(cz/2) * 132897987541L + seed + seed);
 			if ( r.nextInt(solidChance2x2) == 0 )
 				return true;
 			
 			r.setSeed((int)Math.floor(cx/3) * 341873128712L + (int)Math.floor(cz/3) * 132897987541L + seed + seed + seed);
 			if ( r.nextInt(solidChance3x3) == 0 )
 				return true;
 			
 			return false;
 		}
 
 		private byte[] selectChunkOutline(boolean north, boolean south, boolean east, boolean west, Random random)
 		{
 			int num = (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);
 			
 			if ( num == 4 )
 			{
 				return Arrays.copyOf(templateNESW, 256);
 			}
 			else if ( num == 3 )
 			{
 				byte[] data = Arrays.copyOf(templateAllButNorth, 256);
 				if ( !north )
 					;
 				else if ( !south )
 					rotate180(data);
 				else if ( !east )
 					rotateClockwise90(data);
 				else if ( !west )
 					rotateAnticlockwise90(data);
 				return data;
 			}
 			else if ( num == 2 )
 			{
 				byte[] data;
 				if ( north )
 				{
 					if ( south )
 						return Arrays.copyOf(templateNorthSouth, 256);
 					
 					data = Arrays.copyOf(templateSouthWest, 256);
 					if ( east )
 						rotate180(data);
					else if ( west )
						rotateClockwise90(data);
 				}
 				else if ( south )
 				{
 					data = Arrays.copyOf(templateSouthWest, 256);
 					if ( east )
 						rotateAnticlockwise90(data);
 					else if ( west )
 						;
 				}
 				else
 				{
 					data = Arrays.copyOf(templateNorthSouth, 256);
 					rotateClockwise90(data);
 				}
 				return data;
 			}
 			else if ( num == 1 )
 			{
 				byte[] data = Arrays.copyOf(templateNorthOnly, 256);
 				if ( north )
 					;
 				else if ( south )
 					rotate180(data);
 				else if ( east )
 					rotateClockwise90(data);
 				else if ( west )
 					rotateAnticlockwise90(data);
 				return data;
 			}
 			else // num == 0
 			{
 				return Arrays.copyOf(templateNoAdjoining, 256);
 			}
 		}
 		
 		final byte[] templateNESW = new byte[] {
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d
 		};
 		
 		final byte[] templateNoAdjoining = new byte[] {
 			0,0,0,0,0,d,d,d,d,d,d,0,0,0,0,0,
 			0,0,0,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			0,0,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,0,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			0,0,0,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			0,0,0,0,0,d,d,d,d,d,d,0,0,0,0,0
 		};
 		
 		final byte[] templateNorthOnly = new byte[] {
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,0,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			0,0,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			0,0,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			0,0,0,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			0,0,0,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			0,0,0,0,d,d,d,d,d,d,d,d,0,0,0,0,
 			0,0,0,0,0,d,d,d,d,d,d,0,0,0,0,0,
 			0,0,0,0,0,0,d,d,d,d,0,0,0,0,0,0,
 			0,0,0,0,0,0,0,d,d,0,0,0,0,0,0,0
 		};
 		
 		final byte[] templateAllButNorth = new byte[] {		
 			d,d,0,0,0,0,0,0,0,0,0,0,0,0,d,d,
 			d,d,d,d,d,d,0,0,0,0,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d
 		};
 		
 		final byte[] templateSouthWest = new byte[] {
 			d,d,d,0,0,0,0,0,0,0,0,0,0,0,0,0,
 			d,d,d,d,d,d,d,0,0,0,0,0,0,0,0,0,
 			d,d,d,d,d,d,d,d,d,d,0,0,0,0,0,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,0,0,0,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d
 		};
 		
 		final byte[] templateNorthSouth = new byte[] {
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,0,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			0,0,0,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			0,0,0,0,d,d,d,d,d,d,d,d,0,0,0,0,
 			0,0,0,0,0,0,d,d,d,d,0,0,0,0,0,0,
 			0,0,0,0,0,0,0,d,d,0,0,0,0,0,0,0,
 			0,0,0,0,0,0,0,d,d,0,0,0,0,0,0,0,
 			0,0,0,0,0,0,d,d,d,d,0,0,0,0,0,0,
 			0,0,0,0,0,d,d,d,d,d,d,d,0,0,0,0,
 			0,0,0,d,d,d,d,d,d,d,d,d,d,0,0,0,
 			0,0,d,d,d,d,d,d,d,d,d,d,d,d,0,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			0,d,d,d,d,d,d,d,d,d,d,d,d,d,d,0,
 			d,d,d,d,d,d,d,d,d,d,d,d,d,d,d,d
 		};
 		
 		private void transpose(byte[] data)
 		{
 			for (int i = 0; i < 16; i++) 
 				for (int j = i; j < 16; j++)
 				{ 
 					int i1 = i + j*16, i2 = j + i*16;
 					byte temp = data[i1]; 
 					data[i1] = data[i2]; 
 					data[i2] = temp;
 				}
 		}
 		
 		private void flipVertical(byte[] data)
 		{
 			for (int i = 0; i < 16; i++) 
 				for (int j = 0; j < 8; j++)
 				{ 
 					int i1 = i + j*16, i2 = i + (15-j)*16;
 					byte temp = data[i1]; 
 					data[i1] = data[i2]; 
 					data[i2] = temp;
 				}
 		}
 		
 		private void flipHorizontal(byte[] data)
 		{
			for (int i = 0; i < 8; i++) 
				for (int j = 0; j < 16; j++)
 				{ 
 					int i1 = i + j*16, i2 = 15-i + j*16;
 					byte temp = data[i1]; 
 					data[i1] = data[i2]; 
 					data[i2] = temp;
 				}
 		}
 		
 		private void rotateClockwise90(byte[] input)
 		{
 			transpose(input);
 			flipHorizontal(input);
 		}
 		
 		private void rotateAnticlockwise90(byte[] input)
 		{
 			transpose(input);
 			flipVertical(input);
 		}
 		
 		private void rotate180(byte[] input)
 		{
 			flipHorizontal(input);
 			flipVertical(input);
 		}
 	}
 }
