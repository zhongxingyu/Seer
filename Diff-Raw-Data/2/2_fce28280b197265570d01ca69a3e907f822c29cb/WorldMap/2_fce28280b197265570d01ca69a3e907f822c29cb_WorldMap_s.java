 package com.game.world;
 
 import com.game.entity.Entity;
 
 public class WorldMap
 {
 	public static class Collumn
 	{
 		public byte[] blocks;
 		
 		public Collumn()
 		{
 			blocks = new byte[14];
 		}
 		
 		public Collumn(byte[] blocks)
 		{
 			this.blocks = blocks;
 		}
 	}
 	
 	private World world;
 	private Collumn[] list;
 	private int scroll;
 	
 	public WorldMap(World world)
 	{
 		this.world = world;
 		list = new Collumn[40];
 	}
 	
 	public Collumn getCollumn(int x)
 	{
 		x = x - scroll;
 		
 		if(x >= 0 & x < list.length)
 		{
 			return list[x];
 		}
 		return null;
 	}
 	
 	public boolean hittingEdge(int x)
 	{
 		x = x - scroll;
 		
 		return list.length - 2 < x;
 	}
 	
 	public void addNewCollumns(int cap, Collumn[] newCollumns, Entity[] entities)
 	{
 		Collumn[] newList = new Collumn[list.length - (cap - scroll) + newCollumns.length];
 		
 		for(int i = 0; i < newList.length; i++)
 		{
 			if(i < list.length - (cap - scroll))
 			{
 				newList[i] = list[i + (cap - scroll)];
 			}
 			else
 			{
 				newList[i] = newCollumns[i - (list.length - (cap - scroll))];
 			}
 		}
 		
 		for(int i = 0; i < entities.length; i++)
 		{
			entities[i].x += (list.length + (cap - scroll)) * 32;
 			world.addEntity(entities[i]);
 		}
 		
 		list = newList;
 		scroll = cap;
 	}
 }
