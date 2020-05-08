 /********************************************************************************
  *                                                                              *
  *  This file is part of Recraft.                                               *
  *                                                                              *
  *  Recraft is free software: you can redistribute it and/or modify             *
  *  it under the terms of the GNU General Public License as published by        *
  *  the Free Software Foundation, either version 3 of the License, or           *
  *  (at your option) any later version.                                         *
  *                                                                              *
  *  Recraft is distributed in the hope that it will be useful,                  *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  *  GNU General Public License for more details.                                *
  *                                                                              *
  *  You should have received a copy of the GNU General Public License           *
  *  along with Recraft.  If not, see <http://www.gnu.org/licenses/>.            *
  *                                                                              *
  *  Copyright 2012 Chris Foster.                                                *
  *                                                                              *
  ********************************************************************************/
 
 package recraft.terrainchunk.rehash;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import recraft.core.Biome;
 import recraft.core.Block;
 import recraft.core.TerrainChunk;
 import recraft.util.IntVector3;
 
 public class RehashTerrainChunk implements TerrainChunk
 {
	public static final IntVector3 defaultSize = new IntVector3(16, 256, 16);
 	private final IntVector3 size;
 
 	private final short[] basicData;
 	private final HashMap<IntVector3, Block> extendedData;
 
 	private final HashSet<IntVector3> constantUpdateList;
 
 	public RehashTerrainChunk(IntVector3 size)
 	{
 		if (size == null)
			size = new IntVector3(defaultSize);
 
		this.size = new IntVector3(size);
 
 		this.basicData = new short[this.size.x * this.size.y * this.size.z];
 		this.extendedData = new HashMap<IntVector3, Block>();
 
 		this.constantUpdateList = new HashSet<IntVector3>();
 	}
 
 	@Override
 	public boolean setBlock(IntVector3 location, Block block)
 	{
 		if (!this.containsLocation(location))
 			return false;
 
 		// Delete previous block data (if any).
 		this.extendedData.remove(location);
 		this.constantUpdateList.remove(location);
 
 		if (block.isSimpleBlock())
 			this.basicData[arrayIndex(location)] = (short)block.getID();
 
 		if (block.requiresConstantUpdate())
 			this.constantUpdateList.add(location);
 
 		return true;
 	}
 
 	@Override
 	public Block getBlock(IntVector3 location)
 	{
 		if (!this.containsLocation(location))
 			return null;
 
 		Block extended = this.extendedData.get(location);
 		if (extended != null)
 			return extended;
 
 		short id = this.basicData[arrayIndex(location)];
 		Block block = Block.registry.get(id);
 
 		return block;
 	}
 
 	@Override
 	public boolean setBiome(IntVector3 location, Biome biome)
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Biome getBiome(IntVector3 location)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IntVector3 getSize()
 	{
		return new IntVector3(this.size);
 	}
 
 	@Override
 	public boolean containsLocation(IntVector3 location)
 	{
 		if (location.x < 0 || location.x >= this.size.x)
 			return false;
 		if (location.y < 0 || location.y >= this.size.y)
 			return false;
 		if (location.z < 0 || location.z >= this.size.z)
 			return false;
 
 		return true;
 	}
 
 	private int arrayIndex(IntVector3 location)
 	{
 		return location.x + this.size.x * location.z + this.size.x * this.size.z * location.y;
 	}
 
 }
