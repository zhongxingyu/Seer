 /*
  * Copyright 2011, Erik Lund
  *
  * This file is part of Voxicity.
  *
  *  Voxicity is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Voxicity is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Voxicity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package voxicity;
 
 public class Chunk
 {
 	int x, y, z;
 
 	long write_timestamp = Time.get_time_ms();
  
 	World world;
 
 	RLETree blocks = new RLETree();
 
 	public Chunk( int x, int y, int z )
 	{
 		this.x = x * Constants.Chunk.side_length;
 		this.y = y * Constants.Chunk.side_length;
 		this.z = z * Constants.Chunk.side_length;
 
 		System.out.println( "Created chunk at " + x + " " + y + " " + z );
 
 		generate_blocks();
 	}
 
 	public int get_x()
 	{
 		return x;
 	}
 
 	public int get_y()
 	{
 		return y;
 	}
 
 	public int get_z()
 	{
 		return z;
 	}
 
 	public World get_world()
 	{
 		return world;
 	}
 
 	public int get_block_pos( int x, int y, int z )
 	{
 		if ( ( x < 0 ) || ( y < 0 ) || ( z < 0 ) )
 			return Constants.Chunk.block_number;
 
 		if ( ( x >= Constants.Chunk.side_length ) || ( y >= Constants.Chunk.side_length ) || ( z >= Constants.Chunk.side_length ) )
 			return Constants.Chunk.block_number;
 
 		int block_pos = 0;
 		block_pos += x;
 		block_pos += y * Constants.Chunk.side_length * Constants.Chunk.side_length;
 		block_pos += z * Constants.Chunk.side_length;
 
 		return block_pos;
 	}
 
 	public int get_block( int x, int y, int z )
 	{
 		int[] offset = Coord.GlobalToChunkOffset( x, y, z );
 		int block_pos = get_block_pos( offset[0], offset[1], offset[2] );
 
 		if ( ( block_pos < 0 ) || ( block_pos >= Constants.Chunk.block_number ) )
 			return Constants.Blocks.air;
 
 		return blocks.get( block_pos );
 	}
 
 	public void set_block( int x, int y, int z, int id )
 	{
 		int[] offset = Coord.GlobalToChunkOffset( x, y, z );
 		int block_pos = get_block_pos( offset[0], offset[1], offset[2] );
 		if ( ( block_pos < 0 ) || ( block_pos >= Constants.Chunk.block_number ) )
 		{
 			return;
 		}
 
 		blocks.set( block_pos, id );
 		update_timestamp();
 	}
 
 	public void generate_blocks()
 	{
 		long start = System.nanoTime();
 
 		double[][] heightmap = new double[Constants.Chunk.side_length][Constants.Chunk.side_length];
 
 		// Make a heightmap for this chunk
 		for ( int x = 0 ; x < Constants.Chunk.side_length ; x++ )
 			for ( int z = 0 ; z < Constants.Chunk.side_length ; z++ )
 				heightmap[x][z] = Noise.perlin( 0, ( this.x + x ) / 600.0f, 0, ( this.z + z ) / 600.0f );
 
 		for ( int x = 0 ; x < Constants.Chunk.side_length ; x++ )
 		{
 			for ( int y = 0 ; y < Constants.Chunk.side_length ; y++ )
 			{
 				for ( int z = 0 ; z < Constants.Chunk.side_length ; z++ )
 				{
 					double noise = Noise.perlin( 0, (this.x + x) / 10.0f, (this.y + y) / 20.0f, (this.z + z) / 10.0f );
 
 					//System.out.println( "Height factor: " + height_factor );
 					int ground_level = (int)Math.round(heightmap[x][z] * 2 + noise * 3);
 					//System.out.println( "x: " + ( this.x + x ) + " y: " + ( this.y + y ) + " z: " + ( this.z + z ) );
 					//System.out.println( "Ground level: " + ground_level );
 
 					if ( ( this.y + y ) > ground_level )
 						set_block( x, y, z, Constants.Blocks.air );
 					else if ( ( this.y + y ) == ground_level )
 						set_block( x, y, z, Constants.Blocks.grass );
 					else
 					{
 					//System.out.println( "Ground level: " + ground_level + " y-coord: " + ( this.y + y ) );
 						//System.out.println( "ground_level - ( y-coord ) = " + ( ground_level - ( this.y + y ) ) );
 						if ( ground_level - ( this.y + y ) <= 1 )
 							set_block( x, y, z, Constants.Blocks.dirt );
 						else
 							set_block( x, y, z, Constants.Blocks.stone );
 					}
 				}
 			}
 		}
 
 		System.out.println( blocks );
 		long end = System.nanoTime();
 	}
 
 	void update_timestamp()
 	{
 		write_timestamp = Time.get_time_ms();
 	}
 
 	public long get_timestamp()
 	{
 		return write_timestamp;
 	}
 }
