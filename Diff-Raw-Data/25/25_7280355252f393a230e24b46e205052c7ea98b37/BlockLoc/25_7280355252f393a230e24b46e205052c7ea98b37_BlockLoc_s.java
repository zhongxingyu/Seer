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
 
 public class BlockLoc
 {
 	public World world;
 	public int x, y, z;
 
 	public BlockLoc( int x, int y, int z, World world )
 	{
 		this.world = world;
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 
 	public BlockLoc( BlockChunkLoc loc )
 	{
 		this( loc.chunk.x + loc.x, loc.chunk.y + loc.y, loc.chunk.z + loc.z, loc.chunk.get_world() );
 	}
 
 	public Chunk get_chunk()
 	{
 		if ( world == null )
 			return null;
 
 		return world.get_chunk( x, y, z );
 	}
 
 	public boolean available()
 	{
 		return world.is_chunk_loaded( x, y, z );
 	}
 
 	public int get()
 	{
		Chunk c = get_chunk();
 
 		if ( c == null )
 			return Constants.Blocks.air;
 
 		return c.get_block( x, y, z );
 	}
 
 	public void set( int id )
 	{
 		world.set_block( x, y, z, id );
 	}
 
 	public BlockLoc get( Constants.Direction dir )
 	{
 		switch ( dir )
 		{
 			case North:
 				return new BlockLoc( x, y, z - 1, world );
 			case East:
 				return new BlockLoc( x + 1, y, z, world );
 			case South:
 				return new BlockLoc( x, y, z + 1, world );
 			case West:
 				return new BlockLoc( x - 1, y, z, world );
 			case Up:
 				return new BlockLoc( x, y + 1, z, world );
 			case Down:
 				return new BlockLoc( x, y - 1, z, world );
 			default:
 				return this;
 		}
 	}
 }
