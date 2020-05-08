 package com.ftwinston.KillerMinecraft.Modules.FlyingIslands;
 
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
 
 		final byte air = (byte)Material.AIR.getId();
 		final byte dirt = (byte)Material.DIRT.getId();
 		
 		final int blockLayers = 12;
 		
 		Random r = new Random();
 		long seed = r.nextLong();
 		
 		public byte[][] generateBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
 		{
 			if ( !isChunkSolid(cx, cz))
 				return new byte[1][];
 			
 			byte[][] chunk = new byte[16][];
 			
 			boolean north = isChunkSolid(cx, cz-1), south = isChunkSolid(cx, cz+1);
 			boolean east = isChunkSolid(cx+1, cz), west = isChunkSolid(cx-1, cz);
 			
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
 		private boolean isChunkSolid(int cx, int cz)
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
 			byte material = dirt;
 			int num = (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);
 			
 			if ( num == 4 )
 			{
 				return applyMaterialsToShapeTemplate(templateNESW, material, Rotation.ALL);
 			}
 			else if ( num == 3 )
 			{
 				byte[] data = applyMaterialsToShapeTemplate(templateAllButNorth, material, Rotation.FLIP_HORIZONTAL);
 				if ( !south )
 					Rotation.ROTATE_180.perform(data);
 				else if ( !east )
 					Rotation.CLOCKWISE_90.perform(data);
 				else if ( !west )
 					Rotation.ANTICLOCKWISE_90.perform(data);
 				return data;
 			}
 			else if ( num == 2 )
 			{
 				byte[] data;
 				if ( north )
 				{
 					if ( south )
 						return applyMaterialsToShapeTemplate(templateNorthSouth, material, Rotation.FLIP_HORIZONTAL, Rotation.FLIP_VERTICAL, Rotation.ROTATE_180);
 					
					data = applyMaterialsToShapeTemplate(templateSouthWest, material, east ? Rotation.TRANSPOSE_INVERSE : Rotation.TRANSPOSE);
 					if ( east )
 						Rotation.ROTATE_180.perform(data);
 					else
 						Rotation.CLOCKWISE_90.perform(data);
 				}
 				else if ( south )
 				{
					data = applyMaterialsToShapeTemplate(templateSouthWest, material, east ? Rotation.TRANSPOSE : Rotation.TRANSPOSE_INVERSE);
 					if ( east )
 						Rotation.ANTICLOCKWISE_90.perform(data);
 				}
 				else
 				{
 					data = applyMaterialsToShapeTemplate(templateNorthSouth, material, Rotation.FLIP_HORIZONTAL, Rotation.FLIP_VERTICAL, Rotation.ROTATE_180);
 					Rotation.CLOCKWISE_90.perform(data);
 				}
 				return data;
 			}
 			else if ( num == 1 )
 			{
 				byte[] data = applyMaterialsToShapeTemplate(templateNorthOnly, material, Rotation.FLIP_HORIZONTAL);
 				if ( south )
 					Rotation.ROTATE_180.perform(data);
 				else if ( east )
 					Rotation.CLOCKWISE_90.perform(data);
 				else if ( west )
 					Rotation.ANTICLOCKWISE_90.perform(data);
 				return data;
 			}
 			else // num == 0
 			{
 				return applyMaterialsToShapeTemplate(templateNoAdjoining, material, Rotation.ALL);
 			}
 		}
 		
 		final byte XX = 01,
 			a1 = 02, a2 = 03, a3 = 04, a4 = 05, a5 = 06, a6 = 07, a7 =  8, a8 =  9, a9 = 10,
 			A1 = 11, A2 = 12, A3 = 13, A4 = 14, A5 = 15, A6 = 16, A7 = 17, A8 = 18, A9 = 19,
 			b1 = 20, b2 = 21, b3 = 22, b4 = 23, b5 = 24, b6 = 25, b7 = 26, b8 = 27, b9 = 28,
 			B1 = 29, B2 = 30, B3 = 31, B4 = 32, B5 = 33, B6 = 34, B7 = 35, B8 = 36, B9 = 37,
 			c1 = 38, c2 = 39, c3 = 40, c4 = 41, c5 = 42, c6 = 43, c7 = 44, c8 = 45, c9 = 46,
 			C1 = 47, C2 = 48, C3 = 49, C4 = 50, C5 = 51, C6 = 52, C7 = 53, C8 = 54, C9 = 55,
 			d1 = 56, d2 = 57, d3 = 58, d4 = 59, d5 = 60, d6 = 61, d7 = 62, d8 = 63, d9 = 64,
 			D1 = 65, D2 = 66, D3 = 67, D4 = 68, D5 = 69, D6 = 70, D7 = 71, D8 = 72, D9 = 73,
 			e1 = 74, e2 = 75, e3 = 76, e4 = 77, e5 = 78, e6 = 79, e7 = 80, e8 = 81, e9 = 82,
 			E1 = 83, E2 = 84, E3 = 85, E4 = 86, E5 = 87, E6 = 88, E7 = 89, E8 = 90, E9 = 91,
 			f1 = 92, f2 = 93, f3 = 94, f4 = 95, f5 = 96, f6 = 97, f7 = 98, f8 = 99, f9 = 100,
 			F1 = 101, F2 = 102, F3 = 103, F4 = 104, F5 = 105, F6 = 106, F7 = 107, F8 = 108, F9 = 109, 
 			numSolidValues = 110;
 		
 		private byte[] applyMaterialsToShapeTemplate(byte[][] templateSet, byte solid, Rotation... allowedRotations)
 		{
 			byte[] template = templateSet[r.nextInt(templateSet.length)];
 			byte[] data = new byte[template.length];
 			
 			byte[] materials = new byte[numSolidValues];
 			materials[XX] = solid;
 			
 			// for each of the "random" block tracks, decide which values correspond to solid blocks
 			
 			int rand = r.nextInt(10);
 			int[] forwardIndices = new int[] { 0, a1, a2, a3, a4, a5, a6, a7, a8, a9 };
 			int[] backwardIndices = new int[] { 0, A1, A2, A3, A4, A5, A6, A7, A8, A9 };
 			for ( int i=1; i<forwardIndices.length; i++ )
				materials[i <= rand ? forwardIndices[i] : backwardIndices[i]] = solid;
 				
 			rand = r.nextInt(10);
 			forwardIndices = new int[] { 0, b1, b2, b3, b4, b5, b6, b7, b8, b9 };
 			backwardIndices = new int[] { 0, B1, B2, B3, B4, B5, B6, B7, B8, B9 };
 			for ( int i=1; i<forwardIndices.length; i++ )
				materials[i <= rand ? forwardIndices[i] : backwardIndices[i]] = solid;
 				
 			rand = r.nextInt(10);
 			forwardIndices = new int[] { 0, c1, c2, c3, c4, c5, c6, c7, c8, c9 };
 			backwardIndices = new int[] { 0, C1, C2, C3, C4, C5, C6, C7, C8, C9 };
 			for ( int i=1; i<forwardIndices.length; i++ )
				materials[i <= rand ? forwardIndices[i] : backwardIndices[i]] = solid;
 				
 			rand = r.nextInt(10);
 			forwardIndices = new int[] { 0, d1, d2, d3, d4, d5, d6, d7, d8, d9 };
 			backwardIndices = new int[] { 0, D1, D2, D3, D4, D5, D6, D7, D8, D9 };
 			for ( int i=1; i<forwardIndices.length; i++ )
				materials[i <= rand ? forwardIndices[i] : backwardIndices[i]] = solid;
 				
 			rand = r.nextInt(10);
 			forwardIndices = new int[] { 0, e1, e2, e3, e4, e5, e6, e7, e8, e9 };
 			backwardIndices = new int[] { 0, E1, E2, E3, E4, E5, E6, E7, E8, E9 };
 			for ( int i=1; i<forwardIndices.length; i++ )
				materials[i <= rand ? forwardIndices[i] : backwardIndices[i]] = solid;
 				
 			rand = r.nextInt(10);
 			forwardIndices = new int[] { 0, f1, f2, f3, f4, f5, f6, f7, f8, f9 };
 			backwardIndices = new int[] { 0, F1, F2, F3, F4, F5, F6, F7, F8, F9 };
 			for ( int i=1; i<forwardIndices.length; i++ )
				materials[i <= rand ? forwardIndices[i] : backwardIndices[i]] = solid;
 			
 			// apply this to the data
 			for ( int i=0; i<data.length; i++ )
 				data[i] = materials[template[i]];
 			
 			// and rotate the data in one of the allowed fashions
 			Rotation.performOne(data, r, true, allowedRotations);
 				
 			return data;
 		}
 		
 		final byte[][] templateNESW = new byte[][] { {
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX
 		} };
 		
 		final byte[][] templateNoAdjoining = new byte[][] { {
 			00,00,00,00,00,XX,XX,XX,XX,XX,XX,00,00,00,00,00,
 			00,00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,
 			00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 			00,00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,
 			00,00,00,00,00,XX,XX,XX,XX,XX,XX,00,00,00,00,00
 		} };
 		
 		final byte[][] templateNorthOnly = new byte[][] { {
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 			00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 			00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 			00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 			00,00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,
 			00,00,00,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,
 			00,00,00,00,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,00,
 			00,00,00,00,00,XX,XX,XX,XX,XX,XX,00,00,00,00,00,
 			00,00,00,00,00,00,XX,XX,XX,XX,00,00,00,00,00,00,
 			00,00,00,00,00,00,00,XX,XX,00,00,00,00,00,00,00
 		} };
 		
 		final byte[][] templateAllButNorth = new byte[][] { {		
 			XX,XX,00,00,00,00,00,00,00,00,00,00,00,00,XX,XX,
 			XX,XX,XX,XX,XX,XX,00,00,00,00,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 			XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX
 		} };
 		
 		final byte[][] templateSouthWest = new byte[][] {
 			{
 				XX,XX,XX,00,00,00,00,00,00,00,00,00,00,00,00,00,
 				XX,XX,XX,XX,XX,XX,XX,00,00,00,00,00,00,00,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,00,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,00,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX
 			}
 		};
 		
 		final byte[][] templateNorthSouth = new byte[][] {
 			{
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 				a5,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b5,
 				a4,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b4,
 				a4,a6,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b6,b4,
 				a3,a5,a7,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b7,b5,b3,
 				a3,a4,a6,a7,a8,XX,XX,XX,XX,XX,XX,b8,b7,b6,b4,b3,
 				a2,a3,a4,a5,a7,a7,a9,XX,XX,b9,b7,b7,b5,b4,b3,b2,
 				a1,a2,a4,a5,a6,a7,a8,a9,b9,b8,b7,b6,b5,b4,b2,b1,
 				a1,a2,a4,a5,a6,a7,a8,a9,b9,b8,b7,b6,b5,b4,b2,b1,
 				a2,a3,a4,a5,a7,a7,a9,XX,XX,b9,b7,b7,b5,b4,b3,b2,
 				a3,a4,a6,a7,a8,XX,XX,XX,XX,XX,XX,b8,b7,b6,b4,b3,
 				a3,a5,a7,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b7,b5,b3,
 				a4,a6,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b6,b4,
 				a4,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b4,
 				a5,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,b5,
 				XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX
 			},
 			{
 				a9,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 				a6,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,c6,
 				a6,a8,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,c9,c6,c5,
 				a5,a7,a9,XX,XX,XX,XX,XX,XX,XX,XX,XX,c9,c7,c5,c3,
 				a5,a7,a9,XX,XX,XX,XX,XX,XX,XX,XX,c9,c9,c7,c5,c3,
 				a6,a8,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,c8,c6,c5,
 				a6,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,c8,c6,
 				a9,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,c8,
 				b9,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 				b8,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 				b8,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,
 				b7,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,d9,
 				b7,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,d9,d7,
 				b8,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,d8,d4,
 				b8,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,d9,d7,
 				b9,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,XX,d9
 			}
 		};
 	}
 }
