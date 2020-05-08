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
 
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class World
 {
 	Config config;
 
 	// Chunk lookup map
 	Map< Collection< Integer >, Chunk > chunks = new HashMap< Collection< Integer >, Chunk >();
 
 	public World( Config config )
 	{
 		this.config = config;
 	}
 
 	// Checks if a chunk is loaded and returns true if so
 	public boolean is_chunk_loaded( int x, int y, int z )
 	{
 		return chunks.containsKey( get_chunk_id( x, y, z ) );
 	}
 
 	public Chunk get_chunk( int x, int y, int z )
 	{
 		ArrayList<Integer> id = get_chunk_id( x, y, z );
 
 		if ( chunks.containsKey( get_chunk_id( x, y, z ) ) )
 		{
 			return chunks.get( id );
 		}
 
 		return null;
 	}
 
 	// Sets a chunk in the world to the given chunk.
	// Doesn't care about overwriting existing chunks
 	// Marks neighbors for update after.
 	public void set_chunk( int x, int y, int z, Chunk chunk )
 	{
 		if ( chunk == null ) return;
 
 		chunks.put( get_chunk_id( x, y, z ), chunk );
 		mark_neighbors( x, y, z );
 	}
 
 	void mark_neighbors( int x, int y, int z )
 	{
 		if ( chunks.containsKey( get_chunk_id( x - Constants.Chunk.side_length, y, z ) ) )
 			chunks.get( get_chunk_id( x - Constants.Chunk.side_length, y, z ) ).update_timestamp();
 
 		if ( chunks.containsKey( get_chunk_id( x + Constants.Chunk.side_length, y, z ) ) )
 			chunks.get( get_chunk_id( x + Constants.Chunk.side_length, y, z ) ).update_timestamp();
 
 		if ( chunks.containsKey( get_chunk_id( x, y - Constants.Chunk.side_length, z ) ) )
 			chunks.get( get_chunk_id( x, y - Constants.Chunk.side_length, z ) ).update_timestamp();
 
 		if ( chunks.containsKey( get_chunk_id( x, y + Constants.Chunk.side_length, z ) ) )
 			chunks.get( get_chunk_id( x, y + Constants.Chunk.side_length, z ) ).update_timestamp();
 
 		if ( chunks.containsKey( get_chunk_id( x, y, z - Constants.Chunk.side_length ) ) )
 			chunks.get( get_chunk_id( x, y, z - Constants.Chunk.side_length ) ).update_timestamp();
 
 		if ( chunks.containsKey( get_chunk_id( x, y, z + Constants.Chunk.side_length ) ) )
 			chunks.get( get_chunk_id( x, y, z + Constants.Chunk.side_length ) ).update_timestamp();
 	}
 
 	public static ArrayList<Integer> get_chunk_id( int x, int y, int z )
 	{
 		ArrayList<Integer> id = new ArrayList<Integer>();
 
 		int[] coords = Coord.GlobalToChunk( x, y, z );
 
 		id.add( coords[0] );
 		id.add( coords[1] );
 		id.add( coords[2] );
 
 		return id;
 	}
 
 	public int get_block( int x, int y, int z )
 	{
 		Chunk c = chunks.get( get_chunk_id( x, y, z ) );
 
 		// If chunk is not loaded, block is air
 		if ( c == null )
 			return Constants.Blocks.air;
 		else // Otherwise, return the loaded block
 			return c.get_block( x, y, z );
 	}
 
 	public int get_block( float x, float y, float z )
 	{
 		return get_block( Math.round( x ), Math.round( y ), Math.round( z ) );
 	}
 
 	// Sets the block id of a block in the world if that chunk is
 	// loaded
 	public void set_block( int x, int y, int z, int id )
 	{
 		Chunk c = chunks.get( get_chunk_id( x, y, z ) );
 
 		// Don't operate on null, otherwise, set the block
 		if ( c != null )
 			c.set_block( x, y, z, id );
 	}
 
 	public void set_block( float x, float y, float z, int id )
 	{
 		set_block( Math.round( x ), Math.round( y ), Math.round( z ), id );
 	}
 
 	public AABB get_hit_box( int x, int y, int z )
 	{
 		int id = get_block( x, y, z );
 
 		if ( id == Constants.Blocks.air )
 			return null;
 
 		AABB box = new Air().bounds();
 		Vector3f.add( box.pos, new Vector3f( x, y, z ), box.pos );
 		return box;
 	}
 }
