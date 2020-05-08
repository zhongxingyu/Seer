 package net.minecraft.src;
 
 import net.minecraft.api.*;
 import java.util.*;
 
 public class World
 	implements IWorld
 {
 	private IBlock blocksList[] = new IBlock[65536];
 	private String blocksName[] = new String[65536];
 	private IItem itemsList[] = new IItem[65536];
 	private String itemsName[] = new String[65536];
 	private byte itemsTracked[] = new byte[65536];
 	private byte itemsUTracked[] = new byte[65536];
 	private List<Chunk> chunksDropping = new ArrayList();
 	private List<Chunk> chunkList = new ArrayList();
 	private Chunk chunkCache = null;
 	
 	@Override
 	public int registerBlock(IBlock block, String name)
 	{
 		int j = -1;
 		
 		for(int i = 0; i < 65536; i++)
 		{
 			if(blocksName[i] == null)
 			{
 				if(j == -1)
 					j = i;
 			}
 			else if(blocksName[i] == name)
 			{
 				if(blocksList[i] != null)
 					throw new RuntimeException("'" + name + "' already exists as a block type");
 				blocksList[i] = block;
 				return i;
 			}
 		}
 		if(j == -1)
 			throw new RuntimeException("Maximum block IDs reached");
 		blocksName[j] = name;
 		blocksList[j] = block;
 		return j;
 	}
 	
 	@Override
 	public int registerItem(IItem item, String name)
 	{
 		int j = -1;
 		
 		for(int i = 0; i < 65536; i++)
 		{
 			if(itemsName[i] == null)
 			{
 				if(j == -1)
 					j = i;
 			}
 			else if(itemsName[i] == name)
 			{
 				if(itemsList[i] != null)
 					throw new RuntimeException("'" + name + "' already exists as an item type");
 				itemsList[i] = item;
 				return i;
 			}
 		}
 		if(j == -1)
 			throw new RuntimeException("Maximum item IDs reached");
 		itemsName[j] = name;
 		itemsList[j] = item;
 		return j;
 	}
 	
 	@Override
 	public int getBlockID(String name)
 	{
 		for(int i = 0; i < 65536; i++)
 			if(blocksName[i] == name)
 				return i;
 		return -1;
 	}
 	
 	@Override
 	public int getItemID(String name)
 	{
 		for(int i = 0; i < 65536; i++)
 			if(itemsName[i] == name)
 				return i;
 		return -1;
 	}
 	
 	@Override
 	public String getBlockName(int id)
 	{
 		if((id >= 0) && (id < 65536))
 			return blocksName[id];
 		return null;
 	}
 	
 	@Override
 	public String getItemName(int id)
 	{
 		if((id >= 0) && (id < 65536))
 			return itemsName[id];
 		return null;
 	}
 
 	@Override
 	public IItem newItem(int id)
 	{
		
 	}
 	
 	@Override
 	public IBlock getBlock(int id)
 	{
 		if((id >= 0) && (id < 65536))
 			return blocksList[id];
 		return null;
 	}
 	
 	@Override
 	public IItem getItem(int id)
 	{
 		if((id >= 0) && (id < 65536))
 			return itemsList[id];
 		return null;
 	}
 
 	@Override
 	public boolean isChunkLoaded(int x, int y, int z)
 	{
 		int tx = x & -16;
 		int ty = y & -16;
 		int tz = z & -16;
 		
 		if((chunkCache != null) && chunkCache.isXYZstart(tx, ty, tz))
 			return true;
 		for(Chunk i : chunkList)
 		{
 			if(i.isXYZstart(tx, ty, tz))
 			{
 				chunkCache = i;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int getBlockIDAt(int x, int y, int z)
 	{
 		int tx = x & -16;
 		int ty = y & -16;
 		int tz = z & -16;
 		
 		if((chunkCache != null) && chunkCache.isXYZstart(tx, ty, tz))
 			return chunkCache.getBlock(tx, ty, tz);
 		for(Chunk i : chunkList)
 		{
 			if(i.isXYZstart(tx, ty, tz))
 			{
 				chunkCache = i;
 				return i.getBlock(tx, ty, tz);
 			}
 		}
 		return -1;
 	}
 	
 	@Override
 	public int getBlockIDAt(Orientation o)
 	{
 		return getBlockIDAt(o.x, o.y, o.z);
 	}
 	
 	@Override
 	public IBlock getBlockAt(int x, int y, int z)
 	{
 		int k = getBlockIDAt(x, y, z);
 		if(k == -1)
 			return null;
 		return blocksList[k];
 	}
 	
 	@Override
 	public boolean setBlockIDAt(int id, int x, int y, int z, double fx, double fy, double fz)
 	{
 		int k = getBlockIDAt(x, y, z);
 		if(k == -1)
 			return false;
 		if(k == id)
 		{
 			IBlock b = blocksList[k];
 			b.onBlockRemove(this, x, y, z, fx, fy, fz);
 			b.onBlockAdd(this, x, y, z, fx, fy, fz);
 			return true;
 		}
 		blocksList[k].onBlockRemove(this, x, y, z, fx, fy, fz);
 		chunkCache.setBlock(x, y, z, id);
 		blocksList[id].onBlockAdd(this, x, y, z, fx, fy, fz);
 		return false;
 	}
 	
 	@Override
 	public boolean isBlockBlocking(int x, int y, int z, int face)
 	{
 		int k = getBlockIDAt(x, y, z);
 		if(k == -1)
 			return false;
 		return blocksList[k].isBlocking(this, x, y, z, face);
 	}
 	
 	@Override
 	public BetterQueue<Integer> getRayTrace(double x1, double y1, double z1, double x2, double y2, double z2)
 	{
 		BetterQueue<Integer> ret = new BetterQueue<Integer>();
 		
 		double dx = x2 - x1;
 		double dy = y2 - y1;
 		double dz = z2 - z1;
 		double tx, ty, tz, revs, dir;
 		int cx1, cy1, cz1;
 		int cx2, cy2, cz2;
 		
 		tx = Math.abs(dx);
 		ty = Math.abs(dy);
 		tz = Math.abs(dz);
 		if((tx == 0) & (ty == 0) & (tz == 0))
 		{
 			ret.add((int)x1).add((int)y1).add((int)z1);
 			return ret;
 		}
 		if(tx > ty)
 		{
 			if(tx > tz)
 			{
 				revs = tx * 2;
 				if(x2 > x1)
 					dx = 0.5;
 				else
 					dx = -0.5;
 				dy /= revs;
 				dz /= revs;
 			} else {
 				revs = dz * 2;
 				if(z2 > z1)
 					dz = 0.5;
 				else
 					dz = -0.5;
 				dx /= revs;
 				dy /= revs;
 			}
 		}
 		else if(ty > tz)
 		{
 			revs = ty * 2;
 			if(y2 > y1)
 				dy = 0.5;
 			else
 				dy = -0.5;
 			dx /= revs;
 			dz /= revs;
 		} else {
 			revs = tz * 2;
 			if(z2 > z1)
 				dz = 0.5;
 			else
 				dz = -0.5;
 			dx /= revs;
 			dy /= revs;
 		}
 		tx = x1;
 		ty = y1;
 		tz = z1;
 		cx1 = (int)tx + 10;
 		cy1 = 0;
 		cz1 = 0;
 		for(; revs > 1; revs--)
 		{
 			cx2 = (int) tx;
 			cy2 = (int) ty;
 			cz2 = (int) tz;
 			if((cx1 != cx2) || (cy1 != cy2) || (cz1 != cz2))
 			{
 				ret.add(cx2).add(cy2).add(cz2);
 				cx1 = cx2;
 				cy1 = cy2;
 				cz1 = cz2;
 			}
 			tx += dx;
 			ty += dy;
 			tz += tz;
 		}
 		return ret;
 	}
 
 	@Override
 	public ITexture registerTexture(String imgfile, int filex, int filey, int width, int height, int colorfilter, boolean transparent)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void addTriangle(Texel t1, Texel t2, Texel t3)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public int getLight(double x, double y, double z)
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int rndInt()
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public double rnd()
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
